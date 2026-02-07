package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
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
 * SWTBot UI tests for custom profile selection in framework combos.
 * Tests that user-created profiles appear in:
 * - Main preferences page (Log4ePreferencePage)
 * - Project properties page (Log4eProjectPropertyPage)
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CustomProfileSelectionTest {

    private static final String PROJECT_NAME = "CustomProfileTestProject";
    private static final String CUSTOM_PROFILE_NAME = "My Test Profile";
    private static SWTWorkbenchBot bot;
    private static boolean projectCreated = false;
    private static boolean profileCreated = false;

    @BeforeClass
    public static void setUpClass() {
        SWTBotPreferences.SCREENSHOTS_DIR = "";
        SWTBotPreferences.TIMEOUT = 600;
        SWTBotPreferences.PLAYBACK_DELAY = 0;
        bot = new SWTWorkbenchBot();
        try {
            bot.viewByTitle("Welcome").close();
        } catch (Exception e) {
            // Ignore
        }
    }

    @AfterClass
    public static void tearDownClass() {
        TestTimingUtil.printSummary();
    }

    @Before
    public void setUp() {
        TestTimingUtil.focusWorkbenchShell(bot);
    }

    @After
    public void tearDown() {
        // Close any open dialogs
        try {
            while (true) {
                SWTBotShell activeShell = bot.activeShell();
                String title = activeShell.getText();
                if (title.equals("Preferences") || title.contains("Properties") ||
                    title.contains("Profile") || title.contains("Duplicate") ||
                    title.equals("New")) {
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

    /**
     * Open project properties dialog using Project menu.
     */
    private SWTBotShell openProjectProperties(SWTBotTreeItem projectItem) {
        log("openProjectProperties: start");

        // Select and focus the project item
        projectItem.select();
        log("openProjectProperties: project selected");

        // Use File > Properties menu (works when project is selected in Project Explorer)
        bot.menu("File").menu("Properties").click();
        log("openProjectProperties: File > Properties menu clicked");

        // Wait for Properties dialog
        String expectedTitle = "Properties for " + PROJECT_NAME;
        try {
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive(expectedTitle), 1000);
            log("openProjectProperties: dialog opened");
            return bot.shell(expectedTitle);
        } catch (Exception e) {
            System.out.println("Properties dialog '" + expectedTitle + "' not found. Available shells:");
            for (SWTBotShell s : bot.shells()) {
                System.out.println("  - '" + s.getText() + "'");
            }
            return null;
        }
    }

    @Test
    public void test01_CreateJavaProject() {
        log("01.0 start - create Java project");

        if (projectCreated) {
            log("01.1 project already created, skipping");
            return;
        }

        try {
            // Create Java project via File -> New -> Other...
            bot.menu("File").menu("New").menu("Other...").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("New"), 600);
            bot.activeShell().setFocus();
            log("01.1 New wizard opened");

            // Filter for Java Project
            bot.text().setText("Java Project");
            TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 600);

            SWTBotTreeItem javaNode = bot.tree().getTreeItem("Java");
            javaNode.expand();
            TestTimingUtil.waitUntil(bot, Conditions.treeItemHasNode(javaNode, "Java Project"), 600);
            javaNode.getNode("Java Project").select();
            log("01.2 Java Project selected");

            bot.button("Next >").click();
            TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.textWithLabel("Project name:")), 600);

            bot.textWithLabel("Project name:").setText(PROJECT_NAME);
            log("01.3 project name set");

            bot.button("Finish").click();

            // Handle perspective dialog if it appears
            try {
                TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Open Associated Perspective?"), 600);
                bot.button("No").click();
            } catch (Exception e) {
                // Dialog may not appear
            }

            TestTimingUtil.waitUntil(bot, TestTimingUtil.projectExists(bot, PROJECT_NAME), 600);
            assertNotNull(bot.tree().getTreeItem(PROJECT_NAME));
            projectCreated = true;
            log("01.4 project created");

        } catch (Exception e) {
            System.out.println("Error creating project: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create Java project: " + e.getMessage());
        }
    }

    @Test
    public void test02_CreateCustomProfile() {
        log("02.0 start - create custom profile");

        if (profileCreated) {
            log("02.1 profile already created, skipping");
            return;
        }

        try {
            // Open Templates preference page
            bot.menu("Window").menu("Preferences...").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 600);
            SWTBotShell prefsShell = bot.shell("Preferences");
            prefsShell.activate();
            log("02.1 Preferences opened");

            SWTBotTreeItem log4eItem = prefsShell.bot().tree().getTreeItem("Log4E 2026");
            log4eItem.expand();
            log4eItem.getNode("Templates").select();
            log("02.2 Templates page selected");

            // Wait for combo to be available
            TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(prefsShell.bot().comboBox(0)), 600);

            // Select SLF4J profile
            SWTBotCombo profileCombo = prefsShell.bot().comboBox(0);
            String[] items = profileCombo.items();
            System.out.println("Available profiles:");
            for (int i = 0; i < items.length; i++) {
                System.out.println("  [" + i + "] " + items[i]);
                if (items[i].contains("SLF4J")) {
                    profileCombo.setSelection(i);
                }
            }
            log("02.3 SLF4J selected");

            // Click Duplicate
            prefsShell.bot().button("Duplicate...").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Duplicate Profile"), 600);
            log("02.4 Duplicate dialog opened");

            // Enter custom name
            SWTBotText nameText = bot.text(0);
            nameText.setText(CUSTOM_PROFILE_NAME);
            log("02.5 custom name entered: " + CUSTOM_PROFILE_NAME);

            // Click OK
            bot.button("OK").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 600);
            prefsShell.activate();
            log("02.6 profile created");

            // Verify custom profile exists in combo
            TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(prefsShell.bot().comboBox(0)), 600);
            items = prefsShell.bot().comboBox(0).items();
            boolean found = false;
            for (String item : items) {
                System.out.println("  Profile: " + item);
                if (item.contains(CUSTOM_PROFILE_NAME)) {
                    found = true;
                }
            }
            assertTrue("Custom profile should appear in Templates combo", found);
            log("02.7 verified custom profile in Templates");

            // Apply and close to save the profile
            prefsShell.bot().button("Apply and Close").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellCloses(prefsShell), 600);
            profileCreated = true;
            log("02.8 preferences saved");

        } catch (Exception e) {
            System.out.println("Error creating custom profile: " + e.getMessage());
            e.printStackTrace();
            try {
                bot.button("Cancel").click();
            } catch (Exception ex) {
                // Ignore
            }
            fail("Failed to create custom profile: " + e.getMessage());
        }
    }

    @Test
    public void test03_CustomProfileAppearsInMainPreferences() {
        log("03.0 start - verify custom profile in main preferences");

        if (!profileCreated) {
            log("03.1 profile not created, skipping");
            return;
        }

        try {
            // Open main Log4E preferences page
            bot.menu("Window").menu("Preferences...").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 600);
            SWTBotShell prefsShell = bot.shell("Preferences");
            prefsShell.activate();
            log("03.1 Preferences opened");

            prefsShell.bot().tree().getTreeItem("Log4E 2026").select();
            log("03.2 Log4E 2026 main page selected");

            // Wait for combo to be available
            TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(prefsShell.bot().comboBox(0)), 600);

            // Find the framework combo
            SWTBotCombo frameworkCombo = null;
            try {
                frameworkCombo = prefsShell.bot().comboBoxWithLabel("Logging Framework:");
            } catch (WidgetNotFoundException e) {
                frameworkCombo = prefsShell.bot().comboBox(0);
            }
            assertNotNull("Framework combo should exist", frameworkCombo);
            log("03.3 found framework combo");

            // List all items and check for custom profile
            String[] items = frameworkCombo.items();
            System.out.println("Available frameworks in main preferences:");
            boolean found = false;
            for (String item : items) {
                System.out.println("  - " + item);
                if (item.contains(CUSTOM_PROFILE_NAME)) {
                    found = true;
                }
            }

            assertTrue("Custom profile '" + CUSTOM_PROFILE_NAME + "' should appear in main preferences framework combo", found);
            log("03.4 verified custom profile appears");

            prefsShell.bot().button("Cancel").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellCloses(prefsShell), 600);
            log("03.5 preferences closed");

        } catch (Exception e) {
            System.out.println("Error verifying custom profile: " + e.getMessage());
            e.printStackTrace();
            try {
                bot.button("Cancel").click();
            } catch (Exception ex) {
                // Ignore
            }
            fail("Failed to verify custom profile in main preferences: " + e.getMessage());
        }
    }

    @Test
    public void test04_SelectCustomProfileInMainPreferences() {
        log("04.0 start - select custom profile in main preferences");

        if (!profileCreated) {
            log("04.1 profile not created, skipping");
            return;
        }

        try {
            // Open main Log4E preferences page
            bot.menu("Window").menu("Preferences...").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 600);
            SWTBotShell prefsShell = bot.shell("Preferences");
            prefsShell.activate();
            log("04.1 Preferences opened");

            prefsShell.bot().tree().getTreeItem("Log4E 2026").select();
            log("04.2 Log4E 2026 main page selected");

            // Wait for combo to be available
            TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(prefsShell.bot().comboBox(0)), 600);

            // Find and select custom profile
            SWTBotCombo frameworkCombo = null;
            try {
                frameworkCombo = prefsShell.bot().comboBoxWithLabel("Logging Framework:");
            } catch (WidgetNotFoundException e) {
                frameworkCombo = prefsShell.bot().comboBox(0);
            }
            String[] items = frameworkCombo.items();
            int customIndex = -1;
            for (int i = 0; i < items.length; i++) {
                if (items[i].contains(CUSTOM_PROFILE_NAME)) {
                    customIndex = i;
                    break;
                }
            }

            assertTrue("Custom profile should exist in combo", customIndex >= 0);
            frameworkCombo.setSelection(customIndex);
            log("04.3 selected custom profile at index " + customIndex);

            // Apply and close
            prefsShell.bot().button("Apply and Close").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellCloses(prefsShell), 600);
            log("04.4 preferences saved with custom profile");

        } catch (Exception e) {
            System.out.println("Error selecting custom profile: " + e.getMessage());
            e.printStackTrace();
            try {
                bot.button("Cancel").click();
            } catch (Exception ex) {
                // Ignore
            }
            fail("Failed to select custom profile: " + e.getMessage());
        }
    }

    @Test
    public void test05_CustomProfileAppearsInProjectProperties() {
        log("05.0 start - verify custom profile in project properties");

        if (!projectCreated || !profileCreated) {
            log("05.1 project or profile not created, skipping");
            return;
        }

        try {
            // Focus Project Explorer and select project
            try {
                bot.viewByTitle("Project Explorer").setFocus();
            } catch (Exception e) {
                bot.viewByTitle("Package Explorer").setFocus();
            }
            SWTBotTreeItem projectItem = bot.tree().getTreeItem(PROJECT_NAME);
            projectItem.select();
            log("05.1 project selected");

            // Open project properties - try menu first, then context menu
            SWTBotShell propsShell = openProjectProperties(projectItem);
            assertNotNull("Properties dialog should open", propsShell);
            propsShell.activate();
            log("05.3 Properties dialog opened");

            // Navigate to Log4E 2026
            SWTBotTreeItem log4eNode = null;
            for (SWTBotTreeItem item : propsShell.bot().tree().getAllItems()) {
                if (item.getText().contains("Log4E")) {
                    log4eNode = item;
                    break;
                }
            }
            assertNotNull("Log4E 2026 should exist in project properties", log4eNode);
            log4eNode.select();
            log("05.4 Log4E 2026 selected");

            // Wait for combo to be available
            TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(propsShell.bot().comboBox(0)), 600);

            // Find framework combo in project properties
            SWTBotCombo frameworkCombo = null;
            try {
                frameworkCombo = propsShell.bot().comboBoxWithLabel("Logging Framework:");
            } catch (WidgetNotFoundException e) {
                frameworkCombo = propsShell.bot().comboBox(0);
            }
            assertNotNull("Framework combo should exist in project properties", frameworkCombo);
            log("05.5 found framework combo");

            // List all items and check for custom profile
            String[] items = frameworkCombo.items();
            System.out.println("Available frameworks in project properties:");
            boolean found = false;
            for (String item : items) {
                System.out.println("  - " + item);
                if (item.contains(CUSTOM_PROFILE_NAME)) {
                    found = true;
                }
            }

            assertTrue("Custom profile '" + CUSTOM_PROFILE_NAME + "' should appear in project properties framework combo", found);
            log("05.6 verified custom profile appears in project properties");

            propsShell.bot().button("Cancel").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellCloses(propsShell), 600);
            log("05.7 properties closed");

        } catch (Exception e) {
            System.out.println("Error verifying custom profile in project properties: " + e.getMessage());
            e.printStackTrace();
            try {
                bot.button("Cancel").click();
            } catch (Exception ex) {
                // Ignore
            }
            fail("Failed to verify custom profile in project properties: " + e.getMessage());
        }
    }

    @Test
    public void test06_SelectCustomProfileInProjectProperties() {
        log("06.0 start - select custom profile in project properties");

        if (!projectCreated || !profileCreated) {
            log("06.1 project or profile not created, skipping");
            return;
        }

        try {
            // Focus Project Explorer and select project
            try {
                bot.viewByTitle("Project Explorer").setFocus();
            } catch (Exception e) {
                bot.viewByTitle("Package Explorer").setFocus();
            }
            SWTBotTreeItem projectItem = bot.tree().getTreeItem(PROJECT_NAME);
            projectItem.select();
            log("06.1 project selected");

            // Open project properties
            SWTBotShell propsShell = openProjectProperties(projectItem);
            assertNotNull("Properties dialog should open", propsShell);
            log("06.3 Properties dialog opened");

            // Navigate to Log4E 2026
            SWTBotTreeItem log4eNode = null;
            for (SWTBotTreeItem item : propsShell.bot().tree().getAllItems()) {
                if (item.getText().contains("Log4E")) {
                    log4eNode = item;
                    break;
                }
            }
            log4eNode.select();
            log("06.4 Log4E 2026 selected");

            // Wait for combo to be available
            TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(propsShell.bot().comboBox(0)), 600);

            // Enable project settings
            propsShell.bot().checkBox("Enable project-specific settings").select();
            log("06.5 enabled project-specific settings");

            // Find and select custom profile
            SWTBotCombo frameworkCombo = null;
            try {
                frameworkCombo = propsShell.bot().comboBoxWithLabel("Logging Framework:");
            } catch (WidgetNotFoundException e) {
                frameworkCombo = propsShell.bot().comboBox(0);
            }
            String[] items = frameworkCombo.items();
            int customIndex = -1;
            for (int i = 0; i < items.length; i++) {
                if (items[i].contains(CUSTOM_PROFILE_NAME)) {
                    customIndex = i;
                    break;
                }
            }

            assertTrue("Custom profile should exist in project properties combo", customIndex >= 0);
            frameworkCombo.setSelection(customIndex);
            log("06.6 selected custom profile");

            // Apply and close
            propsShell.bot().button("Apply and Close").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellCloses(propsShell), 600);
            log("06.7 project properties saved with custom profile");

        } catch (Exception e) {
            System.out.println("Error selecting custom profile in project properties: " + e.getMessage());
            e.printStackTrace();
            try {
                bot.button("Cancel").click();
            } catch (Exception ex) {
                // Ignore
            }
            fail("Failed to select custom profile in project properties: " + e.getMessage());
        }
    }

    @Test
    public void test99_Cleanup() {
        log("99.0 start - cleanup");

        try {
            // Delete project
            if (projectCreated) {
                SWTBotTreeItem projectItem = bot.tree().getTreeItem(PROJECT_NAME);
                projectItem.contextMenu("Delete").click();
                TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Delete Resources"), 600);
                bot.checkBox("Delete project contents on disk (cannot be undone)").select();
                bot.button("OK").click();
                TestTimingUtil.waitUntil(bot, Conditions.shellCloses(bot.shell("Delete Resources")), 600);
                log("99.1 project deleted");
            }
        } catch (Exception e) {
            System.out.println("Cleanup error: " + e.getMessage());
        }

        log("99.2 cleanup complete");
    }
}
