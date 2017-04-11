package com.chickling.models;

import com.chickling.bean.result.ResultMap;
import com.chickling.util.PrestoUtil;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.chickling.sqlite.ConnectionManager;
import com.chickling.boot.Init;
import com.chickling.models.dfs.FSFile;
import com.chickling.models.dfs.OrcFileUtil;
import com.chickling.models.job.JobRunner;
import com.chickling.models.job.PrestoContent;
import com.chickling.util.JobHistoryCatch;
import com.chickling.util.TimeUtil;
import com.chickling.util.YamlLoader;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        //SQLite
        PreparedStatement stat = null;
        ResultSet rs = null;

        String sql = "SELECT * FROM (SELECT *,jh.JobOwner UID FROM (SELECT * FROM Job_History WHERE JobType=0 ORDER BY JHID DESC limit ?) jh,Job_Log jl WHERE jl.JLID=jh.JobLog) jhr,User u WHERE jhr.JobOwner =u.UID;";
        try {
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);

            stat.setInt(1, limit);
            rs = stat.executeQuery();
            List<Map> queryList = new ArrayList<>();
            while (rs.next()) {
                Map<String,Object> queryInfo = new LinkedHashMap<>();
                queryInfo.put("jobrunid", rs.getInt("JHID"));
                String jobSql = rs.getString("JobSQL").replace("\n", " ").replace("\r", " ");
                queryInfo.put("sql", jobSql);
                queryInfo.put("jobLevel", rs.getString("JobLevel"));
                queryInfo.put("type", rs.getString("JobType"));
                queryInfo.put("job_status", rs.getString("JobStatus"));
                queryInfo.put("progress", rs.getString("JobProgress"));
                queryInfo.put("valid",rs.getInt("Valid"));
                queryInfo.put("start_time", rs.getString("JobStartTime"));
                queryInfo.put("stop_time", rs.getString("JobStopTime"));
                try {
                    if (!rs.getString("JobStopTime").equals("")) {
                        queryInfo.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("JobStartTime")),
                                TimeUtil.String2DateTime(rs.getString("JobStopTime"))));
                    } else {
                        queryInfo.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("JobStartTime")),
                                TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                    }
                } catch (NullPointerException npe) {

                    queryInfo.put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(rs.getString("JobStartTime")),
                            TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                }

                queryInfo.put("user", rs.getString("UserName"));
                queryInfo.put("userid", rs.getString("UID"));
                queryInfo.put("group", rs.getString("Gid"));
                queryList.add(queryInfo);
            }

            return MessageFactory.messageList("success", "list", queryList);
        } catch (SQLException e) {
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
                //SQLite
                PreparedStatement stat = null;
                ResultSet rs = null;

                String sql = "SELECT JobOwner FROM Job_History WHERE JHID=?;";
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
                stat.setInt(1, jhid);
                rs = stat.executeQuery();
                int owner = 0;
                while (rs.next()) {
                    owner = rs.getInt("JobOwner");
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
        } catch (SQLException e) {
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
                FSFile fsFile=FSFile.newInstance(FSFile.FSType.HDFS);
                InputStream is = fsFile.createInputStreamWithAbsoultePath(filepath);
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
        } catch (SQLException e) {
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
                resultInfo.put("pageCount", pageCount);
                resultInfo.put("nowPage", page+1);
                resultInfo.put("startRow", startRow);
                resultInfo.put("pageRowCount", pageRowCount);
                try {
                    String path=getResultFilePath(jhid);
                    ResultMap resultData=new PrestoUtil().readJsonAsResult(Init.getDatabase()+"."+path.substring(path.lastIndexOf("/")+1,path.length()),startRow, pageRowCount);
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
        } catch (SQLException e) {
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

                //Read page from orcfileutil
                OrcFileUtil orc = OrcFileUtil.newInstance();
                ByteArrayInputStream stream = null;
                try {
                    stream = orc.readORCFiles(getResultFilePath(jhid), OrcFileUtil.TYPE.HDFS, startRow, pageRowCount);
                } catch (SQLException e) {
                    log.error("Get ResultFilePath Error");
                    log.error(e);
                    return "Get ResultFilePath Error";
                }
                // Read InputStream by InputStreamReader
                InputStreamReader inReader = new InputStreamReader(stream);
                BufferedReader br = new BufferedReader(inReader);
                int i = 0;
                String tHeader="";
                String tBody="";
                try {
                    while (br.ready()) {
                        String tmp = br.readLine();
                        String[] rowArray = tmp.split("\001");
                        //first line is header
                        if (i == 0) {
                            tHeader += "<tr>";
                            for (String col : rowArray) {
                                tHeader +="<th style='padding: 5px;border: 1px solid black;background-color: #5858FA;color: white;'>" + col + "</th>";
                            }
                            tHeader+="</tr>";
                        }else {
                            tBody+="<tr>";
                            for (String col : rowArray) {
                                tBody +="<td style='border: 1px solid black;'>" + col + "</td>";
                            }
                            tBody+="</tr>";
                        }
                        i++;
                    }
                } catch (IOException e) {
                    log.error("Get Page IO Error");
                    log.error(e);
                    return  "Get Page IO Error";
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
    public String getResultFilePath(int jhid) throws SQLException {
        //SQLite
        PreparedStatement stat = null;
        ResultSet rs = null;

        String sql = "SELECT JobOutput FROM Job_History,Job_Log WHERE Job_History.JobLog=Job_Log.JLID AND Job_History.JHID=?;";
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
        stat.setInt(1,jhid);
        rs = stat.executeQuery();
        String jobOutput="";
        while (rs.next()){
            jobOutput=rs.getString("JobOutput");
        }
        return jobOutput;
    }

    /**
     * Get result csv path
     * @return
     */
    public String getResultCSVPath(String sourcePath){
        OrcFileUtil orcFileUtil=OrcFileUtil.newInstance();

//        String csvFilePath=sourcePath.replace("/user/hive/warehouse/" + Init.getDatabase() + "/", "/tmp/presto-job-manager/csv/");
        String csvFilePath= YamlLoader.instance.getCsvtmphdfsPath()+"/csv"+sourcePath.substring(sourcePath.lastIndexOf("/"))+"/";
//        String csvFilePath="/tmp/presto-job-manager/csv"+sourcePath.substring(sourcePath.lastIndexOf("/"))+"/";
        String resultPath=orcFileUtil.downloadORCFilestoCSV(sourcePath + "/", csvFilePath + "/", OrcFileUtil.TYPE.HDFS);
        if(Strings.isNullOrEmpty(resultPath))
            return "";
        else
            return csvFilePath+resultPath;

    }

    /**
     * Get result row count
     * @param jhid
     * @return [count]
     * @throws SQLException
     */
    public int getResultCount(int jhid) throws SQLException {
        //SQLite
        PreparedStatement stat = null;
        ResultSet rs = null;

        String sql = "SELECT ResultCount FROM Job_History,Job_Log WHERE Job_History.JobLog=Job_Log.JLID AND Job_History.JHID=?;";
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
        stat.setInt(1,jhid);
        rs = stat.executeQuery();
        int resultCount=0;
        while (rs.next()){
            resultCount=rs.getInt("ResultCount");
        }
        return resultCount;
    }

    /**
     * Get file name from path
     * @param Path
     * @return [file name]
     */
    public String getFilenameFromPath(String Path){
        return Path.substring(Path.lastIndexOf("/"),Path.length());
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
    public String getJobLogPath(int jhid) throws SQLException {
        //SQLite
        PreparedStatement stat = null;
        ResultSet rs = null;

        String sql = "SELECT JobLogfile FROM Job_History,Job_Log WHERE Job_History.JobLog=Job_Log.JLID AND Job_History.JHID=?;";
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
        stat.setInt(1,jhid);
        rs = stat.executeQuery();
        String logOutput="";
        while (rs.next()){
            logOutput=rs.getString("JobLogfile");
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
}
