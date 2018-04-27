package com.chickling.models.tabletool;

import com.chickling.boot.Init;
import com.chickling.models.HiveJDBC;
import com.google.gson.Gson;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by ey67 on 2018/2/2.
 */
public class TableCreate {
    /*Log4J*/
    Logger log = LogManager.getLogger(TableCreate.class);

    public static Map<String,Status> insertDataStatus=new HashMap<>();
    public String getCSVInfo(String fileName,String readFileName) throws Exception {
        Map csvInfo=new HashMap();
        csvInfo.put("DEFAULT",getCSVHead(fileName,"DEFAULT"));
        csvInfo.put("EXCEL",getCSVHead(fileName,"EXCEL"));
        csvInfo.put("MYSQL",getCSVHead(fileName,"MYSQL"));
        csvInfo.put("POSTGRESQL_CSV",getCSVHead(fileName,"POSTGRESQL_CSV"));
        csvInfo.put("POSTGRESQL_TEXT",getCSVHead(fileName,"POSTGRESQL_TEXT"));
        csvInfo.put("RFC4180",getCSVHead(fileName,"RFC4180"));
        csvInfo.put("file_name",readFileName);
        return new Gson().toJson(csvInfo);
    }
    public List<List<String>> getCSVHead(String file,String csvFormate) {

        List<List<String>> headData=new ArrayList<>();
        try {
            Reader reader = Files.newBufferedReader(Paths.get(file));
            Iterable<CSVRecord> parser = new CSVParser(reader, getCSVFormat(csvFormate));
            Iterator<CSVRecord> iterator = parser.iterator();
            int count = 0;
            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                List<String> tmp = new ArrayList<>();
                for (int i = 0; i < record.size(); i++)
                    tmp.add(record.get(i));
                headData.add(tmp);
                //System.out.println(record);
                if (count++ >= 2)
                    break;
            }
        }catch (Exception e){
            log.info("Try Parser CSV File Fail:"+e.getMessage());
        }
        return headData;
    }
    public static CSVFormat getCSVFormat(String formatName) throws Exception {
        switch (formatName){
            case "DEFAULT":
                return CSVFormat.DEFAULT;
            case "EXCEL":
                return CSVFormat.EXCEL;
            case "MYSQL":
                return CSVFormat.MYSQL;
            case "POSTGRESQL_CSV":
                return CSVFormat.POSTGRESQL_CSV;
            case "POSTGRESQL_TEXT":
                return CSVFormat.POSTGRESQL_TEXT;
            case "RFC4180":
                return CSVFormat.RFC4180;
            default:
                throw new Exception("illegal CSVFormat String");
        }
    }
    public List<String> getDBList() throws Exception {
        List<String> dbList=new ArrayList<>();
        try {
            ResultSet rs= HiveJDBC.getInstance().getConnection().createStatement().executeQuery("show databases");
            while (rs.next()){
                dbList.add(rs.getString(1));
            }
        } catch (SQLException e) {
            log.error(ExceptionUtils.getMessage(e));
            throw new Exception("Get DataBase Fail!");
        }
        return dbList;
    }
    public String createInternalTableSQL(Map tableSetting){
        String partition="";
        String createSQL="CREATE TABLE "+tableSetting.get("db_name")+"."+tableSetting.get("table_name")+"(";
        Map<String,Map<String,Object>> schemas=(Map<String,Map<String,Object>>) tableSetting.get("schemas");
        for(Integer i=0;i<schemas.size();i++){
            Map<String,Object> column=schemas.get(i.toString());
            if(column.get("is_partition").toString().equals("false"))
                createSQL+="\n`"+column.get("column_name").toString()+"` "+column.get("column_type").toString()+",";
            else
                partition+="\n`"+column.get("column_name").toString()+"` "+column.get("column_type").toString()+",";
        }
        if(createSQL.endsWith(","))
            createSQL=createSQL.substring(0,createSQL.length()-1);
        if(partition.endsWith(","))
            partition=partition.substring(0,partition.length()-1);
        createSQL+= "\n)";
        if(!partition.equals("")) {
            createSQL += "\nPARTITIONED BY (";
            createSQL += partition;
            createSQL += "\n)";
        }
        return createSQL+"\n STORED AS ORC";
    }
    public String insertCSVDataToTable(Map tableSetting,String file,String fileName) throws Exception {
        /**Init Status and Update*/
        Status jobStatus=new Status();
        jobStatus.setStatus(Status.WAIT);
        jobStatus.setFileName(fileName);
        jobStatus.setJobID(fileName);
        jobStatus.setProcessCount(0);
        jobStatus.setSchemaMap((Map)tableSetting.get("schemas"));
        TableCreate.setInsertDataStatus(fileName,jobStatus);

        Thread job=new Thread(new InsertJob(file,tableSetting,fileName));
        job.start();
        return file;
    }

    public String createExternalTableSQL(Map tableSetting){
        String partition="";
        String createSQL="CREATE EXTERNAL TABLE "+tableSetting.get("db_name")+"."+tableSetting.get("table_name")+"(";
        Map<String,Map<String,Object>> schemas=(Map<String,Map<String,Object>>) tableSetting.get("schemas");
        for(Integer i=0;i<schemas.size();i++){
            Map<String,Object> column=schemas.get(i.toString());
            if(column.get("is_partition").toString().equals("false"))
                createSQL+="\n`"+column.get("column_name").toString()+"` "+column.get("column_type").toString()+",";
            else
                partition+="\n`"+column.get("column_name").toString()+"` "+column.get("column_type").toString()+",";
        }
        if(createSQL.endsWith(","))
            createSQL=createSQL.substring(0,createSQL.length()-1);
        if(partition.endsWith(","))
            partition=partition.substring(0,partition.length()-1);
        createSQL+= "\n)";
        if(!partition.equals("")) {
            createSQL += "\nPARTITIONED BY (";
            createSQL += partition;
            createSQL += "\n)";
        }
        return createSQL+
                "ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'\n"+
                "STORED AS TEXTFILE\n" +
                "LOCATION" +" '"+getLocation(tableSetting)+"'";
    }
    public static void uploadCSVToHDFS(Map tableSetting,String file,String fileName) throws Exception{
        Status jobStatus=new Status();
        jobStatus.setStatus(Status.WAIT);
        jobStatus.setFileName(fileName);
        jobStatus.setJobID(fileName);
        jobStatus.setProcessCount(0);
        jobStatus.setSchemaMap((Map)tableSetting.get("schemas"));
        TableCreate.setInsertDataStatus(fileName,jobStatus);

        Thread job=new Thread(new UploadJob(file,tableSetting,fileName));
        job.start();
    }

    public static String getLocation(Map tableSetting){
        return Init.getExternalTableHDFSRootPath()+"/"+tableSetting.get("db_name").toString()+"."+tableSetting.get("table_name");
    }
    public static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }

    public synchronized static Map<String, Status> getInsertDataStatus() {
        return insertDataStatus;
    }

    public synchronized static void setInsertDataStatus(Map<String, Status> insertDataStatus) {
        TableCreate.insertDataStatus = insertDataStatus;
    }
    public synchronized static Status getInsertDataStatus(String key){
        return insertDataStatus.get(key);
    }
    public synchronized static void setInsertDataStatus(String key,Status status) {
        TableCreate.insertDataStatus.put(key,status);
    }

    public static void main(String[] args) throws Exception {
        //new TableCreate().getCSVInfo("upload/2018_02_01_16_55_54#output.csv");
    }
}
