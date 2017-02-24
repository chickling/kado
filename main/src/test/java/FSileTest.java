import com.chickling.boot.Init;
import com.chickling.models.dfs.FSFile;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;


/**
 *
 * usage
 *
 *  set LocalPath and HDFS Path
 *  1. new FSFile.newInstamce ( FSFile.FSType [ HDFS /LocalFs])
 *  2. call API
 *
 * Created by gl08 on 2015/12/10.
 */
public class FSileTest {


    private void testLocal() throws Exception {
        String filename="joblog-394bcbf9b33440f8a3ff7f9fece95cc3";
        String filepath="E:\\data\\tmp";
        long filesize=0;
        FSFile fsFile=FSFile.newInstance(FSFile.FSType.LocalFs);
        filesize=fsFile.getDirSize(filepath);

        ArrayList<String> fileList= (ArrayList<String>) fsFile.listChildFileNames(filepath);
//        FileSystem fs=fsFile.getFs(filepath);
//        filesize =  fs.getContentSummary(new Path("E:\\data\\tmp")).getLength();
//        fsFile.deleteFile("E:\\data\\tmp\\joblog-9aaf3244381a461ab69765b17a4ed6a4");

        Path  path= new Path(Init.getHivepath());


        int pause=0;



    }
    private void testHDFS() throws IOException {
        String logname="joblog-fd9d5d42e310460498136e0098ac5f7d";
        System.setProperty("HADOOP_USER_NAME","hdfs");
        FSFile fsFile=FSFile.newInstance(FSFile.FSType.HDFS);
        FileSystem fs=fsFile.getFs();
        String logdir="/tmp/presto-joblog/";
        String logdir2="/tmp/presto-log/";
        Path logPath=new Path(logdir);
//        fs.delete(logPath,true);
        if (!fs.exists(logPath))
            fs.mkdirs(logPath);
        if (!fs.exists(new Path(logdir2)))
            fs.mkdirs(new Path(logdir2));
        String path=this.getClass().getResource("/").getPath()+logname;
//        fs.copyFromLocalFile(false,false,new Path(path),new Path(logdir+"joblog-fd9d5d42e310460498136e0098ac5f7d"));

//                fsFile.copyFileLocalToFs(path, logdir +logname);
//        fsFile.copyFileFsToFs(logdir+logname,logdir2+logname);
        fsFile.deleteFile(logdir2+logname);
        int pause=0;

    }

    private void testWriter() throws Exception {
        String logname="joblog-fd9d5d42e310460498136e0098ac5f7d";
         logname="text.txt";
        String logdir="/tmp/presto-joblog/";
         logdir="E:\\data\\tmp\\";

//        FSFile fsFile=FSFile.newInstance(FSFile.FSType.HDFS);
        FSFile fsFile=FSFile.newInstance(FSFile.FSType.LocalFs);

        PrintWriter writer=new PrintWriter(new File("testFile"));
        fsFile.downCsvFile(writer, fsFile.createInputStream(logdir + logname));

    }

    public FSileTest() {
    }

    public static void main(String[] args) throws Exception {
        Init init=new Init();
        Init.setPrestoURL("http://10.16.46.198:8080");
        Init.setDatabase("temp");
        Init.setHivepath("/user/hive/warehouse");
        Init.setLogpath("/tmp/presto-joblog");


//        JobRunner.saveResultAndLogToHDFS("", "joblog-a488d74bdc0a4689ba2dd27c45489d03");

        FSileTest fSileTest=new FSileTest();
//        fSileTest.testLocal();
//        fSileTest.testHDFS();
        fSileTest.testWriter();





    }
}
