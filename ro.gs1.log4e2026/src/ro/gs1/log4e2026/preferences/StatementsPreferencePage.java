package ro.gs1.log4e2026.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ro.gs1.log4e2026.Log4e2026Plugin;

/**
 * Preference page for log statement templates.
 * Supports all log levels: FINEST, FINER, TRACE, DEBUG, INFO, WARN, ERROR, FATAL.
 */
public class StatementsPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage, PreferenceKeys {

    public StatementsPreferencePage() {
        super(GRID);
        setPreferenceStore(Log4e2026Plugin.getDefault().getPreferenceStore());
        setDescription("Log Statement Templates\n\n" +
            "Available placeholders:\n" +
            "  ${logger} - logger variable name\n" +
            "  ${enclosing_method} - method signature\n" +
            "  ${enclosing_type} - class name\n" +
            "  ${delimiter} - message delimiter\n" +
            "  ${message} - auto-generated message\n" +
            "  ${message_user} - user-provided message\n" +
            "  ${variables} - local variables\n" +
            "  ${return_value} - return value\n" +
            "  ${exception} - caught exception");
    }

    @Override
    public void createFieldEditors() {
        // === Log Level Enabled Flags Section ===
        addSeparator("Log Level Settings");

        // JUL-specific levels (FINEST, FINER)
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

        // Standard levels
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

        // === Statement Templates Section ===
        addSeparator("Log Statement Templates");

        // JUL-specific levels
        addField(new StringFieldEditor(
            LOGGER_FINEST_STATEMENT,
            "FINEST statement:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            LOGGER_FINER_STATEMENT,
            "FINER statement:",
            getFieldEditorParent()
        ));

        // Standard levels
        addField(new StringFieldEditor(
            LOGGER_TRACE_STATEMENT,
            "TRACE statement:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            LOGGER_DEBUG_STATEMENT,
            "DEBUG statement:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            LOGGER_INFO_STATEMENT,
            "INFO statement:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            LOGGER_WARN_STATEMENT,
            "WARN statement:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            LOGGER_ERROR_STATEMENT,
            "ERROR statement:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            LOGGER_FATAL_STATEMENT,
            "FATAL statement:",
            getFieldEditorParent()
        ));

        // === Is-Enabled Check Statements Section ===
        addSeparator("Is-Enabled Check Statements");

        addField(new StringFieldEditor(
            LOGGER_IS_FINEST_ENABLED_STATEMENT,
            "isFinestEnabled:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            LOGGER_IS_FINER_ENABLED_STATEMENT,
            "isFinerEnabled:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            LOGGER_IS_TRACE_ENABLED_STATEMENT,
            "isTraceEnabled:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            LOGGER_IS_DEBUG_ENABLED_STATEMENT,
            "isDebugEnabled:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            LOGGER_IS_INFO_ENABLED_STATEMENT,
            "isInfoEnabled:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            LOGGER_IS_WARN_ENABLED_STATEMENT,
            "isWarnEnabled:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            LOGGER_IS_ERROR_ENABLED_STATEMENT,
            "isErrorEnabled:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            LOGGER_IS_FATAL_ENABLED_STATEMENT,
            "isFatalEnabled:",
            getFieldEditorParent()
        ));
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
