package com.chickling.sql;

import com.chickling.boot.Init;
import com.google.common.base.Strings;
import com.chickling.dbselect.DBConnectionManager;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by gl08 on 2016/1/6.
 */
public class SQLoption {

    private Logger logger= LogManager.getLogger(SQLoption.class);
    private static final int MAX_RETRY_TIMES = 3;
    private static final int SLEEP_TIMES = 3000;
    private DBConnectionManager connMgr=null;
    private  String connnName="";
    private StringBuilder exception=null;

    public String getException() {
        return exception.toString();
    }

    public void setException(String exception) {
        this.exception.append(exception).append("\\n");
    }

    public SQLoption(DBConnectionManager dbconn, String connName) {
        this.connMgr=dbconn;
        this.connnName=connName;
        exception=new StringBuilder();
    }

    public  boolean execute(String sql)  {
        Connection conn = connMgr.getDBConnection(connnName);
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
            }
        } else {
            setException("NO SQL CONNECTION TO DATABASE :" + connnName);
            return false;
        }
    }


    public boolean batchExecute(List<String> sqls)   {
        int retryTimes = 0;
        Connection conn = connMgr.getDBConnection(connnName);
        while (true) {
            try {
                if (null!=conn){
                    if (executeBatch(sqls,conn)){
                        conn.close();
                        break;
                    }
                }else
                    throw new SQLException("Connection is Null");

            } catch (SQLException sqle) {
                if (retryTimes < MAX_RETRY_TIMES) {
                    // close connection
                    connMgr.recycleDBConneciton(conn);
                    try {
                        Thread.sleep(SLEEP_TIMES);
                    } catch (InterruptedException e) {
                        /*do Nothing*/
                    }
                    retryTimes++;
                    // reopen
                    conn = connMgr.getDBConnection(connnName);
                } else {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        setException(ExceptionUtils.getStackTrace(e));
                    }
                    setException(ExceptionUtils.getStackTrace(sqle));
                    return false;
                }
            }

        }
        return true;
    }

    public  boolean executeBatch(List<String> sqlList,Connection conn)   {
        if (conn != null) {
            try {
                conn.setAutoCommit(false);
                Statement statement = conn.createStatement();
                statement.setQueryTimeout(3600);

                if (Init.getNotbatchdb().contains(connnName)){
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
            setException("NO SQL CONNECTION TO DATABASE :" + connnName);
            return false;
        }
    }


}
