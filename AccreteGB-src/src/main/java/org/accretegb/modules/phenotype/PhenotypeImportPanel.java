package org.accretegb.modules.phenotype;

import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.DateFormatter;
import javax.swing.text.JTextComponent;

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.customswingcomponent.CustomCalendar;
import org.accretegb.modules.customswingcomponent.ImageButton;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.MeasurementParameter;
import org.accretegb.modules.hibernate.MeasurementUnit;
import org.accretegb.modules.hibernate.dao.MeasurementValueDAO;
import org.accretegb.modules.hibernate.dao.MeasurementParameterDAO;
import org.accretegb.modules.hibernate.dao.ObservationUnitDAO;
import org.accretegb.modules.hibernate.dao.MeasurementUnitDAO;
import org.accretegb.modules.phenotype.PhenotypeExportPanel.CustomCellEditor;
import org.accretegb.modules.util.LoggerUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

/**
 * This panel used to import file that collects phenotype data. The file
 * was a template exported from export panel.
 * User can add new parameter by clicking "add new parameter "button 
 * or be required to complete new parameter info when click "sync".
 * User can specify tom for each phenotype parameter of each plant
 * @author Ningjing
 *
 */
public class PhenotypeImportPanel extends JPanel {
	
	private JComboBox<String> uniTypeInfo;
	private JPanel importTablePanel;
	private JTable importTable;
	private JButton hideOrShowCol = new JButton("Show Columns");
	private JPopupMenu columnsPopupMenu = new JPopupMenu();
	private LinkedHashMap<String,Integer> parameterNameIDMap;
	private LinkedHashMap<String,List<String>> parameterNameValuesMap;
	private static String descriptors[] = {"observation_unit_id","tagname","stock_name","coordinate_x", "coordinate_y", "coordinate_z", "plot", "row","plant","purpose",
			"planting_date","harvest_date","delay","city","state_province","country","zipcode","field_name","field_number",
			"altitude","latitude","longitude", "pedigree","generation","cycle","mating_type","mate_role",
			"mate_method_name","mate_method_desc","exp_name","exp_factor_name","exp_factor_type","exp_factor_value_level","measurement_value_id"
			};

	private List<String> paramersImported = new ArrayList<String>();
	private List<String> newParameters = new ArrayList<String>();;
	private List<Integer> paramersColIndexes = new ArrayList<Integer>();
	private List<Integer> tomColIndexes = new ArrayList<Integer>();
	private List<Integer> valueChangedRows = new ArrayList<Integer>();
	private JTable currentTable;
	boolean hasSynced = false;
	private JProgressBar progress = new JProgressBar(0, 100);
	private JLabel numberofRows;

