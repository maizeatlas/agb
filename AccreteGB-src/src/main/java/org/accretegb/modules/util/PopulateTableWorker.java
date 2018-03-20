package org.accretegb.modules.util;

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

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.dao.ObservationUnitDAO;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.util.List;
import java.util.Map;

/**
 * @author nkumar
 * This class is used to background processing in tables which retrieve
 * data using a SQL query and populates itself.
 */
public abstract class PopulateTableWorker extends SwingWorker<DefaultTableModel, Object[]> {

    private final DefaultTableModel model;
    private Map<String, String> sqlParameters;
    private JProgressBar progressBar;
    private int rowIndex = 0;

    public PopulateTableWorker(DefaultTableModel model, Map<String, String> sqlParameters, JProgressBar progressBar) {
        this.model = model;
        this.sqlParameters = sqlParameters;
        this.progressBar = progressBar;
    }

    abstract public String createSQLQuery(Map<String, String> sqlParameters);
    /**
     * takes SQL parameters as a map with key being
     * column name of table and value being column value
     * in database table
     * @param sqlParameters Map of SQL Parameters
     * @return List of Object returned from SQL Query
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> createQueryResults(Map<String, String> sqlParameters) {
    	String sql = createSQLQuery(sqlParameters);
        List<Object[]> results = ObservationUnitDAO.getInstance().query(sql);
        return results;
    }

 
    /**
     * publishs the results in the table
     * @return the tableModel
     * @throws Exception
     */
    @Override
    protected DefaultTableModel doInBackground() throws Exception {
        List<Object[]> results = createQueryResults(sqlParameters);
        for (int rowCounter = 0, size = results.size(); rowCounter < size; rowCounter++) {
            publish(results.get(rowCounter));
            getProgressBar().setValue((rowCounter + 1) * 100 / size);
        }

        return model;
    }

    /**
     * inserts or sets the data in the table
     * @param chunks the chunks which are be inserted or set in the table
     */
    @Override
    protected synchronized void process(List<Object[]> chunks) {
        if (chunks != null) {
            for (Object[] row : chunks) {
                for (int columnIndex = 0, size = row.length; columnIndex < size; columnIndex++) {
                    if (StringUtils.isEmpty((String)row[columnIndex])) {
                        row[columnIndex] = new String("NULL");
                    }
                    if (model.getRowCount() > rowIndex) {
                        model.setValueAt(row[columnIndex], rowIndex, columnIndex);
                    } else {
                        model.addRow(row);
                    }
                }
            }
            rowIndex++;
        }
    } 

    public Map<String, String> getSqlParameters() {
        return sqlParameters;
    }

    public void setSqlParameters(Map<String, String> sqlParameters) {
        this.sqlParameters = sqlParameters;
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

}
