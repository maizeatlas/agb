package org.accretegb.modules.germplasm.planting;

import static org.accretegb.modules.customswingcomponent.Utils.saveTableToFile;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.PlotIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.config.AccreteGBContext;
import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.hibernate.dao.ObservationUnitDAO;
import org.accretegb.modules.hibernate.dao.StockDAO;
import org.accretegb.modules.phenotype.PhenotypeExportPanel;
import org.accretegb.modules.projectexplorer.ProjectTreeNode;
import org.accretegb.modules.sampling.SampleSelectionPanel;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.util.ChangeMonitor;
import org.accretegb.modules.util.LoggerUtils;
import org.apache.commons.lang.StringUtils;

import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;

/**
 * @author chinmay
 * @author Ningjing
 *
 */
public class TagGenerator extends TabComponentPanel {

	private static final long serialVersionUID = 1L;
	private TableToolBoxPanel tagsTablePanel;	
	private int fieldId;
	private TableView tableView;
	private JProgressBar progress ;
	private PhenotypeExportPanel phenotypeExportPanel;
	private SampleSelectionPanel sampleSelectionPanel;
	private ProjectTreeNode phenotypeNode;
	private ProjectTreeNode samplingNode;
	private JTabbedPane plantingTabs;
	private List<PlantingRow> stockList;

	JButton exportPlotTags;
	JButton exportPlantTags;
	JButton exportTable;
	JButton getStocks;
	boolean valid;
	
	public void initialize() {
		setLayout(new MigLayout("insets 20, gap 10"));
		add(tagsTablePanel, "w 100%, h 100%, wrap");
		hideColumns();
		setExportPanel();
		addSyncListener();
		progress = new JProgressBar(0, 100);
		progress.setVisible(false);
		add(progress, "w 100%, hidemode 3, wrap");
	}
	
