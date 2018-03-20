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
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.hibernate.Source;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.util.LoggerUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.Level;

import static org.accretegb.modules.customswingcomponent.Utils.saveTableToFile;

/**
 * @author Chinmay
 * This is the last panel in the outside seed tabcomponent
 * showing the statistics of stocks inserted by user.
 */
public class OutsideSeedReportPanel extends TabComponentPanel {

    private static final long serialVersionUID = 1L;

    private JLabel date;
    private JLabel donor;
    private JLabel totalStocks;
    private JLabel successfulEntries;
    private JLabel unsuccessfulEntries;
    private JLabel classification_codeUsed;
    private JLabel taxonomyUsed;
    private JLabel error;

    private JLabel dateValue;
    private JLabel donorValue;
    private JLabel totalStocksValue;
    private JLabel successfulEntriesValue;
    private JLabel unsuccessfulEntriesValue;
    private JLabel classification_codeUsedValue;
    private JLabel taxonomyUsedValue;
    private JTextArea errorMessage;

    private JButton exportReport;
    private JButton stockExportButton;
    private CheckBoxIndexColumnTable finalTagsTable;
    private Source source;
    private StringBuilder errorBlock;
    private JPanel reportPanel;
    private int successful;
    private HashMap<String, Integer> classification_codeFrequency;
    private HashMap<String, Integer> taxonomyFrequency;

