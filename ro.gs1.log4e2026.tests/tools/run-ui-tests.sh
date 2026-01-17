#!/bin/bash
# Run UI tests with Xvfb for headless execution and screenshot capture
# Following IMPLEMENTATION_PLAN.md requirements:
#   - Remove old test results, mvn clean
#   - Use current maven target folder, not global /tmp
#   - Check for old xvfb/surefire processes and remove them
#   - Generate unique run ID for all generated files
#   - Offer choice to rerun in current display if too slow
#   - Capture screen if test takes too long

set -e

# Parse arguments
RECORD_VIDEO=false
USE_CURRENT_DISPLAY=false
CLEAN_BUILD=true
SPECIFIC_TEST=""

print_usage() {
    echo "Usage: $0 [OPTIONS] [TestClass]"
    echo ""
    echo "Options:"
    echo "  -r          Record video of test run"
    echo "  -d          Use current display (not Xvfb) for debugging"
    echo "  -n          No clean (skip mvn clean)"
    echo "  -h          Show this help"
    echo ""
    echo "Examples:"
    echo "  $0                              # Run all tests with clean"
    echo "  $0 -r                           # Run all tests with video recording"
    echo "  $0 -d                           # Run in current display for debugging"
    echo "  $0 Log4eContextMenuTest         # Run specific test class"
    echo "  $0 -n Log4eContextMenuTest      # Run specific test without clean"
}

while getopts "rdnh" opt; do
    case $opt in
        r) RECORD_VIDEO=true ;;
        d) USE_CURRENT_DISPLAY=true ;;
        n) CLEAN_BUILD=false ;;
        h) print_usage; exit 0 ;;
        *) print_usage; exit 1 ;;
    esac
done
shift $((OPTIND-1))

# Optional test class argument
SPECIFIC_TEST="$1"

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
WORKSPACE_DIR="$(cd "$PROJECT_DIR/.." && pwd)"

cd "$WORKSPACE_DIR"

# Generate unique run ID (base32 seconds - 8 chars, sortable)
RUN_ID=$(printf '%010x' $(date +%s) | xxd -r -p | base32 | tr -d '=' | tr 'A-Z' 'a-z')

# Configuration - use target folder, not /tmp
DISPLAY_NUM=99
SCREEN_SIZE="1280x1024x24"
SCREEN_WIDTH=1280
SCREEN_HEIGHT=1024
TARGET_DIR="$PROJECT_DIR/target"
SCREENSHOT_DIR="$TARGET_DIR/screenshots-${RUN_ID}"
LOG_FILE="$TARGET_DIR/ui-test-${RUN_ID}.log"
VIDEO_FILE="$TARGET_DIR/ui-test-${RUN_ID}.mkv"
TIMING_FILE="$TARGET_DIR/timing-${RUN_ID}.log"
FFMPEG_PID=""
XVFB_PID=""
MONITOR_PID=""

# ============================================
# Step 1: Kill old lingering processes (before any output redirect)
# ============================================
echo "Checking for old xvfb/surefire processes..."

# Kill old Xvfb processes on our display
OLD_XVFB=$(pgrep -f "Xvfb :${DISPLAY_NUM}" 2>/dev/null || true)
if [ -n "$OLD_XVFB" ]; then
    echo "  Killing old Xvfb processes: $OLD_XVFB"
    kill $OLD_XVFB 2>/dev/null || true
    sleep 1
fi

# Kill old surefire processes that might be stuck
OLD_SUREFIRE=$(pgrep -f "tycho-surefire" 2>/dev/null || true)
if [ -n "$OLD_SUREFIRE" ]; then
    echo "  Killing old surefire processes: $OLD_SUREFIRE"
    kill $OLD_SUREFIRE 2>/dev/null || true
    sleep 1
fi

# Kill any old ffmpeg recording on our display
OLD_FFMPEG=$(pgrep -f "ffmpeg.*:${DISPLAY_NUM}" 2>/dev/null || true)
if [ -n "$OLD_FFMPEG" ]; then
    echo "  Killing old ffmpeg processes: $OLD_FFMPEG"
    kill $OLD_FFMPEG 2>/dev/null || true
    sleep 1
