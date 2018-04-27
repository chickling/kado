package com.chickling.util;


import com.chickling.models.Auth;
import com.chickling.models.MessageFactory;

import com.facebook.presto.hive.$internal.org.apache.commons.lang3.exception.ExceptionUtils;
import com.google.gson.Gson;
import owlstone.dbclient.db.DBClient;
import owlstone.dbclient.db.module.Action;
import owlstone.dbclient.db.module.DBResult;
import owlstone.dbclient.db.module.PStmt;
import owlstone.dbclient.db.module.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.joda.time.DateTime;


import java.util.*;

/**
 * Created by jw6v on 2015/11/30.
 */
public class JobCRUDUtils {

    private final static String InsertJobSql="INSERT INTO `Job` (`JobName`,`JobOwner`,`JobLevel`,`JobMemo`,`Notification`,`JobStorageType`," +
            "`StorageResources`,`FilePath`,`FileName`,`DBSQL`,`JobSQL`,`Report`,`ReportEmail`,`ReportLength`,`ReportFileType`,`ReportTitle`,`ReportWhileEmpty`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
    private final static String UpdateJobSql="UPDATE `Job` SET `JobName`=?,`JobLevel`=?,`JobMemo`=?,`Notification`=?,`JobStorageType`=?," +
            "`StorageResources`=?,`FilePath`=?,`FileName`=?,`DBSQL`=?,`JobSQL`=?,`Report`=?,`ReportEmail`=?,`ReportLength`=?,`ReportFileType`=?,`ReportTitle`=?,`ReportWhileEmpty`=? WHERE `JobID`=?;";

    private final static String UpdateJobLogSql="UPDATE `Job_Log` SET `ResultCount`=?,`JobOutput`=?,`Valid`=? WHERE `JLID`=?;";

    private final static String UpdateJobHistorySql="UPDATE `Job_History` SET `JobStartTime`=?,`JobStopTime`=?,`JobStatus`=?,`JobProgress`=? WHERE `JHID`=?;";
    private final static String InsertJobHistorySql="INSERT INTO `Job_History` (`JobID`,`PrestoID`,`JobOwner`,`JobLevel`,`JobStartTime`,`JobStopTime`," +
            "`JobStatus`,`JobProgress`,`JobLog`,`JobType`,`Report`,`ReportEmail`,`ReportLength`,`ReportFileType`,`ReportTitle`,`ReportWhileEmpty`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
    private final static String InsertJobLogSql="INSERT INTO `Job_Log` (`JobSQL`,`JobOutput`,`JobLogfile`,`JobStorageType`,`StorageResources`,`FilePath`," +
            "`FileName`,`DBSQL`,`Replace_Value`,`Replace_Sign`,`ResultCount`,`Valid`,`ReportWhileEmpty`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);";

