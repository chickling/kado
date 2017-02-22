package com.chickling.models.jdbc.dbselect;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Created by dl85 on 2016/4/6.
 */
public class ConnectionStatus {
    private Boolean isActive;
    private Boolean isMaintenance;
    private String dsName;
    private Integer group;
    private ComboPooledDataSource cpds;

    public Boolean getMaintenance() {
        return isMaintenance;
    }

    public void setMaintenance(Boolean maintenance) {
        isMaintenance = maintenance;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }

    public ComboPooledDataSource getCpds() {
        return cpds;
    }

    public void setCpds(ComboPooledDataSource cpds) {
        this.cpds = cpds;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getDsName() {
        return dsName;
    }

    public void setDsName(String dsName) {
        this.dsName = dsName;
    }


    public Boolean isConn() {
        return this.isActive && (this.isMaintenance==Boolean.FALSE);
    }
}
