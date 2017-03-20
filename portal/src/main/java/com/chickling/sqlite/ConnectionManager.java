package com.chickling.sqlite;
import com.chickling.util.YamlLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import java.sql.*;

/**
 * Created by ey67 on 2015/11/25.
 */
public class ConnectionManager {
    private static volatile ConnectionManager dbtm;
    private static  SQLiteDataSource ds;
    private static  Connection con;
    private Logger logger=LogManager.getLogger(ConnectionManager.class);
    public void init(){
        logger.info("=== Start  Connect to SQLite ===");
        //todo add Retry ?
        SQLiteConfig config = new SQLiteConfig();
        config.setSharedCache(true);
        config.enableRecursiveTriggers(true);
        ds = new SQLiteDataSource(config);
        String sqlitepath=ConnectionManager.class.getResource("/").getPath()+ YamlLoader.instance.getSqliteName();
        logger.info("Web Portal SQLite source is "+sqlitepath);
        ds.setUrl("jdbc:sqlite:"+sqlitepath);
    }


    public static synchronized int dbInsert(PreparedStatement st){
        Logger log = LogManager.getLogger(ConnectionManager.class);
        int key=-1;
        try{
            st.executeUpdate();

            ResultSet rs=st.getGeneratedKeys();
            while(rs.next()) {
                key = rs.getInt(1);
            }
            st.close();
            return key;
        }
        catch(SQLException sqle){
            log.error(sqle);
            return key;
        }
    }

    public synchronized static ConnectionManager getInstance() {
        if (null == dbtm) {
            dbtm = new ConnectionManager();
        }
        return dbtm;
    }
    public synchronized  Connection getConnection() throws SQLException
    {
        if(con!=null) {
            if (con.isClosed()) {
                con = ds.getConnection();
            }
        }else {
            dbtm.init();
            con = ds.getConnection();
        }
        return con;
    }
    public synchronized void close() throws SQLException {
        if (!con.isClosed()) {
            con.close();
        }
    }
}
