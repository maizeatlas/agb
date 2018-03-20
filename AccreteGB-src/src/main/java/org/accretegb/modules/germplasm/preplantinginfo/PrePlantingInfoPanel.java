package org.accretegb.modules.germplasm.preplantinginfo;

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

import org.accretegb.modules.customswingcomponent.CustomCalendar;
import org.accretegb.modules.customswingcomponent.Table;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.hibernate.dao.FieldDAO;
import org.accretegb.modules.tab.TabComponentPanel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.accretegb.modules.customswingcomponent.Utils.saveTableToFile;

public class PrePlantingInfoPanel extends TabComponentPanel {

    private static final long serialVersionUID = 1L;

    private JProgressBar progressBar;
    private Table tableOutput;
    private JTextField startPlot;
    private JTextField endPlot;
    private CustomCalendar startDate;
    private CustomCalendar endDate;
    private JLabel progressBarValue;
    private JCheckBox includePlants;
    private TableToolBoxPanel fieldTablePanel;
    private JButton submitButton;
    private JButton exportButton;


    public TableToolBoxPanel getFieldTablePanel() {
        return fieldTablePanel;
    }

    public void setFieldTablePanel(TableToolBoxPanel fieldTablePanel) {
        this.fieldTablePanel = fieldTablePanel;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public Table getTableOutput() {
        return tableOutput;
    }

    public void setTableOutput(Table tableOutput) {
        this.tableOutput = tableOutput;
    }

    public JTextField getStartPlot() {
        return startPlot;
    }

    public void setStartPlot(JTextField startPlot) {
        this.startPlot = startPlot;
    }

    public JTextField getEndPlot() {
        return endPlot;
    }

    public void setEndPlot(JTextField endPlot) {
        this.endPlot = endPlot;
    }

    public CustomCalendar getStartDate() {
        return startDate;
    }

    public void setStartDate(CustomCalendar startDate) {
        this.startDate = startDate;
    }

    public CustomCalendar getEndDate() {
        return endDate;
    }

    public void setEndDate(CustomCalendar endDate) {
        this.endDate = endDate;
    }

    public JLabel getProgressBarValue() {
        return progressBarValue;
    }

    public void setProgressBarValue(JLabel progressBarValue) {
        this.progressBarValue = progressBarValue;
    }

    public JCheckBox getIncludePlants() {
        return includePlants;
    }

    public void setIncludePlants(JCheckBox includePlants) {
        this.includePlants = includePlants;
    }

    public JButton getSubmitButton() {
        return submitButton;
    }

    public void setSubmitButton(JButton submitButton) {
        this.submitButton = submitButton;
    }

    public JButton getExportButton() {
        return exportButton;
    }

    public void setExportButton(JButton exportButton) {
        this.exportButton = exportButton;
    }

    public void populateFieldTableData() {
      
        try {
            
            List<Object[]> results = FieldDAO.getInstance().findByLocation();
            if (results.size() > 0) {
                Utils.removeAllRowsFromTable((DefaultTableModel) getFieldTablePanel().getTable().getModel());
            }
            DefaultTableModel model = (DefaultTableModel) getFieldTablePanel().getTable().getModel();
            for (Object[] obj : results) {
                obj[0] = new Boolean(false);
                obj[1] = Integer.toString((Integer) obj[1]);               
                model.addRow(obj);                
            }
            getFieldTablePanel().getTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
            getFieldTablePanel().getTable().setSingleSelection(true);
            getFieldTablePanel().getTable().setModel(model);
        } catch(Exception e) {
           
        }
    }

    JPanel getSearchComponentsPanel() {
        JPanel searchComponentsPanel = new JPanel();
        MigLayout layout = new MigLayout("insets 40 0 0 0");
        searchComponentsPanel.setLayout(layout);
        searchComponentsPanel.add(new JLabel("Start Date"), "align label");
        searchComponentsPanel.add(startDate.getCustomDateCalendar(), "wrap");
        searchComponentsPanel.add(new JLabel("End Date"), "align label");
        searchComponentsPanel.add(endDate.getCustomDateCalendar(), "wrap");
        searchComponentsPanel.add(new JLabel("Plot (from)"), "align label");
        searchComponentsPanel.add(startPlot, "wrap");
        searchComponentsPanel.add(new JLabel("Plot (to)"), "align label");
        searchComponentsPanel.add(endPlot, "wrap");
        searchComponentsPanel.add(includePlants, "span, wrap");
        return searchComponentsPanel;
    }

    public void initializePanel() {
        MigLayout layout = new MigLayout("insets 10, gap 5");
        setLayout(layout);
        populateFieldTableData();
        add(getSearchComponentsPanel(), "h 50%");
        add(getFieldTablePanel(), "growx, spanx, pushx, h 50%, wrap");
        add(new JLabel(""), "align label");
        add(submitButton, "tag submit, sizegroup bttn, alignx right,wrap");

        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executeTask();
            }
        });

        progressBar.setAlignmentX(0f);
        add(progressBarValue, "span");
        add(progressBar, "gapleft 5, gapright 5, span, grow, wrap");
        Utils.setTableSize(tableOutput, 1.0f, 10);
        add(new JScrollPane(tableOutput), "gapleft 5, gapright 5, h 50%, span, grow, wrap");

        JPanel exportTablePanel = new JPanel();
        exportTablePanel.setLayout(new BorderLayout());
        exportTablePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        exportTablePanel.add(exportButton, BorderLayout.EAST);
        add(exportTablePanel, "span, grow, wrap");
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveTableToFile(getTableOutput(), null, "previousPrePlanting.csv");
            }
        });
    }

    public void clearFields() {
        getFieldTablePanel().getTable().clearSelection();
        getStartPlot().setText("");
        getEndPlot().setText("");
        getIncludePlants().setSelected(false);
    }

    public void initialize() {
        initializePanel();
    }

    boolean isInputValid() {
        if(startDate.getEditorText().isEmpty() || endDate.getEditorText().isEmpty()){
        	return false;
        }
        if (!Utils.isValidInteger(startPlot.getText()) && !(startPlot.getText().trim().length() == 0)) {
            JLabel errorFields = new JLabel("<HTML><FONT COLOR = Blue>Error: Start Plot should be integer"
                    + ".</FONT></HTML>");
            JOptionPane.showMessageDialog(this, errorFields);
            return false;
        }
        if (!Utils.isValidInteger(endPlot.getText()) && !(endPlot.getText().trim().length() == 0)) {
            JLabel errorFields = new JLabel("<HTML><FONT COLOR = Blue>Error: End Plot should be integer"
                    + ".</FONT></HTML>");
            JOptionPane.showMessageDialog(this, errorFields);
            return false;
        }
        return true;
    }

    String getSelectedFieldId() {
        if (getFieldTablePanel().getTable().getSelectedRowCount()>0) {
        	StringBuilder fieldIds = new StringBuilder();
        	//System.out.println(getFieldTablePanel().getTable().getSelectedRows());
        	for(int row : getFieldTablePanel().getTable().getSelectedRows()){
        		fieldIds.append(getFieldTablePanel().getTable().getValueAt(row, getFieldTablePanel().getTable().getIndexOf("Field ID")));
        		fieldIds.append("_");
        	}
            return fieldIds.toString().substring(0, fieldIds.length()-1);
        }
        return "-1";
    }

    @SuppressWarnings("deprecation")
    public Map<String, String> generateSearchValuesMap() {
        Map<String, String> searchValues = new HashMap<String, String>();
        String fieldIds = getSelectedFieldId();
        if (!fieldIds.equals("-1")) {
            searchValues.put("field_id", fieldIds);
        }else{
        	JLabel errorFields = new JLabel("Please select a field");
            JOptionPane.showMessageDialog(this, errorFields);
            return searchValues;
        }
        if ((startDate.getCalDate() != null) || (endDate.getCalDate() != null)) {
            String fromDate = "";
            if (startDate.getCalDate() != null) {
                int year = startDate.getCalDate().getTime().getYear() + 1900;
                int month = startDate.getCalDate().getTime().getMonth() + 1;
                int day = startDate.getCalDate().getTime().getDate();
                fromDate = year + "-" + month + "-" + day;
            }

            String toDate = "";
            if (startDate.getCalDate() != null) {
                int year = endDate.getCalDate().getTime().getYear() + 1900;
                int month = endDate.getCalDate().getTime().getMonth() + 1;
                int day = endDate.getCalDate().getTime().getDate();
                toDate = year + "-" + month + "-" + day;
            }

            if (fromDate.length() == 0) {
                fromDate = "0";
            }
            if (toDate.length() == 0) {
                toDate = "0";
            }
            searchValues.put("planting_date", fromDate + ":" + toDate);
        }

        if ((startPlot.getText().trim().length() != 0) || (endPlot.getText().trim().length() != 0)) {
            String fromPlot = startPlot.getText().trim();
            String toPlot = endPlot.getText().trim();
            if (fromPlot.length() == 0) {
                fromPlot = "0";
            }
            if (toPlot.length() == 0) {
                toPlot = "0";
            }
            searchValues.put("plot", fromPlot + ":" + toPlot);
        }

        if (includePlants.isSelected()) {
            searchValues.put("plant", "true");
        }
        return searchValues;
    }

    public void executeTask() {
        if (!isInputValid()) {
            return;
        }
        getProgressBarValue().setText("Processing request..");
        PrePlantingTableWorker worker = new PrePlantingTableWorker((DefaultTableModel) getTableOutput().getModel(),
                generateSearchValuesMap(), getProgressBar());
        worker.execute();
    }

    public void propertyChange(PropertyChangeEvent arg0) {

    }

}