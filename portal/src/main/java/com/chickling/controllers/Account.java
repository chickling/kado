package com.chickling.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.chickling.models.AccountManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Account management and authority RESTful API
 * Created by ey67 on 2015/11/25.
 */
@Path("/account")
public class Account {
    /*Log4J*/
    Logger log = LogManager.getLogger(Account.class);
    /**
     * User Login
     * @param json [Login info account;password...]
     * @return [Login message token;...]
     * @throws UnsupportedEncodingException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(String json) {
        AccountManager accountManager=new AccountManager();
        Type type = new TypeToken<Map>() {}.getType();
        Gson gson = new Gson();
        String account="";
        String password="";

        try {
            Map datas = gson.fromJson(json, type);
            account=(String) datas.get("account");
            password=(String) datas.get("password");

        }catch (JsonSyntaxException e){
            log.error(e);
            return  Response.ok(accountManager.message("error", e.getMessage())).build();
        }catch (ClassCastException e){
            log.error(e);
            return Response.ok(accountManager.message("error", "Json Class Cast Exception")).build();
        }
        return Response.ok(accountManager.login(account,password)).build();
    }

    /**
     * User Logout
     * @param token
     * @return [Logout message success?]
     * @throws UnsupportedEncodingException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @GET
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@HeaderParam("AUTHORIZATION") String token) {
         AccountManager accountManager=new AccountManager();
        return Response.ok(accountManager.logout(token)).build();
    }

    /**
     * Add user [Only Admin]
     * @param json [user info ]
     * @param token
     * @return [message success?]
     * @throws UnsupportedEncodingException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @POST
    @Path("/user/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(String json,@HeaderParam("AUTHORIZATION") String token)  {
        AccountManager accountManager=new AccountManager();
        if(accountManager.isAdmin(token)) {
            Type type = new TypeToken<Map>() {
            }.getType();
            Gson gson = new Gson();
            try {
                Map datas = gson.fromJson(json, type);
                return Response.ok(accountManager.addUser((String) datas.get("username"), (String) datas.get("account"), (String) datas.get("password"), (String) datas.get("email"), ((Double) datas.get("group")).intValue(), ((Double) datas.get("level")).intValue(),(Boolean)datas.get("chartbuilder"))).build();
            } catch (JsonSyntaxException e) {
                log.error(e);
                return Response.ok(accountManager.message("error", e.getMessage())).build();
            } catch (NullPointerException e) {
                log.error(e);
                return Response.ok(accountManager.message("error", "Value can not be null")).build();
            } catch (ClassCastException e) {
                log.error(e);
                return Response.ok(accountManager.message("error", "Json Class Cast Exception")).build();
            }
        }else{
            return Response.ok(accountManager.message("error", "Permission denied")).build();
        }

    }

    /**
     * Add group [Only Admin]
     * @param json [group info ]
     * @param token
     * @return [message success?]
     * @throws UnsupportedEncodingException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @POST
    @Path("/group/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addGroup(String json,@HeaderParam("AUTHORIZATION") String token)  {
        AccountManager accountManager=new AccountManager();
        if(accountManager.isAdmin(token)) {
            Type type = new TypeToken<Map>() {
            }.getType();
            Gson gson = new Gson();
            try {
                Map datas = gson.fromJson(json, type);
                return Response.ok(accountManager.addGroup((String) datas.get("group"), (String) datas.get("group_info"))).build();
            } catch (JsonSyntaxException e) {
                log.error(e);
                return Response.ok(accountManager.message("error", e.getMessage())).build();
            } catch (NullPointerException e) {
                log.error(e);
                return Response.ok(accountManager.message("error", "Value can not be null")).build();
            } catch (ClassCastException e) {
                log.error(e);
                return Response.ok(accountManager.message("error", "Json Class Cast Exception")).build();
            }
        }else {
            return Response.ok(accountManager.message("error", "Permission denied")).build();
        }

    }

    /**
     * Update user data [Only Admin]
     * @param json [user info]
     * @param userID
     * @param token
     * @return [message success?]
     * @throws UnsupportedEncodingException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @POST
    @Path("/user/update/{userid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(String json,@PathParam("userid") int userID,@HeaderParam("AUTHORIZATION") String token)  {

        AccountManager accountManager=new AccountManager();
        if(accountManager.isAdmin(token)) {
            Type type = new TypeToken<Map>() {
            }.getType();
            Gson gson = new Gson();
            try {
                Map datas = gson.fromJson(json, type);
                return Response.ok(accountManager.updateUser(userID, (String) datas.get("username"), (String) datas.get("account"), (String) datas.get("password"), (String) datas.get("email"), ((Double) datas.get("group")).intValue(), ((Double) datas.get("level")).intValue(),(Boolean)datas.get("chartbuilder"))).build();
            } catch (JsonSyntaxException e) {
                log.error(e);
                return Response.ok(accountManager.message("error", e.getMessage())).build();
            } catch (NullPointerException e) {
                log.error(e);
                return Response.ok(accountManager.message("error", "Value can not be null")).build();
            } catch (ClassCastException e) {
                log.error(e);
                return Response.ok(accountManager.message("error", "Json Class Cast Exception")).build();
            }
        }else {
            return Response.ok(accountManager.message("error", "Permission denied")).build();
        }

    }

    /**
     * Update user password
     * @param json [user old&new password]
     * @param userID
     * @param token
     * @return [message success?]
     * @throws UnsupportedEncodingException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @POST
    @Path("/user/update/password/{userid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(String json,@PathParam("userid") int userID,@HeaderParam("AUTHORIZATION") String token){

        AccountManager accountManager=new AccountManager();
        if(accountManager.isSelfToken(userID,token)) {
            Type type = new TypeToken<Map>() {
            }.getType();
            Gson gson = new Gson();
            try {
                Map datas = gson.fromJson(json, type);
                return Response.ok(accountManager.updatePassword(userID, (String) datas.get("password"), (String) datas.get("oldpassword"))).build();
            } catch (JsonSyntaxException e) {
                log.error(e);
                return Response.ok(accountManager.message("error", e.getMessage())).build();
            } catch (NullPointerException e) {
                log.error(e);
                return Response.ok(accountManager.message("error", "value can not be null")).build();
            } catch (ClassCastException e) {
                log.error(e);
                return Response.ok(accountManager.message("error", "Json Class Cast Exception")).build();
            }
        }else {
            return Response.ok(accountManager.message("error", "Permission denied")).build();
        }

    }

    /**
     * Update Group info [Only Admin]
     * @param json [group info]
     * @param groupID
     * @param token
     * @return [message success?]
     * @throws UnsupportedEncodingException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @POST
    @Path("/group/update/{groupid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateGroup(String json,@PathParam("groupid") int groupID,@HeaderParam("AUTHORIZATION") String token)  {
        AccountManager accountManager=new AccountManager();
        if(accountManager.isAdmin(token)) {
            Type type = new TypeToken<Map>() {
            }.getType();
            Gson gson = new Gson();
            try {
                Map datas = gson.fromJson(json, type);
                return Response.ok(accountManager.updateGroup(groupID, (String) datas.get("group"), (String) datas.get("group_info"))).build();
            } catch (JsonSyntaxException e) {
                log.error(e);
                return Response.ok(accountManager.message("error", e.getMessage())).build();
            } catch (NullPointerException e) {
                log.error(e);
                return Response.ok(accountManager.message("error", "Value can not be null")).build();
            } catch (ClassCastException e) {
                log.error(e);
                return Response.ok(accountManager.message("error", "Json Class Cast Exception")).build();
            }
        }else {
            return Response.ok(accountManager.message("error", "Permission denied")).build();
        }

    }

    /**
     * List all user
     * @param token
     * @return [user info list]
     * @throws UnsupportedEncodingException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @GET
    @Path("/user/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserList(@HeaderParam("AUTHORIZATION") String token) {
        AccountManager accountManager=new AccountManager();
        if(accountManager.isAdmin(token)) {
            return Response.ok(accountManager.getUserList()).build();
        }else {
            return Response.ok(accountManager.message("error", "Permission denied")).build();
        }
    }

    /**
     * Get user info [Only Admin]
     * @param userID
     * @param token
     * @return [user info]
     * @throws UnsupportedEncodingException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @GET
    @Path("/user/get/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserInfo(@PathParam("userid") int userID,@HeaderParam("AUTHORIZATION") String token) {
        AccountManager accountManager=new AccountManager();
        if(accountManager.isAdmin(token)) {
            return Response.ok(accountManager.getUserInfo(userID)).build();
        }else {
            return Response.ok(accountManager.message("error", "Permission denied")).build();
        }
    }

    /**
     * Get group list [Only Admin]
     * @param token
     * @return [group list]
     * @throws UnsupportedEncodingException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @GET
    @Path("/group/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupList(@HeaderParam("AUTHORIZATION") String token) {
        AccountManager accountManager=new AccountManager();
        if(accountManager.isAdmin(token)) {
            return Response.ok(accountManager.getGroupList()).build();
        }else {
            return Response.ok(accountManager.message("error", "Permission denied")).build();
        }
    }

    /**
     * Delete User [Only Admin]
     * @param userID
     * @param token
     * @return [message success?]
     * @throws UnsupportedEncodingException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @GET
    @Path("/user/delete/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delUser(@PathParam("userid") int userID,@HeaderParam("AUTHORIZATION") String token) {
        AccountManager accountManager=new AccountManager();
        if(accountManager.isAdmin(token)) {
            return Response.ok(accountManager.delUser(userID)).build();
        }else {
            return Response.ok(accountManager.message("error", "Permission denied")).build();
        }
    }

    /**
     * Delete Group [Only Admin]
     * @param groupID
     * @param token
     * @return [message success?]
     * @throws UnsupportedEncodingException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @GET
    @Path("/group/delete/{groupid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delGroup(@PathParam("groupid") int groupID,@HeaderParam("AUTHORIZATION") String token) {
        AccountManager accountManager=new AccountManager();
        if(accountManager.isAdmin(token)) {
            return Response.ok(accountManager.delGroup(groupID)).build();
        }else {
            return Response.ok(accountManager.message("error", "Permission denied")).build();
        }
    }
}
