package com.chickling.bean.schedule;

/**
 * Created by jw6v on 2015/12/18.
 */
public class ScheduleHistory {
    private int ScheduleID=-1;
    private String ScheduleName="";
    private int ScheduleOwner=-1;
    private int ScheduleLevel=-1;
    private String ScheduleMemo="";
    private int ScheduleStatus=0;
    private String ScheduleStartTime="";
    private String ScheduleStopTime="";
    private String ScheduleLog="";
    private String ScheduleTimeType="";
    private String StartWith="";
    private int TimeEvery=-1;
    private String TimeEveryType="";
    private int TimeCycle=0;
    private String TimeEach="";
    private Boolean Notification=false;

    public ScheduleHistory(){}

    public ScheduleHistory(int ScheduleID,String ScheduleName,int ScheduleOwner,
                           int ScheduleLevel,String ScheduleMemo,int ScheduleStatus,
                           String ScheduleStartTime,String ScheduleStopTime,String ScheduleLog,
                           String ScheduleTimeType,String StartWith,int TimeEvery,
                           String TimeEveryType,int TimeCycle,String TimeEach,Boolean Notification){
        this.ScheduleID=ScheduleID;
        this.ScheduleName=ScheduleName;
        this.ScheduleOwner=ScheduleOwner;
        this.ScheduleLevel=ScheduleLevel;
        this.ScheduleMemo=ScheduleMemo;
        this.ScheduleStatus=ScheduleStatus;
        this.ScheduleStartTime=ScheduleStartTime;
        this.ScheduleStopTime=ScheduleStopTime;
        this.ScheduleLog=ScheduleLog;
        this.ScheduleTimeType=ScheduleTimeType;
        this.StartWith=StartWith;
        this.TimeEvery=TimeEvery;
        this.TimeEveryType=TimeEveryType;
        this.TimeCycle=TimeCycle;
        this.TimeEach=TimeEach;
        this.Notification=Notification;
    }
    public void setScheduleID(int ScheduleID){
        this.ScheduleID=ScheduleID;
    }
    public int getScheduleID(){
        return this.ScheduleID;
    }
    public void setScheduleName(String ScheduleName){
        this.ScheduleName=ScheduleName;
    }
    public String getScheduleName(){
        return ScheduleName;
    }
    public void setScheduleOwner(int ScheduleOwner){
        this.ScheduleOwner=ScheduleOwner;
    }
    public int getScheduleOwner(){
        return this.ScheduleOwner;
    }
    public void setScheduleLevel(int ScheduleLevel){
        this.ScheduleLevel=ScheduleLevel;
    }
    public int getScheduleLevel(){
        return this.ScheduleLevel;
    }
    public void setScheduleMemo(String ScheduleMemo){
        this.ScheduleMemo=ScheduleMemo;
    }
    public String getScheduleMemo(){
        return this.ScheduleMemo;
    }
    public void setScheduleStatus(int ScheduleStatus){
        this.ScheduleStatus=ScheduleStatus;
    }
    public int getScheduleStatus(){
        return this.ScheduleStatus;
    }
    public void setScheduleStartTime(String ScheduleStartTime){
        this.ScheduleStartTime=ScheduleStartTime;
    }
    public String getScheduleStartTime(){
        return this.ScheduleStartTime;
    }
    public void setScheduleStopTime(String ScheduleStopTime){
        this.ScheduleStopTime=ScheduleStopTime;
    }
    public String getScheduleStopTime(){
        return this.ScheduleStopTime;
    }
    public void setScheduleLog(String ScheduleLog){
        this.ScheduleLog=ScheduleLog;
    }
    public String getScheduleLog(){
        return this.ScheduleLog;
    }
    public void setScheduleTimeType(String ScheduleTimeType){
        this.ScheduleTimeType=ScheduleTimeType;
    }
    public String getScheduleTimeType(){
        return this.ScheduleTimeType;
    }

    public void setStartWith(String StartWith){
        this.StartWith=StartWith;
    }

    public String getStartWith(){
        return this.StartWith;
    }

    public void setTimeEvery(int TimeEvery){
        this.TimeEvery=TimeEvery;
    }

    public int getTimeEvery(){
        return this.TimeEvery;
    }

    public void setTimeEveryType(String TimeEveryType){
        this.TimeEveryType=TimeEveryType;
    }

    public String getTimeEveryType(){
        return this.TimeEveryType;
    }

    public void setTimeCycle(int TimeCycle){
        this.TimeCycle=TimeCycle;
    }

    public int getTimeCycle(){
        return this.TimeCycle;
    }

    public void setTimeEach(String TimeEach){
        this.TimeEach=TimeEach;
    }

    public String getTimeEach(){
        return this.TimeEach;
    }

    public void setNotification(Boolean Notification){
        this.Notification=Notification;
    }

    public Boolean getNotification(){
        return this.Notification;
    }

}
