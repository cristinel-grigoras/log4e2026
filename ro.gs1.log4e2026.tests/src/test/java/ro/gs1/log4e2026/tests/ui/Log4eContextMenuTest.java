package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
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
 * SWTBot UI tests for Log4E context menu in Java editor. Creates a Java
 * project, opens a Java file, and tests the context menu.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Log4eContextMenuTest {

	private static final String SCREENSHOT_DIR = System.getProperty("screenshot.dir", "target/screenshots");
	private static final String PROJECT_NAME = "TestProject";
	private static final String CLASS_NAME = "TestClass";

	private SWTWorkbenchBot bot;

	@Rule
	public TimingRule timingRule = new TimingRule();

	@BeforeClass
	public static void setUpClass() {
		File dir = new File(SCREENSHOT_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// Disable automatic failure screenshots
		SWTBotPreferences.SCREENSHOTS_DIR = "";
		SWTBotPreferences.TIMEOUT = 500;
		SWTBotPreferences.PLAYBACK_DELAY = 0;
		SWTBotPreferences.TYPE_INTERVAL = 0;
	}

	@AfterClass
	public static void tearDownClass() {
		TestTimingUtil.printSummary();
	}

	@Before
	public void setUp() {
		bot = new SWTWorkbenchBot();
		try {
			bot.viewByTitle("Welcome").close();
		} catch (Exception e) {
			// Welcome view may not exist
		}
		// Ensure we have an active shell (setFocus doesn't timeout like activate)
		TestTimingUtil.focusWorkbenchShell(bot);
	}

	@After
	public void tearDown() {
		// Close any open dialogs
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
	public void test1_CreateJavaProject() throws Exception {
		log("1.0 start");

		// Open File > New > Other dialog
		bot.menu("File").menu("New").menu("Other...").click();
		log("1.1 after menu click");

		// Wait for "New" wizard dialog to appear
		long waited = TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("New"), 5000);
		log("1.2 after waitUntil New, waited " + waited);
		bot.activeShell().setFocus();

		// Capture the wizard selection dialog
		String wizardFilename = SCREENSHOT_DIR + "/06a_wizard_selection.png";
		captureWithImport(wizardFilename);

		// Type to filter and find Java Project
		bot.text().setText("Java Project");
		log("1.3 after setText filter");

		// Wait for tree to filter and show Java node
		TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 5000);
		log("1.4 after tree filter");

		// Select first match in tree
		SWTBotTreeItem javaNode = bot.tree().getTreeItem("Java");
		javaNode.expand();
		TestTimingUtil.waitUntil(bot, Conditions.treeItemHasNode(javaNode, "Java Project"), 3000);
		javaNode.getNode("Java Project").select();
		log("1.5 after select Java Project");

		// Click Next - wait for next page to load
		bot.button("Next >").click();
		log("1.6 after Next click");
		TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.textWithLabel("Project name:")), 5000);
		log("1.7 after waitUntil Project name enabled");

		// Enter project name
		bot.textWithLabel("Project name:").setText(PROJECT_NAME);
		log("1.8 after set project name");

		// Capture screenshot
		String filename = SCREENSHOT_DIR + "/06_new_java_project.png";
		captureWithImport(filename);

		// Click Finish and wait for project to be created
		bot.button("Finish").click();
		log("1.9 after Finish click");

		// Handle possible "Open Associated Perspective?" dialog
		try {
			TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Open Associated Perspective?"), 3000);
			log("1.10 perspective dialog found");
			bot.button("No").click();
		} catch (Exception e) {
			log("1.10 no perspective dialog");
		}

		// Wait for wizard to close and project to appear in tree
		log("1.11 before wait for tree");
		TestTimingUtil.waitUntil(bot, TestTimingUtil.projectExists(bot, PROJECT_NAME), 10000);
		log("1.12 project found in tree");

		// Verify project created
		SWTBotTreeItem project = bot.tree().getTreeItem(PROJECT_NAME);
		assertNotNull("Project should be created", project);
		log("1.13 test complete");
	}

	@Test
	public void test2_CreateJavaClass() throws Exception {
		log("2.0 start");

		// Select project in Package Explorer
		try {
			bot.tree().getTreeItem(PROJECT_NAME).select();
			log("2.1 project selected");
		} catch (Exception e) {
			System.out.println("Project not found, skipping test");
			return;
		}

		// Use File > New > Other and select Class from wizard
		bot.menu("File").menu("New").menu("Other...").click();
		log("2.2 after menu click");
		TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("New"), 5000);
		log("2.3 after New dialog");

		// Filter and select Class
		bot.text().setText("Class");
		log("2.4 after filter text");
		TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 1), 5000);
		log("2.5 after tree filter");

		SWTBotTreeItem javaNode = bot.tree().getTreeItem("Java");
		javaNode.expand();
		TestTimingUtil.waitUntil(bot, Conditions.treeItemHasNode(javaNode, "Class"), 3000);
		javaNode.getNode("Class").select();
		log("2.6 after Class selected");

		bot.button("Next >").click();
		log("2.7 after Next click");

		// Wait for New Java Class wizard page
		TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.textWithLabel("Name:")), 5000);
		log("2.8 after Name field enabled");

		// Set package name (required for Finish to be enabled)
		bot.textWithLabel("Package:").setText("com.test");
		log("2.8a after package set");

		// Enter class name
		bot.textWithLabel("Name:").setText(CLASS_NAME);
		log("2.9 after class name set");

		// Capture screenshot
		String filename = SCREENSHOT_DIR + "/07_new_java_class.png";
		captureWithImport(filename);

		// Click Finish and wait for wizard to close (shell is now "New Java Class")
		SWTBotShell wizardShell = bot.shell("New Java Class");
		log("2.10 before Finish");
		bot.button("Finish").click();
		log("2.11 after Finish click");
		TestTimingUtil.waitUntil(bot, Conditions.shellCloses(wizardShell), 10000);
		log("2.12 after shell closed");

		// File should be open in editor
		SWTBotEditor editor = bot.editorByTitle(CLASS_NAME + ".java");
		assertNotNull("Editor should be open", editor);
		log("2.13 test complete");
	}

	@Test
	public void test3_AddMethodsToClass() throws Exception {
		log("3.0 start");

		// Try to open the Java file if not already open
		SWTBotEditor editor;
		try {
			editor = bot.editorByTitle(CLASS_NAME + ".java");
			editor.setFocus();
			log("3.1 editor found");
		} catch (Exception e) {
			// Try to open from project
			try {
				bot.tree().getTreeItem(PROJECT_NAME).expand().getNode("src").expand().getNode("com.test").expand()
						.getNode(CLASS_NAME + ".java").doubleClick();
				TestTimingUtil.waitUntil(bot, TestTimingUtil.editorIsActive(bot, CLASS_NAME + ".java"), 5000);
				editor = bot.editorByTitle(CLASS_NAME + ".java");
				log("3.1 editor opened from tree");
			} catch (Exception e2) {
				System.out.println("Could not open Java file: " + e2.getMessage());
				return;
			}
		}

		// Replace the class content with 3 methods
		var textEditor = editor.toTextEditor();
		String newContent = """
				package com.test;

				public class TestClass {

					public void methodOne() {
						String name = "test";
						System.out.println(name);
					}

					public int methodTwo(int value) {
						int result = value * 2;
						return result;
					}

					public String methodThree(String input, int count) {
						StringBuilder sb = new StringBuilder();
						for (int i = 0; i < count; i++) {
							sb.append(input);
						}
						return sb.toString();
					}
				}
				""";
		textEditor.setText(newContent);
		log("3.2 added 3 methods to class");

		// Save the file
		editor.save();
		log("3.3 file saved");

		// Capture screenshot of the editor with methods
		String filename = SCREENSHOT_DIR + "/08_class_with_methods.png";
		captureWithImport(filename);
		log("3.4 screenshot captured");

		System.out.println("Class now has 3 methods: methodOne, methodTwo, methodThree");
		log("3.5 test complete");
	}

	@Test
	public void test4_ListLog4eMenuInMethodAndClass() throws Exception {
		log("4.0 start");

		// Try to activate editor
		SWTBotEditor editor;
		try {
			editor = bot.editorByTitle(CLASS_NAME + ".java");
			editor.setFocus();
			log("4.1 editor found");
		} catch (Exception e) {
			System.out.println("Editor not open, skipping test");
			return;
		}

		var styledText = editor.toTextEditor().getStyledText();

		// ===== Test 1: List Log4E menu when cursor is INSIDE a method =====
		System.out.println("\n=== Log4E Menu inside methodOne ===");
		// Navigate to line 7 (inside methodOne, on the System.out.println line)
		styledText.navigateTo(6, 10);
		log("4.2 navigated inside methodOne (line 7)");

		var contextMenu = styledText.contextMenu();
		boolean foundInMethod = false;
		for (String item : contextMenu.menuItems()) {
			if (item.contains("Log4E")) {
				foundInMethod = true;
				System.out.println("Found Log4E menu: " + item);
				var log4eMenu = contextMenu.menu(item);
				System.out.println("Log4E submenu items (inside method):");
				for (String subItem : log4eMenu.menuItems()) {
					System.out.println("  - " + subItem);
				}
				break;
			}
		}
		// Close menu
		bot.activeShell().pressShortcut(Keystrokes.ESC);
		log("4.3 listed Log4E menu inside method");

		// ===== Test 2: List Log4E menu when cursor is at CLASS level =====
		System.out.println("\n=== Log4E Menu at class level ===");
		// Navigate to line 4 (inside class but outside methods - the empty line after class declaration)
		styledText.navigateTo(3, 0);
		log("4.4 navigated to class level (line 4)");

		contextMenu = styledText.contextMenu();
		boolean foundAtClass = false;
		for (String item : contextMenu.menuItems()) {
			if (item.contains("Log4E")) {
				foundAtClass = true;
				System.out.println("Found Log4E menu: " + item);
				var log4eMenu = contextMenu.menu(item);
				System.out.println("Log4E submenu items (at class level):");
				for (String subItem : log4eMenu.menuItems()) {
					System.out.println("  - " + subItem);
				}
				break;
			}
		}
		// Close menu
		bot.activeShell().pressShortcut(Keystrokes.ESC);
		log("4.5 listed Log4E menu at class level");

		assertTrue("Log4E 2026 menu should be present inside method", foundInMethod);
		assertTrue("Log4E 2026 menu should be present at class level", foundAtClass);
		log("4.6 test complete");
	}

	@Test
	public void test5_ExecuteDeclareLogger() throws Exception {
		log("5.0 start");

		// Try to activate editor
		SWTBotEditor editor;
		try {
			editor = bot.editorByTitle(CLASS_NAME + ".java");
			editor.setFocus();
			log("5.1 editor found");
		} catch (Exception e) {
			System.out.println("Editor not open, skipping test");
			return;
		}

		var styledText = editor.toTextEditor().getStyledText();

		// Navigate to class level (line 4, empty line after class declaration)
		styledText.navigateTo(3, 0);
		log("5.2 navigated to class level");

		// Execute Declare Logger from context menu
		System.out.println("\n=== Executing 'Declare Logger' ===");
		var contextMenu = styledText.contextMenu();
		for (String item : contextMenu.menuItems()) {
			if (item.contains("Log4E")) {
				var log4eMenu = contextMenu.menu(item);
				log4eMenu.menu("Declare Logger").click();
				log("5.3 clicked Declare Logger");
				break;
			}
		}

		// Wait a moment for any dialog to appear
		bot.sleep(500);

		// Check what shell/dialog is active
		try {
			SWTBotShell activeShell = bot.activeShell();
			String shellTitle = activeShell.getText();
			System.out.println("Active shell after 'Declare Logger': " + shellTitle);

			// List all buttons in the dialog
			System.out.println("Dialog buttons:");
			try {
				for (int i = 0; i < 10; i++) {
					try {
						String btnText = bot.button(i).getText();
						System.out.println("  - Button " + i + ": " + btnText);
					} catch (Exception e) {
						break;
					}
				}
			} catch (Exception e) {
				// No more buttons
			}

			// Capture screenshot of the dialog
			String filename = SCREENSHOT_DIR + "/09_declare_logger_dialog.png";
			captureWithImport(filename);
			log("5.4 screenshot captured");

			// Close any dialog that appeared
			if (!shellTitle.contains("TestProject") && !shellTitle.equals("data")) {
				System.out.println("Closing dialog: " + shellTitle);
				bot.activeShell().pressShortcut(Keystrokes.ESC);
			}
		} catch (Exception e) {
			System.out.println("No dialog appeared or error: " + e.getMessage());
		}

		log("5.5 test complete");
	}

	@Test
	public void test6_ExecuteInsertLogStatement() throws Exception {
		log("6.0 start");

		// Try to activate editor
		SWTBotEditor editor;
		try {
			editor = bot.editorByTitle(CLASS_NAME + ".java");
			editor.setFocus();
			log("6.1 editor found");
		} catch (Exception e) {
			System.out.println("Editor not open, skipping test");
			return;
		}

		var textEditor = editor.toTextEditor();
		var styledText = textEditor.getStyledText();

		// Print current editor content to see line numbers
		String contentBefore = styledText.getText();
		System.out.println("\n=== Editor content BEFORE Insert Log Statement ===");
		String[] lines = contentBefore.split("\n");
		for (int i = 0; i < lines.length; i++) {
			System.out.println((i + 1) + ": " + lines[i]);
		}

		// Navigate inside methodTwo (to the line with "int result = value * 2;")
		// This should be around line 12-13 after Declare Logger added imports
		int targetLine = -1;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].contains("int result = value * 2")) {
				targetLine = i;
				break;
			}
		}

		if (targetLine == -1) {
			System.out.println("Could not find target line, using line 12");
			targetLine = 11; // 0-indexed
		}

		styledText.navigateTo(targetLine, 5);
		log("6.2 navigated to line " + (targetLine + 1) + " inside methodTwo");

		// Execute Insert Log Statement from context menu
		System.out.println("\n=== Executing 'Insert Log Statement' inside methodTwo ===");
		var contextMenu = styledText.contextMenu();
		for (String item : contextMenu.menuItems()) {
			if (item.contains("Log4E")) {
				var log4eMenu = contextMenu.menu(item);
				log4eMenu.menu("Insert Log Statement").click();
				log("6.3 clicked Insert Log Statement");
				break;
			}
		}

		// Wait for action to complete
		bot.sleep(500);

		// Check what shell/dialog is active
		try {
			SWTBotShell activeShell = bot.activeShell();
			String shellTitle = activeShell.getText();
			System.out.println("Active shell after 'Insert Log Statement': " + shellTitle);

			// List all buttons in the dialog
			System.out.println("Dialog buttons:");
			try {
				for (int i = 0; i < 10; i++) {
					try {
						String btnText = bot.button(i).getText();
						System.out.println("  - Button " + i + ": " + btnText);
					} catch (Exception e) {
						break;
					}
				}
			} catch (Exception e) {
				// No more buttons
			}

			// Close any dialog that appeared
			if (!shellTitle.contains("TestProject") && !shellTitle.equals("data")) {
				System.out.println("Closing dialog: " + shellTitle);
				bot.activeShell().pressShortcut(Keystrokes.ESC);
			}
		} catch (Exception e) {
			System.out.println("No dialog appeared: " + e.getMessage());
		}

		// Print editor content AFTER to see what changed
		String contentAfter = styledText.getText();
		System.out.println("\n=== Editor content AFTER Insert Log Statement ===");
		String[] linesAfter = contentAfter.split("\n");
		for (int i = 0; i < linesAfter.length; i++) {
			System.out.println((i + 1) + ": " + linesAfter[i]);
		}

		// Capture screenshot
		String filename = SCREENSHOT_DIR + "/10_insert_log_statement_result.png";
		captureWithImport(filename);
		log("6.4 screenshot captured");

		// Check if content changed
		if (!contentBefore.equals(contentAfter)) {
			System.out.println("\n*** Content CHANGED after Insert Log Statement! ***");
		} else {
			System.out.println("\n*** Content did NOT change after Insert Log Statement ***");
		}

		log("6.5 test complete");
	}

	@Test
	public void test7_CheckLog4eProjectSettings() throws Exception {
		log("7.0 start");

		// Close editors first to ensure project tree is focused
		try {
			bot.closeAllEditors();
		} catch (Exception e) {
			// Ignore
		}

		// Select project in tree
		try {
			bot.tree().getTreeItem(PROJECT_NAME).select();
			log("7.1 project selected");
		} catch (Exception e) {
			System.out.println("Project not found, skipping test");
			return;
		}

		// List Project menu items to debug
		System.out.println("\n=== Opening Project Properties ===");
		var projectMenu = bot.menu("Project");
		System.out.println("Project menu items:");
		for (String item : projectMenu.menuItems()) {
			System.out.println("  - '" + item + "'");
		}

		// Try to find Properties (might have different name)
		try {
			projectMenu.menu("Properties").click();
			log("7.2 opened Properties menu");
		} catch (Exception e) {
			System.out.println("Could not find 'Properties', trying alternatives...");
			// Try with Alt+Enter shortcut instead
			bot.activeShell().pressShortcut(SWT.ALT, SWT.CR);
			log("7.2 opened Properties via Alt+Enter");
		}

		// Wait for Properties dialog
		TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Properties for " + PROJECT_NAME), 5000);
		log("7.3 Properties dialog opened");

		SWTBotShell propsShell = bot.shell("Properties for " + PROJECT_NAME);
		propsShell.activate();

		// List all items in the tree (left panel of properties dialog)
		System.out.println("Properties categories:");
		var propsTree = bot.tree();
		for (String item : propsTree.getAllItems()[0].getNodes()) {
			System.out.println("  - " + item);
		}

		// Also list top-level items
		System.out.println("\nTop-level property pages:");
		for (var treeItem : propsTree.getAllItems()) {
			System.out.println("  - " + treeItem.getText());
		}

		// Look for Log4E in the properties tree
		boolean foundLog4e = false;
		for (var treeItem : propsTree.getAllItems()) {
			if (treeItem.getText().contains("Log4E")) {
				foundLog4e = true;
				System.out.println("\nFound Log4E settings: " + treeItem.getText());
				treeItem.select();
				bot.sleep(500);

				// Capture screenshot of Log4E settings
				String filename = SCREENSHOT_DIR + "/11_log4e_project_settings.png";
				captureWithImport(filename);
				log("7.4 Log4E settings screenshot captured");

				// Try to list any visible controls/options
				System.out.println("Log4E project settings page opened");
				break;
			}
		}

		if (!foundLog4e) {
			System.out.println("\nLog4E not found in project properties, capturing current view");
			String filename = SCREENSHOT_DIR + "/11_project_properties.png";
			captureWithImport(filename);
		}

		// Close properties dialog
		bot.button("Apply and Close").click();
		log("7.5 closed Properties dialog");

		log("7.6 test complete");
	}

	@Test
	public void test8_CheckLog4ePreferences() throws Exception {
		log("8.0 start");

		// Open Preferences - list Window menu items first
		System.out.println("\n=== Opening Preferences ===");
		var windowMenu = bot.menu("Window");
		System.out.println("Window menu items:");
		for (String item : windowMenu.menuItems()) {
			System.out.println("  - '" + item + "'");
		}

		// Try to find Preferences (menu item has ellipsis: "Preferences...")
		boolean prefsOpened = false;
		for (String item : windowMenu.menuItems()) {
			if (item.contains("Preferences")) {
				try {
					windowMenu.menu(item).click();
					prefsOpened = true;
					log("8.1 opened '" + item + "' from Window menu");
					break;
				} catch (Exception e) {
					System.out.println("Could not click '" + item + "': " + e.getMessage());
				}
			}
		}

		if (!prefsOpened) {
			System.out.println("Could not open Preferences dialog, skipping test");
			log("8.1 skipped - Preferences not accessible");
			return;
		}

		// Wait for Preferences dialog
		TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"), 5000);
		log("8.2 Preferences dialog opened");

		SWTBotShell prefsShell = bot.shell("Preferences");
		prefsShell.activate();

		// Filter for Log4E
		bot.text().setText("Log4E");
		bot.sleep(500);
		log("8.3 filtered for Log4E");

		// Capture screenshot of filtered preferences
		String filename1 = SCREENSHOT_DIR + "/12_preferences_log4e_filter.png";
		captureWithImport(filename1);

		// List all visible items after filtering
		System.out.println("Preferences after filtering for 'Log4E':");
		var prefsTree = bot.tree();
		for (var treeItem : prefsTree.getAllItems()) {
			System.out.println("  - " + treeItem.getText());
			// Expand and list sub-items
			try {
				treeItem.expand();
				for (String subItem : treeItem.getNodes()) {
					System.out.println("    - " + subItem);
				}
			} catch (Exception e) {
				// No sub-items
			}
		}

		// Select Log4E 2026 main page if found
		boolean foundLog4e = false;
		for (var treeItem : prefsTree.getAllItems()) {
			if (treeItem.getText().contains("Log4E")) {
				foundLog4e = true;
				treeItem.select();
				bot.sleep(300);
				System.out.println("\nSelected: " + treeItem.getText());

				// Capture main Log4E preferences page
				String filename2 = SCREENSHOT_DIR + "/13_log4e_preferences_main.png";
				captureWithImport(filename2);
				log("8.4 Log4E main preferences captured");

				// Expand and check sub-pages
				try {
					treeItem.expand();
					for (String subPage : treeItem.getNodes()) {
						System.out.println("\nLog4E sub-page: " + subPage);
						treeItem.getNode(subPage).select();
						bot.sleep(300);

						// Capture each sub-page
						String safeName = subPage.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
						String filename3 = SCREENSHOT_DIR + "/14_log4e_" + safeName + ".png";
						captureWithImport(filename3);
					}
				} catch (Exception e) {
					System.out.println("No sub-pages or error: " + e.getMessage());
				}

				break;
			}
		}

		if (!foundLog4e) {
			System.out.println("Log4E not found in preferences");
		}

		// Close preferences dialog
		bot.button("Cancel").click();
		log("8.5 closed Preferences dialog");

		log("8.6 test complete");
	}

	@Test
	public void test9_CleanupProject() throws Exception {
		log("9.0 start");

		// Close all editors
		try {
			bot.closeAllEditors();
			log("9.1 editors closed");
		} catch (Exception e) {
			// Ignore
		}

		// Delete project using Edit menu
		try {
			SWTBotTreeItem project = bot.tree().getTreeItem(PROJECT_NAME);
			project.select();
			log("9.2 project selected");

			// Delete using Edit > Delete menu
			bot.menu("Edit").menu("Delete").click();
			log("9.3 delete menu clicked");
			TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Delete Resources"), 3000);
			log("9.4 delete dialog appeared");

			// Confirm deletion
			try {
				bot.checkBox("Delete project contents on disk (cannot be undone)").click();
				bot.button("OK").click();
				TestTimingUtil.waitUntil(bot, Conditions.treeHasRows(bot.tree(), 0), 5000);
				log("9.5 project deleted");
			} catch (Exception e) {
				// Dialog might be different
				try {
					bot.button("OK").click();
				} catch (Exception e2) {
					// Ignore
				}
			}
		} catch (Exception e) {
			System.out.println("Could not delete project: " + e.getMessage());
		}
		log("9.6 test complete");
	}

	private void captureWithImport(String filename) throws Exception {
		ProcessBuilder pb = new ProcessBuilder("import", "-window", "root", "PNG:" + filename);
		pb.inheritIO();
		Process process = pb.start();
		int exitCode = process.waitFor();

		if (exitCode == 0) {
			System.out.println("Screenshot saved: " + filename);
			assertTrue("Screenshot file not created", new File(filename).exists());
		} else {
			System.out.println("import command failed with exit code: " + exitCode);
			fail("Screenshot failed: " + filename);
		}
	}

}
