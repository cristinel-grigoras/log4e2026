package ro.gs1.log4e2026.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import ro.gs1.log4e2026.templates.Profile;

/**
 * Dialog for editing logger profile settings.
 * Contains tabs for: Declaration, Log Levels, Statement Patterns, Position Settings, Format.
 */
public class TemplateDialog extends Dialog {

    private final Profile profile;
    private final boolean readOnly;

    // Declaration tab
    private Text loggerTypeText;
    private Text loggerFactoryText;
    private Text factoryMethodText;
    private Text declarationText;
    private Text importsText;

    // Log Levels tab
    private Text traceMethodText;
    private Text debugMethodText;
    private Text infoMethodText;
    private Text warnMethodText;
    private Text errorMethodText;
    private Text fatalMethodText;
    private Text finestMethodText;
    private Text finerMethodText;

    // Statement Patterns tab
    private Text patternStartText;
    private Text patternEndText;
    private Text patternCatchText;
    private Text patternVariableText;

    // Default Levels tab
    private Text levelStartText;
    private Text levelEndText;
    private Text levelCatchText;
    private Text levelDefaultText;

    public TemplateDialog(Shell parentShell, Profile profile) {
        super(parentShell);
        this.profile = profile;
        this.readOnly = profile.isBuiltIn();
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        String mode = readOnly ? " (Read-only)" : " (Editable)";
        shell.setText("Edit Profile: " + profile.getTitle() + mode);
        shell.setMinimumSize(500, 400);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        TabFolder tabFolder = new TabFolder(container, SWT.NONE);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        createDeclarationTab(tabFolder);
        createLogLevelsTab(tabFolder);
        createStatementPatternsTab(tabFolder);
        createDefaultLevelsTab(tabFolder);

        loadProfileData();

        return container;
    }

