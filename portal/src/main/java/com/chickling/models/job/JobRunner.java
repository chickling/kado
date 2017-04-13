package com.chickling.models.job;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.chickling.bean.job.Job;
import com.chickling.bean.job.JobLog;
import com.chickling.bean.result.ResultMap;
import com.chickling.face.ResultWriter;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.chickling.boot.Init;
import com.chickling.models.Auth;
import com.chickling.models.ControlManager;
import com.chickling.util.*;
import com.chickling.bean.job.JobHistory;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;


/**
 * Created by gl08 on 2015/12/3.
 */
public class JobRunner   implements Callable<Boolean> {
    public Logger log= LogManager.getLogger(JobRunner.class);
    private  Gson gson=new Gson();
    private  int jobid=0;
    private Job job;
    private int jobType=PrestoContent.QUERY_UI;
    private AtomicInteger page=new AtomicInteger(1);
    private String job_process="0";
    private String jobHistoryCatchKey;
    private int scheduleHistoryID=Integer.MIN_VALUE;
    private int jobSortIndex=0;
    private int userLevel=Integer.MIN_VALUE;
    private StringBuilder exception=new StringBuilder();

    @Override
    public Boolean call() {

        Boolean isSuccess=Boolean.TRUE;
        PrestoUtil prestoUtil=new PrestoUtil();

        //******************************************************************************************
        //  Initialized
        //******************************************************************************************
        int resultCount=0;
        boolean isAdmin=false;
        boolean isManager=false;
        String result = "";
        String jobUUID=UUID.randomUUID().toString().replaceAll("-","");
        String tempdb=Init.getDatabase();
        String tempTableName =tempdb+ ".temp_" + jobUUID;


        // if user is Admin
        if (PrestoContent.ADMIN.equals(userLevel))
            isAdmin=true;
        else if (PrestoContent.MANAGER.equals(userLevel))
            isManager=true;
        //set Log FileName  for Log4j Router Appender
        String logFileName= "joblog-"+jobUUID;
        ThreadContext.put("logFileName", logFileName);
        log.info("log File name : " + logFileName);

        //******************************************************************************************
        // Replace Sql  Conditions
        // Current Time is 2016-01-07 15:32:00
        // ex : $lase 24 hour$ --> (dt='20160106' and hour>='15') or (dt>'20160106')
        //******************************************************************************************
        try {
            String jobsql=job.getSql();
            JSONObject queryReplace= (JSONObject)PrestoGrammarUtil.parseConst(jobsql, new HashMap<>()).get("constConditions");
            Iterator keys=queryReplace.keys();
            while (keys.hasNext()){
                String key= (String) keys.next();
                jobsql=jobsql.replace(key, queryReplace.getString(key));
            }

            job.setSql(jobsql.trim());
            log.info("Query Replace Finished");
        } catch (JSONException e) {
            log.error("Replace Query Error  " + e);
            this.exception.append(ExceptionUtils.getStackTrace(e)).append("\n");
            isSuccess=Boolean.FALSE;
            return isSuccess;
        }

        String dropTable="DROP TABLE if EXISTS "+ tempTableName;
        String tmpsql="CREATE TABLE "+ tempTableName +" WITH (format='ORC' ) AS SELECT ";
        String sql=replaceSQL(job.getSql(),tmpsql);
        Integer jobHistoryid = 0;

        try {
            //******************************************************************************************
            // Drop Exists Tmp Table , Do not needs this if USER is Admin
            //******************************************************************************************
            if (!isAdmin){
                try {
//                    prestoUtil.post(dropTable, jobType, Init.getDatabase());
                    prestoUtil.doJdbcRequest(dropTable);
                } catch (Exception e) {
                    log.error("drop table " + tempTableName + " error : " + e);
                    this.exception.append(e.getMessage()).append("\n");
                    return Boolean.FALSE;
                }
                log.info("Drop Temp Table " + tempTableName + "  finish");
            }
            //******************************************************************************************
            // Set Output Path  Job Result and LogFile  in HDFS
            // use Matcher Patten , support those
            // create table t1  | CREATE table if not exists t1
            // drop table t2 | DROP  if exists t2
            // insert into t3 | INSERT INTO t3
            ///******************************************************************************************
            String jobOutPut=Init.getHivepath()+"/"+Init.getDatabase()+".db/temp_"+jobUUID;
            String logFileOutPut=Init.getLogpath()+Init.getFileseparator()+ThreadContext.get("logFileName")+".log";
            String[] tableFullName;
            List<String> tableFullNameLsit=new ArrayList<>();
            Matcher matcher=PrestoContent.SQL_PASER.matcher(sql.trim());
            if (! sql.trim().toLowerCase().startsWith("select")){
                if (isAdmin || isManager){
                    if (matcher.find()) {
                        if (!Strings.isNullOrEmpty(matcher.group(22))){
                            tableFullName=matcher.group(22).split("\\.");
                            tableFullNameLsit.addAll(Arrays.asList(tableFullName));
                            jobOutPut = Init.getHivepath() + "/"+tableFullName[0]+ ".db/"+matcher.group(22).replaceFirst(tableFullName[0]+"\\.","");
                        }
                    }
                }
            }

            Integer jobLogid = 0;

            //******************************************************************************************
            // Start Job
            //******************************************************************************************
            try {
                if (isAdmin || isManager) {
                    if (job.getSql().toLowerCase().trim().startsWith("select")) {
                        job.setSql(sql);
                        sql = sql.trim();
                    } else{
                        // Manager only can use TempDB , if use other , response Permission
                        if (isManager)
                            if ( ! tableFullNameLsit.get(0).equalsIgnoreCase(tempdb)
                                    ||  !tableFullNameLsit.get(1).equalsIgnoreCase(tempdb) ) {
                                throw new Exception("Permission denied !!  Only Can User the TempDB : " + tempdb);
                            }
                        tempTableName=matcher.group(22);
                        sql =job.getSql().trim();
                    }
                }else  if (!job.getSql().trim().replaceAll("\\n", " ").toLowerCase().startsWith("select"))
                    throw new Exception("Permission denied");

                // replace last ";" to empty
                while (true){
                    if (sql.lastIndexOf(";")==sql.length()-1){
                        sql=sql.substring(0,sql.length()-1).trim();
                    }else
                        break;
                }
                job.setSql(sql);
                sql = sql.trim().replaceAll("\\n", " ");
                log.info("execute SQL : \n "+sql);
                result =prestoUtil.postStatement(sql, jobType, Init.getDatabase());
                Thread.sleep(PrestoContent.JOB_START_WAIT_TIME);
            } catch (Exception e) {
                log.error("Start Job Error : "+e);
                this.exception.append(ExceptionUtils.getStackTrace(e)).append("\n");
                isSuccess=Boolean.FALSE;

                JobLog jobLog=new JobLog(Base64.getEncoder().encodeToString(job.getSql().getBytes()),jobOutPut.trim(),logFileOutPut.trim(),job.getSave_type(),job.getLocation_id(),job.getFilepath(),job.getFilename(),Base64.getEncoder().encodeToString(job.getInsertsql().getBytes()),job.getReplace_value(),job.getReplace_sign(),0,1,job.getReporWhileEmpty());
                try {
                    jobLogid =JobCRUDUtils.InsertJobLog(jobLog.getInsertList());
                    JobHistory jobHistory=new JobHistory(jobid, "job_error",job.getJobowner(),job.getJobLevel(), TimeUtil.getCurrentTime(),TimeUtil.getCurrentTime(),PrestoContent.FAILED,"0", jobLogid,String.valueOf(jobType),job.getReport(),job.getReportEmail(),job.getReportLength(),job.getReportFileType(),job.getReportTitle(),job.getReporWhileEmpty());
                    jobHistoryid =JobCRUDUtils.InsertJobHistory(jobHistory.getInsertList());
                    if(!Strings.isNullOrEmpty(jobHistoryCatchKey)){
                        JobHistoryCatch.getInstance().jobHistoryIDs.put(jobHistoryCatchKey,jobHistoryid);
                        log.info("Put JobHistoryID to JobHistoryCatch,Key:"+jobHistoryCatchKey+" Value:"+jobHistoryid);
                    }
                    log.info("jobHistoryid:"+jobHistoryid);
                    if(scheduleHistoryID>0){
                        log.info("Insert New Record to Schedule_History");
                        log.info(ScheduleCRUDUtils.insertScheduleJobHistory(scheduleHistoryID, jobHistoryid, jobid, jobSortIndex));
                    }
                } catch (SQLException e1) {
                    log.error("Insert Job Log  Error  : " + ExceptionUtils.getStackTrace(e1));
                    this.exception.append(ExceptionUtils.getStackTrace(e1)).append("\n");
                }
                return isSuccess;
            }
            //get Start Job Response  Info and Presto ID
            HashMap queryMap= gson.fromJson(result, HashMap.class);
            String prestoid = ((String) queryMap.get("id"));

            //******************************************************************************************
            //  Insert New Record to Job_Log   FIRST
            //******************************************************************************************

            JobLog jobLog=new JobLog(Base64.getEncoder().encodeToString(job.getSql().getBytes()),jobOutPut.trim(),logFileOutPut.trim(),job.getSave_type(),job.getLocation_id(),job.getFilepath(),job.getFilename(),Base64.getEncoder().encodeToString(job.getInsertsql().getBytes()),job.getReplace_value(),job.getReplace_sign(),0,1,job.getReporWhileEmpty());
            try {
                log.info("Insert New Record to Job_Log");
                jobLogid =JobCRUDUtils.InsertJobLog(jobLog.getInsertList());
            } catch (Exception e) {
                log.error("Insert Job Log  Error  : " + ExceptionUtils.getStackTrace(e));
                try {
                    prestoUtil.delete(prestoid, jobType);
                    prestoUtil.doJdbcRequest(dropTable);
//                    prestoUtil.post(dropTable, jobType, Init.getDatabase());
                } catch (Exception e1) {
                    log.error("Delete job "+prestoid + "Error  : " +e1);
                }
                this.exception.append(ExceptionUtils.getStackTrace(e)).append("\n");
                isSuccess=Boolean.FALSE;
                return isSuccess;
            }
            //******************************************************************************************
            //  Insert New Record to JobHistory
            //******************************************************************************************
            JobHistory jobHistory=new JobHistory(jobid, prestoid,job.getJobowner(),job.getJobLevel(), TimeUtil.getCurrentTime(),"",PrestoContent.RUNNING,"0", jobLogid,String.valueOf(jobType),job.getReport(),job.getReportEmail(),job.getReportLength(),job.getReportFileType(),job.getReportTitle(),job.getReporWhileEmpty());

            try {
                log.info("Insert New Record to Job_History");
                jobHistoryid =JobCRUDUtils.InsertJobHistory(jobHistory.getInsertList());
                log.info("jobHistoryid:"+jobHistoryid);
                if(!Strings.isNullOrEmpty(jobHistoryCatchKey)){
                    JobHistoryCatch.getInstance().jobHistoryIDs.put(jobHistoryCatchKey,jobHistoryid);
                    log.info("Put JobHistoryID to JobHistoryCatch,Key:"+jobHistoryCatchKey+" Value:"+jobHistoryid);
                }
                if(scheduleHistoryID>0){
                    log.info("Insert New Record to Schedule_History");
                    log.info(ScheduleCRUDUtils.insertScheduleJobHistory(scheduleHistoryID, jobHistoryid, jobid, jobSortIndex));
                }

            } catch (SQLException e) {
                log.error("Insert Job History Error  : "+ ExceptionUtils.getStackTrace(e));
                try {
                    prestoUtil.delete(prestoid,jobType);
                    prestoUtil.doJdbcRequest(dropTable);
//                    prestoUtil.post(dropTable, jobType, Init.getDatabase());
                } catch (Exception e1) {
                    log.error("Delete job [ "+prestoid + " ] Error  : " +e1);
                }
                this.exception.append(ExceptionUtils.getStackTrace(e)).append("\n");
                isSuccess=Boolean.FALSE;
                return isSuccess;
            }
            //******************************************************************************************
            //  Start Job Process
            //******************************************************************************************
            String jobstatus="";
            boolean jobRunning=true;
            boolean isdelete=false;
            jobHistory.setStatus(PrestoContent.RUNNING.toString());

            try {
                do {
                    // todo add if result file at HDFS file Size over @PrestoContent.JOB_RESULT_SIZE
                    // stop Job command  or IDLE or HDFS size too big
                    if (TimeUtil.String2DateTime(jobHistory.getStart_time()).getMillis() - System.currentTimeMillis() > PrestoContent.JOB_IDLE_TIME
                            || Init.getDeleteJobList().contains(jobHistoryid)
                            ){
                        try {
                            // delete presto job, drop temp table , remove jobhistory fom deletejobList
                            prestoUtil.delete(prestoid,jobType);
                            prestoUtil.doJdbcRequest(dropTable);
//                            prestoUtil.post(dropTable, jobType, Init.getDatabase());
                            Init.getDeleteJobList().remove(jobHistoryid);
                            isdelete=true;
                        } catch (Exception e) {
                            this.exception.append(ExceptionUtils.getStackTrace(e)).append("\n");
                            log.error("Delete job "+prestoid + "Error  : " +e);
                        }
                        log.info("Cancel Job Success ");
                        isSuccess=Boolean.FALSE;
                        return isSuccess;
                    }
                    //******************************************************************************************
                    // get Presto Job Process from post Statement
                    //******************************************************************************************
                    result = prestoUtil.getStatement(prestoid, String.valueOf(page), jobType);
                    queryMap= gson.fromJson(result, HashMap.class);

                    // if result not Empty
                    if (!Strings.isNullOrEmpty(result)){
                        //******************************************************************************************
                        // Job Status
                        //******************************************************************************************
                        jobstatus= (String) ((LinkedTreeMap)queryMap.get("stats")).get("state");

                        //todo if  ERROR occurred   , Kill This Job  and Throw Exception
                        if (queryMap.containsKey("error") || "FAILED".equals(jobstatus)) {
                            log.error("Query Error , Kill this Job !!!!");
                            prestoUtil.delete(prestoid, jobType);
                            prestoUtil.doJdbcRequest(dropTable);
//                            prestoUtil.post(dropTable, jobType, Init.getDatabase());
                            jobHistory.setStatus(PrestoContent.FAILED.toString());
                            isdelete=true;
                            isSuccess=Boolean.FALSE;
                            throw new Exception((String) ((LinkedTreeMap) queryMap.get("error")).get("message"));
                        }
                        //******************************************************************************************
                        //  Process Info
                        //******************************************************************************************
                        Number complete = (Number) ((LinkedTreeMap) queryMap.get("stats")).get("completedSplits");
                        Number total = (Number) ((LinkedTreeMap) queryMap.get("stats")).get("totalSplits");
                        //todo 判斷 NAN
                        String tmp_process="0";
                        Double process=(complete.doubleValue() / total.doubleValue())*100;
                        if (!process.equals(Double.NaN))
                            tmp_process=new DecimalFormat("##").format(process);

                        if (!job_process.equals(tmp_process))
                            job_process=tmp_process;

                        if (0 != complete.intValue()){
                            if (! "100".equals(job_process)){
                                log.info("Job Process : " + job_process + " %");
                            }
                        }else{
                            log.info("Job Process : 0 %");
                        }
                        jobHistory.setProgress(job_process);

                        if ("FINISHED".equals(jobstatus)) {

                            jobHistory.setProgress("100");
                            log.info("Job Process :  100 %");
                            log.info("Job Finished !! ");
                            jobRunning=false;
                        }

                        if (queryMap.containsKey("nextUri"))
                            page.getAndIncrement();
                    }else{
                        jobHistory.setJob_status(PrestoContent.FAILED.toString());
                        throw new Exception("Job Running Error ");
                    }
                    //******************************************************************************************
                    // Update Job_History
                    //******************************************************************************************
                    JobCRUDUtils.UpdateJobHistory(jobHistoryid,jobHistory.getStart_time(),"",Integer.parseInt(jobHistory.getJob_status()),Integer.parseInt(jobHistory.getProgress()));
                    // sleep Thread , and go on next
                    Thread.sleep(PrestoContent.JOB_STATUS_INTERVAL);

                } while(jobRunning);

                if ("FINISHED".equals(jobstatus)) {
                    jobHistory.setJob_status(PrestoContent.FINISH.toString());
                }
                //******************************************************************************************
                // Do count result
                //******************************************************************************************
                if (!sql.toLowerCase().startsWith("drop") && !sql.toLowerCase().startsWith("insert")) {
                    log.info("Finish Presto job , now start row Count");
                    String countStr="select count(*)  from "+ tempTableName;
                    ResultMap resultMap=prestoUtil.doJdbcRequest(countStr);
                    if (resultMap.getCount()>0)
                        resultCount=Integer.parseInt(resultMap.getData().get(0).get(0).toString());
//                    }
                    log.info("Result Count is : " + resultCount);
                }else
                    log.info("Is Drop or Insert Job , we don't COUNT this job result");
                //******************************************************************************************
                // Start Storage Result
                //******************************************************************************************
                if (resultCount>0 && job.getSave_type()>0){
                    // calculate save type be Binary
                    //
                    String binarySaveType=getDestinationBinary(job.getSave_type(),3);
                    char [] tmp=binarySaveType.toCharArray();
                    List<String> activeWriter=new ArrayList<>();
                    for (int i = tmp.length,index=0; i >0 ; i--,index++) {
                        activeWriter.add(String.valueOf(tmp[i-1]));
                    }

                    HashMap<String, Object> parameter=new HashMap<>();
                    parameter.put("jobLog",jobLog);
                    parameter.put("location_id",job.getLocation_id());
                    parameter.put("insertsql",job.getInsertsql());
                    parameter.put("resultCount",resultCount);
                    parameter.put("tableName",tempTableName);
                    // start Writer
                    //
                    if(!doWriter(activeWriter,parameter)){
                        isSuccess=Boolean.FALSE;
                        return isSuccess;
                    }
                }else
                    log.info("No Result to Storage !! ");
                return Boolean.TRUE;
            } catch (Exception e) {
                jobHistory.setJob_status(PrestoContent.FAILED.toString());
                log.error("Do Job Error , " + ExceptionUtils.getStackTrace(e));
                prestoUtil.delete(prestoid, jobType);
                if (!prestoUtil.isSuccess())
                    log.error("Delete job "+prestoid + "Error  : " +prestoUtil.getException());
                prestoUtil.doJdbcRequest(dropTable);
//                prestoUtil.post(dropTable, jobType, Init.getDatabase());
                if (!prestoUtil.isSuccess())
                    log.error("Drop Table "+tempTableName + "Error  : " +prestoUtil.getException());
                isSuccess=Boolean.FALSE;
                this.exception.append(ExceptionUtils.getStackTrace(e)).append("\n");
                return isSuccess;
            }finally {
                //******************************************************************************************
                // Recode This Job Info to Job_History , Job_Log
                //******************************************************************************************
                try {
                    log.info("Insert Final Info to Job_History and Job_Log");
                    if (isdelete)
                        jobHistory.setJob_status(PrestoContent.FAILED.toString());
                    log.info("Job Status is  : ["+("1".equalsIgnoreCase(jobHistory.getJob_status()) ? "Success" : "Failed")+"] and Job Process is : ["+jobHistory.getProgress()+"% ]");
                    JobCRUDUtils.UpdateJobHistory(jobHistoryid, jobHistory.getStart_time(), TimeUtil.getCurrentTime(), Integer.parseInt(jobHistory.getJob_status()), Integer.parseInt(jobHistory.getProgress()));
                    JobCRUDUtils.UpdateJobLog(jobLogid, resultCount, jobOutPut, true);
                    log.info("Finished Last Update JobHistory and JobLog");
                } catch (Exception e) {
                    log.error("Close Job  Error : " + e);
                    this.exception.append(ExceptionUtils.getStackTrace(e)).append("\n");
                    isSuccess=Boolean.FALSE;
                }
            }
        }finally {
            if (!isSuccess && job.getNotification() && !PrestoContent.QUERY_UI.equals(jobType)){
                Notification.notification(jobHistoryid,this.exception.toString(),"Info",null);
            }
            /**Get report **/
            if(job.getReport() )
                if (resultCount>0 || ( resultCount==0 && job.getReporWhileEmpty() ) ){
                    StringBuilder content=new StringBuilder();
                    String success="<font color='green'>Success</font>";
                    if(!isSuccess){
                        success="<font color='red'>Fail</font>";
                    }
                    content.append("<html><body>");
                    content.append("<h4><font color='blue'>Job Name:</font>"+job.getJobname()+"</h4>");
                    content.append("<h4><font color='blue'>Job Memo:</font>"+job.getMemo()+"</h4>");
                    content.append("<h4><font color='blue'>Job Status:</font>"+success+"</h4>");
                    content.append("<h4><font color='blue'>Result Count:</font>"+resultCount + "</h4>");
                    content.append("<p><font color='blue'>Message:</font>"+job.getReportTitle()+"</p>");
                    if(resultCount>0){
                        ControlManager cm = new ControlManager();
                        String rs=cm.getResultPageTable(jobHistoryid, 0, job.getReportLength());
                        content.append(rs);
                        if(job.getReportFileType()>0){
                            content.append("<p><h4><a href='"+Init.getSiteURLBase() + "/query/get/result/file/"+jobHistoryid+"/"+new Auth().generateDownloadToken(jobHistoryid)+ "'>Download Result CSV File...</a></h4></p>");
                        }
                    }
                    content.append("<p>" + TimeUtil.getCurrentTime() + " Send From <a href='" + Init.getSiteURLBase()+"'>Kado</a></p>");
                    content.append("</body></html>");
                    String[] recipients=job.getReportEmail().split(";");
                    Notification.notification(jobHistoryid, content.toString(), "(info)[Job Report][" + jobHistoryid+"]"+job.getJobname(), recipients);
                }
            log.info("Job End");
            StopLogger.stopLogger(log);
            ThreadContext.remove("logFileName");
        }
    }

