package com.chickling.models.job.bean;

import java.util.ArrayList;

/**
 * Created by gl08 on 2015/12/7.
 */
public class JobLog {


    private ArrayList<String> insertList=new ArrayList<>();


    private String jobsql="";
    private String joboutput="";
    private String joblogfile="";
    private Integer jobstoragetype=0;
    private Integer storageresources=0;
    private String filepath="";
    private String filename="";
    private String dbsql="";
    private Integer replace_value=0;
    private String replace_sign="";
    private Integer resultcount=0;
    private Integer valid=1;
    private Boolean reportWhileEmpty=false;

    public JobLog() {
    }

    public JobLog(String jobsql, String joboutput, String joblogfile, Integer jobstoragetype, Integer storageresources, String filepath, String filename, String dbsql, Integer replace_value, String replace_sign, Integer resultcount, Integer valid, Boolean reportWhileEmpty) {
        this.jobsql = jobsql;
        this.joboutput = joboutput;
        this.joblogfile = joblogfile;
        this.jobstoragetype = jobstoragetype;
        this.storageresources = storageresources;
        this.filepath = filepath;
        this.filename = filename;
        this.dbsql = dbsql;
        this.replace_value = replace_value;
        this.replace_sign = replace_sign;
        this.resultcount = resultcount;
        this.valid = valid;
        this.reportWhileEmpty=reportWhileEmpty;
        this.setInsertList(this.jobsql,this.joboutput,this.joblogfile,this.jobstoragetype,this.storageresources,this.filepath,this.filename,this.dbsql,this.replace_value,this.replace_sign,this.resultcount,this.valid, this.reportWhileEmpty);
    }


    public ArrayList<String> getInsertList() {
        return insertList;
    }
    public void setInsertList(String jobsql, String joboutput, String joblogfile, Integer jobstoragetype, Integer storageresources, String filepath, String filename, String dbsql, Integer replace_value, String replace_sign, Integer resultcount, Integer valid,Boolean reportWhileEmpty) {
        this.insertList.add(jobsql);
        this.insertList.add(joboutput);
        this.insertList.add(joblogfile);
        this.insertList.add(String.valueOf(jobstoragetype));
        this.insertList.add(String.valueOf(storageresources));
        this.insertList.add(filepath);
        this.insertList.add(filename);
        this.insertList.add(dbsql);
        this.insertList.add(String.valueOf(replace_value));
        this.insertList.add(replace_sign);
        this.insertList.add(String.valueOf(resultcount));
        this.insertList.add(String.valueOf(valid));
        this.insertList.add(String.valueOf(reportWhileEmpty));
    }
    public void setInsertList(ArrayList<String> insertList) {
        this.insertList = insertList;
    }

    public String getJobsql() {
        return jobsql;
    }

    public void setJobsql(String jobsql) {
        this.jobsql = jobsql;
    }

    public String getJoboutput() {
        return joboutput;
    }

    public void setJoboutput(String joboutput) {
        this.joboutput = joboutput;
    }

    public String getJoblogfile() {
        return joblogfile;
    }

    public void setJoblogfile(String joblogfile) {
        this.joblogfile = joblogfile;
    }

    public Integer getJobstoragetype() {
        return jobstoragetype;
    }

    public void setJobstoragetype(Integer jobstoragetype) {
        this.jobstoragetype = jobstoragetype;
    }

    public Integer getStorageresources() {
        return storageresources;
    }

    public void setStorageresources(Integer storageresources) {
        this.storageresources = storageresources;
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

    public String getDbsql() {
        return dbsql;
    }

    public void setDbsql(String dbsql) {
        this.dbsql = dbsql;
    }

    public Integer getReplace_value() {
        return replace_value;
    }

    public void setReplace_value(Integer replace_value) {
        this.replace_value = replace_value;
    }

    public String getReplace_sign() {
        return replace_sign;
    }

    public void setReplace_sign(String replace_sign) {
        this.replace_sign = replace_sign;
    }

    public Integer getResultcount() {
        return resultcount;
    }

    public void setResultcount(Integer resultcount) {
        this.resultcount = resultcount;
    }

    public Integer getValid() {
        return valid;
    }

    public void setValid(Integer valid) {
        this.valid = valid;
    }

    public Boolean getReportWhileEmpty() {
        return this.reportWhileEmpty;
    }

    public void setReportWhileEmpty(Boolean reportWhileEmpty) {
        this.reportWhileEmpty = reportWhileEmpty;
    }
}
