package com.ravi.android.buddy.ledger.utility;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ravi on 5/2/17.
 */

public class DateUtil {

    public static String getCurrentFormattedDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static Boolean isSameDay(String date1, String date2) {
        if (date1 != null && date2 != null && date1 != "" && date2 != "") {
            for (int i = 0; i <= 9; i++) {
                if (date1.charAt(i) != date2.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static Boolean isSameMonth(String date1, String date2) {
        if (date1 != null && date2 != null && date1 != "" && date2 != "") {
            for (int i = 0; i <= 6; i++) {
                if (date1.charAt(i) != date2.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static String getMonthAndYearOnly(String date) {
        if (date != null && !date.equalsIgnoreCase("")) {
            String[] splittedDate = date.split("-");
            return new DateFormatSymbols().getMonths()[Integer.parseInt(splittedDate[1]) - 1] + " " + splittedDate[0];
        }
        return null;
    }

    public static int compareDates(String d1, String d2) {
        if (d1 != null && d2 != null && !d1.equalsIgnoreCase("") && !d2.equalsIgnoreCase("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                Date date1 = dateFormat.parse(d1);
                Date date2 = dateFormat.parse(d2);
                return date1.compareTo(date2);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    // "yyyy-MM-dd HH:mm:ss" --> "dd-mm-yyyy"
    public static String getFormattedDateOnly(String date) {
        if (date != null && date != "") {
            date = date.substring(0, 10);
            String[] splittedDate = date.split("-");
            return splittedDate[2] + "-" + splittedDate[1] + "-" + splittedDate[0];
        }
        return null;
    }

    //get date "HH:mm"
    public static String getFormattedTimeOnly(String date) {
        if (date != null && date != "") {
            return date.substring(11, 16);
        }
        return null;
    }

    public static Boolean isDateIsOfCurrentMonth(String date1, String date2) {
        if (date1 != null && !date1.equalsIgnoreCase("") && date2 != null && !date2.equalsIgnoreCase("")) {
            for (int i = 0; i <= 6; i++) {
                if (date1.charAt(i) != date2.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
