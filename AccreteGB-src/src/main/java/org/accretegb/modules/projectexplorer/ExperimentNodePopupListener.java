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
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.PlotIndexColumnTable;
import org.accretegb.modules.customswingcomponent.PlotIndexToolBoxPanel;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.germplasm.experimentaldesign.ExperimentSelectionPanel;
import org.accretegb.modules.germplasm.experimentaldesign.SyncExperimentalDesign;
import org.accretegb.modules.germplasm.phenotype.Phenotype;
import org.accretegb.modules.germplasm.phenotype.PhenotypeExportPanel;
import org.accretegb.modules.germplasm.phenotype.PhenotypeImportPanel;
import org.accretegb.modules.germplasm.planting.*;
import org.accretegb.modules.germplasm.sampling.SampleSelectionPanel;
import org.accretegb.modules.germplasm.sampling.SampleSettingPanel;
import org.accretegb.modules.germplasm.sampling.Sampling;
import org.accretegb.modules.germplasm.stocksinfo.StocksInfoPanel;
import org.accretegb.modules.hibernate.dao.ExperimentGroupDAO;
import org.accretegb.modules.hibernate.dao.PMProjectDAO;
import org.accretegb.modules.hibernate.dao.StockSelectionGroupDAO;
import org.accretegb.modules.projectmanager.ProjectManager;
import org.accretegb.modules.tab.TabComponent;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.tab.TabManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.accretegb.modules.constants.ColumnConstants;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.tree.DefaultTreeModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;
import static org.accretegb.modules.projectexplorer.Utils.getTreePath;

/**
 * @author nkumar & tnj
 * This class defines the action when a user right clicks on
 * any of the experiment modules i.e. children of
 * the experiments node of Project Tree
 */
public class ExperimentNodePopupListener extends MouseAdapter {
    private JTree projectsTree;
    private ProjectTreeNode treeNode;

    private JPopupMenu experimentSelectionPopupMenu;

