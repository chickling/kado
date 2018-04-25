package com.chickling.models;

import java.sql.*;
import java.util.Properties;

/**
 * Created by ey67 on 2018/2/1.
 */
public class HiveJDBC {
    private static volatile HiveJDBC hijdbc;
    Connection conn=null;
    private void init(){
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
            conn = DriverManager.getConnection("jdbc:hive2://172.16.157.11:10000");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public synchronized static HiveJDBC getInstance() {
        if (null == hijdbc) {
            hijdbc = new HiveJDBC();
            hijdbc.init();
        }
        return hijdbc;
    }
    public synchronized  Connection getConnection() throws SQLException {
        if(conn.isClosed())
            init();
        return conn;
    }

    public static void main(String[] args) throws SQLException {

        ResultSet rs=HiveJDBC.getInstance().getConnection().createStatement().executeQuery("CREATE EXTERNAL TABLE mars.truesight_ipinfo_test(\n" +
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
}
