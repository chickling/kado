package com.chickling.util;


import owlstone.dbclient.db.DBClient;
import owlstone.dbclient.db.module.DBResult;
import owlstone.dbclient.db.module.PStmt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;



/**
 * Created by jw6v on 2017/1/18.
 */
public class CheckQuery {
    private static Logger log = LogManager.getLogger(CheckQuery.class);

public static boolean recentQuery(int jobID,int period){
    //todo test and check
    try {
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        String query = "Select `JobStopTime` from `Job_History` where `JobID`=?";

        queryBean=PStmt.buildQueryBean("kado-meta",query,new Object[]{
                jobID
        });
        rs=dbClient.execute(queryBean);

        if(!rs.isSuccess())
            throw rs.getException();
        KadoRow r=new KadoRow(rs.getRowList().get(0));
        String result=r.getString("JobStopTime");
        DateTime current = new DateTime();
        DateTime stopTime=TimeUtil.String2DateTime(result);
        if((current.getMillis()-stopTime.getMillis())>period){
            return true;
        }
        else{
            return false;
        }
    }catch(Exception sqle){
        log.error("SQLexception: "+sqle);
        return false;
    }
}

}