	private void addSyncListener() {
		final CheckBoxIndexColumnTable table = getTagsTablePanel().getTable();
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		Utils.removeAllRowsFromTable((DefaultTableModel)table.getModel());
		getTagsTablePanel().getRefreshButton().setToolTipText("Sync with database");
		getTagsTablePanel().getRefreshButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				String message = "Are you sure you want to save the data to database?";
				int option = JOptionPane.showConfirmDialog(null, message, "",
						JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
				if (option == JOptionPane.OK_OPTION){
					SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
						@Override
						protected Boolean doInBackground() throws Exception {;
							progress.setStringPainted(true);
						    progress.setVisible(true);
						    progress.setValue(0);
							progress.setString("Checking if tags already exist...");
						  	valid = validateSync();						
							return null;
				
					}
					 @Override
			            protected void done() {
						 if(valid){
							new SyncTags(table, fieldId, progress, TagGenerator.this).execute();
								  
						 }
					  }
					};
					worker.execute();
						
				}	
				ChangeMonitor.markAsChanged(tableView.getProjectID());
			}
		});
	}

	private void hideColumns() {
		CheckBoxIndexColumnTable table = tagsTablePanel.getTable();
		table.hideColumn(ColumnConstants.STOCK_ID);
		table.hideColumn(ColumnConstants.ROW_NUM);
		table.hideColumn(ColumnConstants.GENERATION);
		table.hideColumn(ColumnConstants.CYCLE);
	}

	public void populateTableFromTabelView(){
		PlotIndexColumnTable tableViewTable= tableView.getStocksOrderPanel().getTable();
		CheckBoxIndexColumnTable syncTagtable = getTagsTablePanel().getTable();
		DefaultTableModel tableModel = ((DefaultTableModel)syncTagtable.getModel());
	    removeAllRowsFromTable(tableModel);	
	    
	    for (int row = 0; row < tableViewTable.getRowCount();row++){
	        Object[] newRow = new Object[syncTagtable.getColumnCount()];
	        newRow[0] = new Boolean(false);
	        for(int col_view = 0; col_view<tableViewTable.getColumnCount(); ++col_view ){
	        	newRow[col_view+1] = tableViewTable.getValueAt(row, col_view);
	        }
	        tableModel.addRow(newRow);
	               
	    }
	   
	    syncTagtable.hideColumn(ColumnConstants.ROW_NUM);
	    updateNumberOfItems(tableModel.getRowCount());
	    updateSyncStatus();  
	}
	
	// change tab need to validate 
	private boolean validateSync(){						
	  	final CheckBoxIndexColumnTable table = getTagsTablePanel().getTable();	  
	  	ArrayList<String> tags = new ArrayList<String>();
	  	for(int row = 0; row < table.getRowCount();++row){
			String tag = (String) table.getValueAt(row, table.getIndexOf(ColumnConstants.TAG));
			progress.setValue((int) ((row * 1.0 / table.getRowCount()) * 100));
			progress.setString("Checking if tags already exist... ");
			tags.add(tag);
						
	  	}
	  	if(ObservationUnitDAO.getInstance().isExist(tags)){
			int option = JOptionPane.showOptionDialog(null, 
			        "Some tagnames were found in the database.\n"
			        + "Choose 'Reset start row or planting index in Table View' to generate new tagnames for all the plots\n"
			        + "Choose 'Continue' to update the existing tags with the current table data  ",
			        "", 
			        JOptionPane.YES_NO_CANCEL_OPTION, 
			        JOptionPane.INFORMATION_MESSAGE, 
			        null, 
			        new String[]{"Reset start row or planting index in Table View","Continue","Cancel"},
			        "default");
	        if(option ==JOptionPane.OK_OPTION )
	        {	
	        	//reset
	        	plantingTabs.setSelectedIndex(1);
	        	progress.setVisible(false);
	        	return false;
	        }else if(option == JOptionPane.NO_OPTION){
	        	//update existing
	        	return true;				 
	        }else{
	        	progress.setVisible(false);
	        	return false;
	        }
		
		
		}	
	  	return true;	
	}

	private void importTags() {
		try {
			final JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter("Comma separated files(.csv)", "csv"));
			fc.showOpenDialog(this);

			if(fc.getSelectedFile()==null)
				return;

			String filename = fc.getSelectedFile().toString();	

			if (!(filename.trim().endsWith(".csv"))) {
				JLabel errorFields = new JLabel("<HTML><FONT COLOR = Blue>"
						+ "File should have a .csv extension only" + ".</FONT></HTML>");
				JOptionPane.showMessageDialog((Component) AccreteGBContext.getContext().getBean("tagGeneratorChildPanel0"), errorFields);
			} else {
				CheckBoxIndexColumnTable table = getTagsTablePanel().getTable();
				BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
				String line = br.readLine();
				for(int rowCount = 0; rowCount < table.getRowCount() && ( line=br.readLine() ) !=null ; rowCount++) {
					String tags[] = line.split(",");
					table.setValueAt(Integer.parseInt(tags[0]), rowCount, table.getIndexOf(ColumnConstants.START_TAG));	
					table.setValueAt(Integer.parseInt(tags[1]), rowCount, table.getIndexOf(ColumnConstants.END_TAG));
					rowCount = rowCount+ (Integer.parseInt(tags[1]) - Integer.parseInt(tags[0]))+1;
				}				
				br.close();	
			}

		} catch (Exception e) {
			if(LoggerUtils.isLogEnabled())
				LoggerUtils.log(Level.INFO, e.toString());
		}
	}

	private void setExportPanel() {
		JPanel exportPanel = getTagsTablePanel().getBottomHorizonPanel();
		exportPlotTags = new JButton("Plot Tags");
		exportPlantTags = new JButton("Plant Tags");
		exportTable = new JButton("Export");
		getStocks = new JButton("Get Stocks");
		addExportPlotTagsListener();
		addExportPlantTagsListener();
		addExportTableListener();
		addGetStocksListener();
		exportPanel.add(exportTable, "pushx, al right");
		exportPanel.add(getStocks);
		exportPanel.add(exportPlantTags);
		exportPanel.add(exportPlotTags);
	}
	
	public void updateNumberOfItems(int size){
		if(size == 0){
			getTagsTablePanel().getNumberOfRows().setText("Loading...");
		}else{
			getTagsTablePanel().getNumberOfRows().setText(String.valueOf(size));
		}
		
	}

	private void addExportTableListener() {
		exportTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveTableToFile(getTagsTablePanel().getTable(), TagGenerator.this , "tagGeneratorTable.csv");
			}			
		});
	}
	
	private void addGetStocksListener(){
		
		getStocks.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					if (getTagsTablePanel().getTable() == null || getTagsTablePanel().getTable().getRowCount()<=0) {
			                return;
			            }
					JFileChooser fileChooser = new JFileChooser();
		            File file = new File(System.getProperty("java.io.tmpdir") + "/" + "StocksInventory.csv");
		            fileChooser.setSelectedFile(file);
		            int approve = fileChooser.showSaveDialog(null);
		            if (approve != JFileChooser.APPROVE_OPTION) {
		                return;
		            }
		            file = fileChooser.getSelectedFile();
		            
		            LinkedHashMap<String, List<String>> stockname_info = new LinkedHashMap<String, List<String>>();
					Set<String> stocknames = new HashSet<String>();
					for(int row = 0; row <getTagsTablePanel().getTable().getRowCount(); ++row ){
						String stockname = String.valueOf(getTagsTablePanel().getTable().getValueAt(row, ColumnConstants.STOCK_NAME));
						String plot = "|"+String.valueOf(getTagsTablePanel().getTable().getValueAt(row, ColumnConstants.ROW));
						String accession = String.valueOf(getTagsTablePanel().getTable().getValueAt(row, ColumnConstants.ACCESSION));
						String pedigree = String.valueOf(getTagsTablePanel().getTable().getValueAt(row, ColumnConstants.PEDIGREE));
						String kernel = String.valueOf(getTagsTablePanel().getTable().getValueAt(row, ColumnConstants.KERNELS));
						String delay = String.valueOf(getTagsTablePanel().getTable().getValueAt(row, ColumnConstants.DELAY));
						
						stockname_info.put(stockname, new ArrayList<String>());
						Collections.addAll(stockname_info.get(stockname),plot, accession.equals("NA")? pedigree:accession,kernel, delay);
						
						stocknames.add(stockname);
					}
					List<Object []> results = StockDAO.getInstance().searchInventory(stocknames);
					if(results.size()>0){
						for(Object[] result : results ){
							int index = 0;
							for(Object str : result){
								if(index >= 2)
								{
									stockname_info.get(result[1]).add(String.valueOf(str));
								}
								index++;
							}
						}
						
						BufferedWriter writer = new BufferedWriter(new FileWriter(file));
						writer.write("Stock Name,");
						writer.write("Row,");
						writer.write("Accession/Pedigree,");
						writer.write("Kernel,");
						writer.write("Delay,");
						writer.write("Pkt No.,");
						writer.write("Weight,");
						writer.write("No_seed,");
						writer.write("Tier1_position,");
						writer.write("Tier2_position,");
						writer.write("Tier3_position,");
						writer.write("Shelf,");
						writer.write("Box,");
						writer.write("Room,");
						writer.write("Building,");
						writer.write("Location Name,");
						writer.write("City,");
						writer.write("State,");
						writer.write("Country,");
						writer.newLine();
						for(String key : stockname_info.keySet() ){
							writer.write(key + ",");
							for(String str :stockname_info.get(key)){
								writer.write(str + ",");
							}
							writer.newLine();
						}						
						writer.close();
					 } 
				 }catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
		});
	}

	private void addExportPlantTagsListener() {
		exportPlantTags.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				try {
					JFileChooser fileChooser = new JFileChooser();
					File file = new File(System.getProperty("user.home")+"/"+"PlantTags.csv");
					fileChooser.setSelectedFile(file);
					int approve = fileChooser.showSaveDialog(null);
					if (approve != JFileChooser.APPROVE_OPTION) {
						return;
					}
					file = fileChooser.getSelectedFile();
					writeTagsToFile(file, "   Plant");
					Desktop.getDesktop().open(file);				
				} catch (IOException e) {
					if(LoggerUtils.isLogEnabled())
						LoggerUtils.log(Level.INFO, e.toString());
				}
			}	
		});
	}
	
	private void updateTableView(){
		PlotIndexColumnTable table = tableView.getStocksOrderPanel().getTable();
		for(int row = 0; row < table.getRowCount();++row){
			if(getTagsTablePanel().getTable().getValueAt(row,  getTagsTablePanel().getTable().getIndexOf(ColumnConstants.TAG_ID))!= null){
				int tagid =  (Integer) getTagsTablePanel().getTable().getValueAt(row,  getTagsTablePanel().getTable().getIndexOf(ColumnConstants.TAG_ID));			
				table.setValueAt(tagid, row, table.getIndexOf(ColumnConstants.TAG_ID));
				table.setValueAt(false, row, table.getIndexOf(ColumnConstants.MODIFIED));
			}
			if(getTagsTablePanel().getTable().getValueAt(row,  getTagsTablePanel().getTable().getIndexOf(ColumnConstants.EXP_FACTOR_VALUE_IDS))!= null){
				String ids = (String)getTagsTablePanel().getTable().getValueAt(row,  getTagsTablePanel().getTable().getIndexOf(ColumnConstants.EXP_FACTOR_VALUE_IDS));
				table.setValueAt(ids, row, table.getIndexOf(ColumnConstants.EXP_FACTOR_VALUE_IDS));
			}
		}
		tableView.updateWhenRowCountChanged();
		tableView.synced = true;
	}

	private void addExportPlotTagsListener() {
		exportPlotTags.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				try {
					JFileChooser fileChooser = new JFileChooser();
					File file = new File(System.getProperty("user.home")+"/"+"PlotTags.csv");
					fileChooser.setSelectedFile(file);
					int approve = fileChooser.showSaveDialog(null);
					if (approve != JFileChooser.APPROVE_OPTION) {
						return;
					}
					file = fileChooser.getSelectedFile();
					writeTagsToFile(file, ColumnConstants.ROW);
					Desktop.getDesktop().open(file);				
				} catch (IOException e) {
					if(LoggerUtils.isLogEnabled())
						LoggerUtils.log(Level.INFO, e.toString());
				}
			}
		});
	}
	
	private void writeTagsToFile(File file, String type) throws IOException {
		CheckBoxIndexColumnTable table = getTagsTablePanel().getTable();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write("field1,field2,field3,field4,field5");
		writer.newLine();
		for(int rowCounter = 0; rowCounter < table.getRowCount(); rowCounter++) {
			if((String.valueOf(table.getValueAt(rowCounter, ColumnConstants.TYPES))).equals(type)) {
				writer.write("|" + (String)table.getValueAt(rowCounter, ColumnConstants.ROW));
				writer.write(",");
				writer.write((String)table.getValueAt(rowCounter, ColumnConstants.TAG));
				writer.write(",");
				if(type.equals("Row")){
					writer.write("|" + (String)table.getValueAt(rowCounter, ColumnConstants.TAG));
				}else{
					writer.write("|" + (String)table.getValueAt(rowCounter, ColumnConstants.PLANT));
				}
				
				writer.write(",");
				String filed4 = ((String)table.getValueAt(rowCounter, ColumnConstants.ACCESSION)).equals("NA") 
						? (String)table.getValueAt(rowCounter, ColumnConstants.PEDIGREE)
								: (String)table.getValueAt(rowCounter, ColumnConstants.ACCESSION);
				writer.write(filed4);
				writer.write(",");
				writer.write(StringUtils.defaultIfBlank((String)table.getValueAt(rowCounter, ColumnConstants.MATING_PLAN), ""));
				writer.newLine();
			}
		}
		writer.close();
	}	

	//used in retrieving rows from database.
	public void populateTableFromObjects(List<Object[]> tableRows ) {
        DefaultTableModel tableModel = ((DefaultTableModel) getTagsTablePanel().getTable().getModel());
        removeAllRowsFromTable(tableModel);	
    	for(Object[] row : tableRows){
    		tableModel.addRow(row);               
    	}
    	updateNumberOfItems(tableModel.getRowCount());
    	updateSyncStatus();    	
    	
    }
	

	public void finishedSync(boolean rollbacked) {
		setEnabled(true);
		if(!rollbacked) {
			updateTableView();
			tableView.prefixIsFixed = true;
			//mapView.getNeedSync().setVisible(false);
			getTagsTablePanel().getTable().setHasSynced(true);
			if(getTagsTablePanel().getTable().getRowCount() >= 1)
			{
				if(getPhenotypeExportPanel().getIsFirstSync() == null || getPhenotypeExportPanel().getIsFirstSync()){
					Vector data = ((DefaultTableModel) getTagsTablePanel().getTable().getModel()).getDataVector();
					getPhenotypeExportPanel().getSubsetJlistMap().clear();
					getPhenotypeExportPanel().getSubsetTableMap().clear();
					getPhenotypeExportPanel().getSubsetCommentMap().clear();
					getPhenotypeExportPanel().setTableData(data);
					getPhenotypeExportPanel().populateTable();
					getPhenotypeExportPanel().setIsFirstSync(false);

				}else{
					Vector data = ((DefaultTableModel) getTagsTablePanel().getTable().getModel()).getDataVector();
					getPhenotypeExportPanel().getSubsetJlistMap().clear();
					getPhenotypeExportPanel().getSubsetTableMap().clear();
					getPhenotypeExportPanel().getSubsetCommentMap().clear();
					getPhenotypeExportPanel().setTableData(data);
					getPhenotypeExportPanel().plantingResync();
					getPhenotypeExportPanel().populateTable();

				}
				if(getSampleSelectionPanel().getIsFirstSync() == null || getSampleSelectionPanel().getIsFirstSync()){
					Vector data = ((DefaultTableModel) getTagsTablePanel().getTable().getModel()).getDataVector();
					getSampleSelectionPanel().getSubsetCommentMap().clear();
					getSampleSelectionPanel().getSubsetTableMap().clear();
					getSampleSelectionPanel().setShouldResetMaps(true);
					getSampleSelectionPanel().setTableData(data);
					getSampleSelectionPanel().populateTable();
					getSampleSelectionPanel().setIsFirstSync(false);

				}else{
					Vector data = ((DefaultTableModel) getTagsTablePanel().getTable().getModel()).getDataVector();
					getSampleSelectionPanel().getSubsetCommentMap().clear();
					getSampleSelectionPanel().getSubsetTableMap().clear();
					getSampleSelectionPanel().setShouldResetMaps(true);
					getSampleSelectionPanel().setTableData(data);
					getSampleSelectionPanel().plantingResync();
					getSampleSelectionPanel().populateTable();
				}
			}
			getTagsTablePanel().getTable().clearSelection();
			tableView.synced = true;	
		}
		
		else{
			tableView.synced = false;	
			JOptionPane.showMessageDialog(null, "There were erros in syncing","", JOptionPane.ERROR_MESSAGE);
		}
		progress.setVisible(false);
	}
	
	public List<PlantingRow> allStocks() {
		PlotIndexColumnTable table = tableView.getStocksOrderPanel().getTable();
		List<PlantingRow> allStocks = new ArrayList<PlantingRow>();
		for(int row=0;row<table.getRowCount();row++) {
			Object[] rowData = new Object[table.getColumnCount()];
			for(int column=0;column<table.getColumnCount();column++) {
				rowData[column] = table.getValueAt(row, column);
			}
			allStocks.add(new PlantingRow(rowData, table));
		}
		return allStocks;
	}
	
	public void updateSyncStatus(){
		getTagsTablePanel().getTable().setHasSynced(tableView.synced);
	}
	
	public JTabbedPane getPlantingTabs() {
		return plantingTabs;
	}

	public void setPlantingTabs(JTabbedPane plantingTabs) {
		this.plantingTabs = plantingTabs;
	}
	
	public List<PlantingRow> getStockList() {
		return stockList;
	}

	public void setStockList(List<PlantingRow> stockList) {
		this.stockList = stockList;
	}
	
	public ProjectTreeNode getPhenotypeNode() {
		return phenotypeNode;
	}

	public void setPhenotypeNode(ProjectTreeNode phenotypeNode) {
		this.phenotypeNode = phenotypeNode;
	}
	public TableToolBoxPanel getTagsTablePanel() {
		return tagsTablePanel;
	}

	public void setTagsTablePanel(TableToolBoxPanel tagsTablePanel) {
		this.tagsTablePanel = tagsTablePanel;
	}
	
	public PhenotypeExportPanel getPhenotypeExportPanel() {
		return phenotypeExportPanel;
	}

	public void setPhenotypeExportPanel(PhenotypeExportPanel phenotypeExportPanel) {
		this.phenotypeExportPanel = phenotypeExportPanel;
	}
	
	public SampleSelectionPanel getSampleSelectionPanel() {
		return sampleSelectionPanel;
	}

	public void setSampleSelectionPanel(SampleSelectionPanel sampleSelectionPanel) {
		this.sampleSelectionPanel = sampleSelectionPanel;
	}
	
	public ProjectTreeNode getSamplingNode() {
		return samplingNode;
	}

	public void setSamplingNode(ProjectTreeNode samplingNode) {
		this.samplingNode = samplingNode;
	}

	public void setFieldId(int fieldId) {
		this.fieldId = fieldId;
	}	
	
	public TableView getTableView() {
		return tableView;
	}

	public void setTableView(TableView tableView) {
		this.tableView = tableView;
	}
}
