package com.chickling.sql;


import com.chickling.dbselect.DBConnectionManager;
import com.chickling.models.dfs.OrcFileUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by gl08 on 2016/1/6.
 */
public class ImportDB {

    private Logger log= LogManager.getLogger(ImportDB.class);
    private int RETRY_COUNT=3;
    private int RETRT_SLEEP=500;
    private String importSQL="";
    private int importCount=0;
    private int batchSize=SqlContent.MAX_BATCH_ROWS;
    private String prestoDirpath ="";
    private StringBuilder exception=null;
    private boolean success=false;
    private String connName="";

    public String getConnName() {
        return connName;
    }

    public void setConnName(String connName) {
        this.connName = connName;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }


//    private  void init(){
//        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
//        InputStream input = classloader.getResourceAsStream("dbselect-config.yaml");
//        Yaml yaml = new Yaml();
//        ManagerConfig contactMap = yaml.loadAs(input,ManagerConfig.class);
//            for (ManagerConfig.DataSource val : contactMap.getDataSourceList()) {
//                System.out.println(val.getName());
//                locationList.add(val.getName());
//            }
//
//
//    }

    /**
     * @param importSQL         import SQL String
     * @param prestoDirpath               where is the orc file Path  on HDFS  ,
     * @param importCount      total rows count with this result
     * @param batchSize            SQL batch execute size
     */
    public ImportDB(String importSQL , String prestoDirpath , String connName,int importCount , int batchSize)  {
        this.importSQL = importSQL;
        this.importCount=importCount;
        this.prestoDirpath =prestoDirpath;
        this.connName=connName;
        this.batchSize=batchSize;
        this.exception=new StringBuilder();
    }

    public String getException() {
        return exception.toString();
    }

    public void setException(String exception) {
        this.exception.append(exception).append("\\n");
    }

    public String getPrestoDirpath() {
        return prestoDirpath;
    }

    public void setPrestoDirpath(String prestoDirpath) {
        this.prestoDirpath = prestoDirpath;
    }

    public String getImportSQL() {
        return importSQL;
    }

    public void setImportSQL(String importSQL) {
        this.importSQL = importSQL;
    }

    public int getImportCount() {
        return importCount;
    }

    public void setImportCount(int importCount) {
        this.importCount = importCount;
    }

