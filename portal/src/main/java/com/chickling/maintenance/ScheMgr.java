package com.chickling.maintenance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.newJob;

/**
 * Created by jw6v on 2016/1/13.
 */
public class ScheMgr {
    private Logger log = LogManager.getLogger(ScheMgr.class);
    public ScheMgr(){}

    public void startSche(){

        try{

            SchedulerFactory sf = new StdSchedulerFactory();
            Scheduler sched = sf.getScheduler();
        /**Create Quartz scheduler by scheduleID and passed the value of scheduleID and scheduleOwner for maintaining scheduleInfo**/
            JobDetail scheduler = newJob(MScheRunner.class)
                    .withIdentity("Maintain", "Daily")//(,scheduleID)
                    .build();

            sched.start();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("Maintain", "Daily")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 5 0 * * ?").withMisfireHandlingInstructionFireAndProceed())
                    .startNow()
                    .build();
            sched.scheduleJob(scheduler, trigger);
        }
        catch(SchedulerException ex){
            log.error(ex.getMessage());
        }
    }
}
