package org.accretegb.modules.germplasm.sampling;

import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;
import static org.accretegb.modules.customswingcomponent.Utils.saveTableToFile;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.constants.PhenotypeConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.ImageButton;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.TextField;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.germplasm.phenotype.PhenotypeExportPanel;
import org.accretegb.modules.hibernate.MeasurementParameter;
import org.accretegb.modules.hibernate.MeasurementUnit;
import org.accretegb.modules.hibernate.dao.MeasurementValueDAO;
import org.accretegb.modules.hibernate.dao.MeasurementParameterDAO;
import org.accretegb.modules.hibernate.dao.ObservationUnitDAO;
import org.accretegb.modules.util.ChangeMonitor;
import org.accretegb.modules.hibernate.dao.MeasurementUnitDAO;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import net.miginfocom.swing.MigLayout;

/**
 * Choose plants as samples
 * @author Ningjing
 *
 */
public class SampleSelectionPanel extends JPanel {
	private JButton searchTagsButton;
	private TableToolBoxPanel sampleSelectionTablePanel;
	private Vector tableData;
	private TextField commentField;
	private String currentSubsetName;
	private JComboBox  uniTypeInfo;
	private Boolean isFirstSync = true;
	private PhenotypeExportPanel phenotypeExportPanel;
	private LinkedHashMap<String, Object[][]> subsetTableMap = new LinkedHashMap<String, Object[][]>();
	private LinkedHashMap<String, String> subsetCommentMap = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, List<String>> parameterInfoMap;
	private int projectID = -1;
	public ArrayList<String> options = new ArrayList<String>();
	private boolean shouldResetMaps = false;


	public void initialize() {
		setLayout(new MigLayout("insets 10, gap 5"));
		JPanel subsetPanel = sampleSelectionTablePanel.getBottomHorizonPanel();
		subsetPanel.add(addStockSetComment(),"push, gapleft 10%, w 60%, al right");
		subsetPanel.add(createSubsetButton()," push, al right, wrap");
		sampleSelectionTablePanel.getTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		add(sampleSelectionTablePanel," w 100%, h 100%,  wrap");
		populateTable();
		initializeSubsetComboBox();
		removeAllRowsFromTable((DefaultTableModel)getSampleSelectionTablePanel().getTable().getModel());
	
	}
	
	private JPanel addStockSetComment(){
		JPanel panel = new JPanel(new MigLayout("insets 0 0 0 0"));
		JLabel label = new JLabel("Stock Set Comment:");
		TextField commentField = new TextField(30); 
		commentField.setPlaceholder("All");
		commentField.setForeground(new Color(160, 160, 160));
		commentField.addFocusListener(new FocusListener(){

			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void focusLost(FocusEvent e) {
				getSubsetCommentMap().put(getCurrentSubsetName(), getCommentField().getText().trim());
				phenotypeExportPanel.getSubsetCommentMap().put(getCurrentSubsetName(), getCommentField().getText().trim());
			}
			
		});
		setCommentField(commentField);
		panel.add(label);
		panel.add(commentField,"w 100%");
		return panel;
	}
	

	
	
	public void updateNumofItems(){
		getSampleSelectionTablePanel().getNumberOfRows().setText(String.valueOf(getSampleSelectionTablePanel().getTable().getRowCount()));
	}
	
	public void populateTable(){
		if(getTableData() != null){
			CheckBoxIndexColumnTable table = getSampleSelectionTablePanel().getTable();
			 DefaultTableModel model = (DefaultTableModel) table.getModel();
			Utils.removeAllRowsFromTable((DefaultTableModel)model);
			int rows = getTableData().size();
			int cols = ((Vector)getTableData().elementAt(0)).size();
			Object data[][] = new Object[rows][cols]; 
			for(int row = 0; row < rows; row ++){
				Object[] newRow = new Object[cols -1];
				for(int col = 0 ; col < (cols -1 ); ++col){
					if(col < 29){//remove "modified"
						newRow[col] = ((Vector)getTableData().elementAt(row)).elementAt(col);						
					}else{
						newRow[col] = ((Vector)getTableData().elementAt(row)).elementAt(col+1);	
					}
					data[row][col] = newRow[col];
				}
				model.addRow(newRow);
			}
			table.setModel(model);
			getSubsetTableMap().put("All", data);
			updateNumofItems();			
		}
	}
	
