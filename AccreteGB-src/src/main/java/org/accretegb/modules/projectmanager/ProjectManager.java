package org.accretegb.modules.projectmanager;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.hibernate.dao.ExperimentGroupDAO;
import org.accretegb.modules.hibernate.dao.HarvestingGroupDAO;
import org.accretegb.modules.hibernate.dao.PhenotypeGroupDAO;
import org.accretegb.modules.hibernate.dao.PlantingGroupDAO;
import org.accretegb.modules.hibernate.dao.SamplingGroupDAO;
import org.accretegb.modules.hibernate.dao.PMProjectDAO;
import org.accretegb.modules.hibernate.dao.StockSelectionGroupDAO;
import org.accretegb.modules.projectexplorer.ProjectExplorerPanel;
import org.accretegb.modules.projectexplorer.ProjectExplorerTabbedPane;
import org.accretegb.modules.projectexplorer.ProjectTreeNode;
import org.accretegb.modules.tab.TabComponent;
import org.accretegb.modules.tab.TabManager;


/**
 * actions taken on GUI after remove project or add project
 * @author Ningjing
 *
 */
public class ProjectManager {
	
	public static void removeProjectFromExploer(String projectName){
		DefaultMutableTreeNode removedProjectTree = null;
		JTree projectTrees = AccreteGBBeanFactory.getContext().getBean("projectExplorerTabbedPane", ProjectExplorerTabbedPane.class)
				.getExplorerPanel().getProjectsTree();
		DefaultMutableTreeNode projectsRoot = (DefaultMutableTreeNode) projectTrees.getModel().getRoot();
		for(int childIndex = 0; childIndex < projectsRoot.getChildCount();++childIndex ){
			if(projectsRoot.getChildAt(childIndex).toString().equals(projectName)){
				removedProjectTree = (DefaultMutableTreeNode) projectsRoot.getChildAt(childIndex);
			}
		}
		AccreteGBBeanFactory.getContext().getBean("projectExplorerTabbedPane", ProjectExplorerTabbedPane.class)
			.getExplorerPanel().removeProject(removedProjectTree);	
	}
	
