package ro.gs1.log4e2026.settings;

import org.eclipse.jface.preference.IPreferenceStore;

import ro.gs1.log4e2026.Log4e2026Plugin;

/**
 * Abstract base class for position-specific logging settings.
 * Each position (start, end, catch, etc.) has its own configuration.
 */
public abstract class PositionSettings {

    // Position type constants
    public static final String POSITION_METHOD_OTHER = "POSITION_METHOD_OTHER";
    public static final String POSITION_METHOD_START = "POSITION_METHOD_START";
    public static final String POSITION_METHOD_EXIT = "POSITION_METHOD_EXIT";
    public static final String POSITION_METHOD_CATCH = "POSITION_METHOD_CATCH";
    public static final String POSITION_METHOD_EMPTY_CATCH = "POSITION_METHOD_EMPTY_CATCH";
    public static final String POSITION_METHOD_RETURN = "POSITION_METHOD_RETURN";
    public static final String POSITION_METHOD_FOR = "POSITION_METHOD_FOR";
    public static final String POSITION_METHOD_IF = "POSITION_METHOD_IF";
    public static final String POSITION_METHOD_COMMENT = "POSITION_METHOD_COMMENT";

    // All supported positions
    public static final String[] POSITIONS = {
        POSITION_METHOD_START,
        POSITION_METHOD_EXIT,
        POSITION_METHOD_RETURN,
        POSITION_METHOD_CATCH
    };

    // Position statement keys
    public static final String LOGGER_POS_START_STATEMENT = "LOGGER_POS_START_STATEMENT";
    public static final String LOGGER_POS_END_STATEMENT = "LOGGER_POS_END_STATEMENT";
    public static final String LOGGER_POS_CATCH_STATEMENT = "LOGGER_POS_CATCH_STATEMENT";

    // Setting suffixes
    public static final String SUFFIX_ENABLED = "_ENABLED";
    public static final String SUFFIX_LEVEL = "_LEVEL";
    public static final String SUFFIX_METHOD_INFO = "_METHOD_INFO";
    public static final String SUFFIX_PARAM_INFO = "_PARAM_INFO";
    public static final String SUFFIX_RETURN_INFO = "_RETURN_INFO";
    public static final String SUFFIX_EXCEPTION_INFO = "_EXCEPTION_INFO";

    protected final String keyPrefix;

    protected PositionSettings(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    /**
     * Gets the method position identifier.
     */
    public abstract String getMethodPosition();

    /**
     * Gets the preference key prefix for this position.
     */
    public String getKeyPrefix() {
        return keyPrefix;
    }

    /**
     * Gets a string preference value.
     */
    public String getString(String name) {
        return getPreferenceStore().getString(keyPrefix + name);
    }

    /**
     * Gets an integer preference value.
     */
    public int getInt(String name) {
        return getPreferenceStore().getInt(keyPrefix + name);
    }

    /**
     * Gets a boolean preference value.
     */
    public boolean getBoolean(String name) {
        return getPreferenceStore().getBoolean(keyPrefix + name);
    }

    /**
     * Gets the full preference key for a setting.
     */
    public String getKey(String suffix) {
        return keyPrefix + suffix;
    }

    /**
     * Returns whether logging is enabled for this position.
     */
    public boolean isEnabled() {
        return getBoolean(SUFFIX_ENABLED);
    }

    /**
     * Returns the log level for this position.
     */
    public String getLevel() {
        return getString(SUFFIX_LEVEL);
    }

    /**
     * Returns whether to include method name in log.
     */
    public boolean includeMethodName() {
        return getBoolean(SUFFIX_METHOD_INFO);
    }

    /**
     * Returns whether to include parameters in log.
     */
    public boolean includeParameters() {
        return getBoolean(SUFFIX_PARAM_INFO);
    }

    /**
     * Returns whether to include return value in log.
     */
    public boolean includeReturnValue() {
        return getBoolean(SUFFIX_RETURN_INFO);
    }

    /**
     * Returns whether to include exception info in log.
     */
    public boolean includeException() {
        return getBoolean(SUFFIX_EXCEPTION_INFO);
    }

    /**
     * Gets the preference store.
     */
    protected IPreferenceStore getPreferenceStore() {
        return Log4e2026Plugin.getPreferences();
    }

    /**
     * Factory method to get the appropriate PositionSettings for a method position.
     * @param methodPosition the position identifier
     * @return the corresponding PositionSettings instance
     */
    public static PositionSettings getPositionSettings(String methodPosition) {
        if (POSITION_METHOD_START.equals(methodPosition)) {
            return new PositionStartSettings();
        }
        if (POSITION_METHOD_EXIT.equals(methodPosition)) {
            return new PositionEndSettings();
        }
        if (POSITION_METHOD_RETURN.equals(methodPosition)) {
            return new PositionReturnSettings();
        }
        if (POSITION_METHOD_CATCH.equals(methodPosition)) {
            return new PositionCatchSettings();
        }
        if (POSITION_METHOD_EMPTY_CATCH.equals(methodPosition)) {
            return new PositionCatchSettings(); // Same settings as catch
        }
        // Default to other
        return new PositionOtherSettings();
    }
}
