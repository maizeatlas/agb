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
import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.projectexplorer.NewProjectPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author nkumar
 *
 */
public class CreateProjectMenuItem extends MenuItem {

    private static final long serialVersionUID = 1L;

    public CreateProjectMenuItem(String label) {
        super(label);
        this.addActionListener(new NewProjectActionListener());
    }

    /**
     * @author nkumar
     * This class implements a new listener for a new project
     */
    public static class NewProjectActionListener implements ActionListener {

        /**
         * shows a new project jframe to the user
         * @param e
         */
        public void actionPerformed(ActionEvent e) {
            MainLayout mainLayout = AccreteGBBeanFactory.getMainLayoutBean();
            NewProjectPanel projectFrame = AccreteGBBeanFactory.getNewProjectPanel();
            int currentWidth = mainLayout.getFrame().getWidth();
            int currentHeight = mainLayout.getFrame().getHeight();
            int xCoord = mainLayout.getFrame().getX() + currentWidth / 2 - projectFrame.getWidth() / 2;
            int yCoord = mainLayout.getFrame().getY() + currentHeight / 2 - projectFrame.getHeight() / 2;
            if (xCoord < 0 || yCoord < 0) {
                projectFrame.setLocation(0, 0);
            } else {
                projectFrame.setLocation(xCoord, yCoord);
            }
            projectFrame.getProjectName().setText("");
           
            projectFrame.setVisible(true);
        }

    }

}
