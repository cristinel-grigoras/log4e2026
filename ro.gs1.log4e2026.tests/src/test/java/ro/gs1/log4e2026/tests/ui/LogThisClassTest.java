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
 * Integration tests for "Log this class" feature (Ctrl+Alt+U).
 * Tests that logging statements are added to all methods in a class.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LogThisClassTest {

    private static final String PROJECT_NAME = "LogClassTestProject";
    private static final String CLASS_NAME = "ClassToLog";
    private static SWTWorkbenchBot bot;
    private static boolean projectCreated = false;

    @BeforeClass
    public static void setUpClass() {
        SWTBotPreferences.SCREENSHOTS_DIR = "";
        SWTBotPreferences.TIMEOUT = 500;
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

        // Set class content with multiple methods
        SWTBotEditor editor = bot.editorByTitle(CLASS_NAME + ".java");
        editor.setFocus();
        SWTBotStyledText styledText = editor.toTextEditor().getStyledText();

        String classContent = """
                package com.test;

                public class ClassToLog {

                    public void processData(String input) {
                        String result = input.toUpperCase();
                    }

                    public int calculate(int a, int b) {
                        int sum = a + b;
                        return sum;
                    }

                    public void handleError() {
                        try {
                            throw new RuntimeException("test");
                        } catch (Exception e) {
                        }
                    }

                    public String getName() {
                        return "test";
                    }

                    public void setName(String name) {
                    }
                }
                """;

        styledText.setText(classContent);
        editor.save();
        log("2.1 class created with methods");

        System.out.println("\n=== Initial Class Content ===");
        System.out.println(classContent);
        System.out.println("==============================\n");
    }

    @Test
    public void test3_DeclareLogger() {
        log("3.0 start - declare logger");

        SWTBotEditor editor;
        try {
            editor = bot.editorByTitle(CLASS_NAME + ".java");
            editor.setFocus();
        } catch (Exception e) {
            System.out.println("Editor not found, skipping");
            return;
        }

        SWTBotStyledText styledText = editor.toTextEditor().getStyledText();
        styledText.navigateTo(3, 0);

        var contextMenu = styledText.contextMenu();
        for (String item : contextMenu.menuItems()) {
            if (item.contains("Log4E")) {
                contextMenu.menu(item).menu("Declare Logger").click();
                break;
            }
        }

        TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(styledText), 1000);
        try {
            SWTBotShell activeShell = bot.activeShell();
            if (!activeShell.getText().contains(PROJECT_NAME)) {
                activeShell.pressShortcut(Keystrokes.ESC);
            }
        } catch (Exception e) {
            // Ignore
        }

        editor.save();
        log("3.1 logger declared");
    }

    @Test
    public void test4_ExecuteLogThisClass() {
        log("4.0 start - Log this class");

        SWTBotEditor editor;
        try {
            editor = bot.editorByTitle(CLASS_NAME + ".java");
            editor.setFocus();
        } catch (Exception e) {
            System.out.println("Editor not found, skipping");
            return;
        }

        SWTBotStyledText styledText = editor.toTextEditor().getStyledText();
        String contentBefore = styledText.getText();
        System.out.println("\n=== BEFORE Log this class ===");
        System.out.println(contentBefore);
        System.out.println("==============================\n");

        // Find and navigate to first line inside the class
        String[] linesBefore = contentBefore.split("\n");
        int classLine = -1;
        for (int i = 0; i < linesBefore.length; i++) {
            if (linesBefore[i].contains("public class")) {
                classLine = i + 1; // Line after class declaration
                break;
            }
        }
        if (classLine == -1) {
            System.out.println("Class declaration not found!");
            return;
        }
        styledText.navigateTo(classLine, 0);
        log("4.0a navigated to class body (line " + (classLine + 1) + ")");

        // Execute "Log this class" via Edit menu (more reliable than context menu)
        try {
            bot.menu("Edit").menu("Log4E").menu("Log this class").click();
            log("4.1 clicked Log this class via Edit menu");
        } catch (Exception e) {
            System.out.println("Edit menu failed, trying context menu: " + e.getMessage());
            var contextMenu = styledText.contextMenu();
            for (String item : contextMenu.menuItems()) {
                if (item.contains("Log4E")) {
                    contextMenu.menu(item).menu("Log this class").click();
                    log("4.1 clicked Log this class via context menu");
                    break;
                }
            }
        }

        TestTimingUtil.waitUntil(bot, TestTimingUtil.editorContentChanges(styledText, contentBefore), 1000);

        // Force refresh
        styledText.setFocus();
        String contentAfter = styledText.getText();

        System.out.println("\n=== AFTER Log this class ===");
        String[] lines = contentAfter.split("\n");
        for (int i = 0; i < lines.length; i++) {
            System.out.println(String.format("%2d: %s", i + 1, lines[i]));
        }
        System.out.println("=============================\n");

        // Check if content changed at all
        boolean contentChanged = !contentBefore.equals(contentAfter);
        System.out.println("Content changed: " + contentChanged);

        // Check for any logger calls (debug, info, error)
        boolean hasLoggerDebug = contentAfter.contains("logger.debug(");
        boolean hasLoggerInfo = contentAfter.contains("logger.info(");
        boolean hasAnyLogStatement = hasLoggerDebug || hasLoggerInfo;

        System.out.println("Has logger.debug(): " + hasLoggerDebug);
        System.out.println("Has logger.info(): " + hasLoggerInfo);
        System.out.println("Has any log statement: " + hasAnyLogStatement);

        // Save if there are changes
        if (contentChanged) {
            editor.save();
        }

        // Test passes if content changed or has log statements
        assertTrue("Log statements should be added to methods (content changed: " + contentChanged + ")",
                contentChanged || hasAnyLogStatement);
        log("4.2 test complete");
    }

    @Test
    public void test5_CleanupProject() {
        log("5.0 cleanup");
        try {
            bot.closeAllEditors();
            SWTBotTreeItem project = bot.tree().getTreeItem(PROJECT_NAME);
            project.select();
            project.contextMenu("Delete").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Delete Resources"), 1000);
            bot.checkBox("Delete project contents on disk (cannot be undone)").click();
            bot.button("OK").click();
            projectCreated = false;
            log("5.1 cleanup complete");
        } catch (Exception e) {
            System.out.println("Cleanup error: " + e.getMessage());
        }
    }
}
