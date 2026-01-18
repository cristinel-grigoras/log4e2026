package ro.gs1.log4e2026.preferences;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;
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
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.templates.Profile;
import ro.gs1.log4e2026.templates.ProfileManager;

/**
 * Project property page for Log4E settings.
 * Allows users to configure project-specific logger settings that override workspace defaults.
 */
public class Log4eProjectPropertyPage extends FieldEditorPreferencePage
        implements IWorkbenchPropertyPage, PreferenceKeys {

    private IProject project;
    private ScopedPreferenceStore projectStore;
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

        // Logging framework selection - dynamically load from ProfileManager
        String[][] frameworks = getAvailableFrameworks();
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
        System.out.println("[Log4eProjectPropertyPage] setElement called with: " + element);
        if (element instanceof IProject) {
            this.project = (IProject) element;
        } else if (element != null) {
            this.project = element.getAdapter(IProject.class);
        }
        System.out.println("[Log4eProjectPropertyPage] project set to: " + (project != null ? project.getName() : "null"));
        // Reset store so it gets recreated with correct project scope
        this.projectStore = null;
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        System.out.println("[Log4eProjectPropertyPage] doGetPreferenceStore called, project=" +
            (project != null ? project.getName() : "null") + ", projectStore=" + projectStore);
        if (project != null) {
            // Use lazy initialization to ensure project is set before creating store
            if (projectStore == null) {
                IScopeContext projectScope = new ProjectScope(project);
                projectStore = new ScopedPreferenceStore(projectScope, Log4e2026Plugin.PLUGIN_ID);
                System.out.println("[Log4eProjectPropertyPage] Created project-scoped store for: " + project.getName());
                // Set defaults from workspace preferences
                initializeDefaults(projectStore);
            }
            return projectStore;
        }
        System.out.println("[Log4eProjectPropertyPage] WARNING: Returning workspace store (project is null)");
        return Log4e2026Plugin.getDefault().getPreferenceStore();
    }

    /**
     * Initialize project preference defaults from workspace preferences.
     */
    private void initializeDefaults(ScopedPreferenceStore store) {
        IPreferenceStore workspaceStore = Log4e2026Plugin.getPreferences();
        // Copy current workspace values as defaults for the project
        store.setDefault(PREFERENCES_SCOPE, false);
        store.setDefault(LOGGER_PROFILE, workspaceStore.getString(PreferenceConstants.P_LOGGING_FRAMEWORK));
        store.setDefault(LOGGER_NAME, workspaceStore.getString(PreferenceConstants.P_LOGGER_NAME));
        store.setDefault(STATIC_FLAG, workspaceStore.getBoolean(PreferenceConstants.P_LOGGER_STATIC));
        store.setDefault(FINAL_FLAG, workspaceStore.getBoolean(PreferenceConstants.P_LOGGER_FINAL));
        store.setDefault(AUTOMATIC_IMPORTS, true);
        store.setDefault(AUTOMATIC_DECLARE, true);
        store.setDefault(START_ENABLED, true);
        store.setDefault(END_ENABLED, true);
        store.setDefault(CATCH_ENABLED, true);
        store.setDefault(FORMAT_DELIMITER, workspaceStore.getString(PreferenceConstants.P_DELIMITER));
    }

    @Override
    public boolean performOk() {
        System.out.println("[Log4eProjectPropertyPage] performOk called, project=" +
            (project != null ? project.getName() : "null") + ", projectStore=" + projectStore);
        boolean result = super.performOk();
        System.out.println("[Log4eProjectPropertyPage] super.performOk() returned: " + result);
        if (result && project != null && projectStore != null) {
            try {
                // Debug: print store values before save
                System.out.println("[Log4eProjectPropertyPage] Store values before save:");
                System.out.println("  PREFERENCES_SCOPE=" + projectStore.getBoolean(PREFERENCES_SCOPE));
                System.out.println("  LOGGER_NAME=" + projectStore.getString(LOGGER_NAME));
                System.out.println("  LOGGER_PROFILE=" + projectStore.getString(LOGGER_PROFILE));

                // Save the ScopedPreferenceStore
                projectStore.save();
                System.out.println("[Log4eProjectPropertyPage] projectStore.save() completed");

                // Also flush underlying IEclipsePreferences node to ensure persistence
                IScopeContext projectScope = new ProjectScope(project);
                IEclipsePreferences node = projectScope.getNode(Log4e2026Plugin.PLUGIN_ID);

                // Debug: print node values before flush
                System.out.println("[Log4eProjectPropertyPage] Node values before flush:");
                try {
                    String[] keys = node.keys();
                    System.out.println("  Keys: " + java.util.Arrays.toString(keys));
                    for (String key : keys) {
                        System.out.println("  " + key + "=" + node.get(key, "<not set>"));
                    }
                } catch (BackingStoreException e) {
                    System.out.println("  Could not list keys: " + e.getMessage());
                }

                node.flush();
                System.out.println("[Log4eProjectPropertyPage] node.flush() completed for project: " + project.getName());
                Log4e2026Plugin.log("Saved and flushed project settings for: " + project.getName());
            } catch (java.io.IOException e) {
                System.out.println("[Log4eProjectPropertyPage] IOException: " + e.getMessage());
                Log4e2026Plugin.logError("Failed to save project preferences", e);
            } catch (BackingStoreException e) {
                System.out.println("[Log4eProjectPropertyPage] BackingStoreException: " + e.getMessage());
                Log4e2026Plugin.logError("Failed to flush project preferences", e);
            }
        } else {
            System.out.println("[Log4eProjectPropertyPage] Skipping save: result=" + result +
                ", project=" + (project != null) + ", projectStore=" + (projectStore != null));
        }
        return result;
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        Log4e2026Plugin.log("Reset project settings to defaults");
    }

    /**
     * Get available frameworks from ProfileManager.
     * Returns array of {displayName, profileName} pairs for ComboFieldEditor.
     */
    private String[][] getAvailableFrameworks() {
        List<Profile> profiles = ProfileManager.getInstance().getProfiles().getProfileList();
        String[][] frameworks = new String[profiles.size()][2];
        for (int i = 0; i < profiles.size(); i++) {
            Profile p = profiles.get(i);
            String displayName = p.getTitle();
            if (p.isBuiltIn()) {
                displayName += " (built-in)";
            }
            frameworks[i][0] = displayName;
            frameworks[i][1] = p.getName();  // Use profile name as value
        }
        return frameworks;
    }
}
