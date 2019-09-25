package org.accretegb.modules.projectmanager;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;
import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.table.DefaultTableModel;

import org.accretegb.modules.ToolBar;
import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.PlotIndexColumnTable;
import org.accretegb.modules.customswingcomponent.PlotIndexToolBoxPanel;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.germplasm.planting.Planting;
import org.accretegb.modules.hibernate.SamplingGroup;
import org.accretegb.modules.hibernate.dao.PhenotypeGroupDAO;
import org.accretegb.modules.hibernate.dao.SamplingGroupDAO;
import org.accretegb.modules.phenotype.Phenotype;
import org.accretegb.modules.sampling.SampleSelectionPanel;
import org.accretegb.modules.sampling.SampleSettingPanel;
import org.accretegb.modules.sampling.Sampling;
import org.accretegb.modules.projectexplorer.ProjectTree;
import org.accretegb.modules.projectexplorer.ProjectTreeNode;
import org.accretegb.modules.projectexplorer.Utils;
import org.accretegb.modules.projectexplorer.ProjectTreeNode.NodeType;
import org.accretegb.modules.tab.TabComponent;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.tab.TabManager;
import org.accretegb.modules.util.LoggerUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * populate sampling groups based on projectId of projects that login user has token on
 * @author Ningjing
 *
 */
public class CreateSamplingGroup {
	
	private static ParseSamplingGroup parseSamplingGroup;
	
	public CreateSamplingGroup(ProjectTree projectTree, int projectId){
		ParseSamplingGroup ParseSamplingGroup = new ParseSamplingGroup(projectId);
		this.parseSamplingGroup = ParseSamplingGroup;
		List<SamplingGroup> samplingGroups = SamplingGroupDAO.getInstance().findByProjectid(projectId);
		if(samplingGroups.size() > 0)
     	{     	
     		for(SamplingGroup samplingGroup : samplingGroups){
     			String groupName = samplingGroup.getSamplingGroupName();
     			if(groupName.contains("sampling_")) {
     				//for backward compatibility, "sampling_" was removed from sampling_ group names
     				String oldGroupName = groupName;
     				groupName = oldGroupName.replace("sampling_", "");
     				SamplingGroupDAO.getInstance().updateGroupName(projectId, oldGroupName, groupName);
     			}
     			ProjectTreeNode samplingNode = projectTree.getSamplingNode();
     			samplingNode.setParent(projectTree.getProjectRootNode());
		    	ProjectTreeNode groupNode = new ProjectTreeNode(groupName);
		        groupNode.setType(ProjectTreeNode.NodeType.SAMPLING_NODE);		       
		        List<ProjectTreeNode> parentNodes = new ArrayList<ProjectTreeNode>();
		        parentNodes.add((ProjectTreeNode) samplingNode.getParent());
		        groupNode.setParentNodes(parentNodes);
		        samplingNode.insert(groupNode,samplingNode.getChildCount());
		        String groupPath = Utils.getPathStr(groupNode.getPath());
			   try{
			        TabComponent sampleTab = createsamplingPanel(projectId, groupPath,groupName, samplingGroup.getSampleSelectionJson(), samplingGroup.getSampleSettingJson());
			        groupNode.setTabComponent(sampleTab); 
     			}catch (Exception e){
     				if(LoggerUtils.isLogEnabled())
     				{
     					LoggerUtils.log(Level.INFO, "error in loading Sampling group " + groupName);
     				}
     				TabComponent sampleTab = createsamplingPanel(projectId, groupPath,groupName, null, null);
			        groupNode.setTabComponent(sampleTab); 
				}
     		}
     	}
        
    }

