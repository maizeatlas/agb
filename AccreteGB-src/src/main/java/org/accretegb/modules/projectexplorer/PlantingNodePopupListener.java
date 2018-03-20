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
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.germplasm.harvesting.Bulk;
import org.accretegb.modules.germplasm.harvesting.FieldGenerated;
import org.accretegb.modules.germplasm.harvesting.Harvesting;
import org.accretegb.modules.germplasm.harvesting.StickerGenerator;
import org.accretegb.modules.germplasm.planting.Planting;
import org.accretegb.modules.hibernate.dao.PhenotypeGroupDAO;
import org.accretegb.modules.hibernate.dao.PlantingGroupDAO;
import org.accretegb.modules.hibernate.dao.PMProjectDAO;
import org.accretegb.modules.hibernate.dao.StockSelectionGroupDAO;
import org.accretegb.modules.projectmanager.ProjectManager;
import org.accretegb.modules.tab.TabComponent;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.tab.TabManager;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.accretegb.modules.constants.ColumnConstants;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;
import static org.accretegb.modules.projectexplorer.Utils.getTreePath;

/**
 * @author nkumar
 * This class acts as an action listener
 * for the plantings modules i.e. children of the
 * Plantings Node of Project Tree
 */
public class PlantingNodePopupListener extends MouseAdapter {
    private JTree projectsTree;
    private ProjectTreeNode treeNode;
    private JPopupMenu plantingSelectionPopupMenu;

