package com.chickling.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.chickling.models.MessageFactory;
import com.chickling.util.ScheduleCRUDUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by ey67 on 2015/12/10.
 */
@Path("/schedule")
public class Schedule {
    /*Log4J*/
    Logger log = LogManager.getLogger(Schedule.class);

    /**
     * Add schedule
     * @param json
     * @param token
     * @return [message success?]
     */
    @POST
    @Path("/manage/add/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSchedule(String json,@HeaderParam("AUTHORIZATION") String token){
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        try {



            Map datas = gson.fromJson(json, type);
            return Response.ok(ScheduleCRUDUtils.addSchedule(datas, token)).build();
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }
        catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }
        catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        }

    }

    /**
     * Update schedule
     * @param json
     * @param scheduleId
     * @param token
     * @return [message success?]
     */
    @POST
    @Path("/manage/update/{scheduleid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSchrdule(String json, @PathParam("scheduleid")int scheduleId,
                              @HeaderParam("AUTHORIZATION") String token){
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();

        try {

            Map datas = gson.fromJson(json, type);
            return Response.ok(ScheduleCRUDUtils.updateSchedule(datas, scheduleId, token)).build();
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e) {
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        }


    }

    /**
     * Get schedule info
     * @param scheduleId
     * @param token
     * @return [schedule info]
     */
    @GET
    @Path("/manage/get/{scheduleid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScheduleInfo(@PathParam("scheduleid")int scheduleId,
                               @HeaderParam("AUTHORIZATION") String token){
        try {
            return Response.ok(ScheduleCRUDUtils.getScheduleInfo(scheduleId, token)).build();
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
            return Response.ok(MessageFactory.message("error", "Sql error")).build();
        }
    }

    /**
     * Delete schedule
     * @param scheduleId
     * @param token
     * @return [message success?]
     */
    @GET
    @Path("/manage/delete/{scheduleid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSchedule( @PathParam("scheduleid")int scheduleId,
                               @HeaderParam("AUTHORIZATION") String token){
        try {
            return Response.ok(ScheduleCRUDUtils.deleteSchedule(scheduleId, token)).build();
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
     * Get schedule list
     * @param token
     * @return [schedule list]
     */
    @GET
    @Path("/manage/list/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScheduleList(@HeaderParam("AUTHORIZATION") String token) {

        try {
            return Response.ok(ScheduleCRUDUtils.getScheduleList(token)).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        }

    }

    /**
     * Get schedule list (limit)
     * @param limit
     * @param token
     * @return [schedule list]
     */
    @GET
    @Path("/manage/run/list/{limit}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScheduleStatusList(@PathParam("limit") String limit,@HeaderParam("AUTHORIZATION") String token){
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        try {
            return Response.ok(ScheduleCRUDUtils.getScheduleStatusList(limit, token)).build();
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }

        catch (ClassCastException e){
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        }
    }

    /**
     * Get schedule run history (start~stop schedule id)
     * @param start
     * @param stop
     * @param scheduleId
     * @param token
     * @return [schedule history list]
     */
    @GET
    @Path("/manage/run/history/range/{start}/{stop}/{scheduleid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScheduleHistoryList(@PathParam("start")String start,
                                      @PathParam("stop") String stop,@PathParam("scheduleid") String scheduleId,@HeaderParam("AUTHORIZATION") String token){

        try {
            return Response.ok(ScheduleCRUDUtils.getScheduleHistoryList(start, stop, scheduleId, token)).build();
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "value can not be null", "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "",  e.getMessage(), "")).build();
        }
    }

    /**
     * Get schedule run history (start~stop)
     * @param start
     * @param stop
     * @param token
     * @return [schedule history lsit]
     */
    @GET
    @Path("/manage/run/history/range/{start}/{stop}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScheduleHistoryList(@PathParam("start")String start,
                                      @PathParam("stop") String stop,@HeaderParam("AUTHORIZATION") String token){

        try {
            return Response.ok(ScheduleCRUDUtils.getScheduleHistoryList(start, stop, "", token)).build();
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "",  e.getMessage(), "")).build();
        }


    }

    /**
     * Get schedule run history (schedule id)
     * @param scheduleId
     * @param token
     * @return [schedule histroy list]
     */
    @GET
    @Path("/manage/run/history/range/{scheduleid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScheduleHistoryList(@PathParam("scheduleid")String scheduleId,@HeaderParam("AUTHORIZATION") String token){

        try {
            return Response.ok(ScheduleCRUDUtils.getScheduleHistoryList("","",scheduleId, token)).build();
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "",  e.getMessage(), "")).build();
        }


    }

}