    /**
     * @param headerSQL    header String
     * @return                       replace SQL
     */
    private String replaceSQL(String sql,String headerSQL){
        if (sql.toLowerCase().startsWith("select")){
            Matcher matcher=PrestoContent.SQL_SELECT_PASER.matcher(sql);
            String firstSelectStr="";
            if(matcher.find()){
                firstSelectStr=matcher.group();
            }
            return sql.replaceFirst(firstSelectStr,headerSQL);
        }
        return sql;
    }

    private String setTemplateValue(int jobid,String sql, Map template){
        //TODO test
        String rtnSql=sql;
        List<Map> templateInfo=TemplateCRUDUtils.readSqlTemplate(jobid);
        HashMap<String,String> t=checkmap(templateInfo,template);
        for (Map.Entry<String,String> entry : t.entrySet())
        {
            rtnSql=rtnSql.replace(entry.getKey(),entry.getValue());
        }
        return rtnSql;
    }

    private HashMap<String,String> checkmap(List<Map> list,Map inputMap){
        //TODO test
        HashMap<String, String> rtnMap=new HashMap<String, String>();
        for(Map m: list){
            if(inputMap.get(m.get("URLKey"))!=null){
                rtnMap.put((String)m.get("SQLKey"),(String)inputMap.get((String)m.get("URLKey")));
            }
            else{
                rtnMap.put((String)m.get("SQLKey"),(String)m.get("DefaultValue"));
            }
        }
        return rtnMap;
    }

