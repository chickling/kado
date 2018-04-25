package com.chickling.util;

import com.chickling.bean.schedule.ScheduleHistory;
import com.facebook.presto.hive.$internal.org.apache.commons.lang3.exception.ExceptionUtils;
import com.google.gson.Gson;

import com.chickling.schedule.ScheduleMgr;
import com.chickling.models.Auth;
import com.chickling.models.MessageFactory;
import com.chickling.models.job.PrestoContent;
import owlstone.dbclient.db.DBClient;
import owlstone.dbclient.db.module.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by jw6v on 2015/12/8.
 */
public class ScheduleCRUDUtils {

    private final static String InsertScheduleSql="INSERT INTO `Schedule` (`ScheduleName`,`ScheduleOwner`,`ScheduleLevel`,`ScheduleMemo`," +
            "`ScheduleStatus`,`ScheduleStartTime`,`ScheduleTimeType`,`StartWith`,`TimeEvery`,`TimeEveryType`,`TimeCycle`,`TimeEach`,`Notification`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);";
    private final static String UpdateScheduleSql="UPDATE `Schedule` SET `ScheduleName`=?,`ScheduleLevel`=?,`ScheduleMemo`=?,`ScheduleStatus`=?" +
            ", `ScheduleStartTime`=?,`ScheduleTimeType`=?,`StartWith`=?,`TimeEvery`=?,`TimeEveryType`=?,`TimeCycle`=?,`TimeEach`=?,`Notification`=? WHERE `ScheduleID`=?;";
    private final static String UpdateScheduleHistorySql="UPDATE `Schedule_History` SET `ScheduleStopTime`=?,`ScheduleStatus`=?,`ScheduleLog`=? WHERE `SHID`=?;";
    private final static String InsertScheduleHistorySql="INSERT INTO `Schedule_History` (`ScheduleID`,`ScheduleName`,`ScheduleOwner`,`ScheduleLevel`,`ScheduleMemo`,`ScheduleStatus`,`ScheduleStartTime`,`ScheduleStopTime`," +
            "`ScheduleLog`,`ScheduleTimeType`,`StartWith`,`TimeEvery`,`TimeEveryType`,`TimeCycle`,`TimeEach`,`Notification`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
    private final static String GetScheduleInfoSql="SELECT * FROM `Schedule` WHERE `ScheduleID`=?;";
    private final static String GetScheduleInfoSql_usr="SELECT *, ScheduleOwner UID FROM `Schedule` WHERE `ScheduleID`=? and ( ScheduleOwner in (Select UID From User WHERE Gid=?) or  ScheduleLevel=1)";
    private final static String DeleteSchedule="DELETE FROM `Schedule` WHERE `ScheduleID`=?;";
    private final static String CheckScheduleID="SELECT * FROM `Schedule` WHERE `ScheduleID`=?;";
    private final static String SelectAllScheduleSql= "SELECT *, j.ScheduleOwner UID FROM Schedule j  LEFT JOIN (SELECT ScheduleID, Max(ScheduleStartTime) FROM Schedule_History WHERE ScheduleStatus = 1 group by ScheduleID) jh on j.ScheduleID=jh.ScheduleID INNER JOIN User u ON u.UID=j.ScheduleOwner;";
    private final static String SelectScheduleListSql ="SELECT *, j.ScheduleOwner UID FROM Schedule j LEFT JOIN (SELECT ScheduleID, Max(ScheduleStartTime) FROM Schedule_History WHERE ScheduleStatus=1 group by ScheduleID) jh on j.ScheduleID=jh.ScheduleID INNER JOIN User u ON j.ScheduleOwner=u.UID WHERE (j.ScheduleOwner in (Select UID From User WHERE Gid=?) or  j.ScheduleLevel=1);";
    private final static String SelectScheduleExecutionList="SELECT *,j.ScheduleOwner UID FROM Schedule_History jh INNER JOIN Schedule j ON j.ScheduleID=jh.ScheduleID JOIN User u ON u.UID=j.ScheduleOwner  WHERE j.ScheduleOwner in (Select UID From User WHERE Gid=?) or  j.ScheduleLevel=1 ORDER BY jh.ScheduleStartTime DESC limit ?;";
    private final static String SelectAllScheduleExecutionList="SELECT *,j.ScheduleOwner UID FROM Schedule_History jh INNER JOIN Schedule j ON jh.ScheduleID=j.ScheduleID INNER JOIN User u ON j.ScheduleOwner =u.UID  ORDER BY jh.ScheduleStartTime DESC limit ?";
    //old sql no join rule
    //private final static String SelectHistoryScheduleList_time_user="SELECT * FROM (SELECT *, Schedule_History.ScheduleOwner UID FROM Schedule INNER JOIN Schedule_History WHERE  Schedule_History.ScheduleStartTime>? and Schedule_History.ScheduleStopTime<? and (UID in (Select UID From User WHERE Gid=?) or  Schedule_History.ScheduleLevel=1)) jhr,User u WHERE jhr.ScheduleOwner =u.UID;";
    private final static String SelectHistoryScheduleList_time_user="SELECT *, Schedule_History.ScheduleOwner UID FROM Schedule INNER JOIN Schedule_History ON Schedule_History.ScheduleID=Schedule.ScheduleID INNER JOIN User u ON Schedule_History.ScheduleOwner =u.UID WHERE Schedule_History.ScheduleStartTime>? and Schedule_History.ScheduleStopTime<? and (Schedule_History.ScheduleOwner in (Select UID From User WHERE Gid=?) or  Schedule_History.ScheduleLevel=1);";
    private final static String SelectHistoryScheduleList_ScheduleID_user="SELECT *,  Schedule_History.ScheduleOwner UID,Schedule_History.ScheduleStartTime RScheduleStartTime FROM Schedule INNER JOIN Schedule_History ON Schedule.ScheduleID=Schedule_History.ScheduleID INNER JOIN User u ON Schedule_History.ScheduleOwner =u.UID WHERE Schedule_History.ScheduleID=? and (Schedule_History.ScheduleOwner in (Select UID From User WHERE Gid=?) or Schedule.ScheduleLevel=1);";
    private final static String SelectHistoryScheduleList_timeandScheduleId_user="SELECT *, Schedule_History.ScheduleOwner UID FROM Schedule INNER JOIN Schedule_History INNER JOIN User u ON Schedule_History.ScheduleOwner =u.UID WHERE Schedule_History.ScheduleStartTime>? and Schedule_History.ScheduleStopTime<? and Schedule_History.ScheduleID=? and(Schedule_History.ScheduleOwner in (Select UID From User WHERE Gid=?) or Schedule.ScheduleLevel=1);";
    //MySQL if time is none then '0000-00-00 00:00:00' is < every stop time
    private final static String SelectHistoryScheduleList_time="SELECT *, Schedule_History.ScheduleOwner UID,Schedule_History.ScheduleStartTime RScheduleStartTime FROM Schedule INNER JOIN Schedule_History ON Schedule.ScheduleID=Schedule_History.ScheduleID  INNER JOIN User u ON Schedule.ScheduleOwner =u.UID WHERE Schedule_History.ScheduleStartTime>? and Schedule_History.ScheduleStopTime<?;";
    private final static String SelectHistoryScheduleList_ScheduleID="SELECT *,  Schedule_History.ScheduleOwner UID,Schedule_History.ScheduleStartTime RScheduleStartTime FROM Schedule INNER JOIN Schedule_History ON Schedule.ScheduleID=Schedule_History.ScheduleID INNER JOIN User u ON Schedule.ScheduleOwner =u.UID WHERE Schedule.ScheduleID=?;";
    private final static String SelectHistoryScheduleList_timeandScheduleId="SELECT *, Schedule_History.ScheduleOwner UID,Schedule_History.ScheduleStartTime RScheduleStartTime FROM Schedule INNER JOIN Schedule_History ON Schedule.ScheduleID=Schedule_History.ScheduleID INNER JOIN User u ON Schedule.ScheduleOwner =u.UID WHERE Schedule_History.ScheduleStartTime>? and Schedule_History.ScheduleStopTime<? and Schedule.ScheduleID=?;";
    private final static String SelectScheduleHistoryInfo="SELECT *,Schedule_History.ScheduleOwner UID,Schedule_History.ScheduleStartTime RScheduleStartTime FROM Schedule_History INNER JOIN Schedule ON Schedule.ScheduleID=Schedule_History.ScheduleID INNER JOIN User u ON Schedule.ScheduleOwner=u.UID WHERE SHID=?;";
    private final static String SelectScheduleHistoryInfo_user="SELECT *,Schedule_History.ScheduleOwner UID,Schedule_History.ScheduleStartTime RScheduleStartTime FROM Schedule_History INNER JOIN Schedule ON Schedule.ScheduleID=Schedule_History.ScheduleID INNER JOIN User u ON Schedule.ScheduleOwner =u.UID WHERE SHID=? and (UID in (Select UID From User WHERE Gid=?) or Schedule.ScheduleLevel=1);";

