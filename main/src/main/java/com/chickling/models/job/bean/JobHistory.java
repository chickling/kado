package com.chickling.models.job.bean;

import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * Created by gl08 on 2015/12/7.
 */
public class JobHistory {

    //insert History
    private ArrayList<String> insertList=new ArrayList<>();
    private Integer jobowner=0;

    //get Job_Historyinfo
    private String status="";
    private DateTime time= DateTime.now();
    private  Integer jobrunid=0;
    private  Integer jobid=0;
    private String presto_id="";
    private String sql="";
    private  Integer replace_value=0;
    private String jobname="";
    private  Integer jobLevel=0;
    private String memo="";
    private String type="";
    private String job_status="";
    private String progress="";
    private String start_time="";
    private String stop_time="";
    private String runingtime="";
    private String user="";
    private  Integer userid=0;
    private  Integer group=0;
    private String storage="";
    private String save_type="";
    private String filepath="";
    private String filename="";
    private  Integer location_id=0;
    private String insertsql="";
    private String log="";
    private String ResultCount="";
    private Boolean report=false;
    private String reportEmail="";
    private int reportLength=0;
    private int reportFileType=0;
    private String reportTitle="";
    private Boolean reportWhileEmpty=false;


    public JobHistory(){}

    public JobHistory(ArrayList<String> insertList) {
        this.insertList = insertList;
    }
    // for insert Job_History
    public JobHistory(Integer jobid, String presto_id, Integer jobowner, Integer jobLevel, String start_time, String stop_time, Integer job_status, String progress,Integer log,  String type,Boolean report,String reportEmail,int reportLength,int reportFileType,String reportTitle,Boolean reportWhileEmpty) {
        this.jobid = jobid;
        this.presto_id = presto_id;
        this.jobowner = jobowner;
        this.jobLevel = jobLevel;
        this.start_time = start_time;
        this.stop_time = stop_time;
        this.job_status = String.valueOf(job_status);
        this.progress = progress;
        this.log = String.valueOf(log);
        this.type = type;
        this.report=report;
        this.reportEmail=reportEmail;
        this.reportLength=reportLength;
        this.reportFileType=reportFileType;
        this.reportTitle=reportTitle;
        this.reportWhileEmpty=reportWhileEmpty;


        insertList.add(String.valueOf(this.jobid));
        insertList.add(this.presto_id);
        insertList.add(String.valueOf(this.jobowner));
        insertList.add(String.valueOf(this.jobLevel));
        insertList.add(this.start_time);
        insertList.add(this.stop_time);
        insertList.add(this.job_status);
        insertList.add(this.progress);
        insertList.add(this.log);
        insertList.add(this.type);
        insertList.add(String.valueOf(this.report));
        insertList.add(this.reportEmail);
        insertList.add(String.valueOf(this.reportFileType));
        insertList.add(String.valueOf(this.reportLength));
        insertList.add(this.reportTitle);
        insertList.add(String.valueOf(this.reportWhileEmpty));

    }

    // for Parse get Job_History
    public JobHistory(String status, DateTime time, Integer jobrunid, Integer jobid, String presto_id, String sql, Integer replace_value, String jobname, Integer jobLevel, String memo, String type, String job_status, String progress, String start_time, String stop_time, String runingtime, String user, Integer userid, Integer group, String storage, String save_type, String filepath, String filename, Integer location_id, String insertsql, String log, String resultCount,Boolean report,String reportEmail,int reportLength,int reportFileType,String reportTitle,Boolean reportWhileEmpty) {
        this.status = status;
        this.time = time;
        this.jobrunid = jobrunid;
        this.jobid = jobid;
        this.presto_id = presto_id;
        this.sql = sql;
        this.replace_value = replace_value;
        this.jobname = jobname;
        this.jobLevel = jobLevel;
        this.memo = memo;
        this.type = type;
        this.job_status = job_status;
        this.progress = progress;
        this.start_time = start_time;
        this.stop_time = stop_time;
        this.runingtime = runingtime;
        this.user = user;
        this.userid = userid;
        this.group = group;
        this.storage = storage;
        this.save_type = save_type;
        this.filepath = filepath;
        this.filename = filename;
        this.location_id = location_id;
        this.insertsql = insertsql;
        this.log = log;
        this.report=report;
        this.reportEmail=reportEmail;
        this.reportLength=reportLength;
        this.reportFileType=reportFileType;
        this.reportTitle=reportTitle;
        this.reportWhileEmpty=reportWhileEmpty;
        ResultCount = resultCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public Integer getJobrunid() {
        return jobrunid;
    }

    public void setJobrunid(Integer jobrunid) {
        this.jobrunid = jobrunid;
    }

    public Integer getJobid() {
        return jobid;
    }

    public void setJobid(Integer jobid) {
        this.jobid = jobid;
    }

    public String getPresto_id() {
        return presto_id;
    }

    public void setPresto_id(String presto_id) {
        this.presto_id = presto_id;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Integer getReplace_value() {
        return replace_value;
    }

    public void setReplace_value(Integer replace_value) {
        this.replace_value = replace_value;
    }

    public String getJobname() {
        return jobname;
    }

    public void setJobname(String jobname) {
        this.jobname = jobname;
    }

    public Integer getJobLevel() {
        return jobLevel;
    }

    public void setJobLevel(Integer jobLevel) {
        this.jobLevel = jobLevel;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJob_status() {
        return job_status;
    }

    public void setJob_status(String job_status) {
        this.job_status = job_status;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getStop_time() {
        return stop_time;
    }

    public void setStop_time(String stop_time) {
        this.stop_time = stop_time;
    }

    public String getRuningtime() {
        return runingtime;
    }

    public void setRuningtime(String runingtime) {
        this.runingtime = runingtime;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getSave_type() {
        return save_type;
    }

    public void setSave_type(String save_type) {
        this.save_type = save_type;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getLocation_id() {
        return location_id;
    }

    public void setLocation_id(Integer location_id) {
        this.location_id = location_id;
    }

    public String getInsertsql() {
        return insertsql;
    }

    public void setInsertsql(String insertsql) {
        this.insertsql = insertsql;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getResultCount() {
        return ResultCount;
    }

    public void setResultCount(String resultCount) {
        ResultCount = resultCount;
    }
    public Integer getJobowner() {
        return jobowner;
    }

    public Boolean getReport(){return this.report;}

    public void setReport(Boolean report){this.report=report;}

    public String getReportEmail(){return this.reportEmail;}

    public void setReportEmail(String reportEmail){this.reportEmail=reportEmail;}

    public int getReportLength(){return this.reportLength;}

    public void setReportLength(int reportLength){this.reportLength=reportLength;}

    public int getReportFileType(){return this.reportFileType;}

    public void setReportFileType(int reportFileType){this.reportFileType=reportFileType;}

    public String getReportTitle(){return this.reportTitle;}

    public void setReportTitle(String reportTitle){this.reportTitle=reportTitle;}


    public void setJobowner(Integer jobowner) {
        this.jobowner = jobowner;
    }
    public ArrayList<String> getInsertList() {
        return insertList;
    }

    public void setInsertList(ArrayList<String> insertList) {
        this.insertList = insertList;
    }

    public void setReportWhileEmpty(Boolean reportWhileEmpty){this.reportWhileEmpty=reportWhileEmpty;}
    public boolean getRepReportWhileEmpty(){return this.reportWhileEmpty;}
}
