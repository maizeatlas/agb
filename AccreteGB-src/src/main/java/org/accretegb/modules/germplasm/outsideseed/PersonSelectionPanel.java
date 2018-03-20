package org.accretegb.modules.germplasm.outsideseed;

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

import org.accretegb.modules.MainLayout;
import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.hibernate.Source;
import org.accretegb.modules.hibernate.dao.LocationDAO;
import org.accretegb.modules.hibernate.dao.SourceDAO;
import org.accretegb.modules.hibernate.dao.StockDAO;
import org.accretegb.modules.hibernate.Location;
import org.accretegb.modules.tab.TabComponent;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.tab.TabManager;
import org.accretegb.modules.util.LoggerUtils;
import org.hibernate.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

/**
 * @author chinmay
 * This is first panel shown to the user in outside seed tabcomponent
 */
public class PersonSelectionPanel extends TabComponentPanel {

    private TableToolBoxPanel sourceTablePanel;
    private int sourceIdColumn;

    private static final long serialVersionUID = 1L;

    /**
     * initializes the person selection component panel
     */
    public void initialize() {
    	sourceTablePanel.getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setLayout(new MigLayout("insets 20, gap 10"));
        getPrevPanelButton().setEnabled(false);
        sourceIdColumn = getSourceTablePanel().getTable().getColumn("Source id").getModelIndex();
        add(getPrevPanelButton(), "pushx");
        add(getNextPanelButton(), "wrap");       
        populateSourceTableData();
        add(getSourceTablePanel(), "h 100%, growx, spanx, pushx, wrap");
        addActionListeners();
        addNavigationButtonListeners();
    }

