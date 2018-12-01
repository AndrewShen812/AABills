package com.sddy.utils;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
    private static final String PATTERN_TIME = "yyyy-MM-dd HH:mm:ss";
    private static final String PATTERN_DATE = "yyyy-MM-dd";

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_TIME, Locale.getDefault());
        return sdf.format(new Date());
    }
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DATE, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * 获取指定格式的日期字符串
     * @param date 日期
     * @param pattern 日期格式
     * @return 日期字符串
     */
    public static String getDateString(Date date, String pattern) {
        pattern = TextUtils.isEmpty(pattern) ? PATTERN_TIME : pattern;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * 获取默认格式的日期字符串，日期默认格式：yyyy-MM-dd
     * @param date 日期
     * @return 日期字符串
     */
    public static String getDateString(Date date) {
        return getDateString(date, PATTERN_DATE);
    }

    /**
     * 获取指定格式的时间字符串
     * @param timestamp 时间戳
     * @param pattern 时间格式
     * @return 时间字符串
     */
    public static String getTimeString(long timestamp, String pattern) {
        pattern = TextUtils.isEmpty(pattern) ? PATTERN_TIME : pattern;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * 获取默认格式的时间字符串，时间默认格式：yyyy-MM-dd HH:mm:ss
     * @param timestamp 时间戳
     * @return 时间字符串
     */
    public static String getTimeString(long timestamp) {
        return getTimeString(timestamp, PATTERN_TIME);
    }

    public static int getYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static int getMonth() {
        return Calendar.getInstance().get(Calendar.MONTH);
    }

    public static int getDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    public static String getTimeString(Date date, String format) {
        date = date == null ? new Date() : date;
        format = TextUtils.isEmpty(format) ? PATTERN_TIME : format;
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(date);
    }

    private static final int MINUTE_SEC = 60;
    private static final int HOUR_SEC = MINUTE_SEC * 60;
    private static final int DAY_SEC = HOUR_SEC * 24;
    public static String getDurationDesc(int durationSec) {
        if (durationSec < MINUTE_SEC) {
            return String.format(Locale.getDefault(), "00:%02d", durationSec);
        } else if (durationSec < HOUR_SEC) {
            int min = durationSec / MINUTE_SEC;
            int sec = durationSec % MINUTE_SEC;
            return String.format(Locale.getDefault(), "%02d:%02d", min, sec);
        } else if (durationSec < DAY_SEC) {
            int hour = durationSec / HOUR_SEC;
            int min = (durationSec % HOUR_SEC) / MINUTE_SEC;
            if (min == 0) {
                return hour + "小时";
            } else {
                return hour + "小时" + min + "分钟";
            }
        }
        return "";
    }
}
