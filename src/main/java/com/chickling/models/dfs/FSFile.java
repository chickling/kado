package com.chickling.models.dfs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.*;

/**
 *
 * @author lg22
 *
 */
public abstract class FSFile {

	private static Logger log = LogManager.getLogger(FSFile.class);

	protected static Configuration conf = null;
	private static String HWI_PATH;
	public static final String UDF_SQL_PATH = "./sqls";
	public static final String HWI_QUERY_PATH = "hwi.query.path";
	public static final String HWI_LOCAL_PATH="local.path";
	public final static String HWI_API_DOWN_MAXSIZE="hwi.api.down.maxsize";
	private FileSystem fs = null;
	private static int maxSize = 0;
	private static ThreadLocal<FSFile> threadLocal = new ThreadLocal<FSFile>();

	public enum FSType {
		HDFS, LocalFs;
	}

	static {
		System.setProperty("HADOOP_USER_NAME","hdfs");
		conf = new Configuration(false);
		conf.addResource("core-site.xml");
		conf.addResource("hdfs-site.xml");

		maxSize = conf.getInt(HWI_API_DOWN_MAXSIZE, 5) * 1024 * 1024;
	}

	public static String getHwiPath() {
		return HWI_PATH;
	}

	public static void setHwiPath(String hwiPath) {
		HWI_PATH = hwiPath;
	}

	public FSFile() {
		fs = genFileSysteam();
		if (fs == null) {
			log.error("No Instance FileSystem");
			throw new RuntimeException();
		}
	}
//
//	public static FSFile newInstance() {
//		FSFile fsFile = threadLocal.get();
//		if (fsFile == null) {
//			if (HWI_PATH.toLowerCase().startsWith("hdfs:")) {
//				fsFile = new HDFSFile();
//			} else {
//				fsFile = new LocalDFSFile();
//			}
//			threadLocal.set(fsFile);
//		}
//		return fsFile;
//	}

	public static FSFile newInstance(FSType type) {
		FSFile fsFile = threadLocal.get();
		if (fsFile == null) {
			log.info("FSFile is Null , Create New Connection");
			if (FSType.HDFS == type) {
				HWI_PATH = conf.get(HWI_QUERY_PATH, "/tmp/");

				if (HWI_PATH.lastIndexOf("/") < 0) {
					HWI_PATH += "/";
				}
				fsFile = new HDFSFile();
			} else {
				HWI_PATH = conf.get(HWI_LOCAL_PATH, FSFile.class.getResource("/").toString());
				fsFile = new LocalDFSFile();
			}
			threadLocal.set(fsFile);
		}
//		System.out.println(HWI_PATH);
		return fsFile;
	}

	public boolean existsFile(String fileName) throws IOException {
		Path file = new Path(HWI_PATH, fileName);
		if (fs.exists(file)) {
			return true;
		}
		return false;
	}

	public boolean existsChildFile(String path) throws IOException {
		Path file = new Path(HWI_PATH, path);
		if (fs.exists(file)) {
			FileStatus[] fileLists=fs.listStatus(file);
			if(null!=fileLists&fileLists.length==0){
				return false;
			}else{
				return true;
			}
		}
		return false;
	}



	public void listFile(String hdfsDirPath) throws IOException {
		long length = 0;
		FileStatus fileList[] = fs.listStatus(new Path(hdfsDirPath));
		for (FileStatus status : fileList) {
			length = status.getLen();
			System.out.println(status.getPath().getName() + "\t\t" + length);
		}
	}

//	public Map<String,List> listHistoryFileNames() throws IOException {
//		String filePath = HWI_PATH + "/history";
//		List<String> historyDateNames = listChildFileNames(filePath);
//		Map<String, List> AllFileNames= new HashMap<String, List>();
//		if(null!=historyDateNames){
//			for(int i=0;i<historyDateNames.size();i++){
//				AllFileNames.put(historyDateNames.get(i), listChildFileNames(filePath+"/"+historyDateNames.get(i)));
//			}
//		}
//		return AllFileNames;
//	}

//	public List<String> listUDFJarsFileNames() throws IOException {
//		String filePath = UDF_LIB_PATH;
//		return listChildFileNames(filePath);
//	}
//
//	public List<String> listUDFSqlsFileNames() throws IOException {
//		String filePath = UDF_SQL_PATH;
//		return listChildFileNames(filePath);
//	}

