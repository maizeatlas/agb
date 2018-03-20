package org.accretegb.modules.config.logging;

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

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogRecord;

/**
 * @author nkumar
 * @author Ningjing
 * This class is used to maintain the logs in the textarea
 * for users to see the behind operations or exceptions while
 * working in the application
 */
public class TextAreaLoggingHandler extends java.util.logging.Handler {

    private JTextArea textArea;
    private boolean loggingEnabled = true;

    public TextAreaLoggingHandler() {
        textArea = new JTextArea(50, 50);
        textArea.setLineWrap(true);
    }

    /**
     * runs as a seperate thread to maintain logs
     * @param record is the log record added to the textarea
     */
    @Override
    public void publish(final LogRecord record) {
        if (isLoggingEnabled()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    StringWriter text = new StringWriter();
                    PrintWriter out = new PrintWriter(text);
                    out.println(textArea.getText());
                    textArea.setText(textArea.getText() + "\n" + record.getMessage());
                }

            });
        }        
    }

    /**
     * to support flushing after textarea is full by certain number of lines
     */
    @Override
    public void flush() {
        // @todo Support flushing
        throw new UnsupportedOperationException("Not Supported Exception");
    }

    /**
     * to support when used stops logginging
     * by closing the logging buffer
     * @throws SecurityException
     */
    @Override
    public void close() throws SecurityException {
        // @todo support closing the buffer
        throw new UnsupportedOperationException("Not Supported Exception");
    }

    public JTextArea getTextArea() {
        return this.textArea;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

}
