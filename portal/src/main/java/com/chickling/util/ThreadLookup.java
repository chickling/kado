package com.chickling.util;

import com.facebook.presto.hive.$internal.com.google.common.base.Strings;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrLookup;


/**
 * Created by gl08 on 2017/2/7.
 */
@Plugin(name="ctx",category = StrLookup.CATEGORY)
public class ThreadLookup implements StrLookup {
    @Override
    public String lookup(String s) {
        return s;
    }

    @Override
    public String lookup(LogEvent logEvent, String s) {
        return Strings.isNullOrEmpty(logEvent.getContextData().toMap().get(s)) ?"init":logEvent.getContextData().toMap().get(s);
    }
}
