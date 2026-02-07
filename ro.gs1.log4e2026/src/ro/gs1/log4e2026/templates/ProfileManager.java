package ro.gs1.log4e2026.templates;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import ro.gs1.log4e2026.Log4e2026Plugin;
import ro.gs1.log4e2026.exceptions.Log4eSystemException;
import ro.gs1.log4e2026.preferences.PreferenceConstants;

/**
 * Singleton manager for logger profiles.
 * Handles loading built-in and user profiles, and saving user profiles.
 */
public class ProfileManager {

    private static final String PROFILES_FILE = "log4e-profiles.xml";
    private static final String BUILTIN_PROFILES_FILE = "log4e-profiles.xml";

    private static ProfileManager instance;
    private static final Object LOCK = new Object();

    private Profiles profiles;
    private final ProfilePersistence persistence = new ProfilePersistence();

    private ProfileManager() {
        initProfiles();
    }

    /**
     * Get the singleton instance.
     */
    public static ProfileManager getInstance() {
        synchronized (LOCK) {
            if (instance == null) {
                instance = new ProfileManager();
            }
        }
        return instance;
    }

    /**
     * Reset the singleton instance (for testing).
     */
    public static void resetInstance() {
        synchronized (LOCK) {
            instance = null;
        }
    }

    /**
     * Initialize profiles by loading built-in and user profiles.
     */
    private void initProfiles() {
        profiles = new Profiles();

        // Load built-in profiles from bundle
        try {
            Profiles builtInProfiles = loadBuiltInProfiles();
            if (builtInProfiles != null) {
                profiles.addProfiles(builtInProfiles);
            } else {
                createDefaultProfiles();
            }
        } catch (Exception e) {
            Log4e2026Plugin.logError("Failed to load built-in profiles", e);
            // Create default profiles programmatically
            createDefaultProfiles();
        }

        // Sort built-in profiles
        profiles.sort();

        // Load user profiles from state location
        try {
            File userProfilesFile = getUserProfilesFile();
            if (userProfilesFile != null && userProfilesFile.exists()) {
                Profiles userProfiles = persistence.readProfiles(userProfilesFile);
                userProfiles.sort();
                profiles.addProfiles(userProfiles);
            }
        } catch (Log4eSystemException e) {
            Log4e2026Plugin.logError("Failed to load user profiles", e);
        }

        // If names were changed during merging, save
        if (profiles.isNameChanged()) {
            storeProfiles();
            profiles.reset();
        }
    }

    /**
     * Load built-in profiles from the plugin bundle.
     */
    private Profiles loadBuiltInProfiles() throws Exception {
        Log4e2026Plugin plugin = Log4e2026Plugin.getDefault();
        if (plugin == null) {
            return null;
        }

        Bundle bundle = plugin.getBundle();
        if (bundle == null) {
            return null;
        }

        URL fileUrl = FileLocator.find(bundle, new Path(BUILTIN_PROFILES_FILE), null);
        if (fileUrl == null) {
            Log4e2026Plugin.log("Built-in profiles file not found in bundle: " + BUILTIN_PROFILES_FILE);
            return null;
        }

        try (InputStream inputStream = fileUrl.openStream()) {
            return persistence.readProfiles(inputStream);
        }
    }

