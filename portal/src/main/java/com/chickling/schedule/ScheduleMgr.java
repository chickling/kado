package com.chickling.schedule;

import com.chickling.util.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.chickling.models.Auth;
import com.chickling.models.MessageFactory;
import com.chickling.bean.job.User;

import owlstone.dbclient.db.DBClient;
import owlstone.dbclient.db.module.DBResult;
import owlstone.dbclient.db.module.PStmt;
import owlstone.dbclient.db.module.Query;
import owlstone.dbclient.db.module.Row;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.quartz.JobBuilder.*;

/**
 * Created by jw6v on 2015/11/27.
 */


public class ScheduleMgr {
    Logger log = LogManager.getLogger(ScheduleMgr.class);

    public void ScheduleMgr() {}

    public String initSchedule(){
        /**Find out the schedule is triggered before service shut down or reboot**/
        String query="SELECT `ScheduleID` FROM `Schedule` WHERE `ScheduleStatus`='1';";
        int sid=0;
        //DBClient
        Query queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        try {


            queryBean=new Query("kado-meta",query);
            rs=dbClient.execute(queryBean);
            if(!rs.isSuccess())
                throw rs.getException();
            ArrayList<Integer> schedule=new ArrayList<>();
            for(Row row:rs.getRowList()){
                KadoRow r=new KadoRow(row);
                schedule.add(r.getInt("ScheduleID"));
            }


            /**Start schedule of Quartz**/
            for(int id:schedule) {
                sid=id;
                startSchedule(id, "");
            }
        }
        catch(Exception e) {
            log.error(e.getMessage());
            return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), e.toString(), Integer.toString(sid));
        }
        return MessageFactory.rtnScheduleMessage("success", TimeUtil.getCurrentTime(), "initiate quartz schedule is finished", "");
    }

    public String startSchedule(int scheduleID,String token){
        try {
            Type type = new TypeToken<Map>() {
            }.getType();
            Gson gson = new Gson();
            int scheduleOwner = 0;
            String scheduleInfo = "";
            String currentTime = TimeUtil.getCurrentTime();
            Date currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(currentTime);
            Auth au = new Auth();
            User user = new User();
            try {
                user = au.verify2(token);
            } catch (Exception e) {
                log.error(ExceptionUtils.getMessage(e));
            }

            log.info("start");
            try {
                if (!(token.equals(""))) {
                    /**Triggering from user**/
                    if ( user.getPermission()==0 || !(user.getLogIN())) {
                        return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
                    }
                    else{
                    scheduleInfo = ScheduleCRUDUtils.getScheduleInfo(scheduleID, token);
                    scheduleOwner = user.getUserID();
                    }
                } else {
                    /**Initiating**/
                    String query = "SELECT `ScheduleOwner` from `Schedule_History` where `ScheduleID`=? and `ScheduleStatus`=1 order by `ScheduleStartTime` DESC limit 1;";
                    //DBClient
                    PStmt queryBean=null;
                    DBResult rs=null;
                    DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
                    queryBean=PStmt.buildQueryBean("kado-meta",query,new Object[]{
                            scheduleID
                    });
                    rs=dbClient.execute(queryBean);

                    if(!rs.isSuccess())
                        throw rs.getException();
                    //log.info("Start schedule id: "+scheduleID);

                    if(rs.getRowSize()>0){
                        KadoRow r=new KadoRow(rs.getRowList().get(0));
                        scheduleOwner = r.getInt("ScheduleOwner");
                    }
                    //log.info("Start Schedule Owner: "+scheduleOwner);
                    scheduleInfo = ScheduleCRUDUtils.getScheduleInfo(scheduleID);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), e.toString(), Integer.toString(scheduleID));
            }
            Map info = gson.fromJson(scheduleInfo, type);

            if (((String) info.get("status")).equals("success")) {
                if (scheduleOwner <= 0) {
                    scheduleOwner = (((Double) info.get("schedule_owner")).intValue());
                }
                SchedulerFactory sf = new StdSchedulerFactory();
                Scheduler sched = sf.getScheduler();
                /**Create Quartz scheduler by scheduleID and passed the value of scheduleID and scheduleOwner for maintaining scheduleInfo**/
                JobDetail scheduler = newJob(ScheduleRunner.class)
                        .withIdentity(Integer.toString(scheduleID), Integer.toString(scheduleID))//(,scheduleID)
                        .usingJobData("ScheduleID", scheduleID)
                        .usingJobData("ScheduleOwner", scheduleOwner)
                        .build();

                String mode = (String) info.get("schedule_mode");
                String[] args = new String[2];
                String croneformat = "";

                sched.start();
                if (!mode.equals("single")) {
                    /**The schedule stat time is periodical**/
                    String startDateStr = (String) info.get("startwith");
                    Date startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startDateStr);


                    if (mode.equals("interval")) {
                        args[0] = Integer.toString(((Double) info.get("every")).intValue());
                        args[1] = (String) info.get("unit");
                    } else {
                        args[0] = Integer.toString(((Double) info.get("time")).intValue());
                        args[1] = Integer.toString(((Double) info.get("each")).intValue());
                    }
                    /**Provide crone representing **/
                    croneformat = StringUtil.cronGenerator(mode, args,startDate);
                    log.info(croneformat);
                    if (startDate.before(currentDate)) {
                        startDate = currentDate;
                    }
                    /**Crone trigger**/
                    Trigger trigger = TriggerBuilder.newTrigger()
                            .withIdentity(Integer.toString(scheduleID), Integer.toString(scheduleID))
                            .withSchedule(CronScheduleBuilder.cronSchedule(croneformat).withMisfireHandlingInstructionFireAndProceed())
                            .startAt(startDate)
                            .build();

                    sched.scheduleJob(scheduler, trigger);
                } else {
                    /**The schedule start time is pinned on several calender time **/
                    Set<Trigger> triggerList = new HashSet<Trigger>();
                    ArrayList shcheduletime = (ArrayList) info.get("mod_set");
                    for (Object time : shcheduletime) {
                        int i = 0;

                        log.info("Starting schedule : " + scheduleID);

                        Object[] s = ((Map) time).keySet().toArray();

                        Date startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((String) s[0]);

                        log.info((String) s[0]);
                        /**Only start the schedule which start time is after current time**/
                        if (startDate.after(currentDate)) {
                            Trigger trigger = TriggerBuilder.newTrigger()
                                    .withIdentity((String) s[0], Integer.toString(scheduleID))
                                    .startAt(startDate)
                                    .build();


                            log.info(startDate);
                            /**Store trigger time in list**/
                            triggerList.add(trigger);
                        }
                    }
                    Map<JobDetail, Set<? extends Trigger>> map = new HashMap<>();
                    map.put(scheduler, triggerList);
                    sched.scheduleJobs(map, true);
                }
                try {
                    /**Update Schedule info**/
                    ScheduleCRUDUtils.updateScheduleStatus(scheduleID, 1);
                    ScheduleCRUDUtils.updateScheduleStartTime(scheduleID, TimeUtil.getCurrentTime());
                } catch (Exception e) {
                    log.error(e.getMessage());
                    return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Update status failed, " + e.toString(), Integer.toString(scheduleID));
                }
                return MessageFactory.rtnScheduleMessage("success", TimeUtil.getCurrentTime(), "", Integer.toString(scheduleID));
            } else {
                /**Return error message from ScheduleCRUDUtils.getScheduleInfo**/
                return scheduleInfo;
            }

        }
        catch(SchedulerException | ParseException e){
            log.error(e.getMessage());
            return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Update status failed, " + e.toString(), Integer.toString(scheduleID));
        }
    }

    public String stopSchedule(int scheduleID, String token){

        try {
            Auth au = new Auth();
            ArrayList<Object> userInfo = au.verify(token);
            if (!(Boolean) userInfo.get(4) || userInfo.get(0).equals(0)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else {
                Type type = new TypeToken<Map>() {
                }.getType();
                Gson gson = new Gson();
                SchedulerFactory sf = new StdSchedulerFactory();
                Scheduler sched = sf.getScheduler();
                String scheduleInfo;
                ArrayList<TriggerKey> keyList = new ArrayList<>();
                try {
                    scheduleInfo = ScheduleCRUDUtils.getScheduleInfo(scheduleID, token);
                } catch (SQLException se) {
                    log.error(se.getMessage());
                    return MessageFactory.rtnScheduleRunMessage("error", TimeUtil.getCurrentTime(), se.toString(), Integer.toString(scheduleID));
                }
                Map info = gson.fromJson(scheduleInfo, type);
                String mode = (String) info.get("schedule_mode");
                /**Stop and decline schedule by schedule ID**/
                if (mode.equals("single")) {
                    ArrayList shcheduletime = (ArrayList) info.get("mod_set");
                    for (Object time : shcheduletime) {
                        Object[] s = ((Map) time).keySet().toArray();
                        keyList.add(TriggerKey.triggerKey((String) s[0], Integer.toString(scheduleID)));
                    }
                } else {
                    keyList.add(TriggerKey.triggerKey(Integer.toString(scheduleID), Integer.toString(scheduleID)));
                }
                sched.unscheduleJobs(keyList);
                try {
                    ScheduleCRUDUtils.updateScheduleStatus(scheduleID, 0);
                } catch (SQLException se) {
                    log.error(se.getMessage());
                    return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), se.toString(), Integer.toString(scheduleID));
                }
                return MessageFactory.rtnScheduleMessage("success", TimeUtil.getCurrentTime(), "", Integer.toString(scheduleID));
            }
        }
        catch(Exception sche){
            return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), sche.toString(), Integer.toString(scheduleID));
        }
    }
}
