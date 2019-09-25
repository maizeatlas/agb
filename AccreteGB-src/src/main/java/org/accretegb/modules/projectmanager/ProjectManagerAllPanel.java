package org.accretegb.modules.projectmanager;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.constants.ProjectManagerErrorConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.hibernate.PMProject;
import org.accretegb.modules.hibernate.Project;
import org.accretegb.modules.hibernate.TokenRelation;
import org.accretegb.modules.hibernate.User;
import org.accretegb.modules.hibernate.dao.CollaborateRelationDAO;
import org.accretegb.modules.hibernate.dao.PMProjectDAO;
import org.accretegb.modules.hibernate.dao.TokenRelationDAO;
import org.accretegb.modules.hibernate.dao.UserDAO;
import org.accretegb.modules.main.LoginScreen;
import org.accretegb.modules.main.SendEmail;
import org.accretegb.modules.projectexplorer.ProjectExplorerPanel;
import org.accretegb.modules.projectexplorer.ProjectExplorerTabbedPane;
import org.accretegb.modules.projectexplorer.ProjectTree;
import org.accretegb.modules.util.ChangeMonitor;

import net.miginfocom.swing.MigLayout;


/**
 * project manager panel, which shows project information related to login user
 * @author Ningjing
 *
 */
public class ProjectManagerAllPanel extends JPanel {

	private CheckBoxIndexColumnTable allTable;
	private static TableToolBoxPanel allTablePanel;
	private JButton returnButton;
	private JButton checkoutButton;
	private JButton editCollaboratorButton;
	private JButton requestTokenButton;
	private static String userName;
	private static int userId;
	public void initialize() {
		setLayout(new MigLayout("insets 10, gap 10"));
		add(buttonsPanel(), "w 100%, h 10%, wrap");
		add(getAllTablePanel(), "w 100%, h 90%");
		userId = LoginScreen.loginUserId;		
		userName = UserDAO.getInstance().findUserName(userId);
		//retrieve collaborated projects of the login user and update tokens. 
		List<Integer> projectIds = CollaborateRelationDAO.getInstance().findByUserId(userId);
		TokenRelationDAO.getInstance().deleteOutdatedTokens(projectIds);		
		getAllTablePanel().getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		populateTable();
	}


	public JPanel buttonsPanel() {
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new MigLayout("insets 0, gap 0"));

		setReturnButton();
		setCheckoutButton();		
		setEditCollaboratorsButton();
		setRequestTokenButton();

