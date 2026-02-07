#!/bin/bash
# Run each UI test class individually and report results
# Usage: ./run-all-individual.sh

cd "$(dirname "$0")/../.."

TESTS=(
  Log4eContextMenuTest
  TemplateManagementUITest
  ProfileDefaultSelectionUITest
  SubstituteSystemOutTest
  AutoDeclareLoggerTest
  PreviewWizardTest
  Log4ePreferencePageUITest
  ExchangeFrameworkTest
  PreferencePagesTest
  LogThisClassTest
  PreferenceSaveTest
  CustomProfileSelectionTest
  ProjectContextMenuTest
  Log4eMenuUITest
  MenuVisibilityByCursorPositionTest
  Log4eScreenshotTest
  ProjectPreferencesTest
)

RESULTS_FILE="ro.gs1.log4e2026.tests/target/individual-results.txt"
mkdir -p ro.gs1.log4e2026.tests/target

echo "============================================" | tee "$RESULTS_FILE"
echo "Individual Test Run - $(date)" | tee -a "$RESULTS_FILE"
echo "============================================" | tee -a "$RESULTS_FILE"
echo "" | tee -a "$RESULTS_FILE"

PASS=0
FAIL=0
TOTAL=${#TESTS[@]}

for TEST in "${TESTS[@]}"; do
  echo ">>> Running $TEST ..." | tee -a "$RESULTS_FILE"
  START=$(date +%s)

  # Kill any leftover processes
  pkill -f "Xvfb :99" 2>/dev/null
  pkill -f "tycho-surefire" 2>/dev/null
  sleep 1

  # Run test
  bash ro.gs1.log4e2026.tests/tools/run-ui-tests.sh -n "$TEST" > "ro.gs1.log4e2026.tests/target/log-${TEST}.txt" 2>&1
  EXIT_CODE=$?

  END=$(date +%s)
  ELAPSED=$((END - START))

  # Extract test summary line
  SUMMARY=$(grep "^Tests run:" "ro.gs1.log4e2026.tests/target/log-${TEST}.txt" | grep "$TEST" | tail -1)

  if [ $EXIT_CODE -eq 0 ]; then
    STATUS="PASS"
    PASS=$((PASS + 1))
  else
    STATUS="FAIL"
    FAIL=$((FAIL + 1))
  fi

  echo "  $STATUS (${ELAPSED}s) $SUMMARY" | tee -a "$RESULTS_FILE"
  echo "" | tee -a "$RESULTS_FILE"
done

echo "============================================" | tee -a "$RESULTS_FILE"
echo "TOTAL: $TOTAL  PASS: $PASS  FAIL: $FAIL" | tee -a "$RESULTS_FILE"
echo "============================================" | tee -a "$RESULTS_FILE"