	public static void saveOrDeleteProject(int projectId, String operation){
		ProjectTreeNode stockSelectionNode = null;
		ProjectTreeNode plantingNode = null;
		ProjectTreeNode phenotypeNode = null;
		ProjectTreeNode samplingNode = null;
		ProjectTreeNode experimentNode = null;
		ProjectTreeNode harvestNode = null;
		
		JTree projectTree = AccreteGBBeanFactory.getContext().getBean("projectExplorerPanel", ProjectExplorerPanel.class).getProjectsTree();
		int projectsCount = projectTree.getModel().getChildCount(projectTree.getModel().getRoot());
		for(int index = 0; index < projectsCount; ++ index){			
			String projectName = projectTree.getModel().getChild(projectTree.getModel().getRoot(), index).toString();
			if(projectName.equals(PMProjectDAO.getInstance().findProjectName(projectId).getProjectName())){
				ProjectTreeNode projectRootNode = (ProjectTreeNode) projectTree.getModel().getChild(projectTree.getModel().getRoot(), index);
				stockSelectionNode = (ProjectTreeNode) projectTree.getModel().getChild(projectRootNode,0);
				experimentNode = (ProjectTreeNode) projectTree.getModel().getChild(projectRootNode,1);
				plantingNode = (ProjectTreeNode) projectTree.getModel().getChild(projectRootNode,2);
				phenotypeNode = (ProjectTreeNode) projectTree.getModel().getChild(projectRootNode,3);
				samplingNode = (ProjectTreeNode) projectTree.getModel().getChild(projectRootNode,4);
				harvestNode = (ProjectTreeNode) projectTree.getModel().getChild(projectRootNode,5);
			}
			
		}
		
		//save stock selection groups
		if(stockSelectionNode != null && projectTree.getModel().getChildCount(stockSelectionNode) > 0)
     	{     
			for(int childIndex = 0; childIndex < projectTree.getModel().getChildCount(stockSelectionNode); ++childIndex){
				String groupName = ((ProjectTreeNode)projectTree.getModel().getChild(stockSelectionNode, childIndex)).getNodeName();
				if(operation.equals("save")){
					ParseStockSelectionGroup parseStockSelectionGroup = new ParseStockSelectionGroup(projectId);
	    			parseStockSelectionGroup.saveCartTable(groupName);
				}else{
					StockSelectionGroupDAO.getInstance().delete(projectId, groupName);
				}
    			//remove tabs
    			TabComponent tabComponent = (TabComponent) getContext().getBean("stockSelectionTab" +projectId+groupName);
    			removeTab(tabComponent);
			}
  
     	}
		//save planting groups
		if(plantingNode != null &&projectTree.getModel().getChildCount(plantingNode) > 0)
     	{     	
			for(int childIndex = 0; childIndex < projectTree.getModel().getChildCount(plantingNode); ++childIndex){
				String groupName = ((ProjectTreeNode)projectTree.getModel().getChild(plantingNode, childIndex)).getNodeName();
				if(operation.equals("save")){
					ParsePlantingGroup parsePlantingGroup = new ParsePlantingGroup(projectId);
	     		    parsePlantingGroup.saveTables(groupName); 
				}else{
					PlantingGroupDAO.getInstance().delete(projectId, groupName);
				}					
     			TabComponent tabComponent = (TabComponent) getContext().getBean("plantingTab" + projectId + groupName);
     			removeTab(tabComponent);
     		}
     	}
		
		//save phenotype groups			
		if(phenotypeNode != null && projectTree.getModel().getChildCount(phenotypeNode) > 0)
     	{     	
			for(int childIndex = 0; childIndex < projectTree.getModel().getChildCount(phenotypeNode); ++childIndex){
				String groupName = ((ProjectTreeNode)projectTree.getModel().getChild(phenotypeNode, childIndex)).getNodeName();
				if(operation.equals("save")){
					ParsePhenotypeGroup parsePhenotypeGroup = new ParsePhenotypeGroup(projectId);
	     		    parsePhenotypeGroup.saveTables(groupName); 
				}else{
					PhenotypeGroupDAO.getInstance().delete(projectId, groupName);
				}						
     			TabComponent tabComponent = (TabComponent) getContext().getBean("phenotypeTab" + projectId + groupName);
     			removeTab(tabComponent);
     		}
     	}
		
		//save sampling groups			
		if(samplingNode != null && projectTree.getModel().getChildCount(samplingNode) > 0)
	     	{     	
				for(int childIndex = 0; childIndex < projectTree.getModel().getChildCount(samplingNode); ++childIndex){
					String groupName = ((ProjectTreeNode)projectTree.getModel().getChild(samplingNode, childIndex)).getNodeName();
					if(operation.equals("save")){
						ParseSamplingGroup parseSamplingGroup = new ParseSamplingGroup(projectId);
		     		    parseSamplingGroup.saveTables(groupName); 
					}else{
						SamplingGroupDAO.getInstance().delete(projectId, groupName);
					}						
	     			TabComponent tabComponent = (TabComponent) getContext().getBean("samplingTab" + projectId + groupName);
	     			removeTab(tabComponent);
	     		}
	     	}
		
		//save experiment groups
		if(experimentNode != null && projectTree.getModel().getChildCount(experimentNode) > 0)
     	{     	
			for(int childIndex = 0; childIndex < projectTree.getModel().getChildCount(experimentNode); ++childIndex){
				String groupName = ((ProjectTreeNode)projectTree.getModel().getChild(experimentNode, childIndex)).getNodeName();
				if(operation.equals("save")){
					ParseExperimentGroup parseExperimentGroup = new ParseExperimentGroup(projectId);
	     		    parseExperimentGroup.saveTables(groupName); 
				}else{
					ExperimentGroupDAO.getInstance().delete(projectId, groupName);
				}
				TabComponent tabComponent = (TabComponent) getContext().getBean("experimentDesignTab" + projectId + groupName);
				removeTab(tabComponent);
     		}
     	}
		
		//save harvest groups
		if(harvestNode != null && projectTree.getModel().getChildCount(harvestNode) > 0)
     	{     	
			for(int childIndex = 0; childIndex < projectTree.getModel().getChildCount(harvestNode); ++childIndex){
				String groupName = ((ProjectTreeNode)projectTree.getModel().getChild(harvestNode, childIndex)).getNodeName();
				if(operation.equals("save")){
					ParseHarvestGroup parseHarvestGroup = new ParseHarvestGroup(projectId);
	     		    parseHarvestGroup.saveTables(groupName);  
				}else{
					HarvestingGroupDAO.getInstance().delete(projectId, groupName);
				}					
     			TabComponent tabComponent = (TabComponent) getContext().getBean("harvestingTab" + projectId + groupName);
     			removeTab(tabComponent);
     		}
     	}	
		if(operation.equals("save")){
		    PMProjectDAO.getInstance().updateLastModifiedDate(projectId);
		    ProjectManagerAllPanel.populateTable();	    
		}else{
			PMProjectDAO.getInstance().delete(projectId);
		}
		
	}
	
	public static void removeTab(TabComponent tabComponent){
		TabManager tabManager = (TabManager) getContext().getBean("tabManager");
	    tabManager.getTabComponents().remove(tabComponent);
	    JTabbedPane tabbedPane = AccreteGBBeanFactory.getTabbedPane();
        int i = tabbedPane.indexOfTabComponent(tabComponent);
        if(i != -1)
        {
        	tabbedPane.remove(i);
        }
		
	}
	
