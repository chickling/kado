package com.chickling.models.jdbc.dbselect;


import com.chickling.models.jdbc.dbselect.module.ColumnType;
import com.chickling.models.jdbc.dbselect.module.DBResult;
import com.chickling.models.jdbc.dbselect.module.MapResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 * @author: aj65
 */
public class DBSelect extends DBBase {
    private final static Logger log = LogManager.getLogger(DBSelect.class);

    public DBSelect(DBConnectionManager connectionManager) {
        super(connectionManager);
    }


    /**
     * @param sql
     * @param conPoolName
     * @return DBResult
     */
    public DBResult executeSQL(String sql, String conPoolName) {
        log.entry(sql, conPoolName);
        DBResult ret = new DBResult(sql);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ConnectionStatus cs = connectionManager.getDBConnectionStatus( conPoolName);
        if (null == cs) {
            ret.setException(new Exception("all db conn are out of connection "));
            return ret;
        }
        conn = connectionManager.getDBConnection(cs);
        if (null != conn) {
            try {
                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                stmt.setFetchSize(FETCHSIZE);

                boolean isResultSet = false;
                isResultSet = stmt.execute(sql);
                while (!isResultSet)
                    isResultSet = stmt.getMoreResults();

                if (isResultSet) {
                    rs = stmt.getResultSet();
                }

                if (rs != null && rs.first()) {
                    log.trace("has first row");
                    //
                    // remember column name first
                    //
                    ResultSetMetaData mt = rs.getMetaData();
                    final int columnNum = mt.getColumnCount();
                    String[] columns = new String[columnNum];
                    ColumnType[] types = new ColumnType[columnNum];

                    for (int i = 0; i < columnNum; i++) {
                        columns[i] = mt.getColumnName(i + 1);
                        types[i] = ColumnType.fromSQLType(mt.getColumnType(i + 1));
                    }
                    //ret = new DBResult(sql,types,columns);
                    ret.setColumnTypes(types);
                    ret.setColumnNames(columns);

                    //
                    // read data in first row
                    //
                    String[] values = new String[columnNum];
                    for (int i = 0; i < columnNum; i++) {
                        values[i] = (null != rs.getString(i + 1) ? rs.getString(i + 1).trim() : null);
                    }
                    ret.addRow(values);

                    //
                    // read data in the other rows
                    //
                    while (rs.next()) {
                        String[] newRow = new String[columnNum];
                        for (int i = 0; i < columnNum; i++) {
                            newRow[i] = (null != rs.getString(i + 1) ? rs.getString(i + 1).trim() : null);
                        }
                        ret.addRow(newRow);
                    }
                }
                ret.setSuccess();
            } catch (SQLException sqle) {
                log.error("SQL:<" + sql + ">, execute sql fail", sqle);
                cs.setActive(Boolean.FALSE);
                ret.setException(sqle);
            }


            releaseResource(stmt, rs);

            connectionManager.recycleDBConneciton(conn);
        }

        if (ret.isSuccess() && ret.getRowSize() != 0) {
            log.debug("get {} rows", ret.getRowSize());
        }

        return ret;
    }


    /**
     * This method design to avoid out-of-memory.
     * If a SQL Statement return a huge number of rows, it may cause out-of-memory if transfer all rows to DBResult inside a method call.
     * This method design to avoid the above case, it return MapResultSet which wrap java ResultSet.
     * Call MapResultSet.fetchBlock() to get a block of data once a time.
     *
     * @param sql
     * @param conPoolName
     * @return MapResultSet
     */
    public MapResultSet executeSQLResultSet(String sql, String conPoolName) {
        log.entry(sql, conPoolName);
        MapResultSet ret = new MapResultSet(sql);

        Connection conn = null;
        Statement stmt = null;
        ConnectionStatus cs = connectionManager.getDBConnectionStatus( conPoolName);
        if (null == cs) {
            ret.setException(new Exception("all db conn are out of connection "));
            return log.exit(ret);
        }
        conn = connectionManager.getDBConnection(cs);
        if (null != conn) {
            try {
                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                stmt.setFetchSize(FETCHSIZE);
                ret = new MapResultSet(sql, stmt);
                ret.setSuccess();
            } catch (SQLException sqle) {
                log.error("SQL:<" + sql + ">, execute sql fail", sqle);
                cs.setActive(Boolean.FALSE);
                ret.setException(sqle);
            }
        }
        return log.exit(ret);
    }