    /**
     * @param jobID         must set this , if use by QueryUI set zero
     * @param jobType     must set this , Query UI or User Job or Schedule , you can set this By PrestoContent.class
     * @param autoken     must set this , Valid  user
     * @param sql
     * @throws Exception
     */
    public JobRunner(Integer jobID,Integer jobType,String autoken,String... sql) throws Exception{
        assert jobID!=null;
        assert jobType!=null;
        assert !Strings.isNullOrEmpty(autoken);

        this.jobid=jobID;
        this.jobType=jobType;

        Auth au = new Auth();
        ArrayList <Object> userInfo=au.verify(autoken);
        if(!Strings.isNullOrEmpty(userInfo.get(0).toString()))
            this.userLevel=(Integer)userInfo.get(0);
        if (jobID!=0)
        {
            this.job = gson.fromJson(JobCRUDUtils.getJobInfo(this.jobid,autoken), Job.class);
            this.job.setSql(new String(Base64.getDecoder().decode(this.job.getSql()), "UTF-8"));
        } else{
            this.job=new Job();
            setRealTimeJob(userInfo, sql);
        }
        job.setJobowner((Integer) userInfo.get(2));
        //todo get JOB info
    }
    //for the query with no template
    public JobRunner(Integer jobID,Integer jobType,String autoken,String jobHistoryCatchKey,String sql) throws Exception {

        assert jobID!=null;
        assert jobType!=null;
        assert !Strings.isNullOrEmpty(autoken);

        this.jobid=jobID;
        this.jobType=jobType;


        Auth au = new Auth();
        ArrayList <Object> userInfo=au.verify(autoken);
        if(!Strings.isNullOrEmpty(userInfo.get(0).toString()))
            this.userLevel=(Integer)userInfo.get(0);
        if (jobID!=0)
        {
            this.job = gson.fromJson(JobCRUDUtils.getJobInfo(this.jobid,autoken), Job.class);
            this.job.setSql(new String(Base64.getDecoder().decode(this.job.getSql()), "UTF-8"));

        } else{
            this.job=new Job();
            setRealTimeJob(userInfo, sql);
        }
        job.setJobowner((Integer) userInfo.get(2));
        this.jobHistoryCatchKey=jobHistoryCatchKey;
        //todo get JOB info
    }
    //for the query with template
    public JobRunner(Integer jobID,Integer jobType,String autoken,Map template,String jobHistoryCatchKey,String... sql) throws Exception {

        assert jobID!=null;
        assert jobType!=null;
        assert !Strings.isNullOrEmpty(autoken);

        this.jobid=jobID;
        this.jobType=jobType;


        Auth au = new Auth();
        ArrayList <Object> userInfo=au.verify(autoken);
        if(!Strings.isNullOrEmpty(userInfo.get(0).toString()))
            this.userLevel=(Integer)userInfo.get(0);
        if (jobID!=0)
        {
            this.job = gson.fromJson(JobCRUDUtils.getJobInfo(this.jobid,autoken), Job.class);
            String jobSql=new String(Base64.getDecoder().decode(this.job.getSql()), "UTF-8");
            this.job.setSql(setTemplateValue(this.jobid,jobSql,template));

        } else{
            this.job=new Job();
            setRealTimeJob(userInfo, sql);
        }
        job.setJobowner((Integer) userInfo.get(2));
        this.jobHistoryCatchKey=jobHistoryCatchKey;
        //todo get JOB info
    }

