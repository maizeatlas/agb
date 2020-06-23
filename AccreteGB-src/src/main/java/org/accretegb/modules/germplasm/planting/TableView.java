package org.accretegb.modules.germplasm.planting;
import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.config.AccreteGBContext;
import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.*;
import org.accretegb.modules.customswingcomponent.TextField;
import org.accretegb.modules.germplasm.experimentaldesign.ExperimentSelectionPanel;
import org.accretegb.modules.germplasm.stocksinfo.CreateStocksInfoPanel;
import org.accretegb.modules.germplasm.stocksinfo.StocksInfoPanel;
import org.accretegb.modules.hibernate.MateMethodConnect;
import org.accretegb.modules.hibernate.dao.ObservationUnitDAO;
import org.accretegb.modules.hibernate.dao.StockDAO;
import org.accretegb.modules.util.ChangeMonitor;
import org.accretegb.modules.util.GlobalProjectInfo;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;

public class TableView extends JPanel {

	private static final long serialVersionUID = 1L;
	private PlotIndexToolBoxPanel stocksOrderPanel;
	private List<PlantingRow> stockList;
	private boolean autoDelete;
	private String zipcode = null;
	private MatingPlanSelector matingPlanSelector;
	private JButton matingPlanButton;
	private TagGenerator tagGenerator;
	private Object[][] validTableRows;
	private HashMap<Integer,ExperimentSelectionPanel> expid_expPanel;
	private boolean tableChanged = false;
	private Map<String, List<PlantingRow>> tag_plants = new HashMap<String, List<PlantingRow>>();  
	private Map<String, Color> tag_Color = new HashMap<String, Color>();  
	private Date plantingDate = null;   
	private int currentStartRow = 0;
	private int projectID = -1;
	public String prefix = null;
	public boolean prefixIsFixed = false;
	public String plantingIndex = null;
	public boolean synced = false;
	final CustomCalendar costomizedCalendar = new CustomCalendar();
	public void initialize() {
		setLayout(new MigLayout("insets 10, gap 10"));
		addStockTableListeners();		
		JPanel bottomPanel = new JPanel(new MigLayout("insets 0, gap 0"));
		bottomPanel.add(getParametersPanel(), "w 100%, wrap");
		bottomPanel.add(getPlantsSetterPanel(), "w 100%, wrap");
		bottomPanel.add(stocksOrderPanel, "w 100%, h 100%, wrap");
		setExportButtonPanel();
		add(bottomPanel, "w 100%, gaptop 30, growy, spany, pushy, wrap");
		addStockTableToolListeners();
		getStocksOrderPanel().getTable().setTransferHandler(new TableTransferHandler(this));
		getStocksOrderPanel().getTable().setDropMode(DropMode.INSERT_ROWS);
		getStocksOrderPanel().getTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}


	private void addStockTableToolListeners(){
		getStocksOrderPanel().getTable().setFlagColunName(ColumnConstants.REP);
		//needSync = new JLabel("<HTML><FONT color = #B80000>Changes not synced with database</FONT></HTML>");
		//((JPanel)getStocksOrderPanel().getComponent(1)).add(needSync, 2);
		setMouseListenerMoveDownButton();
		setMouseListenerMoveUpButton();
	}

	private JPanel getPlantsSetterPanel() {
		JPanel plantsSetterPanel = new JPanel(new MigLayout("insets 0, gapx 0"));
		addStartRow(plantsSetterPanel);
		addCountSetter(plantsSetterPanel);
		addMatingSetterPanel(plantsSetterPanel);		
		return plantsSetterPanel;
	}	

	private void addMatingSetterPanel(JPanel plantsSetterPanel){		 
		JPanel subpanel = new JPanel(new MigLayout("insets 0, gapx 0"));
		configureMatingPlanSetter();
		subpanel.add(matingPlanSelector);
		subpanel.add(matingPlanButton);
		plantsSetterPanel.add(subpanel, "push, al right");		
	}

