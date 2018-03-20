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

import org.accretegb.modules.config.logging.TextAreaLoggingHandler;

/**
 * @author nkumar
 * This class a singleton class used for logging in the application
 */
public class AccreteGBLogger {

    private static TextAreaLoggingHandler textAreaLogHandler;

    private static boolean logEnabled = true;

    private static boolean debugEnabled = false;

    public static boolean isLogEnabled() {
        return logEnabled;
    }

    public static void setLogEnabled(boolean logEnabled) {
        AccreteGBLogger.logEnabled = logEnabled;
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static void setDebugEnabled(boolean debugEnabled) {
        AccreteGBLogger.debugEnabled = debugEnabled;
    }

    /**
     * This returns the text area logger
     * @return TextAreaLoggingHandler
     */
    public static synchronized TextAreaLoggingHandler getTextAreaLogHandler() {
        if (textAreaLogHandler == null) {
            textAreaLogHandler = new TextAreaLoggingHandler();
        }
        return textAreaLogHandler;
    }

}