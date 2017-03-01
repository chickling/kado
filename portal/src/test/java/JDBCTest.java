//
//
//
//import com.chickling.models.jdbc.dbselect.DBConnectionManager;
//
//import java.sql.Connection;
//
//import java.sql.Statement;
//
//
///**
// * Created by gl08 on 2016/1/6.
// */
//public class JDBCTest {
//
//
//
//
//
//
//    public static void main(String[] args) throws Exception {
//    String connName="S7BIDB08";
//        DBConnectionManager dbconn = DBConnectionManager.getInstance();
//
//        Connection conn=dbconn.getDBConnection(connName);
//
//        conn.close();
//
//        if (conn.isClosed()) {
//            dbconn.recycleDBConneciton(conn);
//            dbconn.getDBConnection(connName).close();
////            dbconn.removeDSConn(connName);
//
//        }
//
//
////        dbconn=DBConnectionManager.getInstance();
//
//        conn=dbconn.getDBConnection("S7BIDB08");
//
//        if (!conn.isClosed())
//            System.out.println("Reconnect !!!");
//        int pause=0;
//        String a="insert into DataMining.dbo.EC_NVTCSummary (sonumber,dt,nvtc,firstvisit,previousvisit,currentvisit,diff,count_www,count_secure,count_review) select * from (VALUES(?,'20160429' ,'248326808.0001.37496833.1455130234.1461962486.1461964292.13','2016-02-10 18:50:34.000','2016-04-29 20:41:26.000' ,'2016-04-29 21:11:32.000' ,6834058 ,0 ,16 ,0)) as a(sonumber,dt,nvtc,firstvisit,previousvisit,currentvisit,diff,count_www,count_secure,count_review) where not exists (select top 1 1 from DataMining.dbo.EC_NVTCSummary b with(nolock) where a.sonumber=b.sonumber)";
//
//
//
//        Statement state=conn.createStatement();
//        Statement state1=conn.createStatement();
//        Statement state2=conn.createStatement();
//        Statement state3=conn.createStatement();
//
//        state.executeUpdate(null);
//
////        ps.executeUpdate();
////        ps.executeBatch();
//
//        state.close();
//        conn.close();
//
////        SQLoption sqlOption = new SQLoption(dbconn, "Presto");
////        Connection conn=dbconn.getDBConnection("Presto");
//
////        List<String> sqlList = new ArrayList<>();
////        for (int i = 0; i < 10; i++) {
////            String insertSql = "insert into Ecommerce.dbo.EC_CrawlerList (Content,utma,TotalClicks,Memo,[Type],[Level])  values('TTT' , 'CCC',50" + i * 10 + ",null,'4',5)";
////            sqlList.add(insertSql);
////        }
////        sqlOption.execute("INSERT INTO [Presto].[dbo].[syn] (item,country,numner,icc,time)  VALUES('00-000-001','usa',1003,'shoppingcart',50)");
////        try {
////             sqlOption.batchExecute(sqlList);
////        } catch (Exception e) {
////            throw new SQLException(e);
////        }
//
////        if (null!=conn) {
////            try {
////                conn.setAutoCommit(false);
////                Statement state = conn.createStatement();
////                for (int i = 0 ; i <10 ;i ++){
////                    String insertSql="insert into Ecommerce.dbo.EC_CrawlerList (Content,utma,TotalClicks,Memo,[Type],[Level])  values('TTT' , 'CCC',50"+i*10+",null,'4',5)";
////                    state.addBatch(insertSql);
////                }
////                state.executeBatch();
////                conn.commit();
//                state.closeOnCompletion();
//                conn.close();
////            }catch (Exception e){
////                conn.rollback();
////                throw new SQLException(e);
////            }
//    }
//}