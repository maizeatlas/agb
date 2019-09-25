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
import org.accretegb.modules.constants.TableColumnList;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.TextField;
import org.accretegb.modules.germplasm.stocksinfo.StocksInfoPanel;
import org.accretegb.modules.hibernate.StockSelectionGroup;
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
 * create StockSelection group panel and populate cart table after parsing json file
 * @author Matthew & Ningjing
 *
 */
public class CreateStockSelectionGroup {
	
	private static ParseStockSelectionGroup parseStockSelectionGroup;
	public CreateStockSelectionGroup(ProjectTree projectTree, int projectId){
		ParseStockSelectionGroup parseStockSelectionGroup = new ParseStockSelectionGroup(projectId);
		this.parseStockSelectionGroup = parseStockSelectionGroup;
		List<StockSelectionGroup> stockSelectionGroups = StockSelectionGroupDAO.getInstance().findByProjectid(projectId);
		if(stockSelectionGroups.size() > 0)
     	{     	
     		for(StockSelectionGroup stockSelectionGroup : stockSelectionGroups){
     			String groupName = stockSelectionGroup.getStockSelectionGroupName();
		        ProjectTreeNode stocknode = projectTree.getStockSelectionNode();
		        stocknode.setParent(projectTree.getProjectRootNode());
		    	ProjectTreeNode groupNode = new ProjectTreeNode(groupName);
		        groupNode.setType(ProjectTreeNode.NodeType.STOCK_SELECTION_NODE);		       
		        List<ProjectTreeNode> parents = new ArrayList<ProjectTreeNode>();
		        parents.add(stocknode);
		        groupNode.setParentNodes(parents);
		        stocknode.insert(groupNode,stocknode.getChildCount());
		        String groupPath = Utils.getPathStr(groupNode.getPath());
		        try{
			        TabComponent stockTab = createStockInfoPanel(projectId, groupPath,groupName,stockSelectionGroup.getCartJson());
			        groupNode.setTabComponent(stockTab);
			    }catch (Exception e){
			    	if(LoggerUtils.isLogEnabled())
     				{
     					LoggerUtils.log(Level.INFO, "error in loading Stock Selection group " + groupName);
     				}
			    	TabComponent stockTab = createStockInfoPanel(projectId, groupPath,groupName,null);
			        groupNode.setTabComponent(stockTab);
			    }
     			
     		}
     	}
        
    }

