package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import ro.gs1.log4e2026.tests.util.TestTimingUtil;
import ro.gs1.log4e2026.tests.util.TimingRule;

/**
 * SWTBot UI tests for Log4E method-node context menu in Package Explorer / Project Explorer.
 * Tests that right-clicking a method node in an expanded .java file tree
 * shows the Log4E submenu with 5 method-level operations.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MethodNodeContextMenuTest {

	private static final String PROJECT_NAME = "MethodNodeMenuTest";
	private static final String CLASS_NAME = "SampleClass";
	private static SWTWorkbenchBot bot;
	private static boolean projectCreated = false;

	@Rule
	public TimingRule timingRule = new TimingRule();

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
			// Welcome view may not exist
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
	public void test01_CreateJavaProject() {
		log("1.0 start");
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
		log("1.1 after Finish click, before waiting for perspective dialog");

		try {
			TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Open Associated Perspective?"), 1000);
			bot.button("No").click();
			log("1.1 dismissed perspective dialog");
		} catch (Exception e) {
			log("1.1 no perspective dialog");
		}

		TestTimingUtil.waitUntil(bot, TestTimingUtil.projectExists(bot, PROJECT_NAME), 2000);
		assertNotNull(bot.tree().getTreeItem(PROJECT_NAME));
		projectCreated = true;
		log("1.2 project created");
	}

	@Test
	public void test02_CreateClassWithMethods() throws Exception {
		log("2.0 start");

		try {
			bot.tree().getTreeItem(PROJECT_NAME).select();
		} catch (Exception e) {
			System.out.println("Project not found, skipping test");
			return;
		}

		// Create Java class via wizard
		bot.menu("File").menu("New").menu("Other...").click();
		TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("New"), 1000);

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
		log("2.1 class created");

		// Set class content with methods
		SWTBotEditor editor = bot.editorByTitle(CLASS_NAME + ".java");
		editor.toTextEditor().setText(
				"package com.test;\n\n"
				+ "public class SampleClass {\n\n"
				+ "\tpublic void doSomething(String input) {\n"
				+ "\t\tSystem.out.println(input);\n"
				+ "\t}\n\n"
				+ "\tpublic int calculate(int a, int b) {\n"
				+ "\t\ttry {\n"
				+ "\t\t\tint result = a / b;\n"
				+ "\t\t\treturn result;\n"
				+ "\t\t} catch (ArithmeticException e) {\n"
				+ "\t\t}\n"
				+ "\t}\n"
				+ "}\n");
		editor.save();
		log("2.2 class content set with 2 methods");
	}

	@Test
	public void test03_ExpandClassAndFindMethodNodes() throws Exception {
		log("3.0 start");

		// Close editors so the tree view is focused
		bot.closeAllEditors();
		TestTimingUtil.focusWorkbenchShell(bot);

		// Navigate to the .java file in the tree
		SWTBotTreeItem projectNode = bot.tree().getTreeItem(PROJECT_NAME);
		projectNode.expand();
		log("3.1 project expanded");

		SWTBotTreeItem srcNode = projectNode.getNode("src");
		srcNode.expand();
		log("3.2 src expanded");

		SWTBotTreeItem pkgNode = srcNode.getNode("com.test");
		pkgNode.expand();
		log("3.3 package expanded");

		SWTBotTreeItem classFile = pkgNode.getNode(CLASS_NAME + ".java");
		classFile.expand();
		log("3.4 class file expanded");

		// List nodes under the .java file
		System.out.println("=== Nodes under " + CLASS_NAME + ".java ===");
		for (String node : classFile.getNodes()) {
			System.out.println("  - '" + node + "'");
		}

		// Find method nodes (might be directly under .java or under class sub-node)
		boolean foundDoSomething = false;
		boolean foundCalculate = false;

		for (String node : classFile.getNodes()) {
			if (node.contains("doSomething")) foundDoSomething = true;
			if (node.contains("calculate")) foundCalculate = true;
		}

		// If not found directly, try expanding the class node
		if (!foundDoSomething) {
			for (String node : classFile.getNodes()) {
				if (node.contains(CLASS_NAME)) {
					SWTBotTreeItem classNode = classFile.getNode(node);
					classNode.expand();
					System.out.println("=== Nodes under class node '" + node + "' ===");
					for (String subNode : classNode.getNodes()) {
						System.out.println("  - '" + subNode + "'");
						if (subNode.contains("doSomething")) foundDoSomething = true;
						if (subNode.contains("calculate")) foundCalculate = true;
					}
				}
			}
		}

		assertTrue("Should find doSomething method node", foundDoSomething);
		assertTrue("Should find calculate method node", foundCalculate);
		log("3.5 method nodes found");
	}

	@Test
	public void test04_VerifyLog4eMenuOnMethodNode() throws Exception {
		log("4.0 start");

		// Navigate to a method node
		SWTBotTreeItem methodNode = findMethodNode("doSomething");
		assertNotNull("doSomething method node should exist", methodNode);

		// Right-click to get context menu
		methodNode.select();
		var contextMenu = methodNode.contextMenu();
		log("4.1 context menu obtained on method node");

		List<String> menuItems = contextMenu.menuItems();
		System.out.println("=== Context menu items on method node ===");
		for (String item : menuItems) {
			System.out.println("  - '" + item + "'");
		}

		// Check for Log4E submenu
		boolean hasLog4e = menuItems.stream().anyMatch(s -> s.contains("Log4E"));
		assertTrue("Context menu should have Log4E submenu on method node", hasLog4e);
		log("4.2 Log4E submenu found");

		// List Log4E submenu items
		var log4eMenu = contextMenu.menu("Log4E");
		List<String> log4eItems = log4eMenu.menuItems();
		System.out.println("=== Log4E submenu items ===");
		for (String item : log4eItems) {
			System.out.println("  - '" + item + "'");
		}

		// Verify all 5 method operations are present
		boolean hasLogMethod = log4eItems.stream().anyMatch(s -> s.contains("Log this method"));
		boolean hasLogErrors = log4eItems.stream().anyMatch(s -> s.contains("Log errors"));
		boolean hasReapply = log4eItems.stream().anyMatch(s -> s.contains("Reapply"));
		boolean hasRemove = log4eItems.stream().anyMatch(s -> s.contains("Remove logger"));
		boolean hasSubstitute = log4eItems.stream().anyMatch(s -> s.contains("Substitute"));

		assertTrue("Should have 'Log this method'", hasLogMethod);
		assertTrue("Should have 'Log errors of this method'", hasLogErrors);
		assertTrue("Should have 'Reapply in this method'", hasReapply);
		assertTrue("Should have 'Remove logger in method'", hasRemove);
		assertTrue("Should have 'Substitute System.out in method'", hasSubstitute);

		// Close context menu
		bot.activeShell().pressShortcut(Keystrokes.ESC);
		log("4.3 all 5 method operations verified");
	}

	@Test
	public void test05_ExecuteLogThisMethodFromExplorer() throws Exception {
		log("5.0 start");

		// Find the doSomething method node
		SWTBotTreeItem methodNode = findMethodNode("doSomething");
		assertNotNull("doSomething method node should exist", methodNode);
		methodNode.select();
		log("5.1 method node selected");

		// Open the file in editor to get content before
		SWTBotTreeItem classFile = findClassFile();
		classFile.doubleClick();
		TestTimingUtil.waitUntil(bot, TestTimingUtil.editorIsActive(bot, CLASS_NAME + ".java"), 1000);
		SWTBotEditor editor = bot.editorByTitle(CLASS_NAME + ".java");
		String contentBefore = editor.toTextEditor().getText();
		System.out.println("\n=== Content BEFORE Log this method ===");
		printNumbered(contentBefore);

		// Close editor, focus tree, select method node
		editor.close();
		TestTimingUtil.focusWorkbenchShell(bot);
		methodNode = findMethodNode("doSomething");
		methodNode.select();

		// Execute "Log this method" from context menu
		methodNode.contextMenu("Log4E").menu("Log this method").click();
		log("5.2 executed Log this method");

		// Open file to check result
		classFile = findClassFile();
		classFile.doubleClick();
		TestTimingUtil.waitUntil(bot, TestTimingUtil.editorIsActive(bot, CLASS_NAME + ".java"), 1000);
		editor = bot.editorByTitle(CLASS_NAME + ".java");
		String contentAfter = editor.toTextEditor().getText();
		System.out.println("\n=== Content AFTER Log this method ===");
		printNumbered(contentAfter);

		assertNotEquals("Content should change after Log this method", contentBefore, contentAfter);
		// Verify log statements were added (case-insensitive check for logger/log/LOG)
		boolean hasLogContent = contentAfter.toLowerCase().contains("log");
		System.out.println("Content contains 'log' (case-insensitive): " + hasLogContent);
		assertTrue("Content should contain log statement, actual content:\n" + contentAfter, hasLogContent);
		log("5.3 log statements added to method");
	}

	@Test
	public void test06_Cleanup() {
		log("6.0 start");
		bot.closeAllEditors();
		if (TestTimingUtil.deleteProjectIfExists(bot, PROJECT_NAME)) {
			projectCreated = false;
			log("6.1 project deleted");
		}
		log("6.2 test complete");
	}

	/**
	 * Finds a method node in the explorer tree.
	 * Handles both layouts: methods directly under .java or under a class sub-node.
	 */
	private SWTBotTreeItem findMethodNode(String methodNamePrefix) {
		try {
			SWTBotTreeItem classFile = findClassFile();
			classFile.expand();

			// First try directly under .java file
			for (String node : classFile.getNodes()) {
				if (node.contains(methodNamePrefix)) {
					return classFile.getNode(node);
				}
			}

			// Then try under class sub-node (Project Explorer layout)
			for (String node : classFile.getNodes()) {
				if (node.contains(CLASS_NAME)) {
					SWTBotTreeItem classNode = classFile.getNode(node);
					classNode.expand();
					for (String subNode : classNode.getNodes()) {
						if (subNode.contains(methodNamePrefix)) {
							return classNode.getNode(subNode);
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Could not find method node: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Finds the .java class file node in the explorer tree.
	 */
	private SWTBotTreeItem findClassFile() {
		return bot.tree().getTreeItem(PROJECT_NAME)
				.getNode("src").getNode("com.test")
				.getNode(CLASS_NAME + ".java");
	}

	private void printNumbered(String content) {
		String[] lines = content.split("\n");
		for (int i = 0; i < lines.length; i++) {
			System.out.println((i + 1) + ": " + lines[i]);
		}
	}
}
