package org.accretegb.modules.germplasm.outsideseed;

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.MainLayout;
import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.CustomCalendar;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.hibernate.Source;
import org.accretegb.modules.hibernate.Classification;
import org.accretegb.modules.hibernate.Passport;
import org.accretegb.modules.hibernate.Taxonomy;
import org.accretegb.modules.hibernate.dao.ClassificationDAO;
import org.accretegb.modules.hibernate.dao.PassportDAO;
import org.accretegb.modules.hibernate.dao.TaxonomyDAO;
import org.accretegb.modules.tab.TabComponent;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.tab.TabManager;
import org.accretegb.modules.util.LoggerUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

public class StockSelectionPanel extends TabComponentPanel {

    private static final long serialVersionUID = 1L;

    private JButton downloadButton;
    private JLabel downloadTemplate;
    private JButton browseButton;
    private JTextField filePath;
    private JLabel uploadTemplate;
    private TableToolBoxPanel stockDetailsTablePanel;
    private TableToolBoxPanel classificationTablePanel;
    private TableToolBoxPanel taxonomyTablePanel;
    private CheckBoxIndexColumnTable stockDetailsTable;
    private CheckBoxIndexColumnTable classificationTable;
    private CheckBoxIndexColumnTable taxonomyTable;
    private CustomCalendar customCalendar;
    private int taxonomySelectedRow = -1;
    private int classificationSelectedRow = -1;
    private int stockDetailsTableRowID = 0;
    private Source source;

    private JLabel donorName = new JLabel();

    public JButton getDownloadButton() {
        return downloadButton;
    }

    public void setDownloadButton(JButton downloadButton) {
        this.downloadButton = downloadButton;
    }

    public JLabel getDownloadTemplate() {
        return downloadTemplate;
    }

    public void setDownloadTemplate(JLabel downloadTemplate) {
        this.downloadTemplate = downloadTemplate;
    }

    public JButton getBrowseButton() {
        return browseButton;
    }

    public void setBrowseButton(JButton browseButton) {
        this.browseButton = browseButton;
    }

    public JTextField getFilePath() {
        return filePath;
    }

    public void setFilePath(JTextField filePath) {
        this.filePath = filePath;
    }

    public JLabel getUploadTemplate() {
        return uploadTemplate;
    }

    public void setUploadTemplate(JLabel uploadTemplate) {
        this.uploadTemplate = uploadTemplate;
    }

    public TableToolBoxPanel getStockDetailsTablePanel() {
        return stockDetailsTablePanel;
    }

    public void setStockDetailsTablePanel(TableToolBoxPanel stockDetailsTablePanel) {
        this.stockDetailsTablePanel = stockDetailsTablePanel;
    }


	public void setClassificationTablePanel(TableToolBoxPanel classificationTablePanel) {
		this.classificationTablePanel = classificationTablePanel;
	}

	public CheckBoxIndexColumnTable getClassificationTable() {
		return classificationTable;
	}

	public void setClassificationTable(CheckBoxIndexColumnTable classificationTable) {
		this.classificationTable = classificationTable;
	}

	public void setTaxonomyTablePanel(TableToolBoxPanel taxonomyTablePanel) {
        this.taxonomyTablePanel = taxonomyTablePanel;
    }

    public TableToolBoxPanel getClassificationTablePanel() {
        return classificationTablePanel;
    }

    public TableToolBoxPanel getTaxonomyTablePanel() {
        return taxonomyTablePanel;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
        donorName.setText(source.getPersonName());
    }

    public CustomCalendar getCustomCalendar() {
        return customCalendar;
    }

    public void setCustomCalendar(CustomCalendar customCalendar) {
        this.customCalendar = customCalendar;
    }

