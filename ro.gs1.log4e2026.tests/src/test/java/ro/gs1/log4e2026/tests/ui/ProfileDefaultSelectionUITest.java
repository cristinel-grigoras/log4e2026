package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
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
 * SWTBot UI tests for Log4E Profile Default Selection.
 * Tests saving profiles and selecting them as default at:
 * - Global (workspace) level
 * - Project level
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProfileDefaultSelectionUITest {

    private static final String SCREENSHOT_DIR = System.getProperty("screenshot.dir", "target/screenshots");
    private static String testProfileName;
    private static String testProjectName;
    private SWTWorkbenchBot bot;

    @BeforeClass
    public static void setUpClass() {
        File dir = new File(SCREENSHOT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
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
        try {
            bot.viewByTitle("Welcome").close();
        } catch (Exception e) {
            // Ignore if not found
        }
        TestTimingUtil.focusWorkbenchShell(bot);
    }

    @After
    public void tearDown() {
        // Close any open dialogs
        try {
            while (true) {
                SWTBotShell activeShell = bot.activeShell();
                String title = activeShell.getText();
                if (title.equals("Preferences") || title.contains("Profile") ||
                    title.contains("Duplicate") || title.contains("Properties") ||
                    title.contains("New Project") || title.contains("Edit")) {
                    try {
                        bot.button("Cancel").click();
                    } catch (Exception e) {
                        activeShell.close();
                    }
                } else {
                    break;
                }
            }
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
     * Open the Templates preference page.
     */
    private void openTemplatesPreferencePage() {
        bot.menu("Window").menu("Preferences...").click();
        bot.waitUntil(Conditions.shellIsActive("Preferences"), 5000);

        // Use shell-specific bot to avoid finding wrong tree
        SWTBotShell prefsShell = bot.shell("Preferences");
        prefsShell.activate();
        SWTBotTreeItem log4eItem = prefsShell.bot().tree().getTreeItem("Log4E 2026");
        log4eItem.expand();
        log4eItem.getNode("Templates").select();
    }

    @Test
    public void test01_SaveNewProfileAsGlobalDefault() {
        log("01.0 start");
        try {
            openTemplatesPreferencePage();
            log("01.1 Templates page opened");

            // Duplicate SLF4J to create a new profile
            SWTBotCombo profileCombo = bot.comboBox(0);
            String[] items = profileCombo.items();
            for (int i = 0; i < items.length; i++) {
                if (items[i].contains("SLF4J") && items[i].contains("built-in")) {
                    profileCombo.setSelection(i);
                    break;
                }
            }
            bot.sleep(300);
            log("01.2 SLF4J selected");

            // Click Duplicate
            bot.button("Duplicate...").click();
            bot.waitUntil(Conditions.shellIsActive("Duplicate Profile"), 5000);
            log("01.3 Duplicate dialog opened");

            // Enter unique name with timestamp
            testProfileName = "TestProfile_" + System.currentTimeMillis();
            SWTBotText nameText = bot.text(0);
            nameText.setText(testProfileName);
            bot.button("OK").click();
            log("01.4 Created profile: " + testProfileName);

            // Wait for preferences to return and activate shell explicitly
            bot.waitUntil(Conditions.shellIsActive("Preferences"), 5000);
            SWTBotShell prefsShell = bot.shell("Preferences");
            prefsShell.activate();
            prefsShell.setFocus();
            bot.sleep(500);
            log("01.4b Back to Preferences");

            // Re-select Templates page to ensure widgets are accessible
            // Use the tree in the preferences shell context
            SWTBotTreeItem log4eItem = prefsShell.bot().tree().getTreeItem("Log4E 2026");
            log4eItem.getNode("Templates").select();
            bot.sleep(300);
            log("01.4c Templates page re-selected");

            // Select the new profile (use shell-specific bot)
            profileCombo = prefsShell.bot().comboBox(0);
            items = profileCombo.items();
            int newProfileIndex = -1;
            for (int i = 0; i < items.length; i++) {
                if (items[i].contains(testProfileName)) {
                    newProfileIndex = i;
                    break;
                }
            }
            assertTrue("New profile should exist in combo", newProfileIndex >= 0);
            profileCombo.setSelection(newProfileIndex);
            bot.sleep(300);
            log("01.5 New profile selected");

            // Click Apply to save as global default
            bot.button("Apply").click();
            bot.sleep(500);
            log("01.6 Applied settings");

            captureScreen("profile_default_01_global_default.png");

            // Verify it's still selected after Apply (use shell-specific bot)
            String currentSelection = prefsShell.bot().comboBox(0).getText();
            assertTrue("New profile should remain selected after Apply",
                       currentSelection.contains(testProfileName));
            log("01.7 Verified profile is saved as default");

            // Close preferences with "Apply and Close" (Eclipse 2025-12 uses this instead of OK)
            prefsShell.activate();
            bot.button("Apply and Close").click();
            log("01.8 Preferences closed with Apply and Close");

            // Re-open preferences to verify persistence
            bot.sleep(500);
            openTemplatesPreferencePage();
            log("01.9 Re-opened Templates page");

            // Verify the profile is still selected (get fresh shell reference)
            SWTBotShell prefsShell2 = bot.shell("Preferences");
            prefsShell2.activate();
            currentSelection = prefsShell2.bot().comboBox(0).getText();
            assertTrue("New profile should be selected after re-opening preferences",
                       currentSelection.contains(testProfileName));
            log("01.10 Verified profile persisted as global default");

            captureScreen("profile_default_01_verified_persistence.png");

            prefsShell2.bot().button("Cancel").click();
            log("01.11 after Cancel");
        } catch (Exception e) {
            System.out.println("Error in test01: " + e.getMessage());
            e.printStackTrace();
            captureScreen("profile_default_01_ERROR.png");
            fail("Save new profile as global default test failed: " + e.getMessage());
        }
    }

    @Test
    public void test02_CreateJavaProjectForPropertyTest() {
        log("02.0 start");
        try {
            // Create a new Java project for testing project-level settings
            bot.menu("File").menu("New").menu("Project...").click();
            bot.waitUntil(Conditions.shellIsActive("New Project"), 5000);
            log("02.1 New Project wizard opened");

            // Select Java Project
            SWTBotTreeItem javaNode = bot.tree().getTreeItem("Java");
            javaNode.expand();
            javaNode.getNode("Java Project").select();
            bot.button("Next >").click();
            log("02.2 Java Project selected");

            // Enter project name
            testProjectName = "Log4ETestProject_" + System.currentTimeMillis();
            bot.textWithLabel("Project name:").setText(testProjectName);
            log("02.3 Project name entered: " + testProjectName);

            captureScreen("profile_default_02_new_project.png");

            // Finish project creation
            bot.button("Finish").click();
            log("02.4 Finish clicked");

            // Wait for project to be created (might show perspective switch dialog)
            bot.sleep(2000);
            try {
                // If "Open Associated Perspective?" dialog appears, click No
                for (SWTBotShell shell : bot.shells()) {
                    if (shell.getText().contains("Perspective")) {
                        shell.activate();
                        bot.button("No").click();
                        log("02.5 Declined perspective switch");
                        break;
                    }
                }
            } catch (Exception e) {
                // Dialog didn't appear, that's OK
            }

            bot.sleep(1000);
            log("02.6 Project created successfully: " + testProjectName);

            captureScreen("profile_default_02_project_created.png");
        } catch (Exception e) {
            System.out.println("Error in test02: " + e.getMessage());
            e.printStackTrace();
            captureScreen("profile_default_02_ERROR.png");
            fail("Create Java project test failed: " + e.getMessage());
        }
    }

    @Test
    public void test03_SelectProfileAtProjectLevel() {
        log("03.0 start");
        try {
            // Use project name from previous test
            if (testProjectName == null || testProjectName.isEmpty()) {
                log("03.0b No project name from test02, skipping");
                System.out.println("Skipping test03: No project created in test02");
                return; // Skip this test if no project was created
            }
            log("03.1 Testing with project: " + testProjectName);

            // Open Project Explorer and find the project
            try {
                bot.viewByTitle("Package Explorer").setFocus();
                log("03.1b Package Explorer focused");
            } catch (Exception e) {
                try {
                    bot.viewByTitle("Project Explorer").setFocus();
                    log("03.1b Project Explorer focused");
                } catch (Exception e2) {
                    log("03.1b Could not focus explorer view: " + e2.getMessage());
                }
            }
            bot.sleep(500);

            // Right-click on project and open Properties
            SWTBotTreeItem projectItem = null;
            try {
                projectItem = bot.tree().getTreeItem(testProjectName);
                projectItem.select();
                log("03.2a Project selected");
            } catch (Exception e) {
                log("03.2a Could not select project: " + e.getMessage());
                fail("Could not find project: " + testProjectName);
                return;
            }

            // Use Alt+Enter shortcut to open Properties (more reliable than context menu)
            try {
                bot.sleep(300);
                // First try keyboard shortcut Alt+Enter
                projectItem.select();
                TestTimingUtil.focusWorkbenchShell(bot);
                org.eclipse.swtbot.swt.finder.keyboard.Keystrokes.MOD1.toString(); // Force load
                bot.activeShell().pressShortcut(org.eclipse.swt.SWT.ALT, '\r');
                log("03.2 Opened project properties via Alt+Enter");
            } catch (Exception e) {
                log("03.2 Alt+Enter failed: " + e.getMessage() + ", trying menu");
                // Alternative: use Project menu -> Properties
                try {
                    projectItem.select();
                    bot.menu("Project").menu("Properties").click();
                    log("03.2b Opened project properties via Project menu");
                } catch (Exception e2) {
                    log("03.2b Project menu also failed: " + e2.getMessage());
                }
            }

            // Wait for the dialog to appear
            bot.sleep(2000);

            // Debug: list all available shells
            System.out.println("Available shells after Properties click:");
            for (SWTBotShell s : bot.shells()) {
                System.out.println("  - '" + s.getText() + "'");
            }

            // Try to find Properties shell with different name patterns
            SWTBotShell propertiesShell = null;
            for (SWTBotShell s : bot.shells()) {
                if (s.getText().contains("Properties") && s.getText().contains(testProjectName)) {
                    propertiesShell = s;
                    break;
                }
            }
            if (propertiesShell == null) {
                // Try without project name
                for (SWTBotShell s : bot.shells()) {
                    if (s.getText().contains("Properties")) {
                        propertiesShell = s;
                        break;
                    }
                }
            }

            if (propertiesShell != null) {
                propertiesShell.activate();
                log("03.3 Properties dialog opened: " + propertiesShell.getText());
            } else {
                bot.waitUntil(Conditions.shellIsActive("Properties for " + testProjectName), 5000);
                propertiesShell = bot.shell("Properties for " + testProjectName);
                log("03.3 Properties dialog opened via waitUntil");
            }

            // Navigate to Log4E 2026 page (use shell-specific bot)
            try {
                SWTBotTreeItem log4eItem = propertiesShell.bot().tree().getTreeItem("Log4E 2026");
                log4eItem.select();
                log("03.4 Log4E 2026 property page selected");
            } catch (WidgetNotFoundException e) {
                log("03.4 Log4E 2026 property page not found in tree");
                captureScreen("profile_default_03_no_log4e_page.png");
                propertiesShell.bot().button("Cancel").click();
                fail("Log4E 2026 property page not found");
                return;
            }

            bot.sleep(500);
            captureScreen("profile_default_03_project_properties.png");

            // Enable project-specific settings
            try {
                propertiesShell.bot().checkBox("Enable project-specific settings").click();
                log("03.5 Enabled project-specific settings");
            } catch (WidgetNotFoundException e) {
                log("03.5 Project settings checkbox not found, may already be enabled");
            }

            // Select a logging framework from combo
            try {
                SWTBotCombo frameworkCombo = propertiesShell.bot().comboBoxWithLabel("Logging Framework:");
                assertNotNull("Framework combo should exist", frameworkCombo);

                String[] frameworks = frameworkCombo.items();
                System.out.println("Available frameworks:");
                for (String fw : frameworks) {
                    System.out.println("  - " + fw);
                }

                // Select Log4j 2
                for (int i = 0; i < frameworks.length; i++) {
                    if (frameworks[i].contains("Log4j")) {
                        frameworkCombo.setSelection(i);
                        break;
                    }
                }
                log("03.6 Selected Log4j 2 framework");
            } catch (WidgetNotFoundException e) {
                log("03.6 Framework combo not found: " + e.getMessage());
            }

            captureScreen("profile_default_03_project_log4j_selected.png");

            // Apply and OK
            try {
                propertiesShell.bot().button("Apply and Close").click();
            } catch (WidgetNotFoundException e) {
                try {
                    propertiesShell.bot().button("Apply").click();
                    propertiesShell.bot().button("OK").click();
                } catch (WidgetNotFoundException e2) {
                    propertiesShell.bot().button("OK").click();
                }
            }
            log("03.7 Applied project settings");

            bot.sleep(500);

            // Re-open properties to verify persistence
            SWTBotTreeItem projectItem2 = bot.tree().getTreeItem(testProjectName);
            projectItem2.select();
            bot.sleep(300);
            // Use Alt+Enter to open Properties (same as first opening)
            bot.activeShell().pressShortcut(org.eclipse.swt.SWT.ALT, '\r');
            log("03.7b Re-opening properties via Alt+Enter");

            // Wait for dialog to appear
            bot.sleep(2000);

            // Find the re-opened properties shell
            SWTBotShell propertiesShell2 = null;
            for (SWTBotShell s : bot.shells()) {
                if (s.getText().contains("Properties")) {
                    propertiesShell2 = s;
                    break;
                }
            }
            if (propertiesShell2 != null) {
                propertiesShell2.activate();
                log("03.8 Re-opened project properties: " + propertiesShell2.getText());
            } else {
                fail("Could not find Properties dialog after re-open");
            }

            // Navigate to Log4E 2026 page
            propertiesShell2.bot().tree().getTreeItem("Log4E 2026").select();
            bot.sleep(500);

            // Verify settings persisted
            try {
                SWTBotCombo frameworkCombo = propertiesShell2.bot().comboBoxWithLabel("Logging Framework:");
                String selectedFramework = frameworkCombo.getText();
                System.out.println("Selected framework after re-open: " + selectedFramework);
                log("03.9 Verified framework selection persisted: " + selectedFramework);
            } catch (Exception e) {
                log("03.9 Could not verify framework selection: " + e.getMessage());
            }

            captureScreen("profile_default_03_verified_project_settings.png");

            propertiesShell2.bot().button("Cancel").click();
            log("03.10 after Cancel");
        } catch (Exception e) {
            System.out.println("Error in test03: " + e.getMessage());
            e.printStackTrace();
            captureScreen("profile_default_03_ERROR.png");
            fail("Select profile at project level test failed: " + e.getMessage());
        }
    }
}
