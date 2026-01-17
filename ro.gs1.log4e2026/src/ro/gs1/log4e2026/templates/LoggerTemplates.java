package ro.gs1.log4e2026.templates;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for logger templates for different logging frameworks.
 */
public class LoggerTemplates {

    public static final String SLF4J = "SLF4J";
    public static final String LOG4J2 = "LOG4J2";
    public static final String JUL = "JUL";

    private static final Map<String, LoggerTemplate> TEMPLATES = new HashMap<>();

    static {
        // SLF4J
        TEMPLATES.put(SLF4J, new LoggerTemplate(
            SLF4J,
            "SLF4J",
            "Logger",
            "LoggerFactory",
            "getLogger",
            "private static final Logger ${logger} = LoggerFactory.getLogger(${enclosing_type}.class);",
            "org.slf4j.Logger"
        ));

        // Log4j 2
        TEMPLATES.put(LOG4J2, new LoggerTemplate(
            LOG4J2,
            "Log4j 2",
            "Logger",
            "LogManager",
            "getLogger",
            "private static final Logger ${logger} = LogManager.getLogger(${enclosing_type}.class);",
            "org.apache.logging.log4j.Logger"
        ));

        // JDK Logging (java.util.logging)
        TEMPLATES.put(JUL, new LoggerTemplate(
            JUL,
            "JDK Logging",
            "Logger",
            "Logger",
            "getLogger",
            "private static final Logger ${logger} = Logger.getLogger(${enclosing_type}.class.getName());",
            "java.util.logging.Logger"
        ));
    }

    public static LoggerTemplate getTemplate(String id) {
        return TEMPLATES.get(id);
    }

    public static LoggerTemplate getSLF4J() {
        return TEMPLATES.get(SLF4J);
    }

    public static LoggerTemplate getLog4j2() {
        return TEMPLATES.get(LOG4J2);
    }

    public static LoggerTemplate getJUL() {
        return TEMPLATES.get(JUL);
    }

    public static String[] getFrameworkIds() {
        return new String[]{SLF4J, LOG4J2, JUL};
    }

    public static String[][] getFrameworkOptions() {
        return new String[][]{
            {"SLF4J", SLF4J},
            {"Log4j 2", LOG4J2},
            {"JDK Logging", JUL}
        };
    }

    private LoggerTemplates() {
    }
}
