package com.chickling.models.writer;

import java.util.concurrent.Callable;

/**
 * Created by gl08 on 2016/9/23.
 */
 public  abstract class ResultWriter implements Callable{

    public abstract void init(Object parameter);

    public   StringBuilder exception=new StringBuilder();

    public String getException() {
        return exception.toString();
    }

    public void setException(String exception) {
        this.exception.append(exception).append("\n");
    }

}
