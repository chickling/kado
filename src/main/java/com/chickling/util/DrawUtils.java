package com.chickling.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.chickling.DB.ConnectionManager;
import com.chickling.models.MessageFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by jw6v on 2017/1/25.
 */
public class DrawUtils {

    private static Logger log = LogManager.getLogger(DrawUtils.class);

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
        String rtn=pu.post(query,0,null);

        return rtn;
    }




    public String draw(Map datas, String table){
        //        {
//            "table_name":"",
//                "sort":"DESC",
//                "limit":1000,
//                "xAxis":"",
//                "yAxis":["",""]
//        }

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
        return MessageFactory.rtnDrawMessage((Map) rtn.get("data"),count,"success","");}


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
       // String QuerySQL="Select * from `Job_Log`";
        PreparedStatement stat = null;
        ResultSet rs = null;
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
        stat.setInt(1, JHID);
        rs = stat.executeQuery();
        //rs.getString("JobOutput");
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
        PrestoUtil pu=new PrestoUtil();
        String rtn=pu.post(query,0,null);

        return rtn;
    }


    public ArrayList<Map> strcTransfer4Pie(List x_axis, String queryResult){
//        {
//            "axis":["",""]
//        }
//
////data response for pie chart
//        {
//            "count":2,
//                "data":{
//            "axis1":30,
//                    "axis2":80
//        }
//        }
        Gson gson =new Gson();
        ArrayList<Integer> index_y=new ArrayList<>();
        int index_x=0;
        Type type = new TypeToken<Map>() {}.getType();
        Map<String,Map> obj = gson.fromJson(queryResult, type);
        ArrayList<Map> ob=(ArrayList<Map>)obj.get("columns");


        ArrayList<Map> rtndata=new ArrayList<>();
        ArrayList<String> cols=new ArrayList<>();
        for(Map m: ob){
            String col=(String)m.get("name");
            cols.add(col);
        }

        ArrayList<ArrayList<Object>> data= (ArrayList<ArrayList<Object>>)obj.get("data");
        //count=data.size();
        Map m=new HashMap<String,Object>();
        for(ArrayList<Object> al:data){
            while(index_x<cols.size()){
                Object x_value=al.get(index_x);
                m.put(cols.get(index_x),x_value);
                index_x++;
            }
        }
        rtndata.add(m);
        HashMap<String,ArrayList> result=new HashMap<>();
        //result.put("data",rtndata);
        return rtndata;

    }


    public Map strcTransfer(ArrayList<String> y_axis,String x_axis,String queryResult){
        Gson gson =new Gson();
        //ArrayList<String> y_axis=new ArrayList<>();
        //y_axis.add("Session_Revenue");
        //String x_axis="SessionID";
        //PrestoUtil pu=new PrestoUtil();
        ArrayList<Integer> index_y=new ArrayList<>();
        // HashMap<String,Integer> index_y=new HashMap<>();
        int index_x=0;
        //queryResult="{\"columns\":[{\"name\":\"SessionID\",\"type\":\"varchar\",\"typeSignature\":{\"rawType\":\"varchar\",\"typeArguments\":[],\"literalArguments\":[],\"arguments\":[{\"kind\":\"LONG_LITERAL\",\"value\":2147483647}]}},{\"name\":\"Session_Revenue\",\"type\":\"double\",\"typeSignature\":{\"rawType\":\"double\",\"typeArguments\":[],\"literalArguments\":[],\"arguments\":[]}}],\"data\":[[\"3151264397613666175-4611692630529713435-1\",60750.0],[\"3151126207039473638-4611705302830572357-1\",60696.0],[\"3151178977155024387-6917530359080977004-2\",46197.0],[\"3150632603629719247-4611713543262272744-6\",22558.680000000015],[\"3151287788003993622-4611687349867280630-1\",16875.0],[\"3150972734973384920-6917530180303140905-3\",13364.289999999999],[\"3148663253748922217-4611687157669858050-52\",12889.810000000001],[\"3151185224184958822-6917530364986543016-1\",12300.0],[\"3133924768529594620-6917530342438136722-217\",11709.71],[\"3151158913215312480-4611687332150559018-2\",9753.769999999999],[\"3068627390738531128-4611687205448983665-18\",8619.82],[\"3150311838290786275-4611687155520030768-5\",7791.439999999992],[\"3143584444185402982-4611687541530539209-113\",7763.299999999999],[\"3134894531344414368-6917547749940428381-4\",7695.839999999998],[\"3151256290862908888-6917535647796321668-2\",7489.86],[\"3149552999173665683-6917548286274584124-6\",7419.0],[\"3151194497019351849-6917530336532820675-1\",6967.7699999999995],[\"3131522945510086121-6917556524021730894-2\",6899.9400000000005],[\"3151244932820313901-6917530230768856997-1\",6898.97],[\"3151106699299477295-4611692644488222339-3\",6812.909999999999],[\"3097027492617730837-4611692678847947381-373\",6474.8],[\"3151186343023938236-4611687135655823588-1\",6407.0],[\"3150906528052087830-4611687210280918123-4\",6399.72],[\"3109097110928706745-6917535681082321711-6\",6265.83],[\"3148424842261516674-4611705298535647300-3\",6054.62],[\"3073807331752486911-4611692690122307436-196\",5912.63],[\"3146040907779245895-4611687160894536022-5\",5899.95],[\"3149978615400176999-4611687334298151219-12\",5890.7699999999995],[\"3082344459720013038-6917530214663008361-2\",5850.03],[\"3123591012792479767-4611692655762483635-25\",5684.32],[\"3119405822711500481-4611687426103018448-336\",5659.919999999999],[\"3150553030768796321-6917530189966674557-32\",5549.919999999999],[\"3138216282673465693-4611692691196162168-46\",5448.15],[\"3045458035041438543-4611687196859982537-9\",5401.879999999999],[\"3106496153895019844-6917530192650992159-91\",5305.849999999999],[\"3141917464298334299-4611692707839127315-5\",5305.8499999999985],[\"3150249456038283323-4611687135656226241-8\",5234.16],[\"3146941212381387723-6917530193726307179-3\",5087.97],[\"3146937780702240695-6917548316339299677-6\",4897.4],[\"3133552081332862786-4611687214576221668-177\",4874.7],[\"3108880208637808893-4611692666499935688-29\",4837.460000000001],[\"3145787466053651816-4611687222628979424-5\",4799.9400000000005],[\"3085362140250987026-4611692640730088468-3\",4687.5599999999995],[\"3149218167819675718-4611692656299362246-39\",4669.929999999999],[\"3146788889218330067-4611687218870857960-8\",4649.97],[\"3128358400721509262-6917530577587451308-163\",4621.81],[\"3151184947160011856-6917530210367714895-1\",4549.74],[\"3151221544575908039-6917530231305683357-2\",4526.48],[\"3109096324948113462-6917530215736491750-16\",4499.06],[\"3133016531122862283-4611687360604678095-165\",4319.119999999999],[\"3151248377384089714-6917530344585450704-1\",4269.929999999999],[\"3110388521693678708-4611687187195642384-21\",4224.759999999999],[\"3150889958068258014-4611687161425621409-2\",4143.89],[\"3151198961637857288-6917530217884308316-2\",4139.94],[\"3117105768941832106-4611692690123025326-22\",4039.7000000000003],[\"3140272079505536634-4611687147467228008-15\",4026.8900000000003],[\"3149518224972406417-6917535671418640100-5\",4018.74],[\"3151191958693813794-6917548286274452469-1\",3999.0],[\"3151183224877689449-4611687357920343922-1\",3999.0],[\"3122286949050691377-6917530138964099662-21\",3911.7599999999998],[\"3146780024407675262-4611713529304093327-8\",3815.8399999999992],[\"3141770224229751123-6917530478803357004-2\",3799.96],[\"3151253398202419755-6917535717052654312-1\",3793.1400000000003],[\"3151254968011533457-4611705271692087274-1\",3793.1399999999994],[\"3151265988899052048-6917535632764170510-2\",3787.8499999999985],[\"3151221364187706911-4611687146934283742-1\",3706.2699999999995],[\"3119640491136198215-6917535632763913875-78\",3659.98],[\"3122911381460948874-6917530218420802125-39\",3644.8999999999996],[\"3151194673113008421-4611687335371765567-2\",3578.99],[\"3151026669026347152-6917535681082332663-2\",3559.8],[\"3122081570157483143-4611687214039181781-145\",3539.95],[\"3151273103511240914-6917530202314644016-1\",3538.95],[\"3141896416809960732-6917530200704682646-10\",3524.8500000000004],[\"3146269715719071828-6917530166345179092-7\",3499.98],[\"3151093591057696652-6917530188892973791-2\",3436.32],[\"3137621921475139132-4611710240432517255-29\",3429.96],[\"3079204344706973899-4611692631603283775-128\",3414.76],[\"3151057672246338422-4611705267933998405-2\",3399.98],[\"3117036199059984141-4611687161962502661-15\",3399.9],[\"3129186927828347069-6917530366063663105-15\",3379.64],[\"3131342370052310919-6917535697188426981-47\",3362.9299999999994],[\"3144227521046855511-4611687421271136960-4\",3325.8999999999996],[\"3151246274997592478-6917530183524682256-2\",3325.8999999999987],[\"3147310147925794649-4611692664889370616-18\",3299.7],[\"3149920740717431997-6917535681619241814-6\",3287.91],[\"3133943241185495746-6917535690209181691-29\",3260.339999999999],[\"3023181843564938244-4611692669184263083-418\",3254.0299999999997],[\"3151012555762246464-6917530174397415276-5\",3249.95],[\"3150855682081888389-6917548307212412802-4\",3236.95],[\"3144677219155127106-4611687128676439277-5\",3235.98],[\"3151220840202847075-6917535687525400244-1\",3222.54],[\"3151242626424439046-6917535675176913187-1\",3199.99],[\"3135274107667977627-6917530187819148657-56\",3187.6899999999996],[\"3149505915596398611-4611713569032003977-21\",3184.41],[\"3138970970704402389-4611692636435201604-8\",3180.6],[\"3151201547208171524-4611687357920497921-1\",3167.88],[\"3151234882598409038-6917535644038513067-1\",3149.99],[\"3151036233917071645-6917548279295153422-3\",3128.99],[\"3151093591057696652-6917530188892973791-5\",3120.87],[\"3150882712460278636-6917530155070163463-9\",3111.96]]}";
        Type type = new TypeToken<Map>() {}.getType();
        Map<String,Map> obj = gson.fromJson(queryResult, type);
        ArrayList<Map> ob=(ArrayList<Map>)obj.get("columns");

//        {
//            "table_name":"",
//                "sort":"DESC",
//                "limit":1000,
//                "xAxis":"",
//                "yAxis":["",""]
//        }
//
////data response for line/bar chart
//        {
//            "count":1000,
//                "data":{
//            "yAxis1":[{"x":1,"y":10},{"x":2,"y":20}],
//            "yAxis2":[{"x":1,"y":10},{"x":2,"y":20}]
//        }
//        }

        HashMap<String,ArrayList<Map>> rtndata=new HashMap<>();
        ArrayList<String> cols=new ArrayList<>();
        int i=0;
        for(Map m: ob){

            String col=(String)m.get("name");
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
        //System.out.println(gson.toJson(result));


        return result;
    }
//    public static void main(String args[]) throws SQLException{
//        ArrayList<String> y_axis=new ArrayList<>();
//        y_axis.add("campaign");
//        y_axis.add("position");
//        y_axis.add("strategy");
//        DrawUtils draw=new DrawUtils();
//        Map json=new LinkedHashMap();
//        Gson gson=new Gson();
//        json.put("sort","DESC");
//        json.put("limit",10);
//        json.put("xAxis","session");
//        json.put("yAxis",y_axis);
//        DrawUtils dr=new DrawUtils();
//        // String table=dr.getLastResult(1);
//        String table=dr.getResultTable(1);
//        String rtn=dr.draw(json,table);
//        System.out.println(rtn);
//    }
}
