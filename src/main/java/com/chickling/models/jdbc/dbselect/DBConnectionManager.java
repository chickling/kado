package com.chickling.models.jdbc.dbselect;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: aj65
 */
public class DBConnectionManager {
    private final static Logger log = LogManager.getLogger(DBConnectionManager.class);
    private final static String CONFIG_FILE_NAME = "dbselect-config.yaml";

    private static Map<String, ConnectionStatus> dsMap = new ConcurrentHashMap();
    private static DBConnectionManager instance;

    private DBConnectionManager() {
        //
        // read config file
        //
        log.info("Initialize DBConnectionManager, reading config file...");
        URL url = Thread.currentThread().getContextClassLoader().getResource(CONFIG_FILE_NAME);
        if (null == url) {
            log.error("File {} can not be found in class path", CONFIG_FILE_NAME);
        } else {

            Constructor constructor = new Constructor();
            constructor.addTypeDescription(new TypeDescription(ManagerConfig.class, "!dbselect-config"));

            Yaml yaml = new Yaml(constructor);
            ManagerConfig managerConfig = null;
            try {
                InputStream inputStream = url.openStream();
                managerConfig = (ManagerConfig) yaml.load(inputStream);
                log.info("Load file from {}", url.getFile());
            } catch (Exception e) {
                log.error("Load DBConnectionManager config file " + CONFIG_FILE_NAME + " fail, check your file format is correct !", e);
                managerConfig = null;
            }

            if (null != managerConfig) {
                String jdbcClassName = managerConfig.getJDBCClassDriver();
                final int poolSize = managerConfig.getPoolSize();
                for (ManagerConfig.DataSource ds : managerConfig.getDataSourceList()) {
                    this.addDSConn(jdbcClassName, ds.getName(), ds.getUrl(), ds.getUsername(), ds.getPasswd(), ds.getGroup(), poolSize);
                }
            }
        }
    }

    public static synchronized DBConnectionManager getInstance() {

        if (null == instance) {
            synchronized (DBConnectionManager.class) {
                if (null == instance) {
                    instance = new DBConnectionManager();
                }
            }
        }
        return instance;
    }

    public Connection getDBConnection(ConnectionStatus cs) {
        return this.getDBConnection(cs.getDsName());
    }

    public Connection getDBConnection(String name) {

        ConnectionStatus cs = dsMap.get(name);
        Connection con = null;
        //check the connection is alive
        if (cs.getActive()) {
            ComboPooledDataSource cpds = cs.getCpds();
            if (null != cpds) {
                try {
                    con = cpds.getConnection();
                } catch (SQLException e) {
                    cs.setActive(Boolean.FALSE);
                    log.error("get db connection fail for name {}. ", name, e);
                }
            }
        }


        if (null == con) {
            log.error("can't get db connection {}", name);
        }

        return con;
    }



    public synchronized void removeAllConnection() {
        if (null != instance) {

            for (ConnectionStatus cs : dsMap.values()) {
                ComboPooledDataSource cpds = cs.getCpds();
                cpds.close();
            }
            dsMap.clear();
            instance = null;
        }
    }

    public void recycleDBConneciton(Connection conn) {
        recycleDBConneciton(null, null, conn);
    }

    public void recycleDBConneciton(Statement stmt, ResultSet rs, Connection conn) {
        if (null != rs) {
            try {
                rs.close();
            } catch (SQLException e) {
            } finally {
                rs = null;
            }
        }

        if (null != stmt) {
            try {
                stmt.close();
            } catch (SQLException e) {
            } finally {
                stmt = null;
            }
        }
        if (null != conn) {
            try {
                conn.close();
            } catch (SQLException sqle) {
                log.warn("connection recycle fail.", sqle);
            } finally {
                conn = null;
            }
        }
    }

    public List<ManagerConfig.DataSource> getAllDS() {
        List<ManagerConfig.DataSource> ret = new ArrayList();
        ComboPooledDataSource ds = null;
        for (String name : dsMap.keySet()) {
            ConnectionStatus cs = dsMap.get(name);

            ds = cs.getCpds();
            ret.add(new ManagerConfig.DataSource(name, ds.getJdbcUrl(), ds.getUser(), ds.getPassword(), cs.getGroup()));
        }

        return ret;
    }

