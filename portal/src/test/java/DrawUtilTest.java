import com.google.gson.Gson;
import com.chickling.boot.Init;
import com.chickling.util.DrawUtils;
import com.chickling.util.PrestoUtil;
import org.junit.*;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by jw6v on 2017/1/25.
 */
public class DrawUtilTest {
    @BeforeClass
    public static void init(){
        Init.setPrestoURL("http://172.16.31.62:8080");
        Init.setPrestoCatalog("hive");
    }
    @org.junit.Test
    public void testgetQueryResult(){
        DrawUtils draw=new DrawUtils();
        ArrayList<String> cols=new ArrayList<>();
        cols.add("AAA");
        cols.add("BBB");
        cols.add("CCC");
        draw.getQueryResult(cols,10,"AAA","ASC","test");
    }

    @Test
    public void testDraw()throws SQLException{
        ArrayList<String> y_axis=new ArrayList<>();
        y_axis.add("campaign");
        y_axis.add("position");
        y_axis.add("strategy");
        DrawUtils draw=new DrawUtils();
        Map json=new LinkedHashMap();
        Gson gson=new Gson();
        json.put("sort","DESC");
        json.put("limit",10);
        json.put("xAxis","session");
        json.put("yAxis",y_axis);
        DrawUtils dr=new DrawUtils();
       String table=dr.getLastResult(1);
        //String table=dr.getResultTable(1);
        String rtn=dr.draw(json,table);
        System.out.println(rtn);
    }

    @Test
    public void testPie()throws SQLException{
        ArrayList<String> x_axis=new ArrayList<>();
        x_axis.add("refquery");
        x_axis.add("ip");
        x_axis.add("useragent");
        x_axis.add("refpath");
        x_axis.add("cookie");
        DrawUtils draw=new DrawUtils();
        Map json=new LinkedHashMap();
        Gson gson=new Gson();
        json.put("axis",x_axis);
        DrawUtils dr=new DrawUtils();
        String table=dr.getLastResult(1);
        //String table=dr.getResultTable(1);
        String rtn=dr.drawPie(json,table);
        System.out.println(rtn);
    }

}
