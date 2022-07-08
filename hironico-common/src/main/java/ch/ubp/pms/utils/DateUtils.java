package ch.ubp.pms.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Utilities for manipulating dates
 */
public class DateUtils {

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
