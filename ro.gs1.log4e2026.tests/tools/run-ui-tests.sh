#!/bin/bash
# Run UI tests with Xvfb for headless execution and screenshot capture
# Optional video recording with -r flag

set -e

# Parse arguments
RECORD_VIDEO=false
while getopts "r" opt; do
    case $opt in
        r) RECORD_VIDEO=true ;;
    esac
done
shift $((OPTIND-1))

# Navigate to project root (parent of tools folder)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
WORKSPACE_DIR="$(cd "$PROJECT_DIR/.." && pwd)"

cd "$WORKSPACE_DIR"

# Configuration
DISPLAY_NUM=99
SCREEN_SIZE="1100x940x24"
SCREEN_WIDTH=1100
SCREEN_HEIGHT=940
SCREENSHOT_DIR="$PROJECT_DIR/target/screenshots"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
LOG_FILE="$PROJECT_DIR/target/ui-test-${TIMESTAMP}.log"
VIDEO_FILE="$PROJECT_DIR/target/ui-test-${TIMESTAMP}.mkv"
FFMPEG_PID=""

# Create directories
mkdir -p "$SCREENSHOT_DIR"
mkdir -p "$(dirname "$LOG_FILE")"

# Check if Xvfb is running on the display
if ! xdpyinfo -display :$DISPLAY_NUM > /dev/null 2>&1; then
    echo "Starting Xvfb on display :$DISPLAY_NUM with size $SCREEN_SIZE..."
    Xvfb :$DISPLAY_NUM -screen 0 $SCREEN_SIZE &
    XVFB_PID=$!
    sleep 2

    # Verify Xvfb started
    if ! xdpyinfo -display :$DISPLAY_NUM > /dev/null 2>&1; then
        echo "ERROR: Failed to start Xvfb"
        exit 1
    fi
    echo "Xvfb started with PID $XVFB_PID"
else
    echo "Xvfb already running on display :$DISPLAY_NUM"
    XVFB_PID=""
fi

# Set display
export DISPLAY=:$DISPLAY_NUM

# Function to cleanup
cleanup() {
    if [ -n "$FFMPEG_PID" ]; then
        echo "Stopping video recording..."
        kill -INT $FFMPEG_PID 2>/dev/null || true
        sleep 2
        kill $FFMPEG_PID 2>/dev/null || true
    fi
    if [ -n "$XVFB_PID" ]; then
        echo "Stopping Xvfb..."
        kill $XVFB_PID 2>/dev/null || true
    fi
}
trap cleanup EXIT

# Start video recording if requested
if [ "$RECORD_VIDEO" = true ]; then
    if command -v ffmpeg &> /dev/null; then
        echo "Starting video recording at 5 fps to: $VIDEO_FILE"
        ffmpeg -y -f x11grab -framerate 5 -video_size ${SCREEN_WIDTH}x${SCREEN_HEIGHT} \
            -i :${DISPLAY_NUM} -c:v libx264 -preset ultrafast -crf 23 \
            -f matroska "$VIDEO_FILE" </dev/null >/dev/null 2>&1 &
        FFMPEG_PID=$!
        sleep 1
        echo "Video recording started with PID $FFMPEG_PID"
    else
        echo "WARNING: ffmpeg not found, video recording disabled"
    fi
fi

echo "Running UI tests with DISPLAY=$DISPLAY..."
echo "Screenshots will be saved to: $SCREENSHOT_DIR"
echo "Maven output logged to: $LOG_FILE"
echo ""

# Run Maven with specific test class or all tests
if [ -n "$1" ]; then
    TEST_CLASS="$1"
    echo "Running specific test: $TEST_CLASS"
    mvn verify -pl ro.gs1.log4e2026.tests -am \
        -Dtest="$TEST_CLASS" \
        -DfailIfNoTests=false \
        -Dtycho.testArgLine="-Xms512m -Xmx1024m" \
        2>&1 | tee "$LOG_FILE"
else
    echo "Running all UI tests..."
    mvn verify -pl ro.gs1.log4e2026.tests -am \
        -DfailIfNoTests=false \
        -Dtycho.testArgLine="-Xms512m -Xmx1024m" \
        2>&1 | tee "$LOG_FILE"
fi

MVN_EXIT_CODE=${PIPESTATUS[0]}

# Report results
echo ""
echo "=== Test Results ==="
echo "Maven exit code: $MVN_EXIT_CODE"
echo "Full output saved to: $LOG_FILE"

if [ -d "$SCREENSHOT_DIR" ]; then
    SCREENSHOT_COUNT=$(find "$SCREENSHOT_DIR" -name "*.png" 2>/dev/null | wc -l)
    echo "Screenshots captured: $SCREENSHOT_COUNT"
    find "$SCREENSHOT_DIR" -name "*.png" -printf "  %f\n" 2>/dev/null || true
fi

if [ -f "$VIDEO_FILE" ]; then
    VIDEO_SIZE=$(du -h "$VIDEO_FILE" | cut -f1)
    echo "Video recording: $VIDEO_FILE ($VIDEO_SIZE)"
    echo "Play with: mpv $VIDEO_FILE"
fi

echo ""
echo "Test reports: $PROJECT_DIR/target/surefire-reports/"
echo ""
echo "To view log: cat $LOG_FILE"
echo "To tail log: tail -f $LOG_FILE"

exit $MVN_EXIT_CODE
