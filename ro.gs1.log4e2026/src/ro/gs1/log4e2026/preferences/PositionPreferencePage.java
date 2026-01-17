package ro.gs1.log4e2026.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ro.gs1.log4e2026.Log4e2026Plugin;

/**
 * Preference page for position-specific log statement settings.
 * Uses a 4-tab interface for START, END, CATCH, and OTHER position settings.
 */
public class PositionPreferencePage extends PreferencePage
        implements IWorkbenchPreferencePage, PreferenceKeys {

    private static final String[] LOG_LEVEL_NAMES = {
        "FINEST", "FINER", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"
    };

    private static final String[] LOG_LEVEL_VALUES = {
        LEVEL_FINEST, LEVEL_FINER, LEVEL_TRACE, LEVEL_DEBUG,
        LEVEL_INFO, LEVEL_WARN, LEVEL_ERROR, LEVEL_FATAL
    };

    // START tab controls
    private Button startEnabledCheck;
    private Combo startLevelCombo;
    private Text startMsgText;
    private Button startSkipGetterCheck;
    private Button startSkipSetterCheck;
    private Button startSkipConstructorCheck;
    private Button startSkipToStringCheck;
    private Button startMethodInfoCheck;
    private Button startParamNamesCheck;
    private Button startParamValuesCheck;
    private Button startSkipEmptyMethodsCheck;

    // END tab controls
    private Button endEnabledCheck;
    private Combo endLevelCombo;
    private Text endMsgText;
    private Button endSkipGetterCheck;
    private Button endSkipSetterCheck;
    private Button endSkipConstructorCheck;
    private Button endSkipToStringCheck;
    private Button endReturnValueCheck;
    private Button endSkipEmptyMethodsCheck;

    // CATCH tab controls
    private Button catchEnabledCheck;
    private Combo catchLevelCombo;
    private Text catchMsgText;
    private Button catchSkipSameExceptionCheck;
    private Button catchSkipEmptyCatchCheck;

    // OTHER tab controls
    private Combo otherLevelCombo;
    private Text otherMsgText;
    private Text otherUserMsgText;

    private IPreferenceStore store;

    public PositionPreferencePage() {
        super();
        setPreferenceStore(Log4e2026Plugin.getDefault().getPreferenceStore());
        setDescription("Position-specific Settings for Log Statements");
        store = getPreferenceStore();
    }

    @Override
    public void init(IWorkbench workbench) {
        // Nothing to initialize
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabFolder tabFolder = new TabFolder(container, SWT.TOP);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Create tabs
        createStartTab(tabFolder);
        createEndTab(tabFolder);
        createCatchTab(tabFolder);
        createOtherTab(tabFolder);

        // Load values
        loadPreferences();

        return container;
    }

    private void createStartTab(TabFolder tabFolder) {
        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText("START");
        tabItem.setToolTipText("Log statements at method entry");

        Composite content = createTabContent(tabFolder);
        tabItem.setControl(content);

        // Enable checkbox
        startEnabledCheck = createCheckbox(content, "Enable log statements at method START",
            "Insert log statement at the beginning of methods");

        // Log level
        startLevelCombo = createLevelCombo(content, "Log level:");

        // Message
        startMsgText = createTextField(content, "Message:", "Message text to include (e.g., 'entering')");

        // Skip options
        Group skipGroup = createGroup(content, "Skip Options");

        startSkipGetterCheck = createCheckbox(skipGroup, "Skip getter methods", null);
        startSkipSetterCheck = createCheckbox(skipGroup, "Skip setter methods", null);
        startSkipConstructorCheck = createCheckbox(skipGroup, "Skip constructors", null);
        startSkipToStringCheck = createCheckbox(skipGroup, "Skip toString() methods", null);
        startSkipEmptyMethodsCheck = createCheckbox(skipGroup, "Skip empty methods", null);

        // Include options
        Group includeGroup = createGroup(content, "Include Options");

        startMethodInfoCheck = createCheckbox(includeGroup, "Include method signature", null);
        startParamNamesCheck = createCheckbox(includeGroup, "Include parameter names", null);
        startParamValuesCheck = createCheckbox(includeGroup, "Include parameter values", null);
    }

    private void createEndTab(TabFolder tabFolder) {
        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText("END");
        tabItem.setToolTipText("Log statements at method exit");

        Composite content = createTabContent(tabFolder);
        tabItem.setControl(content);

        // Enable checkbox
        endEnabledCheck = createCheckbox(content, "Enable log statements at method END",
            "Insert log statement at the end of methods");

        // Log level
        endLevelCombo = createLevelCombo(content, "Log level:");

        // Message
        endMsgText = createTextField(content, "Message:", "Message text to include (e.g., 'exiting')");

        // Skip options
        Group skipGroup = createGroup(content, "Skip Options");

        endSkipGetterCheck = createCheckbox(skipGroup, "Skip getter methods", null);
        endSkipSetterCheck = createCheckbox(skipGroup, "Skip setter methods", null);
        endSkipConstructorCheck = createCheckbox(skipGroup, "Skip constructors", null);
        endSkipToStringCheck = createCheckbox(skipGroup, "Skip toString() methods", null);
        endSkipEmptyMethodsCheck = createCheckbox(skipGroup, "Skip empty methods", null);

        // Include options
        Group includeGroup = createGroup(content, "Include Options");

        endReturnValueCheck = createCheckbox(includeGroup, "Include return value", null);
    }

    private void createCatchTab(TabFolder tabFolder) {
        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText("CATCH");
        tabItem.setToolTipText("Log statements in catch blocks");

        Composite content = createTabContent(tabFolder);
        tabItem.setControl(content);

        // Enable checkbox
        catchEnabledCheck = createCheckbox(content, "Enable log statements in CATCH blocks",
            "Insert log statement in exception catch blocks");

        // Log level
        catchLevelCombo = createLevelCombo(content, "Log level:");

        // Message
        catchMsgText = createTextField(content, "Message:", "Message text to include (e.g., 'exception caught')");

        // Skip options
        Group skipGroup = createGroup(content, "Skip Options");

        catchSkipSameExceptionCheck = createCheckbox(skipGroup,
            "Skip if exception is re-thrown",
            "Don't add logging if the catch block throws the same exception");
        catchSkipEmptyCatchCheck = createCheckbox(skipGroup,
            "Skip empty catch blocks",
            "Don't add logging to empty catch blocks");
    }

    private void createOtherTab(TabFolder tabFolder) {
        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText("OTHER");
        tabItem.setToolTipText("Settings for custom log positions");

        Composite content = createTabContent(tabFolder);
        tabItem.setControl(content);

        Label descLabel = new Label(content, SWT.WRAP);
        descLabel.setText("Settings for custom log positions added via 'Log at this position' command.");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        descLabel.setLayoutData(gd);

        // Log level
        otherLevelCombo = createLevelCombo(content, "Default log level:");

        // Message
        otherMsgText = createTextField(content, "Default message:", "Default message for custom positions");

        // User message
        otherUserMsgText = createTextField(content, "User message prompt:", "Prompt shown to user for custom message");
    }

    private Composite createTabContent(TabFolder parent) {
        Composite content = new Composite(parent, SWT.NONE);
        content.setLayout(new GridLayout(2, false));
        content.setLayoutData(new GridData(GridData.FILL_BOTH));
        return content;
    }

    private Button createCheckbox(Composite parent, String text, String tooltip) {
        Button check = new Button(parent, SWT.CHECK);
        check.setText(text);
        if (tooltip != null) {
            check.setToolTipText(tooltip);
        }
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        check.setLayoutData(gd);
        return check;
    }

    private Combo createLevelCombo(Composite parent, String labelText) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(labelText);

        Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setItems(LOG_LEVEL_NAMES);
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return combo;
    }

    private Text createTextField(Composite parent, String labelText, String tooltip) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(labelText);

        Text text = new Text(parent, SWT.BORDER);
        if (tooltip != null) {
            text.setToolTipText(tooltip);
        }
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return text;
    }

    private Group createGroup(Composite parent, String text) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(text);
        group.setLayout(new GridLayout(1, false));
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        group.setLayoutData(gd);
        return group;
    }

    private void loadPreferences() {
        // START tab
        startEnabledCheck.setSelection(store.getBoolean(START_ENABLED));
        setComboByValue(startLevelCombo, store.getString(POS_START + POS_ATT_LEVEL));
        startMsgText.setText(store.getString(POS_START + POS_ATT_MSG));
        startSkipGetterCheck.setSelection(store.getBoolean(POS_START + POS_ATT_SKIP_GETTER));
        startSkipSetterCheck.setSelection(store.getBoolean(POS_START + POS_ATT_SKIP_SETTER));
        startSkipConstructorCheck.setSelection(store.getBoolean(POS_START + POS_ATT_SKIP_CONSTRUCTOR));
        startSkipToStringCheck.setSelection(store.getBoolean(POS_START + POS_ATT_SKIP_TO_STRING));
        startSkipEmptyMethodsCheck.setSelection(store.getBoolean(POS_START + POS_ATT_SKIP_EMPTY_METHODS));
        startMethodInfoCheck.setSelection(store.getBoolean(POS_START + POS_ATT_METHOD_INFO));
        startParamNamesCheck.setSelection(store.getBoolean(POS_START + POS_ATT_PARAMNAMES));
        startParamValuesCheck.setSelection(store.getBoolean(POS_START + POS_ATT_PARAMVALUES));

        // END tab
        endEnabledCheck.setSelection(store.getBoolean(END_ENABLED));
        setComboByValue(endLevelCombo, store.getString(POS_END + POS_ATT_LEVEL));
        endMsgText.setText(store.getString(POS_END + POS_ATT_MSG));
        endSkipGetterCheck.setSelection(store.getBoolean(POS_END + POS_ATT_SKIP_GETTER));
        endSkipSetterCheck.setSelection(store.getBoolean(POS_END + POS_ATT_SKIP_SETTER));
        endSkipConstructorCheck.setSelection(store.getBoolean(POS_END + POS_ATT_SKIP_CONSTRUCTOR));
        endSkipToStringCheck.setSelection(store.getBoolean(POS_END + POS_ATT_SKIP_TO_STRING));
        endSkipEmptyMethodsCheck.setSelection(store.getBoolean(POS_END + POS_ATT_SKIP_EMPTY_METHODS));
        endReturnValueCheck.setSelection(store.getBoolean(POS_END + POS_ATT_RETURN_VALUE));

        // CATCH tab
        catchEnabledCheck.setSelection(store.getBoolean(CATCH_ENABLED));
        setComboByValue(catchLevelCombo, store.getString(POS_CATCH + POS_ATT_LEVEL));
        catchMsgText.setText(store.getString(POS_CATCH + POS_ATT_MSG));
        catchSkipSameExceptionCheck.setSelection(store.getBoolean(POS_CATCH + POS_ATT_SKIP_SAME_EXCEPTION));
        catchSkipEmptyCatchCheck.setSelection(store.getBoolean(POS_CATCH + POS_ATT_SKIP_EMPTY_CATCH_BLOCK));

        // OTHER tab
        setComboByValue(otherLevelCombo, store.getString(POS_OTHER + POS_ATT_LEVEL));
        otherMsgText.setText(store.getString(POS_OTHER + POS_ATT_MSG));
        otherUserMsgText.setText(store.getString(POS_OTHER + POS_ATT_MSG_USER));
    }

    private void setComboByValue(Combo combo, String value) {
        for (int i = 0; i < LOG_LEVEL_VALUES.length; i++) {
            if (LOG_LEVEL_VALUES[i].equals(value)) {
                combo.select(i);
                return;
            }
        }
        // Default to DEBUG (index 3)
        combo.select(3);
    }

    private String getComboValue(Combo combo) {
        int idx = combo.getSelectionIndex();
        if (idx >= 0 && idx < LOG_LEVEL_VALUES.length) {
            return LOG_LEVEL_VALUES[idx];
        }
        return LEVEL_DEBUG;
    }

    @Override
    public boolean performOk() {
        savePreferences();
        return super.performOk();
    }

    @Override
    protected void performApply() {
        savePreferences();
        super.performApply();
    }

    private void savePreferences() {
        // START tab
        store.setValue(START_ENABLED, startEnabledCheck.getSelection());
        store.setValue(POS_START + POS_ATT_LEVEL, getComboValue(startLevelCombo));
        store.setValue(POS_START + POS_ATT_MSG, startMsgText.getText());
        store.setValue(POS_START + POS_ATT_SKIP_GETTER, startSkipGetterCheck.getSelection());
        store.setValue(POS_START + POS_ATT_SKIP_SETTER, startSkipSetterCheck.getSelection());
        store.setValue(POS_START + POS_ATT_SKIP_CONSTRUCTOR, startSkipConstructorCheck.getSelection());
        store.setValue(POS_START + POS_ATT_SKIP_TO_STRING, startSkipToStringCheck.getSelection());
        store.setValue(POS_START + POS_ATT_SKIP_EMPTY_METHODS, startSkipEmptyMethodsCheck.getSelection());
        store.setValue(POS_START + POS_ATT_METHOD_INFO, startMethodInfoCheck.getSelection());
        store.setValue(POS_START + POS_ATT_PARAMNAMES, startParamNamesCheck.getSelection());
        store.setValue(POS_START + POS_ATT_PARAMVALUES, startParamValuesCheck.getSelection());

        // END tab
        store.setValue(END_ENABLED, endEnabledCheck.getSelection());
        store.setValue(POS_END + POS_ATT_LEVEL, getComboValue(endLevelCombo));
        store.setValue(POS_END + POS_ATT_MSG, endMsgText.getText());
        store.setValue(POS_END + POS_ATT_SKIP_GETTER, endSkipGetterCheck.getSelection());
        store.setValue(POS_END + POS_ATT_SKIP_SETTER, endSkipSetterCheck.getSelection());
        store.setValue(POS_END + POS_ATT_SKIP_CONSTRUCTOR, endSkipConstructorCheck.getSelection());
        store.setValue(POS_END + POS_ATT_SKIP_TO_STRING, endSkipToStringCheck.getSelection());
        store.setValue(POS_END + POS_ATT_SKIP_EMPTY_METHODS, endSkipEmptyMethodsCheck.getSelection());
        store.setValue(POS_END + POS_ATT_RETURN_VALUE, endReturnValueCheck.getSelection());

        // CATCH tab
        store.setValue(CATCH_ENABLED, catchEnabledCheck.getSelection());
        store.setValue(POS_CATCH + POS_ATT_LEVEL, getComboValue(catchLevelCombo));
        store.setValue(POS_CATCH + POS_ATT_MSG, catchMsgText.getText());
        store.setValue(POS_CATCH + POS_ATT_SKIP_SAME_EXCEPTION, catchSkipSameExceptionCheck.getSelection());
        store.setValue(POS_CATCH + POS_ATT_SKIP_EMPTY_CATCH_BLOCK, catchSkipEmptyCatchCheck.getSelection());

        // OTHER tab
        store.setValue(POS_OTHER + POS_ATT_LEVEL, getComboValue(otherLevelCombo));
        store.setValue(POS_OTHER + POS_ATT_MSG, otherMsgText.getText());
        store.setValue(POS_OTHER + POS_ATT_MSG_USER, otherUserMsgText.getText());
    }

    @Override
    protected void performDefaults() {
        // START tab
        startEnabledCheck.setSelection(store.getDefaultBoolean(START_ENABLED));
        setComboByValue(startLevelCombo, store.getDefaultString(POS_START + POS_ATT_LEVEL));
        startMsgText.setText(store.getDefaultString(POS_START + POS_ATT_MSG));
        startSkipGetterCheck.setSelection(store.getDefaultBoolean(POS_START + POS_ATT_SKIP_GETTER));
        startSkipSetterCheck.setSelection(store.getDefaultBoolean(POS_START + POS_ATT_SKIP_SETTER));
        startSkipConstructorCheck.setSelection(store.getDefaultBoolean(POS_START + POS_ATT_SKIP_CONSTRUCTOR));
        startSkipToStringCheck.setSelection(store.getDefaultBoolean(POS_START + POS_ATT_SKIP_TO_STRING));
        startSkipEmptyMethodsCheck.setSelection(store.getDefaultBoolean(POS_START + POS_ATT_SKIP_EMPTY_METHODS));
        startMethodInfoCheck.setSelection(store.getDefaultBoolean(POS_START + POS_ATT_METHOD_INFO));
        startParamNamesCheck.setSelection(store.getDefaultBoolean(POS_START + POS_ATT_PARAMNAMES));
        startParamValuesCheck.setSelection(store.getDefaultBoolean(POS_START + POS_ATT_PARAMVALUES));

        // END tab
        endEnabledCheck.setSelection(store.getDefaultBoolean(END_ENABLED));
        setComboByValue(endLevelCombo, store.getDefaultString(POS_END + POS_ATT_LEVEL));
        endMsgText.setText(store.getDefaultString(POS_END + POS_ATT_MSG));
        endSkipGetterCheck.setSelection(store.getDefaultBoolean(POS_END + POS_ATT_SKIP_GETTER));
        endSkipSetterCheck.setSelection(store.getDefaultBoolean(POS_END + POS_ATT_SKIP_SETTER));
        endSkipConstructorCheck.setSelection(store.getDefaultBoolean(POS_END + POS_ATT_SKIP_CONSTRUCTOR));
        endSkipToStringCheck.setSelection(store.getDefaultBoolean(POS_END + POS_ATT_SKIP_TO_STRING));
        endSkipEmptyMethodsCheck.setSelection(store.getDefaultBoolean(POS_END + POS_ATT_SKIP_EMPTY_METHODS));
        endReturnValueCheck.setSelection(store.getDefaultBoolean(POS_END + POS_ATT_RETURN_VALUE));

        // CATCH tab
        catchEnabledCheck.setSelection(store.getDefaultBoolean(CATCH_ENABLED));
        setComboByValue(catchLevelCombo, store.getDefaultString(POS_CATCH + POS_ATT_LEVEL));
        catchMsgText.setText(store.getDefaultString(POS_CATCH + POS_ATT_MSG));
        catchSkipSameExceptionCheck.setSelection(store.getDefaultBoolean(POS_CATCH + POS_ATT_SKIP_SAME_EXCEPTION));
        catchSkipEmptyCatchCheck.setSelection(store.getDefaultBoolean(POS_CATCH + POS_ATT_SKIP_EMPTY_CATCH_BLOCK));

        // OTHER tab
        setComboByValue(otherLevelCombo, store.getDefaultString(POS_OTHER + POS_ATT_LEVEL));
        otherMsgText.setText(store.getDefaultString(POS_OTHER + POS_ATT_MSG));
        otherUserMsgText.setText(store.getDefaultString(POS_OTHER + POS_ATT_MSG_USER));

        super.performDefaults();
    }
}