    public boolean addDSConn(String jdbcClassName, String name, String url, String acc, String pw, Integer group, int maxPoolSize) {
        boolean ret = false;
        if (dsMap.containsKey(name)) {
            return true;
        }
        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        try {
            comboPooledDataSource.setDriverClass(jdbcClassName);
        } catch (PropertyVetoException e) {
            log.error("Load JDBC Class {} fail. ", jdbcClassName, e);
        }


        comboPooledDataSource.setJdbcUrl(url);
        comboPooledDataSource.setUser(acc);
        comboPooledDataSource.setPassword(pw);
        comboPooledDataSource.setMinPoolSize(maxPoolSize);
        //comboPooledDataSource.setAcquireIncrement(5);
        comboPooledDataSource.setMaxPoolSize(maxPoolSize);
        comboPooledDataSource.setAcquireRetryAttempts(3);
        comboPooledDataSource.setAcquireRetryDelay(8000);
        //comboPooledDataSource.setUnreturnedConnectionTimeout(5000);
        comboPooledDataSource.setCheckoutTimeout(30000);
        comboPooledDataSource.setDataSourceName(name);


        ConnectionStatus cs = new ConnectionStatus();
        cs.setDsName(name);
        cs.setCpds(comboPooledDataSource);
        cs.setGroup(group);
        dsMap.put(name, cs);

        try {
            // test connection
            Connection tempConnn = comboPooledDataSource.getConnection();
            this.recycleDBConneciton(tempConnn);
            cs.setActive(Boolean.TRUE);
            ret = true;
            log.info("Added {} connections into pool of DataSource={}", maxPoolSize, url);
        } catch (SQLException e) {
            log.warn("add connection {} to dsMap fail. ", name, e);
            cs.setActive(Boolean.FALSE);
        }

        return ret;
    }


    public boolean removeDSConn(String name) {
        return dsMap.remove(name) != null;
    }

    /**
     * get same group connection
     *
     * @param dsName
     * @param group
     * @return
     */
    private List<ConnectionStatus> getGroups(String dsName, Integer group) {
        if (null == group) {
            return null;
        }
        List<ConnectionStatus> csList = new ArrayList<>();
        for (ConnectionStatus cs : dsMap.values()) {
            if (cs.getDsName().equals(dsName)) {
                continue;
            }
            if (cs.getActive() == Boolean.FALSE) {
                continue;
            }
            if (cs.getGroup().equals(group)) {
                csList.add(cs);
            }
        }
        return csList;
    }
    /**
     * get db connection status object
     *
     * @param name
     * @return
     */
    public ConnectionStatus getDBConnectionStatus(String name) {

        ConnectionStatus cs = dsMap.get(name);
        ConnectionStatus failedCs = null;

        if (null == cs) {
            return null;
        }
        if (cs.getActive() == Boolean.FALSE) {
            failedCs = cs;
        }

        //check group connection is alive
        if (null != failedCs) {
            List<ConnectionStatus> csList = this.getGroups(failedCs.getDsName(), failedCs.getGroup());
            if (null != csList && csList.size() > 0) {
                cs = csList.get(0);
            }
        }

        if (null == cs || cs.getActive() == Boolean.FALSE) {
            log.error("all db connection is down");
        } else {
            log.debug("use {} database", cs.getDsName());
        }
        return cs;
    }


    /**
     * get failed db connection
     *
     * @return
     */
    public List<ConnectionStatus> getFailedConn() {
        List<ConnectionStatus> tmp = new ArrayList<>();
        for (ConnectionStatus cs : dsMap.values()) {
            if (cs.getActive() == Boolean.FALSE) {
                tmp.add(cs);
            }
        }
        return tmp;
    }

    /**
     * get not failed db connection,include alive and maintain connection
     *
     * @return
     */
    public List<ConnectionStatus> getNotFailedConn() {
        List<ConnectionStatus> tmp = new ArrayList<>();
        for (ConnectionStatus cs : dsMap.values()) {
            if (cs.getActive() != Boolean.FALSE) {
                tmp.add(cs);
            }
        }
        return tmp;
    }

    /**
     * get could work database connection
     *
     * @return
     */
    public List<ConnectionStatus> getAliveConn() {
        List<ConnectionStatus> tmp = new ArrayList<>();
        for (ConnectionStatus cs : dsMap.values()) {
            if (cs.isConn()) {
                tmp.add(cs);
            }
        }
        return tmp;
    }

    /**
     * get specific db connection
     *
     * @param dbUrl
     * @return
     */
    public ConnectionStatus getXDataBase(String dbUrl) {
        for (ConnectionStatus cs : dsMap.values()) {
            if (cs.getActive() == Boolean.FALSE) {
                continue;
            }
            if (cs.getCpds().getJdbcUrl().toLowerCase().indexOf(dbUrl.toLowerCase()) != -1) {
                return cs;
            }
        }
        return null;
    }
}