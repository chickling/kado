import com.chickling.boot.Init;
import com.chickling.models.job.JobRunner;
import com.chickling.models.job.PrestoContent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.util.concurrent.*;

/**
 * Created by gl08 on 2015/12/3.
 */
public class JobTest {

    private static Logger log=LogManager.getLogger(JobTest.class);
    class testAppender implements Runnable {
        private  String name="";

        public  String getName() {
            return name;
        }

        public testAppender(String name) {
            this.name =name;
        }
        private Logger log= LogManager.getLogger(testAppender.class);
        @Override
        public void run() {
//            ThreadContext.push("logdir","www");
            ThreadContext.put("logFileName", getName());
            log.info("Error happened");

            int count=0;
            while (true){
                if (count>10)
                    break;
                log.error(count++ + "--WWWWWWWWWWW--"+ this.getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private  void test() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        Callable<Boolean> callable=new Callable<Boolean>() {
            @Override
            public Boolean call()   {
                try {
                    if (true)
                        throw new Exception("1111");
                } catch (Exception e) {
                    System.out.print("11111111");
                    return Boolean.FALSE;
                }finally {
                    System.out.println("55555555");
                }
                System.out.print("123");
                return  Boolean.TRUE;
            }
        };

        Future<Boolean> future=executor.submit(callable);
        System.out.println(future.get());
        executor.shutdown();
        int pause=0;




    }
    private  void testMultipleLogs(){

        Thread t1 = new Thread(new testAppender("aaa"));
        Thread t2 = new Thread(new testAppender("bbb"));
        Thread t3 = new Thread(new testAppender("ccc"));

        t1.start();
        t2.start();
        t3.start();
    }
    /*
        private  void testMultipleQueryUIJobRunner() throws Exception {
            int jobid=0;
            String token="f8e7651af62d4a878021243fb3c81404b710d0e192fc0ff6e248028ae8c0e69d";
            String sql="select * from ec.truesight_page";

            Thread job1=new Thread(new JobRunner(jobid,PrestoContent.QUERY_UI,token,sql));
            Thread job2=new Thread(new JobRunner(jobid,PrestoContent.QUERY_UI,token,sql));
            Thread job3=new Thread(new JobRunner(jobid,PrestoContent.QUERY_UI,token,sql));

            job1.start();
            job2.start();
            job3.start();
        }
*/
    private  void testSingleQueryUIJobRunner()   {
        int jobid=0;
        String token="41cbddcf913de8fece8ef8da2df2692f4e5f29eb3c7682b560ef8e08e23e4619";
        String sql="select * from ec.truesight_page ";
//        sql="SELECT COUNT(*) FROM ec.truesight_page";
        sql="drop table temp.temp_0020bdfd1e564cabbe6e4eccf7d9d059";
        sql="select d.c_ip,d.ct,d.org,d.orgdetail from (\n" +
                "   select\n" +
                "        aaa.c_ip,\n" +
                "        aaa.ct,\n" +
                "        aaa.org,\n" +
                "        aaa.orgdetail,\n" +
                "        sum(if(aaa.ipLong>=bbb.ipintegerfrom and aaa.ipLong<=bbb.ipintegerto,1,0)) whiteIp\n" +
                "    from(\n" +
                "        select aa.* from (\n" +
                "            select a.*,b.org,concat(b.cityname,',',b.statename,',',b.countryname,',',b.zipcode) orgdetail from (\n" +
                "                select\n" +
                "                    c_ip,\n" +
                "                    count(cs_uri_stem) ct,\n" +
                "                    cast(split(c_ip,'.')[4] as bigint)+cast(split(c_ip,'.')[3] as bigint)*256+cast(split(c_ip,'.')[2] as bigint)*256*256+cast(split(c_ip,'.')[1] as bigint)*256*256*256 as ipLong\n" +
                "                from ec.truesight_page\n" +
                "                where $last 24 hour$ and \n" +
                "                 lower(cs_host) like 'www.newegg <http://www.newegg/> %'\n" +
                "                and c_ip<>''\n" +
                "                and (\n" +
                "                    cs_uri_stem like '/Product/ProductList.aspx%' or\n" +
                "                    cs_uri_stem like '/Product/Product.aspx%' or\n" +
                "                    cs_uri_stem like '%/Store%' or\n" +
                "                    cs_uri_stem like '%/Category/ID%' or\n" +
                "                    cs_uri_stem like '%/SubCategory/ID%' or\n" +
                "                    cs_uri_stem like '%/BrandSubCat/ID%' or\n" +
                "                    cs_uri_stem like '%/BrandStore/ID%'\n" +
                "                )\n" +
                "                group by c_ip\n" +
                "                having count(cs_uri_stem)>100\n" +
                "                limit 2000\n" +
                "            ) a left outer join ec.truesight_ipinfo b on split(b.ipstart,'.')[1]=split(a.c_ip,'.')[1] where a.ipLong>=b.ipstartint64 and a.ipLong<=b.ipendint64\n" +
                "        ) aa left outer join ec.truesight_ip_whitelist bb on aa.c_ip = trim(bb.content) where bb.status is null\n" +
                "    ) aaa left outer join ec.truesight_iprules_whitelist bbb on split(trim(bbb.ipfrom),'.')[1]=split(aaa.c_ip,'.')[1] group by aaa.c_ip, aaa.ct, aaa.org, aaa.orgdetail\n" +
                ") d where whiteIp < 1 order by d.ct desc\n";
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Boolean> future = null;
        try {
            future = executor.submit(new JobRunner(jobid, PrestoContent.QUERY_UI, token, sql));

            if (future.isDone()) {
                System.out.println(future.get());

                System.out.println("testSingleQueryUIJobRunner  End");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            executor.shutdown();
        }
    }
    private  void testSingleJobRunner()   {
        int jobid=2;
        String token="41cbddcf913de8fece8ef8da2df2692f4e5f29eb3c7682b560ef8e08e23e4619";
        String sql="  select * from ec.truesight_page limit 1000  ";
        sql="  create table ec.syns with (partitioned_by = ARRAY['icc', 'time']) as select * from ec.syn  ";
//        String sqltoBase64= Base64.getEncoder().encodeToString(sql.getBytes());
//        sql="SELECT COUNT(*) FROM ec.truesight_page";
//        sql="drop table ec.syns";

        sql="select d.c_ip,d.ct,d.org,d.orgdetail from (\n" +
                "   select\n" +
                "        aaa.c_ip,\n" +
                "        aaa.ct,\n" +
                "        aaa.org,\n" +
                "        aaa.orgdetail,\n" +
                "        sum(if(aaa.ipLong>=bbb.ipintegerfrom and aaa.ipLong<=bbb.ipintegerto,1,0)) whiteIp\n" +
                "    from(\n" +
                "        select aa.* from (\n" +
                "            select a.*,b.org,concat(b.cityname,',',b.statename,',',b.countryname,',',b.zipcode) orgdetail from (\n" +
                "                select\n" +
                "                    c_ip,\n" +
                "                    count(cs_uri_stem) ct,\n" +
                "                    cast(split(c_ip,'.')[4] as bigint)+cast(split(c_ip,'.')[3] as bigint)*256+cast(split(c_ip,'.')[2] as bigint)*256*256+cast(split(c_ip,'.')[1] as bigint)*256*256*256 as ipLong\n" +
                "                from ec.truesight_page\n" +
                "                where $last 24 hour$ and \n" +
                "                 lower(cs_host) like 'www.newegg <http://www.newegg/> %'\n" +
                "                and c_ip<>''\n" +
                "                and (\n" +
                "                    cs_uri_stem like '/Product/ProductList.aspx%' or\n" +
                "                    cs_uri_stem like '/Product/Product.aspx%' or\n" +
                "                    cs_uri_stem like '%/Store%' or\n" +
                "                    cs_uri_stem like '%/Category/ID%' or\n" +
                "                    cs_uri_stem like '%/SubCategory/ID%' or\n" +
                "                    cs_uri_stem like '%/BrandSubCat/ID%' or\n" +
                "                    cs_uri_stem like '%/BrandStore/ID%'\n" +
                "                )\n" +
                "                group by c_ip\n" +
                "                having count(cs_uri_stem)>100\n" +
                "                limit 2000\n" +
                "            ) a left outer join ec.truesight_ipinfo b on split(b.ipstart,'.')[1]=split(a.c_ip,'.')[1] where a.ipLong>=b.ipstartint64 and a.ipLong<=b.ipendint64\n" +
                "        ) aa left outer join ec.truesight_ip_whitelist bb on aa.c_ip = trim(bb.content) where bb.status is null\n" +
                "    ) aaa left outer join ec.truesight_iprules_whitelist bbb on split(trim(bbb.ipfrom),'.')[1]=split(aaa.c_ip,'.')[1] group by aaa.c_ip, aaa.ct, aaa.org, aaa.orgdetail\n" +
                ") d where whiteIp < 1 order by d.ct desc\n";
//        sql=sql.replaceAll("\\n","");
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Boolean> future = null;
        try {
            future = executor.submit(new JobRunner(jobid, PrestoContent.USER_JOB, token));

            if (future.isDone()) {
                System.out.println(future.get());

                System.out.println("testSingleJobRunner  End");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            executor.shutdown();
        }
    }
    /*
    private void testSingleJobRunner() throws Exception {
        int jobid=7;
        String token="f8e7651af62d4a878021243fb3c81404b710d0e192fc0ff6e248028ae8c0e69d";
        Thread job=new Thread(new JobRunner(jobid, PrestoContent.USER_JOB,token));
        job.start();

    }
    private void testMultipleJobRunner() throws Exception {
        int jobid=7;
        String token="f8e7651af62d4a878021243fb3c81404b710d0e192fc0ff6e248028ae8c0e69d";
        Thread job1=new Thread(new JobRunner(jobid,PrestoContent.USER_JOB,token));
        Thread job2=new Thread(new JobRunner(jobid,PrestoContent.USER_JOB,token));
        Thread job3=new Thread(new JobRunner(jobid,PrestoContent.USER_JOB,token));

        job1.start();
        job2.start();
        job3.start();
    }
*/
    public JobTest() {
    }

    public static void main(String[] args) throws Exception {
        Init.setSqliteName("PrestoJobPortal.sqlite");
        Init.setPrestoURL("http://10.16.46.198:8080");
        Init.setDatabase("temp");
        Init.setHivepath("/user/hive/warehouse");
        Init.setLogpath("/tmp/presto-joblog");
        JobTest jobTest=new JobTest();
//        jobTest.test();
//        jobTest.testMultipleQueryUIJobRunner();
//        Init.getDeleteJobList().add(9);
        jobTest.testSingleQueryUIJobRunner();
//        jobTest.testSingleJobRunner();
//        jobTest.testMultipleLogs();
//        jobTest.testMultipleJobRunner();
//        jobTest.testSingleJobRunner();
//        System.out.println(testTryFinal());

//            String sql=" as select * ";
//
//        if (!sql.toLowerCase().contains("insert") && !sql.toLowerCase().contains("create") && !sql.toLowerCase().contains("drop")){
//            boolean pass =true;
//        }
    }

    protected static int testTryFinal(){
        try {
            try {
                throw new Exception("inner case");
            }catch (Exception e){
              log.error(e);
                return 0;
            }finally {
                log.fatal("INNER  FINALLY");
            }
        }finally {
            log.fatal("OUT FINALLY");
        }
    }
}
