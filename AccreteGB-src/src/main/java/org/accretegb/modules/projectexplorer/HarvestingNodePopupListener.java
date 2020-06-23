package org.accretegb.modules.projectexplorer;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.germplasm.harvesting.Harvesting;
import org.accretegb.modules.germplasm.planting.Planting;
import org.accretegb.modules.hibernate.dao.HarvestingGroupDAO;
import org.accretegb.modules.hibernate.dao.PMProjectDAO;
import org.accretegb.modules.hibernate.dao.PlantingGroupDAO;
import org.accretegb.modules.projectmanager.ProjectManager;
import org.accretegb.modules.tab.TabComponent;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * 
 * @author Ningjing
 *
 */
public class HarvestingNodePopupListener extends MouseAdapter {

	private JTree projectsTree;
    private ProjectTreeNode treeNode;

    private JPopupMenu harvestingPopupMenu;

	public HarvestingNodePopupListener(JTree projectsTree) {
		this.projectsTree = projectsTree;
        harvestingPopupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem(ProjectConstants.EDIT_GROIP_NAME);
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
							  HarvestingGroupDAO.getInstance().updateGroupName(projectId, oldGroupName, newGroupName);
							  
							  //change beans
							  String oldTabBeanName = "harvestingTab" + projectId + oldGroupName;
							  String newTabBeanName = "harvestingTab" + projectId + newGroupName;
							  ((GenericXmlApplicationContext) getContext()).registerAlias(oldTabBeanName, newTabBeanName);							  
							  TabComponent tabComponent = (TabComponent) getContext().getBean(newTabBeanName);
							  tabComponent.setTitle("Harvesting - " + newGroupName);						        
							  ((JLabel)tabComponent.getComponent(0)).setText("Harvesting - " + newGroupName);
							  
							  String oldPanelBeanName = "Harvesting - " + projectId + oldGroupName;
							  String newPanelBeanName = "Harvesting - " + projectId + newGroupName;
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
        harvestingPopupMenu.add(menuItem);
        
        JMenuItem deleteGroup = new JMenuItem(ProjectConstants.DELETE_GROUP);
        deleteGroup.addActionListener(new DeleteHarvestingGroupActionListener(this));
        harvestingPopupMenu.add(deleteGroup);
	}
	
	 private class DeleteHarvestingGroupActionListener implements ActionListener {

		public DeleteHarvestingGroupActionListener(HarvestingNodePopupListener harvestingNodePopupListener) {
			// TODO Auto-generated constructor stub
		}

		public void actionPerformed(ActionEvent e) {
			List<ProjectTreeNode> parentNodes = new ArrayList<ProjectTreeNode>();
            for (int counter = 0; counter < getProjectsTree().getSelectionCount(); counter++) {
                ProjectTreeNode node = (ProjectTreeNode) getProjectsTree().getSelectionPaths()[counter].getLastPathComponent();
                parentNodes.add(node);
            }
            Harvesting harvesting = (Harvesting)parentNodes.get(0).getTabComponent().getComponentPanels().get(0);
        	CheckBoxIndexColumnTable tagTable = harvesting.getStickerGenerator().getStickerTablePanel().getTable();
        	int packetIdCol = tagTable.getIndexOf(ColumnConstants.PACKET_ID);
        	boolean proceed = true;
            if(tagTable.hasSynced() == true || (tagTable.getRowCount()>0 &&!String.valueOf(tagTable.getValueAt(0, packetIdCol)).equalsIgnoreCase("null"))){
            	int option = JOptionPane.showConfirmDialog(null, "You are deleting a synced group. "
            			+ "Deleting this group only removes data from ProjactManager database. "
            			+ "Are you sure you want to delete this group?", "Warning", JOptionPane.OK_CANCEL_OPTION);
            	if (option != JOptionPane.OK_OPTION) {
            		proceed = false;
            	}
            }
            if (proceed) {
        		String harvestingGroupName = getTreeNode().getNodeName();		
				String projectName = getTreeNode().getParent().getParent().toString();
				int projectId = PMProjectDAO.getInstance().findProjectId(projectName);
				DefaultTreeModel model = (DefaultTreeModel)getProjectsTree().getModel();
				
        		HarvestingGroupDAO.getInstance().delete(projectId, harvestingGroupName);
				TabComponent tabComponent = (TabComponent) getContext().getBean("harvestingTab" + projectId + harvestingGroupName);
     			ProjectManager.removeTab(tabComponent);
     			
     			model.removeNodeFromParent(getTreeNode());
            }
		}
	
		 
	 }
	
	public void mousePressed(MouseEvent e) {
	    maybeShowPopup(e);
	}

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            harvestingPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    public JPopupMenu getharvestingPopupMenu() {
        return harvestingPopupMenu;
    }

    public void setharvestingPopupMenu(JPopupMenu harvestingPopupMenu) {
        this.harvestingPopupMenu = harvestingPopupMenu;
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
