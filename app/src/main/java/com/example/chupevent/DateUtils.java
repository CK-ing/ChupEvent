package com.example.chupevent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static boolean isToday(String date) {
        return isSameDay(date, new Date());
    }

    public static boolean isInThisWeek(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date eventDate = sdf.parse(date);
            Calendar today = Calendar.getInstance();
            Calendar eventCalendar = Calendar.getInstance();
            eventCalendar.setTime(eventDate);

            // Calculate start and end of the current week
            today.set(Calendar.DAY_OF_WEEK, today.getFirstDayOfWeek());
            Date weekStart = today.getTime();
            today.add(Calendar.DAY_OF_WEEK, 6);
            Date weekEnd = today.getTime();

            return eventDate.compareTo(weekStart) >= 0 && eventDate.compareTo(weekEnd) <= 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isInThisMonth(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date eventDate = sdf.parse(date);
            Calendar today = Calendar.getInstance();
            Calendar eventCalendar = Calendar.getInstance();
            eventCalendar.setTime(eventDate);

            return eventCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    eventCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isSameDay(String date, Date targetDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date eventDate = sdf.parse(date);
            return sdf.format(eventDate).equals(sdf.format(targetDate));
        } catch (Exception e) {
            return false;
        }
    }
}
