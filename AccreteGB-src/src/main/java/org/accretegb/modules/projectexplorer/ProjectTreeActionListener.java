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

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.menu.SaveProjectSubMenuItem;
import org.accretegb.modules.projectexplorer.*;
/**
 * @author nkumar
 * This class acts as an action Listener on the Projects Tree
 * all Node Action Listeners initiate from this class
 */
public class ProjectTreeActionListener extends MouseAdapter {

    private JTree projectsTree;
    private StockSelectionParentNodePopupListener stockSelectionListener;
    private StockSelectionNodePopupListener stockSelectionChildListener;
    private ExperimentParentNodePopupListener experimentListener;
    private ExperimentNodePopupListener experimentChildListener;
    private PlantingNodeParentPopupListener plantingListener;
    private HarvestingNodePopupListener havestingChildListener;
    private PlantingNodePopupListener plantingChildListener;
    

    private HarvestingParentNodePopupListener harvestingListener;
    private StockPackagingParentNodePopupListener stockPackagingListener;
    private SaveProjectSubMenuItem saveProjectItem;
    
    public SaveProjectSubMenuItem getSaveProjectItem(){
        return AccreteGBBeanFactory.getContext().getBean("saveProjectSubMenu", SaveProjectSubMenuItem.class);
    }
  
	 

