package org.accretegb.modules;

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

import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.tab.TabComponent;
import org.accretegb.modules.tab.TabManager;
import org.accretegb.modules.util.LoggerUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author nkumar
 * This class maintains the tool bar used
 * for static tabComponents such as outside seed,
 * preplanting data etc
 */
public class ToolBar extends JToolBar {

    private static final long serialVersionUID = 1L;

    private Map<String, JButton> indexToButtonMap = new LinkedHashMap<String, JButton>();

    public void initialize() {
        setFloatable(false);
        setRollover(true);
        for (final Map.Entry<String, JButton> indexMap : getIndexToButtonMap().entrySet()) {
            indexMap.getValue().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    TabManager tabManager = AccreteGBBeanFactory.getTabManager();
                    TabComponent tabComponent = null;
                    for (TabComponent component : tabManager.getTabComponents()) {
                        if (component.getTitle().equalsIgnoreCase(indexMap.getKey().trim())) {
                            tabComponent = component;
                            break;
                        }
                    }
                    if (tabComponent != null) {
                        setTabComponent(tabComponent);
                    }
                }
            });
            add(indexMap.getValue());
            addSeparator(null);
        }
    }

    /**
     * used to remove a tabComponent,mainly used when user clicks on close button on a
     * tabComponent
     * @param tabComponent tabComponent to be removed
     */
    public void removeTabComponent(TabComponent tabComponent) {
        TabManager tabManager = AccreteGBBeanFactory.getTabManager();
        boolean found = false;
        int total = tabManager.getTabbedPane().getTabCount();
        int tabCount;
        for (tabCount = 0; tabCount < total; tabCount++) {
            if (tabComponent == tabManager.getTabbedPane().getComponentAt(tabCount)) {
                found = true;
                break;
            }
        }
        if (found) {
            tabManager.getTabbedPane().remove(tabCount);
        } else {
            for (TabComponent component : tabManager.getTabComponents()) {
                if (component == tabComponent) {
                    tabManager.getTabbedPane().remove(tabCount);
                }
            }
        }
        MainLayout mainLayout = AccreteGBBeanFactory.getMainLayoutBean();
        mainLayout.getFrame().invalidate();
        mainLayout.getFrame().validate();
        mainLayout.getFrame().repaint();
    }

    /**
     * if found then make the tabComponent as current Component
     * else add the tabComponent used when we add a new tabComponent
     * or a user clicks on an existing tabComponent
     * @param tabComponent tabComponent to be set or added
     */
    public void setTabComponent(TabComponent tabComponent) {
        TabManager tabManager = AccreteGBBeanFactory.getTabManager();
        boolean found = false;
        int total = tabManager.getTabbedPane().getTabCount();
        int tabCount;
        for (tabCount = 0; tabCount < total; tabCount++) {
            TabComponent component = (TabComponent) tabManager.getTabbedPane().getTabComponentAt(tabCount);
            // check for already open tab Component
            if (component == tabComponent) {
                found = true;
                break;
            }
        }
        if (found) {
            tabManager.getTabbedPane().setSelectedIndex(tabCount);
        } else {
            // add the new tab Component
            for (TabComponent component : tabManager.getTabComponents()) {
                if (component == tabComponent) {
                    tabManager.getTabbedPane().add(tabComponent.getComponentPanels().get(0));
                    tabManager.getTabbedPane().setTabComponentAt(tabCount, tabComponent);
                    tabManager.getTabbedPane().setTitleAt(tabCount, tabComponent.getTitle());
                    tabManager.getTabbedPane().setSelectedIndex(tabCount);
                }
            }
        }
        MainLayout mainLayout = AccreteGBBeanFactory.getMainLayoutBean();
        mainLayout.getFrame().invalidate();
        mainLayout.getFrame().validate();
        mainLayout.getFrame().repaint();

    }

    /**
     * returns the name of tabComponent from button
     * @param toolButton the button for tabComponent
     * @return the name of tabComponent
     */
    public String getComponent(JButton toolButton) {
        try {
            for (Map.Entry<String, JButton> indexMap : getIndexToButtonMap().entrySet()) {
                if (indexMap.getValue() == toolButton) {
                    return indexMap.getKey();
                }
            }
        } catch (Exception ex) {
            if (LoggerUtils.isLogEnabled()) {
                LoggerUtils.log(Level.INFO, "Not able to get index of a given button");
            }
        }
        return null;
    }

    public Map<String, JButton> getIndexToButtonMap() {
        return indexToButtonMap;
    }

    public void setIndexToButtonMap(Map<String, JButton> indexToButtonMap) {
        this.indexToButtonMap = indexToButtonMap;
    }

}