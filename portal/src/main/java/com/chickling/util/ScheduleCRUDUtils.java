package com.chickling.util;

import com.chickling.bean.schedule.ScheduleHistory;
import com.chickling.sqlite.ReadOnlyConnectionManager;
import com.google.gson.Gson;
import com.chickling.sqlite.ConnectionManager;
import com.chickling.schedule.ScheduleMgr;
import com.chickling.models.Auth;
import com.chickling.models.MessageFactory;
import com.chickling.models.job.PrestoContent;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by jw6v on 2015/12/8.
 */
public class ScheduleCRUDUtils {

    private final static String InsertScheduleSql="INSERT INTO `main`.`Schedule` (`ScheduleName`,`ScheduleOwner`,`ScheduleLevel`,`ScheduleMemo`," +
            "`ScheduleStatus`,`ScheduleStartTime`,`ScheduleTimeType`,`StartWith`,`TimeEvery`,`TimeEveryType`,`TimeCycle`,`TimeEach`,`Notification`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);";
    private final static String UpdateScheduleSql="UPDATE `main`.`Schedule` SET `ScheduleName`=?,`ScheduleLevel`=?,`ScheduleMemo`=?,`ScheduleStatus`=?" +
            ", `ScheduleStartTime`=?,`ScheduleTimeType`=?,`StartWith`=?,`TimeEvery`=?,`TimeEveryType`=?,`TimeCycle`=?,`TimeEach`=?,`Notification`=? WHERE `ScheduleID`=?;";
    private final static String UpdateScheduleHistorySql="UPDATE `main`.`Schedule_History` SET `ScheduleStopTime`=?,`ScheduleStatus`=?,`ScheduleLog`=? WHERE `SHID`=?;";
    private final static String InsertScheduleHistorySql="INSERT INTO `main`.`Schedule_History` (`ScheduleID`,`ScheduleName`,`ScheduleOwner`,`ScheduleLevel`,`ScheduleMemo`,`ScheduleStatus`,`ScheduleStartTime`,`ScheduleStopTime`," +
            "`ScheduleLog`,`ScheduleTimeType`,`StartWith`,`TimeEvery`,`TimeEveryType`,`TimeCycle`,`TimeEach`,`Notification`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
    private final static String GetScheduleInfoSql="SELECT * FROM `main`.`Schedule` WHERE `ScheduleID`=?;";
    private final static String GetScheduleInfoSql_usr="SELECT *, ScheduleOwner UID FROM `main`.`Schedule` WHERE `ScheduleID`=? and ( UID in (Select UID From User WHERE Gid=?) or  ScheduleLevel=1);";
    private final static String DeleteSchedule="DELETE FROM `main`.`Schedule` WHERE `ScheduleID`=?;";
    private final static String CheckScheduleID="SELECT * FROM `Schedule` WHERE `ScheduleID`=?;";
    private final static String SelectAllScheduleSql= "SELECT * FROM(SELECT *, j.ScheduleOwner UID FROM Schedule j LEFT JOIN (SELECT *, Max(ScheduleStartTime) FROM Schedule_History WHERE ScheduleStatus = 1 group by ScheduleID) jh on j.ScheduleID=jh.ScheduleID ) jl ,User u WHERE u.UID=jl.UID;";
    private final static String SelectScheduleListSql ="SELECT * FROM(SELECT *, j.ScheduleOwner UID FROM Schedule j LEFT JOIN (SELECT *, Max(ScheduleStartTime) FROM Schedule_History WHERE ScheduleStatus=1 group by ScheduleID) jh on j.ScheduleID=jh.ScheduleID ) jl,User u WHERE jl.UID=u.UID AND (jl.UID in (Select UID From User WHERE Gid=?) or  ScheduleLevel=1);";
    private final static String SelectScheduleExecutionList="SELECT * FROM (SELECT *,j.ScheduleOwner UID FROM Schedule_History jh INNER JOIN Schedule j ON j.ScheduleID=jh.ScheduleID WHERE UID in (Select UID From User WHERE Gid=?) or  j.ScheduleLevel=1 ORDER BY ScheduleStartTime DESC limit ?) jhr,User u WHERE u.UID=jhr.ScheduleOwner;";
    private final static String SelectAllScheduleExecutionList="SELECT * FROM (SELECT *,j.ScheduleOwner UID FROM Schedule_History jh INNER JOIN Schedule j ON jh.ScheduleID=j.ScheduleID ORDER BY jh.ScheduleStartTime DESC limit ?) jhr,User u WHERE jhr.ScheduleOwner =u.UID;";
    private final static String SelectHistoryScheduleList_time_user="SELECT * FROM (SELECT *, Schedule_History.ScheduleOwner UID FROM Schedule INNER JOIN Schedule_History WHERE  Schedule_History.ScheduleStartTime>? and Schedule_History.ScheduleStopTime<? and (UID in (Select UID From User WHERE Gid=?) or  Schedule_History.ScheduleLevel=1)) jhr,User u WHERE jhr.ScheduleOwner =u.UID;";
    private final static String SelectHistoryScheduleList_ScheduleID_user="SELECT * FROM (SELECT *,  Schedule_History.ScheduleOwner UID,Schedule_History.ScheduleStartTime RScheduleStartTime FROM Schedule INNER JOIN Schedule_History ON Schedule.ScheduleID=Schedule_History.ScheduleID WHERE Schedule_History.ScheduleID=? and (UID in (Select UID From User WHERE Gid=?) or Schedule.ScheduleLevel=1)) shl,User u WHERE shl.UID =u.UID;";
    private final static String SelectHistoryScheduleList_timeandScheduleId_user="SELECT * FROM (SELECT *, Schedule_History.ScheduleOwner UID FROM Schedule INNER JOIN Schedule_History WHERE Schedule_History.ScheduleStartTime>? and Schedule_History.ScheduleStopTime<? and Schedule_History.ScheduleID=? and(UID in (Select UID From User WHERE Gid=?) or Schedule.ScheduleLevel=1)) shl,User u WHERE shl.UID =u.UID ;";
    private final static String SelectHistoryScheduleList_time="SELECT * FROM (SELECT *, Schedule_History.ScheduleOwner UID,Schedule_History.ScheduleStartTime RScheduleStartTime FROM Schedule INNER JOIN Schedule_History ON Schedule.ScheduleID=Schedule_History.ScheduleID WHERE Schedule_History.ScheduleStartTime>? and Schedule_History.ScheduleStopTime<? ) shl,User u WHERE shl.UID =u.UID;";
    private final static String SelectHistoryScheduleList_ScheduleID="SELECT * FROM (SELECT *,  Schedule_History.ScheduleOwner UID,Schedule_History.ScheduleStartTime RScheduleStartTime FROM Schedule INNER JOIN Schedule_History ON Schedule.ScheduleID=Schedule_History.ScheduleID WHERE Schedule.ScheduleID=?) shl,User u WHERE shl.UID =u.UID;";
    private final static String SelectHistoryScheduleList_timeandScheduleId="SELECT * FROM (SELECT *, Schedule_History.ScheduleOwner UID,Schedule_History.ScheduleStartTime RScheduleStartTime FROM Schedule INNER JOIN Schedule_History ON Schedule.ScheduleID=Schedule_History.ScheduleID WHERE Schedule_History.ScheduleStartTime>? and Schedule_History.ScheduleStopTime<? and Schedule.ScheduleID=?) shl,User u WHERE shl.UID =u.UID;";
    private final static String SelectScheduleHistoryInfo="SELECT * FROM (SELECT *,Schedule_History.ScheduleOwner UID,Schedule_History.ScheduleStartTime RScheduleStartTime FROM Schedule_History INNER JOIN Schedule WHERE SHID=?) shl,User u WHERE shl.UID =u.UID;";
    private final static String SelectScheduleHistoryInfo_user="SELECT * FROM (SELECT *,Schedule_History.ScheduleOwner UID,Schedule_History.ScheduleStartTime RScheduleStartTime FROM Schedule_History INNER JOIN Schedule WHERE SHID=? and (UID in (Select UID From User WHERE Gid=?) or Schedule.ScheduleLevel=1)) shl,User u WHERE shl.UID =u.UID;";

