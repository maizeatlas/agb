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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.hibernate.dao.PMProjectDAO;
import org.accretegb.modules.projectexplorer.ProjectExplorerTabbedPane;
import org.accretegb.modules.projectexplorer.ProjectTreeNode;
import org.accretegb.modules.projectmanager.ProjectManager;

/**
 * @author nkumar
 * shows exit menu iten
 */
public class ExitMenuItem extends MenuItem {

    private static final long serialVersionUID = 1L;
    private static String EXIT_MESSAGE = "Exit MaizeAtlas Application";
    private static String CONFIRM_MESSAGE = "Are you sure you want to exit ?";
    private static String SAVE_PROJECTS_MESSAGE = "You may have modified the following projects :\n";
    private static String SAVE_BEFORE_EXIT = "Save all before exit, click yes\nExit without saving, click no";
    

    public ExitMenuItem(String label) {
        super(label);
        this.addActionListener(new ExitButtonActionListener());
    }

    /**
     * @author nkumar
     * action listener for exit menu item
     */
    private class ExitButtonActionListener implements ActionListener {
        /**
         * exit the application
         * @param e
         */
        public void actionPerformed(ActionEvent e) {
        	ArrayList<String> modifiedProjects = new ArrayList();
            JTree projectTrees = AccreteGBBeanFactory.getContext().getBean("projectExplorerTabbedPane", ProjectExplorerTabbedPane.class)
    				.getExplorerPanel().getProjectsTree();
    		DefaultMutableTreeNode projectsRoot = (DefaultMutableTreeNode) projectTrees.getModel().getRoot();
    		for(int index = 0;index < projectsRoot.getChildCount(); ++index){
    			ProjectTreeNode projectNode = (ProjectTreeNode) projectsRoot.getChildAt(index);
    				modifiedProjects.add(projectNode.getNodeName());
    		}
    		
    		if(modifiedProjects.size() > 0){
    			int result = JOptionPane.showConfirmDialog(null, SAVE_PROJECTS_MESSAGE + modifiedProjects + "\n" +  SAVE_BEFORE_EXIT, EXIT_MESSAGE,
                        JOptionPane.YES_NO_CANCEL_OPTION);
    			if (result == JOptionPane.YES_OPTION) {
    				for(int index = 0;index < projectsRoot.getChildCount(); ++index){
            			ProjectTreeNode projectNode = (ProjectTreeNode) projectsRoot.getChildAt(index);
            			if(projectNode.isModified()){
            				projectNode.setModified(false);
            				String projectName = projectNode.getNodeName();
        					int projectId = PMProjectDAO.getInstance().findProjectId(projectName);
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
    }
}