	/**
	 * action performed when click "save project" in menu
	 * @param projectId
	 */
	public static void saveProject(int projectId){
		try{
			int option = option = JOptionPane.showConfirmDialog(null, "Are you sure to save the selected project?", "", JOptionPane.OK_OPTION);				
			if(option == JOptionPane.OK_OPTION){
				ProjectTreeNode stockSelectionNode = null;
				ProjectTreeNode plantingNode = null;
				ProjectTreeNode phenotypeNode = null;
				ProjectTreeNode samplingNode = null;
				ProjectTreeNode experimentNode = null;
				ProjectTreeNode harvestNode = null;
				
				JTree projectTree = AccreteGBBeanFactory.getContext().getBean("projectExplorerPanel", ProjectExplorerPanel.class).getProjectsTree();
				int projectsCount = projectTree.getModel().getChildCount(projectTree.getModel().getRoot());
				for(int index = 0; index < projectsCount; ++ index){			
					String projectName = projectTree.getModel().getChild(projectTree.getModel().getRoot(), index).toString();
					if(projectName.equals(PMProjectDAO.getInstance().findProjectName(projectId).getProjectName())){
						ProjectTreeNode projectRootNode = (ProjectTreeNode) projectTree.getModel().getChild(projectTree.getModel().getRoot(), index);
						stockSelectionNode = (ProjectTreeNode) projectTree.getModel().getChild(projectRootNode,0);
						experimentNode = (ProjectTreeNode) projectTree.getModel().getChild(projectRootNode,1);
						plantingNode = (ProjectTreeNode) projectTree.getModel().getChild(projectRootNode,2);
						phenotypeNode = (ProjectTreeNode) projectTree.getModel().getChild(projectRootNode,3);
						samplingNode = (ProjectTreeNode) projectTree.getModel().getChild(projectRootNode,4);
						harvestNode = (ProjectTreeNode) projectTree.getModel().getChild(projectRootNode,5);
					}
					
				}
				
				//save stock selection groups
				if(projectTree.getModel().getChildCount(stockSelectionNode) > 0)
		     	{     
					for(int childIndex = 0; childIndex < projectTree.getModel().getChildCount(stockSelectionNode); ++childIndex){
						String groupName = ((ProjectTreeNode)projectTree.getModel().getChild(stockSelectionNode, childIndex)).getNodeName();
						ParseStockSelectionGroup parseStockSelectionGroup = new ParseStockSelectionGroup(projectId);
		    			parseStockSelectionGroup.saveCartTable(groupName);
					}
		  
		     	}
				//save planting groups
				if(projectTree.getModel().getChildCount(plantingNode) > 0)
		     	{     	
					for(int childIndex = 0; childIndex < projectTree.getModel().getChildCount(plantingNode); ++childIndex){
						String groupName = ((ProjectTreeNode)projectTree.getModel().getChild(plantingNode, childIndex)).getNodeName();
						ParsePlantingGroup parsePlantingGroup = new ParsePlantingGroup(projectId);
		     		    parsePlantingGroup.saveTables(groupName);
		     		}
		     	}
				
				//save phenotype groups			
				if(projectTree.getModel().getChildCount(phenotypeNode) > 0)
		     	{     	
					for(int childIndex = 0; childIndex < projectTree.getModel().getChildCount(phenotypeNode); ++childIndex){
						String groupName = ((ProjectTreeNode)projectTree.getModel().getChild(phenotypeNode, childIndex)).getNodeName();
						ParsePhenotypeGroup parsePhenotypeGroup = new ParsePhenotypeGroup(projectId);
		     		    parsePhenotypeGroup.saveTables(groupName); 
		     		}
		     	}
				
				//save sampling groups			
				if(projectTree.getModel().getChildCount(samplingNode) > 0)
		     	{     	
					for(int childIndex = 0; childIndex < projectTree.getModel().getChildCount(samplingNode); ++childIndex){
						String groupName = ((ProjectTreeNode)projectTree.getModel().getChild(samplingNode, childIndex)).getNodeName();
						ParseSamplingGroup parseSamplingGroup = new ParseSamplingGroup(projectId);
						parseSamplingGroup.saveTables(groupName); 
		     		}
		     	}
				
				//save experiment groups
				if(projectTree.getModel().getChildCount(experimentNode) > 0)
		     	{     	
					for(int childIndex = 0; childIndex < projectTree.getModel().getChildCount(experimentNode); ++childIndex){
						String groupName = ((ProjectTreeNode)projectTree.getModel().getChild(experimentNode, childIndex)).getNodeName();
						ParseExperimentGroup parseExperimentGroup = new ParseExperimentGroup(projectId);
		     		    parseExperimentGroup.saveTables(groupName); 
		     		}
		     	}
				
				//save harvest groups
				if(projectTree.getModel().getChildCount(harvestNode) > 0)
		     	{     	
					for(int childIndex = 0; childIndex < projectTree.getModel().getChildCount(harvestNode); ++childIndex){
						String groupName = ((ProjectTreeNode)projectTree.getModel().getChild(harvestNode, childIndex)).getNodeName();
						ParseHarvestGroup parseHarvestGroup = new ParseHarvestGroup(projectId);
		     		    parseHarvestGroup.saveTables(groupName);
		     		}
		     	}
				
				PMProjectDAO.getInstance().updateLastModifiedDate(projectId);
			    ProjectManagerAllPanel.populateTable();	 
			}
		}catch(Exception e){
			System.out.println("error in saving" +  e.getMessage());
		}
		
	}

}
