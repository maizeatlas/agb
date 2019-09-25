package org.accretegb.modules.germplasm.stocksinfo;

/*
 * Licensed to Openaccretegb-common under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Openaccretegb-common licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.hibernate.dao.ClassificationDAO;
import org.accretegb.modules.hibernate.dao.StockDAO;
import org.accretegb.modules.projectexplorer.ProjectExplorerPanel;
import org.accretegb.modules.projectexplorer.ProjectTree;
import org.accretegb.modules.projectexplorer.ProjectTreeNode;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.util.ChangeMonitor;
import org.accretegb.modules.customswingcomponent.TextField;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;
import static org.accretegb.modules.customswingcomponent.Utils.addRowToTable;
import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;
import static org.accretegb.modules.customswingcomponent.Utils.saveTableToFile;

/**
 * @author nkumar
 * This file is the Panel of Stock Info Static Module
 * helps in searching stocks in the database
 */
public class StocksInfoPanel extends TabComponentPanel {

    private static final long serialVersionUID = 1L;
    private JTextField stockname;
 	private JTextField accession;
    private JTextField pedigree;
    private TextField zipcode;
    private JTextField personName;
    private JTextField fromDate;
    private JTextField toDate;
    private JTextField generation;
    private JTextField cycle;
    private JComboBox classificationCodeComboBox;
    private StockInfoTableWorker worker = null;
  

	private List<String> classificationCodeValues;
    private List<String> selectedRows;
    private JTextField population;
    private JButton buttonClear;
    private JButton buttonSubmit;
    private JButton buttonSelect;
    private JTextField multiplier;
    private JButton buttonSave;
    private TableToolBoxPanel searchResultsPanel;
    private TableToolBoxPanel saveTablePanel;
    private String projectName;
    private boolean popup;
	private JLabel matchNotFound;
    private JPanel exportPanel;
    private JCheckBox showAllPackets;
    private int projectID = -1;

	/**
     * does layout of inputs from user
     * @return the panel which has user inputs
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public JPanel searchInputsPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setToolTipText("use '*' to indicate regex");
        searchPanel.setLayout(new MigLayout("insets 5 5 5 5, gapx 0"));
        searchPanel.add(new JLabel("Search the database for stocks:"), "span, grow, wrap");
        searchPanel.add(new JLabel("Stock Name:"), "pushx");
        getStockname().setColumns(15);
        searchPanel.add(getStockname(), "wrap");
        searchPanel.add(new JLabel("Accession:"), "pushx");      
        getAccession().setColumns(15);
        searchPanel.add(getAccession(), "wrap");
        searchPanel.add(new JLabel("Pedigree:"), "pushx");
        getPedigree().setColumns(15);
        searchPanel.add(getPedigree(), "wrap");       
        searchPanel.add(new JLabel("Person Name:  "), "pushx");
        getPersonName().setColumns(15);
        searchPanel.add(getPersonName(), "wrap");
        searchPanel.add(new JLabel("From Date:"), "pushx");
        getFromDate().setToolTipText("YYYY-MM-DD Format");
        getFromDate().setColumns(15);
        searchPanel.add(getFromDate(), "wrap");
        searchPanel.add(new JLabel("To Date:"), "pushx");
        getToDate().setToolTipText("YYYY-MM-DD Format");
        getToDate().setColumns(15);
        searchPanel.add(getToDate(), "wrap");
        searchPanel.add(new JLabel("Generation:"), "pushx");
        getGeneration().setColumns(15);
        searchPanel.add(getGeneration(), "wrap");
        searchPanel.add(new JLabel("Cycle:"), "pushx");
        getCycle().setColumns(15);
        searchPanel.add(getCycle(), "wrap");
        searchPanel.add(new JLabel("Classification:"), "pushx");
        populateClassificationCodeValues();
        getClassificationCodeComboBox().setModel(
                new DefaultComboBoxModel(getClassificationCodeValues().toArray(new String[getClassificationCodeValues().size()])));
        getClassificationCodeComboBox().setPreferredSize(new Dimension(168, 20));
        getClassificationCodeComboBox().setMaximumSize(new Dimension(168, 20));
        searchPanel.add(getClassificationCodeComboBox(), "wrap");
        searchPanel.add(new JLabel("Population:"), "pushx");
        getPopulation().setColumns(15);
        searchPanel.add(getPopulation(), "wrap");
        searchPanel.add(new JLabel("ZIP Code:"), "pushx");
        getZipcode().setColumns(15);
        getZipcode().setPlaceholder("where stocks were planted");
        searchPanel.add(getZipcode(), "wrap");
        searchPanel.add(new JLabel("Show all packets:"), "pushx");
        showAllPackets = new JCheckBox("");
        searchPanel.add(showAllPackets, "wrap");
        searchPanel.add(getButtonClear(), "h 21:21:21, skip, split, pushx");
        
        getButtonClear().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
        searchPanel.add(getButtonSubmit(), "h 21:21:21, right, gapleft 5");
        getButtonSubmit().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executeTask();
              }
        });
        
        matchNotFound = new JLabel("<html><font color='red'>Not Found</font></html>");
        matchNotFound.setVisible(false);
        searchPanel.add(matchNotFound, "left");

        return searchPanel;
    }
    
    /**
     * Add import stocknames function
     * 
     */
    
