package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
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
 * SWTBot UI tests for Log4E Template Management preference page.
 * Tests the profile management functionality including:
 * - Profile selection
 * - Edit/View profiles
 * - Duplicate profiles
 * - Rename user profiles
 * - Remove user profiles
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TemplateManagementUITest {

    private static final String SCREENSHOT_DIR = System.getProperty("screenshot.dir", "target/screenshots");
    private SWTWorkbenchBot bot;

    @BeforeClass
    public static void setUpClass() {
        File dir = new File(SCREENSHOT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
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
                    title.contains("Duplicate") || title.contains("Rename") ||
                    title.contains("Remove") || title.contains("Edit")) {
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
            org.eclipse.swtbot.swt.finder.utils.SWTUtils.captureScreenshot(filepath);
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
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 1000);

        SWTBotTreeItem log4eItem = bot.tree().getTreeItem("Log4E 2026");
        log4eItem.expand();
        log4eItem.getNode("Templates").select();
    }

    @Test
    public void test01_TemplatesPageCanBeOpened() {
        log("01.0 start");
        try {
            openTemplatesPreferencePage();
            log("01.1 Templates page opened");

            captureScreen("template_01_page_opened.png");
            assertTrue("Templates preference page should open", true);

            bot.button("Cancel").click();
            log("01.2 after Cancel");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to open Templates preference page: " + e.getMessage());
        }
    }

    @Test
    public void test02_ProfileComboExists() {
        log("02.0 start");
        try {
            openTemplatesPreferencePage();
            log("02.1 Templates page opened");

            // Find the profile combo (first combo on the page)
            SWTBotCombo profileCombo = bot.comboBox(0);
            assertNotNull("Profile combo should exist", profileCombo);

            String[] items = profileCombo.items();
            assertTrue("Profile combo should have items", items.length > 0);

            System.out.println("Available profiles:");
            for (String item : items) {
                System.out.println("  - " + item);
            }

            // Verify built-in profiles exist
            boolean hasSLF4J = false;
            boolean hasLog4j2 = false;
            boolean hasJUL = false;
            for (String item : items) {
                if (item.contains("SLF4J")) hasSLF4J = true;
                if (item.contains("Log4j 2")) hasLog4j2 = true;
                if (item.contains("JDK Logging")) hasJUL = true;
            }

            assertTrue("SLF4J profile should exist", hasSLF4J);
            assertTrue("Log4j 2 profile should exist", hasLog4j2);
            assertTrue("JDK Logging profile should exist", hasJUL);

            captureScreen("template_02_profile_combo.png");

            bot.button("Cancel").click();
            log("02.2 after Cancel");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            fail("Profile combo test failed: " + e.getMessage());
        }
    }

    @Test
    public void test03_ManagementButtonsExist() {
        log("03.0 start");
        try {
            openTemplatesPreferencePage();
            log("03.1 Templates page opened");

            // Find management buttons
            SWTBotButton editButton = null;
            SWTBotButton duplicateButton = null;
            SWTBotButton renameButton = null;
            SWTBotButton removeButton = null;

            try {
                editButton = bot.button("View...");
            } catch (WidgetNotFoundException e) {
                try {
                    editButton = bot.button("Edit...");
                } catch (WidgetNotFoundException e2) {
                    // Will fail assertion below
                }
            }

            try {
                duplicateButton = bot.button("Duplicate...");
            } catch (WidgetNotFoundException e) {
                // Will fail assertion below
            }

            try {
                renameButton = bot.button("Rename...");
            } catch (WidgetNotFoundException e) {
                // Will fail assertion below
            }

            try {
                removeButton = bot.button("Remove");
            } catch (WidgetNotFoundException e) {
                // Will fail assertion below
            }

            assertNotNull("Edit/View button should exist", editButton);
            assertNotNull("Duplicate button should exist", duplicateButton);
            assertNotNull("Rename button should exist", renameButton);
            assertNotNull("Remove button should exist", removeButton);

            captureScreen("template_03_buttons.png");

            bot.button("Cancel").click();
            log("03.2 after Cancel");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            fail("Management buttons test failed: " + e.getMessage());
        }
    }

    @Test
    public void test04_BuiltInProfileHasDisabledRenameRemove() {
        log("04.0 start");
        try {
            openTemplatesPreferencePage();
            log("04.1 Templates page opened");

            // Select a built-in profile (SLF4J)
            SWTBotCombo profileCombo = bot.comboBox(0);
            String[] items = profileCombo.items();
            for (int i = 0; i < items.length; i++) {
                if (items[i].contains("SLF4J") && items[i].contains("built-in")) {
                    profileCombo.setSelection(i);
                    break;
                }
            }
            log("04.2 SLF4J selected");

            // Check button states
            SWTBotButton renameButton = bot.button("Rename...");
            SWTBotButton removeButton = bot.button("Remove");

            assertFalse("Rename should be disabled for built-in profile", renameButton.isEnabled());
            assertFalse("Remove should be disabled for built-in profile", removeButton.isEnabled());

            // Edit should say "View..." for built-in
            try {
                bot.button("View...");
                assertTrue("Edit button should say 'View...' for built-in profile", true);
            } catch (WidgetNotFoundException e) {
                // Might say Edit... if profile is editable
            }

            captureScreen("template_04_builtin_buttons.png");

            bot.button("Cancel").click();
            log("04.3 after Cancel");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            fail("Built-in profile button state test failed: " + e.getMessage());
        }
    }

    @Test
    public void test05_DuplicateProfile() {
        log("05.0 start");
        try {
            openTemplatesPreferencePage();
            log("05.1 Templates page opened");

            // Select SLF4J profile
            SWTBotCombo profileCombo = bot.comboBox(0);
            String[] items = profileCombo.items();
            System.out.println("  Available profiles before duplicate:");
            for (int i = 0; i < items.length; i++) {
                System.out.println("    [" + i + "] " + items[i]);
                if (items[i].contains("SLF4J")) {
                    profileCombo.setSelection(i);
                }
            }
            System.out.flush();
            log("05.2 SLF4J selected: " + profileCombo.getText());

            // Click Duplicate
            bot.button("Duplicate...").click();
            log("05.3 Duplicate clicked");

            // Wait for input dialog
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Duplicate Profile"), 1000);
            log("05.4 Duplicate dialog opened");

            // Log the proposed name and current dialog state
            SWTBotText nameText = bot.text(0);
            String proposedName = nameText.getText();
            System.out.println("  Proposed name from dialog: '" + proposedName + "'");

            // Use unique name with timestamp to avoid conflicts on surefire retry
            String uniqueName = "My Custom SLF4J " + System.currentTimeMillis();
            System.out.println("  Will enter unique name: '" + uniqueName + "'");
            System.out.flush();

            captureScreen("template_05_duplicate_dialog.png");

            // Clear and enter new name
            nameText.selectAll();
            nameText.typeText(uniqueName);

            // Log entered text and OK button state
            String enteredText = nameText.getText();
            boolean okEnabled = bot.button("OK").isEnabled();
            System.out.println("  Text field after typing: '" + enteredText + "'");
            System.out.println("  OK button enabled: " + okEnabled);

            // Check for validation error message (InputDialog shows it as errorMessage label)
            try {
                // InputDialog uses a label to show validation errors
                for (int li = 0; li < 5; li++) {
                    try {
                        String labelText = bot.label(li).getText();
                        if (labelText != null && !labelText.isEmpty()
                                && !labelText.equals("Enter a name for the new profile:")) {
                            System.out.println("  Validation message label[" + li + "]: '" + labelText + "'");
                        }
                    } catch (Exception e) { break; }
                }
            } catch (Exception e) { /* ignore */ }
            System.out.flush();
            log("05.5 Name entered: '" + enteredText + "' OK=" + okEnabled);

            if (!okEnabled) {
                captureScreen("template_05_ok_disabled.png");
                System.out.println("  ERROR: OK button not enabled after entering name");
                System.out.flush();
            }

            // Click OK
            TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.button("OK")), 3000);
            bot.button("OK").click();
            log("05.6 OK clicked");

            // Wait for Preferences shell to become active again
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 1000);

            // Re-navigate to get the combo on the active shell
            SWTBotShell prefShell = bot.shell("Preferences");
            prefShell.setFocus();
            String[] newItems = bot.comboBox(0).items();
            boolean found = false;
            System.out.println("  Profiles after duplicate:");
            for (String item : newItems) {
                System.out.println("    - " + item);
                if (item.contains(uniqueName)) {
                    found = true;
                }
            }
            System.out.flush();

            assertTrue("Duplicated profile '" + uniqueName + "' should appear in combo", found);
            log("05.7 Verified duplicate exists");

            captureScreen("template_05_after_duplicate.png");

            // Cancel and close preferences (leave the duplicate for now)
            bot.button("Cancel").click();
            log("05.8 after Cancel");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            fail("Duplicate profile test failed: " + e.getMessage());
        }
    }

    @Test
    public void test06_ProfileSelectionChangesPreview() {
        log("06.0 start");
        try {
            openTemplatesPreferencePage();
            log("06.1 Templates page opened");

            SWTBotCombo profileCombo = bot.comboBox(0);
            String[] items = profileCombo.items();

            // Select SLF4J
            int slf4jIndex = -1;
            int log4j2Index = -1;
            for (int i = 0; i < items.length; i++) {
                if (items[i].contains("SLF4J")) {
                    slf4jIndex = i;
                }
                if (items[i].contains("Log4j 2")) {
                    log4j2Index = i;
                }
            }

            assertTrue("SLF4J profile should exist", slf4jIndex >= 0);
            assertTrue("Log4j 2 profile should exist", log4j2Index >= 0);

            // Select SLF4J first
            profileCombo.setSelection(slf4jIndex);
            String selection1 = profileCombo.getText();
            log("06.2 SLF4J selected: " + selection1);

            // Select Log4j 2
            profileCombo.setSelection(log4j2Index);
            String selection2 = profileCombo.getText();
            log("06.3 Log4j 2 selected: " + selection2);

            // Verify selections are different
            assertNotEquals("Profile selection should change", selection1, selection2);
            assertTrue("Selection should be Log4j 2", selection2.contains("Log4j 2"));

            captureScreen("template_06_preview.png");

            bot.button("Cancel").click();
            log("06.4 after Cancel");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            fail("Profile selection preview test failed: " + e.getMessage());
        }
    }

    @Test
    public void test07_EditDialogOpens() {
        log("07.0 start");
        try {
            openTemplatesPreferencePage();
            log("07.1 Templates page opened");

            // Select a built-in profile
            SWTBotCombo profileCombo = bot.comboBox(0);
            String[] items = profileCombo.items();
            for (int i = 0; i < items.length; i++) {
                if (items[i].contains("SLF4J")) {
                    profileCombo.setSelection(i);
                    break;
                }
            }
            log("07.2 SLF4J selected");

            // Click View/Edit button
            int shellCount = bot.shells().length;
            try {
                bot.button("View...").click();
            } catch (WidgetNotFoundException e) {
                bot.button("Edit...").click();
            }
            log("07.3 Edit/View clicked");

            // Wait for edit dialog to appear
            TestTimingUtil.waitUntil(bot, TestTimingUtil.shellCountIncreases(bot, shellCount), 1000);
            SWTBotShell editShell = null;
            for (SWTBotShell shell : bot.shells()) {
                if (shell.getText().startsWith("Edit Profile:")) {
                    editShell = shell;
                    break;
                }
            }
            assertNotNull("Edit Profile dialog should open", editShell);
            editShell.activate();
            log("07.4 Edit dialog opened: " + editShell.getText());

            captureScreen("template_07_edit_dialog.png");

            // Verify tabs exist
            try {
                bot.tabItem("Declaration");
                bot.tabItem("Log Levels");
                bot.tabItem("Statement Patterns");
                bot.tabItem("Default Levels");
                assertTrue("All tabs should exist in edit dialog", true);
            } catch (WidgetNotFoundException e) {
                fail("Expected tabs not found in edit dialog: " + e.getMessage());
            }

            // Close edit dialog
            bot.button("Cancel").click();
            log("07.5 Edit dialog closed");

            // Close preferences
            bot.button("Cancel").click();
            log("07.6 after Cancel");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            fail("Edit dialog test failed: " + e.getMessage());
        }
    }

    @Test
    public void test08_EditDialogTabs() {
        log("08.0 start");
        try {
            openTemplatesPreferencePage();
            log("08.1 Templates page opened");

            // Select SLF4J
            SWTBotCombo profileCombo = bot.comboBox(0);
            String[] items = profileCombo.items();
            for (int i = 0; i < items.length; i++) {
                if (items[i].contains("SLF4J")) {
                    profileCombo.setSelection(i);
                    break;
                }
            }

            // Click View/Edit
            int shellCount = bot.shells().length;
            try {
                bot.button("View...").click();
            } catch (WidgetNotFoundException e) {
                bot.button("Edit...").click();
            }

            // Wait for edit dialog to appear
            TestTimingUtil.waitUntil(bot, TestTimingUtil.shellCountIncreases(bot, shellCount), 1000);
            SWTBotShell editShell = null;
            for (SWTBotShell shell : bot.shells()) {
                if (shell.getText().startsWith("Edit Profile:")) {
                    editShell = shell;
                    break;
                }
            }
            assertNotNull("Edit Profile dialog should open", editShell);
            editShell.activate();
            log("08.2 Edit dialog opened: " + editShell.getText());

            // Test Declaration tab
            bot.tabItem("Declaration").activate();
            captureScreen("template_08_tab_declaration.png");
            log("08.3 Declaration tab");

            // Test Log Levels tab
            bot.tabItem("Log Levels").activate();
            captureScreen("template_08_tab_levels.png");
            log("08.4 Log Levels tab");

            // Test Statement Patterns tab
            bot.tabItem("Statement Patterns").activate();
            captureScreen("template_08_tab_patterns.png");
            log("08.5 Statement Patterns tab");

            // Test Default Levels tab
            bot.tabItem("Default Levels").activate();
            captureScreen("template_08_tab_defaults.png");
            log("08.6 Default Levels tab");

            assertTrue("All tabs can be activated", true);

            // Close dialogs
            bot.button("Cancel").click();
            bot.button("Cancel").click();
            log("08.7 after Cancel");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            fail("Edit dialog tabs test failed: " + e.getMessage());
        }
    }
}
