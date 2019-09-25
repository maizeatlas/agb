package org.accretegb.modules.germplasm.experimentaldesign;

import net.miginfocom.swing.MigLayout;
import org.accretegb.modules.config.AccreteGBConfiguration;
import org.accretegb.modules.constants.ExperimentConstants;
import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.TextField;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.germplasm.stocksinfo.CreateStocksInfoPanel;
import org.accretegb.modules.germplasm.stocksinfo.StocksInfoPanel;
import org.accretegb.modules.hibernate.Classification;
import org.accretegb.modules.hibernate.ExperimentFactor;
import org.accretegb.modules.hibernate.ExperimentFactorValue;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.MeasurementUnit;
import org.accretegb.modules.hibernate.Passport;
import org.accretegb.modules.hibernate.Stock;
import org.accretegb.modules.hibernate.dao.ExperimentDAO;
import org.accretegb.modules.hibernate.dao.ExperimentFactorDAO;
import org.accretegb.modules.hibernate.dao.ExperimentFactorValueDAO;
import org.accretegb.modules.hibernate.dao.MeasurementUnitDAO;
import org.accretegb.modules.hibernate.dao.StockDAO;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.util.ChangeMonitor;
import org.accretegbR.experimental.AlphaDesign;
import org.accretegbR.experimental.CompleteRandomizedDesign;
import org.accretegbR.experimental.ExperimentDesign;
import org.accretegbR.experimental.RandomizedCompleteBlockDesign;
import org.accretegbR.experimental.SplitDesign;
import org.hibernate.Session;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericXmlApplicationContext;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;
import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;
import static org.accretegb.modules.customswingcomponent.Utils.saveTableToFile;
import static org.accretegbR.main.Utils.getRPath;

/**
 * @author nkumar & tnj
 */
public class ExperimentSelectionPanel extends TabComponentPanel {
	private static final long serialVersionUID = 1L;

    private TableToolBoxPanel experimentalSelectedStocksPanel;
    private JScrollPane treatmentPane;
	private JPanel designPanel;
    @SuppressWarnings("rawtypes")
    private JComboBox experimentSelectionBox;
    private String experimentId;
	private TableToolBoxPanel experimentalOutputPanel;
    private List<Integer> isSyncedFactorValueIds = new ArrayList<Integer>();
    private JButton randomizeButton ;
    private JTextArea expComm;   
    private HashMap<String,List<String>>  treatmentInfoMap;
    private HashMap<String,List<String>>  treatmentValuesMap;
    private HashMap<String,JPopupMenu>  trtValueMenu =  new HashMap<String,JPopupMenu>();
	private List<String>  uniTypeInfo;
    private LinkedHashMap<String,List<String>> chosenTrtsInfo;
    private String stockTrtName;
    private List<String> designFactorNames;
    private JButton export = new JButton();;
    private JPopupMenu currentValuesMenu;
    private TextField entitiesTextField ;
    public JProgressBar progress = new JProgressBar();
    private JLabel reminderMsg;
    private String currentComm = "";
    private int projectID = -1;

	public void initialize() {
		setDesignSelectionComboBoxActionListener();
        setSyncButtonListener();
		setLayout(new MigLayout("insets 20 30 20 30"));
		setCurrentValuesMenu(new JPopupMenu()) ;
		getTreatmentsAndUnitTypeInfo();
		getTreatmentPanelScrollPane();
		getExperimentalSelectedStocksPanel().getTable().getColumnModel().getColumn(0).setPreferredWidth(10);
		addExperimentalSelectedStocksTableListeners();
		add(getSettingstPanel(), "gapleft 10,h 35%, w 100%, grow, gapright 5, wrap ");
		add(getExperimentalOutputPanel(), "span, h 65%, grow, push, wrap");
		addExportbuttonToButtomPanel();
		getExperimentalOutputPanel().setActionListenerDeleteButton();
		add(progress, "w 100%, span, hidemode 3");
		Utils.removeAllRowsFromTable((DefaultTableModel) getExperimentalOutputPanel().getTable().getModel());	
		String[] designFactNames = {ColumnConstants.REPLICATION};
        setDesignFactorNames(Arrays.asList(designFactNames));
        hideColumns();      
    }
    public void hideColumns(){
    	String hidecolumns[] = {ColumnConstants.EXP_ID,ColumnConstants.EXP_FACTOR_VALUE_IDS,ColumnConstants.STOCK_ID};
		getExperimentalSelectedStocksPanel().getTable().hideColumn(ColumnConstants.ROW);
		getExperimentalSelectedStocksPanel().getTable().hideColumn(ColumnConstants.STOCK_ID);
		for(String hide : hidecolumns)
		{
			getExperimentalOutputPanel().getTable().hideColumn(hide);
		}
    }
    
    //removed setComboBoxActionListener() & setSyncButtonListener() compared to initialize 
    //Otherwise, multiple same actions would be performed for one click
    public void initializeBySelection(String designName) {
		removeAll();
		revalidate();
		repaint();
		setLayout(new MigLayout("insets 20 30 20 30"));
		getExperimentalSelectedStocksPanel().getTable().getColumnModel().getColumn(0).setPreferredWidth(10);
		getTreatmentPanelScrollPane();
		add(getSettingstPanel(), "gapleft 10,h 35%, w 100%, grow, gapright 5, wrap ");
		add(getExperimentalOutputPanel(), "h 65%, span, grow, push, wrap");
		add(progress, "w 100%,span, hidemode 3");
		Utils.removeAllRowsFromTable((DefaultTableModel) getExperimentalOutputPanel().getTable().getModel());
		resetOutputTableColumnNames(designName);
		hideColumns();
		getTreatmentsAndUnitTypeInfo();
    }
    