	public boolean deleteFile(String filePath) throws IOException {
		return fs.delete(new Path(filePath), true);
	}

	public List<String> listChildFileNames(String filePath) throws IOException {
		List<String> fileNames = new ArrayList<String>();
		FileStatus fileList[] = fs.listStatus(new Path(filePath));
		for (FileStatus status : fileList) {
			fileNames.add(status.getPath().getName());
		}
		return fileNames;
	}

//	public OutputStream createTempOutputStream(String fileName) throws IOException {
//		return new FileOutputStream(TEMP_PATH + "/" + fileName);
//	}

	public OutputStream createOutputStream(String fileName) throws IOException {
		Path file = new Path(HWI_PATH, fileName);
		if (fs.exists(file)) {
			fs.delete(file, true);
		}
		return fs.create(file, true, 4096, (short) 1, 128L * 1024L * 1024L);
	}

	public InputStream createInputStream(String filePath) throws IOException {
		Path file = new Path(HWI_PATH, filePath);
//		System.out.println(file.toString());
		if (fs.exists(file)) {
			return fs.open(file);
		} else {
			throw new IOException("Not exists file ..." + filePath);
		}
	}
	public InputStream createInputStreamWithAbsoultePath(String filePath) throws IOException {
		Path file = new Path(filePath);
		if (fs.exists(file)) {
//			System.out.println(file.toString());
			return fs.open(file);
		} else {
			throw new IOException("Not exists file ..." + filePath);
		}
	}

	public boolean isOutOfDownloadSize(String filePath) throws IOException {
		Path file = new Path(HWI_PATH, filePath).getParent();
		if (fs.exists(file)) {
			long length = fs.getContentSummary(file).getLength();
			if (length > maxSize) {
				return true;
			} else {
				return false;
			}
		} else {
			throw new IOException("Not exists file ..." + filePath);
		}
	}

	public InputStream createtInputStreamByAbstracPath(String filePath) throws IOException {
		Path file = new Path(filePath);
		if (fs.exists(file)) {
			return fs.open(file);
		} else {
			throw new IOException("Not exists file ..." + filePath);
		}
	}

	/**
	 * Get DIR total Size
	 * @param dirPath
	 * @return
	 * @throws IOException
	 */
	public  long getDirSize(String dirPath) throws IOException {
		Path file = new Path(HWI_PATH, dirPath);
		if (fs.exists(file)) {
			return fs.getContentSummary(file).getLength();
		}
		return 0;
	}
	public long getFileLength(String filePath) throws IOException {
		Path file = new Path(HWI_PATH, filePath);
		if (fs.exists(file)) {
			return fs.getFileStatus(file).getLen();
		}
		return 0;
	}
//
//	public long getTempFileLength(String filePath) throws IOException {
//		File tmpFile = new File(TEMP_PATH + "/" + filePath);
//		if (tmpFile.exists()) {
//			return tmpFile.length();
//		}
//		return 0;
//	}

	public List<String> readFile(String filePath) {
		String line = "";
		BufferedReader br = null;
		List<String> lists = new ArrayList<String>();
		try {
			InputStream in = createInputStream(filePath);
			if (in != null) {
				br = new BufferedReader(new InputStreamReader(in));
				while ((line = br.readLine()) != null) {
					lists.add(line);
				}
			}
		} catch (Exception e) {
			log.error(e);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				log.error(e);
			}
		}
		return lists;
	}

	public void writeFile(String filePath, List<String> lists) {
		OutputStream out = null;
		try {
			out = createOutputStream(filePath);
			for (String line : lists) {
				out.write((line + "\n").getBytes());
			}
		} catch (Exception e) {
			log.error(e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				log.error(e);
			}
		}
	}

//	public InputStream getSQLInputStream(String fileName) throws IOException {
//		return createInputStreamWithAbsoultePath(UDF_SQL_PATH + "/" + fileName);
//	}