	public void initialize() {
		
		createUnitTypeJCombobox();
		createTablePanel();								
		setLayout(new MigLayout("insets 20, gap 0"));	
		add(getToolPanel(),"span, w 100%, wrap");
		add(getImportTablePanel(),"span, w 100%, h 100%");
		progress.setVisible(false);
		add(progress, "w 100%,span, hidemode 3");
		
	}
	private JPanel getToolPanel(){
		JPanel toolPanel = new JPanel(new MigLayout("insets 0, gap 0"));
		JPanel firstRow = new JPanel(new MigLayout("insets 0, gap 0"));;		
		addNewParameterButton(firstRow);
		addImportFileButton(firstRow);	
		toolPanel.add(firstRow,"wrap");
		
		JPanel secondRow = new JPanel(new MigLayout("insets 0, gap 0"));;	
		addSelectionPanel(secondRow);
		addDate(secondRow);
		secondRow.add(getHideOrShowCol(),"pushx, al right");	
		addSyncButton(secondRow);		
		toolPanel.add(secondRow,"w 100%, wrap");
		
		return toolPanel;
	}
	private void addDate(final JPanel toolPanel) {
		final CustomCalendar calendar = new CustomCalendar();
		ImageButton addDates = new ImageButton("checkmarkColor.png");
		addDates.setToolTipText("Apply this date to tom");
		toolPanel.add(new JLabel("tom:  "), "gapleft 5");
		toolPanel.add(calendar.getCustomDateCalendar());
		final SpinnerDateModel hms = getSpinner(toolPanel);
		addDates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int column = getImportTable().getSelectedColumn();
				if(!getImportTable().getColumnName(0).equals("") && getTomColIndexes().contains(column))
				{
					Date date = calendar.getCustomDateCalendar().getDate();			
					date.setHours(hms.getDate().getHours());
					date.setMinutes(hms.getDate().getMinutes());
					date.setSeconds(hms.getDate().getSeconds());
					if(getImportTable().getSelectedRows().length == 0){
						for(int row = 0; row < getImportTable().getRowCount();++row){
							getImportTable().setValueAt(date, row, column);
						}
					}else{
						for(int row: getImportTable().getSelectedRows()) {
							getImportTable().setValueAt(date, row, column);
						}
					}
					
				}
			}			
		});		
		toolPanel.add(addDates, "h 24:24:24, w 24:24:24");
	
	}
	
	private SpinnerDateModel getSpinner(JPanel toolPanel){
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, 24); // 24 == 12 PM == 00:00:00
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        SpinnerDateModel model = new SpinnerDateModel();
        model.setValue(calendar.getTime());
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "HH:mm:ss");
        DateFormatter formatter = (DateFormatter)editor.getTextField().getFormatter();
        formatter.setAllowsInvalid(false); 
        formatter.setOverwriteMode(true);

        spinner.setEditor(editor);
        toolPanel.add(spinner);
		return model;
		
	}
	
	private void addSelectionPanel(JPanel toolPanel ) {
		JPanel selectionPanel = new JPanel();
		final JLabel all = new JLabel("<HTML><FONT color = #00A0FF>all</FONT></HTML>");		
		JLabel separator = new JLabel(" | ");		
		final JLabel none = new JLabel("<HTML><FONT color = #00A0FF>none</FONT></HTML>");
		all.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent arg0) {				
				all.setText("<HTML><FONT color = #00A0FF><U>all<U></FONT></HTML>");					
			}
			@Override
			public void mouseExited(MouseEvent arg0) {				
				all.setText("<HTML><FONT color = #00A0FF>all</FONT></HTML>");					
			}
			@Override
			public void mouseClicked(MouseEvent arg0) {				
				getImportTable().selectAll();
			}			
		});
		
		none.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent arg0) {				
				none.setText("<HTML><FONT color = #00A0FF><U>none<U></FONT></HTML>");				
			}
			@Override
			public void mouseExited(MouseEvent arg0) {				
				none.setText("<HTML><FONT color = #00A0FF>none</FONT></HTML>");					
			}
			@Override
			public void mouseClicked(MouseEvent arg0) {		
				getImportTable().clearSelection();
			}			
		});
		selectionPanel.add(all);
		selectionPanel.add(separator);
		selectionPanel.add(none);
		toolPanel.add(selectionPanel);
	}
	private void addNewParameterButton(JPanel toolPanel){
		JButton addButton = new JButton("Add New Parameter");
		addButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				addNewParameterPerformed("");
			}
			
		});
		toolPanel.add(addButton,"al left");	

	}
	public boolean addNewParameterPerformed(String parameterName){
		String labelNames[] = { "<HTML>Parameter Name<FONT COLOR = Red>*</FONT></HTML>", "Parameter Code","Type","To_accession","Protocol", 
				"Format", "defaultValue","minValue","maxValue","categories","<HTML>isVisible<FONT COLOR = Red>*</FONT></HTML>",
				"<HTML>Unit Type<FONT COLOR = Red>*</FONT></HTML>" };
		String values[] = { parameterName, "", "", "", "" ,"", "", "", "", "" ,"",""};
		JLabel labels[] = new JLabel[labelNames.length];
		final JTextField textBoxes[] = new JTextField[labelNames.length-1];
		JComboBox units = getUniTypeInfo();
		
		boolean invalid = true;
		boolean notcancel = true;
		while(invalid && notcancel){
			JPanel addNewParemeterPanel = new JPanel(new MigLayout("insets 0, gapx 0"));
			for (int labelIndex = 0; labelIndex < labels.length; labelIndex++) {
				labels[labelIndex] = new JLabel(labelNames[labelIndex]);
				addNewParemeterPanel.add(labels[labelIndex],"gapleft 10, push");
				if(labelIndex!=labels.length-1)
				{
					textBoxes[labelIndex] = new JTextField();
					textBoxes[labelIndex].setPreferredSize(new Dimension(200, 0));
					textBoxes[labelIndex].setText(values[labelIndex]);
					addNewParemeterPanel.add(textBoxes[labelIndex],"growx, gapRight 10, wrap");
				
				}else{
					((JTextComponent) units.getEditor().getEditorComponent()).setText(values[labelIndex]);
					addNewParemeterPanel.add(units,"gapRight 10, wrap");									
				}
				
				
			}
			int newParameterOption = JOptionPane.showConfirmDialog(
					null, addNewParemeterPanel,
					"Enter New Parameter Information ",
					JOptionPane.OK_CANCEL_OPTION);
			if (newParameterOption == JOptionPane.CANCEL_OPTION) {
				notcancel = false;
			}
			if (newParameterOption == JOptionPane.OK_OPTION) {
				for (int labelIndex = 0; labelIndex < labels.length; labelIndex++) {
					if(labelIndex != labels.length-1)
					{
						values[labelIndex]= textBoxes[labelIndex].getText();
					}else{
						values[labelIndex] = (String) units.getEditor().getItem();
					}
				}
				
				if(!values[0].trim().equals("")&&!values[10].trim().equals("")&&!values[11].trim().equals(""))
				{
					
					List<String> newParaInfo = new ArrayList<String>();	
					for(String value : values){
						newParaInfo.add(value.trim().equals("") ? null : value.trim());
					}
					newParaInfo.add("0");//id					
					int newid = MeasurementParameterDAO.getInstance().insert(newParaInfo);
					newParaInfo.set(12, String.valueOf(newid));
					invalid = false;
					getParameterNameIDMap().put(values[0].trim(), newid);
				}else{
					JOptionPane.showConfirmDialog(
							 addNewParemeterPanel,
                            "<HTML><FONT COLOR = Red>*</FONT> marked fields are mandatory.</HTML>",
                            "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
				}						
			}
		}
		return notcancel;
	}
	public void createUnitTypeJCombobox(){
		List<MeasurementUnit> existUnitypes = MeasurementUnitDAO.getInstance().findAll();
		String unitsInfo[] = new String[existUnitypes.size()];
		int i = 0;
        for(MeasurementUnit uniType :existUnitypes ){   	
        	unitsInfo[i]=uniType.getUnitType();
        	i++;
        }       
        final JComboBox<String> unitssComboBox = new JComboBox<String>(unitsInfo);
        unitssComboBox.setEditable(true);    
        AutoCompleteDecorator.decorate(unitssComboBox);
        unitssComboBox.setPreferredSize(new Dimension(200, 0));
        setUniTypeInfo(unitssComboBox);
	}
	
	public void addImportFileButton(JPanel toolPanel){
		JButton importButton = new JButton("Import File");
		importButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				importButtonActionPerformed() ;
				
			}
			
		});
		toolPanel.add(importButton,"gapleft 5, wrap");
	}
	public void importButtonActionPerformed() {
		try {
			boolean validInput = true;
			final JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter("Comma separated files(.csv)", "csv"));
			int status = fc.showOpenDialog(null);
			if (fc.getSelectedFile() == null)
			{
				return;
			}
			String filename = fc.getSelectedFile().toString();
			if (status == JFileChooser.APPROVE_OPTION) {
				Utils.removeAllRowsFromTable((DefaultTableModel) getImportTable().getModel());
				if (!(filename.trim().endsWith(".csv"))) {
					JLabel errorFields = new JLabel("<HTML><FONT COLOR = Blue>"
							+ "File should have a .csv extension only"
							+ ".</FONT></HTML>");
					JOptionPane.showMessageDialog(null,errorFields);
				} else {
					numberofRows.setText("0");
					setValueChangedRows(new ArrayList<Integer>());
					BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
					String firstLine = br.readLine();
					String headerLine = br.readLine();
					if (headerLine != null){
						headerLine = headerLine.trim();
					}
					List<String> headers =  new ArrayList<String>(Arrays.asList(headerLine.split(",")));
					List<String> parametersAndTom =  new ArrayList<String>();
					for(String header : headers)
					{
						if(!Arrays.asList(descriptors).contains(header)){
					
							parametersAndTom.add(header);
						}
			    	}
					
					for(int index = 0; index < parametersAndTom.size();++index){

						if(index + 1 == parametersAndTom.size()){
							int insert = headers.indexOf(parametersAndTom.get(index));
							headers.add(insert+1,"tom_"+parametersAndTom.get(index));
						}
						else if(!parametersAndTom.get(index+1).equals("tom_"+parametersAndTom.get(index))){
							int insert = headers.indexOf(parametersAndTom.get(index));
							headers.add(insert+1,"tom_"+parametersAndTom.get(index));
						}
						else{
							index = index + 1;
						}
					}
					if(!headers.contains("measurement_value_id")){
						headers.add("measurement_value_id");
					}
					String[] colIndentifiers = new String[headers.size()];
					colIndentifiers = headers.toArray(colIndentifiers);
					((DefaultTableModel) getImportTable().getModel()).setColumnIdentifiers(colIndentifiers);
					getImportTable().setGridColor(Color.LIGHT_GRAY);
					String dataLine = br.readLine();
					
					while (dataLine != null) {
						dataLine = dataLine.trim();
						List<String> rowdata = Arrays.asList(dataLine.split(","));
						Object[] row = new Object[rowdata.size()];
						row = rowdata.toArray(row);
						try{
							Integer.parseInt(String.valueOf(row[0]));
						}
						catch(Exception e){
							getImportTable().setModel(new DefaultTableModel());
							JOptionPane.showMessageDialog(null, "Invalid input File", "Error", JOptionPane.ERROR_MESSAGE);
							validInput = false;
							break;
						}
						((DefaultTableModel) getImportTable().getModel()).addRow(row);
						dataLine = br.readLine();
					}
									
					br.close();
				}
			}
			if(validInput){
				createColumnsMenu();
				hasSynced = false;
				setParametersInfo();
				getImportTable().validate();
				getImportTable().repaint();
				numberofRows.setText(String.valueOf(getImportTable().getRowCount()));
				System.out.println(getValueChangedRows());
			}
		} catch (Exception E) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in import " + E);
			}
			
		}
		

	}
	
	private void createColumnsMenu(){		
		JPopupMenu columnsPopupMenu = new JPopupMenu();
		final JCheckBox[] columnNameCheckBox = new JCheckBox[getImportTable().getColumnCount()];
		JPanel columnsPopupPanel = new JPanel(new MigLayout("insets 0, gap 0")); 
	    JScrollPane scrollPane = new JScrollPane(columnsPopupPanel);	    
	    for(int columnCounter =0; columnCounter< columnNameCheckBox.length;++columnCounter){
	    	String columnName = getImportTable().getColumnName(columnCounter);
	    	columnNameCheckBox[columnCounter]= new JCheckBox(columnName);
	    	if(getImportTable().getColumnModel().getColumn(columnCounter).getMaxWidth() > 0) {
	    		columnNameCheckBox[columnCounter].setSelected(true);
	    	}
	    	final int index = columnCounter;
	    	columnNameCheckBox[columnCounter].addActionListener(new ActionListener() {				
				public void actionPerformed(ActionEvent e) {
        					if(!columnNameCheckBox[index].isSelected()) {
        						hideColumn(getImportTable().getColumnName(index));
        					} else {
        						showColumn(getImportTable().getColumnName(index));
        					}
				}
			});
	    	columnsPopupPanel.add(columnNameCheckBox[columnCounter],"wrap");
	    	
		}
	    //hideColumn("measurement_value_id");	    
		columnsPopupMenu.setPopupSize(95, 200);
	    columnsPopupMenu.add(scrollPane);
        columnsPopupMenu.setVisible(false);
	    for(ActionListener action : getHideOrShowCol().getActionListeners()){
	    	getHideOrShowCol().removeActionListener(action);
	    }
	    getHideOrShowCol().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				hideOrShowColumnPerformed();
			}
			
		});
	  
	    setColumnsPopupMenu(columnsPopupMenu);
	}
	
	private void hideOrShowColumnPerformed(){
		 if(!getImportTable().getColumnName(0).equals(""))
		 {
			repaint();
			final JPopupMenu columnsPopupMenu = getColumnsPopupMenu();			
	        if (!columnsPopupMenu.isVisible()) {
	        	Point p = getHideOrShowCol().getLocationOnScreen();
	            columnsPopupMenu.setInvoker(getHideOrShowCol());
	            columnsPopupMenu.setLocation((int) p.getX(),
	                    (int) p.getY() + getHideOrShowCol().getHeight());
	            columnsPopupMenu.setVisible(true);		     
	        }
	    }
			

	 }
	
	public void hideColumn(String columnName) {
		getImportTable().getColumn(columnName).setWidth(0);
		getImportTable().getColumn(columnName).setMinWidth(0);
		getImportTable().getColumn(columnName).setMaxWidth(0);
    }

    public void showColumn(String columnName) {
        getImportTable().getColumn(columnName).setMinWidth(15);
        getImportTable().getColumn(columnName).setMaxWidth(Integer.MAX_VALUE);
        int colNum = getImportTable().getColumnCount();
        Dimension tableSize = getImportTable().getSize();
        int avergeWidth = tableSize.width / colNum;
        for (int index = 0; index < colNum; ++index) {
        	 getImportTable().getColumn(getImportTable().getColumnName(index)).setPreferredWidth(avergeWidth);
        }
        repaint();
    }

	
	public void setParametersInfo(){
		LinkedHashMap<String, Integer> parameterInfomap = new LinkedHashMap<String, Integer>();
		LinkedHashMap<String, List<String>> parameterNameValuesMap = new LinkedHashMap<String, List<String>>();
		List<MeasurementParameter> measurementParameters = MeasurementParameterDAO.getInstance().findAll();
		for(MeasurementParameter dmp : measurementParameters){
			List<String> values = new ArrayList<String>();
			values.add(dmp.getMinValue());
			values.add(dmp.getMaxValue());
			parameterNameValuesMap.put(dmp.getParameterName(), values);
			parameterInfomap.put(dmp.getParameterName(), dmp.getMeasurementParameterId());
			if(dmp.getParameterCode()!=null)
			{
				parameterInfomap.put(dmp.getParameterCode(), dmp.getMeasurementParameterId());
				parameterNameValuesMap.put(dmp.getParameterCode(), values);
			}
		}
		setParameterNameValuesMap(parameterNameValuesMap);
		setParameterNameIDMap(parameterInfomap);
		
		setParamersImported(new ArrayList<String>());
		setTomColIndexes(new ArrayList<Integer>());
		if(!getImportTable().getColumnName(0).equals("")){
			for(int columnCounter =0; columnCounter< getImportTable().getColumnCount();++columnCounter){
		    	String columnName = getImportTable().getColumnName(columnCounter);	
		    	if(!Arrays.asList(descriptors).contains(columnName))
		    	{	if(!StringUtils.substring(columnName,0,3).equals("tom")){
			    		 getParamersImported().add(columnName);
			    	}else{
			    		int colIndex = getImportTable().getColumnModel().getColumnIndex(columnName);
			    		getTomColIndexes().add(colIndex);
			    	}
		    	}
			}
			
			setParamersColIndexes(new ArrayList<Integer>());
			for(String parameter :getParamersImported()){
				int colIndex = getImportTable().getColumnModel().getColumnIndex(parameter);
				getParamersColIndexes().add(colIndex);
			}
			
			
		}
		
		List<String> dbParameters = new ArrayList<String>();
		for(Entry<String, Integer> entry: getParameterNameIDMap().entrySet()) {
			dbParameters.add(entry.getKey());
	    }
		
		List<String> newParameters = new ArrayList<String>();
		 for(int columnCounter =0; columnCounter< getImportTable().getColumnCount();++columnCounter){
	    	String columnName = getImportTable().getColumnName(columnCounter);	
	    	if(!dbParameters.contains(columnName) && getParamersImported().contains(columnName)){				    		
	    		newParameters.add(columnName);
	    	}
		 }
		setNewParameters(newParameters);
	}
	public void createTablePanel(){
		JPanel importTablePanel = new JPanel(new MigLayout("insets 0, gap 5"));
		String[] colIndentifiers = {"","","","",""};
		final MyTableModel model = new MyTableModel(1, colIndentifiers.length){
			@Override
			public boolean isCellEditable(int row, int col) {
				 if(getParamersColIndexes().contains(col) || col == 0){
					 return true;
				 }

				return false;
			}
		} ;
		model.setColumnIdentifiers(colIndentifiers);
		
		hasSynced = false;
		final JTable table= new JTable(model);
		table.addFocusListener(new FocusAdapter() {
		    @Override
		    public void focusLost(FocusEvent e) {
		        TableCellEditor tce = table.getCellEditor();
		        if(tce != null)
		        {
		        	if(hasSynced)
		        	{
		        		getValueChangedRows().add(table.getSelectedRow());
		        	}
		        	hasSynced = false;
		        	
		        }
		    }
		});
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);		
		table.setDefaultRenderer(Object.class, new MyRenderer());
		setImportTable(table);
		table.setShowGrid(true);
		table.setRowSelectionAllowed(false);
		table.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				validateSelection(e);
			}

			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				validateSelection(e);
			}

			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				validateSelection(e);
			}

			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
	        public void validateSelection(MouseEvent e) {
	        	  int col = table.columnAtPoint(e.getPoint());  
	        	  if(getTomColIndexes().contains(col))
	        	  {
		              table.setRowSelectionAllowed(true);
		              table.setColumnSelectionAllowed(true);	
		            
	        	  }else{
		              table.setRowSelectionAllowed(false);
		              table.setColumnSelectionAllowed(false);
	        	  }
	        }  
	    });
		
		final JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);  
        header.addMouseListener(new MouseAdapter() {  
            public void mouseClicked(MouseEvent e) {  
                int col = header.columnAtPoint(e.getPoint());   
                if(header.getCursor().getType() == Cursor.E_RESIZE_CURSOR)  
                    e.consume();  
                else if(getTomColIndexes().contains(col)){  
                    table.setColumnSelectionAllowed(true);
                    table.setRowSelectionAllowed(false);
                    table.clearSelection();
                    table.setColumnSelectionInterval(col,col);
               }  
          }  
        });  
       
		JScrollPane tableScrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		importTablePanel.add(tableScrollPane,"span, w 100%, h 100%, wrap");
		setImportTablePanel(importTablePanel);
		setCurrentTable(table);
		importTablePanel.add(new JLabel("The current number of rows in this table:"));
		numberofRows = new JLabel("0");
		importTablePanel.add(numberofRows);
		
	}
	
	public class MyRenderer extends DefaultTableCellRenderer  
	{ 
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
	   { 
	    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); 
	    c.setForeground(Color.BLACK);
		c.setBackground(Color.WHITE);
		
		if (table.getSelectedRows().length > 0) {
			if (isSelected) {
				c.setForeground(Color.BLACK);
				c.setBackground(new Color(184, 207, 229));
			}
		} else {
			c.setForeground(Color.BLACK);
			c.setBackground(Color.WHITE);
		}
	    if(getParamersColIndexes().contains(column))
		{	
	    	
			String parameter = (String) getImportTable().getColumnModel().getColumn(column).getHeaderValue();
		
			if(!getNewParameters().contains(parameter))
			{
				
				Object min = getParameterNameValuesMap().get(parameter).get(0);
				Object max = getParameterNameValuesMap().get(parameter).get(1);	
				if(!String.valueOf(value).trim().equals("") && StringUtils.isNumeric(String.valueOf(value)))
				{	
					if(!String.valueOf(min).equals("null") && !String.valueOf(max).equals("null")
							&&!String.valueOf(min).trim().equals("") && !String.valueOf(max).trim().equals(""))
					{
						if(StringUtils.isNumeric((String) min) && StringUtils.isNumeric((String) max))								
						{	
							if(Integer.parseInt((String)value) < Integer.parseInt((String)min) 
									||Integer.parseInt((String)value) > Integer.parseInt((String)max)) 
							{
							    c.setBackground(new Color(252, 218, 218));										
							}
						}
					}
				}
		   }
		}
	    if(hasSynced){
	    	c.setBackground(new Color(255, 255, 224));
	    	if (isSelected) {
				c.setForeground(Color.BLACK);
				c.setBackground(new Color(184, 207, 229));
			} else {
				c.setForeground(Color.LIGHT_GRAY);
			}
	    }
	    //select multiple cells in one column
	    if(table.getRowSelectionAllowed() && table.getSelectedRow() == row && table.getSelectedColumn() == column){
	    	c.setForeground(Color.BLACK);
			c.setBackground(new Color(184, 207, 229));
	    }
	    //select whole column
	    if(!table.getRowSelectionAllowed() && table.getColumnSelectionAllowed() && table.getSelectedColumn() == column){
	    	c.setForeground(Color.BLACK);
			c.setBackground(new Color(184, 207, 229));
	    }
	    if(column == 0 && !String.valueOf(value).equals("null")&&!ObservationUnitDAO.getInstance().isObservationUnitIdExist(Integer.parseInt((String)value))){
	    	 c.setBackground(new Color(252, 218, 218));	
	    }
        
	    return c; 
	   } 

	}
	
	class MyTableModel extends DefaultTableModel {
		
		MyTableModel(int numRows, int length){
			super();
		};
	    public void removeColumn(int column) {
	        columnIdentifiers.remove(column);
	        for (Object row: dataVector) {
	            ((Vector) row).remove(column);
	        }
	        fireTableStructureChanged();
	    }
	}
	public void deleteColumn(int col){
	 	
		((MyTableModel)getImportTable().getModel()).removeColumn(col);
		setParametersInfo();		
		createColumnsMenu();
		getImportTable().validate();
		getImportTable().repaint();
	}
	public void addSyncButton(JPanel toolPanel){
		ClassLoader loader = PhenotypeExportPanel.class.getClassLoader();
		ImageIcon syncIcon = new ImageIcon(loader.getResource("images/reload.png"));		
		ImageIcon syncColorIcon = new ImageIcon(loader.getResource("images/reloadColor.png"));
		ImageButton syncButton = new ImageButton(syncIcon,syncColorIcon);
		syncButton.setToolTipText("sync with database");
		
		syncButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				getImportTable().clearSelection();
				boolean valid = true;
				for(int row = 0; row < getImportTable().getRowCount();++row){		
					if(!ObservationUnitDAO.getInstance().isObservationUnitIdExist(Integer.parseInt((String) getImportTable().getValueAt(row, 0)))){
						valid=false;
						break;
					}
				}
				if(valid){
					if(!getImportTable().getColumnName(0).equals(""))
					{
						 setParametersInfo();					
						 boolean isExist = true;
						 if(!getNewParameters().isEmpty()){
							 isExist = false;
						 }
						// progress = new JProgressBar(0, 100);
			             progress.setStringPainted(true);
				         progress.setVisible(false);
						 if(isExist == false){
							 for(String newParameter: getNewParameters())
							 {
								
								 int option= JOptionPane.showOptionDialog(null, 
									        "Compelete information for "+newParameter+" ?", 
									        "", 
									        JOptionPane.OK_CANCEL_OPTION, 
									        JOptionPane.INFORMATION_MESSAGE, 
									        null, 
									        new String[]{"OK", "Ignore"}, // this is the array
									        "default");
								 if(option == JOptionPane.OK_OPTION)
								 {
									 addNewParameterPerformed(newParameter);
								 }else{
									 int parameterCol = getImportTable().getColumnModel().getColumnIndex(newParameter);									
									 deleteColumn(parameterCol);
									 //tom
									 deleteColumn(parameterCol);
								 }
							 }
						 }
					    new SyncPhenotypeData(getImportTable(),  progress).execute();
					
					}
				
			   }else{
				   JOptionPane.showMessageDialog(null,"Red colored observation_unit_id does not exist in database, please fix it. ");
			   }
			}
			
		});
		toolPanel.add(syncButton,"h 24:24:24, w 24:24:24, wrap");
		
	}
	private class SyncPhenotypeData extends SwingWorker<Void, Void> {

	    private JTable table;
	    private JProgressBar progress;
	    private long initialTime;

	    public SyncPhenotypeData(JTable jTable, JProgressBar progress) {
	        this.table = jTable;
	        this.progress = progress;
	    }

	    @Override
	    protected Void doInBackground() throws Exception {
	        
	        long duration = System.currentTimeMillis() - initialTime;
	        System.out.println("Opening connection: " + duration / 1000);
	        java.util.Date date = new Date();
	        int option = JOptionPane.showOptionDialog(null, 
			        "All unfilled tom will be filled with current system time", 
			        "", 
			        JOptionPane.OK_CANCEL_OPTION, 
			        JOptionPane.INFORMATION_MESSAGE, 
			        null, 
			        new String[]{"Yes, fill", "No, don't fill"}, // this is the array
			        "default");
	        if(option ==JOptionPane.OK_OPTION )
	        {	
	        	
	        	for(int tomColumn : getTomColIndexes()){
					for(int row = 0; row < table.getRowCount();++row){		
						String value = String.valueOf(table.getValueAt(row, tomColumn-1)).trim();
						if (value.equalsIgnoreCase("null") || value.equalsIgnoreCase("")){
							table.setValueAt("", row, tomColumn-1);
						}
						//System.out.println(String.valueOf(table.getValueAt(row, 2)) + " - " + table.getColumnName(tomColumn) + " - " + table.getValueAt( row, tomColumn-1));
						Object tom = String.valueOf(table.getValueAt(row, tomColumn)).trim().equals("") ?
								null :  table.getValueAt(row, tomColumn);
						if(tom == null){
							 table.setValueAt(date, row, tomColumn);
						}else{
							if(tom instanceof Date){
								break;
							}else{
								DateFormat formatter = new SimpleDateFormat("MM/dd/yy");
								try {
									Date convertedDate = (Date)formatter.parse(String.valueOf(tom));
									table.setValueAt(convertedDate, row, tomColumn);
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									
									JOptionPane.showMessageDialog(null, "Please check the row with observation_unit_id "+table.getValueAt(row, 0)+". Tom value: "+String.valueOf(tom)+".\nTom columns format has to be MM/dd/yy.");
									return null;
								}
							}
							
						}
				  }
				}
	        	SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
				Session session = sessionFactory.openSession();
				Transaction transaction = session.beginTransaction();
				initialTime = System.currentTimeMillis();
		        progress.setVisible(true);
		        progress.setValue(0);
				if(getValueChangedRows().size() == 0){
					for(int row = 0; row < table.getRowCount();++row){
						int result = syncWithDatabase(row,session);
						if (result == -1) {
							return null;
						}
						if ( row % 1000 == 0 ) { 
							session.flush();
							session.clear();
						}
					}
				}else{
					for(int row : getValueChangedRows()){
						int result = syncWithDatabase(row,session);
						if (result == -1) {
							return null;
						}
						if ( row % 1000 == 0 ) { 
							session.flush();
							session.clear();
						}
					}
				}
				hasSynced = true;
				table.repaint();
				transaction.commit();
				session.close();
				setValueChangedRows(new ArrayList<Integer>());
	        }
	        return null;
	    }
	    
	    public int syncWithDatabase(int row,Session session){
	    	int measurementCol = table.getColumnModel().getColumnIndex("measurement_value_id");
	    	int obsUnitId = Integer.parseInt((String) table.getValueAt(row, 0));	
			List<Integer> measurementIds = new ArrayList<Integer>();
			int index = -1;
			for(String parameter : getParamersImported())
			{
				index++;
				int parameterId = getParameterNameIDMap().get(parameter);
				int parameterCol = table.getColumnModel().getColumnIndex(parameter);
				int tomCol = parameterCol+1;
				String value = String.valueOf(table.getValueAt(row, parameterCol));
				Object  measurementIdList = table.getValueAt(row, measurementCol);	
				Date tom = null;
				if (table.getValueAt(row, tomCol) != null){
					tom = (Date)table.getValueAt(row, tomCol);
				}

				if(measurementIdList == null){			
					int measurementId = MeasurementValueDAO.getInstance().insert(obsUnitId, parameterId, value, tom, session);
					if (measurementId == 0) {
						JOptionPane.showConfirmDialog(
								 null,
	                            "<HTML><FONT COLOR = Red>*</FONT> Can not sync duplicate records.</HTML>",
	                            "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
						return -1;
						
					}else {
						measurementIds.add( measurementId);
					}
					
					
				}else{
					measurementIds = (List<Integer>) measurementIdList;
					MeasurementValueDAO.getInstance().update(obsUnitId, parameterId, value, tom, measurementIds.get(index),session);
				}
				progress.setValue((int) ((row * 1.0 / table.getRowCount()) * 100));	
			}
		
			table.setValueAt(measurementIds, row, measurementCol);
			return 0;
			
	    }
	    @Override
	    public void done() {
	        long duration = System.currentTimeMillis() - initialTime;        
  	        System.out.println("Time: " + duration / 1000);						
	        progress.setVisible(false);
	    }
	}
	
	public void populateTableFromObjects(List<Object[]> exportTableRows) {
	    DefaultTableModel tableModel = ((DefaultTableModel)getCurrentTable().getModel());
	    removeAllRowsFromTable(tableModel);	
		for(Object[] row : exportTableRows){
			tableModel.addRow(row);               
		}		
	}
	public JComboBox<String> getUniTypeInfo() {
		return uniTypeInfo;
	}

	public void setUniTypeInfo(JComboBox<String> uniTypeInfo) {
		this.uniTypeInfo = uniTypeInfo;
	}

	public JPanel getImportTablePanel() {
		return importTablePanel;
	}

	public void setImportTablePanel(JPanel importTablePanel) {
		this.importTablePanel = importTablePanel;
	}
	public JTable getImportTable() {
		return importTable;
	}

	public void setImportTable(JTable importTable) {
		this.importTable = importTable;
	}
	public JButton getHideOrShowCol() {
		return hideOrShowCol;
	}

	public void setHideOrShowCol(JButton hideOrShowCol) {
		this.hideOrShowCol = hideOrShowCol;
	}
	public JPopupMenu getColumnsPopupMenu() {
		return columnsPopupMenu;
	}

	public void setColumnsPopupMenu(JPopupMenu columnsPopupMenu) {
		this.columnsPopupMenu = columnsPopupMenu;
	}
	public LinkedHashMap<String, Integer> getParameterNameIDMap() {
		return parameterNameIDMap;
	}

	public void setParameterNameIDMap(
			LinkedHashMap<String, Integer> parameterInfoMap) {
		this.parameterNameIDMap = parameterInfoMap;
	}
	public List<String> getParamersImported() {
		return paramersImported;
	}

	public void setParamersImported(List<String> paramersImported) {
		this.paramersImported = paramersImported;
	}
	public LinkedHashMap<String, List<String>> getParameterNameValuesMap() {
		return parameterNameValuesMap;
	}

	public void setParameterNameValuesMap(
			LinkedHashMap<String, List<String>> parameterNameValuesMap) {
		this.parameterNameValuesMap = parameterNameValuesMap;
	}
	public List<Integer> getParamersColIndexes() {
		return paramersColIndexes;
	}

	public void setParamersColIndexes(List<Integer> paramersColIndexes) {
		this.paramersColIndexes = paramersColIndexes;
	}
	public List<String> getNewParameters() {
		return newParameters;
	}

	public void setNewParameters(List<String> newParameters) {
		this.newParameters = newParameters;
	}

	public List<Integer> getTomColIndexes() {
		return tomColIndexes;
	}
	public void setTomColIndexes(List<Integer> tomColIndexes) {
		this.tomColIndexes = tomColIndexes;
	}

	public List<Integer> getValueChangedRows() {
		return valueChangedRows;
	}
	public void setValueChangedRows(List<Integer> valueChangedRows) {
		this.valueChangedRows = valueChangedRows;
	}
	

	public JTable getCurrentTable() {
		return currentTable;
	}
	public void setCurrentTable(JTable currentTable) {
		this.currentTable = currentTable;
	}
	

}
