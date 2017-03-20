package com.chickling.boot;

import com.chickling.face.ResultWriter;

import com.chickling.maintenance.DBmaintenance;
import com.chickling.models.dfs.FSFile;
import com.chickling.schedule.ScheduleMgr;
import com.chickling.util.YamlLoader;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.hadoop.fs.FsShell;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.util.*;

/**
 * Created by gl08 on 2015/11/30.
 * Update by gl08 on 016/09/29
 */
public class Init implements ServletContextListener{
    private Logger log= LogManager.getLogger(Init.class);
    private static String prestoURL="";
    private static String database="";
    private static String hivepath="";
    private static String logpath="";
    private static String sqliteName="";
    private static String SiteURLBase="";
    private static String Expiration="";
    private static String prestoCatalog="";
    private static String csvtmphdfsPath="";
    private static String csvlocalPath="";
    private static String deleteLogTTL="";
    private static Map<String,ResultWriter> injectionMap;

    public static String getDeleteLogTTL() {
        return deleteLogTTL;
    }

    public static void setDeleteLogTTL(String deleteLogTTL) {
        Init.deleteLogTTL = deleteLogTTL;
    }

    public static String getCsvlocalPath() {
        return csvlocalPath;
    }

    public static void setCsvlocalPath(String csvlocalPath) {
        Init.csvlocalPath = csvlocalPath;
    }

    private static Set<String> notbatchdb=new HashSet<>();

    public static Set<String> getNotbatchdb() {
        return notbatchdb;
    }

    public static void setNotbatchdb(Set<String> notbatchdb) {
        Init.notbatchdb = notbatchdb;
    }

    private static ArrayList<String> locationList=new ArrayList<>();
    private static ArrayList<Integer> deleteJobList=new ArrayList<>();
    private static int importBatchSize=0;


    public static ArrayList<String> getLocationList() {
        return locationList;
    }

    public static void setLocationList(ArrayList<String> locationList) {
        Init.locationList = locationList;
    }

    public static String getLogpath() {
        return logpath;
    }

    public static void setLogpath(String logpath) {
        Init.logpath = logpath;
    }

    public static String getHivepath() {
        return hivepath;
    }

    public static void setHivepath(String hivepath) {
        Init.hivepath = hivepath;
    }

    public static String getDatabase() {
        return database;
    }
    public static void setDatabase(String database) {
        Init.database = database;
    }
    public static void setPrestoURL(String prestoURL) {
        Init.prestoURL = prestoURL;
    }
    public static String getPrestoURL() {
        return prestoURL;
    }

    public static String getSqliteName() {
        return sqliteName;
    }

    public static void setSqliteName(String sqliteName) {
        Init.sqliteName = sqliteName;
    }

    public static ArrayList<Integer> getDeleteJobList() {
        return deleteJobList;
    }
    public static void setSiteURLBase(String siteURLBase){Init.SiteURLBase=siteURLBase;}
    public static String getSiteURLBase(){return SiteURLBase;}

    public static void setExpiration(String expiration){Init.Expiration=expiration;}
    public static String getExpiration(){return Expiration;}

    public static String getCsvtmphdfsPath() {
        return csvtmphdfsPath;
    }

    public static void setCsvtmphdfsPath(String csvtmphdfsPath) {
        Init.csvtmphdfsPath = csvtmphdfsPath;
    }

    public static void setPrestoCatalog(String prestoCatalog){Init.prestoCatalog=prestoCatalog;}
    public static String getPrestoCatalog(){return prestoCatalog;}


    public Init() {
        ThreadContext.put("logFileName","init");
    }


    public  static ResultWriter  getInjectionInstance(String className) {
        return injectionMap.get(className);
    }


