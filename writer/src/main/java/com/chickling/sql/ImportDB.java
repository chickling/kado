package com.chickling.sql;


import com.chickling.bean.result.ResultMap;
import com.chickling.face.PrestoResult;
import owlstone.dbclient.db.DBConnectionManager;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.IntStream;
enum Sqltype{none,insert,update};
/**
 * Created by gl08 on 2016/1/6.
 */
public class ImportDB {

    private Logger log= LogManager.getLogger(ImportDB.class);
    private String importSQL="";
    private int batchSize=SqlContent.MAX_BATCH_ROWS;
    private String tableName ="";
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

    public ImportDB() {
    }

    /**
     * @param importSQL         import SQL String
     * @param tableName          result of table
     * @param batchSize            SQL batch execute size
     */
    public ImportDB(String importSQL , String tableName , String connName, int batchSize)  {
        this.importSQL = importSQL;
        this.tableName =tableName;
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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getImportSQL() {
        return importSQL;
    }

    public void setImportSQL(String importSQL) {
        this.importSQL = importSQL;
    }

    public void execute()  {

        String sql=getImportSQL();
        Matcher insertMatch= null;
        Matcher updateMatch= null;

        if (SqlContent.SQL_UNION_PATTERN2.matcher(sql.toLowerCase()).find())
            insertMatch= SqlContent.SQL_UNION_PATTERN2.matcher(sql);
        if (SqlContent.SQL_UPDATE_PATTEN.matcher(sql.toLowerCase()).find())
            updateMatch= SqlContent.SQL_UPDATE_PATTEN.matcher(sql);

        log.info("DB SQL Find Matcher");
        ResultMap resultMap=null;
        PrestoResult jdbc=null;
        try {
            Class c = Class.forName("com.chickling.util.PrestoUtil");
            jdbc=(PrestoResult) c.newInstance();
            resultMap=jdbc.getPrestoResult("select * from "+tableName);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        assert resultMap != null;
        Sqltype type=Sqltype.none;
        String original="";
        if (null != insertMatch && insertMatch.find()) {
            original = insertMatch.group(0);
            type=Sqltype.insert;
        }
        else if (null != updateMatch &&  updateMatch.find()){
            type=Sqltype.update;
        }
        if (type == Sqltype.update || type==Sqltype.insert) {
            log.info("Total Row is : "+resultMap.getCount()+" , Batch Size is : "+getBatchSize());
            List<Integer> fieldIndex = parseFieldsIndex(sql);
            List<String> sqlList=new ArrayList<>();
            if ( type==Sqltype.insert) {
                String relpace = original.replaceAll("#\\{", "VALUES(").replaceAll("}", ")");
                sql = sql.replace(original, relpace);
            }
            final  String execSQL=sql;
            final  ResultMap map=resultMap;


            IntStream.range(0,resultMap.getCount()).forEach( dataIndex->{
                        String tmpSql=execSQL;
                        List<Object>  rowData=map.getData().get(dataIndex);
                        if (fieldIndex.size()>0) {
                            for (Integer index : fieldIndex) {
                                if (index <= 0 || index > rowData.size())
                                    tmpSql = tmpSql.replaceAll("\\$" + index + "\\$", "");
                                else {
                                    try {
                                        tmpSql = tmpSql.replaceAll("\\$" + index + "\\$", rowData.get(index-1).toString());
                                    } catch (Exception e2) {
                                        log.error(ExceptionUtils.getStackTrace(e2));
                                    }
                                }
                            }
                        }

                        sqlList.add(tmpSql);
                        if(dataIndex<10){
                            log.info(tmpSql);
                        }
                        if (dataIndex==(map.getCount()-1)){
                            if (startImport(sqlList)){
                                log.info("Import to DB Process is : " + 100 + " % ");
                                log.info("Import to DB Finished !!");
                                setSuccess(true);
                            }else
                                log.info(this::getException);
                        }else{
                            if (sqlList.size() > getBatchSize()) {
                                if (startImport(sqlList)){
                                    int process=((dataIndex*100)/map.getCount());
                                    log.info("Import to DB Process is : " + process + " % ");
                                    sqlList.clear();
                                }else
                                    log.info(this::getException);
                            }
                        }
                    }
            );
        }
    }


    public  boolean startImport(List<String> sqlList )  {
        /**
         * Create DB Connection  and execute Batch Statement
         */
        DBConnectionManager dbconn = null;
        try {
            dbconn = new DBConnectionManager();
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
//        String table="presto_temp.temp_fea6e977b2f14163822927b16f3889a4";
//        table="presto_temp.temp_c66612e49f414aceaefc442df4ca811e";
//        ResultMap resultMap=null;
//
//        PrestoResult jdbc=null;
//        try {
//            Class c = Class.forName("com.chickling.util.PrestoUtil");
//            jdbc=(PrestoResult) c.newInstance();
//            resultMap=jdbc.getPrestoResult("select * from "+table);
//
//
//
//            int pause=0;
//        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
        String table = "presto_temp.temp_7523db10414a47cabcf34c2741dffcaf";
        String sql = "insert into Presto.dbo.EC_CrawlerList (Content,utma,TotalClicks,Memo,[Type],[Level],[Domain],[ResStr1]) #{'$1$',null,$5$,'Top_Mobile_Site_SSL_HTTPS_Access_ReCaptcha','I',5,'WWWSSL','ALL'}";
       table=" presto_temp.temp_295ff9e9ac46443195ba1a814ded2ba4";
       sql="UPDATE  Fraud.dbo.HistoricNVTCDataForBI  SET [count_www]=$8$,[count_secure]=$9$,[count_review]=$10$  WHERE [SONumber]=$1$ and [count_www] is null";

//        Init.setPrestoURL("http://172.16.157.11:8080");
//        Init.setPrestoCatalog("hive");
//        Init.setPresto_user("presto");


//        sql="UPDATE   Presto.dbo.EC_CrawlerList  SET [Content]=111  WHERE [Status]='B' and [InUser] is null";
        String connName = "ST02CPS03";
//        Matcher matcher2=SqlContent.SQL_UPDATE_PATTEN.matcher(sql.toLowerCase());
        ImportDB importDB = new ImportDB(sql, table, connName, 2);
        importDB.execute();

//        String  sql="INSERT INTO [Ecommerce].[dbo].[syn] (item,country,numner,icc,time)  #{'$1$','$2$',$3$,'shoppingcart',50}";
//        String prestoPath="/user/hive/warehouse/temp.db/temp_c5d2d283d9074757a3d23b9eba307374";
//        String connName="Presto";
//        ImportDB importDB=new ImportDB(sql,prestoPath,connName,10,3);
//        importDB.execute();
//    }

    }

}

