package com.chickling.sqlite;

import com.chickling.util.YamlLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ey67 on 2015/11/25.
 */
public class ReadOnlyConnectionManager {
    private   SQLiteDataSource ds;
    private   Connection con;
    private Logger logger=LogManager.getLogger(ReadOnlyConnectionManager.class);

    public void init(){
//        logger.info("=== Start  Connect to SQLite ===");
        //todo add Retry ?
        SQLiteConfig config = new SQLiteConfig();
//        config.setSharedCache(true);
        config.setReadUncommited(true);
//        config.r
//        config.setSharedCache(true);
//        config.enableRecursiveTriggers(true);
        ds = new SQLiteDataSource(config);
        String sqlitepath=ReadOnlyConnectionManager.class.getResource("/").getPath()+ YamlLoader.instance.getSqliteName();
//        logger.info("Web Portal SQLite source is "+sqlitepath);
        ds.setUrl("jdbc:sqlite:"+sqlitepath);
    }

    public  Connection getConnection() throws SQLException
    {
        if(con!=null) {
            if (con.isClosed()) {
                con = ds.getConnection();
            }
        }else {
            init();
            con = ds.getConnection();
        }
        return con;
    }
    public void close() throws SQLException {
        if (!con.isClosed()) {
            con.close();
        }
    }
}
