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
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.TextField;
import org.accretegb.modules.germplasm.stocksinfo.StocksInfoPanel;
import org.accretegb.modules.hibernate.dao.PMProjectDAO;
import org.accretegb.modules.tab.TabComponent;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.tab.TabManager;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericXmlApplicationContext;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.constants.TableColumnList;

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
 * acts as a Listener to the Stock Selection Node of Project Tree.
 */

public class StockSelectionParentNodePopupListener extends MouseAdapter {

    private JTree projectsTree;
    private JPopupMenu stockSelectionPopupMenu;

    public JPopupMenu getStockSelectionPopupMenu() {
        return stockSelectionPopupMenu;
    }

    public void setStockSelectionPopupMenu(JPopupMenu stockSelectionPopupMenu) {
        this.stockSelectionPopupMenu = stockSelectionPopupMenu;
    }

    StockSelectionParentNodePopupListener(JTree projectsTree) {
        this.projectsTree = projectsTree;
        stockSelectionPopupMenu = new JPopupMenu();
        JMenuItem menuItem;
        menuItem = new JMenuItem(ProjectConstants.CREATE_NEW_GROUP);
        menuItem.addActionListener(new CreateGroupActionListener(this));
        stockSelectionPopupMenu.add(menuItem);
        JMenu sendToMenu = new JMenu(ProjectConstants.SEND_TO);
        JMenuItem experiments = new JMenuItem("Experiment Design");
        JMenuItem plantings = new JMenuItem("Plantings");
        sendToMenu.add(experiments);
        sendToMenu.add(plantings);
        stockSelectionPopupMenu.add(sendToMenu);
    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void processMenuItems(DefaultMutableTreeNode treeNode) {
        if (treeNode != null) {
            JMenu sendToMenu = (JMenu) getStockSelectionPopupMenu().getComponent(1);
            if (treeNode.getChildCount() == 0) {
                sendToMenu.getItem(0).setEnabled(false);
                sendToMenu.getItem(1).setEnabled(false);
            } else {
                sendToMenu.getItem(0).setEnabled(true);
                sendToMenu.getItem(1).setEnabled(true);
            }
        }
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

    private class CreateGroupActionListener implements ActionListener {
        private StockSelectionParentNodePopupListener listener;

        CreateGroupActionListener(StockSelectionParentNodePopupListener listener) {
            this.setListener(listener);
        }

        public void actionPerformed(ActionEvent arg0) {
            String str;
            do {
                str = JOptionPane.showInputDialog(null, "Enter Group Name:", ProjectConstants.CREATE_NEW_GROUP, 1);
                if (str != null) {
                    if (str.length() < 5) {
                        JOptionPane.showMessageDialog(null, "Group Name must be greater than 5 characters", "", 1);
                        str = null;
                        continue;
                    } else {
                        DefaultTreeModel model = (DefaultTreeModel) getProjectsTree().getModel();
                        ProjectTreeNode stocknode = (ProjectTreeNode) getListener().getProjectsTree()
                                .getSelectionPath().getLastPathComponent();
                        boolean isDuplicate = false;
                        for (int childCount = 0; childCount < stocknode.getChildCount(); childCount++) {
                            ProjectTreeNode childNode = (ProjectTreeNode) stocknode.getChildAt(childCount);
                            String childName = (String) childNode.getUserObject().toString();
                            if (childName.trim().equalsIgnoreCase(str.trim())) {
                                isDuplicate = true;
                                break;
                            }
                        }
                        if (isDuplicate) {
                            JOptionPane.showMessageDialog(null, "Group with the specified Name already exists", "", 1);
                            str = null;
                            continue;
                        }
                        ProjectTreeNode groupNode = new ProjectTreeNode(str);
                        groupNode.setType(ProjectTreeNode.NodeType.STOCK_SELECTION_NODE);
                        List<ProjectTreeNode> parents = new ArrayList<ProjectTreeNode>();
                        parents.add(stocknode);
                        groupNode.setParentNodes(parents);

                        model.insertNodeInto(groupNode, stocknode, stocknode.getChildCount());
                        TreePath treePath = getTreePath(groupNode);
                        getProjectsTree().expandPath(treePath);
                        getProjectsTree().setSelectionPath(treePath);
                        String groupPath = Utils.getPathStr(groupNode.getPath());
                        String projectName = stocknode.getParent().toString();  
                        int projectId = PMProjectDAO.getInstance().findProjectId(projectName);
                        groupNode.setTabComponent(createStockInfoPanel(projectId, groupPath, str));
                    }
                } else {
                    str = "";
                }
            } while (str == null);
        }

        public StockSelectionParentNodePopupListener getListener() {
            return listener;
        }

        public void setListener(StockSelectionParentNodePopupListener listener) {
            this.listener = listener;
        }

        public TabComponent createStockInfoPanel(int projectId, String groupPath, String groupName) {
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
            ((GenericXmlApplicationContext) getContext()).registerBeanDefinition("stockSelectionTab" + projectId + groupName,
                    stocksInfoTabDefinitionBuilder.getBeanDefinition());
            TabComponent tabComponent = (TabComponent) getContext().getBean("stockSelectionTab" + +projectId + groupName);
            TabManager tabManager = (TabManager) getContext().getBean("tabManager");
            tabManager.getTabComponents().add(tabComponent);
            ToolBar toolBar = (ToolBar) getContext().getBean("toolBar");
            toolBar.setTabComponent(tabComponent);
            return tabComponent;
        }

    }

    private class DeleteGroupsActionListener implements ActionListener {
        private StockSelectionParentNodePopupListener listener;

        DeleteGroupsActionListener(StockSelectionParentNodePopupListener listener) {
            this.setListener(listener);
        }

        public StockSelectionParentNodePopupListener getListener() {
            return listener;
        }

        public void setListener(StockSelectionParentNodePopupListener listener) {
            this.listener = listener;
        }

        public void actionPerformed(ActionEvent arg0) {
            // TODO Auto-generated method stub
            DefaultTreeModel model = (DefaultTreeModel) getProjectsTree().getModel();
            DefaultMutableTreeNode stocknode = (DefaultMutableTreeNode) getListener().getProjectsTree()
                    .getSelectionPath().getLastPathComponent();
            ToolBar toolBar = (ToolBar) getContext().getBean("toolBar");
            while (stocknode.getChildCount() != 0) {
                ProjectTreeNode childNode = (ProjectTreeNode) stocknode.getFirstChild();
                toolBar.removeTabComponent(childNode.getTabComponent());
                model.removeNodeFromParent(childNode);
            }
        }
    }
}