    @Override
    public void contextInitialized(ServletContextEvent sce) {

        String siteURLBase=YamlLoader.instance.getSiteURLBase()+sce.getServletContext().getContextPath();
        String expiration=YamlLoader.instance.getExpiration();
        String prestoURL=YamlLoader.instance.getPrestoURL();
        String database=YamlLoader.instance.getDatabase();
        String hivepath=YamlLoader.instance.getHivepath();
        String logpath=YamlLoader.instance.getLogpath();
        String sqliteName=YamlLoader.instance.getSqliteName();
        String prestoCatalog=YamlLoader.instance.getPrestoCatalog();
        String csvtmphdfsPath=YamlLoader.instance.getCsvtmphdfsPath();
        String csvlocalpath=YamlLoader.instance.getCsvlocalPath();
        String deleteLogTTL=YamlLoader.instance.getDeleteLogTTL();
        String writerinjection=YamlLoader.instance.getWrtierinjection();

        try {

            injectionMap=new HashMap<>();
            //injection init
            for (String injection: writerinjection.split("\\$")){
                Class c=Class.forName(injection);
                injectionMap.put(injection,(ResultWriter)c.newInstance());
            }
            if (!Strings.isNullOrEmpty(System.getProperty("locationlist"))){
                locationList.addAll(new Gson().fromJson(System.getProperty("locationlist"),ArrayList.class));
            }
            if (!Strings.isNullOrEmpty(prestoURL)) {
                sce.getServletContext().setAttribute("prestoURL", prestoURL);
            } else {
                sce.getServletContext().setAttribute("prestoURL", "");

                throw new Exception("presto url not set Error , please check your config.yaml");
            }
            if (!Strings.isNullOrEmpty(database)) {
                sce.getServletContext().setAttribute("database", database);
            } else {
                sce.getServletContext().setAttribute("database", "temp");

                throw new Exception("hive database not set Error , please check your config.yaml");
            }
            if (!Strings.isNullOrEmpty(hivepath)) {
                sce.getServletContext().setAttribute("hivepath", hivepath);
            } else {
                sce.getServletContext().setAttribute("hivepath", "/user/hive/warehouse");

                throw new Exception("hive path not set Error , please check your config.yaml");
            }
            if (!Strings.isNullOrEmpty(logpath)) {
                sce.getServletContext().setAttribute("logpath", logpath);
            } else {
                sce.getServletContext().setAttribute("logpath", "/tmp/presto-joblog");

                throw new Exception("logpath not set Error , please check your config.yaml");
            }
            if (!Strings.isNullOrEmpty(sqliteName)) {
                sce.getServletContext().setAttribute("sqliteName", sqliteName);
            } else {
                sce.getServletContext().setAttribute("sqliteName", "Kado.sqlite");

                throw new Exception("dbLocation not set Error , please check your config.yaml");
            }
            if (!Strings.isNullOrEmpty(siteURLBase)) {
                sce.getServletContext().setAttribute("SiteURLBase", SiteURLBase);
            } else {
                sce.getServletContext().setAttribute("SiteURLBase", "");

                throw new Exception("SiteURLBase set Error , please check your config.yaml");
            }
            if (!Strings.isNullOrEmpty(expiration)) {
                sce.getServletContext().setAttribute("expiration", Expiration);
            } else {
                sce.getServletContext().setAttribute("expiration", "");
                throw new Exception("SiteURLBase set Error , please check your config.yaml");
            }
            if (!Strings.isNullOrEmpty(prestoCatalog)) {
                sce.getServletContext().setAttribute("prestoCatalog", prestoCatalog);
            } else {
                sce.getServletContext().setAttribute("prestoCatalog", "");
                throw new Exception("prestoCatalog set Error , please check your config.yaml");
            }
            if (!Strings.isNullOrEmpty(csvtmphdfsPath)) {
                sce.getServletContext().setAttribute("csv.tmp.hdfs.path", csvtmphdfsPath);
            } else {
                sce.getServletContext().setAttribute("csv.tmp.hdfs.path", "");
                throw new Exception("csvtmphdfsPath set Error , please check your config.yaml");
            }

            if (!Strings.isNullOrEmpty(deleteLogTTL)) {
                sce.getServletContext().setAttribute("deleteLogTTL", deleteLogTTL);
            } else {
                throw new Exception("csvlocalpath set Error , please check your config.yaml");
            }

            setCsvlocalPath(csvlocalpath);
            setHivepath(hivepath);
            setDatabase(database);
            setPrestoURL(prestoURL);
            setLogpath(logpath);
            setSqliteName(sqliteName);
            setSiteURLBase(siteURLBase);
            setExpiration(expiration);
            setDeleteLogTTL(deleteLogTTL);
            setPrestoCatalog(prestoCatalog);
            setCsvtmphdfsPath(csvtmphdfsPath);

            String sqliteSite="";
            if ( !Strings.isNullOrEmpty(System.getenv("sqlitedb")) ){
                sqliteSite=System.getenv("sqlitedb");
                log.info("start load HDFS SQLite DB to Local");
                FSFile fsFile=FSFile.newInstance(FSFile.FSType.HDFS);
                FsShell fsShell=new FsShell(fsFile.getFs().getConf());
                File file =new File(YamlLoader.instance.getSqliteLOCALpath());
                if (file.delete())
                    log.info("Remove exist SQLite  DB");
                log.info("get SQLite DB From HDFS ");
                fsShell.run(new String[]{"-copyToLocal",sqliteSite,YamlLoader.instance.getSqliteLOCALpath()});
                log.info("Finish load HDFS File from "+sqliteSite+" to "+YamlLoader.instance.getSqliteLOCALpath());
            }
            checkHDFSPath();
            DBmaintenance dbm=new DBmaintenance();
            dbm.maintain();
            ScheduleMgr smgr=new ScheduleMgr();
            smgr.initSchedule();

        } catch (Exception e) {
            log.error("Init Service Error " + ExceptionUtils.getStackTrace(e));
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        Enumeration attrNames = sc.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            sc.removeAttribute(attrNames.nextElement().toString());
        }
    }


    private  void checkHDFSPath(){
        FSFile fsFile=FSFile.newInstance(FSFile.FSType.HDFS);
        FsShell fsShell=new FsShell(fsFile.getFs().getConf());
        try {

            // check log dilr
            //
            if (! fsFile.getFs().exists(new Path(logpath))){
                fsFile.getFs().mkdirs(new Path(logpath));
                fsShell.run(new String[]{"-chmod","-R","775",logpath});
                log.warn("Create [ Job Log ]  HDFS Dir!");
            }else
                log.info("Check [ Job Log ] HDFS path Exist !!!!");
            //check csv temp dir
            //
            if (! fsFile.getFs().exists(new Path(csvtmphdfsPath))){
                fsFile.getFs().mkdirs(new Path(csvtmphdfsPath));
                fsShell.run(new String[]{"-chmod","-R","775",csvtmphdfsPath});
                log.warn("Create [ CSV  Temp  ]  HDFS Dir!");
            }else
                log.info("Check [ CSV  Temp ] HDFS path Exist !!!!");
            //check SQLite backup dir
            //
            if (! fsFile.getFs().exists(new Path( YamlLoader.instance.getSqliteHDFSpath()))){
                fsFile.getFs().mkdirs(new Path( YamlLoader.instance.getSqliteHDFSpath()));
                fsShell.run(new String[]{"-chmod","-R","775", YamlLoader.instance.getSqliteHDFSpath()});
                log.warn("Create [ SQLite backup ]  HDFS Dir!");
            }else
                log.info("Check [ SQLite backup ] HDFS path Exist !!!!");
        } catch (Exception e) {
            log.error("check hdfs dir Error : "+ExceptionUtils.getStackTrace(e));
        }


    }
}