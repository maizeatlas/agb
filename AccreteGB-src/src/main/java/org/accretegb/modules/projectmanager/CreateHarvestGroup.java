package org.accretegb.modules.projectmanager;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;
import static org.accretegb.modules.projectexplorer.Utils.getTreePath;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;

import org.accretegb.modules.ToolBar;
import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.germplasm.harvesting.Bulk;
import org.accretegb.modules.germplasm.harvesting.FieldGenerated;
import org.accretegb.modules.germplasm.harvesting.Harvesting;
import org.accretegb.modules.germplasm.harvesting.StickerGenerator;
import org.accretegb.modules.germplasm.planting.Planting;
import org.accretegb.modules.germplasm.stocksinfo.StocksInfoPanel;
import org.accretegb.modules.hibernate.HarvestingGroup;
import org.accretegb.modules.hibernate.StockSelectionGroup;
import org.accretegb.modules.hibernate.dao.ExperimentGroupDAO;
import org.accretegb.modules.hibernate.dao.HarvestingGroupDAO;
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
 * create harvest group panel and populate tables after parsing the json files
 * @author Ningjing
 *
 */
public class CreateHarvestGroup {
	
	private static ParseHarvestGroup parseHarvestGroup;
	public CreateHarvestGroup(ProjectTree projectTree, int projectId){
		ParseHarvestGroup parseHarvestGroup = new ParseHarvestGroup(projectId);
		this.parseHarvestGroup = parseHarvestGroup;
		List<HarvestingGroup> harvestGroups = HarvestingGroupDAO.getInstance().findByProjectid(projectId);
		if(harvestGroups.size() > 0)
     	{     	
     		for(HarvestingGroup harvestingGroup : harvestGroups){
     			String groupName = harvestingGroup.getHarvestingGroupName();
     			ProjectTreeNode harvestingNode = projectTree.getHarvestingNode();
     			harvestingNode.setParent(projectTree.getProjectRootNode());
		    	ProjectTreeNode groupNode = new ProjectTreeNode(groupName);
		        groupNode.setType(ProjectTreeNode.NodeType.HARVESTING_NODE);		       
		        List<ProjectTreeNode> parentNodes = new ArrayList<ProjectTreeNode>();
		        parentNodes.add((ProjectTreeNode) harvestingNode.getParent());
		        groupNode.setParentNodes(parentNodes);
		        harvestingNode.insert(groupNode,harvestingNode.getChildCount());
		        String groupPath = Utils.getPathStr(groupNode.getPath());
		        try{
			        TabComponent harvestTab = createHarvestPanel(projectId, groupPath,groupName, harvestingGroup.getCrossRecordJson(),harvestingGroup.getBulkJson(),harvestingGroup.getStickerGeneratorJson());
			        groupNode.setTabComponent(harvestTab); 
     			}catch (Exception e){
     				if(LoggerUtils.isLogEnabled())
     				{
     					LoggerUtils.log(Level.INFO, "error in loading Harvesting group " + groupName);
     				}
     				 TabComponent harvestTab = createHarvestPanel(projectId, groupPath,groupName, null,null,null);
 			        groupNode.setTabComponent(harvestTab); 
				}
     		}
     	}
        
    }

