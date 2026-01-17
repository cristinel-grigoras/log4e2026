package ro.gs1.log4e2026.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import ro.gs1.log4e2026.templates.LoggerTemplate;
import ro.gs1.log4e2026.templates.LoggerTemplates;

/**
 * Unit tests for LoggerTemplates class.
 */
public class LoggerTemplatesTest {

    @Test
    public void testGetSLF4J() {
        LoggerTemplate template = LoggerTemplates.getSLF4J();
        assertNotNull(template);
        assertEquals("SLF4J", template.getId());
        assertEquals("Logger", template.getLoggerClass());
        assertEquals("LoggerFactory", template.getFactoryClass());
        assertEquals("getLogger", template.getFactoryMethod());
        assertTrue(template.getImportStatement().contains("org.slf4j"));
    }

    @Test
    public void testGetLog4j2() {
        LoggerTemplate template = LoggerTemplates.getLog4j2();
        assertNotNull(template);
        assertEquals("LOG4J2", template.getId());
        assertEquals("Logger", template.getLoggerClass());
        assertEquals("LogManager", template.getFactoryClass());
        assertEquals("getLogger", template.getFactoryMethod());
        assertTrue(template.getImportStatement().contains("org.apache.logging.log4j"));
    }

    @Test
    public void testGetJUL() {
        LoggerTemplate template = LoggerTemplates.getJUL();
        assertNotNull(template);
        assertEquals("JUL", template.getId());
        assertEquals("Logger", template.getLoggerClass());
        assertEquals("Logger", template.getFactoryClass());
        assertEquals("getLogger", template.getFactoryMethod());
        assertTrue(template.getImportStatement().contains("java.util.logging"));
    }

    @Test
    public void testGetTemplate() {
        assertNotNull(LoggerTemplates.getTemplate(LoggerTemplates.SLF4J));
        assertNotNull(LoggerTemplates.getTemplate(LoggerTemplates.LOG4J2));
        assertNotNull(LoggerTemplates.getTemplate(LoggerTemplates.JUL));
        assertNull(LoggerTemplates.getTemplate("UNKNOWN"));
    }

    @Test
    public void testGetFrameworkIds() {
        String[] ids = LoggerTemplates.getFrameworkIds();
        assertNotNull(ids);
        assertEquals(3, ids.length);
    }

    @Test
    public void testGetFrameworkOptions() {
        String[][] options = LoggerTemplates.getFrameworkOptions();
        assertNotNull(options);
        assertEquals(3, options.length);
        assertEquals(2, options[0].length);
    }

    @Test
    public void testSLF4JDeclaration() {
        LoggerTemplate template = LoggerTemplates.getSLF4J();
        String declaration = template.getDeclaration();
        assertTrue(declaration.contains("LoggerFactory.getLogger"));
        assertTrue(declaration.contains("${enclosing_type}"));
        assertTrue(declaration.contains("${logger}"));
    }

    @Test
    public void testLog4j2Declaration() {
        LoggerTemplate template = LoggerTemplates.getLog4j2();
        String declaration = template.getDeclaration();
        assertTrue(declaration.contains("LogManager.getLogger"));
    }

    @Test
    public void testJULDeclaration() {
        LoggerTemplate template = LoggerTemplates.getJUL();
        String declaration = template.getDeclaration();
        assertTrue(declaration.contains("Logger.getLogger"));
        assertTrue(declaration.contains(".class.getName()"));
    }

    @Test
    public void testDeclarationSubstitution() {
        LoggerTemplate template = LoggerTemplates.getSLF4J();
        String declaration = template.getDeclaration()
                .replace("${enclosing_type}", "MyClass")
                .replace("${logger}", "logger");
        assertTrue(declaration.contains("MyClass.class"));
        assertTrue(declaration.contains("logger ="));
        assertFalse(declaration.contains("${"));
    }
}
