package ro.gs1.log4e2026.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;

import ro.gs1.log4e2026.Log4e2026Plugin;

/**
 * Utility for managing project-scoped preferences.
 * Provides methods to get/set preferences at project level,
 * with fallback to workspace-level preferences.
 */
public class ProjectPreferences implements PreferenceKeys {

    private final IProject project;
    private final IEclipsePreferences projectPrefs;
    private final IPreferenceStore workspacePrefs;

    public ProjectPreferences(IProject project) {
        this.project = project;
        if (project != null) {
            IScopeContext projectScope = new ProjectScope(project);
            this.projectPrefs = projectScope.getNode(Log4e2026Plugin.PLUGIN_ID);
        } else {
            this.projectPrefs = null;
        }
        this.workspacePrefs = Log4e2026Plugin.getPreferences();
    }

    /**
     * Check if project-specific settings are enabled for this project.
     */
    public boolean useProjectSettings() {
        if (projectPrefs == null) {
            System.out.println("[ProjectPreferences] useProjectSettings: projectPrefs is null");
            return false;
        }
        boolean enabled = projectPrefs.getBoolean(PREFERENCES_SCOPE, false);
        System.out.println("[ProjectPreferences] useProjectSettings: PREFERENCES_SCOPE=" + enabled);
        // Also dump all keys for debugging
        try {
            String[] keys = projectPrefs.keys();
            System.out.println("[ProjectPreferences] Available keys in project prefs: " + java.util.Arrays.toString(keys));
        } catch (Exception e) {
            System.out.println("[ProjectPreferences] Could not list keys: " + e.getMessage());
        }
        return enabled;
    }

    /**
     * Enable or disable project-specific settings.
     */
    public void setUseProjectSettings(boolean enabled) {
        if (projectPrefs != null) {
            projectPrefs.putBoolean(PREFERENCES_SCOPE, enabled);
            flush();
        }
    }

    /**
     * Get a string preference, checking project scope first if enabled.
     */
    public String getString(String key) {
        if (useProjectSettings() && projectPrefs != null) {
            String value = projectPrefs.get(key, null);
            if (value != null) {
                return value;
            }
        }
        // Fall back to workspace preference
        return workspacePrefs.getString(key);
    }

    /**
     * Get a string preference with a default value.
     */
    public String getString(String key, String defaultValue) {
        if (useProjectSettings() && projectPrefs != null) {
            String value = projectPrefs.get(key, null);
            if (value != null) {
                return value;
            }
        }
        String workspaceValue = workspacePrefs.getString(key);
        return workspaceValue != null && !workspaceValue.isEmpty() ? workspaceValue : defaultValue;
    }

    /**
     * Get a boolean preference, checking project scope first if enabled.
     */
    public boolean getBoolean(String key) {
        if (useProjectSettings() && projectPrefs != null) {
            // Check if key exists in project prefs
            String value = projectPrefs.get(key, null);
            if (value != null) {
                return projectPrefs.getBoolean(key, false);
            }
        }
        // Fall back to workspace preference
        return workspacePrefs.getBoolean(key);
    }

    /**
     * Get a boolean preference with a default value.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        if (useProjectSettings() && projectPrefs != null) {
            String value = projectPrefs.get(key, null);
            if (value != null) {
                return projectPrefs.getBoolean(key, defaultValue);
            }
        }
        return workspacePrefs.getBoolean(key);
    }

    /**
     * Get an int preference, checking project scope first if enabled.
     */
    public int getInt(String key) {
        if (useProjectSettings() && projectPrefs != null) {
            String value = projectPrefs.get(key, null);
            if (value != null) {
                return projectPrefs.getInt(key, 0);
            }
        }
        return workspacePrefs.getInt(key);
    }

    /**
     * Set a string preference at project level.
     */
    public void setString(String key, String value) {
        if (projectPrefs != null) {
            projectPrefs.put(key, value);
            flush();
        }
    }

    /**
     * Set a boolean preference at project level.
     */
    public void setBoolean(String key, boolean value) {
        if (projectPrefs != null) {
            projectPrefs.putBoolean(key, value);
            flush();
        }
    }

    /**
     * Set an int preference at project level.
     */
    public void setInt(String key, int value) {
        if (projectPrefs != null) {
            projectPrefs.putInt(key, value);
            flush();
        }
    }

    /**
     * Remove a preference from project scope (will fall back to workspace).
     */
    public void remove(String key) {
        if (projectPrefs != null) {
            projectPrefs.remove(key);
            flush();
        }
    }

