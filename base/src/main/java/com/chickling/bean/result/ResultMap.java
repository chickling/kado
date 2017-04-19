package com.chickling.bean.result;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gl08 on 2017/4/10.
 */
public class ResultMap {

    private int count;
    private int start;
    private List<String> schema=new ArrayList<>();
    private List<String> type=new ArrayList<>();
    private List<List<Object>> data=new ArrayList<>();

    public ResultMap() {
    }

    public ResultMap(int count, int start, List<String> schema, List<String> type, List<List<Object>> data) {
        this.count = count;
        this.start = start;
        this.schema = schema;
        this.type = type;
        this.data = data;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public List<String> getSchema() {
        return schema;
    }

    public void setSchema(List<String> schema) {
        this.schema = schema;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public List<List<Object>> getData() {
        return data;
    }

    public void setData(List<List<Object>> data) {
        this.data = data;
    }

}
