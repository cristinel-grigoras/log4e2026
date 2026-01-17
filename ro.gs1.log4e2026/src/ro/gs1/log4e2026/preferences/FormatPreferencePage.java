package ro.gs1.log4e2026.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ro.gs1.log4e2026.Log4e2026Plugin;

/**
 * Preference page for log message format settings.
 */
public class FormatPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage, PreferenceKeys {

    public FormatPreferencePage() {
        super(GRID);
        setPreferenceStore(Log4e2026Plugin.getDefault().getPreferenceStore());
        setDescription("Log Message Format Settings");
    }

    @Override
    public void createFieldEditors() {
        // Delimiters
        addField(new StringFieldEditor(
            FORMAT_DELIMITER,
            "General delimiter:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            FORMAT_DELIMITER_MSG,
            "Message delimiter:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            FORMAT_DELIMITER_PARAM,
            "Parameter delimiter:",
            getFieldEditorParent()
        ));

        // Formatting symbols
        addField(new StringFieldEditor(
            FORMAT_PARENTHESIS_OPEN,
            "Opening parenthesis:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            FORMAT_PARENTHESIS_CLOSE,
            "Closing parenthesis:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            FORMAT_EQUALS_SYMBOL,
            "Equals symbol:",
            getFieldEditorParent()
        ));

        // Return value
        addField(new StringFieldEditor(
            FORMAT_RETURN_VALUE_NAME,
            "Return value name:",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            FORMAT_ARGS_EMPTY_NAME,
            "Empty args placeholder:",
            getFieldEditorParent()
        ));

        // Placeholder format
        addField(new StringFieldEditor(
            LOGGER_FORMAT_VARIABLE_PLACEHOLDER,
            "Variable placeholder (e.g., {} for SLF4J):",
            getFieldEditorParent()
        ));

        // String style
        String[][] stringStyles = {
            {"String concatenation", "concatenation"},
            {"String.format()", "format"},
            {"MessageFormat", "messageformat"}
        };
        addField(new RadioGroupFieldEditor(
            FORMAT_STRING_STYLE,
            "String building style:",
            1,
            stringStyles,
            getFieldEditorParent(),
            true
        ));

        // Layout options
        addField(new BooleanFieldEditor(
            FORMAT_ONE_LINE,
            "Generate log statements on single line",
            getFieldEditorParent()
        ));

        // String generator options
        addField(new BooleanFieldEditor(
            FORMAT_USE_STRING_GENERATOR,
            "Use custom string generator class",
            getFieldEditorParent()
        ));

        addField(new StringFieldEditor(
            FORMAT_STRING_GENERATOR_CLASS,
            "String generator class:",
            getFieldEditorParent()
        ));

        // Miscellaneous
        addField(new BooleanFieldEditor(
            ADD_NON_NLS,
            "Add $NON-NLS$ comments to string literals",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            INNER_CLASSES_ENABLED,
            "Process inner classes",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            ANONYMOUS_CLASS_ENABLED,
            "Process anonymous classes",
            getFieldEditorParent()
        ));
    }

    @Override
    public void init(IWorkbench workbench) {
        // Nothing to initialize
    }
}
