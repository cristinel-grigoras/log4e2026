package ro.gs1.log4e2026.tests.ui;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite for all Log4E UI tests.
 */
@RunWith(Suite.class)
@SuiteClasses({
    Log4ePreferencePageUITest.class,
    Log4eMenuUITest.class,
    TemplateManagementUITest.class,
    ProfileDefaultSelectionUITest.class
})
public class AllUITests {
}
