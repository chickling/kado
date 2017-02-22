package com.chickling.models;

import com.google.gson.Gson;
import com.chickling.DB.ConnectionManager;
import com.chickling.util.TimeUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains the account verification and management provided to the RESTful API
 * Created by ey67 on 2015/11/26.
 */
public class AccountManager {
    /*Log4J*/
    Logger log = LogManager.getLogger(AccountManager.class);
    /**
     * Return Account Message
     * @param status Message Status error|success
     * @param message Echo Message content
     * @return Json String
     */
    public String message(String status,String message){
        Map json=new LinkedHashMap();
        json.put("status",status);
        json.put("time", TimeUtil.getCurrentTime());
        json.put("message",message);
        Gson gson = new Gson();
        return gson.toJson(json);
    }
    /**
     * Return Account Message List
     * @param status Message Status error|success
     * @parm listName
     * @param message Echo Message List
     * @return Json String
     */
    public String messageList(String status,String listName,List<Map> message){
        Map json=new LinkedHashMap();
        json.put("status",status);
        json.put("time",TimeUtil.getCurrentTime());
        json.put(listName,message);
        Gson gson = new Gson();
        return gson.toJson(json);
    }
    /**
     * User Login
     * @param account AccountID
     * @param password Password MD5
     * @return Json Format Message
     * @throws SQLException
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public String login(String account,String password)  {
        //SQLite
        PreparedStatement stat = null;
        ResultSet rs = null;
        //Check User Account and Password
        String sql = "select * from `User`  U,`Group`  G where U.Gid=G.GID AND U.AccountID=? AND U.Password=? AND U.Enable=1";


        try {
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
            stat.setString(1, account);
            stat.setString(2, password);
            rs = stat.executeQuery();
        } catch (SQLException e) {
            log.error(e);
            log.error("Error SQL:"+sql);
            log.error("account->"+account+";password"+password);
            return message("error", "sql error");
        }


        //Store User info
        String token="";
        int uid=0;
        boolean admin=false;
        Map loginMessage=new LinkedHashMap();

        try {
            while(rs.next())
            {
                //Generate TOKEN
                token=sha256("95e945b0fd96631979e5580b1297947200684d09db4b185450b6f6dc9c9255e0" + rs.getString("UserName") + TimeUtil.getCurrentTime());
                uid=rs.getInt("UID");
                admin=rs.getBoolean("Admin");
                //Store to Map
                loginMessage.put("status","success");
                loginMessage.put("token",token);
                loginMessage.put("time", TimeUtil.getCurrentTime());
                loginMessage.put("uid",rs.getString("UID"));
                loginMessage.put("username",rs.getString("UserName"));
                loginMessage.put("group", rs.getString("GroupName"));
                if(rs.getBoolean("Admin")) {
                    loginMessage.put("level", "Admin");
                }else {
                    loginMessage.put("level", "User");
                }
            }
        } catch (SQLException e) {
            log.error("SQL Error:");
            log.error(ExceptionUtils.getStackTrace(e));
            return message("error", "SQL:"+e.getMessage());
        }catch (NoSuchAlgorithmException|UnsupportedEncodingException e) {
            log.error("SHA 256 Error:");
            log.error(ExceptionUtils.getStackTrace(e));
            return message("error", "SHA 256:"+e.getMessage());
        }

        if(!token.equals("")){
            //IF login success
            //Add login info to DataBase
            sql = "INSERT INTO `User_Login` (`UID`,`Admin`,`LoginTime`,`Token`) VALUES (?,?,?,?)";


            try {
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
                stat.setInt(1, uid);
                stat.setBoolean(2, admin);
                stat.setString(3, TimeUtil.getCurrentTime());
                stat.setString(4, token);
                synchronized (ConnectionManager.class) {
                    stat.executeUpdate();
                }
            } catch (SQLException e) {
                log.error(e);
                log.error("Error SQL:"+sql);
                log.error("token->"+token+";uid->"+uid);
                return message("error", e.getMessage());

            }
            Gson gson=new Gson();
            return gson.toJson(loginMessage);
        }else {
            return message("error", "account or password error");
        }
    }

    /**
     * User Logout
     * @param token SHA-256 CODE
     * @return Json Format Message
     * @throws SQLException
     */
    public String logout(String token) {
        //SQLite
        PreparedStatement stat = null;
        ResultSet rs = null;
        //Check Token
        String sql = "SELECT `LogoutTime` FROM `main`.`User_Login` WHERE `Token`=?";

        try {
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
            stat.setString(1, token);
            rs = stat.executeQuery();


            boolean flag = false;
            String logoutTime = "";
            while (rs.next()) {
                flag = true;
                logoutTime = rs.getString("LogoutTime");

            }
            if ((logoutTime == null || logoutTime.equals("")) && flag == true) {
                sql = "UPDATE `main`.`User_Login` SET `LogoutTime` = ? WHERE  `token` = ?";
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
                stat.setString(1, TimeUtil.getCurrentTime());
                stat.setString(2, token);
                synchronized (ConnectionManager.class) {
                    stat.executeUpdate();
                }
                return message("success", "Logout successful");
            } else if (flag == false) {
                return message("error", "Token Error!");
            } else {
                return message("error", "It had previously been Logout");
            }
        } catch (SQLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            log.error("Error Token:" + token);
            return message("error", "sql error");
        }
    }

