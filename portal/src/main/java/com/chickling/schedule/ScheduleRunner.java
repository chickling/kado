package com.chickling.schedule;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.chickling.models.job.JobRunner;
import com.chickling.models.job.PrestoContent;
import com.chickling.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.quartz.*;


import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Created by jw6v on 2015/11/27.
 */

public class ScheduleRunner implements Job{

    //add and excute job
    private  Logger log = LogManager.getLogger(ScheduleRunner.class);
    private boolean notification=false;
    //ArrayList<HashMap> DATA=null;
    @Override
    public void execute (JobExecutionContext context) throws JobExecutionException {
        /**Execute jobs under the schedule plan**/
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String logPath="";
        String UserLevel="";
        int ScheduleHistoryID=-1;
        try{
            log.info("execute schedule" + Thread.currentThread().getName());

            ArrayList<String> ScheduleHistoryInput =new ArrayList<>();

            Type type = new TypeToken<Map>() {}.getType();
            Gson gson = new Gson();
            JobDataMap data = context.getJobDetail().getJobDataMap();
            int scheduleID=data.getInt("ScheduleID");
            int scheduleOwner=0;

            log.info("execute Schedule:" + scheduleID);

            String scheduleInfo="";
            //query schedule table

            scheduleInfo = ScheduleCRUDUtils.getScheduleInfo(scheduleID);
            log.info(scheduleInfo);
            Map info=gson.fromJson(scheduleInfo, type);
            this.notification=Boolean.parseBoolean((String) info.get("notification"));
            //insert schedule history

            if(data.getInt("ScheduleOwner")>0) {
                /**Trigger by other user**/
                scheduleOwner = data.getInt("ScheduleOwner");
            }
            else{
                /**Trigger by whom created**/
                scheduleOwner=((Double)info.get("schedule_owner")).intValue();
            }

            ScheduleHistoryInput=insertInfo(info, scheduleID,scheduleOwner);

            ScheduleHistoryID=ScheduleCRUDUtils.insertScheduleHistory(ScheduleHistoryInput);

            log.info("ScheduleHistoryID"+ScheduleHistoryID);
            //ScheduleCRUDUtils.insertScheduleJobHistory(ScheduleHistoryID,);

            logPath="ScheduleHistoryLog_"+ScheduleHistoryID;

            ThreadContext.put("logFileName", logPath);

//            logPath=logPath+".log";

            ArrayList Jobs=(ArrayList)info.get("runjob");

            //int ScheduleOwner=((Double)info.get("schedule_owner")).intValue();

            UserLevel=Integer.toString(ScheduleCRUDUtils.getUserLevel(scheduleOwner));

            log.info("User Level"+UserLevel);

            int sortIndex=0;
            //ExecutorService executor = Executors.newFixedThreadPool(10);
            log.info(Jobs.size());

            for(Object jobID:Jobs){
                sortIndex++;
                Future<Boolean> future = null;
                try {
                    log.info("Execute Job: " + ((Double) jobID).intValue());
                    future = executor.submit(new JobRunner( ((Double)jobID).intValue(), PrestoContent.SCHEDULE, UserLevel,ScheduleHistoryID,sortIndex,scheduleOwner));

                } catch (Exception e) {
                    e.printStackTrace();
                    ScheduleCRUDUtils.updateScheduleHistory(ScheduleHistoryID, TimeUtil.getCurrentTime(), 0, logPath);
                    executor.shutdown();
                    StopLogger.stopLogger(log);
                    ThreadContext.remove("logFileName");
                    return;
                }

                //executor.shutdown();
                try {

                    if (!(future.get())) {
                        log.info("testSingleQueryUIJobRunner  End");
                        String errMessage="Job:" + ((Double) jobID).intValue() + "is failed during executing Schedule in History" + ScheduleHistoryID;
                        log.error(errMessage);
                        if(this.notification){
                            Notification.notification(-1,errMessage,"Info",null);
                        }
                        ScheduleCRUDUtils.updateScheduleHistory(ScheduleHistoryID, TimeUtil.getCurrentTime(), 0, logPath);
                        executor.shutdown();
                        log.debug("executor shut down");
                        // throw new Exception("");
                        StopLogger.stopLogger(log);
                        ThreadContext.remove("logFileName");
                        return;
                    }

                }
                catch(NullPointerException npe){
                    ScheduleCRUDUtils.updateScheduleHistory(ScheduleHistoryID, TimeUtil.getCurrentTime(), 0, logPath);
                    executor.shutdown();
                    log.debug("executor shut down");
                    // throw new Exception("");
                    StopLogger.stopLogger(log);
                    ThreadContext.remove("logFileName");
                    return;
                }
                catch(Exception e){
                    log.error(e.getCause());
                    StopLogger.stopLogger(log);
                }
            }

            ScheduleCRUDUtils.updateScheduleHistory(ScheduleHistoryID, TimeUtil.getCurrentTime(),1,logPath);
            //execute job
            //update schedule history
            log.info(ThreadContext.get("logFileName"));
        }
        catch(SQLException e){
            log.error(e.toString());
            // ThreadContext.remove("logFileName");
            //ScheduleCRUDUtils.UpdateScheduleHistory(ScheduleHistoryID,TimeUtil.getCurrentTime(),0,logPath);
            return ;
        } finally {
            executor.shutdown();
            log.info(logPath);
            StopLogger.stopLogger(log);
            ThreadContext.remove("logFileName");
        }
    }

    public ArrayList<String> insertInfo(Map info, int ScheduleID,int ScheduleOwner){

        int scheduleOwner=0;

        if(ScheduleOwner>0){
            scheduleOwner=ScheduleOwner;
        }else{
            scheduleOwner=((Double) info.get("schedule_owner")).intValue();
        }

        ArrayList<String> ScheduleHistoryInput =new ArrayList<>();

        ScheduleHistoryInput.add(Integer.toString(ScheduleID));
        ScheduleHistoryInput.add((String) info.get("schedule_name"));
        ScheduleHistoryInput.add(Integer.toString(scheduleOwner));// todo ScheduleOwner
        ScheduleHistoryInput.add(Integer.toString(((Double) info.get("schedule_level")).intValue()));//ScheduleLevel
        ScheduleHistoryInput.add((String) info.get("memo"));//memo
        ScheduleHistoryInput.add("0");
        ScheduleHistoryInput.add(TimeUtil.getCurrentTime());//ScheduleStartTime
        ScheduleHistoryInput.add("N/A");//ScheduleStopTime
        ScheduleHistoryInput.add("N/A");//todo ScheduleLog
        ScheduleHistoryInput.add((String) info.get("schedule_mode"));//ScheduleTimeType
        ScheduleHistoryInput.add((String) info.get("startwith"));//startWith
        ScheduleHistoryInput.add(Integer.toString(((Double) info.get("every")).intValue()));//timeEvery
        ScheduleHistoryInput.add((String) info.get("unit"));//timeEveryType
        ScheduleHistoryInput.add(Integer.toString(((Double) info.get("time")).intValue()));//timeCycle
        ScheduleHistoryInput.add(Integer.toString(((Double) info.get("each")).intValue()));//timeEach
        ScheduleHistoryInput.add((String) info.get("notification"));//notification

        return ScheduleHistoryInput;
    }

}
