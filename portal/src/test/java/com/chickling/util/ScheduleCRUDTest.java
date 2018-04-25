package com.chickling.util;

import org.junit.Test;

import java.util.Map;

/**
 * Created by ey67 on 2018/4/12.
 */
public class ScheduleCRUDTest {
    @Test
    public void getScheduleJobandTime() throws Exception {
        Map map=ScheduleCRUDUtils.getAllScheduleJobandTime();
        System.out.println(map);
    }
}
