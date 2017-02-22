package com.chickling.models.jdbc.dbselect;

/**
 * @author: aj65
 */
public class ManagerConfig {
    public static class DataSource {
        private String name;
        private String url;
        private String username;
        private String passwd;
        private Integer group ;

        public DataSource() {
        }

        public DataSource(String name, String url, String username, String passwd, Integer group) {
            this.name = name;
            this.url = url;
            this.username = username;
            this.passwd = passwd;
            if (null != group) {
                this.group = group;
            }
        }

        public Integer getGroup() {
            return group;
        }

        public void setGroup(Integer group) {
            this.group = group;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPasswd() {
            return passwd;
        }

        public void setPasswd(String passwd) {
            this.passwd = passwd;
        }
    }

    private String JDBCClassDriver;
    private int poolSize;
    private DataSource[] dataSourceList;

    public String getJDBCClassDriver() {
        return JDBCClassDriver;
    }

    public void setJDBCClassDriver(String JDBCClassDriver) {
        this.JDBCClassDriver = JDBCClassDriver;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public DataSource[] getDataSourceList() {
        return dataSourceList;
    }

    public void setDataSourceList(DataSource[] dataSourceList) {
        this.dataSourceList = dataSourceList;
    }
}
