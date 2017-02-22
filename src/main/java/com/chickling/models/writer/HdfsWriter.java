package com.chickling.models.writer;

import com.google.common.base.Strings;
import com.chickling.models.dfs.FSFile;
import com.chickling.models.dfs.OrcFileUtil;
import com.chickling.models.job.bean.JobLog;
import org.apache.log4j.NDC;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;

import java.util.HashMap;

/**
 * Created by gl08 on 2016/9/23.
 */
public class HdfsWriter extends ResultWriter {

    private  Logger log=LogManager.getLogger(HdfsWriter.class);

    private JobLog jobLog;

    @Override
    public void init(Object parameter) {
        HashMap map= (HashMap) parameter;
        this.jobLog= (JobLog) map.get("jobLog");
    }


    @Override
    public Integer call()   {
        String csvResultPath=jobLog.getFilepath().trim().replace(" ","").replaceAll("\\\\","").replaceAll("/+","/");
        if (!csvResultPath.startsWith("/"))
            csvResultPath="/"+csvResultPath;
        if (!csvResultPath.endsWith("/"))
            csvResultPath=csvResultPath+"/";
        if (!Strings.isNullOrEmpty(jobLog.getFilename()))
            csvResultPath=csvResultPath+jobLog.getFilename();
        String sourceDir=jobLog.getJoboutput();

        if(!sourceDir.endsWith("/"))
            sourceDir=sourceDir+"/";
        log.info("Start Save CSV Format Result  to HDFS Path :  "+csvResultPath);

        OrcFileUtil orcFileUtil= OrcFileUtil.newInstance();
        if(orcFileUtil.writeORCFilestoCSV(sourceDir,csvResultPath, OrcFileUtil.TYPE.HDFS, FSFile.FSType.HDFS))
            log.info("Save Orc File to [HDFS] CSV File Success !!! ");
        else{
            log.error("Save CSV File Error");
            return 0;
        }
        return 1;
    }
}
