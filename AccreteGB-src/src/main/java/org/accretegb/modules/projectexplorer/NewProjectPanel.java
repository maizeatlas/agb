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

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.hibernate.dao.CollaborateRelationDAO;
import org.accretegb.modules.hibernate.dao.PMProjectDAO;
import org.accretegb.modules.hibernate.dao.TokenRelationDAO;
import org.accretegb.modules.main.LoginScreen;
import org.accretegb.modules.projectmanager.ProjectManagerAllPanel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author nkumar
 * This is the frame of the new project when user
 * wants to create a new proect.
 */
public class NewProjectPanel extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField projectName;
    private JLabel projectNameError;
    private static ProjectManagerAllPanel projectManagerPanel;
    
    private JButton browseButton;
  
    private JTextField date;
    private JButton createProject;
    private int userId;
	private Date createdDate;
    public NewProjectPanel() {
        projectName = new JTextField(20);
        projectNameError = new JLabel("Project Name is Required");

        date = new JTextField(20);
        
        browseButton = new JButton("Browse");
        createProject = new JButton("Create Project");
        userId = LoginScreen.loginUserId;
        
        createdDate = new Date();
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		date.setText(dateFormat.format(createdDate));
    }

    void setInvisible() {
        this.setVisible(false);
    }

    void initialize() {
        setVisible(false);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setTitle("Create New Project");
        setLayout(new MigLayout("insets 40 40 10 10, gap 5"));
        setResizable(false);
        setSize(new Dimension(450, 220));
        add(new JLabel("Project Name (*):"));
        add(projectName, "growx, wrap");
        add(projectNameError,"span, wrap");
        projectNameError.setForeground(Color.red);
        projectNameError.setVisible(false);          
        browseButton.addActionListener(new BrowseProjectDirectoryListener());
       
        add(new JLabel("Date:"));
        add(date, "growx, wrap");        
        createProject.addActionListener(new CreateProjectListener());
        add(new JLabel(""),"wrap");
        add(createProject,"split, span, al right");
        add(browseButton);
    }
	
    public ProjectTree addNewProject(String projectName){
		ProjectTree projectTree = new ProjectTree(projectName);
        getProjectExplorerTabbedPane().getExplorerPanel().addProject(projectTree);
        return projectTree;
    }

    private class CreateProjectListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            boolean validation = true;
            if (getProjectName().getText().equals("")) {
                projectNameError.setVisible(true);
                validation = false;
            } else {
            	if(PMProjectDAO.getInstance().isProjectNameExist(getProjectName().getText()))
            	{
            		projectNameError.setText("Project name already exist!");
            		projectNameError.setVisible(true);
            		validation = false;
            	}else{
            		projectNameError.setVisible(false);
            	}
                
            }

            if (validation) {           	
	    		//TODO update modified date
            	int projectId = PMProjectDAO.getInstance().insert(userId, projectName.getText(), createdDate, createdDate);
            	CollaborateRelationDAO.getInstance().insert(projectId, LoginScreen.loginUserId);
            	TokenRelationDAO.getInstance().insert(projectId, LoginScreen.loginUserId);
            	ProjectManagerAllPanel projectManagerAllPanel = AccreteGBBeanFactory.getContext()
            			.getBean("projectManagerAllPanel", ProjectManagerAllPanel.class);
            	projectManagerAllPanel.populateTable();
				addNewProject(getProjectName().getText());
	            setInvisible();               
            }
        }
    }
    
    

    private class BrowseProjectDirectoryListener implements ActionListener {

        public void actionPerformed(ActionEvent arg0) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            
        }

    }

    public static ProjectExplorerTabbedPane getProjectExplorerTabbedPane() {
        return AccreteGBBeanFactory.getContext().getBean("projectExplorerTabbedPane", ProjectExplorerTabbedPane.class);
		
    }

    public JTextField getProjectName() {
        return projectName;
    }

    public void setProjectName(JTextField projectName) {
        this.projectName = projectName;
    }

    public JTextField getDate() {
        return date;
    }

    public void setDate(JTextField date) {
        this.date = date;
    }

    public JButton getBrowseButton() {
        return browseButton;
    }

    public void setBrowseButton(JButton browseButton) {
        this.browseButton = browseButton;
    }

    public JLabel getProjectNameError() {
        return projectNameError;
    }

    public void setProjectNameError(JLabel projectNameError) {
        this.projectNameError = projectNameError;
    }
    public static void setProjectManagerPanel(ProjectManagerAllPanel panel){
    	projectManagerPanel = panel;
    	
    }
}
