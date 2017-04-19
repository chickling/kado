package com.chickling.util;

import com.chickling.sqlite.ConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by jw6v on 2017/1/18.
 */
public class CheckQuery {
    private static Logger log = LogManager.getLogger(CheckQuery.class);

public static boolean recentQuery(int jobID,int period){
    //todo test and check
    try {
        String query = "Select `JobStopTime` from `main`.`Job_History` where `JobID`=?";
        PreparedStatement stat = ConnectionManager.getInstance().getConnection().prepareStatement(query);
        stat.setInt(1, jobID);
        ResultSet rs = stat.executeQuery();
        String result=rs.getString("JobStopTime");
        DateTime current = new DateTime();
        DateTime stopTime=TimeUtil.String2DateTime(result);
        if((current.getMillis()-stopTime.getMillis())>period){
            return true;
        }
        else{
            return false;
        }
    }catch(SQLException sqle){
        log.error("SQLexception: "+sqle);
        return false;
    }
}

}