    /**
     * sets the action listener for the next button
     */
    private void addNavigationButtonListeners() {
        getNextPanelButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (sourceTablePanel.getTable().getSelectedRowCount() != 1) {
                    JOptionPane.showConfirmDialog(
                            PersonSelectionPanel.this,
                            "Select a donor to continue", "Error!", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                MainLayout mainLayout =AccreteGBBeanFactory.getMainLayoutBean();
                TabManager tabManager = mainLayout.getTabManager();
                TabComponent tabComponent = tabManager.getTabComponent(getTabComponentPanel());
                tabComponent.setCurrentComponentPanel(getTabComponentPanel());
                TabComponentPanel nextTabComponentPanel = tabComponent.getNextComponentPanel();
                ((StockSelectionPanel) nextTabComponentPanel).setSource(getSelectedSource());
                tabManager.setReplaceComponentPanel(getTabComponentPanel(), nextTabComponentPanel);
            }
        });
    }

    protected Source getSelectedSource() {
    	int row = sourceTablePanel.getTable().getSelectedRow();
        String sourceID = sourceTablePanel.getTable().getValueAt(row, sourceIdColumn).toString();
        Source source = SourceDAO.getInstance().findSource(sourceID);
        return source;
    }

    private void addActionListeners() {

        sourceTablePanel.getDeleteButton().setEnabled(false);
        sourceTablePanel.getEditButton().setEnabled(false);
        sourceTablePanel.getAddButton().setEnabled(true);

        addAddButtonActionListener();
        addEditButtonActionListene();
        addDeleteButtonActionListener();

        sourceTablePanel.getTable().addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
                if (sourceTablePanel.getTable().getSelectedRowCount() > 0) {
                    sourceTablePanel.getEditButton().setEnabled(true);
                    sourceTablePanel.getDeleteButton().setEnabled(true);
                } else {
                    sourceTablePanel.getEditButton().setEnabled(false);
                    sourceTablePanel.getDeleteButton().setEnabled(false);
                }
            }
        });

    }

    private void addDeleteButtonActionListener() {
        sourceTablePanel.getDeleteButton().addActionListener(new ActionListener() {

            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {

                CheckBoxIndexColumnTable personsTable = sourceTablePanel.getTable();
                int selRow = personsTable.getSelectedRow();
                int selcolumn = personsTable.getSelectedColumn();
                String columnName = personsTable.getColumnName(selcolumn + 1);
                int donorID = Integer.parseInt(String.valueOf(personsTable.getValueAt(selRow, sourceIdColumn)));

                try {
                    List<BigInteger> stocksUnderSource =StockDAO.getInstance().getSourceFromStock(donorID);
                    if (stocksUnderSource.get(0).intValue() > 0) {
                        JOptionPane
                                .showConfirmDialog(
                                        PersonSelectionPanel.this, "Cannot delete sources with stocks",
                                        "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
                        personsTable.setValueAt(false, selRow, 0);
                        personsTable.clearSelection();
                    } else {                       
                        ((DefaultTableModel) personsTable.getModel()).removeRow(personsTable.convertRowIndexToModel(selRow));
                        //donorID = -1;
                        SourceDAO.getInstance().delete(donorID);
                    }
                    personsTable.setForeground(Color.BLACK);
                } catch (HibernateException ex) {
                    if (LoggerUtils.isLogEnabled()) {
                        LoggerUtils.log(Level.INFO, "error in delete" + ex);
                    }
                }
                sourceTablePanel.getDeleteButton().setEnabled(false);
                sourceTablePanel.getEditButton().setEnabled(false);
            }
        });
    }

    private void addEditButtonActionListene() {
        sourceTablePanel.getEditButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                CheckBoxIndexColumnTable personsTable = sourceTablePanel.getTable();

                int selRow = personsTable.getSelectedRow();
                int donorID = Integer.parseInt(String.valueOf(personsTable.getValueAt(selRow, sourceIdColumn)));

                JPanel newSource = new JPanel(new MigLayout("insets 0, gapx 0"));

                String labelNames[] = { "<HTML>First Name<FONT COLOR = Red>*</FONT></HTML>", "<HTML>Last Name<FONT COLOR = Red>*" + "</FONT></HTML>","Institute","Department","Street Address",ColumnConstants.CITY, ColumnConstants.STATE,
                        ColumnConstants.COUNTRY, "<HTML>Zipcode<FONT COLOR = Red>*" + "</FONT></HTML>",ColumnConstants.LOCATION_NAME, "Location Comment","Phone Number", "Fax", "Email", "URL", "Source Comment" };

                String values[] = new String[labelNames.length];
                String oldValues[] = new String[labelNames.length];

                for (int valueCounter = 0; valueCounter < values.length; valueCounter++)
                {
                	values[valueCounter] = String.valueOf(personsTable.getValueAt(selRow, valueCounter + 2));
                	oldValues[valueCounter] = String.valueOf(personsTable.getValueAt(selRow, valueCounter + 2));
                }

                boolean validInput = false;
                JLabel labels[] = new JLabel[labelNames.length];
                final JTextField textBoxes[] = new JTextField[labelNames.length];
                int labelsOrder[] = { 0, 1, 2, 3, 4, 8, 5, 6, 7, 9, 10, 11, 12, 13, 14,15};              
                for (int labelIndex : labelsOrder) {
                    labels[labelIndex] = new JLabel(labelNames[labelIndex]);
                    newSource.add(labels[labelIndex], "gapleft 10, push");
                    textBoxes[labelIndex] = new JTextField();
                    textBoxes[labelIndex].setPreferredSize(new Dimension(200, 0));
                    textBoxes[labelIndex].setText(values[labelIndex]);
                    newSource.add(textBoxes[labelIndex], "gapRight 10, wrap");
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

                do {
                    for (int columnIndex = 0; columnIndex < labelNames.length; columnIndex++)
                    {
                    	textBoxes[columnIndex].setText(values[columnIndex]);
                    }

                    int option = JOptionPane.showConfirmDialog(
                            PersonSelectionPanel.this, newSource, "Update Donor Information ",
                            JOptionPane.DEFAULT_OPTION);
                    validInput = true;

                    if (option == JOptionPane.OK_OPTION) {

                        for (int columnIndex = 0; columnIndex < labelNames.length; columnIndex++) {
                      
                            values[columnIndex] = textBoxes[columnIndex].getText();
                            if (labelNames[columnIndex].indexOf('*') != -1
                                    && textBoxes[columnIndex].getText().equals("")) {
                                validInput = false;
                            }
                        }
                        if (validInput) {
                        	for (int columnIndex = 0; columnIndex < values.length; columnIndex++)
                            {	
                                personsTable.setValueAt(String.valueOf(values[columnIndex]), selRow, columnIndex + 2);
                            }                        	
                        	Location existingLocation = LocationDAO.getInstance().findLocation(oldValues[9], oldValues[5], oldValues[6], oldValues[7], oldValues[8],oldValues[10]);
                        	LocationDAO.getInstance().updateLocation(values[9], values[5], values[6], values[7], values[8],values[10], existingLocation.getLocationId());
                            SourceDAO.getInstance().update(values, donorID, existingLocation);
                            
                        }
                    }
        
                    if (!validInput)
                        JOptionPane.showConfirmDialog(
                                        PersonSelectionPanel.this,
                                        "<HTML><FONT COLOR = Red>*</FONT> marked fields are mandatory.</HTML>",
                                        "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
                } while (!validInput);
            }
        });
    }

    private void addAddButtonActionListener() {
        sourceTablePanel.getAddButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                sourceTablePanel.getDeleteButton().setEnabled(false);
                sourceTablePanel.getEditButton().setEnabled(false);

                int selRow = sourceTablePanel.getTable().getSelectedRow();

                if (selRow >= 0)
                    sourceTablePanel.getTable().setValueAt(false, selRow, 0);

                sourceTablePanel.getTable().clearSelection();
                // donorID = -1;
                sourceTablePanel.getTable().setForeground(Color.BLACK);

                JPanel newSource = new JPanel(new MigLayout("insets 0, gapx 0"));

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
                    newSource.add(labels[labelIndex], "gapleft 10, push");
                    textBoxes[labelIndex] = new JTextField();
                    textBoxes[labelIndex].setPreferredSize(new Dimension(200, 0));
                    textBoxes[labelIndex].setText(values[labelIndex]);
                    newSource.add(textBoxes[labelIndex], "gapRight 10, wrap");
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

                do {
                    for (int textBoxIndex = 0; textBoxIndex < labelNames.length; textBoxIndex++)
                        textBoxes[textBoxIndex].setText(values[textBoxIndex]);

                    int option = JOptionPane.showConfirmDialog(
                            PersonSelectionPanel.this, newSource, "Enter New Source Information ",
                            JOptionPane.OK_CANCEL_OPTION);

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
                            Object newRow[] = new Object[values.length + 2];
                            newRow[0] = new Boolean(false);
                            newRow[1] = String.valueOf(newSourceId);
                            for (int columnIndex = 2; columnIndex < newRow.length; columnIndex++)
                            {
                            	newRow[columnIndex] = values[columnIndex - 2];
                            	if(String.valueOf(newRow[columnIndex]).equalsIgnoreCase("null")){
                            		newRow[columnIndex] = "NULL";
                            	}
                            }
                            ((DefaultTableModel) sourceTablePanel.getTable().getModel()).insertRow(sourceTablePanel
                                    .getTable().getRowCount(), newRow);
                        }
                    }else{
                    	break;
                    }
                    if (!validInput)
                        JOptionPane
                                .showConfirmDialog(
                                        PersonSelectionPanel.this,
                                        "<HTML><FONT COLOR = Red>*</FONT> marked fields are mandatory.</HTML>",
                                        "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);

                } while (!validInput);
            }
        });

    }

  

    private Location manageLocation(String[] values) {

        Location location = LocationDAO.getInstance().findLocation(values[9], values[5], values[6], values[7], values[8], values[10]);
        if(location == null){
        	location = LocationDAO.getInstance().insertNewLocation(values[9], values[5], values[6], values[7], values[8], values[10],null, null, null);
        }
        return location;
    }

    private void populateSourceTableData() {
       
        try {           
        	List<Object[]> results = SourceDAO.getInstance().findByLocation();
            getSourceTablePanel().getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            if (results.size() > 0) {
                Utils.removeAllRowsFromTable((DefaultTableModel) getSourceTablePanel().getTable().getModel());
            }
            for (Object[] result : results) {
                result[0] = new Boolean(false);
                result[3] = ((String) result[2]).split(" ")[1];
                result[2] = ((String) result[2]).split(" ")[0];
            }
            Collections.sort(results, new Comparator<Object[]>() {
                public int compare(Object[] o1, Object[] o2) {
                    return ((String) o1[3]).toLowerCase().compareTo(((String) o2[3]).toLowerCase());
                }
            });
            DefaultTableModel model = (DefaultTableModel) getSourceTablePanel().getTable().getModel();
            for (Object[] result : results)
            {
            	for(int i = 0; i < result.length; ++i){
            		if(String.valueOf(result[i]).equalsIgnoreCase("null")){
            			result[i] = "NULL";
            		}
            	}
            	model.addRow(result);
            }
            getSourceTablePanel().getTable().setModel(model);
            getSourceTablePanel().getTable().getColumn("Source id").setWidth(0);
            getSourceTablePanel().getTable().getColumn("Source id").setMinWidth(0);
            getSourceTablePanel().getTable().getColumn("Source id").setMaxWidth(0);

        }catch(Exception e){}
    }
    public TableToolBoxPanel getSourceTablePanel() {
        return sourceTablePanel;
    }

    public void setSourceTablePanel(TableToolBoxPanel sourceTablePanel) {
        this.sourceTablePanel = sourceTablePanel;
    }

}
