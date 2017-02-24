package com.chickling.models.dfs;

import com.facebook.presto.hive.orc.HdfsOrcDataSource;
import com.facebook.presto.orc.*;
import com.facebook.presto.orc.memory.AggregatedMemoryContext;
import com.facebook.presto.orc.metadata.OrcMetadataReader;
import com.facebook.presto.orc.metadata.OrcType;
import com.facebook.presto.spi.block.*;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.testing.TestingConnectorSession;
import com.google.common.collect.ImmutableMap;
import com.chickling.util.TimeUtil;
import io.airlift.units.DataSize;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTimeZone;

import static com.facebook.presto.spi.type.VarcharType.VARCHAR;
import static com.facebook.presto.spi.type.BigintType.BIGINT;
import static com.facebook.presto.spi.type.DoubleType.DOUBLE;
import static com.facebook.presto.spi.type.TimestampType.TIMESTAMP;
import static com.facebook.presto.spi.type.BooleanType.BOOLEAN;
import static com.facebook.presto.spi.type.DateType.DATE;
import static io.airlift.units.DataSize.Unit.MEGABYTE;

import java.io.*;
import java.util.*;

/**
 *  use Presto Programmer Dain Sample to Read Orc File
 *  https://gist.github.com/dain/e931a43b3463136fd7bf
 * Created by gl08 on 2015/12/14.
 */
public  class OrcFileUtil {

    private static Logger log= LogManager.getLogger(OrcFileUtil.class);
    protected static Configuration conf;
    private static String ORC_PATH;
    public static final String ORC_HDFS_PATH = "hwi.query.path";
    private static ThreadLocal<OrcFileUtil> threadLocal=new ThreadLocal<>();
    private  int realRow=0;
    private int rowcount=0;
    private String  newline=System.getProperty("line.separator");

    public int getRowcount() {
        return rowcount;
    }

    public void setRowcount(int rowcount) {
        this.rowcount = rowcount;
    }

    public int getRealRow() {
        return realRow;
    }

    public void setRealRow(int realRow) {
        this.realRow = realRow;
    }

    public void addRealRow(int count){
        this.realRow=this.realRow+count;
    }

    public enum TYPE{
        HDFS,FILE
    }
    static {
        System.setProperty("HADOOP_USER_NAME","hdfs");
        conf = new Configuration();
    }

    public static String getOrcPath() {
        return ORC_PATH;
    }

    public static void setOrcPath(String orcPath) {
        ORC_PATH = orcPath;
    }

    public OrcFileUtil() {
    }

    public static OrcFileUtil newInstance  (){
        OrcFileUtil orcFileUtil =threadLocal.get();
        if (orcFileUtil == null) {
            orcFileUtil =new OrcFileUtil();
            threadLocal.set(orcFileUtil);
        }
        return orcFileUtil;
    }



    public boolean writeORCFilestoCSV(String sourceDir , String outputDir , TYPE type , FSFile.FSType fstype)  {
        FSFile fsFile=FSFile.newInstance(fstype);
        int count=0;
        String currentTime= TimeUtil.getSaveHDFSTime();
        try {
            for (String filepath:fsFile.listChildFileNames(sourceDir)){
                writeORCtoCSV(sourceDir+filepath,type,outputDir,currentTime,count);
                count++;
            }
            return true;
        } catch (IOException e) {
            log.error(e);
            return false;
        }
    }
    public String writeORCFilestoCSVLocal(String sourceDir , String outputDir , TYPE type , FSFile.FSType fstype)  {
        FSFile fsFile=FSFile.newInstance(fstype);
        int count=0;
        String currentTime= TimeUtil.getSaveHDFSTime();
        String csvresultPAth="";
        try {
            for (String filepath:fsFile.listChildFileNames(sourceDir)){
                csvresultPAth=writeORCtoCSV(sourceDir+filepath,type,outputDir,currentTime,count);
                count++;
            }
            return csvresultPAth;
        } catch (IOException e) {
            log.error(e);
            return csvresultPAth;
        }
    }

