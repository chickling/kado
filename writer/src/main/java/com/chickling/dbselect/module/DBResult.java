package com.chickling.dbselect.module;


import java.util.ArrayList;
import java.util.List;

/**
 * This class wrap the result of DB query
 * @author: aj65
 */
public class DBResult {
    private boolean success;
    private String sql;
    private List<Row>      rowList;
    private String[]       columnNames;
    private ColumnType[]   columnTypes;
    private Exception exception;

    public DBResult(String sql,ColumnType[] types, String[] names) {
        assert(types!=null && columnNames!=null);
        assert(types.length == columnNames.length);

        rowList = new ArrayList();
        this.sql = sql;
        this.columnNames = names;
        this.columnTypes = types;
    }

    public DBResult(String sql) {
        this.sql = sql;
        rowList = new ArrayList();
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<Row> getRowList() {
        return rowList;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public Row getRow(int index){
        return rowList.get(index);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess() {
        this.success = true;
    }

    public void setFail(){
        this.success = false;
    }

    public void setRowList(List<Row> rowList) {
        this.rowList = rowList;
    }

    public ColumnType[] getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypes(ColumnType[] columnTypes) {
        this.columnTypes = columnTypes;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    /**
     * @param name  the name of column , case is sensitive
     * @return if find return true; otherwise false
     */
    public boolean hasColumn(String name){
        boolean ret = false;
        if(null!=columnNames)
        {
            for(String n:columnNames)
            {
                if(n.equals(name))
                {
                    ret =true;
                    break;
                }
            }
        }

        return ret;
    }

    /**
     * always use this method to add new row , DON'T use getRowList() to get a List to add new row
     * @param values
     */
    public void addRow(String[] values)
    {
        if(values!=null && values.length == columnNames.length)
        {
            Row row = new Row(this.columnTypes,this.columnNames,values);
            rowList.add(row);
        }
    }

    public Row.Cell getCell(int rowIndex, int columnIndex){
        return getRow(rowIndex).getCell(columnIndex);
    }

    public Row.Cell getCell(int rowIndex, String columnName){
        return getRow(rowIndex).getCell(columnName);
    }

    public int getRowSize(){
        return rowList.size();
    }

    public int getColumnSize(){
        return columnNames!=null?columnNames.length:0;
    }
}
