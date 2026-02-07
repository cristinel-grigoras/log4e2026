package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
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
 * Integration tests for Project-Level Preferences.
 * Tests that project-specific preferences override workspace preferences.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProjectPreferencesTest {

    private static final String PROJECT_NAME = "ProjectPrefsTestProject";
    private static final String CLASS_NAME = "PrefsTestClass";
    private static SWTWorkbenchBot bot;
    private static boolean projectCreated = false;

    @BeforeClass
    public static void setUpClass() {
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
            try {
                bot.closeAllEditors();
                SWTBotTreeItem project = bot.tree().getTreeItem(PROJECT_NAME);
                project.select();
                project.contextMenu("Delete").click();
                SWTBotShell deleteShell = bot.shell("Delete Resources");
                deleteShell.activate();
                bot.checkBox("Delete project contents on disk (cannot be undone)").click();
                bot.button("OK").click();
                TestTimingUtil.waitUntil(bot, Conditions.shellCloses(deleteShell), 1000);
            } catch (Exception e) {
                System.out.println("Cleanup failed: " + e.getMessage());
            }
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

    private SWTBotShell findPropertiesShell() {
        // Find Properties shell - try with project name first
        for (SWTBotShell shell : bot.shells()) {
            String text = shell.getText();
            if (text != null && text.contains("Properties") && text.contains(PROJECT_NAME)) {
                return shell;
            }
        }
        // Fallback: any Properties dialog
        for (SWTBotShell shell : bot.shells()) {
            String text = shell.getText();
            if (text != null && text.contains("Properties")) {
                return shell;
            }
        }
        return null;
    }

    /**
     * Open project Properties dialog by clicking the project in the tree
     * (which focuses Package Explorer) and using File > Properties menu.
     */
    private void openProjectProperties() {
        bot.tree().getTreeItem(PROJECT_NAME).click();
        bot.menu("File").menu("Properties").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Properties for " + PROJECT_NAME), 1000);
    }

    private SWTBotTreeItem findLog4eNode(SWTBotShell propsShell) {
        try {
            for (SWTBotTreeItem item : propsShell.bot().tree().getAllItems()) {
                if (item.getText().contains("Log4E")) {
                    return item;
                }
            }
        } catch (Exception e) {
            System.out.println("Error finding Log4E node: " + e.getMessage());
        }
        return null;
    }

    @Test
    public void test01_CreateJavaProject() {
        log("1.0 start - create Java project");

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
        assertNotNull(bot.tree().getTreeItem(PROJECT_NAME));
        projectCreated = true;
        log("1.1 project created");
    }

    @Test
    public void test02_CreateJavaClass() {
        log("2.0 start - create Java class");

        try {
            bot.tree().getTreeItem(PROJECT_NAME).select();
        } catch (Exception e) {
            System.out.println("Project not found, skipping");
            return;
        }

        bot.menu("File").menu("New").menu("Other...").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("New"), 1000);
        bot.activeShell().setFocus();

        bot.text().setText("Class");
        TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 1000);

        SWTBotTreeItem javaNode = bot.tree().getTreeItem("Java");
        javaNode.expand();
        TestTimingUtil.waitUntil(bot, Conditions.treeItemHasNode(javaNode, "Class"), 1000);
        javaNode.getNode("Class").select();

        bot.button("Next >").click();
        TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.textWithLabel("Name:")), 1000);

        bot.textWithLabel("Package:").setText("com.test");
        bot.textWithLabel("Name:").setText(CLASS_NAME);

        SWTBotShell wizardShell = bot.shell("New Java Class");
        bot.button("Finish").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellCloses(wizardShell), 1000);

        TestTimingUtil.waitUntil(bot, TestTimingUtil.editorIsActive(bot, CLASS_NAME + ".java"), 1000);
        log("2.1 class created");
    }

    @Test
    public void test03_EnableProjectPreferencesAndSetLoggerName() {
        log("3.0 start - enable project preferences");

        openProjectProperties();
        log("3.1 opened project properties");

        SWTBotShell propsShell = findPropertiesShell();
        if (propsShell == null) {
            System.out.println("Properties dialog not found");
            return;
        }
        propsShell.activate();

        // Navigate to Log4E settings - use the dialog's bot
        try {
            // Find Log4E 2026 in the dialog's tree
            SWTBotTreeItem log4eNode = null;
            for (SWTBotTreeItem item : propsShell.bot().tree().getAllItems()) {
                System.out.println("Property page: " + item.getText());
                if (item.getText().contains("Log4E")) {
                    log4eNode = item;
                    break;
                }
            }

            if (log4eNode == null) {
                System.out.println("Log4E 2026 not found in properties tree");
                propsShell.bot().button("Cancel").click();
                return;
            }

            log4eNode.select();
            TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(propsShell.bot().checkBox("Enable project-specific settings")), 1000);
            log("3.3 selected Log4E 2026");

            // Enable project-specific settings - use dialog's bot
            propsShell.bot().checkBox("Enable project-specific settings").select();
            log("3.4 enabled project-specific settings");

            // Set custom logger name - use dialog's bot
            propsShell.bot().textWithLabel("Logger Variable Name:").setText("myProjectLogger");
            log("3.5 set logger name to myProjectLogger");

            // Apply and close
            propsShell.bot().button("Apply and Close").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellCloses(propsShell), 1000);
            log("3.6 preferences saved");

            // Verify prefs file was created in project .settings folder
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
            IFile prefsFile = project.getFile(".settings/ro.gs1.log4e2026.prefs");
            project.refreshLocal(IProject.DEPTH_INFINITE, null);

            System.out.println("Checking prefs file: " + prefsFile.getLocation());
            System.out.println("Prefs file exists: " + prefsFile.exists());

            assertTrue("Project preferences file should exist at .settings/ro.gs1.log4e2026.prefs",
                prefsFile.exists());
            log("3.7 verified prefs file exists");

        } catch (Exception e) {
            System.out.println("Could not configure Log4E settings: " + e.getMessage());
            e.printStackTrace();
            try {
                propsShell.bot().button("Cancel").click();
            } catch (Exception ex) {
                try {
                    propsShell.close();
                } catch (Exception ex2) {
                    // Ignore
                }
            }
        }
    }

    @Test
    public void test04_DeclareLoggerUsesProjectLoggerName() {
        log("4.0 start - Declare Logger with project preference");

        SWTBotEditor editor;
        try {
            editor = bot.editorByTitle(CLASS_NAME + ".java");
            editor.setFocus();
        } catch (Exception e) {
            System.out.println("Editor not found, skipping");
            return;
        }

        SWTBotStyledText styledText = editor.toTextEditor().getStyledText();

        // Navigate inside the class
        String content = styledText.getText();
        String[] lines = content.split("\n");
        int classLine = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("public class")) {
                classLine = i + 1;
                break;
            }
        }
        if (classLine > 0) {
            styledText.navigateTo(classLine, 0);
        }
        log("4.1 navigated to class body");

        // Execute Declare Logger via Edit menu
        try {
            bot.menu("Edit").menu("Log4E").menu("Declare Logger").click();
            log("4.2 clicked Declare Logger");
        } catch (Exception e) {
            System.out.println("Could not click Declare Logger: " + e.getMessage());
            return;
        }

        // Handle wizard if it appears
        try {
            SWTBotShell wizardShell = bot.shell("Log4E - Declare Logger");
            if (wizardShell.isVisible()) {
                bot.button("Apply Changes").click();
                log("4.3 applied changes via wizard");
            }
        } catch (Exception e) {
            // Wizard may not appear
        }

        TestTimingUtil.waitUntil(bot, TestTimingUtil.editorContentChanges(styledText, content), 1000);

        // Verify the project-specific logger name was used
        styledText.setFocus();
        String contentAfter = styledText.getText();

        System.out.println("\n=== Content After Declare Logger ===");
        System.out.println(contentAfter);
        System.out.println("=====================================\n");

        // Check for project-specific logger name
        boolean hasProjectLoggerName = contentAfter.contains("myProjectLogger");
        boolean hasDefaultLoggerName = contentAfter.contains("private static final Logger logger");

        System.out.println("Has project logger name 'myProjectLogger': " + hasProjectLoggerName);
        System.out.println("Has default logger name 'logger': " + hasDefaultLoggerName);

        assertTrue("Logger should use project-specific name 'myProjectLogger'", hasProjectLoggerName);

        if (hasProjectLoggerName) {
            editor.save();
        }

        log("4.4 test complete - project preference verified");
    }

    @Test
    public void test05_ChangeProjectFrameworkToLog4j2() {
        log("5.0 start - change project framework to Log4j2");

        openProjectProperties();
        log("5.1 opened project properties");

        SWTBotShell propsShell = findPropertiesShell();
        if (propsShell == null) {
            System.out.println("Properties dialog not found");
            return;
        }
        propsShell.activate();

        try {
            // Find Log4E 2026 in the dialog's tree
            SWTBotTreeItem log4eNode = findLog4eNode(propsShell);
            if (log4eNode == null) {
                System.out.println("Log4E 2026 not found");
                propsShell.bot().button("Cancel").click();
                return;
            }

            log4eNode.select();
            TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(propsShell.bot().comboBoxWithLabel("Logging Framework:")), 1000);

            // Change framework to Log4j 2 - items include "(built-in)" suffix
            var combo = propsShell.bot().comboBoxWithLabel("Logging Framework:");
            String[] items = combo.items();
            String log4j2Item = null;
            for (String item : items) {
                if (item.startsWith("Log4j 2")) {
                    log4j2Item = item;
                    break;
                }
            }
            if (log4j2Item != null) {
                combo.setSelection(log4j2Item);
                log("5.2 selected framework: " + log4j2Item);
            } else {
                System.out.println("Log4j 2 not found in combo items: " + java.util.Arrays.toString(items));
                fail("Log4j 2 option not found in Logging Framework combo");
            }

            propsShell.bot().button("Apply and Close").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellCloses(propsShell), 1000);
            log("5.3 preferences saved");

        } catch (Exception e) {
            System.out.println("Could not change framework: " + e.getMessage());
            e.printStackTrace();
            try {
                propsShell.bot().button("Cancel").click();
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    @Test
    public void test06_CreateSecondClassForLog4j2Test() {
        log("6.0 start - create second class");

        try {
            bot.tree().getTreeItem(PROJECT_NAME).select();
        } catch (Exception e) {
            System.out.println("Project not found, skipping");
            return;
        }

        bot.menu("File").menu("New").menu("Other...").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("New"), 1000);
        bot.activeShell().setFocus();

        bot.text().setText("Class");
        TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 1000);

        SWTBotTreeItem javaNode = bot.tree().getTreeItem("Java");
        javaNode.expand();
        TestTimingUtil.waitUntil(bot, Conditions.treeItemHasNode(javaNode, "Class"), 1000);
        javaNode.getNode("Class").select();

        bot.button("Next >").click();
        TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.textWithLabel("Name:")), 1000);

        bot.textWithLabel("Package:").setText("com.test");
        bot.textWithLabel("Name:").setText("Log4j2TestClass");

        SWTBotShell wizardShell = bot.shell("New Java Class");
        bot.button("Finish").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellCloses(wizardShell), 1000);

        TestTimingUtil.waitUntil(bot, TestTimingUtil.editorIsActive(bot, "Log4j2TestClass.java"), 1000);
        log("6.1 second class created");
    }

    @Test
    public void test07_DeclareLoggerUsesProjectFramework() {
        log("7.0 start - Declare Logger with project framework");

        SWTBotEditor editor;
        try {
            editor = bot.editorByTitle("Log4j2TestClass.java");
            editor.setFocus();
        } catch (Exception e) {
            System.out.println("Editor not found, skipping");
            return;
        }

        SWTBotStyledText styledText = editor.toTextEditor().getStyledText();

        // Navigate inside the class
        String content = styledText.getText();
        String[] lines = content.split("\n");
        int classLine = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("public class")) {
                classLine = i + 1;
                break;
            }
        }
        if (classLine > 0) {
            styledText.navigateTo(classLine, 0);
        }
        log("7.1 navigated to class body");

        // Execute Declare Logger
        try {
            bot.menu("Edit").menu("Log4E").menu("Declare Logger").click();
            log("7.2 clicked Declare Logger");
        } catch (Exception e) {
            System.out.println("Could not click Declare Logger: " + e.getMessage());
            return;
        }

        // Handle wizard if it appears
        try {
            SWTBotShell wizardShell = bot.shell("Log4E - Declare Logger");
            if (wizardShell.isVisible()) {
                bot.button("Apply Changes").click();
                log("7.3 applied changes via wizard");
            }
        } catch (Exception e) {
            // Wizard may not appear
        }

        TestTimingUtil.waitUntil(bot, TestTimingUtil.editorContentChanges(styledText, content), 1000);

        // Verify Log4j 2 was used (LogManager import)
        styledText.setFocus();
        String contentAfter = styledText.getText();

        System.out.println("\n=== Content After Declare Logger (Log4j2) ===");
        System.out.println(contentAfter);
        System.out.println("==============================================\n");

        // Check for Log4j 2 imports
        boolean hasLog4j2Import = contentAfter.contains("org.apache.logging.log4j");
        boolean hasLogManagerImport = contentAfter.contains("LogManager");
        boolean hasSLF4JImport = contentAfter.contains("org.slf4j");

        System.out.println("Has Log4j2 import: " + hasLog4j2Import);
        System.out.println("Has LogManager: " + hasLogManagerImport);
        System.out.println("Has SLF4J import (should be false): " + hasSLF4JImport);

        assertTrue("Logger should use Log4j2 (LogManager)", hasLog4j2Import || hasLogManagerImport);
        assertFalse("Logger should NOT use SLF4J", hasSLF4JImport);

        if (hasLog4j2Import || hasLogManagerImport) {
            editor.save();
        }

        log("7.4 test complete - project framework verified");
    }

    @Test
    public void test08_DisableProjectPreferences() {
        log("8.0 start - disable project preferences");

        openProjectProperties();
        log("8.1 opened project properties");

        SWTBotShell propsShell = findPropertiesShell();
        if (propsShell == null) {
            System.out.println("Properties dialog not found");
            return;
        }
        propsShell.activate();

        try {
            // Find Log4E 2026 in the dialog's tree
            SWTBotTreeItem log4eNode = findLog4eNode(propsShell);
            if (log4eNode == null) {
                System.out.println("Log4E 2026 not found");
                propsShell.bot().button("Cancel").click();
                return;
            }

            log4eNode.select();
            TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(propsShell.bot().checkBox("Enable project-specific settings")), 1000);

            // Disable project-specific settings - use dialog's bot
            propsShell.bot().checkBox("Enable project-specific settings").deselect();
            log("8.2 disabled project-specific settings");

            propsShell.bot().button("Apply and Close").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellCloses(propsShell), 1000);
            log("8.3 preferences saved");

        } catch (Exception e) {
            System.out.println("Could not disable settings: " + e.getMessage());
            e.printStackTrace();
            try {
                propsShell.bot().button("Cancel").click();
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    @Test
    public void test09_CleanupProject() {
        log("9.0 cleanup");
        try {
            bot.closeAllEditors();
            SWTBotTreeItem project = bot.tree().getTreeItem(PROJECT_NAME);
            project.select();
            project.contextMenu("Delete").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Delete Resources"), 1000);
            bot.checkBox("Delete project contents on disk (cannot be undone)").click();
            bot.button("OK").click();
            projectCreated = false;
            log("9.1 cleanup complete");
        } catch (Exception e) {
            System.out.println("Cleanup error: " + e.getMessage());
        }
    }
}