	public static TabComponent createsamplingPanel(int projectId, String groupPath, String groupName, String selectionTableJson, String settingTableJson) {
		List<String> columnNames = new ArrayList<String>();
		columnNames.add(ColumnConstants.SELECT);
		columnNames.add(ColumnConstants.ROW_NUM);
		columnNames.add(ColumnConstants.TAG_ID);
		columnNames.add(ColumnConstants.TYPES);
		columnNames.add(ColumnConstants.ROW);
		columnNames.add(ColumnConstants.PLANT);
		columnNames.add(ColumnConstants.X);
		columnNames.add(ColumnConstants.Y);
		columnNames.add(ColumnConstants.TAG);
		columnNames.add(ColumnConstants.STOCK_ID);
		columnNames.add(ColumnConstants.STOCK_NAME);
		columnNames.add(ColumnConstants.ACCESSION);
		columnNames.add(ColumnConstants.PEDIGREE);
		columnNames.add(ColumnConstants.GENERATION);
		columnNames.add(ColumnConstants.CYCLE);
		columnNames.add(ColumnConstants.CLASSIFICATION_CODE);
		columnNames.add(ColumnConstants.POPULATION);
		columnNames.add(ColumnConstants.START_TAG);
		columnNames.add(ColumnConstants.END_TAG);
		columnNames.add(ColumnConstants.COUNT);
		columnNames.add(ColumnConstants.PLANTING_DATE);
		columnNames.add(ColumnConstants.KERNELS);
		columnNames.add(ColumnConstants.DELAY);
		columnNames.add(ColumnConstants.PURPOSE);
		columnNames.add(ColumnConstants.COMMENT);
		columnNames.add(ColumnConstants.MATING_PLANT_ID);
		columnNames.add(ColumnConstants.MATING_PLAN);
		columnNames.add(ColumnConstants.REP);
		columnNames.add(ColumnConstants.EXP_ID);
		columnNames.add(ColumnConstants.EXP_FACTOR_VALUE_IDS);

		List<String> showColumns = new ArrayList<String>();
		showColumns.add(ColumnConstants.SELECT);
		showColumns.add(ColumnConstants.TYPES);
		showColumns.add(ColumnConstants.ROW);
		showColumns.add(ColumnConstants.PLANT);
		showColumns.add(ColumnConstants.X);
		showColumns.add(ColumnConstants.Y);
		showColumns.add(ColumnConstants.TAG);
		showColumns.add(ColumnConstants.STOCK_NAME);
		showColumns.add(ColumnConstants.ACCESSION);
		showColumns.add(ColumnConstants.PEDIGREE);
		showColumns.add(ColumnConstants.GENERATION);
		showColumns.add(ColumnConstants.CYCLE);
		showColumns.add(ColumnConstants.CLASSIFICATION_CODE);
		showColumns.add(ColumnConstants.POPULATION);
		showColumns.add(ColumnConstants.START_TAG);
		showColumns.add(ColumnConstants.END_TAG);
		showColumns.add(ColumnConstants.COUNT);
		showColumns.add(ColumnConstants.PLANTING_DATE);
		showColumns.add(ColumnConstants.KERNELS);
		showColumns.add(ColumnConstants.DELAY);
		showColumns.add(ColumnConstants.PURPOSE);
		showColumns.add(ColumnConstants.COMMENT);
		showColumns.add(ColumnConstants.MATING_PLAN);
		showColumns.add(ColumnConstants.REP);

		BeanDefinitionBuilder sampleSelectionTableDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(CheckBoxIndexColumnTable.class).addPropertyValue("columnNames", columnNames)
				.addPropertyValue("showColumns", showColumns)
				.addPropertyValue("checkBoxHeader", new Boolean(false)).setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("sampleSelectionTable" + groupPath,
				sampleSelectionTableDefinitionBuilder.getBeanDefinition());

		List<String> horizontalList = new ArrayList<String>();
		horizontalList.add("selection");
		horizontalList.add("search");
		horizontalList.add("gap");
		horizontalList.add("subset");
		horizontalList.add("columnSelector");
		horizontalList.add("counter");

		BeanDefinitionBuilder samplingTablePanelDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(TableToolBoxPanel.class)
				.addPropertyValue("table", getContext().getBean("sampleSelectionTable" + groupPath))
				.addPropertyValue("horizontalList", horizontalList)
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("sampleSelectionTablePanel"
				+ groupPath, samplingTablePanelDefinitionBuilder.getBeanDefinition());


		
		BeanDefinitionBuilder sampleSelectionPanelDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(SampleSelectionPanel.class)
				.addPropertyValue("projectID", projectId)
				.addPropertyValue("sampleSelectionTablePanel", getContext().getBean("sampleSelectionTablePanel" + groupPath))
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("sampleSelectionPanel" + groupPath,
				sampleSelectionPanelDefinitionBuilder.getBeanDefinition());
		
		
    	columnNames = new ArrayList<String>();
		columnNames.add(ColumnConstants.SELECT);
		columnNames.add(ColumnConstants.ROW_NUM);
		columnNames.add(ColumnConstants.TAG_ID);
		columnNames.add(ColumnConstants.TYPES);
		columnNames.add(ColumnConstants.ROW);
		columnNames.add(ColumnConstants.PLANT);
		columnNames.add(ColumnConstants.X);
		columnNames.add(ColumnConstants.Y);
		columnNames.add(ColumnConstants.TAG);
		columnNames.add(ColumnConstants.STOCK_ID);
		columnNames.add(ColumnConstants.STOCK_NAME);
		columnNames.add(ColumnConstants.ACCESSION);
		columnNames.add(ColumnConstants.PEDIGREE);
		columnNames.add(ColumnConstants.GENERATION);
		columnNames.add(ColumnConstants.CYCLE);
		columnNames.add(ColumnConstants.CLASSIFICATION_CODE);
		columnNames.add(ColumnConstants.POPULATION);
		columnNames.add(ColumnConstants.START_TAG);
		columnNames.add(ColumnConstants.END_TAG);
		columnNames.add(ColumnConstants.COUNT);
		columnNames.add(ColumnConstants.PLANTING_DATE);
		columnNames.add(ColumnConstants.KERNELS);
		columnNames.add(ColumnConstants.DELAY);
		columnNames.add(ColumnConstants.PURPOSE);
		columnNames.add(ColumnConstants.COMMENT);
		columnNames.add(ColumnConstants.MATING_PLANT_ID);
		columnNames.add(ColumnConstants.MATING_PLAN);
		columnNames.add(ColumnConstants.REP);
		columnNames.add(ColumnConstants.EXP_ID);
		columnNames.add(ColumnConstants.EXP_FACTOR_VALUE_IDS);
		columnNames.add(ColumnConstants.SAMPLENAME);
		columnNames.add(ColumnConstants.LOCATION);
		columnNames.add(ColumnConstants.COLLECTION_DATE);
		columnNames.add(ColumnConstants.COLLECTOR);
		
		

		showColumns = new ArrayList<String>();
		showColumns.add(ColumnConstants.SELECT);
		showColumns.add(ColumnConstants.SAMPLENAME);
		showColumns.add(ColumnConstants.TAG);
		showColumns.add(ColumnConstants.STOCK_NAME);
		showColumns.add(ColumnConstants.ACCESSION);
		showColumns.add(ColumnConstants.PEDIGREE);
		showColumns.add(ColumnConstants.LOCATION);
		showColumns.add(ColumnConstants.COLLECTOR);
		showColumns.add(ColumnConstants.COLLECTION_DATE);

		BeanDefinitionBuilder sampleSettingTableDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(CheckBoxIndexColumnTable.class).addPropertyValue("columnNames", columnNames)
				.addPropertyValue("showColumns", showColumns)
				.addPropertyValue("checkBoxHeader", new Boolean(false)).setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("sampleSettingTable" + groupPath,
				sampleSettingTableDefinitionBuilder.getBeanDefinition());

		horizontalList = new ArrayList<String>();
		horizontalList.add("selection");
		horizontalList.add("search");
		horizontalList.add("gap");
		horizontalList.add("subset");
		horizontalList.add("columnSelector");
		horizontalList.add("counter");
		horizontalList.add("refresh");

		BeanDefinitionBuilder samplingSettingTablePanelDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(TableToolBoxPanel.class)
				.addPropertyValue("table", getContext().getBean("sampleSettingTable" + groupPath))
				.addPropertyValue("horizontalList", horizontalList)
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("sampleSettingTablePanel"
				+ groupPath, samplingSettingTablePanelDefinitionBuilder.getBeanDefinition());

		
		BeanDefinitionBuilder samplingSettingPanelDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(SampleSettingPanel.class)
				.addPropertyValue("projectID", projectId)
				.addPropertyValue("sampleSettingTablePanel", getContext().getBean("sampleSettingTablePanel" + groupPath))
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("sampleSettingPanel" + groupPath,
				samplingSettingPanelDefinitionBuilder.getBeanDefinition());
		
		
		BeanDefinitionBuilder samplingChildPanel0DefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(Sampling.class)
				.addPropertyValue("sampleSelectionPanel", getContext().getBean("sampleSelectionPanel" + groupPath))
				.addPropertyValue("sampleSettingPanel", getContext().getBean("sampleSettingPanel" + groupPath))
				.setInitMethodName("initialize");
		
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("Sampling - " + projectId + groupName,
				samplingChildPanel0DefinitionBuilder.getBeanDefinition());

    	// TabComponent
		Sampling samplingTab = (Sampling) getContext().getBean("Sampling - " + projectId + groupName);
		samplingTab.setName(groupPath);
		List<TabComponentPanel> componentPanels = new ArrayList<TabComponentPanel>();
		componentPanels.add(samplingTab);

		BeanDefinitionBuilder samplingTabDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(TabComponent.class).addPropertyValue("title", "Sampling - " + groupName)
				.addPropertyValue("isStatic", false).addPropertyValue("componentPanels", componentPanels)
				.setInitMethodName("initialize");			
		
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("samplingTab" + projectId + groupName,
				samplingTabDefinitionBuilder.getBeanDefinition());			
		TabComponent tabComponent = (TabComponent) getContext().getBean("samplingTab" + projectId + groupName);
		
		TabManager tabManager = (TabManager) getContext().getBean("tabManager");
		tabManager.getTabComponents().add(tabComponent);  
		
	    String plantingGroupName = groupName;
		Planting plantingPanel = (Planting) getContext().getBean("Planting - " + projectId + plantingGroupName);
		plantingPanel.getTagGenerator().setSampleSelectionPanel(samplingTab.getSampleSelectionPanel());
		String phenotypeGroupName = groupName;
		Phenotype phenotypePanel = (Phenotype) getContext().getBean("Phenotyping - " + projectId + phenotypeGroupName);
		phenotypePanel.getPhenotypeExportPanel().setSampleSelectionPanel(samplingTab.getSampleSelectionPanel());
		samplingTab.getSampleSelectionPanel().setPhenotypeExportPanel(phenotypePanel.getPhenotypeExportPanel());
	
		
		//parse selectionTableJson for export panel
        if(selectionTableJson !=null && !selectionTableJson.equals(""))
        {
        	parseSamplingGroup.getSamplingSelectionTable(selectionTableJson, groupName);
        	samplingTab.getSampleSelectionPanel().setIsFirstSync(false);
        }
        
      //parse selectionSettingJson for export panel
        if(settingTableJson !=null && !settingTableJson.equals(""))
        {
        	parseSamplingGroup.getSamplingSettingTable(settingTableJson, groupName);
        }
        
		return tabComponent;
    }

}
