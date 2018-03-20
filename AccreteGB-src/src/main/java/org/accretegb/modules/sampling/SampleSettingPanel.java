package org.accretegb.modules.sampling;

import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;
import static org.accretegb.modules.customswingcomponent.Utils.saveTableToFile;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.CustomCalendar;
import org.accretegb.modules.customswingcomponent.ImageButton;
import org.accretegb.modules.customswingcomponent.PlotIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.TextField;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.germplasm.harvesting.StickerGenerator;
import org.accretegb.modules.germplasm.outsideseed.PersonSelectionPanel;
import org.accretegb.modules.germplasm.planting.SyncTags;
import org.accretegb.modules.germplasm.planting.TagGenerator;
import org.accretegb.modules.hibernate.Location;
import org.accretegb.modules.hibernate.dao.LocationDAO;
import org.accretegb.modules.hibernate.dao.ObservationUnitDAO;
import org.accretegb.modules.hibernate.dao.ObservationUnitSampleDAO;
import org.accretegb.modules.hibernate.dao.SourceDAO;
import org.accretegb.modules.hibernate.dao.StockDAO;
import org.accretegb.modules.util.LoggerUtils;
import org.hibernate.HibernateException;

import net.miginfocom.swing.MigLayout;

/**
 * @author Ningjing
 */
public class SampleSettingPanel extends JPanel {
	private static TableToolBoxPanel sampleSettingTablePanel;
	private LinkedHashMap<String, Object[][]> subsetTableMap = new LinkedHashMap<String, Object[][]>();
	//private Date collectionDate = null;
	private LinkedHashMap<String, HashMap<String, Object>> subsetInfo = new LinkedHashMap<String,HashMap<String, Object>>();
	private LinkedHashMap<String, Integer> prefixIndex = new LinkedHashMap<String, Integer>();
	private JTextField setSampleNames = new JTextField(30);
	private HashMap<String, Integer> nameSourceid= new HashMap<String, Integer>();
	private LinkedHashMap<String, String> subsetCommentMap = new LinkedHashMap<String, String>();
	private String location = "";
	private String currentSubset = null;
	private JProgressBar progress ;
	public boolean synced = false;
	
	public void initialize() {
		setLayout(new MigLayout("insets 10, gap 10"));
		JPanel bottomPanel = new JPanel(new MigLayout("insets 0, gap 0"));
		add(bottomPanel, "w 100%, gaptop 30, growy, spany, pushy, wrap");
		bottomPanel.add(getSamplesSetterPanel(), "w 100%, wrap");
		bottomPanel.add(sampleSettingTablePanel, "w 100%, h 100%, wrap");
		add(bottomPanel, "w 100%, gaptop 30, growy, spany, pushy, wrap");
		progress = new JProgressBar(0, 100);
		progress.setVisible(false);
		bottomPanel.add(progress, "w 100%, hidemode 3, wrap");
		initializeSubsetComboBox();
		setExportButtonsPanel();
		setSyncButton();
		
	}
	
	public void setZipcode(){
		String zipcode = ((String) getSampleSettingTablePanel().getTable().getValueAt(0,
									getSampleSettingTablePanel().getTable().getIndexOf(ColumnConstants.TAG))).split("\\.")[2];
		System.out.println(zipcode);
		location = LocationDAO.getInstance().findLocationByZipcode(zipcode).get(0).getStateProvince();
		System.out.println("?? " + location);
	}
	
	public void populateSelectionSubset(String subsetName){
		//System.out.println("populateSelectionSubset");
		Object[][] subsetData =	(Object[][]) getSubsetTableMap().get(subsetName);
		CheckBoxIndexColumnTable table = getSampleSettingTablePanel().getTable();
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Utils.removeAllRowsFromTable((DefaultTableModel)model);
		if (subsetData != null){
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
		}
		setZipcode();
		updateNumofItems();
		updateSettingTableSubset(subsetName);
		populateSettingSubset(subsetName);
		
	}
	
