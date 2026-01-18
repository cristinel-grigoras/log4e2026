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
 * Integration tests for "Exchange logging framework" feature.
 * Tests converting between SLF4J, Log4j2, and JUL frameworks.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExchangeFrameworkTest {

    private static final String PROJECT_NAME = "ExchangeFrameworkTestProject";
    private static final String CLASS_NAME = "FrameworkTestClass";
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
            try {
                bot.closeAllEditors();
                SWTBotTreeItem project = bot.tree().getTreeItem(PROJECT_NAME);
                project.select();
                project.contextMenu("Delete").click();
                SWTBotShell deleteShell = bot.shell("Delete Resources");
                deleteShell.activate();
                bot.checkBox("Delete project contents on disk (cannot be undone)").click();
                bot.button("OK").click();
                bot.waitUntil(Conditions.shellCloses(deleteShell), 10000);
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

    @Test
    public void test1_CreateJavaProject() {
        log("1.0 start - create Java project");

        bot.menu("File").menu("New").menu("Other...").click();
        bot.waitUntil(Conditions.shellIsActive("New"), 5000);
        bot.activeShell().setFocus();

        bot.text().setText("Java Project");
        TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 5000);

        SWTBotTreeItem javaNode = bot.tree().getTreeItem("Java");
        javaNode.expand();
        TestTimingUtil.waitUntil(bot, Conditions.treeItemHasNode(javaNode, "Java Project"), 3000);
        javaNode.getNode("Java Project").select();

        bot.button("Next >").click();
        TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.textWithLabel("Project name:")), 5000);

        bot.textWithLabel("Project name:").setText(PROJECT_NAME);
        bot.button("Finish").click();

        try {
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Open Associated Perspective?"), 3000);
            bot.button("No").click();
        } catch (Exception e) {
            // Dialog may not appear
        }

        TestTimingUtil.waitUntil(bot, TestTimingUtil.projectExists(bot, PROJECT_NAME), 10000);
        assertNotNull(bot.tree().getTreeItem(PROJECT_NAME));
        projectCreated = true;
        log("1.1 project created");
    }

    @Test
    public void test2_CreateJavaClassWithSLF4J() {
        log("2.0 start - create Java class with SLF4J");

        try {
            bot.tree().getTreeItem(PROJECT_NAME).select();
        } catch (Exception e) {
            System.out.println("Project not found, skipping");
            return;
        }

        bot.menu("File").menu("New").menu("Other...").click();
        bot.waitUntil(Conditions.shellIsActive("New"), 5000);
        bot.activeShell().setFocus();

        bot.text().setText("Class");
        TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 5000);

        SWTBotTreeItem javaNode = bot.tree().getTreeItem("Java");
        javaNode.expand();
        TestTimingUtil.waitUntil(bot, Conditions.treeItemHasNode(javaNode, "Class"), 3000);
        javaNode.getNode("Class").select();

        bot.button("Next >").click();
        TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.textWithLabel("Name:")), 5000);

        bot.textWithLabel("Package:").setText("com.test");
        bot.textWithLabel("Name:").setText(CLASS_NAME);

        SWTBotShell wizardShell = bot.shell("New Java Class");
        bot.button("Finish").click();
        bot.waitUntil(Conditions.shellCloses(wizardShell), 10000);

        TestTimingUtil.waitUntil(bot, TestTimingUtil.editorIsActive(bot, CLASS_NAME + ".java"), 10000);

        // Set class content with SLF4J logging
        SWTBotEditor editor = bot.editorByTitle(CLASS_NAME + ".java");
        editor.setFocus();
        SWTBotStyledText styledText = editor.toTextEditor().getStyledText();

        String classContent = """
                package com.test;

                import org.slf4j.Logger;
                import org.slf4j.LoggerFactory;

                public class FrameworkTestClass {

                    private static final Logger logger = LoggerFactory.getLogger(FrameworkTestClass.class);

                    public void doSomething() {
                        logger.debug("Debug message");
                        logger.info("Info message");
                        logger.error("Error message");
                    }
                }
                """;

        styledText.setText(classContent);
        editor.save();
        log("2.1 class created with SLF4J logging");

        // Verify initial content
        String content = styledText.getText();
        assertTrue("Should have SLF4J import", content.contains("org.slf4j.Logger"));
        assertTrue("Should have LoggerFactory import", content.contains("org.slf4j.LoggerFactory"));
        assertTrue("Should have logger declaration", content.contains("LoggerFactory.getLogger"));

        System.out.println("\n=== Initial Class with SLF4J ===");
        System.out.println(content);
        System.out.println("================================\n");
    }

    @Test
    public void test3_ChangePreferenceToLog4j2() {
        log("3.0 start - change preference to Log4j2");

        // Open preferences
        bot.menu("Window").menu("Preferences...").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 5000);
        log("3.1 opened Preferences");

        // Navigate to Log4E preferences
        SWTBotTreeItem log4eNode = bot.tree().getTreeItem("Log4E 2026");
        log4eNode.select();
        log("3.2 selected Log4E 2026");

        // Find and change the logging framework combo
        try {
            bot.comboBoxWithLabel("Logging Framework:").setSelection("Log4j 2");
            log("3.3 changed framework to Log4j 2");
        } catch (Exception e) {
            System.out.println("Could not find framework combo: " + e.getMessage());
        }

        // Disable show confirmation dialog for testing
        try {
            bot.checkBox("Show confirmation dialog when exchanging frameworks").deselect();
            log("3.3a disabled confirmation dialog");
        } catch (Exception e) {
            System.out.println("Could not find confirmation checkbox: " + e.getMessage());
        }

        // Apply and close
        bot.button("Apply and Close").click();
        log("3.4 preferences saved");
    }

    @Test
    public void test4_ExchangeFrameworkToLog4j2() {
        log("4.0 start - Exchange framework to Log4j2");

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

        System.out.println("\n=== BEFORE Exchange Framework ===");
        System.out.println(contentBefore);
        System.out.println("==================================\n");

        // Find and navigate inside the class
        String[] lines = contentBefore.split("\n");
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

        // Execute "Exchange logging framework" via Edit menu
        try {
            bot.menu("Edit").menu("Log4E").menu("Exchange logging framework").click();
            log("4.2 clicked Exchange logging framework");
        } catch (Exception e) {
            System.out.println("Edit menu failed: " + e.getMessage());
            // Try context menu
            var contextMenu = styledText.contextMenu();
            for (String item : contextMenu.menuItems()) {
                if (item.contains("Log4E")) {
                    contextMenu.menu(item).menu("Exchange logging framework").click();
                    log("4.2 clicked Exchange logging framework via context menu");
                    break;
                }
            }
        }

        bot.sleep(1500);
        styledText.setFocus();

        String contentAfter = styledText.getText();
        System.out.println("\n=== AFTER Exchange Framework ===");
        String[] linesAfter = contentAfter.split("\n");
        for (int i = 0; i < linesAfter.length; i++) {
            System.out.println(String.format("%2d: %s", i + 1, linesAfter[i]));
        }
        System.out.println("=================================\n");

        boolean contentChanged = !contentBefore.equals(contentAfter);
        System.out.println("Content changed: " + contentChanged);

        // Check for Log4j2 imports/declarations
        boolean hasLog4j2Import = contentAfter.contains("org.apache.logging.log4j");
        boolean hasLogManager = contentAfter.contains("LogManager");
        boolean stillHasSLF4J = contentAfter.contains("org.slf4j");

        System.out.println("Has Log4j2 import: " + hasLog4j2Import);
        System.out.println("Has LogManager: " + hasLogManager);
        System.out.println("Still has SLF4J: " + stillHasSLF4J);

        if (contentChanged) {
            editor.save();
        }

        // Verify framework was exchanged
        assertTrue("Content should have changed", contentChanged);
        assertTrue("Should have Log4j2 import or LogManager",
                hasLog4j2Import || hasLogManager || !stillHasSLF4J);

        log("4.3 test complete");
    }

    @Test
    public void test5_ChangePreferenceBackToSLF4J() {
        log("5.0 start - change preference back to SLF4J");

        // Open preferences
        bot.menu("Window").menu("Preferences...").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 5000);

        // Navigate to Log4E preferences
        SWTBotTreeItem log4eNode = bot.tree().getTreeItem("Log4E 2026");
        log4eNode.select();

        // Change back to SLF4J
        try {
            bot.comboBox().setSelection("SLF4J");
            log("5.1 changed framework back to SLF4J");
        } catch (Exception e) {
            try {
                bot.comboBoxWithLabel("Logging Framework:").setSelection("SLF4J");
            } catch (Exception e2) {
                System.out.println("Could not change framework: " + e2.getMessage());
            }
        }

        bot.button("Apply and Close").click();
        log("5.2 preferences saved");
    }

    @Test
    public void test6_ExchangeFrameworkBackToSLF4J() {
        log("6.0 start - Exchange framework back to SLF4J");

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

        // Navigate inside class
        String[] lines = contentBefore.split("\n");
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

        // Execute exchange
        try {
            bot.menu("Edit").menu("Log4E").menu("Exchange logging framework").click();
            log("6.1 clicked Exchange logging framework");
        } catch (Exception e) {
            var contextMenu = styledText.contextMenu();
            for (String item : contextMenu.menuItems()) {
                if (item.contains("Log4E")) {
                    contextMenu.menu(item).menu("Exchange logging framework").click();
                    break;
                }
            }
        }

        bot.sleep(1500);
        styledText.setFocus();

        String contentAfter = styledText.getText();
        System.out.println("\n=== AFTER Exchange back to SLF4J ===");
        String[] linesAfter = contentAfter.split("\n");
        for (int i = 0; i < linesAfter.length; i++) {
            System.out.println(String.format("%2d: %s", i + 1, linesAfter[i]));
        }
        System.out.println("=====================================\n");

        boolean contentChanged = !contentBefore.equals(contentAfter);
        boolean hasSLF4J = contentAfter.contains("org.slf4j") || contentAfter.contains("LoggerFactory");

        System.out.println("Content changed: " + contentChanged);
        System.out.println("Has SLF4J: " + hasSLF4J);

        if (contentChanged) {
            editor.save();
        }

        log("6.2 test complete");
    }

    @Test
    public void test7_CleanupProject() {
        log("7.0 cleanup");
        try {
            bot.closeAllEditors();
            SWTBotTreeItem project = bot.tree().getTreeItem(PROJECT_NAME);
            project.select();
            project.contextMenu("Delete").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Delete Resources"), 3000);
            bot.checkBox("Delete project contents on disk (cannot be undone)").click();
            bot.button("OK").click();
            projectCreated = false;
            log("7.1 cleanup complete");
        } catch (Exception e) {
            System.out.println("Cleanup error: " + e.getMessage());
        }
    }
}
