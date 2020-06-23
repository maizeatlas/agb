package org.accretegb.modules.projectexplorer;
/*
 * Licensed to Openaccretegb-common under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Openaccretegb-common licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.accretegb.modules.ToolBar;
import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.customswingcomponent.*;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.germplasm.experimentaldesign.AlphaDesignPanel;
import org.accretegb.modules.germplasm.experimentaldesign.CompleteRandomizedDesignPanel;
import org.accretegb.modules.germplasm.experimentaldesign.ExperimentSelectionPanel;
import org.accretegb.modules.germplasm.experimentaldesign.RandomizedCompleteBlockDesignPanel;
import org.accretegb.modules.germplasm.experimentaldesign.SplitDesignPanel;
import org.accretegb.modules.germplasm.planting.*;
import org.accretegb.modules.germplasm.stocksinfo.StocksInfoPanel;
import org.accretegb.modules.hibernate.Experiment;
import org.accretegb.modules.hibernate.dao.ExperimentDAO;
import org.accretegb.modules.hibernate.dao.PMProjectDAO;
import org.accretegb.modules.hibernate.dao.StockSelectionGroupDAO;
import org.accretegb.modules.hibernate.dao.UserDAO;
import org.accretegb.modules.main.LoginScreen;
import org.accretegb.modules.phenotype.Phenotype;
import org.accretegb.modules.phenotype.PhenotypeExportPanel;
import org.accretegb.modules.phenotype.PhenotypeImportPanel;
import org.accretegb.modules.projectmanager.ProjectManager;
import org.accretegb.modules.sampling.SampleSelectionPanel;
import org.accretegb.modules.sampling.SampleSettingPanel;
import org.accretegb.modules.sampling.Sampling;
import org.accretegb.modules.tab.TabComponent;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.tab.TabManager;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.accretegb.modules.constants.ColumnConstants;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;
import static org.accretegb.modules.projectexplorer.Utils.getTreePath;

/**
 * @author nkumar & Ningjing
 * acts as a Listener to the stock selection modules
 * i.e. children of the Stock Selection Node of Project Tree.
 */
public class StockSelectionNodePopupListener extends MouseAdapter {

    private JTree projectsTree;
    private ProjectTreeNode treeNode;

    private JPopupMenu stockSelectionPopupMenu;

    public JPopupMenu getStockSelectionPopupMenu() {
        return stockSelectionPopupMenu;
    }

    public void setStockSelectionPopupMenu(JPopupMenu stockSelectionPopupMenu) {
        this.stockSelectionPopupMenu = stockSelectionPopupMenu;
    }

