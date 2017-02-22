package com.chickling.conn;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by ey67 on 2015/11/25.
 */
public class DBConnectionManager {
    private static volatile DBConnectionManager dbtm;
    private static  SQLiteDataSource ds;

    public void init(){
        SQLiteConfig config = new SQLiteConfig();
        // config.setReadOnly(true);
        config.setSharedCache(true);
        config.enableRecursiveTriggers(true);
        ds = new SQLiteDataSource(config);
        ds.setUrl("jdbc:sqlite:PrestoJobPortal.sqlite");
    }

    public static DBConnectionManager getInstance() {
        if (null == dbtm) {
            synchronized (DBConnectionManager.class) {
                if (null == dbtm) {
                    dbtm = new DBConnectionManager();
                    dbtm.init();
                }
            }
        }
        return dbtm;
    }
    public Connection getConnection() throws SQLException
    {
        if(ds.getConnection()==null){
            dbtm.init();
        }
        return ds.getConnection();
    }
    public void close() throws SQLException {
        ds.getConnection().close();
    }
}
