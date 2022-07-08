package ch.ubp.pms.utils;

public class StringUtils {

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