package ro.gs1.log4e2026.settings;

/**
 * Settings for logging at method exit (end) position.
 */
public class PositionEndSettings extends PositionSettings {

    public static final String PREFIX = "POS_END";

    public PositionEndSettings() {
        super(PREFIX);
    }

    @Override
    public String getMethodPosition() {
        return POSITION_METHOD_EXIT;
    }
}