    private final static String GetJobInfoSql="SELECT * FROM `Job` WHERE `JobID`=?;";
    private final static String GetJobInfoSql_usr="SELECT *, JobOwner UID FROM `main`.`Job` WHERE `JobID`=? and (JobOwner in (Select UID From User WHERE Gid=?) or JobLevel=1);";
    private final static String DeleteJobSql="DELETE FROM `Job` WHERE `JobID`=?;";
    private final static String CheckJobID="SELECT * FROM `Job` WHERE `JobID`=?;";
    /**NEED*/
    private final static String SelectAllJobSql= "SELECT * FROM(SELECT j.*, j.JobOwner UID,jh.JHID,jh.PrestoID,jh.JobStartTime,jh.JobStopTime,jh.JobStatus,jh.JobProgress,jh.JobLog,jh.JobType  FROM Job j LEFT JOIN (SELECT *, Max(JobStartTime) FROM Job_History WHERE JobStatus = 1 group by JobID) jh on j.JobID=jh.JobID ) jl ,User u WHERE u.UID=jl.uid";
    private final static String SelectJobListSql ="SELECT * FROM(SELECT j.*, j.JobOwner UID,jh.JHID,jh.PrestoID,jh.JobStartTime,jh.JobStopTime,jh.JobStatus,jh.JobProgress,jh.JobLog,jh.JobType FROM Job j LEFT JOIN (SELECT *, Max(JobStartTime) FROM Job_History WHERE JobStatus=1 group by JobID) jh on j.JobID=jh.JobID ) jl,User u WHERE jl.UID=u.UID AND (jl.UID in (Select UID From User WHERE Gid=?) or JobLevel=1);";
    private final static String SelectJobExecutionList="SELECT * FROM (SELECT j.*,jh.JobOwner UID,jh.JHID,jh.PrestoID,jh.JobStartTime,jh.JobStopTime,jh.JobStatus,jh.JobProgress,jh.JobLog,jh.JobType FROM Job_History jh INNER JOIN Job j ON j.JobID=jh.JobID WHERE j.JobOwner in (Select UID From User WHERE Gid=?) or j.JobLevel=1 ORDER BY JHID DESC limit ?) jhr,User u WHERE u.UID=jhr.UID;";
    private final static String SelectAllJobExecutionList="SELECT * FROM (select j.*, jh.JobOwner UID,jh.JHID,jh.PrestoID,jh.JobStartTime,jh.JobStopTime,jh.JobStatus,jh.JobProgress,jh.JobLog,jh.JobType from (select * FROM Job_History order by JHID desc limit ?) jh left join Job j ON jh.JobID=j.JobID ) jhr,User u WHERE jhr.UID =u.UID";
    private final static String SelectHistoryJobList_time_user="SELECT * FROM (SELECT Job.*, Job.JobOwner UID,Job_History.JHID,Job_History.PrestoID,Job_History.JobStartTime,Job_History.JobStopTime,Job_History.JobStatus,Job_History.JobProgress,Job_History.JobLog,Job_History.JobType FROM Job LEFT JOIN Job_History ON Job.JobID=Job_History.JobID WHERE JobStartTime>? and JobStopTime<? and (Job.JobOwner in (Select UID From User WHERE Gid=?) or Job.JobLevel=1)) jhr,User u WHERE jhr.JobOwner =u.UID";
    private final static String SelectHistoryJobList_jobID_user="SELECT * FROM (SELECT Job.*, Job.JobOwner UID,Job_History.JHID,Job_History.PrestoID,Job_History.JobStartTime,Job_History.JobStopTime,Job_History.JobStatus,Job_History.JobProgress,Job_History.JobLog,Job_History.JobType FROM Job INNER JOIN Job_History ON Job.JobID=Job_History.JobID WHERE Job_History.JobID=? and (Job.JobOwner in (Select UID From User WHERE Gid=?) or Job.JobLevel=1)) jhr,User u WHERE jhr.JobOwner =u.UID";
    private final static String SelectHistoryJobList_timeandjobId_user="SELECT * FROM (SELECT Job.*, Job.JobOwner UID,Job_History.JHID,Job_History.PrestoID,Job_History.JobStartTime,Job_History.JobStopTime,Job_History.JobStatus,Job_History.JobProgress,Job_History.JobLog,Job_History.JobType  FROM Job INNER JOIN Job_History WHERE JobStartTime>? and JobStopTime<? and Job.JobID=? and (Job.JobOwner in (Select UID From User WHERE Gid=?) or Job.JobLevel=1)) jhr,User u WHERE jhr.JobOwner =u.UID";
    private final static String SelectHistoryJobList_time="SELECT * FROM (SELECT j.*, j.JobOwner UID,jh.JHID,jh.PrestoID,jh.JobStartTime,jh.JobStopTime,jh.JobStatus,jh.JobProgress,jh.JobLog,jh.JobType FROM Job j LEFT JOIN Job_History jh ON j.JobID=jh.JobID WHERE JobStartTime>? and JobStopTime<?) jhl,User u WHERE jhl.JobOwner=u.UID";
    private final static String SelectHistoryJobList_jobID="SELECT * FROM (SELECT Job.*, Job.JobOwner UID,Job_History.JHID,Job_History.PrestoID,Job_History.JobStartTime,Job_History.JobStopTime,Job_History.JobStatus,Job_History.JobProgress,Job_History.JobLog,Job_History.JobType FROM Job INNER JOIN Job_History ON Job.JobID=Job_History.JobID WHERE Job_History.JobID=?) jhl,User u WHERE jhl.JobOwner=u.UID";
    private final static String SelectHistoryJobList_timeandjobId="SELECT * FROM (SELECT Job.*, Job.JobOwner UID,Job_History.JHID,Job_History.PrestoID,Job_History.JobStartTime,Job_History.JobStopTime,Job_History.JobStatus,Job_History.JobProgress,Job_History.JobLog,Job_History.JobType FROM Job INNER JOIN Job_History ON Job.JobID=Job_History.JobID WHERE JobStartTime>? and JobStopTime<? and Job.JobID=? ) jhl,User u WHERE jhl.JobOwner=u.UID";
    //private final static String SelectJobHistoryInfo="SELECT *  FROM (SELECT *,Job.JobOwner UID,Job_Log.JobSQL JobSQLLog FROM Job_History INNER JOIN Job INNER JOIN Job_Log ON Job_History.JobLog=Job_Log.JLID  AND Job.JobID=Job_History.JobID WHERE JHID=?) jhl,User u WHERE jhl.JobOwner=u.UID;";
    private final static String SelectJobHistoryInfo="SELECT *  FROM (SELECT Job.JobName,Job.JobMemo,Job.JobOwner UID,Job_Log.JobSQL JobSQLLog ,Job_History.*,Job_Log.JLID,Job_Log.JobSQL,Job_Log.JobOutput,Job_Log.JobLogfile,Job_Log.JobStorageType,Job_Log.StorageResources,Job_Log.FilePath,Job_Log.FileName,Job_Log.DBSQL,Job_Log.Replace_Value,Job_Log.Replace_Sign,Job_Log.ResultCount,Job_Log.Valid FROM Job_History INNER JOIN Job INNER JOIN Job_Log ON Job_History.JobLog=Job_Log.JLID  AND Job.JobID=Job_History.JobID WHERE JHID=?) jhl,User u WHERE jhl.JobOwner=u.UID";
    //private final static String SelectJobHistoryInfo_user="SELECT *  FROM (SELECT *,Job.JobOwner UID,Job_Log.JobSQL JobSQLLog FROM Job_History INNER JOIN Job INNER JOIN Job_Log ON Job_History.JobLog=Job_Log.JLID  AND Job.JobID=Job_History.JobID WHERE JHID=? and (UID in (Select UID From User WHERE Gid=?) or Job.JobLevel=1)) jhl,User u WHERE jhl.JobOwner=u.UID;";
    private final static String SelectJobHistoryInfo_user="SELECT *  FROM (SELECT Job.JobName,Job.JobMemo,Job.JobOwner UID,Job_Log.JobSQL JobSQLLog ,Job_History.*,Job_Log.JLID,Job_Log.JobSQL,Job_Log.JobOutput,Job_Log.JobLogfile,Job_Log.JobStorageType,Job_Log.StorageResources,Job_Log.FilePath,Job_Log.FileName,Job_Log.DBSQL,Job_Log.Replace_Value,Job_Log.Replace_Sign,Job_Log.ResultCount,Job_Log.Valid FROM Job_History INNER JOIN Job INNER JOIN Job_Log ON Job_History.JobLog=Job_Log.JLID  AND Job.JobID=Job_History.JobID WHERE JHID=? and (Job.JobOwner in (Select UID From User WHERE Gid=?) or Job.JobLevel=1)) jhl,User u WHERE jhl.JobOwner=u.UID";
    private static Logger log = LogManager.getLogger(JobCRUDUtils.class);


