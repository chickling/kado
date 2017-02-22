package com.chickling.Schedule;

import com.chickling.util.StringUtil;
import com.chickling.util.TimeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.quartz.DateBuilder.*;
import static org.quartz.JobBuilder.*;
import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;
/**
 * Created by jw6v on 2015/11/27.
 */


public class QuartsTest {

    public static void main(String args[]) throws SchedulerException,ParseException{
        Logger log = LogManager.getLogger(QuartsTest.class);
        log.info("start");
        String startDateStr  = "2016-09-27 00:00:00";
        Date startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startDateStr);
        String currentTime = TimeUtil.getCurrentTime();
        log.info(currentTime);
        String[] interval_args={"559","1111111"};
        String rtn= StringUtil.cronGenerator("cycel", interval_args, startDate);
        log.info(rtn);



        Date currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(currentTime);
        //Date startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startDateStr);
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();
        int  scheduleID=1;
        if (startDate.before(currentDate)) {
            startDate = currentDate;
        }
        JobDetail scheduler = newJob(CronTest.class)
                .withIdentity("job1", "group1")//(,scheduleID)
                .usingJobData("ScheduleID", scheduleID)
                .build();

        Date runTime = evenSecondDate(new Date());
        CronTrigger trigger = newTrigger()
                .withIdentity("trigger1", "group1")
                .withSchedule(cronSchedule(rtn))
                .startAt(startDate)
                .build();

        sched.scheduleJob(scheduler, trigger);
        sched.start();
        System.out.println(trigger.getNextFireTime());
        System.out.println(trigger.getNextFireTime());
    }
}
