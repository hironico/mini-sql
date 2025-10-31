package net.hironico.common.utils;

/**
 * Utility class for String manipulation operations.
 * Provides helper methods for common string operations.
 */
public class StringUtils {

    /**
     * Count the number of occurrences of a specific character in a string.
     * 
     * @param src the source string to search in
     * @param theCar the character to count
     * @return the number of occurrences of the character, or -1 if src is null
     */
    public static int countOccurences(String src, char theCar) {
        if (src == null) {
            return -1;
        }

        // java 8 : long count = someString.chars().filter(ch -> ch == 'e').count();
        return src.length() - src.replaceAll("" + theCar, "").length();
    }

    /**
     * Check for "NULL" or "null" or null values of the given string.
     * @param str the String to test
     * @return true if considered NULL
     */
    public static boolean isNULL(String str) {
        return str == null || "NULL".equalsIgnoreCase(str);
    }
}
