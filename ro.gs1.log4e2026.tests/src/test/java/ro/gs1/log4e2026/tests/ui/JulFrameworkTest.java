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
 * Integration tests for JUL (java.util.logging) framework code generation.
 * Verifies that switching to JDK Logging produces correct JUL-specific code:
 * - Logger declaration uses java.util.logging.Logger
 * - Log methods use fine/info/warning/severe instead of debug/info/warn/error
 * - Is-enabled checks use isLoggable(Level.XXX)
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JulFrameworkTest {

    private static final String PROJECT_NAME = "JulTestProject";
    private static final String CLASS_NAME = "JulTestClass";
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

    private void openPreferences() {
        bot.menu("Window").menu("Preferences...").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 1000);
    }

    @Test
    public void test1_CreateJavaProject() {
        log("JUL 1.0 start - create Java project");

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
        log("JUL 1.1 project created");
    }

    @Test
    public void test2_SwitchToJulFramework() {
        log("JUL 2.0 start - switch to JDK Logging framework");

        // Open preferences - use "Preferences..." with ellipsis on Linux
        openPreferences();
        SWTBotShell prefsShell = bot.shell("Preferences");
        prefsShell.setFocus();

        // Navigate to Log4E > Templates
        SWTBotTreeItem log4eItem = prefsShell.bot().tree().getTreeItem("Log4E 2026");
        log4eItem.expand();
        log4eItem.getNode("Templates").select();

        // Find the profile combo and select JDK Logging
        org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo profileCombo =
                prefsShell.bot().comboBox(0);
        String[] items = profileCombo.items();
        System.out.println("Available profiles: " + java.util.Arrays.toString(items));

        // Find JDK Logging in the combo
        int julIndex = -1;
        for (int i = 0; i < items.length; i++) {
            if (items[i].contains("JDK") || items[i].contains("JUL") || items[i].contains("Logging")) {
                julIndex = i;
                break;
            }
        }
        assertTrue("JDK Logging profile should be available", julIndex >= 0);
        profileCombo.setSelection(julIndex);

        log("JUL 2.1 selected JDK Logging profile");

        // Apply and close
        prefsShell.bot().button("Apply and Close").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellCloses(prefsShell), 1000);
        log("JUL 2.2 preferences applied");
    }

    @Test
    public void test3_CreateJavaClass() {
        log("JUL 3.0 start - create Java class");

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

        bot.textWithLabel("Package:").setText("com.test.jul");
        bot.textWithLabel("Name:").setText(CLASS_NAME);

        SWTBotShell wizardShell = bot.shell("New Java Class");
        bot.button("Finish").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellCloses(wizardShell), 1000);

        TestTimingUtil.waitUntil(bot, TestTimingUtil.editorIsActive(bot, CLASS_NAME + ".java"), 1000);

        SWTBotEditor editor = bot.editorByTitle(CLASS_NAME + ".java");
        editor.setFocus();
        SWTBotStyledText styledText = editor.toTextEditor().getStyledText();

        String classContent = """
                package com.test.jul;

                public class JulTestClass {

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
                }
                """;

        styledText.setText(classContent);
        editor.save();
        log("JUL 3.1 class created");
    }

    @Test
    public void test4_DeclareLogger() {
        log("JUL 4.0 start - declare logger");

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
        styledText.navigateTo(3, 0);

        var contextMenu = styledText.contextMenu();
        for (String item : contextMenu.menuItems()) {
            if (item.contains("Log4E")) {
                contextMenu.menu(item).menu("Declare Logger").click();
                break;
            }
        }

        TestTimingUtil.waitUntil(bot, TestTimingUtil.editorContentChanges(styledText, contentBefore), 1000);

        try {
            SWTBotShell activeShell = bot.activeShell();
            if (!activeShell.getText().contains(PROJECT_NAME)) {
                activeShell.pressShortcut(Keystrokes.ESC);
            }
        } catch (Exception e) {
            // Ignore
        }

        editor.save();
        String content = styledText.getText();

        System.out.println("\n=== After Declare Logger (JUL) ===");
        System.out.println(content);
        System.out.println("===================================\n");

        // Verify JUL-specific logger declaration
        assertTrue("Should import java.util.logging.Logger",
                content.contains("java.util.logging.Logger"));
        assertTrue("Should declare Logger using Logger.getLogger()",
                content.contains("Logger.getLogger("));

        log("JUL 4.1 logger declared with JUL imports");
    }

    @Test
    public void test5_LogThisMethod() {
        log("JUL 5.0 start - log this method");

        SWTBotEditor editor;
        try {
            editor = bot.editorByTitle(CLASS_NAME + ".java");
            editor.setFocus();
        } catch (Exception e) {
            System.out.println("Editor not found, skipping");
            return;
        }

        SWTBotStyledText styledText = editor.toTextEditor().getStyledText();

        // Navigate to the processData method
        String content = styledText.getText();
        String[] lines = content.split("\n");
        int methodLine = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("processData")) {
                methodLine = i;
                break;
            }
        }
        assertTrue("processData method should exist", methodLine >= 0);
        styledText.navigateTo(methodLine, 10);

        String contentBefore = styledText.getText();

        // Execute Log this method
        try {
            bot.menu("Edit").menu("Log4E").menu("Log this method").click();
            log("JUL 5.1 clicked Log this method");
        } catch (Exception e) {
            System.out.println("Edit menu failed, trying context menu: " + e.getMessage());
            var contextMenu = styledText.contextMenu();
            for (String item : contextMenu.menuItems()) {
                if (item.contains("Log4E")) {
                    contextMenu.menu(item).menu("Log this method").click();
                    break;
                }
            }
        }

        TestTimingUtil.waitUntil(bot, TestTimingUtil.editorContentChanges(styledText, contentBefore), 1000);
        editor.save();

        String contentAfter = styledText.getText();
        System.out.println("\n=== After Log this method (JUL) ===");
        String[] linesAfter = contentAfter.split("\n");
        for (int i = 0; i < linesAfter.length; i++) {
            System.out.println(String.format("%2d: %s", i + 1, linesAfter[i]));
        }
        System.out.println("=====================================\n");

        // Verify JUL-specific method names are used, NOT SLF4J
        boolean hasJulMethod = contentAfter.contains("logger.fine(")
                || contentAfter.contains("logger.info(")
                || contentAfter.contains("logger.warning(")
                || contentAfter.contains("logger.severe(")
                || contentAfter.contains("logger.finest(")
                || contentAfter.contains("logger.finer(")
                || contentAfter.contains("logger.config(");
        boolean hasSlfMethod = contentAfter.contains("logger.debug(")
                || contentAfter.contains("logger.warn(")
                || contentAfter.contains("logger.error(")
                || contentAfter.contains("logger.trace(");

        System.out.println("Has JUL methods: " + hasJulMethod);
        System.out.println("Has SLF4J methods: " + hasSlfMethod);

        assertTrue("Should use JUL method names (fine/info/warning/severe), not SLF4J (debug/warn/error)",
                hasJulMethod);
        assertFalse("Should NOT use SLF4J method names (debug/warn/error/trace)",
                hasSlfMethod);

        log("JUL 5.2 verified JUL method names");
    }

    @Test
    public void test6_LogThisClass() {
        log("JUL 6.0 start - log this class");

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

        // Navigate to class body
        String[] lines = contentBefore.split("\n");
        int classLine = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("public class")) {
                classLine = i + 1;
                break;
            }
        }
        assertTrue("Class declaration should exist", classLine >= 0);
        styledText.navigateTo(classLine, 0);

        // Execute Log this class
        try {
            bot.menu("Edit").menu("Log4E").menu("Log this class").click();
            log("JUL 6.1 clicked Log this class");
        } catch (Exception e) {
            var contextMenu = styledText.contextMenu();
            for (String item : contextMenu.menuItems()) {
                if (item.contains("Log4E")) {
                    contextMenu.menu(item).menu("Log this class").click();
                    break;
                }
            }
        }

        TestTimingUtil.waitUntil(bot, TestTimingUtil.editorContentChanges(styledText, contentBefore), 1000);
        editor.save();

        String contentAfter = styledText.getText();
        System.out.println("\n=== After Log this class (JUL) ===");
        String[] linesAfter = contentAfter.split("\n");
        for (int i = 0; i < linesAfter.length; i++) {
            System.out.println(String.format("%2d: %s", i + 1, linesAfter[i]));
        }
        System.out.println("===================================\n");

        // Verify all logging uses JUL syntax
        assertFalse("Should NOT contain logger.debug()",
                contentAfter.contains("logger.debug("));
        assertFalse("Should NOT contain logger.error()",
                contentAfter.contains("logger.error("));
        assertFalse("Should NOT contain logger.warn()",
                contentAfter.contains("logger.warn("));
        assertFalse("Should NOT contain logger.trace()",
                contentAfter.contains("logger.trace("));

        // Should contain JUL methods
        boolean hasJulLogCalls = contentAfter.contains("logger.fine(")
                || contentAfter.contains("logger.info(")
                || contentAfter.contains("logger.warning(")
                || contentAfter.contains("logger.severe(")
                || contentAfter.contains("logger.config(");
        assertTrue("Should contain JUL log method calls", hasJulLogCalls);

        log("JUL 6.2 all methods use JUL syntax");
    }

    @Test
    public void test7_SwitchBackToSlf4j() {
        log("JUL 7.0 start - switch back to SLF4J");

        openPreferences();
        SWTBotShell prefsShell = bot.shell("Preferences");
        prefsShell.setFocus();

        // Navigate to Log4E > Templates
        SWTBotTreeItem log4eItem = prefsShell.bot().tree().getTreeItem("Log4E 2026");
        log4eItem.expand();
        log4eItem.getNode("Templates").select();

        // Find and select SLF4J
        org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo profileCombo =
                prefsShell.bot().comboBox(0);
        String[] items = profileCombo.items();
        int slf4jIndex = -1;
        for (int i = 0; i < items.length; i++) {
            if (items[i].contains("SLF4J")) {
                slf4jIndex = i;
                break;
            }
        }
        assertTrue("SLF4J profile should be available", slf4jIndex >= 0);
        profileCombo.setSelection(slf4jIndex);

        prefsShell.bot().button("Apply and Close").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellCloses(prefsShell), 1000);
        log("JUL 7.1 switched back to SLF4J");
    }

    @Test
    public void test8_CleanupProject() {
        log("JUL 8.0 cleanup");
        try {
            bot.closeAllEditors();
            SWTBotTreeItem project = bot.tree().getTreeItem(PROJECT_NAME);
            project.select();
            project.contextMenu("Delete").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Delete Resources"), 1000);
            bot.checkBox("Delete project contents on disk (cannot be undone)").click();
            bot.button("OK").click();
            projectCreated = false;
            log("JUL 8.1 cleanup complete");
        } catch (Exception e) {
            System.out.println("Cleanup error: " + e.getMessage());
        }
    }
}
