package com.chickling.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.chickling.models.Auth;
import com.chickling.models.MessageFactory;
import com.chickling.util.ChartCRUDUtils;
import com.chickling.util.DrawUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by jw6v on 2017/1/20.
 */

@Path("/chart")
public class Chart {

        /*Log4J*/
        Logger log = LogManager.getLogger(Chart.class);

        @POST
        @Path("/manage/add/")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response addChart(String json, @HeaderParam("AUTHORIZATION") String token){
            Type type = new TypeToken<Map>() {}.getType();
            Gson gson = new Gson();
            Auth au=new Auth();
            try {
                if(!au.checkChartBuilder(token)){
                    return Response.ok(MessageFactory.rtnChartMessage("error", "", "You are not a chart builder", "")).build();
                }
                else {
                    Map<String, String> datas = gson.fromJson(json, type);

                    String rtn = ChartCRUDUtils.addChart(datas, token);

                    return Response.ok(rtn).build();
                }

            }catch (JsonSyntaxException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartMessage("error", "", e.toString(), "")).build();
            }catch (NullPointerException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartMessage("error", "", "value can not be null", "")).build();
            }catch (ClassCastException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartMessage("error", "", "Json Class Cast Exception", "")).build();
            }catch (NumberFormatException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartMessage("error", "","Lost data:"+ e.toString(), "")).build();
            }catch(SQLException sqle){
                return Response.ok(MessageFactory.rtnChartMessage("error", "", sqle.toString(), "")).build();
            }

        }


        @POST
        @Path("/manage/update/{chartid}/{jobID}")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response updateChart(String json, @PathParam("chartid")int chartID,@PathParam("jobid")int jobID,
                                  @HeaderParam("AUTHORIZATION") String token){
            Auth au=new Auth();
            Type type = new TypeToken<Map>() {}.getType();
            Gson gson = new Gson();
            try {

                if(!au.checkChartBuilder(token)){
                    return Response.ok(MessageFactory.rtnChartMessage("error", "", "You are not a chart builder", "")).build();
                }
                else {
                    Map<String,String> datas = gson.fromJson(json, type);
                    String rtn=ChartCRUDUtils.updateChart(datas, jobID, token);
                    return Response.ok(rtn).build();
                }


            }catch (JsonSyntaxException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartMessage("error", "", e.toString(), "")).build();
            }catch (NullPointerException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartMessage("error", "", "value can not be null", "")).build();
            }
            catch (ClassCastException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartMessage("error", "", "Json Class Cast Exception", "")).build();
            }catch(SQLException sqle){
                return Response.ok(MessageFactory.rtnChartMessage("error", "", sqle.toString(), "")).build();
            }


        }


        @GET
        @Path("/manage/get/{chartid}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response getChartInfo(@PathParam("chartid")int chartid,
                                   @HeaderParam("AUTHORIZATION") String token){
            Auth au=new Auth();
            try {
                return Response.ok(ChartCRUDUtils.readChart(chartid, token)).build();
            }catch (JsonSyntaxException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartMessage("error", "", e.toString(), "")).build();
            }catch (NullPointerException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartMessage("error", "", "value can not be null", "")).build();
            }catch (ClassCastException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartMessage("error", "", "Json Class Cast Exception", "")).build();
            }

        }


        @GET
        @Path("/manage/delete/{chartid}/{jobID}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response deleteChart( @PathParam("chartid")int chartid,@PathParam("jobid")int jobID,
                                   @HeaderParam("AUTHORIZATION") String token){

            Gson gson=new Gson();
            Type type = new TypeToken<Map>() {}.getType();
            Auth au=new Auth();
            try {
                if(!au.checkChartBuilder(token)){
                    return Response.ok(MessageFactory.rtnChartMessage("failed", "", "You are not a chart builder", "")).build();
                }
                else {
                    String rtn = ChartCRUDUtils.deleteChart(chartid, jobID, token);

                    return Response.ok(rtn).build();
                }
            }catch (JsonSyntaxException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartMessage("error", "", e.toString(), "")).build();
            }catch (NullPointerException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartMessage("error", "", "value can not be null", "")).build();
            }catch (ClassCastException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartMessage("error", "", "Json Class Cast Exception", "")).build();
            }catch(SQLException sqle){
                return Response.ok(MessageFactory.rtnChartMessage("error", "", sqle.toString(), "")).build();
            }

        }


        @GET
        @Path("/manage/list/{jobID}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response getChartList(@HeaderParam("AUTHORIZATION") String token,@PathParam("jobID")int jobID){
            //todo
            Auth au=new Auth();
            try {
                if(!au.checkChartBuilder(token)){
                    return Response.ok(MessageFactory.rtnChartListMessage("failed", "", "You are not a chart builder", "", new ArrayList<>())).build();
                }else{
                    return Response.ok(ChartCRUDUtils.readChartbyJobID(jobID,token)).build();
                }
            }catch (JsonSyntaxException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartListMessage("error", "",e.toString(), "", new ArrayList<>())).build();
            }catch (NullPointerException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartListMessage("error", "", "value can not be null", "", new ArrayList<>())).build();
            }catch (ClassCastException e){
                log.warn(ExceptionUtils.getStackTrace(e));
                return Response.ok(MessageFactory.rtnChartListMessage("error", "", "Json Class Cast Exception", "", new ArrayList<>())).build();
            }catch(SQLException sqle){
                return Response.ok(MessageFactory.rtnChartMessage("error", "", sqle.toString(), "")).build();
            }

        }
    @POST
    @Path("/draw/{jobhistoryid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response drawByJHID(String json, @PathParam("jobhistoryid")int jobhistoryid,
                                @HeaderParam("AUTHORIZATION") String token){
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        Auth au=new Auth();
        try {
            Map<String, String> datas = gson.fromJson(json, type);
            DrawUtils dr = new DrawUtils();
            String table = dr.getResultTable(jobhistoryid);
            if (table.equals("")) {
                return Response.ok(MessageFactory.rtnDrawMessage(null, 0, "sucess", "No execution record")).build();
            } else {
                if (!(Boolean) au.verify(token).get(4)) {//check login
                    return Response.ok(MessageFactory.rtnDrawMessage(null, 0, "failed", "Permission Denied")).build();
                } else if ((au.groupMatchwithJHid(token, jobhistoryid)) || ((Integer) au.verify(token).get(0) == 2)) {
                    String rtn = dr.draw(datas, table);
                    return Response.ok(rtn).build();
                } else {
                    return Response.ok(MessageFactory.rtnDrawMessage(null, 0, "failed", "Permission Denied")).build();
                }

            }
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnDrawMessage(null, 0, "failed", e.toString())).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnDrawMessage(null, 0, "failed", e.toString())).build();
        }
        catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnDrawMessage(null, 0, "failed", e.toString())).build();
        }
        catch(SQLException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnDrawMessage(null, 0, "failed", e.toString())).build();
        }


    }



    @POST
    @Path("/manage/update/{jobid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response drawByJobID(String json,@PathParam("jobid")int jobID,
                               @HeaderParam("AUTHORIZATION") String token){

        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        Auth au=new Auth();
        try {
            if(!au.checkChartBuilder(token)){
                return Response.ok(MessageFactory.rtnDrawMessage(null, 0, "failed","You are not a chart builder")).build();
            }else{
            Map datas = gson.fromJson(json, type);
            DrawUtils dr=new DrawUtils();
            String table=dr.getLastResult(jobID);
            if(table.endsWith("")){
                return Response.ok(MessageFactory.rtnDrawMessage(null, 0, "sucess", "No execution record")).build();
            }else{
                if (!(Boolean) au.verify(token).get(4)) {//check login
                    return Response.ok(MessageFactory.rtnDrawMessage(null, 0, "failed","Permission Denied")).build();
                } else if ((au.groupMatch(token, jobID)) || ((Integer) au.verify(token).get(0) == 2)) {
                    String rtn=dr.draw(datas,table);
                    return Response.ok(rtn).build();
                }
                else {
                    return Response.ok(MessageFactory.rtnDrawMessage(null, 0, "failed","Permission Denied")).build();
                }
            }
        }

        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnDrawMessage(null, 0, "failed",e.toString())).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnDrawMessage(null, 0, "failed", e.toString())).build();
        }
        catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnDrawMessage(null, 0, "failed", e.toString())).build();
        }catch(SQLException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnDrawMessage(null, 0, "failed", e.toString())).build();
        }


    }
    @POST
    @Path("/draw/pie/{jobhistoryid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response drawPieByJHID(String json, @PathParam("jobhistoryid")int jobhistoryid,
                               @HeaderParam("AUTHORIZATION") String token){
//Todo permission check
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        Auth au=new Auth();
        try {
            Map<String,String> datas = gson.fromJson(json, type);
            DrawUtils dr=new DrawUtils();
            String table=dr.getResultTable(jobhistoryid);
            if(table.equals("")){
                return Response.ok(MessageFactory.rtnPieMessage(null,"sucess", "No execution record")).build();
            }else{
                if (!(Boolean) au.verify(token).get(4)) {//check login
                    return Response.ok(MessageFactory.rtnPieMessage(null,  "failed","Permission Denied")).build();
                } else if ((au.groupMatchwithJHid(token, jobhistoryid)) || ((Integer) au.verify(token).get(0) == 2)) {
                    String rtn=dr.drawPie(datas,table);
                    return Response.ok(rtn).build();
                }
                else {
                    return Response.ok(MessageFactory.rtnPieMessage(null, "failed","Permission Denied")).build();
                }

            }
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnPieMessage(null,"failed", e.toString())).build();
        }catch (NullPointerException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnPieMessage(null,"failed", e.toString())).build();
        }
        catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnPieMessage(null,"failed", e.toString())).build();
        }
        catch(SQLException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnPieMessage(null,"failed",  e.toString())).build();
        }
    }
}
