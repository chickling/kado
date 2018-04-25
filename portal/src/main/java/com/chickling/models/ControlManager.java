package com.chickling.models;

import com.chickling.bean.result.ResultMap;
import com.chickling.util.*;
import com.facebook.presto.hive.$internal.org.apache.commons.lang3.exception.ExceptionUtils;
import com.google.gson.Gson;
import com.chickling.boot.Init;
import com.chickling.models.job.JobRunner;
import com.chickling.models.job.PrestoContent;
import owlstone.dbclient.db.DBClient;
import owlstone.dbclient.db.module.DBResult;
import owlstone.dbclient.db.module.PStmt;
import owlstone.dbclient.db.module.Row;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by ey67 on 2015/12/14.
 */
public class ControlManager {
    /*Log4J*/
    Logger log = LogManager.getLogger(ControlManager.class);
    /**
     * Get QueryUI query run history
     * @param limit
     * @return [json string]
     * @throws SQLException
     */
    public String getQueryRunHistory(int limit) {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        //SELECT *,jh.JobOwner UID FROM (SELECT * FROM Job_History WHERE JobType=0 ORDER BY JHID DESC limit 100) jh INNER JOIN Job_Log jl ON jl.JLID=jh.JobLog  INNER JOIN User u ON jh.JobOwner =u.UID
        String sql = "SELECT *,jh.JobOwner UID FROM (SELECT * FROM Job_History WHERE JobType=0 ORDER BY JHID DESC limit ?) jh INNER JOIN Job_Log jl ON jl.JLID=jh.JobLog  INNER JOIN User u ON jh.JobOwner =u.UID";
        try {
            queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                    limit
            });
            rs=dbClient.execute(queryBean);
            if(!rs.isSuccess())
                throw rs.getException();

