package org.accretegb.modules.projectmanager;

import javax.swing.JButton;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.tab.TabComponentPanel;

public class ProjectManagerTab extends TabComponentPanel {
	private ProjectManagerAllPanel projectManagerAllPanel;	
	
	public void initialize(){
		setLayout(new MigLayout("insets 10, gap 10"));
		
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add(projectManagerAllPanel);
		tabbedPane.setTitleAt(0, "ALL");		
		add(tabbedPane,"w 100%, h 100%");
		
	}
	
	public ProjectManagerAllPanel getProjectManagerAllPanel() {
		return projectManagerAllPanel;
	}
	public void setProjectManagerAllPanel(
			ProjectManagerAllPanel projectManagerAllPanel) {
		this.projectManagerAllPanel = projectManagerAllPanel;
	}
	
	
}
