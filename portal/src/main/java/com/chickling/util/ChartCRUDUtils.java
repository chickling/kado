package com.chickling.util;

import com.chickling.sqlite.ConnectionManager;
import com.chickling.models.Auth;
import com.chickling.models.MessageFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jw6v on 2017/1/19.
 */
public class ChartCRUDUtils {

    private final static String InsertChart="INSERT INTO `main`.`Chart` (`JobID`,`Type`,`Chart_Name`,`Chart_Setting`) VALUES (?,?,?,?);";
    private final static String DeleteChart="DELETE FROM `main`.`Chart` WHERE `Number`=?;";
    private final static String DeleteChartbyJob="DELETE FROM `main`.`Chart` WHERE `JobID`=?;";
    private final static String GetChart="SELECT * FROM `main`.`Chart` WHERE `JobID`=?;";
    private final static String GetChartbyNumber="SELECT * FROM `main`.`Chart` WHERE `Number`=?;";
    private final static String UpdateChart="UPDATE `main`.`Chart` SET `Type`=?,`Chart_Name`=?,`Chart_Setting`=? WHERE `Number`=?;";


    private static Logger log = LogManager.getLogger(ChartCRUDUtils.class);

    public synchronized static String addChart(Map<String,String> input,String token){
        PreparedStatement stat = null;
        String QuerySQL="";

        try {
            Auth au = new Auth();
            ArrayList<Object> userInfo = au.verify(token);
            if (!(Boolean) userInfo.get(4)) {// Check login
                return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if (((Integer) userInfo.get(0)) > 0) {//Not a general user
                QuerySQL=InsertChart;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                stat.setString(1, (String) input.get("JobID"));//userID
                stat.setString(2, (String) input.get("Type"));
                stat.setString(3, (String) input.get("Chart_Name"));
                stat.setString(4, (String) input.get("Chart_Setting"));
                stat.executeUpdate();
                QuerySQL = stat.toString();
                String key = Integer.toString(stat.getGeneratedKeys().getInt(1));

                stat.close();
                log.info("Status:success; TimeStamp:" + TimeUtil.getCurrentTime() + "; ChartID:" + key);
                return MessageFactory.rtnChartMessage("success", TimeUtil.getCurrentTime(), "", key);
            }else {
                return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");

            }
        }catch(SQLException sqle){
            log.error(sqle.toString() + ";SQL:" + QuerySQL);
            return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }

    }

    public synchronized static String deleteChart(int ChartID,int jobID,String token){
        PreparedStatement stat = null;

        String QuerySQL = "";
        try {
            Auth au = new Auth();
            if (!(Boolean) au.verify(token).get(4)) {//check login
                return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((au.jobMatch(token, Integer.toString(jobID))) || ((Integer) au.verify(token).get(0) == 2)) {
                QuerySQL = DeleteChart;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);


            } else {
                return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), "Permission Denied", Integer.toString(ChartID));
            }
            QuerySQL=stat.toString();
            stat.setInt(1, ChartID);
            stat.execute();
            stat.close();
            log.info( "Status:success; TimeStamp:"+TimeUtil.getCurrentTime()+"; ChartID:"+ChartID);
            return MessageFactory.rtnChartMessage("success", TimeUtil.getCurrentTime(), "", Integer.toString(ChartID));
        }catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    public synchronized static String deleteChartbyJob(int jobID,String token){
        PreparedStatement stat = null;

        String QuerySQL = "";
        try {
            Auth au = new Auth();
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((au.jobMatch(token, Integer.toString(jobID))) || ((Integer) au.verify(token).get(0) == 2)) {
                QuerySQL = DeleteChartbyJob;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);

            } else {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission Denied", Integer.toString(jobID));
            }
            QuerySQL=stat.toString();
            stat.setInt(1, jobID);
            stat.execute();
            stat.close();
            log.info( "Status:success; TimeStamp:"+TimeUtil.getCurrentTime()+"; JobID:"+jobID);
            return MessageFactory.rtnJobMessage("success", TimeUtil.getCurrentTime(), "", Integer.toString(jobID));
        }catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), Integer.toString(jobID));
        }
    }
    public synchronized static String updateChart( Map input,int jobID,String token){
        PreparedStatement stat = null;
        String QuerySQL ="";
        try {
            //INSERT SQL
            Auth au = new Auth();
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((au.jobMatch(token, Integer.toString(jobID))) || ((Integer) au.verify(token).get(0) == 2)) {

                QuerySQL = UpdateChart;
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
            } else {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", Integer.toString(jobID));
            }

            stat.setString(1, input.get("Type").toString());
            stat.setString(2, input.get("Chart_Name").toString());
            stat.setString(3,input.get("Chart_Setting").toString());
            stat.setInt(4, ((Double) input.get("ChartID")).intValue());
            stat.executeUpdate();
            stat.close();
            log.info( "Status:success; TimeStamp:"+TimeUtil.getCurrentTime());
            return MessageFactory.rtnChartMessage("success", TimeUtil.getCurrentTime(), "", "");
        }
        catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }

    public synchronized static String readChartbyJobID(int JobID,String token){
        //todo
        String QuerySQL =GetChart;
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);
            if (!(Boolean) au.verify(token).get(4)) {// check login
                return MessageFactory.rtnChartListMessage("error", "","permission denied", "",new ArrayList());
            } else if(((Integer) au.verify(token).get(0) == 2)||(au.groupMatch(token, JobID))){
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                stat.setInt(1, JobID);
                QuerySQL=stat.toString();
                rs = stat.executeQuery();
                List<Map> rtn = MessageFactory.rtnChartMessage(rs);
                stat.close();
                return MessageFactory.rtnChartListMessage("success", "","", "",rtn);
            }else{
                return MessageFactory.rtnChartListMessage("error", "","permission denied", "",new ArrayList());
            }
        }
        catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnChartListMessage("error", "",sqle.toString(), "",new ArrayList());
        }

    }

    public synchronized static String readChart(int ChartID,String token){
        String QuerySQL =GetChartbyNumber;
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnChartInfoMessage("error", "","permission denied", "",new HashMap());
            } else{
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(QuerySQL);
                stat.setInt(1, ChartID);
                QuerySQL=stat.toString();
                rs = stat.executeQuery();
                int jobID=0;
                HashMap<String,String> rtn = new HashMap<>();
                if(rs.next()){
                    jobID=rs.getInt("Number");
                    rtn.put("ChartID",Integer.toString(rs.getInt("Number")));
                    rtn.put("JobID",Integer.toString(rs.getInt("JobID")));
                    rtn.put("Type",rs.getString("Type"));
                    rtn.put("Chart_Name",rs.getString("Chart_Name"));
                    rtn.put("Chart_Setting",rs.getString("Chart_Setting"));
                    stat.close();
                }
                if(((Integer) au.verify(token).get(0) == 2)||(au.groupMatch(token, jobID))){
                    return MessageFactory.rtnChartInfoMessage("success","","","",rtn);
                }else{
                    return MessageFactory.rtnChartInfoMessage("error", "","permission denied", "",new HashMap());
                }
            }
        }
        catch(SQLException sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnChartInfoMessage("error", "",sqle.toString(), "",new HashMap());
        }
    }

}
