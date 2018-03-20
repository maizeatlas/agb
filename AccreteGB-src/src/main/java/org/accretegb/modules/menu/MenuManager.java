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

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author nkumar
 * This class is used to maintain the menu's of the
 * Application
 */
public class MenuManager {

    private JMenuBar menuBar;

    private List<Menu> menus = new LinkedList<Menu>();

    /**
     * initialize MenuManager
     */
    public void initialize() {

        for (Menu menu : getMenus()) {
            addMenu(menu);
            for (MenuItem menuItem : menu.getSubMenus()) {
                if (menuItem.getSubSubMenus().size() > 0) {
                    // there are further subMenu's we need to create a new menu and add that
                    Menu subMenu = createMenuFromMenuItem(menuItem);
                    menu.add(subMenu);
                    for (MenuItem subMenuItem : menuItem.getSubSubMenus()) {
                        addMenuItem(subMenuItem, subMenu);
                    }
                } else {
                    addMenuItem(menuItem, menu);
                }
            }
        }
    }

    /**
     * adds Menu to MenuBar
     * @param menu
     */
    public void addMenu(Menu menu) {
        menuBar.add(menu);
    }

    /**
     * adds menu item to menu
     * @param menuItem - menu item which is to be added
     * @param menu - menu in which item to be added
     */
    public void addMenuItem(MenuItem menuItem, Menu menu) {
        menu.add(menuItem);
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    public void setMenuBar(JMenuBar menuBar) {
        this.menuBar = menuBar;
    }

    public List<Menu> getMenus() {
        return menus;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }

    @SuppressWarnings("deprecation")
    public Menu createMenuFromMenuItem(MenuItem menuItem) {
        Menu subMenu = new Menu(menuItem.getLabel());
        subMenu.setMnemonic(menuItem.getMnemonic());
        return subMenu;
    }

}