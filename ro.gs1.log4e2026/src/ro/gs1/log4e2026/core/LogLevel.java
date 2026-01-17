package ro.gs1.log4e2026.core;

/**
 * Enumeration of supported logging levels.
 * Covers standard levels from SLF4J, Log4j2, and JUL.
 */
public enum LogLevel {

    // Standard levels (SLF4J, Log4j2)
    TRACE("trace", "Trace", 100),
    DEBUG("debug", "Debug", 200),
    INFO("info", "Info", 300),
    WARN("warn", "Warn", 400),
    ERROR("error", "Error", 500),
    FATAL("fatal", "Fatal", 600),

    // JUL-specific levels
    FINEST("finest", "Finest", 50),
    FINER("finer", "Finer", 75);

    private final String methodName;
    private final String displayName;
    private final int priority;

    LogLevel(String methodName, String displayName, int priority) {
        this.methodName = methodName;
        this.displayName = displayName;
        this.priority = priority;
    }

    /**
     * Gets the method name used in logging statements.
     * For example, "debug" for logger.debug(...).
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Gets the display name for UI elements.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the priority (higher = more severe).
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns all standard levels (excluding JUL-specific).
     */
    public static LogLevel[] getStandardLevels() {
        return new LogLevel[] { TRACE, DEBUG, INFO, WARN, ERROR, FATAL };
    }

    /**
     * Returns all JUL levels.
     */
    public static LogLevel[] getJulLevels() {
        return new LogLevel[] { FINEST, FINER, TRACE, DEBUG, INFO, WARN, ERROR };
    }

    /**
     * Returns all levels sorted by priority.
     */
    public static LogLevel[] getAllLevelsByPriority() {
        return new LogLevel[] { FINEST, FINER, TRACE, DEBUG, INFO, WARN, ERROR, FATAL };
    }

    /**
     * Find a LogLevel by its method name.
     * @param name the method name (case-insensitive)
     * @return the LogLevel or null if not found
     */
    public static LogLevel fromMethodName(String name) {
        if (name == null) {
            return null;
        }
        String lower = name.toLowerCase();
        for (LogLevel level : values()) {
            if (level.methodName.equals(lower)) {
                return level;
            }
        }
        return null;
    }

    /**
     * Find a LogLevel by its name constant.
     * @param name the constant name (e.g., "DEBUG", "INFO")
     * @return the LogLevel or null if not found
     */
    public static LogLevel fromName(String name) {
        if (name == null) {
            return null;
        }
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns whether this level is supported by the given framework.
     * @param framework the framework name (SLF4J, Log4j2, JUL)
     */
    public boolean isSupportedBy(String framework) {
        if (framework == null) {
            return false;
        }
        return switch (framework) {
            case "SLF4J" -> this != FATAL && this != FINEST && this != FINER;
            case "Log4j2" -> this != FINEST && this != FINER;
            case "JUL" -> this != FATAL;
            default -> true;
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}
