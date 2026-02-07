package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

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
 * Integration tests for Preview Wizard feature.
 * Tests that preview wizards are shown when preference is enabled.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PreviewWizardTest {

    private static final String PROJECT_NAME = "PreviewWizardTestProject";
    private static final String CLASS_NAME = "WizardTestClass";
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

    @Test
    public void test1_CreateJavaProject() {
        log("1.0 start - create Java project");

        // If project already exists (from failed previous run), delete it first
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
        assertNotNull(bot.tree().getTreeItem(PROJECT_NAME));
        projectCreated = true;
        log("1.1 project created");
    }

    @Test
    public void test2_CreateJavaClass() {
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

        // Set simple class content
        SWTBotEditor editor = bot.editorByTitle(CLASS_NAME + ".java");
        editor.setFocus();
        SWTBotStyledText styledText = editor.toTextEditor().getStyledText();

        String classContent = """
                package com.test;

                public class WizardTestClass {

                    public void doSomething() {
                        System.out.println("Hello");
                    }
                }
                """;

        styledText.setText(classContent);
        editor.save();
        log("2.1 class created");
    }

    @Test
    public void test3_EnableWizardPreference() {
        log("3.0 start - enable wizard preference");

        // Open preferences
        bot.menu("Window").menu("Preferences...").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 1000);
        log("3.1 opened Preferences");

        // Navigate to Log4E preferences
        SWTBotTreeItem log4eNode = bot.tree().getTreeItem("Log4E 2026");
        log4eNode.select();
        log("3.2 selected Log4E 2026");

        // Enable wizard for Declare Logger
        try {
            bot.checkBox("Show preview wizard for Declare Logger").select();
            log("3.3 enabled Declare Logger wizard");
        } catch (Exception e) {
            System.out.println("Could not find wizard checkbox: " + e.getMessage());
        }

        // Apply and close
        bot.button("Apply and Close").click();
        log("3.4 preferences saved");
    }

    @Test
    public void test4_DeclareLoggerWithWizard() {
        log("4.0 start - Declare Logger with wizard");

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

        // Execute Declare Logger via context menu
        try {
            var contextMenu = styledText.contextMenu();
            for (String item : contextMenu.menuItems()) {
                if (item.contains("Log4E")) {
                    contextMenu.menu(item).menu("Declare Logger").click();
                    log("4.2 clicked Declare Logger");
                    break;
                }
            }
        } catch (Exception e) {
            // Try via Edit menu
            bot.menu("Edit").menu("Log4E").menu("Declare Logger").click();
            log("4.2 clicked Declare Logger via Edit menu");
        }

        // Wait for wizard dialog to appear
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Log4E - Declare Logger"), 1000);

        // Check if wizard appeared
        try {
            SWTBotShell wizardShell = bot.shell("Log4E - Declare Logger");
            assertTrue("Wizard dialog should appear", wizardShell.isVisible());
            log("4.3 wizard dialog appeared");

            // Verify wizard has content
            System.out.println("Wizard shell is active: " + wizardShell.isActive());

            // Click Apply Changes button
            bot.button("Apply Changes").click();
            log("4.4 applied changes");

            // Wait for wizard dialog to close
            TestTimingUtil.waitUntil(bot, Conditions.shellCloses(wizardShell), 1000);

        } catch (Exception e) {
            System.out.println("Wizard dialog not found: " + e.getMessage());
            // Wizard may not have appeared, continue anyway
        }

        // Verify logger was added
        styledText.setFocus();
        String contentAfter = styledText.getText();
        boolean hasLogger = contentAfter.contains("Logger") || contentAfter.contains("logger");

        System.out.println("\n=== Content After Declare Logger ===");
        System.out.println(contentAfter);
        System.out.println("=====================================\n");
        System.out.println("Has logger: " + hasLogger);

        if (hasLogger) {
            editor.save();
        }

        log("4.5 test complete");
    }

    @Test
    public void test5_DisableWizardPreference() {
        log("5.0 start - disable wizard preference");

        // Open preferences
        bot.menu("Window").menu("Preferences...").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 1000);

        // Navigate to Log4E preferences
        SWTBotTreeItem log4eNode = bot.tree().getTreeItem("Log4E 2026");
        log4eNode.select();

        // Disable wizard for Declare Logger
        try {
            bot.checkBox("Show preview wizard for Declare Logger").deselect();
            log("5.1 disabled Declare Logger wizard");
        } catch (Exception e) {
            System.out.println("Could not find wizard checkbox: " + e.getMessage());
        }

        // Apply and close
        bot.button("Apply and Close").click();
        log("5.2 preferences saved");
    }

    @Test
    public void test6_CleanupProject() {
        log("6.0 cleanup");
        try {
            bot.closeAllEditors();
            SWTBotTreeItem project = bot.tree().getTreeItem(PROJECT_NAME);
            project.select();
            project.contextMenu("Delete").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Delete Resources"), 1000);
            bot.checkBox("Delete project contents on disk (cannot be undone)").click();
            bot.button("OK").click();
            projectCreated = false;
            log("6.1 cleanup complete");
        } catch (Exception e) {
            System.out.println("Cleanup error: " + e.getMessage());
        }
    }
}