    public JobRunner(Integer jobID,Integer jobType,String UserLevel,int ScheduleHistoryID,int JobSortIndex,int jobOwner) throws Exception {
        assert jobID!=null;
        assert jobType!=null;
        assert !Strings.isNullOrEmpty(UserLevel);

        this.userLevel=Integer.parseInt(UserLevel);
        //this.userToken=autoken;
        this.jobid=jobID;
        this.jobType=jobType;
        this.scheduleHistoryID=ScheduleHistoryID;
        this.jobSortIndex = JobSortIndex;
        this.job = gson.fromJson(JobCRUDUtils.getJobInfo(this.jobid), Job.class);
        this.job.setJobowner(jobOwner);
        this.job.setSql(new String(Base64.getDecoder().decode(this.job.getSql()), "UTF-8"));

    }

    public JobRunner(Integer jobID,Integer jobType,String UserLevel,int ScheduleHistoryID,int JobSortIndex,int jobOwner,String jobHistoryCatchKey) throws Exception {
        assert jobID!=null;
        assert jobType!=null;
        assert !Strings.isNullOrEmpty(UserLevel);

        this.userLevel=Integer.parseInt(UserLevel);
        this.jobid=jobID;
        this.jobType=jobType;
        this.scheduleHistoryID=ScheduleHistoryID;
        this.jobSortIndex = JobSortIndex;
        this.job = gson.fromJson(JobCRUDUtils.getJobInfo(this.jobid), Job.class);
        this.job.setJobowner(jobOwner);
        this.job.setSql(new String(Base64.getDecoder().decode(this.job.getSql()), "UTF-8"));
        this.jobHistoryCatchKey=jobHistoryCatchKey;

    }