    ExperimentNodePopupListener(JTree projectsTree) {
        this.projectsTree = projectsTree;
        experimentSelectionPopupMenu = new JPopupMenu();
        JMenuItem menuItem;
        JMenu sendToMenu = new JMenu(ProjectConstants.SEND_TO);
        JMenu plantings = new JMenu(ProjectConstants.PLANTINGS);
        final JMenu addToGroup = new JMenu(ProjectConstants.ADD_TO_GROUP);  
        JMenuItem createNewGroup = new JMenuItem(ProjectConstants.CREATE_NEW_GROUP);
        plantings.add(createNewGroup);
        plantings.add(addToGroup);     
        createNewGroup.addActionListener(new PlantingGroupActionListener(this));
        
        sendToMenu.addMenuListener(new MenuListener(){
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
	                 				plantingPanel.getTagGenerator().getTagsTablePanel().getTable().setHasSynced(false);
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
        sendToMenu.add(plantings);
        experimentSelectionPopupMenu.add(sendToMenu);
        menuItem = new JMenuItem(ProjectConstants.EDIT_GROIP_NAME);
        experimentSelectionPopupMenu.add(menuItem);        
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
							  ExperimentGroupDAO.getInstance().updateGroupName(projectId, oldGroupName, newGroupName);
							  
							  //change beans
							  String oldTabBeanName = "experimentDesignTab" + projectId + oldGroupName;
							  String newTabBeanName = "experimentDesignTab" + projectId + newGroupName;
							  ((GenericXmlApplicationContext) getContext()).registerAlias(oldTabBeanName, newTabBeanName);							  
							  TabComponent tabComponent = (TabComponent) getContext().getBean(newTabBeanName);
							  tabComponent.setTitle("Experiment Design - " + newGroupName);						        
							  ((JLabel)tabComponent.getComponent(0)).setText("Experiment Design - " + newGroupName);
							  
							  String oldPanelBeanName = "Experiment Design - " + projectId + oldGroupName;
							  String newPanelBeanName = "Experiment Design - " + projectId + newGroupName;
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
             
        experimentSelectionPopupMenu.add(menuItem);
    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            experimentSelectionPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
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

    private class OpenGroupActionListener implements ActionListener {

        private ExperimentNodePopupListener listener;

        OpenGroupActionListener(ExperimentNodePopupListener listener) {
            this.setListener(listener);
        }

        public void actionPerformed(ActionEvent arg0) {

        }

        public ExperimentNodePopupListener getListener() {
            return listener;
        }

        public void setListener(ExperimentNodePopupListener listener) {
            this.listener = listener;
        }

    }

    public ProjectTreeNode getExperimentDesignNode(ProjectTreeNode rootNode) {
        int childCount = rootNode.getChildCount();
        for (int counter = 0; counter < childCount; counter++) {
            ProjectTreeNode node = (ProjectTreeNode) rootNode.getChildAt(counter);
            String nodeName = node.getUserObject().toString();
            if (nodeName.equalsIgnoreCase(ProjectConstants.EXPERIMENTS)) {
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

    
    private class PlantingGroupActionListener implements ActionListener {

        private static final String ENTER_GROUP_MESSAGE = "Enter Group Name:";

        private ExperimentNodePopupListener listener;

        PlantingGroupActionListener(ExperimentNodePopupListener listener) {
            this.setListener(listener);
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

        public void actionPerformed(ActionEvent arg0) {
        	final List<ProjectTreeNode> parentNodes = new ArrayList<ProjectTreeNode>();
            for (int counter = 0; counter < getProjectsTree().getSelectionCount(); counter++) {
                ProjectTreeNode node = (ProjectTreeNode) getProjectsTree().getSelectionPaths()[counter]
                        .getLastPathComponent();
                parentNodes.add(node);
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
            	if(node.getType().equals(ProjectTreeNode.NodeType.STOCK_SELECTION_NODE)){
            		boolean hasDuplicate = false;
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
            				}																
            			}
            		} 
            		if(hasDuplicate){
            			int option = JOptionPane.showConfirmDialog(null, "Stocks Selected have duplicates, proceed to next step?", "Warning", JOptionPane.OK_OPTION);
            			if(option != JOptionPane.OK_OPTION){
            				return;
            			} 
            		}  
            	}
            }
            String str;
            do {
                str = JOptionPane.showInputDialog(null, ENTER_GROUP_MESSAGE, "Create New Planting Group", 1);
                if (str != null) {
                    if (str.length() < 5) {
                        JOptionPane.showMessageDialog(null, "Group Name must be greater than 5 characters", "", 1);
                        str = null;
                        continue;
                    } else {
                        final DefaultTreeModel model = (DefaultTreeModel) getProjectsTree().getModel();
                        final ProjectTreeNode plantingnode = getPlantingNode((ProjectTreeNode) getTreeNode().getParent()
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
                        TabComponent plantingTab = createPlantingBeans(stocksList,expid_expPanel,projectId,groupPath, nodeName, parentNodes);
                        groupNode.setTabComponent(plantingTab);
                        ProjectTreeNode plantingNode = groupNode;
                      
                        // create Phenotype Node also with Planting
                        ProjectTreeNode phenotypenode = getPhenotypeNode((ProjectTreeNode) getTreeNode().getParent().getParent());                       
                        String groupName = nodeName;
                        groupNode = new ProjectTreeNode(groupName);
                        groupNode.setType(ProjectTreeNode.NodeType.PHENOTYPE_NODE);
                        groupNode.setParentNodes(parentNodes);
                        model.insertNodeInto(groupNode, phenotypenode, phenotypenode.getChildCount());
                        getProjectsTree().expandPath(getTreePath(groupNode));
                        groupPath = org.accretegb.modules.projectexplorer.Utils.getPathStr(groupNode.getPath());
                        groupNode.setTabComponent(createPhenotypeBeans(projectId, groupPath, groupName, parentNodes));
                        
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
					.addPropertyValue("horizontalList", horizontalList).setInitMethodName("initialize");
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
        
        public ExperimentNodePopupListener getListener() {
            return listener;
        }

        public void setListener(ExperimentNodePopupListener listener) {
            this.listener = listener;
        }

    }
    
    private void createStockList(List<PlantingRow> stocksList, HashMap<Integer,ExperimentSelectionPanel> expid_expPanel, List<ProjectTreeNode> parentNodes){
        for(ProjectTreeNode node : parentNodes){   
     	   if (node.getType().equals(ProjectTreeNode.NodeType.STOCK_SELECTION_NODE)) {
					StocksInfoPanel stockInfoPanel = (StocksInfoPanel) node.getTabComponent().getComponentPanels().get(0);
					CheckBoxIndexColumnTable stocksOutputTable = stockInfoPanel.getSaveTablePanel().getTable();
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
   		       if(!experimentOutputTable.hasSynced()) {
	      			JOptionPane.showMessageDialog(null, "Experimental design needes to be synced with database first");
	      			return;
	      	   }
	      	 
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
    public JPopupMenu getExperimentSelectionPopupMenu() {
        return experimentSelectionPopupMenu;
    }

    public void setExperimentSelectionPopupMenu(JPopupMenu experimentSelectionPopupMenu) {
        this.experimentSelectionPopupMenu = experimentSelectionPopupMenu;
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
}