//	public void uploadFileToHDFS(String srcFile, String hdfsDestFile, ProgressDTO progress) {
//		InputStream in = null;
//		OutputStream out = null;
//		File localFile = null;
//		byte buf[];
//		buf = new byte[4096];
//		long totalBytes = 0L;
//		long uploadBytes = 0L;
//		try {
//			String localSrcFile = TEMP_PATH + "/" + srcFile;
//			localFile = new File(localSrcFile);
//			if (localFile.exists()) {
//				totalBytes = localFile.length();
//				in = new FileInputStream(localFile);
//				out = createOutputStream(hdfsDestFile);
//				progress.setFileName(srcFile);
//				for (int bytesRead = in.read(buf); bytesRead >= 0; bytesRead = in.read(buf)) {
//					uploadBytes += bytesRead;
//					out.write(buf, 0, bytesRead);
//					progress.setUploadProgress(Math.round((uploadBytes * 1.0 / totalBytes) * 100));
//				}
//			}
//		} catch (IOException ioex) {
//			log.error(ioex);
//		} finally {
//			try {
//				if (out != null)
//					out.close();
//				if (in != null)
//					in.close();
//				if (localFile != null) {
//					localFile.delete();
//				}
//			} catch (Exception ioex) {
//				log.error(ioex);
//			}
//		}
//	}

//	public boolean removePath(String sessionName) throws IOException {
//		boolean resultFlag = false;
//		Path path = new Path(conf.get("hwi.query.path", "/tmp/"), sessionName);
//		if (fs.exists(path)) {
//			resultFlag = fs.delete(path, true);
//		}
//		return resultFlag;
//	}

	public void downCsvFile(PrintWriter pw, InputStream fis) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		writeCSVFormat(pw, br);
	}

	public void downXMLFile(PrintWriter pw, InputStream fis) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		writeXMLFormat(pw, br);
	}

	public void downJSONFile(PrintWriter pw, InputStream fis) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		writeJSONFormat(pw, br);
	}

	public void downTxtFile(PrintWriter pw, InputStream fis) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		writeTxtFormat(pw, br);
	}

	public void writeCSVFormat(PrintWriter pw, BufferedReader br) throws IOException {
		String line;
		String head = br.readLine();
		if (StringUtils.isNotEmpty(head)) {
			pw.println(head);
		}
		while ((line = br.readLine()) != null) {
			line = "\"" + line.replaceAll("\'", "\\\\'").replaceAll("\"", "\'").replaceAll("\t|\001", "\",\"") + "\"";
			pw.println(line);
		}
		pw.flush();
	}

	public void writeTxtFormat(PrintWriter pw, BufferedReader br) throws IOException {
		String line;
		while ((line = br.readLine()) != null) {
			pw.println(line);
		}
		pw.flush();
	}

	public static void writeJSONFormat(PrintWriter pw, BufferedReader br) throws IOException {
		JsonObject jsonRoot=new JsonObject();
//		JSONObject jsonRoot = new JSONObject();
		JsonArray jsonRows=new JsonArray();
//		JSONArray jsonRows = new JSONArray();


		String line = "";
		String head = "";
		head = br.readLine();
		if (StringUtils.isNotEmpty(head)) {
			jsonRoot.addProperty("columns", head);
			while ((line = br.readLine()) != null) {
				String results[] = line.split("\t|\001");
				int cindex = 0;

				JsonObject jsonColumn = new JsonObject();
				for (String result : results) {
					jsonColumn.addProperty("c" + cindex++, result);
				}
				jsonRows.add(jsonColumn);
			}
			jsonRoot.add("rows", jsonRows);

			pw.write(jsonRoot.toString());
			pw.flush();
		}
	}

	public void writeXMLFormat(PrintWriter pw, BufferedReader br) throws IOException {
		String line = "";
		String head = "";
		head = br.readLine();
		if (StringUtils.isNotEmpty(head)) {
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement("root");
			Element columns = root.addElement("columns");
			columns.addText(head);
			while ((line = br.readLine()) != null) {
				String results[] = line.split("\t|\001");
				Element rows = root.addElement("rows");
				Element row = rows.addElement("row");
				int cindex = 0;
				for (int i = 0; i < results.length; i++) {
					Element c = row.addElement("c" + cindex++);
					c.addText(results[i]);
				}
			}

			OutputFormat xmlFormat = OutputFormat.createPrettyPrint();
			xmlFormat.setEncoding("utf-8");
			XMLWriter writer = new XMLWriter(pw, xmlFormat);
			writer.write(document);
			pw.flush();
			writer.close();
		}
	}

