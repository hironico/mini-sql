package net.hironico.common.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utilities for manipulating dates
 */
public class DateUtils {

    /**
     * Check if two dates represent the same calendar day.
     * Compares only the year, month, and day components, ignoring time.
     * 
     * @param date1 the first date to compare
     * @param date2 the second date to compare
     * @return true if both dates represent the same calendar day, false otherwise or if either date is null
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (!LangUtils.isAllNotNull(date1, date2)) {
            return false;
        }

        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        String str1 = df.format(date1);
        String str2 = df.format(date2);

        return str1.equals(str2);
    }
}
