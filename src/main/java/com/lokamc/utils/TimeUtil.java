package com.lokamc.utils;

import org.bukkit.World;
import org.ocpsoft.prettytime.PrettyTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.bukkit.ChatColor.GREEN;

public class TimeUtil {
    public static final DateFormat fullDateFormat = new SimpleDateFormat("MM/dd/yy H:mm a", Locale.ENGLISH);
    public static final DateFormat monthDayFormat = new SimpleDateFormat("M/d h:mm a", Locale.ENGLISH);
    public static final SimpleDateFormat timeStampFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    public static float secondsSince(Long time) {
        return secondsSince(time, false);
    }

    public static float secondsSince(Long time, boolean zeroMax) {
        if (time == 0) return zeroMax ? Float.MAX_VALUE : 0;

        return MILLISECONDS.toSeconds(currentTimeMillis() - time);
    }

    public static int minutesSince(Long time) {
        return minutesSince(time, false);
    }

    public static int minutesSince(Long time, boolean zeroMax) {
        if (time == 0) return zeroMax ? Integer.MAX_VALUE : 0;

        return (int) MILLISECONDS.toMinutes(currentTimeMillis() - time);
    }

    public static int hoursSince(Long time) {
        return hoursSince(time, false);
    }

    public static int hoursSince(Long time, boolean zeroMax) {
        if (time == 0) return zeroMax ? Integer.MAX_VALUE : 0;

        return (int) MILLISECONDS.toHours(currentTimeMillis() - time);
    }

    public static int daysSince(Long time) {
        if (time == 0) return 0;

        return (int) MILLISECONDS.toDays(currentTimeMillis() - time);
    }

    public static String minutesUntil(Long time, int start) {
        return minutesUntil(time, start, true);
    }

    public static String minutesUntil(Long time, int start, boolean showSecondsOver1Minute) {
        float seconds = Math.max(0, (start * 60) - secondsSince(time));
        if (seconds <= 60) {
            return seconds + "s";
        }

        return showSecondsOver1Minute ? ((seconds / 60) + "m " + (seconds % 60) + "s") : (seconds / 60) + "m";
    }

    public static String getPrettyTimeUntil(long date, int timeType, int amount) {
        Date time = new Date(date);
        PrettyTime pTime = new PrettyTime();
        Calendar c = Calendar.getInstance();
        c.setTime(time);
        c.add(timeType, amount);
        return pTime.format(c);
    }

    public static String getPrettyTimeFrom(long date) {
        Date time = new Date(date);
        PrettyTime pTime = new PrettyTime();
        return pTime.format(time);
    }

    public static boolean isDay(World world) {
        long time = world.getTime();

        return time < 12300 || time > 23850;
    }


    public static String getDateAt(long date, int timeType, int amount) {
        Date time = new Date(date);
        Calendar c = Calendar.getInstance();
        c.setTime(time);
        c.add(timeType, amount);
        return monthDayFormat.format(c.getTime());
    }


    public static String getTimeUntil(long date, int timeType, int amount) {
        Date time = new Date(date);
        Calendar c = Calendar.getInstance();
        c.setTime(time);
        c.add(timeType, amount);
        return getTimeUntil(c.getTimeInMillis());
    }

    public static String getTimeUntil(float time) {
        return getTimeUntil(time, false);
    }

    public static String getTimeUntil(float time, boolean fullWord) {
        float date = time - System.currentTimeMillis();
        if (date <= 0) return "shortly";

        return getTimeFromSeconds(date / 1000, fullWord);
    }

    public static String getTimeFromSeconds(float seconds) {
        return getTimeFromSeconds(seconds, false);
    }

    public static String getTimeFromSeconds(float seconds, boolean fullWord) {
        String time;
        if (seconds < 120) {
            if (seconds <= 60) {
                time = "" + GREEN + seconds + (fullWord ? " " + "second" + (seconds > 1 ? "s" : "") : "s");
            } else {
                seconds = seconds % 60;
                time = "" + GREEN + "1" + (fullWord ? " minutes" : "m " + seconds + "s");
            }
        } else if (seconds < 3600) {
            int minutes = (int) Math.ceil(seconds / 60f);
            time = "" + GREEN + minutes + (fullWord ? " " + "minute" + (minutes > 1 ? "s" : "") : "m");
        } else if (seconds < 86400) {
            int minutes = (int) ((seconds % 3600) / 60);
            int hours = (int) (Math.ceil(seconds / 3600));
            if (hours > 2) {
                time = "" + GREEN + hours + (fullWord ? " hours" : "h");
            } else {
                time = "" + GREEN + hours + (fullWord ? " hour " + minutes + " minutes" : "h " + minutes + "m");
            }
        } else {
            int days = (int) TimeUnit.SECONDS.toDays((long) seconds);
            time = "" + GREEN + days + (fullWord ? " " + "day" + (days > 1 ? "s" : "") : "d");
        }
        return time;
    }

    public static String getMillisPassedFromNanos(long start) {
        return TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS) + "ms";
    }

    public static long getElapsedFromNanos(long start) {
        return TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
    }

    public static long getMillisFromNanos(double nanos) {
        return TimeUnit.MILLISECONDS.convert((long) nanos, TimeUnit.NANOSECONDS);
    }
}