    /**
     * Add User to Table
     * @param username
     * @param account
     * @param password
     * @param email
     * @param groupID
     * @param level
     * @return json message
     * @throws SQLException
     */
    public String addUser(String username,String account,String password,String email,int groupID,int level,Boolean chartBuilder){
        if(!username.equals("")&&!account.equals("")&&!password.equals("")&&!email.equals("")&&groupID!=0&&level<3){
            try {
                if(gidIsExist(groupID)){
                    if(!accountIsExist(account)) {
                        //SQLite
                        PreparedStatement stat = null;
                        ResultSet rs = null;
                        //INSERT SQL
                        String sql = "INSERT INTO `main`.`User` (`AccountID`,`UserName`,`Password`,`Email`,`Gid`,`Admin`,`General`,`Enable`,`ChartBuilder`) VALUES (?,?,?,?,?,?,?,1,?)";
                        stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
                        stat.setString(1, account);
                        stat.setString(2, username);
                        stat.setString(3, password);
                        stat.setString(4, email);
                        stat.setInt(5, groupID);
                        if (level == 1) {
                            stat.setBoolean(6, false);
                            stat.setBoolean(7, false);
                        } else if (level == 2) {
                            stat.setBoolean(6, true);
                            stat.setBoolean(7, false);
                        } else {
                            stat.setBoolean(6, false);
                            stat.setBoolean(7, true);
                        }
                        stat.setBoolean(8,chartBuilder);
                        synchronized (ConnectionManager.class) {
                            stat.executeUpdate();
                        }
                        return message("success", "Account successfully added");
                    }else {
                        return message("error","Account is exist");

                    }
                }else {
                    return message("error","Group does not exist");
                }
            } catch (SQLException e) {
                log.error(ExceptionUtils.getStackTrace(e));
                return message("error","Sql error");
            }
        }else {
            return message("error","Field can not be empty");
        }
    }

    /**
     * Add Group to Table
     * @param group
     * @param group_info
     * @return json message
     * @throws SQLException
     */
    public String addGroup(String group,String group_info) {
        if(!group.equals("")&&!group_info.equals("")){

            try {
                if(!groupIsExist(group)){
                        //SQLite
                        PreparedStatement stat = null;
                        //INSERT SQL
                        String sql = "INSERT INTO `main`.`Group` (`GroupName`,`Memo`) VALUES (?,?)";
                        stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
                        stat.setString(1, group);
                        stat.setString(2, group_info);
                        synchronized (ConnectionManager.class) {
                            stat.executeUpdate();
                        }
                        return message("success", "Group successfully added");


                }else {
                    return message("error","Group is exist");
                }
            } catch (SQLException e) {
                log.error(ExceptionUtils.getStackTrace(e));
                return message("error","Sql error");
            }
        }else {
            return message("error","Field can not be empty");
        }
    }

