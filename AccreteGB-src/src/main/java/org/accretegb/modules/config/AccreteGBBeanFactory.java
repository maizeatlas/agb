package org.accretegb.modules.config;

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
import org.accretegb.modules.ToolBar;
import org.accretegb.modules.phenotype.PhenotypeInfoPanel;
import org.accretegb.modules.projectexplorer.NewProjectPanel;
import org.accretegb.modules.tab.TabManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import javax.swing.*;

/*
 * @author nkumar
 * This class creates the beans by reading the springconfig.xml file
 * Also it has getter methods of some commonly used beans
 */
public class AccreteGBBeanFactory {

    private static ApplicationContext appContext = buildAppContext();

    /*
     * Creates all beans
     * @return appContext application context will all beans
     */
    private static ApplicationContext buildAppContext() {

        ApplicationContext appContext = new GenericXmlApplicationContext("application-context.xml");

        return appContext;
    }

    public static ApplicationContext getContext() {

        return appContext;
    }

    public static MainLayout getMainLayoutBean() {
        MainLayout mainLayout = (MainLayout) getContext().getBean("mainLayoutBean");
        return mainLayout;
    }

    public static TabManager getTabManager() {
        TabManager tabManager = (TabManager) getContext().getBean("tabManager");
        return tabManager;
    }

    public static NewProjectPanel getNewProjectPanel() {
        NewProjectPanel newProjectPanel = (NewProjectPanel) getContext().getBean("createNewProjectPanel");
        return newProjectPanel;
    }


    public static PhenotypeInfoPanel getPhenotypeInfoPanel() {
        PhenotypeInfoPanel phenotypeInfoChildPanel=  (PhenotypeInfoPanel) getContext().getBean("phenotypeInfoChildPanel0");
        return phenotypeInfoChildPanel;
    }

    /**
     * gets the global tabbed pane of the application
     * @return
     */
    public static JTabbedPane getTabbedPane() {
        javax.swing.JTabbedPane tabbedPane = (javax.swing.JTabbedPane) getContext().getBean("tabbedPane");
        return tabbedPane;
    }

    /**
     * get the toolbar of the application
     */
    public static ToolBar getToolBar() {
        ToolBar toolBar = (ToolBar) getContext().getBean("toolBar");
        return toolBar;
    }

}