    public void initialize() {
        setLayout(new MigLayout("insets 20, gap 0"));// set layout of base TabComponentPanel. Insets – Space between
                                                     // parent container edges and grid

        stockDetailsTable = stockDetailsTablePanel.getTable();// initialize table
        taxonomyTable = taxonomyTablePanel.getTable();
        classificationTable = classificationTablePanel.getTable();

        // (Gaps between columns/rows and/or components in a cell) push: the gap should be greedy and use any free space
        // available.
        // Pushing gaps between columns/rows will make the layout fill the whole space of the container, if any is
        // available.
        // add "prev" button, push left horizontally
        add(getPrevPanelButton(), "pushx");

        // add "next" button, wrap: jump to next row
        add(getNextPanelButton(), "wrap");

        // add import-panel defined in this file to base panel; Grow horizontally, span horizontally
        add(getImportExportPanel(), "gaptop 20, growx, spanx, wrap");

        // add stockDetailsTablePanel to base panel
        add(stockDetailsTablePanel, "gaptop 5, h 50%, grow, span, wrap");

        // new panel for combining ClassificationCodePanel and TaxonomyPanel
        JPanel subPanels = new JPanel(new MigLayout("insets 0, gapx 0"));
        subPanels.add(getClassificationTablePanel(), "gaptop 5, w MIN(303, 40%), h 100%, spany, growy, spanx, growx");
        subPanels.add(getTaxonomyTablePanel(), "gapleft 10, h 100%, spanx, growx, spany, growy, pushx");
        add(subPanels, "h 25%, grow, span, push, wrap");

        // add listeners to components of each panel
        addStockDetailsTableListeners();
        addClassificationCodeListeners();
        addTaxonomyListeners();
        addNavigationButtonListeners();

        // get info from db and populate to table
        populateClassificationCodeTable();
        populateTaxonomyTable();

        // empty table
        initilizeStockDetailsTable();

    }


