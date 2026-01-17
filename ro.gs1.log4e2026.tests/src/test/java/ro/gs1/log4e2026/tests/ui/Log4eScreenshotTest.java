package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.junit.After;

import ro.gs1.log4e2026.tests.util.TestTimingUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ro.gs1.log4e2026.tests.util.TestTimingUtil;
import ro.gs1.log4e2026.tests.util.TimingRule;

/**
 * SWTBot UI tests that capture screenshots for documentation.
 * Uses external 'import' command (ImageMagick) to capture screenshots
 * while menus are still open.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class Log4eScreenshotTest {

    private static final String SCREENSHOT_DIR = System.getProperty("screenshot.dir", "target/screenshots");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static String runId;
    private SWTWorkbenchBot bot;

    @Rule
    public TimingRule timingRule = new TimingRule();

    @BeforeClass
    public static void setUpClass() {
        runId = LocalDateTime.now().format(TIME_FORMAT);
        File dir = new File(SCREENSHOT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // Disable automatic failure screenshots
        SWTBotPreferences.SCREENSHOTS_DIR = "";
        SWTBotPreferences.TIMEOUT = 10000;
    }

    @AfterClass
    public static void tearDownClass() {
        TestTimingUtil.printSummary();
    }

    @Before
    public void setUp() {
        bot = new SWTWorkbenchBot();
        try {
            bot.viewByTitle("Welcome").close();
        } catch (Exception e) {
            // Welcome view may not exist
        }
    }

    @After
    public void tearDown() {
        try {
            bot.activeShell().pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke.getInstance(SWT.ESC));
        } catch (Exception e) {
            // Ignore
        }
    }

    private void log(String step) {
        TestTimingUtil.log(bot, step);
    }

    @Test
    public void captureWorkbenchWithEditMenu() throws Exception {
        log("start");
        bot.shells()[0].setFocus();
        int shellCount = bot.shells().length;
        log("after setFocus");

        bot.activeShell().pressShortcut(SWT.ALT, 'e');
        TestTimingUtil.waitUntil(bot, TestTimingUtil.shellCountIncreases(bot, shellCount), 2000);
        log("after Alt+E");

        String filename = SCREENSHOT_DIR + "/" + runId + "_01_edit_menu.png";
        captureWithImport(filename);
        log("after screenshot");
    }

    @Test
    public void captureLog4eInEditMenu() throws Exception {
        log("start");
        bot.shells()[0].setFocus();
        int shellCount = bot.shells().length;
        log("after setFocus");

        bot.activeShell().pressShortcut(SWT.ALT, 'e');
        TestTimingUtil.waitUntil(bot, TestTimingUtil.shellCountIncreases(bot, shellCount), 2000);
        log("after Alt+E");

        String filename = SCREENSHOT_DIR + "/" + runId + "_02_edit_menu_with_log4e.png";
        captureWithImport(filename);
        log("after screenshot");
    }

    @Test
    public void captureFileMenu() throws Exception {
        log("start");
        bot.shells()[0].setFocus();
        int shellCount = bot.shells().length;
        log("after setFocus");

        bot.activeShell().pressShortcut(SWT.ALT, 'f');
        TestTimingUtil.waitUntil(bot, TestTimingUtil.shellCountIncreases(bot, shellCount), 2000);
        log("after Alt+F");

        String filename = SCREENSHOT_DIR + "/" + runId + "_03_file_menu.png";
        captureWithImport(filename);
        log("after screenshot");
    }

    @Test
    public void captureWindowMenu() throws Exception {
        log("start");
        bot.shells()[0].setFocus();
        int shellCount = bot.shells().length;
        log("after setFocus");

        bot.activeShell().pressShortcut(SWT.ALT, 'w');
        TestTimingUtil.waitUntil(bot, TestTimingUtil.shellCountIncreases(bot, shellCount), 2000);
        log("after Alt+W");

        String filename = SCREENSHOT_DIR + "/" + runId + "_04_window_menu.png";
        captureWithImport(filename);
        log("after screenshot");
    }

    @Test
    public void captureLog4ePreferencePage() throws Exception {
        log("start");
        bot.shells()[0].setFocus();
        int shellCount = bot.shells().length;
        log("after setFocus");

        bot.activeShell().pressShortcut(SWT.ALT, 'w');
        TestTimingUtil.waitUntil(bot, TestTimingUtil.shellCountIncreases(bot, shellCount), 2000);
        log("after Alt+W");

        bot.activeShell().pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke.getInstance(SWT.END));
        bot.activeShell().pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke.getInstance(SWT.CR));
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 5000);
        log("after Preferences open");

        bot.tree().getTreeItem("Log4E 2026").select();
        TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.button("Apply")), 2000);
        log("after Log4E select");

        String filename = SCREENSHOT_DIR + "/" + runId + "_05_log4e_preferences.png";
        captureWithImport(filename);
        log("after screenshot");

        bot.button("Cancel").click();
        log("after Cancel");
    }

    private void captureWithImport(String filename) throws Exception {
        // Use ImageMagick 'import' to capture the root window with explicit PNG format
        ProcessBuilder pb = new ProcessBuilder("import", "-window", "root", "PNG:" + filename);
        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            System.out.println("Screenshot saved: " + filename);
            assertTrue("Screenshot file not created", new File(filename).exists());
        } else {
            System.out.println("import command failed with exit code: " + exitCode);
            fail("Screenshot failed: " + filename);
        }
    }
}
