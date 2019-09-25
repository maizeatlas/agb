package org.accretegb.modules.germplasm.harvesting;

import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.germplasm.planting.PlantingRow;
import org.accretegb.modules.hibernate.MateMethod;
import org.accretegb.modules.hibernate.MateMethodConnect;
import org.accretegb.modules.hibernate.ObservationUnit;
import org.accretegb.modules.hibernate.Stock;
import org.accretegb.modules.hibernate.dao.MateMethodConnectDAO;
import org.accretegb.modules.hibernate.dao.MateMethodDAO;
import org.accretegb.modules.hibernate.dao.ObservationUnitDAO;
import org.accretegb.modules.hibernate.dao.StockDAO;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.util.ChangeMonitor;
import org.accretegb.modules.constants.ColumnConstants;

public class FieldGenerated extends TabComponentPanel {
	
	private TableToolBoxPanel crossingTablePanel;
	private JButton importButton;
	private JButton selfButton;
	private JButton sbButton;
	private JButton poolButton;
	private JButton crButton;
	private JButton bcButton;
	private JButton multiFemaleButton;
	private JButton clearMatingtypeButton;
	private JButton setUnsetSelectionButton;
	private JProgressBar progressBar;
	private JComboBox matingMethod;
	private JButton setMatingMethod;
	private JButton unsetMatingMethod;
	private List<PlantingRow> stockList;
	private HashSet<Integer> tagsCreatedinHarvest = new HashSet<Integer>();
	private int projectID = -1;
	public HashMap<String, Integer> mateMethodtoID = new HashMap<String, Integer>();
	public int rowNum;
	public int nextLink;
	
	public boolean modified = false;
	
	public TableToolBoxPanel getCrossingTablePanel() {
		return crossingTablePanel;
	}

	public void setCrossingTablePanel(TableToolBoxPanel crossingTablePanel) {
		this.crossingTablePanel = crossingTablePanel;
	}
	
	public int getProjectID() {
		return projectID;
	}

	public void setProjectID(int projectID) {
		this.projectID = projectID;
	}

	public void initialize() {
		setLayout(new MigLayout("insets 10, gap 10"));
		setupProgressBar();
		setUpMatingMethodCombo();
		generateImportButton();
		generateButtons();
		addListeners();
		add(getButtonsPanel(), "grow, span, pushx, wrap");
		add(crossingTablePanel, "grow, span, push, wrap");
		add(progressBar, "hidemode 3, growx, spanx, pushx, wrap");
		if(stockList!=null) {
			fillStocks();
		}
		getCrossingTablePanel().getTable().hideColumn(ColumnConstants.QUANTITY);
		getCrossingTablePanel().getTable().hideColumn(ColumnConstants.UNIT);		
	}

	private void fillStocks() {
		CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();
		DefaultTableModel model = (DefaultTableModel) crossingTablePanel.getTable().getModel();
		long startTime = System.currentTimeMillis();
		for(PlantingRow stock : stockList) {
			if(stock.getTagId() > 0) {
				Object rowData[] = new Object[crossingTablePanel.getTable().getColumnCount()];
				rowData[table.getIndexOf(ColumnConstants.SELECT)] = false;
				rowData[table.getIndexOf(ColumnConstants.TAG_ID)] = stock.getTagId();
				rowData[table.getIndexOf(ColumnConstants.TAG_NAME)] = stock.getTag();
				rowData[table.getIndexOf(ColumnConstants.ACCESSION)] = stock.getAccession();
				rowData[table.getIndexOf(ColumnConstants.PEDIGREE)] = stock.getPedigree();
				rowData[table.getIndexOf(ColumnConstants.GENERATION)] = stock.getGeneration();
				rowData[table.getIndexOf(ColumnConstants.ROW_NUM)] = rowNum++;
				rowData[table.getIndexOf(ColumnConstants.SELECTION)] = new Boolean(false);				
				model.addRow(rowData);
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("That took " + (endTime - startTime) + " milliseconds");
	}

	private void setUpMatingMethodCombo() {
		matingMethod = new JComboBox();
		setMatingMethod = new JButton("Set Method");
		unsetMatingMethod = new JButton("Clear");
		refreshMethods();
		matingMethod.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(matingMethod.getSelectedIndex() == matingMethod.getItemCount()-1) {
					matingMethod.setSelectedIndex(0);
					new MateMethodPanel(FieldGenerated.this);
					ChangeMonitor.markAsChanged(projectID);
				}
			}
		});
		setMatingMethod.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();
				int[] selectedRows = table.getSelectedRows();
				HashSet<Integer> mateLinkSet = new HashSet<Integer>();
				int flag = 0;
				for(int row:selectedRows) {
					if(String.valueOf(table.getValueAt(row, ColumnConstants.MATE_LINK)).equalsIgnoreCase("null")){
						flag ++;						
					}else{
						mateLinkSet.add((Integer) table.getValueAt(row, ColumnConstants.MATE_LINK));	
					}					
				}
				if(flag == selectedRows.length){
					JOptionPane.showMessageDialog(null, "Please specify mating type first.");
					return;
				}
				
