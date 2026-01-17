package ro.gs1.log4e2026.settings;

/**
 * Settings for logging in catch blocks.
 */
public class PositionCatchSettings extends PositionSettings {

    public static final String PREFIX = "POS_CATCH";

    public PositionCatchSettings() {
        super(PREFIX);
    }

    @Override
    public String getMethodPosition() {
        return POSITION_METHOD_CATCH;
    }

    /**
     * Returns whether to log in empty catch blocks.
     */
    public boolean logInEmptyCatch() {
        return getBoolean("_EMPTY_CATCH");
    }

    /**
     * Returns whether to include the exception variable in the log.
     */
    public boolean includeExceptionVariable() {
        return getBoolean("_EXCEPTION_VAR");
    }

    /**
     * Returns whether to include the stack trace.
     */
    public boolean includeStackTrace() {
        return getBoolean("_STACK_TRACE");
    }
}
