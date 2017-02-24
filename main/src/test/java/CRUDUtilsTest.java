import com.chickling.models.job.bean.JobLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.chickling.boot.Init;
import com.chickling.models.AccountManager;
import com.chickling.models.job.bean.JobHistory;
import com.chickling.util.ChartCRUDUtils;
import com.chickling.util.JobCRUDUtils;
import com.chickling.util.ScheduleCRUDUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;
import org.junit.Test;

import static org.junit.Assert.*;
//import static org.hamcrest.Matchers.*;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jw6v on 2015/12/3.
 */
public class CRUDUtilsTest {

/**
 * Test JobCRUDUtils
 *
 */
    public static String jobInput="{\"jobname\":\"count row\",\"jobLevel\":\"0\",\"memo\":\"test\",\"notification\":true,\"sql\":\"{Base64 code}\",\"replace_value\":\"60\",\"replace_sign\":\"$1hour\",\"type\":\"userjob\",\"storage\":true,\"save_type\":\"DB\",\"filepath\":\"test\",\"filename\":\"test\",\"location_id\":\"1\",\"insertsql\":\"{Base64 code}\",\"Report\":\"true\",\"ReportEmail\":\"123@123;456@456\",\"ReportLength\":\"1000\",\"ReportFileType\":\"0\",\"ReportTitle\":\"test\",\"ReportWhileEmpty\":\"True\"}";
    public static String jobUpdate="{\"jobname\":\"update\",\"jobLevel\":\"0\",\"memo\":\"test\",\"notification\":true,\"sql\":\"{Base64 code}\",\"type\":\"userjob\",\"storage\":true,\"save_type\":\"DB\",\"filepath\":\"test\",\"filename\":\"test\",\"location_id\":\"2\",\"insertsql\":\"{Base64 code}\",\"Report\":\"falese\",\"ReportEmail\":\"123@123;456@456\",\"ReportLength\":\"1000\",\"ReportFileType\":\"0\",\"ReportTitle\":\"update\",\"ReportWhileEmpty\":\"True\"}";
    //public static String jobInfo="{\"jobname\":\"count row\",\"jobLevel\":\"0\",\"memo\":\"test\",\"notification\":true,\"sql\":\"{Base64 code}\",\"replace_value\":\"60\",\"replace_sign\":\"$1hour\",\"type\":\"userjob\",\"storage\":true,\"save_type\":\"DB\",\"filepath\":null,\"filename\":null,\"location_id\":\"1\",\"insertsql\":\"{Base64 code}\"}";;
    public static String admin_token_1="";
    public static String manager_token_1="";
    public static String general_token_1="";
    public static String admin_token_2="";
    public static String manager_token_2="";
    public static String general_token_2="";
    public static String logout_token="logouttoken";
    public static int admin_1_ID=0;
    public static int admin_2_ID=0;
    public static int manager_1_ID=0;
    public static int manager_2_ID=0;
    public static int general_1_ID=0;
    public static int general_2_ID=0;
public static Logger logger= LogManager.getLogger(CRUDUtilsTest.class);
    @BeforeClass
    public static void initialize() throws Exception{
        Init.setExpiration("7");
        Init.setSqliteName("PrestoJobPortal.sqlite");
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        AccountManager acm=new AccountManager();
        //add admin
        String a=acm.addUser("Jerome","Jerome","admin_test","xx@xxxx.xxx",1,2,true);
        acm.addUser("Jerome2","Jerome2","admin_test1","xx@xxxx.xxx",2,2,false);
        //admin login
        String adinfo=acm.login("Jerome","admin_test");
        Map a1=gson.fromJson(adinfo, type);
        String adinfo2=acm.login("Jerome2", "admin_test1");
        Map a2=gson.fromJson(adinfo2, type);

        admin_token_1=(String)a1.get("token");
        admin_token_2=(String)a2.get("token");

        admin_1_ID=Integer.parseInt((String)a1.get("uid"));
        admin_2_ID=Integer.parseInt((String)a2.get("uid"));
        //add manager
        acm.addUser("JeromeM","JeromeM","manager_test","xx@xxxx.xxx",1,1,true);
        acm.addUser("JeromeM2","JeromeM2","manager_test1","xx@xxxx.xxx",2,1,false);
        //manager login


        String mainfo=acm.login("JeromeM","manager_test");
        Map m1=gson.fromJson(mainfo, type);
        String mainfo2=acm.login("JeromeM2","manager_test1");
        Map m2=gson.fromJson(mainfo2, type);

        manager_token_1=(String)m1.get("token");
        manager_token_2=(String)m2.get("token");

        manager_1_ID=Integer.parseInt((String)m1.get("uid"));
        manager_2_ID=Integer.parseInt((String)m2.get("uid"));


        //add general
        acm.addUser("JeromeG","JeromeG","general_test","xx@xxxx.xxx",1,0,true);
        acm.addUser("JeromeG2","JeromeG2","general_test1","xx@xxxx.xxx",2,0,false);
        //general login

        String gainfo=acm.login("JeromeG","general_test");
        Map g1=gson.fromJson(gainfo, type);
        String gainfo2=acm.login("JeromeG2","general_test1");
        Map g2=gson.fromJson(gainfo2, type);

        general_token_1=(String)g1.get("token");
        general_token_2=(String)g2.get("token");

        general_1_ID=Integer.parseInt((String) g1.get("uid"));
        general_2_ID=Integer.parseInt((String) g2.get("uid"));

        logger.debug("Execute before");


    }

