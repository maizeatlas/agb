package org.accretegb.modules.germplasm.harvesting;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.CustomCalendar;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.hibernate.MeasurementUnit;
import org.accretegb.modules.hibernate.dao.MeasurementUnitDAO;
import org.accretegb.modules.hibernate.dao.ObservationUnitDAO;
import org.accretegb.modules.hibernate.dao.PassportDAO;
import org.accretegb.modules.main.StringEncrypter;
import org.accretegb.modules.util.AdjustTableColumnSize;
import org.accretegb.modules.util.LoggerUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.accretegb.modules.constants.ColumnConstants;

import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;
import static org.accretegb.modules.customswingcomponent.Utils.saveTableToFile;

public class StickerGenerator extends JPanel {

	private TableToolBoxPanel stickerTablePanel;
	private CustomCalendar calendar;
	private JButton dateApply;
	private JButton setQuantity;
	private JButton setPacketNumber;
	private JTextField quantity;
	private JComboBox mixUnit;
	private JButton setUnit;
	private List<MeasurementUnit> unitsList;
	private JTextField packetNumber;
	private JButton importButton;
	private JLabel syncLabel;
	private FieldGenerated fieldGenerated;
	private Bulk bulk;
	private int uniqueRowCounter;
	private boolean autoUpdate;
	private Map<String, Integer> stocknamePacketcount;
	private Harvesting harvesting;
	private JProgressBar progress;
	private List<Integer> deletedPakcets;
	private TreeMap<String, List<ArrayList<String>>> stockCompositionByMate = new TreeMap<String, List<ArrayList<String>>>();
	private TreeMap<String, List<ArrayList<String>>> stockCompositionByBulk = new TreeMap<String, List<ArrayList<String>>>();


	public TableToolBoxPanel getStickerTablePanel() {
		return stickerTablePanel;
	}

	public void setStickerTablePanel(TableToolBoxPanel stickerTablePanel) {
		this.stickerTablePanel = stickerTablePanel;
	}

	public FieldGenerated getFieldGenerated() {
		return fieldGenerated;
	}

	public void setFieldGenerated(FieldGenerated fieldGenerated) {
		this.fieldGenerated = fieldGenerated;
	}

	public Bulk getBulk() {
		return bulk;
	}

	public void setBulk(Bulk bulk) {
		this.bulk = bulk;
	}

	public List<Integer> getDeletedPakcets() {
		return deletedPakcets;
	}

	public void setDeletedPakcets(List<Integer> deletedPakcets) {
		this.deletedPakcets = deletedPakcets;
	}

	public TreeMap<String, List<ArrayList<String>>> getStockCompositionByMate() {
		return stockCompositionByMate;
	}

	public void setStockCompositionByMate(TreeMap<String, List<ArrayList<String>>> stockCompositionByMate) {
		this.stockCompositionByMate = stockCompositionByMate;
	}

	public TreeMap<String, List<ArrayList<String>>> getStockCompositionByBulk() {
		return stockCompositionByBulk;
	}

	public void setStockCompositionByBulk(TreeMap<String, List<ArrayList<String>>> stockCompositionByBulk) {
		this.stockCompositionByBulk = stockCompositionByBulk;
	}

	public JButton getImportButton() {
		return importButton;
	}