    /**
     * initializes the text Labels and layouts for the panel
     */
    public void initialize() {
        initializeTextLabels();
        setLayout(new MigLayout("insets 20, gap 5"));
        add(getPrevPanelButton(), "pushx");
        getNextPanelButton().setEnabled(false);
        add(getNextPanelButton(), "wrap");
        reportPanel = new JPanel();
        add(reportPanel, "gaptop 10, h 100%, grow, span, wrap");

        getPrevPanelButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                prevButtonActionPerformed();
            }
        });
    }

    /**
     * initializes the text labels
     */
    private void initializeTextLabels() {
        date = new JLabel("Date");
        donor = new JLabel("Donor");
        totalStocks = new JLabel("Total Stocks");
        successfulEntries = new JLabel("Successful Entries");
        unsuccessfulEntries = new JLabel("Unsuccessful Entries");
        classification_codeUsed = new JLabel("Classification Codes used");
        taxonomyUsed = new JLabel("Taxonomies used");
        error = new JLabel("Error");
        errorMessage = new JTextArea();
        exportReport = new JButton("Export Report");
        stockExportButton = new JButton("Export Table");
    }

    /**
     * initializes the report panel for display
     * @return
     */
    public JPanel initializeReportPanel() {

        reportPanel.removeAll();

        classification_codeFrequency = new HashMap<String, Integer>();
        taxonomyFrequency = new HashMap<String, Integer>();

        for (int rowCounter = 0; rowCounter < finalTagsTable.getRowCount(); rowCounter++) {
            String sampVal = String.valueOf(finalTagsTable.getValueAt(rowCounter,
                    finalTagsTable.getColumn("Classification Type").getModelIndex()));
            String taxVal = String.valueOf(finalTagsTable.getValueAt(rowCounter, finalTagsTable.getColumn("Taxonomy").getModelIndex()));
            if (classification_codeFrequency.containsKey(sampVal))
            {
            	classification_codeFrequency.put(sampVal, classification_codeFrequency.get(sampVal) + 1);
            }
            else
            {
            	classification_codeFrequency.put(sampVal, 1);
            }
            if (taxonomyFrequency.containsKey(taxVal))
            {
              taxonomyFrequency.put(taxVal, taxonomyFrequency.get(taxVal) + 1);
            }
            else
            {
            	taxonomyFrequency.put(taxVal, 1);
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
    	Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        dateValue = new JLabel(dateFormat.format(calendar.getTime()));
        donorValue = new JLabel(source.getPersonName());
        totalStocksValue = new JLabel(String.valueOf(finalTagsTable.getRowCount()));
        successfulEntriesValue = new JLabel(String.valueOf(successful));
        unsuccessfulEntriesValue = new JLabel(String.valueOf(finalTagsTable.getRowCount() - successful));
        classification_codeUsedValue = new JLabel(String.valueOf(classification_codeFrequency.size()));
        taxonomyUsedValue = new JLabel(String.valueOf(taxonomyFrequency.size()));

        reportPanel.setLayout(new MigLayout("insets 0, gap 5"));
        JPanel valuePanel = new JPanel(new MigLayout("insets 0, gap 5"));
        JPanel labelPanel = new JPanel(new MigLayout("insets 0, gap 5"));
        valuePanel.add(new JLabel(), "pushx");
        valuePanel.add(dateValue, "right, wrap");
        valuePanel.add(new JLabel(), "pushx");
        valuePanel.add(donorValue, "right, wrap");
        valuePanel.add(new JLabel(), "pushx");
        valuePanel.add(totalStocksValue, "right, wrap");
        valuePanel.add(new JLabel(), "pushx");
        valuePanel.add(successfulEntriesValue, "right, wrap");
        valuePanel.add(new JLabel(), "pushx");
        valuePanel.add(unsuccessfulEntriesValue, "right, wrap");
        valuePanel.add(new JLabel(), "pushx");
        valuePanel.add(classification_codeUsedValue, "right, wrap");
        labelPanel.add(date, "wrap");
        labelPanel.add(donor, "wrap");
        labelPanel.add(totalStocks, "wrap");
        labelPanel.add(successfulEntries, "wrap");
        labelPanel.add(unsuccessfulEntries, "wrap");
        labelPanel.add(classification_codeUsed, "wrap");

        for (String sampval : classification_codeFrequency.keySet()) {
            JLabel text = new JLabel("      |-# stocks for " + sampval);
            JLabel value = new JLabel(String.valueOf(classification_codeFrequency.get(sampval)));
            labelPanel.add(text, "wrap");
            valuePanel.add(new JLabel(), "pushx");
            valuePanel.add(value, "right, wrap");
        }

        labelPanel.add(taxonomyUsed, "wrap");
        valuePanel.add(new JLabel(), "pushx");
        valuePanel.add(taxonomyUsedValue, "right, wrap");

        for (String taxval : taxonomyFrequency.keySet()) {
            JLabel text = new JLabel("      |-# stocks for " + taxval);
            JLabel value = new JLabel(String.valueOf(taxonomyFrequency.get(taxval)));
            labelPanel.add(text, "wrap");
            valuePanel.add(new JLabel(), "pushx");
            valuePanel.add(value, "right, wrap");
        }

        reportPanel.add(valuePanel, "w 50%");
        reportPanel.add(labelPanel, "w 50%, wrap");

        reportPanel.add(error, "wrap");
        errorMessage.setEditable(false);
        if (errorBlock.toString().equalsIgnoreCase(""))
            errorBlock = new StringBuilder("None");
        errorMessage.setText(errorBlock.toString());

        JScrollPane scroll = new JScrollPane(errorMessage, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        reportPanel.add(scroll, "h 100%, w 100%, grow, span, wrap");

        JPanel buttons = new JPanel(new MigLayout("Insets 0, gapx 5"));
        buttons.add(new JLabel(), "pushx");
        buttons.add(exportReport);
        buttons.add(stockExportButton, "wrap");
        reportPanel.add(buttons, "grow, span, push, wrap");

        stockExportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    saveTableToFile(getFinalTagsTable(),
                            OutsideSeedReportPanel.this,
                            "outsideSeedExport.csv");
                } catch (Exception e1) {
                    if (LoggerUtils.isLogEnabled()) {
                        LoggerUtils.log(Level.INFO, e.toString());
                    }
                    JLabel errorFields = new JLabel(
                            "<HTML><FONT COLOR = Blue>Failed to create file. File might be already open or<br>you do not have permission to write in that folder </FONT></HTML>");
                    JOptionPane.showMessageDialog(
                            OutsideSeedReportPanel.this,
                            errorFields);
                }
            }
        });
        addExportReportActionListener();

        return reportPanel;
    }

    /**
     * add the action listener on the export report button
     */
    private void addExportReportActionListener() {
        exportReport.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                String fileName = dateFormat.format(calendar.getTime()) + "OutsideSeedReport";
                String extension = ".txt";
                File finalExportFile = new File(fileName + extension);
                JFileChooser fileChooser = new JFileChooser();
                File file = new File(System.getProperty("java.io.tmpdir") + "/" + finalExportFile);
                fileChooser.setSelectedFile(file);
                int approve = fileChooser.showSaveDialog(OutsideSeedReportPanel.this);
                if (approve != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                file = fileChooser.getSelectedFile();
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                    bw.write(padRight(date.getText(), 70));
                    bw.write(padLeft(dateValue.getText(), 30) + "\r\n");
                    bw.write(padRight(donor.getText(), 70));
                    bw.write(padLeft(donorValue.getText(), 30) + "\r\n");
                    bw.write(padRight(totalStocks.getText(), 70));
                    bw.write(padLeft(totalStocksValue.getText(), 30) + "\r\n");
                    bw.write(padRight(successfulEntries.getText(), 70));
                    bw.write(padLeft(successfulEntriesValue.getText(), 30) + "\r\n");
                    bw.write(padRight(unsuccessfulEntries.getText(), 70));
                    bw.write(padLeft(unsuccessfulEntriesValue.getText(), 30) + "\r\n");
                    bw.write(padRight(classification_codeUsed.getText(), 70));
                    bw.write(padLeft(classification_codeUsedValue.getText(), 30) + "\r\n");

                    for (String sampval : classification_codeFrequency.keySet()) {
                        bw.write(padRight("   |-# stocks for " + sampval, 70));
                        bw.write(padLeft(String.valueOf(classification_codeFrequency.get(sampval)), 30) + "\r\n");
                    }

                    bw.write(padRight(taxonomyUsed.getText(), 70));
                    bw.write(padLeft(taxonomyUsedValue.getText(), 30) + "\r\n");

                    for (String taxval : taxonomyFrequency.keySet()) {
                        bw.write(padRight("   |-# stocks for " + taxval, 70));
                        bw.write(padLeft(String.valueOf(taxonomyFrequency.get(taxval)), 30) + "\r\n");
                    }
                    bw.write(error.getText() + "\r\n");
                    bw.write(errorMessage.getText());
                    bw.close();
                    Desktop.getDesktop().open(finalExportFile);

                } catch (IOException e) {
                    if (LoggerUtils.isLogEnabled()) {
                        LoggerUtils.log(Level.INFO, e.toString());
                    }
                    JLabel errorFields = new JLabel(
                            "<HTML><FONT COLOR = Blue>Failed to create file. File might be already open or<br>you do not have permission to write in that folder </FONT></HTML>");
                    JOptionPane.showMessageDialog(OutsideSeedReportPanel.this, errorFields);
                }
            }

            public String padRight(String s, int n) {
                return String.format("%1$-" + n + "s", s);
            }

            public String padLeft(String s, int n) {
                return String.format("%1$" + n + "s", s);
            }

        });
    }

    public int getSuccessful() {
        return successful;
    }

    public void setSuccessful(int successful) {
        this.successful = successful;
    }
    public JLabel getDate() {
        return date;
    }

    public void setDate(JLabel date) {
        this.date = date;
    }

    public JLabel getDonor() {
        return donor;
    }

    public void setDonor(JLabel donor) {
        this.donor = donor;
    }

    public JLabel getTotalStocks() {
        return totalStocks;
    }

    public void setTotalStocks(JLabel totalStocks) {
        this.totalStocks = totalStocks;
    }

    public JLabel getSuccessfulEntries() {
        return successfulEntries;
    }

    public void setSuccessfulEntries(JLabel successfulEntries) {
        this.successfulEntries = successfulEntries;
    }

    public JLabel getUnsuccessfulEntries() {
        return unsuccessfulEntries;
    }

    public void setUnsuccessfulEntries(JLabel unsuccessfulEntries) {
        this.unsuccessfulEntries = unsuccessfulEntries;
    }

    public JLabel getClassificationCodeUsed() {
        return classification_codeUsed;
    }

    public void setClassificationCodeUsed(JLabel classification_codeUsed) {
        this.classification_codeUsed = classification_codeUsed;
    }

    public JLabel getTaxonomyUsed() {
        return taxonomyUsed;
    }

    public void setTaxonomyUsed(JLabel tanomyUsed) {
        this.taxonomyUsed = tanomyUsed;
    }

    public JLabel getError() {
        return error;
    }

    public void setError(JLabel error) {
        this.error = error;
    }

    public JTextArea getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(JTextArea errorMessage) {
        this.errorMessage = errorMessage;
    }

    public JButton getExportReport() {
        return exportReport;
    }

    public void setExportReport(JButton exportReport) {
        this.exportReport = exportReport;
    }

    public CheckBoxIndexColumnTable getFinalTagsTable() {
        return finalTagsTable;
    }

    public void setFinalTagsTable(CheckBoxIndexColumnTable finalTagsTable) {
        this.finalTagsTable = finalTagsTable;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public StringBuilder getErrorBlock() {
        return errorBlock;
    }

    public void setErrorBlock(StringBuilder errorBlock) {
        this.errorBlock = errorBlock;
    }

    public JButton getStockExportButton() {
        return stockExportButton;
    }

    public void setStockExportButton(JButton stockExportButton) {
        this.stockExportButton = stockExportButton;
    }

}