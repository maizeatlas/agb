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
import org.accretegb.modules.hibernate.PhenotypeGroup;
import org.accretegb.modules.hibernate.dao.PMProjectDAO;
import org.accretegb.modules.hibernate.dao.PhenotypeGroupDAO;
import org.accretegb.modules.hibernate.dao.PlantingGroupDAO;
import org.accretegb.modules.phenotype.Phenotype;
import org.accretegb.modules.phenotype.PhenotypeExportPanel;
import org.accretegb.modules.phenotype.PhenotypeImportPanel;
import org.accretegb.modules.phenotype.PhenotypeInfoPanel;
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
 * populate phenotype groups based on projectId of projects that login user has token on
 * @author Ningjing
 *
 */
public class CreatePhenotypeGroup {
	
	private static ParsePhenotypeGroup parsePhenotypeGroup;
	
	public CreatePhenotypeGroup(ProjectTree projectTree, int projectId){
		ParsePhenotypeGroup ParsePhenotypeGroup = new ParsePhenotypeGroup(projectId);
		this.parsePhenotypeGroup = ParsePhenotypeGroup;
		List<PhenotypeGroup> phenotypeGroups = PhenotypeGroupDAO.getInstance().findByProjectid(projectId);
		if(phenotypeGroups.size() > 0)
     	{     	
     		for(PhenotypeGroup phenotypeGroup : phenotypeGroups){
     			String groupName = phenotypeGroup.getPhenotypeGroupName();   	
     			if(groupName.contains("phenotype_")) {
     				//for backward compatibility, "phenotype_" was removed from phenotype group names
     				String oldGroupName = groupName;
     				groupName = oldGroupName.replace("phenotype_", "");
     				PhenotypeGroupDAO.getInstance().updateGroupName(projectId, oldGroupName, groupName);
     			}
     			ProjectTreeNode phenotypeNode = projectTree.getPhenotypeNode();
     			phenotypeNode.setParent(projectTree.getProjectRootNode());
		    	ProjectTreeNode groupNode = new ProjectTreeNode(groupName);
		        groupNode.setType(ProjectTreeNode.NodeType.PHENOTYPE_NODE);		       
		        List<ProjectTreeNode> parentNodes = new ArrayList<ProjectTreeNode>();
		        parentNodes.add((ProjectTreeNode) phenotypeNode.getParent());
		        groupNode.setParentNodes(parentNodes);
		        phenotypeNode.insert(groupNode,phenotypeNode.getChildCount());
		        String groupPath = Utils.getPathStr(groupNode.getPath());
			    try{
			        TabComponent phenoTab = createphenotypePanel(projectId, groupPath,groupName, phenotypeGroup.getExportTableJson(),phenotypeGroup.getImportTableJson());
			        groupNode.setTabComponent(phenoTab); 
     			}catch (Exception e){
     				if(LoggerUtils.isLogEnabled())
     				{
     					LoggerUtils.log(Level.INFO, "error in loading Phenotype group " + groupName);
     				}
     				 TabComponent phenoTab = createphenotypePanel(projectId, groupPath,groupName, null,null);
 			        groupNode.setTabComponent(phenoTab); 
				}
     		}
     	}
        
    }

