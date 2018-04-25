package com.chickling.controllers;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.chickling.boot.Init;
import com.chickling.models.HiveJDBC;
import com.chickling.models.MessageFactory;
import com.chickling.models.tabletool.Status;
import com.chickling.models.tabletool.TableCreate;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ey67 on 2018/2/1.
 */
@Path("/table/tool")
public class TableTool {
    /*Log4J*/
    Logger log = LogManager.getLogger(TableTool.class);
    final String UPLOAD_FILE_PATH="upload/";
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {
        String dateString=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        final String finalFileName=dateString+"#"+fileDetail.getFileName();
        String uploadedFileLocation =  UPLOAD_FILE_PATH+finalFileName;
        try {
            // save file to disk
            writeToFile(uploadedInputStream, uploadedFileLocation);
            return Response.status(200).entity(new TableCreate().getCSVInfo(uploadedFileLocation,finalFileName)).build();
        } catch (IOException e) {
            log.error(ExceptionUtils.getMessage(e));
            return Response.status(200).entity(MessageFactory.message("error",e.getMessage())).build();
        } catch (Exception e) {
            log.error(ExceptionUtils.getMessage(e));
            return Response.status(200).entity(MessageFactory.message("error",e.getMessage())).build();
        }

    }

    // save uploaded file to new location
    private void writeToFile(InputStream uploadedInputStream,
                             String uploadedFileLocation) throws Exception {
        try {
            OutputStream out = new FileOutputStream(new File(
                    uploadedFileLocation));
            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
           log.error(ExceptionUtils.getMessage(e));
           throw new Exception("Write File to Disk Fail!");
        }

    }
    @POST
    @Path("/create/internal/table")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createInternalTable(String json,@HeaderParam("AUTHORIZATION") String token){
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        try {
            Map datas = gson.fromJson(json, type);
            String sql=new TableCreate().createInternalTableSQL(datas);
            log.info("Create Internal SQL:\n{}",sql);
            Map msg=new HashMap();
            msg.put("status","success");
            msg.put("data",sql);
            //Create Table
            HiveJDBC.getInstance().getConnection().createStatement().execute(sql);
            //Insert Data
            new TableCreate().insertCSVDataToTable(datas,UPLOAD_FILE_PATH+datas.get("file_name").toString(),datas.get("file_name").toString());
            return Response.ok(new Gson().toJson(msg)).build();
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        } catch (SQLException e) {
            log.error(ExceptionUtils.getMessage(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        } catch (Exception e) {
            log.error(ExceptionUtils.getMessage(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }

    }

    @POST
    @Path("/create/external/table")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createExternalTable(String json,@HeaderParam("AUTHORIZATION") String token){
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        try {
            Map datas = gson.fromJson(json, type);
            String sql=new TableCreate().createExternalTableSQL(datas);
            log.info("Create External SQL:\n{}",sql);
            Map msg=new HashMap();
            msg.put("status","success");
            msg.put("data",sql);
            //Create Table
            HiveJDBC.getInstance().getConnection().createStatement().execute(sql);
            ArrayList<String> fileList=(ArrayList<String>) datas.get("file_name");
            //Insert Data
            for (String fileName:fileList) {
                new TableCreate().uploadCSVToHDFS(datas, UPLOAD_FILE_PATH + fileName, fileName);
            }
            return Response.ok(new Gson().toJson(msg)).build();
        }catch (JsonSyntaxException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }catch (ClassCastException e){
            log.warn(ExceptionUtils.getStackTrace(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", "Json Class Cast Exception", "")).build();
        } catch (SQLException e) {
            log.error(ExceptionUtils.getMessage(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        } catch (Exception e) {
            log.error(ExceptionUtils.getMessage(e));
            return Response.ok(MessageFactory.rtnJobMessage("error", "", e.getMessage(), "")).build();
        }

    }

    @GET
    @Path("/list/database")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTableList(@HeaderParam("AUTHORIZATION") String token){
        try {
            Map dbList = new HashMap();
            dbList.put("status", "success");
            //dbList.put("data", new TableCreate().getDBList());
            /*Use Whitelist*/
            dbList.put("data", Init.getPrestoDBWhitelist());
            return Response.ok(new Gson().toJson(dbList)).build();
        }catch (Exception e){
            log.error(ExceptionUtils.getMessage(e));
            return Response.ok(MessageFactory.message("error",e.getMessage())).build();
        }
    }

    @GET
    @Path("/get/job/status/{jobID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobStatus(@HeaderParam("AUTHORIZATION") String token,@PathParam("jobID") String jobID){
        try {
            /*Support multi get*/
            String[] jobs=jobID.split(",");
            ArrayList<Status> status=new ArrayList<>();
            Arrays.stream(jobs).forEach(id->{
                status.add(TableCreate.getInsertDataStatus(id));
            });
            Map response = new HashMap();
            response.put("status", "success");
            response.put("data", status);
            return Response.ok(new Gson().toJson(response)).build();
        }catch (Exception e){
            log.error(ExceptionUtils.getMessage(e));
            return Response.ok(MessageFactory.message("error",e.getMessage())).build();
        }
    }
}
