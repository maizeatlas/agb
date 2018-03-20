package org.accretegb.modules.tab;

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

import javax.swing.*;

/**
 * @author nkumar
 * This is basically JPanel for a tabComponent
 * a tabComponent can have multiple TabComponentPanel
 * which can be traversed using Prev & Next Button
 */
public class TabComponentPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static String PREV = "Prev";
    private static String NEXT = "Next";

    private String parentName;
    private JButton prevPanelButton;
    private JButton nextPanelButton;
    private int panelIndex;

    public TabComponentPanel() {
        setPrevPanelButton(new JButton(PREV));
        setNextPanelButton(new JButton(NEXT));
    }

    /**
     * used to refresh the current tab Component Panel
     */
    public void refreshComponent() {
        MainLayout mainLayout = AccreteGBBeanFactory.getMainLayoutBean();
        TabManager tabManager = mainLayout.getTabManager();
        TabComponent tabComponent = tabManager.getTabComponent(getTabComponentPanel());
        tabComponent.setCurrentComponentPanel(getTabComponentPanel());
        tabManager.refreshComponentPanel(getTabComponentPanel());
    }

    /**
     * used when a user presses a previous button in
     * a tabComponentPanel for rendering the next Panel of
     * that TabComponent
     */
    public void prevButtonActionPerformed() {
        MainLayout mainLayout = AccreteGBBeanFactory.getMainLayoutBean();
        TabManager tabManager = mainLayout.getTabManager();
        TabComponent tabComponent = tabManager.getTabComponent(getTabComponentPanel());
        tabComponent.setCurrentComponentPanel(getTabComponentPanel());
        TabComponentPanel prevTabComponentPanel = tabComponent.getPrevComponentPanel();
        tabManager.setReplaceComponentPanel(getTabComponentPanel(), prevTabComponentPanel);
    }

    /**
     * used when a user presses a next button in
     * a tabComponentPanel for rendering the next Panel of
     * that TabComponent
     */
    public void nextButtonActionPerformed() {
        MainLayout mainLayout = AccreteGBBeanFactory.getMainLayoutBean();
        TabManager tabManager = mainLayout.getTabManager();
        TabComponent tabComponent = tabManager.getTabComponent(getTabComponentPanel());
        tabComponent.setCurrentComponentPanel(getTabComponentPanel());
        TabComponentPanel nextTabComponentPanel = tabComponent.getNextComponentPanel();
        tabManager.setReplaceComponentPanel(getTabComponentPanel(), nextTabComponentPanel);
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public int getPanelIndex() {
        return panelIndex;
    }

    public void setPanelIndex(int panelIndex) {
        this.panelIndex = panelIndex;
    }

    public JButton getPrevPanelButton() {
        return prevPanelButton;
    }

    public void setPrevPanelButton(JButton prevPanelButton) {
        this.prevPanelButton = prevPanelButton;
    }

    public JButton getNextPanelButton() {
        return nextPanelButton;
    }

    public void setNextPanelButton(JButton nextPanelButton) {
        this.nextPanelButton = nextPanelButton;
    }

    public TabComponentPanel getTabComponentPanel() {
        return this;
    }

}