		buttonsPanel.add(editCollaboratorButton,"gapleft 5" );
		buttonsPanel.add(checkoutButton);
		buttonsPanel.add(returnButton);
		buttonsPanel.add(requestTokenButton);
		addDeleteButtonListener();
		addRefreshButtonListener();
		return buttonsPanel;
	}
	
	
	private boolean validateSelectedTables(){
		if (getAllTablePanel().getTable().getSelectedRows().length == 0) {
			ProjectManagerErrorConstants.promptNoProjectSelectedError();
			return false;
		}
		return true;
	}

    /**
     * Panel for removing or adding collaborators
     * @param projectName
     */
	private void getEidtCollaboratePanel(String projectName){
		int projectId = PMProjectDAO.getInstance().findProjectId(projectName);
		JPanel panel = new JPanel(new MigLayout("insets 0 0 0 0"));	
		panel.setPreferredSize(new Dimension(400, 300));
		final JList nonCollaborators = new JList();
		
		JScrollPane scrollPaneOption = new JScrollPane();
		scrollPaneOption.add(nonCollaborators);		
		scrollPaneOption.setViewportView(nonCollaborators);		
		scrollPaneOption.setOpaque(false);
		scrollPaneOption.setBorder(BorderFactory.createTitledBorder("Non Collaborators"));
		final DefaultListModel listModelNonExist  = new DefaultListModel();
		nonCollaborators.setModel(listModelNonExist);
		
		final JList collaborators = new JList();
		JScrollPane scrollPaneSelect = new JScrollPane();
		scrollPaneSelect.add(collaborators);
		scrollPaneSelect.setOpaque(false);
		scrollPaneSelect.setViewportView(collaborators);		
		scrollPaneSelect.setBorder(BorderFactory.createTitledBorder("Collaborators"));			
		final DefaultListModel listModelExist  = new DefaultListModel();
		collaborators.setModel(listModelExist);
		
		//get all users
		ArrayList<Integer> userIds = UserDAO.getInstance().findAllUserIds();
		//get all exiting collaborators
		ArrayList<Integer> collaboratorIds = CollaborateRelationDAO.getInstance().findByProjectId(projectId);		
		ArrayList<Integer> nonCollaboratorIds = new ArrayList<Integer>();
		

		for (Integer userId : userIds) {
			if (!collaboratorIds.contains(userId)) {
				listModelNonExist.addElement(UserDAO.getInstance().findUserName(userId));
				nonCollaboratorIds.add(userId);
			}
		}
		for(Integer userId : collaboratorIds){
			listModelExist.addElement(UserDAO.getInstance().findUserName(userId));
		}
					
		
	    JButton add = new JButton("+");
	    add.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				List selected = nonCollaborators.getSelectedValuesList();					
				for(Object item : selected){
				   listModelExist.addElement(item);
				   listModelNonExist.removeElement(item);
				}			
				nonCollaborators.clearSelection();
			}
	    	
	    });
	    
	    JButton remove = new JButton("-");
	    remove.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Object[] selected = collaborators.getSelectedValues(); 
				for(Object item : selected){
					 if(item.toString().equals(userName))
					 {
						 ProjectManagerErrorConstants.promptCannotRemoveSelf();
					 }else{
						 listModelNonExist.addElement(item);
					     listModelExist.removeElement(item);
					 }
					 
				}
			}
	    	
	    });
		panel.add(scrollPaneOption,"w 50%, h 100%, west");
		panel.add(add,   "cell 1 0,  al center, gaptop 120, hmax 20, wmax 30");
		panel.add(remove,"cell 1 1,  al center, gapbottom 100, hmax 20, wmax 30");
		panel.add(scrollPaneSelect,"gapleft 5, w 50%, h 100%, east");
		
	
		int res = JOptionPane.showConfirmDialog(null, 
				 panel, 
				"Edit " + projectName + "'s Collaborators", 
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		// User hit OK
		if (res == JOptionPane.OK_OPTION) 
		{
			Object[] editedNonCollaborators = listModelNonExist.toArray();
			for(Object item : editedNonCollaborators){
				int userId = UserDAO.getInstance().findUserId((String)item);
				if(!nonCollaboratorIds.contains(userId)){
					CollaborateRelationDAO.getInstance().delete(projectId, userId);
				}
			}
			Object[] editedCollaborators = listModelExist.toArray();
			for(Object item : editedCollaborators){
				int userId = UserDAO.getInstance().findUserId((String)item);
				if(!collaboratorIds.contains(userId)){
					CollaborateRelationDAO.getInstance().insert(projectId, userId);
				}
			}
		}
		
			
	}
	
	/**
	 * Panels for reviewing collaborators
	 * Only project owners can edit collaborators
	 * @param projectName
	 */
	private void getViewCollaboratePanel(String projectName){
		int projectId = PMProjectDAO.getInstance().findProjectId(projectName);
		JPanel panel = new JPanel(new MigLayout("insets 0 0 0 0"));	
		panel.setPreferredSize(new Dimension(300, 200));
		JTextArea collaborators = new JTextArea();
		//get all exiting collaborators		
		ArrayList<Integer> collaboratorIds = CollaborateRelationDAO.getInstance().findByProjectId(projectId);		
		StringBuilder collaboratorsText = new StringBuilder();
		for(int userId : collaboratorIds){
			collaboratorsText.append(UserDAO.getInstance().findUserName(userId));
			collaboratorsText.append("\n");			
		}
		collaborators.setText(collaboratorsText.toString());
		panel.add(collaborators,"w 100%, h 100%");
		JOptionPane.showConfirmDialog(null, 
				 panel, 
				"View " + projectName + "'s Collaborators", 
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
			
	}
	
	private void setEditCollaboratorsButton(){
		editCollaboratorButton = new JButton("Edit/View Collaborators");
		editCollaboratorButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(!validateSelectedTables()) return;		
				for (int row : getAllTablePanel().getTable().getSelectedRows()) {
					String projectName = getProjectName(row);
					String projectOwner = getProjectOwner(row);
					//only project owner can add or remove collaborators
					if(!projectOwner.equals(userName)){
						ProjectManagerErrorConstants.promptNotProjectOwner();
						getViewCollaboratePanel(projectName);
					 }else{
						getEidtCollaboratePanel(projectName);
					}	
				 }
				}
			});
		
	}
	
	/**
	 * Login user return token 
	 * TODO: remove project from panel ? 
	 */
	private void setReturnButton() {
		returnButton = new JButton("Return");
		returnButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!validateSelectedTables()) return;
				
				for (int row : getAllTablePanel().getTable().getSelectedRows()) {
					String token = getProjectToken(row);
					String projectName = getProjectName(row);
					int projectId = PMProjectDAO.getInstance().findProjectId(projectName);						
					if (token != null && token.equals(userName)) {	
						if(CollaborateRelationDAO.getInstance().isCollaborator(projectId,userId)){
							if (ChangeMonitor.changedProject.get(projectId)) {
								int option = JOptionPane.showConfirmDialog(null, "Changes to this project have been detected. Click Yes to save them or No to discard them", "", JOptionPane.OK_OPTION);	
								if(option ==JOptionPane.OK_OPTION )
								{
									ProjectManager.saveOrDeleteProject(projectId, "save");	
									Date lastModified = PMProjectDAO.getInstance().updateLastModifiedDate(projectId);
									getAllTablePanel().getTable().setValueAt(lastModified, row, getAllTablePanel().getTable().getIndexOf("Last Modified"));
								}
								if(option != JOptionPane.CLOSED_OPTION) {
									TokenRelationDAO.getInstance().delete(projectId, userId);
									getAllTablePanel().getTable().setValueAt(null, row, getAllTablePanel().getTable().getIndexOf("Token Holder"));
									getAllTablePanel().getTable().setValueAt(null, row, getAllTablePanel().getTable().getIndexOf("Expiration Date"));						
									ProjectManager.removeProjectFromExploer(projectName);
									ChangeMonitor.changedProject.remove(projectId);
								}
							}else {
								TokenRelationDAO.getInstance().delete(projectId, userId);
								getAllTablePanel().getTable().setValueAt(null, row, getAllTablePanel().getTable().getIndexOf("Token Holder"));
								getAllTablePanel().getTable().setValueAt(null, row, getAllTablePanel().getTable().getIndexOf("Expiration Date"));						
								ProjectManager.removeProjectFromExploer(projectName);
								ChangeMonitor.changedProject.remove(projectId);
							}
						}else{
							ProjectManagerErrorConstants.promptCollaboratorRemovedError(projectName);
							DefaultTableModel model =(DefaultTableModel)getAllTablePanel().getTable().getModel();
						    model.removeRow(getAllTablePanel().getTable().convertRowIndexToModel(row));
						}
					} else {
						if (token == null) {
							ProjectManagerErrorConstants.promptNoTokenToReturnError(projectName);
						} else {
							ProjectManagerErrorConstants.promptNotOwnerOfTokenToReturnError(projectName);
						}
					}
					
				}
			}
		});
	}
	
	private void setRequestTokenButton() {
		requestTokenButton = new JButton("Request Token Release");
		requestTokenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!validateSelectedTables()) return;
				HashMap<String, ArrayList<String>> name_projects = new HashMap<String, ArrayList<String>>();
				for (int row : getAllTablePanel().getTable().getSelectedRows()) {
					String token = getProjectToken(row);
					String projectName = getProjectName(row);
					if(!String.valueOf(token).equals("null") && !token.equals(userName)){
						if(!name_projects.containsKey(token)){
							ArrayList<String> projects = new ArrayList<String>();
							projects.add(projectName);
							name_projects.put(token, projects);
						}else{
							name_projects.get(token).add(projectName);
						}	
					}
					
				}
				String message = "";
				for (Entry<String, ArrayList<String>> entry : name_projects.entrySet()) {
				    String name = entry.getKey();
				    ArrayList<String> projects = entry.getValue();
				    message = message + "An email will be sent to '"+name+"' to require token release for project(s):\n"+projects+"\n";
				    
				}
				if(!message.trim().equals("")){
					int option = JOptionPane.showConfirmDialog(null, message, "", JOptionPane.OK_CANCEL_OPTION);
					if(option == JOptionPane.OK_OPTION){
						for (Entry<String, ArrayList<String>> entry : name_projects.entrySet()) {
						    
						    User tokenHolderUser = UserDAO.getInstance().findUser(entry.getKey());
						    String tokenHolder = tokenHolderUser.getFirstName();
						    ArrayList<String> projects = entry.getValue();
						    User reply_to_user = UserDAO.getInstance().findUser(userName);
						    String collaborator = reply_to_user.getFirstName()+" "+reply_to_user.getLastName();
						    String send_to = UserDAO.getInstance().findEmail(tokenHolder);
						    SendEmail s = new SendEmail(reply_to_user.getEmail(),send_to, tokenHolder, collaborator, projects);
						    s.send();
							 
						}
					}	
				}else{
					JOptionPane.showMessageDialog(null, "Either you are the token holder or there is no token holder for the selected projects.");
				}
				
			}
		});
	}
	
	private void setCheckoutButton() {
		checkoutButton = new JButton("Checkout");
		checkoutButton.addActionListener(checkoutActionListener(true));
	}
	
	/**
	 * Login user check out token
	 */
	private ActionListener checkoutActionListener(boolean checkingOut){
		return new ActionListener() {	
			public void actionPerformed(ActionEvent e) {
				if(!validateSelectedTables()) return;
				final JDialog barDialog = PopulateProjectTree.ProjectsLoadingBar();
			    SwingWorker pmWorker = new SwingWorker() {
			        @Override
			        protected Object doInBackground() throws Exception {
			        	getProjectExplorerTabbedPane().getExplorerPanel().getProjectsTree().disable();
						for (int row : getAllTablePanel().getTable().getSelectedRows()) {
							String projectName = getProjectName(row);
							int projectId = PMProjectDAO.getInstance().findProjectId(projectName);
							TokenRelation token = TokenRelationDAO.getInstance().findTokenHolder(projectId);		
							if(CollaborateRelationDAO.getInstance().isCollaborator(projectId,userId)){
								if (token == null) {						
									TokenRelation tokenRelation = TokenRelationDAO.getInstance().insert(projectId, userId);
									getAllTablePanel().getTable().setValueAt(userName, row, getAllTablePanel().getTable().getIndexOf("Token Holder"));
									getAllTablePanel().getTable().setValueAt(tokenRelation.getExpirationTime(), row, getAllTablePanel().getTable().getIndexOf("Expiration Date"));
									ProjectTree projectTree = new ProjectTree(projectName);
									AccreteGBBeanFactory.getContext().getBean("projectExplorerTabbedPane", ProjectExplorerTabbedPane.class)
										.getExplorerPanel().addProject(projectTree);
									new CreateStockSelectionGroup(projectTree,projectId);
									new CreatePlantingGroup(projectTree,projectId);
									new CreatePhenotypeGroup(projectTree,projectId);
									new CreateSamplingGroup(projectTree,projectId);
									new CreateExperimentGroup(projectTree,projectId);
									new CreateHarvestGroup(projectTree,projectId);
									ChangeMonitor.changedProject.put(projectId,false);
									ChangeMonitor.projectIdName.put(projectId, projectName);
								} else {
									ProjectManagerErrorConstants.promptProjectAlreadyCheckedOutError(projectName);
									getAllTablePanel().getTable().setValueAt(UserDAO.getInstance().findUserName(token.getUserId()), row, getAllTablePanel().getTable().getIndexOf("Token Holder"));
									getAllTablePanel().getTable().setValueAt(new Date(token.getExpirationTime().getTime()), row, getAllTablePanel().getTable().getIndexOf("Expiration Date"));
									
								}
							}else{
								ProjectManagerErrorConstants.promptCollaboratorRemovedError(projectName);
								populateTable();
							}
						}
			            return null;
			        }

			        @Override
			        public void done(){
			        	barDialog.setVisible(false);
			            barDialog.dispose();
				  		getProjectExplorerTabbedPane().getExplorerPanel().getProjectsTree().enable();
			        }
			    };
			    pmWorker.execute();
			}
		};
	}
	
	
	//TODO:
	public void addRefreshButtonListener() {
		getAllTablePanel().getRefreshButton().setToolTipText("Refresh");
		getAllTablePanel().getRefreshButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				populateTable();
			}
		});
	}

	/**
	 * Project owner deletes project
	 */
	public void addDeleteButtonListener() {
		getAllTablePanel().getDeleteButton().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				for (int row : getAllTablePanel().getTable().getSelectedRows()){
					String projectOwner = getProjectOwner(row);
					if(projectOwner.equals(userName))
					{
						String projectName = getProjectName(row);
						int projectId = PMProjectDAO.getInstance().findProjectId(projectName);	
						int option = JOptionPane.showConfirmDialog(null, "Are you sure to delete the project? ", "", JOptionPane.OK_OPTION); 							
						if(option == JOptionPane.OK_OPTION){
							CollaborateRelationDAO.getInstance().delete(projectId);
							TokenRelationDAO.getInstance().delete(projectId);
							ProjectManager.saveOrDeleteProject(projectId,"delete");
							PMProjectDAO.getInstance().delete(projectId);
							DefaultTableModel model =(DefaultTableModel)getAllTablePanel().getTable().getModel();
						    model.removeRow(getAllTablePanel().getTable().convertRowIndexToModel(row));
							DefaultMutableTreeNode removedProjectTree = null;
							JTree projectTrees = AccreteGBBeanFactory.getContext().getBean("projectExplorerTabbedPane", ProjectExplorerTabbedPane.class)
									.getExplorerPanel().getProjectsTree();
							DefaultMutableTreeNode projectsRoot = (DefaultMutableTreeNode) projectTrees.getModel().getRoot();
							for(int childIndex = 0; childIndex < projectsRoot.getChildCount();++childIndex ){
								if(projectsRoot.getChildAt(childIndex).toString().equals(projectName)){
									removedProjectTree = (DefaultMutableTreeNode) projectsRoot.getChildAt(childIndex);
								}
							}
							AccreteGBBeanFactory.getContext().getBean("projectExplorerTabbedPane", ProjectExplorerTabbedPane.class)
								.getExplorerPanel().removeProject(removedProjectTree);		
						}
						
					}else{
						ProjectManagerErrorConstants.promptNotProjectOwnerDeleteError();
					}
				}				
			}			
		});
		
	}

	/**
	 * Get info of projects that login user collaborates on.
	 */
	public static void populateTable() {
		DefaultTableModel model = (DefaultTableModel) getAllTablePanel().getTable().getModel();
		Utils.removeAllRowsFromTable(model);
		List<Integer> projectIds = CollaborateRelationDAO.getInstance().findByUserId(userId);
		if(projectIds.size()>0){
			for (Integer projectId : projectIds) {
				PMProject project = PMProjectDAO.getInstance().findProjectObj(projectId);
				TokenRelation tokenRelation = TokenRelationDAO.getInstance().findTokenHolder(projectId);
				ProjectRow newRow = new ProjectRow(project.getProjectName(), UserDAO.getInstance().findUserName(project.getUserId()), 
						tokenRelation == null? null : UserDAO.getInstance().findUserName(tokenRelation.getUserId()), 
					    tokenRelation == null? null : new Date(tokenRelation.getExpirationTime().getTime()),
					    project.getDateCreated(),
						project.getLastModified());
				model.addRow(newRow.toObjects());		    
			}
		}
		getAllTablePanel().getTable().setModel(model);
	}
	
	public CheckBoxIndexColumnTable getAllTable() {
		return allTable;
	}

	public void setAllTable(CheckBoxIndexColumnTable allTable) {
		this.allTable = allTable;
	}

	public static TableToolBoxPanel getAllTablePanel() {
		return allTablePanel;
	}

	public void setAllTablePanel(TableToolBoxPanel allTablePanel) {
		this.allTablePanel = allTablePanel;
	}

	private String getProjectName(int row){
		int column = getAllTablePanel().getTable().getIndexOf("Project Name");
		return (String) getAllTablePanel().getTable().getValueAt(row, column);
	}
	private String getProjectToken(int row){
		int column = getAllTablePanel().getTable().getIndexOf("Token Holder");
		return (String) getAllTablePanel().getTable().getValueAt(row, column);
	}
	
	private String getProjectOwner(int row){
		int column = getAllTablePanel().getTable().getIndexOf("Owner");
		return (String) getAllTablePanel().getTable().getValueAt(row, column);	
	}
  public  ProjectExplorerTabbedPane getProjectExplorerTabbedPane() {
		return AccreteGBBeanFactory.getContext().getBean("projectExplorerTabbedPane", ProjectExplorerTabbedPane.class);
	}
}
