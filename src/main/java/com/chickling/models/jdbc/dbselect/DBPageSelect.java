package com.chickling.models.jdbc.dbselect;


import com.chickling.models.jdbc.dbselect.module.ColumnType;
import com.chickling.models.jdbc.dbselect.module.DBResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 *  Created by dl85
 *  Modified by aj65
 *  TODO
 *  1. add function to support PrepareStatement
 */
public class DBPageSelect extends DBBase {
    // keep the last method call parameter and related value, for later call nextPage()
    class LastExecInfo{
        String sql;
        int pagSize;
        PreparedStatement preparedStatement;

        LastExecInfo() {}

        LastExecInfo(String sql,int pagSize, PreparedStatement ps) {
            this.sql = sql;
            this.pagSize = pagSize;
            this.preparedStatement = ps;
        }
    }

    private static final Logger log = LogManager.getLogger(DBPageSelect.class);
    private final static int MAX_PAGESIZE = 1000;
    private int totalPage;
    private int pageNow;
    private LastExecInfo lastExecInfo;

    public DBPageSelect(DBConnectionManager connectionManager){
        super(connectionManager);

        totalPage = 0;
        pageNow = 0;
        lastExecInfo = new LastExecInfo();
    }

    /**
     * combine paging sql
     *
     * @param sql
     * @param primaryKey
     * @return
     */
    private String getPagingSql(String sql, String primaryKey) {
        StringBuilder pagingSql = new StringBuilder();
        pagingSql.append("SELECT * FROM (SELECT ROW_NUMBER() OVER (ORDER BY " + primaryKey + ") AS " + ROW_NUM + ",* FROM(");
        pagingSql.append(sql);
        pagingSql.append(") AS NewTable ) AS NewTable2 ");
        pagingSql.append(" WHERE " + ROW_NUM + ">=? AND " + ROW_NUM + " <=?");

        return pagingSql.toString();
    }

    /**
     * add array to another array
     *
     * @param oriObjs
     * @param targetObjs
     * @return
     */
    private Object[] addArray(Object[] oriObjs, Object[] targetObjs) {
        int oriLength = (oriObjs == null ? 0 : oriObjs.length);
        int targetLength = (targetObjs == null ? 0 : targetObjs.length);

        Object[] newObjs = new Object[oriLength + targetLength];
        if (null != oriObjs) {
            System.arraycopy(oriObjs, 0, newObjs, 0, oriLength);
        }
        System.arraycopy(targetObjs, 0, newObjs, oriLength, targetLength );
        return newObjs;
    }

    private long updateTotalPage(Connection conn, String sql,int pageSize ){
        Statement stmt = null;
        ResultSet rs = null;
        String countSQL = String.format("SELECT COUNT(1) FROM( %s ) AS src",sql);

        try
        {
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(countSQL);
            if( rs.first() )
            {
                long totalRow = rs.getLong(1);
                totalPage = (int)(totalRow/pageSize);
                if(totalRow%pageSize!=0)
                    totalPage++;
            }
        }
        catch (SQLException sqle)
        {
            log.error("get total page fail.",sqle);
        }

        releaseResource(stmt,rs);

        return totalPage;
    }

    public int getTotalPage() {
        return totalPage;
    }


