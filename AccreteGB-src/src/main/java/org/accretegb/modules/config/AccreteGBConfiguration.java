package org.accretegb.modules.config;

/*
 * Licensed to Openaccretegb-common under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Openaccretegb-common licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.accretegb.modules.util.LoggerUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @author nkumar
 * This class is used to load properties which are global
 * to the application such as Rpath since Application depends on RPath
 * for experimental design, etc
 */
public class AccreteGBConfiguration {

    private static AccreteGBConfiguration accreteGBConfiguration;

    private String rPath;

    public static synchronized AccreteGBConfiguration getConfiguration() {
        if (accreteGBConfiguration == null) {
            accreteGBConfiguration = new AccreteGBConfiguration();
        }
        return accreteGBConfiguration;
    }

    /**
     * loads configuration from the config file
     */
    private void loadConfigs() {
        Properties prop = new Properties();
        InputStream input = null;
        try {
        	//running in eclipse
            //input = getClass().getResourceAsStream("/config.properties");
        	
        	//pack as a jar
        	File f = new File(System.getProperty("java.class.path"));
        	File dir = f.getAbsoluteFile().getParentFile();
        	String path = dir.toString();
            input = new FileInputStream(path+"/config.properties");         
           
            prop.load(input);
            setrPath(prop.getProperty("RPath"));           
            
        } catch (IOException ex) {
            if (LoggerUtils.isLogEnabled()) {
                LoggerUtils.log(Level.INFO, "Not able to load Configuration!!");
            }
        }finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    if (LoggerUtils.isLogEnabled()) {
                        LoggerUtils.log(Level.INFO, "Not able to close configuration file");
                    }
                }
            }
        }
    }

    /**
     * saved proprties in the config.properties for next time application opens
     * @return true if the properties are saved
     */
    public boolean saveConfig() {
        Properties prop = new Properties();
        OutputStream output = null;
        try {
        	//running in eclipse
            //output = new FileOutputStream("src/main/resources/config.properties");
        	
        	//pack as a jar
        	File f = new File(System.getProperty("java.class.path"));
        	File dir = f.getAbsoluteFile().getParentFile();
        	String path = dir.toString();
            output = new FileOutputStream(path+"/config.properties");
            
            
            prop.setProperty("RPath", getrPath());
            prop.store(output, null);
        } catch (IOException io) {
            if (LoggerUtils.isLogEnabled()) {
                LoggerUtils.log(Level.INFO, "Not able to save configuration file.");
            }
            return false;
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    if (LoggerUtils.isLogEnabled()) {
                        LoggerUtils.log(Level.INFO, "Not able to close configuration file!!");
                    }
                }
            }
        }
        return true;
    }

    public String getrPath() {
        return rPath;
    }

    public void setrPath(String rPath) {
        this.rPath = rPath;
    }

}
