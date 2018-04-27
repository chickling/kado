package com.chickling.util;

import com.chickling.boot.Init;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by ey67 on 2018/2/26.
 */
public class WebHDFSUtil {
    public static void putFileToHDFSPG(String url,File file,CountingRequestBody.Listener listener) throws Exception {
        /**Use Proxy**/
        Proxy proxy=new Proxy(Proxy.Type.HTTP,new InetSocketAddress("172.22.29.20", 4326));
        //OkHttpClient client = new OkHttpClient.Builder().proxy(proxy).build();

        OkHttpClient client = new OkHttpClient.Builder().build();
        MediaType csvType = MediaType.parse("text/csv; charset=utf-8");
        //RequestBody body = RequestBody.create(csvType, file);
        RequestBody body =new CountingRequestBody(RequestBody.create(csvType, file),listener);
//        RequestBody body =new CountingRequestBody(RequestBody.create(csvType, file), new CountingRequestBody.Listener() {
//            @Override
//            public void onRequestProgress(long bytesWritten, long contentLength) {
//                float progress = (bytesWritten / (float) contentLength) * 100;
//                System.out.println(progress);
//            }
//        });
//        RequestBody body=new CountingFileRequestBody(file, csvType.toString(), new CountingFileRequestBody.ProgressListener() {
//            @Override
//            public void transferred(long num) {
//                float progress = (num / (float) file.length()) * 100;
//                System.out.println(progress);
//
//            }
//        });
        Request request = new Request.Builder().url(url+"&user.name="+Init.getHdfsUser()).put(body).build();
        Response response = client.newCall(request).execute();
        if(response.code()>=300)
            throw new Exception("Upload file fail:"+response.code()+"==>\n"+response.body().string());
    }
    public static void putFileToHDFS(String url,File file) throws Exception {
        /**Use Proxy**/
        //Proxy proxy=new Proxy(Proxy.Type.HTTP,new InetSocketAddress("172.22.29.20", 4326));
        //OkHttpClient client = new OkHttpClient.Builder().proxy(proxy).build();

        OkHttpClient client = new OkHttpClient.Builder().build();
        MediaType csvType = MediaType.parse("text/csv; charset=utf-8");
        RequestBody body = RequestBody.create(csvType, file);
        Request request = new Request.Builder().url(url+"&user.name="+Init.getHdfsUser()).put(body).build();
        Response response = client.newCall(request).execute();
        if(response.code()>=300)
            throw new Exception("Upload file fail:"+response.code()+"==>\n"+response.body().string());
    }
    public static String getRealPathURL(String path) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("upload", "hdfs")
                .build();
        Request request = new Request.Builder().url(Init.getHdfsHost()+"/webhdfs/v1"+path+"?op=CREATE").put(formBody).build();
        Response response = client.newCall(request).execute();
        return response.header("Location");
    }
    public static boolean mkdirHDFS(String path) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("upload", "hdfs")
                .build();
        Request request = new Request.Builder().url(Init.getHdfsHost()+"/webhdfs/v1"+path+"?op=MKDIRS&user.name="+Init.getHdfsUser()).put(formBody).build();
        Response response = client.newCall(request).execute();
        return response.code()==200;
    }
    public static void uploadTableFile(String tableName,String partition,File file,CountingRequestBody.Listener listener) throws Exception {
        if(mkdirHDFS(Init.getExternalTableHDFSRootPath()+"/"+tableName)){
            String url=WebHDFSUtil.getRealPathURL(Init.getExternalTableHDFSRootPath()+"/"+tableName+(partition.isEmpty()?"":("/"+partition))+"/"+file.getName().replace("#","_"));
            /*For DEV*/
            //putFileToHDFS(parseHostURL(url),file);

            /*For PRD*/
            putFileToHDFSPG(url,file,listener);
        }else {
            throw new Exception("Make Table Dir Fail!");
        }

    }
    public static String parseHostURL(String url){
        String tmp=url.substring(url.indexOf(":",url.indexOf(":")+1),url.length());
        String host=url.substring(url.indexOf("://")+3,url.indexOf("."));
        System.out.println(tmp);
        System.out.println(host);
        String number="";
        Pattern p = Pattern.compile("[0-9]+$");
        Matcher m = p.matcher(host);
        if(m.find()) {
            number = m.group();
        }
        if(!number.isEmpty()){
            int offset=Integer.parseInt(number);
            String ip="172.16.156."+(10+offset);

            return "http://"+ip+tmp;
        }
        return number+tmp;
    }
    public static void main(String[] args) throws Exception {
        //uploadTableFile("test.test1","region_name=Canillo",new File("upload/2018_02_27_11_14_55#IP2LOCATION-COUNTRY-REGION.CSV"));

    }

}
