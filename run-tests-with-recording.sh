#!/bin/bash
# Run UI tests with video recording

set -e

DISPLAY_NUM=99
export DISPLAY=:${DISPLAY_NUM}
SCREEN_SIZE="1280x1024x24"
VIDEO_DIR="ro.gs1.log4e2026.tests/target/screenshots"
VIDEO_FILE="${VIDEO_DIR}/test-recording-$(date +%Y%m%d_%H%M%S).mp4"

# Create output directory
mkdir -p "${VIDEO_DIR}"

# Start Xvfb
echo "Starting Xvfb on display :${DISPLAY_NUM}..."
Xvfb :${DISPLAY_NUM} -screen 0 ${SCREEN_SIZE} &
XVFB_PID=$!
sleep 1

# Start recording
echo "Starting video recording to ${VIDEO_FILE}..."
ffmpeg -y -f x11grab -video_size 1280x1024 -i :${DISPLAY_NUM} -codec:v libx264 -preset ultrafast -crf 25 "${VIDEO_FILE}" &
FFMPEG_PID=$!
sleep 1

# Cleanup function
cleanup() {
    echo "Stopping recording..."
    kill -SIGINT ${FFMPEG_PID} 2>/dev/null || true
    sleep 2
    kill ${XVFB_PID} 2>/dev/null || true
    echo "Recording saved to: ${VIDEO_FILE}"
}
trap cleanup EXIT

# Run Maven tests
echo "Running tests..."
mvn verify -pl ro.gs1.log4e2026.tests -am 2>&1 | tee /tmp/mvn-output.log

echo "Tests completed."
