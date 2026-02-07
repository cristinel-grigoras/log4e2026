package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.AfterClass;

import ro.gs1.log4e2026.tests.util.TestTimingUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * SWTBot UI tests for Log4E menu contributions.
 *
 * Tests verify that the Edit -> Log4E menu:
 * - Is NOT visible without a Java file open (context-sensitive menu)
 * - IS visible when a Java file is open in the editor
 * - Contains all expected menu items when visible
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Log4eMenuUITest {

    private static final String PROJECT_NAME = "Log4eMenuTestProject";
    private static final String CLASS_NAME = "TestClass";
    private static SWTWorkbenchBot bot;
    private static boolean projectCreated = false;

    @BeforeClass
    public static void setUpClass() {
        SWTBotPreferences.SCREENSHOTS_DIR = "";
        SWTBotPreferences.TIMEOUT = 1000;
        bot = new SWTWorkbenchBot();
        // Close welcome page if present
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
        // Press ESC to close any open menus
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
     * Test 1: Verify Log4E menu is NOT visible without Java file open.
     * The menu should only appear when a Java editor is active.
     */
    @Test
    public void test1_Log4eMenuNotVisibleWithoutJavaContext() {
        log("1.0 start - verify menu NOT visible without Java context");

        // Close all editors to ensure no Java file is open
        try {
            bot.closeAllEditors();
        } catch (Exception e) {
            // Ignore
        }
        log("1.1 editors closed");

        // Try to find Log4E menu - it should NOT exist
        boolean menuFound = false;
        try {
            bot.menu("Edit").menu("Log4E");
            menuFound = true;
        } catch (WidgetNotFoundException e) {
            menuFound = false;
        }
        log("1.2 menu search complete");

        assertFalse("Log4E menu should NOT be visible without Java file open", menuFound);
        log("1.3 CORRECT: Log4E menu not visible without Java context");
    }

    /**
     * Test 2: Create a Java project for subsequent tests.
     */
    @Test
    public void test2_CreateJavaProject() {
        log("2.0 start - create Java project");

        // Use File -> New -> Other... and filter for Java Project
        bot.menu("File").menu("New").menu("Other...").click();
        log("2.1 New wizard opened");

        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("New"), 1000);
        bot.activeShell().setFocus();

        // Type to filter and find Java Project
        bot.text().setText("Java Project");
        log("2.2 filter set");

        // Wait for tree to filter
        TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 1000);
        log("2.3 tree filtered");

        // Select Java > Java Project
        SWTBotTreeItem javaNode = bot.tree().getTreeItem("Java");
        javaNode.expand();
        TestTimingUtil.waitUntil(bot, Conditions.treeItemHasNode(javaNode, "Java Project"), 1000);
        javaNode.getNode("Java Project").select();
        log("2.4 Java Project selected");

        // Click Next
        bot.button("Next >").click();
        log("2.5 Next clicked");
        TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.textWithLabel("Project name:")), 1000);

        // Enter project name
        bot.textWithLabel("Project name:").setText(PROJECT_NAME);
        log("2.6 project name set");

        // Click Finish
        bot.button("Finish").click();
        log("2.7 Finish clicked");

        // Handle "Open Associated Perspective" dialog if it appears
        try {
            TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Open Associated Perspective?"), 1000);
            bot.button("No").click();
            log("2.8 perspective dialog dismissed");
        } catch (Exception e) {
            log("2.8 no perspective dialog");
        }

        // Wait for project to appear in tree
        TestTimingUtil.waitUntil(bot, TestTimingUtil.projectExists(bot, PROJECT_NAME), 1000);
        SWTBotTreeItem project = bot.tree().getTreeItem(PROJECT_NAME);
        assertNotNull("Project should be created", project);
        log("2.9 project created");

        projectCreated = true;
    }

    /**
     * Test 3: Create a Java class and open it in editor.
     */
    @Test
    public void test3_CreateJavaClass() {
        log("3.0 start - create Java class");

        // Select project in tree
        try {
            bot.tree().getTreeItem(PROJECT_NAME).select();
            log("3.1 project selected");
        } catch (Exception e) {
            System.out.println("Project not found, skipping test");
            return;
        }

        // Use File -> New -> Other... and filter for Class
        bot.menu("File").menu("New").menu("Other...").click();
        log("3.2 New wizard opened");

        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("New"), 1000);
        bot.activeShell().setFocus();

        // Type to filter and find Class
        bot.text().setText("Class");
        log("3.3 filter set");

        // Wait for tree to filter
        TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 1000);
        log("3.4 tree filtered");

        // Select Java > Class
        SWTBotTreeItem javaNode = bot.tree().getTreeItem("Java");
        javaNode.expand();
        TestTimingUtil.waitUntil(bot, Conditions.treeItemHasNode(javaNode, "Class"), 1000);
        javaNode.getNode("Class").select();
        log("3.5 Class selected");

        // Click Next
        bot.button("Next >").click();
        log("3.6 Next clicked");

        // Wait for Name field to be enabled
        TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.textWithLabel("Name:")), 1000);
        log("3.7 Name field enabled");

        // Set package name (required for Finish to be enabled)
        bot.textWithLabel("Package:").setText("com.test");
        log("3.8 package set");

        // Enter class name
        bot.textWithLabel("Name:").setText(CLASS_NAME);
        log("3.9 class name set");

        // Click Finish and wait for wizard to close
        SWTBotShell wizardShell = bot.shell("New Java Class");
        bot.button("Finish").click();
        log("3.10 Finish clicked");
        TestTimingUtil.waitUntil(bot, Conditions.shellCloses(wizardShell), 1000);
        log("3.11 wizard closed");

        // Wait for editor to open using custom condition
        TestTimingUtil.waitUntil(bot, TestTimingUtil.editorIsActive(bot, CLASS_NAME + ".java"), 1000);
        log("3.9 editor opened");

        // Verify editor is open and focus it
        assertTrue("Editor should be open", bot.editors().size() > 0);
        bot.editorByTitle(CLASS_NAME + ".java").setFocus();
        log("3.10 editor focused");
    }

    /**
     * Test 4: Verify Log4E menu IS visible with Java file open.
     */
    @Test
    public void test4_Log4eMenuVisibleWithJavaContext() {
        log("4.0 start - verify menu IS visible with Java context");

        // Ensure editor is focused
        bot.activeEditor().setFocus();
        log("4.1 editor focused");

        // Now Log4E menu should be visible
        boolean menuFound = false;
        try {
            SWTBotMenu log4eMenu = bot.menu("Edit").menu("Log4E");
            menuFound = (log4eMenu != null);
        } catch (WidgetNotFoundException e) {
            menuFound = false;
        }
        log("4.2 menu search complete");

        assertTrue("Log4E menu SHOULD be visible with Java file open", menuFound);
        log("4.3 CORRECT: Log4E menu is visible with Java context");
    }

    /**
     * Test 5: Verify Declare Logger menu item exists.
     */
    @Test
    public void test5_DeclareLoggerMenuItemExists() {
        log("5.0 start - verify Declare Logger menu item");

        bot.activeEditor().setFocus();

        try {
            SWTBotMenu log4eMenu = bot.menu("Edit").menu("Log4E");
            log("5.1 Log4E menu found");

            SWTBotMenu declareLoggerItem = log4eMenu.menu("Declare Logger");
            log("5.2 Declare Logger found");

            assertNotNull("Declare Logger menu item should exist", declareLoggerItem);
        } catch (WidgetNotFoundException e) {
            fail("Declare Logger menu item not found: " + e.getMessage());
        }
    }

    /**
     * Test 6: Verify Insert Log Statement menu item exists.
     */
    @Test
    public void test6_InsertLogStatementMenuItemExists() {
        log("6.0 start - verify Insert Log Statement menu item");

        bot.activeEditor().setFocus();

        try {
            SWTBotMenu log4eMenu = bot.menu("Edit").menu("Log4E");
            log("6.1 Log4E menu found");

            SWTBotMenu insertLogItem = log4eMenu.menu("Insert Log Statement");
            log("6.2 Insert Log Statement found");

            assertNotNull("Insert Log Statement menu item should exist", insertLogItem);
        } catch (WidgetNotFoundException e) {
            fail("Insert Log Statement menu item not found: " + e.getMessage());
        }
    }

    /**
     * Test 7: Verify all expected menu items are accessible.
     */
    @Test
    public void test7_AllMenuItemsAccessible() {
        log("7.0 start - verify all menu items");

        bot.activeEditor().setFocus();

        try {
            SWTBotMenu log4eMenu = bot.menu("Edit").menu("Log4E");
            log("7.1 Log4E menu found");

            // Verify core menu items exist
            assertNotNull("Declare Logger", log4eMenu.menu("Declare Logger"));
            assertNotNull("Insert Log Statement", log4eMenu.menu("Insert Log Statement"));
            log("7.2 core items verified");

            // Verify additional menu items (may be disabled but should exist)
            assertNotNull("Log this variable", log4eMenu.menu("Log this variable"));
            assertNotNull("Log at this position...", log4eMenu.menu("Log at this position..."));
            assertNotNull("Log this method", log4eMenu.menu("Log this method"));
            assertNotNull("Log this class", log4eMenu.menu("Log this class"));
            log("7.3 logging items verified");

            assertNotNull("Log errors of this method", log4eMenu.menu("Log errors of this method"));
            assertNotNull("Log errors of this class", log4eMenu.menu("Log errors of this class"));
            log("7.4 error logging items verified");

            assertNotNull("Reapply in this method", log4eMenu.menu("Reapply in this method"));
            assertNotNull("Reapply in this class", log4eMenu.menu("Reapply in this class"));
            log("7.5 reapply items verified");

            assertNotNull("Remove logger in method", log4eMenu.menu("Remove logger in method"));
            assertNotNull("Remove logger in class", log4eMenu.menu("Remove logger in class"));
            log("7.6 remove items verified");

            assertNotNull("Substitute System.out in method", log4eMenu.menu("Substitute System.out in method"));
            assertNotNull("Substitute System.out in class", log4eMenu.menu("Substitute System.out in class"));
            log("7.7 substitute items verified");

            assertNotNull("Exchange logging framework", log4eMenu.menu("Exchange logging framework"));
            log("7.8 exchange item verified");

            log("7.9 all 15 menu items verified successfully");
        } catch (WidgetNotFoundException e) {
            fail("Menu item not found: " + e.getMessage());
        }
    }
}
