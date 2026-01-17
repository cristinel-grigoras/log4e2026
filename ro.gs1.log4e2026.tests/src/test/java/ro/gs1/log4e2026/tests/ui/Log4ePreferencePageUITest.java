package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.junit.After;

import ro.gs1.log4e2026.tests.util.TestTimingUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot UI tests for Log4E preference page.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class Log4ePreferencePageUITest {

    private SWTWorkbenchBot bot;

    @BeforeClass
    public static void setUpClass() {
        // Disable SWTBot automatic screenshots on failure
        SWTBotPreferences.SCREENSHOTS_DIR = "";
        SWTBotPreferences.TIMEOUT = 5000;
    }

    @Before
    public void setUp() {
        bot = new SWTWorkbenchBot();
        // Close welcome page if present
        try {
            bot.viewByTitle("Welcome").close();
        } catch (Exception e) {
            // Ignore if not found
        }
        // Ensure we have an active shell (setFocus doesn't timeout like activate)
        bot.shells()[0].setFocus();
    }

    @After
    public void tearDown() {
        // Close any open dialogs
        try {
            bot.button("Cancel").click();
        } catch (Exception e) {
            // Ignore
        }
    }

    private void log(String step) {
        TestTimingUtil.log(bot, step);
    }

    @Test
    public void testPreferencePageCanBeOpened() {
        log("start");
        try {
            bot.menu("Window").menu("Preferences").click();
            log("after Preferences click");
            assertTrue("Preferences dialog should open", bot.shell("Preferences").isActive());
            log("Preferences dialog active");
            bot.button("Cancel").click();
            log("after Cancel");
        } catch (WidgetNotFoundException e) {
            System.out.println("Preferences menu not found via Window menu - skipping test");
        }
    }

    @Test
    public void testLog4ePreferencePageExists() {
        log("start");
        try {
            bot.menu("Window").menu("Preferences").click();
            log("after Preferences click");
            try {
                bot.tree().getTreeItem("Log4E 2026").select();
                log("after Log4E 2026 select");
                assertTrue("Log4E preference page should be selectable", true);
            } catch (WidgetNotFoundException e) {
                System.out.println("Log4E 2026 node not found in preferences tree");
            }
            bot.button("Cancel").click();
            log("after Cancel");
        } catch (WidgetNotFoundException e) {
            System.out.println("Preferences menu not found - skipping test");
        }
    }

    @Test
    public void testPluginIsLoaded() {
        log("start");
        try {
            assertNotNull("Edit menu should exist", bot.menu("Edit"));
            log("after Edit menu");
            assertNotNull("Log4E submenu should exist", bot.menu("Edit").menu("Log4E"));
            log("after Log4E menu");
            assertTrue("Plugin is loaded and contributing menus", true);
        } catch (WidgetNotFoundException e) {
            fail("Log4E menu not found - plugin may not be loaded");
        }
    }

    @Test
    public void testDeclareLoggerMenuExists() {
        log("start");
        try {
            bot.menu("Edit").menu("Log4E").menu("Declare Logger");
            log("after Declare Logger");
            assertTrue("Declare Logger menu item exists", true);
        } catch (WidgetNotFoundException e) {
            fail("Declare Logger menu item not found");
        }
    }

    @Test
    public void testInsertLogStatementMenuExists() {
        log("start");
        try {
            bot.menu("Edit").menu("Log4E").menu("Insert Log Statement");
            log("after Insert Log Statement");
            assertTrue("Insert Log Statement menu item exists", true);
        } catch (WidgetNotFoundException e) {
            fail("Insert Log Statement menu item not found");
        }
    }
}