    /**
     * Update User Info By UID
     * @param UID
     * @param username
     * @param account
     * @param password
     * @param email
     * @param groupID
     * @param level
     * @return json message
     * @throws SQLException
     */
    public String updateUser(int UID,String username,String account,String password,String email,int groupID,int level,Boolean chartBuilder) {
        if(!username.equals("")&&!account.equals("")&&!email.equals("")&&groupID!=0&&level<3){

            try {
                if(gidIsExist(groupID)){
                    if(uidIsExist(UID)) {
                        //SQLite
                        PreparedStatement stat = null;
                        //INSERT SQL
                        String sql;

                        //if password is blank don't update it
                        if(!password.equals(""))
                            sql = "UPDATE `main`.`User` SET `AccountID` = ?, `UserName` = ?,`Email` = ?, `Gid` = ?, `Admin` = ?, `General` = ?, `Password` = ?,`ChartBuilder` = ? WHERE  `UID` = ?  AND Enable=1";
                        else
                            sql = "UPDATE `main`.`User` SET `AccountID` = ?, `UserName` = ?,`Email` = ?, `Gid` = ?, `Admin` = ?, `General` = ? ,`ChartBuilder` = ? WHERE  `UID` = ?  AND Enable=1";
                        stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
                        stat.setString(1, account);
                        stat.setString(2, username);
                        stat.setString(3, email);
                        stat.setInt(4, groupID);
                        if (level == 1) {
                            stat.setBoolean(5, false);
                            stat.setBoolean(6, false);
                        } else if (level == 2) {
                            stat.setBoolean(5, true);
                            stat.setBoolean(6, false);
                        } else {
                            stat.setBoolean(5, false);
                            stat.setBoolean(6, true);
                        }

                        //if password is blank don't update it
                        if(!password.equals("")) {
                            stat.setString(7, password);
                            stat.setBoolean(8, chartBuilder);
                            stat.setInt(9, UID);

                        }else {
                            stat.setBoolean(7, chartBuilder);
                            stat.setInt(8, UID);

                        }
                        synchronized (ConnectionManager.class) {
                            stat.executeUpdate();
                        }
                        return message("success", "Account successfully added");
                    }else {
                        return message("error","UID does not exist");
                    }

                }else {
                    return message("error","Group does not exist");
                }
            } catch (SQLException e) {
                log.error(ExceptionUtils.getStackTrace(e));
                return message("error","Sql error");
            }
        }else {
            return message("error","Field can not be empty");
        }
    }

    /**
     * Update User Password,Require old password
     * @param UID
     * @param newPassword
     * @param oldPassword
     * @return json message
     * @throws SQLException
     */
    public String updatePassword(int UID,String newPassword,String oldPassword) {
        if(!newPassword.equals("")&&!oldPassword.equals("")&&UID!=0){

            try {
                if(uidIsExist(UID)){
                    //Check old password is correct?
                    if(checkPassword(UID, oldPassword)) {
                        //SQLite
                        PreparedStatement stat = null;
                        //INSERT SQL
                        String sql = "UPDATE `main`.`User` SET  `Password` = ?  WHERE  `UID` = ?  AND Enable=1";
                        stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
                        stat.setString(1, newPassword);
                        stat.setInt(2, UID);
                        synchronized (ConnectionManager.class) {
                            stat.executeUpdate();
                        }
                        return message("success", "Password successfully update");
                    }else {
                        return message("error","Old password error");

                    }
                }else {
                    return message("error","UID does not exist");
                }
            } catch (SQLException e) {
                log.error(ExceptionUtils.getStackTrace(e));
                return message("error","Sql error");
            }
        }else {
            return message("error","Field can not be empty");
        }
    }

    /**
     * Update Group Info By GID
     * @param GID
     * @param group
     * @param group_info
     * @return json message
     * @throws SQLException
     */
    public String updateGroup(int GID,String group,String group_info){
        if(!group.equals("")&&!group_info.equals("")){
            try {
                if(gidIsExist(GID)){
                    //SQLite
                    PreparedStatement stat = null;
                    //INSERT SQL
                    String sql = "UPDATE `main`.`Group` SET `GroupName` = ?, `Memo` = ? WHERE  `GID` = ?";
                    stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
                    stat.setString(1, group);
                    stat.setString(2, group_info);
                    stat.setInt(3, GID);
                    synchronized (ConnectionManager.class) {
                        stat.executeUpdate();
                    }
                    return message("success", "Group successfully update");


                }else {
                    return message("error","Group is not exist");
                }
            } catch (SQLException e) {
                log.error(ExceptionUtils.getStackTrace(e));
                return message("error","Sql error");
            }
        }else {
            return message("error","Field can not be empty");
        }
    }

