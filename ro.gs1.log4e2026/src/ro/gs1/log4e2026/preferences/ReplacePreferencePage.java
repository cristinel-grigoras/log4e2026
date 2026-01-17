package ro.gs1.log4e2026.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ro.gs1.log4e2026.Log4e2026Plugin;

/**
 * Preference page for System.out/err replacement settings and UI options.
 */
public class ReplacePreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage, PreferenceKeys {

    private static final String[][] LOG_LEVELS = {
        {"TRACE", LEVEL_TRACE},
        {"DEBUG", LEVEL_DEBUG},
        {"INFO", LEVEL_INFO},
        {"WARN", LEVEL_WARN},
        {"ERROR", LEVEL_ERROR}
    };

    public ReplacePreferencePage() {
        super(GRID);
        setPreferenceStore(Log4e2026Plugin.getDefault().getPreferenceStore());
        setDescription("System.out/err Replacement and UI Settings");
    }

    @Override
    public void createFieldEditors() {
        // === System.out Replacement ===
        addField(new BooleanFieldEditor(
            REPLACE_SOUT_ENABLED,
            "Enable System.out replacement",
            getFieldEditorParent()
        ));

        addField(new ComboFieldEditor(
            REPLACE_SOUT_LEVEL,
            "System.out replacement log level:",
            LOG_LEVELS,
            getFieldEditorParent()
        ));

        // === System.err Replacement ===
        addField(new BooleanFieldEditor(
            REPLACE_SERR_ENABLED,
            "Enable System.err replacement",
            getFieldEditorParent()
        ));

        addField(new ComboFieldEditor(
            REPLACE_SERR_LEVEL,
            "System.err replacement log level:",
            LOG_LEVELS,
            getFieldEditorParent()
        ));

        // === printStackTrace Replacement ===
        addField(new BooleanFieldEditor(
            REPLACE_STACK_TRACE_ENABLED,
            "Enable printStackTrace() replacement",
            getFieldEditorParent()
        ));

        String[][] streams = {
            {"System.out", "System.out"},
            {"System.err", "System.err"}
        };
        addField(new RadioGroupFieldEditor(
            REPLACE_STACK_TRACE_STREAM,
            "printStackTrace() target stream:",
            2,
            streams,
            getFieldEditorParent(),
            true
        ));

        // === UI Settings ===
        addField(new BooleanFieldEditor(
            UI_SUCCESS_DIALOG,
            "Show success dialog after operations",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            UI_CONFIRMATION,
            "Show confirmation dialogs",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            UI_OUTPUT_ENABLED,
            "Enable output console",
            getFieldEditorParent()
        ));

        String[][] outputOptions = {
            {"Console view", UI_OPTION_OUTPUT_CONSOLE},
            {"Separate window", UI_OPTION_OUTPUT_WINDOW}
        };
        addField(new RadioGroupFieldEditor(
            UI_OUTPUT,
            "Output destination:",
            2,
            outputOptions,
            getFieldEditorParent(),
            true
        ));

        addField(new BooleanFieldEditor(
            UI_CONSOLE_ACTIVATE,
            "Activate console view on output",
            getFieldEditorParent()
        ));

        // === Wizard Settings ===
        addField(new BooleanFieldEditor(
            UI_DECLARE_C_WIZARD,
            "Show wizard for Declare Logger (Class)",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            UI_INSERT_M_WIZARD,
            "Show wizard for Insert Log Statement (Method)",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            UI_INSERT_C_WIZARD,
            "Show wizard for Insert Log Statement (Class)",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            UI_REPLACE_M_WIZARD,
            "Show wizard for Replace System.out (Method)",
            getFieldEditorParent()
        ));

        addField(new BooleanFieldEditor(
            UI_REPLACE_C_WIZARD,
            "Show wizard for Replace System.out (Class)",
            getFieldEditorParent()
        ));
    }

    @Override
    public void init(IWorkbench workbench) {
        // Nothing to initialize
    }
}
