package ro.gs1.log4e2026.settings;

/**
 * Settings for logging at other/custom positions.
 */
public class PositionOtherSettings extends PositionSettings {

    public static final String PREFIX = "POS_OTHER";

    public PositionOtherSettings() {
        super(PREFIX);
    }

    @Override
    public String getMethodPosition() {
        return POSITION_METHOD_OTHER;
    }
}