    private void setRealTimeJob(ArrayList<Object> auList,String... sql) throws Exception {
        this.job.setJobowner((Integer) auList.get(2));
        if (PrestoContent.ADMIN.equals(this.userLevel) || PrestoContent.MANAGER.equals(this.userLevel)) {
            this.job.setSql(sql[0]);
        }else {
            //If user not set limit then auto add limit
            Matcher matcher=PrestoContent.LIMIT_CHECK.matcher(sql[0].toLowerCase());
            if(matcher.find()){
                this.job.setSql(sql[0]);
            }else {
                this.job.setSql(sql[0] + " limit " + PrestoContent.REALTIME_QUERY_LIMIT);
            }
        }
    }
    /**
     * ex: 3 to '010'
     * @param saveType
     * @param destSize
     * @return
     */
    private String getDestinationBinary(int saveType , int destSize){
        String type=Integer.toBinaryString(saveType);
        while (type.length()<destSize){
            type="0"+type;
        }

        return type;
    }

    private boolean doWriter(List<String> activeWriter , Map parameter) throws Exception {

        int resultCode = 0;
//            HDFS binary  100
        String name = "";
//        if ("1".equals(activeWriter.get(0))) {
//            name = "com.chickling.models.writer.HdfsWriter";
//            ResultWriter hdfsWriter =Init.getInjectionInstance(name);
//            hdfsWriter.init(parameter);
//            resultCode += (int) hdfsWriter.call();
//        }
        //LOCAL  binary 010
        //
        if ("1".equals(activeWriter.get(1))) {
            name = "com.chickling.models.writer.LocalWriter";
            ResultWriter localWriter =Init.getInjectionInstance(name);
            localWriter.init(parameter);
            resultCode += (int) localWriter.call();
        }
//            DB binary 001
        if ("1".equals(activeWriter.get(2))) {
            name = "com.chickling.writer.DBWriter";
            ResultWriter dbWriter =Init.getInjectionInstance(name);
            dbWriter.init(parameter);
            resultCode += (int) dbWriter.call();
        }

        return resultCode > 0;
    }


}
