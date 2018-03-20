package org.accretegb.modules.phenotype;


import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.tab.TabComponentPanel;

public class Phenotype extends TabComponentPanel {
	private static final long serialVersionUID = 1L;
	private PhenotypeExportPanel phenotypeExportPanel;
	private PhenotypeImportPanel phenotypeImportPanel;
	
	public void initialize() {
		setLayout(new MigLayout("insets 10, gap 10"));
		
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add(phenotypeExportPanel);
		tabbedPane.setTitleAt(0, "Export");
		
		tabbedPane.add(phenotypeImportPanel);
		tabbedPane.setTitleAt(1, "Import");
		
		// when click tabs, refresh current invenroty address info in packets inventory
		tabbedPane.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				phenotypeImportPanel.setParametersInfo();
			}			
		});		
		add(tabbedPane, "w 100%, h 100%");
	}
	
	
	
	
	
	public PhenotypeExportPanel getPhenotypeExportPanel() {
		return phenotypeExportPanel;
	}
	public void setPhenotypeExportPanel(PhenotypeExportPanel phenotypeExportPanel) {
		this.phenotypeExportPanel = phenotypeExportPanel;
	}
	public PhenotypeImportPanel getPhenotypeImportPanel() {
		return phenotypeImportPanel;
	}
	public void setPhenotypeImportPanel(PhenotypeImportPanel phenotypeImportPanel) {
		this.phenotypeImportPanel = phenotypeImportPanel;
	}
	

}