    private static Logger log = LogManager.getLogger(ScheduleCRUDUtils.class);
    /**
     * Adding schedule information in SQLite table: Schedule
     * @param args; json data: Map
     * @param token; user token: String
     * @return Success or Error, since permission denied
     * @throws SQLException
     */
    public synchronized static String addSchedule(Map args, String token) {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        String querySQL="";
        Boolean singleJob=false;
        ArrayList stime=null;
        try {
            Auth au = new Auth();
            ArrayList<Object> userInfo = au.verify(token);
            if (!(Boolean) userInfo.get(4)) {
                /**If user has logged out**/
                return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if (((Integer) userInfo.get(0)) > 0) {
                /**Insert**/
                querySQL=InsertScheduleSql;
                Object[] data=new Object[13];
                data[0]= ((String) args.get("schedule_name"));//name
                data[1]=  (Integer) userInfo.get(2);//owner
                data[2]=  (Integer.parseInt((String) args.get("schedule_level")));//level
                data[3]=  ((String) args.get("memo"));//memo
                data[4]=  0;//status
                data[5]=  "";//starttime
                data[6]=  ((String) args.get("schedule_mode"));//timetype


                if (((String) args.get("schedule_mode")).equals("interval")) {
                    data[7]=  ((String) args.get("starttwith"));//startwith
                    data[8]=  ((Integer.parseInt((String) args.get("every"))));//timeevery
                    data[9]=  ((String) args.get("unit"));//timeeverytype
                } else if (((String) args.get("schedule_mode")).equals("cycle")) {
                    data[7]=  ((String) args.get("starttwith"));//startwith
                    data[10]=  (Integer.parseInt((String) args.get("time")));//timecycle
                    data[11]=  (Integer.parseInt((String) args.get("each")));//timeeach
                } else {
                    //Insert schedule_time
                    stime = (ArrayList) args.get("mod_set");
                    singleJob = true;
                }
                if (((String) args.get("notification")).equals("1")) {
                    data[12]=  true;//notification
                } else {
                    data[12]=  false;//notification
                }
                queryBean=PStmt.buildBatchUpdateBean("kado-meta",querySQL,new ArrayList<Object[]>() {{
                    add(data);
                }});
                rs=dbClient.execute(queryBean);
                querySQL=queryBean.getSql();
                if(!rs.isSuccess())
                    throw rs.getException();


                String ScheduleID = rs.getGeneratedPKList().get(0).toString();

                //Insert schedule_job
                ArrayList sjob = (ArrayList) args.get("runjob");
                insertScheduleJob(Integer.parseInt(ScheduleID), sjob);
                if (singleJob) {
                    insertScheduleTime(Integer.parseInt(ScheduleID), stime);
                }

                return MessageFactory.rtnScheduleMessage("success", TimeUtil.getCurrentTime(), "", ScheduleID);
            } else {
                return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            }
        }catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+querySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }

    }


