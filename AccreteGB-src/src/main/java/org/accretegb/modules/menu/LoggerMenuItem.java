package org.accretegb.modules.menu;

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


import org.accretegb.modules.MainLayout;
import org.accretegb.modules.config.AccreteGBLogger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

/**
 * @author nkumar
 * This is LoggerMenuItem to be shown in the menu
 */
public class LoggerMenuItem extends MenuItem {

    public LoggerMenuItem(String label) {
        super(label);
        this.addActionListener(new LoggerActionListener());
    }

    /**
     * @author nkumar
     * This is the ActionListener class for LoggerMenuItem
     */
    private class LoggerActionListener implements ActionListener {

        private static final String LOG_TITLE = "AccreteGB Logs:";

        /**
         * show the textarea logger to the user by using AccreteGB textarea
         * logger
         * @param e - action event i.e. user press event
         */
        public void actionPerformed(ActionEvent e) {
            MainLayout mainLayout = (MainLayout) getContext().getBean("mainLayoutBean");
            if (mainLayout != null) {
                JFrame loggerFrame = new JFrame();
                loggerFrame.setSize(mainLayout.getFrame().getWidth() / 2, mainLayout.getFrame().getHeight() / 2);
                int currentWidth = mainLayout.getFrame().getWidth();
                int currentHeight = mainLayout.getFrame().getHeight();
                int xCoord = mainLayout.getFrame().getX() + currentWidth / 2 - loggerFrame.getWidth() / 2;
                int yCoord = mainLayout.getFrame().getY() + currentHeight / 2 - loggerFrame.getHeight() / 2;
                if (xCoord < 0 || yCoord < 0) {
                    loggerFrame.setLocation(0, 0);
                } else {
                    loggerFrame.setLocation(xCoord, yCoord);
                }
                loggerFrame.setTitle(LOG_TITLE);
                AccreteGBLogger.getTextAreaLogHandler().getTextArea().setEditable(false);
                JScrollPane scrollPane = new JScrollPane(AccreteGBLogger.getTextAreaLogHandler().getTextArea());
                scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                loggerFrame.add(scrollPane);
                loggerFrame.setVisible(true);
            }
        }
    }

}
