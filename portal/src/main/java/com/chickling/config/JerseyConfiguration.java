package com.chickling.config;

import com.chickling.controllers.*;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;

import java.util.HashSet;

/**
 * Created by ey67 on 2015/10/6.
 */
public class JerseyConfiguration extends ResourceConfig {
    public JerseyConfiguration() {
        //for freemarker
        HashSet<Class<?>> controllerClazz =new HashSet<>();
        controllerClazz.add(LoadPage.class);
        controllerClazz.add(Account.class);
        controllerClazz.add(Presto.class);
        controllerClazz.add(Job.class);
        controllerClazz.add(Schedule.class);
        controllerClazz.add(QueryUI.class);
        controllerClazz.add(Control.class);
        controllerClazz.add(Monitor.class);
        controllerClazz.add(Chart.class);
        registerClasses(controllerClazz);
        register(FreemarkerMvcFeature.class);
    }
}
