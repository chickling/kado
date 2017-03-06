package com.chickling.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by gl08 on 2015/11/30.
 */
public class TimeUtil {

    private final static String DATE_FORMAT="yyyy-MM-dd HH:mm:ss";
    public final static String LONG_FORMAT_2 = "yyyy-MM-dd HH:mm";
    public final static String LONG_FORMAT_3 = "yyyy-MM-dd HH:mm:ss.SSS";
    public final static String HDFS_SAVE_FORMAT="yyyy_MM_dd_HH_mm_ss.SSS";

    public final static String LONG_FORMAT = "yyyyMMddHHmmss";
    public final static String SHORT_FORMAT = "yyyyMMdd";
    public final static String SHORT_YEARMONTH_FORMAT = "yyyyMM";
    public final static String SHORT_YEAR_FORMAT = "yyyy";
    public final static String SHORT_MONTH_FORMAT = "MM";
    public final static String SHORT_DAY_FORMAT = "dd";
    public final static String HOUR_FORMAT = "HH";
    public final static String LONG_FORMAT_1 = "yyyy-MM-dd HH:mm:ss";


    public static DateTime String2DateTime(String time){
        if(time.isEmpty()){
            return parseDate(getCurrentTime(),DATE_FORMAT);
        }
        else{
            return DateTimeFormat.forPattern(DATE_FORMAT).parseDateTime(time);
        }
    }

    public static DateTime parseDate(String dateStr,String formater){
        return DateTimeFormat.forPattern(formater).parseDateTime(dateStr);
    }

    public static String formatDateToStr(Date date , String formater){
        if (null==date)return "";
        return new DateTime(date).toString(formater);
    }
    public static String toString(DateTime time){
        return time.toString(DATE_FORMAT);
    }

    public static String getCurrentTime(){
        DateTime dt = new DateTime();
//        return dt.toString("yyyy-MM-dd HH:mm:ss");
        return dt.toString(DATE_FORMAT);

    }

    public static String beforeDate (String period){
        DateTime dt =new DateTime();
        dt=dt.minusDays(Integer.parseInt(period));
        return dt.toString(DATE_FORMAT);
    }
    public static String getSaveHDFSTime(){
        return String.valueOf(new DateTime().getMillis());
//        return new DateTime().toString(HDFS_SAVE_FORMAT);
    }



    public static DateTime getUDFDateTime(String time){
        return DateTimeFormat.forPattern(LONG_FORMAT_2).parseDateTime(time);

    }


    public static String getRunTime(DateTime startTime,DateTime stopTime){

        return Long.toString((stopTime.getMillis()-startTime.getMillis())/1000);
    }



}
