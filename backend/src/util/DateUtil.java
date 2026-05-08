package util;

import java.time.LocalDate;

/**
 * DateUtil — simple date helpers used across the application.
 */
public class DateUtil {

    private DateUtil() {}

    /** Returns today's date as a String in YYYY-MM-DD format. */
    public static String today() {
        return LocalDate.now().toString();
    }

    /**
     * Adds {@code days} to a date string (YYYY-MM-DD) and returns
     * the result as a String in the same format.
     * Used for calculating book return dates (borrow + 14 days).
     */
    public static String plusDays(String date, int days) {
        return LocalDate.parse(date).plusDays(days).toString();
    }
}
