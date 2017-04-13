package com.chickling.models.writer;

import com.chickling.bean.job.JobLog;
import com.chickling.boot.Init;
import com.chickling.face.ResultWriter;
import com.chickling.models.dfs.FSFile;
import com.chickling.models.dfs.OrcFileUtil;
import com.chickling.util.PrestoUtil;
import com.google.common.base.Strings;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by gl08 on 2016/9/23.
 */
public class LocalWriter implements ResultWriter {

    private Logger log=LogManager.getLogger(LocalWriter.class);

    private JobLog jobLog;

    private String tableName;
    private String exception="";


    @Override
    public void init(Object parameter) {
        HashMap map= (HashMap) parameter;
        this.jobLog= (JobLog) map.get("jobLog");
        this.tableName= (String) map.get("tableName");
    }

    @Override
    public String getException() {
        return this.exception;
    }

    @Override
    public Integer call()   {
        String fs=File.separator;
        String csvResultPath= Init.getCsvlocalPath()+fs+jobLog.getFilepath().trim().replace(" ","");
        String result=new PrestoUtil().writeAsCSV(tableName,csvResultPath);
        log.info("tmp csv file Path is "+result);
        if(!Strings.isNullOrEmpty(result)) {
            log.info("Save Result to  Local  CSV File Success !!! ");
        }else{
            log.error("Save CSV File Error");
            return 0;
        }
        return 2;
    }
}