    private void addImportToSaveTablePanel(){
    	JPanel toolPanel = (JPanel) getSaveTablePanel().getTopHorizonPanel();
    	JButton importStocks = new JButton("Import Stocks");
    	toolPanel.add(importStocks, 2);
    	importStocks.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				importButtonActionPerformed();
			}				
    		
		});
    	
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
				Utils.removeAllRowsFromTable((DefaultTableModel) getSaveTablePanel().getTable().getModel());
				if (!(filename.trim().endsWith(".csv"))) {
					JLabel errorFields = new JLabel("<HTML><FONT COLOR = Blue>"
							+ "File should have a .csv extension only"
							+ ".</FONT></HTML>");
					JOptionPane.showMessageDialog(null,errorFields);
				} else {
					BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
					String line = br.readLine().trim();
					ArrayList<String> stocknames = new ArrayList<String>();
					while (line != null) {
						if(line.contains("#")){
							String stockname = line.substring(0,line.trim().length() - 2);
							stocknames.add(stockname);	
						}else{
							stocknames.add(line.trim());
						}						
						line = br.readLine();
					}
					
					//populate saveTable
					List<Object []> results = StockDAO.getInstance().searchByStocks(stocknames);
                    
                    //keep duplicates
                    int index = 0 ; 
                    for (String stockname : stocknames){
                        Object[] row = null;
                        for (int rowCounter = 0; rowCounter < results.size(); rowCounter++){
                            if (results.get(rowCounter)[2].equals(stockname))
                            {
                                row = results.get(rowCounter);
                                row[0] = String.valueOf(index);
                            }
                        }                       
                        Utils.addRowToTable(row, (DefaultTableModel)getSaveTablePanel().getTable().getModel(), index, true);
                        index++;
                    }
                    				
					br.close();	
					ChangeMonitor.markAsChanged(projectID);
				}
			}
		
	}catch (Exception E) {

	}
    }
    
    

    /**
     * get the classification_code values and stores them in sampStatValues
     */
    private void populateClassificationCodeValues() {
        List<String> classificationCodeValues = ClassificationDAO.getInstance().findClassification();
        classificationCodeValues.add(0, "Select Classification Code");
        setClassificationCodeValues(classificationCodeValues);
      
    }

    /**
     * sets the properties of search results table
     * @param table - the search results table
     */
    void setSearchResultsTable(CheckBoxIndexColumnTable table) {
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setDragEnabled(true);
    }
    
    void setSaveResultsTable(CheckBoxIndexColumnTable table) {
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setDragEnabled(true);
    }

    /**
     * adds the multiplier to the search Results table Panle
     * @param tablePanel - the panel for search Results table
     */
    public void addMultiplierToSearchResultsPanel(TableToolBoxPanel tablePanel) {      
        JPanel bottomPanel = (JPanel)tablePanel.getBottomHorizonPanel();
        JButton stopSearch = new JButton("Stop Search");
        stopSearch.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(worker!=null && !worker.isDone()){
		        	worker.cancel(true);
		        	getSearchResultsPanel().getNumberOfRows().setText("0");
		        	removeAllRowsFromTable((DefaultTableModel) getSearchResultsPanel().getTable().getModel());
			        getSearchResultsPanel().getExpandButton().setEnabled(false);
			        getSearchResultsPanel().getAll().setEnabled(false);
			        getSearchResultsPanel().getNone().setEnabled(false);
		        }    
						
			}
        	
        });
        bottomPanel.add(stopSearch, 0);
        bottomPanel.add(new JLabel(ColumnConstants.X), "split, pushx, al right ");
        getMultiplier().setColumns(3);
        getMultiplier().setText("1");
        bottomPanel.add(getMultiplier(), "right");
        bottomPanel.add(getButtonSelect(), "h 25:25:25, right, wrap");
        
        
        getMultiplier().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    actionPerformedSelectButton(getSearchResultsPanel().getTable());
                }
            }
        });
    }

    /**
     * performs action when the user clicks on add to cart button
     *
     */
    private void actionPerformedSelectButton(CheckBoxIndexColumnTable  ResultsTable) {
        try {
            int times = Integer.parseInt(getMultiplier().getText());
            CheckBoxIndexColumnTable saveTable = getSaveTablePanel().getTable();
            //CheckBoxIndexColumnTable searchResultsTable = getSearchResultsPanel().getTable();

            int startRow = saveTable.getRowCount();
            int selectedRows[] = ResultsTable.getSelectedRows();
         
            for (int selectedRow : selectedRows) {
                for (int counter = 0; counter < times; counter++) {
                    Object rowData[] = new Object[saveTable.getColumnCount()];
                    rowData[1] = String.valueOf(startRow);
                    for (int columnCounter = 2; columnCounter < saveTable.getColumnCount(); columnCounter++) {
                        rowData[columnCounter] = ResultsTable.getValueAt(selectedRow, columnCounter);
                    }

                    rowData[0] = new Boolean(false);
                    DefaultTableModel model = (DefaultTableModel) saveTable.getModel();
                    model.addRow(rowData);
                    saveTable.setModel(model);
                    startRow++;
                }
            }
            updateNumberofItemsCart();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(getSearchResultsPanel(), "Select a valid number");
        }
    }
    /**
     * adds action listener to the select button
     * the action listener checks for selected stocks in the search results table
     * and using the multiplier it adds to the results to the save table
     */
    private void addActionListenerSelectButton() {
        getButtonSelect().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionPerformedSelectButton(getSearchResultsPanel().getTable());
            }

        });
    }

    /**
     * initialize the stockInfoPanel component
     */
    public void initialize() {
        setLayout(new MigLayout("insets 20, gap 0, center"));
        add(searchInputsPanel(), "gaptop 0, growy");
        setSearchResultsTable(getSearchResultsPanel().getTable());
        setSaveResultsTable(getSaveTablePanel().getTable());
        addActionListenerSelectButton();
        addMultiplierToSearchResultsPanel(getSearchResultsPanel());
        add(getSearchResultsPanel(), "gaptop 0,  h 50%, w 100%, wrap");
        add(new JLabel(""), "wrap");
        add(getSaveTablePanel(), "gaptop 5, h 50%, grow, span, push,wrap");
        addImportToSaveTablePanel();
        addSaveTablePanelListeners(getSaveTablePanel().getTable(),getSaveTablePanel());
        addExpandButtonListeners();
        exportPanel = (JPanel) getSaveTablePanel().getBottomHorizonPanel();
        exportPanel.add(getButtonSave(), "pushx, align right");
        setExportPanel(exportPanel);
        getSearchResultsPanel().getAll().setEnabled(false);
        getSearchResultsPanel().getNone().setEnabled(false);
        getButtonSave().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveTableToFile(getSaveTablePanel().getTable(), null, "selectedStocks.csv");
            }
        });
        removeAllRowsFromTable((DefaultTableModel) getSaveTablePanel().getTable().getModel());
        removeAllRowsFromTable((DefaultTableModel) getSearchResultsPanel().getTable().getModel());
        getSaveTablePanel().setActionListenerDeleteButton();
        addSaveTableModelListeners();
        if (!isPopup()) {
            getSaveTablePanel().getTable().addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        final JPopupMenu popUpMenu = new JPopupMenu();
                        JMenu addMenu = new JMenu("Add Stocks To");
                        JMenu experimentMenu = new JMenu("Experiment Design");
                        JMenu plantingMenu = new JMenu("Plantings");
                        addMenu.add(experimentMenu);
                        addMenu.add(plantingMenu);
                        popUpMenu.add(addMenu);
                        boolean isExperimentExist = false;
                        boolean isPlantingExist = false;
                        ProjectExplorerPanel explorerPanel = (ProjectExplorerPanel) getContext().getBean(
                                "projectExplorerPanel");
                        if (explorerPanel != null) {
                            List<ProjectTree> projects = explorerPanel.getProjects();
                            if (projects != null) {
                                for (ProjectTree project : projects) {
                                    if (projectName != null && !project.getProjectName().equals(projectName))
                                        continue;

                                    int experiments = project.getExperimentNode().getChildCount();
                                    if (experiments > 0) {
                                        isExperimentExist = true;
                                    }
                                    for (int counter = 0; counter < experiments; counter++) {
                                        ProjectTreeNode node = (ProjectTreeNode) project.getExperimentNode()
                                                .getChildAt(counter);
                                        JMenuItem item = new JMenuItem(node.getNodeName() + "("
                                                + project.getProjectName() + ")");
                                        item.addActionListener(new StocksInfoExperimentsListener(node, saveTablePanel));
                                        experimentMenu.add(item);
                                    }
                                    int plantings = project.getPlantingNode().getChildCount();
                                    if (plantings > 0) {
                                        isPlantingExist = true;
                                    }
                                    for (int counter = 0; counter < plantings; counter++) {
                                        ProjectTreeNode node = (ProjectTreeNode) project.getPlantingNode().getChildAt(
                                                counter);
                                        JMenuItem item = new JMenuItem(node.getNodeName() + "("
                                                + project.getProjectName() + ")");
                                        item.addActionListener(new StocksInfoPlantingsListener(node, saveTablePanel));
                                        plantingMenu.add(item);
                                    }
                                }
                            }
                        }
                        if (!isExperimentExist) {
                            experimentMenu.setEnabled(false);
                        }
                        if (!isPlantingExist) {
                            plantingMenu.setEnabled(false);
                        }
                        getSaveTablePanel().getTable().setComponentPopupMenu(popUpMenu);
                    }
                }
            });
        }
    }
    
    public void initializeForOtherModules() {		
    	remove(getSaveTablePanel());
        setLayout(new MigLayout("insets 0, gap 0, center"));
        setSize(700,21);
        getSearchResultsPanel().remove(getMultiplier());
        getSearchResultsPanel().remove(getButtonSelect());
        remove(getExportPanel());
        ((JLabel)getSearchResultsPanel().getComponent(3)).setText("");
        add(getSearchResultsPanel(), "gaptop 0, h 100%, w 100% ");
        repaint();
    }
    
    private void addExpandButtonListeners(){
    	if(getSearchResultsPanel().getExpandButton() != null){ 	
    		getSearchResultsPanel().getExpandButton().setEnabled(false);
        	getSearchResultsPanel().getExpandButton().addActionListener(new ActionListener(){
    			public void actionPerformed(ActionEvent e) {
    				TableToolBoxPanel expandedPanel = new TableToolBoxPanel();
    				Window win = SwingUtilities.getWindowAncestor((JScrollPane) getContext().getBean("tabbedScrollBarPane"));
    				final JDialog dialog = new JDialog(win, "",Dialog.ModalityType.APPLICATION_MODAL);
    				List<String> horizontalList = new ArrayList<String>();
    				horizontalList.add("selection");
    	            horizontalList.add("search");
    	        	horizontalList.add("gap");
    	    		horizontalList.add("columnSelector");
    	    		horizontalList.add("counter");
    				expandedPanel.setHorizontalList(horizontalList);   				    				
    				final CheckBoxIndexColumnTable expandedTable = new CheckBoxIndexColumnTable();   				
    				final CheckBoxIndexColumnTable sourceTable = getSearchResultsPanel().getTable();
    				int[] selectedRows = sourceTable.getSelectedRows();
    				expandedPanel.copyTable(sourceTable, expandedTable);
    				expandedPanel.setTable(expandedTable);
    				expandedPanel.initialize();
    				for( int row : selectedRows){
    					expandedTable.setValueAt(true, row, 0);
    					expandedTable.setRowSelectionInterval(row, row);
    				}
    				
    				expandedPanel.getNumberOfRows().setText(String.valueOf(sourceTable.getRowCount()));
    				//expandedTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    		        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	
    		        expandedPanel.setPreferredSize(new Dimension(screenSize.width-300, screenSize.height-300));	        
    		        expandedPanel.add(new JLabel(ColumnConstants.X), "split, right");
    		        final JTextField multiplier = new JTextField(3);
    		        multiplier.setText("1");
    		        expandedPanel.add(multiplier, "right");
    		        JButton addToCart = new JButton("Add to Cart");
    		        expandedPanel.add(addToCart, "h 25:25:25, right");
    		        addToCart.addActionListener(new ActionListener(){
    					public void actionPerformed(ActionEvent e) {
    						setMultiplier(multiplier);
    						actionPerformedSelectButton(expandedTable);	
    						dialog.dispose();
    					}
    		        	
    		        });
    		       
    		        JButton cancel = new JButton("Cancel");
    		        cancel.addActionListener(new ActionListener(){
    					public void actionPerformed(ActionEvent e) {
    						dialog.dispose();
    					}		        	
    		        });
    		        expandedPanel.add(cancel, "h 25:25:25, right, wrap");				
    	            dialog.getContentPane().add(expandedPanel);    	     
    			    dialog.pack();
       	            dialog.setLocationRelativeTo(null);
       				dialog.setVisible(true);
       				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    			
    			}
        		
        	});
    	}  	
    	
    	if(getSaveTablePanel().getExpandButton() != null){
        	getSaveTablePanel().getExpandButton().addActionListener(new ActionListener(){
    			public void actionPerformed(ActionEvent e) {
    				TableToolBoxPanel expandedPanel = new TableToolBoxPanel();
    				Window win = SwingUtilities.getWindowAncestor((JScrollPane) getContext().getBean("tabbedScrollBarPane"));
    				final JDialog dialog = new JDialog(win, "",Dialog.ModalityType.APPLICATION_MODAL);
    				List<String> horizontalList = new ArrayList<String>();
    				horizontalList.add("selection");
    	            horizontalList.add("search");
    	        	horizontalList.add("gap");
    	    		horizontalList.add("columnSelector");
    	    		horizontalList.add("counter");
    				expandedPanel.setHorizontalList(horizontalList);
    				List<String> verticalList = new ArrayList<String>();
    				verticalList.add("delete");
    				verticalList.add("moveup");
    				verticalList.add("movedown");
    				expandedPanel.setVerticalList(verticalList);   				
    				final CheckBoxIndexColumnTable expandedTable = new CheckBoxIndexColumnTable();   				
    				final CheckBoxIndexColumnTable sourceTable = getSaveTablePanel().getTable();
    				int[] selectedRows = sourceTable.getSelectedRows();
    				expandedPanel.copyTable(sourceTable, expandedTable);
    				expandedPanel.setTable(expandedTable);
    				expandedPanel.initialize();
    				expandedPanel.getNumberOfRows().setText(String.valueOf(expandedTable.getRowCount()));
    		        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	
    		        expandedPanel.setPreferredSize(new Dimension(screenSize.width-300, screenSize.height-300));	        
    		        JButton done = new JButton("Done");
    		        expandedPanel.add(done, "h 25:25:25, right");		        
    		        expandedPanel.setActionListenerDeleteButton();
    		        for( int row : selectedRows){
    					expandedTable.setValueAt(true, row, 0);
    					expandedTable.setRowSelectionInterval(row, row);
    				}
    		        done.addActionListener(new ActionListener(){
    					public void actionPerformed(ActionEvent e) {
    						dialog.dispose();
    					}
    		        	
    		        });
    		        dialog.getContentPane().add(expandedPanel);    	       				
    			    dialog.pack();
       	            dialog.setLocationRelativeTo(null);
       				dialog.setVisible(true);
       				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    			
    			}
        		
        	});
    	}  	
    }
    private void addSaveTableModelListeners(){
    	DefaultTableModel cartModel= (DefaultTableModel) getSaveTablePanel().getTable().getModel();
    	cartModel.addTableModelListener(new TableModelListener(){
    		 public void tableChanged(TableModelEvent e) {
    			 switch (e.getType()) {
    			    case TableModelEvent.INSERT:
    			    	updateNumberofItemsCart();
    			    case TableModelEvent.DELETE:
    			    	updateNumberofItemsCart();
    			    break;
    			 }
    		 }
    		 
    	});    	
    }
        
    private void updateNumberofItemsCart(){
    	ChangeMonitor.markAsChanged(projectID);
		getSaveTablePanel().getNumberOfRows().setText(String.valueOf(getSaveTablePanel().getTable().getRowCount()));
	 }
    
    private void addSaveTablePanelListeners(final CheckBoxIndexColumnTable table,final TableToolBoxPanel tableToolBoxPanel) {
    	table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setSaveTablePanelButtons(table,tableToolBoxPanel);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				if( event.getSource() == table.getSelectionModel()&& event.getFirstIndex() >= 0 )
				{
					setSaveTablePanelButtons(table,tableToolBoxPanel);
				}
			}
        	
        });
    }
    
    public void setSaveTablePanelButtons(final CheckBoxIndexColumnTable table, TableToolBoxPanel tableToolBoxPanel) {
        //final CheckBoxIndexColumnTable saveTable = getSaveTablePanel().getTable();
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length > 0) {
            Arrays.sort(selectedRows);
            tableToolBoxPanel.getMoveUpButton().setEnabled(selectedRows[0] != 0);
            tableToolBoxPanel.getMoveDownButton().setEnabled(
                    selectedRows[selectedRows.length - 1] != table.getRowCount() - 1);
            tableToolBoxPanel.getDeleteButton().setEnabled(true);
        } else {
        	tableToolBoxPanel.getMoveUpButton().setEnabled(false);
            tableToolBoxPanel.getMoveDownButton().setEnabled(false);
            tableToolBoxPanel.getDeleteButton().setEnabled(false);
        }
    }

    public JPanel getSaveTablePanel(CheckBoxIndexColumnTable table) {
        JPanel saveTablePanel = new JPanel();
        saveTablePanel.setLayout(new BorderLayout());
        saveTablePanel.add(table.getTableHeader(), BorderLayout.NORTH);
        JScrollPane tableScrollpane = new JScrollPane(table);
        saveTablePanel.add(tableScrollpane, BorderLayout.CENTER);
        return saveTablePanel;
    }

    public void clearFields() {
        getStockname().setText("");
        getAccession().setText("");
        getPedigree().setText("");
        getZipcode().setPlaceholder("where stocks were planted");
        getPersonName().setText("");
        getFromDate().setText("");
        getToDate().setText("");
        getGeneration().setText("");
        getCycle().setText("");
        getClassificationCodeComboBox().setSelectedIndex(0);
        getPopulation().setText("");
        matchNotFound.setVisible(false);
        /*getSearchResultsPanel().getNumberOfRows().setText("0");
        getSearchResultsPanel().getExpandButton().setEnabled(false);
        getSearchResultsPanel().getAll().setEnabled(false);
        getSearchResultsPanel().getNone().setEnabled(false);*/
        }

    boolean isAtLeastOneInput() {
        if ((getStockname().getText().trim().length()==0 && getAccession().getText().trim().length() == 0) && (getPedigree().getText().trim().length() == 0)
                && (getZipcode().getText().trim().length() == 0) && (getPersonName().getText().trim().length() == 0)
                && (getFromDate().getText().trim().length() == 0) && (getToDate().getText().trim().length() == 0)
                && (getGeneration().getText().trim().length() == 0) && (getCycle().getText().trim().length() == 0)
                && (getClassificationCodeComboBox().getSelectedIndex() == 0) && (getPopulation().getText().trim().length() == 0)) {
            return false;
        }
        return true;
    }

    public Map<String, String> generateSearchValuesMap() {
        Map<String, String> searchValues = new HashMap<String, String>();
        if (getZipcode().getText().trim().length() != 0) {
            searchValues.put("zipcode", getZipcode().getText().trim());
        }
        if ((getFromDate().getText().trim().length()) != 0 || (getToDate().getText().trim().length() != 0)) {
            String fromDate = getFromDate().getText().trim();
            String toDate = getToDate().getText().trim();
            if (fromDate.length() == 0) {
                fromDate = "0";
            }
            if (toDate.length() == 0) {
                toDate = "0";
            }
            searchValues.put("stock_date", fromDate + ":" + toDate);
        }
        if (getStockname().getText().trim().length() != 0) {
            searchValues.put("stock_name", getStockname().getText().trim());
        }

        if (getAccession().getText().trim().length() != 0) {
            searchValues.put("accession_name", getAccession().getText().trim());
        }

        if (getPedigree().getText().trim().length() != 0) {
            searchValues.put("pedigree", getPedigree().getText().trim());
        }

        if (getPersonName().getText().trim().length() != 0) {
            searchValues.put("person_name", getPersonName().getText().trim());
        }

        if (getGeneration().getText().trim().length() != 0) {
            searchValues.put("generation", getGeneration().getText().trim());
        }
        if (getCycle().getText().trim().length() != 0) {
            searchValues.put("cycle", getCycle().getText().trim());
        }

        if (getPopulation().getText().trim().length() != 0) {
            searchValues.put("population", getPopulation().getText().trim());
        }

        if (getClassificationCodeComboBox().getSelectedIndex() != 0) {
            String selectedItem = (String) getClassificationCodeComboBox().getSelectedItem();
            String numberValue[] = selectedItem.trim().split("-");
            searchValues.put("classification_code", numberValue[0].trim());
        }

        return searchValues;
    }

    public boolean isInputValid() {
        if (!isAtLeastOneInput()) {
            JLabel errorFields = new JLabel("<HTML><FONT COLOR = Blue>Error: Enter atleast one parameter"
                    + ".</FONT></HTML>");
            JOptionPane.showMessageDialog(this, errorFields);
            return false;
        }

        if (getZipcode().getText().trim().length() != 5 && !(getZipcode().getText().trim().length() == 0)) {
            JLabel errorFields = new JLabel(
                    "<HTML><FONT COLOR = Blue>Error: Zip Code must be entered with 5 characters" + ".</FONT></HTML>");
            JOptionPane.showMessageDialog(this, errorFields);
            return false;
        }
        if (!Utils.isValidDate(getFromDate().getText()) && !(getFromDate().getText().trim().length() == 0)) {
            JLabel errorFields = new JLabel("<HTML><FONT COLOR = Blue>Error: Start Date is not valid"
                    + ".</FONT></HTML>");
            JOptionPane.showMessageDialog(this, errorFields);
            return false;
        }
        if (!Utils.isValidDate(getToDate().getText()) && !(getToDate().getText().trim().length() == 0)) {
            JLabel errorFields = new JLabel("<HTML><FONT COLOR = Blue>Error: End Date is not valid" + ".</FONT></HTML>");
            JOptionPane.showMessageDialog(this, errorFields);
            return false;
        }

        return true;
    }
    
    public void populateTableFromObjects(List<Object[]> exportTableRows) {
	    DefaultTableModel tableModel = ((DefaultTableModel)getSaveTablePanel().getTable().getModel());
	    removeAllRowsFromTable(tableModel);	
		for(Object[] row : exportTableRows){
			row[0] = new Boolean(false);
			tableModel.addRow(row);               
		}		
	}
    
    public void executeTask() {
        if (!isInputValid()) {
            return;
        }
        this.matchNotFound.setVisible(false);
        removeAllRowsFromTable((DefaultTableModel) getSearchResultsPanel().getTable().getModel());
        getSearchResultsPanel().getNumberOfRows().setText("Loading...");
        getSearchResultsPanel().getExpandButton().setEnabled(false);
        getSearchResultsPanel().getAll().setEnabled(false);
        getSearchResultsPanel().getNone().setEnabled(false);
        worker = new StockInfoTableWorker((DefaultTableModel) getSearchResultsPanel().getTable()
                .getModel(),this, generateSearchValuesMap(), null);
        worker.execute();
       
    }

    public List<String> getSelectedRows() {
        return selectedRows;
    }

    public void setSelectedRows(List<String> selectedRows) {
        this.selectedRows = selectedRows;
    }

    public JTextField getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(JTextField multiplier) {
        this.multiplier = multiplier;
    }

    public TableToolBoxPanel getSearchResultsPanel() {
        return searchResultsPanel;
    }

    public void setSearchResultsPanel(TableToolBoxPanel searchResultsPanel) {
        this.searchResultsPanel = searchResultsPanel;
    }
    
    public JTextField getStockname() {
		return stockname;
	}

	public void setStockname(JTextField stockname) {
		this.stockname = stockname;
	}

    public JTextField getAccession() {
        return accession;
    }

    public void setAccession(JTextField accession) {
        this.accession = accession;
    }

    public JTextField getPedigree() {
        return pedigree;
    }

    public void setPedigree(JTextField pedigree) {
        this.pedigree = pedigree;
    }

    public TextField getZipcode() {
        return zipcode;
    }

    public void setZipcode(TextField zipcode) {
        this.zipcode = zipcode;
    }

    public JTextField getPersonName() {
        return personName;
    }

    public void setPersonName(JTextField personName) {
        this.personName = personName;
    }

    public JTextField getFromDate() {
        return fromDate;
    }

    public void setFromDate(JTextField fromDate) {
        this.fromDate = fromDate;
    }

    public JTextField getToDate() {
        return toDate;
    }

    public void setToDate(JTextField toDate) {
        this.toDate = toDate;
    }

    public JTextField getGeneration() {
        return generation;
    }

    public void setGeneration(JTextField generation) {
        this.generation = generation;
    }

    public JTextField getCycle() {
        return cycle;
    }

    public void setCycle(JTextField cycle) {
        this.cycle = cycle;
    }

    public JComboBox getClassificationCodeComboBox() {
        return classificationCodeComboBox;
    }

    public void setClassificationCodeComboBox(JComboBox classification_code) {
        this.classificationCodeComboBox = classification_code;
    }
    

    public List<String> getClassificationCodeValues() {
        return classificationCodeValues;
    }

    public void setClassificationCodeValues(List<String> classification_codeValues) {
        this.classificationCodeValues = classification_codeValues;
    }

    public JTextField getPopulation() {
        return population;
    }

    public void setPopulation(JTextField population) {
        this.population = population;
    }

    public JButton getButtonClear() {
        return buttonClear;
    }

    public void setButtonClear(JButton buttonClear) {
        this.buttonClear = buttonClear;
    }

    public JButton getButtonSubmit() {
        return buttonSubmit;
    }

    public void setButtonSubmit(JButton buttonSubmit) {
        this.buttonSubmit = buttonSubmit;
    }

    public JButton getButtonSelect() {
        return buttonSelect;
    }

    public void setButtonSelect(JButton buttonSelect) {
        this.buttonSelect = buttonSelect;
    }

    public JButton getButtonSave() {
        return buttonSave;
    }

    public void setButtonSave(JButton buttonSave) {
        this.buttonSave = buttonSave;
    }

    public TableToolBoxPanel getSaveTablePanel() {
        return saveTablePanel;
    }
    
	public void setSaveTablePanel(TableToolBoxPanel saveTablePanel) {
        this.saveTablePanel = saveTablePanel;
    }
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public boolean isPopup() {
		return popup;
	}
	
	public void setPopup(boolean popup) {
		this.popup = popup;
	}
	public JPanel getExportPanel() {
		return exportPanel;
	}

	public void setExportPanel(JPanel exportPanel) {
		this.exportPanel = exportPanel;
	}
	
	public JLabel getMatchNotFound() {
		return matchNotFound;
	}

	public void setMatchNotFound(JLabel matchNotFound) {
		this.matchNotFound = matchNotFound;
	}

	public JCheckBox getShowAllPackets() {
		return showAllPackets;
	}

	public void setShowAllPackets(JCheckBox showAllPackets) {
		this.showAllPackets = showAllPackets;
	}

	public int getProjectID() {
		return projectID;
	}
	public void setProjectID(int projectID) {
		this.projectID = projectID;
	}
}