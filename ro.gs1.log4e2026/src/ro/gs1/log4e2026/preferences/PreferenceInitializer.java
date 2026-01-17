package ro.gs1.log4e2026.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ro.gs1.log4e2026.Log4e2026Plugin;

/**
 * Initializes default preference values for Log4E plugin.
 * Default values are based on SLF4J logging framework.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer implements PreferenceKeys {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Log4e2026Plugin.getDefault().getPreferenceStore();

        // === Scope and Profile Settings ===
        store.setDefault(PREFERENCES_SCOPE, "instance");
        store.setDefault(LOGGER_DEFAULT, "SLF4J");
        store.setDefault(LOGGER_PROFILE, "SLF4J");

        // === Declaration Settings ===
        store.setDefault(AUTOMATIC_IMPORTS, true);
        store.setDefault(TASK_IMPORTS, false);
        store.setDefault(LOGGER_IMPORTS, "org.slf4j.Logger\norg.slf4j.LoggerFactory");
        store.setDefault(AUTOMATIC_DECLARE, true);
        store.setDefault(AUTOMATIC_DECLARE_INNER_CLASSES, false);
        store.setDefault(TASK_DECLARE, false);
        store.setDefault(REAPPLY_DECLARE, false);
        store.setDefault(CREATE_COMMENTS, true);
        store.setDefault(CREATE_COMMENTS_ONELINE, false);
        store.setDefault(LOGGER_COMMENT, "Logger for this class");
        store.setDefault(STATIC_FLAG, true);
        store.setDefault(FINAL_FLAG, true);
        store.setDefault(ACCESS_FLAG, "private");
        store.setDefault(LOGGER_NAME, "logger");
        store.setDefault(LOGGER_TYPE, "Logger");

        // SLF4J logger initializer
        store.setDefault(LOGGER_INITIALIZER,
            "/**\n * Logger for this class\n */\n" +
            "private static final Logger ${logger} = LoggerFactory.getLogger(${enclosing_type}.class)");

        // === Log Level Enabled Flags ===
        store.setDefault(FINEST_ENABLED, false);
        store.setDefault(FINER_ENABLED, false);
        store.setDefault(TRACE_ENABLED, true);
        store.setDefault(DEBUG_ENABLED, true);
        store.setDefault(INFO_ENABLED, true);
        store.setDefault(WARN_ENABLED, true);
        store.setDefault(ERROR_ENABLED, true);
        store.setDefault(FATAL_ENABLED, false);
        store.setDefault(START_ENABLED, true);
        store.setDefault(END_ENABLED, true);
        store.setDefault(CATCH_ENABLED, true);
        store.setDefault(ENABLED_BRACES, true);

        // === Logger Statements (SLF4J templates) ===
        store.setDefault(LOGGER_FINEST_STATEMENT, "");
        store.setDefault(LOGGER_FINER_STATEMENT, "");
        store.setDefault(LOGGER_TRACE_STATEMENT,
            "${logger}.trace(\"${enclosing_method_with_placeholders}${delimiter}${message}${delimiter}${message_user}${delimiter}${variable_placeholders}${delimiter}${return_value_placeholder}\"${enclosing_method_arguments_as_parameters}${variables_as_parameters}${return_value_as_parameter})");
        store.setDefault(LOGGER_DEBUG_STATEMENT,
            "${logger}.debug(\"${enclosing_method_with_placeholders}${delimiter}${message}${delimiter}${message_user}${delimiter}${variable_placeholders}${delimiter}${return_value_placeholder}\"${enclosing_method_arguments_as_parameters}${variables_as_parameters}${return_value_as_parameter})");
        store.setDefault(LOGGER_INFO_STATEMENT,
            "${logger}.info(\"${enclosing_method_with_placeholders}${delimiter}${message}${delimiter}${message_user}${delimiter}${variable_placeholders}${delimiter}${return_value_placeholder}\"${enclosing_method_arguments_as_parameters}${variables_as_parameters}${return_value_as_parameter})");
        store.setDefault(LOGGER_WARN_STATEMENT,
            "${logger}.warn(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\", ${exception})");
        store.setDefault(LOGGER_ERROR_STATEMENT,
            "${logger}.error(\"${enclosing_method}${delimiter}${message}${delimiter}${message_user}${delimiter}${variables}${delimiter}${return_value}\", ${exception})");
        store.setDefault(LOGGER_FATAL_STATEMENT, "");

        // === Is-Enabled Check Statements (SLF4J) ===
        store.setDefault(LOGGER_IS_FINEST_ENABLED_STATEMENT, "");
        store.setDefault(LOGGER_IS_FINER_ENABLED_STATEMENT, "");
        store.setDefault(LOGGER_IS_TRACE_ENABLED_STATEMENT, "${logger}.isTraceEnabled()");
        store.setDefault(LOGGER_IS_DEBUG_ENABLED_STATEMENT, "${logger}.isDebugEnabled()");
        store.setDefault(LOGGER_IS_INFO_ENABLED_STATEMENT, "${logger}.isInfoEnabled()");
        store.setDefault(LOGGER_IS_WARN_ENABLED_STATEMENT, "${logger}.isWarnEnabled()");
        store.setDefault(LOGGER_IS_ERROR_ENABLED_STATEMENT, "${logger}.isErrorEnabled()");
        store.setDefault(LOGGER_IS_FATAL_ENABLED_STATEMENT, "");

        // === Position-Specific Statements (SLF4J doesn't have entry/exit) ===
        store.setDefault(LOGGER_POS_START_STATEMENT, "");
        store.setDefault(LOGGER_POS_END_STATEMENT, "");
        store.setDefault(LOGGER_POS_CATCH_STATEMENT, "");
        store.setDefault(LOGGER_POS_IS_START_STATEMENT, "");
        store.setDefault(LOGGER_POS_IS_END_STATEMENT, "");
        store.setDefault(LOGGER_POS_IS_CATCH_STATEMENT, "");

        // === Exception Handling ===
        store.setDefault(ERROR_EXCEPTION, true);
        store.setDefault(FATAL_EXCEPTION, true);
        store.setDefault(LOGGER_FORMAT_VARIABLE_PLACEHOLDER, "{}");

        // === Format Settings ===
        store.setDefault(FORMAT_DELIMITER, " - ");
        store.setDefault(FORMAT_DELIMITER_MSG, " - ");
        store.setDefault(FORMAT_RETURN_VALUE_NAME, "returnValue");
        store.setDefault(FORMAT_ARGS_EMPTY_NAME, "");
        store.setDefault(FORMAT_PARENTHESIS_OPEN, "(");
        store.setDefault(FORMAT_PARENTHESIS_CLOSE, ")");
        store.setDefault(FORMAT_EQUALS_SYMBOL, "=");
        store.setDefault(FORMAT_DELIMITER_PARAM, ", ");
        store.setDefault(FORMAT_ONE_LINE, true);
        store.setDefault(FORMAT_USE_STRING_GENERATOR, false);
        store.setDefault(FORMAT_STRING_GENERATOR_CLASS, "");
        store.setDefault(FORMAT_STRING_STYLE, "concatenation");
        store.setDefault(FORMAT_STRING_GENERATOR_PARAMETER1, "");

        // === Replace Settings (System.out/err replacement) ===
        store.setDefault(REPLACE_SOUT_LEVEL, LEVEL_DEBUG);
        store.setDefault(REPLACE_SERR_LEVEL, LEVEL_ERROR);
        store.setDefault(REPLACE_SOUT_ENABLED, true);
        store.setDefault(REPLACE_SERR_ENABLED, true);
        store.setDefault(REPLACE_STACK_TRACE_ENABLED, true);
        store.setDefault(REPLACE_STACK_TRACE_STREAM, "System.err");

        // === Miscellaneous Settings ===
        store.setDefault(INNER_CLASSES_ENABLED, false);
        store.setDefault(ADD_NON_NLS, false);
        store.setDefault(ANONYMOUS_CLASS_ENABLED, false);
        store.setDefault(JDK15_ENABLED, true);
        store.setDefault(ECLIPSE_API_VERSION, "2025-12");

        // === Position Settings: START ===
        store.setDefault(POS_START + POS_ATT_DISABLE_ADD_LOGGING, false);
        store.setDefault(POS_START + POS_ATT_LEVEL, LEVEL_DEBUG);
        store.setDefault(POS_START + POS_ATT_MSG, "start");
        store.setDefault(POS_START + POS_ATT_MSG_USER, "");
        store.setDefault(POS_START + POS_ATT_CONFIRM_INSERT, false);
        store.setDefault(POS_START + POS_ATT_CONFIRM_MODIFY, false);
        store.setDefault(POS_START + POS_ATT_SKIP_CONSTRUCTOR, false);
        store.setDefault(POS_START + POS_ATT_SKIP_EMPTY_METHODS, false);
        store.setDefault(POS_START + POS_ATT_SKIP_GETTER, true);
        store.setDefault(POS_START + POS_ATT_SKIP_SETTER, true);
        store.setDefault(POS_START + POS_ATT_SKIP_TO_STRING, true);
        store.setDefault(POS_START + POS_ATT_METHOD_INFO, true);
        store.setDefault(POS_START + POS_ATT_PARAMTYPES, false);
        store.setDefault(POS_START + POS_ATT_PARAMNAMES, true);
        store.setDefault(POS_START + POS_ATT_PARAMVALUES, true);
        store.setDefault(POS_START + POS_ATT_GREEDY, false);

        // === Position Settings: END ===
        store.setDefault(POS_END + POS_ATT_DISABLE_ADD_LOGGING, false);
        store.setDefault(POS_END + POS_ATT_LEVEL, LEVEL_DEBUG);
        store.setDefault(POS_END + POS_ATT_MSG, "end");
        store.setDefault(POS_END + POS_ATT_MSG_USER, "");
        store.setDefault(POS_END + POS_ATT_CONFIRM_INSERT, false);
        store.setDefault(POS_END + POS_ATT_CONFIRM_MODIFY, false);
        store.setDefault(POS_END + POS_ATT_SKIP_CONSTRUCTOR, false);
        store.setDefault(POS_END + POS_ATT_SKIP_EMPTY_METHODS, false);
        store.setDefault(POS_END + POS_ATT_SKIP_GETTER, true);
        store.setDefault(POS_END + POS_ATT_SKIP_SETTER, true);
        store.setDefault(POS_END + POS_ATT_SKIP_TO_STRING, true);
        store.setDefault(POS_END + POS_ATT_RETURN_REPLACEMENT, false);
        store.setDefault(POS_END + POS_ATT_RETURN_VALUE, true);
        store.setDefault(POS_END + POS_ATT_GREEDY, false);

        // === Position Settings: CATCH ===
        store.setDefault(POS_CATCH + POS_ATT_DISABLE_ADD_LOGGING, false);
        store.setDefault(POS_CATCH + POS_ATT_LEVEL, LEVEL_ERROR);
        store.setDefault(POS_CATCH + POS_ATT_MSG, "exception");
        store.setDefault(POS_CATCH + POS_ATT_MSG_USER, "");
        store.setDefault(POS_CATCH + POS_ATT_CONFIRM_INSERT, false);
        store.setDefault(POS_CATCH + POS_ATT_CONFIRM_MODIFY, false);
        store.setDefault(POS_CATCH + POS_ATT_SKIP_SAME_EXCEPTION, true);
        store.setDefault(POS_CATCH + POS_ATT_SKIP_EMPTY_CATCH_BLOCK, false);
        store.setDefault(POS_CATCH + POS_ATT_CATCH_BLOCK_LEVEL, LEVEL_ERROR);
        store.setDefault(POS_CATCH + POS_ATT_CATCH_BLOCK_MSG, "exception");
        store.setDefault(POS_CATCH + POS_ATT_GREEDY, false);

        // === Position Settings: EMPTY_CATCH ===
        store.setDefault(POS_EMPTY_CATCH + POS_ATT_DISABLE_ADD_LOGGING, false);
        store.setDefault(POS_EMPTY_CATCH + POS_ATT_LEVEL, LEVEL_ERROR);
        store.setDefault(POS_EMPTY_CATCH + POS_ATT_MSG, "empty catch block");
        store.setDefault(POS_EMPTY_CATCH + POS_ATT_MSG_USER, "");

        // === Position Settings: OTHER ===
        store.setDefault(POS_OTHER + POS_ATT_LEVEL, LEVEL_DEBUG);
        store.setDefault(POS_OTHER + POS_ATT_MSG, "");
        store.setDefault(POS_OTHER + POS_ATT_MSG_USER, "");

        // === UI Settings ===
        store.setDefault(UI_SUCCESS_DIALOG, true);
        store.setDefault(UI_DECLARE_C_WIZARD, true);
        store.setDefault(UI_DECLARE_CC_WIZARD, true);
        store.setDefault(UI_INSERT_M_WIZARD, true);
        store.setDefault(UI_INSERT_C_WIZARD, true);
        store.setDefault(UI_REPLACE_M_WIZARD, true);
        store.setDefault(UI_REPLACE_C_WIZARD, true);
        store.setDefault(UI_MODIFY_M_WIZARD, true);
        store.setDefault(UI_MODIFY_C_WIZARD, true);
        store.setDefault(UI_REMOVE_M_WIZARD, true);
        store.setDefault(UI_REMOVE_C_WIZARD, true);
        store.setDefault(UI_EXCHANGE_C_WIZARD, true);
        store.setDefault(UI_CONFIRMATION, true);
        store.setDefault(UI_OUTPUT_ENABLED, true);
        store.setDefault(UI_OUTPUT, UI_OPTION_OUTPUT_CONSOLE);
        store.setDefault(UI_CONSOLE_ACTIVATE, true);

        // === Legacy PreferenceConstants support ===
        store.setDefault(PreferenceConstants.P_LOGGING_FRAMEWORK, PreferenceConstants.DEFAULT_FRAMEWORK);
        store.setDefault(PreferenceConstants.P_LOGGER_NAME, PreferenceConstants.DEFAULT_LOGGER_NAME);
        store.setDefault(PreferenceConstants.P_LOGGER_STATIC, PreferenceConstants.DEFAULT_LOGGER_STATIC);
        store.setDefault(PreferenceConstants.P_LOGGER_FINAL, PreferenceConstants.DEFAULT_LOGGER_FINAL);
        store.setDefault(PreferenceConstants.P_TEMPLATE_ENTRY, PreferenceConstants.DEFAULT_TEMPLATE_ENTRY);
        store.setDefault(PreferenceConstants.P_TEMPLATE_EXIT, PreferenceConstants.DEFAULT_TEMPLATE_EXIT);
        store.setDefault(PreferenceConstants.P_TEMPLATE_CATCH, PreferenceConstants.DEFAULT_TEMPLATE_CATCH);
        store.setDefault(PreferenceConstants.P_DELIMITER, PreferenceConstants.DEFAULT_DELIMITER);
        store.setDefault(PreferenceConstants.P_INCLUDE_CLASS_NAME, true);
        store.setDefault(PreferenceConstants.P_INCLUDE_METHOD_NAME, true);
        store.setDefault(PreferenceConstants.P_SHOW_EXCHANGE_DIALOG, PreferenceConstants.DEFAULT_SHOW_EXCHANGE_DIALOG);
        store.setDefault(PreferenceConstants.P_LOG_LEVEL_TRACE, true);
        store.setDefault(PreferenceConstants.P_LOG_LEVEL_DEBUG, true);
        store.setDefault(PreferenceConstants.P_LOG_LEVEL_INFO, true);
        store.setDefault(PreferenceConstants.P_LOG_LEVEL_WARN, true);
        store.setDefault(PreferenceConstants.P_LOG_LEVEL_ERROR, true);
    }
}