				String selectedMethod = (String) matingMethod.getSelectedItem();
				for(int i=0; i<table.getRowCount();i++) {
					if(mateLinkSet.contains(table.getValueAt(i, ColumnConstants.MATE_LINK))) {
						table.setValueAt(selectedMethod, i, table.getIndexOf(ColumnConstants.MATE_METHOD));
					}
				}
				ChangeMonitor.markAsChanged(projectID);
			}
		});
		unsetMatingMethod.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();
				int[] selectedRows = table.getSelectedRows();
				HashSet<Integer> mateLinkSet = new HashSet<Integer>();
				for(int row:selectedRows) {
					if(!String.valueOf(table.getValueAt(row, ColumnConstants.MATE_LINK)).equalsIgnoreCase("null")){
						mateLinkSet.add((Integer) table.getValueAt(row, ColumnConstants.MATE_LINK));						
					}
				}
				for(int i=0; i<table.getRowCount();i++) {
					if(mateLinkSet.contains(table.getValueAt(i, ColumnConstants.MATE_LINK))) {
						table.setValueAt(null, i, table.getIndexOf(ColumnConstants.MATE_METHOD));
					}
				}
				ChangeMonitor.markAsChanged(projectID);
			}
		});
	}

	private void setupProgressBar() {
		progressBar = new JProgressBar();
		progressBar.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		progressBar.setVisible(false);
		progressBar.setIndeterminate(true);
	}
	
	private void SFOrSB(String type){
		final CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();
		modified = true;
		int[] selectedRows = table.getSelectedRows();
		HashSet<String> tagsUsed = tagsUsedOtherGroup();
		HashSet<String> reuseTags = new HashSet<String>();
		for(int row:selectedRows) {
			if(tagsUsed.contains((String) table.getValueAt(row, ColumnConstants.TAG_NAME))){
				reuseTags.add((String) table.getValueAt(row, ColumnConstants.TAG_NAME));
			}
			
		}
		
		if(reuseTags.size() > 0 && !shouldReuseTags()) {
			table.clearSelection();
			setButtonsUsability();
			return;
		}
		
		for(int row:selectedRows) {				
			row = table.convertRowIndexToModel(row);
			String tagname = (String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.TAG_NAME));										
			if(reuseTags.contains(tagname)){
				Object[] newRow = new Object[table.getColumnCount()];	
				newRow[table.getIndexOf(ColumnConstants.MATING_TYPE)] = type;
				ObservationUnit newTag = ObservationUnitDAO.getInstance().createNewTagInHarvesting(tagname);
				tagsCreatedinHarvest.add(newTag.getObservationUnitId());
				newRow[table.getIndexOf(ColumnConstants.SELECT)] = false;
				newRow[table.getIndexOf(ColumnConstants.TAG_ID)] = newTag.getObservationUnitId();
				newRow[table.getIndexOf(ColumnConstants.TAG_NAME)] = newTag.getTagname();
				newRow[table.getIndexOf(ColumnConstants.ROLE)] = "F";
				newRow[table.getIndexOf(ColumnConstants.MATE_LINK)] = ++nextLink;
				newRow[table.getIndexOf(ColumnConstants.STOCK_NAME)] = newTag.getTagname();
				newRow[table.getIndexOf(ColumnConstants.ROW_NUM)] = table.getRowCount();
				newRow[table.getIndexOf(ColumnConstants.ACCESSION)] =(String) table.getModel().getValueAt(row,table.getIndexOf(ColumnConstants.ACCESSION));
				newRow[table.getIndexOf(ColumnConstants.PEDIGREE)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.PEDIGREE));						
				newRow[table.getIndexOf(ColumnConstants.GENERATION)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.GENERATION));						
				newRow[table.getIndexOf(ColumnConstants.SELECTION)] = false;
				((DefaultTableModel)table.getModel()).addRow(newRow);					
			}else{
				table.getModel().setValueAt(type, row, table.getIndexOf(ColumnConstants.MATING_TYPE));
				table.getModel().setValueAt(table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.TAG_NAME)), row, table.getIndexOf(ColumnConstants.STOCK_NAME));
				table.getModel().setValueAt("F", row, table.getIndexOf(ColumnConstants.ROLE));
				table.getModel().setValueAt(++nextLink, row, table.getIndexOf(ColumnConstants.MATE_LINK));
			}					
			
		}
		table.clearSelection();
		setButtonsUsability();
		table.getRowSorter().toggleSortOrder(table.getIndexOf(ColumnConstants.MATE_LINK));
		table.getRowSorter().toggleSortOrder(table.getIndexOf(ColumnConstants.MATE_LINK));
	}
    
	
	private void addMatingButtonListener(JButton matingButton, final String matingtype){
		final CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();
		matingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modified = true;
				int[] selectedRows = table.getSelectedRows();
				
				HashSet<String> tagsUsed = tagsUsedOtherGroup();
				HashSet<String> reuseTags = new HashSet<String>();
				for(int row:selectedRows) {
					if(tagsUsed.contains((String) table.getValueAt(row, ColumnConstants.TAG_NAME))){
						reuseTags.add((String) table.getValueAt(row, ColumnConstants.TAG_NAME));						
					}
					
				}
				if(reuseTags.size() > 0 && !shouldReuseTags()) {
					table.clearSelection();
					setButtonsUsability();
					return;
				}

				String femaleTag=null;
				if(selectedRows.length == 1){					
					femaleTag = (String) table.getValueAt(selectedRows[0], ColumnConstants.TAG_NAME);
				}else{
					femaleTag = getFemaleTag();
				}
				
				if(femaleTag == null){
					return;
				}
				
				int nextLink = ++FieldGenerated.this.nextLink;
				String matingType = matingtype;
				
				if(selectedRows.length >=2){
					ArrayList<Object[]> rowsToBeAdded = new ArrayList<Object[]>();
					for(int row:selectedRows) {
						row = table.convertRowIndexToModel(row);						
						String tagname = (String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.TAG_NAME));
						if(tagname == femaleTag){
						//current row is female tag	
							if(reuseTags.contains(tagname)){
								String role = "";
								for(int rowIndex = 0; rowIndex < table.getRowCount(); ++rowIndex)
								{
									if(((String) table.getValueAt(rowIndex, ColumnConstants.TAG_NAME)).equals(tagname)){
										role = ((String) table.getValueAt(rowIndex, ColumnConstants.ROLE));
										if(role.equals("F")){
											break;
										}
									}
								}								
								String tag = null;
								Object tagId = 0;
								if(role.equals("F")){
									ObservationUnit newTag = ObservationUnitDAO.getInstance().createNewTagInHarvesting(tagname);
									tagsCreatedinHarvest.add(newTag.getObservationUnitId());
									tag = newTag.getTagname();
									tagId = newTag.getObservationUnitId();
								}else{
									tag = tagname;
									tagId = table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.TAG_ID));
								}
								
								//female tag is reused, create new one
								Object[] newRow = new Object[table.getColumnCount()];
								newRow[table.getIndexOf(ColumnConstants.SELECT)] = false;
								newRow[table.getIndexOf(ColumnConstants.MATING_TYPE)] = matingType;								
								newRow[table.getIndexOf(ColumnConstants.TAG_ID)] = tagId;
								newRow[table.getIndexOf(ColumnConstants.TAG_NAME)] = tag;
								newRow[table.getIndexOf(ColumnConstants.ROLE)] = "F";
								newRow[table.getIndexOf(ColumnConstants.MATE_LINK)] = nextLink;
								newRow[table.getIndexOf(ColumnConstants.STOCK_NAME)] = tag;
								newRow[table.getIndexOf(ColumnConstants.ROW_NUM)] = table.getRowCount();
								newRow[table.getIndexOf(ColumnConstants.ACCESSION)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.ACCESSION));
								newRow[table.getIndexOf(ColumnConstants.PEDIGREE)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.PEDIGREE));						
								newRow[table.getIndexOf(ColumnConstants.GENERATION)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.GENERATION));						
								newRow[table.getIndexOf(ColumnConstants.SELECTION)] = false;
								rowsToBeAdded.add(newRow);
								//((DefaultTableModel)table.getModel()).addRow(newRow);					
							}else{
								table.getModel().setValueAt(matingType, row, table.getIndexOf(ColumnConstants.MATING_TYPE));
								table.getModel().setValueAt(table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.TAG_NAME)), row, table.getIndexOf(ColumnConstants.STOCK_NAME));
								table.getModel().setValueAt("F", row, table.getIndexOf(ColumnConstants.ROLE));
								table.getModel().setValueAt(nextLink, row, table.getIndexOf(ColumnConstants.MATE_LINK));
							}	
						}else{
						//current row is male tag	
							if(reuseTags.contains(tagname)){
								//male tag is reused, create new one
								Object[] newRow = new Object[table.getColumnCount()];	
								newRow[table.getIndexOf(ColumnConstants.SELECT)] = false;
								newRow[table.getIndexOf(ColumnConstants.MATING_TYPE)] = matingType;								
								newRow[table.getIndexOf(ColumnConstants.TAG_ID)] = table.getModel().getValueAt(row,table.getIndexOf(ColumnConstants.TAG_ID));
								newRow[table.getIndexOf(ColumnConstants.TAG_NAME)] = tagname;
								newRow[table.getIndexOf(ColumnConstants.ROLE)] = "M";
								newRow[table.getIndexOf(ColumnConstants.MATE_LINK)] = nextLink;
								newRow[table.getIndexOf(ColumnConstants.ROW_NUM)] = table.getRowCount();
								newRow[table.getIndexOf(ColumnConstants.ACCESSION)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.ACCESSION));
								newRow[table.getIndexOf(ColumnConstants.PEDIGREE)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.PEDIGREE));	
								newRow[table.getIndexOf(ColumnConstants.GENERATION)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.GENERATION));
								newRow[table.getIndexOf(ColumnConstants.SELECTION)] = false;
								rowsToBeAdded.add(newRow);
								//((DefaultTableModel)table.getModel()).addRow(newRow);					
							}else{
								table.getModel().setValueAt(matingType, row, table.getIndexOf(ColumnConstants.MATING_TYPE));
								table.getModel().setValueAt("M", row, table.getIndexOf(ColumnConstants.ROLE));
								table.getModel().setValueAt(nextLink, row, table.getIndexOf(ColumnConstants.MATE_LINK));
							}
						}						
					}
					for(Object[] newRow : rowsToBeAdded){
						((DefaultTableModel)table.getModel()).addRow(newRow);
					}
				}
				table.clearSelection();
				setButtonsUsability();
				table.getRowSorter().toggleSortOrder(table.getIndexOf(ColumnConstants.MATE_LINK));
				table.getRowSorter().toggleSortOrder(table.getIndexOf(ColumnConstants.MATE_LINK));
				ChangeMonitor.markAsChanged(projectID);
			}			
		});
	}
	
	private void generateButtons() {
		selfButton = new JButton("SF");
		sbButton = new JButton("SB");
		poolButton = new JButton("Pool");
		crButton = new JButton("CR");
		bcButton = new JButton("BC");
		multiFemaleButton = new JButton("1M -> nF");
		clearMatingtypeButton = new JButton("Clear Mating");
		setUnsetSelectionButton = new JButton("Selection Applied");
		final CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();
		selfButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SFOrSB("SF");
				ChangeMonitor.markAsChanged(projectID);
			}			
		});
		sbButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SFOrSB("SB");
				ChangeMonitor.markAsChanged(projectID);
			}			
		});
		addMatingButtonListener(crButton, "CR");
		addMatingButtonListener(bcButton, "BC");
		addMatingButtonListener(poolButton, "PP");
		multiFemaleButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				modified = true;
				int[] selectedRows = table.getSelectedRows();				
				HashSet<String> tagsUsed = tagsUsedOtherGroup();
				HashSet<String> reuseTags = new HashSet<String>();
				for(int row:selectedRows) {
					if(tagsUsed.contains((String) table.getValueAt(row, ColumnConstants.TAG_NAME))){
						reuseTags.add((String) table.getValueAt(row, ColumnConstants.TAG_NAME));						
					}
					
				}
				if(reuseTags.size() > 0 && !shouldReuseTags()) {
					table.clearSelection();
					setButtonsUsability();
					return;
				}
				String maleTag = getMaleTag();
				if (maleTag != null)
				{
					String matingType = getMatingType();
					if(matingType == null) matingType = "CR";
					
					if(selectedRows.length >=2){
						int notUsedMaleRow = -1;
						int maleRow = -1;
							
						for(int row:selectedRows) {
							row = table.convertRowIndexToModel(row);						
							String tagname = (String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.TAG_NAME));
							if(tagname.equals(maleTag)){
								maleRow = row;
								if(!reuseTags.contains(tagname)){
									notUsedMaleRow = row;
								}
							}	
						}					
						
						ArrayList<Object[]> rowsToBeAdded = new ArrayList<Object[]>();
						for(int row:selectedRows) {
							nextLink++;
							row = table.convertRowIndexToModel(row);						
							String tagname = (String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.TAG_NAME));												
							if(!tagname.equals(maleTag)){
							//current row is female tag	
								if(reuseTags.contains(tagname)){
									String role = "";
									for(int rowIndex = 0; rowIndex < table.getRowCount(); ++rowIndex)
									{
										if(((String) table.getValueAt(rowIndex, ColumnConstants.TAG_NAME)).equals(tagname)){
											role = ((String) table.getValueAt(rowIndex, ColumnConstants.ROLE));
											if(role.equals("F")){
												break;
											}
										}
									}								
									String tag = null;
									Object tagId = 0;
									if(role.equals("F")){
										ObservationUnit newTag = ObservationUnitDAO.getInstance().createNewTagInHarvesting(tagname);
										tagsCreatedinHarvest.add(newTag.getObservationUnitId());
										tag = newTag.getTagname();
										tagId = newTag.getObservationUnitId();
									}else{
										tag = tagname;
										tagId = table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.TAG_ID));
									}
									
									//female tag is reused, create new one
									Object[] newRow = new Object[table.getColumnCount()];	
									newRow[table.getIndexOf(ColumnConstants.SELECT)] = false;
									newRow[table.getIndexOf(ColumnConstants.MATING_TYPE)] = matingType;								
									newRow[table.getIndexOf(ColumnConstants.TAG_ID)] = tagId;
									newRow[table.getIndexOf(ColumnConstants.TAG_NAME)] = tag;
									newRow[table.getIndexOf(ColumnConstants.ROLE)] = "F";
									newRow[table.getIndexOf(ColumnConstants.MATE_LINK)] = nextLink;
									newRow[table.getIndexOf(ColumnConstants.STOCK_NAME)] = tag;
									newRow[table.getIndexOf(ColumnConstants.ROW_NUM)] = table.getRowCount();
									newRow[table.getIndexOf(ColumnConstants.ACCESSION)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.ACCESSION));
									newRow[table.getIndexOf(ColumnConstants.PEDIGREE)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.PEDIGREE));						
									newRow[table.getIndexOf(ColumnConstants.GENERATION)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.GENERATION));						
									newRow[table.getIndexOf(ColumnConstants.SELECTION)] = false;
									rowsToBeAdded.add(newRow);	
								
								}else{
									table.getModel().setValueAt(matingType, row, table.getIndexOf(ColumnConstants.MATING_TYPE));
									table.getModel().setValueAt(table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.TAG_NAME)), row, table.getIndexOf(ColumnConstants.STOCK_NAME));
									table.getModel().setValueAt("F", row, table.getIndexOf(ColumnConstants.ROLE));
									table.getModel().setValueAt(nextLink, row, table.getIndexOf(ColumnConstants.MATE_LINK));
								}
								Object[] newRow = getNewMaleRow(maleRow, matingType, maleTag, nextLink);
								rowsToBeAdded.add(newRow);
								
							}						
						}
						if(notUsedMaleRow != -1){
							rowsToBeAdded.remove(rowsToBeAdded.size()-1);
							table.getModel().setValueAt(matingType, notUsedMaleRow, table.getIndexOf(ColumnConstants.MATING_TYPE));
							table.getModel().setValueAt("M", notUsedMaleRow, table.getIndexOf(ColumnConstants.ROLE));
							table.getModel().setValueAt(--nextLink, notUsedMaleRow, table.getIndexOf(ColumnConstants.MATE_LINK));
						}
						for(Object[] newRow : rowsToBeAdded){
							((DefaultTableModel)table.getModel()).addRow(newRow);
						}
					}
					table.clearSelection();
					setButtonsUsability();
					table.getRowSorter().toggleSortOrder(table.getIndexOf(ColumnConstants.MATE_LINK));
					table.getRowSorter().toggleSortOrder(table.getIndexOf(ColumnConstants.MATE_LINK));
			
				}	  
				ChangeMonitor.markAsChanged(projectID);
			}
			
		});
		clearMatingtypeButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				modified = true;
				int[] selectedRows = table.getSelectedRows();
				HashSet<Integer> mateLinkSet = new HashSet<Integer>();
				for(int row:selectedRows) {
					if(!String.valueOf(table.getValueAt(row, ColumnConstants.MATE_LINK)).equalsIgnoreCase("null")){
						mateLinkSet.add((Integer) table.getValueAt(row, ColumnConstants.MATE_LINK));						
					}
				}
				for(int i=0; i<table.getRowCount();i++) {
					if(mateLinkSet.contains(table.getValueAt(i, ColumnConstants.MATE_LINK))) {
						table.setValueAt(null, i, table.getIndexOf(ColumnConstants.MATING_TYPE));
						table.setValueAt(null, i, table.getIndexOf(ColumnConstants.ROLE));
						table.setValueAt(null, i, table.getIndexOf(ColumnConstants.MATE_LINK));
						table.setValueAt(null, i, table.getIndexOf(ColumnConstants.MATE_METHOD));
						table.setValueAt(null, i, table.getIndexOf(ColumnConstants.STOCK_NAME));
					}
				}
				
				table.clearSelection();
				setButtonsUsability();
				ChangeMonitor.markAsChanged(projectID);
			}	
		});
		
		setUnsetSelectionButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				modified = true;
				int[] selectedRows = table.getSelectedRows();
				HashSet<Integer> mateLinkSet = new HashSet<Integer>();
				for(int row:selectedRows) {
					if(!String.valueOf(table.getValueAt(row, ColumnConstants.MATE_LINK)).equalsIgnoreCase("null")){
						mateLinkSet.add((Integer) table.getValueAt(row, ColumnConstants.MATE_LINK));						
					}
				}
				for(int i=0; i<table.getRowCount();i++) {
					if(mateLinkSet.contains(table.getValueAt(i, ColumnConstants.MATE_LINK))) {
						boolean currentSelection = (Boolean) table.getValueAt(i, table.getIndexOf(ColumnConstants.SELECTION));
						currentSelection = !currentSelection;
						table.setValueAt(currentSelection, i, table.getIndexOf(ColumnConstants.SELECTION));
					}
				}
				
				table.clearSelection();
				setButtonsUsability();
				ChangeMonitor.markAsChanged(projectID);
			}	
		});
		
		
	}
	
	private String getMatingType(){
		JPanel popup = new JPanel(new MigLayout("insets 0, gap 5"));
		ButtonGroup group = new ButtonGroup();
		String[] names = {"CR","BC"};
		for(String name : names) {
			JRadioButton button = new JRadioButton(name);
			group.add(button);
			popup.add(button, "wrap");
			button.setSelected(true);
		}
		int option = JOptionPane.showConfirmDialog(this, popup, "Select Mating Type:", JOptionPane.OK_CANCEL_OPTION); 
		if(option == JOptionPane.CANCEL_OPTION)
		{
			return null;
		}else if(option == JOptionPane.OK_OPTION){
			for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements();) {
				AbstractButton button = buttons.nextElement();
				if (button.isSelected()) {
					String matingType = button.getText();			
					return matingType;
				}
			}	
		}
		
		return null;
	}
	
	private Object[] getNewMaleRow(int row, String matingType, String tagname, int link){
		CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();
		Object[] maleRow = new Object[table.getColumnCount()];
		maleRow[table.getIndexOf(ColumnConstants.MATING_TYPE)] = matingType;								
		maleRow[table.getIndexOf(ColumnConstants.TAG_ID)] = table.getModel().getValueAt(row,table.getIndexOf(ColumnConstants.TAG_ID));
		maleRow[table.getIndexOf(ColumnConstants.TAG_NAME)] = tagname;
		maleRow[table.getIndexOf(ColumnConstants.ROLE)] = "M";
		maleRow[table.getIndexOf(ColumnConstants.MATE_LINK)] = link;
		maleRow[table.getIndexOf(ColumnConstants.ROW_NUM)] = table.getRowCount();
		maleRow[table.getIndexOf(ColumnConstants.ACCESSION)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.ACCESSION));
		maleRow[table.getIndexOf(ColumnConstants.PEDIGREE)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.PEDIGREE));													
		maleRow[table.getIndexOf(ColumnConstants.GENERATION)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.PEDIGREE));													
		
		return maleRow;
	}
	private String getFemaleTag(){
		CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();
		JPanel popup = new JPanel(new MigLayout("insets 0, gap 5"));
		JScrollPane jsp = new JScrollPane(popup){
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(250, 320);
            }
        };
		ButtonGroup group = new ButtonGroup();
		for(int row : table.getSelectedRows()) {
			JRadioButton button = new JRadioButton(String.valueOf(table.getValueAt(row,  ColumnConstants.TAG_NAME)));
			group.add(button);
			popup.add(button, "wrap");
			
			button.setSelected(true);
		}
		int option = JOptionPane.showConfirmDialog(this, jsp, "Select female tag: ", JOptionPane.OK_CANCEL_OPTION); 
		if(option == JOptionPane.CANCEL_OPTION)
		{
			return null;
		}else if(option == JOptionPane.OK_OPTION){
			for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements();) {
				AbstractButton button = buttons.nextElement();
				if (button.isSelected()) {
					String femaleTag = button.getText();			
					return femaleTag;
				}
			}	
		}
		
		return null;
	}
	
	private String getMaleTag(){
		CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();
		JPanel popup = new JPanel(new MigLayout("insets 0, gap 5"));
		ButtonGroup group = new ButtonGroup();
		for(int row : table.getSelectedRows()) {
			JRadioButton button = new JRadioButton(String.valueOf(table.getValueAt(row,  ColumnConstants.TAG_NAME)));
			group.add(button);
			popup.add(button, "wrap");
			button.setSelected(true);
		}
		int option = JOptionPane.showConfirmDialog(this, popup, "Select Male tag:", JOptionPane.OK_CANCEL_OPTION); 
		if(option == JOptionPane.CANCEL_OPTION)
		{
			return null;
		}else if(option == JOptionPane.OK_OPTION){
			for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements();) {
				AbstractButton button = buttons.nextElement();
				if (button.isSelected()) {
					String maleTag = button.getText();			
					return maleTag;
				}
			}	
		}
		
		return null;
	}
	
	
	public boolean isUnique(String name) {
		CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();		
		for(int row=0; row<table.getRowCount(); row++) {
			if(name.equals(table.getValueAt(row, ColumnConstants.STOCK_NAME)))
				return false;
		}
		List<Stock> results = StockDAO.getInstance().findStockByName(name);
        if (results.size() > 0) {
        	for(Stock result : results){
        		System.out.println(result.getStockName());
        	}
            return false;
        }
        
		return true;
	}

	private JPanel getButtonsPanel() {
		JPanel buttonsPanel = new JPanel(new MigLayout("insets 0, gapx 5"));
		buttonsPanel.add(importButton, "push");
		buttonsPanel.add(selfButton);
		buttonsPanel.add(sbButton);
		buttonsPanel.add(crButton);
		buttonsPanel.add(bcButton);
		buttonsPanel.add(poolButton);
		buttonsPanel.add(multiFemaleButton);
		buttonsPanel.add(clearMatingtypeButton);
		buttonsPanel.add(setUnsetSelectionButton,"gapleft 20, push");
		buttonsPanel.add(matingMethod);
		buttonsPanel.add(setMatingMethod);
		buttonsPanel.add(unsetMatingMethod);
		setButtonsUsability();
		return buttonsPanel;
	}

	private void addListeners() {
		final CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		Utils.removeAllRowsFromTable((DefaultTableModel)table.getModel());
		table.getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.DELETE || e.getType() == TableModelEvent.INSERT) {
					getCrossingTablePanel().getNumberOfRows().setText(String.valueOf(getCrossingTablePanel().getTable().getRowCount()));
					ChangeMonitor.markAsChanged(projectID);
				}
				
			}});
		getCrossingTablePanel().getDeleteButton().addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				modified = true;
				HashSet<Integer> dissolveLinks = linksToDissolve();
				if(dissolveLinks.size() > 0 && shouldCancelDissolve()) {
					table.clearSelection();
					setButtonsUsability();
				} else {
					dissolveLinks(dissolveLinks);
					CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();								
					int rows[] = table.getSelectedRows();
					DefaultTableModel model = (DefaultTableModel) table.getModel();
					if(rows.length == table.getRowCount()){
						model.setRowCount(0);
						table.getCheckedRows().clear();
					}else{
						for(int rowCounter=0;rowCounter<rows.length;rowCounter++){
							int deleteRow = rows[rowCounter]-rowCounter;
							deleteRow = table.convertRowIndexToModel(deleteRow);
							int tagid = (Integer) table.getValueAt(deleteRow, table.getIndexOf(ColumnConstants.TAG_ID));
							if(tagsCreatedinHarvest.contains(tagid)){
								tagsCreatedinHarvest.remove(tagid);
								ObservationUnitDAO.getInstance().delete(tagid);
							}
							table.getCheckedRows().remove(deleteRow);
							model.removeRow(deleteRow);
						}
						table.clearSelection();
						updateTableStatus();
					}
				}
				
				
			}
		});
		
		getCrossingTablePanel().getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener(){

			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1){
					setButtonsUsability();
				}
			}
			
		});
		getCrossingTablePanel().getAll().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setButtonsUsability();
			}
		});
		
		getCrossingTablePanel().getNone().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setButtonsUsability();
			}
		});
		
	}
	
	private HashSet<Integer> linksToDissolve() {
		CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();
		HashSet<Integer> dissolveLinks = new HashSet<Integer>();
		for(int row : table.getSelectedRows()) {
			if(table.getValueAt(row, ColumnConstants.MATING_TYPE)!= null) {
				dissolveLinks.add((Integer) table.getValueAt(row, ColumnConstants.MATE_LINK));
			}
		}
		return dissolveLinks;
	}
	
	private HashSet<String> tagsUsedOtherGroup() {
		CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();
		HashSet<String> dissolveLinks = new HashSet<String>();
		for(int row : table.getSelectedRows()) {
			if(table.getValueAt(row, ColumnConstants.MATING_TYPE)!= null) {
				dissolveLinks.add((String) table.getValueAt(row, ColumnConstants.TAG_NAME));
			}
		}
		return dissolveLinks;
	}
	
	private boolean shouldCancelDissolve() {
		int option = JOptionPane.showConfirmDialog(FieldGenerated.this, "<HTML><FONT COLOR = Red>Some of the tags from your selection are part of other groups."
				+ "<br>If you click ok, those groups will be dissolved</FONT></HTML>", "Warning!", JOptionPane.OK_CANCEL_OPTION);       
		if(option == JOptionPane.CANCEL_OPTION) {
			return true;
		}
		return false;
	}
	
	private boolean shouldReuseTags() {
		int option = JOptionPane.showConfirmDialog(FieldGenerated.this, "<HTML><FONT COLOR = Red>Some of the tags from your selection are part of other groups."
				+ "<br>If you click ok, new tags will be created for resuing female tags;<br >new copies will be created for resuing male tags</FONT></HTML>", "Warning!", JOptionPane.OK_CANCEL_OPTION);       
		if(option == JOptionPane.OK_OPTION) {
			return true;
		}
		return false;
	}

	private boolean dissolveLinks(HashSet<Integer> dissolveLinks) {
		CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();
		
		if(dissolveLinks.size() > 0) {			
			for(int row=0;row<table.getRowCount();row++) {
				if(table.getValueAt(row, ColumnConstants.MATE_LINK) != null) {
					if(dissolveLinks.contains(table.getValueAt(row, ColumnConstants.MATE_LINK))) {
						table.setValueAt(null, row, table.getIndexOf(ColumnConstants.MATE_LINK));
						table.setValueAt(null, row, table.getIndexOf(ColumnConstants.STOCK_NAME));
						table.setValueAt(null, row, table.getIndexOf(ColumnConstants.ROLE));
						table.setValueAt(null, row, table.getIndexOf(ColumnConstants.MATE_METHOD));
						table.setValueAt(null, row, table.getIndexOf(ColumnConstants.MATING_TYPE));
						table.setValueAt(false, row, table.getIndexOf(ColumnConstants.SELECTION));
					}
				}
			}
		}
		return false;
	}

	private void generateImportButton() {
		importButton = new JButton("Import");
		importButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				modified = true;
				final JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("Text files(.txt)", "txt"));
				fc.showOpenDialog(FieldGenerated.this);

				if(fc.getSelectedFile()==null)
					return;
				
				progressBar.setVisible(true);
				new HarvestingImportWorker(FieldGenerated.this, fc).execute();	
                updateTableStatus();
                ChangeMonitor.markAsChanged(projectID);
			}
		});

	}
	
	public void setButtonsUsability(){
		CheckBoxIndexColumnTable table = getCrossingTablePanel().getTable();
		int selectedRowsNum = table.getSelectedRows().length ;
		multiFemaleButton.setEnabled(selectedRowsNum >= 2);
		getCrossingTablePanel().getDeleteButton().setEnabled(selectedRowsNum > 0);
		selfButton.setEnabled(selectedRowsNum > 0);
		sbButton.setEnabled(selectedRowsNum > 0);
		poolButton.setEnabled(selectedRowsNum > 2);	
		crButton.setEnabled(selectedRowsNum == 2);		
		bcButton.setEnabled(selectedRowsNum == 2);	
		setUnsetSelectionButton.setEnabled(selectedRowsNum > 0);	
		clearMatingtypeButton.setEnabled(selectedRowsNum > 0);	
		/*for(int row : table.getSelectedRows()){
			if (!String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.MATING_TYPE))).equals("null")){
				clearMatingtypeButton.setEnabled(true);	
				break;
			}
		}*/		
		setMatingMethod.setEnabled(selectedRowsNum > 0);
		unsetMatingMethod.setEnabled(selectedRowsNum > 0);
		
		
	}

	public void finishedImport(ArrayList<String> duplicate, ArrayList<String> nonExistTags,
			ArrayList<String> invalidformat) {
		progressBar.setVisible(false);
		String msg = "Stock Names were not created for some groups.\n";
		if(duplicate.size() > 0){
			msg += "\nBecause the stock names to be created already exist :\n";
			for(String d : duplicate){
				msg += d+"\n";
			}
		}
		if(nonExistTags.size() > 0){
			msg += "\nBecause some tags of the group can not be found in database:\n";
			for(String e : nonExistTags){
				msg += e+"\n";
			}
		}
		if(invalidformat.size() > 0){
			msg += "\nBecause of invalid format:\n";
			for(String e : invalidformat){
				msg += e+"\n";
			}
		}
		 JTextArea jta = new JTextArea(msg);
         JScrollPane jsp = new JScrollPane(jta){
             @Override
             public Dimension getPreferredSize() {
                 return new Dimension(420, 350);
             }
         };
		if(duplicate.size() > 0 || nonExistTags.size() > 0) {
			JOptionPane.showConfirmDialog(FieldGenerated.this, jsp, "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE); 
			
		}
		updateTableStatus();
		
	}
	public void updateTableStatus(){
		getCrossingTablePanel().getTable().clearSelection();
		setButtonsUsability();
		getCrossingTablePanel().getNumberOfRows().setText(String.valueOf(getCrossingTablePanel().getTable().getRowCount()));
	}
	public void populateTableFromObjects(List<Object[]> tableRows) {
		DefaultTableModel tableModel = ((DefaultTableModel) getCrossingTablePanel().getTable().getModel());
        removeAllRowsFromTable(tableModel);	
    	for(Object[] row : tableRows){
    		tableModel.addRow(row);               
    	}
    	updateTableStatus();
	}

	/**
	 * @return the stockList
	 */
	public List<PlantingRow> getStockList() {
		return stockList;
	}

	/**
	 * @param stockList the stockList to set
	 */
	public void setStockList(List<PlantingRow> stockList) {
		this.stockList = stockList;
	}

	public void refreshMethods() {
		List<MateMethod> mateMethods = MateMethodDAO.getInstance().getAllMateMethods();
		mateMethodtoID.clear();
		String[] methodNames = new String[mateMethods.size()+1];
		for(int i=0;i<mateMethods.size();i++) {
			methodNames[i] = mateMethods.get(i).getMateMethodName();
			mateMethodtoID.put(mateMethods.get(i).getMateMethodName(), mateMethods.get(i).getMateMethodId());
		}
		methodNames[methodNames.length-1] = "Add new..";
		matingMethod.setModel(new DefaultComboBoxModel(methodNames));
	}

}