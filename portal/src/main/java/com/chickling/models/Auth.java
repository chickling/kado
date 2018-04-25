package com.chickling.models;

import com.chickling.bean.job.User;
import com.chickling.util.DBClientUtil;
import com.chickling.util.KadoRow;
import com.chickling.util.YamlLoader;
import com.facebook.presto.hive.$internal.org.apache.commons.lang3.exception.ExceptionUtils;
import owlstone.dbclient.db.DBClient;
import owlstone.dbclient.db.module.DBResult;
import owlstone.dbclient.db.module.PStmt;
import owlstone.dbclient.db.module.Row;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by jw6v on 2015/12/1.
 */
public class Auth {
    private final String GetgidAdminSql="Select * From User INNER JOIN User_Login on User.UID=User_Login.UID where User_Login.Token=? AND LogoutTime is null;";

    private static final String CheckJob="SELECT JobOwner FROM Job WHERE JobID=?;";
    private static final String CheckTable="SELECT JobOutput FROM Job_History INNER JOIN Job_Log ON Job_History.JobLog=Job_Log.JLID WHERE Job_History.JobOwner=?;";
    private static final String CheckChartBuilder="SELECT ChartBuilder from User INNER JOIN User_Login on User.UID=User_Login.UID where User_Login.Token=?";
    private static final String CheckSchedule="SELECT ScheduleOwner FROM Schedule WHERE ScheduleID=?;";
    private static final String checkGroup="Select Gid from User_Login INNER JOIN User ON User.UID=User_Login.UID where Token=? AND Gid in (Select Gid from Job INNER JOIN User ON Job.JobOwner=User.UID where JobID=?)";
    private static final String getJobIDfromJH="Select JobID from Job_History where JHID=?";
    public Auth(){}
    /*Log4J*/
    Logger log = LogManager.getLogger(Auth.class);
    public User verify2(String token) throws Exception{
        User user=new User();
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        queryBean=PStmt.buildQueryBean("kado-meta",GetgidAdminSql,new Object[]{
                token
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();

        if(rs.getRowList().size()>0){
            for(Row row:rs.getRowList()) {
                KadoRow r = new KadoRow(row);
                int permission = 0;

                if (r.getBoolean("Admin"))
                    permission = 2;
                else if (r.getBoolean("General"))
                    permission = 0;
                else
                    permission = 1;

                user.setPermission(permission);
                user.setGroupID(r.getInt("Gid"));
                user.setUserID(r.getInt("UID"));
                user.setUserName(r.getString("UserName"));
                user.setLogIn(true);
            }
        }
        else{

            user.setLogIn(false);
        }

        return user;
    }

    public ArrayList<Object> verify(String token) throws Exception{
        ArrayList<Object> rtn =new ArrayList<Object>();
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        queryBean=PStmt.buildQueryBean("kado-meta",GetgidAdminSql,new Object[]{
                token
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();

        if(rs.getRowSize()>0){
            KadoRow r = new KadoRow(rs.getRowList().get(0));
            int permission=0;

            if(r.getBoolean("Admin"))
                permission=2;
            else if(r.getBoolean("General"))
                permission=0;
            else
                permission=1;


            rtn.add(0, permission);
            rtn.add(1, r.getInt("Gid"));
            rtn.add(2, r.getInt("UID"));
            rtn.add(3, r.getString("UserName"));
                rtn.add(4, true);//login

        }
        else{
            rtn.add(0,"");
            rtn.add(1,"");
            rtn.add(2,"");
            rtn.add(3,"");
            rtn.add(4, false);
        }
        return rtn;
    }

    public Boolean jobMatch(String token , String JobID) throws Exception{
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        queryBean=PStmt.buildQueryBean("kado-meta",CheckJob,new Object[]{
                JobID
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();

        ArrayList<Object> UserInfo = verify(token);
        int permission=(Integer)UserInfo.get(0);
        if(rs.getRowSize()>0){
            KadoRow r = new KadoRow(rs.getRowList().get(0));

            Boolean rtn=((permission==2)||(r.getInt("JobOwner")==((Integer) UserInfo.get(2))));
            return rtn;
        }
        else{
            return false;
        }


    }

    public Boolean groupMatch(String token , int JobID) throws Exception{
        //todo
        int gid=0;
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        queryBean=PStmt.buildQueryBean("kado-meta",checkGroup,new Object[]{
                token,
                JobID
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();
        if(rs.getRowSize()>0){
            KadoRow r = new KadoRow(rs.getRowList().get(0));
            gid=r.getInt("Gid");
        }
        return !(gid==0);
    }

    public Boolean groupMatchwithJHid(String token , int JHID) throws Exception{
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        queryBean=PStmt.buildQueryBean("kado-meta",getJobIDfromJH,new Object[]{
                JHID
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();


        if(rs.getRowSize()>0) {
            KadoRow r=new KadoRow(rs.getRowList().get(0));
            int jobID = r.getInt("JobID");
            return groupMatch(token,jobID);
        }else {
            return false;
        }

    }

    public Boolean tableMatch(String token , String TableName) throws Exception{
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        queryBean=PStmt.buildQueryBean("kado-meta",CheckTable,new Object[]{
                TableName
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();

        Boolean rtn=false;

       User UserInfo = verify2(token);
        int permission=UserInfo.getPermission();
        if(permission==2) {
            return true;
        }
        else if(rs.getRowSize()>0){
            KadoRow r = new KadoRow(rs.getRowList().get(0));
            rtn = (rtn|| (r.getString("JobOutput").contains(TableName)));
            return rtn;
        }
        else{
            return false;
        }
    }

    public Boolean scheduleMatch(String token , String JobID) throws Exception{
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        queryBean=PStmt.buildQueryBean("kado-meta",CheckSchedule,new Object[]{
                JobID
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();

        ArrayList<Object> UserInfo = verify(token);
        int permission=(Integer)UserInfo.get(0);
        if(rs.getRowSize()>0){
            KadoRow r=new KadoRow(rs.getRowList().get(0));
            Boolean rtn=(permission==2)||(r.getInt("ScheduleOwner")==((Integer)UserInfo.get(2)));
            return rtn;
        }
        else{
            return false;
        }


    }

    public String generateDownloadToken(int jobrunid){
        AccountManager am=new AccountManager();
        String token="";
        try {
            token=am.sha256(YamlLoader.instance.getDownloadToken() + String.valueOf(jobrunid));
        } catch (NoSuchAlgorithmException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        } catch (UnsupportedEncodingException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return token;
    }

    public Boolean checkChartBuilder(String token) throws Exception{
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        queryBean=PStmt.buildQueryBean("kado-meta",CheckChartBuilder,new Object[]{
                token
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();
        if(rs.getRowSize()>0) {
            KadoRow r=new KadoRow(rs.getRowList().get(0));
            return r.getBoolean("ChartBuilder");
        }else {
            return false;
        }
    }




}
