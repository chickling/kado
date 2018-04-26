package com.chickling.util;

import owlstone.dbclient.db.DBConnectionManager;

/**
 * Created by ey67 on 2018/4/2.
 */
public class WriterDBClientUtil {
    private static DBConnectionManager dbConnectionManager;
    public static DBConnectionManager getDbConnectionManager(){
        if(dbConnectionManager==null)
            dbConnectionManager=new DBConnectionManager();
        return dbConnectionManager;
    }
}
