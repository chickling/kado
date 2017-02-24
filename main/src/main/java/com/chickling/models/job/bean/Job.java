package com.chickling.models.job.bean;

/**
 * Created by gl08 on 2015/12/7.
 */
public class Job {

    private int jobID=0;
    private String jobname="";
    private int jobowner=0;
    private int jobLevel=0;
    private String memo="";
    private String notification;
    private String sql="";
    private int replace_value=0;
    private String replace_sign="";
    private int save_type=0;
    private String filepath="";
    private String filename="";
    private int location_id=0;
    private String insertsql="";
    private Boolean Report=false;
    private String ReportEmail="";
    private int ReportLength=0;
    private int ReportFileType=0;
    private String ReportTitle="";
    private Boolean ReportWhileEmpty=false;

    public Job(){

    }

    public Job(int jobID, String jobname, int jobowner, int jobLevel, String memo, String notification, String sql, int replace_value, String replace_sign, int save_type, String filepath, String filename, int location_id, String insertsql,Boolean report,String reportEmail,int reportLength,int reportFileType,String reportTitle, Boolean ReportWhileEmpty) {
        this.jobID = jobID;
        this.jobname = jobname;
        this.jobowner = jobowner;
        this.jobLevel = jobLevel;
        this.memo = memo;
        this.notification =notification;
        this.sql = sql;
        this.replace_value = replace_value;
        this.replace_sign = replace_sign;
        this.save_type = save_type;
        this.filepath = filepath;
        this.filename = filename;
        this.location_id = location_id;
        this.insertsql = insertsql;
        this.Report=report;
        this.ReportEmail=reportEmail;
        this.ReportLength=reportLength;
        this.ReportFileType=reportFileType;
        this.ReportTitle=reportTitle;
        this.ReportWhileEmpty=ReportWhileEmpty;
    }

    public int getJobID() {
        return jobID;
    }

    public void setJobID(int jobID) {
        this.jobID = jobID;
    }

    public String getJobname() {
        return jobname;
    }

    public void setJobname(String jobname) {
        this.jobname = jobname;
    }

    public int getJobowner() {
        return jobowner;
    }

    public void setJobowner(int jobowner) {
        this.jobowner = jobowner;
    }

    public int getJobLevel() {
        return jobLevel;
    }

    public void setJobLevel(int jobLevel) {
        this.jobLevel = jobLevel;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public boolean getNotification() {
        return "1".equals(notification);
    }

    public void setNotification(String notification) {
        this.notification =notification;
    }

    public int getSave_type() {
        return save_type;
    }

    public void setSave_type(int save_type) {
        this.save_type = save_type;
    }

    public int getLocation_id() {
        return location_id;
    }

    public void setLocation_id(int location_id) {
        this.location_id = location_id;
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

    public String getInsertsql() {
        return insertsql;
    }

    public void setInsertsql(String insertsql) {
        this.insertsql = insertsql;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public int getReplace_value() {
        return replace_value;
    }

    public void setReplace_value(int replace_value) {
        this.replace_value = replace_value;
    }

    public String getReplace_sign() {
        return replace_sign;
    }

    public void setReplace_sign(String replace_sign) {
        this.replace_sign = replace_sign;
    }

    public Boolean getReport(){return this.Report;}

    public void setReport(Boolean report){this.Report=report;}

    public String getReportEmail(){return this.ReportEmail;}

    public void setReportEmail(String reportEmail){this.ReportEmail=reportEmail;}

    public int getReportLength(){return this.ReportLength;}

    public void setReportLength(int reportLength){this.ReportLength=reportLength;}

    public int getReportFileType(){return this.ReportFileType;}

    public void setReportFileType(int reportFileType){this.ReportFileType=reportFileType;}

    public String getReportTitle(){return this.ReportTitle;}

    public void setReportTitle(String reportTitle){this.ReportTitle=reportTitle;}
    public Boolean getReporWhileEmpty(){return this.ReportWhileEmpty;}

    public void setReportWhileEmpty(Boolean ReportWhileEmpty){this.ReportWhileEmpty=ReportWhileEmpty;}
}
