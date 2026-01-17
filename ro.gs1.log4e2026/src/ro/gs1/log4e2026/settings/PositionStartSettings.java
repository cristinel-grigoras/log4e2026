package ro.gs1.log4e2026.settings;

/**
 * Settings for logging at method entry (start) position.
 */
public class PositionStartSettings extends PositionSettings {

    public static final String PREFIX = "POS_START";

    public PositionStartSettings() {
        super(PREFIX);
    }

    @Override
    public String getMethodPosition() {
        return POSITION_METHOD_START;
    }
}