            List<Map> queryList = new ArrayList<>();
            for(Row row:rs.getRowList()){
                KadoRow r=new KadoRow(row);
                Map<String,Object> queryInfo = new LinkedHashMap<>();
                queryInfo.put("jobrunid", r.getInt("JHID"));
                String jobSql = r.getString("JobSQL").replace("\n", " ").replace("\r", " ");
                queryInfo.put("sql", getLimitSQL(jobSql));
                queryInfo.put("jobLevel", r.getString("JobLevel"));
                queryInfo.put("type", r.getString("JobType"));
                queryInfo.put("job_status", r.getString("JobStatus"));
                queryInfo.put("progress", r.getString("JobProgress"));
                queryInfo.put("valid",r.getInt("Valid"));
                queryInfo.put("start_time", r.getString("JobStartTime"));
                queryInfo.put("stop_time", r.getString("JobStopTime"));
                try {
                    if (!r.getString("JobStopTime").equals("")) {
                        queryInfo.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                                TimeUtil.String2DateTime(r.getString("JobStopTime"))));
                    } else {
                        queryInfo.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                                TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                    }
                } catch (NullPointerException npe) {

                    queryInfo.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(r.getString("JobStartTime")),
                            TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                }

                queryInfo.put("user", r.getString("UserName"));
                queryInfo.put("userid", r.getString("UID"));
                queryInfo.put("group", r.getString("Gid"));
                queryList.add(queryInfo);
            }

            return MessageFactory.messageList("success", "list", queryList);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return MessageFactory.message("error", "Sql error");
        }

    }

    /**
     * Set job stop to kill job
     * @param jhid
     * @param token
     * @return [json string]
     * @throws SQLException
     */
    public String setJobStop(int jhid, String token) {
        Auth auth = new Auth();
        ArrayList<Object> ver = null;
        try {
            ver = auth.verify(token);
            if ((Boolean) ver.get(4) != false) {
                //DBClient
                PStmt queryBean=null;
                DBResult rs=null;
                DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

                String sql = "SELECT JobOwner FROM Job_History WHERE JHID=?;";

                queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                        jhid
                });
                rs=dbClient.execute(queryBean);
                if(!rs.isSuccess())
                    throw rs.getException();
                int owner = 0;
                if(rs.getRowSize()>0){
                    KadoRow r= new KadoRow(rs.getRowList().get(0));
                    owner = r.getInt("JobOwner");
                }
                if ((Integer) ver.get(2) == owner || (Integer) ver.get(1) == 2) {
                    Init.getDeleteJobList().add(jhid);
                    return MessageFactory.rtnJobMessage("success", "", "Success add job to stop list", "");
                } else {
                    return MessageFactory.rtnJobMessage("error", "", "Permission Denied", "");
                }
            } else {
                return MessageFactory.rtnJobMessage("error", "", "Not Login", "");

            }
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return MessageFactory.message("error", "Sql error");
        }
    }

    /**
     * Run job
     * @param jid
     * @param token
     * @return  [json string]
     * @throws SQLException
     */
    public String doRunJob(int jid,String token) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Boolean> future = null;
        try {
            String jobHistoryCatchKey=TimeUtil.getCurrentTime()+":"+String.valueOf(jid);
            future = executor.submit(new JobRunner(jid, PrestoContent.USER_JOB,token,jobHistoryCatchKey,null));

            if (future.isDone()) {
                log.info("future :"+future.get());
                log.info("SingleQueryUIJobRunner  End");
                return MessageFactory.rtnJobMessage("success", "","Job Success", "");
            }
            int waitCount=0;
            while (waitCount<100){
                Integer jhid=JobHistoryCatch.getInstance().jobHistoryIDs.get(jobHistoryCatchKey);
                if(jhid!=null){
                    JobHistoryCatch.getInstance().jobHistoryIDs.put(jobHistoryCatchKey,null);
                    return MessageFactory.rtnJobHistoryMessage("success", TimeUtil.getCurrentTime(),"Job Success",jhid.toString());
                }
                Thread.sleep(200);
                waitCount++;
            }
            return MessageFactory.rtnJobHistoryMessage("error", "","Can't get JobHistoryIDs", "");
        }catch (ClassCastException cce){
            return MessageFactory.rtnJobHistoryMessage("error", "","Permission denied", "");
        }catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return MessageFactory.rtnJobHistoryMessage("error", "",e.getMessage(), "");
        }finally {
            executor.shutdown();
        }
    }


    public String doRunJobWithTemplate(int jid,String token,Map template) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Boolean> future = null;
        try {
            String jobHistoryCatchKey=TimeUtil.getCurrentTime()+":"+String.valueOf(jid);
            future = executor.submit(new JobRunner(jid, PrestoContent.USER_JOB, token,template,jobHistoryCatchKey));

            int waitCount=0;
            while (waitCount<100){
                Integer jhid=JobHistoryCatch.getInstance().jobHistoryIDs.get(jobHistoryCatchKey);
                if(jhid!=null){
                    JobHistoryCatch.getInstance().jobHistoryIDs.put(jobHistoryCatchKey,null);
                    return MessageFactory.rtnJobHistoryMessage("success", TimeUtil.getCurrentTime(),"Job Success",jhid.toString());
                }
                Thread.sleep(200);
                waitCount++;
            }
            return MessageFactory.rtnJobHistoryMessage("error", "","Can't get JobHistoryIDs", "");
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return MessageFactory.rtnJobMessage("error", "",e.getMessage(), "");
        }finally {
            executor.shutdown();
        }
    }





    /**
     * Get in HDFS result file
     * @param filepath
     * @return [file stream]
     */
    public StreamingOutput getResultFile(String filepath){

        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException,
                    WebApplicationException {
                InputStream is = new FileInputStream(new File(filepath));
                try {
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while((len = is.read(buffer)) > -1) {
                        os.write(buffer, 0, len);
                    }
                    os.flush();
                } finally {
                    if(is != null) is.close();
                    if(os != null) os.close();
                }
            }
        };
        return stream;
    }

    /**
     * Get result page
     * @param jhid
     * @param page
     * @param pageRowCount
     * @return [page json]
     * @throws IOException
     * @throws SQLException
     */
    public String getResultPage(int jhid,int page,int pageRowCount)  {

        Map<String,Object> resultInfo=new LinkedHashMap<>();
        int resultCount= 0;
        try {
            resultCount = getResultCount(jhid);
        } catch (Exception e) {
            log.error("Get Result count Fail!");
            log.error(e);
            return MessageFactory.message("error", "Get Result count Fail!");
        }

        if(resultCount>0) {
            int pageCount = (int) Math.ceil((double) resultCount / (double) pageRowCount);
            //real start 0
            page=(page>0)?page-1:0;
            int startRow = pageRowCount * page;
            if (page <= pageCount) {
                resultInfo.put("status", "success");
                resultInfo.put("time", TimeUtil.getCurrentTime());
                resultInfo.put("resultCount", resultCount);
                resultInfo.put("pageCount", pageCount>200?200:pageCount);
                resultInfo.put("nowPage", page+1);
                resultInfo.put("startRow", startRow);
                resultInfo.put("pageRowCount", pageRowCount);
                try {
                    String path=getResultFilePath(jhid);
                    ResultMap resultData=new PrestoUtil().readJsonAsResult(Init.getDatabase()+"."+path.substring(path.lastIndexOf("/")+1,path.length()),page+1,resultCount);
                    resultInfo.put("header", resultData.getSchema());
                    resultInfo.put("row", resultData.getData().stream().map(item-> {
                        return item.stream().map(value->value.toString()).toArray();
                    }).toArray());
                    return new Gson().toJson(resultInfo);
                }catch (Exception e){
                    log.error("Get ResultFilePath Error");
                    log.error(ExceptionUtils.getMessage(e));
                    return MessageFactory.message("error", "Get ResultFilePath Error");
                }
            } else {
                return MessageFactory.message("error", "Page number out of index");
            }
        }else {
            return MessageFactory.message("error", "No Result");
        }

    }
    /**
     * Get result page table HTML
     * @param jhid
     * @param page
     * @param pageRowCount
     * @return [page json]
     * @throws IOException
     * @throws SQLException
     */
    public String getResultPageTable(int jhid,int page,int pageRowCount)  {
        int resultCount= 0;
        try {
            resultCount = getResultCount(jhid);
        } catch (Exception e) {
            log.error("Get Result count Fail!");
            log.error(e);
            return MessageFactory.message("error", "Get Result count Fail!");
        }
        if(resultCount>0) {
            int pageCount = (int) Math.ceil((double) resultCount / (double) pageRowCount);
            //real start 0
            page=(page>0)?page-1:0;

            if (page <= pageCount) {
                ResultMap resultData;
                try {
                    String path = getResultFilePath(jhid);
                    resultData = new PrestoUtil().readJsonAsResult(Init.getDatabase() + "." + path.substring(path.lastIndexOf("/") + 1, path.length()), page+1,resultCount);
                } catch (Exception e) {
                    log.error("Get ResultFilePath Error");
                    log.error(e);
                    return "Get ResultFilePath Error";
                }
                Object[] dataList= resultData.getData().stream().map(item-> {
                    return item.stream().map(value->value.toString()).toArray();
                }).toArray();
                int i = 0;
                String tHeader="";
                String tBody="";
                //first line is header
                tHeader += "<tr>";
                for (String col : resultData.getSchema()) {
                    tHeader +="<th style='padding: 5px;border: 1px solid black;background-color: #5858FA;color: white;'>" + col + "</th>";
                }
                tHeader+="</tr>";

                for (Object dataRow: dataList) {
                    tBody+="<tr>";

                    for (Object col : (Object[]) dataRow) {
                        tBody +="<td style='border: 1px solid black;'>" + col.toString() + "</td>";
                    }
                    tBody+="</tr>";
                    i++;
                }


                return "<table style='border-collapse: collapse;border: 1px solid black;'>" +"<thead>"+tHeader+"</thead>"+"<tbody>"+tBody+"</tbody>"+"</table>";
            } else {
                return "Page number out of index";
            }
        }else {
            return "No Result";
        }

    }
    /**
     * Get resule file path
     * @param jhid
     * @return [file path]
     * @throws SQLException
     */
    public String getResultFilePath(int jhid) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String sql = "SELECT JobOutput FROM Job_History,Job_Log WHERE Job_History.JobLog=Job_Log.JLID AND Job_History.JHID=?;";
        queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                jhid
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();
        String jobOutput="";
        if(rs.getRowSize()>0){
            KadoRow r=new KadoRow(rs.getRowList().get(0));
            jobOutput=r.getString("JobOutput");
        }
        return jobOutput;
    }

    /**
     * Get result csv path
     * @return
     */
