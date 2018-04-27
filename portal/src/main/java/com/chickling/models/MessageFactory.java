package com.chickling.models;

import com.chickling.util.KadoRow;
import com.google.gson.Gson;
import com.chickling.boot.Init;
import com.chickling.util.TemplateCRUDUtils;
import com.chickling.util.TimeUtil;
import owlstone.dbclient.db.module.DBResult;
import owlstone.dbclient.db.module.Row;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by jw6v on 2015/11/30.
 */
public class MessageFactory {

    public synchronized static String rtnJobInfoMessage(DBResult rs)throws SQLException{
        Map json=new LinkedHashMap();
        Gson gson = new Gson();
        if(rs.getRowSize()==0) {
            json.put("status","fail");
            json.put("message","not found");
            return gson.toJson(json);
        }
        KadoRow r=new KadoRow(rs.getRowList().get(0));
        int jobID=r.getInt("JobID");
        json.put("jobID", jobID);
        json.put("jobname",r.getString("JobName"));
        json.put("jobowner",r.getInt("JobOwner"));
        json.put("jobLevel",r.getInt("JobLevel"));
        json.put("memo",r.getString("JobMemo"));
        json.put("notification",r.getString("Notification"));
        json.put("sql",r.getString("JobSQL"));
//        json.put("replace_value",rs.getInt("Replace_Value"));
//        json.put("replace_sign",rs.getString("Replace_Sign"));
        json.put("save_type",r.getInt("JobStorageType"));
        json.put("filepath",r.getString("FilePath"));
        json.put("filename",r.getString("FileName"));
        json.put("location_id",r.getInt("StorageResources"));
        json.put("insertsql",r.getString("DBSQL"));
        json.put("Report",r.getBoolean("Report"));
        json.put("ReportEmail",r.getString("ReportEmail"));
        json.put("ReportLength",r.getInt("ReportLength"));
        json.put("ReportFileType",r.getInt("ReportFileType"));
        json.put("ReportTitle",r.getString("ReportTitle"));
        json.put("ReportWhileEmpty",r.getBoolean("ReportWhileEmpty"));
        json.put("status","success");

        List<Map> templateInfo=TemplateCRUDUtils.readSqlTemplate(jobID);
        json.put("SQLTemplate",templateInfo);

        return gson.toJson(json);
    }

