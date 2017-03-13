package com.chickling.models;

import com.chickling.sqlite.ConnectionManager;
import com.chickling.bean.job.User;
import com.chickling.util.YamlLoader;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private static final String checkGroup="Select Gid from User_Login INNER JOIN User ON User.UID=User_Login.UID where Token= ? INTERSECT Select Gid from Job INNER JOIN USER ON Job.JobOwner=User.UID where JobID=?";
    private static final String getJobIDfromJH="Select JobID from Job_History where JHID=?";
    public Auth(){}
    /*Log4J*/
    Logger log = LogManager.getLogger(Auth.class);
    public User verify2(String token) throws SQLException{
        User user=new User();
        PreparedStatement stat = null;
        ResultSet rs = null;
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(GetgidAdminSql);
        stat.setString(1, token);
        rs=stat.executeQuery();

        if(rs != null && rs.next()){
            int permission=0;

            if(rs.getBoolean("Admin"))
                permission=2;
            else if(rs.getBoolean("General"))
                permission=0;
            else
                permission=1;

            user.setPermission(permission);
            user.setGroupID(rs.getInt("Gid"));
            user.setUserID(rs.getInt("UID"));
            user.setUserName(rs.getString("UserName"));
            user.setLogIn(true);
        }
        else{

            user.setLogIn(false);
        }
        rs.close();
        stat.close();

        return user;
    }

    public ArrayList<Object> verify(String token) throws SQLException{
        ArrayList<Object> rtn =new ArrayList<Object>();
        PreparedStatement stat = null;
        ResultSet rs = null;
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(GetgidAdminSql);
        stat.setString(1, token);
        rs=stat.executeQuery();

        if(rs != null && rs.next()){
        int permission=0;

        if(rs.getBoolean("Admin"))
            permission=2;
        else if(rs.getBoolean("General"))
            permission=0;
        else
            permission=1;


        rtn.add(0, permission);
        rtn.add(1, rs.getInt("Gid"));
        rtn.add(2, rs.getInt("UID"));
        rtn.add(3, rs.getString("UserName"));
            rtn.add(4, true);//login

        }
        else{
            rtn.add(0,"");
            rtn.add(1,"");
            rtn.add(2,"");
            rtn.add(3,"");
            rtn.add(4, false);
        }
        rs.close();
        stat.close();

        return rtn;
    }

    public Boolean jobMatch(String token , String JobID) throws SQLException{
        PreparedStatement stat = null;
        ResultSet rs = null;
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(CheckJob);
        stat.setString(1, JobID);
        rs=stat.executeQuery();

        ArrayList<Object> UserInfo = verify(token);
        int permission=(Integer)UserInfo.get(0);
        if(rs != null && rs.next()){


            Boolean rtn=((permission==2)||(rs.getInt("JobOwner")==((Integer) UserInfo.get(2))));

            stat.close();
            return rtn;
        }
        else{
            stat.close();
            return false;
        }


    }

    public Boolean groupMatch(String token , int JobID) throws SQLException{
        //todo
        int gid=0;
        PreparedStatement stat = null;
        ResultSet rs = null;

        stat = ConnectionManager.getInstance().getConnection().prepareStatement(checkGroup);
        stat.setString(1, token);
        stat.setInt(2, JobID);
        rs=stat.executeQuery();
        while(rs.next()){
            gid=rs.getInt("Gid");
        }
        rs.close();
        stat.close();
        return !(gid==0);
    }

    public Boolean groupMatchwithJHid(String token , int JHID) throws SQLException{
        PreparedStatement stat = null;
        ResultSet rs = null;
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(getJobIDfromJH);
        stat.setInt(1, JHID);
        rs=stat.executeQuery();
        int jobID=rs.getInt("JobID");
        return groupMatch(token,jobID);
    }

    public Boolean tableMatch(String token , String TableName) throws SQLException{
        PreparedStatement stat = null;
        ResultSet rs = null;
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(CheckTable);
        stat.setString(1, TableName);
        rs=stat.executeQuery();
        Boolean rtn=false;

       User UserInfo = verify2(token);
        int permission=UserInfo.getPermission();
        if(permission==2) {
            return true;
        }
        else if(rs != null){
            while(rs.next()) {
                 rtn = (rtn|| (rs.getString("JobOutput").contains(TableName)));
            }
            stat.close();
            return rtn;
        }
        else{
            stat.close();
            return false;
        }

    }

    public Boolean scheduleMatch(String token , String JobID) throws SQLException{
        PreparedStatement stat = null;
        ResultSet rs = null;
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(CheckSchedule);
        stat.setString(1, JobID);
        rs=stat.executeQuery();

        ArrayList<Object> UserInfo = verify(token);
        int permission=(Integer)UserInfo.get(0);
        if(rs != null && rs.next()){


            Boolean rtn=(permission==2)||(rs.getInt("ScheduleOwner")==((Integer)UserInfo.get(2)));

            stat.close();
            return rtn;
        }
        else{
            stat.close();
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

    public Boolean checkChartBuilder(String token) throws SQLException{
        PreparedStatement stat = null;
        ResultSet rs = null;
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(CheckChartBuilder);
        stat.setString(1, token);
        rs=stat.executeQuery();
        Boolean rtn=rs.getBoolean("ChartBuilder");
        return  rtn;
    }




}
