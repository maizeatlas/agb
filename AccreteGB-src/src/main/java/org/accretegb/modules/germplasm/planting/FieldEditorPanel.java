package org.accretegb.modules.germplasm.planting;

import java.util.List;
import java.util.logging.Level;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.hibernate.Field;
import org.accretegb.modules.hibernate.Location;
import org.accretegb.modules.hibernate.dao.FieldDAO;
import org.accretegb.modules.hibernate.dao.LocationDAO;
import org.accretegb.modules.util.LoggerUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;


import net.miginfocom.swing.MigLayout;

public class FieldEditorPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	JLabel fieldNumberLabel;
	JLabel fieldNameLabel;
	JLabel latitudeLabel;
	JLabel longitudeLabel;
	JLabel zipcodeLabel;
	JLabel cityLabel;
	JLabel stateLabel;
	JLabel countryLabel;
	JLabel locationNameLabel;
	JLabel locationCommentLabel;
	JTextField fieldNumber;
	JTextField fieldName;
	JTextField latitude;
	JTextField longitude;
	JTextField zipcode;
	JTextField city;
	JTextField state;
	JTextField country;
	JTextField locationName;
	JTextField locationComment;
	
	Object[] input;
	Object[] output;
	int editRow;
	CheckBoxIndexColumnTable table;
	JPanel parent;
	
	public Object[] getOutput() {
		return output;
	}

	public FieldEditorPanel(JPanel parent, Object[] input, int editRow, CheckBoxIndexColumnTable table) {
		initialize();
		this.input = input;
		this.editRow = editRow;
		this.table = table;
		this.parent = parent;
		populateForm();
		populateFields();
		addListener();
		showForm();
	}

	private void showForm() {
		boolean validInput;
		do {
			int option = JOptionPane.showConfirmDialog(parent, this, "Enter New Field Information ", JOptionPane.DEFAULT_OPTION);
			validInput = true;

			if(option == JOptionPane.OK_OPTION){				
				//validate input
				if(StringUtils.isBlank(latitude.getText()) || StringUtils.isBlank(longitude.getText()))
					validInput = false;
				if(validInput) {
					output = new Object[table.getColumnCount()];
					output[table.getIndexOf(ColumnConstants.FIELD_NUMBER)] = fieldNumber.getText();
					output[table.getIndexOf(ColumnConstants.FIELD_NAME)] = fieldName.getText();
					output[table.getIndexOf(ColumnConstants.LATITUDE)] = latitude.getText();
					output[table.getIndexOf(ColumnConstants.LONGITUDE)] = longitude.getText();
					output[table.getIndexOf(ColumnConstants.ZIPCODE)] = zipcode.getText();
					output[table.getIndexOf(ColumnConstants.CITY)] = city.getText();
					output[table.getIndexOf(ColumnConstants.STATE)] = state.getText();
					output[table.getIndexOf(ColumnConstants.COUNTRY)] = country.getText();
					if(input != null) {
						output[table.getIndexOf(ColumnConstants.FIELD_ID)] = input[table.getIndexOf(ColumnConstants.FIELD_ID)];
					}
					//updateDatabase(output);
					System.out.println(updateDatabase(output).getFieldNumber());
				}                       
			}
			if(!validInput)  {                     
                JOptionPane.showConfirmDialog(parent, "<HTML><FONT COLOR = Red>*</FONT> marked fields are mandatory.</HTML>", "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
			}
		} while(!validInput);
	}
	
	private Field updateDatabase(Object[] rowData) {

		Location Location = getLocation(rowData);
		Field field;
		if(rowData[table.getIndexOf(ColumnConstants.FIELD_ID)] != null)
		{
			
			field = FieldDAO.getInstance().getField(Integer.parseInt((String)rowData[table.getIndexOf(ColumnConstants.FIELD_ID)]));
		}
		else
		{
			field = new Field();
		}
		field.setFieldNumber(StringUtils.defaultIfBlank((String) rowData[table.getIndexOf(ColumnConstants.FIELD_NUMBER)], null));
		field.setFieldName(StringUtils.defaultIfBlank((String) rowData[table.getIndexOf(ColumnConstants.FIELD_NAME)], null));
		field.setLatitude(StringUtils.defaultIfBlank((String) rowData[table.getIndexOf(ColumnConstants.LATITUDE)], null));
		field.setLongitude(StringUtils.defaultIfBlank((String) rowData[table.getIndexOf(ColumnConstants.LONGITUDE)], null));
		field.setLocation(Location);
		FieldDAO.getInstance().saveField(field);
		rowData[table.getIndexOf(ColumnConstants.FIELD_ID)] = String.valueOf(field.getFieldId());
		return field;
	}

	private Location getLocation(Object[] rowData) {
		String city = StringUtils.defaultIfBlank((String) rowData[table.getIndexOf(ColumnConstants.CITY)], null);
		String state = StringUtils.defaultIfBlank((String) rowData[table.getIndexOf(ColumnConstants.STATE)], null);
		String country = StringUtils.defaultIfBlank((String) rowData[table.getIndexOf(ColumnConstants.COUNTRY)], null);
		String zipcode = StringUtils.defaultIfBlank((String) rowData[table.getIndexOf(ColumnConstants.ZIPCODE)], null);
		try {
			List<Location> localities = LocationDAO.getInstance().findLocationByCityZipcode(city, zipcode);
			if(localities.size() > 0)
				return localities.get(0);
			return LocationDAO.getInstance().insertNewLocation( null, city, state, country, zipcode, null, null, null, null);
			
		} catch (HibernateException ex) {           
        	if(LoggerUtils.isLogEnabled())
        		LoggerUtils.log(Level.INFO, ex.toString());
        }
		return null;
	}

	private void addListener() {
		zipcode.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent arg0) {                       
				if(zipcode.getText().length() == 5)                           
					updateBasedOnZipcode();                       
			}
			public void insertUpdate(DocumentEvent arg0) {                       
				if(zipcode.getText().length() == 5)                           
					updateBasedOnZipcode();                       
			}
			public void removeUpdate(DocumentEvent arg0) {                       
				if(zipcode.getText().length() == 5)                           
					updateBasedOnZipcode();                       
			}                 
		});
	}	

	private void updateBasedOnZipcode() {
		List<Location> localities = LocationDAO.getInstance().findLocationByZipcode(zipcode.getText());
		if(localities.size() > 0) {
			Location Location = localities.get(0);
			city.setText(Location.getCity());                               
			state.setText(Location.getStateProvince());                               
			country.setText(Location.getCountry());  
		}
	}  

	private void populateForm() {
		add(fieldNumberLabel);	add(fieldNumber, "wrap");
		add(fieldNameLabel); add(fieldName, "wrap");
		add(latitudeLabel); add(latitude, "wrap");
		add(longitudeLabel); add(longitude, "wrap");
		add(zipcodeLabel); add(zipcode, "wrap");
		add(cityLabel); add(city, "wrap");
		add(stateLabel); add(state, "wrap");
		add(countryLabel); add(country, "wrap");
	}

	private void populateFields() {
		if(input != null) {
			fieldNumber.setText(String.valueOf(input[table.getIndexOf(ColumnConstants.FIELD_NUMBER)]));
			fieldName.setText(String.valueOf(input[table.getIndexOf(ColumnConstants.FIELD_NAME)]));
			latitude.setText(String.valueOf(input[table.getIndexOf(ColumnConstants.LATITUDE)]));
			longitude.setText(String.valueOf(input[table.getIndexOf(ColumnConstants.LONGITUDE)]));
			zipcode.setText(String.valueOf(input[table.getIndexOf(ColumnConstants.ZIPCODE)]));
			city.setText(String.valueOf(input[table.getIndexOf(ColumnConstants.CITY)]));
			state.setText(String.valueOf(input[table.getIndexOf(ColumnConstants.STATE)]));
			country.setText(String.valueOf(input[table.getIndexOf(ColumnConstants.COUNTRY)]));
		}
	}

	private void initialize() {
		setLayout(new MigLayout("insets 0, gapy 5"));
		fieldNumberLabel = new JLabel(ColumnConstants.FIELD_NUMBER);
		fieldNameLabel = new JLabel(ColumnConstants.FIELD_NAME);
		latitudeLabel = new JLabel("<HTML>Latitude<FONT COLOR = Red>*</FONT></HTML>");
		longitudeLabel = new JLabel("<HTML>Longitude<FONT COLOR = Red>*</FONT></HTML>");
		zipcodeLabel = new JLabel(ColumnConstants.ZIPCODE);
		cityLabel = new JLabel(ColumnConstants.CITY);
		stateLabel = new JLabel(ColumnConstants.STATE);
		countryLabel = new JLabel(ColumnConstants.COUNTRY);
		locationNameLabel = new JLabel(ColumnConstants.LOCATION_NAME);
		locationCommentLabel = new JLabel("Location Comment");
		fieldNumber = new JTextField(16);
		fieldName = new JTextField(16);
		latitude = new JTextField(16);
		longitude = new JTextField(16);
		zipcode = new JTextField(16);
		city = new JTextField(16);
		state = new JTextField(16);
		country = new JTextField(16);
		locationName = new JTextField(16);
		locationComment = new JTextField(16);
	}
	
}