    @Test
    public void adduser() throws Exception{
        Init.setExpiration("7");
        Init.setSqliteName("PrestoJobPortal.sqlite");
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        AccountManager acm=new AccountManager();
        //add admin
        String a=acm.addUser("Jerome","Jerome","admin_test","xx@xxxx.xxx",1,2,true);
        acm.addUser("Jerome2","Jerome2","admin_test1","xx@xxxx.xxx",2,2,false);
        //admin login
        String adinfo=acm.login("Jerome","admin_test");
        Map a1=gson.fromJson(adinfo, type);
        String adinfo2=acm.login("Jerome2", "admin_test1");
        Map a2=gson.fromJson(adinfo2, type);

        admin_token_1=(String)a1.get("token");
        admin_token_2=(String)a2.get("token");

        admin_1_ID=Integer.parseInt((String)a1.get("uid"));
        admin_2_ID=Integer.parseInt((String)a2.get("uid"));
        //add manager
        acm.addUser("JeromeM","JeromeM","manager_test","xx@xxxx.xxx",1,1,true);
        acm.addUser("JeromeM2","JeromeM2","manager_test1","xx@xxxx.xxx",2,1,false);
        //manager login


        String mainfo=acm.login("JeromeM","manager_test");
        Map m1=gson.fromJson(mainfo, type);
        String mainfo2=acm.login("JeromeM2","manager_test1");
        Map m2=gson.fromJson(mainfo2, type);

        manager_token_1=(String)m1.get("token");
        manager_token_2=(String)m2.get("token");

        manager_1_ID=Integer.parseInt((String)m1.get("uid"));
        manager_2_ID=Integer.parseInt((String)m2.get("uid"));


        //add general
        acm.addUser("JeromeG","JeromeG","general_test","xx@xxxx.xxx",1,0,true);
        acm.addUser("JeromeG2","JeromeG2","general_test1","xx@xxxx.xxx",2,0,false);
        //general login

        String gainfo=acm.login("JeromeG","general_test");
        Map g1=gson.fromJson(gainfo, type);
        String gainfo2=acm.login("JeromeG2","general_test1");
        Map g2=gson.fromJson(gainfo2, type);

        general_token_1=(String)g1.get("token");
        general_token_2=(String)g2.get("token");

        general_1_ID=Integer.parseInt((String) g1.get("uid"));
        general_2_ID=Integer.parseInt((String) g2.get("uid"));

        logger.debug("Execute before");


    }

    @Test
    public void updateUser(){
        AccountManager acm=new AccountManager();
        acm.updateUser(2,"Jerome.J.Wu","jw6v","","Jerome.J.Wu@newegg.com",1,0,false);
    }

    @Test
    public void testGetUserInfo(){
        AccountManager acm=new AccountManager();
        System.out.println(acm.getUserInfo(2));
    }
    @Test
    public void testGetUserList(){
        AccountManager acm=new AccountManager();
        System.out.println(acm.getUserList());
    }

    @Ignore
    @Test
    public void testAddJobInfotoDB_logout() throws SQLException{
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        Map datas = gson.fromJson(jobInput, type);
        //only admin and manager could add job
        String message=JobCRUDUtils.addJobInfotoDB(datas, logout_token);
        Map r = gson.fromJson(message,type);
        assertTrue("error".equals(r.get("status")) && "Permission denied".equals(r.get("message")));

    }