    public ProjectTreeActionListener(JTree projectsTree) {
        this.setProjectsTree(projectsTree);
        
        setStockSelectionChildListener(new StockSelectionNodePopupListener(projectsTree));
        setExperimentChildListener(new ExperimentNodePopupListener(projectsTree));
        setPlantingChildListener(new PlantingNodePopupListener(projectsTree));
        setHavestingChildListener(new HarvestingNodePopupListener(projectsTree));
       
        stockSelectionListener = new StockSelectionParentNodePopupListener(projectsTree);
        experimentListener = new ExperimentParentNodePopupListener(projectsTree);
        plantingListener = new PlantingNodeParentPopupListener(projectsTree);
        harvestingListener = new HarvestingParentNodePopupListener(projectsTree);
        stockPackagingListener = new StockPackagingParentNodePopupListener(projectsTree);
    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            if (evt.getButton() == MouseEvent.BUTTON3) {
                JTree projectsTree = (JTree) evt.getSource();
                if (projectsTree != null) {
                    TreePath path = projectsTree.getPathForLocation(evt.getX(), evt.getY());
                    if (path == null)
                        return;
                    if(path.getLastPathComponent() instanceof ProjectTreeNode) 
                    {
                    	 ProjectTreeNode selectedNode = ((ProjectTreeNode) path.getLastPathComponent());
                    	 int level = selectedNode.getLevel();
                         String nodeName = (String) selectedNode.getUserObject().toString();
                         System.out.println("level:=>"+level+" nodeName:=>"+nodeName);
                         if (level == 2) {
                             if(nodeName.equals(ProjectConstants.STOCK_SELECTION)) {
                                 getStockSelectionListener().processMenuItems(selectedNode);
                                 getStockSelectionListener().getStockSelectionPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
                             } else if (nodeName.equals(ProjectConstants.EXPERIMENTS)) {
                                 getExperimentListener().getExperimentPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
                             } else if (nodeName.equals(ProjectConstants.PLANTINGS)) {
                                 getPlantingListener().getPlantingPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
                             } else if (nodeName.equals(ProjectConstants.HARVESTINGS)) {
                                 getHarvestingListener().getHarvestingPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
                             } else if (nodeName.equals(ProjectConstants.STOCK_PACKAGING)) {
                                 getStockPackagingListener().getStockPackagingPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
                             }
                         } else if (level == 3) {
                             ProjectTreeNode parentNode = ((ProjectTreeNode) selectedNode.getParent());
                             String parentNodeName = parentNode.getUserObject().toString();
                             System.out.print("Parent Node Name "+parentNodeName);
                             if (parentNodeName.equals(ProjectConstants.STOCK_SELECTION)) {
                                 getStockSelectionChildListener().setTreeNode(selectedNode);
                                 //project name is root node. 
                                 getStockSelectionChildListener().getStockSelectionPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
                             } else if (parentNodeName.equals(ProjectConstants.EXPERIMENTS)) {
                                 getExperimentChildListener().setTreeNode(selectedNode);
                                 getExperimentChildListener().getExperimentSelectionPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
                             } else if (parentNodeName.equals(ProjectConstants.PLANTINGS)) {
                                 getPlantingChildListener().setTreeNode(selectedNode);
                                 //getExperimentChildListener().processMenuItems((ProjectTreeNode) (selectedNode.getParent().getParent()));
                                 //For harvesting menu ? 
                                 getPlantingChildListener().getPlantingSelectionPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
                             }else if (parentNodeName.equals(ProjectConstants.HARVESTINGS)) {
                            	 getHavestingChildListener().setTreeNode(selectedNode);
                            	 getHavestingChildListener().getharvestingPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
                             }
                         }
                    }
                   
                }
            }
        } else if (evt.isControlDown() && evt.getButton() == MouseEvent.BUTTON1) {
            JTree projectsTree = (JTree) evt.getSource();
            if (projectsTree != null) {
                TreePath path = projectsTree.getPathForLocation(evt.getX(), evt.getY());
                if (path == null) {
                    return;
                }
                DefaultMutableTreeNode selectedNode = ((DefaultMutableTreeNode) path.getLastPathComponent());
                if (!isSelectionAllowed(projectsTree, selectedNode)) {
                    projectsTree.removeSelectionPath(path);
                }
            }
        } else if (evt.getButton() == MouseEvent.BUTTON1) {
            // For opening that group
            JTree projectsTree = (JTree) evt.getSource();
            if (projectsTree != null) {
                TreePath path = projectsTree.getPathForLocation(evt.getX(), evt.getY());
                if (path == null)
                    return;
                
                if(  path.getLastPathComponent() instanceof ProjectTreeNode){  
                	
                	 ProjectTreeNode selectedNode = ((ProjectTreeNode) path.getLastPathComponent());
                	 int level = selectedNode.getLevel();
                	 selectedNode.setModified(true);
                	 if(level != 1){
                		 int levelFlag = level;
                		 while(levelFlag >=1)
                		 {	
                			 ProjectTreeNode modifiedNode = selectedNode.getParentNodes().get(0);
                			 modifiedNode.setModified(true);
                			 levelFlag --;
                		 }
                	 }
                    
                     if(level == 1){
                    	 getSaveProjectItem().setEnabled(true); 
                    	 selectedNode.setModified(true);
                    	 
                     }else{
                    	 getSaveProjectItem().setEnabled(false);
                     }
                     if (level == 3) {
                         ToolBar toolBar = AccreteGBBeanFactory.getToolBar();
                         ProjectTreeNode parentNode = (ProjectTreeNode) selectedNode.getParent();
                         if ((parentNode.getUserObject().toString().equals(ProjectConstants.STOCK_SELECTION)
                                 || parentNode.getUserObject().toString().equals(ProjectConstants.EXPERIMENTS)
                                 || parentNode.getUserObject().toString().equals(ProjectConstants.PLANTINGS)
                                 || parentNode.getUserObject().toString().equals(ProjectConstants.HARVESTINGS)
                                 || parentNode.getUserObject().toString().equals(ProjectConstants.PHENOTYPE)
                                 || parentNode.getUserObject().toString().equals(ProjectConstants.SAMPLING))) {
                             toolBar.setTabComponent(selectedNode.getTabComponent());
                         }
                     }
                }            
            }
        }
    }

    private boolean isSelectionAllowed(JTree jtree, DefaultMutableTreeNode selectedNode) {
        if (jtree != null) {
            int count = jtree.getSelectionCount();
            if (count == 0) {
                return true;
            }
            DefaultMutableTreeNode prevSelectedNode = (DefaultMutableTreeNode) jtree.getSelectionPaths()[0].getLastPathComponent();
            String selectedNodeParent = ((ProjectTreeNode) selectedNode.getParent()).getNodeName();
            String prevSelectedNodeParent = ((ProjectTreeNode) prevSelectedNode.getParent()).getNodeName();
            boolean flag = false;
            if ((selectedNodeParent.equals(ProjectConstants.STOCK_SELECTION) && prevSelectedNodeParent.equals(ProjectConstants.EXPERIMENTS))
                    || (selectedNodeParent.equals(ProjectConstants.EXPERIMENTS) && prevSelectedNodeParent.equals(ProjectConstants.STOCK_SELECTION))) {
                flag = true;
            }
            if (selectedNode.getParent() == prevSelectedNode.getParent() || flag) {
                return true;
            }
        }
        return false;
    }

    public StockSelectionParentNodePopupListener getStockSelectionListener() {
        return stockSelectionListener;
    }

    public void setStockSelectionListener(StockSelectionParentNodePopupListener stockSelectionListener) {
        this.stockSelectionListener = stockSelectionListener;
    }

    public StockPackagingParentNodePopupListener getStockPackagingListener() {
        return stockPackagingListener;
    }

    public void setStockPackagingListener(StockPackagingParentNodePopupListener stockPackagingListener) {
        this.stockPackagingListener = stockPackagingListener;
    }

    public ExperimentParentNodePopupListener getExperimentListener() {
        return experimentListener;
    }

    public void setExperimentListener(ExperimentParentNodePopupListener experimentListener) {
        this.experimentListener = experimentListener;
    }

    public PlantingNodeParentPopupListener getPlantingListener() {
        return plantingListener;
    }

    public void setPlantingListener(PlantingNodeParentPopupListener plantingListener) {
        this.plantingListener = plantingListener;
    }

    public HarvestingParentNodePopupListener getHarvestingListener() {
        return harvestingListener;
    }

    public void setHarvestingListener(HarvestingParentNodePopupListener harvestingListener) {
        this.harvestingListener = harvestingListener;
    }

    public JTree getProjectsTree() {
        return projectsTree;
    }

    public void setProjectsTree(JTree projectsTree) {
        this.projectsTree = projectsTree;
    }

    public StockSelectionNodePopupListener getStockSelectionChildListener() {
        return stockSelectionChildListener;
    }

    public void setStockSelectionChildListener(StockSelectionNodePopupListener stockSelectionChildListener) {
        this.stockSelectionChildListener = stockSelectionChildListener;
    }

    public ExperimentNodePopupListener getExperimentChildListener() {
        return experimentChildListener;
    }

    public void setExperimentChildListener(ExperimentNodePopupListener experimentChildListener) {
        this.experimentChildListener = experimentChildListener;
    }

    public PlantingNodePopupListener getPlantingChildListener() {
        return plantingChildListener;
    }

    public void setPlantingChildListener(PlantingNodePopupListener plantingChildListener) {
        this.plantingChildListener = plantingChildListener;
    }
    
    public HarvestingNodePopupListener getHavestingChildListener() {
       
		return havestingChildListener;
    }

    public void setHavestingChildListener(HarvestingNodePopupListener havestingChildListener) {
        this.havestingChildListener = havestingChildListener;
    }

}