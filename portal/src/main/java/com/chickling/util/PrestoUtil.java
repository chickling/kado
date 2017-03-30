package com.chickling.util;

import com.google.common.base.Strings;
import com.chickling.boot.Init;
import com.chickling.models.job.PrestoContent;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gl08 on 2015/12/3.
 */
public class PrestoUtil {

    private int RETRY_COUNT=3;
    private long RECONNECT_TIME=500;
    private boolean success=false;
    private Gson gson=new Gson();
    private StringBuilder exception;

    public PrestoUtil() {
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

}
