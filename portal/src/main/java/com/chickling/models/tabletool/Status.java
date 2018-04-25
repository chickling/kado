package com.chickling.models.tabletool;

import java.util.Map;

/**
 * Created by ey67 on 2018/2/5.
 */
public class Status {
    final public static Integer WAIT=0;
    final public static Integer RUNING=1;
    final public static Integer SUCCESS=2;
    final public static Integer FAIL=3;
    private Integer status;
    private Integer progress=0;
    private Integer rowCount;
    private Integer processCount;
    private String jobID;
    private String fileName;
    private String message;
    private Map schemaMap;

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Map getSchemaMap() {
        return schemaMap;
    }

    public void setSchemaMap(Map schemaMap) {
        this.schemaMap = schemaMap;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public Integer getProcessCount() {
        return processCount;
    }

    public void setProcessCount(Integer processCount) {
        this.processCount = processCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