    //TODO :report schema
    /**
     * TODO: Change to DBClient wait getGeneratedKeys
     * **/
    public synchronized static String addJobInfotoDB(Map args, String token) {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String querySQL="";
        try {
            Auth au = new Auth();
            ArrayList<Object> userInfo = au.verify(token);
            if (!(Boolean) userInfo.get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if (((Integer) userInfo.get(0)) > 0) {
                //INSERT SQL
                queryBean=PStmt.buildBatchUpdateBean("kado-meta",InsertJobSql,new ArrayList<Object[]>() {{
                    add(new Object[]{
                            ((String) args.get("jobname")),
                            (Integer) userInfo.get(2),
                            (Integer.parseInt((String) args.get("jobLevel"))),
                            ((String) args.get("memo")),
                            (Boolean) args.get("notification"),
                            (Boolean) args.get("storage")?((Double) args.get("save_type")).intValue():0,
                            (Integer.parseInt((String) args.get("location_id"))),
                            ((String) args.get("filepath")),
                            ((String) args.get("filename")),
                            ((String) args.get("insertsql")),
                            ((String) args.get("sql")),
                            (Boolean.valueOf((String)args.get("Report"))),
                            ((String) args.get("ReportEmail")),
                            (Integer.parseInt((String) args.get("ReportLength"))),
                            (Integer.parseInt((String) args.get("ReportFileType"))),
                            ((String) args.get("ReportTitle")),
                            (Boolean.valueOf((String) args.get("ReportWhileEmpty")))
                    });
                }});
                rs=dbClient.execute(queryBean);
                querySQL=queryBean.getSql();
                if(!rs.isSuccess())
                    throw rs.getException();

                String key = rs.getGeneratedPKList().get(0).toString();

                return MessageFactory.rtnJobMessage("success", TimeUtil.getCurrentTime(), "", key);
            } else {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");

            }
        }
        catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+querySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    //TODO :report schema
    public synchronized static String updateJobtoDB(Map args,int JobID, String token) {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        String QuerySQL = "";
        try {
            if (JobIsExist(JobID)) {

                Auth au = new Auth();
                if (!(Boolean) au.verify(token).get(4)) {
                    return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
                } else if ((au.jobMatch(token, Integer.toString(JobID))) || ((Integer) au.verify(token).get(0) == 2)) {

                    QuerySQL = UpdateJobSql;
                } else {
                    return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", Integer.toString(JobID));
                }
                //INSERT SQL

                queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                        ((String) args.get("jobname")),
                        (Integer.parseInt((String) args.get("jobLevel"))),
                        ((String) args.get("memo")),
                        (Boolean) args.get("notification"),
                        (Boolean) args.get("storage")?((Double) args.get("save_type")).intValue():0,
                        (Integer.parseInt((String) args.get("location_id"))),
                        ((String) args.get("filepath")),
                        ((String) args.get("filename")),
                        ((String) args.get("insertsql")),
                        ((String) args.get("sql")),
                        (Boolean.valueOf((String)args.get("Report"))),
                        ((String) args.get("ReportEmail")),
                        (Integer.parseInt((String) args.get("ReportLength"))),
                        (Integer.parseInt((String) args.get("ReportFileType"))),
                        ((String) args.get("ReportTitle")),
                        (Boolean.valueOf((String) args.get("ReportWhileEmpty"))),
                        JobID
                });
                rs=dbClient.execute(queryBean);

                if(!rs.isSuccess())
                    throw rs.getException();
                QuerySQL=queryBean.getSql();
                return MessageFactory.rtnJobMessage("success", TimeUtil.getCurrentTime(), "", Integer.toString(JobID));
            }
            else {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Job is not exit", Integer.toString(JobID));
            }
        }
        catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    //TODO :report schema
    public static String getJobInfo(int JobID){
        //DBClient
        String QuerySQL="";
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        try {
            QuerySQL = GetJobInfoSql;
            queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                    JobID
            });
            rs=dbClient.execute(queryBean);

            if(!rs.isSuccess())
                throw rs.getException();

            String rtn = MessageFactory.rtnJobInfoMessage(rs);

            return rtn;
        }
        catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    //TODO :report schema
    public static String getJobInfo(int JobID, String token) {
        //DBClient
        String QuerySQL="";
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        try {
            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((Integer) info.get(0) > 1) {
                QuerySQL = GetJobInfoSql;
                queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                        JobID
                });
            } else {
                QuerySQL = GetJobInfoSql_usr;
                queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                        JobID,
                        (Integer) info.get(1)
                });
            }
            //INSERT SQL
            QuerySQL=queryBean.getSql();

            rs=dbClient.execute(queryBean);

            if(!rs.isSuccess())
                throw rs.getException();

            String rtn = MessageFactory.rtnJobInfoMessage(rs);
            return rtn;
        }catch(Exception sqle){
            log.error(sqle);
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");

        }
    }
    //TODO :report schema
    public synchronized static String deleteJob(int JobID,String token) {
        //DBClient
        String QuerySQL="";
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        try {
            Auth au = new Auth();
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((au.jobMatch(token, Integer.toString(JobID))) || ((Integer) au.verify(token).get(0) == 2)) {
                QuerySQL = DeleteJobSql;
                queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                        JobID
                });
                rs=dbClient.execute(queryBean);
                if(!rs.isSuccess())
                    throw rs.getException();

                QuerySQL=queryBean.getSql();
                return MessageFactory.rtnJobMessage("success", TimeUtil.getCurrentTime(), "", Integer.toString(JobID));
            } else {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission Denied", Integer.toString(JobID));
            }
        }catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    //TODO :report schema
    public static String getJobList(String token) {
        //DBClient
        String QuerySQL="";
        Action queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        try {
            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((Integer) info.get(0) > 1) {
                QuerySQL = SelectAllJobSql;
                queryBean=new Query("kado-meta",QuerySQL);
            } else {
                QuerySQL = SelectJobListSql;
                queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                        (Integer) info.get(1)
                });
            }
            QuerySQL=queryBean.getSql();
            rs=dbClient.execute(queryBean);
            if(!rs.isSuccess())
                throw rs.getException();

            String rtn = MessageFactory.JobListMessage(rs, (Integer) info.get(2), (Integer) info.get(1), (String) info.get(3));

            return rtn;
        }catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");

        }
    }
    //TODO :report schema
    public static String getJobStatusList(String limit,String token)  {
        //DBClient
        String QuerySQL="";
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
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
                queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                        recordLimit
                });
            } else {
                QuerySQL = SelectJobExecutionList;
                queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                        (Integer) info.get(1),
                        recordLimit
                });
            }
            //INSERT SQL
            QuerySQL=queryBean.getSql();
            rs=dbClient.execute(queryBean);
            if(!rs.isSuccess())
                throw rs.getException();
            String rtn = MessageFactory.JobStatusListMessage(rs, (Integer) info.get(2), (Integer) info.get(1), (String) info.get(3));

            return rtn;
        }catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    public static String getJobStatusListFromCatch(Integer limit,String token){
        try {
            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);

            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            }
            Map<Integer,Map> dataCatchMap=JobHistoryCatch.getInstance().getJobStatusMap(limit);
            List<Map> resultList=new ArrayList<>();
            if ((Integer) info.get(0) > 0) {
                // get all list
                for (Integer jhid:dataCatchMap.keySet()){
                    resultList.add(dataCatchMap.get(jhid));
                }
            } else {
                //get group list
                //(Integer) info.get(1)
                for (Integer jhid:dataCatchMap.keySet()){
                    if(dataCatchMap.get(jhid)!=null&&dataCatchMap.get(jhid).get("group").toString().equals(info.get(1).toString()))
                        resultList.add(dataCatchMap.get(jhid));
                }
            }
            Collections.reverse(resultList);
            Map<String,Object> resultMap=new HashMap<>();

            resultMap.put("status","success");
            resultMap.put("time",TimeUtil.getCurrentTime());
            resultMap.put("list",resultList);

            return new Gson().toJson(resultMap);
        }catch (Exception e){
            log.error(ExceptionUtils.getMessage(e));
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), e.getMessage(), "");
        }
    }
    /**
     *  Get all job status list for status catch
     **/
    public static Map<Integer,Map> getAllJobStatusList(int limit) throws Exception {
        //DBClient
        String QuerySQL="";

        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        QuerySQL = SelectAllJobExecutionList;
        queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                limit
        });

        //INSERT SQL
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();
        Map<Integer,Map> rtn= MessageFactory.JobStatusListMessage(rs);

        return rtn;

    }
    //TODO :report schema
    public static String getJobHistoryList(String start, String stop,String jobID,String token) {
        //DBClient
        String QuerySQL="";
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        try {


            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);

            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if (!(start.equals("") || stop.equals("") || jobID.equals(""))) {

                if ((Integer) info.get(0) > 1) {
                    QuerySQL = SelectHistoryJobList_timeandjobId;
                    queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                            start,
                            stop,
                            Integer.parseInt(jobID)
                    });
                } else {
                    QuerySQL = SelectHistoryJobList_timeandjobId_user;
                    queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                            start,
                            stop,
                            Integer.parseInt(jobID),
                            (Integer) info.get(1)
                    });
                }
            } else if (!jobID.equals("")) {
                if ((Integer) info.get(0) > 1) {
                    QuerySQL = SelectHistoryJobList_jobID;

                    queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                            Integer.parseInt(jobID)
                    });
                } else {
                    QuerySQL = SelectHistoryJobList_jobID_user;

                    queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                            Integer.parseInt(jobID),
                            (Integer) info.get(1)
                    });
                }
            } else if (!(start.equals("") || stop.equals(""))) {
                if ((Integer) info.get(0) > 1) {
                    QuerySQL = SelectHistoryJobList_time;

                    queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                            start,
                            stop
                    });
                } else {
                    QuerySQL = SelectHistoryJobList_time_user;

                    queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                            start,
                            stop,
                            (Integer) info.get(1)
                    });
                }
            } else {
                DateTime dt = new DateTime();

                return MessageFactory.rtnJobMessage("error", dt.toString("yyyy-MM-dd HH:mm:ss.SSS"), "illegal parameter", jobID);

            }
            QuerySQL=queryBean.getSql();
            rs=dbClient.execute(queryBean);
            if(!rs.isSuccess())
                throw rs.getException();
            String rtn = MessageFactory.HistoryListMessage(rs, (Integer) info.get(2), (Integer) info.get(1), (String) info.get(3));

            return rtn;
        }catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    public static String getHasResultJobHistory(String jobID,String token) throws Exception {
        //DBClient
        String QuerySQL="";
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        Auth au = new Auth();
        if (!(Boolean) au.verify(token).get(4)) {
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
        }else {
            QuerySQL = "SELECT jh.JHID,jh.JobID,jh.JobStartTime,jl.ResultCount,jl.JobOutput FROM Job_History as jh  JOIN Job_Log jl  ON jh.JobLog=jl.JLID WHERE jh.JobID=? AND jl.ResultCount>0 Order By jh.JobStartTime DESC limit ?";

            queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                    Integer.parseInt(jobID),
                    10,
            });
            rs=dbClient.execute(queryBean);
            if(!rs.isSuccess())
                throw rs.getException();

            String rtn = MessageFactory.hasResultJobHistory(rs);

            return rtn;
        }
    }
    //TODO :report schema
    public static String getJobHistoryInfo(String token, int runid) {
        //DBClient
        String QuerySQL="";
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        try {

            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((Integer) info.get(0) > 1) {
                QuerySQL = SelectJobHistoryInfo;

                queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                        runid
                });
            } else {
                QuerySQL = SelectJobHistoryInfo_user;

                queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                        runid,
                        (Integer) info.get(1)
                });
            }
            //INSERT SQL
            QuerySQL=queryBean.getSql();
            rs=dbClient.execute(queryBean);
            if(!rs.isSuccess())
                throw rs.getException();
            String rtn = MessageFactory.JobHistoryInfoMessage(rs, (Integer) info.get(2), (Integer) info.get(1), (String) info.get(3));

            return rtn;
        }catch(Exception sqle){
            log.error(sqle);
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    public static Map getJobHistoryInfo(int runid) {
        //DBClient
        String QuerySQL="";
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        try {
            QuerySQL = SelectJobHistoryInfo;
            queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                    runid
            });

            QuerySQL=queryBean.getSql();
            rs=dbClient.execute(queryBean);
            if(!rs.isSuccess())
                throw rs.getException();
            Map rtn = MessageFactory.JobHistoryInfoMessage(rs);

            return rtn;
        }catch(Exception sqle){
            log.error(sqle);
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return new LinkedHashMap();
        }
    }
    //TODO :report schema
    public synchronized static int InsertJobHistory(ArrayList<String> args)throws Exception{

        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        //INSERT SQL

        queryBean=PStmt.buildBatchUpdateBean("kado-meta",InsertJobHistorySql,new ArrayList<Object[]>(){{
            add(new Object[]{
                    Integer.parseInt(args.get(0)),//JobID
                    args.get(1),//PrestoID
                    Integer.parseInt(args.get(2)),//JobOwner
                    Integer.parseInt(args.get(3)),//JobLevel
                    args.get(4),//JobStartTime
                    args.get(5).isEmpty()?null:args.get(5),//JobStopTime
                    Integer.parseInt(args.get(6)),//JobStatus
                    Integer.parseInt(args.get(7)),//JobProgress
                    Integer.parseInt(args.get(8)),//JobLog
                    Integer.parseInt(args.get(9)),//JobType
                    Boolean.valueOf(args.get(10)),
                    args.get(11),
                    (Integer.parseInt( args.get(12))),
                    (Integer.parseInt(args.get(13))),
                    args.get(14),
                    Boolean.valueOf(args.get(15))
            });
        }});
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();

        //return JobHistoryID
        if(rs.getGeneratedPKList().size()>0)
            return rs.getGeneratedPKList().get(0).intValue();
        else
            return -1;
    }
    //TODO :report schema
    public synchronized static void UpdateJobHistory(int JobHistoryID,String JobStartTime, String JobStopTime, int JobStatus, int JobProgress)throws Exception{

        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        //INSERT SQL
        queryBean=PStmt.buildQueryBean("kado-meta",UpdateJobHistorySql,new Object[]{
                JobStartTime,
                JobStopTime.isEmpty()?null:JobStopTime,
                JobStatus,
                JobProgress,
                JobHistoryID
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();
    }

    public synchronized static int InsertJobLog(ArrayList<String> args)throws Exception{

        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        //INSERT SQL
        queryBean=PStmt.buildBatchUpdateBean("kado-meta",InsertJobLogSql,new ArrayList<Object[]>(){{
            add(new Object[]{
                    args.get(0),//JobSql
                    args.get(1),//JobOutPut
                    args.get(2),//JobLogFile
                    Integer.parseInt(args.get(3)),//JobStorageType
                    Integer.parseInt(args.get(4)),//StorageResources
                    args.get(5),//FilePath
                    args.get(6),//FileName
                    args.get(7),//DBSQL
                    Integer.parseInt(args.get(8)),//Replace_Value
                    args.get(9),//Replace_Sign
                    Integer.parseInt(args.get(10)),//ResulCount
                    Integer.parseInt(args.get(11)),//Valid
                    Boolean.valueOf(args.get(12))//Valid
            });
        }});
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();

        //return JobLogID
        if(rs.getGeneratedPKList().size()>0)
            return rs.getGeneratedPKList().get(0).intValue();
        else
            return -1;
    }

    public synchronized static void UpdateJobLog(int JobLogID,int resultCount, String JobOutPut,Boolean valid)throws Exception{
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        //INSERT SQL
        queryBean=PStmt.buildQueryBean("kado-meta",UpdateJobLogSql,new Object[]{
                resultCount,
                JobOutPut,
                valid?true:false,
                JobLogID
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();

    }

    public static boolean JobIsExist(int JobID) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        //Check User Account and Password
        String sql = "select `JobName` from `Job` where  JobID=?";

        queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                JobID
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();

        return rs.getRowSize()>0;
    }

}