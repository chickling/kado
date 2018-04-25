package com.chickling.models.tabletool;

import com.chickling.models.HiveJDBC;
import com.chickling.util.CountingRequestBody;
import com.chickling.util.WebHDFSUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;

/**
 * Created by ey67 on 2018/2/5.
 */
public class UploadJob implements Runnable{
    /*Log4J*/
    Logger log = LogManager.getLogger(UploadJob.class);
    private String file;
    private String fileName;
    private Map tableSetting;

    public UploadJob(String file, Map tableSetting, String fileName) throws Exception {
        this.file=file;
        this.tableSetting=tableSetting;
        this.fileName=fileName;
    }

    @Override
    public void run() {
        try {
            /**Update JobInfo**/
            Status jobStatus=TableCreate.getInsertDataStatus().get(fileName);
            jobStatus.setStatus(Status.RUNING);
            jobStatus.setRowCount(0);
            TableCreate.setInsertDataStatus(file,jobStatus);

            CountingRequestBody.Listener listener=new CountingRequestBody.Listener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength) {
                    Float progress = (bytesWritten / (float) contentLength) * 100;
                    Status jobStatus=TableCreate.getInsertDataStatus().get(fileName);
                    jobStatus.setStatus(Status.RUNING);
                    jobStatus.setProcessCount(0);
                    jobStatus.setProgress(progress.intValue());
                    TableCreate.setInsertDataStatus(fileName,jobStatus);
                }
            };
            if(tableSetting.get("partitionName")!=null &&!tableSetting.get("partitionName").toString().isEmpty()) {
                /**Has Partition**/
                WebHDFSUtil.uploadTableFile(
                        tableSetting.get("db_name").toString() + "." + tableSetting.get("table_name").toString(),
                        tableSetting.get("partitionName").toString(),
                        new File(file),
                        listener
                );

                /*Create Partition*/
                String createPartitionSQL="alter table " + tableSetting.get("db_name").toString() + "." + tableSetting.get("table_name").toString() + " add if not exists partition ("+getPartitionAddSQL(tableSetting.get("partitionName").toString())+")";
                log.debug("Create Partition SQL: {}",createPartitionSQL);
                HiveJDBC.getInstance().getConnection().createStatement().execute(createPartitionSQL);
            }else {
                WebHDFSUtil.uploadTableFile(
                        tableSetting.get("db_name").toString() + "." + tableSetting.get("table_name").toString(),
                        "",
                        new File(file),
                        listener
                );
            }
            /**Update JobInfo**/
            jobStatus=TableCreate.getInsertDataStatus().get(fileName);
            jobStatus.setStatus(Status.SUCCESS);
            jobStatus.setProcessCount(0);
            jobStatus.setProgress(100);
            TableCreate.setInsertDataStatus(fileName,jobStatus);
            log.info("Upload Data Done!"+file);
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            /**Update JobInfo**/
            Status jobStatus=TableCreate.getInsertDataStatus().get(fileName);
            jobStatus.setStatus(Status.FAIL);
            jobStatus.setMessage(ExceptionUtils.getMessage(e));
            TableCreate.setInsertDataStatus(fileName,jobStatus);
        }
    }

    private String getPartitionAddSQL(String partitionString) throws Exception {
        String sql="";
        String[] partitions=partitionString.split("/");

        for(String partition:partitions){
            String[] tmp=partition.split("=");
            if(tmp.length==2)
                sql+="`"+tmp[0]+"`"+"='"+tmp[1]+"',";
            else
                throw new Exception("Parser Partition Fail! : split partition fail");
        }

        if(!sql.isEmpty())
            return sql.endsWith(",")?sql.substring(0,sql.length()-1):sql;
        else
            throw new Exception("Parser Partition Fail! : partitions is Empty");
    }
}
