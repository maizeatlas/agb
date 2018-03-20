package org.accretegb.modules.projectmanager;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;
import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.table.DefaultTableModel;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.PlotIndexColumnTable;
import org.accretegb.modules.customswingcomponent.PlotIndexToolBoxPanel;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.germplasm.planting.Canvas;
import org.accretegb.modules.germplasm.planting.FieldSelection;
import org.accretegb.modules.germplasm.planting.MapView;
import org.accretegb.modules.germplasm.planting.Planting;
import org.accretegb.modules.germplasm.planting.PlantingRow;
import org.accretegb.modules.germplasm.planting.TableView;
import org.accretegb.modules.germplasm.planting.TagGenerator;
import org.accretegb.modules.hibernate.PlantingGroup;
import org.accretegb.modules.hibernate.dao.PlantingGroupDAO;
import org.accretegb.modules.hibernate.dao.StockSelectionGroupDAO;
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
 * populate planting groups based on projectId of projects that login user has token on
 * @author Ningjing
 *
 */
public class CreatePlantingGroup {
	
	private static ParsePlantingGroup parsePlantingGroup;
	
	public CreatePlantingGroup(ProjectTree projectTree, int projectId){
		ParsePlantingGroup parsePlantingGroup = new ParsePlantingGroup(projectId);
		this.parsePlantingGroup = parsePlantingGroup;
		List<PlantingGroup> plantingGroups = PlantingGroupDAO.getInstance().findByProjectid(projectId);
		if(plantingGroups.size() > 0)
     	{     	
     		for(PlantingGroup plantingGroup : plantingGroups){
     			String groupName = plantingGroup.getPlantingGroupName();   			
     			ProjectTreeNode plantingNode = projectTree.getPlantingNode();
     			plantingNode.setParent(projectTree.getProjectRootNode());
		    	ProjectTreeNode groupNode = new ProjectTreeNode(groupName);
		        groupNode.setType(ProjectTreeNode.NodeType.PLANTING_NODE);		       
		        List<ProjectTreeNode> parentNodes = new ArrayList<ProjectTreeNode>();
		        parentNodes.add((ProjectTreeNode) plantingNode.getParent());
		        groupNode.setParentNodes(parentNodes);
		        plantingNode.insert(groupNode,plantingNode.getChildCount());
		        String groupPath = Utils.getPathStr(groupNode.getPath());
		        try{
			        TabComponent plantingTab = createPlantingPanel(projectId, groupPath,groupName, plantingGroup.getTableViewJson(),plantingGroup.getTagGeneratorJson());
			        groupNode.setTabComponent(plantingTab); 
     			}catch (Exception e){
     				if(LoggerUtils.isLogEnabled())
     				{
     					LoggerUtils.log(Level.INFO, "error in loading Planting group " + groupName);
     				}
     				TabComponent plantingTab = createPlantingPanel(projectId, groupPath,groupName, null,null);
			        groupNode.setTabComponent(plantingTab); 
				}
     		}
     	}
        
    }