	public void populateSubset(String subsetName){
		Object[][] subsetData =	(Object[][]) getSubsetTableMap().get(subsetName);
		CheckBoxIndexColumnTable table = getSampleSelectionTablePanel().getTable();
		 DefaultTableModel model = (DefaultTableModel) table.getModel();
		Utils.removeAllRowsFromTable((DefaultTableModel)model);
		int rows = subsetData.length;
		int cols = subsetData[0].length;
		for(int row = 0; row < rows; row ++){
			Object[] newRow = new Object[cols];
			for(int col = 0 ; col < cols; ++col){
				newRow[col] = subsetData[row][col];
			}
			model.addRow(newRow);
		}
		table.setModel(model);	
		updateNumofItems();
		Vector data = ((DefaultTableModel) getSampleSelectionTablePanel().getTable().getModel()).getDataVector();
		setTableData(data);
	}
	
	public JButton createSubsetButton(){
		JButton createSubset = new JButton("Create Subset");	
		createSubset.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
			    JComboBox subset = getSampleSelectionTablePanel().getTableSubset();
			    String str=null;
	           
				CheckBoxIndexColumnTable table = getSampleSelectionTablePanel().getTable();
				if(table.getSelectedRowCount() > 0)
				{
					 do {
		            	 str = JOptionPane.showInputDialog(null, "Enter Subset Name:", "Create New Subset", 1);
		            	 if (str != null) {            		
		            		 DefaultComboBoxModel model = (DefaultComboBoxModel) subset.getModel();
		            		 if (model.getIndexOf(str) == -1) {
		            			 subset.addItem(str);
		            			 
								} else {
									str = null;
									JOptionPane.showConfirmDialog(null,
											"Duplicate Subset Name", "",
											JOptionPane.DEFAULT_OPTION,
											JOptionPane.PLAIN_MESSAGE);
								}
		            	 }else {
		                     str = "";
		                 }
		            }while (str == null);
					Object subsetData[][] = new Object[table.getSelectedRowCount()][table.getColumnCount()]; 
					for(int row = 0; row < table.getSelectedRowCount();++row){
						subsetData[row][0] = new Boolean(false);
						for(int col = 1; col < table.getColumnCount();++col){
							subsetData[row][col] = table.getValueAt(table.getSelectedRows()[row], col);
						}						
					}
					getSubsetTableMap().put(str, subsetData);
					phenotypeExportPanel.getSubsetTableMap().put(str, subsetData);
					if(str != "All")
					{
						phenotypeExportPanel.getPhenotypeTagsTablePanel().getTableSubset().addItem(str);
						options.add(str);
					}
					subset.setSelectedItem(str);
					ChangeMonitor.markAsChanged(projectID);
				}else{
					JOptionPane.showConfirmDialog(null,
							"Please select subset from the table", "",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.PLAIN_MESSAGE);
				}
			}			
		});
		return createSubset;
		
	}
	
	public void initializeSubsetComboBox(){
		final JComboBox subset = getSampleSelectionTablePanel().getTableSubset();
		subset.addItem("All");	
		subset.setPrototypeDisplayValue("              ");
		setCurrentSubsetName("All");
		options.add("All");
		subset.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					String selected = subset.getSelectedItem().toString();
					populateSubset(selected);					
					setCurrentSubsetName(selected);
					if(getSubsetCommentMap().get(selected) == null || getSubsetCommentMap().get(selected).equals("")){
						getCommentField().setPlaceholder(selected);
					}else{
						getCommentField().setPlaceholder(selected);
						getCommentField().setText(getSubsetCommentMap().get(selected));
						getCommentField().setForeground(Color.black);
					}
				}
			}
		});
	}
	
	
	
	
	
	 private void saveTableToFile(JTable table, JPanel panel, String fileName, String firstRowComment) {
	        try {
	            if (table == null || fileName == null) {
	                return;
	            }
	            table.setRowSorter(null);
	            JFileChooser fileChooser = new JFileChooser();
	            File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
	            fileChooser.setSelectedFile(file);
	            int approve = fileChooser.showSaveDialog(panel);
	            if (approve != JFileChooser.APPROVE_OPTION) {
	                return;
	            }
	            file = fileChooser.getSelectedFile();
	            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	            writer.write(firstRowComment);
	            writer.newLine();
	            //write the column names first
	            for (int columnCounter = 0; columnCounter < table.getColumnCount(); columnCounter++) {
	                writer.write(table.getColumnName(columnCounter));
	                writer.write(",");
	            }

	            for (int rowCounter = 0; rowCounter < table.getRowCount(); rowCounter++) {
	                writer.newLine();
	                for (int columnCounter = 0; columnCounter < table.getColumnCount(); columnCounter++) {
	                    String value = "";
	                    if (table.getValueAt(rowCounter, columnCounter) instanceof java.sql.Timestamp) {
	                        java.sql.Timestamp timeStamp = (java.sql.Timestamp) table.getValueAt(rowCounter, columnCounter);
	                        value = (timeStamp.getYear() + 1900) + "-" + (timeStamp.getMonth() + 1) + "-"
	                                + timeStamp.getDate();
	                    } else {
	                        value = String.valueOf(table.getValueAt(rowCounter, columnCounter)).equals("null")?"":String.valueOf(table.getValueAt(rowCounter, columnCounter));
	                    }
	                    writer.write(value);
	                    writer.write(",");
	                }
	            }
	            writer.close();
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    }
	
	
	public void plantingResync(){
		setSubsetTableMap(new LinkedHashMap<String, Object[][]>());
		setSubsetCommentMap( new LinkedHashMap<String, String>() );
		for(int index = 1; index<getSampleSelectionTablePanel().getTableSubset().getItemCount();++index)
		{
			getSampleSelectionTablePanel().getTableSubset().removeItemAt(index);
		}
		
	}
	
	
  /*	
   public void populateTableFromObjects(List<Object[]> exportTableRows) {
	    DefaultTableModel tableModel = ((DefaultTableModel) getSampleSelectionTablePanel().getTable().getModel());
	    removeAllRowsFromTable(tableModel);	
		for(Object[] row : exportTableRows){
			tableModel.addRow(row);               
		}
		updateNumofItems();
		Vector data = ((DefaultTableModel) getSampleSelectionTablePanel().getTable().getModel()).getDataVector();
		setTableData(data);	
		
   }*/

	
	public JButton getSearchTagsButton() {
		return searchTagsButton;
	}

	public void setSearchTagsButton(JButton searchTagsButton) {
		this.searchTagsButton = searchTagsButton;
	}
	
	public TableToolBoxPanel getSampleSelectionTablePanel() {
		return sampleSelectionTablePanel;
	}

	public void setSampleSelectionTablePanel(TableToolBoxPanel sampleSelectionTablePanel) {
		this.sampleSelectionTablePanel = sampleSelectionTablePanel;
	}
	
	public Vector getTableData() {
		return tableData;
	}

	public void setTableData(Vector tableData) {
		this.tableData = tableData;
	}

	public LinkedHashMap<String, Object[][]> getSubsetTableMap() {
		return subsetTableMap;
	}

	public void setSubsetTableMap(LinkedHashMap<String, Object[][]> subsetTableMap) {
		this.subsetTableMap = subsetTableMap;
	}
	
	public LinkedHashMap<String, String> getSubsetCommentMap() {
		return subsetCommentMap;
	}

	public void setSubsetCommentMap(LinkedHashMap<String, String> subsetCommentMap) {
		this.subsetCommentMap = subsetCommentMap;
	}
	public TextField getCommentField() {
		return commentField;
	}

	public void setCommentField(TextField commentField) {
		this.commentField = commentField;
	}

	public String getCurrentSubsetName() {
		return currentSubsetName;
	}

	public void setCurrentSubsetName(String currentSubsetName) {
		this.currentSubsetName = currentSubsetName;
	}
	public LinkedHashMap<String, List<String>> getParameterInfoMap() {
		return parameterInfoMap;
	}

	public void setParameterInfoMap(LinkedHashMap<String, List<String>> parameterInfoMap) {
		this.parameterInfoMap = parameterInfoMap;
	}
	public JComboBox<String> getUniTypeInfo() {
		return uniTypeInfo;
	}

	public void setUniTypeInfo(JComboBox<String> uniTypeInfo) {
		this.uniTypeInfo = uniTypeInfo;
	}

	public Boolean getIsFirstSync() {
		return isFirstSync;
	}

	public void setIsFirstSync(Boolean isFirstSync) {
		this.isFirstSync = isFirstSync;
	}

	public PhenotypeExportPanel getPhenotypeExportPanel() {
		return phenotypeExportPanel;
	}

	public void setPhenotypeExportPanel(PhenotypeExportPanel phenotypeExportPanel) {
		this.phenotypeExportPanel = phenotypeExportPanel;
	}
	public int getProjectID() {
		return projectID;
	}

	public void setProjectID(int projectID) {
		this.projectID = projectID;
	}
	
	public boolean isShouldResetMaps() {
		return shouldResetMaps;
	}

	public void setShouldResetMaps(boolean shouldResetMaps) {
		this.shouldResetMaps = shouldResetMaps;
	}

}
