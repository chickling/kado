package com.chickling.util;

import org.apache.commons.httpclient.util.ExceptionUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by ey67 on 2017/1/13.
 */
public class JobHistoryCatch {
    private static Logger log = LogManager.getLogger(JobHistoryCatch.class);
    private static volatile JobHistoryCatch instance;
    public Map<String, Integer> jobHistoryIDs;
    private Integer jobStatusLimit=100;
    private Map<Integer, Map> jobStatusMap=new TreeMap<>();
    JobHistoryCatch(){
        this.jobHistoryIDs=new HashMap<>();
    }
    public static JobHistoryCatch getInstance() {
        if (null == instance) {
            synchronized (JobHistoryCatch.class) {
                if (null == instance) {
                    instance = new JobHistoryCatch();
                }
            }
        }
        return instance;
    }
    public void updateJobStatusList(){
        try {
            jobStatusMap=JobCRUDUtils.getAllJobStatusList(jobStatusLimit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateJobStatus(int JobHistoryID,String JobStartTime, String JobStopTime, int JobStatus, int JobProgress){
        if(jobStatusMap!=null&&jobStatusMap.get(JobHistoryID)!=null){
            try {
                jobStatusMap.get(JobHistoryID).put("start_time",JobStartTime);
                jobStatusMap.get(JobHistoryID).put("stop_time",JobStopTime);
                jobStatusMap.get(JobHistoryID).put("job_status",JobStatus);
                jobStatusMap.get(JobHistoryID).put("progress",JobProgress);
                if(!JobStopTime.equals("")&&!JobStopTime.equals("0000-00-00 00:00:00")) {
                    jobStatusMap.get(JobHistoryID).put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(JobStartTime),
                            TimeUtil.String2DateTime(JobStopTime)));
                }else {
                    jobStatusMap.get(JobHistoryID).put("runingtime", TimeUtil.getRunTime(TimeUtil.String2DateTime(JobStartTime),
                            TimeUtil.String2DateTime(TimeUtil.getCurrentTime())));
                }
            }catch (Exception e){
                log.warn("update job history id:{} fail! {}", ExceptionUtils.getMessage(e));
            }
        }else{
            log.info("JobHistoryID {} not found! or jobStatusMap is null ",JobHistoryID);
        }

    }

    public Map<Integer, Map> getJobStatusMap(Integer limit) {
        if(jobStatusMap.size()==0||!limit.equals(jobStatusLimit)){
            jobStatusLimit=limit;
            updateJobStatusList();
        }
        return jobStatusMap;
    }

    public void setJobStatusMap(Map<Integer, Map> jobStatusList) {
        this.jobStatusMap = jobStatusList;
    }
}
