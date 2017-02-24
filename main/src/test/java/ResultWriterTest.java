//import com.chickling.boot.Init;
//
//import com.chickling.models.dfs.FSFile;
//import com.chickling.models.job.bean.JobLog;
//import com.chickling.models.writer.HdfsWriter;
//import com.chickling.models.writer.LocalWriter;
//import com.chickling.models.writer.ResultWriter;
//import org.junit.Before;
//import org.junit.Test;
//import org.yaml.snakeyaml.Yaml;
//
//import java.io.File;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//
///**
// * Created by gl08 on 2016/9/26.
// */
//public class ResultWriterTest {
//    private JobLog jobLog;
//    private HashMap<String,Object> parameter;
//
//    @Before
//    public void init(){
//
//        Init.setCsvtmphdfsPath("/tmp/presto-csvtemp");
//        ArrayList<String> locationList=new ArrayList<>();
//        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
//        InputStream input = classloader.getResourceAsStream("dbselect-config.yaml");
//        Yaml yaml = new Yaml();
//
//        Init.setImportBatchSize(1000);
//        Init.setLocationList(locationList);
//        Init.setCsvlocalPath("/home/hadoop/gary");
//
//
//        String csvName="test_orc";
//        String csvHDFSPath="/tmp/gl08/output/";
//
//        jobLog=new JobLog();
//        jobLog.setFilepath(csvHDFSPath);
//        jobLog.setFilename(csvName);
//        jobLog.setJoboutput("/tmp/gl08/input");
//
//        int location_id=1;
//        String insertsql="aW5zZXJ0IGludG8gRWNvbW1lcmNlLmRiby5FQ19DcmF3bGVyTGlzdCAoQ29udGVudCx1dG1hLFRvdGFsQ2xpY2tzLE1lbW8sW1R5cGVdLFtMZXZlbF0sW0RvbWFpbl0sW1Jlc1N0cjFdKSAjeyckMSQnLG51bGwsJDIkLCdUb3BfT3ZlcmFsbF9QYWdlX0RhaWx5X1JlY2FwdGNoYScsJ0knLDUsJ1dXV1NTTCcsJ0FMTCd9Ow==";
//        int resultCount=11;
//
//        parameter=new HashMap<>();
//        parameter.put("jobLog",jobLog);
//        parameter.put("location_id",location_id);
//        parameter.put("insertsql",insertsql);
//        parameter.put("resultCount",resultCount);
//
//
//    }
//
//    private Integer doWriter(ResultWriter resultWriter) throws Exception {
//        ExecutorService executor= Executors.newSingleThreadExecutor();
//        Future future=executor.submit(resultWriter);
//        Thread.currentThread().sleep(2000);
//        return (Integer) future.get();
//    }
//
//
//    @Test
//    public void testAll(){
//        ExecutorService service= Executors.newFixedThreadPool(3);
//
//        HdfsWriter hdfsWriter =new HdfsWriter();
//        hdfsWriter.init(jobLog);
//
//        LocalWriter localWriter=new LocalWriter();
//        localWriter.init(jobLog);
//
//        List<Future> futureList=new ArrayList<>();
//        futureList.add(service.submit(hdfsWriter));
////        futureList.add(service.submit(localWriter));
//
//        int result=0;
//
//        for (Future future : futureList){
//            try {
//                result+=(int)future.get();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//        }
//            System.out.println("Result code is "+result);
//        assert result==5;
//    }
//
//
//
//    @Test
//    public void testHDFSWriter() throws Exception {
//
//        // init Path
//        //
//
//        String csvResultPath="/tmp/gl08/output/";
//        jobLog.setFilepath(csvResultPath);
//
//        //clean up output file
//        //
//        FSFile fsFile= FSFile.newInstance(FSFile.FSType.HDFS);
//        fsFile.deleteFile(csvResultPath);
//
//        //start HDFS Writer with csv
//        //
//        ResultWriter resultWriter=new HdfsWriter();
//        resultWriter.init(jobLog);
//       int result= doWriter(resultWriter);
//
//        // if output Path file size > 0 , assert Success
//        //
//        assert result==1;
//        assert (fsFile.listChildFileNames(csvResultPath).size()>0);
//
//    }
//
//
//
//    @Test
//    public  void testLocalWriter() throws Exception {
//
//        //clean up output file
//        //
//        File file=new File(Init.getCsvlocalPath());
//        if (file.exists()){
//            for (File childFile: file.listFiles()){
//               childFile.delete();
//            }
//            file.delete();
//        }
//
//        file.mkdirs();
//
//        //start Local Writer with csv
//        //
//        ResultWriter resultWriter=new LocalWriter();
//        resultWriter.init(jobLog);
//        resultWriter.call();
//        int result=doWriter(resultWriter);
//
//        assert result==2;
////        doWriter(resultWriter);
//
//    }
//
//
//
//
//
//
//}
