package ro.gs1.log4e2026.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.templates.Profile;
import ro.gs1.log4e2026.templates.ProfileManager;

/**
 * Preference page for log statement settings.
 * Shows log level enabled flags (global preferences) and a read-only preview
 * of the active profile's statement templates.
 */
public class StatementsPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage, PreferenceKeys {

    private Text previewText;

    public StatementsPreferencePage() {
        super(GRID);
        setPreferenceStore(Log4e2026Plugin.getDefault().getPreferenceStore());
        setDescription("Log Statement Settings");
    }

    @Override
    public void createFieldEditors() {
        // === Log Level Enabled Flags Section ===
        addSeparator("Log Level Settings");

        addField(new BooleanFieldEditor(
            FINEST_ENABLED,
            "Enable FINEST level (JUL only)",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            FINER_ENABLED,
            "Enable FINER level (JUL only)",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            TRACE_ENABLED,
            "Enable TRACE level",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            DEBUG_ENABLED,
            "Enable DEBUG level",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            INFO_ENABLED,
            "Enable INFO level",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            WARN_ENABLED,
            "Enable WARN level",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            ERROR_ENABLED,
            "Enable ERROR level",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            FATAL_ENABLED,
            "Enable FATAL level (Log4j2 only)",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            ENABLED_BRACES,
            "Wrap log statements in if-enabled checks",
            getFieldEditorParent()
        ));

        // === Profile Statement Preview Section ===
        Composite parent = getFieldEditorParent();
        addSeparator("Active Profile Statement Templates (read-only)");

        Label infoLabel = new Label(parent, SWT.WRAP);
        infoLabel.setText("Statement templates are defined per profile. Edit them via the Templates preference page.");
        GridData infoGd = new GridData(GridData.FILL_HORIZONTAL);
        infoGd.horizontalSpan = 2;
        infoLabel.setLayoutData(infoGd);

        Group previewGroup = new Group(parent, SWT.NONE);
        previewGroup.setText("Current Profile Templates");
        previewGroup.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
        GridData groupGd = new GridData(GridData.FILL_BOTH);
        groupGd.horizontalSpan = 2;
        groupGd.heightHint = 200;
        previewGroup.setLayoutData(groupGd);

        previewText = new Text(previewGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
        previewText.setLayoutData(new GridData(GridData.FILL_BOTH));

        loadPreview();
    }

    private void loadPreview() {
        Profile profile = ProfileManager.getInstance().getCurrentProfile();
        if (profile == null || previewText == null || previewText.isDisposed()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Profile: ").append(profile.getTitle()).append("\n\n");

        sb.append("=== Log Statements ===\n");
        appendSetting(sb, profile, "LOGGER_TRACE_STATEMENT", "TRACE");
        appendSetting(sb, profile, "LOGGER_DEBUG_STATEMENT", "DEBUG");
        appendSetting(sb, profile, "LOGGER_INFO_STATEMENT", "INFO");
        appendSetting(sb, profile, "LOGGER_WARN_STATEMENT", "WARN");
        appendSetting(sb, profile, "LOGGER_ERROR_STATEMENT", "ERROR");
        appendSetting(sb, profile, "LOGGER_FATAL_STATEMENT", "FATAL");
        appendSetting(sb, profile, "LOGGER_FINEST_STATEMENT", "FINEST");
        appendSetting(sb, profile, "LOGGER_FINER_STATEMENT", "FINER");

        sb.append("\n=== Is-Enabled Checks ===\n");
        appendSetting(sb, profile, "LOGGER_IS_TRACE_ENABLED_STATEMENT", "TRACE");
        appendSetting(sb, profile, "LOGGER_IS_DEBUG_ENABLED_STATEMENT", "DEBUG");
        appendSetting(sb, profile, "LOGGER_IS_INFO_ENABLED_STATEMENT", "INFO");
        appendSetting(sb, profile, "LOGGER_IS_WARN_ENABLED_STATEMENT", "WARN");
        appendSetting(sb, profile, "LOGGER_IS_ERROR_ENABLED_STATEMENT", "ERROR");
        appendSetting(sb, profile, "LOGGER_IS_FATAL_ENABLED_STATEMENT", "FATAL");
        appendSetting(sb, profile, "LOGGER_IS_FINEST_ENABLED_STATEMENT", "FINEST");
        appendSetting(sb, profile, "LOGGER_IS_FINER_ENABLED_STATEMENT", "FINER");

        sb.append("\n=== Position Statements ===\n");
        appendSetting(sb, profile, "LOGGER_POS_START_STATEMENT", "START");
        appendSetting(sb, profile, "LOGGER_POS_END_STATEMENT", "END");
        appendSetting(sb, profile, "LOGGER_POS_CATCH_STATEMENT", "CATCH");

        previewText.setText(sb.toString());
    }

    private void appendSetting(StringBuilder sb, Profile profile, String key, String label) {
        String value = profile.getString(key);
        if (value != null && !value.isEmpty()) {
            sb.append(label).append(": ").append(value).append("\n");
        }
    }

    private void addSeparator(String text) {
        Label label = new Label(getFieldEditorParent(), SWT.NONE);
        label.setText(text);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        label.setLayoutData(gd);
    }

    @Override
    public void init(IWorkbench workbench) {
        // Nothing to initialize
    }
}
