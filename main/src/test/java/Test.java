import com.chickling.DB.ConnectionManager;
import com.chickling.util.TimeUtil;

import java.sql.*;

/**
 * Created by ey67 on 2015/11/25.
 */
public class Test {
    public Test() throws SQLException {
        //DBConnectionManager dbConnectionManager=DBConnectionManager.getInstance();
        Connection con=ConnectionManager.getInstance().getConnection();

        String sql = "select * from User";
        Statement stat = null;
        ResultSet rs = null;
        stat = con.createStatement();
        rs = stat.executeQuery(sql);
        while(rs.next())
        {
            System.out.println(rs.getInt("UID")+"\t"+rs.getString("UserName"));
        }

    }
    public static void testConn() throws SQLException {
        for(int j=0;j<10;j++) {
            Thread thread = new Thread() {
                public void run() {
                    int count = 0;
                    for (int i = 0; i < 1000; i++) {
                        Connection con = null;
                        try {
                            PreparedStatement stat = null;
                            con = ConnectionManager.getInstance().getConnection();
                            String sql = "INSERT INTO `User_Login` (`UID`,`Admin`,`LoginTime`,`Token`) VALUES (?,?,?,?)";
                            stat = con.prepareStatement(sql);
                            stat.setInt(1, 0);
                            stat.setBoolean(2, true);
                            stat.setString(3, TimeUtil.getCurrentTime());
                            stat.setString(4, "test");
                            synchronized (ConnectionManager.class) {
                                stat.executeUpdate();
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        if (con == null) {
                            //System.out.println("NULL");
                            count++;
                        } else {
                            //System.out.println("SUCCESS");
//                            try {
////                                //con.close();
//                                DBConnectionManager.getInstance().close();
//                               DBConnectionManager.getInstance().close();
//                            } catch (SQLException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }
                    System.out.println(count);
                }
            };
            thread.start();
        }
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
        new Test();
        //testConn();
//        SQLiteDataSource ds;
//        SQLiteConfig config = new SQLiteConfig();
//        // config.setReadOnly(true);
//        config.setSharedCache(true);
//        config.enableRecursiveTriggers(true);
//        ds = new SQLiteDataSource(config);
//        ds.setUrl("jdbc:sqlite:PrestoJobPortal.sqlite");
//        String test="TEST";
//        test.toLowerCase();
//        System.out.print(test);
//        while (true) {
//            PreparedStatement stat = null;
//            ResultSet rs = null;
//            //Connection con = ds.getConnection();
//            stat = ConnectionManager.getInstance().getConnection().prepareStatement("SELECT JobOwner FROM Job WHERE JobID=?;");
//            stat.setString(1, "1");
//            rs = stat.executeQuery();
//            rs.close();
//            stat.close();
////            con.close();
////            DBConnectionManager.getInstance().getConnection().close();
//            Thread.sleep(100);
//            System.out.println("query");
//        }
    }
}
