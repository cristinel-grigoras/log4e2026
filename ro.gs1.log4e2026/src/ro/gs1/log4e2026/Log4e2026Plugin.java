package ro.gs1.log4e2026;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ro.gs1.log4e2026.preferences.ProjectPreferences;

/**
 * The activator class controls the plug-in life cycle.
 * Ported from de.jayefem.log4e.LoggerPlugin
 */
public class Log4e2026Plugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "ro.gs1.log4e2026";

    private static Log4e2026Plugin plugin;

    public Log4e2026Plugin() {
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        log("Log4E 2026 plugin started");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static Log4e2026Plugin getDefault() {
        if (plugin == null) {
            plugin = new Log4e2026Plugin();
        }
        return plugin;
    }

    public String getSymbolicName() {
        if (getBundle() != null) {
            return getBundle().getSymbolicName();
        }
        return PLUGIN_ID;
    }

    public static void log(String message) {
        if (plugin != null) {
            plugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
        }
    }

    public static void logError(String message, Throwable e) {
        if (plugin != null) {
            plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
        }
    }

    public static void log(Throwable e) {
        logError(e.getMessage(), e);
    }

    public static void logWarning(String message) {
        if (plugin != null) {
            plugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message));
        }
    }

    public static IPreferenceStore getPreferences() {
        return getDefault().getPreferenceStore();
    }

    /**
     * Get project-aware preferences for a specific project.
     * If project-specific settings are enabled, project preferences take precedence.
     * Otherwise, workspace preferences are used.
     *
     * @param project the project to get preferences for (may be null for workspace prefs)
     * @return ProjectPreferences instance
     */
    public static ProjectPreferences getProjectPreferences(IProject project) {
        return new ProjectPreferences(project);
    }
}