    @Test
    public void testAddJobInfotoDB_admin() throws SQLException{
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        Map datas = gson.fromJson(jobInput, type);
        String message=JobCRUDUtils.addJobInfotoDB(datas, admin_token_1);
        Map r = gson.fromJson(message, type);
        String res=JobCRUDUtils.getJobInfo(Integer.parseInt((String) r.get("jobid")));
        datas.put("jobID",((String)r.get("jobid")));
        datas.put("jobowner",admin_1_ID);
        Map resm = gson.fromJson(res, type);
//        assertTrue(
//                (datas.get("jobname").equals(resm.get("jobname")))
//                        &&((Integer)datas.get("jobowner"))==(Double.valueOf((Double)resm.get("jobowner")).intValue())
//                        &&(Integer.parseInt((String)datas.get("jobLevel"))==(Double.valueOf((Double)resm.get("jobLevel")).intValue()))
//                        && (datas.get("memo").equals(resm.get("memo")))
//                        && "1".equals(resm.get("notification"))
//                        && (datas.get("sql").equals(resm.get("sql")))
//                        &&(Integer.parseInt((String)datas.get("replace_value"))==Double.valueOf((Double)resm.get("replace_value")).intValue())
//                        && (datas.get("replace_sign").equals(resm.get("replace_sign")))
//                        &&(2==((Double)resm.get("save_type")).intValue())
//                        && (datas.get("filepath").equals(resm.get("filepath")))
//                        && (datas.get("filename").equals(resm.get("filename")))
//                        &&(Integer.parseInt((String)datas.get("location_id"))==Double.valueOf((Double)resm.get("location_id")).intValue())
//                        && (datas.get("insertsql").equals(resm.get("insertsql"))));
        //assertThat(datas.entrySet(), equalTo(resm.entrySet()));

    }
    @Ignore
    @Test
    public void testAddJobInfotoDB_manager() throws SQLException{
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        Map datas = gson.fromJson(jobInput, type);
        String message= JobCRUDUtils.addJobInfotoDB(datas, manager_token_1);
        Map r = gson.fromJson(message, type);
        String res=JobCRUDUtils.getJobInfo(Integer.parseInt((String) r.get("jobid")));
        datas.put("jobID",Integer.parseInt((String)r.get("jobid")));
        datas.put("jobowner", manager_1_ID);
        Type type2 = new TypeToken<Map>() {}.getType();
        Map resm = gson.fromJson(res, type2);
        System.out.println(res);
        System.out.println(Double.valueOf((Double) resm.get("jobowner")).intValue());
        assertTrue(
                (datas.get("jobname").equals(resm.get("jobname")))
                        &&((Integer)datas.get("jobowner"))==(Double.valueOf((Double)resm.get("jobowner")).intValue())
                        &&(Integer.parseInt((String)datas.get("jobLevel"))==(Double.valueOf((Double)resm.get("jobLevel")).intValue()))
                        && (datas.get("memo").equals(resm.get("memo")))
                        && "1".equals(resm.get("notification"))
                        && (datas.get("sql").equals(resm.get("sql")))
                        &&(Integer.parseInt((String)datas.get("replace_value"))==Double.valueOf((Double)resm.get("replace_value")).intValue())
                        && (datas.get("replace_sign").equals(resm.get("replace_sign")))
                        &&(2==((Double)resm.get("save_type")).intValue())
                        && (datas.get("filepath").equals(resm.get("filepath")))
                        && (datas.get("filename").equals(resm.get("filename")))
                        &&(Integer.parseInt((String)datas.get("location_id"))==Double.valueOf((Double)resm.get("location_id")).intValue())
                        && (datas.get("insertsql").equals(resm.get("insertsql"))));
    }
    @Ignore
    @Test
    public void testAddJobInfotoDB_general() throws SQLException{
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        Map datas = gson.fromJson(jobInput, type);
        String message=JobCRUDUtils.addJobInfotoDB(datas, general_token_1);
        Map r = gson.fromJson(message,type);
        assertTrue("error".equals(r.get("status")) && "Permission denied".equals(r.get("message")));
    }
    @Ignore
    @Test
    public void testAddJobInfotoDB_admin2() throws SQLException{
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        Map datas = gson.fromJson(jobInput, type);
        String message=JobCRUDUtils.addJobInfotoDB(datas, admin_token_2);
        Map r = gson.fromJson(message, type);
        String res=JobCRUDUtils.getJobInfo(Integer.parseInt((String) r.get("jobid")));
        datas.put("jobID",Integer.parseInt((String)r.get("jobid")));
        datas.put("jobowner",admin_2_ID);
        Map resm = gson.fromJson(res, type);
        assertTrue(
                (datas.get("jobname").equals(resm.get("jobname")))
                        &&((Integer)datas.get("jobowner"))==(Double.valueOf((Double)resm.get("jobowner")).intValue())
                        &&(Integer.parseInt((String)datas.get("jobLevel"))==(Double.valueOf((Double)resm.get("jobLevel")).intValue()))
                        && (datas.get("memo").equals(resm.get("memo")))
                        && "1".equals(resm.get("notification"))
                        && (datas.get("sql").equals(resm.get("sql")))
                        &&(Integer.parseInt((String)datas.get("replace_value"))==Double.valueOf((Double)resm.get("replace_value")).intValue())
                        && (datas.get("replace_sign").equals(resm.get("replace_sign")))
                        &&(2==((Double)resm.get("save_type")).intValue())
                        && (datas.get("filepath").equals(resm.get("filepath")))
                        && (datas.get("filename").equals(resm.get("filename")))
                        &&(Integer.parseInt((String)datas.get("location_id"))==Double.valueOf((Double)resm.get("location_id")).intValue())
                        && (datas.get("insertsql").equals(resm.get("insertsql"))));
       // assertThat(datas.entrySet(), equalTo(resm.entrySet()));
    }
    @Ignore
    @Test
    public void testAddJobInfotoDB_manager2() throws SQLException{
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        Map datas = gson.fromJson(jobInput, type);
        String message=JobCRUDUtils.addJobInfotoDB(datas, manager_token_2);
        Map r = gson.fromJson(message, type);
        String res=JobCRUDUtils.getJobInfo(Integer.parseInt((String) r.get("jobid")));
        datas.put("jobID",Integer.parseInt((String)r.get("jobid")));
        datas.put("jobowner",manager_2_ID);
        Map resm = gson.fromJson(res, type);
        assertTrue(
                (datas.get("jobname").equals(resm.get("jobname")))
                        &&((Integer)datas.get("jobowner"))==(Double.valueOf((Double)resm.get("jobowner")).intValue())
                        &&(Integer.parseInt((String)datas.get("jobLevel"))==(Double.valueOf((Double)resm.get("jobLevel")).intValue()))
                        && (datas.get("memo").equals(resm.get("memo")))
                        && "1".equals(resm.get("notification"))
                        && (datas.get("sql").equals(resm.get("sql")))
                        &&(Integer.parseInt((String)datas.get("replace_value"))==Double.valueOf((Double)resm.get("replace_value")).intValue())
                        && (datas.get("replace_sign").equals(resm.get("replace_sign")))
                        &&(2==((Double)resm.get("save_type")).intValue())
                        && (datas.get("filepath").equals(resm.get("filepath")))
                        && (datas.get("filename").equals(resm.get("filename")))
                        &&(Integer.parseInt((String)datas.get("location_id"))==Double.valueOf((Double)resm.get("location_id")).intValue())
                        && (datas.get("insertsql").equals(resm.get("insertsql"))));
        //assertThat(datas.entrySet(), equalTo(resm.entrySet()));
    }
    @Ignore
    @Test
    public void testAddJobInfotoDB_general2() throws SQLException{
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        Map datas = gson.fromJson(jobInput, type);
        String message=JobCRUDUtils.addJobInfotoDB(datas, general_token_2);
        Map r = gson.fromJson(message,type);
        assertTrue("error".equals(r.get("status")) && "Permission denied".equals(r.get("message")));
    }
    @Test
    public void testupdateJobtoDB(){
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        Map datas = gson.fromJson(jobUpdate, type);
        String message=JobCRUDUtils.updateJobtoDB(datas, 156,admin_token_1);
        Map r = gson.fromJson(message, type);
        //String res=JobCRUDUtils.getJobInfo(Integer.parseInt((String) r.get("jobid")));
    }
    @Test
    public void testgetJobInfo(){
        //with or without token
        System.out.print(JobCRUDUtils.getJobInfo(100));
    }
    @Test
    public void testdeleteJob(){

    }
    @Test
    public void testgetJobList(){
        System.out.print(JobCRUDUtils.getJobList("54af83c9153ff64445ab0b71d56fbf118f49d4d0511a554dab961bf5f7fa93f0"));
    }
    @Test
    public void testgetJobStatusList(){
        System.out.print(JobCRUDUtils.getJobStatusList("","54af83c9153ff64445ab0b71d56fbf118f49d4d0511a554dab961bf5f7fa93f0"));
    }
    @Test
    public void testgetJobHistoryList(){

    }
    @Test
    public void testgetJobHistoryInfo(){

    }
    @Test
    public void testgetJobResultInfo(){

    }
    @Test
    public void testInsertJobHistory(){
        ArrayList<String> jhl=new ArrayList<>();
        JobHistory jh=new JobHistory(153, "123", 13, 1, "123", "123", 0, "123",123,  "123",false,"123@123",100,1,"test",true);
        try{
        int message=JobCRUDUtils.InsertJobHistory(jh.getInsertList());
        }catch (SQLException e){logger.info(e);}
    }
    @Test
    public void testUpdateJobHistory(){

    }
    @Test
    public void testInsertJobLog(){
        ArrayList<String> jhl=new ArrayList<>();
        JobLog jL=new JobLog("test", "test", "test", 1, 1, "test", "test", "123",123,  "123",1,1,true);
        try{
            int message=JobCRUDUtils.InsertJobLog(jL.getInsertList());
        }catch (SQLException e){logger.info(e);}
    }
    @Test
    public void testUpdateJobLog(){

    }
    @Test
    public void testJobIsExist(){

    }


/**
 * Test ScheduleCRUDUtils
 */

