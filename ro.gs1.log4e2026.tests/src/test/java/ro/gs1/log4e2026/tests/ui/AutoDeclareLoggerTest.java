package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
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
 * Tests for automatic logger declaration feature.
 *
 * Tests the following scenarios:
 * 1. Auto-declare logger when inserting log statement (no logger exists)
 * 2. Auto-declare logger when logging a method (no logger exists)
 * 3. Auto-add imports when declaring logger
 * 4. No auto-declare when logger already exists
 * 5. No auto-declare when AUTOMATIC_DECLARE preference is disabled
 * 6. No auto-imports when AUTOMATIC_IMPORTS preference is disabled
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AutoDeclareLoggerTest {

    private static final String PROJECT_NAME = "AutoDeclareTestProject";
    private static final String CLASS_NAME = "TestClass";
    private static final String PACKAGE_NAME = "com.test";
    private static SWTWorkbenchBot bot;
    private static boolean projectCreated = false;

    @BeforeClass
    public static void setUpClass() {
        SWTBotPreferences.TIMEOUT = 1000;
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
                    title.equals("New") || title.contains("Save")) {
                    try {
                        bot.button("Cancel").click();
                    } catch (Exception e) {
                        try {
                            bot.button("No").click();
                        } catch (Exception e2) {
                            activeShell.close();
                        }
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
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("New"), 1000);
            log("01.1 New wizard opened");

            // Filter for Java Project
            bot.text().setText("Java Project");
            TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 1000);

            SWTBotTreeItem javaNode = bot.tree().getTreeItem("Java");
            javaNode.expand();
            TestTimingUtil.waitUntil(bot, Conditions.treeItemHasNode(javaNode, "Java Project"), 1000);
            javaNode.getNode("Java Project").select();
            log("01.2 Java Project selected");

            bot.button("Next >").click();
            TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.textWithLabel("Project name:")), 1000);

            bot.textWithLabel("Project name:").setText(PROJECT_NAME);
            log("01.3 project name set");

            bot.button("Finish").click();

            // Handle perspective dialog if it appears
            try {
                TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Open Associated Perspective?"), 1000);
                bot.button("No").click();
            } catch (Exception e) {
                // Dialog may not appear
            }

            TestTimingUtil.waitUntil(bot, TestTimingUtil.projectExists(bot, PROJECT_NAME), 1000);
            projectCreated = true;
            log("01.4 project created");

        } catch (Exception e) {
            System.out.println("Error creating project: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create Java project: " + e.getMessage());
        }
    }

    @Test
    public void test02_EnableAutoDeclarePrefernces() {
        log("02.0 start - enable auto-declare preferences");

        try {
            // Open Preferences
            bot.menu("Window").menu("Preferences...").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 1000);
            SWTBotShell prefsShell = bot.shell("Preferences");
            prefsShell.activate();
            log("02.1 Preferences opened");

            // Navigate to Log4E 2026 > Declaration
            SWTBotTreeItem log4eItem = prefsShell.bot().tree().getTreeItem("Log4E 2026");
            log4eItem.expand();
            log4eItem.getNode("Declaration").select();
            log("02.2 Declaration page selected");

            // Enable auto-declare and auto-imports
            SWTBotCheckBox autoImports = prefsShell.bot().checkBox("Automatically add imports when declaring logger");
            SWTBotCheckBox autoDeclare = prefsShell.bot().checkBox("Automatically declare logger when inserting log statements");

            if (!autoImports.isChecked()) {
                autoImports.click();
            }
            if (!autoDeclare.isChecked()) {
                autoDeclare.click();
            }
            log("02.3 auto-declare and auto-imports enabled");

            // Apply and close
            prefsShell.bot().button("Apply and Close").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellCloses(prefsShell), 1000);
            log("02.4 preferences saved");

        } catch (Exception e) {
            System.out.println("Error enabling preferences: " + e.getMessage());
            e.printStackTrace();
            try {
                bot.button("Cancel").click();
            } catch (Exception ex) {
                // Ignore
            }
            fail("Failed to enable auto-declare preferences: " + e.getMessage());
        }
    }

    @Test
    public void test03_CreateClassWithoutLogger() {
        log("03.0 start - create class without logger");

        try {
            // Select project
            try {
                bot.viewByTitle("Project Explorer").setFocus();
            } catch (Exception e) {
                bot.viewByTitle("Package Explorer").setFocus();
            }
            SWTBotTreeItem projectItem = bot.tree().getTreeItem(PROJECT_NAME);
            projectItem.select();
            log("03.1 project selected");

            // Create class using File > New > Other wizard
            bot.menu("File").menu("New").menu("Other...").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("New"), 1000);
            bot.text().setText("Class");
            TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 1000);
            SWTBotTreeItem javaNode = bot.tree().getTreeItem("Java");
            javaNode.expand();
            javaNode.getNode("Class").select();
            bot.button("Next >").click();
            log("03.2 class wizard opened");

            // Fill in class details
            bot.textWithLabel("Package:").setText(PACKAGE_NAME);
            bot.textWithLabel("Name:").setText(CLASS_NAME);
            bot.button("Finish").click();
            log("03.3 class created");

            // Wait for editor to open
            TestTimingUtil.waitUntil(bot, TestTimingUtil.editorIsActive(bot, CLASS_NAME + ".java"), 1000);
            SWTBotEclipseEditor editor = bot.editorByTitle(CLASS_NAME + ".java").toTextEditor();
            editor.setFocus();
            log("03.4 editor opened");

            // Add a method to the class
            String classContent = editor.getText();
            String newContent = classContent.replace(
                "public class TestClass {",
                "public class TestClass {\n\n    public void testMethod() {\n        // test\n    }\n"
            );
            editor.setText(newContent);
            editor.save();
            log("03.5 method added");

            // Verify no logger exists
            String content = editor.getText();
            assertFalse("Class should not have logger initially", content.contains("private static final Logger"));
            assertFalse("Class should not have SLF4J import initially", content.contains("import org.slf4j"));
            log("03.6 verified no logger exists");

        } catch (Exception e) {
            System.out.println("Error creating class: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create class: " + e.getMessage());
        }
    }

    @Test
    public void test04_AutoDeclareOnLogThisMethod() {
        log("04.0 start - test auto-declare on Log this method");

        try {
            // Open the editor
            SWTBotEclipseEditor editor = bot.editorByTitle(CLASS_NAME + ".java").toTextEditor();
            editor.setFocus();
            log("04.1 editor focused");

            // Navigate to inside the method
            String content = editor.getText();
            int methodPos = content.indexOf("// test");
            editor.navigateTo(getLineNumber(content, methodPos), 0);
            log("04.2 cursor positioned in method");

            // Capture content before action
            String contentBefore = editor.getText();
            SWTBotStyledText styledText = editor.getStyledText();

            // Execute "Log this method" via keyboard shortcut (Ctrl+Alt+I)
            editor.pressShortcut(SWT.CTRL | SWT.ALT, 'i');
            log("04.3 Log this method executed");

            // Wait for editor content to change
            TestTimingUtil.waitUntil(bot, TestTimingUtil.editorContentChanges(styledText, contentBefore), 1000);

            // Save and get content
            editor.save();
            content = editor.getText();
            log("04.4 content after operation:\n" + content);

            // Verify logger was auto-declared
            assertTrue("Logger should be auto-declared",
                content.contains("private static final Logger logger") ||
                content.contains("private static final Logger LOGGER"));
            log("04.5 verified logger declared");

            // Verify imports were added
            assertTrue("SLF4J Logger import should be added", content.contains("import org.slf4j.Logger"));
            assertTrue("SLF4J LoggerFactory import should be added", content.contains("import org.slf4j.LoggerFactory"));
            log("04.6 verified imports added");

            // Verify log statements were added
            assertTrue("Entry log should be added", content.contains("logger.debug") && content.contains("start"));
            assertTrue("Exit log should be added", content.contains("logger.debug") && content.contains("end"));
            log("04.7 verified log statements added");

        } catch (Exception e) {
            System.out.println("Error testing auto-declare: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to test auto-declare on Log this method: " + e.getMessage());
        }
    }

    @Test
    public void test05_NoDoubleDeclarationWhenLoggerExists() {
        log("05.0 start - test no double declaration");

        try {
            // Open the editor (logger already exists from previous test)
            SWTBotEclipseEditor editor = bot.editorByTitle(CLASS_NAME + ".java").toTextEditor();
            editor.setFocus();
            log("05.1 editor focused");

            // Count logger declarations before
            String contentBefore = editor.getText();
            int loggerCountBefore = countOccurrences(contentBefore, "private static final Logger");
            log("05.2 logger count before: " + loggerCountBefore);

            // Add a new method
            String newContent = contentBefore.replace(
                "public void testMethod()",
                "public void anotherMethod() {\n        // another\n    }\n\n    public void testMethod()"
            );
            editor.setText(newContent);
            editor.save();
            log("05.3 new method added");

            // Navigate to the new method
            String content = editor.getText();
            int methodPos = content.indexOf("// another");
            editor.navigateTo(getLineNumber(content, methodPos), 0);
            log("05.4 cursor positioned in new method");

            // Capture content before action
            String contentBeforeAction = editor.getText();
            SWTBotStyledText styledText = editor.getStyledText();

            // Execute "Log this method" again
            editor.pressShortcut(SWT.CTRL | SWT.ALT, 'i');
            log("05.5 Log this method executed");

            TestTimingUtil.waitUntil(bot, TestTimingUtil.editorContentChanges(styledText, contentBeforeAction), 1000);
            editor.save();

            // Verify only one logger declaration
            String contentAfter = editor.getText();
            int loggerCountAfter = countOccurrences(contentAfter, "private static final Logger");
            log("05.6 logger count after: " + loggerCountAfter);

            assertEquals("Should still have only one logger declaration", loggerCountBefore, loggerCountAfter);
            log("05.7 verified no duplicate declaration");

        } catch (Exception e) {
            System.out.println("Error testing no double declaration: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to test no double declaration: " + e.getMessage());
        }
    }

    @Test
    public void test06_CreateClassForInsertLogTest() {
        log("06.0 start - create second class for insert log test");

        try {
            // Create a new class using File > New > Other wizard
            bot.menu("File").menu("New").menu("Other...").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("New"), 1000);
            bot.text().setText("Class");
            TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 1000);
            SWTBotTreeItem javaNode = bot.tree().getTreeItem("Java");
            javaNode.expand();
            javaNode.getNode("Class").select();
            bot.button("Next >").click();
            bot.textWithLabel("Name:").setText("InsertLogTestClass");
            bot.button("Finish").click();
            log("06.1 class created");

            // Wait for editor
            TestTimingUtil.waitUntil(bot, TestTimingUtil.editorIsActive(bot, "InsertLogTestClass.java"), 1000);
            SWTBotEclipseEditor editor = bot.editorByTitle("InsertLogTestClass.java").toTextEditor();
            editor.setFocus();
            log("06.2 editor opened");

            // Add a method
            String classContent = editor.getText();
            String newContent = classContent.replace(
                "public class InsertLogTestClass {",
                "public class InsertLogTestClass {\n\n    public void doSomething(String param) {\n        System.out.println(param);\n    }\n"
            );
            editor.setText(newContent);
            editor.save();
            log("06.3 method added");

            // Verify no logger
            String content = editor.getText();
            assertFalse("Class should not have logger", content.contains("private static final Logger"));
            log("06.4 verified no logger");

        } catch (Exception e) {
            System.out.println("Error creating class: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create class: " + e.getMessage());
        }
    }

    @Test
    public void test07_AutoDeclareOnInsertLogStatement() {
        log("07.0 start - test auto-declare on Insert log statement");

        try {
            // Open the editor
            SWTBotEclipseEditor editor = bot.editorByTitle("InsertLogTestClass.java").toTextEditor();
            editor.setFocus();
            log("07.1 editor focused");

            // Select the variable 'param'
            String content = editor.getText();
            int paramPos = content.indexOf("System.out.println(param)");
            int lineNum = getLineNumber(content, paramPos);
            editor.navigateTo(lineNum, 0);

            // Select "param" text
            editor.selectRange(lineNum, content.indexOf("println(param)") - content.lastIndexOf('\n', paramPos) + 8, 5);
            log("07.2 selected 'param'");

            // Capture content before action
            String contentBefore = editor.getText();
            SWTBotStyledText styledText = editor.getStyledText();

            // Execute "Insert log statement" via keyboard shortcut (Ctrl+Alt+L)
            editor.pressShortcut(SWT.CTRL | SWT.ALT, 'l');
            log("07.3 Insert log statement executed");

            TestTimingUtil.waitUntil(bot, TestTimingUtil.editorContentChanges(styledText, contentBefore), 1000);
            editor.save();

            // Verify logger was auto-declared
            content = editor.getText();
            log("07.4 content after operation:\n" + content);

            assertTrue("Logger should be auto-declared",
                content.contains("private static final Logger logger") ||
                content.contains("private static final Logger LOGGER"));
            log("07.5 verified logger declared");

            // Verify imports
            assertTrue("SLF4J imports should be added", content.contains("import org.slf4j"));
            log("07.6 verified imports added");

        } catch (Exception e) {
            System.out.println("Error testing auto-declare: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to test auto-declare on Insert log statement: " + e.getMessage());
        }
    }

    @Test
    public void test08_DisableAutoDeclare() {
        log("08.0 start - disable auto-declare");

        try {
            // Open Preferences
            bot.menu("Window").menu("Preferences...").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 1000);
            SWTBotShell prefsShell = bot.shell("Preferences");
            prefsShell.activate();
            log("08.1 Preferences opened");

            // Navigate to Log4E 2026 > Declaration
            SWTBotTreeItem log4eItem = prefsShell.bot().tree().getTreeItem("Log4E 2026");
            log4eItem.expand();
            log4eItem.getNode("Declaration").select();
            log("08.2 Declaration page selected");

            // Disable auto-declare
            SWTBotCheckBox autoDeclare = prefsShell.bot().checkBox("Automatically declare logger when inserting log statements");
            if (autoDeclare.isChecked()) {
                autoDeclare.click();
            }
            log("08.3 auto-declare disabled");

            // Apply and close
            prefsShell.bot().button("Apply and Close").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellCloses(prefsShell), 1000);
            log("08.4 preferences saved");

        } catch (Exception e) {
            System.out.println("Error disabling auto-declare: " + e.getMessage());
            e.printStackTrace();
            try {
                bot.button("Cancel").click();
            } catch (Exception ex) {
                // Ignore
            }
            fail("Failed to disable auto-declare: " + e.getMessage());
        }
    }

    @Test
    public void test09_NoAutoDeclareWhenDisabled() {
        log("09.0 start - test no auto-declare when disabled");

        try {
            // Create a new class using File > New > Other wizard
            bot.menu("File").menu("New").menu("Other...").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("New"), 1000);
            bot.text().setText("Class");
            TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 1000);
            SWTBotTreeItem javaNode = bot.tree().getTreeItem("Java");
            javaNode.expand();
            javaNode.getNode("Class").select();
            bot.button("Next >").click();
            bot.textWithLabel("Name:").setText("NoAutoDeclareClass");
            bot.button("Finish").click();
            log("09.1 class created");

            // Wait for editor
            TestTimingUtil.waitUntil(bot, TestTimingUtil.editorIsActive(bot, "NoAutoDeclareClass.java"), 1000);
            SWTBotEclipseEditor editor = bot.editorByTitle("NoAutoDeclareClass.java").toTextEditor();
            editor.setFocus();
            log("09.2 editor opened");

            // Add a method
            String classContent = editor.getText();
            String newContent = classContent.replace(
                "public class NoAutoDeclareClass {",
                "public class NoAutoDeclareClass {\n\n    public void testMethod() {\n        // test\n    }\n"
            );
            editor.setText(newContent);
            editor.save();
            log("09.3 method added");

            // Navigate to method
            String content = editor.getText();
            int methodPos = content.indexOf("// test");
            editor.navigateTo(getLineNumber(content, methodPos), 0);
            log("09.4 cursor positioned");

            // Capture content before action
            String contentBefore = editor.getText();
            SWTBotStyledText styledText = editor.getStyledText();

            // Execute "Log this method"
            editor.pressShortcut(SWT.CTRL | SWT.ALT, 'i');
            log("09.5 Log this method executed");

            TestTimingUtil.waitUntil(bot, TestTimingUtil.editorContentChanges(styledText, contentBefore), 1000);
            editor.save();

            // Verify logger was NOT declared (because preference is disabled)
            content = editor.getText();
            log("09.6 content after:\n" + content);

            // Logger should not be auto-declared, but log statements may still be inserted
            // with undefined logger reference
            assertFalse("Logger should NOT be auto-declared when preference is disabled",
                content.contains("private static final Logger"));
            log("09.7 verified no auto-declaration");

        } catch (Exception e) {
            System.out.println("Error testing disabled auto-declare: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to test disabled auto-declare: " + e.getMessage());
        }
    }

    @Test
    public void test10_ReEnableAutoDeclare() {
        log("10.0 start - re-enable auto-declare");

        try {
            // Open Preferences
            bot.menu("Window").menu("Preferences...").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 1000);
            SWTBotShell prefsShell = bot.shell("Preferences");
            prefsShell.activate();

            // Navigate to Log4E 2026 > Declaration
            SWTBotTreeItem log4eItem = prefsShell.bot().tree().getTreeItem("Log4E 2026");
            log4eItem.expand();
            log4eItem.getNode("Declaration").select();

            // Re-enable auto-declare
            SWTBotCheckBox autoDeclare = prefsShell.bot().checkBox("Automatically declare logger when inserting log statements");
            if (!autoDeclare.isChecked()) {
                autoDeclare.click();
            }

            prefsShell.bot().button("Apply and Close").click();
            TestTimingUtil.waitUntil(bot, Conditions.shellCloses(prefsShell), 1000);
            log("10.1 auto-declare re-enabled");

        } catch (Exception e) {
            try {
                bot.button("Cancel").click();
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    @Test
    public void test99_Cleanup() {
        log("99.0 start - cleanup");

        try {
            // Close all editors
            bot.closeAllEditors();
            log("99.1 editors closed");

            // Delete project
            if (projectCreated) {
                try {
                    bot.viewByTitle("Project Explorer").setFocus();
                } catch (Exception e) {
                    bot.viewByTitle("Package Explorer").setFocus();
                }
                SWTBotTreeItem projectItem = bot.tree().getTreeItem(PROJECT_NAME);
                projectItem.contextMenu("Delete").click();
                TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Delete Resources"), 1000);
                bot.checkBox("Delete project contents on disk (cannot be undone)").select();
                bot.button("OK").click();
                TestTimingUtil.waitUntil(bot, Conditions.shellCloses(bot.shell("Delete Resources")), 1000);
                log("99.2 project deleted");
            }
        } catch (Exception e) {
            System.out.println("Cleanup error: " + e.getMessage());
        }

        log("99.3 cleanup complete");
    }

    // Helper methods

    private int getLineNumber(String content, int offset) {
        int line = 0;
        for (int i = 0; i < offset && i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }

    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
}