    /**
     * Create default profiles programmatically (fallback if XML not found).
     */
    private void createDefaultProfiles() {
        // SLF4J
        Profile slf4j = new Profile(Profile.ID_BUILTIN, "SLF4J");
        slf4j.put("LOGGER_TYPE", "org.slf4j.Logger");
        slf4j.put("LOGGER_FACTORY", "org.slf4j.LoggerFactory");
        slf4j.put("LOGGER_FACTORY_METHOD", "getLogger");
        slf4j.put("LOGGER_DECLARATION", "private static final Logger ${logger} = LoggerFactory.getLogger(${enclosing_type}.class);");
        slf4j.put("LOGGER_IMPORTS", "org.slf4j.Logger\norg.slf4j.LoggerFactory");
        slf4j.put("LOG_METHOD_TRACE", "trace");
        slf4j.put("LOG_METHOD_DEBUG", "debug");
        slf4j.put("LOG_METHOD_INFO", "info");
        slf4j.put("LOG_METHOD_WARN", "warn");
        slf4j.put("LOG_METHOD_ERROR", "error");
        slf4j.put("LOGGER_TRACE_STATEMENT", "${logger}.trace(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\")");
        slf4j.put("LOGGER_DEBUG_STATEMENT", "${logger}.debug(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\")");
        slf4j.put("LOGGER_INFO_STATEMENT", "${logger}.info(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\")");
        slf4j.put("LOGGER_WARN_STATEMENT", "${logger}.warn(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\")");
        slf4j.put("LOGGER_ERROR_STATEMENT", "${logger}.error(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}${delimiter_msg}exception: \" + ${exception})");
        slf4j.put("LOGGER_IS_TRACE_ENABLED_STATEMENT", "${logger}.isTraceEnabled()");
        slf4j.put("LOGGER_IS_DEBUG_ENABLED_STATEMENT", "${logger}.isDebugEnabled()");
        slf4j.put("LOGGER_IS_INFO_ENABLED_STATEMENT", "${logger}.isInfoEnabled()");
        slf4j.put("LOGGER_IS_WARN_ENABLED_STATEMENT", "${logger}.isWarnEnabled()");
        slf4j.put("LOGGER_IS_ERROR_ENABLED_STATEMENT", "${logger}.isErrorEnabled()");
        slf4j.put("LOGGER_POS_START_STATEMENT", "${logger}.debug(\"${enclosing_method}() - start\")");
        slf4j.put("LOGGER_POS_END_STATEMENT", "${logger}.debug(\"${enclosing_method}() - end\")");
        slf4j.put("LOGGER_POS_CATCH_STATEMENT", "${logger}.error(\"${enclosing_method}() - ${exception}\", ${exception})");
        slf4j.put("LOGGER_POS_IS_START_STATEMENT", "${logger}.isDebugEnabled()");
        slf4j.put("LOGGER_POS_IS_END_STATEMENT", "${logger}.isDebugEnabled()");
        slf4j.put("LOGGER_POS_IS_CATCH_STATEMENT", "${logger}.isErrorEnabled()");
        profiles.addProfile(slf4j);

        // Log4j 2
        Profile log4j2 = new Profile(Profile.ID_BUILTIN, "Log4j 2");
        log4j2.put("LOGGER_TYPE", "org.apache.logging.log4j.Logger");
        log4j2.put("LOGGER_FACTORY", "org.apache.logging.log4j.LogManager");
        log4j2.put("LOGGER_FACTORY_METHOD", "getLogger");
        log4j2.put("LOGGER_DECLARATION", "private static final Logger ${logger} = LogManager.getLogger(${enclosing_type}.class);");
        log4j2.put("LOGGER_IMPORTS", "org.apache.logging.log4j.Logger\norg.apache.logging.log4j.LogManager");
        log4j2.put("LOG_METHOD_TRACE", "trace");
        log4j2.put("LOG_METHOD_DEBUG", "debug");
        log4j2.put("LOG_METHOD_INFO", "info");
        log4j2.put("LOG_METHOD_WARN", "warn");
        log4j2.put("LOG_METHOD_ERROR", "error");
        log4j2.put("LOG_METHOD_FATAL", "fatal");
        log4j2.put("LOGGER_TRACE_STATEMENT", "${logger}.trace(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\")");
        log4j2.put("LOGGER_DEBUG_STATEMENT", "${logger}.debug(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\")");
        log4j2.put("LOGGER_INFO_STATEMENT", "${logger}.info(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\")");
        log4j2.put("LOGGER_WARN_STATEMENT", "${logger}.warn(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\")");
        log4j2.put("LOGGER_ERROR_STATEMENT", "${logger}.error(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}${delimiter_msg}exception: \" + ${exception})");
        log4j2.put("LOGGER_FATAL_STATEMENT", "${logger}.fatal(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}${delimiter_msg}exception: \" + ${exception})");
        log4j2.put("LOGGER_IS_TRACE_ENABLED_STATEMENT", "${logger}.isTraceEnabled()");
        log4j2.put("LOGGER_IS_DEBUG_ENABLED_STATEMENT", "${logger}.isDebugEnabled()");
        log4j2.put("LOGGER_IS_INFO_ENABLED_STATEMENT", "${logger}.isInfoEnabled()");
        log4j2.put("LOGGER_IS_WARN_ENABLED_STATEMENT", "${logger}.isWarnEnabled()");
        log4j2.put("LOGGER_IS_ERROR_ENABLED_STATEMENT", "${logger}.isErrorEnabled()");
        log4j2.put("LOGGER_IS_FATAL_ENABLED_STATEMENT", "${logger}.isFatalEnabled()");
        log4j2.put("LOGGER_POS_START_STATEMENT", "${logger}.debug(\"${enclosing_method}() - start\")");
        log4j2.put("LOGGER_POS_END_STATEMENT", "${logger}.debug(\"${enclosing_method}() - end\")");
        log4j2.put("LOGGER_POS_CATCH_STATEMENT", "${logger}.error(\"${enclosing_method}() - ${exception}\", ${exception})");
        log4j2.put("LOGGER_POS_IS_START_STATEMENT", "${logger}.isDebugEnabled()");
        log4j2.put("LOGGER_POS_IS_END_STATEMENT", "${logger}.isDebugEnabled()");
        log4j2.put("LOGGER_POS_IS_CATCH_STATEMENT", "${logger}.isErrorEnabled()");
        profiles.addProfile(log4j2);

        // JDK Logging
        Profile jul = new Profile(Profile.ID_BUILTIN, "JDK Logging");
        jul.put("LOGGER_TYPE", "java.util.logging.Logger");
        jul.put("LOGGER_FACTORY", "java.util.logging.Logger");
        jul.put("LOGGER_FACTORY_METHOD", "getLogger");
        jul.put("LOGGER_DECLARATION", "private static final Logger ${logger} = Logger.getLogger(${enclosing_type}.class.getName());");
        jul.put("LOGGER_IMPORTS", "java.util.logging.Logger\njava.util.logging.Level");
        jul.put("LOG_METHOD_FINEST", "finest");
        jul.put("LOG_METHOD_FINER", "finer");
        jul.put("LOG_METHOD_FINE", "fine");
        jul.put("LOG_METHOD_TRACE", "fine");
        jul.put("LOG_METHOD_DEBUG", "config");
        jul.put("LOG_METHOD_INFO", "info");
        jul.put("LOG_METHOD_WARN", "warning");
        jul.put("LOG_METHOD_WARNING", "warning");
        jul.put("LOG_METHOD_ERROR", "severe");
        jul.put("LOG_METHOD_SEVERE", "severe");
        jul.put("LOG_METHOD_FATAL", "severe");
        jul.put("LOGGER_FINEST_STATEMENT", "${logger}.finest(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\")");
        jul.put("LOGGER_FINER_STATEMENT", "${logger}.finer(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\")");
        jul.put("LOGGER_TRACE_STATEMENT", "${logger}.fine(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\")");
        jul.put("LOGGER_DEBUG_STATEMENT", "${logger}.config(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\")");
        jul.put("LOGGER_INFO_STATEMENT", "${logger}.info(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\")");
        jul.put("LOGGER_WARN_STATEMENT", "${logger}.warning(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\")");
        jul.put("LOGGER_ERROR_STATEMENT", "${logger}.severe(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}${delimiter_msg}exception: \" + ${exception})");
        jul.put("LOGGER_IS_FINEST_ENABLED_STATEMENT", "${logger}.isLoggable(Level.FINEST)");
        jul.put("LOGGER_IS_FINER_ENABLED_STATEMENT", "${logger}.isLoggable(Level.FINER)");
        jul.put("LOGGER_IS_TRACE_ENABLED_STATEMENT", "${logger}.isLoggable(Level.FINE)");
        jul.put("LOGGER_IS_DEBUG_ENABLED_STATEMENT", "${logger}.isLoggable(Level.CONFIG)");
        jul.put("LOGGER_IS_INFO_ENABLED_STATEMENT", "${logger}.isLoggable(Level.INFO)");
        jul.put("LOGGER_IS_WARN_ENABLED_STATEMENT", "${logger}.isLoggable(Level.WARNING)");
        jul.put("LOGGER_IS_ERROR_ENABLED_STATEMENT", "${logger}.isLoggable(Level.SEVERE)");
        jul.put("LOGGER_POS_START_STATEMENT", "${logger}.entering(\"${enclosing_type}\", \"${enclosing_method}\")");
        jul.put("LOGGER_POS_END_STATEMENT", "${logger}.exiting(\"${enclosing_type}\", \"${enclosing_method}\")");
        jul.put("LOGGER_POS_CATCH_STATEMENT", "${logger}.throwing(\"${enclosing_type}\", \"${enclosing_method}\", ${exception})");
        jul.put("LOGGER_POS_IS_START_STATEMENT", "${logger}.isLoggable(Level.FINER)");
        jul.put("LOGGER_POS_IS_END_STATEMENT", "${logger}.isLoggable(Level.FINER)");
        jul.put("LOGGER_POS_IS_CATCH_STATEMENT", "${logger}.isLoggable(Level.FINER)");
        profiles.addProfile(jul);
    }