	public void populateSettingSubset(String subsetName){
		Object[][] subsetData =	(Object[][]) getSubsetTableMap().get(subsetName);
		CheckBoxIndexColumnTable table = getSampleSettingTablePanel().getTable();
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Utils.removeAllRowsFromTable((DefaultTableModel)model);
		//System.out.println( " table column count "+model.getColumnCount());
		if (subsetData != null){
			//System.out.println( " data length "+subsetData.length);
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
		}
		updateNumofItems();
		table.setHasSynced(synced);
	}
	
	public void updateAllSamplaNamesSubsets(){
		//System.out.println("SET BY updateAllSamplaNamesSubsets");
		final CheckBoxIndexColumnTable table = getSampleSettingTablePanel().getTable();
		for(String subsetName : subsetInfo.keySet()){
			if(!currentSubset.equals(subsetName)){
				String prefix = (String) subsetInfo.get(subsetName).get("prefix");
				Date collectionDate = (Date) subsetInfo.get(subsetName).get("date");
				SimpleDateFormat sdf = new SimpleDateFormat("MMddYY");
				String dateString = sdf.format(collectionDate);
				prefixIndex.put(prefix+"-"+dateString, 1);
			}
			
		}
		for(String subsetName : subsetTableMap.keySet()){
			if(!currentSubset.equals(subsetName)){
				Object[][] subsetData =	(Object[][]) getSubsetTableMap().get(subsetName);
				if (subsetData != null){
					int rows = subsetData.length;
					String prefix = (String) subsetInfo.get(subsetName).get("prefix");
					Date collectionDate = (Date) subsetInfo.get(subsetName).get("date");
					SimpleDateFormat sdf = new SimpleDateFormat("MMddYY");
					String dateString = sdf.format(collectionDate);
					int cols = subsetData[0].length;
					for(int row = 0; row < rows; row ++){
						for(int col = 0 ; col < cols; ++col){
							if (col == table.getIndexOf(ColumnConstants.SAMPLENAME)){
								subsetData[row][col] = prefix+"."+dateString+"."+String.valueOf(prefixIndex.get(prefix+"-"+dateString)+row);
							}
						}
					}
					int updatedIndex = prefixIndex.get(prefix+"-"+dateString) + rows;
					prefixIndex.put(prefix+"-"+dateString, updatedIndex);
					
				}
			}
		}
		
	}
	
	public void updateSettingTableSubset(String subsetName){
		//System.out.println("SET BY updateSettingTableSubset" + subsetName);
		final CheckBoxIndexColumnTable table = getSampleSettingTablePanel().getTable();
		Object subsetData[][] = new Object[table.getRowCount()][table.getColumnCount()];
		if(subsetInfo.size()>=1){
			if(subsetInfo.get(subsetName).get("date") == null){
				subsetInfo.get(subsetName).put("date",new Date());
			}
			Date collectionDate = (Date) subsetInfo.get(subsetName).get("date");
			SimpleDateFormat sdf = new SimpleDateFormat("MMddYY");
			String dateString = sdf.format(collectionDate);
			if((String) subsetInfo.get(subsetName).get("prefix") == null){
				subsetInfo.get(subsetName).put("prefix","sample");
			}
			String prefix = (String) subsetInfo.get(subsetName).get("prefix");
			int indexDB = ObservationUnitSampleDAO.getInstance().getSeasonIndex(dateString, prefix);
			int indexLocal = 1;
			if(prefixIndex.containsKey(prefix+"-"+dateString)){
				 indexLocal = prefixIndex.get(prefix+"-"+dateString);
			}
			int index = indexDB <= indexLocal ? indexLocal : indexDB;
			for(int row = 0; row < table.getRowCount(); row++){
				subsetData[row][0] = new Boolean(false);
				for(int col = 1; col < table.getColumnCount(); col++){
					if(col == table.getIndexOf(ColumnConstants.COLLECTION_DATE)){
						subsetData[row][col] = collectionDate;
					}
					else if (col == table.getIndexOf(ColumnConstants.SAMPLENAME)){
						subsetData[row][col] = prefix+"."+dateString+"."+String.valueOf(index+row);
					}
					else if(col == table.getIndexOf(ColumnConstants.LOCATION)){
						subsetData[row][col] = location;
					}
					else{
						subsetData[row][col] = table.getValueAt(row, col);
					}
				}						
			}
			//System.out.println("???"+subsetData.length);
			getSubsetTableMap().put(subsetName, subsetData);
			prefixIndex.put(prefix+"-"+dateString, index + table.getRowCount());
		}
		
		table.setHasSynced(false);
		synced = false;
	}
	
	
	
