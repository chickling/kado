package com.chickling.models.tabletool;

import com.chickling.models.PrestoJDBC;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by ey67 on 2018/2/5.
 */
public class InsertJob implements Runnable{
    /*Log4J*/
    Logger log = LogManager.getLogger(InsertJob.class);
    private String file;
    private String fileName;
    private Map tableSetting;
    private CSVFormat PARSER;
    public InsertJob(String file,Map tableSetting,String fileName) throws Exception {
        this.file=file;
        this.tableSetting=tableSetting;
        this.fileName=fileName;
        this.PARSER=TableCreate.getCSVFormat(tableSetting.get("csv_parser").toString());
    }
    @Override
    public void run() {
        try {
            /**Count File Row Size*/
            Integer fileLine=TableCreate.countLines(file);

            /**Update JobInfo**/
            Status jobStatus=TableCreate.getInsertDataStatus().get(fileName);
            jobStatus.setStatus(Status.RUNING);
            jobStatus.setRowCount(fileLine);
            TableCreate.setInsertDataStatus(file,jobStatus);

            Reader reader = Files.newBufferedReader(Paths.get(file));
            Iterable<CSVRecord> parser = new CSVParser(reader, PARSER);
            Iterator<CSVRecord> iterator = parser.iterator();
            Map schemas=(Map)tableSetting.get("schemas");


            int count = 0;

            /*Init Presto JDBC */
            Connection conn= PrestoJDBC.getInstance().getConnection();
            Statement statement =conn.createStatement();
            boolean hasPartition=hasPartition(schemas);
            String sql=buildInsertSQL(tableSetting.get("db_name").toString()+"."+tableSetting.get("table_name").toString());
            /*Read  CSV File*/
            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                /*check skip first line*/
                if(count++==0&&tableSetting.get("skip_firstline").toString().equals("true"))
                    continue;

                /*Add row to batch  sql*/

                sql+=(hasPartition?buildInsertValuePartation(schemas,record):buildInsertValue(schemas,record))+",";

                if(count%5000==0){
                    sql=sql.endsWith(",")?sql.substring(0,sql.length()-1):sql;
                    executeSQL(statement,sql);
                    /**Update JobInfo**/
                    jobStatus=TableCreate.getInsertDataStatus().get(fileName);
                    jobStatus.setProcessCount(count);
                    jobStatus.setProgress((count/fileLine)*100);
                    TableCreate.setInsertDataStatus(fileName,jobStatus);
                    log.info("Batch Insert Data :"+count+"/"+fileLine+"==>"+(count/fileLine)*100+"%");
                    sql=buildInsertSQL(tableSetting.get("db_name").toString()+"."+tableSetting.get("table_name").toString());
                }
            }
            sql=sql.endsWith(",")?sql.substring(0,sql.length()-1):sql;
            executeSQL(statement,sql);

            /**Update JobInfo**/
            jobStatus=TableCreate.getInsertDataStatus().get(fileName);
            jobStatus.setStatus(Status.SUCCESS);
            jobStatus.setProcessCount(count);
            jobStatus.setProgress(100);
            TableCreate.setInsertDataStatus(fileName,jobStatus);

            log.info("Insert Data Done! ==>"+count+" row");
        }catch (Exception e){
            log.error(e.getMessage());
            /**Update JobInfo**/
            Status jobStatus=TableCreate.getInsertDataStatus().get(fileName);
            jobStatus.setStatus(Status.FAIL);
            jobStatus.setMessage(ExceptionUtils.getMessage(e));
            TableCreate.setInsertDataStatus(fileName,jobStatus);
        }
    }
    private void executeSQL(Statement statement,String sql) throws SQLException {
        int retryNum=3;
        for(int i=0;i<=retryNum;i++){
            try{
                statement.setQueryTimeout(6000);
                statement.execute(sql);
                return;
            }catch (Exception e){
                log.warn("Execute SQL Fail! Error Msg:{}", ExceptionUtils.getMessage(e));
                log.warn("Retry Exectue SQL {}/{}", i+1,retryNum);
                if(i==retryNum){
                    log.error("Execute SQL Retry Fail!:"+ExceptionUtils.getMessage(e));
                    throw e;
                }
            }
        }

    }
    private String buildInsertSQL(String tableName){
        return "INSERT INTO "+tableName+" VALUES ";
    }
    private String buildInsertSQLPartation(String tableName,Map schemas){
        String partition="";
        for (int i=0;i<schemas.size();i++){
            if(((Map)schemas.get(String.valueOf(i))).get("is_partition").toString().equals("true"))
                partition+=((Map)schemas.get(String.valueOf(i))).get("column_name")+",";
        }
        partition=partition.endsWith(",")?partition.substring(0,partition.length()-1):partition;
        return "INSERT INTO "+tableName+" PARTITION ("+partition+") VALUES ";
    }
    private String buildInsertValue(Map schemas,CSVRecord record){

        String values="";
        for (int i=0;i<schemas.size();i++){
            values+=getValueString(((Map)schemas.get(String.valueOf(i))).get("column_type").toString(),record.get(i))+",";
        }
        values=values.endsWith(",")?values.substring(0,values.length()-1):values;
        return "("+values+")";
    }
    private String buildInsertValuePartation(Map schemas,CSVRecord record){

        String values="";
        /*For Value*/
        for (int i=0;i<schemas.size();i++){
            if(((Map)schemas.get(String.valueOf(i))).get("is_partition").toString().equals("false"))
                values+=getValueString(((Map)schemas.get(String.valueOf(i))).get("column_type").toString(),record.get(i))+",";
        }
        /*For Partition*/
        for (int i=0;i<schemas.size();i++){
            if(((Map)schemas.get(String.valueOf(i))).get("is_partition").toString().equals("true"))
                values+=getValueString(((Map)schemas.get(String.valueOf(i))).get("column_type").toString(),record.get(i))+",";
        }
        // PARTITION (datestamp)
        values=values.endsWith(",")?values.substring(0,values.length()-1):values;
        return "("+values+")";
    }
    private boolean hasPartition(Map schemas){
        for(Object key:schemas.keySet()){
            if(((Map)schemas.get(key)).get("is_partition").toString().equals("true"))
                return true;
        }
        return false;
    }
    private PreparedStatement setPSData(PreparedStatement ps,Map schemas,CSVRecord record,int index) throws SQLException {
        int offset=index*schemas.size();
        for(int i=0;i<schemas.size();i++){
            Map<String,String> schema=(Map<String,String>)schemas.get(String.valueOf(i));
            switch (schema.get("column_type")){

                case "int":
                    try {
                        ps.setInt(offset + i + 1, Integer.valueOf(record.get(i)));
                    }catch (Exception e){
                        ps.setInt(offset +i + 1,0);
                    }
                    break;
                case "bigint":
                    try {
                        ps.setLong(offset +i + 1, Long.valueOf(record.get(i)));
                    }catch (Exception e){
                        ps.setLong(offset +i + 1,0L);
                    }
                    break;
                case "double":
                    try {
                        ps.setDouble(offset +i + 1, Double.valueOf(record.get(i)));
                    }catch (Exception e){
                        ps.setDouble(offset +i + 1,0.0);
                    }
                    break;
                case "boolean":
                    try {
                        ps.setBoolean(offset +i + 1, Boolean.valueOf(record.get(i)));
                    }catch (Exception e){
                        ps.setBoolean(offset +i + 1,false);
                    }
                    break;
                case "string":
                default:
                    ps.setString(offset +i+1,record.get(i));
                    break;


            }
        }
        return ps;
    }
    private String getValueString(String type,String value){
        switch (type){
            case "bigint":
            case "int":
            case "double":
            case "boolean":
                return value.replace("'","''");
            case "string":
            default:
                return "'"+value.replace("'","''")+"'";
        }
    }
}
