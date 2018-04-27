package com.chickling.boot;

import com.chickling.face.ResultWriter;
import com.chickling.maintenance.DBmaintenance;
import com.chickling.schedule.ScheduleMgr;
import com.chickling.util.YamlLoader;
import com.google.common.base.Strings;
import com.google.gson.Gson;

import org.apache.commons.lang3.exception.ExceptionUtils;
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

    private static String SiteURLBase="";
    private static String Expiration="";
    private static String prestoCatalog="";
    private static String csvlocalPath="";
    private static String deleteLogTTL="";
    private static String presto_user="";
    private  static String fileseparator=File.separator;
    private static String jsonDir="";
    private static String tempDir="json";
    private static Map<String,ResultWriter> injectionMap;
    private static String hdfsHost="http://172.16.156.11:50070";
    private static String externalTableHDFSRootPath="/user/ec/kado_external";
    private static String hdfsUser="hdfs";
    private static String hiveJDBC="";
    private static String[] prestoDBWhitelist=new String[]{};

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

    public static ArrayList<Integer> getDeleteJobList() {
        return deleteJobList;
    }
    public static void setSiteURLBase(String siteURLBase){Init.SiteURLBase=siteURLBase;}
    public static String getSiteURLBase(){return SiteURLBase;}

    public static void setExpiration(String expiration){Init.Expiration=expiration;}
    public static String getExpiration(){return Expiration;}


    public static void setPrestoCatalog(String prestoCatalog){Init.prestoCatalog=prestoCatalog;}
    public static String getPrestoCatalog(){return prestoCatalog;}



    public static String getFileseparator() {
        return fileseparator;
    }

    public static void setFileseparator(String fileseparator) {
        Init.fileseparator = fileseparator;
    }

    public static String getPresto_user() {
        return presto_user;
    }

    public static void setPresto_user(String presto_user) {
        Init.presto_user = presto_user;
    }

    public static String getTempDir() {
        return tempDir;
    }

    public static String getJsonDir() {
        return jsonDir;
    }

    public static void setJsonDir(String jsonDir) {
        Init.jsonDir = jsonDir;
    }

    public static void setTempDir(String tempDir) {
        Init.tempDir = tempDir;
    }

    public Init() {
        ThreadContext.put("logFileName","init");
    }


    public  static ResultWriter  getInjectionInstance(String className) {
        return injectionMap.get(className);
    }


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Start Service Initial~");
        String siteURLBase=YamlLoader.instance.getSiteURLBase()+sce.getServletContext().getContextPath();
        String expiration=YamlLoader.instance.getExpiration();
        String prestoURL=YamlLoader.instance.getPrestoURL();
        String database=YamlLoader.instance.getDatabase();
        String hivepath=YamlLoader.instance.getHivepath();
        String logpath=YamlLoader.instance.getLogpath();
        String prestoCatalog=YamlLoader.instance.getPrestoCatalog();
        String csvlocalpath=YamlLoader.instance.getCsvlocalPath();
        String deleteLogTTL=YamlLoader.instance.getDeleteLogTTL();
        String writerinjection=YamlLoader.instance.getWrtierinjection();
        String presto_user=YamlLoader.instance.getPresto_hdfs_user();
        String hdfsUser=YamlLoader.instance.getHdfsUser();
        String hiveJDBC=YamlLoader.instance.getHiveJDBC();
        String hdfsHost=YamlLoader.instance.getHdfsHost();
        String externalTableHDFSRootPath=YamlLoader.instance.getExternalTableHDFSRootPath();
        String[] prestoDBWhitelist=YamlLoader.instance.getPrestoDBWhitelist();

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

            if (!Strings.isNullOrEmpty(deleteLogTTL)) {
                sce.getServletContext().setAttribute("deleteLogTTL", deleteLogTTL);
            } else {
                sce.getServletContext().setAttribute("deleteLogTTL", "30");
                throw new Exception("csvlocalpath set Error , please check your config.yaml");
            }
            if (!Strings.isNullOrEmpty(presto_user)) {
                sce.getServletContext().setAttribute("presto_user", presto_user);
            } else {
                sce.getServletContext().setAttribute("presto_user", "root");
            }

            if (!Strings.isNullOrEmpty(hdfsHost)) {
                sce.getServletContext().setAttribute("hdfsHost", hdfsHost);
            } else {
                sce.getServletContext().setAttribute("hdfsHost", "http://172.16.156.12:50070");
            }

            if (!Strings.isNullOrEmpty(hdfsUser)) {
                sce.getServletContext().setAttribute("hdfsUser", hdfsUser);
            } else {
                sce.getServletContext().setAttribute("hdfsUser", "hdfs");
            }

            if (!Strings.isNullOrEmpty(hiveJDBC)) {
                sce.getServletContext().setAttribute("hiveJDBC", hiveJDBC);
            } else {
                sce.getServletContext().setAttribute("hiveJDBC", "hive");
            }

            if (!Strings.isNullOrEmpty(externalTableHDFSRootPath)) {
                sce.getServletContext().setAttribute("externalTableHDFSRootPath", externalTableHDFSRootPath);
            } else {
                sce.getServletContext().setAttribute("externalTableHDFSRootPath", "/user/ec/kado_external");
            }

            if (prestoDBWhitelist!=null) {
                sce.getServletContext().setAttribute("prestoDBWhitelist", prestoDBWhitelist);
            } else {
                sce.getServletContext().setAttribute("prestoDBWhitelist", new String[]{});
            }



            setCsvlocalPath(csvlocalpath);
            setHivepath(hivepath);
            setDatabase(database);
            setPrestoURL(prestoURL);
            setLogpath(logpath);
            setSiteURLBase(siteURLBase);
            setExpiration(expiration);
            setDeleteLogTTL(deleteLogTTL);
            setPrestoCatalog(prestoCatalog);
            setPresto_user(presto_user);
            setJsonDir(csvlocalPath+fileseparator+tempDir);
            setHdfsHost(hdfsHost);
            setHdfsUser(hdfsUser);
            setHiveJDBC(hiveJDBC);
            setExternalTableHDFSRootPath(externalTableHDFSRootPath);
            setPrestoDBWhitelist(prestoDBWhitelist);
            DBmaintenance dbm=new DBmaintenance();
            dbm.maintain();
            ScheduleMgr smgr=new ScheduleMgr();
            smgr.initSchedule();

        } catch (Exception e) {
            log.error("Init Service Error " + ExceptionUtils.getStackTrace(e));
        }

    }

    public static String getHdfsHost() {
        return hdfsHost;
    }

    public static void setHdfsHost(String hdfsHost) {
        Init.hdfsHost = hdfsHost;
    }

    public static String getExternalTableHDFSRootPath() {
        return externalTableHDFSRootPath;
    }

    public static void setExternalTableHDFSRootPath(String externalTableHDFSRootPath) {
        Init.externalTableHDFSRootPath = externalTableHDFSRootPath;
    }

    public static String getHdfsUser() {
        return hdfsUser;
    }

    public static void setHdfsUser(String hdfsUser) {
        Init.hdfsUser = hdfsUser;
    }

    public static String getHiveJDBC() {
        return hiveJDBC;
    }

    public static void setHiveJDBC(String hiveJDBC) {
        Init.hiveJDBC = hiveJDBC;
    }

    public static String[] getPrestoDBWhitelist() {
        return prestoDBWhitelist;
    }

    public static void setPrestoDBWhitelist(String[] prestoDBWhitelist) {
        Init.prestoDBWhitelist = prestoDBWhitelist;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        Enumeration attrNames = sc.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            sc.removeAttribute(attrNames.nextElement().toString());
        }
    }


}