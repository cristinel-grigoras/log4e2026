package ro.gs1.log4e2026.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ro.gs1.log4e2026.util.BeanUtil;

/**
 * Unit tests for BeanUtil class.
 */
public class BeanUtilTest {

    @Test
    public void testIsNull_Null() {
        assertTrue(BeanUtil.isNull(null));
    }

    @Test
    public void testIsNull_EmptyString() {
        assertTrue(BeanUtil.isNull(""));
    }

    @Test
    public void testIsNull_NonEmptyString() {
        assertFalse(BeanUtil.isNull("test"));
    }

    @Test
    public void testIsNull_EmptyList() {
        assertTrue(BeanUtil.isNull(new ArrayList<>()));
    }

    @Test
    public void testIsNull_NonEmptyList() {
        assertFalse(BeanUtil.isNull(Arrays.asList("a", "b")));
    }

    @Test
    public void testIsNull_EmptyMap() {
        assertTrue(BeanUtil.isNull(new HashMap<>()));
    }

    @Test
    public void testIsNull_NonEmptyMap() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        assertFalse(BeanUtil.isNull(map));
    }

    @Test
    public void testIsNull_EmptyArray() {
        assertTrue(BeanUtil.isNull(new Object[0]));
    }

    @Test
    public void testIsNull_NonEmptyArray() {
        assertFalse(BeanUtil.isNull(new Object[]{"a"}));
    }

    @Test
    public void testIsNotNull_Null() {
        assertFalse(BeanUtil.isNotNull(null));
    }

    @Test
    public void testIsNotNull_NonEmptyString() {
        assertTrue(BeanUtil.isNotNull("test"));
    }

    @Test
    public void testIsAsciiText_Valid() {
        assertEquals(-1, BeanUtil.isAsciiText("validText123"));
        assertEquals(-1, BeanUtil.isAsciiText("VALID_TEXT"));
    }

    @Test
    public void testIsAsciiText_Invalid() {
        assertTrue(BeanUtil.isAsciiText("invalid text") >= 0);
        assertTrue(BeanUtil.isAsciiText("test@example") >= 0);
    }

    @Test
    public void testIsAsciiText_Null() {
        assertEquals(-1, BeanUtil.isAsciiText(null));
    }

    @Test
    public void testIsConstantCharacter() {
        assertTrue(BeanUtil.isConstantCharacter("CONSTANT"));
        assertTrue(BeanUtil.isConstantCharacter("CONSTANT_NAME"));
        assertTrue(BeanUtil.isConstantCharacter("CONSTANT123"));
        assertFalse(BeanUtil.isConstantCharacter("notConstant"));
        assertFalse(BeanUtil.isConstantCharacter(""));
    }

    @Test
    public void testFirstElement() {
        List<String> list = Arrays.asList("first", "second");
        assertEquals("first", BeanUtil.firstElement(list));
    }

    @Test
    public void testFirstElement_Null() {
        assertNull(BeanUtil.firstElement(null));
    }

    @Test
    public void testFirstElement_Empty() {
        assertNull(BeanUtil.firstElement(new ArrayList<>()));
    }

    @Test
    public void testContains_List() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertTrue(BeanUtil.contains(list, "a"));
        assertFalse(BeanUtil.contains(list, "d"));
    }

    @Test
    public void testContains_Array() {
        Object[] array = {"a", "b", "c"};
        assertTrue(BeanUtil.contains(array, "a"));
        assertFalse(BeanUtil.contains(array, "d"));
    }

    @Test
    public void testTestEqual() {
        assertTrue(BeanUtil.testEqual(null, null));
        assertTrue(BeanUtil.testEqual("test", "test"));
        assertFalse(BeanUtil.testEqual(null, "test"));
        assertFalse(BeanUtil.testEqual("test", null));
        assertFalse(BeanUtil.testEqual("test1", "test2"));
    }

    @Test
    public void testSplit() {
        String[] result = BeanUtil.split("a,b,c", ",");
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test
    public void testSplit_NoDelimiter() {
        String[] result = BeanUtil.split("abc", ",");
        assertArrayEquals(new String[]{"abc"}, result);
    }

    @Test
    public void testCount() {
        assertEquals(3, BeanUtil.count("a,b,c,d", ","));
        assertEquals(0, BeanUtil.count("abcd", ","));
        assertEquals(3, BeanUtil.count("hello hello hello", "hello"));
    }

    @Test
    public void testGetClassName() {
        assertEquals("String", BeanUtil.getClassName(String.class));
        assertEquals("BeanUtilTest", BeanUtil.getClassName(BeanUtilTest.class));
        assertNull(BeanUtil.getClassName(null));
    }

    @Test
    public void testEquals() {
        assertTrue(BeanUtil.equals(null, null));
        assertTrue(BeanUtil.equals("test", "test"));
        assertFalse(BeanUtil.equals(null, "test"));
        assertFalse(BeanUtil.equals("test", null));
        assertFalse(BeanUtil.equals("test1", "test2"));
    }

    @Test
    public void testConcat() {
        Object[] arr1 = {"a", "b"};
        Object[] arr2 = {"c", "d"};
        Object[] result = BeanUtil.concat(arr1, arr2);
        assertArrayEquals(new Object[]{"a", "b", "c", "d"}, result);
    }

    @Test
    public void testConcat_NullFirst() {
        Object[] arr2 = {"c", "d"};
        Object[] result = BeanUtil.concat(null, arr2);
        assertArrayEquals(arr2, result);
    }

    @Test
    public void testConcat_NullSecond() {
        Object[] arr1 = {"a", "b"};
        Object[] result = BeanUtil.concat(arr1, null);
        assertArrayEquals(arr1, result);
    }

    @Test
    public void testMinus() {
        String[] arr1 = {"a", "b", "c"};
        String[] arr2 = {"b"};
        String[] result = BeanUtil.minus(arr1, arr2);
        assertArrayEquals(new String[]{"a", "c"}, result);
    }
}
