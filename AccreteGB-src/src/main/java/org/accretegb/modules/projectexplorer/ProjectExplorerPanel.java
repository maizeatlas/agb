package org.accretegb.modules.projectexplorer;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

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

/**
 * @author nkumar This class acts as a Panel which is shown in the left side of
 *         application having all projects, it maintains the list of Projects
 *         also to be shown in the panel.
 */
public class ProjectExplorerPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTree projectsTree;
	private ProjectPanelPopupListener projectPopupListener;
	private List<ProjectTree> projects = new LinkedList<ProjectTree>();

	ProjectExplorerPanel() {
		projectsTree = new JTree();
		projectsTree.setRootVisible(false);
		DefaultMutableTreeNode projectsNode = new DefaultMutableTreeNode("Projects");
		DefaultTreeModel defaultModel = new DefaultTreeModel(projectsNode);
		projectsTree.setModel(defaultModel);
		projectsTree.addMouseListener(new ProjectTreeActionListener(projectsTree));
		setBackground(new Color(255, 255, 255));
	}

	public ProjectPanelPopupListener getProjectPopupListener() {
		return projectPopupListener;
	}
	
	public void setProjectPopupListener(
			ProjectPanelPopupListener projectPopupListener) {
		this.projectPopupListener = projectPopupListener;
	}

	void initialize() {
		setLayout(new MigLayout());
		add(getProjectsTree());
		addMouseListener(projectPopupListener);
	}

	public List<ProjectTree> getProjects() {
		return projects;
	}

	public void setProjects(List<ProjectTree> projects) {
		this.projects = projects;
	}

	public void addProject(ProjectTree project) {
		getProjectsTree().setRootVisible(true);
		((DefaultMutableTreeNode) getProjectsTree().getModel().getRoot()).add(project.getProjectRootNode());
		getProjectsTree().setModel(new DefaultTreeModel((TreeNode) getProjectsTree().getModel().getRoot()));
		revalidate();
		projects.add(project);
	}

	public void removeProject(DefaultMutableTreeNode project) {
		DefaultTreeModel model = (DefaultTreeModel)getProjectsTree().getModel();
		DefaultMutableTreeNode projectsRoot = ((DefaultMutableTreeNode) getProjectsTree().getModel().getRoot());
		projectsRoot.remove(project);
		model.reload();
		projects.remove(project);
	}

	public JTree getProjectsTree() {
		return projectsTree;
	}

	public void setProjectsTree(JTree projectsTree) {
		this.projectsTree = projectsTree;
	}
	

}