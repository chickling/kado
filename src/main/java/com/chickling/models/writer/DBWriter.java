package com.chickling.models.writer;

import com.chickling.boot.Init;
import com.chickling.models.jdbc.ImportDB;
import com.chickling.models.job.JobRunner;
import com.chickling.models.job.bean.JobLog;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;

/**
 * Created by gl08 on 2016/9/23.
 */
public class DBWriter extends ResultWriter {

    private Logger log=LogManager.getLogger(DBWriter.class);
//    private  Logger log=LogManager.getLogger("RoutingAppender");

    private JobLog jobLog;
    private int location_id;
    private int resultCount;
    private String insertsql;


    @Override
    public void init(Object parameter) {
        HashMap map= (HashMap) parameter;
        this.jobLog= (JobLog) map.get("jobLog");
        this.location_id= (int) map.get("location_id");
        this.resultCount= (int) map.get("resultCount");
        this.insertsql= (String) map.get("insertsql");

//        this.log= (Logger) map.get("logObject");
//        ThreadContext.put("logFileName",  (String) map.get("logFileName"));

    }

    @Override
    public Integer call()   {

        String connName= Init.getLocationList().get(this.location_id);
        log.info("Start Insert Result to SQL Server : [ "+connName+" ]");
        ImportDB importDB = null;
        try {
            importDB = new ImportDB(new String(Base64.getDecoder().decode(this.insertsql), "UTF-8"), this.jobLog.getJoboutput(),connName, this.resultCount, Init.getImportBatchSize());
        } catch (UnsupportedEncodingException e) {
            setException(ExceptionUtils.getStackTrace(e));
        }
        importDB.execute();
        if (!importDB.isSuccess()){
            log.error("Import to DB  Error!! ");
            log.error(importDB.getException());
            setException(importDB.getException());
            return 0;
        }
        return 4;
    }
}
