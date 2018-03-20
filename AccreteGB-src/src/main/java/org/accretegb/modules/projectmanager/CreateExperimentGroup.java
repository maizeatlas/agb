package org.accretegb.modules.projectmanager;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.accretegb.modules.ToolBar;
import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.germplasm.experimentaldesign.AlphaDesignPanel;
import org.accretegb.modules.germplasm.experimentaldesign.CompleteRandomizedDesignPanel;
import org.accretegb.modules.germplasm.experimentaldesign.ExperimentSelectionPanel;
import org.accretegb.modules.germplasm.experimentaldesign.RandomizedCompleteBlockDesignPanel;
import org.accretegb.modules.germplasm.experimentaldesign.SplitDesignPanel;
import org.accretegb.modules.hibernate.ExperimentGroup;
import org.accretegb.modules.hibernate.dao.ExperimentGroupDAO;
import org.accretegb.modules.hibernate.dao.SamplingGroupDAO;
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
 * populate experiment groups based on projectId of projects that login user has token on
 * @author Ningjing
 *
 */
public class CreateExperimentGroup {
	
	private static ParseExperimentGroup ParseExperimentGroup;
	
	public CreateExperimentGroup(ProjectTree projectTree, int projectId){
		ParseExperimentGroup ParseExperimentGroup = new ParseExperimentGroup(projectId);
		this.ParseExperimentGroup = ParseExperimentGroup;
		List<ExperimentGroup> ExperimentGroups = ExperimentGroupDAO.getInstance().findByProjectid(projectId);
		if(ExperimentGroups.size() > 0)
     	{     	
     		for(ExperimentGroup ExperimentGroup : ExperimentGroups){
     			String groupName = ExperimentGroup.getExperimentGroupName();     			
     			ProjectTreeNode experimentNode = projectTree.getExperimentNode();
     			experimentNode.setParent(projectTree.getProjectRootNode());
		    	ProjectTreeNode groupNode = new ProjectTreeNode(groupName);
		        groupNode.setType(ProjectTreeNode.NodeType.EXPERIMENTAL_DESIGN_NODE);		       
		        List<ProjectTreeNode> parentNodes = new ArrayList<ProjectTreeNode>();
		        parentNodes.add((ProjectTreeNode) experimentNode.getParent());
		        groupNode.setParentNodes(parentNodes);
		        experimentNode.insert(groupNode,experimentNode.getChildCount());
		        String groupPath = Utils.getPathStr(groupNode.getPath());
		        try{
			        TabComponent expTab = createExperimentPanel(projectId, groupPath,groupName, ExperimentGroup.getStockListJson(),ExperimentGroup.getExpResultJson());
			        groupNode.setTabComponent(expTab);
     			}catch (Exception e){
     				if(LoggerUtils.isLogEnabled())
     				{
     					LoggerUtils.log(Level.INFO, "error in loading experiment group " + groupName);
     					
     				}
     				 TabComponent expTab = createExperimentPanel(projectId, groupPath,groupName, null, null);
 			        groupNode.setTabComponent(expTab);
				}
     		}
     	}
        
    }

