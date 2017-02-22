package com.chickling.models;

import com.google.gson.Gson;
import com.chickling.boot.Init;
import com.chickling.util.TemplateCRUDUtils;
import com.chickling.util.TimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by jw6v on 2015/11/30.
 */
public class MessageFactory {

    public synchronized static String rtnJobInfoMessage(ResultSet rs)throws SQLException{
        Map json=new LinkedHashMap();
        Gson gson = new Gson();
        int jobID=rs.getInt("JobID");
        json.put("jobID", jobID);
        json.put("jobname",rs.getString("JobName"));
        json.put("jobowner",rs.getInt("JobOwner"));
        json.put("jobLevel",rs.getInt("JobLevel"));
        json.put("memo",rs.getString("JobMemo"));
        json.put("notification",rs.getString("Notification"));
        json.put("sql",rs.getString("JobSQL"));
//        json.put("replace_value",rs.getInt("Replace_Value"));
//        json.put("replace_sign",rs.getString("Replace_Sign"));
        json.put("save_type",rs.getInt("JobStorageType"));
        json.put("filepath",rs.getString("FilePath"));
        json.put("filename",rs.getString("FileName"));
        json.put("location_id",rs.getInt("StorageResources"));
        json.put("insertsql",rs.getString("DBSQL"));
        json.put("Report",rs.getBoolean("Report"));
        json.put("ReportEmail",rs.getString("ReportEmail"));
        json.put("ReportLength",rs.getInt("ReportLength"));
        json.put("ReportFileType",rs.getInt("ReportFileType"));
        json.put("ReportTitle",rs.getString("ReportTitle"));
        json.put("ReportWhileEmpty",rs.getBoolean("ReportWhileEmpty"));
        json.put("status","success");

        List<Map> templateInfo=TemplateCRUDUtils.readSqlTemplate(jobID);
        json.put("SQLTemplate",templateInfo);

        return gson.toJson(json);
    }

