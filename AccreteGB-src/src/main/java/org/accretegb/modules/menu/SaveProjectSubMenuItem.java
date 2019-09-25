package org.accretegb.modules.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.hibernate.dao.PMProjectDAO;
import org.accretegb.modules.menu.CreateProjectMenuItem.NewProjectActionListener;
import org.accretegb.modules.projectexplorer.ProjectExplorerTabbedPane;
import org.accretegb.modules.projectexplorer.ProjectTreeNode;
import org.accretegb.modules.projectmanager.ProjectManager;

/**
 * 
 * @author Ningjing
 *
 */
public class SaveProjectSubMenuItem extends MenuItem {
	 private static final long serialVersionUID = 1L;

	    public  SaveProjectSubMenuItem(String label) {
	        super(label);
	        this.setEnabled(false);
	        this.addActionListener(new SaveProjectActionListener());
	    }
	    
	    public static class SaveProjectActionListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				JTree projectTrees = AccreteGBBeanFactory.getContext().getBean("projectExplorerTabbedPane", ProjectExplorerTabbedPane.class)
						.getExplorerPanel().getProjectsTree();
				Object root = projectTrees.getModel().getRoot();
				for(int selectedProject :  projectTrees.getSelectionModel().getSelectionRows()){
					ProjectTreeNode projectNode = (ProjectTreeNode) projectTrees.getModel().getChild(root, selectedProject-1);
					String projectName = projectNode.getNodeName();
					int projectId = PMProjectDAO.getInstance().findProjectId(projectName);
					ProjectManager.saveProject(projectId);
				}				
			}
	    }
}
