package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import ro.gs1.log4e2026.tests.util.TestTimingUtil;
import ro.gs1.log4e2026.tests.util.TimingRule;

/**
 * Comprehensive tests for all Log4E preference pages.
 * Tests verify that preference pages are accessible and contain expected fields.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PreferencePagesTest {

    private static final String SCREENSHOT_DIR = System.getProperty("screenshot.dir", "target/screenshots");
    private static SWTWorkbenchBot bot;
    private static boolean preferencesOpen = false;

    @Rule
    public TimingRule timingRule = new TimingRule();

    @BeforeClass
    public static void setUpClass() {
        // Reduce delays for faster test execution
        SWTBotPreferences.PLAYBACK_DELAY = 0;   // No delay between actions
        SWTBotPreferences.TIMEOUT = 5000;       // 5 seconds timeout
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        SWTBotPreferences.TYPE_INTERVAL = 0;    // No delay between keystrokes

        File dir = new File(SCREENSHOT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // Disable automatic failure screenshots (we use custom screenshots)
        SWTBotPreferences.SCREENSHOTS_DIR = "";

        bot = new SWTWorkbenchBot();

        // Close welcome view if present
        try {
            bot.viewByTitle("Welcome").close();
        } catch (Exception e) {
            // Ignore
        }
    }

    @AfterClass
    public static void tearDownClass() {
        closePreferences();
        TestTimingUtil.printSummary();
    }

    @Before
    public void setUp() throws Exception {
        if (!preferencesOpen) {
            openPreferences();
        }
    }

    @After
    public void afterTest() {
        // Keep preferences open between tests for speed
    }

    private static void openPreferences() throws Exception {
        TestTimingUtil.log(bot, "openPreferences start");
        // Ensure we have an active shell
        TestTimingUtil.focusWorkbenchShell(bot);
        TestTimingUtil.log(bot, "after setFocus");

        // Use menu click instead of keyboard shortcuts (more reliable)
        bot.menu("Window").menu("Preferences...").click();
        TestTimingUtil.log(bot, "after menu click");

        // Wait for Preferences dialog to appear
        bot.waitUntil(Conditions.shellIsActive("Preferences"), 5000);
        TestTimingUtil.log(bot, "Preferences open");
        preferencesOpen = true;
    }

    private static void closePreferences() {
        if (preferencesOpen) {
            try {
                bot.button("Cancel").click();
                preferencesOpen = false;
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private void selectPreferencePage(String... path) throws Exception {
        log("selectPreferencePage: " + String.join("/", path));
        SWTBotTree tree = bot.tree();
        SWTBotTreeItem item = tree.getTreeItem(path[0]);

        for (int i = 1; i < path.length; i++) {
            item.expand();
            TestTimingUtil.waitUntil(bot, Conditions.treeItemHasNode(item, path[i]));
            item = item.getNode(path[i]);
        }
        item.select();
        log("after select " + path[path.length - 1]);
    }

    private void log(String step) {
        TestTimingUtil.log(bot, step);
    }

    private void captureScreenshot(String name) {
        try {
            // Ensure directory exists
            File dir = new File(SCREENSHOT_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String filename = SCREENSHOT_DIR + "/" + name + ".png";
            // Use import with explicit PNG format
            ProcessBuilder pb = new ProcessBuilder("import", "-window", "root", "PNG:" + filename);
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
            System.out.println("Screenshot: " + filename);
        } catch (Exception e) {
            System.out.println("Screenshot failed: " + e.getMessage());
        }
    }

    @Test
    public void test01_MainPreferencePage() throws Exception {
        log("start");
        selectPreferencePage("Log4E 2026");
        log("after select Log4E 2026");

        assertNotNull("Logging Framework combo should exist",
            bot.comboBoxWithLabel("Logging Framework:"));
        assertNotNull("Logger Variable Name field should exist",
            bot.textWithLabel("Logger Variable Name:"));
        log("after assertions");

        captureScreenshot("01_main_page");
    }

    @Test
    public void test02_DeclarationPreferencePage() throws Exception {
        log("start");
        selectPreferencePage("Log4E 2026", "Declaration");
        log("after select Declaration");

        assertNotNull("Automatic imports checkbox should exist",
            bot.checkBox("Automatically add imports when declaring logger"));
        assertNotNull("Logger name field should exist",
            bot.textWithLabel("Logger variable name:"));
        assertNotNull("Static checkbox should exist",
            bot.checkBox("Declare logger as static"));
        assertNotNull("Final checkbox should exist",
            bot.checkBox("Declare logger as final"));
        log("after assertions");

        captureScreenshot("02_declaration_page");
    }

    @Test
    public void test03_StatementsPreferencePage() throws Exception {
        log("start");
        selectPreferencePage("Log4E 2026", "Statements");
        log("after select Statements");

        assertNotNull("TRACE enabled checkbox should exist",
            bot.checkBox("Enable TRACE level"));
        assertNotNull("DEBUG enabled checkbox should exist",
            bot.checkBox("Enable DEBUG level"));
        assertNotNull("INFO enabled checkbox should exist",
            bot.checkBox("Enable INFO level"));
        assertNotNull("WARN enabled checkbox should exist",
            bot.checkBox("Enable WARN level"));
        assertNotNull("ERROR enabled checkbox should exist",
            bot.checkBox("Enable ERROR level"));
        log("after assertions");

        captureScreenshot("03_statements_page");
    }

    @Test
    public void test04_PositionPreferencePage() throws Exception {
        log("start");
        selectPreferencePage("Log4E 2026", "Position");
        log("after select Position");

        assertNotNull("START enabled checkbox should exist",
            bot.checkBox("Enable log statements at method START"));
        assertNotNull("END enabled checkbox should exist",
            bot.checkBox("Enable log statements at method END"));
        assertNotNull("CATCH enabled checkbox should exist",
            bot.checkBox("Enable log statements in CATCH blocks"));
        log("after assertions");

        captureScreenshot("04_position_page");
    }

    @Test
    public void test05_FormatPreferencePage() throws Exception {
        log("start");
        selectPreferencePage("Log4E 2026", "Format");
        log("after select Format");

        assertNotNull("Delimiter field should exist",
            bot.textWithLabel("General delimiter:"));
        assertNotNull("Placeholder field should exist",
            bot.textWithLabel("Variable placeholder (e.g., {} for SLF4J):"));
        log("after assertions");

        captureScreenshot("05_format_page");
    }

    @Test
    public void test06_ReplaceUIPreferencePage() throws Exception {
        log("start");
        selectPreferencePage("Log4E 2026", "Replace & UI");
        log("after select Replace & UI");

        assertNotNull("System.out replacement checkbox should exist",
            bot.checkBox("Enable System.out replacement"));
        assertNotNull("System.err replacement checkbox should exist",
            bot.checkBox("Enable System.err replacement"));
        assertNotNull("Success dialog checkbox should exist",
            bot.checkBox("Show success dialog after operations"));
        log("after assertions");

        captureScreenshot("06_replace_ui_page");
    }

    @Test
    public void test07_NavigateAllPages() throws Exception {
        log("start");
        String[] pages = {"Declaration", "Statements", "Position", "Format", "Replace & UI"};

        for (String page : pages) {
            selectPreferencePage("Log4E 2026", page);
            log("after select " + page);
        }

        selectPreferencePage("Log4E 2026");
        log("after return to main");
        assertTrue("Navigation through all pages completed", true);
    }

    @Test
    public void test08_DefaultsButton() throws Exception {
        log("start");
        selectPreferencePage("Log4E 2026");
        log("after select Log4E 2026");

        assertNotNull("Restore Defaults button should exist",
            bot.button("Restore Defaults"));
        log("after assertion");
    }

    @Test
    public void test09_ApplyButton() throws Exception {
        log("start");
        selectPreferencePage("Log4E 2026");
        log("after select Log4E 2026");

        assertNotNull("Apply button should exist",
            bot.button("Apply"));
        log("after assertion");
    }

    @Test
    public void test10_ExpandCollapseTree() throws Exception {
        log("start");
        SWTBotTree tree = bot.tree();
        SWTBotTreeItem log4eItem = tree.getTreeItem("Log4E 2026");
        log("after get Log4E item");

        log4eItem.expand();
        TestTimingUtil.waitUntil(bot, Conditions.treeItemHasNode(log4eItem, "Declaration"));
        log("after expand");
        assertTrue("Log4E node should be expanded", log4eItem.isExpanded());

        String[] expectedChildren = {"Declaration", "Statements", "Position", "Format", "Replace & UI"};
        for (String child : expectedChildren) {
            assertNotNull("Child node '" + child + "' should exist", log4eItem.getNode(child));
        }
        log("after verify children");

        log4eItem.collapse();
        log("after collapse");
        assertFalse("Log4E node should be collapsed", log4eItem.isExpanded());

        log4eItem.expand();
        log("after re-expand");
    }
}
