package org.accretegb.modules.germplasm.planting;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.hibernate.Field;
import org.accretegb.modules.hibernate.dao.CollectionInfoDAO;
import org.accretegb.modules.hibernate.dao.FieldDAO;
import org.accretegb.modules.hibernate.dao.MeasurementValueDAO;
import org.accretegb.modules.hibernate.dao.ObservationUnitDAO;

import net.miginfocom.swing.MigLayout;

public class FieldSelection extends JPanel{
	private TableToolBoxPanel fieldSelectionPanel;
	private String zipcode;	
	private int fieldId = -1;
	private boolean fieldChanged = false;

	public void initialize() {
        setLayout(new MigLayout("insets 10, gap 10"));
        populateFieldTable();
        addFieldTableListeners();
      	add(fieldSelectionPanel, "w 100%, h 100%, wrap");
	}
	
	private void populateFieldTable() {
	    	final CheckBoxIndexColumnTable fieldTable = getFieldSelectionPanel().getTable();
	    	List<Field> fields = FieldDAO.getInstance().getFields();
	    	fieldTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    	fieldTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    	if (fields.size() > 0) {
	    		Utils.removeAllRowsFromTable((DefaultTableModel) fieldTable.getModel());
	    	}
	    	DefaultTableModel model = (DefaultTableModel) fieldTable.getModel();
	    	for (Field field : fields) {
	    		Object[] result = getFieldRowData(field);
	    		model.addRow(result);
	    	}
	    	fieldTable.setModel(model);
			getFieldSelectionPanel().getEditButton().setEnabled(false);
			getFieldSelectionPanel().getDeleteButton().setEnabled(false);
	    }
	 
	private Object[] getFieldRowData(Field field) {
    	final CheckBoxIndexColumnTable fieldTable = getFieldSelectionPanel().getTable();
    	Object[] rowData = new Object[fieldTable.getColumnCount()];
    	rowData[fieldTable.getIndexOf(ColumnConstants.SELECT)] = false;
    	rowData[fieldTable.getIndexOf(ColumnConstants.FIELD_ID)] = field.getFieldId().toString();
    	rowData[fieldTable.getIndexOf(ColumnConstants.FIELD_NUMBER)] = field.getFieldNumber();			
    	rowData[fieldTable.getIndexOf(ColumnConstants.FIELD_NAME)] = field.getFieldName();
    	rowData[fieldTable.getIndexOf(ColumnConstants.LATITUDE)] = field.getLatitude();
    	rowData[fieldTable.getIndexOf(ColumnConstants.LONGITUDE)] = field.getLongitude();
    	rowData[fieldTable.getIndexOf(ColumnConstants.CITY)] = field.getLocation().getCity();
    	rowData[fieldTable.getIndexOf(ColumnConstants.ZIPCODE)] = field.getLocation().getZipcode();
    	rowData[fieldTable.getIndexOf(ColumnConstants.STATE)] = field.getLocation().getStateProvince();
    	rowData[fieldTable.getIndexOf(ColumnConstants.COUNTRY)] = field.getLocation().getCountry();
    	return rowData;
    }
	
	private void addFieldTableListeners() {
		addListenerToFieldTableAddButton();
		addListenerToFieldTableDeleteButton();
		addListenerToFieldTableEditButton();
		addListenerToFieldTable();
	}
	
