package ro.gs1.log4e2026.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite for all Log4E unit tests.
 */
@RunWith(Suite.class)
@SuiteClasses({
    StringUtilTest.class,
    BeanUtilTest.class,
    LoggerTemplatesTest.class
})
public class AllTests {
}