	public void initialize() {
		setLayout(new MigLayout("insets 5, gap 5"));
		stocknamePacketcount = new HashMap<String, Integer>();
		deletedPakcets = new ArrayList<Integer>();
		progress = new JProgressBar(0, 100);
		progress.setVisible(false);
		add(getButtonsPanel(), "w 100%, wrap");
		addListeners();
		add(stickerTablePanel, "w 100%, h 100%, wrap");
		add(progress, "w 100%, hidemode 3, wrap");
		stickerTablePanel.getTable().getTableHeader().setReorderingAllowed(false);
		setExportButtonsPanel();
		addTableLiseners();
		stickerTablePanel.getTable().hideColumn(ColumnConstants.UNIT_ID);
		stickerTablePanel.getTable().hideColumn(ColumnConstants.UNIQUE_ID);
		stickerTablePanel.getTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	private void addTableLiseners(){
		stickerTablePanel.getTable().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					CheckBoxIndexColumnTable table = (CheckBoxIndexColumnTable)e.getSource();
					int row = table.getSelectedRow();
					String selectedStockName = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.STOCK_NAME)));
					List<ArrayList<String>> compositions = new ArrayList<ArrayList<String>>();
					String type = "";
					if(getStockCompositionByMate().containsKey(selectedStockName)){
						compositions = getStockCompositionByMate().get(selectedStockName);
						type = "mate";
					}else  if(getStockCompositionByBulk().containsKey(selectedStockName)){
						compositions = getStockCompositionByBulk().get(selectedStockName);
						type = "bulk";			        	 
					}

					//create table panel
					JPanel stockCompositionPanel = new JPanel(new MigLayout("insets 0 0 0 0"));
					CheckBoxIndexColumnTable compositionTable = new CheckBoxIndexColumnTable();
					compositionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					compositionTable.setEnabled(false);
					DefaultTableModel model =  new DefaultTableModel();
					if(type.equals("mate")){
						ArrayList<String> columnNames = new ArrayList<String>();
						columnNames.add(ColumnConstants.SELECT);
						columnNames.add(ColumnConstants.STOCK_NAME);
						columnNames.add("Parent Tag Name");
						columnNames.add("Mate Type");
						columnNames.add("Mate Role");
						columnNames.add("Accession");
						columnNames.add("Pedigree");
						columnNames.add("Generation");
						model.setColumnIdentifiers(columnNames.toArray());
						for(ArrayList<String> list : compositions){
							Object[] newRow = new Object[8];
							newRow[0] = new Boolean(false);
							newRow[1] = selectedStockName;
							newRow[2] = list.get(1);
							newRow[3] = list.get(4);
							newRow[4] = list.get(5);
							newRow[5] = list.get(6);
							newRow[6] = list.get(7);
							newRow[7] = list.get(8);
							model.addRow(newRow);
						}		        	 

					}else if(type.equals("bulk")){
						ArrayList<String> columnNames = new ArrayList<String>();
						columnNames.add(ColumnConstants.SELECT);
						columnNames.add(ColumnConstants.STOCK_NAME);
						columnNames.add("Mix From_Stock Name");
						columnNames.add(ColumnConstants.QUANTITY);
						columnNames.add(" Unit ");
						model.setColumnIdentifiers(columnNames.toArray());
						for(ArrayList<String> list : compositions){
							Object[] newRow = new Object[7];
							newRow[0] = new Boolean(false);
							newRow[1] = selectedStockName;
							newRow[2] = list.get(1);
							newRow[3] = list.get(2);
							System.out.println("??"+list.get(2));
							newRow[4] = list.get(4);
							newRow[5] = list.get(5);
							newRow[6] = list.get(6);
							model.addRow(newRow);
						}		

					}

					compositionTable.setModel(model);
					if(compositionTable.getRowCount()>0)
					{
						compositionTable.hideColumn(ColumnConstants.SELECT);	
						JScrollPane tableSP = new JScrollPane(compositionTable);
						compositionTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
						int width = 0;
						DefaultTableColumnModel colModel = (DefaultTableColumnModel) compositionTable.getColumnModel();
						for (int i = 0; i < compositionTable.getColumnCount(); i++) {
							AdjustTableColumnSize.adjustColumnSize(compositionTable, i, 5);
							TableColumn col = colModel.getColumn(i);
							width = width + col.getWidth();
						}
						stockCompositionPanel.add(tableSP,"span, h 100%, w 100%");
						stockCompositionPanel.setPreferredSize(new Dimension(width + 2, 300));
						JOptionPane.showConfirmDialog(stickerTablePanel, stockCompositionPanel, "Stock Composition", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);

					}

				}
			}
		});
	}
	private void addListeners() {
		final CheckBoxIndexColumnTable table = getStickerTablePanel().getTable();
		Utils.removeAllRowsFromTable((DefaultTableModel) table.getModel());
		dateApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int column = table.getIndexOf(ColumnConstants.DATE);
				Date date = calendar.getCustomDateCalendar().getDate();
				for(int row: table.getSelectedRows()) {
					table.setValueAt(date, row, column);
					table.setValueAt(true, row, table.getIndexOf(ColumnConstants.MODIFIED));
				}					
			}
		});
		setQuantity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int column = table.getIndexOf(ColumnConstants.QUANTITY);
				int count = Integer.parseInt(quantity.getText());
				for(int row: table.getSelectedRows()) {
					table.setValueAt(count, row, column);
					table.setValueAt(true, row, table.getIndexOf(ColumnConstants.MODIFIED));
				}					
			}
		});
		setUnit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String unit = null;
				Integer unitId = null;
				if(mixUnit.getSelectedIndex() == 0) {
					unit = StringUtils.EMPTY;
					unitId = null;
				}
				else {
					unit = String.valueOf( mixUnit.getSelectedItem() );
					unitId  = unitsList.get(mixUnit.getSelectedIndex() - 1).getMeasurementUnitId();
				}
				for(int row:table.getSelectedRows()) {
					table.setValueAt(unit, row, table.getIndexOf(ColumnConstants.UNIT));
					table.setValueAt(unitId, row, table.getIndexOf(ColumnConstants.UNIT_ID));
					table.setValueAt(true, row, table.getIndexOf(ColumnConstants.MODIFIED));
				}

			}			
		});
		setPacketNumber.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int column = table.getIndexOf(ColumnConstants.PACKET_COUNT);
				int count = Integer.parseInt(packetNumber.getText());

				autoUpdate = true;
				int countIndex = 0 ;
				for(int row: table.getSelectedRows()) {
					row = row + countIndex;
					table.setValueAt(count, row, column);
					table.setValueAt(true, row, table.getIndexOf(ColumnConstants.MODIFIED));					

					String stockname = String.valueOf( table.getValueAt(row, ColumnConstants.STOCK_NAME));
					int newCount = (Integer) table.getValueAt(row, ColumnConstants.PACKET_COUNT);
					autoUpdate = true;
					stocknamePacketcount.put(stockname, newCount);
					ArrayList<Integer> packetRows = new ArrayList<Integer>();
					for(int rowCounter=0; rowCounter<table.getRowCount(); rowCounter++) {
						if(String.valueOf( table.getValueAt(rowCounter, ColumnConstants.STOCK_NAME)) == stockname) {
							table.setValueAt(newCount, rowCounter, table.getIndexOf(ColumnConstants.PACKET_COUNT));
							packetRows.add(rowCounter);
						}
					}
					Collections.sort(packetRows);
					if(packetRows.size() > newCount){
						for(int rowCounter = packetRows.size()-1; rowCounter >= newCount;rowCounter--){
							int deleteRow = packetRows.get(rowCounter);
							((DefaultTableModel) table.getModel()).removeRow(table.convertRowIndexToModel(deleteRow));
						}
					}
					else if (packetRows.size() < newCount){
						Object rowData[] = new Object[table.getColumnCount()];
						rowData[table.getIndexOf(ColumnConstants.SELECT)] = false;
						rowData[table.getIndexOf(ColumnConstants.ROW_NUM)] = String.valueOf(uniqueRowCounter++);
						rowData[table.getIndexOf(ColumnConstants.STOCK_NAME)] = table.getValueAt(packetRows.get(0), table.getIndexOf(ColumnConstants.STOCK_NAME));
						rowData[table.getIndexOf(ColumnConstants.ACCESSION)] = table.getValueAt(packetRows.get(0), table.getIndexOf(ColumnConstants.ACCESSION));				
						rowData[table.getIndexOf(ColumnConstants.PEDIGREE)] = table.getValueAt(packetRows.get(0), table.getIndexOf(ColumnConstants.PEDIGREE));
						rowData[table.getIndexOf(ColumnConstants.GENERATION)] = table.getValueAt(packetRows.get(0), table.getIndexOf(ColumnConstants.GENERATION));;
						rowData[table.getIndexOf(ColumnConstants.MATING_TYPE)] = table.getValueAt(packetRows.get(0), table.getIndexOf(ColumnConstants.MATING_TYPE));
						rowData[table.getIndexOf(ColumnConstants.PACKET_COUNT)] = table.getValueAt(packetRows.get(0), table.getIndexOf(ColumnConstants.PACKET_COUNT));

						Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
						rowData[table.getIndexOf(ColumnConstants.DATE)] = calendar.getTime();
						rowData[table.getIndexOf(ColumnConstants.MODIFIED)] = true;
						rowData[table.getIndexOf(ColumnConstants.PACKET_ID)] = -1;
						for(int i= newCount; i > packetRows.size(); --i){
							rowData[table.getIndexOf(ColumnConstants.PACKET_NAME)] = rowData[table.getIndexOf(ColumnConstants.STOCK_NAME)] + "#" + String.valueOf(i);
							rowData[table.getIndexOf(ColumnConstants.PACKET_NUMBER)] = i;
							((DefaultTableModel) table.getModel()).insertRow(table.convertRowIndexToModel(row)+1, rowData);
						}
					}
					countIndex += (newCount - packetRows.size());
					autoUpdate = false;
				}
				table.clearSelection();
			}
		});
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showPopup();
			}
		});
		table.getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent event) {
				if(autoUpdate) {
					return;
				}
				if(event.getColumn() == table.getIndexOf(ColumnConstants.QUANTITY) || event.getColumn() == table.getIndexOf(ColumnConstants.COMMENT)) {
					int row = event.getFirstRow();
					table.setValueAt(true, row, table.getIndexOf(ColumnConstants.MODIFIED));
				}
				autoUpdate = false;
				stickerTablePanel.getNumberOfRows().setText(String.valueOf(stickerTablePanel.getTable().getRowCount()));
			}			
		});
		getStickerTablePanel().getRefreshButton().setToolTipText("sync with database");
		getStickerTablePanel().getRefreshButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new SyncPackets(harvesting).execute();
			}
		});
		getStickerTablePanel().getDeleteButton().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int rows[] = table.getSelectedRows();
				DefaultTableModel model = (DefaultTableModel)table.getModel();
				for(int rowCounter=0;rowCounter<rows.length;rowCounter++){
					String stockName = String.valueOf(table.getValueAt(rows[rowCounter]-rowCounter, table.getIndexOf(ColumnConstants.STOCK_NAME)));
					stocknamePacketcount.remove(stockName);

					// when rows are sorted, need to convert to model index
					int deleteRow = rows[rowCounter]-rowCounter;	
					getStickerTablePanel().getTable().getCheckedRows().remove(deleteRow);
					model.removeRow(getStickerTablePanel().getTable().convertRowIndexToModel(deleteRow));

				}				
				table.clearSelection();
			}

		});
	}

	private void showPopup() {
		TableToolBoxPanel thisPlantingPanel = getThisPlantingPanel("popup", true);
		thisPlantingPanel.getTable().hideColumn(ColumnConstants.ROW_NUM);
		thisPlantingPanel.setBorder(BorderFactory.createTitledBorder("Stocks from this planting: "));

		TableToolBoxPanel mixedStocksPanel = getMixedStocksPanel("popup");
		mixedStocksPanel.setBorder(BorderFactory.createTitledBorder("Stocks from mixing: "));

		JPanel popupPanel = new JPanel(new MigLayout("insets 0, gapx 5"));

		popupPanel.add(thisPlantingPanel, "w 50%, h 100%");
		popupPanel.add(mixedStocksPanel, " w 50%, h 100%");
		thisPlantingPanel.getTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		mixedStocksPanel.getTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		popupPanel.setSize(this.getSize());
		int option = JOptionPane.showConfirmDialog(this, popupPanel, "Choose stocks", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if(option == JOptionPane.OK_OPTION) {
			CheckBoxIndexColumnTable stickerTable = stickerTablePanel.getTable();
			DefaultTableModel model = (DefaultTableModel) stickerTablePanel.getTable().getModel();
			CheckBoxIndexColumnTable plantingTable = thisPlantingPanel.getTable();
			for(int row : plantingTable.getSelectedRows()) {
				int packet_number = 1;
				String stockName = (String) plantingTable.getValueAt(row, ColumnConstants.TAG_NAME);
				if(stocknamePacketcount.containsKey(stockName)) {
					packet_number = stocknamePacketcount.get(stockName) + 1;

				}
				Object rowData[] = new Object[stickerTable.getColumnCount()];
				rowData[stickerTable.getIndexOf(ColumnConstants.SELECT)] = false;
				rowData[stickerTable.getIndexOf(ColumnConstants.ROW_NUM)] = String.valueOf(uniqueRowCounter++);
				rowData[stickerTable.getIndexOf(ColumnConstants.STOCK_NAME)] = stockName;
				rowData[stickerTable.getIndexOf(ColumnConstants.PACKET_NAME)] = stockName + "#" + String.valueOf(packet_number);
				rowData[stickerTable.getIndexOf(ColumnConstants.ACCESSION)] = plantingTable.getValueAt(row, plantingTable.getIndexOf(ColumnConstants.ACCESSION));				
				rowData[stickerTable.getIndexOf(ColumnConstants.PEDIGREE)] = plantingTable.getValueAt(row, plantingTable.getIndexOf(ColumnConstants.PEDIGREE));
				rowData[stickerTable.getIndexOf(ColumnConstants.GENERATION)] = plantingTable.getValueAt(row, plantingTable.getIndexOf(ColumnConstants.GENERATION));
				rowData[stickerTable.getIndexOf(ColumnConstants.MATING_TYPE)] = plantingTable.getValueAt(row, plantingTable.getIndexOf(ColumnConstants.MATING_TYPE));
				rowData[stickerTable.getIndexOf(ColumnConstants.PACKET_COUNT)] = 1;
				rowData[stickerTable.getIndexOf(ColumnConstants.PACKET_NUMBER)] = packet_number;
				Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
				rowData[stickerTable.getIndexOf(ColumnConstants.DATE)] = calendar.getTime();
				rowData[stickerTable.getIndexOf(ColumnConstants.MODIFIED)] = true;
				HarvestingDataModel rowDataModel = new HarvestingDataModel(rowData,stickerTable.getColumnModel());
				rowData[stickerTable.getIndexOf(ColumnConstants.UNIQUE_ID)] = rowDataModel.getUniqueId();
				rowData[stickerTable.getIndexOf(ColumnConstants.PACKET_ID)] = -1;
				model.addRow(rowData);
				stocknamePacketcount.put(stockName, packet_number);
			}
			CheckBoxIndexColumnTable mixedTable = mixedStocksPanel.getTable();
			for(int row : mixedTable.getSelectedRows()) {
				String stockName = (String) mixedTable.getValueAt(row, ColumnConstants.STOCK_NAME);
				int packet_number = 1;
				if(stocknamePacketcount.containsKey(stockName)) {
					packet_number = stocknamePacketcount.get(stockName) + 1;			
				}
				Object rowData[] = new Object[stickerTable.getColumnCount()];
				rowData[stickerTable.getIndexOf(ColumnConstants.SELECT)] = false;
				rowData[stickerTable.getIndexOf(ColumnConstants.ROW_NUM)] = String.valueOf(uniqueRowCounter++);
				rowData[stickerTable.getIndexOf(ColumnConstants.STOCK_NAME)] = stockName;
				rowData[stickerTable.getIndexOf(ColumnConstants.PACKET_NAME)] = stockName + "#" + String.valueOf(packet_number);
				rowData[stickerTable.getIndexOf(ColumnConstants.ACCESSION)] = (String) mixedTable.getValueAt(row, ColumnConstants.ACCESSION);
				rowData[stickerTable.getIndexOf(ColumnConstants.PEDIGREE)] = (String) mixedTable.getValueAt(row, ColumnConstants.PEDIGREE);
				rowData[stickerTable.getIndexOf(ColumnConstants.GENERATION)] = "NA";
				rowData[stickerTable.getIndexOf(ColumnConstants.MATING_TYPE)] = "NA";
				rowData[stickerTable.getIndexOf(ColumnConstants.PACKET_COUNT)] = 1;
				rowData[stickerTable.getIndexOf(ColumnConstants.PACKET_NUMBER)] = packet_number;
				Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
				rowData[stickerTable.getIndexOf(ColumnConstants.DATE)] = calendar.getTime();
				rowData[stickerTable.getIndexOf(ColumnConstants.MODIFIED)] = true;
				HarvestingDataModel rowDataModel = new HarvestingDataModel(rowData,stickerTable.getColumnModel());
				rowData[stickerTable.getIndexOf(ColumnConstants.UNIQUE_ID)] = rowDataModel.getUniqueId();
				rowData[stickerTable.getIndexOf(ColumnConstants.PACKET_ID)] = -1;
				model.addRow(rowData);
				stocknamePacketcount.put(stockName, packet_number);
			}
			stickerTablePanel.getTable().setModel(model);
		}


	}

	private TableToolBoxPanel getMixedStocksPanel(String str) {
		List<String> columnNames = new ArrayList<String>();
		columnNames.add(ColumnConstants.SELECT);
		columnNames.add(ColumnConstants.MIX_ID);
		columnNames.add(ColumnConstants.STOCK_NAME);				
		columnNames.add(ColumnConstants.ACCESSION);
		columnNames.add(ColumnConstants.PEDIGREE);

		BeanDefinitionBuilder thisPlantingStocksTableDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(CheckBoxIndexColumnTable.class)
				.addPropertyValue("columnNames", columnNames)
				.addPropertyValue("checkBoxHeader", new Boolean(false))
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("thisMixedStocksTable"+str, thisPlantingStocksTableDefinitionBuilder.getBeanDefinition());

		BeanDefinitionBuilder thisPlantingStocksPanelDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(TableToolBoxPanel.class)
				.addPropertyValue("table", getContext().getBean("thisMixedStocksTable"+str))
				.setInitMethodName("initialize");

		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("thisMixedStocksPanel"+str, thisPlantingStocksPanelDefinitionBuilder.getBeanDefinition());

		CheckBoxIndexColumnTable table = (CheckBoxIndexColumnTable)getContext().getBean("thisMixedStocksTable"+str);
		Utils.removeAllRowsFromTable((DefaultTableModel)table.getModel());

		CheckBoxIndexColumnTable bulkTable = bulk.getBulkTablePanel().getTable();
		Set<String> alreadyAdded = new HashSet<String>();
		for(int row=0; row<bulkTable.getRowCount(); row++) {
			String finalStockName = (String)bulkTable.getValueAt(row, ColumnConstants.FINAL_STOCK_NAME);
			if(StringUtils.isNotBlank(finalStockName)
					&& !alreadyAdded.contains(finalStockName)) {
				alreadyAdded.add(finalStockName);
				Object rowData[] = new Object[table.getColumnCount()];
				rowData[table.getIndexOf(ColumnConstants.STOCK_NAME)] = finalStockName;
				rowData[table.getIndexOf(ColumnConstants.ACCESSION)] = 
						bulkTable.getValueAt(row, ColumnConstants.ACCESSION);
				rowData[table.getIndexOf(ColumnConstants.PEDIGREE)] = 
						bulkTable.getValueAt(row, ColumnConstants.PEDIGREE);
				rowData[table.getIndexOf(ColumnConstants.MIX_ID)] = 
						bulkTable.getValueAt(row, ColumnConstants.MIX_ID);
				((DefaultTableModel)table.getModel()).addRow(rowData);
			}
		}
		return (TableToolBoxPanel) getContext().getBean("thisMixedStocksPanel"+str);
	}


	public String getSavedState(){
		//String file = "state.txt";
		String state = "DE";
		state = JOptionPane.showInputDialog("Please input State code for accession generation");
		if (state == null || state.trim().equals("")){
			state = "DE";
		}else{
			state = state.toUpperCase();
		}
		try {			
			/*File keyFile = new File(file);
			//if(!keyFile.exists()){
				// ask state
				FileWriter wr = new FileWriter(file);
				BufferedWriter writer = new BufferedWriter(wr);
				writer.write(state);
				writer.close();
				wr.close();
			}else{
				FileReader fr = new FileReader(file);
				BufferedReader reader = new BufferedReader(fr);
				state = reader.readLine();
				reader.close();
				fr.close();
			}*/	
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			state = JOptionPane.showInputDialog("Please input State code, default is DE");
		}
		return state;
	}
	public ArrayList<String> generateAccessions(String state, String year, int count){
		ArrayList<String> accessions = new ArrayList<String>();
		for(int i = 0; i < count; ++i ){
			String accession = 	state+year+Gpw.generate(5,5);	
			accessions.add(accession);
		}
		while (true){
			ArrayList<String> existsAcces =  (ArrayList<String>) PassportDAO.getInstance().findAccessions(accessions);
			if(existsAcces.size() == 0) break;
			for(String s : existsAcces){
				accessions.remove(s);
				accessions.add(	state+year+Gpw.generate(5,5));
			}
		}
		return accessions;

	}

	public TableToolBoxPanel getThisPlantingPanel(String str, boolean genNewAccs) {

		List<String> columnNames = new ArrayList<String>();
		columnNames.add(ColumnConstants.SELECT);
		columnNames.add(ColumnConstants.ROW_NUM);
		columnNames.add(ColumnConstants.TAG_ID);
		columnNames.add(ColumnConstants.TAG_NAME);				
		columnNames.add(ColumnConstants.ACCESSION);
		columnNames.add(ColumnConstants.MATING_TYPE);
		columnNames.add(ColumnConstants.PEDIGREE);
		columnNames.add(ColumnConstants.GENERATION);
		columnNames.add(ColumnConstants.MATE_LINK);


		BeanDefinitionBuilder thisPlantingStocksTableDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(CheckBoxIndexColumnTable.class)
				.addPropertyValue("columnNames", columnNames)
				.addPropertyValue("checkBoxHeader", new Boolean(false))
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("thisPlantingStocksTable"+str, thisPlantingStocksTableDefinitionBuilder.getBeanDefinition());

		BeanDefinitionBuilder thisPlantingStocksPanelDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(TableToolBoxPanel.class)
				.addPropertyValue("table", getContext().getBean("thisPlantingStocksTable"+str))
				.setInitMethodName("initialize");

		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("thisPlantingStocksPanel"+str, thisPlantingStocksPanelDefinitionBuilder.getBeanDefinition());

		CheckBoxIndexColumnTable table = (CheckBoxIndexColumnTable)getContext().getBean("thisPlantingStocksTable"+str);
		Utils.removeAllRowsFromTable((DefaultTableModel)table.getModel());

		CheckBoxIndexColumnTable fieldTable = fieldGenerated.getCrossingTablePanel().getTable();
		ArrayList<String> accessions = new ArrayList<String>();
		if(genNewAccs){
			String state = getSavedState();
			String year = fieldTable.getValueAt(0, ColumnConstants.TAG_NAME).toString().split("\\.")[0];
			accessions = generateAccessions(state, year,fieldTable.getRowCount());
		}
		for(int row=0; row<fieldTable.getRowCount(); row++) {
			// SB, PP, SF
			if(!"CR".equals(fieldTable.getValueAt(row, ColumnConstants.MATING_TYPE))
					&& !"BC".equals(fieldTable.getValueAt(row, ColumnConstants.MATING_TYPE))){
				if("F".equals(fieldTable.getValueAt(row, ColumnConstants.ROLE))
						&& fieldTable.getValueAt(row, ColumnConstants.STOCK_NAME) != null
						&& !"".equals(fieldTable.getValueAt(row, ColumnConstants.STOCK_NAME))) {
					Object rowData[] = new Object[table.getColumnCount()];
					rowData[table.getIndexOf(ColumnConstants.SELECT)] = new Boolean(false);
					rowData[table.getIndexOf(ColumnConstants.ROW_NUM)] = row;
					rowData[table.getIndexOf(ColumnConstants.TAG_NAME)] = 
							fieldTable.getValueAt(row, ColumnConstants.STOCK_NAME);
					rowData[table.getIndexOf(ColumnConstants.MATING_TYPE)] = 
							fieldTable.getValueAt(row, ColumnConstants.MATING_TYPE);
					String femaleAccession = String.valueOf(fieldTable.getValueAt(row, ColumnConstants.ACCESSION));
					String femalePedigree = String.valueOf(fieldTable.getValueAt(row, ColumnConstants.PEDIGREE));
					String femaleGeneration = String.valueOf(fieldTable.getValueAt(row, ColumnConstants.GENERATION));
					String matingTpye = String.valueOf(fieldTable.getValueAt(row, ColumnConstants.MATING_TYPE));
					if(matingTpye.equals("PP") && genNewAccs){
						rowData[table.getIndexOf(ColumnConstants.ACCESSION)] = accessions.get(row);
					}else{
						rowData[table.getIndexOf(ColumnConstants.ACCESSION)] = 
								fieldTable.getValueAt(row, ColumnConstants.ACCESSION);
					}
					Boolean selectionMade = (Boolean) fieldTable.getValueAt(row, fieldTable.getIndexOf(ColumnConstants.SELECTION));
					PedigreeGenerationAutomation p = null;
					try{
						p = new PedigreeGenerationAutomation(femalePedigree,"",femaleAccession, "", matingTpye,femaleGeneration,selectionMade);	
					}catch( Exception e){
						System.out.println("ERRORRRR?");
					}
					rowData[table.getIndexOf(ColumnConstants.PEDIGREE)] = p.childPedigree ;
					rowData[table.getIndexOf(ColumnConstants.GENERATION)] = p.childGeneration;				
					rowData[table.getIndexOf(ColumnConstants.TAG_ID)] = 
							fieldTable.getValueAt(row, ColumnConstants.TAG_ID);
					rowData[table.getIndexOf(ColumnConstants.MATE_LINK)] = 
							fieldTable.getValueAt(row, ColumnConstants.MATE_LINK);
					((DefaultTableModel)table.getModel()).addRow(rowData);
				}
			}else{
				// CR, BC
				if("F".equals(fieldTable.getValueAt(row, ColumnConstants.ROLE))
						&& fieldTable.getValueAt(row, ColumnConstants.STOCK_NAME) != null
						&& !"".equals(fieldTable.getValueAt(row, ColumnConstants.STOCK_NAME))) {
					Object rowData[] = new Object[table.getColumnCount()];
					rowData[table.getIndexOf(ColumnConstants.SELECT)] = new Boolean(false);
					rowData[table.getIndexOf(ColumnConstants.ROW_NUM)] = row;
					String femaleStock = String.valueOf(fieldTable.getValueAt(row,fieldTable.getIndexOf(ColumnConstants.STOCK_NAME)));
					rowData[table.getIndexOf(ColumnConstants.TAG_NAME)] = femaleStock;
					if(genNewAccs){
						rowData[table.getIndexOf(ColumnConstants.ACCESSION)] = accessions.get(row);
					}
					rowData[table.getIndexOf(ColumnConstants.MATING_TYPE)] = 
							fieldTable.getValueAt(row, ColumnConstants.MATING_TYPE);
					rowData[table.getIndexOf(ColumnConstants.TAG_ID)] = 
							fieldTable.getValueAt(row, ColumnConstants.TAG_ID);
					rowData[table.getIndexOf(ColumnConstants.MATE_LINK)] = 
							fieldTable.getValueAt(row, ColumnConstants.MATE_LINK);
					//System.out.println(getStockCompositionByMate());
					List<ArrayList<String>> compositions = getStockCompositionByMate().get(femaleStock);
					String malePedigree = "";
					String maleAccession = "";
					for(ArrayList<String> composition : compositions){
						if(composition.get(5).equals("M")){
							maleAccession = composition.get(6);
							malePedigree  = composition.get(7);
						}
					}
					if ( malePedigree.equals("")){
						System.out.println("SOMETHING WENT WRONG");
					}
					String femaleAccession = String.valueOf(fieldTable.getValueAt(row, ColumnConstants.ACCESSION));
					String femalePedigree = String.valueOf(fieldTable.getValueAt(row, ColumnConstants.PEDIGREE));
					String femaleGeneration = String.valueOf(fieldTable.getValueAt(row, ColumnConstants.GENERATION));
					String matingTpye = String.valueOf(fieldTable.getValueAt(row, ColumnConstants.MATING_TYPE));
					Boolean selectionMade = (Boolean) fieldTable.getValueAt(row, fieldTable.getIndexOf(ColumnConstants.SELECTION));
					PedigreeGenerationAutomation p = new PedigreeGenerationAutomation(femalePedigree,malePedigree,femaleAccession,maleAccession,matingTpye,femaleGeneration,selectionMade);					
					rowData[table.getIndexOf(ColumnConstants.PEDIGREE)] = p.childPedigree ;
					rowData[table.getIndexOf(ColumnConstants.GENERATION)] = p.childGeneration;	
					((DefaultTableModel)table.getModel()).addRow(rowData);
				}
			}

		}

		return (TableToolBoxPanel) getContext().getBean("thisPlantingStocksPanel"+str);
	}

	private JPanel getButtonsPanel() {
		JPanel buttonsPanel = new JPanel(new MigLayout("insets 0, gapx 5"));

		importButton = new JButton("Import");
		importButton.setToolTipText("Choose stocks for storing");
		buttonsPanel.add(importButton);

		calendar = new CustomCalendar();
		dateApply = new JButton("Set");
		dateApply.setToolTipText("Set date for selected stocks, default is today.");

		quantity = new JTextField(10);
		setQuantity = new JButton("Set");
		setQuantity.setToolTipText("Set quantity for selected stocks");

		populateUnits();
		setUnit = new JButton("Set");
		setUnit.setToolTipText("Set the quantity for selected stocks");

		packetNumber = new JTextField(10);
		setPacketNumber = new JButton("Set");
		setPacketNumber.setToolTipText("Set sticker count for selected stocks");

		syncLabel = new JLabel("<HTML><FONT color = #B80000>Not synced with database</FONT></HTML>");

		buttonsPanel.add(new JLabel("Date:"));
		buttonsPanel.add(calendar.getCustomDateCalendar());
		buttonsPanel.add(dateApply);
		buttonsPanel.add(new JLabel("Quantity:"));
		buttonsPanel.add(quantity);
		buttonsPanel.add(setQuantity);
		buttonsPanel.add(new JLabel("Unit: "));
		buttonsPanel.add(mixUnit);
		buttonsPanel.add(setUnit);
		buttonsPanel.add(new JLabel("Packet count:"));
		buttonsPanel.add(packetNumber);
		buttonsPanel.add(setPacketNumber, "pushx");
		//		buttonsPanel.add(syncLabel, "wrap");
		return buttonsPanel;
	}

	private void setExportButtonsPanel() {
		JPanel exportButtonsPanel = stickerTablePanel.getBottomHorizonPanel();
		JButton printStickers = new JButton("Print Stickers");
		JButton exportButton = new JButton("Export");
		JButton saveButton = new JButton("Save");

		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				try {
					saveTableToFile(stickerTablePanel.getTable(), StickerGenerator.this, "stickerTable.csv");
				} catch (Exception e) {
					if (LoggerUtils.isLogEnabled()) {
						LoggerUtils.log(Level.INFO,  e.toString());
					}
					JLabel errorFields = new JLabel(
							"<HTML><FONT COLOR = Blue>Failed to create file. File might be already open or<br>you do not have permission to write in that folder </FONT></HTML>");
					JOptionPane.showMessageDialog(StickerGenerator.this, errorFields);					
				}
			}
		});

		printStickers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");				
				Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
				String fileName = dateFormat.format(calendar.getTime())+ "PacketStickers";				
				String extension = ".csv";				
				File finalExportFile = new File(fileName + extension);
				JFileChooser fileChooser = new JFileChooser();
				File file = new File(System.getProperty("user.home")+"/"+finalExportFile);
				fileChooser.setSelectedFile(file);
				int approve = fileChooser.showSaveDialog(StickerGenerator.this);
				if (approve != JFileChooser.APPROVE_OPTION) {
					return;
				}
				file = fileChooser.getSelectedFile();
				try {
					BufferedWriter bw = new BufferedWriter(new FileWriter(file));
					CheckBoxIndexColumnTable table = stickerTablePanel.getTable();
					String[] headers = {"Packet name", "stock name", "pedigree", "accession",
							"date", "generation", "mating type", "quantity", "note"};
					for (String header : headers){
						bw.write(header);
						bw.write(",");
					}
					bw.newLine();
					for(int row = 0; row < table.getRowCount(); row++) {
						int count = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.PACKET_COUNT)));
						for(int counter=0; counter<count; counter++) {
							//Packet name; stock name; pedigree, accession; date; generation; mating type; quantity; note (comment)
							bw.write(String.valueOf(table.getValueAt(row, ColumnConstants.PACKET_NAME))+",");
							bw.write(String.valueOf(table.getValueAt(row, ColumnConstants.STOCK_NAME))+",");
							bw.write(String.valueOf(table.getValueAt(row, ColumnConstants.PEDIGREE))+",");
							bw.write(String.valueOf(table.getValueAt(row, ColumnConstants.ACCESSION))+",");
							Date date = null;
							try{
								date = (Date) table.getValueAt(row, ColumnConstants.DATE);
							}catch(Exception e) {
								DateFormat dateFormatRead = new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy",Locale.getDefault());		
								date = new Date(dateFormatRead.parse(String.valueOf(table.getValueAt(row, ColumnConstants.DATE))).getTime());
							}
							String value = String.valueOf(date.getMonth()+1)+"/"+String.valueOf(date.getDate())+
									"/"+String.valueOf(date.getYear()+1900);
							bw.write(value+",");
							bw.write(String.valueOf(table.getValueAt(row, ColumnConstants.GENERATION))+",");
							bw.write(String.valueOf(table.getValueAt(row, ColumnConstants.MATING_TYPE))+",");
							bw.write(String.valueOf(table.getValueAt(row, ColumnConstants.QUANTITY))+",");
							bw.newLine();
						}
					}
					bw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		exportButtonsPanel.add(saveButton,"pushx, al right");
		exportButtonsPanel.add(exportButton);
		exportButtonsPanel.add(printStickers);
	}

	private void populateUnits() {
		if(mixUnit == null)
		{
			mixUnit = new JComboBox();
		}
		unitsList = MeasurementUnitDAO.getInstance().findAll();
		String[] unitValues = new String[unitsList.size()+2];
		unitValues[0] = "Select quantity unit";
		for(int counter=0; counter<unitsList.size(); counter++) {
			MeasurementUnit unit = unitsList.get(counter);
			unitValues[counter+1] = unit.getUnitType();
		}
		unitValues[unitValues.length-1] = "Add new..";
		mixUnit.setModel(new DefaultComboBoxModel(unitValues));
		mixUnit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(mixUnit.getSelectedIndex() == mixUnit.getItemCount()-1) {
					mixUnit.setSelectedIndex(0);
					JPanel newUnitPanel = new JPanel(new MigLayout("insets 0, gap 0"));
					JTextField unit = new JTextField(16);
					newUnitPanel.add(new JLabel("Unit: "));
					newUnitPanel.add(unit, "wrap");
					boolean valid = false;
					while (!valid) {
						int option = JOptionPane.showConfirmDialog(StickerGenerator.this, newUnitPanel, "Enter New Unit Information ", JOptionPane.OK_CANCEL_OPTION);
						if(option == JOptionPane.OK_OPTION) {
							if(!StringUtils.isBlank(unit.getText())) {
								valid = true;
								MeasurementUnitDAO.getInstance().insertOrUpdate(unit.getText());
								populateUnits();
							}
							else
								JOptionPane.showConfirmDialog(StickerGenerator.this, "<HTML><FONT COLOR = Red>All fields are mandatory.</FONT></HTML>", "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);       
						}
						else break;
					}//while

				}//if last element

			}
		});

	}

	public void setHarvesting(Harvesting harvesting) {
		this.harvesting = harvesting;
	}

	public JProgressBar getProgress() {
		return progress;
	}

	public void finishedSync(boolean rollBacked) {
		progress.setVisible(false);
		if(!rollBacked) {
			getStickerTablePanel().getTable().setHasSynced(true);
		}
	}

	public void populateTableFromObjects(List<Object[]> crossRecordRows) {
		DefaultTableModel tableModel = ((DefaultTableModel)getStickerTablePanel().getTable().getModel());
		removeAllRowsFromTable(tableModel);
		stocknamePacketcount = new HashMap<String, Integer>();
		if(crossRecordRows != null)
		{
			for(Object[] row : crossRecordRows){
				tableModel.addRow(row);               
			}
			CheckBoxIndexColumnTable table = getStickerTablePanel().getTable();
			boolean synced = true;
			for(int row = 0; row < table.getRowCount();++row)    	
			{
				if(String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.MODIFIED))).equals("true")){
					synced = false;
				}
				long uniqueId = String.valueOf(table.getValueAt(row, table.getIndexOf( ColumnConstants.UNIQUE_ID))).equals("null") ? -1 : Long.valueOf(String.valueOf(table.getValueAt(row, table.getIndexOf( ColumnConstants.UNIQUE_ID))));

				String stockName = (String) table.getValueAt(row, ColumnConstants.STOCK_NAME);
				Object rowData[] = new Object[table.getColumnCount()];
				rowData[table.getIndexOf(ColumnConstants.ROW_NUM)] = Integer.parseInt(String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.PACKET_COUNT))));
				rowData[table.getIndexOf(ColumnConstants.STOCK_NAME)] = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.STOCK_NAME)));
				rowData[table.getIndexOf(ColumnConstants.PACKET_NAME)] = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.PACKET_NAME)));
				rowData[table.getIndexOf(ColumnConstants.ACCESSION)] = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.ACCESSION)));
				rowData[table.getIndexOf(ColumnConstants.PEDIGREE)] =String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.PEDIGREE)));
				rowData[table.getIndexOf(ColumnConstants.PACKET_COUNT)] =Integer.parseInt(String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.PACKET_COUNT))));
				rowData[table.getIndexOf(ColumnConstants.PACKET_NUMBER)] = Integer.parseInt(String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.PACKET_NUMBER))));
				DateFormat dateFormatRead = new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy",Locale.getDefault());		
				Date date = null;
				try {
					date = dateFormatRead.parse(String.valueOf(table.getValueAt(row, ColumnConstants.DATE)));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				rowData[table.getIndexOf(ColumnConstants.DATE)] = date;
				rowData[table.getIndexOf(ColumnConstants.MODIFIED)] = new Boolean((Boolean) table.getValueAt(row, table.getIndexOf(ColumnConstants.MODIFIED)));
				HarvestingDataModel rowDataModel = new HarvestingDataModel(rowData,table.getColumnModel());
				rowData[table.getIndexOf(ColumnConstants.UNIQUE_ID)] = uniqueId;
				int packetId = Integer.parseInt(String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.PACKET_ID))));
				rowData[table.getIndexOf(ColumnConstants.PACKET_ID)] = packetId;
				rowDataModel.setUniqueId(uniqueId);
				if(stocknamePacketcount.containsKey(stockName))
				{
					stocknamePacketcount.put(stockName, stocknamePacketcount.get(stockName)+1);
				}
				else
				{
					stocknamePacketcount.put(stockName, 1);
				}
			}
			table.setHasSynced(synced);
		}

	}

	public ArrayList<String> getCreatedStocks(){
		CheckBoxIndexColumnTable table = getStickerTablePanel().getTable();
		ArrayList<String> stocknames = new ArrayList<String>();
		for(int row = 0; row < table.getRowCount();++row)    	
		{
			if(!String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.STOCK_NAME)))
					.equalsIgnoreCase("null")){
				stocknames.add(String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.STOCK_NAME))));	
			}
		}
		return stocknames;		
	}


}