	public void setNewSampleName(String prefix, Date date, int row, int iter){
		//System.out.println("SET BY setNewSampleName" + prefix);
		SimpleDateFormat sdf = new SimpleDateFormat("MMddYY");
		String dateString = sdf.format(date);
		CheckBoxIndexColumnTable table = getSampleSettingTablePanel().getTable();
		int indexDB = ObservationUnitSampleDAO.getInstance().getSeasonIndex(dateString, prefix);
		int indexLocal = -1;
		 if(prefixIndex.containsKey(prefix+"-"+dateString)){
			 indexLocal = prefixIndex.get(prefix+"-"+dateString);
		 }  
		int index = indexDB < indexLocal ? indexLocal : indexDB;
		table.setValueAt(prefix+"."+dateString+"."+String.valueOf(index+iter), row, table.getIndexOf(ColumnConstants.SAMPLENAME));
		prefixIndex.put(prefix+"-"+dateString, index+iter);
	}
	
	public void initializeSubsetComboBox(){
		final JComboBox subset = getSampleSettingTablePanel().getTableSubset();
		subset.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED)
				{	
					String selected = subset.getSelectedItem().toString();
					currentSubset = selected;
					populateSettingSubset(selected);
					//System.out.println(selected + " " +subsetInfo.get(selected));
					if(subsetInfo.get(selected) != null)
					{
						setSampleNames.setText((String) subsetInfo.get(selected).get("prefix"));
					}else{
						setSampleNames.setText("sample");
					}
					
				}
			}
		});
	}
	
	
	 private JPanel getSamplesSetterPanel() {
			JPanel samplesSetterPanel = new JPanel(new MigLayout("insets 0, gapx 0"));
			//addUseDefaultSampleNames(samplesSetterPanel);
			addImportToSaveTablePanel(samplesSetterPanel);
			addSampleNamesSetting(samplesSetterPanel);
			addDateSection(samplesSetterPanel);
			addCollector(samplesSetterPanel);
			return samplesSetterPanel;
		}	

	 
	 private void addSampleNamesSetting(JPanel samplesSetterPanel) {
			JPanel subpanel = new JPanel(new MigLayout("insets 0, gapx 0"));
			final CheckBoxIndexColumnTable table = getSampleSettingTablePanel().getTable();
			JLabel subsetPrefix = new JLabel("Subset Prefix: ");
			JButton setSampleNamesButton = new ImageButton("checkmarkColor.png");;
			setSampleNames.setToolTipText("Customize prefix for sample names");
			setSampleNamesButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String value = setSampleNames.getText();
					subsetInfo.get(currentSubset).put("prefix", value);
					updateSampleNames(value);
					updateAllSamplaNamesSubsets();
					updateSettingTableSubset(currentSubset);
						
				}
			});
			
			if(currentSubset !=null)
			{     if(!subsetInfo.get(currentSubset).containsKey("prefix")){
	            	subsetInfo.get(currentSubset).put("prefix", "sample");
	            }
				setSampleNames.setText((String) subsetInfo.get(currentSubset).get("prefix"));
			}else{
				setSampleNames.setText("sample");
			}
			
			subpanel.add(subsetPrefix);
			subpanel.add(setSampleNames);
			subpanel.add(setSampleNamesButton, "gapleft 3,h 24:24:24, w 24:24:24");
			samplesSetterPanel.add(subpanel, "gapleft 10, w 33%, pushx");
		}
	 
	 private void updateSampleNames(String prefix){
		 //System.out.println("SET BY updateSampleNames" + prefix);
		 SimpleDateFormat sdf = new SimpleDateFormat("MMddYY");
		 String currentSubset = (String)getSampleSettingTablePanel().getTableSubset().getSelectedItem();
		 Date collectionDate = (Date) subsetInfo.get(currentSubset).get("date");
		 String dateString = sdf.format(collectionDate);
		 int indexDB = ObservationUnitSampleDAO.getInstance().getSeasonIndex(dateString, prefix);
		 int indexLocal = -1;
		 if(prefixIndex.containsKey(prefix+"-"+dateString)){
			 indexLocal = prefixIndex.get(prefix+"-"+dateString);
		 }  
		 int index = indexDB < indexLocal ? indexLocal : indexDB;
		 CheckBoxIndexColumnTable table = getSampleSettingTablePanel().getTable();		 
		 for (int row = 0; row < table.getRowCount(); row++){
			table.setValueAt(prefix+"."+dateString+"."+String.valueOf(index+row), row, table.getIndexOf(ColumnConstants.SAMPLENAME));	
		}
		 
		 updateSettingTableSubset(currentSubset);
	 }
	
	 private void addDateSection(JPanel samplesSetterPanel) {
			JPanel subpanel = new JPanel(new MigLayout("insets 0, gapx 0"));
			final CheckBoxIndexColumnTable table = getSampleSettingTablePanel().getTable();
			final CustomCalendar calendar = new CustomCalendar();
			JButton addDates = new ImageButton("checkmarkColor.png");;
			addDates.setToolTipText("Apply this date to current subset");
			addDates.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int column = table.getIndexOf(ColumnConstants.COLLECTION_DATE);
					Date date = calendar.getCustomDateCalendar().getDate();
					date = date == null ? new Date() : date;
					
					for(int row = 0; row < table.getRowCount(); ++row){
						table.setValueAt(date, row, column);
					}
					String currentSubset = (String)getSampleSettingTablePanel().getTableSubset().getSelectedItem();
					String prefix = (String) subsetInfo.get(currentSubset).get("prefix");
					subsetInfo.get(currentSubset).put("date", date);
					updateSampleNames(prefix);
					updateAllSamplaNamesSubsets();
					updateSettingTableSubset(currentSubset);
					
				}			
			});
			subpanel.add(new JLabel("Collection Date: "),"gapleft 15");
			subpanel.add(calendar.getCustomDateCalendar());
			subpanel.add(addDates,"gapleft 3" );
			samplesSetterPanel.add(subpanel, "w 33%, pushx");
		}
	 
	 private String getNewSource(){
		 
		 JPanel sourcePanel = new JPanel(new MigLayout("insets 0, gapx 0"));
		 String personName = null;
         String labelNames[] = { "<HTML>First Name<FONT COLOR = Red>*" + "</FONT></HTML>","<HTML>Last Name<FONT COLOR = Red>*" + "</FONT></HTML>","Institute","Department","Street Address",ColumnConstants.CITY, ColumnConstants.STATE,
                 ColumnConstants.COUNTRY, "<HTML>Zipcode<FONT COLOR = Red>*" + "</FONT></HTML>",ColumnConstants.LOCATION_NAME, "Location Comment","Phone Number", "Fax", "Email", "URL", "Source Comment" };

         String values[] = new String[labelNames.length];
         for(int i = 0; i<labelNames.length; ++i){
         	values[i] = "";
         }

         boolean validInput = false;
         JLabel labels[] = new JLabel[labelNames.length];
         final JTextField textBoxes[] = new JTextField[labelNames.length];
         int labelsOrder[] = { 0, 1, 2, 3, 4, 8, 5, 6, 7, 9, 10, 11, 12, 13, 14,15};

         for (int labelIndex : labelsOrder) {
             labels[labelIndex] = new JLabel(labelNames[labelIndex]);
             sourcePanel.add(labels[labelIndex], "gapleft 10, push");
             textBoxes[labelIndex] = new JTextField();
             textBoxes[labelIndex].setPreferredSize(new Dimension(200, 0));
             textBoxes[labelIndex].setText(values[labelIndex]);
             sourcePanel.add(textBoxes[labelIndex], "gapRight 10, wrap");
         }

         textBoxes[8].getDocument().addDocumentListener(new DocumentListener() {

             public void changedUpdate(DocumentEvent arg0) {
                 if (textBoxes[8].getText().length() == 5)
                     updateBasedOnZipcode();
             }

             public void insertUpdate(DocumentEvent arg0) {
                 if (textBoxes[8].getText().length() == 5)
                     updateBasedOnZipcode();
             }

             public void removeUpdate(DocumentEvent arg0) {
                 if (textBoxes[8].getText().length() == 5)
                     updateBasedOnZipcode();
             }

             private void updateBasedOnZipcode() {

                 try {                         
                     List<Location> LocationList = LocationDAO.getInstance().findLocationByZipcode(textBoxes[8].getText());
                     if (LocationList.size() > 0) {
                         textBoxes[5].setText((String) LocationList.get(0).getCity());
                         textBoxes[6].setText((String) LocationList.get(0).getStateProvince());
                         textBoxes[7].setText((String) LocationList.get(0).getCountry());
                     }
                 } catch (HibernateException ex) {
                     if (LoggerUtils.isLogEnabled()) {
                         LoggerUtils.log(Level.INFO, ex.toString());
                     }
                 } 
             }
         });
         for (int textBoxIndex = 0; textBoxIndex < labelNames.length; textBoxIndex++)
         {
        	 textBoxes[textBoxIndex].setText(values[textBoxIndex]);
         }

         int option = JOptionPane.showConfirmDialog(this, sourcePanel,"Enter New Source Information ",JOptionPane.DEFAULT_OPTION);

         validInput = true;

         if (option == JOptionPane.OK_OPTION) {

             for (int columnIndex = 0; columnIndex < labelNames.length; columnIndex++) {
                 values[columnIndex] = textBoxes[columnIndex].getText().equals("") ? "NULL" : textBoxes[columnIndex].getText();
                 if (labelNames[columnIndex].indexOf('*') != -1
                         && textBoxes[columnIndex].getText().equals("")) {
                     validInput = false;
                 }
             }
             if (validInput) {
             	Location location = manageLocation(values);
                 int newSourceId = SourceDAO.getInstance().insert(values,location);
                 personName = String.valueOf(String.valueOf(values[0] + " " + values[1]));
                 nameSourceid.put(personName, newSourceId);
             }else{
            	 JOptionPane.showConfirmDialog(
                                     null,
                                     "<HTML><FONT COLOR = Red>*</FONT> marked fields are mandatory.</HTML>",
                                     "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
             }
             
         }
         //System.out.println("Press cancel, validInput = " + validInput);
     
         return personName;
     
	 }
	 
	 private Location manageLocation(String[] values) {

	        Location location = LocationDAO.getInstance().findLocation(values[9], values[5], values[6], values[7], values[8], values[10]);
	        if(location == null){
	        	location = LocationDAO.getInstance().insertNewLocation(values[9], values[5], values[6], values[7], values[8], values[10],null, null, null);
	        }
	        return location;
	    }
	 
	 private void addCollector(JPanel samplesSetterPanel) {
			JPanel subpanel = new JPanel(new MigLayout("insets 0, gapx 0"));
			final CheckBoxIndexColumnTable table = getSampleSettingTablePanel().getTable();
			JLabel collectorLabel = new JLabel("Collector: ");
			JButton setCollector = new ImageButton("checkmarkColor.png");
			final JComboBox collectors = new JComboBox();
			collectors.addItem("Select");
			collectors.addItem("Add New");
			for(Object[] s : SourceDAO.getInstance().getSourceNames()){
				if(!nameSourceid.containsKey(String.valueOf(s[0]))){
					collectors.addItem(String.valueOf(s[0]));
					nameSourceid.put(String.valueOf(s[0]), (Integer) s[1]);
				}
				
			}
			collectors.addItemListener(new ItemListener(){

				public void itemStateChanged(ItemEvent e) {
					if(collectors.getSelectedItem().equals("Add New")){
						String newName = getNewSource();
						if(newName!=null)
						{
							collectors.addItem(newName);
							collectors.setSelectedItem(newName);
						}else{
							collectors.setSelectedItem("Select");
						}
						
					}
					
				}
				
			});
			setCollector.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(!collectors.getSelectedItem().equals("Select"))
					{
						for(int row: table.getSelectedRows()) {
							table.setValueAt(collectors.getSelectedItem(), row, table.getIndexOf(ColumnConstants.COLLECTOR));
						}							
					}	
					updateSettingTableSubset(currentSubset);
				}
			});
			
			
			subpanel.add(collectorLabel,"gapleft 15");
			subpanel.add(collectors);
			subpanel.add(setCollector, "gapleft 3, gapright 5, h 24:24:24, w 24:24:24");
			samplesSetterPanel.add(subpanel);
		}
	 
	 
	 private void saveSubsetsToFile(String fileName, Boolean printTable) {
		 	CheckBoxIndexColumnTable table = getSampleSettingTablePanel().getTable();
		 	JPanel panel = this.getSamplesSetterPanel();
	        try {
	            if (table == null || fileName == null) {
	                return;
	            }
	            JFileChooser fileChooser = new JFileChooser();
	            File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
	            fileChooser.setSelectedFile(file);
	            int approve = fileChooser.showSaveDialog(panel);
	            if (approve != JFileChooser.APPROVE_OPTION) {
	                return;
	            }
	            file = fileChooser.getSelectedFile();
	            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	            String[] colNames = {ColumnConstants.TAG, ColumnConstants.PLANT, ColumnConstants.ACCESSION, ColumnConstants.PEDIGREE,
			              ColumnConstants.STOCK_NAME, ColumnConstants.LOCATION,ColumnConstants.COLLECTION_DATE, 
			              ColumnConstants.COLLECTOR};
	          //write the column names first
	            if(printTable){
		            for (int columnCounter = 0; columnCounter < table.getColumnCount(); columnCounter++) {
		                writer.write(table.getColumnName(columnCounter));
		                writer.write(",");
		            }
	            }else{
	            	for(String colName:colNames){
	            		 writer.write(colName);
			             writer.write(",");
	            	}
	            }
	            writer.newLine();
	            for(String subsetName : subsetTableMap.keySet()){
	            	Object[][] subsetData =	(Object[][]) getSubsetTableMap().get(subsetName);
	 				writer.write(">>>> Subset: "+subsetName + ". Subset Comment: "+ getSubsetCommentMap().get(subsetName));
	 				if (subsetData != null){
	 					int rows = subsetData.length;
	 					int cols = subsetData[0].length;
	 					for(int row = 0; row < rows; row ++){
	 						writer.newLine();
	 						if(printTable){
		 						for(int col = 0 ; col < cols; ++col){
		 							String value = "";
		 							if (subsetData[row][col] instanceof java.sql.Timestamp) {
		 		                        java.sql.Timestamp timeStamp = (java.sql.Timestamp) subsetData[row][col];
		 		                        value = (timeStamp.getYear() + 1900) + "-" + (timeStamp.getMonth() + 1) + "-"
		 		                                + timeStamp.getDate();
		 		                    } else {
		 		                        value = String.valueOf(subsetData[row][col]);
		 		                    }
		 							writer.write(value);
		 		                    writer.write(",");
		 						}
	 						}else{
	 							
	 							for(String colName :colNames ){
	 								int col = table.getIndexOf(colName);
		 							String value = "";
		 							if (subsetData[row][col] instanceof java.sql.Timestamp) {
		 		                        java.sql.Timestamp timeStamp = (java.sql.Timestamp) subsetData[row][col];
		 		                        value = (timeStamp.getYear() + 1900) + "-" + (timeStamp.getMonth() + 1) + "-"
		 		                                + timeStamp.getDate();
		 		                    } else {
		 		                        value = String.valueOf(subsetData[row][col]);
		 		                    }
		 							writer.write(value);
		 		                    writer.write(",");
		 						}
	 						}
	 					}
	 					
	 				}
	 				 writer.newLine();
	            }
	            writer.close();
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    }

	 private void setExportButtonsPanel() {
			JPanel exportButtonsPanel = getSampleSettingTablePanel().getBottomHorizonPanel();
			JButton printLabel = new JButton("Print Labels");
			JButton printTable = new JButton("Print table");
			
			
			printTable.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					try {
						saveSubsetsToFile("samples_table.csv", true);
					} catch (Exception e) {
						if (LoggerUtils.isLogEnabled()) {
							LoggerUtils.log(Level.INFO,  e.toString());
						}
						JLabel errorFields = new JLabel(
								"<HTML><FONT COLOR = Blue>Failed to create file. File might be already open or<br>you do not have permission to write in that folder </FONT></HTML>");
						JOptionPane.showMessageDialog(SampleSettingPanel.this, errorFields);					
					}
				}
			});
			
			printLabel.addActionListener(new ActionListener() {			
				public void actionPerformed(ActionEvent actionEvent) {
					try {
						saveSubsetsToFile("sample_labels.csv", false);
					} catch (Exception e) {
						if (LoggerUtils.isLogEnabled()) {
							LoggerUtils.log(Level.INFO,  e.toString());
						}
						JLabel errorFields = new JLabel(
								"<HTML><FONT COLOR = Blue>Failed to create file. File might be already open or<br>you do not have permission to write in that folder </FONT></HTML>");
						JOptionPane.showMessageDialog(SampleSettingPanel.this, errorFields);					
					}
				}
			});
			
			exportButtonsPanel.add(printTable,"pushx, al right");
			exportButtonsPanel.add(printLabel,"wrap");
	}
	
	public void setSyncButton(){
		getSampleSettingTablePanel().getRefreshButton().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent actionEvent) {
				CheckBoxIndexColumnTable table = getSampleSettingTablePanel().getTable();
				System.out.println(table.getModel().getColumnCount() + "," + table.getColumnNames());
				progress.setVisible(true);
				boolean flag = true;
				for(String subsetName : getSubsetTableMap().keySet()){
		        	Object[][] subsetData =	(Object[][])getSubsetTableMap().get(subsetName);
						if (subsetData != null){
							int rows = subsetData.length;
							System.out.println("subset rows " + rows);
							for(int row = 0; row < rows; row ++){
								System.out.println("row " + row);
								int collector_index = table.getIndexOf(ColumnConstants.COLLECTOR);
								
								if(String.valueOf(subsetData[row][collector_index]).equalsIgnoreCase("null"))
								{
									flag = false;
								}
							}
						}
				}
				if(flag){
					new syncSampling(progress, SampleSettingPanel.this).execute();
				}else{
					JOptionPane.showConfirmDialog(null, "Please check the subsets. Collector can not be null","Error", JOptionPane.CLOSED_OPTION);
					progress.setVisible(false);
				}
				
			}
			
		});
	}
	
	public void finishedSync(boolean rollbacked) {
		getSampleSettingTablePanel().getTable().setHasSynced(true);
		synced = true;
		progress.setVisible(false);
	}
	
	
	private void addImportToSaveTablePanel(JPanel samplesSetterPanel){
    	JButton importStocks = new JButton("Import Tags");
    	importStocks.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				importButtonActionPerformed();
			}				
    		
		});
    	samplesSetterPanel.add(importStocks, "gapleft 10");
    }
    	
    private void importButtonActionPerformed(){
    	ArrayList<String> tagnames = new ArrayList<String>();
    	try {
			final JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter("Comma separated files(.csv)", "csv"));
			int status = fc.showOpenDialog(this);				
			if (fc.getSelectedFile() == null)
			{
				return;
			}
			String filename = fc.getSelectedFile().toString();
			if (status == JFileChooser.APPROVE_OPTION) {
				
				if (!(filename.trim().endsWith(".csv"))) {
					JLabel errorFields = new JLabel("<HTML><FONT COLOR = Blue>"
							+ "File should have a .csv extension only"
							+ ".</FONT></HTML>");
					JOptionPane.showMessageDialog(null,errorFields);
				} else {
					BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
					String line = br.readLine();
					
					while (line != null) {
						tagnames.add(line.trim());					
						line = br.readLine();
					}
					br.close();		
				}
			}
			
		}catch (Exception E) {
				System.out.println(E.toString());
				E.printStackTrace();
		}
		    
					
		getSubsetTableMap().clear();
		subsetInfo.clear();
		getSubsetCommentMap().clear();
		prefixIndex.clear();
		
		HashMap<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("prefix", "sample");
		tmp.put("date", new Date());
		subsetInfo.put("import", tmp);
		
		DefaultTableModel model = (DefaultTableModel) getSampleSettingTablePanel().getTable().getModel();
		Utils.removeAllRowsFromTable(model);
		
		//populate saveTable
		List<Object []> results = ObservationUnitDAO.getInstance().searchByTags(tagnames);
		int index = 0;
		for(Object[] result : results){
			Object[] newRow = new Object[getSampleSettingTablePanel().getTable().getColumnCount()];
			newRow[0] = new Boolean(false);
			newRow[1] = index++;
			for(int i = 2; i<result.length;++i){
				if(String.valueOf(result[i]).equals("1")){
					newRow[i] = null;
				}else{
					newRow[i] = result[i];
				}
			}
			model.addRow(newRow);
		}
		getSampleSettingTablePanel().getTable().setModel(model);
		updateSettingTableSubset("import");
		populateSettingSubset("import");
		setZipcode();
		getSampleSettingTablePanel().getTableSubset().removeAllItems();
        getSampleSettingTablePanel().getTableSubset().addItem("import");
		getSampleSettingTablePanel().getTableSubset().setSelectedItem("import");
    }
	
	public void updateNumofItems(){
		getSampleSettingTablePanel().getNumberOfRows().setText(String.valueOf(getSampleSettingTablePanel().getTable().getRowCount()));
	}
	public static TableToolBoxPanel getSampleSettingTablePanel() {
		return sampleSettingTablePanel;
	}

	public static void setSampleSettingTablePanel(TableToolBoxPanel sampleSettingTablePanel) {
		SampleSettingPanel.sampleSettingTablePanel = sampleSettingTablePanel;
	}

	public LinkedHashMap<String, Object[][]> getSubsetTableMap() {
		return subsetTableMap;
	}

	public void setSubsetTableMap(LinkedHashMap<String, Object[][]> subsetTableMap) {
		this.subsetTableMap = subsetTableMap;
	}
	
	public LinkedHashMap<String, HashMap<String, Object>> getSubsetInfo() {
		return subsetInfo;
	}

	public void setSubsetInfo(LinkedHashMap<String, HashMap<String, Object>> subsetInfo) {
		this.subsetInfo = subsetInfo;
	}
	public LinkedHashMap<String, String> getSubsetCommentMap() {
		return subsetCommentMap;
	}

	public void setSubsetCommentMap(LinkedHashMap<String, String> subsetCommentMap) {
		this.subsetCommentMap = subsetCommentMap;
	}
	
	public LinkedHashMap<String, Integer> getPrefixIndex() {
		return prefixIndex;
	}

	public void setPrefixIndex(LinkedHashMap<String, Integer> prefixIndex) {
		this.prefixIndex = prefixIndex;
	}

	public HashMap<String, Integer> getNameSourceid() {
		return nameSourceid;
	}

	public void setNameSourceid(HashMap<String, Integer> nameSourceid) {
		this.nameSourceid = nameSourceid;
	}

	

}
