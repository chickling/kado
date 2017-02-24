package com.chickling.controllers;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.chickling.boot.Init;
import com.chickling.models.Auth;
import com.chickling.util.JobCRUDUtils;
import com.chickling.models.MessageFactory;
import com.chickling.util.TemplateCRUDUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jw6v on 2015/11/30.
 */

@Path("/job")
public class Job {
    /*Log4J*/
    Logger log = LogManager.getLogger(Job.class);
    /**
     * Add job
     * @param json [job info]
     * @param token
     * @return [message success?]
     */
    @POST
    @Path("/manage/add/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addJob(String json,@HeaderParam("AUTHORIZATION") String token){
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        try {
            Map datas = gson.fromJson(json, type);
            ArrayList<Map<String,String>> info=(ArrayList)datas.get("SQLTemplate");
            if(info.size()>0){
                String rtn=JobCRUDUtils.addJobInfotoDB(datas, token);
                Map rmap=(Map)gson.fromJson(rtn,type);
                if(rmap.get("status").equals("success")){
                    String jobID=(String)rmap.get("jobid");
                    TemplateCRUDUtils.addSqlTemplate(Integer.parseInt(jobID),info);
                    return Response.ok(rtn).build();
                }else {
                    return Response.ok(rtn).build();
                }
            }else{
                return Response.ok(JobCRUDUtils.addJobInfotoDB(datas, token)).build();
            }

        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        }catch (NumberFormatException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "","Lost data:"+ e.getMessage(), "")).build();
        }

    }

    /**
     * Update job
     * @param json [job info]
     * @param jobId
     * @param token
     * @return [message success?]
     */
    @POST
    @Path("/manage/update/{jobid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateJob(String json, @PathParam("jobid")int jobId,
                              @HeaderParam("AUTHORIZATION") String token){
        //TODO parse to get template
        //TODO update template
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        try {
            Map datas = gson.fromJson(json, type);
            ArrayList<Map<String,String>> info=(ArrayList)datas.get("SQLTemplate");

            String rtn=JobCRUDUtils.updateJobtoDB(datas, jobId, token);
            Map rmap=(Map)gson.fromJson(rtn,type);
            if(rmap.get("status").equals("success")){
                String jobID=(String)rmap.get("jobid");
                TemplateCRUDUtils.updateSqlTemplate(Integer.parseInt(jobID),info);
                return Response.ok(rtn).build();
            }else{
                return Response.ok(rtn).build();
            }

        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }
        catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        }


    }

    /**
     * Get job info
     * @param jobId
     * @param token
     * @return [job info]
     */
    @GET
    @Path("/manage/get/{jobid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobInfo(@PathParam("jobid")int jobId,
                               @HeaderParam("AUTHORIZATION") String token){
        //TODO parse to get template
        //TODO read template
        //TODO rebuild the return json which contains template
        try {
            return Response.ok(JobCRUDUtils.getJobInfo(jobId, token)).build();
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        }

    }

    /**
     * Delete job
     * @param jobId
     * @param token
     * @return [message success?]
     */
    @GET
    @Path("/manage/delete/{jobid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteJob( @PathParam("jobid")int jobId,
                               @HeaderParam("AUTHORIZATION") String token){
        //TODO parse to get template
        //TODO delete template
        Gson gson=new Gson();
        Type type = new TypeToken<Map>() {}.getType();
        try {
            String rtn=JobCRUDUtils.deleteJob(jobId, token);
            Map rmap=(Map)gson.fromJson(rtn,type);
            if(rmap.get("status").equals("success")){
                TemplateCRUDUtils.deleteSqlTemplate(jobId);
                return Response.ok(rtn).build();
            }else{
                return Response.ok(rtn).build();
            }
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        }

    }

    /**
     * Get job list
     * @param token
     * @return [job list]
     */
    @GET
    @Path("/manage/list/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobList(@HeaderParam("AUTHORIZATION") String token){
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        try {

            return Response.ok(JobCRUDUtils.getJobList(token)).build();
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        }

    }

    /**
     * Get job running&history list
     * @param limit
     * @param token
     * @return [job running&history list]
     */
    @GET
    @Path("/manage/run/list/{limit}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobStatusList(@PathParam("limit") String limit,@HeaderParam("AUTHORIZATION") String token){

        try {

            return Response.ok(JobCRUDUtils.getJobStatusList(limit, token)).build();
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        }

    }

    /**
     * Get job history by start~stop jobid
     * @param start
     * @param stop
     * @param jobId
     * @param token
     * @return [job history list]
     */
    @GET
    @Path("/manage/run/history/range/{start}/{stop}/{jobid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobHistoryList(@PathParam("start")String start,
                                      @PathParam("stop") String stop,@PathParam("jobid") String jobId,@HeaderParam("AUTHORIZATION") String token){

        try {
            return Response.ok(JobCRUDUtils.getJobHistoryList(start, stop, jobId, token)).build();
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        }


    }

    /**
     * Get job history by start~stop
     * @param start
     * @param stop
     * @param token
     * @return [job history list]
     */
    @GET
    @Path("/manage/run/history/range/{start}/{stop}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobHistoryList(@PathParam("start")String start,
                                      @PathParam("stop") String stop,@HeaderParam("AUTHORIZATION") String token){

        try {
            return Response.ok(JobCRUDUtils.getJobHistoryList(start, stop,"", token)).build();
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        }


    }

    /**
     * Get job history by jobid
     * @param jobid
     * @param token
     * @return [job history list]
     */
    @GET
    @Path("/manage/run/history/range/{jobid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobHistoryList(@PathParam("jobid")String jobid,
                                      @HeaderParam("AUTHORIZATION") String token){

        try {
            return Response.ok(JobCRUDUtils.getJobHistoryList("", "",jobid, token)).build();
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            System.out.println(e.getMessage());
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }
        catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        }


    }
    @GET
    @Path("/manage/run/history/has/result/{jobid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHasResultJobHistory(@PathParam("jobid")String jobid,
                                           @HeaderParam("AUTHORIZATION") String token){

        try {
            return Response.ok(JobCRUDUtils.getHasResultJobHistory(jobid, token)).build();

        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            System.out.println(e.getMessage());
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }
        catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        }
        catch (Exception e){
            log.error(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", ExceptionUtils.getStackTrace(e), "")).build();
        }


    }
    /**
     * Get job history info
     * @param jobRunId
     * @param token
     * @return [job history info]
     * @throws SQLException
     */
    @GET
    @Path("/manage/run/history/get/info/{jobrunid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobHistoryInfo(@PathParam("jobrunid") int jobRunId,@HeaderParam("AUTHORIZATION") String token) throws SQLException {
        try {
            return Response.ok(JobCRUDUtils.getJobHistoryInfo(token, jobRunId)).build();
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception:" + e.getMessage(), "")).build();
        }

    }
    /**
     * Get job list
     * @param token
     * @return [job list]
     */
    @GET
    @Path("/manage/location/list/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobLocationList(@HeaderParam("AUTHORIZATION") String token){
        Auth auth = new Auth();
        try {
            if ((Boolean) auth.verify(token).get(4) == true) {
                List dbLocation= Init.getLocationList();
                List<Map> list=new ArrayList<>();

                for (int i=0;i<dbLocation.size();i++){
                    Map location=new LinkedHashMap<>();
                    location.put("id", i);
                    location.put("name",dbLocation.get(i));
                    list.add(location);
                }
                return Response.ok(MessageFactory.messageList("success", "list", list)).build();
            }else {
                return Response.ok(MessageFactory.message("error", "Permission Denied")).build();
            }
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        } catch (SQLException e) {
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "sql error", "")).build();
        }

    }

}
