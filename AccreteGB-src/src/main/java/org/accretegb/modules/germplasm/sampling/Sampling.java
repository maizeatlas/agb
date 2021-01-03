package org.accretegb.modules.germplasm.sampling;


import java.util.Date;
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.util.GlobalProjectInfo;
import org.json.JSONObject;

public class Sampling extends TabComponentPanel {
	private static final long serialVersionUID = 1L;
	private SampleSelectionPanel sampleSelectionPanel;
	private SampleSettingPanel sampleSettingPanel;
	
	public void initialize() {
		setLayout(new MigLayout("insets 10, gap 10"));
		
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add(sampleSelectionPanel);
		tabbedPane.setTitleAt(0, "Selection");
		
		tabbedPane.add(sampleSettingPanel);
		tabbedPane.setTitleAt(1, "Settings");
		add(tabbedPane, "w 100%, h 100%");
		
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				int currentTab = tabbedPane.getSelectedIndex();	
				sampleSettingPanel.setSubsetCommentMap(sampleSettingPanel.getSubsetCommentMap());
				if(currentTab == 1) {
					updateSubsets();
				  }
				}
		});
		
	}
	
	
	
	public void updateSubsets(){
		// only contains All
		if (sampleSelectionPanel.isShouldResetMaps()) {
			sampleSettingPanel.getSubsetCommentMap().clear();
			sampleSettingPanel.getSubsetTableMap().clear();
			sampleSettingPanel.getSubsetInfo().clear();
			sampleSettingPanel.getSampleSettingTablePanel().getTableSubset().removeAllItems();
			sampleSelectionPanel.setShouldResetMaps(false);
		}
		JComboBox subset = sampleSettingPanel.getSampleSettingTablePanel().getTableSubset();
		DefaultComboBoxModel subsetNamemodel = (DefaultComboBoxModel)subset.getModel();
		for(String key :sampleSelectionPanel.getSubsetTableMap().keySet()){
			if(!sampleSettingPanel.getSubsetTableMap().keySet().contains(key)){
				sampleSettingPanel.getSubsetTableMap().put(key, sampleSelectionPanel.getSubsetTableMap().get(key));
				Object[][] subsetData =	(Object[][]) sampleSettingPanel.getSubsetTableMap().get(key);
				CheckBoxIndexColumnTable table = sampleSettingPanel.getSampleSettingTablePanel().getTable();
				sampleSettingPanel.setInitialIndex(String.valueOf(subsetData[0][table.getIndexOf(ColumnConstants.TAG)])); 
				HashMap<String, Object> tmp = new HashMap<String, Object>();
				tmp.put("prefix", sampleSettingPanel.getInitialPrefix());
				tmp.put("date", new Date());
				sampleSettingPanel.getSubsetInfo().put(key, tmp);	
				
				if(subsetNamemodel.getIndexOf(key) == -1)
				{
					sampleSettingPanel.getSampleSettingTablePanel().getTableSubset().addItem(key);
				}
				sampleSettingPanel.getSampleSettingTablePanel().getTableSubset().setSelectedItem(key);
				sampleSettingPanel.populateSelectionSubset(key);
				
			}else {
				if (sampleSettingPanel.currentSubset == null ) {
					sampleSettingPanel.getSampleSettingTablePanel().getTableSubset().setSelectedItem(key);
					sampleSettingPanel.currentSubset = key;
				}
				if (sampleSettingPanel.location == null) {
					sampleSettingPanel.setZipcode();
				}
			}
		}
	}
	
	
	public SampleSelectionPanel getSampleSelectionPanel() {
		return sampleSelectionPanel;
	}
	public void setSampleSelectionPanel(SampleSelectionPanel sampleSelectionPanel) {
		this.sampleSelectionPanel = sampleSelectionPanel;
	}
	public SampleSettingPanel getSampleSettingPanel() {
		return sampleSettingPanel;
	}
	public void setSampleSettingPanel(SampleSettingPanel sampleSettingPanel) {
		this.sampleSettingPanel = sampleSettingPanel;
	}
	

}
