package com.chickling.util;

import com.chickling.bean.result.ResultMap;
import com.google.common.base.Strings;
import com.chickling.boot.Init;
import com.chickling.models.job.PrestoContent;
import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gl08 on 2015/12/3.
 */
public class PrestoUtil {

//    private static Logger logger= LogManager.getLogger(PrestoUtil.class);

    private int RETRY_COUNT=3;
    private long RECONNECT_TIME=500;
    private boolean success=false;
    private Gson gson=new Gson();
    private StringBuilder exception;
    private int batchSize=300;
    private String  newline=System.getProperty("line.separator");

    private String catalog;
    private String prstoUrl;
    private String  jdbcUrl;
    private Properties prop=new Properties();
    public PrestoUtil() {
        prstoUrl=Init.getPrestoURL();
        catalog=Init.getPrestoCatalog();
        prop.setProperty("user",Init.getPresto_user());
        jdbcUrl="jdbc:"+prstoUrl.replaceFirst("http","presto")+"/"+catalog;
        this.exception=new StringBuilder();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getException() {
        return exception.toString();
    }

    public void setException(String exception) {
        this.exception.append(exception).append("\n");
    }



    public String getQuery(Integer jobType, String... schema){
        return doHttpRequest(null, Init.getPrestoURL()+"/v1/query","GET",jobType,schema);
    }

    public String getNode(Integer jobType, String... schema){
        return doHttpRequest(null, Init.getPrestoURL()+"/v1/node","GET",jobType,schema);
    }

    public  String get(String prestoid,Integer jobType,String... schema)   {

        return doHttpRequest(null, Init.getPrestoURL()+"/v1/query/"+prestoid,"GET",jobType,schema);
    }

    public  String getStatement(String prestoid , String nextUrlPage,Integer jobType,String... schema)  {
        if (Strings.isNullOrEmpty(nextUrlPage))
            return doHttpRequest(null, Init.getPrestoURL()+"/v1/statement/"+prestoid,"GET",jobType,schema);
        return doHttpRequest(null, Init.getPrestoURL()+"/v1/statement/"+prestoid+"/"+nextUrlPage,"GET",jobType,schema);
    }

    public  String post(String SQL,Integer jobType,String... schema) {
        String result = "";
        String temp="";
        ArrayList tempdata=new ArrayList();
        AtomicInteger page=new AtomicInteger(1);
        boolean postRunning=true;
        String jobstatus="";
        // send async post
        try {
            temp =postStatement(SQL, jobType, Init.getDatabase());
            Thread.sleep(PrestoContent.POST_START_WAIT_TIME);

            // get response
            HashMap queryMap= gson.fromJson(temp, HashMap.class);
            String prestoid = ((String) queryMap.get("id"));

            do {
                temp = getStatement(prestoid, String.valueOf(page), jobType);
                queryMap = gson.fromJson(temp, HashMap.class);
                if (!Strings.isNullOrEmpty(temp)){
                    jobstatus= (String) ((LinkedTreeMap)queryMap.get("stats")).get("state");
                    if ("FINISHED".equals(jobstatus)) {
                        postRunning=false;
                    }
                    if (queryMap.containsKey("error") || "FAILED".equals(jobstatus)) {
                        delete(prestoid, jobType);
                        postRunning=false;
                    }
                    if (queryMap.containsKey("nextUri")){
                        page.getAndIncrement();
                    }
                    if (queryMap.containsKey("data"))
                        tempdata.addAll((ArrayList)queryMap.get("data"));
                }else
                    return "";
                Thread.sleep(PrestoContent.POST_STATUS_WAIT_TIME);
            }while (postRunning);
            System.out.println(tempdata.size());
            queryMap.put("data",tempdata);
            result=gson.toJson(queryMap);
        } catch (InterruptedException e) {
            /*do nothing*/
        }
        return result;
    }

    public  String postStatement(String SQL,Integer jobType,String... schema) {

        return doHttpRequest(SQL, Init.getPrestoURL()+"/v1/statement","POST",jobType,schema);
    }

    public  String delete(String prestoid,Integer jobType,String... schema) {
        return doHttpRequest(null, Init.getPrestoURL()+"/v1/query/"+prestoid,"DELETE",jobType,schema);
    }

    private  String doHttpRequest(String postSQL , String url , String method  ,Integer jobType,String... schema)   {


        URL prestoURL = null;
        HttpURLConnection conn = null;
        try {
            prestoURL = new URL(url);
        } catch (MalformedURLException e) {
            setException("URL Parse Exception " + ExceptionUtils.getStackTrace(e));
            return "";
        }
        int retry=0;
        boolean connecting=false;
        do{
            try {
                conn = (HttpURLConnection) prestoURL.openConnection();
                connecting=true;
            } catch (IOException e) {
                if (retry>RETRY_COUNT){
                    setException("Connection Presto Error , "+ExceptionUtils.getStackTrace(e));
                    return "";
                }
                retry++;
                try {
                    Thread.sleep(RECONNECT_TIME*retry);
                } catch (InterruptedException e1) {
                        /*do nothing*/
                }
            }
        }while (!connecting);
        try {
            conn.setRequestMethod(method);
        } catch (ProtocolException e) {
            setException("URL Parse Exception " + ExceptionUtils.getStackTrace(e));

        }
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Length", String.valueOf(postSQL));
        conn.setRequestProperty("x-presto-user", Init.getPresto_user());
        conn.setRequestProperty("x-presto-catalog", Init.getPrestoCatalog());

        if (PrestoContent.QUERY_UI.equals(jobType))
            conn.setRequestProperty("x-presto-source", "Query UI");
        else if (PrestoContent.USER_JOB.equals(jobType))
            conn.setRequestProperty("x-presto-source", "User Job");
        else
            conn.setRequestProperty("x-presto-source", "Schedule");


        if ("POST".equals(method)){
            DataOutputStream dos = null;
            try {
                dos = new DataOutputStream(conn.getOutputStream());
                dos.write(postSQL.getBytes(Charset.forName("utf-8")));
                dos.flush();
            } catch (IOException e) {
                setException("Parse Post Data Error : " +e.getMessage());
                return "";
            } finally {
                if (dos != null)
                    try {
                        dos.close();
                    } catch (IOException e) {
                        setException("Close Post Data OutputStream Error : "+ ExceptionUtils.getStackTrace(e));
                    }
            }
        }


        InputStreamReader isr=null;
        BufferedReader br=null;
        String line=null;
        StringBuilder sb=new StringBuilder();

        //todo http request timeout , [ short ]  retry 3 , sleep 500 *n (wait time)  , catch error and throw exception
        //
        try {
            isr=new InputStreamReader(conn.getInputStream());
            br=new BufferedReader(isr);
            while ((line=br.readLine())!=null){
                sb.append(line);
            }
            setSuccess(true);
            //todo  catch
        } catch (IOException e) {
            setException("Get Response Data InputStream Error : "+e.getMessage());
            return "";
        } finally {
            if (br!=null)
                try {
                    br.close();
                } catch (IOException e) {
                    setException("Close Response Data [ BufferReader ]  Error : "+e.getMessage());
                    sb=new StringBuilder();
                }
            if (isr!=null)
                try {
                    isr.close();
                } catch (IOException e) {
                    setException("Close Response Data [ InputStreamReader ] Error : "+e.getMessage());
                    sb=new StringBuilder();
                }
        }

        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
//        String sql="select * from mars.truesight_page_orc_v4 limit 13";
//        String sql="drop table mars.orders";
        Init.setPrestoCatalog("hive");
//        Init.setPrestoURL("http://10.16.205.110:8889");
        Init.setPrestoURL("http://172.16.157.11:8080");
        Init.setPresto_user("root");
        Init.setCsvlocalPath("D:\\0_projects\\Kado\\logs");
        PrestoUtil util=new PrestoUtil();
//        util.doJdbcRequest("SELECT * FROM information_schema.tables where table_schema<> 'presto_temp'");

//       util.witerAsJson("presto_temp.temp_586721bafdfd41548f187898fe4f7e72");
        util.readJsonAsResult("presto_temp.temp_dadb1fee570e43b3bcd239aa54b19953 ",0,100);
//        String csvfile=util.readAsCSV("presto_temp.temp_be3d22c827b240c08ff4b129bfa7d74d","D:\\0_projects\\Kado\\logs\\","test");
//        ByteArrayInputStream bais=util.readAsStream("presto_temp.temp_586721bafdfd41548f187898fe4f7e72",0,100);
//
//        InputStreamReader inReader = new InputStreamReader(bais);
//        BufferedReader br = new BufferedReader(inReader);
//        int i = 0;
//        while (br.ready()){
//            String tmp = br.readLine();
//            String[] rowArray = tmp.split("\001");
//            System.out.println("*****");
//            for (String col : rowArray) {
//                System.out.print(col+"=");
//            }
//
//        }
//        int pause=0;
//        util.doJdbcRequest(sql,1);
    }

//    private  String readAsCSV(String table,String outPutPath,String fileName ){
//        String resultFinalPath="";
//        Connection  conn=null;
//        Statement state=null;
//        String sql="select * from "+table;
//        try {
//            Class.forName("com.facebook.presto.jdbc.PrestoDriver");
//            conn = DriverManager.getConnection(jdbcUrl, prop);
//            state = conn.createStatement();
//            ResultSet resultSet = state.executeQuery(sql);
//            int batchsize=5;
//            int datasize=0;
//            int count = 0;
//            boolean flag = true;
//            resultFinalPath=outPutPath +File.separator+ fileName+"@"+ Instant.now().toEpochMilli()+".csv";
//            if (new File(resultFinalPath).exists())
//                new File(resultFinalPath).delete();
//
//            FileWriter fw=new FileWriter(resultFinalPath);
//
//            while (resultSet.next()) {
//                StringBuilder sb=new StringBuilder();
//                if (0 == count) {
//                    count = resultSet.getMetaData().getColumnCount();
//                }
//                if (flag) {
//                    //  table Schema
//                    for (int i = 1; i <= count; i++) {
//                        sb.append(resultSet.getMetaData().getColumnName(i));
//                        if (i!=count)
//                            sb.append("\001");
//                    }
//                    fw.write("\"" + sb.toString().replaceAll("\'", "\\\\'").replaceAll("\"", "\'").replaceAll("\t|\001", "\",\"") + "\"");
//                    fw.write("\n");
//                    fw.flush();
//                    flag = false;
//                }else{
//                    // values
//                    for (int i = 1; i <= count; i++) {
//                        sb.append(resultSet.getString(i));
//                        if (i!=count)
//                            sb.append("\001");
//                    }
//                    fw.write("\"" + sb.toString().replaceAll("\'", "\\\\'").replaceAll("\"", "\'").replaceAll("\t|\001", "\",\"") + "\"");
//                    fw.write("\n");
//                }
//                datasize++;
//                if (datasize%batchsize==0)
//                    fw.flush();
//            }
//            resultSet.close();
//            state.close();
//            conn.close();
//
//            fw.flush();
//            fw.close();
//
//        } catch(ClassNotFoundException | SQLException | IOException e){
//            e.printStackTrace();
//        }
//
//        return resultFinalPath;
//    }
    /**
     * @param tableName  result table Name,  ex: " presto_temp.temp_586721bafdfd41548f187898fe4f7e72 "
     * @param start            start index
     * @param rowCount    how many rows will be taken
     * @return
     */
    public ResultMap readJsonAsResult(String tableName, int start, int rowCount){
        ResultMap resultMap=new ResultMap();
        resultMap.setCount(rowCount);
        resultMap.setStart(start);
        // check file
        String fileName=tableName+".json";
        String filePath=Init.getCsvlocalPath()+File.separator+fileName;
        File jsonFile=new File(filePath);


        try {
            // no exist file , create as csv
            if (!jsonFile.exists()) {
                witerAsJson(tableName);
            }

            FileReader fr=new FileReader(jsonFile);

            // starting read result json file
            JsonParser parser=new JsonParser();


            JsonObject jo= (JsonObject) parser.parse(fr);

            // add Column Name
            jo.getAsJsonArray("columns").forEach((column)->{
                resultMap.getSchema().add(column.getAsString());
            });
            // add Column Type
            jo.getAsJsonArray("types").forEach((column)->{
                resultMap.getType().add(column.getAsString());
            });

            int count=1;
            JsonArray ja= jo.getAsJsonArray("data");

            for (int i=start ;i<ja.size();i++){
                List<Object> rowdata=new ArrayList<>();
                for (int  index=0; index<ja.get(i).getAsJsonArray().size() ; index++){
                    JsonPrimitive jp= ja.get(i).getAsJsonArray().get(index).getAsJsonPrimitive();
                    // add rowData
                    if (null==jp)
                        rowdata.add("");
                    else if (jp.isString())
                        rowdata.add(jp.getAsString());
                    else if (jp.isNumber())
                        rowdata.add(jp.getAsLong());
                    else
                        rowdata.add(jp.getAsBoolean());
                }
                resultMap.getData().add(rowdata);
                if (count<rowCount)
                    count++;
                else
                    break;
            }
        } catch (FileNotFoundException e) {
            this.setException(ExceptionUtils.getStackTrace(e));
            this.setSuccess(false);
        }
        return resultMap;
    }

    /**
     * @param table  result table
     * @return
     */
    private  boolean witerAsJson(String table){

        // initial SQL
        Connection  conn=null;
        Statement state=null;
        String sql="select * from "+table;

        // check file
        String fileName=table+".json";
        String filePath=Init.getCsvlocalPath()+File.separator+fileName;
        File jsonfile=new File(filePath);
        if (jsonfile.exists())
            jsonfile.delete();

        // Json
        JsonArray ja=null;
        try {
            Class.forName("com.facebook.presto.jdbc.PrestoDriver");
            conn = DriverManager.getConnection(jdbcUrl, prop);
            state = conn.createStatement();
            ResultSetMetaData rsmd = null;

            ResultSet resultSet = state.executeQuery(sql);
            int count = 0;
            boolean first = true;
            rsmd=resultSet.getMetaData();
            Object cValue=null;

            FileWriter fw=new FileWriter(jsonfile);

            while (resultSet.next()) {
                ja=new JsonArray();
                if (first){
                    JsonArray type=new JsonArray();
                    //
                    //add columns and types
                    for (int i = 0; i < rsmd.getColumnCount(); i++) {
                        ja.add(rsmd.getColumnName(i + 1));
                        cValue=resultSet.getObject(i+1);
                        if (null== cValue || cValue instanceof String || cValue instanceof Timestamp)
                            type.add("string");
                        else if (cValue instanceof Boolean)
                            type.add("boolean");
                        else
                            type.add("double");
                    }
                    fw.write("{\"columns\":"+ja.toString()+",");
                    fw.write("\"types\":"+type.toString()+",");
                    fw.write("\"data\":[");
                    fw.flush();
                    first=false;
                    ja=new JsonArray();
                }else
                    fw.write(",");
                //
                //add row data
                for (int i = 0; i < rsmd.getColumnCount(); i++) {

                    cValue=resultSet.getObject(i+1);
                    if (null==cValue)
                        ja.add("");
                    else if (cValue instanceof String || cValue instanceof Timestamp)
                        ja.add(cValue.toString());
                    else if (cValue instanceof Boolean)
                        ja.add(Boolean.valueOf(cValue.toString()));
                    else
                        ja.add(Double.valueOf(cValue.toString()));
                }
                fw.write(ja.toString());
                count++;
                if (count%batchSize==0)
                    fw.flush();
            }
            fw.write("]}");
            fw.flush();
            fw.close();

            resultSet.close();
            state.close();
            conn.close();

        }catch (ClassNotFoundException | SQLException | IOException e) {
            this.setException(ExceptionUtils.getStackTrace(e));
            this.setSuccess(false);
            return false;
        }
        return true;
    }

//    private ByteArrayInputStream readAsStream(String table,int start,int batchsize){
//        ByteArrayOutputStream baos=new ByteArrayOutputStream();
//
//        Connection  conn=null;
//        Statement state=null;
//        String sql="select * from "+table;
//        try {
//            Class.forName("com.facebook.presto.jdbc.PrestoDriver");
//            conn = DriverManager.getConnection(jdbcUrl, prop);
//            state = conn.createStatement();
//
//            ResultSet resultSet = state.executeQuery(sql);
//
////            resultSet.absolute(start);
////            resultSet.setFetchSize(batchsize);
//            int data=0;
//            int count = 0;
//            boolean flag = true;
//
//            while (resultSet.next()) {
//                StringBuilder sb=new StringBuilder();
//                if (0 == count) {
//                    count = resultSet.getMetaData().getColumnCount();
//                }
//                if (flag) {
//                    // first Page
//                    sb.append("#").append("\001");
//                    for (int i = 1; i <= count; i++) {
//                        sb.append(resultSet.getMetaData().getColumnName(i));
//                        if (i!=count)
//                            sb.append("\001");
//                    }
//                    baos.write((sb.toString()).getBytes());
//                    flag = false;
//                }else{
//                    data++;
//                    if (data<start)
//                        continue;
//                    // values
//                    for (int i = 1; i <= count; i++) {
//                        sb.append(resultSet.getString(i));
//                        if (i!=count)
//                            sb.append("\001");
//                    }
//
//                    baos.write((sb.toString()).getBytes());
//                }
//            }
//            resultSet.close();
//            state.close();
//            conn.close();
//
//        } catch(ClassNotFoundException | SQLException | IOException e){
//            e.printStackTrace();
//        }
//
//        if(baos.size()==0)
//            return new ByteArrayInputStream(new byte[]{});
//        else
//            return  new ByteArrayInputStream(baos.toByteArray());
//    }

    public ResultMap doJdbcRequest(String jdbcSQL){
        ResultMap resultMap=new ResultMap();

        resultMap.setStart(0);

        String result="";
        Connection  conn=null;
        Statement state=null;
        try {
            Class.forName("com.facebook.presto.jdbc.PrestoDriver");
            conn = DriverManager.getConnection(jdbcUrl, prop);
//            List<String> columns = new ArrayList<>();
            ResultSetMetaData rsmd = null;
            state = conn.createStatement();
//            state.setFetchSize();
            if (jdbcSQL.toLowerCase().contains("drop table"))
                state.execute(jdbcSQL);
            else {
                ResultSet resultSet = state.executeQuery(jdbcSQL);
                rsmd=resultSet.getMetaData();

                int count=rsmd.getColumnCount();
                boolean flag = true;
                List<Object> rowData=null;
                while (resultSet.next()) {

                    if (flag) {
                        for (int i = 0; i < count; i++) {
                            resultMap.getSchema().add(rsmd.getColumnName(i+1));
                            Object cValue=resultSet.getObject(i+1);
                            if (null==cValue || cValue instanceof String || cValue instanceof Timestamp)
                                resultMap.getType().add("string");
                            else if (cValue instanceof Boolean)
                                resultMap.getType().add("boolean");
                            else
                                resultMap.getType().add("double");
                        }
                        flag = false;

                    }
                   rowData=new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        //todo needs column schema ,

                        if ("string".equalsIgnoreCase(resultMap.getType().get(i)))
                            rowData.add(resultSet.getString(i+1));
                        else if ("double".equalsIgnoreCase(resultMap.getType().get(i)))
                            rowData.add(resultSet.getDouble(i+1));
                       else
                           rowData.add(resultSet.getBoolean(i+1));
                    }
                    resultMap.getData().add(rowData);
//                    System.out.println("=====================");
                }
                resultSet.close();
                state.close();
                conn.close();
                resultMap.setCount(resultMap.getData().size());
            }
        } catch(ClassNotFoundException | SQLException e){
            this.setException(ExceptionUtils.getStackTrace(e));
            this.setSuccess(false);
        }
        return resultMap;
    }

}
