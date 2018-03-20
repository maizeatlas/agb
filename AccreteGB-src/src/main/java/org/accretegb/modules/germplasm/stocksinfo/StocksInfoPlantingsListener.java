package org.accretegb.modules.germplasm.stocksinfo;

import org.accretegb.modules.constants.ColumnConstants;

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
import org.accretegb.modules.germplasm.planting.Planting;
import org.accretegb.modules.germplasm.planting.PlantingRow;
import org.accretegb.modules.projectexplorer.ProjectTreeNode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nkumar
 * This listener is used to add the selected stocks to the corresponding
 * planting choosen by the user
 */
public class StocksInfoPlantingsListener implements ActionListener {
    private ProjectTreeNode node;
    private TableToolBoxPanel saveTablePanel;

    public StocksInfoPlantingsListener(ProjectTreeNode node, TableToolBoxPanel saveTablePanel) {
        this.node = node;
        this.saveTablePanel = saveTablePanel;
    }

    /**
     *
     * @param arg0
     */
    public void actionPerformed(ActionEvent arg0) {
        Planting plantingPanel = (Planting) node.getTabComponent().getComponentPanels().get(0);
        if (plantingPanel != null) {
            List<PlantingRow> stocksList = new ArrayList<PlantingRow>();
            CheckBoxIndexColumnTable saveTable = saveTablePanel.getTable();
            for (int row : saveTable.getSelectedRows()) {
                int stockId = Integer.parseInt(String.valueOf(saveTable.getValueAt(row, 2)));
                PlantingRow stock = new PlantingRow(Integer.parseInt(String.valueOf(saveTable.getValueAt(row,
                        saveTable.getColumnModel().getColumnIndex(ColumnConstants.STOCK_ID)))), String.valueOf(saveTable
                        .getValueAt(row, saveTable.getColumnModel().getColumnIndex(ColumnConstants.STOCK_NAME))),
                        String.valueOf(saveTable.getValueAt(row,
                                saveTable.getColumnModel().getColumnIndex(ColumnConstants.ACCESSION))), String.valueOf(saveTable
                        .getValueAt(row, saveTable.getColumnModel().getColumnIndex(ColumnConstants.PEDIGREE))),
                        String.valueOf(saveTable.getValueAt(row,
                                saveTable.getColumnModel().getColumnIndex(ColumnConstants.GENERATION))), String.valueOf(saveTable
                        .getValueAt(row, saveTable.getColumnModel().getColumnIndex(ColumnConstants.CYCLE))),
                        String.valueOf(saveTable.getValueAt(row,
                                saveTable.getColumnModel().getColumnIndex(ColumnConstants.CLASSIFICATION_CODE))), String.valueOf(saveTable
                        .getValueAt(row, saveTable.getColumnModel().getColumnIndex(ColumnConstants.POPULATION))));
                stocksList.add(stock);
            }
           // plantingPanel.getTableView().addTableFromList(stocksList);
        }
    }

}
