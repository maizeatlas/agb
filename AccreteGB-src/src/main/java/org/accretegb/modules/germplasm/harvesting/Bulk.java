package org.accretegb.modules.germplasm.harvesting;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;
import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.TextField;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.germplasm.stocksinfo.CreateStocksInfoPanel;
import org.accretegb.modules.germplasm.stocksinfo.StocksInfoPanel;
import org.accretegb.modules.hibernate.MeasurementUnit;
import org.accretegb.modules.hibernate.Stock;
import org.accretegb.modules.hibernate.dao.MeasurementUnitDAO;
import org.accretegb.modules.hibernate.dao.StockDAO;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.util.ChangeMonitor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericXmlApplicationContext;

public class Bulk extends JPanel {
	
	private TableToolBoxPanel bulkTablePanel;
	private JTextField multiply;
	private JButton mixButton;
	private JButton unmixButton;
	private JButton importStocks;
	private int rowCounter;
	public int nextLink;
	private JButton setQuantity;
	private JButton setUnit;
	private JFormattedTextField mixQuantity;
	private JComboBox mixUnit;
	private List<MeasurementUnit> unitsList;
	private FieldGenerated fieldGenerated;
	private StickerGenerator stickerGenerator;
	private int projectID = -1;
	public boolean modified;
	
	public int getProjectID() {
		return projectID;
	}

	public void setProjectID(int projectID) {
		this.projectID = projectID;
	}
	public TableToolBoxPanel getBulkTablePanel() {
		return bulkTablePanel;
	}

	public void setBulkTablePanel(TableToolBoxPanel bulkTablePanel) {
		this.bulkTablePanel = bulkTablePanel;
	}

	public FieldGenerated getFieldGenerated() {
		return fieldGenerated;
	}

	public void setFieldGenerated(FieldGenerated fieldGenerated) {
		this.fieldGenerated = fieldGenerated;
	}
	
	public StickerGenerator getStickerGenerator() {
		return stickerGenerator;
	}

	public void setStickerGenerator(StickerGenerator stickerGenerator) {
		this.stickerGenerator = stickerGenerator;
	}
	
	public JButton getImportStocks() {
		return importStocks;
	}

	
	public void initialize() {
		setLayout(new MigLayout("insets 5, gap 5"));
		nextLink = 1;
		add(getButtonsPanel(), "w 100%, wrap");
		add(getBulkTablePanel(), "w 100%, h 100%, wrap");
		addListeners();
	}

