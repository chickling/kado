package com.chickling.controllers;


import com.chickling.bean.result.ResultMap;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.chickling.models.MessageFactory;
import com.chickling.models.job.PrestoContent;
import com.chickling.util.JobCRUDUtils;
import com.chickling.util.PrestoUtil;
import com.chickling.util.TimeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by gl08 on 2015/11/27.
 */
@Path("/presto/table")
public class Presto {
    /*Log4J*/
    Logger log = LogManager.getLogger(Presto.class);
    private static Gson gson=new Gson();

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTableList(@Context ServletContext context,@HeaderParam("AUTHORIZATION") String token){

        PrestoUtil prestoUtil=new PrestoUtil();
        String sql="SELECT * FROM information_schema.tables where table_schema<> 'presto_temp'";
        ResultMap resultMap=prestoUtil.doJdbcRequest(sql);

        if (resultMap.getData().size()==0){
            log.error("get Presto Table List Error:");
            log.error(prestoUtil.getException());
            return Response.ok(MessageFactory.message("error","get Presto Table List Error")).build();
        }else {
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", "error");
            responseMap.put("list", "");

            ArrayList<HashMap> tableList = new ArrayList<>();
            resultMap.getData().forEach( data ->{
                HashMap<String, String> tableMap = new HashMap<>();
                tableMap.put("tablename", data.get(1) + "." + data.get(2));
                tableMap.put("table_type", (String) data.get(3));
                tableMap.put("path", "");
                tableList.add(tableMap);
            });
            responseMap.put("list", tableList);
            responseMap.put("status", "success");
            responseMap.put("time", TimeUtil.toString(DateTime.now()));

                return Response.ok(gson.toJson(responseMap)).build();

        }
    }


    @GET
    @Path("schemas/{tablename}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSchemas(
            @Context ServletContext context,
            @PathParam("tablename") String tablename,
            @HeaderParam("AUTHORIZATION") String token){
        PrestoUtil prestoUtil=new PrestoUtil();
//        String     responseStr=prestoUtil.post("DESC " +tablename,PrestoContent.QUERY_UI);
        String sql="DESC " +tablename;
        ResultMap resultMap=prestoUtil.doJdbcRequest(sql);


        if (resultMap.getData().size()==0){
            log.error("get Presto Table Schema Error:");
            log.error(prestoUtil.getException());
            return Response.ok(MessageFactory.message("error", "get Presto Table Schema Error")).build();
        }else{
//            HashMap response=gson.fromJson(responseStr,HashMap.class);
            HashMap<String,Object> responseMap=new HashMap<>();
            responseMap.put("partition","");
            responseMap.put("column","");
            responseMap.put("status","error");
            responseMap.put("path","");
            responseMap.put("table_type","");


            ArrayList<String[]> partitionList = new ArrayList<>();
            ArrayList<HashMap> colMaps = new ArrayList<>();

            resultMap.getData().forEach( data ->{
                HashMap<String, Object> col = new LinkedHashMap<>();
                col.put("column", data.get(0));
                col.put("type", data.get(1));
                boolean partition=false;
                if(data.size()>2) {
                    if (data.get(2).equals("partition key"))
                        partition = true;
                }

                col.put("partition_key",partition);
                if (partition) {
                    partitionList.add(new String[]{(String) data.get(1), (String) data.get(2)});
                }
                colMaps.add(col);
            });


            responseMap.put("partition",partitionList);
            responseMap.put("column",colMaps);
            responseMap.put("status","success");
            responseMap.put("time",TimeUtil.toString(DateTime.now()));

            return Response.ok(gson.toJson(responseMap)).build();
        }
    }