    /**
     * Flush preferences to disk.
     */
    public void flush() {
        if (projectPrefs != null) {
            try {
                projectPrefs.flush();
            } catch (BackingStoreException e) {
                Log4e2026Plugin.logError("Failed to save project preferences", e);
            }
        }
    }

    /**
     * Get the underlying project preferences node.
     */
    public IEclipsePreferences getProjectNode() {
        return projectPrefs;
    }

    /**
     * Get the project this preferences instance is for.
     */
    public IProject getProject() {
        return project;
    }

    // ========== Convenience getters for common preferences ==========

    /**
     * Get the logger variable name for this project.
     */
    public String getLoggerName() {
        System.out.println("[ProjectPreferences] getLoggerName() called for project: " +
            (project != null ? project.getName() : "null"));
        System.out.println("[ProjectPreferences] useProjectSettings(): " + useProjectSettings());

        // Try project-level key first
        String name = getString(LOGGER_NAME);
        System.out.println("[ProjectPreferences] getString(LOGGER_NAME) returned: '" + name + "'");

        if (name != null && !name.isEmpty()) {
            System.out.println("[ProjectPreferences] Returning project-level name: " + name);
            return name;
        }
        // Try workspace-level key (PreferenceConstants uses different key)
        name = workspacePrefs.getString(PreferenceConstants.P_LOGGER_NAME);
        System.out.println("[ProjectPreferences] Workspace preference name: '" + name + "'");
        if (name != null && !name.isEmpty()) {
            return name;
        }
        System.out.println("[ProjectPreferences] Returning default: " + PreferenceConstants.DEFAULT_LOGGER_NAME);
        return PreferenceConstants.DEFAULT_LOGGER_NAME;
    }

    /**
     * Get the logging framework/profile for this project.
     */
    public String getLoggingFramework() {
        // Try project-level key first
        String framework = getString(LOGGER_PROFILE);
        if (framework != null && !framework.isEmpty()) {
            return framework;
        }
        // Try workspace-level key
        framework = workspacePrefs.getString(PreferenceConstants.P_LOGGING_FRAMEWORK);
        if (framework != null && !framework.isEmpty()) {
            return framework;
        }
        return PreferenceConstants.DEFAULT_FRAMEWORK;
    }

    /**
     * Check if logger should be declared as static.
     */
    public boolean isLoggerStatic() {
        if (useProjectSettings() && projectPrefs != null) {
            String value = projectPrefs.get(STATIC_FLAG, null);
            if (value != null) {
                return projectPrefs.getBoolean(STATIC_FLAG, true);
            }
        }
        return workspacePrefs.getBoolean(PreferenceConstants.P_LOGGER_STATIC);
    }

    /**
     * Check if logger should be declared as final.
     */
    public boolean isLoggerFinal() {
        if (useProjectSettings() && projectPrefs != null) {
            String value = projectPrefs.get(FINAL_FLAG, null);
            if (value != null) {
                return projectPrefs.getBoolean(FINAL_FLAG, true);
            }
        }
        return workspacePrefs.getBoolean(PreferenceConstants.P_LOGGER_FINAL);
    }

    /**
     * Get the message delimiter.
     */
    public String getDelimiter() {
        String delimiter = getString(FORMAT_DELIMITER);
        if (delimiter != null && !delimiter.isEmpty()) {
            return delimiter;
        }
        delimiter = workspacePrefs.getString(PreferenceConstants.P_DELIMITER);
        if (delimiter != null && !delimiter.isEmpty()) {
            return delimiter;
        }
        return PreferenceConstants.DEFAULT_DELIMITER;
    }

    /**
     * Check if automatic imports are enabled.
     */
    public boolean isAutomaticImportsEnabled() {
        return getBoolean(AUTOMATIC_IMPORTS, true);
    }

    /**
     * Check if automatic logger declaration is enabled.
     */
    public boolean isAutomaticDeclareEnabled() {
        return getBoolean(AUTOMATIC_DECLARE, true);
    }

    /**
     * Check if START position logging is enabled.
     */
    public boolean isStartEnabled() {
        return getBoolean(START_ENABLED, true);
    }

    /**
     * Check if END position logging is enabled.
     */
    public boolean isEndEnabled() {
        return getBoolean(END_ENABLED, true);
    }

    /**
     * Check if CATCH block logging is enabled.
     */
    public boolean isCatchEnabled() {
        return getBoolean(CATCH_ENABLED, true);
    }
}
