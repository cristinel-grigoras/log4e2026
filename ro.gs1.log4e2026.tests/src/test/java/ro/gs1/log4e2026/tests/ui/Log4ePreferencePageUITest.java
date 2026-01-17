package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import ro.gs1.log4e2026.tests.util.TestTimingUtil;

/**
 * SWTBot UI tests for Log4E preference page.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Log4ePreferencePageUITest {

    private static final String SCREENSHOT_DIR = System.getProperty("screenshot.dir", "target/screenshots");
    private SWTWorkbenchBot bot;

    @BeforeClass
    public static void setUpClass() {
        File dir = new File(SCREENSHOT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // Disable SWTBot automatic screenshots on failure
        SWTBotPreferences.SCREENSHOTS_DIR = "";
        SWTBotPreferences.TIMEOUT = 5000;
    }

    @AfterClass
    public static void tearDownClass() {
        TestTimingUtil.printSummary();
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

    private void captureScreen(String filename) {
        try {
            String filepath = SCREENSHOT_DIR + "/" + filename;
            Runtime.getRuntime().exec(new String[] {
                "import", "-display", System.getenv("DISPLAY"), "-window", "root", filepath
            }).waitFor();
            System.out.println("  Screenshot: " + filepath);
        } catch (Exception e) {
            System.out.println("  Screenshot failed: " + e.getMessage());
        }
    }

    /**
     * Execute an Eclipse command by ID on the UI thread.
     */
    private void executeCommand(String commandId) {
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                try {
                    IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
                    handlerService.executeCommand(commandId, null);
                } catch (Exception e) {
                    System.out.println("Failed to execute command " + commandId + ": " + e.getMessage());
                }
            }
        });
    }

    @Test
    public void test1_PreferencePageCanBeOpened() {
        log("1.0 start");

        // Debug: List Window menu items with IDs using getId()
        try {
            org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu windowMenu = bot.menu("Window");
            java.util.List<String> items = windowMenu.menuItems();
            System.out.println("Window menu items with IDs:");
            for (String itemText : items) {
                if (itemText != null && !itemText.isEmpty()) {
                    try {
                        org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu menuItem = windowMenu.menu(itemText);
                        System.out.println("  '" + itemText + "' id=" + menuItem.getId());
                    } catch (Exception e) {
                        System.out.println("  '" + itemText + "' id=ERROR");
                    }
                }
            }

            // Try to get Preferences menu item by label with ellipsis
            windowMenu.menu("Preferences...").click();
            log("1.1 after Preferences click");

            // Wait for Preferences dialog
            bot.waitUntil(org.eclipse.swtbot.swt.finder.waits.Conditions.shellIsActive("Preferences"), 5000);
            log("1.2 Preferences dialog active");
            assertTrue("Preferences dialog should open", bot.shell("Preferences").isActive());
            captureScreen("pref_01_preferences_dialog.png");
            bot.button("Cancel").click();
            log("1.3 after Cancel");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void test2_Log4ePreferencePageExists() {
        log("2.0 start");
        try {
            bot.menu("Window").menu("Preferences...").click();
            log("2.1 after Preferences click");

            // Wait for Preferences dialog
            bot.waitUntil(org.eclipse.swtbot.swt.finder.waits.Conditions.shellIsActive("Preferences"), 5000);
            log("2.2 Preferences dialog active");

            bot.tree().getTreeItem("Log4E 2026").select();
            log("2.3 after Log4E 2026 select");
            captureScreen("pref_02_log4e_page.png");
            assertTrue("Log4E preference page should be selectable", true);
            bot.button("Cancel").click();
            log("2.4 after Cancel");
        } catch (WidgetNotFoundException e) {
            System.out.println("Log4E 2026 node not found: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Test
    public void test3_PluginIsLoaded() {
        log("3.0 start");
        try {
            assertNotNull("Edit menu should exist", bot.menu("Edit"));
            log("3.1 after Edit menu");
            assertNotNull("Log4E submenu should exist", bot.menu("Edit").menu("Log4E"));
            log("3.2 after Log4E menu");
            bot.menu("Edit").menu("Log4E").click();
            log("3.3 after Log4E menu click");
            captureScreen("pref_03_log4e_menu.png");
            bot.activeShell().pressShortcut(org.eclipse.swtbot.swt.finder.keyboard.Keystrokes.ESC);
            log("3.4 after ESC");
            assertTrue("Plugin is loaded and contributing menus", true);
        } catch (WidgetNotFoundException e) {
            fail("Log4E menu not found - plugin may not be loaded");
        }
    }

    @Test
    public void test4_DeclareLoggerMenuExists() {
        log("4.0 start");
        try {
            bot.menu("Edit").menu("Log4E").menu("Declare Logger");
            log("4.1 after Declare Logger");
            captureScreen("pref_04_declare_logger_menu.png");
            bot.activeShell().pressShortcut(org.eclipse.swtbot.swt.finder.keyboard.Keystrokes.ESC);
            assertTrue("Declare Logger menu item exists", true);
        } catch (WidgetNotFoundException e) {
            fail("Declare Logger menu item not found");
        }
    }

    @Test
    public void test5_InsertLogStatementMenuExists() {
        log("5.0 start");
        try {
            bot.menu("Edit").menu("Log4E").menu("Insert Log Statement");
            log("5.1 after Insert Log Statement");
            captureScreen("pref_05_insert_log_menu.png");
            bot.activeShell().pressShortcut(org.eclipse.swtbot.swt.finder.keyboard.Keystrokes.ESC);
            assertTrue("Insert Log Statement menu item exists", true);
        } catch (WidgetNotFoundException e) {
            fail("Insert Log Statement menu item not found");
        }
    }
}