    /**
     * Get all user list
     * @return user list json
     * @throws SQLException
     */
    public String getUserList() {
        //SQLite
        PreparedStatement stat = null;
        ResultSet rs = null;
        //Check User Account and Password
        String sql = "SELECT * FROM  `User`  U LEFT JOIN `Group`  G ON U.Gid=G.GID AND U.Enable=1 ";

        try {
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);

            rs = stat.executeQuery();
            List<Map> userList = new ArrayList<>();
            while (rs.next()) {
                int level = 0;
                if (rs.getBoolean("Admin") == true && rs.getBoolean("General") == false) {
                    level = 2;
                } else if (rs.getBoolean("Admin") == false && rs.getBoolean("General") == true) {
                    level = 0;
                } else {
                    level = 1;
                }
                Map userInfo = new LinkedHashMap();
                userInfo.put("userid", rs.getInt("UID"));
                userInfo.put("account", rs.getString("AccountID"));
                userInfo.put("username", rs.getString("UserName"));
                userInfo.put("groupid", rs.getString("GID"));
                userInfo.put("group", rs.getString("GroupName"));
                userInfo.put("level", level);
                userInfo.put("email", rs.getString("Email"));
                userInfo.put("chartbuilder",rs.getBoolean("ChartBuilder"));
                userList.add(userInfo);
            }
            if (userList.size() != 0) {
                return messageList("success", "userlist", userList);
            } else {
                return message("error", "Not any user");
            }
        } catch (SQLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return message("error", "Sql error");
        }

    }

    /**
     * Get user info bu UID
     * @param UID
     * @return user info json
     * @throws SQLException
     */
    public String getUserInfo(int UID){
        try {
            if(uidIsExist(UID)) {
                //SQLite
                PreparedStatement stat = null;
                ResultSet rs = null;
                //Check User Account and Password
                String sql = "SELECT * FROM  `User`  U LEFT JOIN `Group`  G ON  U.Gid=G.GID WHERE U.UID=?  AND U.Enable=1";
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
                stat.setInt(1, UID);
                rs = stat.executeQuery();
                Map userInfo = new LinkedHashMap();
                userInfo.put("status","success");
                userInfo.put("time",TimeUtil.getCurrentTime());
                while (rs.next()) {
                    int level = 0;
                    if (rs.getBoolean("Admin") == true && rs.getBoolean("General") == false) {
                        level = 2;
                    } else if (rs.getBoolean("Admin") == false && rs.getBoolean("General") == true) {
                        level = 0;
                    } else {
                        level = 1;
                    }
                    userInfo.put("userid", rs.getInt("UID"));
                    userInfo.put("account", rs.getString("AccountID"));
                    userInfo.put("username", rs.getString("UserName"));
                    userInfo.put("groupid", rs.getString("GID"));
                    userInfo.put("group", rs.getString("GroupName"));
                    userInfo.put("level", level);
                    userInfo.put("email", rs.getString("Email"));
                    userInfo.put("chartbuilder",rs.getBoolean("ChartBuilder"));
                }
                Gson gson=new Gson();
                return gson.toJson(userInfo);
            }else {
                return message("error","User ID is not exist");
            }
        } catch (SQLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return message("error", "Sql error");
        }

    }

    /**
     * Get group list
     * @return group list json
     * @throws SQLException
     */
    public String getGroupList()  {
        //SQLite
        PreparedStatement stat = null;
        ResultSet rs = null;
        //Check User Account and Password
        String sql = "SELECT * FROM `Group`";

        try {
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
            rs = stat.executeQuery();
            List<Map> groupList=new ArrayList<>();
            while (rs.next()){
                Map groupInfo=new LinkedHashMap();
                groupInfo.put("groupid",rs.getInt("GID"));
                groupInfo.put("group", rs.getString("GroupName"));
                groupInfo.put("group_info",rs.getString("Memo"));
                groupList.add(groupInfo);
            }
            if(groupList.size()!=0){
                return messageList("success", "grouplist", groupList);
            }else {
                return message("error","Not any user");
            }
        } catch (SQLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return message("error", "Sql error");

        }

    }

    /**
     * Delete User (set User Enable=0 Not really delete)
     * @param UID
     * @return
     * @throws SQLException
     */

    public String delUser(int UID) {
        try {
            if(uidIsExist(UID)) {
                //SQLite
                PreparedStatement stat = null;
                //Check User Account and Password
                String sql = "UPDATE `main`.`User` SET  `Enable` = '0'  WHERE  `UID` = ?";
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
                stat.setInt(1, UID);
                synchronized (ConnectionManager.class) {
                    stat.executeUpdate();
                }
                return message("success", "User delete is success");
            }else {
                return message("error","User ID is not exist");

            }
        } catch (SQLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return message("error", "Sql error");
        }

    }

    /**
     * Delete Group (Really delete)
     * @param GID
     * @return
     * @throws SQLException
     */

    public String delGroup(int GID) {
        try {
            if(gidIsExist(GID)) {
                //SQLite
                PreparedStatement stat = null;
                //Check User Account and Password
                String sql = "DELETE FROM `main`.`Group`  WHERE  `GID` = ?";
                stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
                stat.setInt(1, GID);
                synchronized (ConnectionManager.class) {
                    stat.executeUpdate();
                }
                return message("success", "Group delete is success");
            }else {
                return message("error","Group ID is not exist");

            }
        } catch (SQLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return message("error", "Sql error");
        }

    }
    /**
     * Check GID Exist
     * @param groupID
     * @return is Exist?
     * @throws SQLException
     */
    public boolean gidIsExist(int groupID) throws SQLException {
        //SQLite
        PreparedStatement stat = null;
        ResultSet rs = null;
        //Check User Account and Password
        String sql = "select `GroupName` from `Group` where  GID=?";
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
        stat.setInt(1, groupID);
        rs = stat.executeQuery();
        boolean flag=false;
        while (rs.next()){
            flag=true;
        }
        return flag;
    }

    /**
     * Check group name Exist
     * @param groupName
     * @return is Exist?
     * @throws SQLException
     */
    public boolean groupIsExist(String groupName) throws SQLException {
        //SQLite
        PreparedStatement stat = null;
        ResultSet rs = null;
        //Check User Account and Password
        String sql = "select `GID` from `Group` where  GroupName=?";
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
        stat.setString(1, groupName);
        rs = stat.executeQuery();
        boolean flag=false;
        while (rs.next()){
            flag=true;
        }
        return flag;
    }
    /**
     * Check UID Exist
     * @param userID
     * @return is Exist?
     * @throws SQLException
     */
    public boolean uidIsExist(int userID) throws SQLException {
        //SQLite
        PreparedStatement stat = null;
        ResultSet rs = null;
        //Check User Account and Password
        String sql = "select `UserName` from `User` where  UID=?  AND Enable=1";
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
        stat.setInt(1, userID);
        rs = stat.executeQuery();
        boolean flag=false;
        while (rs.next()){
            flag=true;
        }
        return flag;
    }

    /**
     * Check account Exist
     * @param accountID
     * @return is Exist?
     * @throws SQLException
     */
    public boolean accountIsExist(String accountID) throws SQLException {
        //SQLite
        PreparedStatement stat = null;
        ResultSet rs = null;
        //Check User Account and Password
        String sql = "select `UserName` from `User` where  AccountID=?  AND Enable=1";
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
        stat.setString(1, accountID);
        rs = stat.executeQuery();
        boolean flag=false;
        while (rs.next()){
            flag=true;
        }
        return flag;
    }

    /**
     * Check password is correct
     * @param UID
     * @param password
     * @return is correct?
     * @throws SQLException
     */
    public boolean checkPassword(int UID,String password) throws SQLException {
        //SQLite
        PreparedStatement stat = null;
        ResultSet rs = null;
        //Check User Account and Password
        String sql = "select `AccountID` from `User`  where UID=? AND Password=?  AND Enable=1";
        stat = ConnectionManager.getInstance().getConnection().prepareStatement(sql);
        stat.setInt(1, UID);
        stat.setString(2, password);
        rs = stat.executeQuery();
        boolean flag=false;
        while (rs.next()){
            flag=true;
        }
        return flag;

    }

    /**
     * token is from admin
     * @param token
     * @return
     */
    public boolean isAdmin(String token){
        Auth auth=new Auth();
        try {
            ArrayList<Object> verify=auth.verify(token);
            if((Integer)verify.get(0)==2)
                return true;
            else
                return false;
        } catch (SQLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }
    public boolean isSelfToken(int UID,String token){
        Auth auth=new Auth();
        try {
            ArrayList<Object> verify=auth.verify(token);
            if((Integer)verify.get(2)==UID)
                return true;
            else
                return false;
        } catch (SQLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }
    /**
     * Encode SHA-256 to Generate Token
     * @param text
     * @return SHA-256 Token
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public String sha256(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //Encode SHA-256
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(text.getBytes("UTF-8"));
        byte[] digest = md.digest();
        return String.format("%064x", new java.math.BigInteger(1, digest));
    }
}