	private void addStartRow(JPanel plantsSetterPanel){
		JPanel subpanel = new JPanel(new MigLayout("insets 0, gapx 0"));
		JLabel startRowLabel = new JLabel("Plot number starts from: ");
		final JTextField startRow = new JTextField(5);
		JButton setRow = new JButton("set");
		JButton reset = new JButton("Auto Set");
		reset.setToolTipText("If current tags already exist in database, use this button to auto reset start row. ");
		setRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Utils.isInteger(startRow.getText()))
				{
					changeTagsByStartRow(Integer.parseInt(startRow.getText()));					
					updateWhenRowDataChanged();
					// System.out.println(stockList.get(0).getTag());
					setSyncedFalse();
				}
			}				
		});

		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				populateTableBasedonDatabase(stockList);	
				setSyncedFalse();
			}

		});

		subpanel.add(startRowLabel,"gapleft 10");
		subpanel.add(startRow);
		subpanel.add(setRow);
		subpanel.add(reset);
		plantsSetterPanel.add(subpanel, "w 35%");

	}

	private void addPlantingIndex(JPanel plantsSetterPanel){
		JPanel subpanel = new JPanel(new MigLayout("insets 0, gapx 0"));
		JLabel plantingIndexLabel = new JLabel("Planting group index: ");
		final JTextField index = new JTextField(5);
		JButton setRow = new JButton("set");
		setRow.setToolTipText("Set the planting group index.");
		setRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Utils.isInteger(index.getText()))
				{
					plantingIndex = index.getText();			
					updateWhenRowCountChanged();
					setSyncedFalse();
					}
				}
						
		});

		subpanel.add(plantingIndexLabel,"gapleft 10");
		subpanel.add(index);
		subpanel.add(setRow);
		plantsSetterPanel.add(subpanel, "gapleft 10, w 35%");	
	}


	private void updatePlotNumbers(){
		//System.out.println("updatePlotNumbers start");
		int plotIndex = currentStartRow-1;
		PlotIndexColumnTable table = getStocksOrderPanel().getTable();
		Date date = null;
		if(plantingDate == null){
			DateFormat dateFormatRead = new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy",Locale.getDefault());		
			try {
				if(table.getValueAt(0, table.getIndexOf(ColumnConstants.PLANTING_DATE))!=null){
					date = dateFormatRead.parse(String.valueOf(table.getValueAt(0, table.getIndexOf(ColumnConstants.PLANTING_DATE))));
				}					
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(date == null)
			{
				Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
				date = calendar.getTime();
			}

		}else{
			date = plantingDate;
		}	

		// IN ORDER TO SPEED UP, ASSUME A USER ONLY WORK ON THE PLANTINGS ON ONE YEAR.
		// ORTHERWISE, EVERY ROW NEEDS TO QURY DATABASE TO SET THE TAG
		prefix = getTagPrefix(date);
		StringBuffer tag = new StringBuffer();
		DecimalFormat plotFormat = new DecimalFormat("00000");
		DecimalFormat tagFormat = new DecimalFormat("0000000");
		for(int row = 0; row < table.getRowCount(); ++row){
			table.setValueAt(row+1, row, table.getIndexOf(ColumnConstants.ROW_NUM));
			if(((String)table.getValueAt(row, ColumnConstants.TYPES)).equals(ColumnConstants.ROW)) {		
				tag = new StringBuffer();
				plotIndex++;
				table.setValueAt(plotFormat.format(plotIndex),row,table.getIndexOf(ColumnConstants.ROW)) ;
				tag.append(prefix).append(plotFormat.format(plotIndex)).append('.').append(tagFormat.format(0));
				table.setValueAt(tag.toString(), row, table.getIndexOf(ColumnConstants.TAG));
				updatePlantTagsWhenRowDataChange(row,table.getIndexOf(ColumnConstants.ROW));
				int count = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.COUNT)));
				for(int i = 1; i <= count; ++i){
					String plantTag = tag.toString().substring(0, tag.length()-7)+tagFormat.format(i);
					table.setValueAt(plantTag,row+i,table.getIndexOf(ColumnConstants.TAG)) ;
					table.setValueAt(row+i+1, row+i, table.getIndexOf(ColumnConstants.ROW_NUM));
				}
			}				
		}
		//System.out.println("updatePlotNumbers end");
		updateWhenRowDataChanged();
	}

	private void changeTagsByStartRow(int startRow){
		autoDelete = true;
		PlotIndexColumnTable table = getStocksOrderPanel().getTable();
		boolean valid = false;
		Date date = null;
		if(table.getValueAt(0, table.getIndexOf(ColumnConstants.PLANTING_DATE)) instanceof Date){
			date = (Date) table.getValueAt(0, table.getIndexOf(ColumnConstants.PLANTING_DATE));
		}else{
			SimpleDateFormat strToDate = new SimpleDateFormat ("E MMM dd HH:mm:ss Z yyyy",Locale.getDefault());
			// parse format String to date
			try {
				date = (Date)strToDate.parse((String) table.getValueAt(0, table.getIndexOf(ColumnConstants.PLANTING_DATE)));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// System.out.println(" >> 2");
		String prefix = getTagPrefix(date);	
		StringBuffer tag = new StringBuffer();
		DecimalFormat plotFormat = new DecimalFormat("00000");
		DecimalFormat tagFormat = new DecimalFormat("0000000");
		if(table.getRowCount()>0)
		{
			//only check the first reset tag		
			if(startRow >= 0)
			{
				tag.append(prefix).append(plotFormat.format(startRow)).append('.').append(tagFormat.format(0));			
				if(ObservationUnitDAO.getInstance().isExist(tag.toString())){
					JOptionPane.showMessageDialog(null, "Tagname :" + tag.toString() + " was taken. Reset starting plot number");
				}else{
					valid = true;
				}			   
			}
		}
		if(valid){
			int plotIndex = startRow-1;
			for(int row = 0; row < table.getRowCount(); ++row){
				if(((String)table.getValueAt(row, ColumnConstants.TYPES)).equals(ColumnConstants.ROW)) {		
					tag = new StringBuffer();
					plotIndex++;
					table.setValueAt(plotFormat.format(plotIndex),row,table.getIndexOf(ColumnConstants.ROW)) ;
					tag.append(prefix).append(plotFormat.format(plotIndex)).append('.').append(tagFormat.format(0));
					table.setValueAt(tag.toString(), row, table.getIndexOf(ColumnConstants.TAG));
					updatePlantTagsWhenRowDataChange(row,table.getIndexOf(ColumnConstants.ROW));
					int count = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.COUNT)));
					for(int i = 1; i <= count; ++i){
						String plantTag = tag.toString().substring(0, tag.length()-7) + tagFormat.format(i);
						table.setValueAt(plantTag,row+i,table.getIndexOf(ColumnConstants.TAG)) ;
					}
				}

			}
			currentStartRow = startRow;
			updateWhenRowDataChanged();
		}

		autoDelete = false;
	}

	private void addCountSetter(JPanel plantsSetterPanel) {
		JPanel subpanel = new JPanel(new MigLayout("insets 0, gapx 0"));
		final PlotIndexColumnTable table = getStocksOrderPanel().getTable();
		JLabel countLabel = new JLabel("Plants for selected plot: ");
		final JTextField plantCount = new JTextField(5);
		plantCount.setText("0");
		JButton setCount = new ImageButton("checkmarkColor.png");
		setCount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {				
				int count = Integer.parseInt(plantCount.getText().trim());
				if(count > 100) {
					Object options[] = {"Stop", "Proceed"};
					int option = JOptionPane.showOptionDialog(TableView.this, "<HTML>Count is larger than 100.<br>This will make your application slow.</HTML>", "Warning!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
					if(option == JOptionPane.OK_OPTION) {
						return;
					}
				}

				TreeMap<Integer,Integer> originalRows_plantCount =new TreeMap<Integer,Integer>();
				for(int row : table.getSelectedRows()) {
					if(((String)table.getValueAt(row, ColumnConstants.TYPES)).equals(ColumnConstants.ROW) 
							&&!table.getValueAt(row, table.getIndexOf(ColumnConstants.STOCK_NAME)).equals("Filler"))
					{
						originalRows_plantCount.put(row, Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.COUNT))));
					}
				}
				int flag = 0 ;
				int rowNumIncreased = 0;
				//// System.out.println(originalRows_plantCount);
				for(int row : originalRows_plantCount.keySet()) {
					//// System.out.println("original = "+ row);
					if ( flag == 0 ){
						table.setValueAt(count, row, table.getIndexOf(ColumnConstants.COUNT));	
						flag = 1;
						//// System.out.println("now1  = "+ row + " rowNumIncreased = " + rowNumIncreased);
					}else{
						rowNumIncreased = rowNumIncreased + count - originalRows_plantCount.get(row);
						table.setValueAt(count, (row + rowNumIncreased), table.getIndexOf(ColumnConstants.COUNT));
						//// System.out.println("now2  = "+ (row + rowNumIncreased));
					}


				}	
				updateWhenRowDataChanged();
				setSyncedFalse();
			}


		});
		subpanel.add(countLabel);
		subpanel.add(plantCount);
		subpanel.add(setCount);
		plantsSetterPanel.add(subpanel, "w 25%");

	}


	private void updateEndFromStart(int row) {
		final PlotIndexColumnTable table = getStocksOrderPanel().getTable();
		int count = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.COUNT)));
		//// System.out.println("row-" + row + " count-"+count);
		int startTag = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.START_TAG)));
		int endTag = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.END_TAG)));
		if(count < 0) {
			count = endTag-startTag+1;
			table.setValueAt(count, row, table.getIndexOf(ColumnConstants.COUNT));
		}
		if(count > 100) {
			Object options[] = {"Stop", "Proceed"};
			int option = JOptionPane.showOptionDialog(TableView.this, "<HTML>Count is larger than 100.<br>This will make your application slow.</HTML>", "Warning!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			if(option == JOptionPane.OK_OPTION) {
				count = endTag-startTag+1;
				table.setValueAt(count, row, table.getIndexOf(ColumnConstants.COUNT));
			}
		}
		//// System.out.println("set " + (startTag+count-1) + " to row num " + row );
		table.setValueAt(startTag+count-1, row, table.getIndexOf(ColumnConstants.END_TAG));
	}

	private Object[] copyRow(int row){
		final PlotIndexColumnTable table = getStocksOrderPanel().getTable();
		Object[] rowdata = new Object[table.getColumnCount()];
		for(int col = 0; col < table.getColumnCount(); ++col){
			rowdata[col] = table.getValueAt(row, col);
		}
		return rowdata;
	}


	private void updatePlantTagsWhenRowDataChange(int row, int updatedColumnIndex){
		final PlotIndexColumnTable table = getStocksOrderPanel().getTable();
		if(((String)table.getValueAt(row, ColumnConstants.TYPES)).equals(ColumnConstants.ROW)) {
			int count = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.COUNT)));
			if(count > 0){
				for(int i = 1; i <= count; ++i){
					table.setValueAt(table.getValueAt(row, updatedColumnIndex), row+i, updatedColumnIndex);
					table.setValueAt(true,row+i, table.getIndexOf(ColumnConstants.MODIFIED));
				}
			}					
		}
		setTableChanged(true);
		table.setValueAt(true,row, table.getIndexOf(ColumnConstants.MODIFIED));
	}
	private void updatePlantTagsWhenRowCountChange(int row) {

		final PlotIndexColumnTable table = getStocksOrderPanel().getTable();
		if(((String)table.getValueAt(row, ColumnConstants.TYPES)).equals(ColumnConstants.ROW)) {
			int startTag = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.START_TAG)));
			int endTag = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.END_TAG)));
			int count = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.COUNT)));
			String tag = (String) table.getValueAt(row, ColumnConstants.TAG);
			DecimalFormat tagFormat = new DecimalFormat("0000000");
			List<PlantingRow> plantTagsList;
			if(tag_plants.containsKey(tag)){
				plantTagsList = tag_plants.get(tag);	
			}else{
				plantTagsList = new ArrayList<PlantingRow>();
				tag_plants.put(tag, plantTagsList);
			}
			for(PlantingRow plant : plantTagsList){
				plant.setStartTag(startTag);
				plant.setEndTag(endTag);
				plant.setCount(count);
			}

			int exsitingPlantSize = plantTagsList.size();
			if(count > exsitingPlantSize){
				for(int i = 1; i <= plantTagsList.size(); ++i){
					table.setValueAt(startTag, row+i, table.getIndexOf(ColumnConstants.START_TAG));
					table.setValueAt(endTag, row+i, table.getIndexOf(ColumnConstants.END_TAG));
					table.setValueAt(count, row+i, table.getIndexOf(ColumnConstants.COUNT));
				}
				int plantIndex = plantTagsList.size();
				int insertRowIndex = row+plantTagsList.size()+1;
				while(count > plantTagsList.size()){
					plantIndex++;
					Object[] rowData = copyRow(row);
					rowData[table.getIndexOf(ColumnConstants.TYPES)] = "   Plant";
					rowData[table.getIndexOf(ColumnConstants.PLANTING_DATE)] = table.getValueAt(row, ColumnConstants.PLANTING_DATE);
					rowData[table.getIndexOf(ColumnConstants.TAG_ID)] = null;
					String plantTag = tag.toString().substring(0, tag.length()-7)+tagFormat.format(plantIndex);
					rowData[table.getIndexOf(ColumnConstants.TAG)] = plantTag;
					rowData[table.getIndexOf(ColumnConstants.MODIFIED)] = true;
					rowData[table.getIndexOf(ColumnConstants.PLANT)] = tagFormat.format(plantIndex);
					rowData[table.getIndexOf(ColumnConstants.EXP_FACTOR_VALUE_IDS)] = null;
					plantTagsList.add(new PlantingRow(rowData, table));
					((DefaultTableModel)table.getModel()).insertRow(insertRowIndex++, rowData);
				}
			}else if(count < exsitingPlantSize){
				int deleteRowIndex = row + plantTagsList.size();
				while(count < plantTagsList.size()){					
					((DefaultTableModel)table.getModel()).removeRow(deleteRowIndex);
					deleteRowIndex--;
					plantTagsList.remove(plantTagsList.size()-1);
				}
			}

		}

		table.updateRowNums();
		table.clearSelection();
		table.repaint();
	}

	private int getMaxRowNum(){
		final PlotIndexColumnTable table = getStocksOrderPanel().getTable();
		int maxRow = -1;
		for(int row = 0; row < table.getRowCount(); ++row){
			if(((String)table.getValueAt(row, ColumnConstants.TYPES)).equals(ColumnConstants.ROW)) {
				int rownum = Integer.parseInt(((String)table.getValueAt(row, ColumnConstants.ROW)));
				if(maxRow < rownum){
					maxRow = rownum;
				}
			}	
		}
		return maxRow;
	}	
	private void configureMatingPlanSetter() {
		final PlotIndexColumnTable table = getStocksOrderPanel().getTable();
		matingPlanSelector = new MatingPlanSelector(this);
		matingPlanButton = new JButton("Add Plan");
		matingPlanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String plan = (String) matingPlanSelector.getSelectedItem();
				MateMethodConnect mateConnect = matingPlanSelector.getSelectedMateConnect();
				int selectedRows[] = table.getSelectedRows();
				if(mateConnect != null){
					for(int row:selectedRows) {
						table.setValueAt(plan, row, table.getIndexOf(ColumnConstants.MATING_PLAN));
						table.setValueAt(mateConnect.getMateMethodConnectId(), row, table.getIndexOf(ColumnConstants.MATING_PLANT_ID));
						updatePlantTagsWhenRowDataChange(row,table.getIndexOf(ColumnConstants.MATING_PLAN));
					}
					updateWhenRowDataChanged();
					setSyncedFalse();
				}	
			}			
		});
	}



	private JPanel getParametersPanel() {
		JPanel parametersPanel = new JPanel(new MigLayout("insets 0, gapx 5"));
		addImport(parametersPanel);
		addDateSection(parametersPanel);
		addKernersSection(parametersPanel);
		addDelaySection(parametersPanel);
		addPurposeSection(parametersPanel);
		addCommentsSection(parametersPanel);
		return parametersPanel;
	}

	private void addImport(JPanel parametersPanel){
		JPanel subpanel = new JPanel(new MigLayout("insets 0, gapx 0"));
		JButton importbutton = new JButton("Import");
		subpanel.add(importbutton);
		importbutton.setToolTipText("Import the data exported from this panel");
		importbutton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				importButtonActionPerformed();
				setSyncedFalse();		

			}

		});
		parametersPanel.add(subpanel, "gapleft 10");
	}

	private void importButtonActionPerformed(){
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
					DefaultTableModel model = (DefaultTableModel) getStocksOrderPanel().getTable().getModel(); 
					Utils.removeAllRowsFromTable(model);
					BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
					// skip first line
					String first = br.readLine();
					String line = br.readLine().trim();
					int stockIdIndex = getStocksOrderPanel().getTable().getIndexOf(ColumnConstants.STOCK_ID);
					int stockNameIndex = getStocksOrderPanel().getTable().getIndexOf(ColumnConstants.STOCK_NAME);
					HashMap<String, Integer> stockNames = new HashMap<String, Integer>();
					while (line != null) {
						line = line.trim();
						String[] row = line.split(",");
						Object[] newRow = new Object[row.length];
						for(int i = 0; i < row.length; ++i){
							if(String.valueOf(row[i]).equals("null")){
								newRow[i] = null;
							}
							else if(String.valueOf(row[i]).equals("true") || String.valueOf(row[i]).equals("false")){
								newRow[i] = new Boolean(String.valueOf(row[i]));
							}
							else{
								newRow[i] = row[i];							
							}
							
						}
						String stockName = (String) newRow[stockNameIndex];
						if(stockNames.containsKey(stockName)){
							newRow[stockIdIndex] = stockNames.get(stockName);
						}else{
							System.out.println("looking for "+stockName);
							int stockId = StockDAO.getInstance().findStockByName(stockName).get(0).getStockId();
							newRow[stockIdIndex] = stockId;
							stockNames.put(stockName, stockId);
						}
						model.addRow(newRow);
						line = br.readLine();
					}
					getStocksOrderPanel().getTable().setModel(model);
					updateWhenRowCountChanged();
					br.close();

				}

			}

		}catch (Exception E) {
			System.out.println(E.toString());
			E.printStackTrace();
		}
	}

	private void addDateSection(JPanel parametersPanel) {
		JPanel subpanel = new JPanel(new MigLayout("insets 0, gapx 0"));
		final PlotIndexColumnTable table = stocksOrderPanel.getTable();
		ImageButton addDates = new ImageButton("checkmarkColor.png");
		addDates.setToolTipText("Apply this date to selected stocks");
		addDates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int column = table.getIndexOf(ColumnConstants.PLANTING_DATE);
				Date date = costomizedCalendar.getCustomDateCalendar().getDate();
				plantingDate = date;
				// Each planting group should only work for one year
				boolean validDate = true;
				if(table.getSelectedRows().length != table.getRowCount()){
					for(int row = 0; row < table.getRowCount(); row++){
						if(date.getYear() != ((Date)table.getValueAt(row, table.getIndexOf(ColumnConstants.PLANTING_DATE))).getYear()){
							validDate = false;
							break;
						}  
					}
				}				

				if(validDate){
					for(int row: table.getSelectedRows()) {
						if(((String)table.getValueAt(row, ColumnConstants.TYPES)).equals(ColumnConstants.ROW)) {
							table.setValueAt(date, row, column);
							updatePlantTagsWhenRowDataChange(row,column);
						}					
					}
					if(table.getSelectedRows().length > 0)
					{
						updateWhenRowDataChanged();
						updateWhenRowCountChanged();
						prefix = null;
						setSyncedFalse();
					}
				}else{
					JOptionPane.showMessageDialog(null, "Only one year allowed in one planting group", "Invalid Date", JOptionPane.ERROR_MESSAGE);
				}
			}			
		});
		subpanel.add(new JLabel("Planting Date: "), "gapleft 10");
		subpanel.add(costomizedCalendar.getCustomDateCalendar());
		subpanel.add(addDates, "h 24:24:24, w 24:24:24, pushx");
		parametersPanel.add(subpanel, "w 20%");
	}

	private void addKernersSection(JPanel parametersPanel) {
		JPanel subpanel = new JPanel(new MigLayout("insets 0, gapx 0"));
		final PlotIndexColumnTable table = stocksOrderPanel.getTable();
		final TextField kernels = new TextField(10);
		kernels.setPlaceholder(ColumnConstants.KERNELS);
		ImageButton setKernels = new ImageButton("checkmarkColor.png");
		setKernels.setToolTipText("Set these many no. of kernels for selected stocks");
		setKernels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int column = table.getIndexOf(ColumnConstants.KERNELS);
				if (Utils.isValidInteger(kernels.getText()))
				{
					String value = String.valueOf(Integer.parseInt(kernels.getText()));
					for(int row: table.getSelectedRows()) {
						table.setValueAt(value, row, column);
						updatePlantTagsWhenRowDataChange(row,column);
					}
					updateWhenRowDataChanged();
				}else{
					JOptionPane.showMessageDialog(null, "Invalid Integer", "", JOptionPane.ERROR_MESSAGE);
				}
				setSyncedFalse();					
			}			
		});
		subpanel.add(kernels, "h 24:24:24, w MIN(100%, 150)");
		subpanel.add(setKernels, "h 24:24:24, w 24:24:24, pushx");
		parametersPanel.add(subpanel, "w 20%, pushx, al right");
	}

	private void addDelaySection(JPanel parametersPanel) {
		JPanel subpanel = new JPanel(new MigLayout("insets 0, gapx 0"));
		final PlotIndexColumnTable table = stocksOrderPanel.getTable();
		final TextField delay = new TextField(10);
		delay.setPlaceholder(ColumnConstants.DELAY);
		ImageButton setDelay = new ImageButton("checkmarkColor.png");
		setDelay.setToolTipText("Set delay for selected stocks");
		setDelay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int column = table.getIndexOf(ColumnConstants.DELAY);
				String value = delay.getText();
				for(int row: table.getSelectedRows()) {
					table.setValueAt(value, row, column);
					updatePlantTagsWhenRowDataChange(row,column);
				}	
				updateWhenRowDataChanged();
				setSyncedFalse();
			}
		});
		subpanel.add(delay, "h 24:24:24, w MIN(100%, 150)");
		subpanel.add(setDelay, "h 24:24:24, w 24:24:24, pushx");
		parametersPanel.add(subpanel, "w 20%");
	}

	private void addPurposeSection(JPanel parametersPanel) {
		JPanel subpanel = new JPanel(new MigLayout("insets 0, gapx 0"));
		final PlotIndexColumnTable table = stocksOrderPanel.getTable();
		final TextField purpose = new TextField(10);
		purpose.setPlaceholder(ColumnConstants.PURPOSE);
		ImageButton setPurpose = new ImageButton("checkmarkColor.png");
		setPurpose.setToolTipText("Set purpose for selected stocks");
		setPurpose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				int column = table.getIndexOf(ColumnConstants.PURPOSE);
				String value = purpose.getText();
				for(int row: table.getSelectedRows()) {
					table.setValueAt(value, row, column);
					updatePlantTagsWhenRowDataChange(row,column);
				}	
				updateWhenRowDataChanged();
				setSyncedFalse();
			}
		});
		subpanel.add(purpose, "h 24:24:24, w MIN(100%, 150)");
		subpanel.add(setPurpose, "h 24:24:24, w 24:24:24, pushx");
		parametersPanel.add(subpanel, "w 20%");
	}

	private void addCommentsSection(JPanel parametersPanel) {
		JPanel subpanel = new JPanel(new MigLayout("insets 0, gapx 0"));
		final PlotIndexColumnTable table = stocksOrderPanel.getTable();
		final TextField comment = new TextField(10);
		comment.setPlaceholder(ColumnConstants.COMMENT);
		ImageButton setComment = new ImageButton("checkmarkColor.png");
		setComment.setToolTipText("Set comment for selected stocks");
		setComment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				int column = table.getIndexOf(ColumnConstants.COMMENT);
				String value = comment.getText();
				for(int row: table.getSelectedRows()) {
					table.setValueAt(value, row, column);
					updatePlantTagsWhenRowDataChange(row,column);
				}
				updateWhenRowDataChanged();
				setSyncedFalse();
			}
		});
		subpanel.add(comment, "h 24:24:24, w MIN(100%, 150)");
		subpanel.add(setComment, "h 24:24:24, w 24:24:24, wrap");
		parametersPanel.add(subpanel, "w 20%");
	}

	private void setExportButtonPanel() {
		JPanel exportPanel = stocksOrderPanel.getBottomHorizonPanel();
		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				Utils.saveTableToFile(stocksOrderPanel.getTable(), null, "PlotOrder.csv");
			}
		});
		exportPanel.add(exportButton,"push, al right");
	}





	private void addStockTableListeners() {    	
		configureStockOrderPanel();  
		addStockOrderTableListeners();
		addStockOrderNoneListener();
		addStockOrderPanelInsertListener();
		addStockOrderPanelDeleteListeners();
	}

	public void extraAdded(PlantingRow plot){
		plot.setType(ColumnConstants.ROW);
		DecimalFormat plotFormat = new DecimalFormat("00000");
		DecimalFormat tagFormat = new DecimalFormat("0000000");
		String row = plotFormat.format(getMaxRowNum() + 1);
		String plant = tagFormat.format(0);
		plot.setRow(row);
		plot.setPlant(plant);
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		Date date = calendar.getTime();
		// System.out.println(" >> 3");
		String prefix = getTagPrefix(date);
		StringBuffer tag = new StringBuffer();
		tag.append(prefix).append(row).append('.').append(plant);
		plot.setTag(tag.toString());
	}

	private void addStockOrderPanelInsertListener() {
		getStocksOrderPanel().getAddButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PlotIndexColumnTable table = getStocksOrderPanel().getTable();
				Object[] rowData = new Object[table.getColumnCount()];
				rowData[table.getIndexOf(ColumnConstants.MODIFIED)] = true;
				rowData[table.getIndexOf(ColumnConstants.ROW_NUM)] = table.getRowCount()+1;	
				rowData[table.getIndexOf(ColumnConstants.TYPES)] = ColumnConstants.ROW;
				rowData[table.getIndexOf(ColumnConstants.STOCK_NAME)] = "Filler";
				Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
				Date date = calendar.getTime();
				// System.out.println("getAddButton().addActionListener");
				//int season = ObservationUnitDAO.getInstance().getSeasonIndex(year, String.valueOf(zipcode));			
				StringBuffer tag = new StringBuffer();
				DecimalFormat plotFormat = new DecimalFormat("00000");
				DecimalFormat tagFormat = new DecimalFormat("0000000");
				String row = plotFormat.format(getMaxRowNum() + 1);
				String plant = tagFormat.format(0);
				rowData[table.getIndexOf(ColumnConstants.ROW)] = row;
				rowData[table.getIndexOf(ColumnConstants.PLANT)] = plant;
				// System.out.println(" >> 4");
				String prefix = getTagPrefix(date);
				tag.append(prefix).append(row).append('.').append(plant);
				rowData[table.getIndexOf(ColumnConstants.TAG)] = tag.toString();	
				rowData[table.getIndexOf(ColumnConstants.X)] = -1;
				rowData[table.getIndexOf(ColumnConstants.Y)] = -1;
				rowData[table.getIndexOf(ColumnConstants.START_TAG)] = 1;
				rowData[table.getIndexOf(ColumnConstants.END_TAG)] = 0;
				rowData[table.getIndexOf(ColumnConstants.COUNT)] = 0;
				rowData[table.getIndexOf(ColumnConstants.PLANTING_DATE)] = null;
				autoDelete = true;
				((DefaultTableModel)table.getModel()).addRow(rowData);
				stockList.add(new PlantingRow(rowData, table));
				updateWhenRowCountChanged();
				setTableChanged(true);
				setSyncedFalse();
				autoDelete = false;
			}			
		});
	}

	private void addStockOrderNoneListener() {
		getStocksOrderPanel().getNone().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				getStocksOrderPanel().getDeleteButton().setEnabled(false);
				getStocksOrderPanel().getMoveUpButton().setEnabled(false);
				getStocksOrderPanel().getMoveDownButton().setEnabled(false);
			}
		});
	}

	public void updateNumberOfItems(int size){
		getStocksOrderPanel().getNumberOfRows().setText(String.valueOf(size));
	}


	private void addStockOrderTableListeners() {
		getStocksOrderPanel().getTable().getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				if(autoDelete) return;
				PlotIndexColumnTable table = getStocksOrderPanel().getTable();
				autoDelete = true;				
				int row = e.getFirstRow();
				if(e.getColumn() == table.getIndexOf(ColumnConstants.COUNT)) {
					updateEndFromStart(row);
					updatePlantTagsWhenRowCountChange(row);		
					table.setValueAt(true, e.getFirstRow(), table.getIndexOf(ColumnConstants.MODIFIED));
					setTableChanged(true);
				}
				// not need to update for every single value change in table - very slow for large amount of rows. 		
				autoDelete = false;	

			}			
		}); 

		getStocksOrderPanel().getTable().addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent mouseEvent) {
				updateButtons(mouseEvent);
			}

			private void updateButtons(MouseEvent mouseEvent) {
				if(SwingUtilities.isRightMouseButton(mouseEvent)) {
					JPopupMenu popup = new JPopupMenu();
					JMenuItem change = new JMenuItem("Change Stock(s)");
					change.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							showPopup();
							updateWhenRowDataChanged();
							setSyncedFalse();
						}						
					});
					popup.add(change);
					change.setEnabled(getStocksOrderPanel().getTable().getSelectedRowCount() > 0);
					popup.show(getStocksOrderPanel().getTable(), mouseEvent.getX(), mouseEvent.getY());

					return;
				}

				int selectedRows[] = getStocksOrderPanel().getTable().getSelectedRows();
				if (selectedRows.length > 0) {
					getStocksOrderPanel().getDeleteButton().setEnabled(true);
					Arrays.sort(selectedRows);
					if (selectedRows[0] == 0) {
						getStocksOrderPanel().getMoveUpButton().setEnabled(false);
					} else {
						if(canMove(selectedRows)){
							getStocksOrderPanel().getMoveUpButton().setEnabled(true);
							getStocksOrderPanel().getTable().setDragEnabled(true);
						}
						else{
							getStocksOrderPanel().getMoveUpButton().setEnabled(false);
							getStocksOrderPanel().getTable().setDragEnabled(false);
						}
					}

					if (selectedRows[selectedRows.length - 1] == getStocksOrderPanel().getTable().getRowCount() - 1) {
						getStocksOrderPanel().getMoveDownButton().setEnabled(false);
					} else {
						if(canMove(selectedRows)){
							getStocksOrderPanel().getMoveDownButton().setEnabled(true);
							getStocksOrderPanel().getTable().setDragEnabled(true);
						}else{
							getStocksOrderPanel().getMoveDownButton().setEnabled(false);
							getStocksOrderPanel().getTable().setDragEnabled(false);
						}
					}
				} else {
					getStocksOrderPanel().getDeleteButton().setEnabled(false);
					getStocksOrderPanel().getMoveUpButton().setEnabled(false);
					getStocksOrderPanel().getMoveDownButton().setEnabled(false);
				}
			}
		});
	}

	//delete, add, move(= add + delete)
	public void updateWhenRowCountChanged(){
		updateNumberOfItems(getStocksOrderPanel().getTable().getRowCount());
		updatePlotNumbers();
		updateStockList();
		updateTagPlantsMap();
	}

	public void updateWhenRowDataChanged(){			
		updateStockList();
		updateTagPlantsMap();		
		updateNumberOfItems(getStocksOrderPanel().getTable().getRowCount());
	}


	public void populateTableBasedonDatabase(List<PlantingRow> stockList) {
		// System.out.println("Populate from populateTableBasedonDatabase");
		this.stockList = stockList;
		autoDelete = true;
		Utils.removeAllRowsFromTable((DefaultTableModel) getStocksOrderPanel().getTable().getModel());
		PlotIndexColumnTable table = getStocksOrderPanel().getTable();
		DefaultTableModel tableModel = ((DefaultTableModel) getStocksOrderPanel().getTable().getModel());
		int startCounter = 0;
		if (tableModel.getRowCount() > 0) {
			startCounter = Integer.parseInt(String.valueOf( tableModel.getValueAt(tableModel.getRowCount(), 1)));
		}
		// System.out.println("re-get season index");
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		Date date = calendar.getTime();
		//System.out.println(" >> 5");
		String prefix = getTagPrefix(date);      
		int plotIndex = ObservationUnitDAO.getInstance().getMaxPlotIndex(prefix);
		currentStartRow = plotIndex+1;
		for (int rowCounter = startCounter; rowCounter < stockList.size() + startCounter; rowCounter++) {
			Object[] rowData = stockList.get(rowCounter).getRowData(table);
			rowData[0] = rowCounter + 1;
			DecimalFormat plotFormat = new DecimalFormat("00000");
			DecimalFormat tagFormat = new DecimalFormat("0000000");
			rowData[table.getIndexOf(ColumnConstants.TYPES)] = ColumnConstants.ROW;	
			int plant = 0;
			rowData[table.getIndexOf(ColumnConstants.PLANT)] = tagFormat.format(plant);
			plotIndex++;
			rowData[table.getIndexOf(ColumnConstants.ROW)] = plotFormat.format(plotIndex);
			StringBuffer tag = new StringBuffer();
			tag.append(prefix).append(plotFormat.format(plotIndex)).append('.').append(tagFormat.format(plant));
			rowData[table.getIndexOf(ColumnConstants.TAG)] = tag.toString();	
			rowData[table.getIndexOf(ColumnConstants.START_TAG)] = 1;
			rowData[table.getIndexOf(ColumnConstants.END_TAG)] = 0;
			rowData[table.getIndexOf(ColumnConstants.COUNT)] = 0;
			((DefaultTableModel) getStocksOrderPanel().getTable().getModel()).addRow(rowData);
		}
		updateWhenRowCountChanged();
		setTableChanged(true);
		autoDelete = false;
	}

	public void populateTableFromList(List<PlantingRow> stockList){
		///System.out.println("Populate from populateTableFromList "+ stockList.get(0).getPlantingDate());
		this.stockList = stockList;
		PlotIndexColumnTable table =  getStocksOrderPanel().getTable();
		DefaultTableModel tableModel = (DefaultTableModel) getStocksOrderPanel().getTable().getModel();
		removeAllRowsFromTable(tableModel);	
		for(PlantingRow p : stockList){
			tableModel.addRow(p.getRowData(table));
			if(p.getCount()>0){
				int count = p.getCount();
				for(int i = 0; i < count; ++i){
					tag_plants.get(p.getTag()).get(i).setX(p.getX());
					tag_plants.get(p.getTag()).get(i).setY(p.getY());					
					tableModel.addRow(tag_plants.get(p.getTag()).get(i).getRowData(table));
				}
			}
		}		
		updateWhenRowCountChanged();
	}


	public void populateTableFromObjects(List<Object[]> tableRows ) {
		autoDelete = true;
		// System.out.println("Populate from populateTableFromObjects " + tableRows.get(0)[getStocksOrderPanel().getTable().getIndexOf(ColumnConstants.PLANTING_DATE)]);
		DefaultTableModel tableModel = ((DefaultTableModel) getStocksOrderPanel().getTable().getModel());
		removeAllRowsFromTable(tableModel);	
		currentStartRow = Integer.parseInt(String.valueOf(tableRows.get(0)[getStocksOrderPanel().getTable().getIndexOf(ColumnConstants.ROW)]));
		for(Object[] row : tableRows){
			tableModel.addRow(row);
			if(Utils.isInteger((String)row[row.length-1])){
				tag_Color.put((String) row[getStocksOrderPanel().getTable().getIndexOf(ColumnConstants.TAG)], new Color(Integer.parseInt((String)row[row.length-1])));					
			}

		}
		getStocksOrderPanel().getTable().setModel(tableModel);
		updateWhenRowCountChanged();

		autoDelete = false;
	}


	private void updateTagPlantsMap(){
		//System.out.println("updateTagPlantsMap start");
		if(tag_plants != null){
			tag_plants.clear();
			PlotIndexColumnTable table = getStocksOrderPanel().getTable();
			int plantCount = 0;
			// System.out.println("updateTagPlantsMap 1" + stockList.get(0).getPlantingDate());

			for(int row=0; row<table.getRowCount(); row++) {  		
				if(table.getValueAt(row, table.getIndexOf(ColumnConstants.TYPES)).equals(ColumnConstants.ROW)){					
					int count = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.COUNT)));
					if(count > 0){			
						String key = stockList.get(row-plantCount).getTag();
						tag_plants.put(key,  new ArrayList<PlantingRow>());
						List<PlantingRow> plantTagsList = tag_plants.get(key);
						for(int plantIndex = 1; plantIndex<=count;++plantIndex){	
							Object[] rowData = new Object[table.getColumnCount()];
							for(int col = 0; col < table.getColumnCount(); ++col){
								rowData[col] = table.getValueAt(row + plantIndex, col);
							}
							plantTagsList.add(new PlantingRow(rowData, table));
							plantCount++;
						}
						row = row + count -1;
					}	
				}
			}
			// System.out.println("updateTagPlantsMap 2" + stockList.get(0).getPlantingDate());

		}
		//System.out.println("updateTagPlantsMap end");
	}

	public boolean inBlock(int[] selectedRows){
		PlotIndexColumnTable table = getStocksOrderPanel().getTable();
		List<String> reps = new ArrayList<String>();
		for(int row : selectedRows)
		{
			String rep =  (String) table.getValueAt(row, table.getIndexOf(ColumnConstants.REP) );
			if(!StringUtils.isBlank(rep)){
				reps.add(rep);
			}
		}
		if(reps.isEmpty()){
			return false;
		}
		String blockRep = reps.get(0);
		String above = "above";
		for(int rowCounter = selectedRows[0]-1; rowCounter >= 0; rowCounter--){
			Object rep = table.getValueAt(rowCounter, table.getIndexOf(ColumnConstants.REP) );
			String currentRep = rep == null ? "null" : (String)rep;
			if(!currentRep.equals("null")){
				above = (String)currentRep;
				break;
			}
		}
		String below = "below";
		for(int rowCounter = selectedRows[selectedRows.length-1]+1; rowCounter <  table.getRowCount(); rowCounter++)
		{
			Object rep = table.getValueAt(rowCounter, table.getIndexOf(ColumnConstants.REP) );
			String currentRep = rep == null ? "null" : (String)rep;
			if(!currentRep.equals("null")){
				below = (String)currentRep;
				break;
			}
		}
		List<Integer> intList = new ArrayList<Integer>();
		for (int index = 0; index < selectedRows.length; index++)
		{
			intList.add(selectedRows[index]);
		} 
		if(above.equals(below)){
			return true;
		}
		return false;
	}

	private int getAbovePlot(int row){
		PlotIndexColumnTable table = getStocksOrderPanel().getTable();
		int aboveRow = row -1;
		String plotNum = (String) table.getValueAt(row, table.getIndexOf(ColumnConstants.ROW));
		while(aboveRow > 0){
			String currrentPlotNum = (String) table.getValueAt(aboveRow-1, table.getIndexOf(ColumnConstants.ROW));
			if(plotNum.equals(currrentPlotNum)){
				aboveRow--;
			}else{
				return aboveRow;
			}
		}
		return 0;
	}

	private void setMouseListenerMoveUpButton() {
		getStocksOrderPanel().getMoveUpButton().addActionListener(new ActionListener(){
			PlotIndexColumnTable table = getStocksOrderPanel().getTable();    	
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				if(rows.length > 1){
					for(int index = 0; index < rows.length; ++index){
						int count = Integer.parseInt(String.valueOf( table.getValueAt(rows[index], table.getIndexOf(ColumnConstants.COUNT))));
						if (rows[index] + count + 1 == rows[index + 1]){
							JOptionPane.showMessageDialog(null, "To move consective rows with type \"Row\", use mouse to drag selected rows and drop to desire location ");
							return ;
						}		        	
					}
				}					

				for(int row : table.getSelectedRows()){
					String type = (String) table.getValueAt(row, table.getIndexOf(ColumnConstants.TYPES));
					if(type.equals("Row")){

					}else{
						JOptionPane.showMessageDialog(null, "Select row with type \"Row\" to move.");
						return;
					}					
				}				

				for(int row : table.getSelectedRows()){
					Object[] moveRow = new Object[table.getColumnCount()];
					for(int i = 0; i < table.getColumnCount(); ++i){
						moveRow[i] = table.getValueAt(row, i);
					}				
					int currentPlantCount = Integer.parseInt(String.valueOf( table.getValueAt(row, table.getIndexOf(ColumnConstants.COUNT))));
					int belowToBe = getAbovePlot(row);
					int belowToBePlantCount = Integer.parseInt(String.valueOf( table.getValueAt(belowToBe, table.getIndexOf(ColumnConstants.COUNT))));

					int moveUpCount = belowToBePlantCount + 1;	
					int insertIndex = row - moveUpCount;

					((DefaultTableModel)table.getModel()).insertRow(insertIndex, moveRow);
					table.addRowSelectionInterval(insertIndex,insertIndex);

					int movedRow = 1;
					for(int plantIndex = 1;plantIndex <= currentPlantCount; ++plantIndex){
						for(int i = 0; i < table.getColumnCount(); ++i){
							moveRow[i] = table.getValueAt(row+plantIndex+movedRow, i);

						}
						((DefaultTableModel)table.getModel()).insertRow(row+plantIndex-moveUpCount, moveRow);
						movedRow++;
					}

					((DefaultTableModel)table.getModel()).removeRow(row + currentPlantCount + 1 );
					for(int plantIndex = 1;plantIndex <= currentPlantCount; ++plantIndex){
						((DefaultTableModel)table.getModel()).removeRow(row + currentPlantCount + 1);
					}

				}

				int[] selectedRows = table.getSelectedRows();
				Arrays.sort(selectedRows);
				if(selectedRows[0] == 0) {
					getStocksOrderPanel().getMoveUpButton().setEnabled(false);
				} else {
					getStocksOrderPanel().getMoveUpButton().setEnabled(true);
				}
				if(selectedRows[selectedRows.length-1] == table.getRowCount()-1) {
					getStocksOrderPanel().getMoveDownButton().setEnabled(false);
				} else {
					getStocksOrderPanel().getMoveDownButton().setEnabled(true);
				}
				updateWhenRowCountChanged();
				setSyncedFalse();
			}

		});

	}



	private void setMouseListenerMoveDownButton() {
		getStocksOrderPanel().getMoveDownButton().addActionListener(new ActionListener(){
			PlotIndexColumnTable table = getStocksOrderPanel().getTable();
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				if(rows.length > 1){
					for(int index = 0; index < rows.length; ++index){
						int count = Integer.parseInt(String.valueOf( table.getValueAt(rows[index], table.getIndexOf(ColumnConstants.COUNT))));
						if (rows[index] + count + 1 == rows[index + 1]){
							JOptionPane.showMessageDialog(null, "To move consective rows with type \"Row\", use mouse to drag selected rows and drop to desire location ");
							return ;
						}

					}
				}			

				for(int row : table.getSelectedRows()){
					String type = (String) table.getValueAt(row, table.getIndexOf(ColumnConstants.TYPES));
					if(type.equals("Row")){

					}else{
						JOptionPane.showMessageDialog(null, "Select row with type \"Row\" to move.");
						return;
					}					
				}

				for(int row : table.getSelectedRows()){
					Object[] moveRow = new Object[table.getColumnCount()];
					for(int i = 0; i < table.getColumnCount(); ++i){
						moveRow[i] = table.getValueAt(row, i);
					}				
					int currentPlantCount = Integer.parseInt(String.valueOf( table.getValueAt(row, table.getIndexOf(ColumnConstants.COUNT))));
					int aboveToBePlantCount = Integer.parseInt(String.valueOf( table.getValueAt((row + currentPlantCount + 1), table.getIndexOf(ColumnConstants.COUNT))));						
					int moveDownCount = currentPlantCount + aboveToBePlantCount + 2;	
					int insertIndex = row+moveDownCount;
					((DefaultTableModel)table.getModel()).insertRow(insertIndex, moveRow);
					table.addRowSelectionInterval(insertIndex,insertIndex);
					for(int plantIndex = 1;plantIndex <= currentPlantCount; ++plantIndex){
						for(int i = 0; i < table.getColumnCount(); ++i){
							moveRow[i] = table.getValueAt(row+plantIndex, i);
						}
						((DefaultTableModel)table.getModel()).insertRow(row+plantIndex+moveDownCount, moveRow);
					}
					((DefaultTableModel)table.getModel()).removeRow(row);
					for(int plantIndex = 1;plantIndex <= currentPlantCount; ++plantIndex){
						((DefaultTableModel)table.getModel()).removeRow(row);
					}

				}


				int[] selectedRows = table.getSelectedRows();
				Arrays.sort(selectedRows);
				if(selectedRows[0] == 0) {
					getStocksOrderPanel().getMoveUpButton().setEnabled(false);
				} else {
					getStocksOrderPanel().getMoveUpButton().setEnabled(true);
				}
				int lastRow = selectedRows[selectedRows.length-1];
				int count = Integer.parseInt(String.valueOf( table.getValueAt(lastRow, table.getIndexOf(ColumnConstants.COUNT))));
				if(lastRow + count == table.getRowCount()-1) {
					getStocksOrderPanel().getMoveDownButton().setEnabled(false);
				} else {
					getStocksOrderPanel().getMoveDownButton().setEnabled(true);
				}
				updateWhenRowCountChanged();
				setSyncedFalse();
			}

		});

	}




	private boolean canMove(int[] selectedRows){
		PlotIndexColumnTable table = getStocksOrderPanel().getTable();
		List<String> reps = new ArrayList<String>();
		List<String> expIds = new ArrayList<String>();

		for(int row : selectedRows)
		{
			Object rep =  table.getValueAt(row, table.getIndexOf(ColumnConstants.REP) );
			Object expId = table.getValueAt(row,table.getIndexOf(ColumnConstants.EXP_ID) );
			reps.add(rep==null ? "null" : (String)rep);
			expIds.add(expId==null ? "null" : Integer.toString(Integer.parseInt(String.valueOf(expId))));
		}

		int aboveRow = selectedRows[0]-1;
		int belowRow = selectedRows[selectedRows.length-1] + 1;
		String aboveRep = "null";
		String belowRep = "null";               
		if(aboveRow >= 0){
			Object rep = table.getValueAt(aboveRow, table.getIndexOf(ColumnConstants.REP) );
			aboveRep = rep == null ? "null" : (String)rep;
		}                               
		if(belowRow < table.getRowCount()){
			Object rep = table.getValueAt(belowRow, table.getIndexOf(ColumnConstants.REP) );
			belowRep = rep == null ? "null" : (String)rep;
		}
		ArrayList<String> uniqueReps = new ArrayList<String>();
		ArrayList<String> uniqueExpids = new ArrayList<String>();
		for(String rep : reps)
		{
			if(!uniqueReps.contains(rep))
			{
				uniqueReps.add(rep);
				uniqueExpids.add(expIds.get(uniqueReps.indexOf(rep)));
			}
		}
		//all the rows are not from experimental design = form stock selection only
		if(uniqueReps.isEmpty()){
			return true;
		}
		//contain different reps, return false           
		if(uniqueReps.size() > 1){
			return false;
		}

		String willMoveRep = uniqueReps.get(0);
		String willMoveExpid = uniqueExpids.get(0);
		boolean completeBlock = true;
		//not complete block, return false
		for(int rowCounter = belowRow; rowCounter < table.getRowCount(); rowCounter++) {
			Object rep = table.getValueAt(rowCounter,table.getIndexOf(ColumnConstants.REP) );
			Object expId = table.getValueAt(rowCounter,table.getIndexOf(ColumnConstants.EXP_ID) );
			String currentRep = rep == null ? "null" : (String)rep;
			String currentExpid = expId == null? "null" : Integer.toString(Integer.parseInt(String.valueOf(expId)));

			if(currentRep.equals("null")){
				continue;
			}
			if(currentRep.equals(willMoveRep) && currentExpid.equals(willMoveExpid)){
				completeBlock = false;
				break;

			}

		}
		for(int rowCounter = aboveRow; rowCounter >= 0 ; rowCounter--) {
			Object rep = table.getValueAt(rowCounter,table.getIndexOf(ColumnConstants.REP) );
			Object expId = table.getValueAt(rowCounter,table.getIndexOf(ColumnConstants.EXP_ID) );
			String currentRep = rep == null ? "null" : (String)rep;
			String currentExpid = expId == null? "null" : Integer.toString(Integer.parseInt(String.valueOf(expId)));

			if(currentRep.equals("null")){
				continue;
			}
			if(currentRep.equals(willMoveRep) && currentExpid.equals(willMoveExpid)){
				completeBlock = false;
				break;
			}                       
		}
		return completeBlock;   
	}



	private void addStockOrderPanelDeleteListeners() {
		getStocksOrderPanel().getDeleteButton().addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						PlotIndexColumnTable table = getStocksOrderPanel().getTable();
						boolean fromExp = false;
						int expid = 0;
						for (int row : table.getSelectedRows()) {
							String rep = table.getValueAt(row,table.getIndexOf(ColumnConstants.REP)) == null ? "null": (String) table.getValueAt(row,table.getIndexOf(ColumnConstants.REP));
							if (!rep.equals("null") && rep != null) {
								fromExp = true;
								expid = Integer.parseInt(String.valueOf( table.getValueAt(row,table.getIndexOf(ColumnConstants.EXP_ID))));
								break;
							}
						}
						if (!fromExp) {

							int rows[] = getStocksOrderPanel().getTable().getSelectedRows();
							DefaultTableModel model = (DefaultTableModel) table.getModel();
							int removedRowCount = 0;
							for (int i = 0; i < rows.length; i++) {
								if(((String)table.getValueAt(rows[i], ColumnConstants.TYPES)).equals(ColumnConstants.ROW)){
									int toRemoveRowIndex = rows[i] - removedRowCount;
									int count = Integer.parseInt(String.valueOf( table.getValueAt(rows[i], table.getIndexOf(ColumnConstants.COUNT))));
									while(count > 0){
										deleteTags(table.getValueAt(toRemoveRowIndex+count, table.getIndexOf(ColumnConstants.TAG_ID)));
										model.removeRow(toRemoveRowIndex+count);
										count--;
										removedRowCount++;
									}
									deleteTags(table.getValueAt(toRemoveRowIndex, table.getIndexOf(ColumnConstants.TAG_ID)));									
									model.removeRow(toRemoveRowIndex);									
									removedRowCount++;
								}								
							}

						} else if (fromExp&& table.getSelectedRows().length > 1) {
							JOptionPane.showMessageDialog(getStocksOrderPanel(),
									"Please select a single row with not-null rep to delete the whole design");
						} else {
							int Option = JOptionPane.showConfirmDialog(getStocksOrderPanel(),
									"Are you sure to delete the whole design ?");
							if (Option == JOptionPane.YES_OPTION) {
								DefaultTableModel model = (DefaultTableModel) table.getModel();
								int totalRow = table.getRowCount();
								ArrayList<Integer> removedRow = new ArrayList<Integer>();
								for (int rowCounter = 0; rowCounter < totalRow; rowCounter++) {
									Object exp = table.getValueAt(rowCounter,table.getIndexOf(ColumnConstants.EXP_ID));
									if (exp != null) {
										if (Integer.parseInt(String.valueOf( exp)) == expid) {
											removedRow.add(rowCounter);
										}
									}
								}
								for (int i = 0; i < removedRow.size(); ++i) {
									model.removeRow(table.convertRowIndexToModel(removedRow.get(i) - i));
								}
								// allow the user to re-randomize the experiments.
								//getExpid_expPanel().get(expid).getRandomizeButton().setEnabled(true);
							}
						}
						updateWhenRowCountChanged();
						setTableChanged(true);
						setSyncedFalse();
						getStocksOrderPanel().getDeleteButton().setEnabled(
								false);
						if (getStocksOrderPanel().getMoveDownButton() != null)
							getStocksOrderPanel().getMoveDownButton()
							.setEnabled(false);
						if (getStocksOrderPanel().getMoveUpButton() != null)
							getStocksOrderPanel().getMoveUpButton().setEnabled(
									false);
						for (int rowCounter = 0; rowCounter < table
								.getRowCount(); rowCounter++) {
							table.setValueAt(String.valueOf(rowCounter + 1),
									rowCounter, 0);
						}
						getStocksOrderPanel().getTable().clearSelection();
					}
				});
	}

	public void deleteTags(Object tagId) {
		if(tagId instanceof Integer)
		{
			ObservationUnitDAO.getInstance().delete(Integer.parseInt(String.valueOf(tagId)));
		}else if(tagId instanceof String){
			ObservationUnitDAO.getInstance().delete(Integer.parseInt((String) tagId));
		}

	}

	private void configureStockOrderPanel() {
		getStocksOrderPanel().getDeleteButton().setEnabled(false);
		getStocksOrderPanel().getMoveUpButton().setEnabled(false);
		getStocksOrderPanel().getMoveDownButton().setEnabled(false);
		getStocksOrderPanel().getTable().hideColumn(ColumnConstants.STOCK_ID);
		getStocksOrderPanel().getTable().hideColumn(ColumnConstants.TAG_ID);
	}

	private void showPopup() {
		StocksInfoPanel stockInfoPanel = CreateStocksInfoPanel.createStockInfoPanel("table view popup");
		CheckBoxIndexColumnTable popupTable = stockInfoPanel.getSaveTablePanel().getTable();
		int stocksNeeded = getStocksOrderPanel().getTable().getSelectedRowCount();
		stockInfoPanel.setSize(this.getSize());
		boolean valid = false;
		while(!valid) {
			stockInfoPanel.setSize(this.getSize());
			int option = JOptionPane.showConfirmDialog((Component) AccreteGBContext.getContext().getBean("plantingChildPanel0"), stockInfoPanel, "Choose " + stocksNeeded + " replacement stocks, add to cart and click OK ", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if(option == JOptionPane.OK_OPTION) {
				if(popupTable.getRowCount() == stocksNeeded) {
					swapData(popupTable, getStocksOrderPanel().getTable());
					valid = true;
				}
				else
					JOptionPane.showConfirmDialog((Component) AccreteGBContext.getContext().getBean("plantingChildPanel0"), "<HTML><FONT COLOR = Red>Choose exactly " + stocksNeeded + " stocks.</FONT></HTML>", "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);       
			}
			else
				break;
		}
	}

	private void swapData(CheckBoxIndexColumnTable source, PlotIndexColumnTable destination) {
		int localRows[] = destination.getSelectedRows();
		int counter = 0;
		for(int rowCounter:localRows) {
			destination.setValueAt(source.getValueAt(counter, ColumnConstants.STOCK_ID),
					rowCounter, destination.getIndexOf(ColumnConstants.STOCK_ID));
			destination.setValueAt(source.getValueAt(counter, ColumnConstants.STOCK_NAME),
					rowCounter, destination.getIndexOf(ColumnConstants.STOCK_NAME));
			destination.setValueAt(source.getValueAt(counter, ColumnConstants.ACCESSION),
					rowCounter, destination.getIndexOf(ColumnConstants.ACCESSION));
			destination.setValueAt(source.getValueAt(counter, ColumnConstants.PEDIGREE),
					rowCounter, destination.getIndexOf(ColumnConstants.PEDIGREE));
			destination.setValueAt(StringUtils.defaultIfBlank((String) source.getValueAt(counter, ColumnConstants.GENERATION), "NULL"),
					rowCounter, destination.getIndexOf(ColumnConstants.GENERATION));
			destination.setValueAt(StringUtils.defaultIfBlank((String) source.getValueAt(counter, ColumnConstants.CYCLE), "NULL"),
					rowCounter, destination.getIndexOf(ColumnConstants.CYCLE));
			destination.setValueAt(StringUtils.defaultIfBlank((String) source.getValueAt(counter, ColumnConstants.CLASSIFICATION_CODE), "NULL"),
					rowCounter, destination.getIndexOf(ColumnConstants.CLASSIFICATION_CODE));
			destination.setValueAt(StringUtils.defaultIfBlank((String) source.getValueAt(counter, ColumnConstants.POPULATION), "NULL"),					
					rowCounter, destination.getIndexOf(ColumnConstants.POPULATION));
			destination.setValueAt(true, rowCounter, destination.getIndexOf(ColumnConstants.MODIFIED));


			//change stock from filler
			if(destination.getValueAt(rowCounter,destination.getIndexOf(ColumnConstants.PLANTING_DATE))==null){
				Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
				Date date = calendar.getTime();
				destination.setValueAt(date, rowCounter, destination.getIndexOf(ColumnConstants.PLANTING_DATE));
				StringBuffer tag = new StringBuffer();
				DecimalFormat plotFormat = new DecimalFormat("00000");
				DecimalFormat tagFormat = new DecimalFormat("0000000");
				//System.out.println(" >> 6");
				String prefix = getTagPrefix(date);	
				int plotIndex = ObservationUnitDAO.getInstance().getMaxPlotIndex(prefix);	
				String row = plotFormat.format(plotIndex);
				destination.setValueAt(row, rowCounter, destination.getIndexOf(ColumnConstants.ROW));
				String plant = tagFormat.format(0);
				destination.setValueAt(plant, rowCounter, destination.getIndexOf(ColumnConstants.PLANT));
				tag.append(prefix).append(row).append('.').append(plant);
				destination.setValueAt(tag.toString(), rowCounter, destination.getIndexOf(ColumnConstants.TAG));			
			}					
			counter++;
		}
	}


	public void updateStockList() {
		//System.out.println("updateStockList start");
		if(stockList != null){
			stockList.clear();
			PlotIndexColumnTable table = getStocksOrderPanel().getTable();
			for(int rowCounter=0; rowCounter<table.getRowCount(); rowCounter++) {
				Object[] rowData = new Object[table.getColumnCount()];
				if(table.getValueAt(rowCounter, table.getIndexOf(ColumnConstants.TYPES)).equals(ColumnConstants.ROW)){
					for(int columnCounter=1; columnCounter<rowData.length; columnCounter++) {
						rowData[columnCounter] = table.getValueAt(rowCounter, columnCounter);
					}     
					PlantingRow newRow = new PlantingRow(rowData, table);
					if(tag_Color.containsKey(rowData[table.getIndexOf(ColumnConstants.TAG)]))
					{
						newRow.setPlotColor(tag_Color.get(rowData[table.getIndexOf(ColumnConstants.TAG)]));
					}
					stockList.add(newRow);          		
				}

			} 
			// System.out.println("updateStockList 2" + stockList.get(0).getPlantingDate());
		} 	
		//System.out.println("updateStockList start");
	}


	private String getTagPrefix(Date date){
		String year = String.valueOf(date.getYear()+1900).substring(2);
		int season = 0;
		if (plantingIndex != null)
		{
			season = Integer.parseInt(plantingIndex);
		}else{
			season = ObservationUnitDAO.getInstance().getSeasonIndex(year, String.valueOf(zipcode));
			plantingIndex = String.valueOf(season);
			// check with other planting groups
			Object maxIndex = GlobalProjectInfo.getPlantingInfo(projectID, "maxPlantingIndex");
			if (maxIndex != null && Integer.valueOf(plantingIndex) <= (Integer)maxIndex ) {
				plantingIndex = String.valueOf((Integer)maxIndex + 1); 
				GlobalProjectInfo.insertNewPlantingInfo(projectID, "maxPlantingIndex", Integer.valueOf(plantingIndex));
			} else {
				GlobalProjectInfo.insertNewPlantingInfo(projectID, "maxPlantingIndex", Integer.valueOf(plantingIndex));
			}
			
		}
		//System.out.println("season " + season);
		StringBuffer prefixbuffer = new StringBuffer();
		prefixbuffer.append(year).append('.').append(season).append('.').append(String.valueOf(zipcode)).append('.').toString();
		prefix = prefixbuffer.toString(); 
		//System.out.println(" new prefix " + prefix);
		return prefix;	

	} 


	public PlotIndexToolBoxPanel getStocksOrderPanel() {
		return stocksOrderPanel;
	}

	public void setStocksOrderPanel(PlotIndexToolBoxPanel stocksOrderPanel) {
		this.stocksOrderPanel = stocksOrderPanel;
	}

	public List<PlantingRow> getStockList() {
		return stockList;
	}

	public void setStockList(List<PlantingRow> stockList) {
		this.stockList = stockList;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public TagGenerator getTagGenerator() {
		return tagGenerator;
	}

	public void setTagGenerator(TagGenerator tagGenerator) {
		this.tagGenerator = tagGenerator;
	}
	public void setValidTableRows(Object[][] validTableRows) {
		this.validTableRows = validTableRows;
	}
	public HashMap<Integer, ExperimentSelectionPanel> getExpid_expPanel() {
		return expid_expPanel;
	}
	public void setExpid_expPanel(HashMap<Integer, ExperimentSelectionPanel> expid_expPanel) {
		this.expid_expPanel = expid_expPanel;
	}
	public Object[][] getValidTableRows() {
		return validTableRows;
	}
	public boolean isTableChanged() { 
		return tableChanged;
	}

	public void setTableChanged(boolean tableChanged) {
		this.tableChanged = tableChanged;
	}
	public void setSyncedFalse() {
		ChangeMonitor.markAsChanged(projectID);
		this.synced = false;
	}
	public Map<String, List<PlantingRow>> getPlantTags() {
		return tag_plants;
	}

	public void setPlantTags(Map<String, List<PlantingRow>> plantTags) {
		this.tag_plants = plantTags;
	}
	public Map<String, Color> getTag_Color() {
		return tag_Color;
	}
	public int getProjectID() {
		return projectID;
	}
	public void setProjectID(int projectID) {
		this.projectID = projectID;
	}
}