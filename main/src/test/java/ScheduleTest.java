
import com.chickling.Schedule.ScheduleMgr;
import com.chickling.Schedule.ScheduleRunner;
import com.chickling.boot.Init;
import com.chickling.models.dfs.FSFile;
import com.chickling.models.job.JobRunner;
import com.chickling.util.StringUtil;
import com.chickling.util.TimeUtil;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


import static org.junit.Assert.assertTrue;
import static org.quartz.JobBuilder.newJob;

/**
 * Created by jw6v on 2015/12/16.
 */
public class ScheduleTest {
    public static Logger logger= org.apache.logging.log4j.LogManager.getLogger(CRUDUtilsTest.class);
    @Ignore
    @Test
    public void testCronFormat()throws Exception{
        String[] interval_args={"2","Hour"};
        Date startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2016-01-28 00:27:00");
        String rtn=StringUtil.cronGenerator("interval", interval_args, startDate);
        logger.info(rtn);
        //assertTrue(("0 0 0/2 * * ?").equals(rtn));
    }

    @Ignore
    @Test
    public void testScheduleMgr() throws Exception{
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();

        ScheduleMgr scheduleMgr=new ScheduleMgr();
//        System.out.println(scheduleMgr.initSchedule());
        System.out.println(scheduleMgr.stopSchedule(11, "6bf6357555994cf11efd90079e4e906855cc42aca11c98e841d60e8040fb95d3"));
        for (String groupName : scheduler.getJobGroupNames()) {

            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                String jobName = jobKey.getName();
                String jobGroup = jobKey.getGroup();

                //get job's trigger
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                Date nextFireTime = triggers.get(0).getNextFireTime();

                System.out.println("[jobName] : " + jobName + " [groupName] : "
                        + jobGroup + " - " + nextFireTime);

            }
        }
        System.out.println(scheduleMgr.startSchedule(11, ""));
        for(String group: scheduler.getTriggerGroupNames()) {
            // enumerate each trigger in group
            for(TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.groupEquals(group))) {
                System.out.println("Found trigger identified by: " + triggerKey);
            }
        }
        for (String groupName : scheduler.getJobGroupNames()) {

            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                String jobName = jobKey.getName();
                String jobGroup = jobKey.getGroup();

                //get job's trigger
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                Date nextFireTime = triggers.get(0).getNextFireTime();

                System.out.println("[jobName] : " + jobName + " [groupName] : "
                        + jobGroup + " - " + nextFireTime);

            }
        }

        for(String group: scheduler.getTriggerGroupNames()) {
            // enumerate each trigger in group
            for(TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.groupEquals(group))) {
                System.out.println("Found trigger identified by: " + triggerKey);
            }
        }
        Thread.sleep(35000);
        System.out.println(scheduleMgr.stopSchedule(11, "6bf6357555994cf11efd90079e4e906855cc42aca11c98e841d60e8040fb95d3"));
        for(String group: scheduler.getTriggerGroupNames()) {
            // enumerate each trigger in group
            for(TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.groupEquals(group))) {
                System.out.println("Found trigger identified by: " + triggerKey);
            }
        }

    }

    @Ignore
    @Test
    public void testScheduleRunner() throws Exception{
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        ScheduleMgr scheduleMgr=new ScheduleMgr();
//        scheduleMgr.stopSchedule(10, "6bf6357555994cf11efd90079e4e906855cc42aca11c98e841d60e8040fb95d3");
//        scheduleMgr.startSchedule(10, "6bf6357555994cf11efd90079e4e906855cc42aca11c98e841d60e8040fb95d3");
        Date startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-12-24 16:02:00");

        JobDetail jobDetail = newJob(ScheduleRunner.class)
                .withIdentity("test","test")//(,scheduleID)
                .usingJobData("ScheduleID", 1)
                .usingJobData("ScheduleOwner",1)
                .build();
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("test", "test")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/2 * * * ?"))
                .startAt(startDate)
                .build();
        scheduler.start();
        scheduler.scheduleJob(jobDetail, trigger);

        for (String groupName : scheduler.getJobGroupNames()) {

            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                String jobName = jobKey.getName();
                String jobGroup = jobKey.getGroup();

                //get job's trigger
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                Date nextFireTime = triggers.get(0).getNextFireTime();

                System.out.println("[jobName] : " + jobName + " [groupName] : "
                        + jobGroup + " - " + nextFireTime);

            }
        }
        for(String group: scheduler.getTriggerGroupNames()) {
            // enumerate each trigger in group
            for(TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.groupEquals(group))) {
                System.out.println("Found trigger identified by: " + triggerKey);
            }
        }

        Thread.sleep(2400000);

    }


    @Ignore
    @Test
    public void saveLogToHDFS()   {
        String logName="ScheduleHistoryLog_16.log.log";
        FSFile hdfs=FSFile.newInstance(FSFile.FSType.HDFS);
//        FSFile localfs=FSFile.newInstance(FSFile.FSType.LocalFs);
//        String localLogPath=JobRunner.class.getResource("/")+ThreadContext.get("logFileName");
        String  localLogPath=JobRunner.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"logs/";
        File file =new File(localLogPath.trim());
        //        FileFilter
//       String [] filename=file.list(new FilenameFilter() {
//           @Override
//           public boolean accept(File file, String s) {
//               return IOCase.SYSTEM.checkStartsWith(s, logName);
//           }
//       });

        String path= Init.getLogpath()+"/Schedule/";
        Path logDir =new Path(path);

        try {
            if (!hdfs.getFs().exists(logDir))
                hdfs.getFs().mkdirs(logDir);
            hdfs.copyFileLocalToFs(localLogPath + logName, path + logName);

        } catch (IOException e) {
            System.out.println(e.toString());
        }

    }

    @Test
    public void testBeforeDate(){
        System.out.println(TimeUtil.beforeDate("7"));

    }


}