	private void addListeners() {

		final CheckBoxIndexColumnTable table = getBulkTablePanel().getTable();
		Utils.removeAllRowsFromTable((DefaultTableModel)table.getModel());
		table.getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.DELETE || e.getType() == TableModelEvent.INSERT) {
					getBulkTablePanel().getNumberOfRows().setText(String.valueOf(getBulkTablePanel().getTable().getRowCount()));	
					ChangeMonitor.markAsChanged(projectID);
				}
				
			}});
		importStocks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPopup();
				ChangeMonitor.markAsChanged(projectID);
			}			
		});
		mixButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modified = true;
				Set<String> idSet = new HashSet<String>();
				Set<Integer> dissolveIds = new HashSet<Integer>();
				if(table.getSelectedRows().length <= 1){
					return;
				}
				for(int row:table.getSelectedRows()) {
					String stockName = String.valueOf(table.getValueAt(row, ColumnConstants.STOCK_NAME) );
					if(idSet.contains(stockName)) {
						JOptionPane.showConfirmDialog(Bulk.this, "<HTML><FONT COLOR = Red>You can add a stock only once to a mix.</FONT></HTML>", "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);       
						return;
					}
					else
						idSet.add(stockName);
					Integer mixId = (Integer) table.getValueAt(row, ColumnConstants.MIX_ID);
					if( mixId != null)
						dissolveIds.add(mixId);
				}
				if(dissolveIds.size() > 0) {
					int option = JOptionPane.showConfirmDialog(Bulk.this, "<HTML><FONT COLOR = Red>Some of the stocks from your selection are part of other mixtures.<br>If you continue, those mixtures will be dissolved.</FONT></HTML>", "Error!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);       
					if(option == JOptionPane.CANCEL_OPTION)
						return;
				}
				String stockName = getResultantName(table);
				if(StringUtils.isBlank(stockName)) {
					return;
				}
				if(dissolveIds.size() > 0) {

					for(int row=0; row<table.getRowCount(); row++) {
						if(dissolveIds.contains(table.getValueAt(row, ColumnConstants.MIX_ID))) {
							table.setValueAt("", row, table.getIndexOf(ColumnConstants.FINAL_STOCK_NAME));
							table.setValueAt(null, row, table.getIndexOf(ColumnConstants.MIX_ID));
						}
					}
				}
				for(int row:table.getSelectedRows()) {
					table.setValueAt(nextLink, row, table.getIndexOf(ColumnConstants.MIX_ID));
					table.setValueAt(stockName, row, table.getIndexOf(ColumnConstants.FINAL_STOCK_NAME));
				}
				if(Utils.isInteger(multiply.getText())){
					int x = Integer.parseInt(multiply.getText());
					int index = 1 ;
					ArrayList<Object[]> rowsToBeAdded = new ArrayList<Object[]>();
					while(index < x){
						nextLink ++;
						index ++ ;
						for(int row : table.getSelectedRows()){
							Object[] newRow = new Object[table.getColumnCount()];
							newRow[table.getIndexOf(ColumnConstants.SELECT)] = false;
							newRow[table.getIndexOf(ColumnConstants.STOCK_ID)] = table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.STOCK_ID));							
							newRow[table.getIndexOf(ColumnConstants.TAG_ID)] = table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.TAG_ID));
							newRow[table.getIndexOf(ColumnConstants.STOCK_NAME)] = table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.STOCK_NAME));
							newRow[table.getIndexOf(ColumnConstants.ACCESSION)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.ACCESSION));
							newRow[table.getIndexOf(ColumnConstants.PEDIGREE)] =(String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.PEDIGREE));						
							newRow[table.getIndexOf(ColumnConstants.QUANTITY)] = table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.QUANTITY));
							newRow[table.getIndexOf(ColumnConstants.UNIT_ID)] = table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.UNIT_ID));
							newRow[table.getIndexOf(ColumnConstants.UNIT)] = table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.UNIT));
							newRow[table.getIndexOf(ColumnConstants.ROW_NUM)] = table.getRowCount();
							newRow[table.getIndexOf(ColumnConstants.MIX_ID)] = nextLink;
							newRow[table.getIndexOf(ColumnConstants.MATE_LINK)] = table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.MATE_LINK));	
							String stockname = (String) table.getModel().getValueAt(row, table.getIndexOf(ColumnConstants.FINAL_STOCK_NAME));
							stockname = stockname.substring(0, stockname.length()-1) + String.valueOf(index);
							newRow[table.getIndexOf(ColumnConstants.FINAL_STOCK_NAME)] =stockname;
							rowsToBeAdded.add(newRow);
						}
						
					}
					for(Object[] newRow : rowsToBeAdded){
						((DefaultTableModel)table.getModel()).addRow(newRow);
					}
					nextLink ++;
				}else{
					nextLink++;
				}
				ChangeMonitor.markAsChanged(projectID);
				
			}			
		});
		unmixButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				int[] selectedRows = table.getSelectedRows();
				HashSet<Integer> mixLinkSet = new HashSet<Integer>();
				for(int row:selectedRows) {
					if(!String.valueOf(table.getValueAt(row, ColumnConstants.MIX_ID)).equalsIgnoreCase("null")){
						mixLinkSet.add((Integer) table.getValueAt(row, ColumnConstants.MIX_ID));						
					}
				}
				for(int i=0; i<table.getRowCount();i++) {
					if(mixLinkSet.contains(table.getValueAt(i, ColumnConstants.MIX_ID))) {
						table.setValueAt(null, i, table.getIndexOf(ColumnConstants.MIX_ID));
						table.setValueAt(null, i, table.getIndexOf(ColumnConstants.FINAL_STOCK_NAME));
					}
				}
				ChangeMonitor.markAsChanged(projectID);
			}
			
		});
		setQuantity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Utils.isInteger(mixQuantity.getText()))
				{
					int quantity = Integer.parseInt(mixQuantity.getText());
					for(int row:table.getSelectedRows()) {
						table.setValueAt(quantity, row, table.getIndexOf(ColumnConstants.QUANTITY));
					}
				}
				ChangeMonitor.markAsChanged(projectID);
			}
		});
		setUnit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String unit;
				Integer unitId;
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
				}
				ChangeMonitor.markAsChanged(projectID);
			}			
		});
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
						int option = JOptionPane.showConfirmDialog(Bulk.this, newUnitPanel, "Enter New Unit Information ", JOptionPane.OK_CANCEL_OPTION);
						if(option == JOptionPane.OK_OPTION) {
							if(!StringUtils.isBlank(unit.getText())) {
								valid = true;
								MeasurementUnitDAO.getInstance().insertOrUpdate(unit.getText());
								populateUnits();
							}
							else
								JOptionPane.showConfirmDialog(Bulk.this, "<HTML><FONT COLOR = Red>All fields are mandatory.</FONT></HTML>", "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);       
						}
						else break;
					}//while
					ChangeMonitor.markAsChanged(projectID);
				}//if last element
			}
		});
		
		
		
		getBulkTablePanel().getAddButton().setToolTipText("Add a duplicate of selected rows");
		getBulkTablePanel().getAddButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				for(int row: table.getSelectedRows()) {
					Object[] rowData = new Object[table.getColumnCount()];
					rowData[1] = rowCounter++;
					for(int col=2; col<rowData.length; col++) {
						rowData[col] = table.getValueAt(row, col);
					}
					rowData[table.getIndexOf(ColumnConstants.MIX_ID)] = null;
					rowData[table.getIndexOf(ColumnConstants.FINAL_STOCK_NAME)] = "";
					model.addRow(rowData);
				}
			}			
		});
		getBulkTablePanel().setActionListenerDeleteButton();
		getBulkTablePanel().getDeleteButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Map<Integer, Integer> map = new HashMap<Integer, Integer>();
				for(int row : table.getSelectedRows()) {
					if(table.getValueAt(row, ColumnConstants.MIX_ID) != null) {
						int mixId = Integer.parseInt( String.valueOf( 
								table.getValueAt(row, ColumnConstants.MIX_ID) ));
						map.put(mixId, 0);
					}
				}
				for(int row=0; row<table.getRowCount(); row++) {
					String mix =  String.valueOf(table.getValueAt(row, ColumnConstants.MIX_ID));
					if(StringUtils.isNotBlank(mix) && !mix.equals("null")) {
						int mixId = Integer.parseInt(mix);
						if(!table.isRowSelected(row) && map.containsKey(mixId) ){
							map.put(mixId, map.get(mixId)+1);
						}
					}
				}
				for(int row=0; row<table.getRowCount(); row++) {
					String mix =  String.valueOf(table.getValueAt(row, ColumnConstants.MIX_ID));
					if(StringUtils.isNotBlank(mix) && !mix.equals("null")) {
						int mixId = Integer.parseInt(mix);
						if(map.containsKey(mixId) && map.get(mixId) == 1){
							table.setValueAt("", row, table.getIndexOf(ColumnConstants.FINAL_STOCK_NAME));
							table.setValueAt(null, row, table.getIndexOf(ColumnConstants.MIX_ID));
						}
					}
				}
			}
		});
		
		getBulkTablePanel().getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				setButtonsUsability();
			}
		});
		
	}
	
	private void setButtonsUsability(){
		CheckBoxIndexColumnTable table = getBulkTablePanel().getTable();
		mixButton.setEnabled(table.getSelectedRowCount() > 0);
		unmixButton.setEnabled(table.getSelectedRowCount() > 0);
		getBulkTablePanel().getAddButton().setEnabled(table.getSelectedRowCount()>0);
		getBulkTablePanel().getDeleteButton().setEnabled(table.getSelectedRowCount()>0);
		getBulkTablePanel().getMoveUpButton().setEnabled(table.getSelectedRowCount()>0 && table.getSelectionModel().getMinSelectionIndex() !=0 );
		getBulkTablePanel().getMoveDownButton().setEnabled(table.getSelectedRowCount()>0 && table.getSelectionModel().getMaxSelectionIndex() != (table.getRowCount()-1));		
	}

	private String getResultantName(CheckBoxIndexColumnTable table) {
		JPanel popup = new JPanel(new MigLayout("insets 0, gap 5"));
		JScrollPane jsp = new JScrollPane(popup){
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(480, 320);
            }
        };
		ButtonGroup group = new ButtonGroup();
		for(int row : table.getSelectedRows()) {
			JRadioButton button = new JRadioButton(String.valueOf(table.getValueAt(row,  ColumnConstants.STOCK_NAME)));
			group.add(button);
			popup.add(button, "wrap");
			button.setSelected(true);
		}
		int option = JOptionPane.showConfirmDialog(Bulk.this, jsp, "Select stock for resultant name: ", JOptionPane.OK_CANCEL_OPTION); 
		if(option == JOptionPane.CANCEL_OPTION)
			return null;

		for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements();) {
			AbstractButton button = buttons.nextElement();
			if (button.isSelected()) {
				int count = 1;
				String stockName = button.getText()+".m";
				while(!isUnique(stockName+count)) {
					count++;
				}				
				return stockName+count;
			}
		}
		return null;
	}
	
	private boolean isUnique(String name) {
		CheckBoxIndexColumnTable table = getBulkTablePanel().getTable();
		for(int row=0; row<table.getRowCount(); row++) {
			if(name.equals(table.getValueAt(row, ColumnConstants.FINAL_STOCK_NAME)))
				return false;
		}		
        List<Stock> results = StockDAO.getInstance().findStockByName(name);
        if (results.size() > 0) {
            return false;
        }		
		return true;
	}


	private void showPopup() {
		StocksInfoPanel stockInfoPanel = CreateStocksInfoPanel.createStockInfoPanel("bulk-popup");
		TableToolBoxPanel thisPlantingPanel = stickerGenerator.getThisPlantingPanel("bulk-popup", false);
		thisPlantingPanel.setBorder(BorderFactory.createTitledBorder("Stocks from this planting: "));
		
		JPanel popupPanel = new JPanel(new MigLayout("insets 0, gapx 5"));
		popupPanel.add(stockInfoPanel, "w 70%, h 100%");
		popupPanel.add(thisPlantingPanel, " w 30%, h 100%");
		CheckBoxIndexColumnTable popupTable = stockInfoPanel.getSaveTablePanel().getTable();
		CheckBoxIndexColumnTable thisplantingTable = thisPlantingPanel.getTable();
		CheckBoxIndexColumnTable bulkTable = getBulkTablePanel().getTable();
		thisplantingTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		int w = (int) (this.getSize().getWidth()+200);
		int h = (int) this.getSize().getHeight();
		popupPanel.setSize(new Dimension(w,h));
		int option = JOptionPane.showConfirmDialog(this, popupPanel, "Choose stocks to bulk", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if(option == JOptionPane.OK_OPTION) {
			DefaultTableModel model = (DefaultTableModel) getBulkTablePanel().getTable().getModel();
			for(int row=0; row<popupTable.getRowCount(); row++) {
				String stockName = String.valueOf(popupTable.getValueAt(row, ColumnConstants.STOCK_NAME) );
				if(!StringUtils.isBlank(stockName)) {
					Object[] rowData = new Object[model.getColumnCount()];
					rowData[1] = rowCounter++;
					rowData[bulkTable.getIndexOf(ColumnConstants.SELECT)] = false;
					rowData[bulkTable.getIndexOf(ColumnConstants.STOCK_ID)] = 
							String.valueOf(popupTable.getValueAt(row, ColumnConstants.STOCK_ID) );
					rowData[bulkTable.getIndexOf(ColumnConstants.ACCESSION)] = 
							(String)popupTable.getValueAt(row, ColumnConstants.ACCESSION);
					rowData[bulkTable.getIndexOf(ColumnConstants.PEDIGREE)] = 
							(String)popupTable.getValueAt(row, ColumnConstants.PEDIGREE);
					rowData[bulkTable.getIndexOf(ColumnConstants.STOCK_NAME)] = stockName;
					//rowData[7] = nextLink++;
					model.addRow(rowData);
				}
			}
			for(int row : thisplantingTable.getSelectedRows()) {
				String stockName = String.valueOf(thisplantingTable.getValueAt(row, ColumnConstants.TAG_NAME) );
				if(!StringUtils.isBlank(stockName)) {
					Object[] rowData = new Object[model.getColumnCount()];
					rowData[1] = rowCounter++;
					rowData[bulkTable.getIndexOf(ColumnConstants.SELECT)] = false;
					rowData[bulkTable.getIndexOf(ColumnConstants.STOCK_NAME)] = stockName;
					rowData[bulkTable.getIndexOf(ColumnConstants.ACCESSION)] = 
							(String)thisplantingTable.getValueAt(row, ColumnConstants.ACCESSION);
					rowData[bulkTable.getIndexOf(ColumnConstants.PEDIGREE)] = 
							(String)thisplantingTable.getValueAt(row, ColumnConstants.PEDIGREE);
					rowData[bulkTable.getIndexOf(ColumnConstants.MATE_LINK)] = 
							thisplantingTable.getValueAt(row, ColumnConstants.MATE_LINK);
					rowData[bulkTable.getIndexOf(ColumnConstants.TAG_ID)] = 
							String.valueOf(thisplantingTable.getValueAt(row, ColumnConstants.TAG_ID) );
					//rowData[7] = nextLink++;
					model.addRow(rowData);
				}
			}
		}
		
		
		
	}

	/*private TableToolBoxPanel getThisPlantingPanel(String str) {
		List<String> columnNames = new ArrayList<String>();
		columnNames.add(ColumnConstants.SELECT);
		columnNames.add(ColumnConstants.TAG_ID);
		columnNames.add(ColumnConstants.TAG_NAME);				
		columnNames.add(ColumnConstants.ACCESSION);
		columnNames.add(ColumnConstants.PEDIGREE);
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
		for(int row=0; row<fieldTable.getRowCount(); row++) {
			if("F".equals(fieldTable.getValueAt(row, ColumnConstants.ROLE)) 
					&& fieldTable.getValueAt(row, ColumnConstants.STOCK_NAME) != null
					&& !"".equals(fieldTable.getValueAt(row, ColumnConstants.STOCK_NAME))) {
				Object rowData[] = new Object[table.getColumnCount()];
				rowData[table.getIndexOf(ColumnConstants.TAG_NAME)] = 
						fieldTable.getValueAt(row, ColumnConstants.TAG_NAME);
				rowData[table.getIndexOf(ColumnConstants.ACCESSION)] = 
						fieldTable.getValueAt(row, ColumnConstants.ACCESSION);
				rowData[table.getIndexOf(ColumnConstants.PEDIGREE)] = 
						fieldTable.getValueAt(row, ColumnConstants.PEDIGREE);
				rowData[table.getIndexOf(ColumnConstants.MATE_LINK)] = 
						fieldTable.getValueAt(row, ColumnConstants.MATE_LINK);
				rowData[table.getIndexOf(ColumnConstants.TAG_ID)] = 
						fieldTable.getValueAt(row, ColumnConstants.TAG_ID);
				((DefaultTableModel)table.getModel()).addRow(rowData);
			}
		}
		return (TableToolBoxPanel) getContext().getBean("thisPlantingStocksPanel"+str);
	}
*/
	

	private JPanel getButtonsPanel() {
		JPanel buttonsPanel = new JPanel(new MigLayout("insets 0, gapx 10"));

		importStocks = new JButton("import");
		JLabel x  = new JLabel("x");
		multiply = new JTextField(5);
		multiply.setText("1");
		multiply.setToolTipText("Multiple same mixes");
		mixButton = new JButton("Mix");
		unmixButton= new JButton("Clear Mix");
		
		mixQuantity = new JFormattedTextField(NumberFormat.getNumberInstance());
		mixQuantity.setColumns(10);
		setQuantity = new JButton("Set");
		setQuantity.setToolTipText("Set the quantity for selected stocks");
		
		populateUnits();
		setUnit = new JButton("Set");
		setUnit.setToolTipText("Set the quantity for selected stocks");
		
		setButtonsUsability();
		
		buttonsPanel.add(importStocks, "pushx");
		buttonsPanel.add(new JLabel("Quantity: "), "gapleft 5");
		buttonsPanel.add(mixQuantity);
		buttonsPanel.add(setQuantity);
		buttonsPanel.add(new JLabel("Unit: "), "gapleft 5");
		buttonsPanel.add(mixUnit);
		buttonsPanel.add(setUnit, "pushx");
		buttonsPanel.add(x);
		buttonsPanel.add(multiply);
		buttonsPanel.add(mixButton);
		buttonsPanel.add(unmixButton, "wrap");
		return buttonsPanel;
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
		
	}

	public void populateTableFromObjects(List<Object[]> crossRecordRows) {
		DefaultTableModel tableModel = ((DefaultTableModel) getBulkTablePanel().getTable().getModel());
        removeAllRowsFromTable(tableModel);	
    	for(Object[] row : crossRecordRows){
    		tableModel.addRow(row);               
    	}	
    	bulkTablePanel.getNumberOfRows().setText(String.valueOf(bulkTablePanel.getTable().getRowCount()));
		
	}
}