    private static Logger log = LogManager.getLogger(ScheduleCRUDUtils.class);
    /**
     * Adding schedule information in SQLite table: Schedule
     * @param args; json data: Map
     * @param token; user token: String
     * @return Success or Error, since permission denied
     * @throws SQLException
     */
    public synchronized static String addSchedule(Map args, String token) {
        PreparedStatement stat = null;
        String QuerySQL="";
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
                QuerySQL=InsertScheduleSql;
                ResultSet rs = null;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                stat.setString(1, ((String) args.get("schedule_name")));//name
                stat.setInt(2, (Integer) userInfo.get(2));//owner
                stat.setInt(3, (Integer.parseInt((String) args.get("schedule_level"))));//level
                stat.setString(4, ((String) args.get("memo")));//memo
                stat.setInt(5, 0);//status
                stat.setString(6, "");//starttime
                stat.setString(7, ((String) args.get("schedule_mode")));//timetype


                if (((String) args.get("schedule_mode")).equals("interval")) {
                    stat.setString(8, ((String) args.get("starttwith")));//startwith
                    stat.setInt(9, ((Integer.parseInt((String) args.get("every")))));//timeevery
                    stat.setString(10, ((String) args.get("unit")));//timeeverytype
                } else if (((String) args.get("schedule_mode")).equals("cycle")) {
                    stat.setString(8, ((String) args.get("starttwith")));//startwith
                    stat.setInt(11, (Integer.parseInt((String) args.get("time"))));//timecycle
                    stat.setInt(12, (Integer.parseInt((String) args.get("each"))));//timeeach
                } else {
                    //Insert schedule_time
                    stime = (ArrayList) args.get("mod_set");
                    singleJob = true;
                }
                if (((String) args.get("notification")).equals("1")) {
                    stat.setBoolean(13, true);//notification
                } else {
                    stat.setBoolean(13, false);//notification
                }
                QuerySQL=stat.toString();
                stat.executeUpdate();

                String ScheduleID = Integer.toString(stat.getGeneratedKeys().getInt(1));
                stat.close();
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
        }catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
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
        PreparedStatement stat = null;
        String QuerySQL = "";
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


