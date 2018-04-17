package org.accretegb.modules.germplasm.stockannotation;

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

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.germplasm.harvesting.Harvesting;
import org.accretegb.modules.germplasm.stocksinfo.CreateStocksInfoPanel;
import org.accretegb.modules.germplasm.stocksinfo.StocksInfoPanel;
import org.accretegb.modules.hibernate.Classification;
import org.accretegb.modules.hibernate.HarvestingGroup;
import org.accretegb.modules.hibernate.PMProject;
import org.accretegb.modules.hibernate.Passport;
import org.accretegb.modules.hibernate.Stock;
import org.accretegb.modules.hibernate.StockGeneration;
import org.accretegb.modules.hibernate.Taxonomy;
import org.accretegb.modules.hibernate.dao.ClassificationDAO;
import org.accretegb.modules.hibernate.dao.HarvestingGroupDAO;
import org.accretegb.modules.hibernate.dao.PassportDAO;
import org.accretegb.modules.hibernate.dao.StockDAO;
import org.accretegb.modules.hibernate.dao.TaxonomyDAO;
import org.accretegb.modules.hibernate.dao.TokenRelationDAO;
import org.accretegb.modules.main.LoginScreen;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.util.LoggerUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

public class StockAnnotationPanel extends TabComponentPanel {

    private static final long serialVersionUID = 1L;
	private JProgressBar progressBar;
    private TableToolBoxPanel stockTablePanel;
	private TableToolBoxPanel classificationTablePanel;
    private TableToolBoxPanel taxonomyTablePanel;
    private CheckBoxIndexColumnTable classificationTable;
    private CheckBoxIndexColumnTable taxonomyTable;
    private int taxonomySelectedRow = -1;
    private int classificationSelectedRow = -1;
    private JTextField pedigreeField;
    private JTextField accessionField; 
    private JTextField generarionField; 
    