    public DBResult executePrepareStatement(String ps, Object[] params, String conPoolName) {
        log.entry(ps, params, conPoolName);
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DBResult ret = new DBResult(ps);

        ConnectionStatus cs = connectionManager.getDBConnectionStatus( conPoolName);
        if (null == cs) {
            ret.setException(new Exception("all db conn are out of connection "));
            return ret;
        }
        conn = connectionManager.getDBConnection(cs);
        if (null != conn) {
            try {
                stmt = conn.prepareStatement(ps, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                stmt.setFetchSize(FETCHSIZE);
                for (int i = 0; null != params && i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }

                rs = stmt.executeQuery();
                if (rs.first()) {
                    log.trace("has first row");
                    //
                    // remember column name first
                    //
                    ResultSetMetaData mt = rs.getMetaData();
                    final int columnNum = mt.getColumnCount();
                    String[] columns = new String[columnNum];
                    ColumnType[] types = new ColumnType[columnNum];
                    for (int i = 0; i < columnNum; i++) {
                        columns[i] = mt.getColumnName(i + 1);
                        types[i] = ColumnType.fromSQLType(mt.getColumnType(i + 1));
                    }

                    ret.setColumnTypes(types);
                    ret.setColumnNames(columns);

                    //
                    // read data in first row
                    //
                    String[] values = new String[columnNum];
                    for (int i = 0; i < columnNum; i++) {
                        values[i] = rs.getString(i + 1);
                    }
                    ret.addRow(values);


                    //
                    // read data in the other rows
                    //
                    while (rs.next()) {
                        String[] newRow = new String[columnNum];
                        for (int i = 0; i < columnNum; i++) {
                            newRow[i] = rs.getString(i + 1);
                        }
                        ret.addRow(newRow);
                    }
                }
                ret.setSuccess();
            } catch (SQLException sqle) {
                log.error("execute fail !!", sqle);
                cs.setActive(Boolean.FALSE);
                ret.setException(sqle);
            }

            releaseResource(stmt, rs);

            connectionManager.recycleDBConneciton(conn);
        }

        if (ret.isSuccess() && ret.getRowSize() != 0) {
            log.debug("get {} rows", ret.getRowSize());
            log.debug("Row[{}] = {}", 0, java.util.Arrays.toString(ret.getRow(0).getValues()));
        }

        return ret;
    }


    /**
     * execute a prepared statement sql without use row_number
     *
     * @param ps          a prepare statement
     * @param params      the parameters value set into ps
     * @param conPoolName
     * @return
     */
    public MapResultSet executePrepareStatementResultSet(String ps, Object[] params, String conPoolName) {
        log.entry(ps, params, conPoolName);
        Connection conn = null;
        PreparedStatement stmt = null;

        MapResultSet ret = new MapResultSet(ps);
        ConnectionStatus cs = connectionManager.getDBConnectionStatus( conPoolName);
        if (null == cs) {
            ret.setException(new Exception("all db conn are out of connection "));
            return ret;
        }
        conn = connectionManager.getDBConnection(cs);
        if (null != conn) {
            try {
                stmt = conn.prepareStatement(ps, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                stmt.setFetchSize(FETCHSIZE);
                for (int i = 0; null != params && i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }

                ret = new MapResultSet(ps, stmt);
                ret.setSuccess();
            } catch (SQLException sqle) {
                log.error("execute sql fail", sqle);
                cs.setActive(Boolean.FALSE);
                ret.setException(sqle);
            }
        }

        return ret;
    }


    public boolean executeDELETESQL(String sql, String conPoolName) {
        log.entry(sql, conPoolName);
        Connection conn = null;
        Statement stmt = null;

        ConnectionStatus cs = connectionManager.getDBConnectionStatus( conPoolName);
        if (null == cs) {
            log.info("can't delete,because all db conn are out of connection ");
            return false;
        }
        conn = connectionManager.getDBConnection(cs);
        if (null != conn) {
            //
            // whether success or fail, it record the sql command
            //
            try {
                stmt = conn.createStatement();
                stmt.setFetchSize(FETCHSIZE);

                if (stmt.execute(sql)) {
                    log.info("success execute sql {" + sql + "}.");
                }
            } catch (SQLException sqle) {
                log.error("SQL:<" + sql + ">, execute sql fail", sqle);
                cs.setActive(Boolean.FALSE);
                return false;
            }

            if (null != stmt) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                } finally {
                    stmt = null;
                }
            }

            connectionManager.recycleDBConneciton(conn);
        }

        return true;
    }
}