    public synchronized static String JobListMessage(ResultSet rs,int GID, int UID, String Username )throws SQLException{
        //ToDO
        Map json=new LinkedHashMap();
        Gson gson = new Gson();
        List<Map> JobList=new ArrayList<>();
        while(rs.next()){
            Map jsonList=new LinkedHashMap();
            jsonList.put( "jobid",rs.getInt("JobID"));
            jsonList.put( "jobname",rs.getString("JobName"));
            jsonList.put("jobLevel",rs.getInt("JobLevel"));
            jsonList.put("memo",rs.getString("JobMemo"));
            jsonList.put("type",rs.getString("JobType"));
            String storage=(rs.getInt("JobStorageType")>0)?"True":"False";
            jsonList.put("storage",storage);

            try{
                jsonList.put("last_runtime", rs.getString("JobStartTime"));
                jsonList.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("JobStartTime")),
                        TimeUtil.String2DateTime(rs.getString("JobStopTime"))));}
            catch(NullPointerException npe){
                jsonList.put("last_runtime", "");
                if(rs.getString("JobStartTime")!=null) {
                    jsonList.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("JobStartTime")),
                            TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                }else {
                    jsonList.put("runingtime","0");
                }
            }
            jsonList.put("Report",rs.getBoolean("Report"));
            jsonList.put("ReportEmail",rs.getString("ReportEmail"));
            jsonList.put("ReportLength",rs.getInt("ReportLength"));
            jsonList.put("ReportFileType",rs.getInt("ReportFileType"));
            jsonList.put("ReportTitle",rs.getString("ReportTitle"));
            jsonList.put("ReportWhileEmpty",rs.getBoolean("ReportWhileEmpty"));
            jsonList.put("user",rs.getString("UserName"));
            jsonList.put("userid",rs.getString("UID"));
            jsonList.put("group",GID);
            JobList.add(jsonList);

        }
        json.put("status","success");
        json.put("time",TimeUtil.getCurrentTime());
        json.put("list",JobList);
        return gson.toJson(json);
    }

    public synchronized static String JobStatusListMessage(ResultSet rs,int GID, int UID, String Username )throws SQLException{
        //ToDO
        Map json=new LinkedHashMap();
        Gson gson = new Gson();
        List<Map> JobList=new ArrayList<>();
        while(rs.next()){
            Map jsonList=new LinkedHashMap();
            jsonList.put( "jobrunid",rs.getInt("JHID"));
            jsonList.put( "jobid",rs.getInt("JobID"));
            if(rs.getString("JobName")!=null)
                jsonList.put( "jobname",rs.getString("JobName"));
            else
                jsonList.put( "jobname",rs.getString("PrestoID"));

            jsonList.put("jobLevel",rs.getInt("JobLevel"));
            jsonList.put("memo",rs.getString("JobMemo"));
            jsonList.put("type",rs.getString("JobType"));
            String storage=(rs.getInt("JobStorageType")>0)?"True":"False";
            jsonList.put("storage",storage);
            jsonList.put("job_status",rs.getString("JobStatus"));
            jsonList.put("start_time",rs.getString("JobStartTime"));
            jsonList.put("stop_time",rs.getString("JobStopTime"));
            jsonList.put("progress",rs.getString("JobProgress"));
            try{

                if(!rs.getString("JobStopTime").equals("")) {
                    jsonList.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("JobStartTime")),
                            TimeUtil.String2DateTime(rs.getString("JobStopTime"))));
                }else {
                    jsonList.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("JobStartTime")),
                            TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                }
            }
            catch(NullPointerException npe){
                jsonList.put("runingtime",TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("JobStartTime")),
                        TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
            }
            jsonList.put("Report",rs.getBoolean("Report"));
            jsonList.put("ReportEmail",rs.getString("ReportEmail"));
            jsonList.put("ReportLength",rs.getInt("ReportLength"));
            jsonList.put("ReportFileType",rs.getInt("ReportFileType"));
            jsonList.put("ReportTitle",rs.getString("ReportTitle"));
            jsonList.put("ReportWhileEmpty",rs.getBoolean("ReportWhileEmpty"));
            jsonList.put("user",rs.getString("UserName"));
            jsonList.put("userid",rs.getString("UID"));
            jsonList.put("group", rs.getString("Gid"));
            JobList.add(jsonList);

        }
        json.put("status","success");
        json.put("time",TimeUtil.getCurrentTime());
        json.put("list",JobList);
        return gson.toJson(json);
    }

    public synchronized static String HistoryListMessage(ResultSet rs,int GID, int UID, String Username )throws SQLException{
        //ToDO
        Map json=new LinkedHashMap();
        Gson gson = new Gson();
        List<Map> JobList=new ArrayList<>();
        while(rs.next()){
            Map jsonList=new LinkedHashMap();
            jsonList.put( "jobrunid",rs.getInt("JHID"));
            jsonList.put( "jobid",rs.getInt("JobID"));
            jsonList.put( "presto_id",rs.getInt("PrestoID"));
            jsonList.put( "jobname",rs.getString("JobName"));
            jsonList.put("jobLevel",rs.getInt("JobLevel"));
            jsonList.put("memo",rs.getString("JobMemo"));
            jsonList.put("type",rs.getString("JobType"));
            String storage=(rs.getInt("JobStorageType")>0)?"True":"False";
            jsonList.put("storage",storage);
            jsonList.put("job_status",rs.getString("JobStatus"));
            jsonList.put("progress",rs.getString("JobProgress"));
            jsonList.put("start_time",rs.getString("JobStartTime"));
            jsonList.put("stop_time",rs.getString("JobStopTime"));
            try{
                jsonList.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("JobStartTime")),
                        TimeUtil.String2DateTime(rs.getString("JobStopTime"))));}
            catch(NullPointerException npe){
                jsonList.put("runingtime",TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("JobStartTime")),
                        TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
            }catch(IllegalArgumentException e){
                jsonList.put("runingtime",TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("JobStartTime")),
                        TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
            }
            jsonList.put("Report",rs.getBoolean("Report"));
            jsonList.put("ReportEmail",rs.getString("ReportEmail"));
            jsonList.put("ReportLength",rs.getInt("ReportLength"));
            jsonList.put("ReportFileType",rs.getInt("ReportFileType"));
            jsonList.put("ReportTitle",rs.getString("ReportTitle"));
            jsonList.put("ReportWhileEmpty",rs.getBoolean("ReportWhileEmpty"));
            jsonList.put("user",rs.getString("UserName"));
            jsonList.put("userid",rs.getString("UID"));
            jsonList.put("group",rs.getString("Gid"));
            JobList.add(jsonList);

        }
        json.put("status","success");
        json.put("time",TimeUtil.getCurrentTime());
        json.put("list",JobList);
        return gson.toJson(json);
    }
    public synchronized static String hasResultJobHistory(ResultSet rs)throws SQLException{
        //ToDO
        Map json=new LinkedHashMap();
        Gson gson = new Gson();
        List<Map> jobList=new ArrayList<>();
        while(rs.next()){
            Map info=new HashMap();
            info.put("jhid",rs.getInt("JHID"));
            info.put("jobid",rs.getInt("JobID"));
            info.put("job_starttime",rs.getString("JobStartTime"));
            info.put("result_count",rs.getInt("ResultCount"));
            info.put("job_output",rs.getString("JobOutput"));
            jobList.add(info);
        }
        json.put("status","success");
        json.put("time",TimeUtil.getCurrentTime());
        json.put("list",jobList);
        return gson.toJson(json);
    }
    public synchronized static String JobHistoryInfoMessage(ResultSet rs, int Uid, int Gid, String UserName)throws SQLException{
        //ToDO
        Map json=JobHistoryInfoMessage(rs);
        json.put("user",UserName);
        json.put("userid",Uid);
        json.put("group",Gid);

        Gson gson = new Gson();
        return gson.toJson(json);
    }
    public synchronized static Map JobHistoryInfoMessage(ResultSet rs)throws SQLException{
        //ToDO
        Map json=new LinkedHashMap();
        json.put("status","success");
        json.put("time",TimeUtil.getCurrentTime());
        json.put("jobrunid",rs.getInt("JHID"));
        json.put("jobid",rs.getInt("JobID"));
        json.put("presto_id",rs.getString("PrestoID"));
        if(rs.getString("PrestoID")!=null&&!rs.getString("PrestoID").equals("")){
            json.put("presto_url", Init.getPrestoURL()+"/query.html?"+rs.getString("PrestoID"));
        }else {
            json.put("presto_url", Init.getPrestoURL());
        }
        json.put("sql",rs.getString("JobSQLLog"));
        json.put("replace_value",rs.getInt("Replace_Value"));
        json.put("replace_sign",rs.getString("Replace_Sign"));
        json.put("jobname",rs.getString("JobName"));
        json.put("jobLevel",rs.getInt("JobLevel"));
        json.put("memo",rs.getString("JobMemo"));
        json.put("type",rs.getString("JobType"));
        json.put("job_status",rs.getString("JobStatus"));
        json.put("progress",rs.getString("JobProgress"));
        json.put("start_time",rs.getString("JobStartTime"));
        json.put("stop_time",rs.getString("JobStopTime"));
        try{
            json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("JobStartTime")),
                    TimeUtil.String2DateTime(rs.getString("JobStopTime"))));}
        catch(NullPointerException npe){
            json.put("runingtime",TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("JobStartTime")),
                    TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
        }catch (IllegalArgumentException e){
            json.put("runingtime",TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("JobStartTime")),
                    TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
        }

        String storage=(rs.getInt("JobStorageType")>0)?"True":"False";
        json.put("storage",storage);
        json.put("save_type",rs.getString("JobStorageType"));
        json.put("filepath",rs.getString("FilePath"));
        json.put("filename",rs.getString("FileName"));
        json.put("location_id",rs.getInt("StorageResources"));
        json.put("location_name",Init.getLocationList().get(rs.getInt("StorageResources")));
        json.put("insertsql",rs.getString("DBSQL"));
        json.put("log",rs.getString("JobLogFile"));
        json.put("ResultCount",rs.getString("ResultCount"));
        json.put("JobLogfile",rs.getString("JobLogfile"));
        json.put("JobOutput",rs.getString("JobOutput"));
        json.put("Valid",rs.getInt("Valid"));
        json.put("Report",rs.getBoolean("Report"));
        json.put("ReportEmail",rs.getString("ReportEmail"));
        json.put("ReportLength",rs.getInt("ReportLength"));
        json.put("ReportFileType",rs.getInt("ReportFileType"));
        json.put("ReportTitle",rs.getString("ReportTitle"));
        json.put("ReportWhileEmpty",rs.getBoolean("ReportWhileEmpty"));

        return json;
    }
    public synchronized static String JobResultInfoMessage()throws SQLException{
        //ToDO
        Map json=new LinkedHashMap();
        Gson gson = new Gson();

        return gson.toJson(json);
    }

    public synchronized static String rtnJobMessage(String status, String time, String message, String jobid){
        Map json=new LinkedHashMap();
        json.put("status",status);
        json.put("time",time);
        json.put("message",message);
        json.put("jobid",jobid);
        Gson gson = new Gson();
        return gson.toJson(json);
    }
    public synchronized static String rtnJobHistoryMessage(String status, String time, String message, String jobHistoryID){
        Map json=new LinkedHashMap();
        json.put("status",status);
        json.put("time",time);
        json.put("message",message);
        json.put("jhid",jobHistoryID);
        Gson gson = new Gson();
        return gson.toJson(json);
    }

    public synchronized static String rtnScheduleMessage(String status, String time, String message, String scheduleid){
        Map json=new LinkedHashMap();
        json.put("status",status);
        json.put("time",time);
        json.put("message",message);
        json.put("scheduleid",scheduleid);
        Gson gson = new Gson();
        return gson.toJson(json);
    }
    public synchronized static String rtnChartMessage(String status, String time, String message, String ChartID){
        Map json=new LinkedHashMap();
        json.put("status",status);
        json.put("time",time);
        json.put("message",message);
        json.put("ChartID",ChartID);
        Gson gson = new Gson();
        return gson.toJson(json);
    }
    public synchronized static String rtnChartInfoMessage(String status, String time, String message, String ChartID,Map chartInfo){
        Map json=new LinkedHashMap();
        json.put("status",status);
        json.put("time",time);
        json.put("message",message);
        json.put("ChartID",ChartID);
        json.put("ChartInfo",chartInfo);
        Gson gson = new Gson();
        return gson.toJson(json);
    }
    public synchronized static String rtnChartListMessage(String status, String time, String message, String ChartID,List chartList){
        Map json=new LinkedHashMap();
        json.put("status",status);
        json.put("time",time);
        json.put("message",message);
        json.put("ChartID",ChartID);
        json.put("ChartInfo",chartList);
        Gson gson = new Gson();
        return gson.toJson(json);
    }


    public synchronized static String rtnScheduleInfoMessage(ResultSet rs,ArrayList<Integer> scheduleJob,ArrayList<LinkedHashMap<String,String>> scheduleTime ) throws SQLException{

        Map json=new LinkedHashMap();
        Gson gson = new Gson();

        json.put("schedule_name",rs.getString("ScheduleName"));
        json.put("schedule_level",rs.getInt("ScheduleLevel"));
        json.put("memo",rs.getString("ScheduleMemo"));
        json.put("notification",rs.getString("Notification"));
        json.put("schedule_owner",rs.getInt("ScheduleOwner"));
        json.put("schedule_status",rs.getInt("ScheduleStatus"));
        json.put("schedule_mode",rs.getString("ScheduleTimeType"));
        if(rs.getString("ScheduleTimeType").equals("single")){
           json.put("mod_set",scheduleTime);
        }

        json.put("runjob",scheduleJob);
        json.put("startwith", rs.getString("StartWith"));
        json.put("every",rs.getInt("TimeEvery"));
        json.put("unit",rs.getString("TimeEveryType"));
        json.put("time",rs.getInt("TimeCycle"));
        json.put("each",rs.getInt("TimeEach"));

        json.put("status","success");

        return gson.toJson(json);

    }

    public synchronized static String scheduleListMessage(List list){
        Map json=new LinkedHashMap();
        Gson gson = new Gson();
        json.put("status","success");
        json.put("Currenttime",TimeUtil.getCurrentTime());
        json.put("list",list);

        return gson.toJson(json);
    }
    public synchronized static String rtnScheduleRunMessage(String status, String time, String message, String schedulerunid){
        Map json=new LinkedHashMap();
        json.put("status",status);
        json.put("time",time);
        json.put("message",message);
        json.put("schedule_runid",schedulerunid);
        Gson gson = new Gson();
        return gson.toJson(json);
    }
    /**
     * Return Account Message List
     * @param status Message Status error|success
     * @parm listName
     * @param message Echo Message List
     * @return Json String
     */
    public static String messageList(String status,String listName,List<Map> message){
        Map json=new LinkedHashMap();
        json.put("status",status);
        json.put("time",TimeUtil.getCurrentTime());
        json.put(listName,message);
        Gson gson = new Gson();
        return gson.toJson(json);
    }

    /**
     * Return Account Message
     * @param status Message Status error|success
     * @param message Echo Message content
     * @return Json String
     */
    public static String message(String status,String message){
        Map json=new LinkedHashMap();
        json.put("status",status);
        json.put("time", TimeUtil.getCurrentTime());
        json.put("message",message);
        Gson gson = new Gson();
        return gson.toJson(json);
    }

    public static List<Map> rtnTemplateMessage(ResultSet rs)throws SQLException{
        List<Map> JobList=new ArrayList<>();
        //String rtn="";
        while(rs.next()) {
            Map json = new LinkedHashMap();
            json.put("URLKey", rs.getString("URLKey"));
            json.put("SQLKey", rs.getString("SQLKey"));
            json.put("DefaultValue", Optional.ofNullable(rs.getString("DefaultValue")).orElse(""));
            JobList.add(json);
        }
        return JobList;
    }

    public static List<Map> rtnChartMessage(ResultSet rs)throws SQLException{
        List<Map> List=new ArrayList<>();
        //String rtn="";
        while(rs.next()) {
            Map json = new LinkedHashMap();
            json.put("ChartID", rs.getString("Number"));
            json.put("JobID", rs.getString("JobID"));
            json.put("Type", rs.getString("Type"));
            json.put("Chart_Name", rs.getString("Chart_Name"));
            json.put("Chart_Setting", rs.getString("Chart_Setting"));
            List.add(json);
        }
        return List;
    }
    public static String rtnPieMessage(List<Map> rs,String status,String message){
        //        {
//            "count":1000,
//                "data":{
//            "yAxis1":[{"x":1,"y":10},{"x":2,"y":20}],
//            "yAxis2":[{"x":1,"y":10},{"x":2,"y":20}]
//        }
//        }
        int size=0;
        if(rs==null){
            size=0;
        }else{
            size=rs.size();
        }
        Map json=new LinkedHashMap();
        Gson gson=new Gson();
        json.put("status",status);
        json.put("message",message);
        json.put("count",size);
        json.put("data",rs);

        String rtn=gson.toJson(json);
        return rtn;
    }
    public static String rtnDrawMessage(Map rs,int count,String status,String message){
        //        {
//            "count":1000,
//                "data":{
//            "yAxis1":[{"x":1,"y":10},{"x":2,"y":20}],
//            "yAxis2":[{"x":1,"y":10},{"x":2,"y":20}]
//        }
//        }
        Map json=new LinkedHashMap();
        Gson gson=new Gson();
        json.put("status",status);
        json.put("message",message);
        json.put("count",count);
        json.put("data",rs);

        String rtn=gson.toJson(json);
        return rtn;
    }

}
