package com.chickling.bean.job;

/**
 * Created by jw6v on 2015/12/23.
 */
public class User {
    private int Permission=Integer.MIN_VALUE;
    private int GroupID=Integer.MIN_VALUE;
    private int UserID=Integer.MIN_VALUE;
    private String UserName="";
    private Boolean LogIn=false;

    public User(){}
    public User(int Permission,int GroupID,int UserID,String UserName,Boolean LogIN)
    {
        this.Permission=Permission;
        this.GroupID=GroupID;
        this.UserID=UserID;
        this.UserName=UserName;
        this.LogIn=LogIN;
    }

    public int getPermission(){return this.Permission;}
    public int getGroupID(){return this.GroupID;}
    public int getUserID(){return this.UserID;}
    public String getUserName(){return this.UserName;}
    public Boolean getLogIN(){return this.LogIn;}
    public void setPermission(int Permission){this.Permission=Permission;}
    public void setGroupID(int GroupID){this.GroupID=GroupID;}
    public void setUserID(int UserID){this.UserID=UserID;}
    public void setUserName(String UserName){this.UserName=UserName;}
    public void setLogIn(Boolean LogIn){this.LogIn=LogIn;}
}
