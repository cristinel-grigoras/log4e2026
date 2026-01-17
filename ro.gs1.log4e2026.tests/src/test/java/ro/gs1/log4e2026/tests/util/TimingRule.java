package ro.gs1.log4e2026.tests.util;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * JUnit Rule to automatically track test method timing.
 *
 * Usage:
 * <pre>
 * @Rule
 * public TimingRule timingRule = new TimingRule();
 * </pre>
 */
public class TimingRule extends TestWatcher {

    @Override
    protected void starting(Description description) {
        String testName = description.getClassName() + "." + description.getMethodName();
        TestTimingUtil.startTest(testName);
    }

    @Override
    protected void finished(Description description) {
        String testName = description.getClassName() + "." + description.getMethodName();
        TestTimingUtil.endTest(testName);
    }
}
