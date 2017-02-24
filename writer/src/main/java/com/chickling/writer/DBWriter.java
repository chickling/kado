package com.chickling.writer;

import com.chickling.boot.Init;
import com.chickling.dbselect.ManagerConfig;
import com.chickling.models.job.bean.JobLog;
import com.chickling.models.writer.ResultWriter;
import com.chickling.sql.ImportDB;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

/**
 * Created by gl08 on 2016/9/23.
 */
public class DBWriter implements ResultWriter {

    private Logger log=LogManager.getLogger(DBWriter.class);
//    private  Logger log=LogManager.getLogger("RoutingAppender");

    private JobLog jobLog;
    private int location_id;
    private int resultCount;
    private String insertsql;
    private String exception="";

    private ArrayList<String> locationList=new ArrayList<>();

    {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream input = classloader.getResourceAsStream("dbselect-config.yaml");
        Yaml yaml = new Yaml();
        ManagerConfig contactMap = yaml.loadAs(input,ManagerConfig.class);
        for (ManagerConfig.DataSource val : contactMap.getDataSourceList()) {
            System.out.println(val.getName());
            this.locationList.add(val.getName());
        }
        Init.setLocationList(locationList);
    }

    @Override
    public void init(Object parameter) {
        HashMap map= (HashMap) parameter;
        this.jobLog= (JobLog) map.get("jobLog");
        this.location_id= (int) map.get("location_id");
        this.resultCount= (int) map.get("resultCount");
        this.insertsql= (String) map.get("insertsql");

    }

    @Override
    public String getException() {
        return this.exception;
    }

    @Override
    public Integer call()   {

        String connName= Init.getLocationList().get(this.location_id);
        log.info("Start Insert Result to SQL Server : [ "+connName+" ]");
        ImportDB importDB = null;
        try {
            importDB = new ImportDB(new String(Base64.getDecoder().decode(this.insertsql), "UTF-8"), this.jobLog.getJoboutput(),connName, this.resultCount, Init.getImportBatchSize());
            importDB.execute();
            if (!importDB.isSuccess()){
                log.error("Import to DB  Error!! ");
                log.error(importDB.getException());
                this.exception=importDB.getException();
            }
        } catch (UnsupportedEncodingException | NullPointerException e) {
            this.exception= ExceptionUtils.getStackTrace(e);
            return 0;
        }
        return 4;
    }
}
