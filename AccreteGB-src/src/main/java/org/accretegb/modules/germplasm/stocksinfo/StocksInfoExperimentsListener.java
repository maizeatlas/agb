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

import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.germplasm.experimentaldesign.ExperimentSelectionPanel;
import org.accretegb.modules.projectexplorer.ProjectTreeNode;

import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author nkumar
 * This handles the event when the user in Stocks Info Panel chooses to
 * add the stocks in an already existing Experiment in that Project
 */
public class StocksInfoExperimentsListener implements ActionListener {
    private ProjectTreeNode node;
    private TableToolBoxPanel saveTablePanel;

    public StocksInfoExperimentsListener(ProjectTreeNode node, TableToolBoxPanel saveTablePanel) {
        this.node = node;
        this.saveTablePanel = saveTablePanel;
    }

    /**
     * added the selected rows from stock info save table to the
     * selected stocks table of Experimental Design
     * @param arg0
     */
    public void actionPerformed(ActionEvent arg0) {
        ExperimentSelectionPanel experimentPanel = (ExperimentSelectionPanel) node.getTabComponent()
                .getComponentPanels().get(0);
        if (experimentPanel != null) {
            CheckBoxIndexColumnTable experimentInputTable = experimentPanel.getExperimentalSelectedStocksPanel()
                    .getTable();
            if (experimentInputTable != null) {
                int tableRowCounter = experimentInputTable.getModel().getRowCount();
                for (int row : saveTablePanel.getTable().getSelectedRows()) {
                    Object[] rowObj = new Object[9];
                    rowObj[0] = String.valueOf(tableRowCounter);
                    for (int column = 1; column < rowObj.length; column++) {
                        rowObj[column] = String.valueOf(saveTablePanel.getTable().getValueAt(row, column + 1));
                    }
                    String stockName = (String) rowObj[2];
                    if (stockName != null && stockName.trim().length() > 1) {
                        Utils.addRowToTable(rowObj, (DefaultTableModel) experimentInputTable.getModel(),
                                tableRowCounter, true);
                        tableRowCounter++;
                    }
                }
            }

        }
    }
}