                    QuerySQL = UpdateScheduleSql;
                    stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                } else {

                    return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission Denied", Integer.toString(ScheduleID));
                }
                /**INSERT SQL**/
                stat.setString(1, ((String) args.get("schedule_name")));//name
                stat.setInt(2, (Integer.parseInt((String) args.get("schedule_level"))));//level
                stat.setString(3, ((String) args.get("memo")));//memo
                stat.setInt(4, 0);//status
                stat.setString(5, TimeUtil.getCurrentTime());//starttime
                stat.setString(6, (String) args.get("schedule_mode"));//timetype
                if (((String) args.get("schedule_mode")).equals("interval")) {
                    stat.setString(7, ((String) args.get("starttwith")));//startwith
                    stat.setInt(8, ((Integer.parseInt((String) args.get("every")))));//timeevery
                    stat.setString(9, ((String) args.get("unit")));//timeeverytype
                } else if (((String) args.get("schedule_mode")).equals("cycle")) {
                    stat.setString(7, ((String) args.get("starttwith")));//startwith
                    stat.setInt(10, (Integer.parseInt((String) args.get("time"))));//timecycle
                    stat.setInt(11, (Integer.parseInt((String) args.get("each"))));//timeeach
                } else {
                    //Insert schedule_time
                    stime = (ArrayList) args.get("mod_set");
                    singleJob = true;
                }
                if (((String) args.get("notification")).equals("1")) {
                    stat.setBoolean(12, true);//notification
                } else {
                    stat.setBoolean(12, false);//notification
                }

                stat.setInt(13, ScheduleID);

                QuerySQL=stat.toString();
                stat.executeUpdate();

                stat.close();
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
        }catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }

    /**
     * Updating schedule status
     * @param ScheduleID; schedule ID: Int
     * @param status; schedule status; String
     * @return
     * @throws SQLException
     */
    public synchronized static String updateScheduleStatus(int ScheduleID, int status) throws SQLException{
        PreparedStatement stat = null;
        String query="UPDATE `main`.`Schedule` SET `ScheduleStatus`=? WHERE `ScheduleID`=?";
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(query);
        stat.setInt(1, status);
        stat.setInt(2, ScheduleID);
        stat.executeUpdate();
        return MessageFactory.rtnScheduleMessage("success",TimeUtil.getCurrentTime(),"Update success",Integer.toString(ScheduleID));
    }

    /**
     * Updating Schedule Start time
     * @param ScheduleID
     * @param time
     * @return
     * @throws SQLException
     */
    public synchronized static String updateScheduleStartTime(int ScheduleID, String time) throws SQLException{
        PreparedStatement stat = null;
        String query="UPDATE `main`.`Schedule` SET `ScheduleStartTime`=? WHERE `ScheduleID`=?";
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(query);
        stat.setString(1, time);
        stat.setInt(2, ScheduleID);
        stat.executeUpdate();
        return MessageFactory.rtnScheduleMessage("success",TimeUtil.getCurrentTime(),"Update success",Integer.toString(ScheduleID));
    }

    public synchronized static void deleteScheduleJob(int ScheduleID) {
        PreparedStatement stat = null;

        String QuerySQL="DELETE FROM `main`.`Schedule_Job` WHERE `ScheduleID`=?;";
        try {
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
            stat.setInt(1,ScheduleID);
            stat.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public synchronized static void deleteScheduleTime(int ScheduleID){
        PreparedStatement stat = null;

        String QuerySQL="DELETE FROM `main`.`Schedule_Time` WHERE `ScheduleID`=?;";
        try {
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
            stat.setInt(1,ScheduleID);
            stat.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public synchronized static void insertScheduleJob(int ScheduleID,ArrayList input) throws SQLException{
        PreparedStatement stat = null;

        String QuerySQL="INSERT INTO `main`.`Schedule_Job` (`ScheduleID`,`JobID`,`SortIndex`) VALUES (?,?,?)";
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
        for(int i=0;i<input.size();i++){
            stat.setInt(1,ScheduleID);
            stat.setInt(2, Integer.parseInt((String) input.get(i)));
            stat.setInt(3, i);
            stat.executeUpdate();
        }
        stat.close();
    }

    public synchronized static void insertScheduleTime(int ScheduleID,ArrayList input) throws SQLException{
        PreparedStatement stat = null;

        String QuerySQL="INSERT INTO `main`.`Schedule_Time` (`ScheduleID`,`Time`,`Tag`) VALUES (?,?,?)";
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
        for(int i=0;i<input.size();i++){
            stat.setInt(1, ScheduleID);
            Map arg=(Map)input.get(i);
            stat.setString(2, (String) arg.get("runtime"));
            stat.setString(3, (String) arg.get("tab"));
            stat.executeUpdate();
        }
        stat.close();
    }

    public static ArrayList<Integer> getRunJob(int ScheduleID) throws SQLException{
        String QuerySQL="SELECT * FROM Schedule_Job WHERE `ScheduleID`=? ORDER BY `SortIndex`";
        PreparedStatement stat = null;
        ResultSet rs = null;
        ArrayList<Integer> rtn=new ArrayList<>();
        stat=ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
        stat.setInt(1,ScheduleID);
        rs=stat.executeQuery();
        while(rs.next()){
            rtn.add(rs.getInt("JobId"));
        }
        stat.close();
        rs.close();
        return rtn;
    }

    public static ArrayList<LinkedHashMap<String,String>> getscheduleTime(int ScheduleID)throws SQLException{
        String QuerySQL="SELECT * FROM Schedule_Time WHERE `ScheduleID`=?";
        PreparedStatement stat = null;
        ResultSet rs = null;
        ArrayList<LinkedHashMap<String,String>> rtn=new ArrayList<>();
        stat=ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
        stat.setInt(1,ScheduleID);
        rs=stat.executeQuery();
        while(rs.next()){
            LinkedHashMap<String,String> element=new LinkedHashMap<>();
            element.put(rs.getString("Time"),rs.getString("Tag"));
            rtn.add(element);
        }
        return rtn;
    }

    public static String getScheduleInfo(int ScheduleID) throws SQLException {
        PreparedStatement stat = null;
        ResultSet rs = null;
        ArrayList <Integer> runjob=getRunJob(ScheduleID);
        ArrayList<LinkedHashMap<String,String>> scheduleTime=getscheduleTime(ScheduleID);
        String QuerySQL="";
        QuerySQL=GetScheduleInfoSql;
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
        stat.setInt(1,ScheduleID);
        rs=stat.executeQuery();
        if(!rs.next()){
            return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", Integer.toString(ScheduleID));
        }
        String rtn=MessageFactory.rtnScheduleInfoMessage(rs,runjob,scheduleTime);
        stat.close();
        return rtn;
    }

    public static String getScheduleInfo(int ScheduleID, String token) throws SQLException {
        PreparedStatement stat = null;
        ResultSet rs = null;
        ArrayList <Integer> runjob=getRunJob(ScheduleID);
        ArrayList<LinkedHashMap<String,String>> scheduleTime=getscheduleTime(ScheduleID);
        String QuerySQL="";
        Auth au=new Auth();
        ArrayList<Object> info =au.verify(token);
        if(!(Boolean)au.verify(token).get(4)){
            return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
        }
        else if((Integer)info.get(0)>1){
            QuerySQL=GetScheduleInfoSql;
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
            stat.setInt(1,ScheduleID);
        }else{

            QuerySQL=GetScheduleInfoSql_usr;
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
            stat.setInt(1, ScheduleID);
            stat.setInt(2,(Integer)info.get(1));
        }

        rs=stat.executeQuery();
        if(!rs.next()){
            return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", Integer.toString(ScheduleID));
        }
        String rtn=MessageFactory.rtnScheduleInfoMessage(rs,runjob,scheduleTime);
        stat.close();
        return rtn;
    }

    public synchronized static String deleteSchedule(int ScheduleID,String token) {
        PreparedStatement stat = null;

        String QuerySQL = "";
        try {

            Auth au = new Auth();
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((au.scheduleMatch(token, Integer.toString(ScheduleID))) || ((Integer) au.verify(token).get(0) == 2)) {
                QuerySQL = DeleteSchedule;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);

            } else {
                return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission Denied", Integer.toString(ScheduleID));
            }

            stat.setInt(1, ScheduleID);

            QuerySQL=stat.toString();
            stat.execute();
            stat.close();

            deleteScheduleJob(ScheduleID);
            deleteScheduleTime(ScheduleID);

            return MessageFactory.rtnScheduleMessage("success", TimeUtil.getCurrentTime(), "", Integer.toString(ScheduleID));
        }
        catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }

    public static String getScheduleList(String token) {

        ReadOnlyConnectionManager rocm=new ReadOnlyConnectionManager();
        String QuerySQL = null;
        PreparedStatement stat = null;
        ResultSet rs = null;

        try {

            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((Integer) info.get(0) > 1) {
                QuerySQL = SelectAllScheduleSql;
                stat = rocm.getConnection().prepareStatement(QuerySQL);
            } else {
                QuerySQL = SelectScheduleListSql;
                stat = rocm.getConnection().prepareStatement(QuerySQL);
                stat.setInt(1, (Integer) info.get(1));
            }
            QuerySQL=stat.toString();
            rs = stat.executeQuery();
            ArrayList<Map> list = new ArrayList<>();
            while (rs.next()) {
                Map json = new LinkedHashMap();
                json.put("schedule_id", rs.getInt("ScheduleID"));
                json.put("schedule_name", rs.getString("ScheduleName"));
                json.put("scheduleLevel", rs.getInt("ScheduleLevel"));
                json.put("memo", rs.getString("ScheduleMemo"));
                json.put("notification", rs.getString("Notification"));
                json.put("schedule_mode", rs.getString("ScheduleTimeType"));

                json.put("startwith", rs.getString("StartWith"));
                json.put("every", rs.getInt("TimeEvery"));
                json.put("unit", rs.getString("TimeEveryType"));
                json.put("time", rs.getInt("TimeCycle"));
                json.put("each", rs.getInt("TimeEach"));
                if (!rs.getString("ScheduleStatus").equals("0")) {
                    if (!((rs.getString("ScheduleStartTime") == null) || (rs.getString("ScheduleStartTime").equals("")))) {
                        try {
                            json.put("last_runtime", rs.getString("ScheduleStartTime"));
                            json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("ScheduleStartTime")),
                                    TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                        } catch (NullPointerException npe) {
                            json.put("last_runtime", "");
                            if (rs.getString("ScheduleStartTime") != null) {
                                json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("ScheduleStartTime")),
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
                json.put("status", rs.getString("ScheduleStatus"));
                json.put("user", rs.getString("UserName"));
                json.put("userid", rs.getString("UID"));
                json.put("group", (Integer) info.get(2));
                list.add(json);
            }

            stat.close();
            rs.close();
            for (Map m : list) {
                int id = (Integer) m.get("schedule_id");
                m.put("runjob", getRunJob(id));
                if (((String) m.get("schedule_mode")).equals("single")) {
                    m.put("mod_set", getscheduleTime(id));
                }
            }

            String rtn = MessageFactory.scheduleListMessage(list);


            list.clear();
            rocm.close();
            return rtn;
        }
        catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }finally {

        }
    }

    public static String getScheduleStatusList(String limit,String token) {
        PreparedStatement stat = null;
        ResultSet rs = null;

        String QuerySQL = "";
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
                QuerySQL = SelectAllScheduleExecutionList;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                stat.setInt(1, recordLimit);
            } else {
                QuerySQL = SelectScheduleExecutionList;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                stat.setInt(1, (Integer) info.get(1));
                stat.setInt(2, recordLimit);
            }
            //INSERT SQL

            QuerySQL=stat.toString();
            rs = stat.executeQuery();
            ArrayList<Map> list = new ArrayList<>();
            while (rs.next()) {
                Map json = new LinkedHashMap();
                json.put("schedule_runid", rs.getInt("SHID"));
                json.put("schedule_id", rs.getInt("ScheduleID"));
                json.put("schedule_name", rs.getString("ScheduleName"));
                json.put("schedule_Level", rs.getInt("ScheduleLevel"));
                json.put("memo", rs.getString("ScheduleMemo"));
                json.put("notification", rs.getString("Notification"));
                json.put("schedule_mode", rs.getString("ScheduleTimeType"));
                json.put("startwith", rs.getString("StartWith"));
                json.put("every", rs.getInt("TimeEvery"));
                json.put("unit", rs.getString("TimeEveryType"));
                json.put("time", rs.getInt("TimeCycle"));
                json.put("each", rs.getInt("TimeEach"));
                if (!((rs.getString("ScheduleStartTime") == null) || (rs.getString("ScheduleStartTime").equals("")))) {
                    try {
                        json.put("last_runtime", rs.getString("ScheduleStartTime"));
                        json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("ScheduleStartTime")),
                                TimeUtil.String2DateTime(rs.getString("ScheduleStopTime"))));
                    } catch (NullPointerException npe) {
                        json.put("last_runtime", "");
                        if (rs.getString("ScheduleStartTime") != null) {
                            json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("ScheduleStartTime")),
                                    TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                        } else {
                            json.put("runingtime", "0");
                        }
                    }
                }
                json.put("user", rs.getString("UserName"));
                json.put("userid", rs.getString("UID"));
                json.put("group", (Integer) info.get(2));


                list.add(json);

            }
            stat.close();

            for (Map m : list) {
                int id = (Integer) m.get("schedule_id");
                m.put("runjob", getRunJob(id));
                int hid = (Integer) m.get("schedule_runid");
                m.put("runHistoryjob", getScheduleRunJob(hid));
                if (((String) m.get("schedule_mode")).equals("single")) {
                    m.put("mod_set", getscheduleTime(id));
                }
            }


            String rtn = MessageFactory.scheduleListMessage(list);

            return rtn;


        }catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }

    public static String getScheduleHistoryList(String start, String stop,String ScheduleID,String token){
        PreparedStatement stat = null;
        ResultSet rs = null;
        String QuerySQL="";
        try{

            Auth au=new Auth();
            ArrayList<Object> info =au.verify(token);
            //Boolean admin=(Boolean)info.get(0);
            if(!((Boolean)info.get(4))){
                return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission Denied", ScheduleID);
            }
            else if(!(start.equals("")||stop.equals("")||ScheduleID.equals(""))){
                if((Integer)info.get(0)>1) {
                    QuerySQL = SelectHistoryScheduleList_timeandScheduleId;
                    stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                    stat.setString(1, start);
                    stat.setString(2, stop);
                    stat.setInt(3, Integer.parseInt(ScheduleID));
                }
                else{
                    QuerySQL = SelectHistoryScheduleList_timeandScheduleId_user;
                    stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                    stat.setString(1, start);
                    stat.setString(2, stop);
                    stat.setInt(3, Integer.parseInt(ScheduleID));
                    stat.setInt(4,(Integer)info.get(1));
                }
            }
            else if(!ScheduleID.equals(""))
            {
                if((Integer)info.get(0)>1) {
                    QuerySQL = SelectHistoryScheduleList_ScheduleID;
                    stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                    stat.setInt(1, Integer.parseInt(ScheduleID));
                }
                else{
                    QuerySQL = SelectHistoryScheduleList_ScheduleID_user;
                    stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                    stat.setInt(1, Integer.parseInt(ScheduleID));
                    stat.setInt(2,(Integer)info.get(1));
                }
            }
            else if(!(start.equals("")||stop.equals(""))){
                if((Integer)info.get(0)>1) {
                    QuerySQL = SelectHistoryScheduleList_time;
                    stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                    stat.setString(1, start);
                    stat.setString(2, stop);
                }
                else{
                    QuerySQL = SelectHistoryScheduleList_time_user;
                    stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                    stat.setString(1, start);
                    stat.setString(2, stop);
                    stat.setInt(3, Integer.parseInt(ScheduleID));
                    stat.setInt(4,(Integer)info.get(1));
                }
            }
            else{
                DateTime dt=new DateTime();
                return MessageFactory.rtnScheduleMessage("error", dt.toString("yyyy-MM-dd HH:mm:ss.SSS"), "illegal parameter", ScheduleID);
            }
            QuerySQL=stat.toString();
            rs=stat.executeQuery();
            ArrayList<Map> list=new ArrayList<>();
            while(rs.next()){
                Map json=new LinkedHashMap();
                json.put("schedule_runid",rs.getInt("SHID"));
                json.put("schedule_id",rs.getInt("ScheduleID"));
                json.put("schedule_name",rs.getString("ScheduleName"));
                json.put("schedule_Level",rs.getInt("ScheduleLevel"));
                json.put("memo",rs.getString("ScheduleMemo"));
                json.put("notification",rs.getString("Notification"));
                json.put("schedule_status",rs.getString("ScheduleStatus"));
                json.put("schedule_mode",rs.getString("ScheduleTimeType"));
                json.put("startwith", rs.getString("StartWith"));
                json.put("every",rs.getInt("TimeEvery"));
                json.put("unit",rs.getString("TimeEveryType"));
                json.put("time",rs.getInt("TimeCycle"));
                json.put("each",rs.getInt("TimeEach"));
                json.put("start_time",rs.getString("RScheduleStartTime"));
                json.put("stop_time",rs.getString("ScheduleStopTime"));
                if(!((rs.getString("RScheduleStartTime")==null)||(rs.getString("RScheduleStartTime").equals(""))) ){
                    try {
                        json.put("last_runtime", rs.getString("RScheduleStartTime"));
                        json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("RScheduleStartTime")),
                                TimeUtil.String2DateTime(rs.getString("ScheduleStopTime"))));
                    } catch (NullPointerException npe) {
                        json.put("last_runtime", "");
                        if (rs.getString("RScheduleStartTime") != null) {
                            json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("RScheduleStartTime")),
                                    TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                        } else {
                            json.put("runingtime", "0");
                        }
                    }catch (IllegalArgumentException e){
                        json.put("last_runtime", "");
                        if (rs.getString("RScheduleStartTime") != null) {
                            json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("RScheduleStartTime")),
                                    TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                        } else {
                            json.put("runingtime", "0");
                        }
                    }
                }
                json.put("user", rs.getString("UserName"));
                json.put("userid",rs.getString("UID"));
                json.put("group", (Integer) info.get(2));



                list.add(json);

            }
            stat.close();

            for(Map m:list){
                int id=(Integer) m.get("schedule_id");
                m.put("runjob",getRunJob(id));
                int hid=(Integer) m.get("schedule_runid");
                m.put("runHistoryjob",getScheduleRunJob(hid));
                if(((String)m.get("schedule_mode")).equals("single")){
                    m.put("mod_set",getscheduleTime(id));
                }
            }



            String rtn=MessageFactory.scheduleListMessage(list);
            return rtn;
        }catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }

    }

    public static String getScheduleHistoryInfo(String token, int runid){
        //Schedule join Schedule history
        PreparedStatement stat = null;
        ResultSet rs = null;
        String QuerySQL="";
        try{
            Gson gson = new Gson();

            Auth au=new Auth();
            ArrayList<Object> info =au.verify(token);
            if(!(Boolean)au.verify(token).get(4)){
                return MessageFactory.rtnScheduleMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            }
            else if((Integer)info.get(0)>1){
                QuerySQL=SelectScheduleHistoryInfo;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                stat.setInt(1, runid);
            }else{
                QuerySQL=SelectScheduleHistoryInfo_user;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                stat.setInt(1, runid);
                stat.setInt(2,(Integer) info.get(1));
            }
            //INSERT SQL
            QuerySQL=stat.toString();


            rs=stat.executeQuery();
            ArrayList<Map> list=new ArrayList<>();

            Map json=new LinkedHashMap();
            json.put("status","success");
            json.put("Currenttime",TimeUtil.getCurrentTime());
            json.put("schedule_runid",rs.getInt("SHID"));
            json.put("schedule_id",rs.getInt("ScheduleID"));
            json.put("schedule_name",rs.getString("ScheduleName"));
            json.put("schedule_Level",rs.getInt("ScheduleLevel"));
            json.put("memo",rs.getString("ScheduleMemo"));
            json.put("notification",rs.getString("Notification"));
            json.put("schedule_status",rs.getString("ScheduleStatus"));
            json.put("schedule_mode",rs.getString("ScheduleTimeType"));
            json.put("startwith", rs.getString("StartWith"));
            json.put("every",rs.getInt("TimeEvery"));
            json.put("unit",rs.getString("TimeEveryType"));
            json.put("time",rs.getInt("TimeCycle"));
            json.put("each",rs.getInt("TimeEach"));
            json.put("start_time",rs.getString("ScheduleStatTime"));
            json.put("stop_time",rs.getString("ScheduleStopTime"));
            try{
                json.put("last_runtime",rs.getString("ScheduleStartTime"));
                json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("ScheduleStartTime")),
                        TimeUtil.String2DateTime(rs.getString("ScheduleStopTime"))));
            }

            catch(NullPointerException npe){
                json.put("last_runtime", "");
                if(rs.getString("ScheduleStartTime")!=null) {
                    json.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("ScheduleStartTime")),
                            TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                }else {
                    json.put("runingtime","0");
                }
            }
            json.put("user",rs.getString("UserName"));
            json.put("userid", rs.getString("UID"));
            json.put("group", (Integer) info.get(2));
            stat.close();

            int id=rs.getInt("schedule_id");
            json.put("runjob", getRunJob(id));
            int hid=rs.getInt("SHID");
            json.put("runHistoryjob",getScheduleRunJob(hid));
            if(((String)rs.getString("schedule_mode")).equals("single")){
                json.put("mod_set", getscheduleTime(id));
            }
            return gson.toJson(json);
        }catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }

    }

    public static ArrayList<Integer> getScheduleRunJob(int SHID) throws SQLException{

        String QuerySQL="SELECT * FROM Schedule_Job_History WHERE `SHID`=? ORDER BY `SortIndex`";
        PreparedStatement stat = null;
        ResultSet rs = null;
        ArrayList<Integer> rtn=new ArrayList<>();
        stat=ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
        stat.setInt(1, SHID);
        rs=stat.executeQuery();
        while(rs.next()){
            rtn.add(rs.getInt("JHId"));
        }
        return rtn;
    }

    public synchronized static int insertScheduleJobHistory(int ShceduleHistoryId,int JobhistoryID,int JobID,int SortIndex) throws SQLException{


        int SJHID=-1;
        String QuerySQL = "INSERT INTO `main`.`Schedule_Job_History` (`SHID`,`JHID`,`JobID`,`SortIndex`) VALUES (?,?,?,?);";
        PreparedStatement stat = null;
        //INSERT SQL
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
        stat.setInt(1, ShceduleHistoryId);//ScheduleID
        stat.setInt(2, JobhistoryID);
        stat.setInt(3, JobID);
        stat.setInt(4, SortIndex);

        SJHID=ConnectionManager.dbInsert(stat);

        return SJHID;


    }

    public synchronized static int insertScheduleHistory(ScheduleHistory scheduleHistoryBean) throws SQLException{
        int ScheduleHistoryID=-1;

        PreparedStatement stat = null;
        ResultSet rs = null;
        //INSERT SQL
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(InsertScheduleHistorySql);
        stat.setInt(1, scheduleHistoryBean.getScheduleID());//ScheduleID
        stat.setString(2, scheduleHistoryBean.getScheduleName());//ScheduleName
        stat.setInt(3, scheduleHistoryBean.getScheduleOwner());//ScheduleOwner
        stat.setInt(4, scheduleHistoryBean.getScheduleLevel());//ScheduleLevel
        stat.setString(5, scheduleHistoryBean.getScheduleMemo());//memo
        stat.setInt(6, scheduleHistoryBean.getScheduleStatus());//status
        stat.setString(7, scheduleHistoryBean.getScheduleStartTime());//ScheduleStartTime
        stat.setString(8, scheduleHistoryBean.getScheduleStopTime());//ScheduleStopTime
        stat.setString(9, scheduleHistoryBean.getScheduleLog());//ScheduleLog
        stat.setString(10, scheduleHistoryBean.getScheduleTimeType());//ScheduleTimeType
        stat.setString(11, scheduleHistoryBean.getStartWith());//startWith
        stat.setInt(12, scheduleHistoryBean.getTimeEvery());//timeEvery
        stat.setString(13, scheduleHistoryBean.getTimeEveryType());//timeEveryType
        stat.setInt(14, scheduleHistoryBean.getTimeCycle());//timeCycle
        stat.setString(15, scheduleHistoryBean.getTimeEach());//timeEach
        stat.setBoolean(16, scheduleHistoryBean.getNotification());//notification

        stat.executeUpdate();
        ScheduleHistoryID=stat.getGeneratedKeys().getInt(1);
        stat.closeOnCompletion();




        return ScheduleHistoryID;
    }

    public synchronized static int getUserLevel(int UserID) throws SQLException {
        String QuerySQL = "SELECT `User`.`Admin`,`User`.`General` From `User`  WHERE  `User`.`UID`=?;";
        PreparedStatement stat = null;
        ResultSet rs = null;
        int rtn = Integer.MIN_VALUE;
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
        stat.setInt(1, UserID);
        rs = stat.executeQuery();

        if (rs.next()){
            if (rs.getBoolean("Admin")) {
                rtn = PrestoContent.ADMIN;
            } else if (rs.getBoolean("General")) {
                rtn = PrestoContent.GENERAL;
            } else {
                rtn = PrestoContent.MANAGER;
            }
        }
        stat.close();
        return rtn;
    }

    public synchronized static int insertScheduleHistory(ArrayList<String> args) throws SQLException{
        int ScheduleHistoryID=-1;

        PreparedStatement stat = null;

        //INSERT SQL
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(InsertScheduleHistorySql);
        stat.setInt(1, Integer.parseInt(args.get(0)));//ScheduleID
        stat.setString(2, args.get(1));//ScheduleName
        stat.setInt(3, Integer.parseInt(args.get(2)));//ScheduleOwner
        stat.setInt(4, Integer.parseInt(args.get(3)));//ScheduleLevel
        stat.setString(5, args.get(4));//memo
        stat.setInt(6, Integer.parseInt(args.get(5)));//status
        stat.setString(7, args.get(6));//ScheduleStartTime
        stat.setString(8, args.get(7));//ScheduleStopTime
        stat.setString(9, args.get(8));//ScheduleLog
        stat.setString(10, args.get(9));//ScheduleTimeType
        stat.setString(11, args.get(10));//startWith
        stat.setInt(12, Integer.parseInt(args.get(11)));//timeEvery
        stat.setString(13, args.get(12));//timeEveryType
        stat.setInt(14, Integer.parseInt(args.get(13)));//timeCycle
        stat.setString(15, args.get(14));//timeEach
        stat.setBoolean(16, Boolean.getBoolean(args.get(15)));//notification

        ScheduleHistoryID=ConnectionManager.dbInsert(stat);
        return ScheduleHistoryID;
    }

    public synchronized static void updateScheduleHistory(int ScheduleHistoryID, String ScheduleStopTime, int ScheduleStatus, String logPath)throws SQLException{

        PreparedStatement stat = null;
        //INSERT SQL
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(UpdateScheduleHistorySql);
        stat.setString(1, ScheduleStopTime);
        stat.setInt(2, ScheduleStatus);
        stat.setString(3, logPath);
        stat.setInt(4,ScheduleHistoryID);
        stat.executeUpdate();
        stat.close();

    }

    public static boolean scheduleIsExist(int ScheduleID) throws SQLException {
        //SQLite
        PreparedStatement stat = null;
        ResultSet rs = null;
        //Check User Account and Password
        String sql = "select `ScheduleName` from `Schedule` where  ScheduleID=?";
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
        stat.setInt(1, ScheduleID);
        rs = stat.executeQuery();
        boolean flag=false;
        while (rs.next()){
            flag=true;
        }
        stat.close();
        return flag;
    }

    public static boolean scheduleIsActivated(int ScheduleID) throws SQLException {
        PreparedStatement stat = null;
        ResultSet rs = null;
        //Check User Account and Password
        String sql = "select `ScheduleName` from `Schedule` where  ScheduleID=? and ScheduleStatus=1";
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
        stat.setInt(1, ScheduleID);
        rs = stat.executeQuery();
        boolean flag=false;
        while (rs.next()){
            flag=true;
        }
        stat.close();
        return flag;
    }

}
