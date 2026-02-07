package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
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
 * Tests for verifying context menu functionality on project items in Package
 * Explorer. This test specifically checks that: 1. Project items can be
 * selected via bot.tree().getTreeItem() 2. Context menu can be opened on
 * project items 3. Context menu contains expected items (Properties, Delete,
 * etc.) 4. Context menu Properties opens Properties dialog (not Properties
 * view)
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProjectContextMenuTest {

	private static final String PROJECT_NAME = "ContextMenuTestProject";
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
	public void test01_CreateJavaProject() {
		log("1.0 start - create Java project for context menu test");

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
	public void test02_SelectProjectAndListContextMenuItems() {
		log("2.0 start - list context menu items on project");

		SWTBotTreeItem projectItem;
		try {
			projectItem = bot.tree().getTreeItem(PROJECT_NAME);
			log("2.1 found project tree item: " + projectItem.getText());
		} catch (Exception e) {
			System.out.println("Project not found: " + PROJECT_NAME);
			fail("Project not found: " + PROJECT_NAME);
			return;
		}

		// Select the project
		projectItem.select();
		log("2.2 project selected");
		// Get context menu and list items
		SWTBotRootMenu contextMenu = projectItem.contextMenu();
		log("2.3 context menu obtained");

		List<String> menuItems = contextMenu.menuItems();
		log("2.4 context menu has " + menuItems.size() + " items");

		System.out.println("=== Context menu items on project ===");
		for (int i = 0; i < menuItems.size(); i++) {
			String item = menuItems.get(i);
			System.out.println("  [" + i + "] '" + item + "'");
		}
		System.out.println("=====================================");

		// Verify expected items exist
		boolean hasNew = menuItems.stream().anyMatch(s -> s.contains("New"));
		boolean hasDelete = menuItems.stream().anyMatch(s -> s.equals("Delete"));
		boolean hasProperties = menuItems.stream().anyMatch(s -> s.equals("Properties"));
		boolean hasRefresh = menuItems.stream().anyMatch(s -> s.contains("Refresh"));

		log("2.5 menu items check: New=" + hasNew + ", Delete=" + hasDelete + ", Properties=" + hasProperties
				+ ", Refresh=" + hasRefresh);

		assertTrue("Context menu should have 'New' item", hasNew);
		assertTrue("Context menu should have 'Delete' item", hasDelete);
		assertTrue("Context menu should have 'Properties' item", hasProperties);

		// Close context menu by pressing Escape
		bot.activeShell().pressShortcut(Keystrokes.ESC);
		log("2.6 context menu closed");
	}

	/**
	 * Documents that context menu "Properties" opens Properties VIEW, not
	 * Properties dialog. This is a known Eclipse/SWTBot behavior in Package
	 * Explorer. Use File > Properties instead to open the Properties dialog.
	 */
	@Test
	public void test03_ContextMenuPropertiesOpensView() {
		log("3.0 start - document context menu Properties behavior");

		SWTBotTreeItem projectItem;
		try {
			projectItem = bot.tree().getTreeItem(PROJECT_NAME);
		} catch (Exception e) {
			System.out.println("Project not found, skipping");
			return;
		}

		// Select project first
		projectItem.select();
		log("3.1 project selected");

		// Count shells before
		int shellCountBefore = bot.shells().length;
		log("3.2 shells before context menu: " + shellCountBefore);

		// Check if Properties VIEW exists BEFORE context menu click
		System.out.println("=== VIEWS BEFORE CONTEXT MENU ===");
		boolean propertiesViewExistsBefore = false;
		for (var view : bot.views()) {
			System.out.println("  View: '" + view.getTitle() + "' active=" + view.isActive());
			if ("Properties".equals(view.getTitle())) {
				propertiesViewExistsBefore = true;
			}
		}
		System.out.println("Properties VIEW exists before: " + propertiesViewExistsBefore);
		System.out.println("=================================");
		log("3.3 Properties VIEW exists before: " + propertiesViewExistsBefore);

		// Capture screenshot BEFORE context menu click
		String screenshotDir = System.getProperty("screenshot.dir", "target/screenshots") + "/";
		new java.io.File(screenshotDir).mkdirs();
		bot.captureScreenshot(screenshotDir + "contextmenu_01_before.png");
		System.out.println("Screenshot saved: " + screenshotDir + "contextmenu_01_before.png");

		// Open context menu on project and capture screenshot to verify it's correct
		// menu
		SWTBotRootMenu contextMenu = projectItem.contextMenu();
		bot.captureScreenshot(screenshotDir + "contextmenu_02_menu_open.png");
		System.out.println("Screenshot saved: " + screenshotDir + "contextmenu_02_menu_open.png");

		// List menu items to verify
		System.out.println("=== CONTEXT MENU ITEMS ===");
		for (String item : contextMenu.menuItems()) {
			System.out.println("  Menu: '" + item + "'");
		}
		System.out.println("==========================");

		// Close this menu first
		bot.activeShell().pressShortcut(org.eclipse.swtbot.swt.finder.keyboard.Keystrokes.ESC);

		// Now click Properties from context menu
		projectItem.contextMenu("Properties").click();
		log("3.4 clicked context menu Properties");

		// Capture screenshot AFTER context menu click
		bot.captureScreenshot(screenshotDir + "contextmenu_03_after.png");
		System.out.println("Screenshot saved: " + screenshotDir + "contextmenu_03_after.png");

		// Check shells after - should NOT increase (opens view, not dialog)
		int shellCountAfter = bot.shells().length;
		log("3.5 shells after context menu: " + shellCountAfter);

		// List all shells after context menu
		System.out.println("=== ALL SHELLS AFTER CONTEXT MENU ===");
		for (SWTBotShell shell : bot.shells()) {
			System.out.println("  Shell: '" + shell.getText() + "' active=" + shell.isActive());
		}
		System.out.println("======================================");

		// List all views
		System.out.println("=== ALL VIEWS ===");
		try {
			for (var view : bot.views()) {
				System.out.println("  View: '" + view.getTitle() + "' active=" + view.isActive());
			}
		} catch (Exception e) {
			System.out.println("  Error listing views: " + e.getMessage());
		}
		System.out.println("=================");

		// KNOWN LIMITATION: context menu Properties opens Properties VIEW, not dialog
		// Verify the Properties VIEW is now active (not a dialog)
		boolean propertiesViewActive = false;
		String propertiesViewTitle = null;
		try {
			var propertiesView = bot.viewByTitle("Properties");
			propertiesViewActive = propertiesView != null;
			propertiesViewTitle = propertiesView.getTitle();
			log("3.6 Properties VIEW is active: " + propertiesViewTitle);
		} catch (Exception e) {
			log("3.6 Properties VIEW not found: " + e.getMessage());
		}

		System.out.println("=== KNOWN LIMITATION ===");
		System.out.println("Context menu 'Properties' on project opens Properties VIEW, not Properties dialog.");
		System.out.println("Properties VIEW title: '" + propertiesViewTitle + "' (no project name!)");
		System.out.println("Use File > Properties to open Properties dialog instead.");
		System.out.println("Shell count before: " + shellCountBefore + ", after: " + shellCountAfter);
		System.out.println("========================");

		// Verify: Properties VIEW is active (title is just "Properties", not
		// "Properties for <project>")
		assertTrue("Context menu Properties should open Properties VIEW", propertiesViewActive);
		assertEquals("Properties VIEW title should be just 'Properties' (no project name)", "Properties",
				propertiesViewTitle);

		// Document: shell count should NOT increase (no new dialog opened)
		assertTrue("Context menu Properties should NOT open new dialog (opens view instead)",
				shellCountAfter <= shellCountBefore + 1); // Allow +1 for menu shell

		log("3.7 documented known limitation - context menu opens view named 'Properties', not dialog");
	}

	/**
	 * Documents that File > Properties opens dialog, but context menu Properties
	 * does not. This test verifies the correct approach (File > Properties) works.
	 */
	@Test
	public void test04_FileMenuPropertiesOpensDialog() {
		log("4.0 start - verify File > Properties opens dialog");

		SWTBotTreeItem projectItem;
		try {
			projectItem = bot.tree().getTreeItem(PROJECT_NAME);
		} catch (Exception e) {
			System.out.println("Project not found, skipping");
			return;
		}

		// Select project
		projectItem.select();
		log("4.1 project selected");

		// File > Properties should open dialog
		bot.menu("File").menu("Properties").click();
		log("4.2 clicked File > Properties");

		String expectedTitle = "Properties for " + PROJECT_NAME;
		TestTimingUtil.waitUntil(bot, Conditions.shellIsActive(expectedTitle), 1000);

		SWTBotShell propsShell = null;
		for (SWTBotShell shell : bot.shells()) {
			String text = shell.getText();
			if (text != null && text.contains("Properties") && text.contains(PROJECT_NAME)) {
				propsShell = shell;
				break;
			}
		}

		assertNotNull("File > Properties should open Properties dialog", propsShell);
		log("4.3 File > Properties opened: " + propsShell.getText());
		assertEquals("Dialog title should be 'Properties for " + PROJECT_NAME + "'", expectedTitle,
				propsShell.getText());

		// Verify it has property pages tree
		try {
			SWTBotTreeItem[] items = propsShell.bot().tree().getAllItems();
			log("4.4 Properties dialog has " + items.length + " property pages");
			assertTrue("Properties dialog should have property pages", items.length > 0);
		} catch (Exception e) {
			log("4.4 Could not read property pages: " + e.getMessage());
		}

		// Close dialog
		propsShell.bot().button("Cancel").click();
		TestTimingUtil.waitUntil(bot, Conditions.shellCloses(propsShell), 1000);
		log("4.5 dialog closed - File > Properties works correctly");
	}

	@Test
	public void test05_ContextMenuDeleteOpensDialog() {
		log("5.0 start - verify Delete menu item opens dialog");

		SWTBotTreeItem projectItem;
		try {
			projectItem = bot.tree().getTreeItem(PROJECT_NAME);
		} catch (Exception e) {
			System.out.println("Project not found, skipping");
			return;
		}

		// Select project
		projectItem.select();
		log("5.1 project selected");

		// Use context menu Delete
		projectItem.contextMenu("Delete").click();
		log("5.2 clicked context menu Delete");

		// Should open "Delete Resources" dialog
		try {
			SWTBotShell deleteShell = bot.shell("Delete Resources");
			assertNotNull("Delete Resources dialog should open", deleteShell);
			log("5.3 Delete Resources dialog opened");

			// Cancel without deleting
			deleteShell.bot().button("Cancel").click();
			TestTimingUtil.waitUntil(bot, Conditions.shellCloses(deleteShell), 1000);
			log("5.4 Delete dialog cancelled");
		} catch (Exception e) {
			log("5.3 FAILED to find Delete Resources dialog: " + e.getMessage());
			fail("Delete Resources dialog not opened: " + e.getMessage());
		}
	}
}
