package com.chickling.schedule;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by jw6v on 2016/2/3.
 */
public class CronTest implements Job {
    public void execute (JobExecutionContext context) throws JobExecutionException {
        System.out.println("Job start");
    }
}
