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
        // SLF4J - needs both Logger and LoggerFactory imports
        TEMPLATES.put(SLF4J, new LoggerTemplate(
            SLF4J,
            "SLF4J",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "getLogger",
            "private static final Logger ${logger} = LoggerFactory.getLogger(${enclosing_type}.class);",
            "org.slf4j.Logger\norg.slf4j.LoggerFactory"
        ));

        // Log4j 2 - needs both Logger and LogManager imports
        TEMPLATES.put(LOG4J2, new LoggerTemplate(
            LOG4J2,
            "Log4j 2",
            "org.apache.logging.log4j.Logger",
            "org.apache.logging.log4j.LogManager",
            "getLogger",
            "private static final Logger ${logger} = LogManager.getLogger(${enclosing_type}.class);",
            "org.apache.logging.log4j.Logger\norg.apache.logging.log4j.LogManager"
        ));

        // JDK Logging (java.util.logging) - only needs Logger import
        TEMPLATES.put(JUL, new LoggerTemplate(
            JUL,
            "JDK Logging",
            "java.util.logging.Logger",
            "java.util.logging.Logger",
            "getLogger",
            "private static final Logger ${logger} = Logger.getLogger(${enclosing_type}.class.getName());",
            "java.util.logging.Logger"
        ));
    }

    public static LoggerTemplate getTemplate(String id) {
        if (id == null) {
            return null;
        }
        LoggerTemplate template = TEMPLATES.get(id);
        if (template == null) {
            template = TEMPLATES.get(id.toUpperCase());
        }
        return template;
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
