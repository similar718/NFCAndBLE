package com.clc.baselibs.utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具
 */
public class TimeUtils {
    public static final String FORMAT_A = "yyyy-MM-dd";
    public static final String FORMAT_B = "yyyy-MM-dd HH:mm";
    public static final String FORMAT_C = "MM-dd";
    public static final String FORMAT_D = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_E = "MM月dd日";
    public static final String FORMAT_F = "yyyy年MM月dd日";
    /**
     * 将字符串按照时间格式转换为Date
     *
     * @param strDate
     * @param format
     * @return
     */
    public static Date strToDate(String strDate, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    /**
     * 将字符串按照时间格式转换为指定时间格式的字符串
     *
     * @param date
     * @param startFormat
     * @param endFormat
     * @return
     */
    public static String stringDateToString(String date, String startFormat, String endFormat) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(startFormat);
            ParsePosition pos = new ParsePosition(0);
            Date strtodate = formatter.parse(date, pos);

            SimpleDateFormat formatter1 = new SimpleDateFormat(endFormat);
            String str = formatter1.format(strtodate);
            return str;
        }catch (Exception e){
            return date;
        }
    }

    /**
     * 将毫秒数按照时间格式转换为字符串
     *
     * @param milliseconds
     * @param format
     * @return
     */
    public static String dateToStr(long milliseconds, String format) {
        if(milliseconds==0){
            return "";
        }
        Date date = new Date(milliseconds);
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String str = formatter.format(date);
        return str;
    }

    public static String updateTime(long time){
        String data = "";
        long l = System.currentTimeMillis() - time;
        if (l > 0 && l < (1000 * 60L)) {
            data = (l / (1000L)) + "秒钟前更新";
        } else if (l > (1000 * 60L) && l < (1000 * 60 * 60L)) {
            data = (l / (1000 * 60L)) + "分钟前更新";
        } else if (l >= (1000 * 60 * 60L) && l < (1000 * 60 * 60 * 24L)) {
            data = (l / (1000 * 60 * 60L)) + "小时前更新";
        } else if (l >= (1000 * 60 * 60 * 24L) && l < (1000 * 60 * 60 * 24 * 30L)) {
            data = (l / (1000 * 60 * 60 * 24L)) + "天前更新";
        } else if (l >= (1000 * 60 * 60 * 24 * 30L) && l < (1000L * 60L * 60L * 24L * 30L * 3L)) {
            data = (l / (1000 * 60 * 60 * 24 * 30L)) + "个月前更新";
        } else {
            data = time + "";
        }
        return data;
    }

    public static String updateTimeSmart(long time){
        String data = "";
        long l = System.currentTimeMillis() - time;
        if (l > 0 && l < (1000 * 60L)) {
            data = "刚刚更新";
        } else if (l > (1000 * 60L) && l < (1000 * 60 * 60L)) {
            data = (l / (1000 * 60L)) + "分钟前更新";
        } else if (l >= (1000 * 60 * 60L) && l < (1000 * 60 * 60 * 24L)) {
            data = (l / (1000 * 60 * 60L)) + "小时前更新";
        } else if (l >= (1000 * 60 * 60 * 24L) && l < (1000 * 60 * 60 * 24 * 30L)) {
            data = (l / (1000 * 60 * 60 * 24L)) + "天前更新";
        } else if (l >= (1000 * 60 * 60 * 24 * 30L) && l < (1000L * 60L * 60L * 24L * 30L * 3L)) {
            data = (l / (1000 * 60 * 60 * 24 * 30L)) + "个月前更新";
        } else {
            data = time + "";
        }
        return data;
    }

    /**
     * 将现在时间按照时间格式转换为字符串
     * @param format
     * @return
     */
    public static String dateToStr(String format) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String str = formatter.format(date);
        return str;
    }
    /**
     * 将现在时间按照时间格式转换为字符串
     * @param time
     * @param format
     * @return
     */
    public static boolean isStrTimeToday(String time,String format) {
        String times=stringDateToString(time,format,"yyyy-MM-dd");
        String todayTime=dateToStr("yyyy-MM-dd");
        if(todayTime.equals(times)){
            return true;
        }
        return false;
    }
    /**
     * 判断是否为昨天(效率比较高)
     * @param day 传入的 时间
     * @param format 传入的 时间格式
     * @return true今天 false不是
     */
    public static boolean isYesterday(String day,String format){
        try {
            Calendar pre = Calendar.getInstance();
            Date predate = new Date(System.currentTimeMillis());
            pre.setTime(predate);

            Calendar cal = Calendar.getInstance();
            Date date = strToDate(day,format);
            cal.setTime(date);

            if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
                int diffDay = cal.get(Calendar.DAY_OF_YEAR) - pre.get(Calendar.DAY_OF_YEAR);

                if (diffDay == -1) {
                    return true;
                }
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 判断是否为前天(效率比较高)
     * @param day 传入的 时间
     * @param format 传入的 时间格式
     * @return true今天 false不是
     */
    public static boolean isBeforeYesterday(String day,String format){
        try {
            Calendar pre = Calendar.getInstance();
            Date predate = new Date(System.currentTimeMillis());
            pre.setTime(predate);

            Calendar cal = Calendar.getInstance();
            Date date = strToDate(day,format);
            cal.setTime(date);

            if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
                int diffDay = cal.get(Calendar.DAY_OF_YEAR) - pre.get(Calendar.DAY_OF_YEAR);

                if (diffDay == -2) {
                    return true;
                }
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }
    /**
     * 根据时间格式format获取现在日期
     * @param format 时间格式
     * @return
     */
    public static Date getNowDateShort(String format) {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String dateString = formatter.format(currentTime);
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(dateString, pos);
        return strtodate;
    }

    public static int getWeek(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if(week_index<0){
            week_index = 0;
        }
        return week_index;
    }

    /**.
     * 获取星期
     * @param dateStr 字符串类型的时间
     * @param format 时间格式
     * @return 0到6 0表示星期天 6表示星期六
     */
    public static int getWeek(String dateStr,String format){
        Date date=strToDate(dateStr,format);
        return getWeek(date);
    }

    /**
     * 将秒数转为时分秒
     * @param second 秒数
     * @return
     */
    public static String change(long second) {
        long h = 0;
        long d = 0;
        long s = 0;
        long temp = second % 3600;
        if (second > 3600) {
            h = second / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60;
                    if (temp % 60 != 0) {
                        s = temp % 60;
                    }
                } else {
                    s = temp;
                }
            }
        } else {
            d = second / 60;
            if (second % 60 != 0) {
                s = second % 60;
            }
        }
        String th = h < 10 ? "0" + h : h + "";
        String td = d < 10 ? "0" + d : d + "";
        String ts = s < 10 ? "0" + s : s + "";
        if (h == 0) {
            return td + ":" + ts + "";
        } else {
            return th + ":" + td + ":" + ts + "";
        }
    }

    /**
     * 时间戳转字符串
     *
     * @param timestamp     时间戳
     * @param isPreciseTime 是否包含时分
     * @return 格式化的日期字符串
     */
    public static String long2Str(long timestamp, boolean isPreciseTime) {
        return long2Str(timestamp, getFormatPattern(isPreciseTime));
    }

    private static String long2Str(long timestamp, String pattern) {
        return new SimpleDateFormat(pattern, Locale.CHINA).format(new Date(timestamp));
    }

    public static int getYear(){
        Calendar cd = Calendar.getInstance();
        return  cd.get(Calendar.YEAR);
    }

    /**
     * 字符串转时间戳
     *
     * @param dateStr       日期字符串
     * @param isPreciseTime 是否包含时分
     * @return 时间戳
     */
    public static long str2Long(String dateStr, boolean isPreciseTime) {
        return str2Long(dateStr, getFormatPattern(isPreciseTime));
    }

    private static long str2Long(String dateStr, String pattern) {
        try {
            return new SimpleDateFormat(pattern, Locale.CHINA).parse(dateStr).getTime();
        } catch (Throwable ignored) {
        }
        return 0;
    }

    private static String getFormatPattern(boolean showSpecificTime) {
        if (showSpecificTime) {
            return FORMAT_B;
        } else {
            return FORMAT_A;
        }
    }
}