	private void addListenerToFieldTableAddButton() {
		getFieldSelectionPanel().getAddButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CheckBoxIndexColumnTable table = getFieldSelectionPanel().getTable();
				FieldEditorPanel form = new FieldEditorPanel(FieldSelection.this, null, -1, table);
				Object[] rowData = form.getOutput();
				if(rowData == null) {
					return;
				}
				((DefaultTableModel)table.getModel()).addRow(rowData);
				table.clearSelection();
				getFieldSelectionPanel().getEditButton().setEnabled(false);
				getFieldSelectionPanel().getDeleteButton().setEnabled(false);
				zipcode = null;
			}			
		});
	}

	private void addListenerToFieldTableDeleteButton() {
		getFieldSelectionPanel().getDeleteButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CheckBoxIndexColumnTable table = getFieldSelectionPanel().getTable();
				int fieldId = Integer.parseInt(String.valueOf(table.getValueAt(table.getSelectedRow(), ColumnConstants.FIELD_ID)));
				boolean successful = deleteField(fieldId);
				if(!successful)
					JOptionPane.showConfirmDialog(FieldSelection.this, "Cannot delete field with associated records", "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
				else {
					int selectedRow = table.getSelectedRow();
					((DefaultTableModel)table.getModel()).removeRow(table.convertRowIndexToModel(selectedRow));
					table.clearSelection();
					getFieldSelectionPanel().getEditButton().setEnabled(false);
					getFieldSelectionPanel().getDeleteButton().setEnabled(false);
					zipcode = null;
				}
			}			
		});
	}
	
	private void addListenerToFieldTableEditButton() {
		getFieldSelectionPanel().getEditButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CheckBoxIndexColumnTable table = getFieldSelectionPanel().getTable();
				int selectedRow = table.getSelectedRow();				
				Object[] input = arrayFromFieldTableRow(selectedRow);				
				FieldEditorPanel form = new FieldEditorPanel(FieldSelection.this, input, selectedRow, table);
				Object[] rowData = form.getOutput();
				updateFieldTableFromArray(selectedRow, rowData);  
			}			
		});
	}
	
	private void addListenerToFieldTable() {
		getFieldSelectionPanel().getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				CheckBoxIndexColumnTable table = getFieldSelectionPanel().getTable();
				int row = table.getSelectedRows()[0];
				if(row>=0) {
					zipcode = String.valueOf(table.getValueAt(row, ColumnConstants.ZIPCODE));
					fieldId = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.FIELD_ID)));
					getFieldSelectionPanel().getEditButton().setEnabled(true);
					getFieldSelectionPanel().getDeleteButton().setEnabled(true);
					System.out.println("change zipcode");
					fieldChanged = true;
				}
				else {
					zipcode = null;
					fieldId = -1;
					getFieldSelectionPanel().getEditButton().setEnabled(false);
					getFieldSelectionPanel().getDeleteButton().setEnabled(false);
				}
			}
		});
	}
	
	

	private boolean deleteField(int fieldId) {
		
    	//find associated observation_units
    	if(ObservationUnitDAO.getInstance().getNumberOfObservationUnit(fieldId) > 0)
    		return false;

    	//find associated measurement_values
    	if(MeasurementValueDAO.getInstance().getNumberOfObservationUnit(fieldId) > 0)
    		return false;

    	//find associated div_accession_collectings
    	if(CollectionInfoDAO.getInstance().getNumberOfObservationUnit(fieldId) > 0)
    		return false;
    	
    	return FieldDAO.getInstance().deleteField(fieldId);
	}
	
	private Object[] arrayFromFieldTableRow(int selectedRow) {
		CheckBoxIndexColumnTable table = getFieldSelectionPanel().getTable();
		Object[] input = new Object[table.getColumnCount()];
		input[table.getIndexOf(ColumnConstants.FIELD_ID)] = table.getValueAt(selectedRow, ColumnConstants.FIELD_ID);
		input[table.getIndexOf(ColumnConstants.FIELD_NUMBER)] = table.getValueAt(selectedRow, ColumnConstants.FIELD_NUMBER);
		input[table.getIndexOf(ColumnConstants.FIELD_NAME)] = table.getValueAt(selectedRow, ColumnConstants.FIELD_NAME);
		input[table.getIndexOf(ColumnConstants.LATITUDE)] = table.getValueAt(selectedRow, ColumnConstants.LATITUDE);
		input[table.getIndexOf(ColumnConstants.LONGITUDE)] = table.getValueAt(selectedRow, ColumnConstants.LONGITUDE);
		input[table.getIndexOf(ColumnConstants.ZIPCODE)] = table.getValueAt(selectedRow, ColumnConstants.ZIPCODE);
		input[table.getIndexOf(ColumnConstants.CITY)] = table.getValueAt(selectedRow, ColumnConstants.CITY);
		input[table.getIndexOf(ColumnConstants.STATE)] = table.getValueAt(selectedRow, ColumnConstants.STATE);
		input[table.getIndexOf(ColumnConstants.COUNTRY)] = table.getValueAt(selectedRow, ColumnConstants.COUNTRY);
		return input;
	}
	
	private void updateFieldTableFromArray(int selectedRow, Object[] rowData) {
		CheckBoxIndexColumnTable table = getFieldSelectionPanel().getTable();
		table.setValueAt(rowData[table.getIndexOf(ColumnConstants.FIELD_NUMBER)], selectedRow, table.getIndexOf(ColumnConstants.FIELD_NUMBER));
		table.setValueAt(rowData[table.getIndexOf(ColumnConstants.FIELD_NAME)], selectedRow, table.getIndexOf(ColumnConstants.FIELD_NAME));
		table.setValueAt(rowData[table.getIndexOf(ColumnConstants.LATITUDE)], selectedRow, table.getIndexOf(ColumnConstants.LATITUDE));
		table.setValueAt(rowData[table.getIndexOf(ColumnConstants.LONGITUDE)], selectedRow, table.getIndexOf(ColumnConstants.LONGITUDE));
		table.setValueAt(rowData[table.getIndexOf(ColumnConstants.ZIPCODE)], selectedRow, table.getIndexOf(ColumnConstants.ZIPCODE));
		table.setValueAt(rowData[table.getIndexOf(ColumnConstants.CITY)], selectedRow, table.getIndexOf(ColumnConstants.CITY));
		table.setValueAt(rowData[table.getIndexOf(ColumnConstants.STATE)], selectedRow, table.getIndexOf(ColumnConstants.STATE));
		table.setValueAt(rowData[table.getIndexOf(ColumnConstants.COUNTRY)], selectedRow, table.getIndexOf(ColumnConstants.COUNTRY));
	}

	public int getFieldId() {
		return fieldId;
	}

	public void setFieldId(int fieldId) {
		this.fieldId = fieldId;
	}

	
	public TableToolBoxPanel getFieldSelectionPanel() {
			return fieldSelectionPanel;
	}

	public void setFieldSelectionPanel(TableToolBoxPanel fieldSelectionPanel) {
			this.fieldSelectionPanel = fieldSelectionPanel;
	}
	
	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	
	public boolean isFieldChanged() {
		return fieldChanged;
	}

	public void setFieldChanged(boolean fieldChanged) {
		this.fieldChanged = fieldChanged;
	}
}
