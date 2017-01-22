package com.baozi.picktimeview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class SimpleDateFormatUtils {

    public static final String DATE_PARTEN = "yyyy年MM月dd日E";
    public static final String DATE_PARTEN2 = "yyyy-MM-dd HH:mm";

    /**
     * 线程安全转换 String -> Date
     */
    public static Date safeParseDate(String dateStr) {
        return safeParseDate(dateStr, DATE_PARTEN2);
    }

    /**
     * 线程安全转换 String -> Date
     */
    public static Date safeParseDate(String dateStr, String pattern) {
        try {
            getFormat2().applyPattern(pattern);
            return getFormat2().parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 线程安全格式化 Date -> String
     */
    public static String safeFormatDate(Date date) {
        return safeFormatDate(date, DATE_PARTEN);
    }

    /**
     * 线程安全格式化 Date -> String
     */
    public static String safeFormatDate(Date date, String pattern) {
        getFormat().applyPattern(pattern);
        return getFormat().format(date);
    }

    /**
     * 借助ThreadLocal完成对每个线程第一次调用时初始化SimpleDateFormat对象
     */
    private static ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<SimpleDateFormat>() {
        protected synchronized SimpleDateFormat initialValue() {
            return new SimpleDateFormat();
        }
    };
    /**
     * 借助ThreadLocal完成对每个线程第一次调用时初始化SimpleDateFormat对象
     */
    private static ThreadLocal<SimpleDateFormat> threadLocal2 = new ThreadLocal<SimpleDateFormat>() {
        protected synchronized SimpleDateFormat initialValue() {
            return new SimpleDateFormat();
        }
    };

    /**
     * 获取当前线程中的安全SimpleDateFormat对象
     */
    private static SimpleDateFormat getFormat() {
        return threadLocal.get();
    }

    /**
     * 获取当前线程中的安全SimpleDateFormat对象
     */
    private static SimpleDateFormat getFormat2() {
        return threadLocal2.get();
    }

}  