    public static TabComponent createHarvestPanel(int projectId, String groupPath, String groupName, String crossRecordJson, String bulkJson, String stickerGeneratorJson) {
        // crossing table bean definition
        List<String> columnNames = new ArrayList<String>();
        columnNames.add(ColumnConstants.SELECT);
        columnNames.add(ColumnConstants.ROW_NUM);
        columnNames.add(ColumnConstants.TAG_ID);
        columnNames.add(ColumnConstants.TAG_NAME);
        columnNames.add(ColumnConstants.ACCESSION);
        columnNames.add(ColumnConstants.PEDIGREE);
        columnNames.add(ColumnConstants.GENERATION);
        columnNames.add(ColumnConstants.MATE_METHOD);
        columnNames.add(ColumnConstants.MATING_TYPE);
        columnNames.add(ColumnConstants.ROLE);
        columnNames.add(ColumnConstants.MATE_LINK);
        columnNames.add(ColumnConstants.QUANTITY);
        columnNames.add(ColumnConstants.UNIT);
        columnNames.add(ColumnConstants.STOCK_NAME);
        columnNames.add(ColumnConstants.SELECTION);
       
        List<Integer> editableColumns = new ArrayList<Integer>();
        //editableColumns.add(10);
        
        BeanDefinitionBuilder crossingTableHarvestingDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(CheckBoxIndexColumnTable.class)
                .addPropertyValue("columnNames", columnNames)
                .addPropertyValue("editableColumns", editableColumns)
                .addPropertyValue("checkBoxHeader", new Boolean(false))
                .addPropertyValue("singleSelection", new Boolean(false))
                .setInitMethodName("initialize");

        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("crossingTable" + groupPath,
                crossingTableHarvestingDefinitionBuilder.getBeanDefinition());

        // crossing table panel
        List<String> horizontalList = new ArrayList<String>();
        horizontalList.add("selection");
        horizontalList.add("search");
        horizontalList.add("gap");
        horizontalList.add("columnSelector");      
        horizontalList.add("delete");
        horizontalList.add("counter");
        BeanDefinitionBuilder crossingTablePanelHarvestingDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(TableToolBoxPanel.class)
                .addPropertyValue("table", getContext().getBean("crossingTable" + groupPath))
                .addPropertyValue("horizontalList", horizontalList)
                .setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("crossingTablePanel"
                + groupPath, crossingTablePanelHarvestingDefinitionBuilder.getBeanDefinition());

        //field generated group
        BeanDefinitionBuilder fieldGeneratedHarvestingDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(FieldGenerated.class)
        		.addPropertyValue("projectID", projectId)
                .addPropertyValue("crossingTablePanel",getContext().getBean("crossingTablePanel"+groupPath))
                .setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("fieldGenerated"
                + groupPath, fieldGeneratedHarvestingDefinitionBuilder.getBeanDefinition());

        // bulk table bean definition
        columnNames = new ArrayList<String>();
        columnNames.add(ColumnConstants.SELECT);
        columnNames.add(ColumnConstants.ROW_NUM);
        columnNames.add(ColumnConstants.STOCK_ID);
        columnNames.add(ColumnConstants.TAG_ID);
        columnNames.add(ColumnConstants.STOCK_NAME);
        columnNames.add(ColumnConstants.ACCESSION);
        columnNames.add(ColumnConstants.PEDIGREE);
        columnNames.add(ColumnConstants.QUANTITY);
        columnNames.add(ColumnConstants.UNIT_ID);
        columnNames.add(ColumnConstants.UNIT);
        columnNames.add(ColumnConstants.MIX_ID);
		columnNames.add(ColumnConstants.MATE_LINK);
        columnNames.add(ColumnConstants.FINAL_STOCK_NAME);
        editableColumns = new ArrayList<Integer>();
        editableColumns.add(7);
        BeanDefinitionBuilder bulkTableHarvestingDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(CheckBoxIndexColumnTable.class)
                .addPropertyValue("columnNames", columnNames)
                .addPropertyValue("editableColumns", editableColumns)
                .addPropertyValue("checkBoxHeader", new Boolean(false))
                .addPropertyValue("singleSelection", new Boolean(false))
                .setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("bulkTable" + groupPath,
                bulkTableHarvestingDefinitionBuilder.getBeanDefinition());

        // bulk table Panel Bean definition
        List<String> verticalList = new ArrayList<String>();
        verticalList.add("add");
        verticalList.add("delete");
        verticalList.add("moveup");
        verticalList.add("movedown");
        
        horizontalList = new ArrayList<String>();
        horizontalList.add("selection");
        horizontalList.add("search");
        horizontalList.add("gap");
        horizontalList.add("columnSelector");
        horizontalList.add("counter");
        BeanDefinitionBuilder bulkTablePanelHarvestingDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(TableToolBoxPanel.class)
                .addPropertyValue("table",getContext().getBean("bulkTable"+groupPath))
                .addPropertyValue("horizontalList", horizontalList)
                .addPropertyValue("verticalList", verticalList)               
                .setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("bulkTablePanel"
                + groupPath, bulkTablePanelHarvestingDefinitionBuilder.getBeanDefinition());


        // bulk bean definition
        BeanDefinitionBuilder bulkHarvestingDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(Bulk.class)
        		.addPropertyValue("projectID", projectId)
                .addPropertyValue("bulkTablePanel",getContext().getBean("bulkTablePanel"+groupPath))
                .addPropertyValue("fieldGenerated", getContext().getBean("fieldGenerated"+groupPath))
                .setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("bulk"
                + groupPath, bulkHarvestingDefinitionBuilder.getBeanDefinition());

        // sticker table
        columnNames = new ArrayList<String>();
        columnNames.add(ColumnConstants.SELECT);
		columnNames.add(ColumnConstants.ROW_NUM);
		columnNames.add(ColumnConstants.PACKET_ID);
		columnNames.add(ColumnConstants.STOCK_NAME);
		columnNames.add(ColumnConstants.PACKET_NAME);
		columnNames.add(ColumnConstants.ACCESSION);
		columnNames.add(ColumnConstants.PEDIGREE);
		columnNames.add(ColumnConstants.GENERATION);
		columnNames.add(ColumnConstants.MATING_TYPE);
		columnNames.add(ColumnConstants.DATE);			
		columnNames.add(ColumnConstants.PACKET_COUNT);	
		columnNames.add(ColumnConstants.PACKET_NUMBER);			
		columnNames.add(ColumnConstants.QUANTITY);
		columnNames.add(ColumnConstants.UNIT_ID);
		columnNames.add(ColumnConstants.UNIT);
		columnNames.add(ColumnConstants.COMMENT);
		columnNames.add(ColumnConstants.MODIFIED);
		columnNames.add(ColumnConstants.UNIQUE_ID);
        editableColumns = new ArrayList<Integer>();
        editableColumns.add(5);
        editableColumns.add(6);
        editableColumns.add(10);
        editableColumns.add(12);
        editableColumns.add(15);
        BeanDefinitionBuilder stickerTableHarvestingDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(CheckBoxIndexColumnTable.class)
                .addPropertyValue("columnNames", columnNames)
                .addPropertyValue("editableColumns", editableColumns)
                .addPropertyValue("checkBoxHeader", new Boolean(false))
                .addPropertyValue("singleSelection", new Boolean(false))
                .setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("stickerTable" + groupPath,
                stickerTableHarvestingDefinitionBuilder.getBeanDefinition());

        //sticker table panel
        verticalList = new ArrayList<String>();
        verticalList.add("delete");
        
        horizontalList = new ArrayList<String>();
        horizontalList.add("selection");
        horizontalList.add("search");      
        horizontalList.add("gap");
        horizontalList.add("columnSelector");
        horizontalList.add("refresh");
        horizontalList.add("counter");
        BeanDefinitionBuilder stickerTablePanelHarvestingDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(TableToolBoxPanel.class)
                .addPropertyValue("table",getContext().getBean("stickerTable"+groupPath))
                .addPropertyValue("verticalList", verticalList)
                .addPropertyValue("horizontalList", horizontalList)
                .setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("stickerTablePanel"
                + groupPath, stickerTablePanelHarvestingDefinitionBuilder.getBeanDefinition());

        //stickerGenerator beans
        BeanDefinitionBuilder stickerGeneratorHarvestingDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(StickerGenerator.class)
        		.addPropertyValue("projectID", projectId)
                .addPropertyValue("stickerTablePanel", getContext().getBean("stickerTablePanel" + groupPath))
                .addPropertyValue("fieldGenerated",getContext().getBean("fieldGenerated" + groupPath))
                .addPropertyValue("bulk", getContext().getBean("bulk" + groupPath))
                .setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("stickerGenerator"
                + groupPath, stickerGeneratorHarvestingDefinitionBuilder.getBeanDefinition());

        //harvesting bean definition
        BeanDefinitionBuilder harvestingChildPanelHarvestingDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(Harvesting.class)
                .addPropertyValue("fieldGenerated",getContext().getBean("fieldGenerated" + groupPath))
                .addPropertyValue("bulk", getContext().getBean("bulk" + groupPath))
                .addPropertyValue("stickerGenerator",getContext().getBean("stickerGenerator" + groupPath))
                .setInitMethodName("initialize");
        
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("Harvesting - "
                + projectId + groupName, harvestingChildPanelHarvestingDefinitionBuilder.getBeanDefinition());

        Harvesting harvestingPanel = (Harvesting) getContext().getBean("Harvesting - " + projectId + groupName);
        harvestingPanel.setName(groupPath);
        List<TabComponentPanel> componentPanels = new ArrayList<TabComponentPanel>();
        componentPanels.add(harvestingPanel);

        // harvesting tabcomponent bean
        BeanDefinitionBuilder harvestingTabDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(TabComponent.class).addPropertyValue("title", "Harvesting - " + groupName)
                .addPropertyValue("isStatic", false).addPropertyValue("componentPanels", componentPanels)
                .setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("harvestingTab" + projectId + groupName,
                harvestingTabDefinitionBuilder.getBeanDefinition());

        //setup the tab component
        TabComponent tabComponent = (TabComponent) getContext().getBean("harvestingTab" + projectId + groupName);
        TabManager tabManager = AccreteGBBeanFactory.getTabManager();
        tabManager.getTabComponents().add(tabComponent);
        
      
        //parse crossRecordJson into crossRecord panel table
        if(crossRecordJson !=null)
        {
        	List<Object[]> crossRecordRows = parseHarvestGroup.getFieldGeneratedTable(crossRecordJson, groupName);	
        	harvestingPanel.getFieldGenerated().populateTableFromObjects(crossRecordRows);
        	harvestingPanel.getFieldGenerated().nextLink = parseHarvestGroup.mateLink;
        }
        
        //parse bulkJson into bulk panel table
        if(bulkJson !=null)
        {
        	List<Object[]> crossRecordRows = parseHarvestGroup.getBulkTable(bulkJson, groupName);
        	harvestingPanel.getBulk().populateTableFromObjects(crossRecordRows);
        	harvestingPanel.getBulk().nextLink = parseHarvestGroup.mixLink;
        }
        
        //parse stickerJson into stickerGenerator panel table
        if(stickerGeneratorJson !=null)
        {
        	List<Object[]> crossRecordRows = parseHarvestGroup.getStickerTable(stickerGeneratorJson, groupName);
        	harvestingPanel.getStickerGenerator().populateTableFromObjects(crossRecordRows);
        }
        harvestingPanel.fromDB = true;
        //harvestingPanel.updateStockCompositionFromBulk();
        //harvestingPanel.updateStockCompositionFromMate();
        return tabComponent;
    }
    

}
