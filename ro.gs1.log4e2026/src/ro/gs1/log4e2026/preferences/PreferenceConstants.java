package ro.gs1.log4e2026.preferences;

/**
 * Preference constants for Log4E plugin.
 */
public class PreferenceConstants {

    // Logging framework
    public static final String P_LOGGING_FRAMEWORK = "loggingFramework";
    public static final String P_LOGGER_PROFILE = "loggerProfile";
    public static final String P_LOGGER_NAME = "loggerName";
    public static final String P_LOGGER_STATIC = "loggerStatic";
    public static final String P_LOGGER_FINAL = "loggerFinal";

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

    // Format
    public static final String P_DELIMITER = "delimiter";
    public static final String P_INCLUDE_CLASS_NAME = "includeClassName";
    public static final String P_INCLUDE_METHOD_NAME = "includeMethodName";

    // User Interaction
    public static final String P_SHOW_EXCHANGE_DIALOG = "showExchangeDialog";
    public static final boolean DEFAULT_SHOW_EXCHANGE_DIALOG = true;

    // Default templates
    public static final String DEFAULT_TEMPLATE_ENTRY = "${logger}.debug(\"${enclosing_method}() - start\");";
    public static final String DEFAULT_TEMPLATE_EXIT = "${logger}.debug(\"${enclosing_method}() - end\");";
    public static final String DEFAULT_TEMPLATE_CATCH = "${logger}.error(\"${enclosing_method}() - ${exception}\", ${exception});";
    public static final String DEFAULT_DELIMITER = " - ";

    private PreferenceConstants() {
    }
}