    /**
     * actions after click "next" and "prev" button
     */
    private void addNavigationButtonListeners() {
        this.getNextPanelButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // // doesn't call parent class function directly because something else needs to be added
                MainLayout mainLayout = (MainLayout) getContext().getBean("mainLayoutBean");
                TabManager tabManager = mainLayout.getTabManager();
                TabComponent tabComponent = tabManager.getTabComponent(getTabComponentPanel());
                tabComponent.setCurrentComponentPanel(getTabComponentPanel());
                TabComponentPanel nextTabComponentPanel = tabComponent.getNextComponentPanel();

                // "initialize" next page
                ((StockVerificationPanel) nextTabComponentPanel).setSource(getSource());
                ((StockVerificationPanel) nextTabComponentPanel).populateStockVerificationTable(stockDetailsTable,
                        customCalendar.getCalDate());

                tabManager.setReplaceComponentPanel(getTabComponentPanel(), nextTabComponentPanel);
            }
        });
        getPrevPanelButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                prevButtonActionPerformed(); // call parent class function.
            }
        });
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
                int selectedRows[] = stockDetailsTable.getSelectedRows();
                String taxVal = (String) ((DefaultTableModel) taxonomyTable.getModel()).getValueAt(taxonomySelectedRow,
                        taxonomyTable.getColumn("taxonomy id").getModelIndex());
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

                for (int rowCounter = 0; rowCounter < stockDetailsTable.getSelectedRowCount(); rowCounter++) {
                    // only selected rows can be uploaded data.
                    if (!StringUtils.isEmpty(String.valueOf(stockDetailsTable.getValueAt(selectedRows[rowCounter],
                            stockDetailsTable.getColumn(ColumnConstants.ACCESSION).getModelIndex())))
                            || !(StringUtils.isEmpty(String.valueOf(stockDetailsTable.getValueAt(
                                    selectedRows[rowCounter], stockDetailsTable.getColumn(ColumnConstants.PEDIGREE).getModelIndex()))))) {
                        stockDetailsTable.setValueAt(taxVal, selectedRows[rowCounter],
                                stockDetailsTable.getColumn("taxonomy id").getModelIndex());
                        stockDetailsTable.setValueAt(value, selectedRows[rowCounter],
                                stockDetailsTable.getColumn("Taxonomy").getModelIndex());
                        validRow++;
                    }
                }

                if (validRow != stockDetailsTable.getSelectedRowCount()) {
                    // AccreteGBLogger.logger.log(Level.INFO,
                    // "Either Acceesion or Pedigree is mandatory for uploading");
                    JOptionPane.showConfirmDialog(StockSelectionPanel.this,
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
                for (int rowCounter = 0; rowCounter < stockDetailsTable.getRowCount(); rowCounter++) {
                    if ((Boolean) stockDetailsTable.getValueAt(rowCounter, 0) == true) {
                        stockDetailsTable.setValueAt("", rowCounter, stockDetailsTable.getColumn("taxonomy id")
                                .getModelIndex());
                        stockDetailsTable.setValueAt("", rowCounter, stockDetailsTable.getColumn("Taxonomy")
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
	                 if (stockDetailsTable.getRowCount() > 0)
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
                        taxonomyTable.getColumn("taxonomy id").getModelIndex());

                // actions on database
               

                try {
                    // can't delete if a passport is associate with this taxonomy id
                    List<Passport> parentPassports =PassportDAO.getInstance().findByTaxonomy(Integer.parseInt(taxval));
                    if (parentPassports.size() > 0) {
                        if (LoggerUtils.isLogEnabled()) {
                            LoggerUtils.log(Level.INFO, "Taxonomy has passport associated " + "with it. Cannot delete");
                        }
                        JOptionPane
                                .showConfirmDialog(StockSelectionPanel.this,
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
                    int option = JOptionPane.showConfirmDialog(StockSelectionPanel.this,
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
                                .showConfirmDialog(StockSelectionPanel.this,
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
                    taxonomyTable.getColumn("taxonomy id").getModelIndex()));
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

                    int option = JOptionPane.showConfirmDialog(StockSelectionPanel.this,
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
                        JOptionPane.showConfirmDialog(StockSelectionPanel.this,
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
                int selectedRows[] = stockDetailsTable.getSelectedRows();
                //System.out.print("\nselected row" + stockDetailsTable.getSelectedRowCount());
                Integer sampVal = (Integer) ((DefaultTableModel) classificationTable.getModel()).getValueAt(
                        classificationSelectedRow, classificationTable.getColumn("classification id").getModelIndex());
                String germplasm = (String) ((DefaultTableModel) classificationTable.getModel()).getValueAt(
                        classificationSelectedRow, classificationTable.getColumn("Classification Type").getModelIndex());
                for (int rowCounter = 0; rowCounter < stockDetailsTable.getSelectedRowCount(); rowCounter++) {
                    // only selected rows can be uploaded data.
                    if (!StringUtils.isEmpty(String.valueOf(stockDetailsTable.getValueAt(selectedRows[rowCounter],
                            stockDetailsTable.getColumn(ColumnConstants.ACCESSION).getModelIndex())))
                            || !(StringUtils.isEmpty(String.valueOf(stockDetailsTable.getValueAt(
                                    selectedRows[rowCounter], stockDetailsTable.getColumn(ColumnConstants.PEDIGREE).getModelIndex()))))) {
                        stockDetailsTable.setValueAt(sampVal, selectedRows[rowCounter],
                                stockDetailsTable.getColumn("classification id").getModelIndex());
                        stockDetailsTable.setValueAt(germplasm, selectedRows[rowCounter],
                                stockDetailsTable.getColumn("Classification Type").getModelIndex());
                        validRow++;
                    }
                }
                ((DefaultTableModel) classificationTable.getModel()).setValueAt(false, classificationSelectedRow, 0);
                if (validRow != stockDetailsTable.getSelectedRowCount()) {
                    // AccreteGBLogger.logger.log(Level.INFO,
                    // "Either Acceesion or Pedigree is mandatory for uploading");
                    JOptionPane.showConfirmDialog(StockSelectionPanel.this,
                            "Either Acceesion or Pedigree is mandatory for uploading", "Warning!",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
                }
                clearClassificationCodeSelection();
            }
        });
        classificationTablePanel.getClearButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                for (int rowCounter = 0; rowCounter < stockDetailsTable.getRowCount(); rowCounter++) {
                    if (!StringUtils.isEmpty(String.valueOf(stockDetailsTable.getValueAt(rowCounter, stockDetailsTable
                            .getColumn(ColumnConstants.ACCESSION).getModelIndex())))
                            || !(StringUtils.isEmpty(String.valueOf(stockDetailsTable.getValueAt(rowCounter,
                                    stockDetailsTable.getColumn(ColumnConstants.PEDIGREE).getModelIndex()))))) {
                        stockDetailsTable.setValueAt("", rowCounter, stockDetailsTable.getColumn("classification id")
                                .getModelIndex());
                        stockDetailsTable.setValueAt("", rowCounter, stockDetailsTable.getColumn("Classification Type")
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
                     if (stockDetailsTable.getSelectedRowCount() > 0)
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
                        JOptionPane.showConfirmDialog(StockSelectionPanel.this,
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

                    int option = JOptionPane.showConfirmDialog(StockSelectionPanel.this, newSource, "Enter New Classification Code Information ",
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
                        JOptionPane.showConfirmDialog(StockSelectionPanel.this,
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
		Object classificationId = classificationTable.getModel().getValueAt(classificationSelectedRow, classificationTable.getColumn("classification id").getModelIndex());
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

                    int option = JOptionPane.showConfirmDialog(StockSelectionPanel.this,
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
                        JOptionPane.showConfirmDialog(StockSelectionPanel.this,
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

    private void addStockDetailsTableListeners() {
        stockDetailsTablePanel.getDeleteButton().setEnabled(false);

        // hide rows
        stockDetailsTable.hideColumn("RowNum");
        stockDetailsTable.hideColumn("classification id");
        stockDetailsTable.hideColumn("taxonomy id");

        // selecting multiple rows with interval is allowed
        stockDetailsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // setValueAt(Object aValue, int rowIndex, int columnIndex)
        for (int rowCounter = 0; rowCounter < stockDetailsTable.getRowCount(); rowCounter++)
            stockDetailsTable.setValueAt(String.valueOf(stockDetailsTableRowID++), rowCounter, stockDetailsTable
                    .getColumn("RowNum").getModelIndex());

        // stop editing a cell in a JTable when the user clicks on any other component than the table itself
        stockDetailsTable.putClientProperty("terminateEditOnFocusLost", true);

        stockDetailsTable.getTableHeader().setReorderingAllowed(false);

        // actions after click "none" label
        stockDetailsTablePanel.getNone().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                setUploadClearButtons();
                stockDetailsTable.clearSelection();
            }

            public void mouseReleased(MouseEvent mouseEvent) {
                setUploadClearButtons();
            }
        });

        // actions after click "all" label
        stockDetailsTablePanel.getAll().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                setUploadClearButtons();
            }

            public void mouseReleased(MouseEvent mouseEvent) {
                setUploadClearButtons();
            }
        });

        // actions after click stockDetailsTable
        stockDetailsTable.addMouseListener(new MouseAdapter() {
            private int mousePressed;

            @Override
            public void mouseClicked(MouseEvent arg0) {

                mousePressed = stockDetailsTable.rowAtPoint(arg0.getPoint());
                setUploadClearButtons();
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                mousePressed = stockDetailsTable.rowAtPoint(arg0.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                int mouseReleased = stockDetailsTable.rowAtPoint(arg0.getPoint());
                if (mouseReleased != mousePressed)
                    setUploadClearButtons();
            }

        });
        addDeleteButtonListener();
        // actions after click add button -- add a blank row
        stockDetailsTablePanel.getAddButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                ((DefaultTableModel) stockDetailsTable.getModel()).addRow(new Object[] { false,
                        String.valueOf(stockDetailsTableRowID++), "", "", "", "", "", "", "", "", "" });
                clearStockDetailsSelection();
            }

        });
    }

    private void clearStockDetailsSelection() {
        stockDetailsTable.clearSelection();
        stockDetailsTable.setForeground(Color.BLACK);
        setUploadClearButtons();
    }

    /**
     * actions after click delete button associated with stock details table
     */
    private void addDeleteButtonListener() {
    	stockDetailsTablePanel.setActionListenerDeleteButton();
    }

    /**
     * check selected rows, rows without specify StockConstants.ACCESSION or "pedigree" can't be uploaded with samsptat and taxonomy
     * info enable delete button associated with stock derails table, enable clear and upload buttons associate with
     * taxonomy table and classification_code table
     */
    private void setUploadClearButtons() {
        /*
         * for(int rowCounter = 0; rowCounter < stockDetailsTable.getRowCount(); rowCounter++) { if(
         * StringUtils.isEmpty(String.valueOf(stockDetailsTable.getValueAt(rowCounter,
         * stockDetailsTable.getColumn(StockConstants.ACCESSION).getModelIndex()))) &&
         * (StringUtils.isEmpty(String.valueOf(stockDetailsTable.getValueAt(rowCounter,
         * stockDetailsTable.getColumn(StockConstants.PEDIGREE).getModelIndex()))))){
         * //stockDetailsTable.removeRowSelectionInterval(rowCounter, rowCounter); stockDetailsTable.setValueAt(false,
         * rowCounter, 0); } }
         */
        boolean stockDetailsRowsSelected = stockDetailsTable.getSelectedRowCount() > 0;
        stockDetailsTablePanel.getDeleteButton().setEnabled(stockDetailsRowsSelected);
        taxonomyTablePanel.getClearButton().setEnabled(stockDetailsRowsSelected);
        classificationTablePanel.getClearButton().setEnabled(stockDetailsRowsSelected);
        taxonomyTablePanel.getUploadButton().setEnabled(stockDetailsRowsSelected && taxonomySelectedRow != -1);
        classificationTablePanel.getUploadButton().setEnabled(stockDetailsRowsSelected && classificationSelectedRow != -1);

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
        taxonomyTable.hideColumn("taxonomy id");
        
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
            rowData[classificationTable.getColumnModel().getColumnIndex("classification id")] = result.getClassificationId();
            rowData[classificationTable.getColumnModel().getColumnIndex(ColumnConstants.CLASSIFICATION_CODE)] = result.getClassificationCode();
            rowData[classificationTable.getColumnModel().getColumnIndex("Classification Type")] = result.getClassificationType();
            DefaultTableModel model = (DefaultTableModel) classificationTable.getModel();
            model.addRow(rowData);
            classificationTable.setModel(model);
        }
        classificationTable.hideColumn("classification id");     
    }

    /**
     * initilize an empty stock details table
     */
    private void initilizeStockDetailsTable() {
        DefaultTableModel model = (DefaultTableModel) stockDetailsTable.getModel();
        Utils.removeAllRowsFromTable(model);
    }

    /**
     * 
     * @return
     */
    private JPanel getImportExportPanel() {
        JPanel importExportPanel = new JPanel(new MigLayout("insets 0, gap 5"));
        filePath.setColumns(20);

        downloadButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                File file = new File(System.getProperty("java.io.tmpdir") + "/" + "MaizeAtlasOutsideSeed.csv");
                fileChooser.setSelectedFile(file);
                int approve = fileChooser.showSaveDialog(StockSelectionPanel.this);
                if (approve != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                file = fileChooser.getSelectedFile();
                // String fileLocation = System.getProperty("user.home");
                // String fileName = "\\MaizeAtlasOutsideSeed";
                // String extension = ".csv";
                // File file = new File(fileLocation + fileName + extension);
                // int attempt = 0;
                // DecimalFormat df = new DecimalFormat("0000");
                // while(file.exists()) {
                // file = new File(fileLocation + fileName + df.format(++attempt) + extension);
                // }

                try {
                    file.createNewFile();
                    BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                    bw.write("Accession Name, Accession Identifier, Pedigree, Passport Comment, Generation,Cycle, Generation Comment");
                    bw.close();
                    Desktop.getDesktop().open(file);
                } catch (IOException ioe) {
                    if (LoggerUtils.isLogEnabled()) {
                        LoggerUtils.log(Level.INFO, "Fail to excute button click");
                    }
                    JLabel errorFields = new JLabel(
                            "<HTML><FONT COLOR = Blue>Failed to create file. File might be already open or<br>you do not have permission to write in that folder </FONT></HTML>");
                    JOptionPane.showMessageDialog(StockSelectionPanel.this, errorFields);
                }
            }
        });

        JPanel downloadPane = new JPanel(new MigLayout("insets 0,gapx 0"));
        downloadPane.add(downloadTemplate);
        downloadPane.add(downloadButton, "gapleft 10, h 24:24:24");
        importExportPanel.add(downloadPane, "spanx, pushy, wrap");

        filePath.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                browsebuttonActionPerformed();
            }
        });

        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                browsebuttonActionPerformed();
            }
        });

        JPanel uploadPane = new JPanel(new MigLayout("insets 0,gapx 0"));
        // uploadPane.add(filePath);
        uploadPane.add(browseButton, "gapleft 3,h 24:24:24,grow");
        uploadPane.add(uploadTemplate, "gapleft 5,grow");
        uploadPane.add(donorName, "gapleft 5,grow");
        uploadPane.add(new JLabel(" on "));
        uploadPane.add(customCalendar.getCustomDateCalendar(), "wrap");

        importExportPanel.add(uploadPane, "spanx, pushy, wrap");

        return importExportPanel;
    }

    public void browsebuttonActionPerformed() {

        try {
            final JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Comma separated files(.csv)", "csv"));
            fc.showOpenDialog(this);

            if (fc.getSelectedFile() == null)
                return;

            String filename = fc.getSelectedFile().toString();
            filePath.setText(filename);

            if (!(filePath.getText().trim().endsWith(".csv"))) {
                JLabel errorFields = new JLabel("<HTML><FONT COLOR = Blue>" + "File should have a .csv extension only"
                        + ".</FONT></HTML>");
                JOptionPane.showMessageDialog(StockSelectionPanel.this, errorFields);
            } else {            	
                DefaultTableModel model = (DefaultTableModel) stockDetailsTable.getModel();
                String filepath = filePath.getText();
                BufferedReader br = new BufferedReader(new FileReader(new File(filepath)));
                br.readLine();
                String line = br.readLine();//first line
                int index = 1;
                while(line != null ){
                	String[] lineValues = line.split(",");
                	Object rowData[] = new Object[stockDetailsTable.getColumnCount()]; 
                	rowData[0] = new Boolean(false);
                	rowData[1] = index++;
                	for(int i = 2; i <= lineValues.length+1; ++i){                		
                		rowData[i] = lineValues[i-2];
                	}
                	model.addRow(rowData); 
                	stockDetailsTable.setModel(model);
                	line = br.readLine();
                }             
                br.close();
                
                clearStockDetailsSelection();
            }

        } catch (Exception E) {
            if (LoggerUtils.isLogEnabled()) {
                LoggerUtils.log(Level.INFO, E.toString());
            }
        }
    }

}
