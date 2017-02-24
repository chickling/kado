package com.chickling.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.chickling.Schedule.ScheduleMgr;
import com.chickling.models.Auth;
import com.chickling.models.ControlManager;
import com.chickling.models.MessageFactory;
import com.chickling.util.TimeUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.*;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ey67 on 2015/12/16.
 */
@Path("/control")
public class Control {
    /*Log4J*/
    Logger log = LogManager.getLogger(Control.class);
    /**
     * Run job sql query
     * @param jobid
     * @param token
     * @return [message success?]
     */
    @POST
    @Path("/job/manage/run/with/template/{jobid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response runJobQuery(@PathParam("jobid")int jobid,@HeaderParam("AUTHORIZATION") String token,String template){
        try {
            Auth au = new Auth();
            if (!(Boolean) au.verify(token).get(4)) {//check login
                return Response.ok(MessageFactory.rtnJobHistoryMessage("error", "","Permission Denied", "")).build();
            } else if ((au.jobMatch(token, Integer.toString(jobid))) || ((Integer) au.verify(token).get(0) == 2)) {
            Type type = new TypeToken<Map>() {}.getType();
            Gson gson = new Gson();
            Map templateInfo = gson.fromJson(template, type);
            return Response.ok( new ControlManager().doRunJobWithTemplate(jobid, token, templateInfo)).build();}
            else {
                return Response.ok(MessageFactory.rtnJobHistoryMessage("error", "","Permission Denied", "")).build();
            }
        }catch (Exception e) {
            return Response.ok(MessageFactory.rtnJobHistoryMessage("error", "",ExceptionUtils.getMessage(e), "")).build();
        }
    }


    @GET
    @Path("/job/manage/runnow/{jobid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response runJobQuery(@PathParam("jobid")int jobid,@HeaderParam("AUTHORIZATION") String token){
        try {
            Auth au = new Auth();
            if (!(Boolean) au.verify(token).get(4)) {//check login
                return Response.ok(MessageFactory.rtnJobHistoryMessage("error", "", "Permission Denied", "")).build();
            } else if ((au.jobMatch(token, Integer.toString(jobid))) || ((Integer) au.verify(token).get(0) == 2)) {
                return Response.ok(new ControlManager().doRunJobWithTemplate(jobid, token, new HashMap())).build();
            } else {
                return Response.ok(MessageFactory.rtnJobHistoryMessage("error", "", "Permission Denied", "")).build();
            }
        }catch (Exception e) {
            return Response.ok(MessageFactory.rtnJobHistoryMessage("error", "",ExceptionUtils.getMessage(e), "")).build();
        }

    }



    /**
     * Get job result page
     * @param jobrunid
     * @param page [page num]
     * @param token
     * @return [result info;result content]
     */
    @GET
    @Path("/get/result/{jobrunid}/{page}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobResult(@PathParam("jobrunid")int jobrunid,@PathParam("page")int page,@HeaderParam("AUTHORIZATION") String token){
        ControlManager controlManager =new ControlManager();
        return Response.ok(controlManager.getResultPage(jobrunid, page, 100)).build();
    }

    /**
     * Start schedule
     * @param scheduleid
     * @param token
     * @return [message success?]
     */
    @GET
    @Path("/schedule/manage/start/{scheduleid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doStartSchedule(@PathParam("scheduleid")int scheduleid,@HeaderParam("AUTHORIZATION") String token){
        try {
            if(!token.equals("")) {
                ScheduleMgr scheduleMgr = new ScheduleMgr();
                return Response.ok(scheduleMgr.startSchedule(scheduleid, token)).build();
            }else {
                return Response.ok(MessageFactory.rtnJobMessage("error", "","Permission denied", "")).build();
            }

        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }

    }

    /**
     * Stop schedule
     * @param scheduleid
     * @param token
     * @return [message success?]
     */
    @GET
    @Path("/schedule/manage/stop/{scheduleid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doStopSchedule(@PathParam("scheduleid")int scheduleid,@HeaderParam("AUTHORIZATION") String token){
        try {
            ScheduleMgr scheduleMgr =new ScheduleMgr();
            return Response.ok(scheduleMgr.stopSchedule(scheduleid, token)).build();

        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }

    }

    /**
     * Get job log
     * @param jobrunid
     * @param token
     * @return [job log message]
     */
    @GET
    @Path("/job/log/{jobrunid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobLog(@PathParam("jobrunid")int jobrunid,@HeaderParam("AUTHORIZATION") String token){
        ControlManager controlManager=new ControlManager();
        try {
            return Response.ok(MessageFactory.rtnJobMessage("success", "", controlManager.getLogFile(controlManager.getJobLogPath(jobrunid)), "")).build();
        } catch (SQLException e) {
            log.error("Get Job Log Sql error!");
            log.error(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.message("error", "Sql error")).build();
        }
    }

    /**
     * Get schedule log
     * @param runid
     * @param token
     * @return [schedule log message]
     */
    @GET
    @Path("/schedule/log/{runid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScheduleLog(@PathParam("runid")int runid,@HeaderParam("AUTHORIZATION") String token){
        ControlManager controlManager=new ControlManager();
        return Response.ok(MessageFactory.rtnJobMessage("success", "",controlManager.getLogFile(controlManager.getScheduleLogPath(runid)), "")).build();
    }

}
