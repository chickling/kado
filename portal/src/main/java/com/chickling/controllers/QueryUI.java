package com.chickling.controllers;

import com.chickling.boot.Init;
import com.chickling.util.PrestoUtil;
import com.facebook.presto.hive.$internal.org.apache.commons.lang3.exception.ExceptionUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.chickling.models.Auth;
import com.chickling.models.MessageFactory;
import com.chickling.models.ControlManager;
import com.chickling.models.job.JobRunner;
import com.chickling.models.job.PrestoContent;
import com.chickling.util.JobHistoryCatch;
import com.chickling.util.TimeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.media.multipart.ContentDisposition;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by ey67 on 2015/12/14.
 */
@Path("/")
public class QueryUI {
    /*Log4J*/
    Logger log = LogManager.getLogger(QueryUI.class);

    /**
     * Submit SQL Query
     * @param json [sql]
     * @param token
     * @return [message success?]
     */
    @POST
    @Path("/query/urlquery/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response runURLQuery(String json,@HeaderParam("AUTHORIZATION") String token){
        Base64 base64 = new Base64();

        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = null;
        Auth auth = new Auth();

        try {
            if ((Boolean) auth.verify(token).get(4) == true) {
                Map datas = gson.fromJson(json, type);
                String sql = new String(base64.decode( ((String)datas.get("sql")).getBytes()),"UTF-8");
                String jobHistoryCatchKey=TimeUtil.getCurrentTime()+":QueryUI:"+sql.hashCode();
                System.out.println(sql);
                future = executor.submit(new JobRunner(0, PrestoContent.QUERY_UI, token,jobHistoryCatchKey, sql));
                int waitCount=0;
                while (waitCount<100){
                    Integer jhid= JobHistoryCatch.getInstance().jobHistoryIDs.get(jobHistoryCatchKey);
                    if(jhid!=null){
                        JobHistoryCatch.getInstance().jobHistoryIDs.put(jobHistoryCatchKey,null);
                        return Response.ok(MessageFactory.rtnJobHistoryMessage("success", TimeUtil.getCurrentTime(),"Job Success",jhid.toString())).build();
                    }
                    Thread.sleep(200);
                    waitCount++;
                }
                return Response.ok(MessageFactory.rtnJobHistoryMessage("error", "","Can't get JobHistoryIDs", "")).build();
            }else {
                return Response.ok(MessageFactory.message("error", "Permission Denied")).build();
            }
        } catch (Exception e) {
            log.error("Submit Query to Presto Error");
            log.error(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "","Submit Query to Presto Error:" +e.getMessage(), "")).build();
        }finally {
            executor.shutdown();
        }

    }

    /**
     * Submit SQL Query
     * @param json [sql]
     * @param token
     * @return [message success?]
     */
    @POST
    @Path("/query/submit/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addQuery(String json,@HeaderParam("AUTHORIZATION") String token){
        Base64 base64 = new Base64();

        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Auth auth = new Auth();

        try {
            if ((Boolean) auth.verify(token).get(4) == true) {
                Map datas = gson.fromJson(json, type);
                String sql = new String(base64.decode( ((String)datas.get("sql")).getBytes()),"UTF-8");
                String jobHistoryCatchKey=TimeUtil.getCurrentTime()+":QueryUI:"+sql.hashCode();
                executor.submit(new JobRunner(0, PrestoContent.QUERY_UI, token,jobHistoryCatchKey, sql));
                int waitCount=0;
                while (waitCount<100){
                    Integer jhid= JobHistoryCatch.getInstance().jobHistoryIDs.get(jobHistoryCatchKey);
                    if(jhid!=null){
                        JobHistoryCatch.getInstance().jobHistoryIDs.put(jobHistoryCatchKey,null);
                        return Response.ok(MessageFactory.rtnJobHistoryMessage("success", TimeUtil.getCurrentTime(),"Job Success",jhid.toString())).build();
                    }
                    Thread.sleep(200);
                    waitCount++;
                }
                return Response.ok(MessageFactory.rtnJobHistoryMessage("error", "","Can't get JobHistoryIDs", "")).build();
            }else {
                return Response.ok(MessageFactory.message("error", "Permission Denied")).build();
            }
        } catch (Exception e) {
            log.error("Submit Query to Presto Error");
            log.error(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobHistoryMessage("error", "","Submit Query to Presto Error:" +e.getMessage(), "")).build();
        }finally {
            executor.shutdown();
        }
    }

    /**
     * Get query history list
     * @param token
     * @return [query history list]
     */
    @GET
    @Path("/query/run/list/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response runQueryList(@HeaderParam("AUTHORIZATION") String token){
        Auth auth = new Auth();
        try {
            if ((Boolean) auth.verify(token).get(4) == true) {
                ControlManager controlManager = new ControlManager();
                return Response.ok(controlManager.getQueryRunHistory(100)).build();
            }else {
                return Response.ok(MessageFactory.message("error", "Permission Denied")).build();
            }
        } catch (Exception e) {
            log.warn(e);
            return Response.ok(MessageFactory.message("error", "Permission Denied")).build();
        }
    }

    /**
     * Get query history list set limit
     * @param limit
     * @param token
     * @return [query history]
     */
    @GET
    @Path("/query/run/list/{limit}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response runQueryList(@PathParam("limit")int limit,@HeaderParam("AUTHORIZATION") String token){
        Auth auth = new Auth();
        try {
            if ((Boolean) auth.verify(token).get(4) == true) {
                ControlManager controlManager = new ControlManager();
                return Response.ok(controlManager.getQueryRunHistory(limit)).build();
            }else {
                return Response.ok(MessageFactory.message("error", "Permission Denied")).build();
            }
        } catch (Exception e) {
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.message("error", "Permission Denied")).build();
        }
    }

    /**
     * Kill running query
     * @param jobrunid
     * @param token
     * @return [message success?]
     */
    @GET
    @Path("/query/kill/{jobrunid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response runQueryStop(@PathParam("jobrunid")int jobrunid,@HeaderParam("AUTHORIZATION") String token){
        ControlManager controlManager =new ControlManager();
        return Response.ok(controlManager.setJobStop(jobrunid, token)).build();
    }



    /**
     * Download result file
     * @param jobrunid
     * @param token
     * @return [CSV file]
     * @throws IOException
     */
    @GET
    @Path("/query/get/result/file/{jobrunid}/{token}")
    @Produces({"text/csv"})
    public Response getResultFile(@PathParam("jobrunid") int jobrunid, @PathParam("token") String token) {
        Auth auth = new Auth();
        try {
            if ((Boolean) auth.verify(token).get(4) == true||auth.generateDownloadToken(jobrunid).equals(token)) {
                ControlManager controlManager = new ControlManager();
                String filePath = "";
                filePath = controlManager.getResultFilePath(jobrunid);
                String fileName=controlManager.getFilenameFromPath(filePath);
                ContentDisposition contentDisposition = ContentDisposition.type("attachment")
                        .fileName(fileName + ".csv").creationDate(new Date()).build();
                return Response.ok(controlManager.getResultFile(new PrestoUtil().downloadCSV(Init.getDatabase()+"."+fileName))).header("Content-Disposition", contentDisposition).build();
            } else {
                log.warn("Get Result File Verify Error");
                log.warn("JHID->"+jobrunid+";Token->"+token);
                return Response.status(404).build();
            }
        } catch (Exception e) {
            log.error("Get Result File SQL Error");
            log.error(ExceptionUtils.getStackTrace(e));
            return Response.status(404).build();
        }
    }
}
