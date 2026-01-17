package ro.gs1.log4e2026.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ro.gs1.log4e2026.Log4e2026Plugin;

/**
 * Preference page for logger declaration settings.
 */
public class DeclarationPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage, PreferenceKeys {

    public DeclarationPreferencePage() {
        super(GRID);
        setPreferenceStore(Log4e2026Plugin.getDefault().getPreferenceStore());
        setDescription("Logger Declaration Settings");
    }

    @Override
    public void createFieldEditors() {
        // Import settings
        addField(new BooleanFieldEditor(
            AUTOMATIC_IMPORTS,
            "Automatically add imports when declaring logger",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            TASK_IMPORTS,
            "Create TODO task for manual import review",
            getFieldEditorParent()
        ));

        // Declaration settings
        addField(new BooleanFieldEditor(
            AUTOMATIC_DECLARE,
            "Automatically declare logger when inserting log statements",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            AUTOMATIC_DECLARE_INNER_CLASSES,
            "Declare logger in inner classes",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            TASK_DECLARE,
            "Create TODO task for manual declaration review",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            REAPPLY_DECLARE,
            "Re-apply declaration settings when modifying existing logger",
            getFieldEditorParent()
        ));

        // Logger variable settings
        addField(new StringFieldEditor(
            LOGGER_NAME,
            "Logger variable name:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            LOGGER_TYPE,
            "Logger type:",
            getFieldEditorParent()
        ));

        // Modifier settings
        String[][] accessModifiers = {
            {"private", "private"},
            {"protected", "protected"},
            {"public", "public"},
            {"package", ""}
        };
        addField(new RadioGroupFieldEditor(
            ACCESS_FLAG,
            "Access modifier:",
            4,
            accessModifiers,
            getFieldEditorParent(),
            true
        ));

        addField(new BooleanFieldEditor(
            STATIC_FLAG,
            "Declare logger as static",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            FINAL_FLAG,
            "Declare logger as final",
            getFieldEditorParent()
        ));

        // Comment settings
        addField(new BooleanFieldEditor(
            CREATE_COMMENTS,
            "Create Javadoc comment for logger declaration",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            CREATE_COMMENTS_ONELINE,
            "Use single-line comment instead of Javadoc",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            LOGGER_COMMENT,
            "Logger comment text:",
            getFieldEditorParent()
        ));
    }

    @Override
    public void init(IWorkbench workbench) {
        // Nothing to initialize
    }
}