	public static TabComponent createExperimentPanel(int projectId, String groupPath, String groupName, String stockListJson, String expResultJson) {
       
		List<String> columnNames = new ArrayList<String>();
        columnNames.add(ColumnConstants.SELECT);
        columnNames.add(ColumnConstants.ROW);
        columnNames.add(ColumnConstants.STOCK_ID);
        columnNames.add(ColumnConstants.STOCK_NAME);
        columnNames.add(ColumnConstants.ACCESSION);
        columnNames.add(ColumnConstants.PEDIGREE);
        columnNames.add(ColumnConstants.GENERATION);
        columnNames.add(ColumnConstants.CYCLE);
        columnNames.add(ColumnConstants.CLASSIFICATION_CODE);
        columnNames.add(ColumnConstants.POPULATION);
        BeanDefinitionBuilder searchResultsTableExperimentDesignDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(CheckBoxIndexColumnTable.class).addPropertyValue("columnNames", columnNames)
                .addPropertyValue("checkBoxHeader", new Boolean(false)).setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition(
                "experimentalDesignSelectedStocksTable" + groupPath,
                searchResultsTableExperimentDesignDefinitionBuilder.getBeanDefinition());
        
        List<String> verticalList = new ArrayList<String>();
        verticalList.add("add");
        verticalList.add("delete");

        BeanDefinitionBuilder experimentalDesignSelectedStocksPanelExperimentDesignDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(TableToolBoxPanel.class)
                .addPropertyValue("verticalList", verticalList)
                .addPropertyValue("table",getContext().getBean("experimentalDesignSelectedStocksTable" + groupPath))
                .setInitMethodName("initialize");
        CheckBoxIndexColumnTable table = (CheckBoxIndexColumnTable)getContext().getBean("experimentalDesignSelectedStocksTable" + groupPath);
        org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable((DefaultTableModel)(table.getModel()));
       
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition(
                "experimentalDesignSelectedStocksPanel" + groupPath,
                experimentalDesignSelectedStocksPanelExperimentDesignDefinitionBuilder.getBeanDefinition());

        columnNames = new ArrayList<String>();
        columnNames.add(ColumnConstants.SELECT);
        columnNames.add(ColumnConstants.PLOT);
        columnNames.add(ColumnConstants.REPLICATION);
    	columnNames.add(ColumnConstants.EXP_ID);
        columnNames.add(ColumnConstants.EXP_FACTOR_VALUE_IDS);
        columnNames.add(ColumnConstants.STOCK_ID);
        columnNames.add(ColumnConstants.STOCK_NAME);
        columnNames.add(ColumnConstants.ACCESSION);
        columnNames.add(ColumnConstants.PEDIGREE);
        columnNames.add(ColumnConstants.GENERATION);
        columnNames.add(ColumnConstants.CYCLE);
        columnNames.add(ColumnConstants.CLASSIFICATION_CODE);
        columnNames.add(ColumnConstants.POPULATION);
        
        List<String> horizontalList = new ArrayList<String>();
        horizontalList.add("selection");
        horizontalList.add("search");
        horizontalList.add("gap");
        horizontalList.add("columnSelector");
        horizontalList.add("refresh");
        horizontalList.add("counter");
        
        verticalList = new ArrayList<String>();
		verticalList.add("delete");
        
        BeanDefinitionBuilder experimentalOutputTableExperimentDesignDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(CheckBoxIndexColumnTable.class).addPropertyValue("columnNames", columnNames)
                .addPropertyValue("checkBoxHeader", new Boolean(false)).setInitMethodName("initialize");
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("experimentalOutputTable"
                + groupPath, experimentalOutputTableExperimentDesignDefinitionBuilder.getBeanDefinition());

        BeanDefinitionBuilder experimentalOutputPanelExperimentalDesignDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(TableToolBoxPanel.class).addPropertyValue("searchvalue", new String())
                .addPropertyValue("searchTextField", new JTextField())
                .addPropertyValue("searchButton", new JButton()).addPropertyValue("filterDonors", new JLabel())
                .addPropertyValue("table", getContext().getBean("experimentalOutputTable" + groupPath))
                .addPropertyValue("horizontalList", horizontalList)
                .addPropertyValue("verticalList", verticalList)
                .setInitMethodName("initialize");
       
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("experimentalOutputPanel"
                + groupPath, experimentalOutputPanelExperimentalDesignDefinitionBuilder.getBeanDefinition());

        Vector<String> methods = new Vector<String>();
        methods.add("Wichmann-Hill");
        methods.add("Marsaglia-Multicarry");
        methods.add("Super-Duper");
        methods.add("Mersenne-Twister");
        methods.add("Knuth-TAOCP");
        methods.add("user-supplied");
        methods.add("Knuth-TAOCP-2002");
        methods.add("default");
        BeanDefinitionBuilder rcbdDesignPanelExperimentalDesignDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(RandomizedCompleteBlockDesignPanel.class)
                .addPropertyValue("reps", new JTextField()).addPropertyValue("seedCount", new JTextField())
                .addPropertyValue("methods", new JComboBox(methods)).setInitMethodName("initialize");

        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("rcbdDesignPanel" + groupPath,
                rcbdDesignPanelExperimentalDesignDefinitionBuilder.getBeanDefinition());

        methods = new Vector<String>();
        methods.add("Wichmann-Hill");
        methods.add("Marsaglia-Multicarry");
        methods.add("Super-Duper");
        methods.add("Mersenne-Twister");
        methods.add("Knuth-TAOCP");
        methods.add("user-supplied");
        methods.add("Knuth-TAOCP-2002");
        methods.add("default");
        BeanDefinitionBuilder crdDesignPanelExperimentalDesignDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(CompleteRandomizedDesignPanel.class)
                .addPropertyValue("reps", new JTextField()).addPropertyValue("seedCount", new JTextField())
                .addPropertyValue("methods", new JComboBox(methods)).setInitMethodName("initialize");

        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("crdDesignPanel" + groupPath,
                crdDesignPanelExperimentalDesignDefinitionBuilder.getBeanDefinition());
        
        methods = new Vector<String>();
        methods.add("Wichmann-Hill");
        methods.add("Marsaglia-Multicarry");
        methods.add("Super-Duper");
        methods.add("Mersenne-Twister");
        methods.add("Knuth-TAOCP");
        methods.add("user-supplied");
        methods.add("Knuth-TAOCP-2002");
        methods.add("default");
        BeanDefinitionBuilder alphaDesignPanelExperimentalDesignDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(AlphaDesignPanel.class)
                .addPropertyValue("reps", new JTextField()).addPropertyValue("blockSize", new JTextField())
                .addPropertyValue("seedCount", new JTextField()).addPropertyValue("methods", new JComboBox(methods)).setInitMethodName("initialize");

        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("alphaDesignPanel" + groupPath,
        		alphaDesignPanelExperimentalDesignDefinitionBuilder.getBeanDefinition());
        
        methods = new Vector<String>();
        methods.add("Wichmann-Hill");
        methods.add("Marsaglia-Multicarry");
        methods.add("Super-Duper");
        methods.add("Mersenne-Twister");
        methods.add("Knuth-TAOCP");
        methods.add("user-supplied");
        methods.add("Knuth-TAOCP-2002");
        methods.add("default");
        BeanDefinitionBuilder splitDesignPanelExperimentalDesignDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(SplitDesignPanel.class)
                .addPropertyValue("reps", new JTextField()).addPropertyValue("seedCount", new JTextField())
                .addPropertyValue("methods", new JComboBox(methods)).setInitMethodName("initialize");

        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("splitDesignPanel" + groupPath,
        		splitDesignPanelExperimentalDesignDefinitionBuilder.getBeanDefinition());

        methods = new Vector<String>();
        methods.add("Randomized Complete Block");
        methods.add("Complete Randomized Block");
        methods.add("Alpha Design");
        methods.add("Split Design");
        
        BeanDefinitionBuilder experimentalDesignDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(ExperimentSelectionPanel.class)
                .addPropertyValue("experimentalSelectedStocksPanel",
                        getContext().getBean("experimentalDesignSelectedStocksPanel" + groupPath))
                .addPropertyValue("designPanel", getContext().getBean("rcbdDesignPanel" + groupPath))
                .addPropertyValue("experimentSelectionBox", new JComboBox(methods))
                .addPropertyValue("experimentalOutputPanel",getContext().getBean("experimentalOutputPanel" + groupPath))
                .setInitMethodName("initialize");

        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition(
                "Experiment Design - " + projectId + groupName, experimentalDesignDefinitionBuilder.getBeanDefinition());

        ExperimentSelectionPanel experimentSelectionPanel = (ExperimentSelectionPanel) getContext().getBean(
                "Experiment Design - " + projectId + groupName);
        
        experimentSelectionPanel.setName(groupPath);
        List<TabComponentPanel> componentPanels = new ArrayList<TabComponentPanel>();
        componentPanels.add(experimentSelectionPanel);

        BeanDefinitionBuilder experimentSelectionTabDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(TabComponent.class)
                .addPropertyValue("title", "Experiment Design - " + groupName)
                .addPropertyValue("isStatic", false)
                .addPropertyValue("componentPanels", componentPanels).setInitMethodName("initialize");
        
        ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("experimentDesignTab" + projectId + groupName,
                experimentSelectionTabDefinitionBuilder.getBeanDefinition());
        
        TabComponent tabComponent = (TabComponent) getContext().getBean("experimentDesignTab" + projectId + groupName);
        TabManager tabManager = (TabManager) getContext().getBean("tabManager");
        tabManager.getTabComponents().add(tabComponent);
        
        //parse stockListJson into stockList table
        if(stockListJson !=null && !stockListJson.equals(""))
        {
        	List<Object[]> stockListRows = ParseExperimentGroup.getStockListTable(stockListJson, groupName);
        	experimentSelectionPanel.populateStockListTableFromObjects(stockListRows);
        	experimentSelectionPanel.setExperimentId(ParseExperimentGroup.experimentId);
        	// parse experiment design settings and results table
        	if(expResultJson != null && !expResultJson.equals("")){
        		List<Object[]> expResultRows = ParseExperimentGroup.getExpResultTable(expResultJson, groupName);
        		int index = 0;
        		for(Component comp : experimentSelectionPanel.getDesignPanel().getComponents()){
        			if(comp instanceof JTextField){
        				((JTextField) comp).setText(ParseExperimentGroup.expDesignSettings.get(index));
        				index ++;
        			}
        			if(comp instanceof JComboBox){
        				((JComboBox) comp).setSelectedIndex(Integer.parseInt(ParseExperimentGroup.expDesignSettings.get(index)));
        			}
        		}
        		experimentSelectionPanel.populateOutputTableFromObjects(expResultRows);  
        		
        	}
        	experimentSelectionPanel.getExpComm().setText(ParseExperimentGroup.experimentComment.split("-")[0]);
        	experimentSelectionPanel.setCurrentComm(ParseExperimentGroup.experimentComment.split("-")[0].trim());
        	experimentSelectionPanel.getReminderMsg().setVisible(Boolean.valueOf(ParseExperimentGroup.experimentComment.split("-")[1]));
        	experimentSelectionPanel.getRandomizeButton().setEnabled(ParseExperimentGroup.randomizeButtonStatus);
        	}        
        return tabComponent;
    }

}
