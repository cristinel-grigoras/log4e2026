package ro.gs1.log4e2026.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import ro.gs1.log4e2026.util.StringUtil;

/**
 * Unit tests for StringUtil class.
 */
public class StringUtilTest {

    @Test
    public void testIsEmpty() {
        assertTrue(StringUtil.isEmpty(null));
        assertTrue(StringUtil.isEmpty(""));
        assertFalse(StringUtil.isEmpty(" "));
        assertFalse(StringUtil.isEmpty("test"));
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse(StringUtil.isNotEmpty(null));
        assertFalse(StringUtil.isNotEmpty(""));
        assertTrue(StringUtil.isNotEmpty(" "));
        assertTrue(StringUtil.isNotEmpty("test"));
    }

    @Test
    public void testIsBlank() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertTrue(StringUtil.isBlank(" "));
        assertTrue(StringUtil.isBlank("   "));
        assertFalse(StringUtil.isBlank("test"));
    }

    @Test
    public void testIsNotBlank() {
        assertFalse(StringUtil.isNotBlank(null));
        assertFalse(StringUtil.isNotBlank(""));
        assertFalse(StringUtil.isNotBlank(" "));
        assertTrue(StringUtil.isNotBlank("test"));
    }

    @Test
    public void testIsLineDelimiterChar() {
        assertTrue(StringUtil.isLineDelimiterChar('\n'));
        assertTrue(StringUtil.isLineDelimiterChar('\r'));
        assertFalse(StringUtil.isLineDelimiterChar(' '));
        assertFalse(StringUtil.isLineDelimiterChar('a'));
    }

    @Test
    public void testIsIndentChar() {
        assertTrue(StringUtil.isIndentChar(' '));
        assertTrue(StringUtil.isIndentChar('\t'));
        assertFalse(StringUtil.isIndentChar('\n'));
        assertFalse(StringUtil.isIndentChar('a'));
    }

    @Test
    public void testRemoveNewLine() {
        assertEquals("hello world", StringUtil.removeNewLine("hello\nworld"));
        assertEquals("hello world", StringUtil.removeNewLine("hello\n\nworld"));
        assertEquals("test", StringUtil.removeNewLine("test"));
    }

    @Test
    public void testContainsOnlyWhitespaces() {
        assertTrue(StringUtil.containsOnlyWhitespaces(""));
        assertTrue(StringUtil.containsOnlyWhitespaces("   "));
        assertTrue(StringUtil.containsOnlyWhitespaces("\t\n"));
        assertFalse(StringUtil.containsOnlyWhitespaces("a"));
        assertFalse(StringUtil.containsOnlyWhitespaces(" a "));
    }

    @Test
    public void testGetLines() {
        List<String> lines = StringUtil.getLines("line1\nline2\nline3");
        assertNotNull(lines);
        assertEquals(3, lines.size());
        assertEquals("line1", lines.get(0));
        assertEquals("line2", lines.get(1));
        assertEquals("line3", lines.get(2));
    }

    @Test
    public void testGetLinesNull() {
        assertNull(StringUtil.getLines(null));
    }

    @Test
    public void testChopBegin() {
        assertEquals("world", StringUtil.chopBegin("hello world", "hello "));
        assertEquals("hello world", StringUtil.chopBegin("hello world", "xyz"));
        assertEquals("", StringUtil.chopBegin(null, "test"));
    }

    @Test
    public void testChopEnd() {
        assertEquals("hello", StringUtil.chopEnd("hello world", " world"));
        assertEquals("hello world", StringUtil.chopEnd("hello world", "xyz"));
        assertEquals("", StringUtil.chopEnd(null, "test"));
    }

    @Test
    public void testChopBeginEnd() {
        assertEquals("test", StringUtil.chopBeginEnd("\"test\"", "\""));
        assertEquals("test", StringUtil.chopBeginEnd("test", null));
    }

    @Test
    public void testComputeIndent() {
        assertEquals(0, StringUtil.computeIndent("test", 4));
        assertEquals(1, StringUtil.computeIndent("\ttest", 4));
        assertEquals(1, StringUtil.computeIndent("    test", 4));
        assertEquals(2, StringUtil.computeIndent("\t\ttest", 4));
    }

    @Test
    public void testGetIndentString() {
        assertEquals("", StringUtil.getIndentString("test", 4));
        assertEquals("\t", StringUtil.getIndentString("\ttest", 4));
        assertEquals("    ", StringUtil.getIndentString("    test", 4));
    }
}
