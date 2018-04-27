package com.chickling.controllers;

import com.chickling.boot.Init;
import com.chickling.util.YamlLoader;
import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by ey67 on 2015/10/6.
 * Load static page
 */

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class LoadPage {
    @GET public Response index(){
        return Response.ok(new Viewable("/page.ftl")).build();
    }
    @GET
    @Path("/index")
    public Response getIndex(){return Response.ok(new Viewable("/page.ftl")).build(); }
    @GET
    @Path("/status")
    public Response getStatus(){
        Map pageSetting=new HashMap();
        pageSetting.put("layer","");
        pageSetting.put("status","active");
        return Response.ok(new Viewable("/status.ftl",pageSetting)).build(); }
    @GET
    @Path("/joblist")
    public Response getJobList(){
        Map pageSetting=new HashMap();
        pageSetting.put("layer","");
        pageSetting.put("job","active");
        return Response.ok(new Viewable("/joblist.ftl",pageSetting)).build();
    }
    @GET
    @Path("/joblist/add")
    @Template(name="/job.ftl")
    public Map<String, Object> getJob(){
        Map<String, Object> map = new HashMap<>();
        map.put("localpath", Init.getCsvlocalPath());
        map.put("layer", "../");
        map.put("job","active");
        return map;
    }
    @GET
    @Path("/joblist/edit")
    @Template(name="/jobedit.ftl")
    public Map<String, Object> getJobEdit(){
        Map<String, Object> map = new HashMap<>();
        map.put("localpath", Init.getCsvlocalPath());
        map.put("layer", "../");
        map.put("job","active");
        return map;
    }
    @GET
    @Path("/schedulelist")
    public Response getScheduleList(){
        Map pageSetting=new HashMap();
        pageSetting.put("layer","");
        pageSetting.put("schedule","active");
        return Response.ok(new Viewable("/schedulelist.ftl",pageSetting)).build();
    }
    @GET
    @Path("/schedulelist/add")
    public Response getSchedule(){
        Map pageSetting=new HashMap();
        pageSetting.put("layer","../");
        pageSetting.put("schedule","active");
        return Response.ok(new Viewable("/schedule.ftl",pageSetting)).build();
    }
    @GET
    @Path("/schedulelist/edit")
    public Response getScheduleEdit(){
        Map pageSetting=new HashMap();
        pageSetting.put("layer","../");
        pageSetting.put("schedule","active");
        return Response.ok(new Viewable("/scheduleedit.ftl",pageSetting)).build();
    }
    @GET
    @Path("/queryui")
    public Response getQueryui(){
        Map pageSetting=new HashMap();
        pageSetting.put("layer","");
        pageSetting.put("queryui","active");
        return Response.ok(new Viewable("/queryui.ftl",pageSetting)).build();
    }
    @GET
    @Path("/login")
    public Response getLogin(){
        return Response.ok(new Viewable("/login.ftl")).build();
    }
    @GET
    @Path("/usermanage")
    public Response getUserManage(){
        Map pageSetting=new HashMap();
        pageSetting.put("layer","");
        return Response.ok(new Viewable("/usermanage.ftl",pageSetting)).build();
    }
    @GET
    @Path("/resultview")
    public Response getResult(){
        Map pageSetting=new HashMap();
        pageSetting.put("layer","");
        pageSetting.put("limit", YamlLoader.instance.getDownloadLimit().toString());
        return Response.ok(new Viewable("/resultviewer.ftl",pageSetting)).build();
    }
    @GET
    @Path("/page")
    public Response getPage(){
        return Response.ok(new Viewable("/index.ftl")).build();
    }
    @GET
    @Path("/realtime/query")
    public Response getRTQuery(){
        Map pageSetting=new HashMap();
        pageSetting.put("layer","../");
        return Response.ok(new Viewable("/realtime-query.ftl",pageSetting)).build();
    }
    @GET
    @Path("/charts/builder")
    public Response getChartBuilder(){
        Map pageSetting=new HashMap();
        pageSetting.put("layer","../");
        return Response.ok(new Viewable("/chartbuilder.ftl",pageSetting)).build();
    }
    @GET
    @Path("/charts/draw")
    public Response getChart(){
        Map pageSetting=new HashMap();
        pageSetting.put("layer","../");
        return Response.ok(new Viewable("/chartpage.ftl",pageSetting)).build();
    }
    @GET
    @Path("/tabletool/create")
    public Response getCreateTable(){
        Map pageSetting=new HashMap();
        pageSetting.put("layer","../");
        pageSetting.put("tabletool","active");
        return Response.ok(new Viewable("/tabletool_internal.ftl",pageSetting)).build();
    }

    @GET
    @Path("/tabletool/external")
    public Response getCreateExternalTable(){
        Map pageSetting=new HashMap();
        pageSetting.put("layer","../");
        pageSetting.put("tabletool","active");
        return Response.ok(new Viewable("/tabletool_external.ftl",pageSetting)).build();
    }

}
