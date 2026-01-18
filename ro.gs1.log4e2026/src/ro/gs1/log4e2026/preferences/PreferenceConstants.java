package ro.gs1.log4e2026.preferences;

/**
 * Preference constants for Log4E plugin.
 */
public class PreferenceConstants {

    // Logging framework - keys match old project and PreferenceKeys
    // P_LOGGING_FRAMEWORK is alias for P_LOGGER_PROFILE (same key)
    public static final String P_LOGGER_PROFILE = "LOGGER_PROFILE";
    public static final String P_LOGGING_FRAMEWORK = P_LOGGER_PROFILE;
    public static final String P_LOGGER_NAME = "LOGGER_NAME";
    public static final String P_LOGGER_STATIC = "STATIC_FLAG";
    public static final String P_LOGGER_FINAL = "FINAL_FLAG";

    // Default values
    public static final String DEFAULT_FRAMEWORK = "SLF4J";
    public static final String DEFAULT_PROFILE = "SLF4J";
    public static final String DEFAULT_LOGGER_NAME = "logger";
    public static final boolean DEFAULT_LOGGER_STATIC = true;
    public static final boolean DEFAULT_LOGGER_FINAL = true;

    // Log levels
    public static final String P_LOG_LEVEL_TRACE = "logLevelTrace";
    public static final String P_LOG_LEVEL_DEBUG = "logLevelDebug";
    public static final String P_LOG_LEVEL_INFO = "logLevelInfo";
    public static final String P_LOG_LEVEL_WARN = "logLevelWarn";
    public static final String P_LOG_LEVEL_ERROR = "logLevelError";

    // Templates
    public static final String P_TEMPLATE_ENTRY = "templateEntry";
    public static final String P_TEMPLATE_EXIT = "templateExit";
    public static final String P_TEMPLATE_CATCH = "templateCatch";

    // Format - keys match old project
    public static final String P_DELIMITER = "FORMAT_DELIMITER";
    public static final String P_INCLUDE_CLASS_NAME = "includeClassName";
    public static final String P_INCLUDE_METHOD_NAME = "includeMethodName";

    // User Interaction - Confirmation/Exchange Dialog
    public static final String P_SHOW_EXCHANGE_DIALOG = "showExchangeDialog";
    public static final boolean DEFAULT_SHOW_EXCHANGE_DIALOG = true;

    // User Interaction - Preview Wizards
    public static final String P_WIZARD_DECLARE_CLASS = "wizardDeclareClass";
    public static final String P_WIZARD_INSERT_METHOD = "wizardInsertMethod";
    public static final String P_WIZARD_INSERT_CLASS = "wizardInsertClass";
    public static final String P_WIZARD_REPLACE_METHOD = "wizardReplaceMethod";
    public static final String P_WIZARD_REPLACE_CLASS = "wizardReplaceClass";

    // Default values for wizards (false = apply directly without preview)
    public static final boolean DEFAULT_WIZARD_ENABLED = false;

    // Default templates
    public static final String DEFAULT_TEMPLATE_ENTRY = "${logger}.debug(\"${enclosing_method}() - start\");";
    public static final String DEFAULT_TEMPLATE_EXIT = "${logger}.debug(\"${enclosing_method}() - end\");";
    public static final String DEFAULT_TEMPLATE_CATCH = "${logger}.error(\"${enclosing_method}() - ${exception}\", ${exception});";
    public static final String DEFAULT_DELIMITER = " - ";

    private PreferenceConstants() {
    }
}
