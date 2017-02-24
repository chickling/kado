package com.chickling.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ey67 on 2017/1/13.
 */
public class JobHistoryCatch {
    private static volatile JobHistoryCatch instance;
    public Map<String, Integer> jobHistoryIDs;
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
}
