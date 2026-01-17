package ro.gs1.log4e2026.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.templates.LoggerTemplate;
import ro.gs1.log4e2026.templates.LoggerTemplates;

/**
 * Preference page for customizing log statement templates.
 * Allows editing of templates used for generating log statements.
 */
public class TemplatePreferencePage extends PreferencePage
        implements IWorkbenchPreferencePage, PreferenceKeys {

    // Template variable buttons
    private Button insertLoggerBtn;
    private Button insertMethodBtn;
    private Button insertClassBtn;
    private Button insertDelimiterBtn;
    private Button insertMessageBtn;
    private Button insertExceptionBtn;
    private Button insertVariablesBtn;
    private Button insertReturnValueBtn;

    // Template editor
    private Text templateEditorText;
    private Text previewText;

    // Framework selector
    private Combo frameworkCombo;

    private IPreferenceStore store;
    private String currentFramework;

    private static final String[] FRAMEWORKS = {
        LoggerTemplates.SLF4J,
        LoggerTemplates.LOG4J2,
        LoggerTemplates.JUL
    };

    public TemplatePreferencePage() {
        super();
        setPreferenceStore(Log4e2026Plugin.getDefault().getPreferenceStore());
        setDescription("Logger Declaration Template Editor\n\n" +
            "Customize the template used for generating logger declarations.");
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

        // Framework selector
        createFrameworkSelector(container);

        // Variable insert buttons
        createVariableButtons(container);

        // Template editor
        createTemplateEditor(container);

        // Preview
        createPreview(container);

        // Load current settings
        loadPreferences();

        return container;
    }

    private void createFrameworkSelector(Composite parent) {
        Composite row = new Composite(parent, SWT.NONE);
        row.setLayout(new GridLayout(2, false));
        row.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(row, SWT.NONE);
        label.setText("Logging Framework:");

        frameworkCombo = new Combo(row, SWT.DROP_DOWN | SWT.READ_ONLY);
        frameworkCombo.setItems(FRAMEWORKS);
        frameworkCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        frameworkCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int idx = frameworkCombo.getSelectionIndex();
                if (idx >= 0 && idx < FRAMEWORKS.length) {
                    currentFramework = FRAMEWORKS[idx];
                    loadTemplateForFramework();
                }
            }
        });
    }

    private void createVariableButtons(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Insert Template Variable");
        group.setLayout(new GridLayout(4, true));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        insertLoggerBtn = createVariableButton(group, "${logger}", "Logger variable name");
        insertMethodBtn = createVariableButton(group, "${enclosing_method}", "Enclosing method signature");
        insertClassBtn = createVariableButton(group, "${enclosing_type}", "Enclosing class name");
        insertDelimiterBtn = createVariableButton(group, "${delimiter}", "Message delimiter");
        insertMessageBtn = createVariableButton(group, "${message}", "Auto-generated message");
        insertExceptionBtn = createVariableButton(group, "${exception}", "Caught exception");
        insertVariablesBtn = createVariableButton(group, "${variables}", "Local variables");
        insertReturnValueBtn = createVariableButton(group, "${return_value}", "Return value");
    }

    private Button createVariableButton(Composite parent, String variable, String tooltip) {
        Button btn = new Button(parent, SWT.PUSH);
        btn.setText(variable);
        btn.setToolTipText(tooltip);
        btn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                insertVariable(variable);
            }
        });
        return btn;
    }

    private void insertVariable(String variable) {
        if (templateEditorText != null && !templateEditorText.isDisposed()) {
            int caretPos = templateEditorText.getCaretPosition();
            String text = templateEditorText.getText();
            String newText = text.substring(0, caretPos) + variable + text.substring(caretPos);
            templateEditorText.setText(newText);
            templateEditorText.setSelection(caretPos + variable.length());
            templateEditorText.setFocus();
            updatePreview();
        }
    }

    private void createTemplateEditor(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Logger Declaration Template");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(GridData.FILL_BOTH));

        templateEditorText = new Text(group, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        templateEditorText.setLayoutData(gd);
        templateEditorText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updatePreview();
            }
        });

        // Reset button
        Button resetBtn = new Button(group, SWT.PUSH);
        resetBtn.setText("Reset to Default");
        resetBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetToDefault();
            }
        });
    }

    private void createPreview(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Preview");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        previewText = new Text(group, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY | SWT.WRAP);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 60;
        previewText.setLayoutData(gd);
    }

    private void loadPreferences() {
        currentFramework = store.getString(LOGGER_PROFILE);
        if (currentFramework == null || currentFramework.isEmpty()) {
            currentFramework = LoggerTemplates.SLF4J;
        }

        // Select framework in combo
        for (int i = 0; i < FRAMEWORKS.length; i++) {
            if (FRAMEWORKS[i].equals(currentFramework)) {
                frameworkCombo.select(i);
                break;
            }
        }

        loadTemplateForFramework();
    }

    private void loadTemplateForFramework() {
        String template = store.getString(LOGGER_INITIALIZER);
        if (template == null || template.isEmpty()) {
            LoggerTemplate tpl = LoggerTemplates.getTemplate(currentFramework);
            if (tpl != null) {
                template = tpl.getDeclaration();
            }
        }
        templateEditorText.setText(template != null ? template : "");
        updatePreview();
    }

    private void resetToDefault() {
        LoggerTemplate tpl = LoggerTemplates.getTemplate(currentFramework);
        if (tpl != null) {
            templateEditorText.setText(tpl.getDeclaration());
            updatePreview();
        }
    }

    private void updatePreview() {
        String template = templateEditorText.getText();

        // Replace placeholders with sample values
        String preview = template
            .replace("${logger}", "logger")
            .replace("${enclosing_type}", "MyClass")
            .replace("${enclosing_method}", "myMethod(String arg)")
            .replace("${enclosing_method_only}", "myMethod")
            .replace("${enclosing_package}", "com.example")
            .replace("${delimiter}", " - ")
            .replace("${message}", "message")
            .replace("${exception}", "e")
            .replace("${variables}", "arg=value")
            .replace("${return_value}", "result");

        previewText.setText(preview);
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
        store.setValue(LOGGER_INITIALIZER, templateEditorText.getText());
        store.setValue(LOGGER_PROFILE, currentFramework);
    }

    @Override
    protected void performDefaults() {
        currentFramework = store.getDefaultString(LOGGER_PROFILE);
        if (currentFramework == null || currentFramework.isEmpty()) {
            currentFramework = LoggerTemplates.SLF4J;
        }

        for (int i = 0; i < FRAMEWORKS.length; i++) {
            if (FRAMEWORKS[i].equals(currentFramework)) {
                frameworkCombo.select(i);
                break;
            }
        }

        resetToDefault();
        super.performDefaults();
    }
}
