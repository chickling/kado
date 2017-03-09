package com.chickling.util;

import com.chickling.sqlite.ConnectionManager;
import com.chickling.models.MessageFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * Created by jw6v on 2017/1/10.
 */
public class TemplateCRUDUtils {

    private final static String InsertSQLTemplate="INSERT INTO `main`.`SQLtemplate` (`JobID`,`URLKey`,`SQLKey`,`DefaultValue`) VALUES (?,?,?,?);";
    private final static String DeleteSQLTemplate="DELETE FROM `main`.`SQLtemplate` WHERE `JobID`=?;";
    private final static String GetSQLTemplate="SELECT * FROM `main`.`SQLtemplate` WHERE `JobID`=?;";

    private static Logger log = LogManager.getLogger(TemplateCRUDUtils.class);

    public synchronized static boolean addSqlTemplate(int jobID, ArrayList<Map<String,String>> templateMaps){
        PreparedStatement stat = null;
        //ResultSet rs = null;
        String QuerySQL="";
        try {
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(InsertSQLTemplate);
            for(Map templateMap: templateMaps) {
                stat.setInt(1, jobID);
                stat.setString(2, (String) templateMap.get("URLKey"));//userID
                stat.setString(3, (String) templateMap.get("SQLKey"));
                stat.setString(4, (String) templateMap.get("DefaultValue"));
                stat.executeUpdate();
                QuerySQL = stat.toString();
            }
            stat.close();
            log.info( "Status:success; TimeStamp:"+TimeUtil.getCurrentTime()+"; JobID:"+jobID);
            return true;
        }catch (SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return false;
        }
    }

    public synchronized static boolean deleteSqlTemplate(int jobID){
        PreparedStatement stat = null;
        //ResultSet rs = null;

        String QuerySQL = DeleteSQLTemplate;
        try {
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
            QuerySQL=stat.toString();
            stat.setInt(1, jobID);
            stat.execute();
            stat.close();
            log.info( "Status:success; TimeStamp:"+TimeUtil.getCurrentTime()+"; JobID:"+jobID);
            return true;
        }catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return false;
        }
    }

    public synchronized static boolean updateSqlTemplate(int jobID, ArrayList<Map<String,String>> templateMap){
        return(deleteSqlTemplate(jobID))&& (addSqlTemplate(jobID, templateMap));
    }

    public synchronized static List<Map> readSqlTemplate(int jobID){
        String QuerySQL = "";
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            QuerySQL = GetSQLTemplate;
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
            stat.setInt(1, jobID);
            QuerySQL=stat.toString();
            rs = stat.executeQuery();
            List<Map> rtn = MessageFactory.rtnTemplateMessage(rs);
            stat.close();
            return rtn;
        }
        catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            //return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
            return new ArrayList<>();
        }
    }



}
