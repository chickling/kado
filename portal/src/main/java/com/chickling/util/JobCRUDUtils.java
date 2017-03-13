package com.chickling.util;

import com.chickling.sqlite.ConnectionManager;

import com.chickling.models.Auth;
import com.chickling.models.MessageFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by jw6v on 2015/11/30.
 */
public class JobCRUDUtils {

    private final static String InsertJobSql="INSERT INTO `main`.`Job` (`JobName`,`JobOwner`,`JobLevel`,`JobMemo`,`Notification`,`JobStorageType`," +
            "`StorageResources`,`FilePath`,`FileName`,`DBSQL`,`JobSQL`,`Report`,`ReportEmail`,`ReportLength`,`ReportFileType`,`ReportTitle`,`ReportWhileEmpty`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
    private final static String UpdateJobSql="UPDATE `main`.`Job` SET `JobName`=?,`JobLevel`=?,`JobMemo`=?,`Notification`=?,`JobStorageType`=?," +
            "`StorageResources`=?,`FilePath`=?,`FileName`=?,`DBSQL`=?,`JobSQL`=?,`Report`=?,`ReportEmail`=?,`ReportLength`=?,`ReportFileType`=?,`ReportTitle`=?,`ReportWhileEmpty`=? WHERE `JobID`=?;";

    private final static String UpdateJobLogSql="UPDATE `main`.`Job_Log` SET `ResultCount`=?,`JobOutput`=?,`Valid`=? WHERE `JLID`=?;";

    private final static String UpdateJobHistorySql="UPDATE `main`.`Job_History` SET `JobStartTime`=?,`JobStopTime`=?,`JobStatus`=?,`JobProgress`=? WHERE `JHID`=?;";
    private final static String InsertJobHistorySql="INSERT INTO `main`.`Job_History` (`JobID`,`PrestoID`,`JobOwner`,`JobLevel`,`JobStartTime`,`JobStopTime`," +
            "`JobStatus`,`JobProgress`,`JobLog`,`JobType`,`Report`,`ReportEmail`,`ReportLength`,`ReportFileType`,`ReportTitle`,`ReportWhileEmpty`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
    private final static String InsertJobLogSql="INSERT INTO `main`.`Job_Log` (`JobSQL`,`JobOutput`,`JobLogfile`,`JobStorageType`,`StorageResources`,`FilePath`," +
            "`FileName`,`DBSQL`,`Replace_Value`,`Replace_Sign`,`ResultCount`,`Valid`,`ReportWhileEmpty`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);";

