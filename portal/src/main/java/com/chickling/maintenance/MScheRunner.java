package com.chickling.maintenance;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by jw6v on 2016/1/13.
 */
public class MScheRunner implements Job {

    public void execute (JobExecutionContext context) throws JobExecutionException {
        DBmaintenance dbm=new DBmaintenance();
        dbm.jobResultMaintain();
        dbm.deleteTempTableOverTTL();
        dbm.deleteLocalLogOverTTL();
        dbm.deleteSQLiteLogOverTTL();
        dbm.deleteTempCSVOverTTL();
        dbm.backupSQLiteDB();
        dbm.deleteTempHDFSCSVdaily();
    }
}