    /**
     * Updating schedule information in SQLite table: Schedule
     * @param args; json data: Map
     * @param ScheduleID; schedule ID: Int
     * @param token; user token: String
     * @return Success; Error: permission denied or Schedule is not exit
     * @throws SQLException
     */
    public synchronized static String updateSchedule(Map args,int ScheduleID, String token)  {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        String querySQL = "";
        try {
            if (scheduleIsExist(ScheduleID)) {
                /**if schedule is exit**/
                Boolean singleJob = false;

                ArrayList stime = null;
                Auth au = new Auth();
                ArrayList<Object> userInfo = au.verify(token);


                if (!(Boolean) userInfo.get(4)) {
                    /**User has logged out**/
                    return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
                } else if ((au.scheduleMatch(token, Integer.toString(ScheduleID))) || ((Integer) au.verify(token).get(0) == 2)) {

                    try{
                        if(ScheduleCRUDUtils.scheduleIsActivated(ScheduleID)){
                            ScheduleMgr scheduleMgr =new ScheduleMgr();
                            scheduleMgr.stopSchedule(ScheduleID, token);
                        }
                    }
                    catch (SQLException e){
                        log.warn(ExceptionUtils.getStackTrace(e));
                    }
                    deleteScheduleJob(ScheduleID);
                    deleteScheduleTime(ScheduleID);


                    querySQL = UpdateScheduleSql;

                } else {

                    return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission Denied", Integer.toString(ScheduleID));
                }
                /**INSERT SQL**/
                Object[] data=new Object[13];
                data[0]= ((String) args.get("schedule_name"));//name
                data[1]=  (Integer.parseInt((String) args.get("schedule_level")));//level
                data[2]=  ((String) args.get("memo"));//memo
                data[3]=  0;//status
                data[4]=  TimeUtil.getCurrentTime();//starttime
                data[5]=  (String) args.get("schedule_mode");//timetype
                if (((String) args.get("schedule_mode")).equals("interval")) {
                    data[6]=  ((String) args.get("starttwith"));//startwith
                    data[7]=  ((Integer.parseInt((String) args.get("every"))));//timeevery
                    data[8]=  ((String) args.get("unit"));//timeeverytype
                } else if (((String) args.get("schedule_mode")).equals("cycle")) {
                    data[6]=  ((String) args.get("starttwith"));//startwith
                    data[9]=  (Integer.parseInt((String) args.get("time")));//timecycle
                    data[10]=  (Integer.parseInt((String) args.get("each")));//timeeach
                } else {
                    //Insert schedule_time
                    stime = (ArrayList) args.get("mod_set");
                    singleJob = true;
                }
                if (((String) args.get("notification")).equals("1")) {
                    data[11]=  true;//notification
                } else {
                    data[11]=  false;//notification
                }

                data[12]=  ScheduleID;
                queryBean=PStmt.buildBatchUpdateBean("kado-meta",querySQL,new ArrayList<Object[]>() {{
                    add(data);
                }});
                rs=dbClient.execute(queryBean);
                querySQL=queryBean.getSql();
                if(!rs.isSuccess())
                    throw rs.getException();

                //Insert schedule_job
                ArrayList sjob = (ArrayList) args.get("runjob");
                insertScheduleJob(ScheduleID, sjob);
                if (singleJob) {
                    insertScheduleTime(ScheduleID, stime);
                }
                return MessageFactory.rtnScheduleMessage("success", TimeUtil.getCurrentTime(), "", Integer.toString(ScheduleID));
            } else {
                return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Schedule is not exit", Integer.toString(ScheduleID));
            }
        }catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+querySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }

    /**
     * Updating schedule status
     * @param ScheduleID; schedule ID: Int
     * @param status; schedule status; String
     * @return
     * @throws Exception
     */
    public synchronized static String updateScheduleStatus(int ScheduleID, int status) throws Exception{
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        String query="UPDATE `Schedule` SET `ScheduleStatus`=? WHERE `ScheduleID`=?";

        queryBean=PStmt.buildQueryBean("kado-meta",query,new Object[]{
                status,
                ScheduleID
        });
        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();
        return MessageFactory.rtnScheduleMessage("success",TimeUtil.getCurrentTime(),"Update success",Integer.toString(ScheduleID));
    }

