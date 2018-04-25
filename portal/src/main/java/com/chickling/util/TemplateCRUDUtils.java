package com.chickling.util;


import com.chickling.models.MessageFactory;
import com.facebook.presto.hive.$internal.org.apache.commons.lang3.exception.ExceptionUtils;
import owlstone.dbclient.db.DBClient;
import owlstone.dbclient.db.module.DBResult;
import owlstone.dbclient.db.module.PStmt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * Created by jw6v on 2017/1/10.
 */
public class TemplateCRUDUtils {

    private final static String InsertSQLTemplate="INSERT INTO `SQLtemplate` (`JobID`,`URLKey`,`SQLKey`,`DefaultValue`) VALUES (?,?,?,?);";
    private final static String DeleteSQLTemplate="DELETE FROM `SQLtemplate` WHERE `JobID`=?;";
    private final static String GetSQLTemplate="SELECT * FROM `SQLtemplate` WHERE `JobID`=?;";

    private static Logger log = LogManager.getLogger(TemplateCRUDUtils.class);

    public synchronized static boolean addSqlTemplate(int jobID, ArrayList<Map<String,String>> templateMaps){
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        try {
            List<Object[]> data=new ArrayList<>();
            for(Map templateMap: templateMaps) {
                data.add(new Object[]{
                        jobID,
                        (String) templateMap.get("URLKey"),
                        (String) templateMap.get("SQLKey"),
                        (String) templateMap.get("DefaultValue")
                });
            }

            queryBean=PStmt.buildBatchUpdateBean("kado-meta",InsertSQLTemplate,data);
            rs=dbClient.execute(queryBean);

            if(!rs.isSuccess())
                throw rs.getException();

            log.info( "Status:success; TimeStamp:"+TimeUtil.getCurrentTime()+"; JobID:"+jobID);
            return true;
        }catch (Exception sqle){
            log.error(ExceptionUtils.getMessage(sqle));
            return false;
        }
    }

    public synchronized static boolean deleteSqlTemplate(int jobID){
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());

        String querySQL = DeleteSQLTemplate;
        try {

            queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                    jobID
            });
            querySQL=queryBean.getSql();
            rs=dbClient.execute(queryBean);

            if(!rs.isSuccess())
                throw rs.getException();
            log.info( "Status:success; TimeStamp:"+TimeUtil.getCurrentTime()+"; JobID:"+jobID);
            return true;
        }catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+querySQL);
            return false;
        }
    }

    public synchronized static boolean updateSqlTemplate(int jobID, ArrayList<Map<String,String>> templateMap){
        return(deleteSqlTemplate(jobID))&& (addSqlTemplate(jobID, templateMap));
    }

    public synchronized static List<Map> readSqlTemplate(int jobID){
        //DBClient
        PStmt queryBean=null;
        DBResult rs=null;
        DBClient dbClient=new DBClient(DBClientUtil.getDbConnectionManager());
        String querySQL = "";

        try {
            querySQL = GetSQLTemplate;
            queryBean=PStmt.buildQueryBean("kado-meta",querySQL,new Object[]{
                    jobID
            });
            querySQL=queryBean.getSql();
            rs=dbClient.execute(queryBean);

            if(!rs.isSuccess())
                throw rs.getException();
            List<Map> rtn = MessageFactory.rtnTemplateMessage(rs);

            return rtn;
        }
        catch(Exception sqle){
            log.error(sqle.toString()+";SQL:"+querySQL);
            return new ArrayList<>();
        }
    }



}
