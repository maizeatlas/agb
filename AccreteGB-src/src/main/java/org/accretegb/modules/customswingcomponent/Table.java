package org.accretegb.modules.customswingcomponent;

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

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import java.awt.Color;
import java.util.List;

/**
 * @author nkumar
 * This is extended tables class which takes
 * column names and initializes the table
 * This class is mainly used when we need just the output
 * such as the preplanting, phenotype etc
 */
public class Table extends JTable {

    private static final long serialVersionUID = 1L;
    private static final int NO_OF_ROWS = 30;
    private List<String> columnNames;

    /**
     * initializes the table
     */
    public void initialize() {
        Object rows[][] = new Object[NO_OF_ROWS][getColumnNames().size()];
        for (int rowCounter = 0; rowCounter < NO_OF_ROWS; rowCounter++) {
            rows[rowCounter] = new Object[getColumnNames().size()];
            for (int columnCounter = 0; columnCounter < getColumnNames().size(); columnCounter++) {
                rows[rowCounter][columnCounter] = new String();
            }
        }
        setGridColor(Color.LIGHT_GRAY);
        setModel(getTableModel(rows, (String[]) getColumnNames().toArray(new String[getColumnNames().size()])));
    }

    public TableModel getTableModel(Object row[][], String[] columnNames) {
        return new DefaultTableModel(row, columnNames) {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings({ "rawtypes", "unchecked" })
            public Class getColumnClass(int column) {
                Class returnValue;
                if ((column >= 0) && (column < getColumnCount())) {
                	if (getValueAt(0, column) != null) {
                        returnValue = getValueAt(0, column).getClass();
                	} else {
                		returnValue = Object.class;
                	}
                } else {
                    returnValue = Object.class;
                }
                return returnValue;
            }

            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

}
