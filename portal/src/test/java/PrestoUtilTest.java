import com.google.common.base.Equivalence;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.chickling.boot.Init;
import org.apache.hadoop.hive.ql.parse.spark.GenSparkProcContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;
import org.junit.Test;

import com.chickling.util.PrestoUtil;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by jw6v on 2016/2/15.
 */

public class PrestoUtilTest {
    @BeforeClass
    public static void init(){
        Init.setPrestoURL("http://172.16.31.62:8080");
        Init.setPrestoCatalog("hive");
    }
    @Test
    public void testGetQuery(){
        Gson gson =new Gson();
        PrestoUtil pu=new PrestoUtil();
        String totallquety=gson.toJson(pu.getQuery(0,null));
//        Type type = new TypeToken<Map<String,Map>>(){}.getType();
//        Map<String,Map> obj = gson.fromJson(totallquety, type);
        System.out.println(totallquety);
    }

    @Test
    public void testPost(){
        Gson gson =new Gson();
        ArrayList<String> y_axis=new ArrayList<>();
        y_axis.add("campaign");
        y_axis.add("position");
        y_axis.add("strategy");
        String x_axis="session";
        PrestoUtil pu=new PrestoUtil();
      String result=pu.post("Select session,campaign,position,strategy from presto_temp.temp_131bfaee784f40be87b230bbaac4fc6a order by session DESC limit 10",0,"");
        System.out.println(gson.toJson(result));

    }


    @Test
    public void testGetNode(){
        PrestoUtil pu=new PrestoUtil();
        String Node=pu.getNode(0,null);
        System.out.println(Node);
    }

}