    public DBResult executeSQL(String sql, String primaryKey, String conPoolName, int pageSize )
    {
        log.entry(sql,primaryKey,conPoolName,pageSize);

        pageSize = ( pageNow>MAX_PAGESIZE || pageSize<1 )?MAX_PAGESIZE:pageSize;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DBResult ret = new DBResult(sql);

        String sql_ps = this.getPagingSql(sql,primaryKey);
        ConnectionStatus cs = connectionManager.getDBConnectionStatus( conPoolName);
        if (null == cs) {
            ret.setException(new Exception("all db conn are out of connection "));
            return ret;
        }
        conn = connectionManager.getDBConnection(cs);
        if( null!=conn)
        {
            this.pageNow = 1;
            Object[] param =  new Object[]{(pageNow - 1) * pageSize + 1, pageNow * pageSize};
            updateTotalPage(conn, sql, pageSize);
            try
            {
                stmt = conn.prepareStatement( sql_ps, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );
                stmt.setFetchSize(FETCHSIZE);

                for(int i=0;i<param.length;i++)
                    stmt.setObject( i+1,param[i] );

                rs = stmt.executeQuery();
                if( rs.first() )
                {
                    log.trace("has first row");
                    //
                    // remember column name first
                    //
                    ResultSetMetaData mt = rs.getMetaData();
                    final int columnNum = mt.getColumnCount() -1 ;
                    String[]     columns = new String[columnNum];
                    ColumnType[] types = new ColumnType[columnNum];

                    // first column is 'RowNum' , not return
                    for (int i = 0; i < columnNum; i++)
                    {
                        columns[i] = mt.getColumnName(i+2);
                        types[i]  = ColumnType.fromSQLType(mt.getColumnType(i + 2));
                    }

                    ret.setColumnTypes(types);
                    ret.setColumnNames(columns);

                    //
                    // read data in first row
                    //
                    String[] values = new String[columnNum];
                    for (int i = 0; i < columnNum; i++)
                        values[i] = (null != rs.getString(i + 2) ? rs.getString(i + 2).trim() : null);
                    ret.addRow(values);

                    //
                    // read data in the other rows
                    //
                    while (rs.next())
                    {
                        String[] newRow = new String[columnNum];
                        for (int i = 0; i < columnNum; i++)
                            newRow[i] = (null != rs.getString(i + 2) ? rs.getString(i + 2).trim() : null);
                        ret.addRow(newRow);
                    }
                }
                ret.setSuccess();

                // keep execute info
                lastExecInfo = new LastExecInfo(sql, pageSize,stmt );
            }
            catch (SQLException sqle)
            {
                log.error("execute fail !!", sqle);
                cs.setActive(Boolean.FALSE);
                ret.setException( sqle );
            }
        }

        // only recycle resultSet, Statement and Connection re-used in method nextPage() gotoPage()
        releaseResource(null,rs);

        return ret;
    }

    public DBResult gotoPage(int page){
        if(page > totalPage)
            throw new IndexOutOfBoundsException(String.format("reach the end of data, total page is %s",totalPage));
        if(page<1)
            throw new IndexOutOfBoundsException("page must be greater than 0");

        DBResult ret = null;
        ResultSet rs = null;
        PreparedStatement stmt = this.lastExecInfo.preparedStatement;
        if( null!= stmt )
        {
            ret = new DBResult(this.lastExecInfo.sql);
            int pageSize = this.lastExecInfo.pagSize;
            Object[] param =  new Object[]{(page - 1) * pageSize + 1, page * pageSize};

            try
            {
                for(int i=0;i<param.length;i++)
                    stmt.setObject( i+1,param[i] );
                rs = stmt.executeQuery();

                if( rs.first() )
                {
                    log.trace("has first row");
                    //
                    // remember column name first
                    //
                    ResultSetMetaData mt = rs.getMetaData();
                    final int columnNum = mt.getColumnCount()-1;
                    String[]     columns = new String[columnNum];
                    ColumnType[] types = new ColumnType[columnNum];

                    for (int i = 0; i < columnNum; i++)
                    {
                        columns[i] = mt.getColumnName(i+2);
                        types[i]  = ColumnType.fromSQLType(mt.getColumnType(i + 2));
                    }

                    ret.setColumnTypes(types);
                    ret.setColumnNames(columns);

                    //
                    // read data in first row
                    //
                    String[] values = new String[columnNum];
                    for (int i = 0; i < columnNum; i++)
                        values[i] = (null != rs.getString(i + 2) ? rs.getString(i + 2).trim() : null);
                    ret.addRow(values);

                    //
                    // read data in the other rows
                    //
                    while (rs.next())
                    {
                        String[] newRow = new String[columnNum];
                        for (int i = 0; i < columnNum; i++)
                            newRow[i] = (null != rs.getString(i + 2) ? rs.getString(i + 2).trim() : null);
                        ret.addRow(newRow);
                    }
                }
                ret.setSuccess();
                this.pageNow = page;

                //
                // only release ResultSet
                //
                releaseResource(null,rs);
            }
            catch(SQLException sqle)
            {
                log.error("Execute sql fail",sqle);
                ret.setException(sqle);
            }
        }

        return ret;
    }

    public DBResult nextPage(){
        return gotoPage(pageNow+1);
    }

    public DBResult prevPage(){
        return gotoPage(pageNow-1);
    }