    @Test
    public void testAddSchedule(){

    }

    @Test
    public void testUpdateSchedule(){

    }
    @Test
    public void testDeleteScheduleJob(){
    }
    @Test
    public void testDeleteScheduleTime(){

    }
    @Test
    public void testInsertScheduleJob(){

    }
    @Test
    public void testInsertScheduleTime(){

    }
    @Test
    public void testGetRunJob(){

    }
    @Test
    public void testGetscheduleTime(){

    }
    @Test
    public void testGetScheduleInfo(){
        // with or without token
    }

    @Test
    public void testDeleteSchedule(){

    }

    @Test
    public void testGetScheduleList() throws Exception{
        while(true){
            System.out.println(ScheduleCRUDUtils.getScheduleList("817f939dd5c27cc294e9411456442e2ffc26be22dbcab481f2bb3cda750d27cb"));
            Thread.sleep(2000);
        }
    }
    @Test
    public void testGetScheduleStatusList() {

    }

    @Test
    public void testGetScheduleHistoryList(){

    }
    @Test
    public void testGetScheduleHistoryInfo(){

    }
    @Test
    public void testGetScheduleRunJob(){

    }
    @Test
    public void testInsertScheduleJobHistory(){

    }

    @Test
    public void testInsertScheduleHistory(){

        //int ScheduleID=ScheduleCRUDUtils.insertScheduleHistory();

    }
    @Test
    public void testUpdateScheduleHistory(){

    }
    @Test
    public void testScheduleIsExist(){

    }



