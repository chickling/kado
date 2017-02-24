package com.chickling.dbselect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dl85 on 2015/4/16.
 */
public class DBUpdate extends DBBase {
    private final static Logger log = LogManager.getLogger(DBSelect.class);

    public DBUpdate(DBConnectionManager connectionManager) {
        super(connectionManager);
    }


    /**
     * update data
     *
     * @param sql
     * @param connName
     * @param params
     * @return
     */
    public int update(String sql, String connName, Object... params) {

        int status = 0;
        ConnectionStatus cs = connectionManager.getDBConnectionStatus( connName);
        if (null == cs) {
            log.info("can't update,because all db conn are out of connection ");
            return -1;
        }
        try (Connection conn = this.connectionManager.getDBConnection(cs); PreparedStatement ps = conn.prepareStatement(sql)) {

            if (null != params && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            status = ps.executeUpdate();
        } catch (Exception e) {
            cs.setActive(Boolean.FALSE);
            log.error("execute update sql fail", e);
            status = -1;
        }
        return status;
    }

    /**
     * insert single data
     *
     * @param tableName
     * @param connName
     * @param insertData
     * @param excludeColumns
     * @return
     */
    public int insert(String tableName, String connName, Map insertData, String... excludeColumns) {
        return insertBatch(tableName, connName,  new Map[]{insertData}, excludeColumns);
    }


    /**
     * insert batch datas
     *
     * @param tableName
     * @param connName
     * @param insertDatas
     * @param excludeColumns
     * @return
     */
    public int insertBatch(String tableName, String connName, Map[] insertDatas, String... excludeColumns) {
        if (insertDatas.length <= 0) {
            return -1;
        }

        String sql = this.combineSql(tableName, insertDatas[0], excludeColumns);
        ConnectionStatus cs = connectionManager.getDBConnectionStatus( connName);
        if (null == cs) {
            log.info("can't insert,because all db conn are out of connection ");
            return -1;
        }
        try (Connection conn = this.connectionManager.getDBConnection(cs); PreparedStatement ps = conn.prepareStatement(sql)) {
            int count = 0;
            for (int i = 0; i < insertDatas.length; i++) {
                Map dm = insertDatas[i];
                Set<Map.Entry> s = dm.entrySet();
                int paramCount = 1;
                for (Map.Entry entry : s) {
                    if (excludeColumnfilter(entry, excludeColumns)) {
                        continue;
                    }

                    ps.setObject(paramCount, entry.getValue());
                    paramCount++;
                }
                ps.addBatch();
                count++;
                if (count % 100 == 0) {
                    count = 0;
                    ps.executeBatch();
                }
            }
            if (count != 0) {
                ps.executeBatch();
            }
        } catch (SQLException e) {
            log.error("excute sql fail", e.fillInStackTrace());
            cs.setActive(Boolean.FALSE);
            return -1;
        }
        return 0;
    }



    /**
     * insert their own datas into target table with transaction
     *
     * @param connectName
     * @param tableNames
     * @param datas
     * @param excludeColumnsMap
     * @return
     * @throws Exception
     */
    public int insertWithTransacction(String connectName,  String[] tableNames, Map<String, Map[]> datas, Map<String, String[]> excludeColumnsMap) throws Exception {

        Connection conn = null;
        ConnectionStatus cs = connectionManager.getDBConnectionStatus( connectName);
        if (null == cs) {
            log.info("can't update,because all db conn are out of connection ");
            return -1;
        }
        try {
            int flush = 0;

            conn = connectionManager.getDBConnection(cs);
            conn.setAutoCommit(Boolean.FALSE);

            for (int i = 0; i < tableNames.length; i++) {
                if (datas.get(tableNames[i]).length <= 0) {
                    continue;
                }
                //get table's exclude columns
                String[] excludeColumns = excludeColumnsMap == null ? null : excludeColumnsMap.get(tableNames[i]);
                //get insert sql
                String sql = this.combineSql(tableNames[i], datas.get(tableNames[i])[0], excludeColumns);

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    Map[] tableDatas = datas.get(tableNames[i]);

                    for (Map m : tableDatas) {
                        int paramCount = 1;
                        Set<Map.Entry> s = m.entrySet();
                        for (Map.Entry entry : s) {
                            //filte exclude column
                            if (excludeColumnfilter(entry, excludeColumns)) {
                                continue;
                            }
                            ps.setObject(paramCount, entry.getValue());
                            paramCount++;
                        }
                        ps.addBatch();
                        flush++;
                        if (flush % 1000 == 0) {
                            ps.executeBatch();
                        }
                    }
                    if (flush > 0) {
                        ps.executeBatch();
                    }
                }
            }
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            cs.setActive(Boolean.FALSE);
            log.error("excute sql fail", e.fillInStackTrace());
            return -1;
        } finally {
            if (null != conn) {
                conn.close();
            }
        }
        return 0;
    }

    /**
     * combine insert sql
     *
     * @param tableName
     * @param data
     * @param excludeColumns
     * @return
     */
    private String combineSql(String tableName, Map data, String[] excludeColumns) {

        Set<Map.Entry> set = data.entrySet();
        StringBuilder columnBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();

        List value = new ArrayList<>();
        for (Map.Entry entry : set) {
            if (null != excludeColumns) {
                boolean isExclude = false;
                for (String columnName : excludeColumns) {
                    if (entry.getKey().equals(columnName)) {
                        isExclude = true;
                        break;
                    }
                }
                if (isExclude) {
                    continue;
                }
            }
            columnBuilder.append(entry.getKey()).append(",");
            valueBuilder.append("?").append(",");
            value.add(entry.getValue());
        }
        if (value.size() > 0) {
            columnBuilder.delete(columnBuilder.length() - 1, columnBuilder.length());
            valueBuilder.delete(valueBuilder.length() - 1, valueBuilder.length());
        }
        return "insert into " + tableName + " (" + columnBuilder.toString() + ") values(" + valueBuilder.toString() + ")";
    }

    /**
     * filte column
     *
     * @param entry
     * @param excludeColumns
     * @return
     */
    private boolean excludeColumnfilter(Map.Entry entry, String[] excludeColumns) {
        if (null != excludeColumns) {
            for (String columnName : excludeColumns) {
                if (entry.getKey().equals(columnName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
