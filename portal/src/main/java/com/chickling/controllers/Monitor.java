package com.chickling.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.chickling.util.PrestoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by jw6v on 2016/2/15.
 */
@Path("/monitor")
public class Monitor {
    private static Gson gson=new Gson();

    @GET
    @Path("node")
    @Produces(MediaType.APPLICATION_JSON)
    public Response monitorNode(@Context ServletContext context){
        PrestoUtil prestoUtil=new PrestoUtil();
        String json=prestoUtil.getNode(0,null);
        Type stringStringMap = new TypeToken<Map<String, String>>(){}.getType();
        Map<String,String> map = gson.fromJson(json, stringStringMap);
        return Response.ok(gson.toJson(prestoUtil.getNode(0,null))).build();
    }

    @GET
    @Path("query")
    @Produces(MediaType.APPLICATION_JSON)
    public Response monitoQuery(@Context ServletContext context){
        PrestoUtil prestoUtil=new PrestoUtil();
        return Response.ok(gson.toJson(prestoUtil.getQuery(0,null))).build();
    }
}

