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
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
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
import org.accretegb.modules.util.ChangeMonitor;
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
	 * Open a progress bar in a separate thread
	 * */
	public static JDialog ProjectsLoadingBar() {
		final JDialog barDialog = new JDialog();
		barDialog.setLayout(new FlowLayout());
		barDialog.setAlwaysOnTop(true);
		barDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

	    final JProgressBar jProgressBar = new JProgressBar(0, 100);
	    final JLabel status = new JLabel("Loading Projects:");
	    barDialog.add(status);
	    barDialog.add("jProgressBar", jProgressBar);

	    barDialog.pack();
	    barDialog.setVisible(true);
	    SwingWorker barWorker = new SwingWorker() {
	        @Override
	        protected Object doInBackground() throws Exception {
	        	int i = 0;
	        	while (i < 100) {
	        		i++;
	        		jProgressBar.setValue(i);
	        		Thread.sleep(500);
	        		if (i == 99) {
	        			i = 0;
	        		}
	        	}
	            return null;
	        }
	    };
	    barWorker.execute(); 
	    return barDialog;
	}
	
	/**
	 * Retrieve projects that login user has token on to project tree.
	 * */
	public  PopulateProjectTree(final int userId ) {			
		final JDialog barDialog = ProjectsLoadingBar();
	    SwingWorker pmWorker = new SwingWorker() {
	        @Override
	        protected Object doInBackground() throws Exception {
	        	getProjectExplorerTabbedPane().getExplorerPanel().getProjectsTree().disable();
		    	ArrayList<Integer> projectIds = TokenRelationDAO.getInstance().findProjects(userId);
		  		if(projectIds.size() > 0){
		  			for(Integer projectId :projectIds ){				
		  				PMProject project = PMProjectDAO.getInstance().findProjectObj(projectId);
		  				ProjectTree projectTree = new ProjectTree(project.getProjectName());
		  				getProjectExplorerTabbedPane().getExplorerPanel().addProject(projectTree);	
		  				new CreateStockSelectionGroup(projectTree,projectId);
		  				new CreatePlantingGroup(projectTree,projectId);
		  				new CreatePhenotypeGroup(projectTree,projectId);
		  				new CreateSamplingGroup(projectTree,projectId);
		  				new CreateExperimentGroup(projectTree,projectId);
		  				new CreateHarvestGroup(projectTree,projectId);
		  				ChangeMonitor.changedProject.put(projectId, false);
		  				ChangeMonitor.projectIdName.put(projectId, project.getProjectName());
		  			}
		  		}
	            return null;
	        }

	        @Override
	        public void done(){
	        	barDialog.setVisible(false);
	            barDialog.dispose();
		  		getProjectExplorerTabbedPane().getExplorerPanel().getProjectsTree().enable();
	        }
	    };
	    pmWorker.execute();
		
	}

}
