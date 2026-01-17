package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.junit.After;

import ro.gs1.log4e2026.tests.util.TestTimingUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot UI tests for Log4E menu contributions.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class Log4eMenuUITest {

    private SWTWorkbenchBot bot;

    @BeforeClass
    public static void setUpClass() {
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
            // Ignore
        }
        // Ensure we have an active shell (setFocus doesn't timeout like activate)
        bot.shells()[0].setFocus();
    }

    @After
    public void tearDown() {
        // Close any open editors
        try {
            bot.closeAllEditors();
        } catch (Exception e) {
            // Ignore
        }
    }

    private void log(String step) {
        TestTimingUtil.log(bot, step);
    }

    @Test
    public void testLog4eMenuExists() {
        log("start");
        try {
            SWTBotMenu editMenu = bot.menu("Edit");
            log("after Edit menu");
            assertNotNull("Edit menu should exist", editMenu);
            SWTBotMenu log4eMenu = editMenu.menu("Log4E");
            log("after Log4E menu");
            assertNotNull("Log4E submenu should exist under Edit menu", log4eMenu);
        } catch (WidgetNotFoundException e) {
            fail("Log4E menu not found: " + e.getMessage());
        }
    }

    @Test
    public void testDeclareLoggerMenuItemExists() {
        log("start");
        try {
            SWTBotMenu log4eMenu = bot.menu("Edit").menu("Log4E");
            log("after Log4E menu");
            SWTBotMenu declareLoggerItem = log4eMenu.menu("Declare Logger");
            log("after Declare Logger");
            assertNotNull("Declare Logger menu item should exist", declareLoggerItem);
        } catch (WidgetNotFoundException e) {
            fail("Declare Logger menu item not found: " + e.getMessage());
        }
    }

    @Test
    public void testInsertLogStatementMenuItemExists() {
        log("start");
        try {
            SWTBotMenu log4eMenu = bot.menu("Edit").menu("Log4E");
            log("after Log4E menu");
            SWTBotMenu insertLogItem = log4eMenu.menu("Insert Log Statement");
            log("after Insert Log Statement");
            assertNotNull("Insert Log Statement menu item should exist", insertLogItem);
        } catch (WidgetNotFoundException e) {
            fail("Insert Log Statement menu item not found: " + e.getMessage());
        }
    }

    @Test
    public void testMenuItemsAreAccessible() {
        log("start");
        try {
            SWTBotMenu log4eMenu = bot.menu("Edit").menu("Log4E");
            log("after Log4E menu");
            assertNotNull("Log4E menu accessible", log4eMenu);
            assertNotNull("Declare Logger exists", log4eMenu.menu("Declare Logger"));
            assertNotNull("Insert Log Statement exists", log4eMenu.menu("Insert Log Statement"));
            log("all menu items verified");
        } catch (WidgetNotFoundException e) {
            fail("Menu items not accessible: " + e.getMessage());
        }
    }
}