//	public void clearTmpFiles() {
//		File tmpPath = new File(TEMP_PATH);
//		for (File f : tmpPath.listFiles()) {
//			f.delete();
//		}
//	}

	public FileSystem getFs() {
		return fs;
	}

	public static void close() throws IOException {
		FSFile fsFile = threadLocal.get();
		if (fsFile != null) {
			if (fsFile.getFs() != null) {
				fsFile.getFs().close();
			}
			threadLocal.remove();
		}
	}

	/**
	 * copy local to local
	 * @param src
	 * @param dst
	 * @throws IOException
	 */
	public void copyFileFsToFs(String src, String dst) throws IOException {
		FileUtil.copy(fs, new Path(HWI_PATH, src), fs, new Path(HWI_PATH, dst), Boolean.FALSE, conf);
	}
	/**
	 *  Copy Local File TO HDFS
	 * @param src
	 * @param dst
	 * @throws IOException
	 */
	public void copyFileLocalToFs(String src, String dst) throws IOException {
		log.info("Copy Local to HDFS is [ " + FileUtil.copy(new File(src), fs, new Path(dst), false, conf) + " ]");
	}

	/**
	 *  MOVE HDFS  File TO Local
	 * @param src
	 * @param dst
	 * @throws IOException
	 */
	public void moveFSFileToLocal(String src, String dst) throws IOException {
		log.info("Move HDFS  to Local is [ " + FileUtil.copy(fs,new Path(src),new File(dst),true, conf) + " ]");
	}

//
//	public void copyUDFFileToBackup(String src) throws IOException {
//		String dst=FSFile.UDF_BACKUP+"/"+src+"-"+ System.currentTimeMillis();
//		FileUtil.copy(fs, new Path(src), fs, new Path(HWI_PATH, dst), Boolean.FALSE, conf);
//	}
//
//	public void moveUDFFileToBackup(String src) throws IOException {
//		String dst=FSFile.UDF_BACKUP+"/"+src+"-"+ System.currentTimeMillis();
//		FileUtil.copy(fs, new Path(src), fs, new Path(HWI_PATH, dst), Boolean.TRUE, conf);
//	}
//*


//	public void copyLocalToBackup(String src, String dst) throws IOException {
//		FileUtil.copy(new File(src), fs, new Path(dst), false, conf);
//	}

//	public void delHistoryFile(String dataKey,String fileName) throws IOException {
//		fs.delete(new Path(HWI_PATH + "/" + HISTORY_DATA +"/"+dataKey+ "/" + fileName), true);
//		if(!existsChildFile(HWI_PATH + "/" + HISTORY_DATA +"/"+dataKey)){
//			fs.delete(new Path(HWI_PATH + "/" + HISTORY_DATA +"/"+dataKey), true);
//		}
//	}

	public abstract FileSystem genFileSysteam();

//	public void listFiles() {
//		try {
//			fs.listStatus(new Path(HWI_PATH));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	public static void main(String[] args) throws IOException {
		String newline=System.lineSeparator();
		String columns="col1\001col2\001col3";
		StringBuilder values=new StringBuilder();
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		baos.write(columns.getBytes());
		baos.write(newline.getBytes());
		values.append("0").append("\001").append("1").append("\001").append("3");
		baos.write(values.toString().getBytes());
		baos.write(newline.getBytes());
		values=new StringBuilder();
		values.append("4").append("\001").append("5").append("\001").append("6");
		baos.write(values.toString().getBytes());
		baos.write(newline.getBytes());
		values=new StringBuilder();
		values.append("7").append("\001").append("8").append("\001").append("9");
		baos.write(values.toString().getBytes());
		baos.write(newline.getBytes());
		values=new StringBuilder();
		values.append("10").append("\001").append("11").append("\001").append("12");
		baos.write(values.toString().getBytes());
		baos.write(newline.getBytes());
		ByteArrayInputStream bais=new ByteArrayInputStream(baos.toByteArray());
		InputStreamReader isReader=new InputStreamReader(bais);
		BufferedReader br=new BufferedReader(isReader);

		PrintWriter pw=new PrintWriter(new File("json"));

		writeJSONFormat(pw, br);
//		FSFile.newInstance().listUDFJarsFileNames();
	}
}
