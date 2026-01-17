package ro.gs1.log4e2026.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.templates.LoggerTemplates;

/**
 * Project property page for Log4E settings.
 * Allows users to configure project-specific logger settings that override workspace defaults.
 */
public class Log4eProjectPropertyPage extends FieldEditorPreferencePage
        implements IWorkbenchPropertyPage, PreferenceKeys {

    private IProject project;
    private BooleanFieldEditor useProjectSettings;

    public Log4eProjectPropertyPage() {
        super(GRID);
        setDescription("Project-specific Log4E Settings");
    }

    @Override
    public void createFieldEditors() {
        Composite parent = getFieldEditorParent();

        // Create info label
        Label infoLabel = new Label(parent, SWT.WRAP);
        infoLabel.setText("Configure project-specific logging settings.\n" +
            "These settings override the workspace-level preferences when enabled.");
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = 2;
        gd.widthHint = 400;
        infoLabel.setLayoutData(gd);

        // Separator
        Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = 2;
        gd.verticalIndent = 10;
        separator.setLayoutData(gd);

        // Enable project-specific settings
        useProjectSettings = new BooleanFieldEditor(
            PREFERENCES_SCOPE,
            "Enable project-specific settings",
            parent
        );
        addField(useProjectSettings);

        // Logging framework selection
        String[][] frameworks = {
            {"SLF4J", LoggerTemplates.SLF4J},
            {"Log4j 2", LoggerTemplates.LOG4J2},
            {"JDK Logging (java.util.logging)", LoggerTemplates.JUL}
        };
        addField(new ComboFieldEditor(
            LOGGER_PROFILE,
            "Logging Framework:",
            frameworks,
            parent
        ));

        // Logger name
        addField(new StringFieldEditor(
            LOGGER_NAME,
            "Logger Variable Name:",
            parent
        ));

        // Logger modifiers
        addField(new BooleanFieldEditor(
            STATIC_FLAG,
            "Declare logger as static",
            parent
        ));

        addField(new BooleanFieldEditor(
            FINAL_FLAG,
            "Declare logger as final",
            parent
        ));

        // Auto-declaration settings
        addField(new BooleanFieldEditor(
            AUTOMATIC_IMPORTS,
            "Automatically add imports",
            parent
        ));

        addField(new BooleanFieldEditor(
            AUTOMATIC_DECLARE,
            "Automatically declare logger when inserting log statements",
            parent
        ));

        // Position settings
        addField(new BooleanFieldEditor(
            START_ENABLED,
            "Enable log statements at method START",
            parent
        ));

        addField(new BooleanFieldEditor(
            END_ENABLED,
            "Enable log statements at method END",
            parent
        ));

        addField(new BooleanFieldEditor(
            CATCH_ENABLED,
            "Enable log statements in CATCH blocks",
            parent
        ));

        // Format settings
        addField(new StringFieldEditor(
            FORMAT_DELIMITER,
            "Message Delimiter:",
            parent
        ));
    }

    @Override
    public IAdaptable getElement() {
        return project;
    }

    @Override
    public void setElement(IAdaptable element) {
        if (element instanceof IProject) {
            this.project = (IProject) element;
        } else if (element != null) {
            this.project = element.getAdapter(IProject.class);
        }
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        // For now, use the plugin's preference store with project qualifier
        // In a full implementation, this would use project-scoped preferences
        return Log4e2026Plugin.getDefault().getPreferenceStore();
    }

    @Override
    public boolean performOk() {
        boolean result = super.performOk();
        if (result && project != null) {
            Log4e2026Plugin.log("Saved project settings for: " + project.getName());
        }
        return result;
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        Log4e2026Plugin.log("Reset project settings to defaults");
    }
}
