package org.accretegb.modules.util;

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

import org.accretegb.modules.config.AccreteGBLogger;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author nkumar
 * This is utitlity class for Logging purposes
 */

public class LoggerUtils {

    /**
     * Log the message in the logger
     * @param level - level such as INFO, DEBUG etc
     * @param message - the message to be logged
     */
    public static synchronized void log(Level level, String message) {
        LogRecord logRecord = new LogRecord(level, message);
        AccreteGBLogger.getTextAreaLogHandler().publish(logRecord);
    }

    /**
     * check if the Logger is enabled or not
     * @return true if logger is enabled
     */
    public static synchronized boolean isLogEnabled() {
        if (AccreteGBLogger.getTextAreaLogHandler().isLoggingEnabled()) {
            return true;
        }
        return false;
    }

}