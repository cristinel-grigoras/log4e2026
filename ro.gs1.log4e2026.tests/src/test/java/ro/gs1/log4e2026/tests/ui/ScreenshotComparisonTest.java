package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
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
 * Compares three screenshot capture methods side-by-side for 5 UI scenarios:
 * <ol>
 *   <li>ImageMagick {@code import -window root} (external process)</li>
 *   <li>{@code SWTUtils.captureScreenshot()} (SWTBot static utility)</li>
 *   <li>{@code bot.captureScreenshot()} (SWTBot instance method)</li>
 * </ol>
 * Each test captures the same UI state with all 3 methods and asserts that
 * non-zero PNG files were created. File sizes are logged for comparison.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ScreenshotComparisonTest {

    private static final String SCREENSHOT_DIR = System.getProperty("screenshot.dir", "target/screenshots");
    private static final String PROJECT_NAME = "ScreenshotTestProject";
    private static SWTWorkbenchBot bot;
    private static boolean projectCreated = false;

    @Rule
    public TimingRule timingRule = new TimingRule();

    @BeforeClass
    public static void setUpClass() {
        new File(SCREENSHOT_DIR).mkdirs();
        SWTBotPreferences.SCREENSHOTS_DIR = "";
        SWTBotPreferences.TIMEOUT = 1000;
        SWTBotPreferences.PLAYBACK_DELAY = 0;
        SWTBotPreferences.TYPE_INTERVAL = 0;
        bot = new SWTWorkbenchBot();
        try {
            bot.viewByTitle("Welcome").close();
        } catch (Exception e) {
            // Ignore
        }
    }

    @AfterClass
    public static void tearDownClass() {
        if (projectCreated) {
            TestTimingUtil.deleteProjectIfExists(bot, PROJECT_NAME);
        }
        TestTimingUtil.printSummary();
    }

    @Before
    public void setUp() {
        TestTimingUtil.focusWorkbenchShell(bot);
    }

    @After
    public void tearDown() {
        try {
            bot.activeShell().pressShortcut(Keystrokes.ESC);
        } catch (Exception e) {
            // Ignore
        }
    }

    private void log(String step) {
        TestTimingUtil.log(bot, step);
    }

    // ----------------------------------------------------------------
    // Capture helpers
    // ----------------------------------------------------------------

    /**
     * Capture with ImageMagick import command.
     * @return the file, or null if capture failed
     */
    private File captureWithImport(String name) {
        String path = SCREENSHOT_DIR + "/" + name;
        try {
            ProcessBuilder pb = new ProcessBuilder("import", "-window", "root", "PNG:" + path);
            pb.inheritIO();
            int exitCode = pb.start().waitFor();
            if (exitCode == 0) {
                return new File(path);
            }
            System.out.println("  [import] exit code " + exitCode + " for " + name);
        } catch (Exception e) {
            System.out.println("  [import] FAILED: " + e.getMessage());
        }
        return null;
    }

    /**
     * Capture with SWTUtils.captureScreenshot (static utility).
     * @return the file, or null if capture failed
     */
    private File captureWithSWTUtils(String name) {
        String path = SCREENSHOT_DIR + "/" + name;
        try {
            org.eclipse.swtbot.swt.finder.utils.SWTUtils.captureScreenshot(path);
            return new File(path);
        } catch (Exception e) {
            System.out.println("  [SWTUtils] FAILED: " + e.getMessage());
        }
        return null;
    }

    /**
     * Capture with bot.captureScreenshot (instance method).
     * @return the file, or null if capture failed
     */
    private File captureWithBot(String name) {
        String path = SCREENSHOT_DIR + "/" + name;
        try {
            bot.captureScreenshot(path);
            return new File(path);
        } catch (Exception e) {
            System.out.println("  [bot] FAILED: " + e.getMessage());
        }
        return null;
    }

    /**
     * Capture the current UI state with all 3 methods and log results.
     */
    private void captureAll(String prefix) {
        File fImport = captureWithImport(prefix + "_import.png");
        File fSWTUtils = captureWithSWTUtils(prefix + "_swtutils.png");
        File fBot = captureWithBot(prefix + "_bot.png");

        System.out.println("  === Screenshot comparison: " + prefix + " ===");
        reportFile("import   ", fImport);
        reportFile("SWTUtils ", fSWTUtils);
        reportFile("bot      ", fBot);
        System.out.println("  =============================================");

        // Assert all files were created with non-zero size
        assertFileValid(prefix + "_import.png", fImport);
        assertFileValid(prefix + "_swtutils.png", fSWTUtils);
        assertFileValid(prefix + "_bot.png", fBot);
    }

    private void reportFile(String label, File f) {
        if (f != null && f.exists()) {
            System.out.println("  " + label + ": " + f.getName() + " (" + f.length() + " bytes)");
        } else {
            System.out.println("  " + label + ": MISSING");
        }
    }

    private void assertFileValid(String name, File f) {
        assertNotNull("Screenshot file should be created: " + name, f);
        assertTrue("Screenshot file should exist: " + name, f.exists());
        assertTrue("Screenshot file should be non-empty: " + name + " (size=" + f.length() + ")", f.length() > 0);
    }

    // ----------------------------------------------------------------
    // Test 0: Create a Java project for later tests
    // ----------------------------------------------------------------

    @Test
    public void test00_CreateJavaProject() {
        log("0.0 start - create Java project");

        TestTimingUtil.deleteProjectIfExists(bot, PROJECT_NAME);

        bot.menu("File").menu("New").menu("Other...").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("New"), 1000);
        bot.activeShell().setFocus();

        bot.text().setText("Java Project");
        TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 1000);

        SWTBotTreeItem javaNode = bot.tree().getTreeItem("Java");
        javaNode.expand();
        TestTimingUtil.waitUntil(bot, Conditions.treeItemHasNode(javaNode, "Java Project"), 1000);
        javaNode.getNode("Java Project").select();

        bot.button("Next >").click();
        TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.textWithLabel("Project name:")), 1000);
        bot.textWithLabel("Project name:").setText(PROJECT_NAME);
        bot.button("Finish").click();

        try {
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Open Associated Perspective?"), 1000);
            bot.button("No").click();
        } catch (Exception e) {
            // Dialog may not appear
        }

        TestTimingUtil.waitUntil(bot, TestTimingUtil.projectExists(bot, PROJECT_NAME), 1000);
        projectCreated = true;
        log("0.1 project created");
    }

    // ----------------------------------------------------------------
    // Test 1: Editor with code
    // ----------------------------------------------------------------

    @Test
    public void test01_EditorWithCode() throws Exception {
        log("1.0 start - editor screenshot comparison");
        TestTimingUtil.focusWorkbenchShell(bot);

        // Capture the workbench with the project tree expanded
        SWTBotTreeItem project = bot.tree().getTreeItem(PROJECT_NAME);
        project.expand();
        project.select();
        log("1.1 project expanded and selected");

        captureAll("cmp_01_editor");
        log("1.2 screenshots captured");
    }

    // ----------------------------------------------------------------
    // Test 2: Open context menu
    // ----------------------------------------------------------------

    @Test
    public void test02_OpenContextMenu() throws Exception {
        log("2.0 start - context menu screenshot comparison");
        TestTimingUtil.focusWorkbenchShell(bot);

        SWTBotTreeItem project = bot.tree().getTreeItem(PROJECT_NAME);
        project.select();

        // Open context menu (it stays open for the captures)
        project.contextMenu();
        log("2.1 context menu opened");

        // Small delay to let the menu fully render
        Thread.sleep(200);

        captureAll("cmp_02_menu");
        log("2.2 screenshots captured");

        // Close menu
        bot.activeShell().pressShortcut(Keystrokes.ESC);
    }

    // ----------------------------------------------------------------
    // Test 3: Preferences dialog
    // ----------------------------------------------------------------

    @Test
    public void test03_PreferencesDialog() throws Exception {
        log("3.0 start - preferences dialog screenshot comparison");
        TestTimingUtil.focusWorkbenchShell(bot);

        bot.menu("Window").menu("Preferences...").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 1000);
        log("3.1 preferences dialog open");

        // Navigate to Log4E 2026 preference page
        try {
            bot.tree().getTreeItem("Log4E 2026").select();
            TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.button("Apply")), 1000);
            log("3.2 Log4E 2026 page selected");
        } catch (Exception e) {
            log("3.2 Log4E 2026 page not found, capturing generic preferences");
        }

        captureAll("cmp_03_prefs");
        log("3.3 screenshots captured");

        bot.button("Cancel").click();
    }

    // ----------------------------------------------------------------
    // Test 4: Tree view expanded
    // ----------------------------------------------------------------

    @Test
    public void test04_TreeViewExpanded() throws Exception {
        log("4.0 start - tree view screenshot comparison");
        TestTimingUtil.focusWorkbenchShell(bot);

        SWTBotTreeItem project = bot.tree().getTreeItem(PROJECT_NAME);
        project.expand();
        log("4.1 project expanded");

        // Expand src if possible
        try {
            project.getNode("src").expand();
            log("4.2 src expanded");
        } catch (Exception e) {
            log("4.2 src not expandable");
        }

        // Expand JRE System Library if possible
        try {
            for (SWTBotTreeItem child : project.getItems()) {
                if (child.getText().contains("JRE System Library")) {
                    child.expand();
                    log("4.3 JRE expanded");
                    break;
                }
            }
        } catch (Exception e) {
            log("4.3 JRE not expandable");
        }

        captureAll("cmp_04_tree");
        log("4.4 screenshots captured");
    }

    // ----------------------------------------------------------------
    // Test 5: Wizard dialog (New Java Class wizard)
    // ----------------------------------------------------------------

    @Test
    public void test05_WizardDialog() throws Exception {
        log("5.0 start - wizard dialog screenshot comparison");
        TestTimingUtil.focusWorkbenchShell(bot);

        bot.menu("File").menu("New").menu("Other...").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("New"), 1000);
        log("5.1 New wizard open");

        // Filter to show Java wizards
        bot.text().setText("Java Class");
        TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 1000);
        log("5.2 filtered to Java Class");

        captureAll("cmp_05_wizard");
        log("5.3 screenshots captured");

        bot.button("Cancel").click();
    }
}
