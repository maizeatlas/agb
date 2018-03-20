package org.accretegb.modules.germplasm.planting;

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.util.ThreadPool;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class Planting extends TabComponentPanel {

	private static final long serialVersionUID = 1L;

	public List<PlantingRow> listOfStocks;
	private FieldSelection fieldSelection;
	private TableView tableView;
	private MapView mapView;
	private TagGenerator tagGenerator;
	public boolean visitedTableView = false;
	public boolean visitedMapView = false;
	public boolean fromDatabase = false;

	
	public void initialize() {
		setLayout(new MigLayout("insets 10, gap 10"));
		
		final JTabbedPane tabbedPane = new JTabbedPane();
				
		tabbedPane.add(fieldSelection);
		tabbedPane.setTitleAt(0, "Field Selection");
		
		tabbedPane.add(tableView);
		tabbedPane.setTitleAt(1, "Table View");
		
		tabbedPane.add(mapView);
		tabbedPane.setTitleAt(2, "Map View");
		
		tabbedPane.add(tagGenerator);
		tabbedPane.setTitleAt(3, "Sync/Export Tags");

		tableView.setTagGenerator(tagGenerator);
		mapView.getCanvas().setTableView(tableView);
		mapView.setTableView(tableView);
		tagGenerator.setTableView(tableView);
		tagGenerator.setPlantingTabs(tabbedPane);
		
		if(listOfStocks == null) {
			listOfStocks = new ArrayList<PlantingRow>();				 				
		}		
		tableView.setStockList(listOfStocks);
		mapView.getCanvas().setStockList(listOfStocks);
		tagGenerator.setStockList(listOfStocks);
		
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {	
				int currentTab = tabbedPane.getSelectedIndex();				
				if(currentTab == 1) {
					if(fieldSelection.getZipcode() == null){
						tabbedPane.setSelectedIndex(0);
						JOptionPane.showConfirmDialog(Planting.this, "Select a location to generate tags", "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
					}else{
						visitedTableView = true;
						tableView.setZipcode(fieldSelection.getZipcode());					
						if(fieldSelection.isFieldChanged()){
							tableView.populateTableBasedonDatabase(listOfStocks);	
							//System.out.println("field changed");
							fieldSelection.setFieldChanged(false);
							if(tableView.prefixIsFixed){
								JOptionPane.showMessageDialog(null, "<HTML>Only one field is allowed in one planting group.<br>"
										+ "You have synced the planting group.<br>"
										+ "Changing field is not going to change prefix of the tag names</HTML>");
							}
							else
							{
								tableView.prefix = null;
							}
						}else{
							tableView.populateTableFromList(listOfStocks);	
						    //System.out.println("field not changed");
						}
					}					
				}else if(currentTab == 2) {
					if(visitedTableView){
						mapView.getCanvas().StartRow = Integer.parseInt(String.valueOf(listOfStocks.get(0).getRow()));
						mapView.getCanvas().setStockList(listOfStocks);						
						mapView.updateDimensions();
						//System.out.println("3" + listOfStocks.get(0).getTag());
					}else{
						tabbedPane.setSelectedIndex(0);
						JOptionPane.showConfirmDialog(Planting.this, "Click Table View tab to generate tags first", "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);					
					}
					
				}else if(currentTab == 3) {
					if(visitedTableView){
						tableView.populateTableFromList(listOfStocks);
						//System.out.println("4" + listOfStocks.get(0).getTag());
						tagGenerator.setFieldId(fieldSelection.getFieldId());												
						if(tagGenerator.getTagsTablePanel().getTable().getRowCount() == 0 ){
							//popluate the table the first time
							tagGenerator.populateTableFromTabelView();
							//System.out.println("5" + listOfStocks.get(0).getTag());
						}else if(tableView.isTableChanged()){
							// updated table view, and re-populate	
							tagGenerator.getTagsTablePanel().getTable().setHasSynced(false);
							tagGenerator.populateTableFromTabelView();
							//System.out.println("6" + listOfStocks.get(0).getTag());
						}						
					}else{
						tabbedPane.setSelectedIndex(0);
						JOptionPane.showConfirmDialog(Planting.this, "Click Table View tab to generate tags first", "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);					
					}
					
				}				
			}
		});
		
		add(tabbedPane, "w 100%, h 100%");
	}	
	
	public TableView getTableView() {
		return tableView;
	}

	public void setTableView(TableView tableView) {
		this.tableView = tableView;
	}

	public MapView getMapView() {
		return mapView;
	}

	public void setMapView(MapView mapView) {
		this.mapView = mapView;
	}

	public TagGenerator getTagGenerator() {
		return tagGenerator;
	}

	public void setTagGenerator(TagGenerator tagGenerator) {
		this.tagGenerator = tagGenerator;
	}

	public List<PlantingRow> getListOfStocks() {
		return listOfStocks;
	}

	public void setListOfStocks(List<PlantingRow> listOfStocks) {
		this.listOfStocks = listOfStocks;
	}

	public FieldSelection getFieldSelection() {
		return fieldSelection;
	}

	public void setFieldSelection(FieldSelection fieldSelection) {
		this.fieldSelection = fieldSelection;
	}

	
}
