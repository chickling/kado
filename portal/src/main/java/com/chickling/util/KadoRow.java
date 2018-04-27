package com.chickling.util;

import owlstone.dbclient.db.module.Row;

/**
 * Created by ey67 on 2018/4/2.
 */
public class KadoRow{
    private Row row;
    public KadoRow(Row row){
        this.row=row;
    }
    public String getString(String name){
        return row.getCell(name)!=null?row.getCell(name).getValue()!=null?row.getCell(name).getValue():"":"";
    }
    public Integer getInt(String name){
        return row.getCell(name)!=null?row.getCell(name).getValue()!=null?Integer.valueOf(row.getCell(name).getValue()):0:0;
    }
    public Boolean getBoolean(String name){
        if(row.getCell(name).getValue().equals("1")||row.getCell(name).getValue().toLowerCase().equals("true"))
            return true;
        else
            return false;
    }

    public Row getRow() {
        return row;
    }

    public void setRow(Row row) {
        this.row = row;
    }
}