    void setDesignSelectionComboBoxActionListener() {
        getExperimentSelectionBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("rawtypes")
                JComboBox combo = ((JComboBox) e.getSource());
                final String selectedExperiment = (String) combo.getSelectedItem();
                if (selectedExperiment.equalsIgnoreCase(ExperimentConstants.RCD_BLOCK)) {
                    RandomizedCompleteBlockDesignPanel rcbdPanel = (RandomizedCompleteBlockDesignPanel) getContext()
                            .getBean(ExperimentConstants.RCBD_DESIGN_PANEL + getName());
                    getCurrentPanel().setDesignPanel(rcbdPanel);    
                    String[] designFactNames = {ColumnConstants.REPLICATION};
                    setDesignFactorNames(Arrays.asList(designFactNames));
                    initializeBySelection(selectedExperiment);

                } else if (selectedExperiment.equalsIgnoreCase(ExperimentConstants.CRB_BLOCK)) {
                    CompleteRandomizedDesignPanel crdPanel = (CompleteRandomizedDesignPanel) getContext().getBean(
                    		ExperimentConstants.CRD_DESIGN_PANEL + getName());
                    getCurrentPanel().setDesignPanel(crdPanel); 
                    String[] designFactNames = {ColumnConstants.REPLICATION};
                    setDesignFactorNames(Arrays.asList(designFactNames));
                    initializeBySelection(selectedExperiment);
                }
                else if (selectedExperiment.equalsIgnoreCase(ExperimentConstants.ALPHA_DESIGN)) {
                    AlphaDesignPanel alphaPanel = (AlphaDesignPanel) getContext().getBean(
                    		ExperimentConstants.ALPHA_DESIGN_PANEL + getName());
                    getCurrentPanel().setDesignPanel(alphaPanel);  
                    String[] designFactNames = {ColumnConstants.REPLICATION,"Block"};
                    setDesignFactorNames(Arrays.asList(designFactNames));
                    initializeBySelection(selectedExperiment);
                }
                else if (selectedExperiment.equalsIgnoreCase(ExperimentConstants.SPLIT_DESIGN)) {
                	SplitDesignPanel splitPanel = (SplitDesignPanel) getContext().getBean(
                			ExperimentConstants.SPLIT_DESIGN_PANEL + getName());
                    getCurrentPanel().setDesignPanel(splitPanel);   
                                      	                   
                    String[] designFactNames = {ColumnConstants.REPLICATION};
                    setDesignFactorNames(Arrays.asList(designFactNames));
                    initializeBySelection(selectedExperiment);
                    addSecondTrtPanelToTrtScrollPane();  
                }
                
            }
        });
    }
    
	public void submitDesignSelectionButton() {
		if(isStockNameEmpty()){
	        String selectedExperiment = (String) experimentSelectionBox.getSelectedItem();
	        Map<String, List<String>> stocksToDetails = new HashMap<String, List<String>>();
	        CheckBoxIndexColumnTable table = getExperimentalSelectedStocksPanel().getTable();
	        for (int counter = 0; counter < getExperimentalSelectedStocksPanel().getTable().getRowCount(); counter++) {
	            String stock = (String) table.getValueAt(counter, table.getColumnModel().getColumnIndex(ColumnConstants.STOCK_NAME));
	            if (stock != null && stock.trim().length() > 0) {
	                String stockId = String.valueOf(table.getValueAt(counter,
	                        table.getColumnModel().getColumnIndex(ColumnConstants.STOCK_ID)));
	                String accession = (String) table.getValueAt(counter, table.getColumnModel()
	                        .getColumnIndex(ColumnConstants.ACCESSION));
	                String pedigree = (String) table.getValueAt(counter, table.getColumnModel().getColumnIndex(ColumnConstants.PEDIGREE));
	                String generation = (String) table.getValueAt(counter,
	                        table.getColumnModel().getColumnIndex(ColumnConstants.GENERATION));
	                String cycle = (String) table.getValueAt(counter, table.getColumnModel().getColumnIndex(ColumnConstants.CYCLE));
	                String classification_code = (String) table.getValueAt(counter, table.getColumnModel().getColumnIndex(ColumnConstants.CLASSIFICATION_CODE));
	                String population = (String) table.getValueAt(counter,
	                        table.getColumnModel().getColumnIndex(ColumnConstants.POPULATION));
	
	                if (stocksToDetails.get(stock.trim()) == null) {
	                    List<String> details = new ArrayList<String>();
	                    details.add(0, accession);
	                    details.add(1, pedigree);
	                    details.add(2, generation);
	                    details.add(3, cycle);
	                    details.add(4, classification_code);
	                    details.add(5, population);
	                    details.add(6, stockId);
	                    stocksToDetails.put(stock.trim(), details);
	                }
	            }
	        }
	        if (selectedExperiment.equalsIgnoreCase(ExperimentConstants.RCD_BLOCK)) {
	            RandomizedCompleteBlockDesignPanel rcbdPanel = (RandomizedCompleteBlockDesignPanel) getContext()
	                    .getBean(ExperimentConstants.RCBD_DESIGN_PANEL + getName());
	            if (!Utils.isValidInteger(rcbdPanel.getReps().getText())) {
	               showInvalidRepNumberMessage();
	                return;
	            }
	
	            if (!Utils.isValidInteger(rcbdPanel.getSeedCount().getText())) {
	                showInvalidSeedNumberMessage();
	                return;
	            }
	
	            processSelectedExperiment(new RandomizedCompleteBlockDesign(), (String) rcbdPanel.getMethods()
	                    .getSelectedItem(), Integer.parseInt(rcbdPanel.getReps().getText()),Integer.parseInt(rcbdPanel
	                    .getSeedCount().getText()), 0,stocksToDetails,selectedExperiment);
	        } else if (selectedExperiment.equalsIgnoreCase(ExperimentConstants.CRB_BLOCK)) {
	            CompleteRandomizedDesignPanel crdPanel = (CompleteRandomizedDesignPanel) getContext().getBean(
	            		ExperimentConstants.CRD_DESIGN_PANEL + getName());
	            if (!Utils.isValidInteger(crdPanel.getReps().getText())) {
	               showInvalidRepNumberMessage();
	                return;
	            }
	
	            if (!Utils.isValidInteger(crdPanel.getSeedCount().getText())) {
	                showInvalidSeedNumberMessage();
	                return;
	            }
	            processSelectedExperiment(new CompleteRandomizedDesign(), (String) crdPanel.getMethods().getSelectedItem(),
	                    Integer.parseInt(crdPanel.getReps().getText()),
	                    Integer.parseInt(crdPanel.getSeedCount().getText()), 0,stocksToDetails,selectedExperiment);
	        }else if (selectedExperiment.equalsIgnoreCase(ExperimentConstants.ALPHA_DESIGN)) {
	        	AlphaDesignPanel alphaDesignPanel = (AlphaDesignPanel) getContext().getBean(
	        			ExperimentConstants.ALPHA_DESIGN_PANEL + getName());
	            if (!Utils.isValidInteger(alphaDesignPanel.getReps().getText())) {
	                showInvalidRepNumberMessage();
	                return;
	            }
	            if (!Utils.isValidInteger(alphaDesignPanel.getBlockSize().getText())) {
	                JOptionPane.showMessageDialog(this, "Block must be positive number");
	                return;
	            }
	            if (!Utils.isValidInteger(alphaDesignPanel.getSeedCount().getText())) {
	                showInvalidSeedNumberMessage();
	                return;
	            }
	            String[] values = {"2","3","4"};
	            List<String> repsConstraint = Arrays.asList(values);
	            String repInput = alphaDesignPanel.getReps().getText();
	            int blockInput =  Integer.parseInt(alphaDesignPanel.getBlockSize().getText());

	            if(!repsConstraint.contains(repInput)){
	            	JOptionPane.showMessageDialog(this, "Valid rep is 2, 3, or 4");
	                return;
	            }
	            if(blockInput <= 2){
	            	JOptionPane.showMessageDialog(this, "Block should be at least 3");
	                return;
	            }
	            if( stocksToDetails.size() % blockInput != 0 || stocksToDetails.size() == 0){
	            	JOptionPane.showMessageDialog(this, "Current Stocks size is " + stocksToDetails.size() +", Stocks size must be multiple of Block");
	                return;
	            }
	            int s = stocksToDetails.size() / blockInput;
	            if(blockInput > s){
	            	JOptionPane.showMessageDialog(this, "Block ( " + blockInput +" ) should be less than Stocks size ( " + stocksToDetails.size() +" ) divided by Block ( " + blockInput +" )");
	                return;
	            }
	            if(repInput.equals("4") && ( s % 2 == 0 || s % 3 == 0)){	            	
	            	JOptionPane.showMessageDialog(this, "When Rep is 4, Stock Stocks size divided by Block should be odd, and not a mutiple of 3");
	                return;
	            }
	            processSelectedExperiment(new AlphaDesign(), (String) alphaDesignPanel.getMethods().getSelectedItem(),
	                    Integer.parseInt(alphaDesignPanel.getReps().getText()),Integer.parseInt(alphaDesignPanel.getSeedCount().getText()),
	                    Integer.parseInt(alphaDesignPanel.getBlockSize().getText()), stocksToDetails,selectedExperiment);
	        }else if (selectedExperiment.equalsIgnoreCase(ExperimentConstants.SPLIT_DESIGN)) {
	        	if(isSplitDesignInputValid()){
	        		SplitDesignPanel splitDesignPanel = (SplitDesignPanel) getContext().getBean(
	        				ExperimentConstants.SPLIT_DESIGN_PANEL + getName());
	                if (!Utils.isValidInteger(splitDesignPanel.getReps().getText())) {
	                    showInvalidRepNumberMessage();
	                    return;
	                }
	                if (!Utils.isValidInteger(splitDesignPanel.getSeedCount().getText())) {
	                    showInvalidSeedNumberMessage();
	                    return;
	                }
	                processSelectedExperiment(new SplitDesign(), (String) splitDesignPanel.getMethods().getSelectedItem(),
	                        Integer.parseInt(splitDesignPanel.getReps().getText()),
	                        Integer.parseInt(splitDesignPanel.getSeedCount().getText()), 0,stocksToDetails,selectedExperiment);
	        	}
	        	
	        }
	        getExperimentalOutputPanel().getNumberOfRows().setText(String.valueOf(getExperimentalOutputPanel().getTable().getRowCount()));
		}
    }
	
	public void showInvalidRepNumberMessage(){
		JOptionPane.showMessageDialog(this, ExperimentConstants.INVALID_REP_INPUT);
	}
	
	public void showInvalidSeedNumberMessage(){
		JOptionPane.showMessageDialog(this, ExperimentConstants.INVALID_SEED_INPUT);
	}
	
	public boolean isStockNameEmpty(){
		List<String> trtNames = new ArrayList<String>();
		JScrollPane trtsPane = getTreatmentPane();
		JPanel view = ((JPanel) trtsPane.getViewport().getView());
	   	LinkedHashMap<String, List<String>> trtsInfo = new LinkedHashMap<String, List<String>>();
    	Component comp = view.getComponent(0);
    	List<String> trtInfo = new ArrayList<String>();
   		String currentTrtName = null ;  
		for(int i = 0; i < ((Container) comp).getComponents().length; ++i){
			 Component subcomp = ((Container) comp).getComponent(i);
			 if(i == 1 && subcomp instanceof javax.swing.JComboBox<?>)
			 {
				 String name = (String)((javax.swing.JComboBox<?>) subcomp).getEditor().getItem();		
				 if(name != null && !name.equals(ExperimentConstants.TREATMENT_NAME))
				 {
					 trtNames.add(name);
					 currentTrtName = name;
				 }
			 }
			 else if(subcomp instanceof javax.swing.JTextField){
				 String input = (String)((javax.swing.JTextField) subcomp).getText();
				 trtInfo.add(input);    										 
			 }   				 
		 }
		trtsInfo.put(currentTrtName, trtInfo); 
   		   		 
	   	if(trtNames.isEmpty()){
	   		 JOptionPane.showMessageDialog(this, "Please input treatment names");
	   		 return false;
	   	 
	   	 }else{
	   		setChosenTrtsInfo(trtsInfo);
	   		return true;
	   		 
	   	 }
	}

	public boolean isSplitDesignInputValid(){
		 JPanel splitDesignTrtsOrderPanel = new JPanel();
		 splitDesignTrtsOrderPanel.setLayout(new MigLayout("insets 0 0 0 0"));
		 List<String> trtNames = new ArrayList<String>();
		 JScrollPane trtsPane = getTreatmentPane();	
    	 JPanel view = ((JPanel)trtsPane.getViewport().getView());
    	 LinkedHashMap<String, List<String>> trtsInfo = new LinkedHashMap<String, List<String>>();
    	 for(Component comp : view.getComponents()){
    		 List<String> trtInfo = new ArrayList<String>();
    		 String currentTrtName = null ;
    		 if(comp instanceof javax.swing.JPanel){
    			 for(int i = 0; i < ((Container) comp).getComponents().length; ++i){
    				 Component subcomp = ((Container) comp).getComponent(i);
    				 if((i == 1 || i == 2)&& subcomp instanceof javax.swing.JComboBox<?>)
    				 {
    					 String name = (String)((javax.swing.JComboBox<?>) subcomp).getEditor().getItem();
    					 if(name != null && !name.equals(ExperimentConstants.TREATMENT_NAME))
    					 {
    						 trtNames.add(name);
    						 currentTrtName = name;
    					 }
    				 }
    				 else{
    					 if(subcomp instanceof javax.swing.JComboBox<?>)
    					 {
    						 trtInfo.add((String)((javax.swing.JComboBox<?>) subcomp).getEditor().getItem());
    					 }else if(subcomp instanceof javax.swing.JTextField){
    						 String input = (String)((javax.swing.JTextField) subcomp).getText();
	    					 trtInfo.add(input);    						 
    					 }   					 
    				 }   				 
    			 }
    			trtsInfo.put(currentTrtName, trtInfo); 
    		 }    		 
    	 }
    	 if(trtNames.size()<2){
    		 JOptionPane.showMessageDialog(this, "Please input treatment names");
    		 return false;
    	 
    	 }else{  		 
    		 String[] trtNamesOption = trtNames.toArray(new String[trtNames.size()]);
	    	 JComboBox<String> trt1  = new JComboBox(trtNamesOption);  
	    	 JComboBox<String> trt2  = new JComboBox(trtNamesOption);  
			 splitDesignTrtsOrderPanel.add(new JLabel("First Treatment:"));
			 splitDesignTrtsOrderPanel.add(trt1,"wrap");
			 splitDesignTrtsOrderPanel.add(new JLabel("Second Treatment:"));
			 splitDesignTrtsOrderPanel.add(trt2,"wrap");
			 int Option;
			 while(true){
				 Option = JOptionPane.showConfirmDialog(this,splitDesignTrtsOrderPanel,"Specify Treatements Order", JOptionPane.YES_NO_OPTION);			
				 if(Option == JOptionPane.YES_OPTION)
				 { 
					 if(trt1.getSelectedItem().equals(trt2.getSelectedItem()))
					 {
						 JOptionPane.showMessageDialog(splitDesignTrtsOrderPanel, "Please choose two different treatments");
						 continue;
					 }else if(!trt1.getSelectedItem().equals(trtNames.get(0)) 
							 && !trt2.getSelectedItem().equals(trtNames.get(0))){
						 JOptionPane.showMessageDialog(splitDesignTrtsOrderPanel, "Stocks must be chosen");	
						 continue;
					 }else{
						 List<String> chosenTrts = new ArrayList<String>();
						 chosenTrts.add((String) trt1.getSelectedItem());
						 chosenTrts.add((String) trt2.getSelectedItem());
						 LinkedHashMap<String, List<String>> chosenTrtsInfo = new LinkedHashMap<String, List<String>>();
						 for(String trt : chosenTrts){
							 chosenTrtsInfo.put(trt, trtsInfo.get(trt));					 
						 }
						 setChosenTrtsInfo(chosenTrtsInfo);
						 return true;
					 }
				 }else{
					 return false;
				 }
			 }
			 
    	 }
	}

	/**
	 * A panel with design selection box, design panel including rep, 
	 * block input fields and submit button
	 * @return
	 */
    public JPanel getDesignSelectionPanel() {
        JPanel designSelectionPanel = new JPanel();
        designSelectionPanel.setBorder(BorderFactory.createTitledBorder("Design Selection"));
        designSelectionPanel.setLayout(new MigLayout("insets 15 20 5 15"));
        getExperimentSelectionBox().setName("Drop Down");
        designSelectionPanel.add(getExperimentSelectionBox(), "w 100%, wrap");
        designSelectionPanel.add(getDesignPanel(), "w 100%, wrap");
        getDesignPanel().setName("Panel");
        JButton randomizeButton = new JButton("Randomize");
        randomizeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {                 
                  submitDesignSelectionButton();  
                  ChangeMonitor.markAsChanged(projectID);
            }
        });
        randomizeButton.setName("Button");
        JPanel subPanel = new JPanel();
        subPanel.add(randomizeButton, "pushx, align right");
        setRandomizeButton(randomizeButton);
        JButton importButton = new JButton("Import");
        importButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {                 
            	importDesignButtonActionPerformed();                  		       
            }
        });
        subPanel.add(importButton,"pushx, align right");
        designSelectionPanel.add(subPanel, "w 100%, spanx");
        return designSelectionPanel;
    }
    
    /**
     * import design
     */
    public void importDesignButtonActionPerformed(){
    	int option = JOptionPane.showOptionDialog(null, 
		        "Choose an option to proceed", 
		        "",
		        JOptionPane.YES_NO_CANCEL_OPTION, 
		        JOptionPane.INFORMATION_MESSAGE, 
		        null, 
		        new String[]{"Import a file","Download a template","Cancel"},
		        "default");
    	String selectedExperiment = (String) experimentSelectionBox.getSelectedItem();
    	if(option ==JOptionPane.OK_OPTION )
        {	
        	//import a file		
        	if (selectedExperiment.equalsIgnoreCase(ExperimentConstants.SPLIT_DESIGN)){
        		if(!isSplitDesignInputValid()){
        			return;
        		}
        	}
        	if(isStockNameEmpty()){
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
        					DefaultTableModel model = (DefaultTableModel) getExperimentalOutputPanel().getTable().getModel(); 
        					Utils.removeAllRowsFromTable(model);
        					BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
        					// skip first line
        					String first = br.readLine();
        					String line = br.readLine().trim();
        					HashMap<String, Stock> stockNames = new HashMap<String, Stock>();
        					if (!selectedExperiment.equalsIgnoreCase(ExperimentConstants.SPLIT_DESIGN)){
        						while (line != null) {
        							System.out.println(line);
        							line = line.trim();
        							String[] row = line.split(",");
        							if(row.length != 3 ){
        								line = br.readLine();
        								continue;
        							}
        							Object[] newRow = new Object[model.getColumnCount()];
        							newRow[0] = new Boolean(false);
        							newRow[1] = row[0]; //plot
        							newRow[2] = row[1]; //replication
        							newRow[3] = null; //Exp_id
        							newRow[4] = null; //Exp_factor_value_id
        							String stockName = row[2];
        							if(stockNames.containsKey(stockName)){
        								newRow[5] = stockNames.get(stockName).getStockId(); //stock_id
        							}else{
        								System.out.println("\nlooking for "+stockName);
        								List<Stock> stocks = StockDAO.getInstance().findStockByName(stockName);
        								if(stocks.size() == 0){
        									continue;
        								}
        								newRow[5] = stocks.get(0).getStockId(); //stock_id
        								stockNames.put(stockName,  stocks.get(0));
        							}
        							newRow[6] = stockName; //stockName
        							Stock s = stockNames.get(stockName);
        							Passport p =  s.getPassport();
        							if (p != null ){
        								newRow[7] = p.getAccession_name(); //Accession_name
        								newRow[8] = p.getPedigree(); //pedigree
        								newRow[11] = p.getClassification() == null ? "NULL" : p.getClassification().getClassificationCode(); //ClassificationCode
        								newRow[12] = p.getTaxonomy() == null ? "NULL" : p.getTaxonomy().getPopulation();// Population
        							}
        							newRow[9] = s.getStockGeneration() == null ? "NULL" : s.getStockGeneration().getGeneration(); //generation
        							newRow[10] = s.getStockGeneration() == null ? "NULL" : s.getStockGeneration().getCycle(); //cycle
        							model.addRow(newRow);
        							line = br.readLine();
        						}
        					}else{
        						while (line != null) {
        							System.out.println(line);
        							line = line.trim();
        							String[] row = line.split(",");
        							if(row.length != 6 ){
        								line = br.readLine();
        								continue;
        							}
        							Object[] newRow = new Object[model.getColumnCount()];
        							newRow[0] = new Boolean(false);
        							newRow[1] = row[0]; //plot
        							newRow[2] = row[1]; //splot
        							newRow[3] = row[2]; //replication
        							newRow[4] = row[3]; //tr1
        							newRow[5] = row[4]; //tr2
        							newRow[6] = null; //Exp_id
        							newRow[7] = null; //Exp_factor_value_id
        							String stockName = row[5];
        							if(stockNames.containsKey(stockName)){
        								newRow[8] = stockNames.get(stockName).getStockId(); //stock_id
        							}else{
        								System.out.println("\nlooking for "+stockName);
        								List<Stock> stocks = StockDAO.getInstance().findStockByName(stockName);
        								if(stocks.size() == 0){
        									continue;
        								}
        								newRow[8] = stocks.get(0).getStockId(); //stock_id
        								stockNames.put(stockName,  stocks.get(0));
        							}
        							newRow[9] = stockName; //stockName
        							Stock s = stockNames.get(stockName);
        							Passport p =  s.getPassport();
        							if (p != null ){
        								newRow[10] = p.getAccession_name(); //Accession_name
        								newRow[11] = p.getPedigree(); //pedigree
        								newRow[14] = p.getClassification() == null ? "NULL" : p.getClassification().getClassificationCode(); //ClassificationCode
        								newRow[15] = p.getTaxonomy() == null ? "NULL" : p.getTaxonomy().getPopulation();// Population
        							}
        							newRow[12] = s.getStockGeneration() == null ? "NULL" : s.getStockGeneration().getGeneration(); //generation
        							newRow[13] = s.getStockGeneration() == null ? "NULL" : s.getStockGeneration().getCycle(); //cycle
        							model.addRow(newRow);
        							line = br.readLine();
        						}
        					}
        					getExperimentalOutputPanel().getTable().setModel(model);
        					br.close();
        					setNumberOfItems();
        				}
        			}

        		}catch (Exception E) {
        			System.out.println(E.toString());
        			E.printStackTrace();
        		}
        	}

        }else if(option == JOptionPane.NO_OPTION){
        	//download a template
        	JFileChooser fileChooser = new JFileChooser();
            File file = new File(System.getProperty("java.io.tmpdir") + "/" + selectedExperiment.replace("\\s+", "_")+".csv");
            fileChooser.setSelectedFile(file);
            int approve = fileChooser.showSaveDialog(this);
            if (approve != JFileChooser.APPROVE_OPTION) {
                return;
            }
            file = fileChooser.getSelectedFile();
            BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter(file));
				if (!selectedExperiment.equalsIgnoreCase(ExperimentConstants.SPLIT_DESIGN)){
					writer.write("Plot,Replication,Stock Name");
				}else{
					writer.write("Plot,Splot,Replication,tr1,tr2,Stock Name");
				}
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	return ;			 
        }else{
        	return;
        }
    	
    }
    
    /**
     * Generate ScrollPane for inputing treatment info 
     */
    public void getTreatmentPanelScrollPane() {
    	final JPanel trtPanel = new JPanel();
        trtPanel.setLayout(new MigLayout("insets 5 5 5 5")); 
        JPanel trt1Panel = treatmentPanel();
        trtPanel.add(trt1Panel,"w 100%, grow, gapleft 15, gapright 15,wrap");
        trtPanel.setPreferredSize(new Dimension (100, 270));
        JScrollPane trtScrollPane = new JScrollPane(trtPanel);
    	trtScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    	trtScrollPane.setBorder(BorderFactory.createTitledBorder("Treatments"));
    	trtScrollPane.setOpaque(false);
        setTreatmentPane(trtScrollPane);        
    }
    public void setgetExperimentalSelectedStocksPanelButtons() {
        final CheckBoxIndexColumnTable saveTable = getExperimentalSelectedStocksPanel().getTable();
        int[] selectedRows = saveTable.getSelectedRows();
        if (selectedRows.length > 0) {
            Arrays.sort(selectedRows);
            getExperimentalSelectedStocksPanel().getDeleteButton().setEnabled(true);
        } else {
        	getExperimentalSelectedStocksPanel().getDeleteButton().setEnabled(false);
        }
    }	
    private void addExperimentalSelectedStocksTableListeners(){
    	getExperimentalSelectedStocksPanel().getTable().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent arg0) {
            	setgetExperimentalSelectedStocksPanelButtons();
            }

            public void mousePressed(MouseEvent arg0) {
            	setgetExperimentalSelectedStocksPanelButtons();
            }

            public void mouseReleased(MouseEvent arg0) {
            	setgetExperimentalSelectedStocksPanelButtons();
            }
        });
    	 
    	getExperimentalSelectedStocksPanel().getAddButton().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				StocksInfoPanel stockInfoPanel = CreateStocksInfoPanel.createStockInfoPanel("exp popup");
				stockInfoPanel.setSize(new Dimension(500,400));
				int option = JOptionPane.showConfirmDialog(
						null,
						stockInfoPanel,
						"Search Stock Packets", JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE);
				if (option == JOptionPane.OK_OPTION) {
					CheckBoxIndexColumnTable stocksOutputTable = stockInfoPanel.getSearchResultsPanel().getTable();
					DefaultTableModel model = (DefaultTableModel) getExperimentalSelectedStocksPanel().getTable().getModel();
					for (int counter = 0; counter < stocksOutputTable.getSelectedRowCount(); counter++) {
	                    Object[] row = new Object[10];
	                    row[0] = new Boolean(false);
	                    row[1] = stocksOutputTable.getValueAt(stocksOutputTable.getSelectedRows()[counter],stocksOutputTable.getIndexOf(ColumnConstants.ROW));	                  
	                    row[2] = stocksOutputTable.getValueAt(stocksOutputTable.getSelectedRows()[counter],stocksOutputTable.getIndexOf(ColumnConstants.STOCK_ID));
	                    row[3] = stocksOutputTable.getValueAt(stocksOutputTable.getSelectedRows()[counter],stocksOutputTable.getIndexOf(ColumnConstants.STOCK_NAME));
	                    row[4] = stocksOutputTable.getValueAt(stocksOutputTable.getSelectedRows()[counter],stocksOutputTable.getIndexOf(ColumnConstants.ACCESSION));
	                    row[5] = stocksOutputTable.getValueAt(stocksOutputTable.getSelectedRows()[counter],stocksOutputTable.getIndexOf(ColumnConstants.PEDIGREE));
	                    row[6] = stocksOutputTable.getValueAt(stocksOutputTable.getSelectedRows()[counter],stocksOutputTable.getIndexOf(ColumnConstants.GENERATION));
	                    row[7] = stocksOutputTable.getValueAt(stocksOutputTable.getSelectedRows()[counter],stocksOutputTable.getIndexOf(ColumnConstants.CYCLE));
	                    row[8] = stocksOutputTable.getValueAt(stocksOutputTable.getSelectedRows()[counter],stocksOutputTable.getIndexOf(ColumnConstants.CLASSIFICATION_CODE));
	                    row[9] = stocksOutputTable.getValueAt(stocksOutputTable.getSelectedRows()[counter],stocksOutputTable.getIndexOf(ColumnConstants.POPULATION));	                    
	                    model.addRow(row);         
	                }
					getExperimentalSelectedStocksPanel().getTable().setModel(model);
				}
			}
    		
    	});
    	
    	getExperimentalSelectedStocksPanel().setActionListenerDeleteButton();
    }
    
    /**
     * treatment1 panel
     */
    private JPanel treatmentPanel(){
    	JButton showStockList = new JButton("Stocks");   
        showStockList.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JPanel popupPanel = getExperimentalSelectedStocksPanel();
				popupPanel.setPreferredSize(new Dimension(600,300));
				JOptionPane.showConfirmDialog(
						null,
						popupPanel,
						"Stocks As Treatment", JOptionPane.OK_CANCEL_OPTION);
			}
        	
        });
    	JPanel trt1Panel = new JPanel(new MigLayout("insets 0 0 0 0"));      
        String[] trtNames = new String[getTreatmentInfoMap().size()];
        trtNames = getTreatmentInfoMap().keySet().toArray(trtNames);
        final JComboBox<String> trtNameComboBox  = new JComboBox(trtNames);
        trtNameComboBox.setEditable(true); 
        AutoCompleteDecorator.decorate(trtNameComboBox);
     	((JTextComponent) trtNameComboBox.getEditor().getEditorComponent()).setText(ExperimentConstants.TREATMENT_NAME);    
     	trtNameComboBox.getEditor().getEditorComponent().setForeground(new Color(160, 160, 160));  
       
     	final TextField descriptionField = new TextField(22); 
        descriptionField.setPlaceholder(ExperimentConstants.DESCRIPTION);
        
        final TextField commentField = new TextField(22);
        commentField.setPlaceholder(ExperimentConstants.COMMENT);   
        
        addTrtnameComboBoxListener(trtNameComboBox, descriptionField, commentField);
        trt1Panel.add(new JLabel("Treatment 1:"), "grow, span, wrap");
        trt1Panel.add(trtNameComboBox,"w 100%,grow, wrap");
        trt1Panel.add(showStockList,"w 100%, grow, wrap");;
        trt1Panel.add(descriptionField,"w 100%, grow, wrap");
        trt1Panel.add(commentField,"w 100%");
       
        return trt1Panel;
    }
    /**
     * add FocusListener to editable combobox 
     * Performed actions(setting default text) are on descriptionField and commentField
     * @param trtNameComboBox: editable combobox that is being added listener to
     * @param descriptionField: text field
     * @param commentField: text field
     */
    public void addTrtnameComboBoxListener(final JComboBox<String> trtNameComboBox, final TextField descriptionField,final TextField commentField){
       trtNameComboBox.setEditable(true);   
       trtNameComboBox.getEditor().getEditorComponent().addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
				if(trtNameComboBox.getEditor().getItem()!= null && trtNameComboBox.getEditor().getItem().equals(ExperimentConstants.TREATMENT_NAME)){
              	   ((JTextComponent) trtNameComboBox.getEditor().getEditorComponent()).setText("");
                   trtNameComboBox.getEditor().getEditorComponent().setForeground(Color.black);
				}
			}
			public void focusLost(FocusEvent e) {
				if(trtNameComboBox.getEditor().getItem() == null || trtNameComboBox.getEditor().getItem().toString().trim().equals("")){				
				  	((JTextComponent) trtNameComboBox.getEditor().getEditorComponent()).setText(ExperimentConstants.TREATMENT_NAME); 
				  	trtNameComboBox.getEditor().getEditorComponent().setForeground(new Color(160, 160, 160));
				  	descriptionField.customizeText(ExperimentConstants.DESCRIPTION);
		        	commentField.customizeText(ExperimentConstants.COMMENT);
                    
			    }
			}       	
        });
        
        trtNameComboBox.addItemListener(new ItemListener(){       	
			public void itemStateChanged(ItemEvent event) {
			  if(trtNameComboBox.getEditor().getItem()!= null && trtNameComboBox.getEditor().getItem().equals(ExperimentConstants.TREATMENT_NAME)){
				  trtNameComboBox.getEditor().getEditorComponent().setForeground(new Color(160, 160, 160));
			  }else{
			      trtNameComboBox.getEditor().getEditorComponent().setForeground(Color.black);							 
			  }
	          String item = (String)event.getItem();
	          if (getTreatmentInfoMap().containsKey(item))
                {	
	        	    setCurrentValuesMenu(getTrtValueMenu().get(item));
	        	    String desc = getTreatmentInfoMap().get(item).get(0);
	        	    String comm = getTreatmentInfoMap().get(item).get(1);
	        	    descriptionField.customizeText(ExperimentConstants.DESCRIPTION);
	        	    commentField.customizeText(ExperimentConstants.COMMENT);
	        	    
	        	    if(!desc.equals(ExperimentConstants.DESCRIPTION))
                	{	
	        	    	descriptionField.setText(desc);		        	    	
	        	    	descriptionField.setForeground(Color.black);
                	}		        	   
	        	    if(!comm.equals(ExperimentConstants.COMMENT)){	
	        	    	commentField.setText(comm);
	        	    	commentField.setForeground(Color.black);
	        	    }
	        	   		                    
                }else{
                	setCurrentValuesMenu(new JPopupMenu());
                	if(descriptionField.getText().equals(ExperimentConstants.DESCRIPTION))
                	{
                		 descriptionField.setForeground(new Color(160, 160, 160));
                	}
                	if(commentField.getText().equals(ExperimentConstants.COMMENT))
                	{
                		 commentField.setForeground(new Color(160, 160, 160));
                	}
                   
                }
	          }
    	
        });
    }
    
    /**
     * Split design requires two treatments. 
     * This function creates new a panel for inputing treatment information
     * @return
     */
    public JPanel getSecondTrtPanelForSplitDesign(){
    	JPanel newTrtPanel = new JPanel(new MigLayout("insets 0 0 0 0")); 
        String[] trtNames = new String[getTreatmentInfoMap().size()];
        trtNames = getTreatmentInfoMap().keySet().toArray(trtNames);
        String unitss[] =  new String[getUniTypeInfo().size()];
        unitss = getUniTypeInfo().toArray(unitss);
       
        final TextField descriptionField = new TextField(22);
        descriptionField.setPlaceholder(ExperimentConstants.DESCRIPTION);

        final TextField commentField = new TextField(22);
        commentField.setPlaceholder(ExperimentConstants.COMMENT);
        
        final JComboBox<String> trtNameComboBox = new JComboBox(trtNames);
        trtNameComboBox.setEditable(true);
        AutoCompleteDecorator.decorate(trtNameComboBox);
        ((JTextComponent) trtNameComboBox.getEditor().getEditorComponent()).setText(ExperimentConstants.TREATMENT_NAME); 
        trtNameComboBox.getEditor().getEditorComponent().setForeground(new Color(160, 160, 160)); 
   	    addTrtnameComboBoxListener(trtNameComboBox, descriptionField, commentField);
        
        final JComboBox<String> unitssComboBox = new JComboBox(unitss);
        unitssComboBox.setEditable(true);    
        AutoCompleteDecorator.decorate(unitssComboBox);
        ((JTextComponent) unitssComboBox.getEditor().getEditorComponent()).setText(ExperimentConstants.UNIT_TYPE);
        unitssComboBox.getEditor().getEditorComponent().setForeground(new Color(160, 160, 160));                 
        unitssComboBox.getEditor().getEditorComponent().addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
				if(unitssComboBox.getEditor().getItem()!= null && unitssComboBox.getEditor().getItem().equals(ExperimentConstants.UNIT_TYPE)){
              	((JTextComponent) unitssComboBox.getEditor().getEditorComponent()).setText("");
              	unitssComboBox.getEditor().getEditorComponent().setForeground(Color.black);
				}
			}
			public void focusLost(FocusEvent e) {
				if(unitssComboBox.getEditor().getItem()!= null && !unitssComboBox.getEditor().getItem().toString().trim().equals("")){				
			    }else{
				  	((JTextComponent) unitssComboBox.getEditor().getEditorComponent()).setText(ExperimentConstants.UNIT_TYPE);
				  	unitssComboBox.getEditor().getEditorComponent().setForeground(new Color(160, 160, 160));               	              	
			    }
			}       	
        });
        
        
        final TextField entitiesField = new TextField(22);
        entitiesField.setPlaceholder(ExperimentConstants.ENTITIES);
        setEntitiesTextField(entitiesField);  
        
        entitiesField.addMouseListener(new MouseListener(){	
			public void mouseClicked(MouseEvent e) {
				if(getCurrentValuesMenu().getComponentCount() !=0){
					if (!getCurrentValuesMenu().isVisible()) {
			            Point p = entitiesField.getLocationOnScreen();
			            getCurrentValuesMenu().setInvoker(entitiesField);
			            getCurrentValuesMenu().setLocation((int) p.getX()+3,
			                    (int) p.getY() + entitiesField.getHeight());
			            getCurrentValuesMenu().setVisible(true);	
			       }else{			    	     
			    	    getCurrentValuesMenu().setVisible(false);			    	    
			       }
				}else{
					entitiesField.requestFocusInWindow();
				}				 				
			}

			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub				
			}

			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub				
			}

			public void mouseEntered(MouseEvent e) {
    			// TODO Auto-generated method stub				
			}

			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub				
			}	    	
	    });
        
        entitiesField.setToolTipText("Separated by comma");            
        newTrtPanel.add(new JLabel("Treatment 2:"), "grow, span, wrap");
        newTrtPanel.add(trtNameComboBox,"w 100%,grow,wrap");                 
        newTrtPanel.add(entitiesField,"w 100%,grow,wrap");            
        newTrtPanel.add(descriptionField,"w 100%,grow,wrap");        
        newTrtPanel.add(commentField,"w 100%,grow,wrap");
        newTrtPanel.add(unitssComboBox,"w 100%,grow");
        
        return newTrtPanel;
        
    }
    
    /**
     * This function adds newly generated panel into existing ScrollPane
     */
    public void addSecondTrtPanelToTrtScrollPane(){
    	JScrollPane trtsPane = getTreatmentPane();	
    	JPanel view = ((JPanel)trtsPane.getViewport().getView());
    	JPanel newTrtPanel = getSecondTrtPanelForSplitDesign();
    	newTrtPanel.revalidate();
    	newTrtPanel.repaint();
    	view.add(newTrtPanel,"w 100%, grow, gapleft 15, gapright 15");
    	trtsPane.revalidate();
		trtsPane.repaint();
    }
    
    /**
     * Combine treatment panel and design panel into a single panel
     * @return
     */
    public JPanel getSettingstPanel(){
    	JPanel settingPanel = new JPanel(new MigLayout("insets 0 0 0 0"));
    	settingPanel.add(getDesignSelectionPanel(), "h 100%, w 30%, growx, pushx");
    	settingPanel.add(getTreatmentPane()," w 40%, h 100%, grow"); 	
    	JPanel commentPanel = new JPanel(new MigLayout("insets 0 0 0 0"));
    	commentPanel.setBorder(BorderFactory.createTitledBorder("Experiment Comment"));
    	expComm = new JTextArea(" ");
    	JScrollPane commentSP = new JScrollPane(expComm);
    	setExpComm(expComm);
    	reminderMsg = new JLabel("<html><font color='red'>Not synced!</font></html>");
    	reminderMsg.setVisible(false);
    	expComm.getDocument().addDocumentListener(new DocumentListener() {

            public void removeUpdate(DocumentEvent e) {
            	if(!currentComm.equals(expComm.getText()))
             	{
            		reminderMsg.setVisible(true);
             	}else{
             		reminderMsg.setVisible(false);
             	}
            }

            public void insertUpdate(DocumentEvent e) {
            	if(!currentComm.equals(expComm.getText()))
             	{
            		reminderMsg.setVisible(true);
             	}else{
             		reminderMsg.setVisible(false);
             	}
            }

            public void changedUpdate(DocumentEvent arg0) {
            	if(!currentComm.equals(expComm.getText()))
             	{
            		reminderMsg.setVisible(true);
             	}else{
             		reminderMsg.setVisible(false);
             	}
            }

			
        });
    	commentPanel.add(commentSP,"h 100%,w 100%,grow,span, wrap");
    	commentPanel.add(reminderMsg, "grow, w 100%");
    	settingPanel.add(commentPanel,"w 30%,  h 100%, grow, wrap");    	
    	return settingPanel;
    }
   
    
    public ExperimentSelectionPanel getCurrentPanel() {
        return this;
    }

    /**
     * Retrieve existing treatment info(treatment name,entities, comment, desc),
     * and unit type info from database. Then store them locally
     */
    public void getTreatmentsAndUnitTypeInfo(){
    	HashMap<String,List<String>>  treatmentInfo =  new HashMap<String,List<String>>();
    	List<ExperimentFactor> existTreatments = ExperimentFactorDAO.getInstance().findByType(ExperimentConstants.TREATMENT);
        for(ExperimentFactor expFactor :existTreatments ){
        	List<String> trtInfo = new ArrayList<String> ();
        	String desc = String.valueOf(expFactor.getExpFactorDesc()).equals("null")? ExperimentConstants.DESCRIPTION : expFactor.getExpFactorDesc();
        	trtInfo.add(desc);
        	String comm = String.valueOf(expFactor.getExpFactorComments()).equals("null")? ExperimentConstants.COMMENT : expFactor.getExpFactorComments();
        	trtInfo.add(comm);
        	treatmentInfo.put(expFactor.getExpFactorName(), trtInfo);
        }
    	setTreatmentInfoMap(treatmentInfo);
    	
    	List<MeasurementUnit> existUnitypes = MeasurementUnitDAO.getInstance().findAll();
    	List<String> unitsInfo = new ArrayList<String> ();
        for(MeasurementUnit uniType :existUnitypes ){   	
        	unitsInfo.add(uniType.getUnitType());
        }
        
        setUniTypeInfo(unitsInfo);
        
        HashMap<String,JPopupMenu>  trtValueMenu =  new HashMap<String,JPopupMenu>();
        for(ExperimentFactor expFactor :existTreatments ){
        	List<String> values = new ArrayList<String>();
        	values = ExperimentFactorValueDAO.getInstance().findByFactorId(expFactor.getExperimentFactorId());
        	JPopupMenu  valueMenu = getEntriesPopupMenu(values);
        	trtValueMenu.put(expFactor.getExpFactorName(), valueMenu);       	
        }
        setTrtValueMenu(trtValueMenu);
    }
    
    /**
     * Create entities pup-up menu for entities text field using values
     * @param values
     * @return
     */
    private JPopupMenu getEntriesPopupMenu(List<String> values ){
    	final JPopupMenu trtValuesMenu = new JPopupMenu();
    	if(!values.isEmpty())
    	{
    		final JCheckBox[] valueCheckBox = new JCheckBox[values.size()];   		
    		JPanel buttonPanel = new JPanel(new MigLayout("insets 10 0 0 10"));
    		JButton ok = new JButton("Ok");
    		buttonPanel.add(ok,"span");
    		int row = (values.size()/2 < 1) ? 1 :( values.size()/2 );
    		JPanel valuePopupPanel = new JPanel(new GridLayout(row+1, 2)); 
    	    JScrollPane scrollPane = new JScrollPane(valuePopupPanel);	
    	   
    	    for(int valueCounter =0; valueCounter< valueCheckBox.length;++valueCounter){
    	    	String value = values.get(valueCounter);
    	    	valueCheckBox[valueCounter]= new JCheckBox(value);
    	    	valuePopupPanel.add(valueCheckBox[valueCounter]);
    		}
    	    valuePopupPanel.add(buttonPanel);
    	    trtValuesMenu.setPopupSize(250,130);
    	    trtValuesMenu.add(scrollPane);
    	    ok.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					StringBuilder selectedValues =new StringBuilder();
					for(JCheckBox jb : valueCheckBox)
					{
						if(jb.isSelected()){
							selectedValues.append(jb.getText());
							selectedValues.append(",");
						}
					}
					if(selectedValues.length()!=0)
					{
						String selection = selectedValues.toString().substring(0, selectedValues.toString().length()-1);
						getEntitiesTextField().setText(selection);
					}
					
					trtValuesMenu.setVisible(false);
				}
		    	
		    });
    	};   	
    	return trtValuesMenu;
    	
    }


    public void addExportbuttonToButtomPanel(){
    	JPanel exportPanel = getExperimentalOutputPanel().getBottomHorizonPanel();
    	JButton export = new JButton("Export");
    	setExport(export);
    	export.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
		         saveTableToFile(getExperimentalOutputPanel().getTable(), null, "ExperimentalDesign.csv");				
			}    		
    		
    	});
    	exportPanel.add(export, "push, al right, wrap");
    	
    }
    
    /**
     * Output table of different design has  different columns
     * @param designName
     */
    public void resetOutputTableColumnNames(String designName){  		  	
    	DefaultTableModel model = (DefaultTableModel) getExperimentalOutputPanel().getTable().getModel();
    	if(designName.equalsIgnoreCase(ExperimentConstants.CRB_BLOCK) || designName.equalsIgnoreCase(ExperimentConstants.RCD_BLOCK))
    	{
    		String[] colHeadings = {ColumnConstants.SELECT,ColumnConstants.PLOT,ColumnConstants.REPLICATION,ColumnConstants.EXP_ID,ColumnConstants.EXP_FACTOR_VALUE_IDS,ColumnConstants.STOCK_ID,ColumnConstants.STOCK_NAME,ColumnConstants.ACCESSION,
    				ColumnConstants.PEDIGREE,ColumnConstants.GENERATION,ColumnConstants.CYCLE,ColumnConstants.CLASSIFICATION_CODE,ColumnConstants.POPULATION};
    		model.setColumnIdentifiers(colHeadings);
    		
    	}else if(designName.equalsIgnoreCase(ExperimentConstants.ALPHA_DESIGN)){
    		String[] colHeadings = {ColumnConstants.SELECT,ColumnConstants.PLOT,"Col","Block",ColumnConstants.REPLICATION,ColumnConstants.EXP_ID,ColumnConstants.EXP_FACTOR_VALUE_IDS,ColumnConstants.STOCK_ID,ColumnConstants.STOCK_NAME,ColumnConstants.ACCESSION,ColumnConstants.PEDIGREE,
    				ColumnConstants.GENERATION,ColumnConstants.CYCLE,ColumnConstants.CLASSIFICATION_CODE,ColumnConstants.POPULATION};
    		model.setColumnIdentifiers(colHeadings);
    	}else if(designName.equalsIgnoreCase(ExperimentConstants.SPLIT_DESIGN)){
    		String[] colHeadings = {ColumnConstants.SELECT,ColumnConstants.PLOT,"Splot",ColumnConstants.REPLICATION,"trt1","trt2",ColumnConstants.EXP_ID,ColumnConstants.EXP_FACTOR_VALUE_IDS,ColumnConstants.STOCK_ID,ColumnConstants.STOCK_NAME,ColumnConstants.ACCESSION,ColumnConstants.PEDIGREE,
    				ColumnConstants.GENERATION,ColumnConstants.CYCLE,ColumnConstants.CLASSIFICATION_CODE,ColumnConstants.POPULATION};
    		model.setColumnIdentifiers(colHeadings);
    	}
    	
    	getExperimentalOutputPanel().getTable().setModel(model);
    }
    
    /**
     * Sync button listener, sync with database
     */
    public void setSyncButtonListener(){
	    progress = new JProgressBar(0, 100);
        progress.setStringPainted(true);
        progress.setVisible(false);
        progress.setIndeterminate(false);
        getExperimentalOutputPanel().getRefreshButton().setToolTipText("sync with database");
    	getExperimentalOutputPanel().getRefreshButton().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int expId = Integer.parseInt(getExperimentId());	
				String function = (String)getExperimentSelectionBox().getSelectedItem();
				String comment = getExpComm().getText();
				ExperimentDAO.getInstance().update(function,comment, expId);
				reminderMsg.setVisible(false);
				setCurrentComm(getExpComm().getText());
				CheckBoxIndexColumnTable table = getExperimentalOutputPanel().getTable();
				if(isSyncedFactorValueIds() != null && !isSyncedFactorValueIds().isEmpty() && !table.hasSynced())
				{
					ExperimentFactorValueDAO.getInstance().deleteAList(isSyncedFactorValueIds());
				}
				
				if(!table.hasSynced() && table.getRowCount() > 0){
					new SyncExperimentalDesign(table,ExperimentSelectionPanel.this).execute();		      		   									
				}
				
			}   
    	});
    }
    
 		
    public ExperimentFactor manageExperimentFactor(String expFactorName, String expFactorType, String desc, String comm){
    	ExperimentFactor existingExpFact = null;
		List<ExperimentFactor> expFactList = ExperimentFactorDAO.getInstance().findByNameType(
				expFactorName.toLowerCase(),
				expFactorType.toLowerCase(),
				desc == null ? null :desc.toLowerCase(),
				comm == null ? null :comm.toLowerCase());
		while(expFactList.size()>0){
			existingExpFact = expFactList.get(0);
			expFactList.remove(0);
		}
		if(existingExpFact == null)
		{
			existingExpFact = ExperimentFactorDAO.getInstance().insert(expFactorName, expFactorType, desc, comm);
		}

    	return existingExpFact;   	
    }
    
    public void manageExperimentFactorValue(ExperimentFactor divExpFactor, int expId, String valueLevel){
    	ExperimentFactorValue existingExpFactValue = null;
		List<ExperimentFactorValue> existingExpFactValueList = ExperimentFactorValueDAO.getInstance().findByIds(divExpFactor.getExperimentFactorId(),expId);
		while(existingExpFactValueList.size()>0){
			existingExpFactValue = existingExpFactValueList.get(0);
			existingExpFactValueList.remove(0);
		}
		if(existingExpFactValue == null)
		{
			Session session = HibernateSessionFactory.getSessionFactory().openSession();
			ExperimentFactorValueDAO.getInstance().insert(session, divExpFactor, expId, valueLevel);
		}
    }
    void processSelectedExperiment(final ExperimentDesign experimentDesign, String methodName, final int reps, int seedCount,int blockSize,
    		final Map<String, List<String>> stocksToDetails, final String designName) {
        experimentDesign.setMethodName(methodName);
        experimentDesign.setReps(reps);
        experimentDesign.setSeeds(seedCount);
        experimentDesign.setBlockSize(blockSize);

        List<String> stockListTrt = new ArrayList<String>();
        ArrayList<List<String>> treatmentList = new ArrayList<List<String>>();
        for (int rowCounter = 0; rowCounter < getExperimentalSelectedStocksPanel().getTable().getRowCount(); rowCounter++) {
            String value = (String) getExperimentalSelectedStocksPanel().getTable().getValueAt(rowCounter,
                    getExperimentalSelectedStocksPanel().getTable().getColumnModel().getColumnIndex(ColumnConstants.STOCK_NAME));
            if (!value.trim().equalsIgnoreCase("")) {
            	stockListTrt.add(value);
            }
        }

        if(experimentDesign instanceof SplitDesign )
        {
        	List<String> trt1info = (List<String>) getChosenTrtsInfo().values().toArray()[0]; 
        	List<String> trt2info = (List<String>) getChosenTrtsInfo().values().toArray()[1]; 
        	if(trt1info.size() < trt2info.size())
        	{
        		setStockTrtName("trt1");
        		treatmentList.add(stockListTrt);
        		List<String> trt2 = new  ArrayList<String>();
        		String values = trt2info.get(0).trim().equals("")? "null" : trt2info.get(0).trim() ; 
        		if(!values.equals("null")){        			
        			trt2 = Arrays.asList(values.split("\\s*,\\s*"));
        		}
        	
        		treatmentList.add(trt2);
        		
        	}else{
        		setStockTrtName("trt2");
        		List<String> trt1 = new  ArrayList<String>();
        		String values = trt1info.get(0).trim().equals("")? "null" : trt1info.get(0).trim() ;
        		if(!values.equals("null")){
        			trt1 = Arrays.asList(values.split("\\s*,\\s*"));
        		}
        		treatmentList.add(trt1);
        		treatmentList.add(stockListTrt);
        		
        	}
        	
        }else{
           treatmentList.add(stockListTrt);
        }
        
        boolean validForR = true;
        for(List<String> trt : treatmentList){
           if(trt.size() == 0)
           {
        	   validForR = false;
        	   break;
           }
        }
        if(validForR){
        	experimentDesign.setTreatments(treatmentList);
        	 Utils.removeAllRowsFromTable((DefaultTableModel) getExperimentalOutputPanel().getTable().getModel());
        	//Rcaller api needs this. Rserve API does not
            /*if (getRPath(AccreteGBConfiguration.getConfiguration().getrPath()).equals("")) {
                int option = JOptionPane.showConfirmDialog(null, "<html>R software is required for Experimental Design."
                        + "Please install R software.<br>If you have R installed, do you want to set R path ?</html>",
                        "R Settings", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    SettingMenuItem settingsMenuItem = (SettingMenuItem) getContext().getBean("settingSubMenu");
                    for (ActionListener actionListener : settingsMenuItem.getActionListeners()) {
                        actionListener.actionPerformed(new ActionEvent(settingsMenuItem, 1, ""));
                    }
                }
                return;
            }*/
            SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>() {
				@Override
				protected Integer doInBackground() throws Exception {
					 progress.setStringPainted(true);
				     progress.setVisible(true);
				  	 progress.setIndeterminate(true);
				  	 progress.setString("Executing R");
				  	 experimentDesign.applyDesign(getRPath(AccreteGBConfiguration.getConfiguration().getrPath()),designName);
				  	 if (experimentDesign.getRcbdOutput() != null) { 
		            	final DefaultTableModel model = (DefaultTableModel) getExperimentalOutputPanel().getTable().getModel();
		                CheckBoxIndexColumnTable table = getExperimentalOutputPanel().getTable();
		                Utils.removeAllRowsFromTable((DefaultTableModel) table.getModel());
		                if(designName.equalsIgnoreCase(ExperimentConstants.CRB_BLOCK) || designName.equalsIgnoreCase(ExperimentConstants.RCD_BLOCK))
		                {	           
		                	for (int rowCounter = 0; rowCounter < getExperimentalSelectedStocksPanel().getTable().getRowCount() * reps; rowCounter++) {
		    	                Object row[] = new Object[13];
		    	                row[0] = new Boolean(false);
		    	                row[3] = "";
		    	                row[4] = "";
		    	                row[table.getColumnModel().getColumnIndex(ColumnConstants.PLOT)] = new String(
		    	                        experimentDesign.getRcbdOutput()[0][rowCounter]);
		    	                row[table.getColumnModel().getColumnIndex(ColumnConstants.REPLICATION)] = new String(
		    	                        experimentDesign.getRcbdOutput()[2][rowCounter]);
		    	                row[table.getColumnModel().getColumnIndex(ColumnConstants.STOCK_NAME)] = new String(
		    	                        experimentDesign.getRcbdOutput()[1][rowCounter]);
		    	                row[table.getColumnModel().getColumnIndex(ColumnConstants.EXP_ID)] = "";
		    	                row[table.getColumnModel().getColumnIndex(ColumnConstants.EXP_FACTOR_VALUE_IDS)] = "";
		    	                String stockName = (String) row[table.getColumnModel().getColumnIndex(ColumnConstants.STOCK_NAME)];
		    	                List<String> details = stocksToDetails.get(stockName.trim());
		    	                if (details != null) {
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.ACCESSION)] = details.get(0);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.PEDIGREE)] = details.get(1);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.GENERATION)] = details.get(2);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.CYCLE)] = details.get(3);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.CLASSIFICATION_CODE)] = details.get(4);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.POPULATION)] = details.get(5);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.STOCK_ID)] = details.get(6);
		    	                }
		    	                model.addRow(row);
		    	             }
		                	 table.setModel(model);
		                }else if(designName.equalsIgnoreCase(ExperimentConstants.ALPHA_DESIGN)){
		                	for (int rowCounter = 0; rowCounter < experimentDesign.getRcbdOutput()[0].length; rowCounter++) {
		    	                Object row[] = new Object[15];
		    	                row[0] = new Boolean(false);
		    	                row[3] = "";
		    	                row[4] = "";
		    	                row[table.getColumnModel().getColumnIndex(ColumnConstants.PLOT)] = new String( experimentDesign.getRcbdOutput()[0][rowCounter]);
		    	                row[table.getColumnModel().getColumnIndex(ColumnConstants.STOCK_NAME)] = new String(experimentDesign.getRcbdOutput()[1][rowCounter]);
		    	                row[table.getColumnModel().getColumnIndex("Col")] = new String(experimentDesign.getRcbdOutput()[2][rowCounter]);
		    	                row[table.getColumnModel().getColumnIndex("Block")] = new String(experimentDesign.getRcbdOutput()[3][rowCounter]);
		    	                row[table.getColumnModel().getColumnIndex(ColumnConstants.REPLICATION)] = new String(experimentDesign.getRcbdOutput()[4][rowCounter]);
		    	                row[table.getColumnModel().getColumnIndex(ColumnConstants.EXP_ID)] = "";
		    	                row[table.getColumnModel().getColumnIndex(ColumnConstants.EXP_FACTOR_VALUE_IDS)] = "";
		    	                String stockName = (String) row[table.getColumnModel().getColumnIndex(ColumnConstants.STOCK_NAME)];
		    	                List<String> details = stocksToDetails.get(stockName.trim());
		    	                if (details != null) {
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.ACCESSION)] = details.get(0);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.PEDIGREE)] = details.get(1);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.GENERATION)] = details.get(2);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.CYCLE)] = details.get(3);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.CLASSIFICATION_CODE)] = details.get(4);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.POPULATION)] = details.get(5);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.STOCK_ID)] = details.get(6);
		    	                }
		    	                model.addRow(row);
		    	             }
		                 	table.setModel(model);
		                }else if(designName.equalsIgnoreCase(ExperimentConstants.SPLIT_DESIGN)){
		                	for (int rowCounter = 0; rowCounter < experimentDesign.getRcbdOutput()[0].length; rowCounter++) {
		    	                Object row[] = new Object[16];
		    	                row[0] = new Boolean(false);
		    	                row[3] = "";
		    	                row[4] = "";
		    	                row[table.getColumnModel().getColumnIndex(ColumnConstants.PLOT)] = new String( experimentDesign.getRcbdOutput()[0][rowCounter]);
		    	                row[table.getColumnModel().getColumnIndex("Splot")] = new String(experimentDesign.getRcbdOutput()[1][rowCounter]);
		    	                row[table.getColumnModel().getColumnIndex(ColumnConstants.REPLICATION)] = new String(experimentDesign.getRcbdOutput()[2][rowCounter]);
		    	                row[table.getColumnModel().getColumnIndex("trt1")] = new String(experimentDesign.getRcbdOutput()[3][rowCounter]);
		    	                row[table.getColumnModel().getColumnIndex("trt2")] = new String(experimentDesign.getRcbdOutput()[4][rowCounter]);
		    	                row[table.getColumnModel().getColumnIndex(ColumnConstants.EXP_ID)] = "";
		    	                row[table.getColumnModel().getColumnIndex(ColumnConstants.EXP_FACTOR_VALUE_IDS)] = "";
		    	                row[table.getColumnModel().getColumnIndex(ColumnConstants.STOCK_NAME)] = (String) row[table.getColumnModel().getColumnIndex(getStockTrtName())];
		    	                String stockName = (String) row[table.getColumnModel().getColumnIndex(ColumnConstants.STOCK_NAME)];
		    	                List<String> details = stocksToDetails.get(stockName.trim());
		    	                if (details != null) {
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.ACCESSION)] = details.get(0);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.PEDIGREE)] = details.get(1);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.GENERATION)] = details.get(2);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.CYCLE)] = details.get(3);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.CLASSIFICATION_CODE)] = details.get(4);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.POPULATION)] = details.get(5);
		    	                    row[table.getColumnModel().getColumnIndex(ColumnConstants.STOCK_ID)] = details.get(6);
		    	                }
		    	                model.addRow(row);
		    	             }
		                	
		                 	table.setModel(model);
		                 	
		                }
		            }  	
					return null;
				}
            	
				 @Override
		            protected void done() {
					 progress.setVisible(false);
					 setNumberOfItems();
				 }
            };
            worker.execute();            
        }else{
        	 JOptionPane
             .showMessageDialog(this,ExperimentConstants.EMPTY_TREATMENT_VALUE);
        }
        
    }
    
    private void setNumberOfItems(){
    	getExperimentalOutputPanel().getNumberOfRows().setText(String.valueOf(getExperimentalOutputPanel().getTable().getRowCount()));
    	
    }

    public void populateStockListTableFromObjects(List<Object[]> stockListRows) {
    	 DefaultTableModel tableModel = ((DefaultTableModel) getExperimentalSelectedStocksPanel().getTable().getModel());
    	    removeAllRowsFromTable(tableModel);	
    		for(Object[] row : stockListRows){
    			tableModel.addRow(row);               
    		}
		
	}
    
    public void populateOutputTableFromObjects(List<Object[]> expResultRows) {
    	CheckBoxIndexColumnTable table = getExperimentalOutputPanel().getTable();
    	DefaultTableModel tableModel = ((DefaultTableModel) table.getModel());
	    removeAllRowsFromTable(tableModel);	
	    int index = 0;
		for(Object[] row : expResultRows){
			if((Boolean)row[0] == true){
				table.getCheckedRows().add(index);
			}
			tableModel.addRow(row); 
			index++;
		}
	    if(!String.valueOf(table.getValueAt(0, table.getIndexOf(ColumnConstants.EXP_FACTOR_VALUE_IDS))).trim().equals("")){
	    	table.setHasSynced(true);
	    }
	    setNumberOfItems();
	}
    
 
    public TableToolBoxPanel getExperimentalOutputPanel() {
        return experimentalOutputPanel;
    }

    public void setExperimentalOutputPanel(TableToolBoxPanel experimentalOutputPanel) {
        this.experimentalOutputPanel = experimentalOutputPanel;
    }

    public TableToolBoxPanel getExperimentalSelectedStocksPanel() {
        return experimentalSelectedStocksPanel;
    }

    public void setExperimentalSelectedStocksPanel(TableToolBoxPanel experimentalSelectedStocksPanel) {
        this.experimentalSelectedStocksPanel = experimentalSelectedStocksPanel;
    }

    public JPanel getDesignPanel() {
        return designPanel;
    }

    public void setDesignPanel(JPanel designPanel) {
        this.designPanel = designPanel;
    }

    @SuppressWarnings("rawtypes")
    public JComboBox getExperimentSelectionBox() {
        return experimentSelectionBox;
    }
    
    @SuppressWarnings("rawtypes")
    public void setExperimentSelectionBox( JComboBox experimentSelectionBox) {
        this.experimentSelectionBox = experimentSelectionBox;
    }
    
	public String getExperimentId() {
		return experimentId;
	}

	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}
	
	public List<Integer> isSyncedFactorValueIds() {
		return isSyncedFactorValueIds;
	}

	public void setIsSyncedFactorValueIds(List<Integer> isSyncedFactorValueIds) {
		this.isSyncedFactorValueIds = isSyncedFactorValueIds;
	}
	
    public JButton getRandomizeButton() {
		return randomizeButton;
	}

	public void setRandomizeButton(JButton randomizeButton) {
		this.randomizeButton = randomizeButton;
		
	}
	public JScrollPane getTreatmentPane() {
		return treatmentPane;
	}


	public void setTreatmentPane(JScrollPane treatmentPane) {
		this.treatmentPane = treatmentPane;
	}

	public JTextArea getExpComm() {
		return expComm;
	}


	public void setExpComm(JTextArea expComm) {
		this.expComm = expComm;
	}
	
	public HashMap<String, List<String>> getTreatmentInfoMap() {
		return treatmentInfoMap;
	}

	public void setTreatmentInfoMap(HashMap<String, List<String>> treatmentInfoMap) {
		this.treatmentInfoMap = treatmentInfoMap;
	}
	
	public List<String> getUniTypeInfo() {
		return uniTypeInfo;
	}


	public void setUniTypeInfo(List<String> uniTypeInfo) {
		this.uniTypeInfo = uniTypeInfo;
	}
	
	public LinkedHashMap<String, List<String>> getChosenTrtsInfo() {
		return chosenTrtsInfo;
	}

	public void setChosenTrtsInfo(LinkedHashMap<String, List<String>> chosenTrtsInfo) {
		this.chosenTrtsInfo = chosenTrtsInfo;
	}
	public String getStockTrtName() {
		return stockTrtName;
	}

	public void setStockTrtName(String stockTrtName) {
		this.stockTrtName = stockTrtName;
	}
	
	public List<String> getDesignFactorNames() {
		return designFactorNames;
	}

	public void setDesignFactorNames(List<String> list) {
		this.designFactorNames = list;
	}	
	
	public JButton getExport() {
		return export;
	}

	public void setExport(JButton export) {
		this.export = export;
	}
	
	public HashMap<String, List<String>> getTreatmentValuesMap() {
		return treatmentValuesMap;
	}

	public void setTreatmentValuesMap(HashMap<String, List<String>> treatmentValuesMap) {
		this.treatmentValuesMap = treatmentValuesMap;
	}
	
	public JPopupMenu getCurrentValuesMenu() {
		return currentValuesMenu;
	}

	public void setCurrentValuesMenu(JPopupMenu currentValuesMenu) {
		this.currentValuesMenu = currentValuesMenu;
	}
	
	public HashMap<String, JPopupMenu> getTrtValueMenu() {
		return trtValueMenu;
	}

	public void setTrtValueMenu(HashMap<String, JPopupMenu> trtValueMenu) {
		this.trtValueMenu = trtValueMenu;
	}
	public TextField getEntitiesTextField() {
		return entitiesTextField;
	}

	public void setEntitiesTextField(TextField valueField) {
		this.entitiesTextField = valueField;
	}
	
	public JLabel getReminderMsg() {
		return reminderMsg;
	}
	public void setReminderMsg(JLabel reminderMsg) {
		this.reminderMsg = reminderMsg;
	}
	public String getCurrentComm() {
		return currentComm;
	}
	public void setCurrentComm(String currentComm) {
		this.currentComm = currentComm;
	}
	public int getProjectID() {
		return projectID;
	}
	public void setProjectID(int projectID) {
		this.projectID = projectID;
	}
}