    public String downloadORCFilestoCSV(String sourceDir , String outputDir , TYPE type)  {
        FSFile fsFile=FSFile.newInstance(FSFile.FSType.HDFS);
        int count=0;
        String currentTime= TimeUtil.getSaveHDFSTime();
        try {
            //cleaexin exist Dir
            fsFile.deleteFile(outputDir);
            for (String filepath:fsFile.listChildFileNames(sourceDir)){
                writeORCtoCSV(sourceDir+filepath,type,outputDir,currentTime,count);
                count++;
            }
            return fsFile.listChildFileNames(outputDir).get(0);
        } catch (IOException e) {
            log.error(e);
            return "";
        }
    }
    /**
     * @param DirPath               OrcFile Location
     * @param type                    Location is LOCAL or HDFS
     * @param startRow             read start row
     * @param rowCount           how much rows get
     * @return                             InputStream
     */
    public ByteArrayInputStream readORCFiles(String DirPath,TYPE type , int startRow,int rowCount)   {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        FSFile fsFile=FSFile.newInstance(FSFile.FSType.HDFS);
        int start=startRow;
        setRealRow(startRow);
        setRowcount(rowCount);
        try {
            for (String filepath:fsFile.listChildFileNames(DirPath)){
                if (this.getRowcount() > 0){
                    if (rowCount!=this.getRowcount())
                        start= readORC(DirPath+"/"+ filepath, type, start,this.getRowcount(), false,baos);
                    else
                        start= readORC(DirPath+"/"+ filepath, type, start, this.getRowcount(), true,baos);
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
//        FSFile.close();
        if(baos.size()==0)
            return new ByteArrayInputStream(new byte[]{});
        else
            return  new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     *
     * @param filepath          Source File Path
     * @param type               Source is File  ( @ TYPE.FILE  )or HDFS path ( @ TYPE.HDFS )
     * @param outPutPath    Writer File Path , is DirPath ,
     * @param index             if is zero , will add Header ( Column Name )
     */
    public String writeORCtoCSV(String filepath, TYPE type ,String outPutPath ,String fileName,int index) {
        FSFile fsFile=FSFile.newInstance(FSFile.FSType.HDFS);
        long fileSzie= 0;
        OrcDataSource orcDataSource=null;
        OrcReader orcReader=null;

        try {
            //get This File Size
            fileSzie = fsFile.getFileLength(filepath);

            if (TYPE.FILE.equals(type)){
                File file=new File(filepath);
                orcDataSource=new FileOrcDataSource(file,new DataSize(8, MEGABYTE),new DataSize(8, MEGABYTE),new DataSize(8, MEGABYTE));
            }else {
                FSDataInputStream inputStream=new FSDataInputStream(fsFile.createInputStream(filepath));
                orcDataSource = new HdfsOrcDataSource("data", fileSzie, new DataSize(8, MEGABYTE), new DataSize(8, MEGABYTE), new DataSize(8, MEGABYTE), inputStream);
            }
            orcReader=new OrcReader(orcDataSource,new OrcMetadataReader(),new DataSize(8,MEGABYTE),new DataSize(8,MEGABYTE));

        } catch (IOException e) {
            log.error(e);
        }

        assert orcReader!=null;

        List<OrcType> types= orcReader.getFooter().getTypes();

        Map<Integer, Type> columnTypes =null;
//            types.get(0).getFieldNames()

        LinkedHashMap <String , String > cols=new LinkedHashMap<>();
        LinkedHashMap <Integer , Type > colstype=new LinkedHashMap<>();

        for(int i =0 ; i< types.get(0).getFieldCount() ; i++){
            OrcType tmp=types.get(i+1);
            switch (tmp.getOrcTypeKind()){
                case STRING:
                    cols.put(types.get(0).getFieldName(i),VARCHAR.getDisplayName());
                    colstype.put(i,VARCHAR);
                    break;
                case LONG:
                    cols.put(types.get(0).getFieldName(i),BIGINT.getDisplayName());
                    colstype.put(i,BIGINT);
                    break;
                case DOUBLE:
                    cols.put(types.get(0).getFieldName(i),DOUBLE.getDisplayName());
                    colstype.put(i,DOUBLE);
                    break;
                case DATE:
                    cols.put(types.get(0).getFieldName(i),DATE.getDisplayName());
                    colstype.put(i,DATE);
                    break;
                case BOOLEAN:
                    cols.put(types.get(0).getFieldName(i),BOOLEAN.getDisplayName());
                    colstype.put(i,BOOLEAN);
                    break;
                case TIMESTAMP:
                    cols.put(types.get(0).getFieldName(i),TIMESTAMP.getDisplayName());
                    colstype.put(i,TIMESTAMP);
                    break;
                default:
                    break;
            }
        }

        columnTypes=ImmutableMap.<Integer, Type>builder().putAll(colstype).build();

        OrcRecordReader recordReader= null;

        String  resultFinalPath="";
        try {
            recordReader = orcReader.createRecordReader(columnTypes, OrcPredicate.TRUE, DateTimeZone.getDefault(),new AggregatedMemoryContext());
            String dirPath=outPutPath.substring(0,outPutPath.lastIndexOf("/"));
            if(!fsFile.getFs().exists(new Path(dirPath)))
                fsFile.getFs().mkdirs(new Path(dirPath));
            OutputStream out=null;
            resultFinalPath=outPutPath + "@"+fileName+".csv";
            if (index==0){
                out=fsFile.createOutputStream(resultFinalPath);
                StringBuilder sb=new StringBuilder();
                int keycount=0;
                for (String key : cols.keySet()){

                    sb.append(key);
                    if (keycount!=cols.keySet().size()-1)
                        sb.append("\001");
                    keycount++;
                }
                out.write(("\"" + sb.toString().replaceAll("\'", "\\\\'").replaceAll("\"", "\'").replaceAll("\t|\001", "\",\"") + "\"").getBytes());
                out.write(newline.getBytes());
            }else
                out=fsFile.getFs().append(new Path(resultFinalPath),4096);

            while (true){
                int  tmpsize=writerBatch(columnTypes, recordReader,out);
                if (tmpsize==-1)
                    break;
            }
            out.close();
            recordReader.close();
        } catch (IOException e) {
            log.error(e);
        }
        finally {
            return resultFinalPath;
        }
    }

    /**
     * @param filepath              source file Path
     * @param type                   source is Local FILE or HDFS File
     * @param startRow            start Row
     * @param rowNumber       needs Row Count
     * @param baos                  get Result with ByteArrayOutputStream
     * @return                            next File needs Row Count (still need how many  rows  for jump to target startrow )
     */
    public int readORC(String filepath ,TYPE type , int startRow , int rowNumber , boolean firstPage, ByteArrayOutputStream baos)   {
        FSFile fsFile=FSFile.newInstance(FSFile.FSType.HDFS);
        long fileSzie= 0;
        OrcReader orcReader= null;
        OrcDataSource orcDataSource=null;

        try {
            fileSzie = fsFile.getFileLength(filepath);
            if (TYPE.FILE.equals(type)){
                File file=new File(filepath);
                orcDataSource=new FileOrcDataSource(file,new DataSize(8, MEGABYTE),new DataSize(8, MEGABYTE),new DataSize(8, MEGABYTE));
            }else {
                FSDataInputStream inputStream=new FSDataInputStream(fsFile.createInputStream(filepath));
                orcDataSource = new HdfsOrcDataSource("data", fileSzie, new DataSize(1, MEGABYTE), new DataSize(8, MEGABYTE), new DataSize(8, MEGABYTE), inputStream);
            }

            orcReader = new OrcReader(orcDataSource,new OrcMetadataReader(),new DataSize(8,MEGABYTE),new DataSize(8,MEGABYTE));
        } catch (IOException e) {
            log.error(e);
        }
        assert orcReader != null;
        List<OrcType> types= orcReader.getFooter().getTypes();

        Map<Integer, Type> columnTypes =null;
//            types.get(0).getFieldNames()

        LinkedHashMap <String , String > cols=new LinkedHashMap<>();
        LinkedHashMap <Integer , Type > colstype=new LinkedHashMap<>();

        for(int i =0 ; i< types.get(0).getFieldCount() ; i++){
            OrcType tmp=types.get(i+1);
            switch (tmp.getOrcTypeKind()){
                case STRING:
                    cols.put(types.get(0).getFieldName(i),VARCHAR.getDisplayName());
                    colstype.put(i,VARCHAR);
                    break;
                case LONG:
                    cols.put(types.get(0).getFieldName(i),BIGINT.getDisplayName());
                    colstype.put(i,BIGINT);
                    break;
                case DOUBLE:
                    cols.put(types.get(0).getFieldName(i),DOUBLE.getDisplayName());
                    colstype.put(i,DOUBLE);
                    break;
                case DATE:
                    cols.put(types.get(0).getFieldName(i),DATE.getDisplayName());
                    colstype.put(i,DATE);
                    break;
                case BOOLEAN:
                    cols.put(types.get(0).getFieldName(i),BOOLEAN.getDisplayName());
                    colstype.put(i,BOOLEAN);
                    break;
                case TIMESTAMP:
                    cols.put(types.get(0).getFieldName(i),TIMESTAMP.getDisplayName());
                    colstype.put(i,TIMESTAMP);
                    break;
                default:
                    break;
            }
        }

        columnTypes=ImmutableMap.<Integer, Type>builder().putAll(colstype).build();

        OrcRecordReader recordReader= null;
        try {
            recordReader = orcReader.createRecordReader(columnTypes, OrcPredicate.TRUE, DateTimeZone.getDefault(), new AggregatedMemoryContext() {
                @Override
                protected void updateBytes(long bytes) {

                }
            });
        } catch (IOException e) {
            log.error(e);
        }

        assert recordReader != null;
        if ( startRow-recordReader.getFileRowCount()>0){
            return (int) (startRow-recordReader.getFileRowCount());
        }
        //  this file has enough row !!
        if (firstPage) {
            // add Columns
            Iterator itera=cols.keySet().iterator();
            StringBuilder colsb=new StringBuilder();
            colsb.append("#").append("\001");
            while (true){
                String col= (String) itera.next();
                colsb.append(col).append("\001");
                if (!itera.hasNext())
                    break;
            }
            try {
                baos.write(colsb.toString().getBytes());
                baos.write(newline.getBytes());
            } catch (IOException e) {
                log.error(e);
            }
        }


        while (true){
            int  tmpsize=readBatch(columnTypes,recordReader,baos,startRow ,rowNumber);
            if (tmpsize==-1){
                //  readBatch has no Data ,  will return -1
                break;
            }
            if (tmpsize==-2){
                //  readBatch  read data enough ( need Rows == 0 ) ,  will return -2
                rowNumber=0;
                break;
            }
            if (tmpsize>0){
                // readBatch read next batch
                startRow=0;
                rowNumber-=tmpsize;
            }
            else {
                // this batch still small than startRow
                startRow -= OrcReader.MAX_BATCH_SIZE;
            }

        }
        setRowcount(rowNumber);

        try {
            recordReader.close();
        } catch (IOException e) {
            log.error(e);
        }
        return  0;
    }

    private int writerBatch(Map<Integer, Type> columnTypes, OrcRecordReader recordReader , OutputStream out ) throws IOException {

        ArrayList<ArrayList> result=new ArrayList<>();
        int batchSize = recordReader.nextBatch();
        if (batchSize<0)
            return batchSize;

        for (Map.Entry<Integer, Type> entry : columnTypes.entrySet()) {
            Block  block =   recordReader.readBlock(entry.getValue(), entry.getKey());
            ArrayList<Object> colList=new ArrayList<>();
            for (int i =0 ;i<block.getPositionCount() ; i++){
                Object col = entry.getValue().getObjectValue(TestingConnectorSession.SESSION, block, i);
                colList.add(col);
            }
            result.add(colList);
        }

        for ( int i = 0 ; i< result.get(0).size() ;i++){
            StringBuilder datarow=new StringBuilder();
            for (int j = 0 ; j< result.size() ; j++){
                datarow.append(result.get(j).get(i));
                if (j!=result.size()-1)
                    datarow.append("\001");
            }
            out.write(("\"" + datarow.toString().replaceAll("\'", "\\\\'").replaceAll("\"", "\'").replaceAll("\t|\001", "\",\"") + "\"").getBytes());
            out.write(newline.getBytes());
        }
        out.flush();
        return batchSize;
    }

    /**
     * @param columnTypes
     * @param recordReader
     * @param baos                              outputstream
     * @param start                               start row number
     * @param rownumber                    needs row
     * @return                                        -1 : no data , -2 : enough row , else :  this batch reads row count
     */
    private int readBatch(Map<Integer, Type> columnTypes, OrcRecordReader recordReader ,ByteArrayOutputStream baos  ,int start ,int rownumber)
    {

        Block block=null;
        ArrayList<ArrayList> result=new ArrayList<>();

        int batchSize = 0;
        try {
            batchSize = recordReader.nextBatch();

            // if has no next data ,   will get -1 batchSize
            if (batchSize<0)
                return batchSize;
            for (Map.Entry<Integer, Type> entry : columnTypes.entrySet()) {
                block =   recordReader.readBlock(entry.getValue(), entry.getKey());
                ArrayList<Object> colList=new ArrayList<>();
                int skipload=0;
                for (int i =0 ;i<block.getPositionCount() ; i++){
                    if(skipload<start)
                        skipload++;
                    else {
                        Object col = entry.getValue().getObjectValue(TestingConnectorSession.SESSION, block, i);
                        colList.add(col);
                    }
                }
                result.add(colList);
            }
        } catch (IOException e) {
            log.error(e);
        }
        for ( int i = 0 ; i< result.get(0).size() ;i++){
            StringBuilder datarow=new StringBuilder();
            datarow.append(getRealRow() + i).append("\001");
            for (int j = 0 ; j< result.size() ; j++){
                datarow.append(result.get(j).get(i));
                if (j!=result.size()-1)
                    datarow.append("\001");

            }
            try {
                baos.write(datarow.toString().getBytes());
                baos.write(newline.getBytes());
            } catch (IOException e) {
                log.error(e);
            }
            log.debug("Col [" + (getRealRow() + i) + "] " + datarow.toString());
            rownumber--;
            if (rownumber==0)
                break;
        }
        addRealRow(result.get(0).size());
        if (rownumber==0)
            return -2;
        return  result.get(0).size();
    }


















    //    private void readORCbyHive() throws IOException {
//        FSFile fsFile=FSFile.newInstance(FSFile.FSType.HDFS);
//        OrcFile.ReaderOptions option=new OrcFile.ReaderOptions(conf);
//
//        Reader reader= OrcFile.createReader(fsFile.getFs(), new Path("/user/hive/warehouse/ec.db/inc/icc=001|usa|1003/time=10/de999936-b98d-451f-80ec-4d6b3db7644e_ebc8376c-0638-4201-9f66-031c14e0252c"));
////
////        Reader reader= OrcFile.createReader(fsFile.getFs(), new Path("/user/hive/warehouse/ec.db/itembase/part-r-00000"));
////        RecordReader record=reader.rows(null);
//
////        OrcSerde sender=new OrcSerde();
//        Properties prop=new Properties();
//
//        prop.setProperty("columns","item:co");
//    }
//    public static void main(String[] args) throws IOException {
//        OrcFileUtil orc=newInstance();
////        orc.readORCbyHive();;
//        orc.readORC();
//    }
//
//}
//    public static void main(String[] args)   {
//        String path="/tmp/aaa/";
//        Pattern pattern=Pattern.compile("//s*");
//        int pause=0;
//
//    }
//        OrcFileUtil orc = newInstance();
//        ByteArrayInputStream stream=orc.readORCFiles("/user/hive/warehouse/ec/db/inc",TYPE.HDFS,2,50);
//        // Read InputStream by InputStreamReader
//        //  First Reader Method///////////////////////////
//        InputStreamReader inReader=new InputStreamReader(stream);
//        BufferedReader br=new BufferedReader(inReader);
//        try {
//            while (br.ready()){
//                String tmp=br.readLine();
//                System.out.println(tmp.replaceAll("\001","=="));
//            }
//        } catch (IOException e) {
//            log.error(e);
//        }
//        ByteArrayOutputStream bao = new ByteArrayOutputStream();
//        int c=0;
//        byte[] buffer = new byte[1024];
//        while ((c = stream.read(buffer)) >= 0) {
//            bao.write(buffer,0,c);
//            System.out.println(new String(bao.toByteArray()));
//        }
//        int puase=0;
//        orc.writeORC("/user/hive/warehouse/ec.db/itembase/part-r-00000",TYPE.HDFS,"/tmp/gary/test1/test2/" ,0);
//        orc.writeORCFiles("/user/hive/warehouse/ec.db/itembase/", "/tmp/gary/test1/test2/", TYPE.HDFS);
//    }

}
