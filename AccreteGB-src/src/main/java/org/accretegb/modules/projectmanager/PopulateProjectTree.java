package org.accretegb.modules.projectmanager;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.accretegb.modules.MainLayout;
import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.hibernate.PMProject;
import org.accretegb.modules.hibernate.Project;
import org.accretegb.modules.hibernate.dao.PMProjectDAO;
import org.accretegb.modules.hibernate.dao.TokenRelationDAO;
import org.accretegb.modules.main.LoginScreen;
import org.accretegb.modules.projectexplorer.ProjectExplorerTabbedPane;
import org.accretegb.modules.projectexplorer.ProjectTree;
import org.accretegb.modules.util.ThreadPool;

/**
 * populate project tree panel with projects that login user has token on
 * @author Ningjing
 *
 */
public class PopulateProjectTree {
	private ProjectExplorerTabbedPane projectExplorerTabbedPane;
	
	public  ProjectExplorerTabbedPane getProjectExplorerTabbedPane() {
		return AccreteGBBeanFactory.getContext().getBean("projectExplorerTabbedPane", ProjectExplorerTabbedPane.class);
	}

	
	/**
	 * Retrieve projects that login user has token on to project tree.
	 * */
	public  PopulateProjectTree(final int userId) {	
		final JDialog dialog =  new JDialog();
		
	    Thread loading = new Thread() {
		      public void run() {
		    	JPanel pan = new JPanel();
		  		pan.setLayout(new FlowLayout());
		  		JLabel label = new JLabel("Loading Projects...");
		  		pan.add(label);
		  		dialog.add(pan);
		  		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		  		dialog.setVisible(true);
		  		dialog.setSize(new Dimension(250, 80));
		  		dialog.setResizable(false);
		      }
		    };
		 loading.start();
		
		Thread populating = new Thread() {
		      public void run() {
		    	getProjectExplorerTabbedPane().getExplorerPanel().getProjectsTree().disable();
		    	ArrayList<Integer> projectIds = TokenRelationDAO.getInstance().findProjects(userId);
		  		if(projectIds.size() > 0){
		  			for(Integer projectId :projectIds ){				
		  				PMProject project = PMProjectDAO.getInstance().findProjectName(projectId);
		  				ProjectTree projectTree = new ProjectTree(project.getProjectName());
		  				getProjectExplorerTabbedPane().getExplorerPanel().addProject(projectTree);	
		  				new CreateStockSelectionGroup(projectTree,projectId);
		  				new CreatePlantingGroup(projectTree,projectId);
		  				new CreatePhenotypeGroup(projectTree,projectId);
		  				new CreateSamplingGroup(projectTree,projectId);
		  				new CreateExperimentGroup(projectTree,projectId);
		  				new CreateHarvestGroup(projectTree,projectId);

		  			}
		  		}
		  		dialog.setVisible(false);
		  		getProjectExplorerTabbedPane().getExplorerPanel().getProjectsTree().enable();
		      }
		    };
		 populating.start();
	}

}