fi

echo "  Done cleaning old processes."
echo ""

# ============================================
# Step 2: Clean old test results
# ============================================
if [ "$CLEAN_BUILD" = true ]; then
    echo "Cleaning old test results..."
    mvn clean -pl ro.gs1.log4e2026.tests -q || true
    echo "  Done."
    echo ""
fi

# ============================================
# Step 3: Create directories and setup logging (AFTER clean)
# ============================================
mkdir -p "$SCREENSHOT_DIR"
mkdir -p "$TARGET_DIR"

# Redirect ALL output to log file (and console)
exec > >(tee -a "$LOG_FILE") 2>&1

echo "=============================================="
echo "Run ID: $RUN_ID"
echo "Log file: $LOG_FILE"
echo "=============================================="
echo ""
echo "Configuration:"
echo "  Workspace:    $WORKSPACE_DIR"
echo "  Target dir:   $TARGET_DIR"
echo "  Screenshots:  $SCREENSHOT_DIR"
echo ""

# ============================================
# Step 4: Setup display
# ============================================
if [ "$USE_CURRENT_DISPLAY" = true ]; then
    echo "Using current display: $DISPLAY"
    # Keep current DISPLAY
else
    echo "Starting Xvfb on display :$DISPLAY_NUM with size $SCREEN_SIZE..."
    Xvfb :$DISPLAY_NUM -screen 0 $SCREEN_SIZE -nolisten tcp &
    XVFB_PID=$!
    sleep 2

    # Verify Xvfb started
    if ! xdpyinfo -display :$DISPLAY_NUM > /dev/null 2>&1; then
        echo "ERROR: Failed to start Xvfb"
        exit 1
    fi
    echo "  Xvfb started with PID $XVFB_PID"
    export DISPLAY=:$DISPLAY_NUM
fi

echo ""

# ============================================
# Cleanup function
# ============================================
cleanup() {
    echo ""
    echo "Cleaning up..."

    if [ -n "$MONITOR_PID" ]; then
        kill $MONITOR_PID 2>/dev/null || true
    fi

    if [ -n "$FFMPEG_PID" ]; then
        echo "  Stopping video recording..."
        kill -INT $FFMPEG_PID 2>/dev/null || true
        sleep 2
        kill $FFMPEG_PID 2>/dev/null || true
    fi

    if [ -n "$XVFB_PID" ]; then
        echo "  Stopping Xvfb..."
        kill $XVFB_PID 2>/dev/null || true
    fi

    echo "  Done."
}
trap cleanup EXIT

# ============================================
# Step 5: Start video recording if requested
# ============================================
if [ "$RECORD_VIDEO" = true ]; then
    if command -v ffmpeg &> /dev/null; then
        echo "Starting video recording at 5 fps to: $VIDEO_FILE"
        ffmpeg -y -f x11grab -framerate 5 -video_size ${SCREEN_WIDTH}x${SCREEN_HEIGHT} \
            -i :${DISPLAY_NUM} -c:v libx264 -preset ultrafast -crf 23 \
            -f matroska "$VIDEO_FILE" </dev/null >/dev/null 2>&1 &
        FFMPEG_PID=$!
        sleep 1
        echo "  Video recording started with PID $FFMPEG_PID"
    else
        echo "WARNING: ffmpeg not found, video recording disabled"
    fi
    echo ""
fi

# ============================================
# Step 6: Start timeout monitor (capture screen if too slow)
# ============================================
TEST_TIMEOUT=300  # 5 minutes per test class
CAPTURE_INTERVAL=60  # Capture every 60 seconds if running long