	public static TabComponent createphenotypePanel(int projectId, String groupPath, String groupName, String exportTableJson, String importTableJson) {
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

		BeanDefinitionBuilder tagsTablePlantingDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(CheckBoxIndexColumnTable.class).addPropertyValue("columnNames", columnNames)
				.addPropertyValue("showColumns", showColumns)
				.addPropertyValue("checkBoxHeader", new Boolean(false)).setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("phenotypeTagsTable" + groupPath,
				tagsTablePlantingDefinitionBuilder.getBeanDefinition());

		List<String> horizontalList = new ArrayList<String>();
		horizontalList.add("selection");
		horizontalList.add("search");
		horizontalList.add("gap");
		horizontalList.add("subset");
		horizontalList.add("columnSelector");
		horizontalList.add("counter");

		BeanDefinitionBuilder tagsTablePanelPhenotypeDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(TableToolBoxPanel.class)
				.addPropertyValue("table", getContext().getBean("phenotypeTagsTable" + groupPath))
				.addPropertyValue("horizontalList", horizontalList)
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("phenotypeTagsPanel"
				+ groupPath, tagsTablePanelPhenotypeDefinitionBuilder.getBeanDefinition());


		
		BeanDefinitionBuilder exportPanelDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(PhenotypeExportPanel.class)
				.addPropertyValue("phenotypeTagsTablePanel", getContext().getBean("phenotypeTagsPanel" + groupPath))
				.addPropertyValue("exportButton", new JButton("Export"))
				.addPropertyValue("descriptorsOptions", new JList())
				.addPropertyValue("descriptorsSelected", new JList())
				.addPropertyValue("parameterOptions", new JList())
				.addPropertyValue("parameterSelected", new JList())
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("phenotypeExportPanel" + groupPath,
				exportPanelDefinitionBuilder.getBeanDefinition());
		
		
		BeanDefinitionBuilder importPanelDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(PhenotypeImportPanel.class)
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("phenotypeImportPanel" + groupPath,
				importPanelDefinitionBuilder.getBeanDefinition());
		
		BeanDefinitionBuilder phenotypeChildPanel0DefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(Phenotype.class)
				.addPropertyValue("phenotypeExportPanel", getContext().getBean("phenotypeExportPanel" + groupPath))
				.addPropertyValue("phenotypeImportPanel", getContext().getBean("phenotypeImportPanel" + groupPath))
				.setInitMethodName("initialize");
		
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("Phenotyping - " + projectId + groupName,
				phenotypeChildPanel0DefinitionBuilder.getBeanDefinition());
		
    	// TabComponent
		Phenotype phenotypePanel = (Phenotype) getContext().getBean("Phenotyping - " + projectId + groupName);
		
		phenotypePanel.setName(groupPath);
		List<TabComponentPanel> componentPanels = new ArrayList<TabComponentPanel>();
		componentPanels.add(phenotypePanel);

		BeanDefinitionBuilder phenotypeTabDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(TabComponent.class).addPropertyValue("title", "Phenotyping - " + groupName)
				.addPropertyValue("isStatic", false).addPropertyValue("componentPanels", componentPanels)
				.setInitMethodName("initialize");	
		
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("phenotypeTab" + projectId + groupName,
				phenotypeTabDefinitionBuilder.getBeanDefinition());
		
		TabComponent tabComponent = (TabComponent) getContext().getBean("phenotypeTab" + projectId + groupName);
		TabManager tabManager = (TabManager) getContext().getBean("tabManager");
		tabManager.getTabComponents().add(tabComponent);  
		
		String plantingGroupName = groupName;

		Planting plantingPanel = (Planting) getContext().getBean("Planting - " + projectId + plantingGroupName);
		plantingPanel.getTagGenerator().setPhenotypeExportPanel(phenotypePanel.getPhenotypeExportPanel());		
	
		//parse exportTableJson for export panel
        if(exportTableJson !=null && !exportTableJson.equals(""))
        {
        	List<Object[]> exportTableRows = parsePhenotypeGroup.getExportTagsTable(exportTableJson, groupName);
        	phenotypePanel.getPhenotypeExportPanel().populateTableFromObjects(exportTableRows);
        	Vector data = ((DefaultTableModel) plantingPanel.getTagGenerator().getTagsTablePanel().getTable().getModel()).getDataVector();
    		phenotypePanel.getPhenotypeExportPanel().setTableData(data);
    		phenotypePanel.getPhenotypeExportPanel().setIsFirstSync(false);
        	
        }
        if(importTableJson !=null && !importTableJson.equals("")){
        	List<Object[]> exportTableRows = parsePhenotypeGroup.getImportTable(importTableJson, groupName);
        	phenotypePanel.getPhenotypeImportPanel().populateTableFromObjects(exportTableRows);
        }
        
		return tabComponent;
    }

}