    /**
     * Store user profiles to file.
     */
    public boolean storeProfiles() {
        try {
            File userProfilesFile = getUserProfilesFile();
            if (userProfilesFile != null) {
                persistence.writeProfiles(profiles, userProfilesFile);
                return true;
            }
        } catch (Log4eSystemException e) {
            Log4e2026Plugin.logError("Failed to store profiles", e);
        }
        return false;
    }

    /**
     * Get all profiles.
     */
    public Profiles getProfiles() {
        return profiles;
    }

    /**
     * Reload profiles from disk.
     */
    public void reloadProfiles() {
        initProfiles();
    }

    /**
     * Get the currently selected profile based on preferences.
     */
    public Profile getCurrentProfile() {
        String profileName = Log4e2026Plugin.getPreferences()
                .getString(PreferenceConstants.P_LOGGER_PROFILE);
        return getProfile(profileName);
    }

    /**
     * Get a profile by name.
     */
    public Profile getProfile(String name) {
        Profile profile = profiles.getProfile(name);
        if (profile != null) {
            return profile;
        }
        // Try by title
        profile = profiles.getProfileByTitle(name);
        if (profile != null) {
            return profile;
        }
        // Fallback to first profile
        return profiles.getFirstProfile();
    }

    /**
     * Check if a profile with the given name exists.
     */
    public boolean existsProfile(String name) {
        return profiles.existsProfile(name);
    }

