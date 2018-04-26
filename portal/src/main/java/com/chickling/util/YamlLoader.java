package com.chickling.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * load ItemService.yaml and initialize. 
 *
 * @author ay21
 * @date 2015-01-21
 */
public class YamlLoader {
	public static final Logger logger = LogManager.getLogger(YamlLoader.class);
	public static YamlLoader instance;
	private  String prestoURL="";
	private  String database="";
	private  String hivepath="";
	private  String logpath="";
	private  String siteURLBase="";
	private  String expiration="";
	private  String prestoCatalog="";
	private  String csvlocalPath="";
	private  String deleteLogTTL="";
	private 	 String scheduleLogDir="";
	private String wrtierinjection="";
	private String downloadToken="";
	private Integer downloadLimit=20000;
	private String presto_hdfs_user="";
	private  String hdfsHost="";
	private  String externalTableHDFSRootPath="";
	private  String hdfsUser="";
	private  String hiveJDBC="";
	private String[] prestoDBWhitelist=new String[]{};

	static {
		loadYaml();
	}

	private static void loadYaml() {
		String yamlPath =YamlLoader.class.getResource("/").getPath()+"config.yaml";
		try {
			logger.info("Loading configuration from " + yamlPath);
			InputStream input;
			try{
				input = new FileInputStream(yamlPath);
			}catch (FileNotFoundException e){
				logger.error("Ymal file not found at " + yamlPath);
				throw new AssertionError(e);
			}
			Constructor constructor = new Constructor(YamlLoader.class);
			Yaml yaml = new Yaml(constructor);
			instance = (YamlLoader) yaml.load(input);
		} catch (YAMLException e) {
			logger.error("Invalid yaml; unable to start. See log for stacktrace.", e);
			System.exit(1);
		}
	}

	public String getPrestoURL() {
		return prestoURL;
	}

	public void setPrestoURL(String prestoURL) {
		this.prestoURL = prestoURL;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getHivepath() {
		return hivepath;
	}

	public void setHivepath(String hivepath) {
		this.hivepath = hivepath;
	}

	public String getLogpath() {
		return logpath;
	}

	public void setLogpath(String logpath) {
		this.logpath = logpath;
	}


	public String getPrestoCatalog() {
		return prestoCatalog;
	}

	public void setPrestoCatalog(String prestoCatalog) {
		this.prestoCatalog = prestoCatalog;
	}

	public String getCsvlocalPath() {
		return csvlocalPath;
	}

	public void setCsvlocalPath(String csvlocalPath) {
		this.csvlocalPath = csvlocalPath;
	}

	public String getDeleteLogTTL() {
		return deleteLogTTL;
	}

	public void setDeleteLogTTL(String deleteLogTTL) {
		this.deleteLogTTL = deleteLogTTL;
	}

	public String getSiteURLBase() {
		return siteURLBase;
	}

	public void setSiteURLBase(String siteURLBase) {
		this.siteURLBase = siteURLBase;
	}

	public String getExpiration() {
		return expiration;
	}

	public void setExpiration(String expiration) {
		this.expiration = expiration;
	}


	public String getScheduleLogDir() {
		return scheduleLogDir;
	}

	public void setScheduleLogDir(String scheduleLogDir) {
		this.scheduleLogDir = scheduleLogDir;
	}



	public String getWrtierinjection() {
		return wrtierinjection;
	}

	public void setWrtierinjection(String wrtierinjection) {
		this.wrtierinjection = wrtierinjection;
	}

	public String getDownloadToken() {
		return downloadToken;
	}

	public void setDownloadToken(String downloadToken) {
		this.downloadToken = downloadToken;
	}

	public Integer getDownloadLimit() {
		return downloadLimit;
	}

	public void setDownloadLimit(Integer downloadLimit) {
		this.downloadLimit = downloadLimit;
	}

	public String getPresto_hdfs_user() {
		return presto_hdfs_user;
	}

	public void setPresto_hdfs_user(String presto_hdfs_user) {
		this.presto_hdfs_user = presto_hdfs_user;
	}

	public String getHdfsHost() {
		return hdfsHost;
	}

	public void setHdfsHost(String hdfsHost) {
		this.hdfsHost = hdfsHost;
	}

	public String getExternalTableHDFSRootPath() {
		return externalTableHDFSRootPath;
	}

	public void setExternalTableHDFSRootPath(String externalTableHDFSRootPath) {
		this.externalTableHDFSRootPath = externalTableHDFSRootPath;
	}

	public String getHdfsUser() {
		return hdfsUser;
	}

	public void setHdfsUser(String hdfsUser) {
		this.hdfsUser = hdfsUser;
	}

	public String getHiveJDBC() {
		return hiveJDBC;
	}

	public void setHiveJDBC(String hiveJDBC) {
		this.hiveJDBC = hiveJDBC;
	}

	public String[] getPrestoDBWhitelist() {
		return prestoDBWhitelist;
	}

	public void setPrestoDBWhitelist(String[] prestoDBWhitelist) {
		this.prestoDBWhitelist = prestoDBWhitelist;
	}

	@Override
	public String toString() {
		return "YamlLoader{" +
				"prestoURL=" + prestoURL +
				", Hive Temp Table Database=" + database +
				", Hive Table HDFS Path=" + hivepath +
				", JobLog HDFS Path=" + logpath +
				", SiteURLBase=" + siteURLBase +
				", Expiration=" + expiration +
				", Presto Catalog=" + prestoCatalog +
				", CSV Local Path=" + csvlocalPath +
				", Delete Log TTL=" + deleteLogTTL +
				", Scheduled  Log Dir=" + scheduleLogDir +
				'}';
	}
}