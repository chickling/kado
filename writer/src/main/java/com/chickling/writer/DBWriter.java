package com.chickling.writer;

import com.chickling.bean.job.JobLog;
import com.chickling.face.ResultWriter;
import com.chickling.sql.ImportDB;
import com.chickling.util.YamlConfig;
import com.newegg.ec.db.DBClient;
import com.newegg.ec.db.DBConnectionManager;
import com.newegg.ec.db.ManagerConfig;
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

    private JobLog jobLog;
    private int location_id;
    private int resultCount;
    private String insertsql;
    private String exception="";
    private int batchSize=500;
    private String tableName;
    private ArrayList<String> locationList=new ArrayList<>();

    {
//        DBConnectionManager dbconnmgr=DBConnectionManager.getSingletonInstance();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream input = classloader.getResourceAsStream("dbselect-config.yaml");
        Yaml yaml = new Yaml();
        ManagerConfig contactMap = yaml.loadAs(input,ManagerConfig.class);
        for (ManagerConfig.DataSource val : contactMap.getDataSourceList()) {
            System.out.println(val.getName());
            this.locationList.add(val.getName());
        }
        System.setProperty("locationlist",this.locationList.toString());
        this.batchSize= YamlConfig.instance.getImportBatchSize();
    }

    @Override
    public void init(Object parameter) {
        HashMap map= (HashMap) parameter;
        this.jobLog= (JobLog) map.get("jobLog");
        this.location_id= (int) map.get("location_id");
        this.resultCount= (int) map.get("resultCount");
        this.insertsql= (String) map.get("insertsql");
        this.tableName= (String) map.get("tableName");
    }

    @Override
    public String getException() {
        return this.exception;
    }

    @Override
    public Integer call()   {

        String connName= this.locationList.get(this.location_id);
        log.info("Start Insert Result to SQL Server : [ "+connName+" ]");
        ImportDB importDB = null;
        try {
            importDB = new ImportDB(new String(Base64.getDecoder().decode(this.insertsql), "UTF-8"), tableName,connName, this.batchSize);
            importDB.execute();
            if (!importDB.isSuccess()){
                log.error("Import to DB  Error!! ");
                log.error(importDB.getException());
                this.exception=importDB.getException();
            }
        } catch (NullPointerException | UnsupportedEncodingException e ) {
            this.exception= ExceptionUtils.getStackTrace(e);
            return 0;
        }
        return 4;
    }
}