    /**
     * Check if a profile with the given title exists.
     */
    public boolean existsProfileWithTitle(String title) {
        return profiles.existsProfileWithTitle(title);
    }

    /**
     * Get the first profile.
     */
    public Profile getFirstProfile() {
        return profiles.getFirstProfile();
    }

    /**
     * Get the default profile.
     */
    public Profile getDefaultProfile() {
        return profiles.getDefaultProfile();
    }

    /**
     * Add a new user profile.
     */
    public void addProfile(Profile profile) {
        if (profile.getId() == null) {
            profile.setId(Profile.ID_USER);
        }
        profiles.addProfile(profile);
        storeProfiles();
    }

    /**
     * Remove a profile (only user profiles can be removed).
     */
    public boolean removeProfile(Profile profile) {
        if (profile == null || profile.isBuiltIn()) {
            return false;
        }
        profiles.remove(profile);
        storeProfiles();
        return true;
    }

    /**
     * Duplicate a profile.
     */
    public Profile duplicateProfile(Profile source, String newTitle) {
        Profile duplicate = source.getClone(Profile.ID_USER, newTitle);
        profiles.addProfile(duplicate);
        storeProfiles();
        return duplicate;
    }

    /**
     * Rename a profile (only user profiles can be renamed).
     */
    public boolean renameProfile(Profile profile, String newTitle) {
        if (profile == null || profile.isBuiltIn()) {
            return false;
        }
        if (profiles.existsProfileWithTitle(newTitle)) {
            return false;
        }
        profile.setTitle(newTitle);
        storeProfiles();
        return true;
    }

    /**
     * Get the user profiles file in the plugin state location.
     */
    private File getUserProfilesFile() {
        Log4e2026Plugin plugin = Log4e2026Plugin.getDefault();
        if (plugin == null) {
            return null;
        }
        IPath stateLocation = plugin.getStateLocation();
        if (stateLocation == null) {
            return null;
        }
        return stateLocation.append(PROFILES_FILE).toFile();
    }
}