    public static TabComponent createStockInfoPanel(int projectId, String groupPath, String groupName, String cartJson) {
    	List<String> columnNames  = TableColumnList.STOCK_SELECTION_TABLE_COLUMN_LIST;
		List<String> showColumns = TableColumnList.STOCK_SELECTION_SHOW_COLUMN_LIST;
        BeanDefinitionBuilder searchResultsTableStockInfoDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(CheckBoxIndexColumnTable.class)
                .addPropertyValue("columnNames", columnNames)
                .addPropertyValue("showColumns", showColumns)
                .addPropertyValue("checkBoxHeader", new Boolean(false)).setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("searchResultsTableStockInfo"
                + groupPath, searchResultsTableStockInfoDefinitionBuilder.getBeanDefinition());
        List<String> horizontalList = new ArrayList<String>();
        horizontalList.add("selection");
        horizontalList.add("search");
    	horizontalList.add("gap");
    	horizontalList.add("expand");
		horizontalList.add("columnSelector");
		horizontalList.add("counter");
        BeanDefinitionBuilder searchResultsPanelDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(TableToolBoxPanel.class).addPropertyValue("horizontalList", horizontalList)
                .addPropertyValue("table", getContext().getBean("searchResultsTableStockInfo" + groupPath))
                .setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("searchResultsPanel" + groupPath,
                searchResultsPanelDefinitionBuilder.getBeanDefinition());

        BeanDefinitionBuilder saveTableStockInfoDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(CheckBoxIndexColumnTable.class)
                .addPropertyValue("columnNames", columnNames)
                .addPropertyValue("showColumns", showColumns)
                .addPropertyValue("checkBoxHeader", new Boolean(false)).setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("saveTableStockInfo" + groupPath,
                saveTableStockInfoDefinitionBuilder.getBeanDefinition());
        
        List<String> verticalList = new ArrayList<String>();
        verticalList.add("delete");
        verticalList.add("moveup");
        verticalList.add("movedown");
        
        List<String> horizontalListSaveTable = new ArrayList<String>();
        horizontalListSaveTable.add("selection");
        horizontalListSaveTable.add("checkDuplicates");
        horizontalListSaveTable.add("gap");
        horizontalListSaveTable.add("search");            
        horizontalListSaveTable.add("gap");
        horizontalListSaveTable.add("expand");
        horizontalListSaveTable.add("columnSelector");
        horizontalListSaveTable.add("counter");
        

        BeanDefinitionBuilder saveTableStockInfoPanelDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(TableToolBoxPanel.class)
                .addPropertyValue("verticalList", verticalList)
                .addPropertyValue("horizontalList", horizontalListSaveTable)
                .addPropertyValue("table", getContext().getBean("saveTableStockInfo" + groupPath))
                .setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("saveTableStockInfoPanel"
                + groupPath, saveTableStockInfoPanelDefinitionBuilder.getBeanDefinition());
        BeanDefinitionBuilder stocksInfoPanel0DefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(TabComponentPanel.class).addPropertyValue("panelIndex", new Integer(0))
                .setInitMethodName("initialize");

        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("stocksInfoPanel0" + groupPath,
                stocksInfoPanel0DefinitionBuilder.getBeanDefinition());

        BeanDefinitionBuilder stocksInfoDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(StocksInfoPanel.class)
                .addPropertyValue("projectID", projectId)
                .addPropertyValue("stockname", new JTextField(23))
                .addPropertyValue("accession", new JTextField(23))
                .addPropertyValue("pedigree", new JTextField(23))
                .addPropertyValue("zipcode", new TextField(23))
                .addPropertyValue("personName", new JTextField(23))
                .addPropertyValue("fromDate", new JTextField(23))
                .addPropertyValue("toDate", new JTextField(23))
                .addPropertyValue("generation", new JTextField(23))
                .addPropertyValue("cycle", new JTextField(23))
                .addPropertyValue("classificationCodeComboBox", new JComboBox())
                .addPropertyValue("selectedRows", new ArrayList<String>())
                .addPropertyValue("population", new JTextField(23))
                .addPropertyValue("buttonClear", new JButton("Clear Input"))
                .addPropertyValue("buttonSubmit", new JButton("Search"))
                .addPropertyValue("multiplier", new JTextField(3))
                .addPropertyValue("buttonSelect", new JButton("Add to Cart"))
                .addPropertyValue("buttonSave", new JButton("Export"))
                .addPropertyValue("searchResultsPanel", getContext().getBean("searchResultsPanel" + groupPath))
                .addPropertyValue("saveTablePanel", getContext().getBean("saveTableStockInfoPanel" + groupPath))
                .setParentName("stocksInfoPanel0" + groupPath).setInitMethodName("initialize");
       
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("Stock Selection - " + projectId + groupName,
                stocksInfoDefinitionBuilder.getBeanDefinition());

        StocksInfoPanel stockInfoPanel = (StocksInfoPanel) getContext().getBean(
                "Stock Selection - " + projectId + groupName);
              
        List<TabComponentPanel> componentPanels = new ArrayList<TabComponentPanel>();
        componentPanels.add(stockInfoPanel);
        BeanDefinitionBuilder stocksInfoTabDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(TabComponent.class)
                .addPropertyValue("title", "Stock Selection - " + groupName).addPropertyValue("isStatic", false)
                .addPropertyValue("componentPanels", componentPanels).setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("stockSelectionTab" + +projectId+groupName,
                stocksInfoTabDefinitionBuilder.getBeanDefinition());
        
        TabComponent tabComponent = (TabComponent) getContext().getBean("stockSelectionTab" + +projectId+groupName);
        TabManager tabManager = (TabManager) getContext().getBean("tabManager");
        tabManager.getTabComponents().add(tabComponent);
        
      
        //parse cartJson into cart table
        if(cartJson !=null)
        {
        	List<Object[]> cartRows = parseStockSelectionGroup.getCartTable(cartJson, groupName); 
        	stockInfoPanel.populateTableFromObjects(cartRows);
        	
        }
        
        return tabComponent;
    }
    

}
