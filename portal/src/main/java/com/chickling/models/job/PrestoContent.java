package com.chickling.models.job;

import java.util.regex.Pattern;

/**
 * Created by gl09 on 2015/12/3.
 */
public class PrestoContent {

    public final static Integer QUERY_UI = 0;
    public final static Integer USER_JOB = 1;
    public final static Integer SCHEDULE = 2;

    public final static Integer NO_SAVE = 0;
    public final static Integer HDFS = 1;
    public final static Integer LOCAL = 2;
    public final static Integer DATABASE = 3;

    public final static Integer RUNNING = 0;
    public final static Integer FINISH = 1;
    public final static Integer FAILED = 2;

    public final static Integer GENERAL = 0;
    public final static Integer MANAGER = 1;
    public final static Integer ADMIN = 2;

    public final static Long JOB_STATUS_INTERVAL = 1000L;
    public final static Long JOB_START_WAIT_TIME = 1000L;

    public final static Integer REALTIME_QUERY_LIMIT = 1000;

    public final static Long JOB_IDLE_TIME = 2 * 60 * 60 * 1000L;  // 2HR idle
    public final static Long JOB_RESULT_SIZE = 10 * 1024 * 1024 * 1024L;  //10G


    public final static Pattern SQL_PASER = Pattern.compile("(((delete|DELETE)\\s*(FROM|from))|((((CREATE|create)|(DROP|drop))\\s*((table|TABLE)(\\s*(if\\s*((exists|EXISTS)|((not|NOT)\\s*(exists|EXISTS))))|)))|((insert|INSERT)\\s*(into|INTO))))\\s*([\\w.]+)");
    public final static Pattern LIMIT_CHECK = Pattern.compile("limit\\s+[0-9]+");
    public final static Pattern CONDITION_LAST_HOUR_CONST = Pattern.compile("\\$LAST\\s*([\\d]+)\\s*HOUR\\s*\\$");  //	$last 24 hour$
    public final static Pattern CONDITION_BETWEEN_LAST_CONST = Pattern.compile("\\$BETWEEN\\s*LAST\\s*([[+-]?\\d]+)\\s*AND\\s*([+-]?[\\d]+)\\s*HOUR\\s*\\$");  // $between last 72 and 2 hour$
    public final static Pattern CONDITION_LAST_HOUR_FROM = Pattern.compile("\\$LAST\\s*([\\d]+)\\s*HOUR\\s*FROM\\s*'?([\\d\\-/]+\\s[\\d]+\\:[\\d]+)'?\\s*\\$");  //$last 24 hour from 2012-03-12 10:00$
    public final static Pattern CONDITION_PT = Pattern.compile("\\{([+-]?[a-zA-Z0-9_\\.]+)\\}");
    public final static Pattern SQL_SELECT_PASER = Pattern.compile("[sS][Ee][Ll][Ee][Cc][Tt]");
    public final static Pattern CONDITION_CONST = Pattern.compile("\\$[+-]?[a-zA-Z0-9_\\:\\'\\+\\-\\s]+\\$");
    public final static Pattern CONDITION_CONST_TODAY = Pattern.compile("\\$TODAY(([\\+|\\-]{1,1})([\\d])+){0,1}\\$");
    public final static Pattern CONDITION_CONST_HOUR = Pattern.compile("\\$HOUR(([\\+|\\-]{1,1})([\\d])+){0,1}\\$");
    public final static Pattern CONDITION_CONST_YEAR = Pattern.compile("\\$YEAR(([\\+|\\-]{1,1})([\\d])+){0,1}\\$");
    public final static Pattern CONDITION_CONST_MONTH = Pattern.compile("\\$MONTH(([\\+|\\-]{1,1})([\\d])+){0,1}\\$");
    public final static Pattern CONDITION_CONST_DAY = Pattern.compile("\\$DAY(([\\+|\\-]{1,1})([\\d])+){0,1}\\$");
    public final static Pattern CONDITION_CURRENT_TIMESTAMP = Pattern.compile("\\$CURRENTTIMESTAMP\\$");
    public final static Pattern CONDITION_CONST_YEARMONTH = Pattern.compile("\\$YM(([\\+|\\-]{1,1})([\\d])+){0,1}\\$");
    public final static Pattern CONDITION_CONST_YEARMONTH_DAY = Pattern.compile("\\$DM_DT(([\\+|\\-]{1,1})([\\d])+){0,1}\\$");

    public final static Pattern PARTITION_YEARMONTH_DAY = Pattern.compile("\\$P_DM_DT(([\\+|\\-]{1,1})([\\d])+){0,1}\\$");
    public final static Pattern PARTITION_YEARMONTH = Pattern.compile("\\$P_DM(([\\+|\\-]{1,1})([\\d])+){0,1}\\$");
    public final static Pattern PARTITION_DAY = Pattern.compile("\\$P_DT(([\\+|\\-]{1,1})([\\d])+){0,1}\\$");

    public final static Pattern PATTERN_IP = Pattern.compile("^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$");
    public final static Pattern[] CONDITION_PTS = new Pattern[]{CONDITION_PT, CONDITION_CONST};
}