    private final static String GetJobInfoSql="SELECT * FROM `main`.`Job` WHERE `JobID`=?;";
    private final static String GetJobInfoSql_usr="SELECT *, JobOwner UID FROM `main`.`Job` WHERE `JobID`=? and (UID in (Select UID From User WHERE Gid=?) or JobLevel=1);";
    private final static String DeleteJobSql="DELETE FROM `main`.`JOb` WHERE `JobID`=?;";
    private final static String CheckJobID="SELECT * FROM `Job` WHERE `JobID`=?;";
    private final static String SelectAllJobSql= "SELECT * FROM(SELECT *, j.JobOwner UID FROM Job j LEFT JOIN (SELECT *, Max(JobStartTime) FROM Job_History WHERE JobStatus = 1 group by JobID) jh on j.JobID=jh.JobID ) jl ,User u WHERE u.UID=jl.uid;";
    private final static String SelectJobListSql ="SELECT * FROM(SELECT *, j.JobOwner UID FROM Job j LEFT JOIN (SELECT *, Max(JobStartTime) FROM Job_History WHERE JobStatus=1 group by JobID) jh on j.JobID=jh.JobID ) jl,User u WHERE jl.UID=u.UID AND (jl.UID in (Select UID From User WHERE Gid=?) or JobLevel=1);";
    private final static String SelectJobExecutionList="SELECT * FROM (SELECT *,j.JobOwner UID FROM Job_History jh INNER JOIN Job j ON j.JobID=jh.JobID WHERE UID in (Select UID From User WHERE Gid=?) or j.JobLevel=1 ORDER BY JobStartTime DESC limit ?) jhr,User u WHERE u.UID=jhr.JobOwner;";
    private final static String SelectAllJobExecutionList="SELECT * FROM (SELECT *,j.JobOwner UID FROM Job_History jh LEFT JOIN Job j ON jh.JobID=j.JobID ORDER BY jh.JobStartTime DESC limit ?) jhr,User u WHERE jhr.JobOwner =u.UID;";
    private final static String SelectHistoryJobList_time_user="SELECT * FROM (SELECT *, Job.JobOwner UID FROM Job LEFT JOIN Job_History ON Job.JobID=Job_History.JobID WHERE JobStartTime>? and JobStopTime<? and (UID in (Select UID From User WHERE Gid=?) or Job.JobLevel=1)) jhr,User u WHERE jhr.JobOwner =u.UID;";
    private final static String SelectHistoryJobList_jobID_user="SELECT * FROM (SELECT *,  Job.JobOwner UID FROM Job INNER JOIN Job_History ON Job.JobID=Job_History.JobID WHERE Job_History.JobID=? and (UID in (Select UID From User WHERE Gid=?) or Job.JobLevel=1)) jhr,User u WHERE jhr.JobOwner =u.UID;";
    private final static String SelectHistoryJobList_timeandjobId_user="SELECT * FROM (SELECT *, Job.JobOwner UID FROM Job INNER JOIN Job_History WHERE JobStartTime>? and JobStopTime<? and Job.JobID=? and (UID in (Select UID From User WHERE Gid=?) or Job.JobLevel=1)) jhr,User u WHERE jhr.JobOwner =u.UID;";
    private final static String SelectHistoryJobList_time="SELECT * FROM (SELECT *, j.JobOwner UID FROM Job j LEFT JOIN Job_History jh ON j.JobID=jh.JobID WHERE JobStartTime>? and JobStopTime<?) jhl,User u WHERE jhl.JobOwner=u.UID";
    private final static String SelectHistoryJobList_jobID="SELECT * FROM (SELECT *,  Job.JobOwner UID FROM Job INNER JOIN Job_History ON Job.JobID=Job_History.JobID WHERE Job_History.JobID=?) jhl,User u WHERE jhl.JobOwner=u.UID";
    private final static String SelectHistoryJobList_timeandjobId="SELECT * FROM (SELECT *, Job.JobOwner UID FROM Job INNER JOIN Job_History ON Job.JobID=Job_History.JobID WHERE JobStartTime>? and JobSopTime<? and Job.JobID=?) jhl,User u WHERE jhl.JobOwner=u.UID";
    private final static String SelectJobHistoryInfo="SELECT *  FROM (SELECT *,Job.JobOwner UID,Job_Log.JobSQL JobSQLLog FROM Job_History INNER JOIN Job INNER JOIN Job_Log ON Job_History.JobLog=Job_Log.JLID  AND Job.JobID=Job_History.JobID WHERE JHID=?) jhl,User u WHERE jhl.JobOwner=u.UID;";
    private final static String SelectJobHistoryInfo_user="SELECT *  FROM (SELECT *,Job.JobOwner UID,Job_Log.JobSQL JobSQLLog FROM Job_History INNER JOIN Job INNER JOIN Job_Log ON Job_History.JobLog=Job_Log.JLID  AND Job.JobID=Job_History.JobID WHERE JHID=? and (UID in (Select UID From User WHERE Gid=?) or Job.JobLevel=1)) jhl,User u WHERE jhl.JobOwner=u.UID;";

    private static Logger log = LogManager.getLogger(JobCRUDUtils.class);