    private void createDeclarationTab(TabFolder tabFolder) {
        TabItem tab = new TabItem(tabFolder, SWT.NONE);
        tab.setText("Declaration");

        Composite content = new Composite(tabFolder, SWT.NONE);
        content.setLayout(new GridLayout(2, false));

        // Logger Type
        new Label(content, SWT.NONE).setText("Logger Type:");
        loggerTypeText = createText(content);

        // Logger Factory
        new Label(content, SWT.NONE).setText("Logger Factory:");
        loggerFactoryText = createText(content);

        // Factory Method
        new Label(content, SWT.NONE).setText("Factory Method:");
        factoryMethodText = createText(content);

        // Declaration
        Group declGroup = new Group(content, SWT.NONE);
        declGroup.setText("Logger Declaration Template");
        declGroup.setLayout(new GridLayout(1, false));
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        declGroup.setLayoutData(gd);

        declarationText = new Text(declGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        declarationText.setLayoutData(new GridData(GridData.FILL_BOTH));
        declarationText.setEditable(!readOnly);

        // Imports
        Group importsGroup = new Group(content, SWT.NONE);
        importsGroup.setText("Required Imports (one per line)");
        importsGroup.setLayout(new GridLayout(1, false));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        gd.heightHint = 60;
        importsGroup.setLayoutData(gd);

        importsText = new Text(importsGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        importsText.setLayoutData(new GridData(GridData.FILL_BOTH));
        importsText.setEditable(!readOnly);

        tab.setControl(content);
    }

    private void createLogLevelsTab(TabFolder tabFolder) {
        TabItem tab = new TabItem(tabFolder, SWT.NONE);
        tab.setText("Log Levels");

        Composite content = new Composite(tabFolder, SWT.NONE);
        content.setLayout(new GridLayout(2, false));

        Label info = new Label(content, SWT.WRAP);
        info.setText("Configure the method names for each log level. Leave empty if not supported by the framework.");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        info.setLayoutData(gd);

        new Label(content, SWT.NONE).setText("TRACE method:");
        traceMethodText = createText(content);

        new Label(content, SWT.NONE).setText("DEBUG method:");
        debugMethodText = createText(content);

        new Label(content, SWT.NONE).setText("INFO method:");
        infoMethodText = createText(content);

        new Label(content, SWT.NONE).setText("WARN method:");
        warnMethodText = createText(content);

        new Label(content, SWT.NONE).setText("ERROR method:");
        errorMethodText = createText(content);

        new Label(content, SWT.NONE).setText("FATAL method:");
        fatalMethodText = createText(content);

        new Label(content, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(
                new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        Label julLabel = new Label(content, SWT.WRAP);
        julLabel.setText("For JDK Logging (java.util.logging):");
        julLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        new Label(content, SWT.NONE).setText("FINEST method:");
        finestMethodText = createText(content);

        new Label(content, SWT.NONE).setText("FINER method:");
        finerMethodText = createText(content);

        tab.setControl(content);
    }

    private void createStatementPatternsTab(TabFolder tabFolder) {
        TabItem tab = new TabItem(tabFolder, SWT.NONE);
        tab.setText("Statement Patterns");

        Composite content = new Composite(tabFolder, SWT.NONE);
        content.setLayout(new GridLayout(1, false));

        Label info = new Label(content, SWT.WRAP);
        info.setText("Configure the log statement patterns. Use variables:\n" +
                "${logger} - logger variable name\n" +
                "${level} - log method (e.g., debug, info)\n" +
                "${enclosing_method} - method name with signature\n" +
                "${enclosing_type} - class name\n" +
                "${variable} - variable name\n" +
                "${exception} - exception variable");
        info.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Start pattern
        Group startGroup = new Group(content, SWT.NONE);
        startGroup.setText("Method Entry (START)");
        startGroup.setLayout(new GridLayout(1, false));
        startGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        patternStartText = new Text(startGroup, SWT.BORDER);
        patternStartText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        patternStartText.setEditable(!readOnly);

        // End pattern
        Group endGroup = new Group(content, SWT.NONE);
        endGroup.setText("Method Exit (END)");
        endGroup.setLayout(new GridLayout(1, false));
        endGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        patternEndText = new Text(endGroup, SWT.BORDER);
        patternEndText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        patternEndText.setEditable(!readOnly);

        // Catch pattern
        Group catchGroup = new Group(content, SWT.NONE);
        catchGroup.setText("Exception Catch (CATCH)");
        catchGroup.setLayout(new GridLayout(1, false));
        catchGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        patternCatchText = new Text(catchGroup, SWT.BORDER);
        patternCatchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        patternCatchText.setEditable(!readOnly);

        // Variable pattern
        Group varGroup = new Group(content, SWT.NONE);
        varGroup.setText("Variable Logging");
        varGroup.setLayout(new GridLayout(1, false));
        varGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        patternVariableText = new Text(varGroup, SWT.BORDER);
        patternVariableText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        patternVariableText.setEditable(!readOnly);

        tab.setControl(content);
    }

    private void createDefaultLevelsTab(TabFolder tabFolder) {
        TabItem tab = new TabItem(tabFolder, SWT.NONE);
        tab.setText("Default Levels");

        Composite content = new Composite(tabFolder, SWT.NONE);
        content.setLayout(new GridLayout(2, false));

        Label info = new Label(content, SWT.WRAP);
        info.setText("Configure the default log level for each position type.");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        info.setLayoutData(gd);

        new Label(content, SWT.NONE).setText("Method Entry (START) level:");
        levelStartText = createText(content);

        new Label(content, SWT.NONE).setText("Method Exit (END) level:");
        levelEndText = createText(content);

        new Label(content, SWT.NONE).setText("Exception Catch level:");
        levelCatchText = createText(content);

        new Label(content, SWT.NONE).setText("Default level:");
        levelDefaultText = createText(content);

        tab.setControl(content);
    }

    private Text createText(Composite parent) {
        Text text = new Text(parent, SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.setEditable(!readOnly);
        return text;
    }

    private void loadProfileData() {
        // Declaration tab
        setText(loggerTypeText, profile.getString("LOGGER_TYPE"));
        setText(loggerFactoryText, profile.getString("LOGGER_FACTORY"));
        setText(factoryMethodText, profile.getString("LOGGER_FACTORY_METHOD"));
        setText(declarationText, profile.getString("LOGGER_DECLARATION"));
        setText(importsText, profile.getString("LOGGER_IMPORTS"));

        // Log Levels tab
        setText(traceMethodText, profile.getString("LOG_METHOD_TRACE"));
        setText(debugMethodText, profile.getString("LOG_METHOD_DEBUG"));
        setText(infoMethodText, profile.getString("LOG_METHOD_INFO"));
        setText(warnMethodText, profile.getString("LOG_METHOD_WARN"));
        setText(errorMethodText, profile.getString("LOG_METHOD_ERROR"));
        setText(fatalMethodText, profile.getString("LOG_METHOD_FATAL"));
        setText(finestMethodText, profile.getString("LOG_METHOD_FINEST"));
        setText(finerMethodText, profile.getString("LOG_METHOD_FINER"));

        // Statement Patterns tab
        setText(patternStartText, profile.getString("LOG_PATTERN_START"));
        setText(patternEndText, profile.getString("LOG_PATTERN_END"));
        setText(patternCatchText, profile.getString("LOG_PATTERN_CATCH"));
        setText(patternVariableText, profile.getString("LOG_PATTERN_VARIABLE"));

        // Default Levels tab
        setText(levelStartText, profile.getString("LOG_LEVEL_START"));
        setText(levelEndText, profile.getString("LOG_LEVEL_END"));
        setText(levelCatchText, profile.getString("LOG_LEVEL_CATCH"));
        setText(levelDefaultText, profile.getString("LOG_LEVEL_DEFAULT"));
    }

    private void setText(Text text, String value) {
        if (text != null && value != null) {
            text.setText(value);
        }
    }

    private void saveProfileData() {
        if (readOnly) {
            return;
        }

        // Declaration tab
        profile.put("LOGGER_TYPE", loggerTypeText.getText());
        profile.put("LOGGER_FACTORY", loggerFactoryText.getText());
        profile.put("LOGGER_FACTORY_METHOD", factoryMethodText.getText());
        profile.put("LOGGER_DECLARATION", declarationText.getText());
        profile.put("LOGGER_IMPORTS", importsText.getText());

        // Log Levels tab
        putIfNotEmpty("LOG_METHOD_TRACE", traceMethodText);
        putIfNotEmpty("LOG_METHOD_DEBUG", debugMethodText);
        putIfNotEmpty("LOG_METHOD_INFO", infoMethodText);
        putIfNotEmpty("LOG_METHOD_WARN", warnMethodText);
        putIfNotEmpty("LOG_METHOD_ERROR", errorMethodText);
        putIfNotEmpty("LOG_METHOD_FATAL", fatalMethodText);
        putIfNotEmpty("LOG_METHOD_FINEST", finestMethodText);
        putIfNotEmpty("LOG_METHOD_FINER", finerMethodText);

        // Statement Patterns tab
        profile.put("LOG_PATTERN_START", patternStartText.getText());
        profile.put("LOG_PATTERN_END", patternEndText.getText());
        profile.put("LOG_PATTERN_CATCH", patternCatchText.getText());
        profile.put("LOG_PATTERN_VARIABLE", patternVariableText.getText());

        // Default Levels tab
        profile.put("LOG_LEVEL_START", levelStartText.getText());
        profile.put("LOG_LEVEL_END", levelEndText.getText());
        profile.put("LOG_LEVEL_CATCH", levelCatchText.getText());
        profile.put("LOG_LEVEL_DEFAULT", levelDefaultText.getText());
    }

    private void putIfNotEmpty(String key, Text text) {
        String value = text.getText();
        if (value != null && !value.trim().isEmpty()) {
            profile.put(key, value);
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

        // Disable OK button for read-only profiles
        if (readOnly) {
            Button okButton = getButton(IDialogConstants.OK_ID);
            if (okButton != null) {
                okButton.setEnabled(false);
            }
        }
    }

    @Override
    protected void okPressed() {
        saveProfileData();
        super.okPressed();
    }

    public Profile getProfile() {
        return profile;
    }
}