    public void execute()  {

        //todo need add Batch Size to execute SQL , done !!
        String sql=getImportSQL();
        Matcher matcher=SqlContent.SQL_UNION_PATTERN2.matcher(sql);
        List<String> sqlList=new ArrayList<>();


        int process=0;
        int startRow=0;
        log.info("DB SQL Find Matcher");
        if (matcher.find() ) {
            log.info("Total Row is : "+getImportCount()+" , Batch Size is : "+getBatchSize());
            while (startRow < getImportCount()) {

                String original = matcher.group(0);
                String relpace = original.replaceAll("#\\{", "VALUES(").replaceAll("}", ")");
                List<Integer> fieldIndex = parseFieldsIndex(sql);
                //Create Orc File InputStream
                OrcFileUtil orc = OrcFileUtil.newInstance();
//                ByteArrayInputStream stream = orc.readORCFiles(getPrestoDirpath(), OrcFileUtil.TYPE.HDFS, startRow, getBatchSize());
//                InputStreamReader inReader = new InputStreamReader(stream);
//                BufferedReader br = new BufferedReader(inReader);
                try (
                        ByteArrayInputStream stream = orc.readORCFiles(getPrestoDirpath(), OrcFileUtil.TYPE.HDFS, startRow, getBatchSize());
                        InputStreamReader inReader = new InputStreamReader(stream);
                        BufferedReader br = new BufferedReader(inReader)
                ){
                    String line = br.readLine();
                    //get Column
                    String[] column = line.split("\t|\001");
                    String tmpSql = "";
                    int count=0;
                    while ((line = br.readLine()) != null) {
                        String[] values = line.split("\t|\001");
                        //get original and relplace "#{}" -> "values()"
                        if (values[0].contains("#"))
                            continue;
                        tmpSql = sql.replace(original, relpace);
                        //replace all $1$ to values
                        for (Integer index : fieldIndex) {
                            if (index <= 0 || index > column.length - 1) {
                                tmpSql = tmpSql.replaceAll("\\$" + index + "\\$", "");
                            } else {
                                try {
                                    tmpSql = tmpSql.replaceAll("\\$" + index + "\\$", values[index]);
                                }catch (Exception e2){
                                    e2.fillInStackTrace();
                                }
                            }
                        }
                        //add replace SQL
                        sqlList.add(tmpSql);
                        if(count<10)
                            log.info(tmpSql);
                        count++;
                    }

                    // batch Import Data to DB
//                    log.info("---Batch SQL is : [ "+sqlList +" ] ----");
                    boolean isBatchSuccess=startImport(sqlList);
                    if (isBatchSuccess) {
                        // set Next Batch Start Row
                        if (sqlList.size() < getBatchSize()) {
                            // if rows less than batchSize , is last batch  , set startRow to END
//                            startRow = getImportCount();
                            log.info("Import to DB Process is : " + 100 + " % ");
                            log.info("Import to DB Finished !!");
                            setSuccess(true);
                            break;
                        } else {
                            startRow += getBatchSize();
                        }
                        process = ((startRow * 100) / getImportCount());
                        log.info("Import to DB Process is : " + process + " % ");
                        sqlList.clear();
                    }else{
                        setSuccess(false);
                        break;
                    }
//                    br.close();
//                    inReader.close();
//                    stream.close();
                } catch (IOException e) {
                    log.error(ExceptionUtils.getStackTrace(e));
                    break;
                }
            }
        }else if(sql.toLowerCase().contains("update")){
            log.info("Total Row is : "+getImportCount()+" , Batch Size is : "+getBatchSize());
            while (startRow < getImportCount()) {

                //String original = matcher.group(0);
                //String relpace = original.replaceAll("#\\{", "VALUES(").replaceAll("}", ")");
                List<Integer> fieldIndex = parseFieldsIndex(sql);
                //Create Orc File InputStream
                OrcFileUtil orc = OrcFileUtil.newInstance();
//                ByteArrayInputStream stream = orc.readORCFiles(getPrestoDirpath(), OrcFileUtil.TYPE.HDFS, startRow, getBatchSize());
//                InputStreamReader inReader = new InputStreamReader(stream);
//                BufferedReader br = new BufferedReader(inReader);
                try (
                        ByteArrayInputStream stream = orc.readORCFiles(getPrestoDirpath(), OrcFileUtil.TYPE.HDFS, startRow, getBatchSize());
                        InputStreamReader inReader = new InputStreamReader(stream);
                        BufferedReader br = new BufferedReader(inReader)
                ){
                    String line = br.readLine();
                    //get Column
                    String[] column = line.split("\t|\001");
                    String tmpSql = "";
                    int count=0;
                    while ((line = br.readLine()) != null) {
                        String[] values = line.split("\t|\001");
                        //get original and relplace "#{}" -> "values()"
                        if (values[0].contains("#"))
                            continue;
                        tmpSql = sql;
                        //replace all $1$ to values
                        for (Integer index : fieldIndex) {
                            if (index <= 0 || index > column.length - 1) {
                                tmpSql = tmpSql.replaceAll("\\$" + index + "\\$", "");
                            } else {
                                try {
                                    tmpSql = tmpSql.replaceAll("\\$" + index + "\\$", values[index]);
                                }catch (Exception e2){
                                    e2.fillInStackTrace();
                                }
                            }
                        }
                        //add replace SQL
                        sqlList.add(tmpSql);
                        if(count<10)
                            log.info(tmpSql);
                        count++;
                    }

                    // batch Import Data to DB
//                    log.info("---Batch SQL is : [ "+sqlList +" ] ----");
                    boolean isBatchSuccess=startImport(sqlList);
                    if (isBatchSuccess) {
                        // set Next Batch Start Row
                        if (sqlList.size() < getBatchSize()) {
                            // if rows less than batchSize , is last batch  , set startRow to END
//                            startRow = getImportCount();
                            log.info("Update to DB Process is : " + 100 + " % ");
                            log.info("Update to DB Finished !!");
                            setSuccess(true);
                            break;
                        } else {
                            startRow += getBatchSize();
                        }
                        process = ((startRow * 100) / getImportCount());
                        log.info("Update to DB Process is : " + process + " % ");
                        sqlList.clear();
                    }else{
                        setSuccess(false);
                        break;
                    }
//                    br.close();
//                    inReader.close();
//                    stream.close();
                } catch (IOException e) {
                    log.error(ExceptionUtils.getStackTrace(e));
                    break;
                }
            }
        }

    }

    public  boolean startImport(List<String> sqlList )  {
        /**
         * Create DB Connection  and execute Batch Statement
         */
        DBConnectionManager dbconn = null;
        try {
            dbconn = DBConnectionManager.getInstance();
        } catch (Exception e) {
            setException(e.getMessage());
            return false;
        }
        SQLoption sqlOption = new SQLoption(dbconn, getConnName());
        boolean isSuccess=sqlOption.batchExecute(sqlList);
        if (isSuccess) {
            log.info("Success Import DB ");
            return true;
        } else{
            log.error("Import DB Error");
            setException(sqlOption.getException());
            return false;
        }

    }
    public List<Integer> parseFieldsIndex(String values){
        Matcher matcher = SqlContent.FINDEX_PATTERN.matcher(values);
        List<Integer> fieldIndex = new ArrayList<Integer>();
        while (matcher.find()) {
            fieldIndex.add(Integer.parseInt(matcher.group(1)));
        }
        return fieldIndex;
    }
    public static void main(String[] args) {
        String  sql="INSERT INTO [Ecommerce].[dbo].[syn] (item,country,numner,icc,time)  #{'$1$','$2$',$3$,'shoppingcart',50}";
        String prestoPath="/user/hive/warehouse/temp.db/temp_c5d2d283d9074757a3d23b9eba307374";
        String connName="Presto";
        ImportDB importDB=new ImportDB(sql,prestoPath,connName,10,3);
        importDB.execute();
    }



}