    @GET
    @Path("schemas/job/result/{jhid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobResultSchemas(
            @Context ServletContext context,
            @PathParam("jhid") Integer jhid,
            @HeaderParam("AUTHORIZATION") String token){
        Map jobHistory= JobCRUDUtils.getJobHistoryInfo(jhid);
        String tablename="";
        if(jobHistory.get("JobOutput")!=null){
            tablename=(String) jobHistory.get("JobOutput");
            tablename=tablename.substring(tablename.lastIndexOf("/")+1,tablename.length());
            if("".equalsIgnoreCase(tablename))
                return Response.ok(MessageFactory.message("error","tablename is Empty")).build();
        }else {
            return Response.ok(MessageFactory.message("error","can't find JHID!")).build();
        }

        PrestoUtil prestoUtil=new PrestoUtil();
        String sql="DESC presto_temp." +tablename;
        ResultMap resultMap=prestoUtil.doJdbcRequest(sql);

        if (resultMap.getData().size()==0){
            log.error("get Presto Table Schema Error:");
            log.error(prestoUtil.getException());
            return Response.ok(MessageFactory.message("error", "get Presto Table Schema Error")).build();
        }else{
            HashMap<String,Object> responseMap=new HashMap<>();
            responseMap.put("partition","");
            responseMap.put("column","");
            responseMap.put("status","error");
            responseMap.put("path","");
            responseMap.put("table_type","");

            ArrayList<String[]> partitionList = new ArrayList<>();
            ArrayList<HashMap> colMaps = new ArrayList<>();

            resultMap.getData().forEach( data -> {
                HashMap<String, Object> col = new LinkedHashMap<>();
                col.put("column", data.get(0));
                col.put("type", data.get(1));
                boolean partition=false;
                if(data.size()>2) {
                    if (data.get(2).equals("Partition Key"))
                        partition = true;
                }
                col.put("partition_key",partition);
                if (partition) {
                    partitionList.add(new String[]{(String) data.get(0), (String) data.get(1)});
                }
                colMaps.add(col);
            });
            responseMap.put("partition",partitionList);
            responseMap.put("column",colMaps);
            responseMap.put("status","success");
            responseMap.put("time",TimeUtil.toString(DateTime.now()));

            return Response.ok(gson.toJson(responseMap)).build();
        }
    }
    @GET
    @Path("/partitions/list/{tablename}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPartitions(
            @Context ServletContext context,
            @PathParam("tablename") String tablename,
            @HeaderParam("AUTHORIZATION")String token){
        PrestoUtil prestoUtil=new PrestoUtil();
        String sql="SHOW PARTITIONS FROM " + tablename;
        HashMap<String, Object> responseMap = new HashMap<>();

        ResultMap resultMap=prestoUtil.doJdbcRequest(sql);
        responseMap.put("status", "error");
        if (resultMap.getData().size()==0){
            log.error("get Presto Table Partitions Error:");
            log.error(prestoUtil.getException());
            responseMap.put("partition", new ArrayList<>());
            responseMap.put("list", new ArrayList<>());
            responseMap.put("status", "success");
        }else {
            responseMap.put("partition", "");
            responseMap.put("list", "");
            // get Column Name With ArrayList
            ArrayList<String[]> partitionList = new ArrayList<>();
            ArrayList<HashMap> partitionMaps = new ArrayList<>();

            for (int i = 0;i<resultMap.getSchema().size();i++){
                partitionList.add(new String[]{resultMap.getSchema().get(i),resultMap.getType().get(i)});
            }
            resultMap.getData().forEach( data->{
                HashMap<String, Object> partionKV = new LinkedHashMap<>();
                for (int i = 0; i < partitionList.size(); i++) {
                    partionKV.put(partitionList.get(i)[0], data.get(i));
                }
                partitionMaps.add(partionKV);
            });
            responseMap.put("partition", partitionList);
            responseMap.put("list", partitionMaps);
            responseMap.put("status", "success");
            responseMap.put("time", TimeUtil.toString(DateTime.now()));

        }
        return Response.ok(gson.toJson(responseMap)).build();
    }


    @GET
    @Path("sample/{tablename}/{limit}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSampleData(
            @Context ServletContext context,
            @PathParam("tablename") String tablename,
            @PathParam("limit") String limit,
            @HeaderParam("AUTHORIZATION")String token){
        PrestoUtil prestoUtil=new PrestoUtil();
//        String responseStr=prestoUtil.post("SELECT*  FROM " + tablename + " limit " + limit, PrestoContent.QUERY_UI);
        String sql="SELECT*  FROM " + tablename + " limit " + limit;
        ResultMap resultMap=prestoUtil.doJdbcRequest(sql);


        if (resultMap.getData().size()==0) {
            log.error("get Table [ " + tablename + " ] Sample Error:");
            log.error(prestoUtil.getException());
            return Response.ok(MessageFactory.message("error", "get Table [ " + tablename + " ] Sample Error")).build();
        }else {

//            HashMap response = gson.fromJson(responseStr, HashMap.class);
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", "error");
            responseMap.put("list", "");

            // get Column Name With ArrayList
            ArrayList<HashMap> columnsMaps = new ArrayList<>();

            resultMap.getData().forEach( data->{
                HashMap<String, Object> col = new LinkedHashMap<>();
                for (int i =0 ; i<data.size();i++){
                    col.put(resultMap.getSchema().get(i),data.get(i));
                }
                columnsMaps.add(col);
            });

            responseMap.put("status", "success");
            responseMap.put("list", columnsMaps);

            responseMap.put("time", TimeUtil.toString(DateTime.now()));
            return Response.ok(gson.toJson(responseMap)).build();
        }
    }

    @GET
    @Path("count/{tablename}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTableCount(
            @Context ServletContext context,
            @PathParam("tablename") String tablename,
            @HeaderParam("AUTHORIZATION") String token){
        PrestoUtil prestoUtil=new PrestoUtil();
        String sql="SELECT COUNT(*)  FROM " + tablename;
        ResultMap resultMap=prestoUtil.doJdbcRequest(sql);
//        String responseStr=prestoUtil.post("SELECT COUNT(*)  FROM " + tablename, PrestoContent.QUERY_UI);
        if (resultMap.getCount()>0) {
            log.error("get  Table [ "+tablename+" ]  Count Error");
            log.error(prestoUtil.getException());
            return Response.ok(MessageFactory.message("error", "get  Table [ "+tablename+" ]  Count Error")).build();
        }else {
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", "error");
            responseMap.put("count", "");

            responseMap.put("status", "success");
            responseMap.put("count", resultMap.getCount());
            responseMap.put("time", TimeUtil.toString(DateTime.now()));

            return Response.ok(gson.toJson(responseMap)).build();
        }
    }

}