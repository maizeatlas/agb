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
 * This node acts a action Listener for Stock Packagings node
 * in the Project Tree
 */
public class StockPackagingParentNodePopupListener extends MouseAdapter {
    private JTree projectsTree;
    private JPopupMenu stockPackagingPopupMenu;

    public JPopupMenu getStockPackagingPopupMenu() {
        return stockPackagingPopupMenu;
    }

    public void setStockPackagingPopupMenu(JPopupMenu stockPackagingPopupMenu) {
        this.stockPackagingPopupMenu = stockPackagingPopupMenu;
    }

    StockPackagingParentNodePopupListener(JTree projectsTree) {
        this.setProjectsTree(projectsTree);
        stockPackagingPopupMenu = new JPopupMenu();
        JMenuItem menuItem;
        menuItem = new JMenuItem("Import Groups");
        //stockPackagingPopupMenu.add(menuItem);
        menuItem = new JMenu("Export Groups");
        //stockPackagingPopupMenu.add(menuItem);
        menuItem = new JMenuItem("Print Report");
        //stockPackagingPopupMenu.add(menuItem);
    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            stockPackagingPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public JTree getProjectsTree() {
        return projectsTree;
    }

    public void setProjectsTree(JTree projectsTree) {
        this.projectsTree = projectsTree;
    }

}
