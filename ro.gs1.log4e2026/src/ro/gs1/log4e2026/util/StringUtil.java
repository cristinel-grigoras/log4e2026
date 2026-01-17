package ro.gs1.log4e2026.util;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String utility methods.
 * Ported from de.jayefem.util.Strings
 */
public class StringUtil {

    public static boolean isFormatterChar(String str) {
        if (str == null) {
            return true;
        }
        char[] chars = str.toCharArray();
        for (char ch : chars) {
            if (!isFormatterChar(ch)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isFormatterChar(char ch) {
        return Character.isWhitespace(ch) || isLineDelimiterChar(ch);
    }

    public static boolean isIndentChar(char ch) {
        return Character.isWhitespace(ch) && !isLineDelimiterChar(ch);
    }

    public static boolean isLowerCase(char ch) {
        return Character.toLowerCase(ch) == ch;
    }

    public static boolean isLineDelimiterChar(char ch) {
        return ch == '\n' || ch == '\r';
    }

    public static String removeNewLine(String message) {
        StringBuilder result = new StringBuilder();
        int current = 0;
        int index = message.indexOf('\n', 0);
        while (index != -1) {
            result.append(message.substring(current, index));
            if (current < index && index != 0) {
                result.append(' ');
            }
            current = index + 1;
            index = message.indexOf('\n', current);
        }
        result.append(message.substring(current));
        return result.toString();
    }

    public static boolean containsOnlyWhitespaces(String s) {
        int size = s.length();
        for (int i = 0; i < size; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static int computeIndent(String line, int tabWidth) {
        int result = 0;
        int blanks = 0;
        int size = line.length();
        for (int i = 0; i < size; i++) {
            char c = line.charAt(i);
            if (c == '\t') {
                result++;
                blanks = 0;
            } else if (isIndentChar(c)) {
                if (++blanks == tabWidth) {
                    result++;
                    blanks = 0;
                }
            } else {
                return result;
            }
        }
        return result;
    }

    public static String getIndentString(String line, int tabWidth) {
        int size = line.length();
        int end = 0;
        int blanks = 0;
        for (int i = 0; i < size; i++) {
            char c = line.charAt(i);
            if (c == '\t') {
                end = i + 1;
                blanks = 0;
            } else if (isIndentChar(c)) {
                if (++blanks == tabWidth) {
                    end = i + 1;
                    blanks = 0;
                }
            } else {
                break;
            }
        }
        if (end == 0) {
            return "";
        }
        if (end == size) {
            return line;
        }
        return line.substring(0, end);
    }

    public static List<String> getLines(String lines) {
        if (lines == null) {
            return null;
        }
        List<String> lineList = new LinkedList<>();
        int current = 0;
        int[] newlineInfo = indexOfNewLine(lines, 0);
        int index = newlineInfo[0];
        int length = newlineInfo[1];
        while (index != -1) {
            String line = lines.substring(current, index);
            lineList.add(line);
            current = index + length;
            newlineInfo = indexOfNewLine(lines, current);
            index = newlineInfo[0];
            length = newlineInfo[1];
        }
        if (current <= lines.length()) {
            lineList.add(lines.substring(current));
        }
        return lineList;
    }

    public static int[] indexOfNewLine(String str, int start) {
        int length;
        int index;
        int indexN = str.indexOf('\n', start);
        int indexR = str.indexOf('\r', start);
        int indexMin = Math.min(indexN, indexR);
        int indexMax = Math.max(indexN, indexR);
        if (indexMax - indexMin == 1) {
            index = indexMin;
            length = 2;
        } else if (indexMin >= 0) {
            index = indexMin;
            length = 1;
        } else if (indexMax >= 0) {
            index = indexMax;
            length = 1;
        } else {
            index = -1;
            length = 0;
        }
        return new int[]{index, length};
    }

    public static String chopBeginEnd(String stringPattern, String startEndString) {
        if (BeanUtil.isNull(stringPattern)) {
            return "";
        }
        if (startEndString == null) {
            return stringPattern;
        }
        String replacement = chopBegin(stringPattern, startEndString);
        replacement = chopEnd(replacement, startEndString);
        return replacement;
    }

    public static String chopBegin(String stringPattern, String beginString) {
        if (BeanUtil.isNull(stringPattern)) {
            return "";
        }
        if (beginString == null) {
            return stringPattern;
        }
        Pattern p = Pattern.compile("^" + Pattern.quote(beginString));
        Matcher m = p.matcher(stringPattern);
        return m.replaceAll("");
    }

    public static String chopEnd(String stringPattern, String endString) {
        if (BeanUtil.isNull(stringPattern)) {
            return "";
        }
        if (endString == null) {
            return stringPattern;
        }
        Pattern p = Pattern.compile(Pattern.quote(endString) + "$");
        Matcher m = p.matcher(stringPattern);
        return m.replaceAll("");
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
