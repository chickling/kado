package com.chickling.models;

import com.chickling.util.DBClientUtil;
import com.chickling.util.KadoRow;
import com.facebook.presto.hive.$internal.org.apache.commons.lang3.exception.ExceptionUtils;
import com.google.common.base.Strings;
import com.google.gson.Gson;

import com.chickling.util.TimeUtil;
import owlstone.dbclient.db.DBClient;
import owlstone.dbclient.db.module.DBResult;
import owlstone.dbclient.db.module.Query;
import owlstone.dbclient.db.module.PStmt;
import owlstone.dbclient.db.module.Row;
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
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        //Check User Account and Password
        String sql = "select * from `User`  U,`Groups`  G where U.Gid=G.GID AND U.AccountID=? AND U.Password=? AND U.Enable=1";


        try {
            queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                    account,
                    password
            });
            rs=dbClient.execute(queryBean);
            if(!rs.isSuccess())
                throw rs.getException();
        } catch (Exception e) {
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

            for(Row row:rs.getRowList()){
                //Generate TOKEN
                KadoRow r=new KadoRow(row);

                token=sha256("95e945b0fd96631979e5580b1297947200684d09db4b185450b6f6dc9c9255e0" + r.getString("UserName") + TimeUtil.getCurrentTime());
                uid=r.getInt("UID");
                admin=r.getBoolean("Admin");
                //Store to Map
                loginMessage.put("status","success");
                loginMessage.put("token",token);
                loginMessage.put("time", TimeUtil.getCurrentTime());
                loginMessage.put("uid",r.getString("UID"));
                loginMessage.put("username",r.getString("UserName"));
                loginMessage.put("group", r.getString("GroupName"));
                if(r.getBoolean("Admin")) {
                    loginMessage.put("level", "Admin");
                }else {
                    loginMessage.put("level", "User");
                }
            }
        } catch (NoSuchAlgorithmException|UnsupportedEncodingException e) {
            log.error("SHA 256 Error:");
            log.error(ExceptionUtils.getStackTrace(e));
            return message("error", "SHA 256:"+e.getMessage());
        } catch (Exception e) {
            log.error("SQL Error:");
            log.error(ExceptionUtils.getStackTrace(e));
            return message("error", "SQL:"+e.getMessage());
        }

        if(!token.equals("")){
            //IF login success
            //Add login info to DataBase
            sql = "INSERT INTO `User_Login` (`UID`,`Admin`,`LoginTime`,`Token`) VALUES (?,?,?,?)";

            try {
                rs=dbClient.execute(PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                        uid,
                        admin,
                        TimeUtil.getCurrentTime(),
                        token
                }));
                if(!rs.isSuccess())
                    throw rs.getException();
            } catch (Exception e) {
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
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        //Check Token
        String sql = "SELECT `LogoutTime` FROM `User_Login` WHERE `Token`=?";

        try {

            queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                    token
            });
            rs=dbClient.execute(queryBean);
            if(!rs.isSuccess())
                throw rs.getException();


            boolean flag = false;
            String logoutTime = "";
            for(Row row:rs.getRowList()){
                KadoRow r=new KadoRow(row);
                flag = true;
                logoutTime = r.getString("LogoutTime");
            }
            if ((logoutTime == null || logoutTime.equals("")) && flag == true) {
                sql = "UPDATE `User_Login` SET `LogoutTime` = ? WHERE  `token` = ?";

                queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                        TimeUtil.getCurrentTime(),
                        token
                });
                rs=dbClient.execute(queryBean);
                if(!rs.isSuccess())
                    throw rs.getException();
                return message("success", "Logout successful");
            } else if (flag == false) {
                return message("error", "Token Error!");
            } else {
                return message("error", "It had previously been Logout");
            }
        } catch (Exception e) {
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
                        //DBClient
                        PStmt queryBean=null;
                        DBResult rs=null;
                        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
                        //INSERT SQL
                        String sql = "INSERT INTO `User` (`AccountID`,`UserName`,`Password`,`Email`,`Gid`,`Admin`,`General`,`Enable`,`ChartBuilder`) VALUES (?,?,?,?,?,?,?,1,?)";
                        queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                                account,
                                username,
                                password,
                                email,
                                groupID,
                                level == 1 ? false:(level == 2 ? true:false),
                                level == 1 ? false:(level == 2 ? false:true),
                                chartBuilder
                        });
                        rs=dbClient.execute(queryBean);
                        if(!rs.isSuccess())
                            throw rs.getException();
                        return message("success", "Account successfully added");
                    }else {
                        return message("error","Account is exist");

                    }
                }else {
                    return message("error","Group does not exist");
                }
            } catch (Exception e) {
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
                        //DBClient
                        PStmt queryBean=null;
                        DBResult rs=null;
                        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
                        //INSERT SQL
                        String sql = "INSERT INTO `Groups` (`GroupName`,`Memo`) VALUES (?,?)";

                        queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                                group,
                                group_info
                        });
                        rs=dbClient.execute(queryBean);
                        if(!rs.isSuccess())
                            throw rs.getException();
                        return message("success", "Group successfully added");
                }else {
                    return message("error","Group is exist");
                }
            } catch (Exception e) {
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
                        //DBClient
                        PStmt queryBean=null;
                        DBResult rs=null;
                        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
                        //INSERT SQL
                        String sql;
                        //if password is blank don't update it
                        if(!password.equals("")) {
                            sql = "UPDATE `User` SET `AccountID` = ?, `UserName` = ?,`Email` = ?, `Gid` = ?, `Admin` = ?, `General` = ?, `Password` = ?,`ChartBuilder` = ? WHERE  `UID` = ?  AND Enable=1";
                            queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                                    account,
                                    username,
                                    email,
                                    groupID,
                                    level == 1 ? false:(level == 2 ? true:false),
                                    level == 1 ? false:(level == 2 ? false:true),
                                    password,
                                    chartBuilder,
                                    UID
                            });
                        }else {
                            sql = "UPDATE `User` SET `AccountID` = ?, `UserName` = ?,`Email` = ?, `Gid` = ?, `Admin` = ?, `General` = ? ,`ChartBuilder` = ? WHERE  `UID` = ?  AND Enable=1";
                            queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                                    account,
                                    username,
                                    email,
                                    groupID,
                                    level == 1 ? false:(level == 2 ? true:false),
                                    level == 1 ? false:(level == 2 ? false:true),
                                    chartBuilder,
                                    UID
                            });
                        }
                        rs=dbClient.execute(queryBean);
                        if(!rs.isSuccess())
                            throw rs.getException();

                        return message("success", "Account successfully added");
                    }else {
                        return message("error","UID does not exist");
                    }

                }else {
                    return message("error","Group does not exist");
                }
            } catch (Exception e) {
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
                        //DBClient
                        PStmt queryBean=null;
                        DBResult rs=null;
                        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
                        //INSERT SQL
                        String sql = "UPDATE `User` SET  `Password` = ?  WHERE  `UID` = ?  AND Enable=1";
                        queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                                newPassword,
                                UID
                        });

                        rs=dbClient.execute(queryBean);
                        if(!rs.isSuccess())
                            throw rs.getException();
                        return message("success", "Password successfully update");
                    }else {
                        return message("error","Old password error");

                    }
                }else {
                    return message("error","UID does not exist");
                }
            } catch (Exception e) {
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
                    //DBClient
                    PStmt queryBean=null;
                    DBResult rs=null;
                    DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
                    //INSERT SQL
                    String sql = "UPDATE `Groups` SET `GroupName` = ?, `Memo` = ? WHERE  `GID` = ?";

                    queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                            group,
                            group_info,
                            GID
                    });

                    rs=dbClient.execute(queryBean);
                    if(!rs.isSuccess())
                        throw rs.getException();

                    return message("success", "Group successfully update");


                }else {
                    return message("error","Group is not exist");
                }
            } catch (Exception e) {
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
        //DBClient
        Query queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        //Check User Account and Password
        String sql = "SELECT * FROM  `User`  U LEFT JOIN `Groups`  G ON U.Gid=G.GID WHERE U.Enable=1 ";

        try {

            queryBean=new Query("kado-meta",sql);

            rs=dbClient.execute(queryBean);
            if(!rs.isSuccess())
                throw rs.getException();
            List<Map> userList = new ArrayList<>();
            for(Row row:rs.getRowList()){
                KadoRow r=new KadoRow(row);
                int level = 0;
                if (r.getBoolean("Admin") == true && r.getBoolean("General") == false) {
                    level = 2;
                } else if (r.getBoolean("Admin") == false && r.getBoolean("General") == true) {
                    level = 0;
                } else {
                    level = 1;
                }
                Map userInfo = new LinkedHashMap();
                userInfo.put("userid", r.getInt("UID"));
                userInfo.put("account", r.getString("AccountID"));
                userInfo.put("username", r.getString("UserName"));
                userInfo.put("groupid", r.getString("GID"));
                userInfo.put("group", r.getString("GroupName"));
                userInfo.put("level", level);
                userInfo.put("email", r.getString("Email"));
                userInfo.put("chartbuilder",r.getBoolean("ChartBuilder"));
                userList.add(userInfo);
            }
            if (userList.size() != 0) {
                return messageList("success", "userlist", userList);
            } else {
                return message("error", "Not any user");
            }
        } catch (Exception e) {
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
                //DBClient
                PStmt queryBean=null;
                DBResult rs=null;
                DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
                //Check User Account and Password
                String sql = "SELECT * FROM  `User`  U LEFT JOIN `Groups`  G ON  U.Gid=G.GID WHERE U.UID=?  AND U.Enable=1";

                queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                        UID
                });

                rs=dbClient.execute(queryBean);
                if(!rs.isSuccess())
                    throw rs.getException();
                Map userInfo = new LinkedHashMap();
                userInfo.put("status","success");
                userInfo.put("time",TimeUtil.getCurrentTime());
                for(Row row:rs.getRowList()){
                    KadoRow r=new KadoRow(row);
                    int level = 0;
                    if (r.getBoolean("Admin") == true && r.getBoolean("General") == false) {
                        level = 2;
                    } else if (r.getBoolean("Admin") == false && r.getBoolean("General") == true) {
                        level = 0;
                    } else {
                        level = 1;
                    }
                    userInfo.put("userid", r.getInt("UID"));
                    userInfo.put("account", r.getString("AccountID"));
                    userInfo.put("username", r.getString("UserName"));
                    userInfo.put("groupid", r.getString("GID"));
                    userInfo.put("group", r.getString("GroupName"));
                    userInfo.put("level", level);
                    userInfo.put("email", r.getString("Email"));
                    userInfo.put("chartbuilder",r.getBoolean("ChartBuilder"));
                }
                Gson gson=new Gson();
                return gson.toJson(userInfo);
            }else {
                return message("error","User ID is not exist");
            }
        } catch (Exception e) {
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
        //DBClient
        Query queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        //Check User Account and Password
        String sql = "SELECT * FROM `Groups`";

        try {
            queryBean=new Query("kado-meta",sql);
            rs=dbClient.execute(queryBean);
            if(!rs.isSuccess())
                throw rs.getException();
            List<Map> groupList=new ArrayList<>();
            for(Row row:rs.getRowList()){
                KadoRow r=new KadoRow(row);
                Map groupInfo=new LinkedHashMap();
                groupInfo.put("groupid",r.getInt("GID"));
                groupInfo.put("group", r.getString("GroupName"));
                groupInfo.put("group_info",r.getString("Memo"));
                groupList.add(groupInfo);
            }
            if(groupList.size()!=0){
                return messageList("success", "grouplist", groupList);
            }else {
                return message("error","Not any user");
            }
        } catch (Exception e) {
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
                //DBClient
                PStmt queryBean=null;
                DBResult rs=null;
                DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
                //Check User Account and Password
                String sql = "UPDATE `User` SET  `Enable` = 0  WHERE  `UID` = ?";
                queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                        UID
                });
                rs=dbClient.execute(queryBean);
                if(!rs.isSuccess())
                    throw rs.getException();

                return message("success", "User delete is success");
            }else {
                return message("error","User ID is not exist");

            }
        } catch (Exception e) {
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
                //DBClient
                PStmt queryBean=null;
                DBResult rs=null;
                DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
                //Check User Account and Password
                String sql = "DELETE FROM `Groups`  WHERE  `GID` = ?";
                queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                        GID
                });
                rs=dbClient.execute(queryBean);
                if(!rs.isSuccess())
                    throw rs.getException();

                return message("success", "Group delete is success");
            }else {
                return message("error","Group ID is not exist");

            }
        } catch (Exception e) {
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
    public boolean gidIsExist(int groupID) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        //Check User Account and Password
        String sql = "select `GroupName` from `Groups` where  GID=?";

        queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                groupID
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();
        return rs.getRowSize()>0;
    }

    /**
     * Check group name Exist
     * @param groupName
     * @return is Exist?
     * @throws SQLException
     */
    public boolean groupIsExist(String groupName) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        //Check User Account and Password
        String sql = "select `GID` from `Groups` where  GroupName=?";

        queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                groupName
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();

        return rs.getRowSize()>0;
    }
    /**
     * Check UID Exist
     * @param userID
     * @return is Exist?
     * @throws SQLException
     */
    public boolean uidIsExist(int userID) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        //Check User Account and Password
        String sql = "select `UserName` from `User` where  UID=?  AND Enable=1";

        queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                userID
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();

        return rs.getRowSize()>0;
    }

    /**
     * Check account Exist
     * @param accountID
     * @return is Exist?
     * @throws SQLException
     */
    public boolean accountIsExist(String accountID) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        //Check User Account and Password
        String sql = "select `UserName` from `User` where  AccountID=?  AND Enable=1";

        queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                accountID
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();

        return rs.getRowSize()>0;
    }

    /**
     * Check password is correct
     * @param UID
     * @param password
     * @return is correct?
     * @throws SQLException
     */
    public boolean checkPassword(int UID,String password) throws Exception {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        //Check User Account and Password
        String sql = "select `AccountID` from `User`  where UID=? AND Password=?  AND Enable=1";

        queryBean=PStmt.buildQueryBean("kado-meta",sql,new Object[]{
                UID,
                password
        });
        rs=dbClient.execute(queryBean);
        if(!rs.isSuccess())
            throw rs.getException();

        return rs.getRowSize()>0;

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
            if (Strings.isNullOrEmpty(verify.get(0).toString()))
                return false;
            else if((Integer)verify.get(0)==2)
                return true;
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return false;
    }
    public boolean isSelfToken(int UID,String token){
        Auth auth=new Auth();
        try {
            ArrayList<Object> verify=auth.verify(token);
            if((Integer)verify.get(2)==UID)
                return true;
            else
                return false;
        } catch (Exception e) {
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
