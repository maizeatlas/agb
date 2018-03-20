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

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author nkumar
 * This class acts as an action listener for Plantings Node
 * of the Project Tree
 */
public class PlantingNodeParentPopupListener extends MouseAdapter {
    private JTree projectsTree;
    private JPopupMenu plantingPopupMenu;

    public JPopupMenu getPlantingPopupMenu() {
        return plantingPopupMenu;
    }

    public void setPlantingPopupMenu(JPopupMenu plantingPopupMenu) {
        this.plantingPopupMenu = plantingPopupMenu;
    }

    PlantingNodeParentPopupListener(JTree projectsTree) {
        this.setProjectsTree(projectsTree);
        plantingPopupMenu = new JPopupMenu();
        JMenuItem menuItem;
        JMenu sendToMenu = new JMenu(ProjectConstants.SEND_TO);
        JMenuItem harvestings = new JMenuItem("Harvesting");
        sendToMenu.add(harvestings);
        plantingPopupMenu.add(sendToMenu);
        menuItem = new JMenuItem("Import Groups");
        //plantingPopupMenu.add(menuItem);
        menuItem = new JMenuItem("Export Groups");
        //plantingPopupMenu.add(menuItem);
        menuItem = new JMenuItem("Print Reports");
        //plantingPopupMenu.add(menuItem);
    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            plantingPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public JTree getProjectsTree() {
        return projectsTree;
    }

    public void setProjectsTree(JTree projectsTree) {
        this.projectsTree = projectsTree;
    }
}
