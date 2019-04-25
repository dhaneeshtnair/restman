/***
 *
 * Copyright (C) 2018 HERE Global B.V. and its affiliate(s).
 All rights reserved.

 This software and other materials contain proprietary information
 controlled by HERE and are protected by applicable copyright legislation.
 Any use and utilization of this software and other materials and
 disclosure to any third parties is conditional upon having a separate
 agreement with HERE for the access, use, utilization or disclosure of this
 software. In the absence of such agreement, the use of the software is not
 allowed.

 */
package com.restman.core;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by dhatb on 06/08/18.
 */
public class ConfigReader {

    private static ConfigReader store;

    private Properties properties;

    public static ConfigReader getInstance(){
        if(store==null){
            synchronized (ConfigReader.class){
                if(store==null){
                    store = new ConfigReader();
                }
            }
        }
        return store;
    }

    private ConfigReader(){

        String appConf = System.getProperty("appConf");
        if(appConf==null) {
            try {
                try (final InputStream inputStream =
                             ConfigReader.class.getClassLoader().getResourceAsStream("application.properties")) {
                    properties = new Properties();
                    properties.load(inputStream);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            try {
                try (final InputStream inputStream =
                             new FileInputStream(appConf)) {
                    properties = new Properties();
                    properties.load(inputStream);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public String getProperty(String name){

        return properties.getProperty(name);

    }


}