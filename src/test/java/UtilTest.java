import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.chickling.models.Auth;
import com.sun.javafx.collections.MappingChange;
import org.junit.Test;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by jw6v on 2017/1/11.
 */
public class UtilTest {
    @Test
    public void parserTest(){
        String template="{\"Template\":[{\"URLKey\":\"aaa\",\"SQLKey\":\"aaa\",\"DefaultValue\":\"123\" },{\"URLKey\":\"aaa\",\"SQLKey\":\"aaa\",\"DefaultValue\":\"123\"},{\"URLKey\":\"aaa\",\"SQLKey\":\"aaa\",\"DefaultValue\":\"123\" }]}";
        Gson gson=new Gson();
        Type type = new TypeToken<Map>() {}.getType();
        Map map=(Map)gson.fromJson(template,type);
        ArrayList<Map<String,String>> info= (ArrayList)map.get("Template");
        for(Map in : info){
            System.out.println(in.get("URLKey"));
            System.out.println(in.get("SQLKey"));
            System.out.println(in.get("DefaultValue"));
        }
    }


    @Test
    public void setTemplateValueTest(){
        String rtnSql="Select * form testTable where aaa=$aaa$ and bbb=$bbb$ and ccc=$ccc$";
        Map<String,String> t =new HashMap();
        t.put("$aaa$","321");
        t.put("$bbb$","654");
        t.put("$ccc$","876");
        for (Map.Entry<String,String> entry : t.entrySet())
        {
            rtnSql=rtnSql.replace(entry.getKey(),entry.getValue());
        }
        System.out.println(rtnSql);
    }

    @Test
    public void checkmapTest(){
        Gson gson=new Gson();
        Type type = new TypeToken<Map>() {}.getType();
        String template="{\"Template\":[{\"URLKey\":\"aaa\",\"SQLKey\":\"$aaa$\",\"DefaultValue\":\"123\" },{\"URLKey\":\"bbb\",\"SQLKey\":\"$bbb$\",\"DefaultValue\":\"123\"},{\"URLKey\":\"ccc\",\"SQLKey\":\"$ccc$\",\"DefaultValue\":\"123\" }]}";
        Map map=(Map)gson.fromJson(template,type);
        ArrayList<Map<String,String>> list= (ArrayList)map.get("Template");
        Map inputMap =new HashMap<String,String>();
        inputMap.put("aaa","321");
        inputMap.put("bbb","654");
        //inputMap.put("ccc","876");
        HashMap<String, String> rtnMap=new HashMap<String, String>();
        for(Map m: list){
            if(inputMap.get(m.get("URLKey"))!=null){
                rtnMap.put((String)m.get("SQLKey"),(String)inputMap.get((String)m.get("URLKey")));
            }
            else{
                rtnMap.put((String)m.get("SQLKey"),(String)m.get("DefaultValue"));
            }

        }
        for (Map.Entry<String,String> entry : rtnMap.entrySet())
        {
            System.out.println(entry.getKey()+" "+entry.getValue());
        }


    }



    @Test
    public void testGroupMatch(){

        Auth auth=new Auth();
        try {
           if( auth.groupMatch("298a209c6271fe3ceb69f38d7b9e9c653de1b9f5f160506608785ea7ce8e30ce", 2))
           {
               System.out.println("Approved");
           }else{
               System.out.println("Access denied");
           }

        }catch(SQLException sqle){
            System.out.println(sqle.toString());
        }



    }
    @Test
    public void testChartBuilder(){
        Auth auth = new Auth();
        try{
            auth.checkChartBuilder("298a209c6271fe3ceb69f38d7b9e9c653de1b9f5f160506608785ea7ce8e30ce");
        }catch(SQLException sqle){
            System.out.println(sqle.toString());
        }
    }

}