    /**
     * Updating Schedule Start time
     * @param ScheduleID
     * @param time
     * @return
     * @throws Exception
     */
    public synchronized static String updateScheduleStartTime(int ScheduleID, String time) throws Exception{
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String query="UPDATE `Schedule` SET `ScheduleStartTime`=? WHERE `ScheduleID`=?";
        queryBean=PStmt.buildQueryBean("kado-meta",query,new Object[]{
                time,
                ScheduleID
        });
        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();
        return MessageFactory.rtnScheduleMessage("success",TimeUtil.getCurrentTime(),"Update success",Integer.toString(ScheduleID));
    }

    public synchronized static void deleteScheduleJob(int ScheduleID) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String querySQL="DELETE FROM `Schedule_Job` WHERE `ScheduleID`=?;";

        queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                ScheduleID
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();
    }

    public synchronized static void deleteScheduleTime(int ScheduleID) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String querySQL="DELETE FROM `main`.`Schedule_Time` WHERE `ScheduleID`=?;";

        queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                ScheduleID
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();

    }

    public synchronized static void insertScheduleJob(int ScheduleID,ArrayList input) throws Exception{
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String querySQL="INSERT INTO `Schedule_Job` (`ScheduleID`,`JobID`,`SortIndex`) VALUES (?,?,?)";

        List<Object[]> data=new ArrayList<>();
        for(int i=0;i<input.size();i++){
            data.add(new Object[]{
                    ScheduleID,
                    Integer.parseInt((String) input.get(i)),
                    i
            });
        }

        queryBean=PStmt.buildBatchUpdateBean("kado-meta",querySQL,data);
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();
    }

    public synchronized static void insertScheduleTime(int ScheduleID,ArrayList input) throws Exception{
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String querySQL="INSERT INTO `main`.`Schedule_Time` (`ScheduleID`,`Time`,`Tag`) VALUES (?,?,?)";

        List<Object[]> data=new ArrayList<>();
        for(int i=0;i<input.size();i++){
            Map arg=(Map)input.get(i);
            data.add(new Object[]{
                    ScheduleID,
                    (String) arg.get("runtime"),
                    (String) arg.get("tab")
            });
        }
        queryBean=PStmt.buildBatchUpdateBean("kado-meta",querySQL,data);
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();
    }

    public static ArrayList<Integer> getRunJob(int ScheduleID) throws Exception{
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String querySQL="SELECT * FROM Schedule_Job WHERE `ScheduleID`=? ORDER BY `SortIndex`";

        ArrayList<Integer> rtn=new ArrayList<>();

        queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                ScheduleID
        });

        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();

        for(Row row:rs.getRowList()){
            KadoRow r=new KadoRow(row);
            rtn.add(r.getInt("JobID"));
        }

        return rtn;
    }

    public static ArrayList<LinkedHashMap<String,String>> getscheduleTime(int ScheduleID)throws Exception{
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String querySQL="SELECT * FROM Schedule_Time WHERE `ScheduleID`=?";
        ArrayList<LinkedHashMap<String,String>> rtn=new ArrayList<>();

        queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                ScheduleID
        });

        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();

        for(Row row:rs.getRowList()){
            KadoRow r=new KadoRow(row);
            LinkedHashMap<String,String> element=new LinkedHashMap<>();
            element.put(r.getString("Time"),r.getString("Tag"));
            rtn.add(element);
        }
        return rtn;
    }

    public static Map<String,Map<String,Object>> getAllScheduleJobandTime()throws Exception{
        //DBClient
        Query queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String querySQL="SELECT sj.ScheduleID,sj.JobIDList,st.TimeList FROM (SELECT ScheduleID,GROUP_CONCAT(`JobID` ORDER BY SortIndex ) as JobIDList FROM Schedule_Job GROUP BY ScheduleID) sj LEFT JOIN (SELECT ScheduleID,GROUP_CONCAT(`Time`,'@',`Tag` SEPARATOR '<br>' ) as TimeList FROM Schedule_Time GROUP BY ScheduleID) st ON sj.ScheduleID=st.ScheduleID";


        queryBean=new Query("kado-meta",querySQL);

        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();
        Map<String,Map<String,Object>> result=new HashMap<>();
        for(Row row:rs.getRowList()){
            KadoRow r=new KadoRow(row);
            ArrayList<LinkedHashMap<String,String>> timeList=new ArrayList<>();
            ArrayList<Integer> jobList=new ArrayList<>();
            /*Time List*/
            if(!r.getString("TimeList").isEmpty()) {
                String timeString=r.getString("TimeList");
                String[] times=timeString.split("<br>");
                for(String time:times) {
                    String[] values=time.split("@");
                    if(values.length==2) {
                        timeList.add(new LinkedHashMap<String, String>(){{
                            put(values[0], values[1]);
                        }});
                    }
                }
            }

            if(!r.getString("JobIDList").isEmpty()){
                String jobString=r.getString("JobIDList");
                String[] jobs=jobString.split(",");
                for(String job:jobs){
                    jobList.add(Integer.valueOf(job));
                }
            }

            result.put(r.getString("ScheduleID"),new HashMap<String,Object>(){{
                put("times",timeList);
                put("jobs",jobList);
            }});
        }

        return result;
    }

    public static String getScheduleInfo(int ScheduleID) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        ArrayList <Integer> runjob=getRunJob(ScheduleID);
        ArrayList<LinkedHashMap<String,String>> scheduleTime=getscheduleTime(ScheduleID);

        queryBean=PStmt.buildQueryBean("kado-meta",GetScheduleInfoSql,new Object[]{
                ScheduleID
        });

        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();
        if(rs.getRowSize()==0){
            return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", Integer.toString(ScheduleID));
        }

        return MessageFactory.rtnScheduleInfoMessage(rs,runjob,scheduleTime);
    }

    public static String getScheduleInfo(int ScheduleID, String token) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        ArrayList <Integer> runjob=getRunJob(ScheduleID);
        ArrayList<LinkedHashMap<String,String>> scheduleTime=getscheduleTime(ScheduleID);
        String querySQL="";
        Auth au=new Auth();
        ArrayList<Object> info =au.verify(token);
        if(!(Boolean)au.verify(token).get(4)){
            return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
        }
        else if((Integer)info.get(0)>1){
            querySQL=GetScheduleInfoSql;
            queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                    ScheduleID
            });
        }else{

            querySQL=GetScheduleInfoSql_usr;
            queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                    ScheduleID,
                    (Integer)info.get(1)
            });
        }

        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();
        if(rs.getRowSize()==0){
            return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", Integer.toString(ScheduleID));
        }
        String rtn=MessageFactory.rtnScheduleInfoMessage(rs,runjob,scheduleTime);

        return rtn;
    }

    public synchronized static String deleteSchedule(int ScheduleID,String token) {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String querySQL = "";
        try {

            Auth au = new Auth();
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((au.scheduleMatch(token, Integer.toString(ScheduleID))) || ((Integer) au.verify(token).get(0) == 2)) {
                querySQL = DeleteSchedule;
                queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                        ScheduleID
                });
                querySQL=queryBean.getSql();
                rs=dbClient.execute(queryBean);

                if(!rs.isSuccess())
                    throw rs.getException();


                deleteScheduleJob(ScheduleID);
                deleteScheduleTime(ScheduleID);

                return MessageFactory.rtnScheduleMessage("success", TimeUtil.getCurrentTime(), "", Integer.toString(ScheduleID));

            } else {
                return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission Denied", Integer.toString(ScheduleID));
            }


        }
        catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+querySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }

    public static String getScheduleList(String token) {
        //DBClient
        Action queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        String querySQL = null;


        try {

            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((Integer) info.get(0) > 1) {
                querySQL = SelectAllScheduleSql;
                queryBean=new Query("kado-meta",querySQL);
            } else {
                querySQL = SelectScheduleListSql;
                queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                        (Integer) info.get(1)
                });
            }
            querySQL=queryBean.getSql();
            rs=dbClient.execute(queryBean);

            if(!rs.isSuccess())
                throw rs.getException();

            ArrayList<Map> list = new ArrayList<>();
            for(Row row:rs.getRowList()){
                KadoRow r=new KadoRow(row);
                Map json = new LinkedHashMap();
                json.put("schedule_id", r.getInt("ScheduleID"));
                json.put("schedule_name", r.getString("ScheduleName"));
                json.put("scheduleLevel", r.getInt("ScheduleLevel"));
                json.put("memo", r.getString("ScheduleMemo"));
                json.put("notification", r.getString("Notification"));
                json.put("schedule_mode", r.getString("ScheduleTimeType"));

                json.put("startwith", r.getString("StartWith"));
                json.put("every", r.getInt("TimeEvery"));
                json.put("unit", r.getString("TimeEveryType"));
                json.put("time", r.getInt("TimeCycle"));
                json.put("each", r.getInt("TimeEach"));
                if (!r.getString("ScheduleStatus").equals("0")) {
                    if (!((r.getString("ScheduleStartTime") == null) || (r.getString("ScheduleStartTime").equals("")))) {
                        try {
                            json.put("last_runtime", r.getString("ScheduleStartTime"));
                            json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("ScheduleStartTime")),
                                    TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                        } catch (NullPointerException npe) {
                            json.put("last_runtime", "");
                            if (r.getString("ScheduleStartTime") != null) {
                                json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("ScheduleStartTime")),
                                        TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                            } else {
                                json.put("runingtime", "0");
                            }
                        }
                    }
                } else {
                    json.put("last_runtime", "");
                    json.put("runingtime", "");
                }
                json.put("status", r.getString("ScheduleStatus"));
                json.put("user", r.getString("UserName"));
                json.put("userid", r.getString("UID"));
                json.put("group", (Integer) info.get(2));
                list.add(json);
            }

            //Catch Schedule Job&Time in Memory
            Map<String,Map<String,Object>> scheduleJobandTimes=getAllScheduleJobandTime();
            for (Map m : list) {
                Integer id = (Integer) m.get("schedule_id");
                if(scheduleJobandTimes.get(id)!=null){

                    ArrayList<LinkedHashMap<String,String>> timeList=scheduleJobandTimes.get(id).get("times")!=null?(ArrayList<LinkedHashMap<String,String>>)scheduleJobandTimes.get(id).get("times"):null;
                    ArrayList<Integer> jobList=scheduleJobandTimes.get(id).get("jobs")!=null?(ArrayList<Integer>)scheduleJobandTimes.get(id).get("jobs"):null;

                    m.put("runjob", jobList);
                    if (((String) m.get("schedule_mode")).equals("single")) {
                        m.put("mod_set", timeList);
                    }
                }else {
                    m.put("runjob", new ArrayList<>());
                    m.put("mod_set", new ArrayList<>());
                }
            }

            String rtn = MessageFactory.scheduleListMessage(list);

            list.clear();
            return rtn;
        }
        catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+querySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }

    public static String getScheduleStatusList(String limit,String token) {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String querySQL = "";
        try {


            int recordLimit = 100;
            if (!limit.equals("")) {
                recordLimit = Integer.parseInt(limit);
            }
            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((Integer) info.get(0) > 1) {
                querySQL = SelectAllScheduleExecutionList;

                queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                        recordLimit
                });
            } else {
                querySQL = SelectScheduleExecutionList;

                queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                        (Integer) info.get(1),
                        recordLimit
                });
            }
            //INSERT SQL

            querySQL=queryBean.getSql();
            rs=dbClient.execute(queryBean);

            if(!rs.isSuccess())
                throw rs.getException();

            ArrayList<Map> list = new ArrayList<>();
            for(Row row:rs.getRowList()){
                KadoRow r=new KadoRow(row);
                Map json = new LinkedHashMap();
                json.put("schedule_runid", r.getInt("SHID"));
                json.put("schedule_id", r.getInt("ScheduleID"));
                Map<String,ArrayList<Integer>> jobs=getScheduleRunJob(r.getInt("SHID"));
                json.put("runHistoryjob",jobs.get("runHistoryjob"));
                json.put("runjob", jobs.get("runJob"));
                if((r.getString("ScheduleTimeType")).equals("single")){
                    json.put("mod_set",getscheduleTime(r.getInt("ScheduleID")));
                }
                json.put("schedule_name", r.getString("ScheduleName"));
                json.put("schedule_Level", r.getInt("ScheduleLevel"));
                json.put("memo", r.getString("ScheduleMemo"));
                json.put("notification", r.getString("Notification"));
                json.put("schedule_mode", r.getString("ScheduleTimeType"));
                json.put("startwith", r.getString("StartWith"));
                json.put("every", r.getInt("TimeEvery"));
                json.put("unit", r.getString("TimeEveryType"));
                json.put("time", r.getInt("TimeCycle"));
                json.put("each", r.getInt("TimeEach"));
                if (!((r.getString("ScheduleStartTime") == null) || (r.getString("ScheduleStartTime").equals("")))) {
                    try {
                        json.put("last_runtime", r.getString("ScheduleStartTime"));
                        json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("ScheduleStartTime")),
                                TimeUtil.String2DateTime(r.getString("ScheduleStopTime"))));
                    } catch (NullPointerException npe) {
                        json.put("last_runtime", "");
                        if (r.getString("ScheduleStartTime") != null) {
                            json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("ScheduleStartTime")),
                                    TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                        } else {
                            json.put("runingtime", "0");
                        }
                    }
                }
                json.put("user", r.getString("UserName"));
                json.put("userid", r.getString("UID"));
                json.put("group", (Integer) info.get(2));


                list.add(json);

            }




            String rtn = MessageFactory.scheduleListMessage(list);

            return rtn;


        }catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+querySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }

    public static String getScheduleHistoryList(String start, String stop,String ScheduleID,String token){
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String querySQL="";
        try{

            Auth au=new Auth();
            ArrayList<Object> info =au.verify(token);
            //Boolean admin=(Boolean)info.get(0);
            if(!((Boolean)info.get(4))){
                return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission Denied", ScheduleID);
            }
            else if(!(start.equals("")||stop.equals("")||ScheduleID.equals(""))){
                if((Integer)info.get(0)>1) {
                    querySQL = SelectHistoryScheduleList_timeandScheduleId;
                    queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                            start,
                            stop,
                            Integer.parseInt(ScheduleID)
                    });
                }
                else{
                    querySQL = SelectHistoryScheduleList_timeandScheduleId_user;
                    queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                            start,
                            stop,
                            Integer.parseInt(ScheduleID),
                            (Integer)info.get(1)
                    });
                }
            }
            else if(!ScheduleID.equals(""))
            {
                if((Integer)info.get(0)>1) {
                    querySQL = SelectHistoryScheduleList_ScheduleID;
                    queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                            Integer.parseInt(ScheduleID)
                    });
                }
                else{
                    querySQL = SelectHistoryScheduleList_ScheduleID_user;
                    queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                            Integer.parseInt(ScheduleID),
                            (Integer)info.get(1)
                    });
                }
            }
            else if(!(start.equals("")||stop.equals(""))){
                if((Integer)info.get(0)>1) {
                    querySQL = SelectHistoryScheduleList_time;
                    queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                            start,
                            stop
                    });
                }
                else{
                    querySQL = SelectHistoryScheduleList_time_user;
                    queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                            start,
                            stop,
                            Integer.parseInt(ScheduleID),
                            (Integer)info.get(1)
                    });
                }
            }
            else{
                DateTime dt=new DateTime();
                return MessageFactory.rtnScheduleMessage("error", dt.toString("yyyy-MM-dd HH:mm:ss.SSS"), "illegal parameter", ScheduleID);
            }
            querySQL=queryBean.getSql();
            rs=dbClient.execute(queryBean);

            if(!rs.isSuccess())
                throw rs.getException();

            ArrayList<Map> list=new ArrayList<>();
            ArrayList<Integer> runJob=new ArrayList<>();

            for(Row row:rs.getRowList()){
                KadoRow r=new KadoRow(row);
                Map json=new LinkedHashMap();
                json.put("schedule_runid",r.getInt("SHID"));
                json.put("schedule_id",r.getInt("ScheduleID"));
                Map<String,ArrayList<Integer>> jobs=getScheduleRunJob(r.getInt("SHID"));
                json.put("runHistoryjob",jobs.get("runHistoryjob"));
                json.put("runjob", jobs.get("runJob"));
                if((r.getString("ScheduleTimeType")).equals("single")){
                    json.put("mod_set",getscheduleTime(r.getInt("ScheduleID")));
                }

                json.put("schedule_name",r.getString("ScheduleName"));
                json.put("schedule_Level",r.getInt("ScheduleLevel"));
                json.put("memo",r.getString("ScheduleMemo"));
                json.put("notification",r.getString("Notification"));
                json.put("schedule_status",r.getString("ScheduleStatus"));
                json.put("schedule_mode",r.getString("ScheduleTimeType"));

                json.put("startwith", r.getString("StartWith"));
                json.put("every",r.getInt("TimeEvery"));
                json.put("unit",r.getString("TimeEveryType"));
                json.put("time",r.getInt("TimeCycle"));
                json.put("each",r.getInt("TimeEach"));
                json.put("start_time",r.getString("RScheduleStartTime"));
                json.put("stop_time",r.getString("ScheduleStopTime"));
                if(!((r.getString("RScheduleStartTime")==null)||(r.getString("RScheduleStartTime").equals(""))) ){
                    try {
                        json.put("last_runtime", r.getString("RScheduleStartTime"));
                        json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("RScheduleStartTime")),
                                TimeUtil.String2DateTime(r.getString("ScheduleStopTime"))));
                    } catch (NullPointerException npe) {
                        json.put("last_runtime", "");
                        if (r.getString("RScheduleStartTime") != null) {
                            json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("RScheduleStartTime")),
                                    TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                        } else {
                            json.put("runingtime", "0");
                        }
                    }catch (IllegalArgumentException e){
                        json.put("last_runtime", "");
                        if (r.getString("RScheduleStartTime") != null) {
                            json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("RScheduleStartTime")),
                                    TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                        } else {
                            json.put("runingtime", "0");
                        }
                    }
                }
                json.put("user", r.getString("UserName"));
                json.put("userid",r.getString("UID"));
                json.put("group", (Integer) info.get(2));



                list.add(json);

            }

            String rtn=MessageFactory.scheduleListMessage(list);
            return rtn;
        }catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+querySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }

    }

    public static String getScheduleHistoryInfo(String token, int runid){
        //Schedule join Schedule history
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String querySQL="";
        try{
            Gson gson = new Gson();

            Auth au=new Auth();
            ArrayList<Object> info =au.verify(token);
            if(!(Boolean)au.verify(token).get(4)){
                return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            }
            else if((Integer)info.get(0)>1){
                querySQL=SelectScheduleHistoryInfo;
                queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                        runid
                });
            }else{
                querySQL=SelectScheduleHistoryInfo_user;
                queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                        runid,
                        (Integer) info.get(1)
                });
            }
            //INSERT SQL
            querySQL=queryBean.getSql();
            rs=dbClient.execute(queryBean);

            if(!rs.isSuccess())
                throw rs.getException();

            Map json=new LinkedHashMap();

            if(rs.getRowSize()==0){
                json.put("status","fail");
                json.put("message","not found");
                return gson.toJson(json);
            }

            KadoRow r=new KadoRow(rs.getRowList().get(0));

            json.put("status","success");
            json.put("Currenttime",TimeUtil.getCurrentTime());
            json.put("schedule_runid",r.getInt("SHID"));
            json.put("schedule_id",r.getInt("ScheduleID"));
            json.put("schedule_name",r.getString("ScheduleName"));
            json.put("schedule_Level",r.getInt("ScheduleLevel"));
            json.put("memo",r.getString("ScheduleMemo"));
            json.put("notification",r.getString("Notification"));
            json.put("schedule_status",r.getString("ScheduleStatus"));
            json.put("schedule_mode",r.getString("ScheduleTimeType"));
            json.put("startwith", r.getString("StartWith"));
            json.put("every",r.getInt("TimeEvery"));
            json.put("unit",r.getString("TimeEveryType"));
            json.put("time",r.getInt("TimeCycle"));
            json.put("each",r.getInt("TimeEach"));
            json.put("start_time",r.getString("ScheduleStatTime"));
            json.put("stop_time",r.getString("ScheduleStopTime"));
            try{
                json.put("last_runtime",r.getString("ScheduleStartTime"));
                json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("ScheduleStartTime")),
                        TimeUtil.String2DateTime(r.getString("ScheduleStopTime"))));
            }

            catch(NullPointerException npe){
                json.put("last_runtime", "");
                if(r.getString("ScheduleStartTime")!=null) {
                    json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("ScheduleStartTime")),
                            TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                }else {
                    json.put("runingtime","0");
                }
            }
            json.put("user",r.getString("UserName"));
            json.put("userid", r.getString("UID"));
            json.put("group", (Integer) info.get(2));


            Map<String,ArrayList<Integer>> jobs=getScheduleRunJob(r.getInt("SHID"));
            json.put("runHistoryjob",jobs.get("runHistoryjob"));
            json.put("runjob", jobs.get("runJob"));
            if((r.getString("ScheduleTimeType")).equals("single")){
                json.put("mod_set",getscheduleTime(r.getInt("ScheduleID")));
            }
            return gson.toJson(json);
        }catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+querySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }

    }

    public static Map<String,ArrayList<Integer>> getScheduleRunJob(int SHID) throws Exception{
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String querySQL="SELECT JHID,JobID FROM Schedule_Job_History WHERE `SHID`=? ORDER BY `SortIndex`";

        ArrayList<Integer> runJob=new ArrayList<>();
        ArrayList<Integer> runHistoryjob=new ArrayList<>();

        queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                SHID
        });

        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();

        for(Row row:rs.getRowList()){
            KadoRow r=new KadoRow(row);
            runHistoryjob.add(r.getInt("JHID"));
            runJob.add(r.getInt("JobID"));
        }
        Map rtn=new HashMap();
        rtn.put("runHistoryjob",runHistoryjob);
        rtn.put("runJob",runJob);
        return rtn;
    }

    public synchronized static int insertScheduleJobHistory(int ShceduleHistoryId,int JobhistoryID,int JobID,int SortIndex) throws Exception{
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        int SJHID=-1;
        String querySQL = "INSERT INTO `Schedule_Job_History` (`SHID`,`JHID`,`JobID`,`SortIndex`) VALUES (?,?,?,?);";

        //INSERT SQL
        queryBean=PStmt.buildBatchUpdateBean("kado-meta",querySQL,new ArrayList<Object[]>(){{
            add(new Object[]{
                    ShceduleHistoryId,
                    JobhistoryID,
                    JobID,
                    SortIndex
            });
        }});
        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();

        SJHID=rs.getGeneratedPKList().get(0).intValue();

        return SJHID;
    }

    public synchronized static int insertScheduleHistory(ScheduleHistory scheduleHistoryBean) throws Exception{
        int ScheduleHistoryID=-1;

        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        //INSERT SQL
        queryBean=PStmt.buildBatchUpdateBean("kado-meta",InsertScheduleHistorySql,new ArrayList<Object[]>(){{
            add(new Object[]{
                    scheduleHistoryBean.getScheduleID(),
                    scheduleHistoryBean.getScheduleName(),
                    scheduleHistoryBean.getScheduleOwner(),
                    scheduleHistoryBean.getScheduleLevel(),
                    scheduleHistoryBean.getScheduleMemo(),
                    scheduleHistoryBean.getScheduleStatus(),
                    scheduleHistoryBean.getScheduleStartTime(),
                    scheduleHistoryBean.getScheduleStopTime(),
                    scheduleHistoryBean.getScheduleLog(),
                    scheduleHistoryBean.getScheduleTimeType(),
                    scheduleHistoryBean.getStartWith(),
                    scheduleHistoryBean.getTimeEvery(),
                    scheduleHistoryBean.getTimeEveryType(),
                    scheduleHistoryBean.getTimeCycle(),
                    scheduleHistoryBean.getTimeEach(),
                    scheduleHistoryBean.getNotification()

            });
        }});
        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();

        ScheduleHistoryID=rs.getGeneratedPKList().get(0).intValue();
        return ScheduleHistoryID;
    }

    public synchronized static int getUserLevel(int UserID) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String querySQL = "SELECT `User`.`Admin`,`User`.`General` From `User`  WHERE  `User`.`UID`=?;";

        int rtn = Integer.MIN_VALUE;

        queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                UserID
        });

        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();

        for(Row row:rs.getRowList()){
            KadoRow r=new KadoRow(row);
            if (r.getBoolean("Admin")) {
                rtn = PrestoContent.ADMIN;
            } else if (r.getBoolean("General")) {
                rtn = PrestoContent.GENERAL;
            } else {
                rtn = PrestoContent.MANAGER;
            }
        }
        return rtn;
    }

    public synchronized static int insertScheduleHistory(ArrayList<String> args) throws Exception{
        int ScheduleHistoryID=-1;

        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        //INSERT SQL
        queryBean=PStmt.buildBatchUpdateBean("kado-meta",InsertScheduleHistorySql,new ArrayList<Object[]>(){{
            add(new Object[]{
                    Integer.parseInt(args.get(0)),//ScheduleID
                    args.get(1),//ScheduleName
                    Integer.parseInt(args.get(2)),//ScheduleOwner
                    Integer.parseInt(args.get(3)),//ScheduleLevel
                    args.get(4),//memo
                    Integer.parseInt(args.get(5)),//status
                    args.get(6),//ScheduleStartTime
                    args.get(7),//ScheduleStopTime
                    args.get(8),//ScheduleLog
                    args.get(9),//ScheduleTimeType
                    args.get(10),//startWith
                    Integer.parseInt(args.get(11)),//timeEvery
                    args.get(12),//timeEveryType
                    Integer.parseInt(args.get(13)),//timeCycle
                    args.get(14),//timeEach
                    Boolean.getBoolean(args.get(15))//notification
            });
        }});
        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();

        ScheduleHistoryID=rs.getGeneratedPKList().get(0).intValue();
        return ScheduleHistoryID;
    }

    public synchronized static void updateScheduleHistory(int ScheduleHistoryID, String ScheduleStopTime, int ScheduleStatus, String logPath)throws Exception{

        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        queryBean=PStmt.buildQueryBean("kado-meta",UpdateScheduleHistorySql,new Object[]{
                ScheduleStopTime,
                ScheduleStatus,
                logPath,
                ScheduleHistoryID
        });

        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();

    }

    public static boolean scheduleIsExist(int ScheduleID) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        //Check User Account and Password
        String sql = "select `ScheduleName` from `Schedule` where  ScheduleID=?";

        queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                ScheduleID
        });
        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();

        return rs.getRowSize()>0;
    }

    public static boolean scheduleIsActivated(int ScheduleID) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        //Check User Account and Password
        String sql = "select `ScheduleName` from `Schedule` where  ScheduleID=? and ScheduleStatus=1";

        queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                ScheduleID
        });
        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();

        return rs.getRowSize()>0;
    }

}