    public PlantingNodePopupListener(JTree projectsTree) {
        this.projectsTree = projectsTree;
        plantingSelectionPopupMenu = new JPopupMenu();
        JMenuItem menuItem;
        JMenu sendToMenu = new JMenu(ProjectConstants.SEND_TO);
        JMenu harvestings = new JMenu(ProjectConstants.HARVESTINGS);
        JMenuItem createNewGroup = new JMenuItem(ProjectConstants.CREATE_NEW_GROUP);
        harvestings.add(createNewGroup);
        createNewGroup.addActionListener(new CreateHarvestingGroupActionListener(this));
        sendToMenu.add(harvestings);
        plantingSelectionPopupMenu.add(sendToMenu);
        menuItem = new JMenuItem(ProjectConstants.EDIT_GROIP_NAME);
        plantingSelectionPopupMenu.add(menuItem);
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
							  PlantingGroupDAO.getInstance().updateGroupName(projectId, oldGroupName, newGroupName);
							  
							  //change planting beans
							  String oldTabBeanName = "plantingTab" + projectId + oldGroupName;
							  String newTabBeanName = "plantingTab" + projectId + newGroupName;
							  ((GenericXmlApplicationContext) getContext()).registerAlias(oldTabBeanName, newTabBeanName);							  
							  TabComponent tabComponent = (TabComponent) getContext().getBean(newTabBeanName);
							  tabComponent.setTitle("Planting - " + newGroupName);						        
							  ((JLabel)tabComponent.getComponent(0)).setText("Planting - " + newGroupName);
							  String oldPanelBeanName = "Planting - " + projectId + oldGroupName;
							  String newPanelBeanName = "Planting - " + projectId + newGroupName;
							  ((GenericXmlApplicationContext) getContext()).registerAlias(oldPanelBeanName, newPanelBeanName);							  
							  
							  //change tree view
							  getTreeNode().setNodeName(newGroupName);
							  getTreeNode().setUserObject(newGroupName);
							  DefaultTreeModel model = (DefaultTreeModel)getProjectsTree().getModel();
							  model.nodeChanged(getTreeNode());
							  
							  
							  oldGroupName = "phenotype_" + oldGroupName;
							  newGroupName = "phenotype_" + newGroupName;	
							  PhenotypeGroupDAO.getInstance().updateGroupName(projectId, oldGroupName, newGroupName);
							  
							  //change phenotype beans
							  oldTabBeanName = "phenotypeTab" + projectId + oldGroupName;
							  newTabBeanName = "phenotypeTab" + projectId + newGroupName;
							  ((GenericXmlApplicationContext) getContext()).registerAlias(oldTabBeanName, newTabBeanName);							  
							  tabComponent = (TabComponent) getContext().getBean(newTabBeanName);
							  tabComponent.setTitle("Phenotype - " + newGroupName);						        
							  ((JLabel)tabComponent.getComponent(0)).setText("Phenotype - " + newGroupName);
							  oldPanelBeanName = "Phenotype - " + projectId + oldGroupName;
							  newPanelBeanName = "Phenotype - " + projectId + newGroupName;
							  ((GenericXmlApplicationContext) getContext()).registerAlias(oldPanelBeanName, newPanelBeanName);							  
							 
							 
							  //change tree view				  
							  PhenotypeGroupDAO.getInstance().updateGroupName(projectId, oldGroupName, newGroupName);
							  ProjectTreeNode plantingNode = (ProjectTreeNode) getTreeNode().getParent();
							  ProjectTreeNode phenotypeNode = (ProjectTreeNode) plantingNode.getNextSibling();
							  for(int index = 0; index < phenotypeNode.getChildCount(); ++ index){
									ProjectTreeNode child = (ProjectTreeNode) phenotypeNode.getChildAt(index);
									if(child.getNodeName().equals(oldGroupName)){
										child.setNodeName(newGroupName);
									    child.setUserObject(newGroupName);
									    model.nodeChanged(child);
									}
							  }					  
						  }
	                    }else {
	                    	newGroupName = "";
	                    }
				  }while (newGroupName == null);
			}
        	
        });
        /*menuItem = new JMenuItem(ProjectConstants.EXPORT_GROUP);
        plantingSelectionPopupMenu.add(menuItem);
        menuItem = new JMenuItem("Delete Group");
        menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int option = option = JOptionPane.showConfirmDialog(null, "Are you sure to delete the selected planting group and corresponding phenotype group?", "", JOptionPane.OK_OPTION);
				if(option == JOptionPane.OK_OPTION){
					String groupName = getTreeNode().getNodeName();
					String projectName = getTreeNode().getParent().getParent().toString();
					ProjectTreeNode stockNode = (ProjectTreeNode) getTreeNode().getParent();
					ProjectTreeNode phenotypeNode = (ProjectTreeNode) stockNode.getNextSibling();
					int projectId = ProjectDAO.getInstance().findProjectId(projectName);
					PlantingGroupDAO.getInstance().delete(projectId, groupName);					
					TabComponent tabComponent = (TabComponent) getContext().getBean("plantingTab" +projectId+groupName);
					ProjectManager.removeTab(tabComponent);
					DefaultTreeModel model = (DefaultTreeModel)getProjectsTree().getModel();
					model.removeNodeFromParent(getTreeNode());
					
					groupName = "phenotype_"+groupName;
					PhenotypeGroupDAO.getInstance().delete(projectId, groupName);
					tabComponent = (TabComponent) getContext().getBean("phenotypeTab" +projectId+groupName);
					ProjectManager.removeTab(tabComponent);
					for(int index = 0; index < phenotypeNode.getChildCount(); ++ index){
						ProjectTreeNode child = (ProjectTreeNode) phenotypeNode.getChildAt(index);
						if(child.getNodeName().equals(groupName)){
							model.removeNodeFromParent(child);
						}
					}					
				}				
			}       	
        });
        plantingSelectionPopupMenu.add(menuItem);*/
    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            plantingSelectionPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private class OpenGroupActionListener implements ActionListener  {

        private PlantingNodePopupListener plantingNodePopupListener;
        public OpenGroupActionListener(PlantingNodePopupListener plantingNodePopupListener) {
            this.plantingNodePopupListener = plantingNodePopupListener;
        }

        public void actionPerformed(ActionEvent e) {

        }
    }

    private class CreateHarvestingGroupActionListener implements ActionListener {

        private static final String ENTER_GROUP_MESSAGE = "Enter Group Name:";

        private PlantingNodePopupListener plantingNodePopupListener;
        public CreateHarvestingGroupActionListener(PlantingNodePopupListener plantingNodePopupListener) {
            this.plantingNodePopupListener = plantingNodePopupListener;
        }

        public ProjectTreeNode getHarvestingNode(ProjectTreeNode rootNode) {
            int childCount = rootNode.getChildCount();
            for (int counter = 0; counter < childCount; counter++) {
                ProjectTreeNode node = (ProjectTreeNode) rootNode.getChildAt(counter);
                String nodeName = node.getUserObject().toString();
                if (nodeName.equalsIgnoreCase(ProjectConstants.HARVESTINGS)) {
                    return node;
                }
            }
            return null;
        }

        public void actionPerformed(ActionEvent e) {
        	List<ProjectTreeNode> parentNodes = new ArrayList<ProjectTreeNode>();
            for (int counter = 0; counter < getProjectsTree().getSelectionCount(); counter++) {
                ProjectTreeNode node = (ProjectTreeNode) getProjectsTree().getSelectionPaths()[counter].getLastPathComponent();
                parentNodes.add(node);
            }
        	Planting planting = (Planting)parentNodes.get(0).getTabComponent().getComponentPanels().get(0);
            String str;
            if(planting.getTagGenerator().getTagsTablePanel().getTable().hasSynced() == false){
            	JOptionPane.showMessageDialog(null, "Please sync Tag Generator data with databse first.", "", 1);               
            }else{
	            do {
	                str = JOptionPane.showInputDialog(null, ENTER_GROUP_MESSAGE, "Create New Harvesting Group", 1);
	                if (str != null) {
	                    if (str.length() < 5) {
	                        JOptionPane.showMessageDialog(null, "Group Name must be greater than 5 characters", "", 1);
	                        str = null;
	                        continue;
	                    } else {
	                        DefaultTreeModel model = (DefaultTreeModel) getProjectsTree().getModel();
	                        ProjectTreeNode harvestingNode = getHarvestingNode((ProjectTreeNode) getTreeNode().getParent().getParent());
	                        if (harvestingNode == null) {
	                            return;
	                        }
	                        // check for duplicates
	                        boolean isDuplicate = false;
	                        for (int counter = 0; counter < harvestingNode.getChildCount(); counter++) {
	                            ProjectTreeNode node = (ProjectTreeNode) harvestingNode.getChildAt(counter);
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
	                        groupNode.setType(ProjectTreeNode.NodeType.HARVESTING_NODE);
	                        groupNode.setParentNodes(parentNodes);
	                        model.insertNodeInto(groupNode, harvestingNode, harvestingNode.getChildCount());
	                        getProjectsTree().expandPath(getTreePath(groupNode));
	                        getProjectsTree().setSelectionPath(getTreePath(groupNode));
	                        String groupPath = org.accretegb.modules.projectexplorer.Utils.getPathStr(groupNode.getPath());
	                        String projectName = groupNode.getParent().getParent().toString();
	                        int projectId = PMProjectDAO.getInstance().findProjectId(projectName);
	                        groupNode.setTabComponent(createHarvestingBeans(projectId,groupPath, nodeName, parentNodes));
	                    }
	                } else {
	                    str = "";
	                }
	            } while (str == null);
            }
        }

        private TabComponent createHarvestingBeans(int projectId, String groupPath, String nodeName, List<ProjectTreeNode> parentNodes) {

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
            
            BeanDefinitionBuilder crossingTableHarvestingDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
            		CheckBoxIndexColumnTable.class)
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
            Planting planting = (Planting)parentNodes.get(0).getTabComponent().getComponentPanels().get(0);
            BeanDefinitionBuilder fieldGeneratedHarvestingDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(FieldGenerated.class)
                    .addPropertyValue("crossingTablePanel",getContext().getBean("crossingTablePanel"+groupPath))
                    .addPropertyValue("stockList", planting.getTagGenerator().allStocks())
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
                    + projectId + nodeName, harvestingChildPanelHarvestingDefinitionBuilder.getBeanDefinition());

            Harvesting harvestingPanel = (Harvesting) getContext().getBean("Harvesting - " + projectId + nodeName);
            harvestingPanel.setName(groupPath);
            List<TabComponentPanel> componentPanels = new ArrayList<TabComponentPanel>();
            componentPanels.add(harvestingPanel);

            // harvesting tabcomponent bean
            BeanDefinitionBuilder harvestingTabDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(TabComponent.class).addPropertyValue("title", "Harvesting - " + nodeName)
                    .addPropertyValue("isStatic", false).addPropertyValue("componentPanels", componentPanels)
                    .setInitMethodName("initialize");
            ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("harvestingTab" +  projectId + nodeName,
                    harvestingTabDefinitionBuilder.getBeanDefinition());

            //setup the tab component
            TabComponent tabComponent = (TabComponent) getContext().getBean("harvestingTab" +  projectId + nodeName);
            TabManager tabManager = AccreteGBBeanFactory.getTabManager();
            tabManager.getTabComponents().add(tabComponent);
            ToolBar toolBar = AccreteGBBeanFactory.getToolBar();
            toolBar.setTabComponent(tabComponent);
            return tabComponent;
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


    public JPopupMenu getPlantingSelectionPopupMenu() {
        return plantingSelectionPopupMenu;
    }

    public void setPlantingSelectionPopupMenu(JPopupMenu plantingSelectionPopupMenu) {
        this.plantingSelectionPopupMenu = plantingSelectionPopupMenu;
    }

}