package com.chickling.util;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class TimeUtilTest {

    @Test
    public void TimetoString() {
        String time=TimeUtil.toString(new Date());
        assert  time!=null;

    }
}