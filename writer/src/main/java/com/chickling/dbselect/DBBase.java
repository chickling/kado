package com.chickling.dbselect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author: aj65
 * 2015/5/7
 * TODO
 * 1. arrange duplicate code of DBSelect and DBPageSelect to DBBase
 */
public abstract class DBBase {
    private final static Logger log = LogManager.getLogger(DBBase.class);
    protected final static String ROW_NUM = "RowNum";
    protected final static int FETCHSIZE = 5120;
    protected DBConnectionManager connectionManager;

    protected DBBase(DBConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    protected void releaseResource(Statement stmt, ResultSet rs) {
        if (null != rs) {
            try {
                rs.close();
            } catch (SQLException e) {
            } finally {
                rs = null;
            }
        }

        if (null != stmt) {
            try {
                stmt.close();
            } catch (SQLException e) {
            } finally {
                stmt = null;
            }
        }
    }


    public long getResultRowNum(String sql, String conPoolName) {
        long ret = -1;
        Statement stmt = null;
        ResultSet rs = null;
        String countSQL = String.format("SELECT COUNT(1) FROM( %s ) AS src", sql);
        ConnectionStatus cs = connectionManager.getDBConnectionStatus( conPoolName);
        if (null == cs) {
            log.error("can't get result,all db conn are out of connection ");
            return ret;
        }
        Connection conn = connectionManager.getDBConnection(cs);

        if (null != conn) {
            try {
                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                rs = stmt.executeQuery(countSQL);
                if (rs.first()) {
                    ret = rs.getLong(1);
                }
            } catch (SQLException sqle) {
                log.error("get total row fail.", sqle);
            }

            releaseResource(stmt, rs);
            connectionManager.recycleDBConneciton(conn);
        }

        return ret;
    }


    /**
     * get the max number of primaryKey in executed SQL result
     *
     * @param sql
     * @param primaryKey
     * @param conPoolName
     */
    public long getMaxPKValue(String sql, String primaryKey, String conPoolName) {
        long ret = -1;
        StringBuilder maxRowNumSql = new StringBuilder();
        maxRowNumSql.append("select MAX(" + ROW_NUM + ") as maxRow from (select ROW_NUMBER() over (order by " + primaryKey + ") as " + ROW_NUM + ",* from(");
        maxRowNumSql.append(sql);
        maxRowNumSql.append(") as NewTable ) as NewTable2 ");

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ConnectionStatus cs = connectionManager.getDBConnectionStatus( conPoolName);
        if (null == cs) {
            log.error("can't get result(max pk),all db conn are out of connection ");
            return ret;
        }
        conn = connectionManager.getDBConnection(cs);
        if (null != conn) {
            try {
                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                rs = stmt.executeQuery(sql);
                if (rs.first()) {
                    ret = rs.getLong(1);
                }
            } catch (SQLException sqle) {
                log.error("getTotalRowNumber fail ", sqle);
            }

            this.releaseResource(stmt, rs);
            connectionManager.recycleDBConneciton(conn);
        }

        return ret;
    }
}
