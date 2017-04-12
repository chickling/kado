package com.chickling.util;

import com.chickling.bean.result.ResultMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.chickling.sqlite.ConnectionManager;
import com.chickling.models.MessageFactory;


import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by jw6v on 2017/1/25.
 */
public class DrawUtils {
    private int count=0;
    public String drawPie(Map datas, String table){

        List x_axis=(List) datas.get("axis");
        String result=getQueryResult4Pie(1,x_axis,table);
        if(result.equals("")){
            return MessageFactory.rtnPieMessage(null,"success","No result");
        }else{
            List<Map> rtn=strcTransfer4Pie(x_axis,result);
            return MessageFactory.rtnPieMessage(rtn,"success","");
        }


    }

    public String getQueryResult4Pie(int limit,List x_axis,String table){

        String query="";
        String selectTemplate="Select ";
        for(int i=0;i<x_axis.size();i++){
            if(i<x_axis.size()-1){
                selectTemplate=selectTemplate+x_axis.get(i)+",";
            }else{
                selectTemplate=selectTemplate+x_axis.get(i)+" ";
            }
        }
        query=selectTemplate+"from presto_temp."+table+ " limit "+ limit;
        PrestoUtil pu=new PrestoUtil();
        ResultMap resultMap=new PrestoUtil().doJdbcRequest(query);
        return new Gson().toJson(resultMap);
    }




    public String draw(Map datas, String table){
    /*
            {
                "table_name":"",
                    "sort":"DESC",
                    "limit":1000,
                    "xAxis":"",
                    "yAxis":["",""]
            }
        */

        String orderOption=(String) datas.get("sort");
        int limit= ((Double) datas.get("limit")).intValue();
        String x_axis=(String) datas.get("xAxis");
        ArrayList<String> y_axis=(ArrayList<String>)datas.get("yAxis");
        ArrayList<String> cols=new ArrayList<>();
        cols.add(x_axis);
        for(String y:y_axis){
            cols.add(y);
        }
        String result=getQueryResult(cols,limit,x_axis,orderOption,table);

        if(result.equals("")){
            return MessageFactory.rtnDrawMessage(null,count,"success","");
        }else{
            Map rtn=strcTransfer(y_axis,x_axis,result);
            return MessageFactory.rtnDrawMessage((Map) rtn.get("data"),count,"success","");
        }
    }


    public String getLastResult(int JobID)throws SQLException{
        String QuerySQL="Select JobOutput from main.Job_Log JOIN main.Job_History on JobLog=JLID where JobID=? and JobStatus=1 and ResultCount>0 order by JobStopTime DESC limit 1";
        PreparedStatement stat = null;
        ResultSet rs = null;
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
        stat.setInt(1, JobID);
        rs = stat.executeQuery();

        if(!rs.next()){
            return "";
        }
        else{
            String jobOutPut=rs.getString("JobOutput");
            String tmp[]=jobOutPut.split("\\/");
            jobOutPut=tmp[tmp.length-1];
            return jobOutPut;
        }
    }

    public String getResultTable(int JHID) throws SQLException{
        String QuerySQL="Select `JobOutput` from `main`.`Job_Log` JOIN `main`.`Job_History` on `JobLog`=`JLID` where `JHID`=?";
        PreparedStatement stat = null;
        ResultSet rs = null;
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
        stat.setInt(1, JHID);
        rs = stat.executeQuery();
        if(!rs.next()){
            return "";
        }
        else{
            String jobOutPut=rs.getString("JobOutput");
            String tmp[]=jobOutPut.split("\\/");
            jobOutPut=tmp[tmp.length-1];
            return jobOutPut;}
    }

    public String getQueryResult(ArrayList<String> cols,int limit, String orderby, String orderOption,String table){
        String query="";
        String selectTemplate="Select ";
        for(int i=0;i<cols.size();i++){
            if(i<cols.size()-1){
                selectTemplate=selectTemplate+cols.get(i)+",";
            }else{
                selectTemplate=selectTemplate+cols.get(i)+" ";
            }
        }
        query=selectTemplate+"from presto_temp."+table+" order by "+ orderby+ " "+orderOption+ " limit "+ limit;
        ResultMap resultMap=new PrestoUtil().doJdbcRequest(query);

        return new Gson().toJson(resultMap);
    }


    public ArrayList<Map> strcTransfer4Pie(List x_axis, String queryResult){
    /*
                {
                    "axis":["",""]
                }

                data response for pie chart
                {
                    "count":2,
                        "data":{
                    "axis1":30,
                            "axis2":80
                }
                }
        */
        Gson gson =new Gson();
        ArrayList<Integer> index_y=new ArrayList<>();
        int index_x=0;
        Type type = new TypeToken<Map>() {}.getType();
        Map<String,Map> obj = gson.fromJson(queryResult, type);


        ArrayList<Map> rtndata=new ArrayList<>();
        ArrayList<String> cols=new ArrayList<>();
        for(String col: (ArrayList<String>) obj.get("schema")){
            cols.add(col);
        }

        ArrayList<ArrayList<Object>> data= (ArrayList<ArrayList<Object>>)obj.get("data");
        Map m=new HashMap<String,Object>();
        for(ArrayList<Object> al:data){
            while(index_x<cols.size()){
                Object x_value=al.get(index_x);
                m.put(cols.get(index_x),x_value);
                index_x++;
            }
        }
        rtndata.add(m);
        return rtndata;

    }


    public Map strcTransfer(ArrayList<String> y_axis,String x_axis,String queryResult){
    /*
            {
                "table_name":"",
                    "sort":"DESC",
                    "limit":1000,
                    "xAxis":"",
                    "yAxis":["",""]
            }
            data response for line/bar chart
            {
                "count":1000,
                    "data":{
                "yAxis1":[{"x":1,"y":10},{"x":2,"y":20}],
                "yAxis2":[{"x":1,"y":10},{"x":2,"y":20}]
            }
            }
        */
        Gson gson =new Gson();
        ArrayList<Integer> index_y=new ArrayList<>();
        int index_x=0;
        Type type = new TypeToken<Map>() {}.getType();
        Map<String,Map> obj = gson.fromJson(queryResult, type);
        HashMap<String,ArrayList<Map>> rtndata=new HashMap<>();
        ArrayList<String> cols=new ArrayList<>();
        int i=0;
        for(String col:(ArrayList<String>) obj.get("schema")){

            cols.add(col);
            if(y_axis.contains(col)){
                index_y.add(i);
                rtndata.put(col,new ArrayList<Map>());
            }
            else if(x_axis.equals(col)){
                index_x=i;
            }
            i++;
        }

        ArrayList<ArrayList<Object>> data= (ArrayList<ArrayList<Object>>)obj.get("data");
        count=data.size();
        for(ArrayList<Object> al:data){
            Object x_value=al.get(index_x);
            for(int y : index_y){
                Map m=new HashMap<String,Object>();
                m.put("x",x_value);
                m.put("y",al.get(y));
                rtndata.get(cols.get(y)).add(m);
            }
        }
        HashMap<String,Map> result=new HashMap<>();
        result.put("data",rtndata);
        return result;
    }
}