	public static TabComponent createPlantingPanel(int projectId, String groupPath, String groupName, String tableViewJson, String tagGeneratorJson) {
    	
		List<String> columnNames = new ArrayList<String>();
		columnNames.add(ColumnConstants.SELECT);
		columnNames.add(ColumnConstants.FIELD_ID);
		columnNames.add(ColumnConstants.FIELD_NUMBER);
		columnNames.add(ColumnConstants.FIELD_NAME);
		columnNames.add(ColumnConstants.LATITUDE);
		columnNames.add(ColumnConstants.LONGITUDE);
		columnNames.add(ColumnConstants.CITY);
		columnNames.add(ColumnConstants.ZIPCODE);
		columnNames.add(ColumnConstants.STATE);
		columnNames.add(ColumnConstants.COUNTRY);

		BeanDefinitionBuilder fieldInfoTablePlantingDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(CheckBoxIndexColumnTable.class).addPropertyValue("columnNames", columnNames)
				.addPropertyValue("checkBoxHeader", new Boolean(false)).setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("fieldInfoTable" + groupPath,
				fieldInfoTablePlantingDefinitionBuilder.getBeanDefinition());
		

		List<String> horizontalList = new ArrayList<String>();
		horizontalList.add("search");
		horizontalList.add("gap");
		horizontalList.add("columnSelector");
		horizontalList.add("add");
		horizontalList.add("edit");
		horizontalList.add("delete");
		
		BeanDefinitionBuilder fieldSelectionPanelPlantingDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(TableToolBoxPanel.class)
				.addPropertyValue("table", getContext().getBean("fieldInfoTable" + groupPath))
				.addPropertyValue("horizontalList", horizontalList).setInitMethodName("initialize");
		
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("plantingFieldSelectionPanel"
				+ groupPath, fieldSelectionPanelPlantingDefinitionBuilder.getBeanDefinition());
		
		
		BeanDefinitionBuilder fieldSelectionPlantingDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(FieldSelection.class)
				.addPropertyValue("fieldSelectionPanel",getContext().getBean("plantingFieldSelectionPanel" + groupPath))
				.setInitMethodName("initialize");
		
		
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("plantingFieldSelection" + groupPath,
				fieldSelectionPlantingDefinitionBuilder.getBeanDefinition());

		columnNames = new ArrayList<String>();
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
		columnNames.add(ColumnConstants.MODIFIED);
		columnNames.add(ColumnConstants.EXP_ID);
		columnNames.add(ColumnConstants.EXP_FACTOR_VALUE_IDS);

		List<Integer> editableColumns = new ArrayList<Integer>();
		editableColumns.add(21);
		editableColumns.add(22);
		editableColumns.add(23);
		editableColumns.add(24);

		BeanDefinitionBuilder stocksInfoTablePlantingDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(PlotIndexColumnTable.class).addPropertyValue("columnNames", columnNames)
				.addPropertyValue("editableColumns", editableColumns)
				.addPropertyValue("checkBoxHeader", new Boolean(false)).setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("stocksInfoTable" + groupPath,
				stocksInfoTablePlantingDefinitionBuilder.getBeanDefinition());
		
		PlotIndexColumnTable stocksInfoTable = (PlotIndexColumnTable) getContext().getBean("stocksInfoTable" + groupPath);
		
		horizontalList = new ArrayList<String>();
		horizontalList.add("selection");
		horizontalList.add("gap");
		horizontalList.add("columnSelector");
		horizontalList.add("counter");
		
		List<String> verticalList = new ArrayList<String>();
		verticalList.add("add");
		verticalList.add("delete");
		verticalList.add("moveUp");
		verticalList.add("moveDown");

		BeanDefinitionBuilder stocksOrderPanelPlantingDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(PlotIndexToolBoxPanel.class)
				.addPropertyValue("table", getContext().getBean("stocksInfoTable" + groupPath))
				.addPropertyValue("horizontalList", horizontalList).addPropertyValue("verticalList", verticalList)
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("plantingStocksOrderPanel"
				+ groupPath, stocksOrderPanelPlantingDefinitionBuilder.getBeanDefinition());

		BeanDefinitionBuilder tableViewPlantingDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(TableView.class)
				.addPropertyValue("stocksOrderPanel",getContext().getBean("plantingStocksOrderPanel" + groupPath))
				.setInitMethodName("initialize");
		
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("plantingTableView" + groupPath,
				tableViewPlantingDefinitionBuilder.getBeanDefinition());

		BeanDefinitionBuilder canvasPlantingDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(Canvas.class)
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("plantingCanvas" + groupPath,
				canvasPlantingDefinitionBuilder.getBeanDefinition());

		BeanDefinitionBuilder mapViewPlantingDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(MapView.class)
				.addPropertyValue("canvas", getContext().getBean("plantingCanvas" + groupPath))
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("plantingMapView" + groupPath,
				mapViewPlantingDefinitionBuilder.getBeanDefinition());

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
		columnNames.add(ColumnConstants.MODIFIED);
		columnNames.add(ColumnConstants.EXP_ID);
		columnNames.add(ColumnConstants.EXP_FACTOR_VALUE_IDS);

		editableColumns = new ArrayList<Integer>();
		editableColumns.add(17);
		editableColumns.add(18);
		editableColumns.add(19);
		editableColumns.add(21);
		editableColumns.add(22);
		editableColumns.add(23);
		editableColumns.add(24);

		BeanDefinitionBuilder tagsTablePlantingDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(CheckBoxIndexColumnTable.class).addPropertyValue("columnNames", columnNames)
				.addPropertyValue("editableColumns", editableColumns)
				.addPropertyValue("checkBoxHeader", new Boolean(false)).setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("tagsTable" + groupPath,
				tagsTablePlantingDefinitionBuilder.getBeanDefinition());

		horizontalList = new ArrayList<String>();
		horizontalList.add("selection");
		horizontalList.add("search");
		horizontalList.add("gap");
		horizontalList.add("columnSelector");
		horizontalList.add("refresh");
		horizontalList.add("counter");

		BeanDefinitionBuilder tagsTablePanelPlantingDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(TableToolBoxPanel.class)
				.addPropertyValue("table", getContext().getBean("tagsTable" + groupPath))
				.addPropertyValue("horizontalList", horizontalList).setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("plantingTagsPanel"
				+ groupPath, tagsTablePanelPlantingDefinitionBuilder.getBeanDefinition());

		BeanDefinitionBuilder tagGeneratorPlantingDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(TagGenerator.class)
				.addPropertyValue("tagsTablePanel", getContext().getBean("plantingTagsPanel" + groupPath))
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("plantingTagGenerator" + groupPath,
				tagGeneratorPlantingDefinitionBuilder.getBeanDefinition());

		BeanDefinitionBuilder plantingChildPanel0DefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(Planting.class)
				.addPropertyValue("fieldSelection", getContext().getBean("plantingFieldSelection" + groupPath))
				.addPropertyValue("tableView", getContext().getBean("plantingTableView" + groupPath))
				.addPropertyValue("mapView", getContext().getBean("plantingMapView" + groupPath))
				.addPropertyValue("tagGenerator", getContext().getBean("plantingTagGenerator" + groupPath))
				.setInitMethodName("initialize");
		
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("Planting - " + projectId + groupName,
				plantingChildPanel0DefinitionBuilder.getBeanDefinition());

		// TabComponent
		Planting plantingPanel = (Planting) getContext().getBean("Planting - " + projectId + groupName);
		plantingPanel.setName(groupPath);
		List<TabComponentPanel> componentPanels = new ArrayList<TabComponentPanel>();
		componentPanels.add(plantingPanel);       

		BeanDefinitionBuilder plantingTabDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(TabComponent.class).addPropertyValue("title", "Planting - " + groupName)
				.addPropertyValue("isStatic", false).addPropertyValue("componentPanels", componentPanels)
				.setInitMethodName("initialize");
		
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("plantingTab" + projectId + groupName,
				plantingTabDefinitionBuilder.getBeanDefinition());
		TabComponent tabComponent = (TabComponent) getContext().getBean("plantingTab" + projectId + groupName);
		TabManager tabManager = (TabManager) getContext().getBean("tabManager");
		tabManager.getTabComponents().add(tabComponent);  
		
		
		DefaultTableModel tableModel = ((DefaultTableModel)stocksInfoTable.getModel());
        removeAllRowsFromTable(tableModel);	
        
        //parse tableViewJson into tableView table
        if(tableViewJson !=null && !tableViewJson.equals(""))
        {
        	List<Object[]> tableViewRows = parsePlantingGroup.getTableViewTable(tableViewJson, groupName);
            //System.out.println(((String) tableViewRows.get(0)[7]).split("\\.") );
            if(Integer.parseInt((String) parsePlantingGroup.fieldId) != -1){
        		CheckBoxIndexColumnTable table = (CheckBoxIndexColumnTable)getContext().getBean("fieldInfoTable" + groupPath);
        		for(int row = 0; row < table.getRowCount();++row){
        			if(table.getValueAt(row, table.getIndexOf(ColumnConstants.FIELD_ID)).equals((String)parsePlantingGroup.fieldId)){
        				table.setValueAt(true, row, table.getIndexOf(ColumnConstants.SELECT));
        				table.addRowSelectionInterval(row, row);
        				table.getCheckedRows().add(row);
        				plantingPanel.getFieldSelection().setZipcode(String.valueOf(table.getValueAt(row, ColumnConstants.ZIPCODE)));
        				plantingPanel.getTableView().setZipcode(String.valueOf(table.getValueAt(row, ColumnConstants.ZIPCODE)));
        				//plantingPanel.getTableView().seasonIndex = seasonIndex;
        				plantingPanel.getFieldSelection().setFieldId(Integer.parseInt((String) table.getValueAt(row, ColumnConstants.FIELD_ID)));   
        				plantingPanel.visitedTableView = true;
                		plantingPanel.fromDatabase=true;
                		plantingPanel.visitedMapView=true;
                		break;
        			}        			
        		}
        		plantingPanel.getTableView().synced = parsePlantingGroup.synced;
        		String[] list = String.valueOf(tableViewRows.get(0)[7]).split("\\.");
        		String prefix = list[0] +"."+ list[1] +"."+ list[2]+".";
        		plantingPanel.getTableView().prefix = prefix;
        		plantingPanel.getTableView().plantingIndex = list[1];
        		plantingPanel.getTableView().prefixIsFixed = parsePlantingGroup.prefixIsFixed;
        		
        		plantingPanel.getTableView().populateTableFromObjects(tableViewRows); 
        	}        	
        	//parse tagGeneratorJson into tag generator panel table
        	if(tagGeneratorJson != null && !tagGeneratorJson.equals("")){
        		List<Object[]> tagGenertorRows = parsePlantingGroup.getTagGeneratorTable(tagGeneratorJson, groupName);       		
        		plantingPanel.getTagGenerator().populateTableFromObjects(tagGenertorRows);        		
        	}       	
        }
        
        return tabComponent;
    }

}
