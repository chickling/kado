package com.chickling.models.writer;


import java.util.concurrent.Callable;

/**
 * Created by gl08 on 2016/9/23.
 */
 public  interface ResultWriter extends  Callable {

    void init(Object parameter);

    String getException();

}