    StockSelectionNodePopupListener(JTree projectsTree) {
        this.projectsTree = projectsTree;
        stockSelectionPopupMenu = new JPopupMenu();
        JMenuItem menuItem;
        JMenu sendToMenu = new JMenu(ProjectConstants.SEND_TO);
       
        JMenu experiments = new JMenu("Experiment Design");
        JMenuItem createNewGroup = new JMenuItem(ProjectConstants.CREATE_NEW_GROUP);     
        experiments.add(createNewGroup);
        createNewGroup.addActionListener(new ExperimentalGroupActionListener(this));
        
        final JMenu addToGroup = new JMenu(ProjectConstants.ADD_TO_GROUP);  
        JMenu plantings = new JMenu("Plantings");  
        createNewGroup = new JMenuItem(ProjectConstants.CREATE_NEW_GROUP); 
        plantings.add(createNewGroup);
        plantings.add(addToGroup);
        
        addToGroup.addMenuListener(new MenuListener(){
			public void menuSelected(MenuEvent e) {
				addToGroup.removeAll();
				ProjectTreeNode plantingnode = getPlantingNode((ProjectTreeNode) getTreeNode().getParent().getParent());		           
				if(plantingnode!=null){
					for (int counter = 0; counter < plantingnode.getChildCount(); counter++) {
	                    ProjectTreeNode node = (ProjectTreeNode) plantingnode.getChildAt(counter);
	                    final JMenuItem item = new JMenuItem(node.getNodeName());
	                    addToGroup.add(item);
	                    item.addActionListener(new ActionListener(){
	        				public void actionPerformed(ActionEvent e) {
	        					String projectName = getTreeNode().getParent().getParent().toString();
								int projectId = PMProjectDAO.getInstance().findProjectId(projectName);
	        					Planting plantingPanel = (Planting) getContext().getBean("Planting - " + projectId + item.getLabel());        					
	        					List<ProjectTreeNode> parentNodes = new ArrayList<ProjectTreeNode>();
	        			            for (int counter = 0; counter < getProjectsTree().getSelectionCount(); counter++) {
	        			                ProjectTreeNode node = (ProjectTreeNode) getProjectsTree().getSelectionPaths()[counter]
	        			                        .getLastPathComponent();
	        			                parentNodes.add(node);
	        			        }
	        					List<PlantingRow> stocksList = new ArrayList<PlantingRow>();
	                 			HashMap<Integer,ExperimentSelectionPanel> expid_expPanel = new HashMap<Integer,ExperimentSelectionPanel>();
	                 			createStockList(stocksList,expid_expPanel,parentNodes); 
	                 			for(PlantingRow plot : stocksList){
	                 				plantingPanel.getTableView().extraAdded(plot);
	                 				plantingPanel.getTableView().getStockList().add(plot);
	                 			}
	                 			plantingPanel.getTableView().setZipcode(plantingPanel.getFieldSelection().getZipcode());
	                 			if(plantingPanel.getFieldSelection().getZipcode()!= null)
	                 			{
	                 				plantingPanel.getTableView().populateTableFromList(plantingPanel.getTableView().getStockList());
	                 			}
	        				}
	                    });
	                }	 
	            }
			}

			public void menuDeselected(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void menuCanceled(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}
        	
        });
       
        createNewGroup.addActionListener(new PlantingGroupActionListener(this));
        sendToMenu.add(experiments);
        sendToMenu.add(plantings);
        stockSelectionPopupMenu.add(sendToMenu);
        
        menuItem = new JMenuItem(ProjectConstants.EDIT_GROIP_NAME);
        stockSelectionPopupMenu.add(menuItem);
        menuItem.addActionListener(new ActionListener(){			
        	public void actionPerformed(ActionEvent e) {				
				String newGroupName; 
				do {
					   newGroupName = (String)JOptionPane.showInputDialog(null, "Edit Group Name:",
							"", JOptionPane.QUESTION_MESSAGE,null,null,getTreeNode().getNodeName());
					  
					   if (newGroupName != null) {						  
						  
						   if (newGroupName.length() < 5) {
	                        JOptionPane.showMessageDialog(null, "Group Name must be greater than 5 characters", "", 1);
	                        newGroupName = null;
	                        continue;
						  }else {
							  //check for duplicates
							  boolean isDuplicate = false;
							  for(int index = 0 ; index < getTreeNode().getParent().getChildCount(); ++index){
								  ProjectTreeNode node = (ProjectTreeNode) getTreeNode().getParent().getChildAt(index);
								  if(newGroupName.equals(node.getNodeName())){
									  isDuplicate = true;
				                      break;
								  }						  
							  }
							  if (isDuplicate) {
		                            JOptionPane.showMessageDialog(null, "Group with the specified Name already exists", "", 1);
		                            newGroupName = null;
		                            continue;
		                      }
							  
							  String oldGroupName = getTreeNode().getNodeName();		
							  String projectName = getTreeNode().getParent().getParent().toString();
							  int projectId = PMProjectDAO.getInstance().findProjectId(projectName);
							  StockSelectionGroupDAO.getInstance().updateGroupName(projectId, oldGroupName, newGroupName);
							  
							  //change beans
							  String oldTabBeanName = "stockSelectionTab" + projectId + oldGroupName;
							  String newTabBeanName = "stockSelectionTab" + projectId + newGroupName;
							  ((GenericXmlApplicationContext) getContext()).registerAlias(oldTabBeanName, newTabBeanName);							  
							  TabComponent tabComponent = (TabComponent) getContext().getBean(newTabBeanName);
							  tabComponent.setTitle("Stock Selection - " + newGroupName);
							  ((JLabel)tabComponent.getComponent(0)).setText("Stock Selection - " + newGroupName);
							  
							  String oldPanelBeanName =  "Stock Selection - " + projectId + oldGroupName;
							  String newPanelBeanName =  "Stock Selection - " + projectId + newGroupName;
							  ((GenericXmlApplicationContext) getContext()).registerAlias(oldPanelBeanName, newPanelBeanName);							  
							 
							  //change tree view
							  getTreeNode().setNodeName(newGroupName);
							  getTreeNode().setUserObject(newGroupName);
							  DefaultTreeModel model = (DefaultTreeModel)getProjectsTree().getModel();
							  model.nodeChanged(getTreeNode());
						  }
	                    }else {
	                    	newGroupName = "";
	                    }
					}while (newGroupName == null);
				}
	        	
        	});
        menuItem = new JMenuItem(ProjectConstants.DELETE_GROUP);
        menuItem.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				int option = option = JOptionPane.showConfirmDialog(null, "Are you sure to delete the selected group?", "", JOptionPane.OK_OPTION);
				if(option == JOptionPane.OK_OPTION){
					String groupName = getTreeNode().getNodeName();
					String projectName = getTreeNode().getParent().getParent().toString();
					int projectId = PMProjectDAO.getInstance().findProjectId(projectName);
					StockSelectionGroupDAO.getInstance().delete(projectId, groupName);					
					TabComponent tabComponent = (TabComponent) getContext().getBean("stockSelectionTab" +projectId+groupName);
					ProjectManager.removeTab(tabComponent);
					DefaultTreeModel model = (DefaultTreeModel)getProjectsTree().getModel();
					model.removeNodeFromParent(getTreeNode());
				}				
			}
        	
        });
        stockSelectionPopupMenu.add(menuItem);
        
    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            stockSelectionPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public JTree getProjectsTree() {
        return projectsTree;
    }

    public void setProjectsTree(JTree projectsTree) {
        this.projectsTree = projectsTree;
    }

    public ProjectTreeNode getTreeNode() {
        return treeNode;
    }
    

    public void setTreeNode(ProjectTreeNode treeNode) {
        this.treeNode = treeNode;
    }

    private class OpenGroupActionListener implements ActionListener {

        private StockSelectionNodePopupListener listener;

        OpenGroupActionListener(StockSelectionNodePopupListener listener) {
            this.setListener(listener);
        }

        public void actionPerformed(ActionEvent arg0) {

        }

        public StockSelectionNodePopupListener getListener() {
            return listener;
        }

        public void setListener(StockSelectionNodePopupListener listener) {
            this.listener = listener;
        }

    }

    public ProjectTreeNode getExperimentDesignNode(ProjectTreeNode rootNode) {
        int childCount = rootNode.getChildCount();
        for (int counter = 0; counter < childCount; counter++) {
            ProjectTreeNode node = (ProjectTreeNode) rootNode.getChildAt(counter);
            String nodeName = node.getUserObject().toString();
            if (nodeName.equalsIgnoreCase("Experiments")) {
                return node;
            }
        }
        return null;
    }
    
    public ProjectTreeNode getPlantingNode(ProjectTreeNode rootNode) {
        int childCount = rootNode.getChildCount();
        for (int counter = 0; counter < childCount; counter++) {
            ProjectTreeNode node = (ProjectTreeNode) rootNode.getChildAt(counter);
            String nodeName = node.getUserObject().toString();
            if (nodeName.equalsIgnoreCase(ProjectConstants.PLANTINGS)) {
                return node;
            }
        }
        return null;
    }

    private boolean actionsToDuplicate(List<ProjectTreeNode> parentNodes){
    	boolean hasDuplicate = false;    
    	for(ProjectTreeNode node : parentNodes){
    		if(node.getType().equals(ProjectTreeNode.NodeType.STOCK_SELECTION_NODE)){
    			StocksInfoPanel stockInfoPanel = (StocksInfoPanel) node.getTabComponent().getComponentPanels().get(0);
    			CheckBoxIndexColumnTable stocksOutputTable = stockInfoPanel.getSaveTablePanel().getTable(); 
    			if(stocksOutputTable.getIndexOf("Stock Name") >= 0){
    				ArrayList<String> stockList = new ArrayList<String>(); 
    				for(int row = 0; row < stocksOutputTable.getRowCount(); ++row){
    					String stockName = String.valueOf(stocksOutputTable.getValueAt(row, stocksOutputTable.getIndexOf("Stock Name")));					
    					if(!stockList.contains(stockName)){
    						stockList.add(stockName);
    					}else{
    						hasDuplicate = true;
    						return true;
    					}																
    				}

    			}
    		}
    	}
		return hasDuplicate;
    	
    }
    private class ExperimentalGroupActionListener implements ActionListener {

        private StockSelectionNodePopupListener listener;

        ExperimentalGroupActionListener(StockSelectionNodePopupListener listener) {
            this.setListener(listener);
        }

        public ProjectTreeNode getExperimentDesignNode(ProjectTreeNode rootNode) {
            int childCount = rootNode.getChildCount();
            for (int counter = 0; counter < childCount; counter++) {
                ProjectTreeNode node = (ProjectTreeNode) rootNode.getChildAt(counter);
                String nodeName = node.getUserObject().toString();
                if (nodeName.equalsIgnoreCase("Experiments")) {
                    return node;
                }
            }
            return null;
        }

        public void actionPerformed(ActionEvent evt) {
        	List<ProjectTreeNode> parentNodes = new ArrayList<ProjectTreeNode>();
            for (int counter = 0; counter < getProjectsTree().getSelectionCount(); counter++) {
                ProjectTreeNode node = (ProjectTreeNode) getProjectsTree().getSelectionPaths()[counter]
                        .getLastPathComponent();
                parentNodes.add(node);
               
        	} 
            
            boolean hasduplicate = actionsToDuplicate(parentNodes);
            if(hasduplicate){
            	int option = JOptionPane.showConfirmDialog(null, "Stocks Selected have duplicates, proceed to next group?", "Warning", JOptionPane.OK_OPTION);
            	if(option != JOptionPane.OK_OPTION){
            		return;
            	} 
            }                 
          
            String str;
            do { 
        		
                str = JOptionPane.showInputDialog(null, "Enter Group Name:", "Create New Experiment Group", 1);
                if (str != null) {
                    if (str.length() < 5) {
                        JOptionPane.showMessageDialog(null, "Group Name must be greater than 5 characters", "", 1);
                        str = null;
                        continue;
                    } else {
                        DefaultTreeModel model = (DefaultTreeModel) getProjectsTree().getModel();
                        ProjectTreeNode experimentnode = getExperimentDesignNode((ProjectTreeNode) getTreeNode().getParent().getParent());
                        if (experimentnode == null) {
                            return;
                        }
                        // check for duplicates
                        boolean isDuplicate = false;
                        for (int counter = 0; counter < experimentnode.getChildCount(); counter++) {
                            ProjectTreeNode node = (ProjectTreeNode) experimentnode.getChildAt(counter);
                            if (str.trim().equalsIgnoreCase(node.getNodeName())) {
                                isDuplicate = true;
                                break;
                            }
                        }
                        if (isDuplicate) {
                            JOptionPane.showMessageDialog(null, "Group with the specified Name already exists", "", 1);
                            str = null;
                            continue;
                        }
                        String nodeName = str;
                        

                        ProjectTreeNode groupNode = new ProjectTreeNode(nodeName);
                        groupNode.setType(ProjectTreeNode.NodeType.EXPERIMENTAL_DESIGN_NODE);
                        groupNode.setParentNodes(parentNodes);
                        model.insertNodeInto(groupNode, experimentnode, experimentnode.getChildCount());
                        getProjectsTree().expandPath(getTreePath(groupNode));
                        getProjectsTree().setSelectionPath(getTreePath(groupNode));
                        String groupPath = org.accretegb.modules.projectexplorer.Utils.getPathStr(groupNode.getPath());
                        String projectName = groupNode.getParent().getParent().toString();
                        int projectId = PMProjectDAO.getInstance().findProjectId(projectName);
                        groupNode.setTabComponent(createExperimentDesignBeans(projectId, groupPath, nodeName, parentNodes));
                    }
                } else {
                    str = "";
                }
            } while (str == null);

        }
        
        public  Experiment manageExperiment(String exp_name, String exp_originator){
        	Experiment existingExp = null;

    		List<Experiment> expFactList = ExperimentDAO.getInstance().findByNameOriginator(exp_name.toLowerCase(), exp_originator);
    		while(expFactList.size()>0){
    			existingExp = expFactList.get(0);
    			expFactList.remove(0);
    		}
    		if(existingExp == null)
    		{
    			existingExp = ExperimentDAO.getInstance().insert(exp_name, exp_originator);
    		}

        	return existingExp;
        	
        }

        private TabComponent createExperimentDesignBeans(int projectId, String groupPath, String nodeName,List<ProjectTreeNode> parentNodes) {
        	ProjectTreeNode root = (ProjectTreeNode) (ProjectTreeNode) getTreeNode().getParent().getParent();
            String projectName = root.getUserObject().toString();
        	Experiment experiment = manageExperiment(projectName+"_"+nodeName, UserDAO.getInstance().findUserName(LoginScreen.loginUserId));
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
                    .addPropertyValue("experimentId", Integer.toString(experiment.getExperimentId()) )
                    .addPropertyValue("experimentalOutputPanel",getContext().getBean("experimentalOutputPanel" + groupPath))
                    .setInitMethodName("initialize");

            ((GenericXmlApplicationContext) getContext()).registerBeanDefinition(
            		"Experiment Design - " + projectId + nodeName, experimentalDesignDefinitionBuilder.getBeanDefinition());

            ExperimentSelectionPanel experimentSelectionPanel = (ExperimentSelectionPanel) getContext().getBean(
            		"Experiment Design - " + projectId + nodeName);
            experimentSelectionPanel.setName(groupPath);
            List<TabComponentPanel> componentPanels = new ArrayList<TabComponentPanel>();
            componentPanels.add(experimentSelectionPanel);

            BeanDefinitionBuilder experimentSelectionTabDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(TabComponent.class)
                    .addPropertyValue("title", "Experiment Design - " + nodeName).addPropertyValue("isStatic", false)
                    .addPropertyValue("componentPanels", componentPanels).setInitMethodName("initialize");
            ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("experimentDesignTab" + projectId + nodeName,
                    experimentSelectionTabDefinitionBuilder.getBeanDefinition());
            TabComponent tabComponent = (TabComponent) getContext().getBean("experimentDesignTab"  + projectId + nodeName);
            TabManager tabManager = (TabManager) getContext().getBean("tabManager");
            tabManager.getTabComponents().add(tabComponent);
            ToolBar toolBar = (ToolBar) getContext().getBean("toolBar");
            toolBar.setTabComponent(tabComponent);
            CheckBoxIndexColumnTable experimentInputTable = experimentSelectionPanel.getExperimentalSelectedStocksPanel().getTable();
            Utils.removeAllRowsFromTable((DefaultTableModel) experimentInputTable.getModel());
            int tableRowCounter = 0;
            for (ProjectTreeNode node : parentNodes) {
            	Utils.removeAllRowsFromTable( (DefaultTableModel) experimentInputTable.getModel());
                StocksInfoPanel stockInfoPanel = (StocksInfoPanel) node.getTabComponent().getComponentPanels().get(0);
                CheckBoxIndexColumnTable stocksOutputTable = stockInfoPanel.getSaveTablePanel().getTable();
                for (int counter = 0; counter < stocksOutputTable.getRowCount(); counter++) {
                    Object[] row = new Object[9];
                    
                	row[0] = stocksOutputTable.getValueAt(counter, stocksOutputTable.getIndexOf(ColumnConstants.ROW));
                	row[1] = stocksOutputTable.getValueAt(counter, stocksOutputTable.getIndexOf(ColumnConstants.STOCK_ID));
                	row[2] = stocksOutputTable.getValueAt(counter, stocksOutputTable.getIndexOf(ColumnConstants.STOCK_NAME));
                	row[3] = stocksOutputTable.getValueAt(counter, stocksOutputTable.getIndexOf(ColumnConstants.ACCESSION));
                	row[4] = stocksOutputTable.getValueAt(counter, stocksOutputTable.getIndexOf(ColumnConstants.PEDIGREE));
                	row[5] = stocksOutputTable.getValueAt(counter, stocksOutputTable.getIndexOf(ColumnConstants.GENERATION));
                	row[6] = stocksOutputTable.getValueAt(counter, stocksOutputTable.getIndexOf(ColumnConstants.CYCLE));
                	row[7] = stocksOutputTable.getValueAt(counter, stocksOutputTable.getIndexOf(ColumnConstants.CLASSIFICATION_CODE));
                	row[8] = stocksOutputTable.getValueAt(counter, stocksOutputTable.getIndexOf(ColumnConstants.POPULATION));                    	
                
                    String stockName = (String) row[2];
                    if (stockName != null && stockName.trim().length() > 1) {
                        Utils.addRowToTable(row, (DefaultTableModel) experimentInputTable.getModel(), tableRowCounter,true);
                        tableRowCounter++;
                    }
                }
            }
            return tabComponent;
        }

        public StockSelectionNodePopupListener getListener() {
            return listener;
        }

        public void setListener(StockSelectionNodePopupListener listener) {
            this.listener = listener;
        }

    }

    private class PlantingGroupActionListener implements ActionListener {

        private StockSelectionNodePopupListener listener;

        PlantingGroupActionListener(StockSelectionNodePopupListener listener) {
            this.setListener(listener);
        }

        public ProjectTreeNode getPlantingNode(ProjectTreeNode rootNode) {
            int childCount = rootNode.getChildCount();
            for (int counter = 0; counter < childCount; counter++) {
                ProjectTreeNode node = (ProjectTreeNode) rootNode.getChildAt(counter);
                String nodeName = node.getUserObject().toString();
                if (nodeName.equalsIgnoreCase("Plantings")) {
                    return node;
                }
            }
            return null;
        }

        public ProjectTreeNode getPhenotypeNode(ProjectTreeNode rootNode) {
            int childCount = rootNode.getChildCount();
            for (int counter = 0; counter < childCount; counter++) {
                ProjectTreeNode node = (ProjectTreeNode) rootNode.getChildAt(counter);
                String nodeName = node.getUserObject().toString();
                if (nodeName.equalsIgnoreCase("Phenotyping")) {
                    return node;
                }
            }
            return null;
        }
        
        public ProjectTreeNode getSamplingNode(ProjectTreeNode rootNode) {
            int childCount = rootNode.getChildCount();
            for (int counter = 0; counter < childCount; counter++) {
                ProjectTreeNode node = (ProjectTreeNode) rootNode.getChildAt(counter);
                String nodeName = node.getUserObject().toString();
                if (nodeName.equalsIgnoreCase("Sampling")) {
                    return node;
                }
            }
            return null;
        }

        TagGenerator tagGenerator;
        public void actionPerformed(ActionEvent arg0) {
            String str;
            List<ProjectTreeNode> parentNodes = new ArrayList<ProjectTreeNode>();
            for (int counter = 0; counter < getProjectsTree().getSelectionCount(); counter++) {
                ProjectTreeNode node = (ProjectTreeNode) getProjectsTree().getSelectionPaths()[counter]
                        .getLastPathComponent();
                if(node.getType().equals(ProjectTreeNode.NodeType.EXPERIMENTAL_DESIGN_NODE) 
                		|| node.getType().equals(ProjectTreeNode.NodeType.STOCK_SELECTION_NODE)) {
                	 parentNodes.add(node);                	
                }          
                
            }
            boolean hasduplicate = actionsToDuplicate(parentNodes);
            if(hasduplicate){
            	int option = JOptionPane.showConfirmDialog(null, "Stocks Selected have duplicates, proceed to next step?", "Warning", JOptionPane.OK_OPTION);
            	if(option != JOptionPane.OK_OPTION){
            		return;
            	} 
            }   
            if(parentNodes.size() == 0) {
            	JOptionPane.showMessageDialog(null, "No group was selected");
            	return;
            }
            for(ProjectTreeNode node : parentNodes){   
            	if (node.getType().equals(ProjectTreeNode.NodeType.EXPERIMENTAL_DESIGN_NODE)){
            		ExperimentSelectionPanel experimentSelectionPanel = (ExperimentSelectionPanel) node.getTabComponent().getComponentPanels().get(0); 
           		    CheckBoxIndexColumnTable experimentOutputTable = (CheckBoxIndexColumnTable) experimentSelectionPanel
           		    			.getExperimentalOutputPanel().getTable();
     	      		if(!experimentOutputTable.hasSynced()) {
     	      			JOptionPane.showMessageDialog(null, "Experimental design needes to be synced with database first");
     	      			return;
     	      		}
     	      		   
            	}
            }
            
            do {
                str = JOptionPane.showInputDialog(null, "Enter Group Name:", "Create New Planting Group", 1);
                if (str != null) {
                    if (str.length() < 5) {
                        JOptionPane.showMessageDialog(null, "Group Name must be greater than 5 characters", "", 1);
                        str = null;
                        continue;
                    } else {
                        DefaultTreeModel model = (DefaultTreeModel) getProjectsTree().getModel();
                        ProjectTreeNode plantingnode = getPlantingNode((ProjectTreeNode) getTreeNode().getParent()
                                .getParent());
                        if (plantingnode == null) {
                            return;
                        }
                        // check for duplicates
                        boolean isDuplicate = false;
                        for (int counter = 0; counter < plantingnode.getChildCount(); counter++) {
                            ProjectTreeNode node = (ProjectTreeNode) plantingnode.getChildAt(counter);
                            if (str.trim().equalsIgnoreCase(node.getNodeName())) {
                                isDuplicate = true;
                                break;
                            }
                        }
                        if (isDuplicate) {
                            JOptionPane.showMessageDialog(null, "Group with the specified Name already exists", "", 1);
                            str = null;
                            continue;
                        }
                        String nodeName = str;                      
                        List<PlantingRow> stocksList = new ArrayList<PlantingRow>();
            			HashMap<Integer,ExperimentSelectionPanel> expid_expPanel = new HashMap<Integer,ExperimentSelectionPanel>();
            			createStockList(stocksList,expid_expPanel,parentNodes); 
                        ProjectTreeNode groupNode = new ProjectTreeNode(nodeName);
                        groupNode.setType(ProjectTreeNode.NodeType.PLANTING_NODE);
                        groupNode.setParentNodes(parentNodes);
                        model.insertNodeInto(groupNode, plantingnode, plantingnode.getChildCount());
                        getProjectsTree().expandPath(getTreePath(groupNode));
                        getProjectsTree().setSelectionPath(getTreePath(groupNode));
                        String groupPath = org.accretegb.modules.projectexplorer.Utils.getPathStr(groupNode.getPath());
                        String projectName = groupNode.getParent().getParent().toString();
                        int projectId = PMProjectDAO.getInstance().findProjectId(projectName);
                        TabComponent plantingTab =createPlantingBeans(stocksList,expid_expPanel,projectId,groupPath, nodeName, parentNodes);
                                              
                        groupNode.setTabComponent(plantingTab);
                        ProjectTreeNode plantingNode = groupNode;

                        // create Phenotype Node also with Planting
                        ProjectTreeNode phenotypenode = getPhenotypeNode((ProjectTreeNode) getTreeNode().getParent().getParent());                        
                        String phenotypeNodeName = nodeName;
                        groupNode = new ProjectTreeNode(phenotypeNodeName);
                        groupNode.setType(ProjectTreeNode.NodeType.PHENOTYPE_NODE);
                        groupNode.setParentNodes(parentNodes);
                        model.insertNodeInto(groupNode, phenotypenode, phenotypenode.getChildCount());
                        getProjectsTree().expandPath(getTreePath(groupNode));
                        groupPath = org.accretegb.modules.projectexplorer.Utils.getPathStr(groupNode.getPath());
                        groupNode.setTabComponent(createPhenotypeBeans(projectId, groupPath, phenotypeNodeName, parentNodes));
                        ProjectTreeNode phenotypeNode = groupNode;                                           
                        Phenotype phenotype = (Phenotype)(phenotypeNode.getTabComponent().getComponentPanels().get(0));
                        Planting planting = (Planting) (plantingNode.getTabComponent().getComponentPanels().get(0));                       
                        TagGenerator tagGenerator =  planting.getTagGenerator();
                        tagGenerator.setPhenotypeExportPanel(phenotype.getPhenotypeExportPanel());
                        tagGenerator.setPhenotypeNode(phenotypeNode);
                        
                        // create Sampling Node also with Planting
                        ProjectTreeNode samplingnode = getSamplingNode((ProjectTreeNode) getTreeNode().getParent().getParent());                        
                        String samplingNodeName = nodeName;
                        groupNode = new ProjectTreeNode(samplingNodeName);
                        groupNode.setType(ProjectTreeNode.NodeType.SAMPLING_NODE);
                        groupNode.setParentNodes(parentNodes);
                        model.insertNodeInto(groupNode, samplingnode, samplingnode.getChildCount());
                        getProjectsTree().expandPath(getTreePath(groupNode));
                        groupPath = org.accretegb.modules.projectexplorer.Utils.getPathStr(groupNode.getPath());
                        groupNode.setTabComponent(createSamplingBeans(projectId, groupPath, samplingNodeName, parentNodes));
                        ProjectTreeNode samplingNode = groupNode;                                           
                        Sampling sampling = (Sampling)(samplingNode.getTabComponent().getComponentPanels().get(0));
                        tagGenerator.setSampleSelectionPanel(sampling.getSampleSelectionPanel());
                        tagGenerator.setSamplingNode(samplingNode);
                        
                        PhenotypeExportPanel phenotypeExportPanel = phenotype.getPhenotypeExportPanel();
                        SampleSelectionPanel sampleSelectionPanel = sampling.getSampleSelectionPanel();
                        phenotypeExportPanel.setSampleSelectionPanel(sampleSelectionPanel);
                        sampleSelectionPanel.setPhenotypeExportPanel(phenotypeExportPanel);
                        
                        JTabbedPane tabbedPane = AccreteGBBeanFactory.getTabbedPane();
                        int i = tabbedPane.indexOfTabComponent(plantingTab);
                        tabbedPane.setSelectedIndex(i);
                        
                    }
                } else {
                    str = "";
                }
            } while (str == null);
        }      
       
        private TabComponent createPlantingBeans(List<PlantingRow> stocksList, HashMap<Integer,ExperimentSelectionPanel> expid_expPanel, int projectId, String groupPath, String nodeName, List<ProjectTreeNode> parentNodes) {			
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

			BeanDefinitionBuilder tableViewPlantingDefinitionBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(TableView.class)
					.addPropertyValue("stocksOrderPanel", getContext().getBean("plantingStocksOrderPanel" + groupPath))
					.addPropertyValue("expid_expPanel", expid_expPanel)
					.addPropertyValue("projectID", projectId)
					.setInitMethodName("initialize");
			
			
			((GenericXmlApplicationContext) getContext()).registerBeanDefinition("plantingTableView" + groupPath,
					tableViewPlantingDefinitionBuilder.getBeanDefinition());
			
			BeanDefinitionBuilder plantingChildPanel0DefinitionBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(Planting.class)
					.addPropertyValue("fieldSelection", getContext().getBean("plantingFieldSelection" + groupPath))
					.addPropertyValue("tableView", getContext().getBean("plantingTableView" + groupPath))
					.addPropertyValue("mapView", getContext().getBean("plantingMapView" + groupPath))
					.addPropertyValue("tagGenerator", getContext().getBean("plantingTagGenerator" + groupPath))
					.addPropertyValue("listOfStocks", stocksList)
					.setInitMethodName("initialize");
			
			System.out.println("Sending number of stocks " + stocksList.size());
			((GenericXmlApplicationContext) getContext()).registerBeanDefinition("Planting - " + projectId + nodeName,
					plantingChildPanel0DefinitionBuilder.getBeanDefinition());

			// TabComponent
			Planting plantingPanel = (Planting) getContext().getBean("Planting - " + projectId + nodeName);
			plantingPanel.setName(groupPath);
			List<TabComponentPanel> componentPanels = new ArrayList<TabComponentPanel>();
			componentPanels.add(plantingPanel);

			BeanDefinitionBuilder plantingTabDefinitionBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(TabComponent.class).addPropertyValue("title", "Planting - " + nodeName)
					.addPropertyValue("isStatic", false).addPropertyValue("componentPanels", componentPanels)
					.setInitMethodName("initialize");
			((GenericXmlApplicationContext) getContext()).registerBeanDefinition("plantingTab" + projectId + nodeName,
					plantingTabDefinitionBuilder.getBeanDefinition());			
			TabComponent tabComponent = (TabComponent) getContext().getBean("plantingTab" + projectId + nodeName);
			TabManager tabManager = (TabManager) getContext().getBean("tabManager");
			tabManager.getTabComponents().add(tabComponent);
			ToolBar toolBar = (ToolBar) getContext().getBean("toolBar");
			toolBar.setTabComponent(tabComponent);			
			return tabComponent;
		}
        

        private TabComponent createPhenotypeBeans(int projectId, String groupPath, String nodeName,List<ProjectTreeNode> parentNodes) {      	
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
			
			((GenericXmlApplicationContext) getContext()).registerBeanDefinition("Phenotyping - " + projectId + nodeName,
					phenotypeChildPanel0DefinitionBuilder.getBeanDefinition());

	    	// TabComponent
			Phenotype phenotypePanel = (Phenotype) getContext().getBean("Phenotyping - " + projectId + nodeName);
			phenotypePanel.setName(groupPath);
			List<TabComponentPanel> componentPanels = new ArrayList<TabComponentPanel>();
			componentPanels.add(phenotypePanel);

			BeanDefinitionBuilder phenotypeTabDefinitionBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(TabComponent.class).addPropertyValue("title", "Phenotyping - " + nodeName)
					.addPropertyValue("isStatic", false).addPropertyValue("componentPanels", componentPanels)
					.setInitMethodName("initialize");			
			
			((GenericXmlApplicationContext) getContext()).registerBeanDefinition("phenotypeTab" + projectId + nodeName,
					phenotypeTabDefinitionBuilder.getBeanDefinition());			
			TabComponent tabComponent = (TabComponent) getContext().getBean("phenotypeTab" + projectId + nodeName);
			
			TabManager tabManager = (TabManager) getContext().getBean("tabManager");
			tabManager.getTabComponents().add(tabComponent);
			ToolBar toolBar = (ToolBar) getContext().getBean("toolBar");
			toolBar.setTabComponent(tabComponent);          
            return tabComponent;
        }
        
        private TabComponent createSamplingBeans(int projectId, String groupPath, String nodeName,List<ProjectTreeNode> parentNodes) {      	
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

			List<Integer> editableColumns = new ArrayList<Integer>();
			editableColumns.add(columnNames.indexOf(ColumnConstants.SAMPLENAME));
			 
			BeanDefinitionBuilder sampleSettingTableDefinitionBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(CheckBoxIndexColumnTable.class)
					.addPropertyValue("columnNames", columnNames)
					.addPropertyValue("columnNames", columnNames)
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
					.addPropertyValue("sampleSettingTablePanel", getContext().getBean("sampleSettingTablePanel" + groupPath))
					.addPropertyValue("projectID", projectId)
					.setInitMethodName("initialize");
			((GenericXmlApplicationContext) getContext()).registerBeanDefinition("sampleSettingPanel" + groupPath,
					samplingSettingPanelDefinitionBuilder.getBeanDefinition());
			
			BeanDefinitionBuilder samplingChildPanel0DefinitionBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(Sampling.class)
					.addPropertyValue("sampleSelectionPanel", getContext().getBean("sampleSelectionPanel" + groupPath))
					.addPropertyValue("sampleSettingPanel", getContext().getBean("sampleSettingPanel" + groupPath))
					.setInitMethodName("initialize");
			
			((GenericXmlApplicationContext) getContext()).registerBeanDefinition("Sampling - " + projectId + nodeName,
					samplingChildPanel0DefinitionBuilder.getBeanDefinition());

	    	// TabComponent
			Sampling samplingTab = (Sampling) getContext().getBean("Sampling - " + projectId + nodeName);
			samplingTab.setName(groupPath);
			List<TabComponentPanel> componentPanels = new ArrayList<TabComponentPanel>();
			componentPanels.add(samplingTab);

			BeanDefinitionBuilder samplingTabDefinitionBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(TabComponent.class).addPropertyValue("title", "Sampling - " + nodeName)
					.addPropertyValue("isStatic", false).addPropertyValue("componentPanels", componentPanels)
					.setInitMethodName("initialize");			
			
			((GenericXmlApplicationContext) getContext()).registerBeanDefinition("samplingTab" + projectId + nodeName,
					samplingTabDefinitionBuilder.getBeanDefinition());			
			TabComponent tabComponent = (TabComponent) getContext().getBean("samplingTab" + projectId + nodeName);
			
			TabManager tabManager = (TabManager) getContext().getBean("tabManager");
			tabManager.getTabComponents().add(tabComponent);
			ToolBar toolBar = (ToolBar) getContext().getBean("toolBar");
			toolBar.setTabComponent(tabComponent);          
            return tabComponent;
        }
        
        public StockSelectionNodePopupListener getListener() {
            return listener;
        }

        public void setListener(StockSelectionNodePopupListener listener) {
            this.listener = listener;
        }

    }
    
    
    
    public void createStockList(List<PlantingRow> stocksList, HashMap<Integer,ExperimentSelectionPanel> expid_expPanel, List<ProjectTreeNode> parentNodes){
        for(ProjectTreeNode node : parentNodes){  
        	System.out.println("node " + node.getType());
     	   if (node.getType().equals(ProjectTreeNode.NodeType.STOCK_SELECTION_NODE)) {
     		   		System.out.println("From stock selection node");
					StocksInfoPanel stockInfoPanel = (StocksInfoPanel) node.getTabComponent().getComponentPanels().get(0);
					CheckBoxIndexColumnTable stocksOutputTable = stockInfoPanel.getSaveTablePanel().getTable();
					System.out.println("Size " + stocksOutputTable.getRowCount());
					for (int counter = 0; counter < stocksOutputTable.getRowCount(); counter++) {
						PlantingRow stock = new PlantingRow(
								Integer.parseInt(String.valueOf(stocksOutputTable.getValueAt(counter, stocksOutputTable.getColumnModel().getColumnIndex(ColumnConstants.STOCK_ID)))),
								String.valueOf(stocksOutputTable.getValueAt(counter, stocksOutputTable.getColumnModel().getColumnIndex(ColumnConstants.STOCK_NAME))),
								String.valueOf(stocksOutputTable.getValueAt(counter, stocksOutputTable.getColumnModel().getColumnIndex(ColumnConstants.ACCESSION))),
								String.valueOf(stocksOutputTable.getValueAt(counter, stocksOutputTable.getColumnModel().getColumnIndex(ColumnConstants.PEDIGREE))),
								String.valueOf(stocksOutputTable.getValueAt(counter, stocksOutputTable.getColumnModel().getColumnIndex(ColumnConstants.GENERATION))),
								String.valueOf(stocksOutputTable.getValueAt(counter, stocksOutputTable.getColumnModel().getColumnIndex(ColumnConstants.CYCLE))),
								String.valueOf(stocksOutputTable.getValueAt(counter, stocksOutputTable.getColumnModel().getColumnIndex(ColumnConstants.CLASSIFICATION_CODE))),
								String.valueOf(stocksOutputTable.getValueAt(counter, stocksOutputTable.getColumnModel().getColumnIndex(ColumnConstants.POPULATION))));
						stocksList.add(stock);
					}
				}
     	   
     	   if (node.getType().equals(ProjectTreeNode.NodeType.EXPERIMENTAL_DESIGN_NODE)){
     		   ExperimentSelectionPanel experimentSelectionPanel = (ExperimentSelectionPanel) node
						.getTabComponent().getComponentPanels().get(0); 
   		       CheckBoxIndexColumnTable experimentOutputTable = (CheckBoxIndexColumnTable) experimentSelectionPanel
						.getExperimentalOutputPanel().getTable();
	      	 
	      		   for (int counter = 0; counter < experimentOutputTable.getRowCount(); counter++) {    							
						String rep = String.valueOf(experimentOutputTable.getValueAt(counter, experimentOutputTable.getColumnModel().getColumnIndex(ColumnConstants.REPLICATION)));
						Object expFactorValueid = experimentOutputTable.getValueAt(counter, experimentOutputTable.getColumnModel().getColumnIndex(ColumnConstants.EXP_FACTOR_VALUE_IDS));
						while(expFactorValueid.equals(""))
			            {
							expFactorValueid = experimentOutputTable.getValueAt(counter, experimentOutputTable.getColumnModel().getColumnIndex(ColumnConstants.EXP_FACTOR_VALUE_IDS));
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			            }
						String expId = experimentSelectionPanel.getExperimentId();
						PlantingRow stock = new PlantingRow(
								Integer.parseInt(String.valueOf(experimentOutputTable.getValueAt(counter, experimentOutputTable.getColumnModel().getColumnIndex(ColumnConstants.STOCK_ID)))),
								String.valueOf(experimentOutputTable.getValueAt(counter, experimentOutputTable.getColumnModel().getColumnIndex(ColumnConstants.STOCK_NAME))),
								String.valueOf(experimentOutputTable.getValueAt(counter, experimentOutputTable.getColumnModel().getColumnIndex(ColumnConstants.ACCESSION))),
								String.valueOf(experimentOutputTable.getValueAt(counter, experimentOutputTable.getColumnModel().getColumnIndex(ColumnConstants.PEDIGREE))),
								String.valueOf(experimentOutputTable.getValueAt(counter, experimentOutputTable.getColumnModel().getColumnIndex(ColumnConstants.GENERATION))),
								String.valueOf(experimentOutputTable.getValueAt(counter, experimentOutputTable.getColumnModel().getColumnIndex(ColumnConstants.CYCLE))),
								String.valueOf(experimentOutputTable.getValueAt(counter, experimentOutputTable.getColumnModel().getColumnIndex(ColumnConstants.CLASSIFICATION_CODE))),
								String.valueOf(experimentOutputTable.getValueAt(counter, experimentOutputTable.getColumnModel().getColumnIndex(ColumnConstants.POPULATION))));							
						stock.setRep(rep);						
						stock.setExpFactorValueids((String)expFactorValueid);
						stock.setExpId(Integer.parseInt(expId));
						stocksList.add(stock);
						experimentSelectionPanel.progress.setValue((int) ((counter * 1.0 / experimentOutputTable.getRowCount()) * 100));
					}
					experimentSelectionPanel.getRandomizeButton().setEnabled(false);
					expid_expPanel.put(Integer.parseInt(experimentSelectionPanel.getExperimentId()), experimentSelectionPanel);
				
     	   }
         }		
     }

}