    @AfterClass
    public static void after()throws Exception{
        AccountManager acm=new AccountManager();
        acm.logout(admin_token_1);
        acm.logout(admin_token_2);
        acm.logout(manager_token_1);
        acm.logout(manager_token_2);
        acm.logout(general_token_1);
        acm.logout(general_token_2);
        //todo delete user
        acm.delUser(admin_1_ID);
        acm.delUser(admin_2_ID);
        acm.delUser(manager_1_ID);
        acm.delUser(manager_2_ID);
        acm.delUser(general_1_ID);
        acm.delUser(general_2_ID);

        logger.debug("Execute after");
    }

    @Test
    public void addChartTest(){
        HashMap<String,String> input=new HashMap<>();
        input.put("JobID","3");
        input.put("Type","Line chart");
        input.put("Chart_Name","test1");
        input.put("Chart_Setting","{}");


        System.out.println(ChartCRUDUtils.addChart(input,""));


    }

    @Test
    public void deleteChartTest(){

        System.out.println(ChartCRUDUtils.deleteChart(2,1,""));

    }

    @Test
    public void deleteChartbyJobTest(){

        System.out.println(ChartCRUDUtils.deleteChartbyJob(2,""));

    }

    @Test
    public void updateChartTest(){
        HashMap<String,String> input=new HashMap<>();
        input.put("ChartID","4");
        input.put("Type","Pie chart");
        input.put("Chart_Name","test1");
        input.put("Chart_Setting","{}");


        System.out.println(ChartCRUDUtils.updateChart(input,1,""));

    }

    @Test
    public void readChartbyJobIDTest(){

       String rtn=ChartCRUDUtils.readChartbyJobID(5,"bd8a0822682f00cbfb1d961b157bd63dc838184c0c0d97dbda252adc8072d92d");

//        Gson gson=new Gson();
//        Type type = new TypeToken<Map>() {}.getType();
      // System.out.println(gson.toJson(rtn));
        System.out.println(rtn);
    }

    @Test
    public void readChartTest(){

        String rtn=ChartCRUDUtils.readChart(5,"bd8a0822682f00cbfb1d961b157bd63dc838184c0c0d97dbda252adc8072d92d");

//        Gson gson=new Gson();
//        Type type = new TypeToken<Map>() {}.getType();
       System.out.println(rtn);
    }
}