//    public String getResultCSVPath(String sourcePath){
//        OrcFileUtil orcFileUtil=OrcFileUtil.newInstance();
//
////        String csvFilePath=sourcePath.replace("/user/hive/warehouse/" + Init.getDatabase() + "/", "/tmp/presto-job-manager/csv/");
//        String csvFilePath= YamlLoader.instance.getCsvtmphdfsPath()+"/csv"+sourcePath.substring(sourcePath.lastIndexOf("/"))+"/";
////        String csvFilePath="/tmp/presto-job-manager/csv"+sourcePath.substring(sourcePath.lastIndexOf("/"))+"/";
//        String resultPath=orcFileUtil.downloadORCFilestoCSV(sourcePath + "/", csvFilePath + "/", OrcFileUtil.TYPE.HDFS);
//        if(Strings.isNullOrEmpty(resultPath))
//            return "";
//        else
//            return csvFilePath+resultPath;
//
//    }

    /**
     * Get result row count
     * @param jhid
     * @return [count]
     * @throws SQLException
     */
    public int getResultCount(int jhid) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String sql = "SELECT ResultCount FROM Job_History,Job_Log WHERE Job_History.JobLog=Job_Log.JLID AND Job_History.JHID=?;";
        queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                jhid
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();
        int resultCount=0;
        if (rs.getRowSize()>0){
            KadoRow r= new KadoRow(rs.getRowList().get(0));
            resultCount=r.getInt("ResultCount");
        }
        return resultCount;
    }

    /**
     * Get file name from path
     * @param Path
     * @return [file name]
     */
    public String getFilenameFromPath(String Path){
        return Path.substring(Path.lastIndexOf("/")+1,Path.length());
    }

    /**
     * Get log message
     * @param filepath
     * @return
     * @throws IOException
     */
    public String getLogFile(String filepath) {
        if (!filepath.equals("")) {
            File localFile=new File(filepath);
//            FSFile fsFile = FSFile.newInstance(FSFile.FSType.LocalFs);
            String logMessage = "";
            try {
                InputStreamReader inReader = new InputStreamReader(new FileInputStream(localFile));
                BufferedReader br = new BufferedReader(inReader);
                while (br.ready()) {
                    String tmp = br.readLine();
                    logMessage += tmp + "<br>";
                }
            } catch (IOException e) {
                log.error("Get Log File io error!");
                log.error(e.getStackTrace());
                logMessage = "Get Log File io error!";
            }
            return logMessage;
        } else {
            return "";
        }
    }

    /**
     * Get job log save path
     * @param jhid
     * @return [file path]
     * @throws SQLException
     */
    public String getJobLogPath(int jhid) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String sql = "SELECT JobLogfile FROM Job_History,Job_Log WHERE Job_History.JobLog=Job_Log.JLID AND Job_History.JHID=?;";

        queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                jhid
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();
        String logOutput="";
        if (rs.getRowSize()>0){
            KadoRow r=new KadoRow(rs.getRowList().get(0));
            logOutput=r.getString("JobLogfile");
        }
//        if(!logOutput.equals("")){
//            logOutput=logOutput+logOutput.substring(logOutput.lastIndexOf("/"),logOutput.length())+".log";
//        }
        return logOutput;
    }

    /**
     * Get schedule log path
     * @param shid
     * @return [log file path]
     */
    public String getScheduleLogPath(int shid){
        return YamlLoader.instance.getLogpath()+Init.getFileseparator()+"ScheduleHistoryLog_"+shid+".log";
    }

    public String getLimitSQL(String sqlBase64){
        Base64 base64 = new Base64();
        try {
            String sql=new String(base64.decode(sqlBase64.getBytes()),"UTF-8");
            if(sql.length()>5000)
                return new String(base64.encode((sql.substring(0,5000)+"...").getBytes()),"UTF-8");
            else
                return sqlBase64;
        } catch (UnsupportedEncodingException e) {
            return sqlBase64;
        }
    }
}
