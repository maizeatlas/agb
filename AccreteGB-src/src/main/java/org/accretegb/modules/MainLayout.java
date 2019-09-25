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
import org.accretegb.modules.hibernate.dao.PMProjectDAO;
import org.accretegb.modules.menu.MenuManager;
import org.accretegb.modules.projectexplorer.ProjectExplorerTabbedPane;
import org.accretegb.modules.projectexplorer.ProjectTreeNode;
import org.accretegb.modules.tab.TabManager;
import org.accretegb.modules.util.ChangeMonitor;
import org.accretegb.modules.projectexplorer.ProjectExplorerPanel;
import org.accretegb.modules.projectmanager.ProjectManager;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

/**
 * @author nkumar
 * This class is the main class of the application
 * it stores all the high level components of the application
 * such a the main JFrame, menuManager, tabManager, toolBar etc
 */
public class MainLayout {

    private static String TITLE = "AccreteGB - The Breeder's ToolBox";
    private static String EXIT_MESSAGE = "Exit MaizeAtlas Application";
    private static String CONFIRM_MESSAGE = "Are you sure you want to exit ?";
    private static String SAVE_PROJECTS_MESSAGE = "Changes to the following projects have been detected:\n";
    private static String SAVE_BEFORE_EXIT = "Save all before exit, click yes\nExit without saving, click no";
    
    
    private JFrame frame;
    private MenuManager menuManager;
    private TabManager tabManager;
    private ToolBar toolBar;
    private JButton currentToolBarButton;
    private JTabbedPane currentTabbedPane;
    private JScrollPane tabbedScrollBarPane;
    private ProjectExplorerTabbedPane projectExplorerTabbedPane;

    /**
     * initialize this component
     * @throws IOException
     */
    public void initialize() throws IOException {
        getFrame().setTitle(TITLE);
        getFrame().setJMenuBar(getMenuManager().getMenuBar());
        getFrame().add(getToolBar(), BorderLayout.PAGE_START);
        getFrame().setLocationByPlatform(true);

        Dimension screenDims = Toolkit.getDefaultToolkit().getScreenSize();
        Double width = new Double(0.75 * screenDims.getWidth());
        Double height = new Double(0.75 * screenDims.getHeight());
        getFrame().setSize(width.intValue(), height.intValue());

        setCurrentToolBarButton(getToolBar().getIndexToButtonMap().get(0));
        JTabbedPane pane = getTabManager().getTabbedPane();
        setCurrentTabbedPane(pane);
        getTabbedScrollBarPane().setViewportView(getCurrentTabbedPane());

        JSplitPane leftsplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(
                getProjectExplorerTabbedPane()), getCurrentTabbedPane());
        getProjectExplorerTabbedPane().setMinimumSize(new Dimension(250, 100));
        getProjectExplorerTabbedPane().setPreferredSize(new Dimension(250, 100));
        leftsplitpane.setDividerLocation(250);
        leftsplitpane.setResizeWeight(0.5);
        leftsplitpane.setOneTouchExpandable(false);
        getFrame().add(leftsplitpane);

        getFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        getFrame().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ArrayList<String> modifiedProjects = new ArrayList();
            	for (Entry<Integer, Boolean> entry : ChangeMonitor.changedProject.entrySet()) {
            	   if(entry.getValue()) {
            		   modifiedProjects.add(ChangeMonitor.projectIdName.get(entry.getKey()));
            	   }
            	} 
        		
        		if(modifiedProjects.size() > 0){
        			int result = JOptionPane.showConfirmDialog(null, SAVE_PROJECTS_MESSAGE + modifiedProjects + "\n" +  SAVE_BEFORE_EXIT, EXIT_MESSAGE,
                            JOptionPane.YES_NO_CANCEL_OPTION);
        			if (result == JOptionPane.YES_OPTION) {
        				for(Entry<Integer, Boolean> entry : ChangeMonitor.changedProject.entrySet()){
        					if(entry.getValue()) {
            					int projectId = entry.getKey();
            					ProjectManager.saveOrDeleteProject(projectId,"save");
            					System.exit(0);
            				}	
                		}
                    }else if(result == JOptionPane.NO_OPTION){
                    	System.exit(0);
                    }
        			
        		}else{
        			int result = JOptionPane.showConfirmDialog(null, CONFIRM_MESSAGE, EXIT_MESSAGE,
                            JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        System.exit(0);
                    }	
        		}
            }

			private JTabbedPane getProjectsTree() {
				// TODO Auto-generated method stub
				return null;
			}
        });
        getFrame().setVisible(false);
    }

    public ProjectExplorerTabbedPane getProjectExplorerTabbedPane() {
        return projectExplorerTabbedPane;
    }

    public void setProjectExplorerTabbedPane(ProjectExplorerTabbedPane projectExplorerTabbedPane) {
        this.projectExplorerTabbedPane = projectExplorerTabbedPane;
    }

    public JScrollPane getTabbedScrollBarPane() {
        return tabbedScrollBarPane;
    }

    public void setTabbedScrollBarPane(JScrollPane tabbedScrollBarPane) {
        this.tabbedScrollBarPane = tabbedScrollBarPane;
    }

    public JButton getCurrentToolBarButton() {
        return currentToolBarButton;
    }

    public void setCurrentToolBarButton(JButton currentToolBarButton) {
        this.currentToolBarButton = currentToolBarButton;
    }

    public JTabbedPane getCurrentTabbedPane() {
        return currentTabbedPane;
    }

    public void setCurrentTabbedPane(JTabbedPane currentTabbedPane) {
        this.currentTabbedPane = currentTabbedPane;
    }

    public TabManager getTabManager() {
        return tabManager;
    }

    public void setTabManager(TabManager tabManager) {
        this.tabManager = tabManager;
    }

    public ToolBar getToolBar() {
        return toolBar;
    }

    public void setToolBar(ToolBar toolBar) {
        this.toolBar = toolBar;
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public void setMenuManager(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

}