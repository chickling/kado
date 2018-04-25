package com.chickling.models;

import com.chickling.boot.Init;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by ey67 on 2018/2/1.
 */
public class PrestoJDBC {
    private static volatile PrestoJDBC prestoJDBC;
    Connection conn=null;
    private void init(){
        try {
            Class.forName("com.facebook.presto.jdbc.PrestoDriver");
            Properties prop=new Properties();
            prop.setProperty("user",Init.getPresto_user());
            conn = DriverManager.getConnection(getPrestoJDBCURL(),prop);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public synchronized static PrestoJDBC getInstance() {
        if (null == prestoJDBC) {
            prestoJDBC = new PrestoJDBC();
            prestoJDBC.init();
        }
        return prestoJDBC;
    }
    public synchronized  Connection getConnection() throws SQLException {
        if(conn.isClosed())
            init();
        return conn;
    }

    public static void main(String[] args) throws SQLException {

        ResultSet rs= PrestoJDBC.getInstance().getConnection().createStatement().executeQuery("CREATE EXTERNAL TABLE mars.truesight_ipinfo_test(\n" +
                "  ipstartint64 bigint,\n" +
                "  ipendint64 bigint,\n" +
                "  ipstart string,\n" +
                "  ipend string,\n" +
                "  org string,\n" +
                "  countrycode string,\n" +
                "  countryname string,\n" +
                "  region string,\n" +
                "  cityname string,\n" +
                "  zipcode string,\n" +
                "  latitude float,\n" +
                "  longitude float,\n" +
                "  statename string)\n" +
                "ROW FORMAT DELIMITED\n" +
                "  FIELDS TERMINATED BY ','\n" +
                "STORED AS TEXTFILE\n" +
                "LOCATION\n" +
                "  '/user/ec/truesight/ipinfo_new'");
        while (rs.next()) {
            System.out.println(rs.getString(1));
        }
    }
    public static String getPrestoJDBCURL(){
        return "jdbc:"+ Init.getPrestoURL().replaceFirst("http","presto")+"/"+Init.getPrestoCatalog();
    }
}
