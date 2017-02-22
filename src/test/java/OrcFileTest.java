import com.chickling.models.dfs.OrcFileUtil;

import java.io.*;
import java.util.Arrays;

/**
 * Created by gl08 on 2015/12/16.
 */
public class OrcFileTest {


    private  void testORCreateInputStream() throws IOException {
        OrcFileUtil orc=OrcFileUtil.newInstance();

        ByteArrayInputStream stream=orc.readORCFiles("/user/hive/warehouse/presto_temp.db/temp_6e0a68f26abb45b2a8e7bccaa56825f5", OrcFileUtil.TYPE.HDFS,0 ,277);

        //  First Reader Method///////////////////////////
//        ByteArrayOutputStream bao = new ByteArrayOutputStream();
//        int c=0;
//        byte[] buffer = new byte[1024];
//        while ((c = stream.read(buffer)) >= 0) {
//            bao.write(buffer,0,c);
//            System.out.println(new String(bao.toByteArray()));
//        }

        //  Second Reader Method////////////////////////
        InputStreamReader inReader=new InputStreamReader(stream);
        BufferedReader br=new BufferedReader(inReader);
        while (br.ready()){
            String[] line=br.readLine().split("\t|\001");
            System.out.println(Arrays.asList(line));
        }
    }

    private  void testORCWriter(){


    }
    public static void main(String[] args) throws IOException {
        OrcFileTest test=new OrcFileTest();
        test.testORCreateInputStream();

    }

}
