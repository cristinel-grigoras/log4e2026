package ro.gs1.log4e2026.settings;

/**
 * Settings for logging at return statement positions.
 */
public class PositionReturnSettings extends PositionSettings {

    public static final String PREFIX = "POS_RETURN";

    public PositionReturnSettings() {
        super(PREFIX);
    }

    @Override
    public String getMethodPosition() {
        return POSITION_METHOD_RETURN;
    }

    /**
     * Returns whether to log the return value.
     */
    public boolean logReturnValue() {
        return getBoolean("_LOG_VALUE");
    }
}
