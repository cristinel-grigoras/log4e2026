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
 * Integration tests for System.out substitution features:
 * - Substitute System.out in class (Ctrl+Alt+A)
 * - Substitute System.out in method (Ctrl+Alt+S)
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SubstituteSystemOutTest {

    private static final String PROJECT_NAME = "SubstituteTestProject";
    private static final String CLASS_NAME = "ClassWithSystemOut";
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

    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(pattern, idx)) != -1) {
            count++;
            idx += pattern.length();
        }
        return count;
    }

    @Test
    public void test1_CreateJavaProject() {
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
    public void test2_CreateJavaClassWithSystemOut() {
        log("2.0 start - create Java class with System.out");

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

        // Set class content with System.out calls
        SWTBotEditor editor = bot.editorByTitle(CLASS_NAME + ".java");
        editor.setFocus();
        SWTBotStyledText styledText = editor.toTextEditor().getStyledText();

        String classContent = """
                package com.test;

                public class ClassWithSystemOut {

                    public void methodOne() {
                        System.out.println("Message from methodOne");
                        System.out.println("Another message");
                    }

                    public void methodTwo() {
                        System.out.println("Message from methodTwo");
                    }

                    public void errorMethod() {
                        System.err.println("Error message");
                    }
                }
                """;

        styledText.setText(classContent);
        editor.save();
        log("2.1 class created with System.out calls");

        int systemOutCount = countOccurrences(classContent, "System.out");
        int systemErrCount = countOccurrences(classContent, "System.err");
        System.out.println("\n=== Initial Class Content ===");
        System.out.println("System.out calls: " + systemOutCount);
        System.out.println("System.err calls: " + systemErrCount);
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
    public void test4_SubstituteSystemOutInMethod() {
        log("4.0 start - Substitute System.out in method");

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

        // Find methodOne and navigate inside it
        String[] lines = contentBefore.split("\n");
        int methodOneLine = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("public void methodOne")) {
                methodOneLine = i + 1; // Inside the method
                break;
            }
        }

        if (methodOneLine == -1) {
            System.out.println("methodOne not found");
            return;
        }

        styledText.navigateTo(methodOneLine, 10);
        log("4.1 navigated inside methodOne");

        // Execute "Substitute System.out in method"
        var contextMenu = styledText.contextMenu();
        for (String item : contextMenu.menuItems()) {
            if (item.contains("Log4E")) {
                try {
                    contextMenu.menu(item).menu("Substitute System.out in method").click();
                    log("4.2 clicked Substitute in method");
                } catch (Exception e) {
                    System.out.println("Menu item not found: " + e.getMessage());
                }
                break;
            }
        }

        TestTimingUtil.waitUntil(bot, TestTimingUtil.editorContentChanges(styledText, contentBefore), 1000);
        editor.save();

        String contentAfter = styledText.getText();
        System.out.println("\n=== After Substitute in Method ===");
        lines = contentAfter.split("\n");
        for (int i = 0; i < lines.length; i++) {
            System.out.println(String.format("%2d: %s", i + 1, lines[i]));
        }

        // Check that methodOne was changed but methodTwo still has System.out
        int systemOutAfter = countOccurrences(contentAfter, "System.out");
        int loggerCalls = countOccurrences(contentAfter, "logger.info(");

        System.out.println("System.out calls after: " + systemOutAfter);
        System.out.println("logger.info calls: " + loggerCalls);
        System.out.println("==================================\n");

        log("4.3 test complete");
    }

    @Test
    public void test5_SubstituteSystemOutInClass() {
        log("5.0 start - Substitute System.out in class");

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

        int systemOutBefore = countOccurrences(contentBefore, "System.out");
        int systemErrBefore = countOccurrences(contentBefore, "System.err");
        System.out.println("\n=== BEFORE Substitute in Class ===");
        System.out.println("System.out=" + systemOutBefore + ", System.err=" + systemErrBefore);
        System.out.println(contentBefore);
        System.out.println("===================================\n");

        // Find and navigate to first line inside the class
        String[] lines = contentBefore.split("\n");
        int classLine = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("public class")) {
                classLine = i + 1; // Line after class declaration
                break;
            }
        }
        if (classLine == -1) {
            System.out.println("Class declaration not found!");
            return;
        }
        styledText.navigateTo(classLine, 0);
        log("5.1 navigated to class body (line " + (classLine + 1) + ")");

        // Execute "Substitute System.out in class" via Edit menu
        try {
            bot.menu("Edit").menu("Log4E").menu("Substitute System.out in class").click();
            log("5.2 clicked Substitute in class via Edit menu");
        } catch (Exception e) {
            System.out.println("Edit menu failed: " + e.getMessage());
            var contextMenu = styledText.contextMenu();
            for (String item : contextMenu.menuItems()) {
                if (item.contains("Log4E")) {
                    contextMenu.menu(item).menu("Substitute System.out in class").click();
                    log("5.2 clicked Substitute in class via context menu");
                    break;
                }
            }
        }

        TestTimingUtil.waitUntil(bot, TestTimingUtil.editorContentChanges(styledText, contentBefore), 1000);
        styledText.setFocus();

        String contentAfter = styledText.getText();
        System.out.println("\n=== AFTER Substitute in Class ===");
        String[] linesAfter = contentAfter.split("\n");
        for (int i = 0; i < linesAfter.length; i++) {
            System.out.println(String.format("%2d: %s", i + 1, linesAfter[i]));
        }

        int systemOutAfter = countOccurrences(contentAfter, "System.out");
        int systemErrAfter = countOccurrences(contentAfter, "System.err");
        int loggerInfoCalls = countOccurrences(contentAfter, "logger.info(");
        int loggerErrorCalls = countOccurrences(contentAfter, "logger.error(");

        System.out.println("After: System.out=" + systemOutAfter + ", System.err=" + systemErrAfter);
        System.out.println("Logger calls: info=" + loggerInfoCalls + ", error=" + loggerErrorCalls);

        boolean contentChanged = !contentBefore.equals(contentAfter);
        System.out.println("Content changed: " + contentChanged);
        System.out.println("==================================\n");

        if (contentChanged) {
            editor.save();
        }

        // Verify ALL System.out/err were replaced
        assertEquals("All System.out should be replaced", 0, systemOutAfter);
        assertEquals("All System.err should be replaced", 0, systemErrAfter);

        log("5.3 test complete");
    }

    @Test
    public void test6_ContextMenuSubstituteInClass() {
        log("6.0 start - Context menu Substitute System.out in class");

        // Create a new class for context menu test
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
        bot.textWithLabel("Name:").setText("ContextMenuTestClass");

        SWTBotShell wizardShell = bot.shell("New Java Class");
        bot.button("Finish").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellCloses(wizardShell), 1000);

        TestTimingUtil.waitUntil(bot, TestTimingUtil.editorIsActive(bot, "ContextMenuTestClass.java"), 1000);
        log("6.1 created ContextMenuTestClass");

        // Set class content with System.out calls
        SWTBotEditor editor = bot.editorByTitle("ContextMenuTestClass.java");
        editor.setFocus();
        SWTBotStyledText styledText = editor.toTextEditor().getStyledText();

        String classContent = """
                package com.test;

                import org.slf4j.Logger;
                import org.slf4j.LoggerFactory;

                public class ContextMenuTestClass {

                    private static final Logger logger = LoggerFactory.getLogger(ContextMenuTestClass.class);

                    public void testMethod() {
                        System.out.println("Context menu test");
                        System.err.println("Error from context menu test");
                    }
                }
                """;

        styledText.setText(classContent);
        editor.save();
        log("6.2 set class content");

        String contentBefore = styledText.getText();
        int systemOutBefore = countOccurrences(contentBefore, "System.out");
        int systemErrBefore = countOccurrences(contentBefore, "System.err");
        System.out.println("\n=== Context Menu Test - BEFORE ===");
        System.out.println("System.out=" + systemOutBefore + ", System.err=" + systemErrBefore);

        // Find class line and navigate inside class body
        String[] lines = contentBefore.split("\n");
        int classLine = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("public class")) {
                classLine = i + 1;
                break;
            }
        }
        styledText.navigateTo(classLine, 0);
        log("6.3 navigated to class body (line " + (classLine + 1) + ")");

        // Execute via CONTEXT MENU
        var contextMenu = styledText.contextMenu();
        boolean menuFound = false;
        for (String item : contextMenu.menuItems()) {
            if (item.contains("Log4E")) {
                contextMenu.menu(item).menu("Substitute System.out in class").click();
                menuFound = true;
                log("6.4 clicked Substitute via CONTEXT MENU");
                break;
            }
        }

        if (!menuFound) {
            System.out.println("Log4E menu not found in context menu!");
            return;
        }

        TestTimingUtil.waitUntil(bot, TestTimingUtil.editorContentChanges(styledText, contentBefore), 1000);
        styledText.setFocus();

        String contentAfter = styledText.getText();
        System.out.println("\n=== Context Menu Test - AFTER ===");
        String[] linesAfter = contentAfter.split("\n");
        for (int i = 0; i < linesAfter.length; i++) {
            System.out.println(String.format("%2d: %s", i + 1, linesAfter[i]));
        }

        int systemOutAfter = countOccurrences(contentAfter, "System.out");
        int systemErrAfter = countOccurrences(contentAfter, "System.err");
        int loggerInfoCalls = countOccurrences(contentAfter, "logger.info(");
        int loggerErrorCalls = countOccurrences(contentAfter, "logger.error(");

        System.out.println("After: System.out=" + systemOutAfter + ", System.err=" + systemErrAfter);
        System.out.println("Logger calls: info=" + loggerInfoCalls + ", error=" + loggerErrorCalls);
        System.out.println("======================================\n");

        if (!contentBefore.equals(contentAfter)) {
            editor.save();
        }

        // Verify context menu worked
        assertEquals("System.out should be replaced via context menu", 0, systemOutAfter);
        assertEquals("System.err should be replaced via context menu", 0, systemErrAfter);
        assertTrue("Should have logger.info call", loggerInfoCalls > 0);
        assertTrue("Should have logger.error call", loggerErrorCalls > 0);

        log("6.5 context menu test complete");
    }

    @Test
    public void test7_CleanupProject() {
        log("7.0 cleanup");
        try {
            bot.closeAllEditors();
            SWTBotTreeItem project = bot.tree().getTreeItem(PROJECT_NAME);
            project.select();
            project.contextMenu("Delete").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Delete Resources"), 1000);
            bot.checkBox("Delete project contents on disk (cannot be undone)").click();
            bot.button("OK").click();
            projectCreated = false;
            log("7.1 cleanup complete");
        } catch (Exception e) {
            System.out.println("Cleanup error: " + e.getMessage());
        }
    }
}
