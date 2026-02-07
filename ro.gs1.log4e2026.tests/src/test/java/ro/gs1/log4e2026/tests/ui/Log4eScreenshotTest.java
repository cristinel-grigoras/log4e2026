package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.junit.After;
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
 * Uses SWTUtils.captureScreenshot() to capture screenshots while menus are still open.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class Log4eScreenshotTest {

    private static final String SCREENSHOT_DIR = System.getProperty("screenshot.dir", "target/screenshots");
    private SWTWorkbenchBot bot;

    @Rule
    public TimingRule timingRule = new TimingRule();

    @BeforeClass
    public static void setUpClass() {
        File dir = new File(SCREENSHOT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // Disable automatic failure screenshots
        SWTBotPreferences.SCREENSHOTS_DIR = "";
        SWTBotPreferences.TIMEOUT = 1000;
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
        TestTimingUtil.focusWorkbenchShell(bot);
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
        TestTimingUtil.focusWorkbenchShell(bot);
        log("after setFocus");

        bot.activeShell().pressShortcut(SWT.ALT, 'e');
        log("after Alt+E");

        String filename = SCREENSHOT_DIR + "/01_edit_menu.png";
        captureWithImport(filename);
        log("after screenshot");
    }

    @Test
    public void captureLog4eInEditMenu() throws Exception {
        log("start");
        TestTimingUtil.focusWorkbenchShell(bot);
        log("after setFocus");

        bot.activeShell().pressShortcut(SWT.ALT, 'e');
        log("after Alt+E");

        String filename = SCREENSHOT_DIR + "/02_edit_menu_with_log4e.png";
        captureWithImport(filename);
        log("after screenshot");
    }

    @Test
    public void captureFileMenu() throws Exception {
        log("start");
        TestTimingUtil.focusWorkbenchShell(bot);
        log("after setFocus");

        bot.activeShell().pressShortcut(SWT.ALT, 'f');
        log("after Alt+F");

        String filename = SCREENSHOT_DIR + "/03_file_menu.png";
        captureWithImport(filename);
        log("after screenshot");
    }

    @Test
    public void captureWindowMenu() throws Exception {
        log("start");
        TestTimingUtil.focusWorkbenchShell(bot);
        log("after setFocus");

        bot.activeShell().pressShortcut(SWT.ALT, 'w');
        log("after Alt+W");

        String filename = SCREENSHOT_DIR + "/04_window_menu.png";
        captureWithImport(filename);
        log("after screenshot");
    }

    @Test
    public void captureLog4ePreferencePage() throws Exception {
        log("start");
        TestTimingUtil.focusWorkbenchShell(bot);
        log("after focusWorkbenchShell");

        // Log active shell and focus state before menu click
        System.out.println("  Active shell: '" + bot.activeShell().getText() + "'");
        System.out.println("  Shell count: " + bot.shells().length);
        for (org.eclipse.swtbot.swt.finder.widgets.SWTBotShell s : bot.shells()) {
            try {
                System.out.println("    shell: '" + s.getText() + "' active=" + s.isActive());
            } catch (Exception e) { /* ignore */ }
        }
        System.out.flush();

        bot.menu("Window").menu("Preferences...").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 1000);
        log("after Preferences open");

        bot.tree().getTreeItem("Log4E 2026").select();
        TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.button("Apply")), 1000);
        log("after Log4E select");

        String filename = SCREENSHOT_DIR + "/05_log4e_preferences.png";
        captureWithImport(filename);
        log("after screenshot");

        bot.button("Cancel").click();
        log("after Cancel");
    }

    private void captureWithImport(String filename) {
        try {
            org.eclipse.swtbot.swt.finder.utils.SWTUtils.captureScreenshot(filename);
            System.out.println("Screenshot saved: " + filename);
        } catch (Exception e) {
            System.out.println("Screenshot failed: " + e.getMessage());
        }
    }
}
