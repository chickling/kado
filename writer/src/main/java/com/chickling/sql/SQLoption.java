package com.chickling.sql;

import com.chickling.util.YamlConfig;
import com.newegg.ec.db.AutoRecycleConnectionManager;
import com.newegg.ec.db.DBClient;
import com.newegg.ec.db.DBConnectionManager;
import com.newegg.ec.db.ManagerConfig;
import com.newegg.ec.db.module.AlterAction;
import com.newegg.ec.db.module.BatchInsert;
import com.newegg.ec.db.module.Delete;
import com.newegg.ec.db.module.Update;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by gl08 on 2016/1/6.
 */
public class SQLoption {

    private Logger logger= LogManager.getLogger(SQLoption.class);
    private static final int MAX_RETRY_TIMES = 3;
    private static final int SLEEP_TIMES = 2000;
    private DBConnectionManager connMgr=null;
    private  String dsName ="";
    private StringBuilder exception=null;
//    private  DBConnectionManager dbcm=null;

    public String getException() {
        return exception.toString();
    }

    public void setException(String exception) {
        this.exception.append(exception).append("\\n");
    }

    public SQLoption(DBConnectionManager dbconn, String dsName) {
        this.connMgr=dbconn;
        this.dsName =dsName;
        exception=new StringBuilder();
    }

    public  boolean execute(String sql)  {

        Connection conn = connMgr.getConnection(dsName);
        boolean isSuccess=false;
        if (conn != null) {
            try {
                conn.setAutoCommit(false);
                Statement statement = conn.createStatement();
                isSuccess = statement.execute(sql);
                conn.commit();
                statement.close();

                return isSuccess;
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    setException(ExceptionUtils.getStackTrace(e1));
                }
                setException(ExceptionUtils.getStackTrace(e));
                return false;
            }finally {
                connMgr.recycleConnection(dsName,conn);
            }

        } else {
            setException("NO SQL CONNECTION TO DATABASE :" + dsName);
            return false;
        }
    }


//    private void stopDBConnectionManager(){
//        this.dbcm.removeAllDBConn();
//        this.dbcm.stopCheckTimer();
//        this.dbcm=null;
//    }


    public boolean batchExecute(List<String> sqls)   {
        int retryTimes = 0;
        Connection conn = connMgr.getConnection(dsName);
        while (true) {
            try {
                if (null!=conn){
                    if (executeBatch(sqls,conn)){
                        logger.info("DB  insert/update  Finished !!");
                        break;
                    }
                }else
                    throw new SQLException("Connection is Null");

            } catch (SQLException sqle) {
                if (retryTimes < MAX_RETRY_TIMES) {
                    logger.info("get New  DBConnectionManager Instance ");
                    try {
                        Thread.sleep(SLEEP_TIMES);
                    } catch (InterruptedException e) {
                        /*do Nothing*/
                    }
                    retryTimes++;
                    // get Connection from  New DBConnectionManager Instance
                    conn = connMgr.getConnection(dsName);
                }else
                    return false;
            }
        }
        connMgr.recycleConnection(dsName,conn);
        return true;
    }

    public  boolean executeBatch(List<String> sqlList,Connection conn)   {
        if (conn != null) {
            try {
                conn.setAutoCommit(false);
                Statement statement = conn.createStatement();
                statement.setQueryTimeout(3600);

//                String nobatchdb=System.getProperty("nobatchdb");
                String nobatchdb= YamlConfig.instance.getNotbatchdb();
                Set<String> noBatchDB=new HashSet<>();
                if (!Strings.isNotEmpty(nobatchdb))
                    noBatchDB.addAll(Arrays.asList(nobatchdb.split(",")));

                if (noBatchDB.contains(dsName)){
                    for (String sql : sqlList) {
                        try {
                            statement.execute(sql);
                        }catch (Exception e){
                            logger.error("SQL Insert Error : "+sql );
                            logger.error(e);
                        }
                    }
                }else{
                    statement.clearBatch();
                    for (String sql : sqlList) {
                        statement.addBatch(sql);
                    }
                    statement.executeBatch();
                }
                conn.commit();
                statement.close();
                return true;
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    logger.error("SQL Roll Back Error" + e);
                }
                logger.error("Batch Insert  Error with : "+e);
                logger.error("This Batch SQL is : "+sqlList);
            }
            return true;
        } else {
            setException("NO SQL CONNECTION TO DATABASE :" + dsName);
            return false;
        }
    }


}
