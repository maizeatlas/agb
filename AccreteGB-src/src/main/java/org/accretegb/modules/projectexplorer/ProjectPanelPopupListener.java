package org.accretegb.modules.projectexplorer;
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

import org.accretegb.modules.menu.CreateProjectMenuItem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author nkumar
 * This acts as a Mouse Listener for the Projects Panel
 * on the left hand side, mainly used to show popup for
 * create new project, open a project, import project, export
 * project etc.
 */
public class ProjectPanelPopupListener extends MouseAdapter {
    private JPopupMenu exploreProjectPopupMenu;

    void initialize() {
        exploreProjectPopupMenu = new JPopupMenu();
        JMenuItem menuItem;
        menuItem = new JMenuItem("New Project");
        menuItem.addActionListener(new CreateProjectMenuItem.NewProjectActionListener());
        exploreProjectPopupMenu.add(menuItem);
        //menuItem = new JMenuItem("Open Project");
        //menuItem.addActionListener(new OpenProjectActionListener());
        //exploreProjectPopupMenu.add(menuItem);
        //menuItem = new JMenuItem("Import Project");
        //menuItem.addActionListener(new OpenProjectActionListener());
        //exploreProjectPopupMenu.add(menuItem);
        //menuItem = new JMenuItem("Export Project");
        //menuItem.addActionListener(new OpenProjectActionListener());
        //exploreProjectPopupMenu.add(menuItem);

    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            exploreProjectPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private class OpenProjectActionListener implements ActionListener {

        public void actionPerformed(ActionEvent arg0) {
            // TODO Auto-generated method stub

        }

    }

}
