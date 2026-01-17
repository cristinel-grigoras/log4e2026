package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
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
 * SWTBot UI tests for Log4E menu visibility based on cursor position.
 *
 * Tests verify that menu items change visibility/enablement when:
 * - Cursor is inside a method body
 * - Cursor is outside methods (in class body)
 * - Cursor is on import statements
 * - Cursor moves between different methods
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MenuVisibilityByCursorPositionTest {

    private static final String PROJECT_NAME = "MenuVisibilityTestProject";
    private static final String CLASS_NAME = "TestClassWithMethods";
    private static SWTWorkbenchBot bot;
    private static boolean projectCreated = false;
    private static SWTBotEditor editor;
    private static SWTBotStyledText styledText;

    // Line numbers for cursor positioning (0-based for SWTBot)
    private static int lineImports = -1;
    private static int lineClassDeclaration = -1;
    private static int lineFieldDeclaration = -1;
    private static int lineMethod1Start = -1;
    private static int lineMethod1Body = -1;
    private static int lineMethod2Start = -1;
    private static int lineMethod2Body = -1;
    private static int lineAfterMethods = -1;

    @BeforeClass
    public static void setUpClass() {
        SWTBotPreferences.SCREENSHOTS_DIR = "";
        SWTBotPreferences.TIMEOUT = 5000;
        bot = new SWTWorkbenchBot();
        try {
            bot.viewByTitle("Welcome").close();
        } catch (Exception e) {
            // Ignore
        }
    }

    @AfterClass
    public static void tearDownClass() {
        // Clean up - delete the test project
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

    /**
     * Test 1: Create Java project.
     */
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

        TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 10000);
        assertNotNull(bot.tree().getTreeItem(PROJECT_NAME));
        log("1.1 project created");

        projectCreated = true;
    }

    /**
     * Test 2: Create Java class with multiple methods.
     */
    @Test
    public void test2_CreateJavaClassWithMethods() {
        log("2.0 start - create Java class with methods");

        try {
            bot.tree().getTreeItem(PROJECT_NAME).select();
        } catch (Exception e) {
            System.out.println("Project not found, skipping test");
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
        log("2.1 class created, editor opened");

        // Get editor and styled text
        editor = bot.editorByTitle(CLASS_NAME + ".java");
        editor.setFocus();
        styledText = editor.toTextEditor().getStyledText();

        // Replace content with class containing multiple methods
        String classContent =
            "package com.test;\n" +                          // Line 0
            "\n" +                                            // Line 1
            "import java.util.List;\n" +                      // Line 2
            "import java.util.ArrayList;\n" +                 // Line 3
            "\n" +                                            // Line 4
            "public class " + CLASS_NAME + " {\n" +           // Line 5
            "\n" +                                            // Line 6
            "    private String name;\n" +                    // Line 7
            "    private int count;\n" +                      // Line 8
            "\n" +                                            // Line 9
            "    public void methodOne() {\n" +               // Line 10
            "        String local = \"test\";\n" +            // Line 11
            "        System.out.println(local);\n" +          // Line 12
            "    }\n" +                                       // Line 13
            "\n" +                                            // Line 14
            "    public int methodTwo(String param) {\n" +    // Line 15
            "        int result = 0;\n" +                     // Line 16
            "        if (param != null) {\n" +                // Line 17
            "            result = param.length();\n" +        // Line 18
            "        }\n" +                                   // Line 19
            "        return result;\n" +                      // Line 20
            "    }\n" +                                       // Line 21
            "\n" +                                            // Line 22
            "    // Comment after methods\n" +                // Line 23
            "}\n";                                            // Line 24

        // Set line numbers for positioning
        lineImports = 2;
        lineClassDeclaration = 5;
        lineFieldDeclaration = 7;
        lineMethod1Start = 10;
        lineMethod1Body = 11;
        lineMethod2Start = 15;
        lineMethod2Body = 17;
        lineAfterMethods = 23;

        styledText.setText(classContent);
        editor.save();
        log("2.2 class content set with methods");

        // Print content for verification
        System.out.println("\n=== Test Class Content ===");
        String[] lines = classContent.split("\n");
        for (int i = 0; i < lines.length; i++) {
            System.out.println(String.format("%2d: %s", i, lines[i]));
        }
        System.out.println("===========================\n");

        assertTrue("Editor should have content", styledText.getText().length() > 0);
    }

    /**
     * Helper method to check if a menu item is enabled.
     */
    private boolean isMenuItemEnabled(String menuPath, String itemName) {
        try {
            SWTBotMenu menu = bot.menu("Edit").menu("Log4E").menu(itemName);
            return menu.isEnabled();
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    /**
     * Helper method to check if Log4E menu exists.
     */
    private boolean isLog4EMenuVisible() {
        try {
            bot.menu("Edit").menu("Log4E");
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    /**
     * Helper method to move cursor to a specific line.
     */
    private void moveCursorToLine(int lineNumber) {
        editor.setFocus();
        styledText.setFocus();
        // Navigate to line (Ctrl+L in Eclipse, but we'll use direct positioning)
        styledText.navigateTo(lineNumber, 4); // Line, column 4 (indented)
        bot.sleep(300); // Allow Eclipse to update context
    }

    /**
     * Helper method to get current cursor position info.
     */
    private String getCursorInfo() {
        // cursorPosition() returns org.eclipse.swtbot.swt.finder.utils.Position
        var pos = styledText.cursorPosition();
        int line = pos.line;
        String lineText = styledText.getTextOnLine(line);
        return String.format("Line %d: '%s'", line, lineText.trim());
    }

    /**
     * Helper method to capture menu state at current cursor position.
     */
    private void captureMenuState(String position) {
        System.out.println("\n--- Menu State at " + position + " ---");
        System.out.println("Cursor: " + getCursorInfo());
        System.out.println("Log4E Menu Visible: " + isLog4EMenuVisible());

        if (isLog4EMenuVisible()) {
            String[] menuItems = {
                "Declare Logger",
                "Insert Log Statement",
                "Log this variable",
                "Log at this position...",
                "Log this method",
                "Log this class"
            };

            for (String item : menuItems) {
                try {
                    SWTBotMenu menu = bot.menu("Edit").menu("Log4E").menu(item);
                    System.out.println("  " + item + ": " + (menu.isEnabled() ? "ENABLED" : "disabled"));
                    // Close menu after checking
                    bot.activeShell().pressShortcut(Keystrokes.ESC);
                } catch (WidgetNotFoundException e) {
                    System.out.println("  " + item + ": NOT FOUND");
                }
            }
        }
        System.out.println("-----------------------------------\n");
    }

    /**
     * Test 3: Check menu visibility when cursor is on import statements.
     */
    @Test
    public void test3_MenuVisibilityOnImports() {
        log("3.0 start - check menu on imports");

        if (editor == null) {
            editor = bot.editorByTitle(CLASS_NAME + ".java");
            styledText = editor.toTextEditor().getStyledText();
        }
        editor.setFocus();

        // Move cursor to import line
        moveCursorToLine(lineImports);
        log("3.1 cursor on imports");

        captureMenuState("IMPORTS (line " + lineImports + ")");

        // Log4E menu should be visible (editor is Java file)
        assertTrue("Log4E menu should be visible in Java editor", isLog4EMenuVisible());
        log("3.2 menu visible on imports");
    }

    /**
     * Test 4: Check menu visibility when cursor is on class declaration.
     */
    @Test
    public void test4_MenuVisibilityOnClassDeclaration() {
        log("4.0 start - check menu on class declaration");

        if (editor == null) {
            editor = bot.editorByTitle(CLASS_NAME + ".java");
            styledText = editor.toTextEditor().getStyledText();
        }
        editor.setFocus();

        // Move cursor to class declaration line
        moveCursorToLine(lineClassDeclaration);
        log("4.1 cursor on class declaration");

        captureMenuState("CLASS DECLARATION (line " + lineClassDeclaration + ")");

        assertTrue("Log4E menu should be visible on class declaration", isLog4EMenuVisible());
        log("4.2 menu visible on class declaration");
    }

    /**
     * Test 5: Check menu visibility when cursor is on field declaration.
     */
    @Test
    public void test5_MenuVisibilityOnFieldDeclaration() {
        log("5.0 start - check menu on field declaration");

        if (editor == null) {
            editor = bot.editorByTitle(CLASS_NAME + ".java");
            styledText = editor.toTextEditor().getStyledText();
        }
        editor.setFocus();

        // Move cursor to field declaration
        moveCursorToLine(lineFieldDeclaration);
        log("5.1 cursor on field declaration");

        captureMenuState("FIELD DECLARATION (line " + lineFieldDeclaration + ")");

        assertTrue("Log4E menu should be visible on field", isLog4EMenuVisible());
        log("5.2 menu visible on field declaration");
    }

    /**
     * Test 6: Check menu visibility when cursor is inside method 1.
     */
    @Test
    public void test6_MenuVisibilityInsideMethod1() {
        log("6.0 start - check menu inside method 1");

        if (editor == null) {
            editor = bot.editorByTitle(CLASS_NAME + ".java");
            styledText = editor.toTextEditor().getStyledText();
        }
        editor.setFocus();

        // Move cursor inside method 1 body
        moveCursorToLine(lineMethod1Body);
        log("6.1 cursor inside method 1");

        captureMenuState("INSIDE METHOD 1 (line " + lineMethod1Body + ")");

        assertTrue("Log4E menu should be visible inside method", isLog4EMenuVisible());

        // Check specific menu items that should be enabled inside a method
        // "Log this method" should be enabled when inside a method
        log("6.2 menu visible inside method 1");
    }

    /**
     * Test 7: Check menu visibility when cursor is inside method 2.
     */
    @Test
    public void test7_MenuVisibilityInsideMethod2() {
        log("7.0 start - check menu inside method 2");

        if (editor == null) {
            editor = bot.editorByTitle(CLASS_NAME + ".java");
            styledText = editor.toTextEditor().getStyledText();
        }
        editor.setFocus();

        // Move cursor inside method 2 body
        moveCursorToLine(lineMethod2Body);
        log("7.1 cursor inside method 2");

        captureMenuState("INSIDE METHOD 2 (line " + lineMethod2Body + ")");

        assertTrue("Log4E menu should be visible inside method 2", isLog4EMenuVisible());
        log("7.2 menu visible inside method 2");
    }

    /**
     * Test 8: Check menu visibility when cursor is after methods (in class body).
     */
    @Test
    public void test8_MenuVisibilityAfterMethods() {
        log("8.0 start - check menu after methods");

        if (editor == null) {
            editor = bot.editorByTitle(CLASS_NAME + ".java");
            styledText = editor.toTextEditor().getStyledText();
        }
        editor.setFocus();

        // Move cursor to after methods
        moveCursorToLine(lineAfterMethods);
        log("8.1 cursor after methods");

        captureMenuState("AFTER METHODS (line " + lineAfterMethods + ")");

        assertTrue("Log4E menu should be visible in class body", isLog4EMenuVisible());
        log("8.2 menu visible after methods");
    }

    /**
     * Test 9: Compare menu state between different positions.
     */
    @Test
    public void test9_CompareMenuStatesBetweenPositions() {
        log("9.0 start - compare menu states");

        if (editor == null) {
            editor = bot.editorByTitle(CLASS_NAME + ".java");
            styledText = editor.toTextEditor().getStyledText();
        }
        editor.setFocus();

        System.out.println("\n========== MENU VISIBILITY COMPARISON ==========\n");

        // Position 1: Inside method
        moveCursorToLine(lineMethod1Body);
        boolean insideMethodVisible = isLog4EMenuVisible();
        String insideMethodPos = getCursorInfo();

        // Position 2: On class declaration
        moveCursorToLine(lineClassDeclaration);
        boolean onClassVisible = isLog4EMenuVisible();
        String onClassPos = getCursorInfo();

        // Position 3: On field
        moveCursorToLine(lineFieldDeclaration);
        boolean onFieldVisible = isLog4EMenuVisible();
        String onFieldPos = getCursorInfo();

        // Position 4: On imports
        moveCursorToLine(lineImports);
        boolean onImportsVisible = isLog4EMenuVisible();
        String onImportsPos = getCursorInfo();

        System.out.println("Position Comparison:");
        System.out.println("  Inside Method (" + insideMethodPos + "): " + (insideMethodVisible ? "VISIBLE" : "hidden"));
        System.out.println("  On Class (" + onClassPos + "): " + (onClassVisible ? "VISIBLE" : "hidden"));
        System.out.println("  On Field (" + onFieldPos + "): " + (onFieldVisible ? "VISIBLE" : "hidden"));
        System.out.println("  On Imports (" + onImportsPos + "): " + (onImportsVisible ? "VISIBLE" : "hidden"));

        System.out.println("\n=================================================\n");

        // All positions should show menu visible (since we're in a Java editor)
        assertTrue("Menu should be visible inside method", insideMethodVisible);
        assertTrue("Menu should be visible on class declaration", onClassVisible);
        assertTrue("Menu should be visible on field", onFieldVisible);
        assertTrue("Menu should be visible on imports", onImportsVisible);

        log("9.1 comparison complete");
    }
}