    /**
     * must call this method after use DBPageSelect
     */
    public void releaseResources(){
        PreparedStatement stmt = this.lastExecInfo.preparedStatement;
        if(stmt!=null)
        {
            Connection conn = null;
            try { conn = stmt.getConnection();}
            catch (SQLException e) {/* do nothing*/}

            releaseResource(stmt, null);
            connectionManager.recycleDBConneciton(conn);
        }
    }

//    /**
//     *
//     * @param ps         prepareStatement
//     * @param params
//     * @param primaryKey
//     * @param conPoolName
//     * @param pageSize  how many rows in a page
//     * @return
//     */
//    public DBResult executePrepareStatement(String ps, Object[] params, String primaryKey, String conPoolName,int pageSize)
//    {
//        log.entry(ps, params, conPoolName);
//
//        Connection conn = null;
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//        pageSize = (pageSize<1 || pageSize>MAX_PAGESIZE)?MAX_PAGESIZE:pageSize;
//
//        nowPage = 1;
//        if (null != primaryKey)
//        {
//            ps = this.getPagingSql(ps, primaryKey);
//            params = this.addArray(params, new Object[]{(nowPage - 1) * pageSize + 1, nowPage * pageSize});
//        }
//        DBResult ret = new DBResult( ps );
//
//        conn = connectionManager.getDBConnection(conPoolName);
//        if (null != conn)
//        {
//            try
//            {
//                stmt = conn.prepareStatement(ps, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//                stmt.setFetchSize(FETCHSIZE);
//                for (int i = 0; null != params && i < params.length; i++)
//                    stmt.setObject(i + 1, params[i]);
//
//                rs = stmt.executeQuery();
//                if (rs.first())
//                {
//                    log.trace("has first row");
//                    //
//                    // remember column name first
//                    //
//                    ResultSetMetaData mt = rs.getMetaData();
//                    final int columnNum = mt.getColumnCount();
//                    String[]     columns = new String[columnNum];
//                    ColumnType[] types = new ColumnType[columnNum];
//                    for (int i = 0; i < columnNum; i++)
//                    {
//                        columns[i] = mt.getColumnName(i+1);
//                        types[i]  = ColumnType.fromSQLType(mt.getColumnType(i + 1));
//                    }
//
//                    ret.setColumnTypes(types);
//                    ret.setColumnNames(columns);
//
//                    //
//                    // read data in first row
//                    //
//                    String[] values = new String[columnNum];
//                    for (int i = 0; i < columnNum; i++)
//                        values[i] = rs.getString(i + 1);
//                    ret.addRow(values);
//
//
//                    //
//                    // read data in the other rows
//                    //
//                    while (rs.next())
//                    {
//                        String[] newRow = new String[columnNum];
//                        for (int i = 0; i < columnNum; i++)
//                            newRow[i] = rs.getString(i + 1);
//                        ret.addRow(newRow);
//                    }
//                }
//                ret.setSuccess();
//            }
//            catch (SQLException sqle)
//            {
//                log.error("execute fail !!", sqle);
//                ret.setException( sqle );
//            }
//
//            if(null != rs)
//            {
//                try {rs.close(); }
//                catch (SQLException e) {}
//                finally { rs = null; }
//            }
//
//            if(null != stmt)
//            {
//                try { stmt.close(); }
//                catch (SQLException e) {}
//                finally { stmt = null;}
//            }
//
//            connectionManager.recycleDBConneciton(conn);
//        }
//
//        if (ret.isSuccess() && ret.getRowSize() != 0)
//        {
//            log.debug("get {} rows", ret.getRowSize());
//            log.debug("Row[{}] = {}", 0, java.util.Arrays.toString(ret.getRow(0).getValues()));
//        }
//
//        return ret;
//    }





//    /**
//     * execute a prepared statement sql & row_number
//     * @param ps          a prepare statement
//     * @param params      the parameters value set into ps
//     * @param primaryKey  primary key
//     * @param conPoolName connection pool name
//     * @param page        now page number
//     * @param pageSize    page size
//     * @return
//     */
//    public MapResultSet executePrepareStatementResultSet(String ps, Object[] params, String primaryKey, String conPoolName, Integer page, Integer pageSize) {
//        log.entry(ps, params, conPoolName);
//
//        Connection conn = null;
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//
//        if (null != primaryKey && null != page && null != pageSize)
//        {
//            ps = this.getPagingSql(ps, primaryKey);
//            params = this.addArray(params, new Object[]{(page - 1) * pageSize + 1, page * pageSize});
//        }
//        MapResultSet ret = new MapResultSet( ps );
//
//        conn = connectionManager.getDBConnection(conPoolName);
//        if(null != conn)
//        {
//            try
//            {
//                stmt = conn.prepareStatement(ps, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//                stmt.setFetchSize(FETCHSIZE);
//                for (int i = 0; null != params && i < params.length; i++)
//                    stmt.setObject(i + 1, params[i]);
//
//                ret = new MapResultSet(ps,stmt);
//                ret.setSuccess();
//            }
//            catch (SQLException sqle)
//            {
//                log.error("execute sql fail", sqle);
//                ret.setException( sqle );
//            }
//        }
//        else
//        {
//            log.error("can't get connection !! conn name: {}", conPoolName);
//        }
//
//        return ret;
//    }

}
