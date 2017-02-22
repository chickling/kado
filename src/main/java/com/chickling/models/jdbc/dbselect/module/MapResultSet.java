package com.chickling.models.jdbc.dbselect.module;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.*;

/**
 * @author: aj65
 */
public class MapResultSet{
    private final static Logger log = LogManager.getLogger( MapResultSet.class );
    private boolean success;
    private String sql;
    private ResultSet resultSet;
    private boolean end;
    private Exception exception;
    public MapResultSet(String sql,Statement stmt) {
        try
        {
            this.sql = sql;
            if(stmt instanceof PreparedStatement)
                resultSet = ((PreparedStatement)stmt).executeQuery();
            else if(stmt instanceof Statement)
              resultSet = stmt.executeQuery(sql);

            success = true;
        }
        catch (SQLException e)
        {
            log.error("execute sql {} fail",sql,e);
            this.exception = e;
        }
    }

    public MapResultSet(String sql){

    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess() {
        this.success = true;
    }

    public void setFail(){
        this.success = false;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public void releaseResources(){
        if(null!=resultSet)
        {
            Statement stmt = null;
            Connection conn = null;
            try
            {
                stmt = resultSet.getStatement();
                conn = stmt.getConnection();
            }
            catch (SQLException e) {}

            try {resultSet.close();}
            catch (SQLException e) {}
            finally {resultSet = null;}

            if( null!=stmt)
            {
                try{stmt.close();}
                catch (SQLException sqle){log.warn( "Statement recycle fail.",sqle );}
                finally { stmt = null;}
            }

            if( null!=conn )
            {
                try{ conn.close();}
                catch(SQLException sqle)
                { log.warn("Connection recycle fail.",sqle ) ;}
                finally
                { conn = null; }
            }
        }
    }

    /**
     * Get a block rows each invoke , block size = result.getFetchSize()
     * After reach the end of data, it auto close underlying resultSet and Statement and Connection
     *
     * if fetch fail, it will not change the success property to false.
     * You should check the success property of the returned DBResult
     *
     * @return
     */
    public DBResult fetchBlock(){
        DBResult ret = new DBResult( sql );
        if( null!=resultSet && !end)
        {
            try
            {
                // if reach the end of data, close
                if( resultSet.isAfterLast() )
                {
                    this.releaseResources();
                    end = true;
                }
                else
                {
                    int blockSize = resultSet.getFetchSize();

                    ResultSetMetaData mt = resultSet.getMetaData();
                    final int columnNum = mt.getColumnCount();
                    String[]     columns = new String[columnNum];
                    ColumnType[] types = new ColumnType[columnNum];

                    for (int i = 0; i < columnNum; i++)
                    {
                        columns[i] = mt.getColumnName(i+1);
                        types[i]  = ColumnType.fromSQLType(mt.getColumnType(i + 1));
                    }
                    //ret = new DBResult(sql,types,columns);
                    ret.setColumnTypes(types);
                    ret.setColumnNames(columns);

                    while( blockSize-->0 && resultSet.next() )
                    {
                        String[] row = new String[ ret.getColumnNames().length ];
                        for( int i=0; i<ret.getColumnNames().length ;i++ )
                            row[i] = null!=resultSet.getString(i+1)?resultSet.getString(i+1).trim():null;
                        ret.addRow(row);
                    }
                }
                ret.setSuccess();
            }
            catch (SQLException e)
            {
                log.warn("get a block of data from resultSet fail. ", e);
                ret.setException(e);
            }
        }

        return ret;
    }
}
