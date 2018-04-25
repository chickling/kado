package com.chickling.util;

import com.chickling.models.Auth;
import com.chickling.models.MessageFactory;
import owlstone.dbclient.db.DBClient;
import owlstone.dbclient.db.module.DBResult;
import owlstone.dbclient.db.module.PStmt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jw6v on 2017/1/19.
 */
public class ChartCRUDUtils {

    private final static String InsertChart="INSERT INTO `Chart` (`JobID`,`Type`,`Chart_Name`,`Chart_Setting`) VALUES (?,?,?,?);";
    private final static String DeleteChart="DELETE FROM `Chart` WHERE `Number`=?;";
    private final static String DeleteChartbyJob="DELETE FROM `Chart` WHERE `JobID`=?;";
    private final static String GetChart="SELECT * FROM `Chart` WHERE `JobID`=?;";
    private final static String GetChartbyNumber="SELECT * FROM `Chart` WHERE `Number`=?;";
    private final static String UpdateChart="UPDATE `Chart` SET `Type`=?,`Chart_Name`=?,`Chart_Setting`=? WHERE `Number`=?;";


    private static Logger log = LogManager.getLogger(ChartCRUDUtils.class);

    public synchronized static String addChart(Map<String,String> input,String token){
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        String QuerySQL="";

        try {
            Auth au = new Auth();
            ArrayList<Object> userInfo = au.verify(token);
            if (!(Boolean) userInfo.get(4)) {// Check login
                return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if (((Integer) userInfo.get(0)) > 0) {//Not a general user
                QuerySQL=InsertChart;
                queryBean=PStmt.buildBatchUpdateBean("kado-meta",QuerySQL,new ArrayList<Object[]>() {{
                    add(new Object[]{
                            (String) input.get("JobID"),
                            (String) input.get("Type"),
                            (String) input.get("Chart_Name"),
                            (String) input.get("Chart_Setting")
                    });
                }});
                rs=dbClient.execute(queryBean);

                if(!rs.isSuccess())
                    throw rs.getException();

                QuerySQL =queryBean.getSql();
                String key = rs.getGeneratedPKList().get(0).toString();

                log.info("Status:success; TimeStamp:" + TimeUtil.getCurrentTime() + "; ChartID:" + key);
                return MessageFactory.rtnChartMessage("success", TimeUtil.getCurrentTime(), "", key);
            }else {
                return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");

            }
        }catch(Exception sqle){
            log.error(sqle.toString() + ";SQL:" + QuerySQL);
            return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }

    }

    public synchronized static String deleteChart(int ChartID,int jobID,String token){
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String QuerySQL = "";
        try {
            Auth au = new Auth();
            if (!(Boolean) au.verify(token).get(4)) {//check login
                return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((au.jobMatch(token, Integer.toString(jobID))) || ((Integer) au.verify(token).get(0) == 2)) {
                QuerySQL = DeleteChart;
                queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                        ChartID
                });
                rs=dbClient.execute(queryBean);

                if(!rs.isSuccess())
                    throw rs.getException();
                QuerySQL=queryBean.getSql();
                log.info( "Status:success; TimeStamp:"+TimeUtil.getCurrentTime()+"; ChartID:"+ChartID);
                return MessageFactory.rtnChartMessage("success", TimeUtil.getCurrentTime(), "", Integer.toString(ChartID));
            } else {
                return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), "Permission Denied", Integer.toString(ChartID));
            }

        }catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }
    public synchronized static String deleteChartbyJob(int jobID,String token){
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String QuerySQL = "";
        try {
            Auth au = new Auth();
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((au.jobMatch(token, Integer.toString(jobID))) || ((Integer) au.verify(token).get(0) == 2)) {
                QuerySQL = DeleteChartbyJob;
                queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                        jobID
                });
                rs=dbClient.execute(queryBean);
                if(!rs.isSuccess())
                    throw rs.getException();
                log.info( "Status:success; TimeStamp:"+TimeUtil.getCurrentTime()+"; JobID:"+jobID);
                return MessageFactory.rtnJobMessage("success", TimeUtil.getCurrentTime(), "", Integer.toString(jobID));
            } else {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission Denied", Integer.toString(jobID));
            }
        }catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), Integer.toString(jobID));
        }
    }
    public synchronized static String updateChart( Map input,int jobID,String token){
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        String QuerySQL ="";
        try {
            //INSERT SQL
            Auth au = new Auth();
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), "Permission denied", "");
            } else if ((au.jobMatch(token, Integer.toString(jobID))) || ((Integer) au.verify(token).get(0) == 2)) {

                QuerySQL = UpdateChart;
                queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                        input.get("Type").toString(),
                        input.get("Chart_Name").toString(),
                        input.get("Chart_Setting").toString(),
                        ((Double) input.get("ChartID")).intValue()
                });
                rs=dbClient.execute(queryBean);
                if(!rs.isSuccess())
                    throw rs.getException();
                log.info( "Status:success; TimeStamp:"+TimeUtil.getCurrentTime());
                return MessageFactory.rtnChartMessage("success", TimeUtil.getCurrentTime(), "", "");
            } else {
                return MessageFactory.rtnJobMessage("error", TimeUtil.getCurrentTime(), "Permission denied", Integer.toString(jobID));
            }
        }
        catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnChartMessage("error", TimeUtil.getCurrentTime(), sqle.getMessage(), "");
        }
    }

    public synchronized static String readChartbyJobID(int JobID,String token){
        //todo
        String QuerySQL =GetChart;
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        try {
            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);
            if (!(Boolean) au.verify(token).get(4)) {// check login
                return MessageFactory.rtnChartListMessage("error", "","permission denied", "",new ArrayList());
            } else if(((Integer) au.verify(token).get(0) == 2)||(au.groupMatch(token, JobID))){
                queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                        JobID
                });
                rs=dbClient.execute(queryBean);
                if(!rs.isSuccess())
                    throw rs.getException();
                QuerySQL=queryBean.getSql();
                List<Map> rtn = MessageFactory.rtnChartMessage(rs);
                return MessageFactory.rtnChartListMessage("success", "","", "",rtn);
            }else{
                return MessageFactory.rtnChartListMessage("error", "","permission denied", "",new ArrayList());
            }
        }
        catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnChartListMessage("error", "",sqle.toString(), "",new ArrayList());
        }

    }

    public synchronized static String readChart(int ChartID,String token){
        String QuerySQL =GetChartbyNumber;
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        try {
            Auth au = new Auth();
            ArrayList<Object> info = au.verify(token);
            if (!(Boolean) au.verify(token).get(4)) {
                return MessageFactory.rtnChartInfoMessage("error", "","permission denied", "",new HashMap());
            } else{
                queryBean=PStmt.buildQueryBean("kado-meta",QuerySQL,new Object[]{
                        ChartID
                });
                rs=dbClient.execute(queryBean);
                if(!rs.isSuccess())
                    throw rs.getException();
                QuerySQL=queryBean.getSql();

                int jobID=0;
                HashMap<String,String> rtn = new HashMap<>();
                if(rs.getRowSize()>0){
                    KadoRow r=new KadoRow(rs.getRowList().get(0));
                    jobID=r.getInt("Number");
                    rtn.put("ChartID",Integer.toString(r.getInt("Number")));
                    rtn.put("JobID",Integer.toString(r.getInt("JobID")));
                    rtn.put("Type",r.getString("Type"));
                    rtn.put("Chart_Name",r.getString("Chart_Name"));
                    rtn.put("Chart_Setting",r.getString("Chart_Setting"));
                }
                if(((Integer) au.verify(token).get(0) == 2)||(au.groupMatch(token, jobID))){
                    return MessageFactory.rtnChartInfoMessage("success","","","",rtn);
                }else{
                    return MessageFactory.rtnChartInfoMessage("error", "","permission denied", "",new HashMap());
                }
            }
        }
        catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+QuerySQL);
            return MessageFactory.rtnChartInfoMessage("error", "",sqle.toString(), "",new HashMap());
        }
    }

}