    //TODO :report schema
    public synchronized static String addJobInfotoDB(Map args, String token) {
        PreparedStatement stat = null;
        ResultSet rs = null;

        String QuerySQL="";
        try {
            Auth au = new Auth();
            ArrayList<Object> userInfo = au.verify(token);
            if (!(Boolean) userInfo.get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if (((Integer) userInfo.get(0)) > 0) {
                //INSERT SQL
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(InsertJobSql);
                stat.setString(1, ((String) args.get("jobname")));
                stat.setInt(2, (Integer) userInfo.get(2));//userID
                stat.setInt(3, (Integer.parseInt((String) args.get("jobLevel"))));
                stat.setString(4, ((String) args.get("memo")));
                stat.setBoolean(5, (Boolean) args.get("notification"));
                if ((Boolean) args.get("storage")) {
                    stat.setInt(6, ((Double) args.get("save_type")).intValue());
                } else {
                    stat.setInt(6, 0);
                }
                stat.setInt(7, (Integer.parseInt((String) args.get("location_id"))));
                stat.setString(8, ((String) args.get("filepath")));
                stat.setString(9, ((String) args.get("filename")));
                stat.setString(10, ((String) args.get("insertsql")));
                stat.setString(11, ((String) args.get("sql")));
                stat.setBoolean(12, (Boolean.valueOf((String)args.get("Report"))));
                stat.setString(13, ((String) args.get("ReportEmail")));
                stat.setInt(14, (Integer.parseInt((String) args.get("ReportLength"))));
                stat.setInt(15, (Integer.parseInt((String) args.get("ReportFileType"))));
                stat.setString(16, ((String) args.get("ReportTitle")));
                stat.setBoolean(17, (Boolean.valueOf((String) args.get("ReportWhileEmpty"))));
                QuerySQL=stat.toString();
                stat.executeUpdate();

                String key = Integer.toString(stat.getGeneratedKeys().getInt(1));
                stat.close();
                return MessageFactory.rtnJobMessage("success", TimeUtil.getCurrentTime(), "", key);
            } else {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");

            }
        }
        catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    //TODO :report schema
    public synchronized static String updateJobtoDB(Map args,int JobID, String token) {
        PreparedStatement stat = null;
        String QuerySQL = "";
    try {
        if (JobIsExist(JobID)) {

            Auth au = new Auth();
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((au.jobMatch(token, Integer.toString(JobID))) || ((Integer) au.verify(token).get(0) == 2)) {

                QuerySQL = UpdateJobSql;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
            } else {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", Integer.toString(JobID));
            }
            //INSERT SQL
            stat.setString(1, ((String) args.get("jobname")));
            stat.setInt(2, (Integer.parseInt((String) args.get("jobLevel"))));
            stat.setString(3, ((String) args.get("memo")));
            stat.setBoolean(4, (Boolean) args.get("notification"));
            if ((Boolean) args.get("storage")) {
                stat.setInt(5, ((Double) args.get("save_type")).intValue());

            } else {
                stat.setInt(5, 0);
            }
            stat.setInt(6, (Integer.parseInt((String) args.get("location_id"))));
            stat.setString(7, ((String) args.get("filepath")));
            stat.setString(8, ((String) args.get("filename")));
            stat.setString(9, ((String) args.get("insertsql")));
            stat.setString(10, ((String) args.get("sql")));
            stat.setBoolean(11, (Boolean.valueOf((String)args.get("Report"))));
            stat.setString(12, ((String) args.get("ReportEmail")));
            stat.setInt(13, (Integer.parseInt((String) args.get("ReportLength"))));
            stat.setInt(14, (Integer.parseInt((String) args.get("ReportFileType"))));
            stat.setString(15, ((String) args.get("ReportTitle")));
            stat.setBoolean(16, (Boolean.valueOf((String) args.get("ReportWhileEmpty"))));
            stat.setInt(17, JobID);

            QuerySQL=stat.toString();
            stat.executeUpdate();
            stat.close();
            return MessageFactory.rtnJobMessage("success", TimeUtil.getCurrentTime(), "", Integer.toString(JobID));
            }
        else {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Job is not exit", Integer.toString(JobID));
            }
        }
        catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    //TODO :report schema
    public static String getJobInfo(int JobID){
        String QuerySQL = "";
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            QuerySQL = GetJobInfoSql;
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
            stat.setInt(1, JobID);
            QuerySQL=stat.toString();
            rs = stat.executeQuery();
            String rtn = MessageFactory.rtnJobInfoMessage(rs);
            stat.close();
            return rtn;
        }
        catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    //TODO :report schema
    public static String getJobInfo(int JobID, String token) {
        String QuerySQL = "";
        PreparedStatement stat = null;
        ResultSet rs = null;

        try {
            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((Integer) info.get(0) > 1) {
                QuerySQL = GetJobInfoSql;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                stat.setInt(1, JobID);
            } else {
                QuerySQL = GetJobInfoSql_usr;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                stat.setInt(1, JobID);
                stat.setInt(2, (Integer) info.get(1));
            }
            //INSERT SQL
            QuerySQL=stat.toString();

            rs = stat.executeQuery();

            String rtn = MessageFactory.rtnJobInfoMessage(rs);
            stat.close();
            return rtn;
        }catch(SQLException sqle){
            log.error(sqle);
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");

        }
    }
    //TODO :report schema
    public synchronized static String deleteJob(int JobID,String token) {
        PreparedStatement stat = null;

        String QuerySQL = "";
        try {
            Auth au = new Auth();
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((au.jobMatch(token, Integer.toString(JobID))) || ((Integer) au.verify(token).get(0) == 2)) {
                QuerySQL = DeleteJobSql;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);


            } else {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission Denied", Integer.toString(JobID));
            }

            stat.setInt(1, JobID);
            QuerySQL=stat.toString();
            stat.execute();
            stat.close();
            return MessageFactory.rtnJobMessage("success", TimeUtil.getCurrentTime(), "", Integer.toString(JobID));
        }catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    //TODO :report schema
    public static String getJobList(String token) {
        String QuerySQL = null;
        PreparedStatement stat = null;
        ResultSet rs = null;

        try {
            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((Integer) info.get(0) > 1) {
                QuerySQL = SelectAllJobSql;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
            } else {
                QuerySQL = SelectJobListSql;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                stat.setInt(1, (Integer) info.get(1));
            }
            QuerySQL=stat.toString();
            rs = stat.executeQuery();
            String rtn = MessageFactory.JobListMessage(rs, (Integer) info.get(2), (Integer) info.get(1), (String) info.get(3));
            stat.close();
            return rtn;
        }catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");

        }
    }
    //TODO :report schema
    public static String getJobStatusList(String limit,String token)  {
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
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((Integer) info.get(0) > 0) {
                QuerySQL = SelectAllJobExecutionList;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                stat.setInt(1, recordLimit);
            } else {
                QuerySQL = SelectJobExecutionList;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                stat.setInt(1, (Integer) info.get(1));
                stat.setInt(2, recordLimit);

            }
            //INSERT SQL
            QuerySQL=stat.toString();
            rs = stat.executeQuery();
            String rtn = MessageFactory.JobStatusListMessage(rs, (Integer) info.get(2), (Integer) info.get(1), (String) info.get(3));
            stat.close();
            return rtn;
        }catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    //TODO :report schema
    public static String getJobHistoryList(String start, String stop,String jobID,String token) {
        PreparedStatement stat = null;
        ResultSet rs = null;

        String QuerySQL = "";
        try {


            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);

            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if (!(start.equals("") || stop.equals("") || jobID.equals(""))) {

                if ((Integer) info.get(0) > 1) {
                    QuerySQL = SelectHistoryJobList_timeandjobId;
                    stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                    stat.setString(1, start);
                    stat.setString(2, stop);
                    stat.setInt(3, Integer.parseInt(jobID));
                } else {
                    QuerySQL = SelectHistoryJobList_timeandjobId_user;
                    stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                    stat.setString(1, start);
                    stat.setString(2, stop);
                    stat.setInt(3, Integer.parseInt(jobID));
                    stat.setInt(4, (Integer) info.get(1));
                }
            } else if (!jobID.equals("")) {
                if ((Integer) info.get(0) > 1) {
                    QuerySQL = SelectHistoryJobList_jobID;
                    stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);

                    stat.setInt(1, Integer.parseInt(jobID));
                } else {
                    QuerySQL = SelectHistoryJobList_jobID_user;
                    stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);

                    stat.setInt(1, Integer.parseInt(jobID));
                    stat.setInt(2, (Integer) info.get(1));
                }
            } else if (!(start.equals("") || stop.equals(""))) {
                if ((Integer) info.get(0) > 1) {
                    QuerySQL = SelectHistoryJobList_time;
                    stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                    stat.setString(1, start);
                    stat.setString(2, stop);
                } else {
                    QuerySQL = SelectHistoryJobList_time_user;
                    stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                    stat.setString(1, start);
                    stat.setString(2, stop);
                    stat.setInt(3, (Integer) info.get(1));
                }
            } else {
                DateTime dt = new DateTime();

                return MessageFactory.rtnJobMessage("error", dt.toString("yyyy-MM-dd HH:mm:ss.SSS"), "illegal parameter", jobID);

            }
            QuerySQL=stat.toString();
            rs = stat.executeQuery();
            String rtn = MessageFactory.HistoryListMessage(rs, (Integer) info.get(2), (Integer) info.get(1), (String) info.get(3));
            stat.close();
            return rtn;
        }catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    public static String getHasResultJobHistory(String jobID,String token) throws SQLException {
        PreparedStatement stat = null;
        ResultSet rs = null;
        String QuerySQL = "";
        Auth au = new Auth();
        if (!(Boolean) au.verify(token).get(4)) {
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
        }else {
            QuerySQL = "SELECT jh.JHID,jh.JobID,jh.JobStartTime,jl.ResultCount,jl.JobOutput FROM Job_History as jh  JOIN Job_Log jl  ON jh.JobLog=jl.JLID WHERE jh.JobID=? AND jl.ResultCount>0 Order By jh.JobStartTime DESC limit ?";
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
            stat.setInt(1, Integer.parseInt(jobID));
            stat.setInt(2, 10);
            rs = stat.executeQuery();
            String rtn = MessageFactory.hasResultJobHistory(rs);
            stat.close();
            return rtn;
        }
    }
    //TODO :report schema
    public static String getJobHistoryInfo(String token, int runid) {
        PreparedStatement stat = null;
        ResultSet rs = null;
        String QuerySQL = "";
        try {

            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((Integer) info.get(0) > 1) {
                QuerySQL = SelectJobHistoryInfo;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                stat.setInt(1, runid);
            } else {
                QuerySQL = SelectJobHistoryInfo_user;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                stat.setInt(1, runid);
                stat.setInt(2, (Integer) info.get(1));
            }
            //INSERT SQL
            QuerySQL=stat.toString();
            rs = stat.executeQuery();
            String rtn = MessageFactory.JobHistoryInfoMessage(rs, (Integer) info.get(2), (Integer) info.get(1), (String) info.get(3));
            stat.close();
            return rtn;
        }catch(SQLException sqle){
            log.error(sqle);
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    public static Map getJobHistoryInfo(int runid) {
        PreparedStatement stat = null;
        ResultSet rs = null;
        String QuerySQL = "";
        try {
            QuerySQL = SelectJobHistoryInfo;
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
            stat.setInt(1, runid);

            QuerySQL=stat.toString();
            rs = stat.executeQuery();
            Map rtn = MessageFactory.JobHistoryInfoMessage(rs);
            stat.close();
            return rtn;
        }catch(SQLException sqle){
            log.error(sqle);
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return new LinkedHashMap();
        }
    }
    //TODO :report schema
    public synchronized static int InsertJobHistory(ArrayList<String> args)throws SQLException{

            int JobHistoryID = -1;

            PreparedStatement stat = null;

            //INSERT SQL
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(InsertJobHistorySql);
            stat.setInt(1, Integer.parseInt(args.get(0)));//JobID
            stat.setString(2, args.get(1));//PrestoID
            stat.setInt(3, Integer.parseInt(args.get(2)));//JobOwner
            stat.setInt(4, Integer.parseInt(args.get(3)));//JobLevel
            stat.setString(5, args.get(4));//JobStartTime
            stat.setString(6, args.get(5));//JobStopTime
            stat.setInt(7, Integer.parseInt(args.get(6)));//JobStatus
            stat.setInt(8, Integer.parseInt(args.get(7)));//JobProgress
            stat.setInt(9, Integer.parseInt(args.get(8)));//JobLog
            stat.setInt(10, Integer.parseInt(args.get(9)));//JobType
            stat.setBoolean(11, Boolean.valueOf(args.get(10)));
            stat.setString(12,  args.get(11));
            stat.setInt(13, (Integer.parseInt( args.get(12))));
            stat.setInt(14, (Integer.parseInt(args.get(13))));
            stat.setString(15, args.get(14));
            stat.setBoolean(16, Boolean.valueOf(args.get(15)));
            JobHistoryID= ConnectionManager.dbInsert(stat);
            return JobHistoryID;

    }
    //TODO :report schema
    public synchronized static void UpdateJobHistory(int JobHistoryID,String JobStartTime, String JobStopTime, int JobStatus, int JobProgress)throws SQLException{

        PreparedStatement stat = null;
        //INSERT SQL
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(UpdateJobHistorySql);
        stat.setString(1, JobStartTime);
        //stat.setInt(2, Integer.parseInt(token));//token
        stat.setString(2, JobStopTime);
        stat.setInt(3, JobStatus);
        stat.setInt(4, JobProgress);
        stat.setInt(5, JobHistoryID);
        stat.executeUpdate();
        stat.close();

    }

    public synchronized static int InsertJobLog(ArrayList<String> args)throws SQLException{
        int JobLogID=-1;

        PreparedStatement stat = null;

        //INSERT SQL
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(InsertJobLogSql);
        stat.setString(1, args.get(0));//JobSql
        stat.setString(2, args.get(1));//JobOutPut
        stat.setString(3, args.get(2));//JobLogFile
        stat.setInt(4, Integer.parseInt(args.get(3)));//JobStorageType
        stat.setInt(5, Integer.parseInt(args.get(4)));//StorageResources
        stat.setString(6, args.get(5));//FilePath
        stat.setString(7, args.get(6));//FileName
        stat.setString(8, args.get(7));//DBSQL
        stat.setInt(9, Integer.parseInt(args.get(8)));//Replace_Value
        stat.setString(10, args.get(9));//Replace_Sign
        stat.setInt(11, Integer.parseInt(args.get(10)));//ResulCount
        stat.setInt(12, Integer.parseInt(args.get(11)));//Valid
        stat.setBoolean(13, Boolean.valueOf(args.get(12)));//Valid

        JobLogID=ConnectionManager.dbInsert(stat);
        return JobLogID;
    }

    public synchronized static void UpdateJobLog(int JobLogID,int resultCount, String JobOutPut,Boolean valid)throws SQLException{


        PreparedStatement stat = null;
        //INSERT SQL
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(UpdateJobLogSql);
        stat.setInt(1, resultCount);
        stat.setString(2, JobOutPut);
        if(valid){
            stat.setBoolean(3, true);
        }
        else{
            stat.setBoolean(3, false);
        }
        stat.setInt(4,JobLogID);
        stat.executeUpdate();
        stat.close();

    }

    public static boolean JobIsExist(int JobID) throws SQLException {
        //SQLite
        PreparedStatement stat = null;
        ResultSet rs = null;
        //Check User Account and Password
        String sql = "select `JobName` from `Job` where  JobID=?";
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
        stat.setInt(1, JobID);
        rs = stat.executeQuery();
        boolean flag=false;
        while (rs.next()){
            flag=true;
        }
        stat.close();
        return flag;
    }

}
