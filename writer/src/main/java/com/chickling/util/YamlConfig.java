package com.chickling.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by gl08 on 2017/2/24.
 */
public class YamlConfig {
    public static final Logger logger = LogManager.getLogger(YamlConfig.class);
    public static YamlConfig instance;
    int importBatchSize=500;
    String notbatchdb="";

    public int getImportBatchSize() {
        return importBatchSize;
    }

    public void setImportBatchSize(int importBatchSize) {
        this.importBatchSize = importBatchSize;
    }

    public String getNotbatchdb() {
        return notbatchdb;
    }

    public void setNotbatchdb(String notbatchdb) {
        this.notbatchdb = notbatchdb;
    }

    static {
        loadYaml();
    }
    private static void loadYaml() {
        try {
            String yamlPath =Thread.currentThread().getContextClassLoader().getResource("dbconfig.yaml").getPath();
            logger.info("Loading configuration from " + yamlPath);
            InputStream input;
            try{
                input = new FileInputStream(yamlPath);
            }catch (FileNotFoundException e){
                logger.error("Ymal file not found at " + yamlPath);
                throw new AssertionError(e);
            }
            Constructor constructor = new Constructor(YamlConfig.class);
            Yaml yaml = new Yaml(constructor);
            instance = (YamlConfig) yaml.load(input);
        } catch (YAMLException | NullPointerException e) {
            logger.error("Invalid yaml; unable to start. See log for stacktrace.", e);
            System.exit(1);
        }
    }
}
