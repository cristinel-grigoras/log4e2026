package ro.gs1.log4e2026.tests.ui;

import static org.junit.Assert.*;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
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
 * Tests that verify preferences are properly saved and restored.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PreferenceSaveTest {

    private static SWTWorkbenchBot bot;

    @Rule
    public TimingRule timingRule = new TimingRule();

    @BeforeClass
    public static void setUpClass() {
        SWTBotPreferences.PLAYBACK_DELAY = 0;    // No delay between actions
        SWTBotPreferences.TIMEOUT = 5000;
        SWTBotPreferences.TYPE_INTERVAL = 0;    // No delay between keystrokes
        // Disable automatic failure screenshots
        SWTBotPreferences.SCREENSHOTS_DIR = "";

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
        // Each test starts fresh
    }

    @After
    public void tearDown() {
        // Close any open dialogs
        try {
            bot.button("Cancel").click();
        } catch (Exception e) {
            // Ignore
        }
    }

    private void log(String step) {
        TestTimingUtil.log(bot, step);
    }

    private void openPreferences() throws Exception {
        log("openPreferences start");
        // Ensure we have an active shell
        bot.shells()[0].setFocus();
        int shellCount = bot.shells().length;
        log("after setFocus");

        // Open Window menu - wait for menu shell to appear
        bot.activeShell().pressShortcut(SWT.ALT, 'w');
        TestTimingUtil.waitUntil(bot, TestTimingUtil.shellCountIncreases(bot, shellCount), 2000);
        log("after Alt+W");

        // Navigate to Preferences (last item) and open
        bot.activeShell().pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke.getInstance(SWT.END));
        bot.activeShell().pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke.getInstance(SWT.CR));
        log("after END+CR");
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"));
        log("Preferences open");
    }

    private void selectPreferencePage(String... path) throws Exception {
        log("selectPreferencePage: " + String.join("/", path));
        SWTBotTree tree = bot.tree();
        SWTBotTreeItem item = tree.getTreeItem(path[0]);
        for (int i = 1; i < path.length; i++) {
            item.expand();
            TestTimingUtil.waitUntil(bot, Conditions.treeItemHasNode(item, path[i]));
            item = item.getNode(path[i]);
        }
        item.select();
        log("after select " + path[path.length - 1]);
    }

    private void applyAndClose() throws Exception {
        log("applyAndClose start");
        SWTBotShell prefsShell = bot.shell("Preferences");
        prefsShell.setFocus();

        // Click Apply to save preferences
        bot.button("Apply").click();
        log("after Apply click");

        // Wait for shell to be active again
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"));

        // Since Apply already saved, use Cancel to close (faster and more reliable)
        // This is safe because Apply already persisted the changes
        bot.button("Cancel").click();
        log("after Cancel click");

        // Wait for dialog to close
        TestTimingUtil.waitUntil(bot, Conditions.shellCloses(prefsShell));
        log("dialog closed");
    }

    private void clickOkAndClose() throws Exception {
        log("clickOkAndClose start");
        SWTBotShell prefsShell = bot.shell("Preferences");
        prefsShell.setFocus();

        // Apply to save, then Cancel to close (more reliable than finding OK button)
        bot.button("Apply").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellIsActive("Preferences"));
        bot.button("Cancel").click();

        TestTimingUtil.waitUntil(bot, Conditions.shellCloses(prefsShell));
        log("dialog closed");
    }

    @Test
    public void test01_SaveLoggerName() throws Exception {
        log("start");
        String testValue = "testLogger123";
        String originalValue;

        openPreferences();
        log("after openPreferences");
        selectPreferencePage("Log4E 2026");
        log("after select Log4E 2026");

        SWTBotText loggerNameField = bot.textWithLabel("Logger Variable Name:");
        originalValue = loggerNameField.getText();
        log("original=" + originalValue);

        loggerNameField.setText(testValue);
        log("after setText");
        assertEquals("Field should show new value", testValue, loggerNameField.getText());

        applyAndClose();
        log("after applyAndClose");

        openPreferences();
        selectPreferencePage("Log4E 2026");
        log("after reopen");

        SWTBotText loggerNameField2 = bot.textWithLabel("Logger Variable Name:");
        assertEquals("Logger name should be saved", testValue, loggerNameField2.getText());
        log("verified saved=" + loggerNameField2.getText());

        loggerNameField2.setText(originalValue);
        clickOkAndClose();
        log("after restore");
    }

    @Test
    public void test02_SaveFrameworkSelection() throws Exception {
        log("start");
        String originalValue;
        int newIndex;

        openPreferences();
        selectPreferencePage("Log4E 2026");
        log("after open and select");

        SWTBotCombo frameworkCombo = bot.comboBoxWithLabel("Logging Framework:");
        originalValue = frameworkCombo.selection();
        log("original=" + originalValue);

        String[] items = frameworkCombo.items();
        newIndex = (frameworkCombo.selectionIndex() + 1) % items.length;
        frameworkCombo.setSelection(newIndex);
        String newValue = frameworkCombo.selection();
        log("changed to=" + newValue);

        applyAndClose();
        log("after applyAndClose");

        openPreferences();
        selectPreferencePage("Log4E 2026");
        log("after reopen");

        SWTBotCombo frameworkCombo2 = bot.comboBoxWithLabel("Logging Framework:");
        assertEquals("Framework should be saved", newValue, frameworkCombo2.selection());
        log("verified saved=" + frameworkCombo2.selection());

        frameworkCombo2.setSelection(originalValue);
        clickOkAndClose();
        log("after restore");
    }

    @Test
    public void test03_SaveCheckboxState() throws Exception {
        log("start");
        boolean originalState;

        openPreferences();
        selectPreferencePage("Log4E 2026", "Declaration");
        log("after open Declaration");

        SWTBotCheckBox staticCheckbox = bot.checkBox("Declare logger as static");
        originalState = staticCheckbox.isChecked();
        log("original=" + originalState);

        if (originalState) {
            staticCheckbox.deselect();
        } else {
            staticCheckbox.select();
        }
        boolean newState = staticCheckbox.isChecked();
        log("changed to=" + newState);

        applyAndClose();
        log("after applyAndClose");

        openPreferences();
        selectPreferencePage("Log4E 2026", "Declaration");
        log("after reopen");

        SWTBotCheckBox staticCheckbox2 = bot.checkBox("Declare logger as static");
        assertEquals("Checkbox state should be saved", newState, staticCheckbox2.isChecked());
        log("verified saved=" + staticCheckbox2.isChecked());

        if (originalState) {
            staticCheckbox2.select();
        } else {
            staticCheckbox2.deselect();
        }
        clickOkAndClose();
        log("after restore");
    }

    @Test
    public void test04_RestoreDefaults() throws Exception {
        log("start");
        openPreferences();
        selectPreferencePage("Log4E 2026");
        log("after open and select");

        SWTBotText loggerNameField = bot.textWithLabel("Logger Variable Name:");
        String currentValue = loggerNameField.getText();
        log("current=" + currentValue);

        loggerNameField.setText("tempTestValue");
        log("after temp change");

        bot.button("Restore Defaults").click();
        log("after Restore Defaults click");
        TestTimingUtil.waitUntil(bot, Conditions.widgetIsEnabled(bot.button("Apply")));

        String defaultValue = loggerNameField.getText();
        log("after restore, value=" + defaultValue);
        assertEquals("Default logger name should be 'logger'", "logger", defaultValue);

        bot.button("Cancel").click();
        log("after Cancel");
    }

    @Test
    public void test05_CancelDoesNotSave() throws Exception {
        log("start");
        String originalValue;

        openPreferences();
        selectPreferencePage("Log4E 2026");
        log("after open and select");

        SWTBotText loggerNameField = bot.textWithLabel("Logger Variable Name:");
        originalValue = loggerNameField.getText();
        log("original=" + originalValue);

        loggerNameField.setText("shouldNotBeSaved");
        log("after change");

        SWTBotShell prefsShell = bot.shell("Preferences");
        bot.button("Cancel").click();
        TestTimingUtil.waitUntil(bot, Conditions.shellCloses(prefsShell));
        log("after Cancel");

        openPreferences();
        selectPreferencePage("Log4E 2026");
        log("after reopen");

        SWTBotText loggerNameField2 = bot.textWithLabel("Logger Variable Name:");
        assertEquals("Value should NOT be saved after Cancel", originalValue, loggerNameField2.getText());
        log("verified not saved, value=" + loggerNameField2.getText());

        bot.button("Cancel").click();
        log("after final Cancel");
    }

    @Test
    public void test06_SaveMultiplePages() throws Exception {
        log("start");
        String testLoggerName = "multiPageTestLogger";
        boolean originalStaticState;

        openPreferences();
        selectPreferencePage("Log4E 2026");
        log("after open main page");
        SWTBotText loggerNameField = bot.textWithLabel("Logger Variable Name:");
        String originalLoggerName = loggerNameField.getText();
        loggerNameField.setText(testLoggerName);
        log("after change logger name");

        selectPreferencePage("Log4E 2026", "Declaration");
        log("after select Declaration");
        SWTBotCheckBox finalCheckbox = bot.checkBox("Declare logger as final");
        originalStaticState = finalCheckbox.isChecked();
        if (originalStaticState) {
            finalCheckbox.deselect();
        } else {
            finalCheckbox.select();
        }
        boolean newFinalState = finalCheckbox.isChecked();
        log("after toggle final checkbox");

        applyAndClose();
        log("after applyAndClose");

        openPreferences();
        selectPreferencePage("Log4E 2026");
        log("after reopen main");
        SWTBotText loggerNameField2 = bot.textWithLabel("Logger Variable Name:");
        assertEquals("Logger name should be saved", testLoggerName, loggerNameField2.getText());

        selectPreferencePage("Log4E 2026", "Declaration");
        log("after select Declaration again");
        SWTBotCheckBox finalCheckbox2 = bot.checkBox("Declare logger as final");
        assertEquals("Final checkbox state should be saved", newFinalState, finalCheckbox2.isChecked());
        log("verified both changes");

        selectPreferencePage("Log4E 2026");
        bot.textWithLabel("Logger Variable Name:").setText(originalLoggerName);
        selectPreferencePage("Log4E 2026", "Declaration");
        if (originalStaticState) {
            bot.checkBox("Declare logger as final").select();
        } else {
            bot.checkBox("Declare logger as final").deselect();
        }
        clickOkAndClose();
        log("after restore");
    }
}