    public void initializePanel() {
        MigLayout layout = new MigLayout("insets 10, gap 5");
        setLayout(layout);
        add(addStockPanelToolButtons(),"growx, spanx, pushx, wrap");
        add(getstockTablePanel(), "growx, spanx, pushx, h 60%, wrap");
        add(new JLabel(""), "align label");
        taxonomyTable = taxonomyTablePanel.getTable();
        classificationTable = classificationTablePanel.getTable();
        taxonomyTable = taxonomyTablePanel.getTable();
        populateClassificationCodeTable();
        populateTaxonomyTable();
        JPanel j = new JPanel(new MigLayout("insets 0 0 0 0, gapx 0"));
        j.add(classificationTablePanel, "growx, h 100%, w 50%");
        j.add(taxonomyTablePanel, " growx, h 100%, w 50%, wrap");
        
        
        progressBar.setVisible(false);
        j.add(progressBar, "w 100%, spanx");
        add(j, "growx, spanx, h 50%, w 100%, wrap");
        stockTablePanel.getRefreshButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executeTask();
            }
        });
        stockTablePanel.setActionListenerDeleteButton();

    }


    public void initialize() {
        initializePanel();
    }
    
    private JPanel addStockPanelToolButtons(){
    	JPanel panel = new JPanel();
    	panel.setLayout(new MigLayout("insets 0 0 0 0, gapx 0"));
        List<Integer> editableColumns = new ArrayList<Integer>();
        editableColumns.add(stockTablePanel.getTable().getIndexOf(ColumnConstants.ACCESSION));
        editableColumns.add(stockTablePanel.getTable().getIndexOf(ColumnConstants.PEDIGREE));
        editableColumns.add(stockTablePanel.getTable().getIndexOf(ColumnConstants.GENERATION));
    	stockTablePanel.getTable().setEditableColumns(editableColumns);;
    	JButton importStockList = new JButton("Import Stocks By Search");
    	JButton importHarvestGroup = new JButton("Import Stocks From Harvest Group");
    	JPanel subPanel1 = new JPanel();
    	subPanel1.setLayout(new MigLayout("insets 0 0 0 0, gapx 0"));
    	subPanel1.add(importStockList, "gapRight 10");
    	subPanel1.add(importHarvestGroup, "gapRight 10, wrap");
    	panel.add(subPanel1,"gapLeft 10, spanx,wrap");
    	importStockList.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				StocksInfoPanel stockInfoPanel = CreateStocksInfoPanel.createStockInfoPanel("stock annotation popup");
				stockInfoPanel.setSize(new Dimension(500,400));
				int option = JOptionPane.showConfirmDialog(
						null,
						stockInfoPanel,
						"Search Stock Packets", JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE);
				ArrayList<Integer> stockIds = new ArrayList<Integer>();
				
				if (option == JOptionPane.OK_OPTION) {
					DefaultTableModel model = (DefaultTableModel) stockTablePanel.getTable().getModel();
					CheckBoxIndexColumnTable stocksOutputTable = stockInfoPanel.getSaveTablePanel().getTable();
					for(int row = 0; row < stocksOutputTable.getRowCount(); ++row ){
						int stockId = (Integer) stocksOutputTable.getValueAt(row, stocksOutputTable.getIndexOf(ColumnConstants.STOCK_ID));
						stockIds.add(stockId);
					}
					List<Stock> stocks = StockDAO.getInstance().getStocksByIds(stockIds);
					for(Stock stock: stocks){
						Object[] rowData = new Object[stockTablePanel.getTable().getColumnCount()];
			            rowData[0] = new Boolean(false);
			            Passport passport = stock.getPassport();
			            StockGeneration generation = stock.getStockGeneration();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.STOCK_NAME)] = stock.getStockName();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.STOCK_ID)] = stock.getStockId();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.GENERATION)] = generation == null ? null : generation.getGeneration();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.PASSPORT_ID)] = passport == null? null : passport.getPassportId();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.ACCESSION)] = passport == null? null :passport.getAccession_name();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.PEDIGREE)] = passport == null? null :passport.getPedigree();		            
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.CLASSIFICATION_CODE)] = 
			            		passport.getClassification() == null ? null :passport.getClassification().getClassificationCode();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.CLASSIFICATION_ID)] = 
			            		passport.getClassification() == null ? null :passport.getClassification().getClassificationId();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.POPULATION)] = 
			            		passport.getTaxonomy() == null ? null : passport.getTaxonomy().getPopulation();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.TAXONOMY_ID)] = 
			            		passport.getTaxonomy() == null ? null : passport.getTaxonomy().getTaxonomyId();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.MODIFIED)] = new Boolean(false);
			            model.addRow(rowData);
					}
					
				}
				
			}
    		
    	});
    	
    	importHarvestGroup.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				ArrayList<PMProject> projects = TokenRelationDAO.getInstance().findProjectObjects(LoginScreen.loginUserId);
				HashMap<String, Harvesting> name_tap = new HashMap<String, Harvesting>();
				for(PMProject project : projects){
					List<HarvestingGroup> HarvestingGroups = HarvestingGroupDAO.getInstance().findByProjectid(project.getProjectId());
					for(HarvestingGroup hg : HarvestingGroups){
						Harvesting harvestingPanel = (Harvesting) getContext().getBean("Harvesting - " + 
								project.getProjectId() + hg.getHarvestingGroupName());
						name_tap.put(project.getProjectName() + "-" + hg.getHarvestingGroupName(), harvestingPanel);
					}
					
				}
				JPanel popup = new JPanel(new MigLayout("insets 0, gap 5"));
				JScrollPane jsp = new JScrollPane(popup){
		            @Override
		            public Dimension getPreferredSize() {
		                return new Dimension(250, 320);
		            }
		        };
		        ButtonGroup group = new ButtonGroup();
				for(String name : name_tap.keySet()) {
					JRadioButton button = new JRadioButton(name);
					group.add(button);
					popup.add(button, "wrap");
					
					button.setSelected(true);
				}
				String selected_group = null;
		        int option = JOptionPane.showConfirmDialog(null, jsp, "Select Harvesting Group Name: ", JOptionPane.OK_CANCEL_OPTION); 
				if (option == JOptionPane.OK_OPTION){
					for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements();) {
						AbstractButton button = buttons.nextElement();
						if (button.isSelected()) {
							selected_group = button.getText();	
							
							
						}
					}	
				}
				if(selected_group != null){
					Harvesting harvestingPanel = name_tap.get(selected_group);
					DefaultTableModel model = (DefaultTableModel) stockTablePanel.getTable().getModel();
					ArrayList<String> stocknames = harvestingPanel.getStickerGenerator().getCreatedStocks();
					List<Stock> stocks = StockDAO.getInstance().getStocksByNames(stocknames);
					for(Stock stock : stocks){
						Object[] rowData = new Object[stockTablePanel.getTable().getColumnCount()];
			            rowData[0] = new Boolean(false);
			            Passport passport = stock.getPassport();
			            StockGeneration generation = stock.getStockGeneration();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.STOCK_NAME)] = stock.getStockName();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.STOCK_ID)] = stock.getStockId();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.GENERATION)] = generation == null ? null : generation.getGeneration();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.PASSPORT_ID)] = passport == null? null : passport.getPassportId();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.ACCESSION)] = passport == null? null :passport.getAccession_name();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.PEDIGREE)] = passport == null? null :passport.getPedigree();
			            
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.CLASSIFICATION_CODE)] = 
			            		passport.getClassification() == null ? null :passport.getClassification().getClassificationCode();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.CLASSIFICATION_ID)] = 
			            		passport.getClassification() == null ? null :passport.getClassification().getClassificationId();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.POPULATION)] = 
			            		passport.getTaxonomy() == null ? null : passport.getTaxonomy().getPopulation();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.TAXONOMY_ID)] = 
			            		passport.getTaxonomy() == null ? null : passport.getTaxonomy().getTaxonomyId();
			            rowData[stockTablePanel.getTable().getIndexOf(ColumnConstants.MODIFIED)] = new Boolean(false);
			            model.addRow(rowData);
					}
				}
				
			}
    		
    	});
    	getstockTablePanel().getTable().addFocusListener(new FocusAdapter() {
    	    public void focusLost(FocusEvent e) {
    	        int row = getstockTablePanel().getTable().getSelectionModel().getAnchorSelectionIndex();
    	        getstockTablePanel().getTable().setValueAt(true, row, getstockTablePanel().getTable().getIndexOf(ColumnConstants.MODIFIED));
    	    }
    	});
    	JPanel subPanel2 = new JPanel();
    	subPanel2.setLayout(new MigLayout("insets 0 0 0 0 , gapx 0"));
    	subPanel2.add(new JLabel("Pedigree: "));
    	this.pedigreeField = new JTextField(10);
    	subPanel2.add(pedigreeField);
    	JButton setPedigree = new JButton("set");
    	setPedigree.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				for(int row : getstockTablePanel().getTable().getSelectedRows()) {
					getstockTablePanel().getTable().setValueAt(pedigreeField.getText(), row, getstockTablePanel().getTable().getIndexOf(ColumnConstants.PEDIGREE));
					getstockTablePanel().getTable().setValueAt(true, row, getstockTablePanel().getTable().getIndexOf(ColumnConstants.MODIFIED));
				}		
			}		
    	});    		
    	subPanel2.add(setPedigree, "gapRight 15");
    	
    	subPanel2.add(new JLabel("Accession: "));
    	this.accessionField = new JTextField(10);
    	subPanel2.add(accessionField);
    	JButton setAccession = new JButton("set");
    	setAccession.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				for(int row : getstockTablePanel().getTable().getSelectedRows()) {
					getstockTablePanel().getTable().setValueAt(accessionField.getText(), row, getstockTablePanel().getTable().getIndexOf(ColumnConstants.ACCESSION));
					getstockTablePanel().getTable().setValueAt(true, row, getstockTablePanel().getTable().getIndexOf(ColumnConstants.MODIFIED));

				}		
			}		
    	});
    	subPanel2.add(setAccession,"gapRight 15");
    	
    	subPanel2.add(new JLabel("Generarion: "));
    	this.generarionField = new JTextField(10);
    	subPanel2.add(generarionField);
    	JButton setGeneration = new JButton("set");
    	setGeneration.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				for(int row : getstockTablePanel().getTable().getSelectedRows()) {
					getstockTablePanel().getTable().setValueAt(generarionField.getText(), row, getstockTablePanel().getTable().getIndexOf(ColumnConstants.GENERATION));
					getstockTablePanel().getTable().setValueAt(true, row, getstockTablePanel().getTable().getIndexOf(ColumnConstants.MODIFIED));
				}		
			}		
    	});
    	subPanel2.add(setGeneration);
    	
    	panel.add(subPanel2, "gapLeft 10,spanx");
    	return panel;
    	
    }  
    
    /**
     * read and write existing rows of classification_code table in database into classification_code table in interface
     */
    private void populateClassificationCodeTable() {      
       List<Classification> results = ClassificationDAO.getInstance().findAll();
        classificationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        classificationTable.setSingleSelection(true);
        if (results.size() > 0) {
            Utils.removeAllRowsFromTable((DefaultTableModel) classificationTable.getModel());
        }
        for (Classification result : results) {
            Object rowData[] = new Object[classificationTable.getColumnCount()];
            rowData[0] = new Boolean(false);
            rowData[classificationTable.getColumnModel().getColumnIndex("Classification id")] = result.getClassificationId();
            rowData[classificationTable.getColumnModel().getColumnIndex(ColumnConstants.CLASSIFICATION_CODE)] = result.getClassificationCode();
            rowData[classificationTable.getColumnModel().getColumnIndex("Classification Type")] = result.getClassificationType();
            DefaultTableModel model = (DefaultTableModel) classificationTable.getModel();
            model.addRow(rowData);
            classificationTable.setModel(model);
        }
        classificationTable.hideColumn("Classification id");     
        addClassificationCodeListeners();
    }
    
    /**
     * read and write existing rows of taxonomy table in database into taxonomy table in interface
     */
    private void populateTaxonomyTable() {
       
        List<Taxonomy> results = TaxonomyDAO.getInstance().findAll();
        taxonomyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taxonomyTable.setSingleSelection(true);
        if (results.size() > 0) {
            Utils.removeAllRowsFromTable((DefaultTableModel) taxonomyTable.getModel());
        }
        for (Taxonomy result : results) {
            Object[] rowData = new Object[10];
            rowData[0] = new Boolean(false);
            rowData[1] = result.getTaxonomyId().toString();
            rowData[2] = result.getGenus() == null ? "NULL" : result.getGenus();
            rowData[3] = result.getSpecies() == null ? "NULL" : result.getSpecies();
            rowData[4] = result.getSubspecies() == null ? "NULL" : result.getSubspecies();
            rowData[5] = result.getSubtaxa() == null ? "NULL" : result.getSubtaxa();
            rowData[6] = result.getRace() == null ? "NULL" : result.getRace();
            rowData[7] = result.getCommonName() == null ? "NULL" : result.getCommonName();
            rowData[8] = result.getPopulation() == null ? "NULL" : result.getPopulation();
            rowData[9] = result.getGto() == null ? "NULL" : result.getGto();
            DefaultTableModel model = (DefaultTableModel) taxonomyTable.getModel();
            model.addRow(rowData);
            taxonomyTable.setModel(model);
        }
        taxonomyTable.hideColumn("Taxonomy id");
        addTaxonomyListeners();
        
        
    }
    
    /**
     * actions after click upload, clear buttons associated with Classification Code table, and actions after click inside ClassificationCode
     * table
     */
    private void addClassificationCodeListeners() {

        classificationTablePanel.getEditButton().setEnabled(false);
        classificationTablePanel.getDeleteButton().setEnabled(false);
        classificationTablePanel.getUploadButton().setEnabled(false);
        classificationTablePanel.getClearButton().setEnabled(false);

        addClassificationCodeAddButtonListener();
        addClassificationCodeEditButtonListener();
        addClassificationCodeDeleteButtonListener();

        classificationTablePanel.getUploadButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                int validRow = 0;
                int selectedRows[] = stockTablePanel.getTable().getSelectedRows();
                //System.out.print("\nselected row" + stockTablePanel.getTable().getSelectedRowCount());
                Integer sampVal = (Integer) ((DefaultTableModel) classificationTable.getModel()).getValueAt(
                        classificationSelectedRow, classificationTable.getColumn("Classification id").getModelIndex());
                String germplasm = (String) ((DefaultTableModel) classificationTable.getModel()).getValueAt(
                        classificationSelectedRow, classificationTable.getColumn("Classification Code").getModelIndex());
                for (int rowCounter = 0; rowCounter < stockTablePanel.getTable().getSelectedRowCount(); rowCounter++) {
                    // only selected rows can be uploaded with data.
                    if (!StringUtils.isEmpty(String.valueOf(stockTablePanel.getTable().getValueAt(selectedRows[rowCounter],
                            stockTablePanel.getTable().getColumn(ColumnConstants.ACCESSION).getModelIndex())))
                            || !(StringUtils.isEmpty(String.valueOf(stockTablePanel.getTable().getValueAt(
                                    selectedRows[rowCounter], stockTablePanel.getTable().getColumn(ColumnConstants.PEDIGREE).getModelIndex()))))) {
                        stockTablePanel.getTable().setValueAt(sampVal, selectedRows[rowCounter],
                                stockTablePanel.getTable().getIndexOf(ColumnConstants.CLASSIFICATION_ID));
                        stockTablePanel.getTable().setValueAt(germplasm, selectedRows[rowCounter],
                        		stockTablePanel.getTable().getIndexOf(ColumnConstants.CLASSIFICATION_CODE));
                        validRow++;
                    }
                }
                ((DefaultTableModel) classificationTable.getModel()).setValueAt(false, classificationSelectedRow, 0);
                if (validRow != stockTablePanel.getTable().getSelectedRowCount()) {
                    // AccreteGBLogger.logger.log(Level.INFO,
                    // "Either Acceesion or Pedigree is mandatory for uploading");
                    JOptionPane.showConfirmDialog(StockAnnotationPanel.this,
                            "Either Acceesion or Pedigree is mandatory for uploading", "Warning!",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
                }
                clearClassificationCodeSelection();
            }
        });
        classificationTablePanel.getClearButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                for (int rowCounter = 0; rowCounter < stockTablePanel.getTable().getRowCount(); rowCounter++) {
                    if (!StringUtils.isEmpty(String.valueOf(stockTablePanel.getTable().getValueAt(rowCounter, stockTablePanel.getTable()
                            .getColumn(ColumnConstants.ACCESSION).getModelIndex())))
                            || !(StringUtils.isEmpty(String.valueOf(stockTablePanel.getTable().getValueAt(rowCounter,
                                    stockTablePanel.getTable().getColumn(ColumnConstants.PEDIGREE).getModelIndex()))))) {
                        stockTablePanel.getTable().setValueAt("", rowCounter, stockTablePanel.getTable().getColumn("Classification id")
                                .getModelIndex());
                        stockTablePanel.getTable().setValueAt("", rowCounter, stockTablePanel.getTable().getColumn("Classification Code")
                                .getModelIndex());
                    }
                }
            }
        });
        classificationTable.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
            	int row = classificationTable.getSelectedRow();
            	classificationSelectedRow = row;
            	if(row != -1){           		 
            		 classificationTablePanel.getEditButton().setEnabled(true);
                     classificationTablePanel.getDeleteButton().setEnabled(true);
                     if (stockTablePanel.getTable().getSelectedRowCount() > 0)
                     {
                    	 classificationTablePanel.getUploadButton().setEnabled(true);
                     }
                     
            	}else{
            		 classificationTablePanel.getEditButton().setEnabled(false);
                     classificationTablePanel.getDeleteButton().setEnabled(false);
                     classificationTablePanel.getUploadButton().setEnabled(false);
            	}
            }
        });
    }

    /**
     * actions after click delete button associated with Classification Code table
     */
    private void addClassificationCodeDeleteButtonListener() {
        classificationTablePanel.getDeleteButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                int selrow = classificationTable.getSelectedRow();
                int selcolumn = 0;

                String columnName = classificationTable.getColumnName(selcolumn + 1);
                String sampval =(String)classificationTable.getValueAt(selrow, classificationTable.getColumn(ColumnConstants.CLASSIFICATION_CODE).getModelIndex());

                try {
                   
                    List<Passport> parentPassports = PassportDAO.getInstance().findByClassificationCode(sampval);
                    if (parentPassports.size() > 0) {
                        if (LoggerUtils.isLogEnabled())
                            LoggerUtils.log(Level.INFO, "Classification Code has passport associated with " + "it. " + "Cannot "
                                    + "delete");
                        JOptionPane.showConfirmDialog(StockAnnotationPanel.this,
                                        "Cannot delete classification_code associated with passports", "Error!",
                                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
                        classificationTable.setValueAt(false, selrow, 0);
                        classificationTable.clearSelection();
                    } else {
                    	ClassificationDAO.getInstance().delete(sampval);
                        ((DefaultTableModel) classificationTable.getModel()).removeRow(classificationTable.convertRowIndexToModel(selrow));
                        classificationTable.setForeground(Color.BLACK);
                        classificationSelectedRow = -1;
                    }
                } catch (HibernateException ex) {
                    if (LoggerUtils.isLogEnabled())
                        LoggerUtils.log(Level.INFO, ex.toString());
                }
                clearClassificationCodeSelection();
            }
        });
    }

    /**
     * actions after click edit button associated with Classification Code table
     */
    private void addClassificationCodeEditButtonListener() {
        classificationTablePanel.getEditButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                classificationTablePanel.getDeleteButton().setEnabled(true);
                classificationTablePanel.getEditButton().setEnabled(true);

                JPanel newSource = new JPanel(new MigLayout("insets 0, gapx 0"));

                String labelNames[] = { "<HTML>Classification Code<FONT COLOR = Red>*" + "</FONT></HTML>",
                        "<HTML>Classification Type<FONT COLOR = Red>*" + "</FONT></HTML>" };

                int selRow = classificationTable.getSelectedRow();
                String oldClassificationCode = (String) classificationTable.getValueAt(selRow, classificationTable.getColumn(ColumnConstants.CLASSIFICATION_CODE)
                        .getModelIndex());
                String oldGermplasm = (String) classificationTable.getValueAt(selRow,
                        classificationTable.getColumn("Classification Type").getModelIndex());

                String values[] = { oldClassificationCode, oldGermplasm };

                boolean validInput = false;
                JLabel labels[] = new JLabel[labelNames.length];
                final JTextField textBoxes[] = new JTextField[labelNames.length];

                for (int labelCounter = 0; labelCounter < labels.length; labelCounter++) {
                    labels[labelCounter] = new JLabel(labelNames[labelCounter]);
                    newSource.add(labels[labelCounter], "gapleft 10, push");
                    textBoxes[labelCounter] = new JTextField();
                    textBoxes[labelCounter].setPreferredSize(new Dimension(200, 0));
                    textBoxes[labelCounter].setText(values[labelCounter]);
                    newSource.add(textBoxes[labelCounter], "gapRight 10, wrap");
                }

                do {
                    for (int textBoxCounter = 0; textBoxCounter < labelNames.length; textBoxCounter++)
                        textBoxes[textBoxCounter].setText(values[textBoxCounter]);

                    int option = JOptionPane.showConfirmDialog(StockAnnotationPanel.this, newSource, "Enter New Classification Code Information ",
                            JOptionPane.DEFAULT_OPTION);

                    validInput = true;

                    if (option == JOptionPane.OK_OPTION) {

                        for (int valuesCounter = 0; valuesCounter < labelNames.length; valuesCounter++) {
                            values[valuesCounter] = textBoxes[valuesCounter].getText();
                            if (labelNames[valuesCounter].indexOf('*') != -1
                                    && textBoxes[valuesCounter].getText().equals("")) {
                                if (LoggerUtils.isLogEnabled())
                                    LoggerUtils.log(Level.INFO, labelNames[valuesCounter] + " " + "is " + "mandatory");
                                validInput = false;
                            }
                        }
                        if (validInput) {
                            Object newRow[] = updateClassificationCodeTable(values, oldClassificationCode);
                            ((DefaultTableModel) classificationTable.getModel()).insertRow(classificationTable.getRowCount(),
                                    newRow);
                            for (int rowCounter = 0; rowCounter < classificationTable.getRowCount(); rowCounter++)
                                if (((String) classificationTable.getValueAt(rowCounter, classificationTable.getColumn(ColumnConstants.CLASSIFICATION_CODE)
                                        .getModelIndex())).equals(oldClassificationCode))
                                    ((DefaultTableModel) classificationTable.getModel()).removeRow(classificationTable.convertRowIndexToModel(rowCounter));
                        }
                    }

                    if (!validInput)
                        JOptionPane.showConfirmDialog(StockAnnotationPanel.this,
                                        "<HTML><FONT COLOR = Red>*</FONT> marked fields are mandatory.</HTML>",
                                        "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);

                } while (!validInput);
                clearClassificationCodeSelection();
            }

        });
    }

    /**
     * Modify existing row in classification table
     * 
     * @param values
     *            old set of values
     * @param oldClassificationCode
     *            old classification_code code used as unique identifier
     * @return rowData for updated row
     */
    protected Object[] updateClassificationCodeTable(String[] values, String oldClassificationCode) {
    	Object newRow[] = new Object[values.length + 2];							
		newRow[0] = new Boolean(false);	
		Object classificationId = classificationTable.getModel().getValueAt(classificationSelectedRow, 
				classificationTable.getColumn("Classification id").getModelIndex());
		newRow[1] = classificationId;
		for(int columnIndex = 2; columnIndex < newRow.length; columnIndex++)								
		{
			newRow[columnIndex] = values[columnIndex-2];		
		}
		ClassificationDAO.getInstance().update(values, oldClassificationCode);
       
        return newRow;
    }

    /**
     * actions after click add button associated with Classification Code table
     */
    private void addClassificationCodeAddButtonListener() {
        classificationTablePanel.getAddButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                classificationTablePanel.getDeleteButton().setEnabled(false);
                classificationTablePanel.getEditButton().setEnabled(false);

                JPanel newSource = new JPanel(new MigLayout("insets 0, gapx 0"));

                String labelNames[] = { "<HTML>Classification Code<FONT COLOR = Red>*" + "</FONT></HTML>",
                        "<HTML>Classification Type<FONT COLOR = Red>*" + "</FONT></HTML>" };
                String values[] = { "", "" };

                boolean validInput = false;
                JLabel labels[] = new JLabel[labelNames.length];
                final JTextField textBoxes[] = new JTextField[labelNames.length];

                for (int labelCounter = 0; labelCounter < labels.length; labelCounter++) {
                    labels[labelCounter] = new JLabel(labelNames[labelCounter]);
                    newSource.add(labels[labelCounter], "gapleft 10, push");
                    textBoxes[labelCounter] = new JTextField();
                    textBoxes[labelCounter].setPreferredSize(new Dimension(200, 0));
                    textBoxes[labelCounter].setText(values[labelCounter]);
                    newSource.add(textBoxes[labelCounter], "gapRight 10, wrap");
                }

                do {
                    for (int textBoxCounter = 0; textBoxCounter < labelNames.length; textBoxCounter++)
                        textBoxes[textBoxCounter].setText(values[textBoxCounter]);

                    int option = JOptionPane.showConfirmDialog(StockAnnotationPanel.this,
                    		newSource, "Enter New Classification Code Information ",
                            JOptionPane.DEFAULT_OPTION);

                    validInput = true;

                    if (option == JOptionPane.OK_OPTION) {

                        for (int valuesCounter = 0; valuesCounter < labelNames.length; valuesCounter++) {
                            values[valuesCounter] = textBoxes[valuesCounter].getText();
                            if (labelNames[valuesCounter].indexOf('*') != -1
                                    && textBoxes[valuesCounter].getText().equals("")) {
                                if (LoggerUtils.isLogEnabled())
                                    LoggerUtils.log(Level.INFO, labelNames[valuesCounter] + " " + "is " + "mandatory");

                                validInput = false;
                            }
                        }
                        // insert new row to database & insert one row into table of interface
                        if (validInput) {

                        	Object newRow[] = new Object[values.length + 2];								
							newRow[0] = new Boolean(false);	
							for(int columnCounter = 2; columnCounter < newRow.length; columnCounter++)								
								newRow[columnCounter] = values[columnCounter-2];
                          
                            try {                             
                                newRow[1] = ClassificationDAO.getInstance().insert(values).getClassificationId().toString();
                            } catch (HibernateException ex) {
                                if (LoggerUtils.isLogEnabled())
                                    LoggerUtils.log(Level.INFO, ex.toString());
                            } 
                            ((DefaultTableModel) classificationTable.getModel()).insertRow(classificationTable.getRowCount(),
                                    newRow);
                        }
                    }
                    if (!validInput)
                        JOptionPane.showConfirmDialog(StockAnnotationPanel.this,
                                        "<HTML><FONT COLOR = Red>*</FONT> marked fields are mandatory.</HTML>",
                                        "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);

                } while (!validInput);

                clearClassificationCodeSelection();
            }
        });
    }

    /**
     * clear selection in classification_code table; disable edit, delete, upload button
     */
    protected void clearClassificationCodeSelection() {
        classificationTable.clearSelection();
        classificationTable.setForeground(Color.BLACK);
        if (classificationSelectedRow != -1)
            ((DefaultTableModel) classificationTable.getModel()).setValueAt(false, classificationSelectedRow, 0);
        classificationTablePanel.getEditButton().setEnabled(false);
        classificationTablePanel.getDeleteButton().setEnabled(false);
        classificationTablePanel.getUploadButton().setEnabled(false);
        classificationSelectedRow = -1;
        classificationTable.repaint();
    }

    /**
     * actions after click upload, clear buttons associated with Taxonomy table, and actions after click taxonomy table
     */
    private void addTaxonomyListeners() {

        // no edit, delete, upload, clear functions allowed
        taxonomyTablePanel.getEditButton().setEnabled(false);
        taxonomyTablePanel.getDeleteButton().setEnabled(false);
        taxonomyTablePanel.getUploadButton().setEnabled(false);
        taxonomyTablePanel.getClearButton().setEnabled(false);

        // add, edit, delete button listener
        addTaxonomyAddButtonListener();
        addTaxonomyEditButtonListener();
        addTaxonomyDeleteButtonListener();

        // actions after click upload button
        taxonomyTablePanel.getUploadButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                int validRow = 0;
                int selectedRows[] = stockTablePanel.getTable().getSelectedRows();
                String taxVal = (String) ((DefaultTableModel) taxonomyTable.getModel()).getValueAt(taxonomySelectedRow,
                        taxonomyTable.getColumn("Taxonomy id").getModelIndex());
                String value = ""
                        + ((String) ((DefaultTableModel) taxonomyTable.getModel()).getValueAt(taxonomySelectedRow,
                                taxonomyTable.getColumn("Genus").getModelIndex())).charAt(0)
                        + ((String) ((DefaultTableModel) taxonomyTable.getModel()).getValueAt(taxonomySelectedRow,
                                taxonomyTable.getColumn("Species").getModelIndex())).charAt(0)
                        + " ("
                        + ((String) ((DefaultTableModel) taxonomyTable.getModel()).getValueAt(taxonomySelectedRow,
                                taxonomyTable.getColumn("Race").getModelIndex()))
                        + ") "
                        + ((String) ((DefaultTableModel) taxonomyTable.getModel()).getValueAt(taxonomySelectedRow,
                                taxonomyTable.getColumn(ColumnConstants.POPULATION).getModelIndex()));

                for (int rowCounter = 0; rowCounter < stockTablePanel.getTable().getSelectedRowCount(); rowCounter++) {
                    // only selected rows can be uploaded data.
                    if (!StringUtils.isEmpty(String.valueOf(stockTablePanel.getTable().getValueAt(selectedRows[rowCounter],
                            stockTablePanel.getTable().getColumn(ColumnConstants.ACCESSION).getModelIndex())))
                            || !(StringUtils.isEmpty(String.valueOf(stockTablePanel.getTable().getValueAt(
                                    selectedRows[rowCounter], stockTablePanel.getTable().getColumn(ColumnConstants.PEDIGREE).getModelIndex()))))) {
                        stockTablePanel.getTable().setValueAt(taxVal, selectedRows[rowCounter],
                                stockTablePanel.getTable().getColumn("Taxonomy id").getModelIndex());
                        stockTablePanel.getTable().setValueAt(value, selectedRows[rowCounter],
                                stockTablePanel.getTable().getColumn("Population").getModelIndex());
                        validRow++;
                    }
                }

                if (validRow != stockTablePanel.getTable().getSelectedRowCount()) {
                    // AccreteGBLogger.logger.log(Level.INFO,
                    // "Either Acceesion or Pedigree is mandatory for uploading");
                    JOptionPane.showConfirmDialog(StockAnnotationPanel.this,
                            "Either Acceesion or Pedigree is mandatory for uploading", "Warning!",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
                }
                ((DefaultTableModel) taxonomyTable.getModel()).setValueAt(false, taxonomySelectedRow, 0);
                clearTaxonomySelection();
            }
        });

        // actions after click clear button
        taxonomyTablePanel.getClearButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                for (int rowCounter = 0; rowCounter < stockTablePanel.getTable().getRowCount(); rowCounter++) {
                    if ((Boolean) stockTablePanel.getTable().getValueAt(rowCounter, 0) == true) {
                        stockTablePanel.getTable().setValueAt("", rowCounter, stockTablePanel.getTable().getColumn("Taxonomy id")
                                .getModelIndex());
                        stockTablePanel.getTable().setValueAt("", rowCounter, stockTablePanel.getTable().getColumn("Population")
                                .getModelIndex());
                    }
                }
            }
        });

        // actions to do after click taxonomy table
        taxonomyTable.addMouseListener(new MouseAdapter() {
        	public void mouseReleased(MouseEvent e) {
	        	int row = taxonomyTable.getSelectedRow();
	        	 taxonomySelectedRow = row;
	        	if(row != -1){	        		
	        		 taxonomyTablePanel.getEditButton().setEnabled(true);
	        		 taxonomyTablePanel.getDeleteButton().setEnabled(true);
	                 if (stockTablePanel.getTable().getRowCount() > 0)
	                 {
	                	 taxonomyTablePanel.getUploadButton().setEnabled(true);
	                 }
	                 
	        	}else{
	        		 taxonomyTablePanel.getEditButton().setEnabled(false);
	        		 taxonomyTablePanel.getDeleteButton().setEnabled(false);
	        		 taxonomyTablePanel.getUploadButton().setEnabled(false);
	        	}
        	}
        });
    }

    /**
     * actions after delete button associated with Taxonomy table
     */
    private void addTaxonomyDeleteButtonListener() {

        taxonomyTablePanel.getDeleteButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int selrow = taxonomyTable.getSelectedRow();
                int selcolumn = 0;

                // String columnName = taxonomyTable.getColumnName(selcolumn + 1);
                String taxval = (String) taxonomyTable.getModel().getValueAt(taxonomySelectedRow,
                        taxonomyTable.getColumn("Taxonomy id").getModelIndex());

                // actions on database
               

                try {
                    // can't delete if a passport is associate with this taxonomy id
                    List<Passport> parentPassports =PassportDAO.getInstance().findByTaxonomy(Integer.parseInt(taxval));
                    if (parentPassports.size() > 0) {
                        if (LoggerUtils.isLogEnabled()) {
                            LoggerUtils.log(Level.INFO, "Taxonomy has passport associated " + "with it. Cannot delete");
                        }
                        JOptionPane
                                .showConfirmDialog(StockAnnotationPanel.this,
                                        "Cannot delete taxonomy associated with passports", "Error!",
                                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
                        taxonomyTable.setValueAt(false, selrow, 0);
                        taxonomyTable.clearSelection();
                    }
                    // otherwise delete record from database and delete row from table
                    else {
                       TaxonomyDAO.getInstance().delete(Integer.parseInt(taxval));
                        for (int i = 0; i < ((DefaultTableModel) taxonomyTable.getModel()).getRowCount(); i++)
                            if (i == taxonomySelectedRow) {
                                ((DefaultTableModel) taxonomyTable.getModel()).removeRow(taxonomyTable.convertRowIndexToModel(i));
                                break;
                            }
                        // no selected row after delete
                        taxonomySelectedRow = -1;
                        taxonomyTable.setForeground(Color.BLACK);
                    }
                } catch (HibernateException ex) {
                    if (LoggerUtils.isLogEnabled()) {
                        LoggerUtils.log(Level.INFO, ex.toString());
                    }
                } 
                clearTaxonomySelection();
            }
        });
    }

    /**
     * actions after edit button associated with Taxonomy table
     */
    private void addTaxonomyEditButtonListener() {
        taxonomyTablePanel.getEditButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                taxonomyTablePanel.getDeleteButton().setEnabled(false);
                taxonomyTablePanel.getEditButton().setEnabled(false);

                // create a new panel for edit interface
                JPanel newSource = new JPanel(new MigLayout("insets 0, gapx 0"));

                String labelNames[] = { "<HTML>Genus<FONT COLOR = Red>*" + "</FONT></HTML>",
                        "<HTML>Species Type<FONT COLOR = Red>*" + "</FONT></HTML>", "Subspecies", "Subtaxa", "Race",
                        "Common Name", ColumnConstants.POPULATION, "Gto" };

                // get current selected row
                int selRow = taxonomyTable.getSelectedRow();
                String values[] = {
                        taxonomyTable.getValueAt(selRow, 2).toString(),
                        taxonomyTable.getValueAt(selRow, 3).toString(),
                        taxonomyTable.getValueAt(selRow, 4) == null ? "" : taxonomyTable.getValueAt(selRow, 4)
                                .toString(),
                        taxonomyTable.getValueAt(selRow, 5) == null ? "" : taxonomyTable.getValueAt(selRow, 5)
                                .toString(),
                        taxonomyTable.getValueAt(selRow, 6) == null ? "" : taxonomyTable.getValueAt(selRow, 6)
                                .toString(),
                        taxonomyTable.getValueAt(selRow, 7) == null ? "" : taxonomyTable.getValueAt(selRow, 7)
                                .toString(),
                        taxonomyTable.getValueAt(selRow, 8) == null ? "" : taxonomyTable.getValueAt(selRow, 8)
                                .toString(),
                        taxonomyTable.getValueAt(selRow, 9) == null ? "" : taxonomyTable.getValueAt(selRow, 9)
                                .toString() };

                // check if input is valid
                boolean validInput = false;

                // create new components for edit interface
                JLabel labels[] = new JLabel[labelNames.length];
                final JTextField textBoxes[] = new JTextField[labelNames.length];

                // add components to new panel, write data from table to text field
                for (int columnIndex = 0; columnIndex < labels.length; columnIndex++) {
                    labels[columnIndex] = new JLabel(labelNames[columnIndex]);
                    newSource.add(labels[columnIndex], "gapleft 10, push");

                    textBoxes[columnIndex] = new JTextField();
                    textBoxes[columnIndex].setPreferredSize(new Dimension(200, 0));
                    textBoxes[columnIndex].setText(values[columnIndex]);
                    newSource.add(textBoxes[columnIndex], "gapRight 10, wrap");
                }

                do {
                    // after editing, reset text for textfield
                    for (int textBoxCounter = 0; textBoxCounter < labelNames.length; textBoxCounter++)
                        textBoxes[textBoxCounter].setText(values[textBoxCounter]);

                    // showConfirmDialog(Component parentComponent, Object message, String title, int optionType)
                    int option = JOptionPane.showConfirmDialog(StockAnnotationPanel.this,
                    		newSource, "Enter New Taxonomy Information ",
                            JOptionPane.DEFAULT_OPTION);
                    validInput = true;

                    if (option == JOptionPane.OK_OPTION) {

                        for (int valueCounter = 0; valueCounter < labelNames.length; valueCounter++) {
                            values[valueCounter] = textBoxes[valueCounter].getText();
                            if (labelNames[valueCounter].indexOf('*') != -1
                                    && textBoxes[valueCounter].getText().equals("")) {
                                if (LoggerUtils.isLogEnabled())
                                    LoggerUtils.log(Level.INFO, labelNames[valueCounter] + " is mandatory");
                                validInput = false; // input is invalid
                            }
                        }
                        // if input is valid
                        if (validInput) {
                            // update database
                            Object newRow[] = updateTaxonomyTable(values);
                            // update table
                            ((DefaultTableModel) taxonomyTable.getModel()).insertRow(taxonomyTable.getRowCount(),
                                    newRow);
                        }
                    }
                    // if input is valid
                    if (!validInput)
                        JOptionPane
                                .showConfirmDialog(StockAnnotationPanel.this,
                                        "<HTML><FONT COLOR = Red>*</FONT> marked fields are mandatory.</HTML>",
                                        "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);

                } while (!validInput);
                clearTaxonomySelection();
            }
        });
    }

    /**
     * Modify existing row in database
     * 
     * @param values
     *            new values the existing row
     * @return rowData for table of interface
     */
    protected Object[] updateTaxonomyTable(String[] values) {
        Object newRow[] = new Object[values.length + 2];
        newRow[0] = new Boolean(false);
        for (int columnIndex = 2; columnIndex < newRow.length; columnIndex++)
            newRow[columnIndex] = values[columnIndex - 2];
        try {
       
            int taxonomyId = Integer.parseInt((String) taxonomyTable.getModel().getValueAt(taxonomySelectedRow,
                    taxonomyTable.getColumn("Taxonomy id").getModelIndex()));
            TaxonomyDAO.getInstance().insert( values[0], values[1]=="NULL" ? null : values[1], values[2]=="NULL" ? null : values[2],
            		values[3]=="NULL" ? null : values[3], values[4]=="NULL" ? null : values[4],
                    values[6]=="NULL" ? null : values[6], values[5]=="NULL" ? null : values[5],
                    values[7]=="NULL" ? null : values[7], taxonomyId);
            newRow[1] = Integer.toString(taxonomyId);
            ((DefaultTableModel) taxonomyTable.getModel()).removeRow(taxonomyTable.convertRowIndexToModel(taxonomySelectedRow));
        } catch (HibernateException ex) {
            if (LoggerUtils.isLogEnabled())
                LoggerUtils.log(Level.INFO, ex.toString());
        } 
        return newRow;
    }

    /**
     * actions after add button associated with Taxonomy table
     */
    private void addTaxonomyAddButtonListener() {

        taxonomyTablePanel.getAddButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                taxonomyTablePanel.getDeleteButton().setEnabled(false);
                taxonomyTablePanel.getEditButton().setEnabled(false);

                // interface for adding new record
                JPanel newSource = new JPanel(new MigLayout("insets 0, gapx 0"));

                String labelNames[] = { "<HTML>Genus<FONT COLOR = Red>*" + "</FONT></HTML>",
                        "<HTML>Species Type<FONT COLOR = Red>*" + "</FONT></HTML>", "Subspecies", "Subtaxa", "Race",
                        "Common Name", ColumnConstants.POPULATION, "Gto" };
                String values[] = { "", "", "", "", "", "", "", "" };

                boolean validInput = false;
                JLabel labels[] = new JLabel[labelNames.length];
                final JTextField textBoxes[] = new JTextField[labelNames.length];

                for (int labelIndex = 0; labelIndex < labels.length; labelIndex++) {
                    labels[labelIndex] = new JLabel(labelNames[labelIndex]);
                    newSource.add(labels[labelIndex], "gapleft 10, push");
                    textBoxes[labelIndex] = new JTextField();
                    textBoxes[labelIndex].setPreferredSize(new Dimension(200, 0));
                    textBoxes[labelIndex].setText(values[labelIndex]);
                    newSource.add(textBoxes[labelIndex], "gapRight 10, wrap");
                }

                do {
                    for (int columnIndex = 0; columnIndex < labelNames.length; columnIndex++)
                        textBoxes[columnIndex].setText(values[columnIndex]);

                    int option = JOptionPane.showConfirmDialog(StockAnnotationPanel.this,
                    		newSource, "Enter New Taxonomy Information ",
                            JOptionPane.DEFAULT_OPTION);
                    validInput = true;

                    if (option == JOptionPane.OK_OPTION) {

                        for (int valueCounter = 0; valueCounter < labelNames.length; valueCounter++) {
                            values[valueCounter] = String.valueOf(textBoxes[valueCounter].getText()).equalsIgnoreCase("") ? "NULL":textBoxes[valueCounter].getText();
                            if (labelNames[valueCounter].indexOf('*') != -1
                                    && textBoxes[valueCounter].getText().equals("")) {
                                if (LoggerUtils.isLogEnabled())
                                    LoggerUtils.log(Level.INFO, labelNames[valueCounter] + " " + "is " + "mandatory");

                                validInput = false;
                            }
                        }
                        if (validInput) {
                            // insert new record to database
                            Object newRow[] = insertTaxonomyValues(values);
                            // insert new row to table
                            ((DefaultTableModel) taxonomyTable.getModel()).insertRow(taxonomyTable.getRowCount(),
                                    newRow);
                        }
                    }

                    if (!validInput)
                        JOptionPane.showConfirmDialog(StockAnnotationPanel.this,
                                        "<HTML><FONT COLOR = Red>*</FONT> marked fields are mandatory.</HTML>",
                                        "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);

                } while (!validInput);
                clearTaxonomySelection();
            }
        });
    }

    /**
     * insert new row into database
     * 
     * @param values
     *            value of new row
     * @return rowData for table
     */
    protected Object[] insertTaxonomyValues(String[] values) {
        Object newRow[] = new Object[values.length + 2];
        newRow[0] = new Boolean(false);
        for (int columnIndex = 2; columnIndex < newRow.length; columnIndex++)
        {
        	newRow[columnIndex] = values[columnIndex - 2];
        }
        try {           
            Taxonomy taxonomy = TaxonomyDAO.getInstance().insert( values[0], values[1]=="NULL" ? null : values[1], values[2]=="NULL" ? null : values[2],
            		values[3]=="NULL" ? null : values[3], values[4]=="NULL" ? null : values[4],
                    values[6]=="NULL" ? null : values[6], values[5]=="NULL" ? null : values[5],
                    values[7]=="NULL" ? null : values[7], 0);
            newRow[1] = taxonomy.getTaxonomyId().toString();
        } catch (HibernateException ex) {
            if (LoggerUtils.isLogEnabled())
                LoggerUtils.log(Level.INFO, ex.toString());
        } 
        return newRow;
    }
   

    /**
     * clear selection of taxonomy table; disable edit, delete, and upload button
     */
    protected void clearTaxonomySelection() {
        taxonomyTable.clearSelection();
        taxonomyTable.setForeground(Color.BLACK);
        if (taxonomySelectedRow != -1)
            ((DefaultTableModel) taxonomyTable.getModel()).setValueAt(false, taxonomySelectedRow, 0);
        taxonomyTablePanel.getEditButton().setEnabled(false);
        taxonomyTablePanel.getDeleteButton().setEnabled(false);
        taxonomyTablePanel.getUploadButton().setEnabled(false);
        taxonomySelectedRow = -1;
        taxonomyTable.repaint();
    }
    
    public void executeTask() {
        new SyncPassport(this).execute();
    }
    
    public TableToolBoxPanel getStockTablePanel() {
		return stockTablePanel;
	}

	public void setStockTablePanel(TableToolBoxPanel stockTablePanel) {
		this.stockTablePanel = stockTablePanel;
	}

	public TableToolBoxPanel getClassificationTablePanel() {
		return classificationTablePanel;
	}

	public void setClassificationTablePanel(TableToolBoxPanel classificationTablePanel) {
		this.classificationTablePanel = classificationTablePanel;
	}

	public TableToolBoxPanel getTaxonomyTablePanel() {
		return taxonomyTablePanel;
	}

	public void setTaxonomyTablePanel(TableToolBoxPanel taxonomyTablePanel) {
		this.taxonomyTablePanel = taxonomyTablePanel;
	}

    public TableToolBoxPanel getstockTablePanel() {
        return stockTablePanel;
    }

    public void setstockTablePanel(TableToolBoxPanel stockTablePanel) {
        this.stockTablePanel = stockTablePanel;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

	
}