    public synchronized static String JobListMessage(DBResult rs,int GID, int UID, String Username )throws SQLException{
        //ToDO
        Map json=new LinkedHashMap();
        Gson gson = new Gson();
        List<Map> JobList=new ArrayList<>();
        for(Row row:rs.getRowList()){
            KadoRow r=new KadoRow(row);
            Map jsonList=new LinkedHashMap();
            jsonList.put("jobid",r.getInt("JobID"));
            jsonList.put("jobname",r.getString("JobName"));
            jsonList.put("jobLevel",r.getInt("JobLevel"));
            jsonList.put("memo",r.getString("JobMemo"));
            jsonList.put("type",r.getString("JobType"));
            String storage=(r.getInt("JobStorageType")>0)?"True":"False";
            jsonList.put("storage",storage);

            try{
                jsonList.put("last_runtime", r.getString("JobStartTime"));
                jsonList.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                        TimeUtil.String2DateTime(r.getString("JobStopTime"))));}
            catch(NullPointerException npe){
                jsonList.put("last_runtime", "");
                if(r.getString("JobStartTime")!=null) {
                    jsonList.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                            TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                }else {
                    jsonList.put("runingtime","0");
                }
            }
            jsonList.put("Report",r.getBoolean("Report"));
            jsonList.put("ReportEmail",r.getString("ReportEmail"));
            jsonList.put("ReportLength",r.getInt("ReportLength"));
            jsonList.put("ReportFileType",r.getInt("ReportFileType"));
            jsonList.put("ReportTitle",r.getString("ReportTitle"));
            jsonList.put("ReportWhileEmpty",r.getBoolean("ReportWhileEmpty"));
            jsonList.put("user",r.getString("UserName"));
            jsonList.put("userid",r.getString("UID"));
            jsonList.put("group",GID);
            JobList.add(jsonList);

        }
        json.put("status","success");
        json.put("time",TimeUtil.getCurrentTime());
        json.put("list",JobList);
        return gson.toJson(json);
    }

    public synchronized static String JobStatusListMessage(DBResult rs,int GID, int UID, String Username )throws SQLException{
        //ToDO
        Map json=new LinkedHashMap();
        Gson gson = new Gson();
        List<Map> JobList=new ArrayList<>();
        for(Row row:rs.getRowList()){
            KadoRow r=new KadoRow(row);
            Map jsonList=new LinkedHashMap();
            jsonList.put( "jobrunid",r.getInt("JHID"));
            jsonList.put( "jobid",r.getInt("JobID"));
            if(r.getString("JobName")!=null)
                jsonList.put( "jobname",r.getString("JobName"));
            else
                jsonList.put( "jobname",r.getString("PrestoID"));

            jsonList.put("jobLevel",r.getInt("JobLevel"));
            jsonList.put("memo",r.getString("JobMemo"));
            jsonList.put("type",r.getString("JobType"));
            String storage=(r.getInt("JobStorageType")>0)?"True":"False";
            jsonList.put("storage",storage);
            jsonList.put("job_status",r.getString("JobStatus"));
            jsonList.put("start_time",r.getString("JobStartTime"));
            jsonList.put("stop_time",r.getString("JobStopTime"));
            jsonList.put("progress",r.getString("JobProgress"));
            try{

                if(!r.getString("JobStopTime").equals("")) {
                    jsonList.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                            TimeUtil.String2DateTime(r.getString("JobStopTime"))));
                }else {
                    jsonList.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                            TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                }
            }
            catch(NullPointerException npe){
                jsonList.put("runingtime",TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                        TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
            }
            jsonList.put("Report",r.getBoolean("Report"));
            jsonList.put("ReportEmail",r.getString("ReportEmail"));
            jsonList.put("ReportLength",r.getInt("ReportLength"));
            jsonList.put("ReportFileType",r.getInt("ReportFileType"));
            jsonList.put("ReportTitle",r.getString("ReportTitle"));
            jsonList.put("ReportWhileEmpty",r.getBoolean("ReportWhileEmpty"));
            jsonList.put("user",r.getString("UserName"));
            jsonList.put("userid",r.getString("UID"));
            jsonList.put("group", r.getString("Gid"));
            JobList.add(jsonList);

        }
        json.put("status","success");
        json.put("time",TimeUtil.getCurrentTime());
        json.put("list",JobList);
        return gson.toJson(json);
    }
    public synchronized static Map<Integer,Map> JobStatusListMessage(DBResult rs)throws Exception{
        //ToDO
        Map<Integer,Map> JobMap=new TreeMap<>();
        for(Row row:rs.getRowList()){
            KadoRow r=new KadoRow(row);
            Map jsonList=new LinkedHashMap();
            jsonList.put( "jobrunid",r.getInt("JHID"));
            jsonList.put( "jobid",r.getInt("JobID"));
            if(r.getString("JobName")!=null)
                jsonList.put( "jobname",r.getString("JobName"));
            else
                jsonList.put( "jobname",r.getString("PrestoID"));

            jsonList.put("jobLevel",r.getInt("JobLevel"));
            jsonList.put("memo",r.getString("JobMemo"));
            jsonList.put("type",r.getString("JobType"));
            String storage=(r.getInt("JobStorageType")>0)?"True":"False";
            jsonList.put("storage",storage);
            jsonList.put("job_status",r.getString("JobStatus"));
            jsonList.put("start_time",r.getString("JobStartTime"));
            jsonList.put("stop_time",r.getString("JobStopTime"));
            jsonList.put("progress",r.getString("JobProgress"));
            try{

                if(!r.getString("JobStopTime").equals("")) {
                    jsonList.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                            TimeUtil.String2DateTime(r.getString("JobStopTime"))));
                }else {
                    jsonList.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                            TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                }
            }
            catch(NullPointerException npe){
                jsonList.put("runingtime",TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                        TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
            }
            jsonList.put("Report",r.getBoolean("Report"));
            jsonList.put("ReportEmail",r.getString("ReportEmail"));
            jsonList.put("ReportLength",r.getInt("ReportLength"));
            jsonList.put("ReportFileType",r.getInt("ReportFileType"));
            jsonList.put("ReportTitle",r.getString("ReportTitle"));
            jsonList.put("ReportWhileEmpty",r.getBoolean("ReportWhileEmpty"));
            jsonList.put("user",r.getString("UserName"));
            jsonList.put("userid",r.getString("UID"));
            jsonList.put("group", r.getString("Gid"));
            JobMap.put(r.getInt("JHID"),jsonList);

        }

        return JobMap;
    }

    public synchronized static String HistoryListMessage(DBResult rs,int GID, int UID, String Username )throws SQLException{
        //ToDO
        Map json=new LinkedHashMap();
        Gson gson = new Gson();
        List<Map> JobList=new ArrayList<>();

        for(Row row:rs.getRowList()){
            KadoRow r=new KadoRow(row);
            Map jsonList=new LinkedHashMap();
            jsonList.put( "jobrunid",r.getInt("JHID"));
            jsonList.put( "jobid",r.getInt("JobID"));
            jsonList.put( "presto_id",r.getInt("PrestoID"));
            jsonList.put( "jobname",r.getString("JobName"));
            jsonList.put("jobLevel",r.getInt("JobLevel"));
            jsonList.put("memo",r.getString("JobMemo"));
            jsonList.put("type",r.getString("JobType"));
            String storage=(r.getInt("JobStorageType")>0)?"True":"False";
            jsonList.put("storage",storage);
            jsonList.put("job_status",r.getString("JobStatus"));
            jsonList.put("progress",r.getString("JobProgress"));
            jsonList.put("start_time",r.getString("JobStartTime"));
            jsonList.put("stop_time",r.getString("JobStopTime"));
            try{
                jsonList.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                        TimeUtil.String2DateTime(r.getString("JobStopTime"))));}
            catch(NullPointerException npe){
                jsonList.put("runingtime",TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                        TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
            }catch(IllegalArgumentException e){
                jsonList.put("runingtime",TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                        TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
            }
            jsonList.put("Report",r.getBoolean("Report"));
            jsonList.put("ReportEmail",r.getString("ReportEmail"));
            jsonList.put("ReportLength",r.getInt("ReportLength"));
            jsonList.put("ReportFileType",r.getInt("ReportFileType"));
            jsonList.put("ReportTitle",r.getString("ReportTitle"));
            jsonList.put("ReportWhileEmpty",r.getBoolean("ReportWhileEmpty"));
            jsonList.put("user",r.getString("UserName"));
            jsonList.put("userid",r.getString("UID"));
            jsonList.put("group",r.getString("Gid"));
            JobList.add(jsonList);

        }
        json.put("status","success");
        json.put("time",TimeUtil.getCurrentTime());
        json.put("list",JobList);
        return gson.toJson(json);
    }
    public synchronized static String hasResultJobHistory(DBResult rs)throws SQLException{
        //ToDO
        Map json=new LinkedHashMap();
        Gson gson = new Gson();
        List<Map> jobList=new ArrayList<>();
        for(Row row:rs.getRowList()){
            KadoRow r=new KadoRow(row);
            Map info=new HashMap();
            info.put("jhid",r.getInt("JHID"));
            info.put("jobid",r.getInt("JobID"));
            info.put("job_starttime",r.getString("JobStartTime"));
            info.put("result_count",r.getInt("ResultCount"));
            info.put("job_output",r.getString("JobOutput"));
            jobList.add(info);
        }
        json.put("status","success");
        json.put("time",TimeUtil.getCurrentTime());
        json.put("list",jobList);
        return gson.toJson(json);
    }
    public synchronized static String JobHistoryInfoMessage(DBResult rs, int Uid, int Gid, String UserName)throws SQLException{
        //ToDO
        Map json=JobHistoryInfoMessage(rs);
        json.put("user",UserName);
        json.put("userid",Uid);
        json.put("group",Gid);

        Gson gson = new Gson();
        return gson.toJson(json);
    }
    public synchronized static Map JobHistoryInfoMessage(DBResult rs)throws SQLException{
        //ToDO
        Map json=new LinkedHashMap();
        if(rs.getRowSize()==0){
            json.put("status","success");
            json.put("message","not found");
            return json;
        }
        KadoRow r=new KadoRow(rs.getRowList().get(0));
        json.put("status","success");
        json.put("time",TimeUtil.getCurrentTime());
        json.put("jobrunid",r.getInt("JHID"));
        json.put("jobid",r.getInt("JobID"));
        json.put("presto_id",r.getString("PrestoID"));
        if(r.getString("PrestoID")!=null&&!r.getString("PrestoID").equals("")){
            json.put("presto_url", Init.getPrestoURL()+"/query.html?"+r.getString("PrestoID"));
        }else {
            json.put("presto_url", Init.getPrestoURL());
        }
        json.put("sql",r.getString("JobSQLLog"));
        json.put("replace_value",r.getInt("Replace_Value"));
        json.put("replace_sign",r.getString("Replace_Sign"));
        json.put("jobname",r.getString("JobName"));
        json.put("jobLevel",r.getInt("JobLevel"));
        json.put("memo",r.getString("JobMemo"));
        json.put("type",r.getString("JobType"));
        json.put("job_status",r.getString("JobStatus"));
        json.put("progress",r.getString("JobProgress"));
        json.put("start_time",r.getString("JobStartTime"));
        json.put("stop_time",r.getString("JobStopTime"));
        try{
            json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                    TimeUtil.String2DateTime(r.getString("JobStopTime"))));}
        catch(NullPointerException npe){
            json.put("runingtime",TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                    TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
        }catch (IllegalArgumentException e){
            json.put("runingtime",TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                    TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
        }

        String storage=(r.getInt("JobStorageType")>0)?"True":"False";
        json.put("storage",storage);
        json.put("save_type",r.getString("JobStorageType"));
        json.put("filepath",r.getString("FilePath"));
        json.put("filename",r.getString("FileName"));
        json.put("location_id",r.getInt("StorageResources"));
        json.put("location_name",Init.getLocationList().size()>r.getInt("StorageResources")?Init.getLocationList().get(r.getInt("StorageResources")):"");
        json.put("insertsql",r.getString("DBSQL"));
        json.put("log",r.getString("JobLogFile"));
        json.put("ResultCount",r.getString("ResultCount"));
        json.put("JobLogfile",r.getString("JobLogfile"));
        json.put("JobOutput",r.getString("JobOutput"));
        json.put("Valid",r.getInt("Valid"));
        json.put("Report",r.getBoolean("Report"));
        json.put("ReportEmail",r.getString("ReportEmail"));
        json.put("ReportLength",r.getInt("ReportLength"));
        json.put("ReportFileType",r.getInt("ReportFileType"));
        json.put("ReportTitle",r.getString("ReportTitle"));
        json.put("ReportWhileEmpty",r.getBoolean("ReportWhileEmpty"));

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


    public synchronized static String rtnScheduleInfoMessage(DBResult rs,ArrayList<Integer> scheduleJob,ArrayList<LinkedHashMap<String,String>> scheduleTime ) throws SQLException{

        Map json=new LinkedHashMap();
        Gson gson = new Gson();

        KadoRow r=new KadoRow(rs.getRowList().get(0));

        json.put("schedule_name",r.getString("ScheduleName"));
        json.put("schedule_level",r.getInt("ScheduleLevel"));
        json.put("memo",r.getString("ScheduleMemo"));
        json.put("notification",r.getString("Notification"));
        json.put("schedule_owner",r.getInt("ScheduleOwner"));
        json.put("schedule_status",r.getInt("ScheduleStatus"));
        json.put("schedule_mode",r.getString("ScheduleTimeType"));
        if(r.getString("ScheduleTimeType").equals("single")){
           json.put("mod_set",scheduleTime);
        }

        json.put("runjob",scheduleJob);
        json.put("startwith", r.getString("StartWith"));
        json.put("every",r.getInt("TimeEvery"));
        json.put("unit",r.getString("TimeEveryType"));
        json.put("time",r.getInt("TimeCycle"));
        json.put("each",r.getInt("TimeEach"));

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

    public static List<Map> rtnTemplateMessage(DBResult rs)throws SQLException{
        List<Map> JobList=new ArrayList<>();
        //String rtn="";
        for(Row row:rs.getRowList()){
            KadoRow r=new KadoRow(row);
            Map json = new LinkedHashMap();
            json.put("URLKey", r.getString("URLKey"));
            json.put("SQLKey", r.getString("SQLKey"));
            json.put("DefaultValue", Optional.ofNullable(r.getString("DefaultValue")).orElse(""));
            JobList.add(json);
        }
        return JobList;
    }

    public static List<Map> rtnChartMessage(DBResult rs)throws SQLException{
        List<Map> List=new ArrayList<>();
        //String rtn="";
        for(Row row:rs.getRowList()){
            KadoRow r=new KadoRow(row);
            Map json = new LinkedHashMap();
            json.put("ChartID", r.getString("Number"));
            json.put("JobID", r.getString("JobID"));
            json.put("Type", r.getString("Type"));
            json.put("Chart_Name", r.getString("Chart_Name"));
            json.put("Chart_Setting", r.getString("Chart_Setting"));
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
