package com.chickling.util;

import owlstone.dbclient.db.DBConnectionManager;

import java.io.InputStream;

/**
 * Created by ey67 on 2018/4/2.
 */
public class DBClientUtil {
    private static DBConnectionManager dbConnectionManager;
    public static InputStream getDBConfig(){
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("mariadb-config.yaml");
    }
    public static DBConnectionManager getDbConnectionManager(){
        if(dbConnectionManager==null)
            dbConnectionManager=new DBConnectionManager(getDBConfig());
        return dbConnectionManager;
    }
}
