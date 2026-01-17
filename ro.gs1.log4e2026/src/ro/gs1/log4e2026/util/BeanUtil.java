package ro.gs1.log4e2026.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ro.gs1.log4e2026.exceptions.Log4eSystemException;

/**
 * Bean utility methods.
 * Ported from de.jayefem.util.BeanUtilities
 */
public class BeanUtil {

    public static boolean isNotNull(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof String) {
            return !((String) obj).isEmpty();
        }
        if (obj instanceof StringBuilder) {
            return ((StringBuilder) obj).length() > 0;
        }
        if (obj instanceof StringBuffer) {
            return ((StringBuffer) obj).length() > 0;
        }
        if (obj instanceof Collection) {
            return !((Collection<?>) obj).isEmpty();
        }
        if (obj instanceof Map) {
            return !((Map<?, ?>) obj).isEmpty();
        }
        if (obj instanceof Object[]) {
            return ((Object[]) obj).length > 0;
        }
        if (obj instanceof int[]) {
            return ((int[]) obj).length > 0;
        }
        if (obj instanceof long[]) {
            return ((long[]) obj).length > 0;
        }
        if (obj instanceof float[]) {
            return ((float[]) obj).length > 0;
        }
        if (obj instanceof double[]) {
            return ((double[]) obj).length > 0;
        }
        if (obj instanceof byte[]) {
            return ((byte[]) obj).length > 0;
        }
        if (obj instanceof char[]) {
            return ((char[]) obj).length > 0;
        }
        if (obj instanceof boolean[]) {
            return ((boolean[]) obj).length > 0;
        }
        return true;
    }

    public static boolean isNull(Object obj) {
        return !isNotNull(obj);
    }

    public static int isAsciiText(String str) {
        if (str == null) {
            return -1;
        }
        char[] strCharArray = str.toCharArray();
        for (int i = 0; i < strCharArray.length; i++) {
            char c = strCharArray[i];
            if (c != '_' && !Character.isDigit(c) && !Character.isUpperCase(c) && !Character.isLowerCase(c)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isConstantCharacter(String str) {
        if (isNull(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (!Character.isUpperCase(ch) && ch != '_' && !Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }

    public static <T> T firstElement(List<T> list) {
        if (isNull(list)) {
            return null;
        }
        return list.get(0);
    }

    public static <T> void append(List<T> list, T obj) {
        if (list == null) {
            return;
        }
        list.add(obj);
    }

    public static boolean contains(List<?> list, Object obj) {
        if (isNull(list) || isNull(obj)) {
            return false;
        }
        return list.contains(obj);
    }

    public static <T> List<T> prependAll(List<T> sourceList, List<T> targetList) {
        List<T> resultList = new LinkedList<>();
        if (sourceList != null) {
            resultList.addAll(sourceList);
        }
        if (targetList != null) {
            resultList.addAll(targetList);
        }
        return resultList;
    }

    public static boolean contains(Object[] objArray, Object obj) {
        if (isNull(objArray) || isNull(obj)) {
            return false;
        }
        for (Object element : objArray) {
            if (obj.equals(element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean testEqual(Object obj1, Object obj2) {
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        return obj1.equals(obj2);
    }

    public static String[] minus(String[] array1, String[] array2) {
        List<Object> minusList = minusAsList(array1, array2);
        return minusList.toArray(new String[0]);
    }

    public static Object[] minus(Object[] array1, Object[] array2) {
        List<Object> minusList = minusAsList(array1, array2);
        return minusList.toArray(new Object[0]);
    }

    public static List<Object> minusAsList(Object[] array1, Object[] array2) {
        if (isNull(array1) && isNull(array2)) {
            return new LinkedList<>();
        }
        if (isNull(array1)) {
            return new LinkedList<>(Arrays.asList(array2));
        }
        if (isNull(array2)) {
            return new LinkedList<>(Arrays.asList(array1));
        }
        List<Object> minusList = new LinkedList<>();
        for (Object object1 : array1) {
            if (!contains(array2, object1)) {
                minusList.add(object1);
            }
        }
        return minusList;
    }

    public static Object[] concat(Object[] array1, Object[] array2) {
        if (isNull(array1) && isNull(array2)) {
            return null;
        }
        if (isNull(array1)) {
            return array2;
        }
        if (isNull(array2)) {
            return array1;
        }
        Object[] concatArray = new Object[array1.length + array2.length];
        System.arraycopy(array1, 0, concatArray, 0, array1.length);
        System.arraycopy(array2, 0, concatArray, array1.length, array2.length);
        return concatArray;
    }

    public static String[] split(String str, String delim) {
        if (str == null || isNull(delim)) {
            return new String[0];
        }
        List<String> tokenList = new LinkedList<>();
        int beginIndex = 0;
        int index;
        while ((index = str.indexOf(delim, beginIndex)) >= 0) {
            if (beginIndex < str.length()) {
                tokenList.add(str.substring(beginIndex, index));
            }
            beginIndex = index + delim.length();
        }
        if (beginIndex <= str.length()) {
            tokenList.add(str.substring(beginIndex));
        }
        return tokenList.toArray(new String[0]);
    }

    public static int count(String str, String substring) {
        if (str == null || substring == null) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(substring, index)) >= 0) {
            count++;
            index += substring.length();
        }
        return count;
    }

    public static String getClassName(Class<?> klass) {
        if (klass == null) {
            return null;
        }
        String name = klass.getName();
        int index = name.lastIndexOf('.');
        if (index + 1 < name.length()) {
            name = name.substring(index + 1);
        }
        return name;
    }

    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T obj) throws Log4eSystemException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new Log4eSystemException(e);
        }
    }

    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        return obj1.equals(obj2);
    }
}
