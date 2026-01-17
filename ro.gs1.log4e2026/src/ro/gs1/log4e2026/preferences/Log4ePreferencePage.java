package ro.gs1.log4e2026.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.templates.LoggerTemplates;

/**
 * Main preference page for Log4E plugin.
 */
public class Log4ePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public Log4ePreferencePage() {
        super(GRID);
        setPreferenceStore(Log4e2026Plugin.getDefault().getPreferenceStore());
        setDescription("Log4E 2026 - Logging Statement Generator");
    }

    @Override
    public void createFieldEditors() {
        // Logging framework selection
        String[][] frameworks = {
            {"SLF4J", LoggerTemplates.SLF4J},
            {"Log4j 2", LoggerTemplates.LOG4J2},
            {"JDK Logging (java.util.logging)", LoggerTemplates.JUL}
        };
        addField(new ComboFieldEditor(
            PreferenceConstants.P_LOGGING_FRAMEWORK,
            "Logging Framework:",
            frameworks,
            getFieldEditorParent()
        ));

        // Logger name
        addField(new StringFieldEditor(
            PreferenceConstants.P_LOGGER_NAME,
            "Logger Variable Name:",
            getFieldEditorParent()
        ));

        // Logger modifiers
        addField(new BooleanFieldEditor(
            PreferenceConstants.P_LOGGER_STATIC,
            "Declare logger as static",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            PreferenceConstants.P_LOGGER_FINAL,
            "Declare logger as final",
            getFieldEditorParent()
        ));

        // Format options
        addField(new StringFieldEditor(
            PreferenceConstants.P_DELIMITER,
            "Message Delimiter:",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            PreferenceConstants.P_INCLUDE_CLASS_NAME,
            "Include class name in log messages",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            PreferenceConstants.P_INCLUDE_METHOD_NAME,
            "Include method name in log messages",
            getFieldEditorParent()
        ));

        // User interaction - Exchange dialog
        addField(new BooleanFieldEditor(
            PreferenceConstants.P_SHOW_EXCHANGE_DIALOG,
            "Show confirmation dialog when exchanging frameworks",
            getFieldEditorParent()
        ));

        // User interaction - Preview wizards
        addField(new BooleanFieldEditor(
            PreferenceConstants.P_WIZARD_DECLARE_CLASS,
            "Show preview wizard for Declare Logger",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            PreferenceConstants.P_WIZARD_INSERT_METHOD,
            "Show preview wizard for Insert Log in Method",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            PreferenceConstants.P_WIZARD_INSERT_CLASS,
            "Show preview wizard for Log This Class",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            PreferenceConstants.P_WIZARD_REPLACE_METHOD,
            "Show preview wizard for Replace in Method",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            PreferenceConstants.P_WIZARD_REPLACE_CLASS,
            "Show preview wizard for Replace in Class",
            getFieldEditorParent()
        ));
    }

    @Override
    public void init(IWorkbench workbench) {
        // Nothing to initialize
    }
}
