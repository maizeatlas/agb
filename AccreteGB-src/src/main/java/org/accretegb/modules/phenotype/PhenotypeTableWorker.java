package org.accretegb.modules.phenotype;

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

import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.util.PopulateTableWorker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.accretegb.modules.customswingcomponent.Utils.addRowToTable;
import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;

/**
 * @author nkumar
 * This file use the input parameters entered by the user
 * for phenotypic data, creates a SQL query, get data using
 * SQL Query and populates the table in the background
 */
public class PhenotypeTableWorker extends PopulateTableWorker {

	public PhenotypeTableWorker(DefaultTableModel model, Map<String, String> sqlParameters, JProgressBar progressBar) {
		super(model, sqlParameters, progressBar);
	}

	/**
	 * generate the SQL Query from user Parameters
	 * @param sqlParameters - the map of column name and user entered value
	 * @return - SQL query as a String
	 */
	@Override
	public String createSQLQuery(Map<String, String> sqlParameters) {
		StringBuffer queryBuff = new StringBuffer();
		queryBuff
		.append("select tagname, parameter_name, value, units, measurement_type, tom"
				+ " from measurement_value"
				+ " left join observation_unit on observation_unit.observation_unit_id=measurement_value.observation_unit_id "
				+ " left join field on observation_unit.field_id=field.field_id "
				+ " left join measurement_type on measurement_type.measurement_type_id = measurement_value.measurement_type_id"
				+ " left join measurement_parameter on measurement_parameter.measurement_parameter_id = measurement_value.measurement_parameter_id"
				+ " left join measurement_unit on measurement_unit.measurement_unit_id = measurement_parameter.measurement_unit_id where 1");

		if (sqlParameters.get("field_id") != null) {
			List<String> fieldIds = Arrays.asList(sqlParameters.get("field_id").split("_"));
			int index = 0;
			for(String id : fieldIds){
				if(index == 0){
					queryBuff.append(" and ( field.field_id='" + id + "'"); 
					index++;
				}else{
					queryBuff.append(" or field.field_id='" + id + "'"); 	
				}

			}
			queryBuff.append(" )");      
		}
		
		if (sqlParameters.get("measurement_date") != null) {
			String range[] = sqlParameters.get("measurement_date").split(":");
			if (!range[0].equalsIgnoreCase("0")) {
				queryBuff.append(" and (measurement_value.tom >='" + range[0] + "' or measurement_value.tom is null) ");
			}
			if (!range[1].equalsIgnoreCase("0")) {
				queryBuff.append(" and (measurement_value.tom <='" + range[1] + "' or measurement_value.tom is null) ");
			}
		}

		if (sqlParameters.get("plot") != null) {
			String range[] = sqlParameters.get("plot").split(":");
			if (!range[0].equalsIgnoreCase("0")) {
				queryBuff.append(" and observation_unit.plot >='" + range[0] + "'");
			}
			if (!range[1].equalsIgnoreCase("0")) {
				queryBuff.append(" and observation_unit.plot <='" + range[1] + "'");
			}
		}

		if (sqlParameters.get("parameter_names") != null) {
			String parameter = sqlParameters.get("parameter_names");
			queryBuff.append(" and measurement_parameter.parameter_name in (" + parameter + ")");
		}
		if (sqlParameters.get("tagname_list") != null) {
			String parameter = sqlParameters.get("tagname_list");
			queryBuff.append(" and tagname in (" + parameter + ")");
		}

		if (sqlParameters.get("tagname") != null) {
			String parameter = sqlParameters.get("tagname").replace("*", "%");
			queryBuff.append(" and tagname like '" + parameter + "'");
		}
		queryBuff.append(" order by tagname, parameter_name");
		System.out.println("Query is " + queryBuff);
		return queryBuff.toString();
	}

	/**
	 * gets results from the query and populates the table
	 * @return - model of populated table
	 * @throws Exception
	 */
	@Override
	protected DefaultTableModel doInBackground() throws Exception {
		try {
			PhenotypeInfoPanel phenotypeInfoPanel = AccreteGBBeanFactory.getPhenotypeInfoPanel();
			List<Object[]> results = createQueryResults(getSqlParameters());
			if(results != null)
			{       
				removeAllRowsFromTable(getModel());
				phenotypeInfoPanel.getTableOutput().revalidate();
				if (results.size() == 0) {
					getProgressBar().setValue(100);
					phenotypeInfoPanel.getProgressBarValue().setText("Progress : " + 100 + " %");
				}
				for (int rowCounter = 0; rowCounter < results.size(); rowCounter++) {
					addRowToTable(results.get(rowCounter), getModel(), rowCounter, false);
					int currentProgress = (rowCounter + 1) * 100 / results.size();
					getProgressBar().setValue(currentProgress);
					phenotypeInfoPanel.getProgressBarValue().setText("Progress : " + currentProgress + " %");
					phenotypeInfoPanel.getTableOutput().revalidate();
					phenotypeInfoPanel.repaint();
				}
			}else{
				getProgressBar().setValue(0);
				phenotypeInfoPanel.getProgressBarValue().setText("Progress : " + 0 + " %");
			}

		} catch (Exception ex) {
			System.out.println("??" + ex.toString());
		}
		return getModel();
	}

}
