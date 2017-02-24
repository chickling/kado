package com.chickling.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.routing.RoutingAppender;
import org.apache.logging.log4j.core.config.AppenderControl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by jw6v on 2016/2/15.
 */
public class StopLogger {

    public synchronized static void stopLogger(Logger log){

        log.info("stop logger");
        String key = ThreadContext.get("logFileName");
        org.apache.logging.log4j.core.Logger coreLogger = (org.apache.logging.log4j.core.Logger) log;
        org.apache.logging.log4j.core.LoggerContext context = (org.apache.logging.log4j.core.LoggerContext)coreLogger.getContext();
        RoutingAppender appender = (RoutingAppender) context.getConfiguration().getAppender("RoutingAppender");
        try
        {
            Method method = appender.getClass().getDeclaredMethod("getControl",String.class,LogEvent.class);
            method.setAccessible(true);
            AppenderControl appenderControl = (AppenderControl) method.invoke(appender,key,null);
            appenderControl.getAppender().stop();
        }
        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            // Shouldn't happen - log anyway
            log.error("Unable to close the logger",e);
        }

    }
}