monitor_test() {
    local start_time=$(date +%s)
    local capture_count=0

    while true; do
        sleep $CAPTURE_INTERVAL
        local elapsed=$(($(date +%s) - start_time))

        if [ $elapsed -gt $TEST_TIMEOUT ]; then
            echo "[MONITOR] Test running too long ($elapsed seconds)"
            # Capture screen
            capture_count=$((capture_count + 1))
            local capture_file="$SCREENSHOT_DIR/${RUN_ID}_timeout_${capture_count}.png"
            import -display :$DISPLAY_NUM -window root "$capture_file" 2>/dev/null || true
            echo "[MONITOR] Captured: $capture_file"

            # After 2 captures, offer to switch to current display
            if [ $capture_count -ge 2 ]; then
                echo ""
                echo "=============================================="
                echo "TEST TAKING TOO LONG!"
                echo "Consider rerunning with -d flag to use current display:"
                echo "  $0 -d $SPECIFIC_TEST"
                echo "=============================================="
            fi
        fi
    done
}

# Start monitor in background
monitor_test &
MONITOR_PID=$!

# ============================================
# Step 7: Run Maven tests
# ============================================
echo "Running UI tests with DISPLAY=$DISPLAY..."
echo "Run ID: $RUN_ID"
echo "Screenshots prefix: $RUN_ID"
echo "Log file: $LOG_FILE"
echo ""

MVN_ARGS=(
    verify
    -pl ro.gs1.log4e2026.tests
    -am
    -DfailIfNoTests=false
    -Dscreenshot.dir="${SCREENSHOT_DIR}"
)

if [ -n "$SPECIFIC_TEST" ]; then
    echo "Running specific test: $SPECIFIC_TEST"
    MVN_ARGS+=(-Dtest="$SPECIFIC_TEST")
else
    echo "Running all UI tests..."
fi

# Run maven (output already redirected via exec)
mvn "${MVN_ARGS[@]}"
MVN_EXIT_CODE=$?

# Stop the monitor
kill $MONITOR_PID 2>/dev/null || true
MONITOR_PID=""

# ============================================
# Step 8: Report results
# ============================================
echo ""
echo "=============================================="
echo "TEST RESULTS - Run ID: $RUN_ID"
echo "=============================================="
echo "Maven exit code: $MVN_EXIT_CODE"
echo ""

# Count and list screenshots
if [ -d "$SCREENSHOT_DIR" ]; then
    SCREENSHOT_COUNT=$(find "$SCREENSHOT_DIR" -name "${RUN_ID}*.png" 2>/dev/null | wc -l)
    TOTAL_SCREENSHOTS=$(find "$SCREENSHOT_DIR" -name "*.png" 2>/dev/null | wc -l)
    echo "Screenshots this run: $SCREENSHOT_COUNT"
    echo "Total screenshots: $TOTAL_SCREENSHOTS"
    if [ $SCREENSHOT_COUNT -gt 0 ]; then
        echo "Screenshots from this run:"
        find "$SCREENSHOT_DIR" -name "${RUN_ID}*.png" -printf "  %f\n" 2>/dev/null | head -20
    fi
fi
echo ""

# Show video info
if [ -f "$VIDEO_FILE" ]; then
    VIDEO_SIZE=$(du -h "$VIDEO_FILE" | cut -f1)
    echo "Video recording: $VIDEO_FILE ($VIDEO_SIZE)"
    echo "  Play with: mpv $VIDEO_FILE"
fi
echo ""

# Show test summary from surefire reports
echo "Test reports: $TARGET_DIR/surefire-reports/"
if [ -d "$TARGET_DIR/surefire-reports" ]; then
    echo ""
    echo "Test Summary:"
    grep -h "Tests run:" "$TARGET_DIR/surefire-reports"/*.txt 2>/dev/null | head -10 || true
fi
echo ""

# Show helpful commands
echo "Useful commands:"
echo "  View log:        cat $LOG_FILE"
echo "  Tail log:        tail -f $LOG_FILE"
echo "  View screenshots: ls -la $SCREENSHOT_DIR/"
if [ "$USE_CURRENT_DISPLAY" = false ]; then
    echo "  Debug mode:      $0 -d $SPECIFIC_TEST"
fi
echo ""

exit $MVN_EXIT_CODE
