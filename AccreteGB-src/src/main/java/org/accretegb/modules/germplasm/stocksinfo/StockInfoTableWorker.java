package org.accretegb.modules.germplasm.stocksinfo;

import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;

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


import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.util.PopulateTableWorker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;
import static org.accretegb.modules.customswingcomponent.Utils.addRowToTable;
import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;

/**
 * @author nkumar
 * This file is used to run worker process to populate the Stock Info table
 * It creates a SQL Query first from the parameters entered by the user,
 * gets results by running the query and populate table in the background.
 */
public class StockInfoTableWorker extends PopulateTableWorker {
	
	public StocksInfoPanel stocksInfoPanel;
	
    public StockInfoTableWorker(DefaultTableModel model,StocksInfoPanel stocksInfoPanel, Map<String, String> sqlParameters,
    		JProgressBar progressBar) {
        super(model, sqlParameters, progressBar);
        this.stocksInfoPanel = stocksInfoPanel;
    }

    /**
     * it creates the SQL statement from the input parameters
     * @param sqlParameters - a map of key being column name and value is value entered by user
     * @return a SQL statement
     */
    @Override
    public String createSQLQuery(Map<String, String> sqlParameters) {
        StringBuffer queryBuff = new StringBuffer();
        
        
        
        queryBuff.append("select distinct 1, stock.stock_id, stock.stock_name, accession_name, accession_identifier, pedigree, generation, cycle,"
        		+ " classification.classification_code, population, stock.stock_date,");
        queryBuff.append("( SELECT  COUNT(*) FROM stock_packet stock_packet2 "
        		+ "WHERE stock.stock_id = stock_packet2.stock_id ) as total_pkts,"
        		+ "packet_no, weight, no_seed, tier1_position, tier2_position, tier3_position, "
        		+ "shelf, unit, room, building, location_name, city, state_province, country, stock2.stock_name as parent_stock_name "
        		+ "from stock "
        		+ "left join passport on stock.passport_id = passport.passport_id "
        		+ "left join classification on passport.classification_id = classification.classification_id "
        		+ "left join stock_generation on stock.stock_generation_id = stock_generation.stock_generation_id "
        		+ "left join source on passport.source_id = source.source_id "
        		+ "left join taxonomy on passport.taxonomy_id = taxonomy.taxonomy_id  "
        		+ "left join stock_packet on stock.stock_id = stock_packet.stock_id  "
        		+ "left join stock_packet_container on stock_packet_container.stock_packet_container_id = stock_packet.stock_packet_container_id  "
        		+ "left join container_location on container_location.container_location_id = stock_packet_container.container_location_id  "
        		+ "left join location on location.location_id = container_location.location_id "
        		+ "left join stock_composition on stock_composition.stock_id = stock.stock_id "
        		+ "left join mate_method_connect on stock_composition.mate_method_connect_id = mate_method_connect.mate_method_connect_id "
        		+ "left join mate on mate.mate_id = mate_method_connect.mate_id "
        		+ "left join observation_unit on observation_unit.observation_unit_id  = stock_composition.observation_unit_id "
        		+ "left join stock as stock2 on stock2.stock_id = observation_unit.stock_id "
        		
        		+ "where 1");
        if (sqlParameters.get("stock_name") != null) {
        	String stocknameInput = sqlParameters.get("stock_name").replace("*", "%");
            queryBuff.append(" and stock.stock_name like '" + stocknameInput+ "'");
        }
        
        if (sqlParameters.get("zipcode") != null) {
        	queryBuff = new StringBuffer();
        	queryBuff.append("select distinct 1, stock.stock_id, stock.stock_name, accession_name, accession_identifier, pedigree, generation, cycle, "
        			+ "classification.classification_code, population, stock.stock_date, ");
        	queryBuff.append("( SELECT  COUNT(*) FROM stock_packet stock_packet2 "
            		+ "WHERE stock.stock_id = stock_packet2.stock_id ) as total_pkts,"
        			+ "packet_no, weight, no_seed, tier1_position, tier2_position, tier3_position, "
        			+ "shelf, unit, room, building, location_name, city, state_province, country, stock2.stock_name as parent_stock_name "
            		+ "from stock "
            		+ "left join passport on stock.passport_id = passport.passport_id "
            		+ "left join classification on passport.classification_id = classification.classification_id "
            		+ "left join stock_generation on stock.stock_generation_id = stock_generation.stock_generation_id "
            		+ "left join source on passport.source_id = source.source_id "
            		+ "left join taxonomy on passport.taxonomy_id = taxonomy.taxonomy_id  "
            		+ "left join stock_packet on stock.stock_id = stock_packet.stock_id  "
            		+ "left join stock_packet_container on stock_packet_container.stock_packet_container_id = stock_packet.stock_packet_container_id  "
            		+ "left join container_location on container_location.container_location_id = stock_packet_container.container_location_id  "
            		+ "left join observation_unit on observation_unit.stock_id = stock.stock_id "
            		+ "left join field on field.field_id = observation_unit.field_id "
            		+ "left join location on location.location_id = field.location_id "
            		+ "left join stock_composition on stock_composition.stock_id = stock.stock_id "
            		+ "left join mate_method_connect on stock_composition.mate_method_connect_id = mate_method_connect.mate_method_connect_id "
            		+ "left join mate on mate.mate_id = mate_method_connect.mate_id "
            		+ "left join observation_unit as observation_unit2 on observation_unit2.observation_unit_id  = stock_composition.observation_unit_id "
            		+ "left join stock as stock2 on stock2.stock_id = observation_unit2.stock_id "
            		+ "where 1"); 
            queryBuff.append(" and zipcode = '" + sqlParameters.get("zipcode") + "'");
        }
        
        if (sqlParameters.get("stock_date") != null) {
            String range[] = sqlParameters.get("stock_date").split(":");
            if (!range[0].equalsIgnoreCase("0")) {
                queryBuff.append(" and stock.stock_date >='" + range[0] + "'");
            }
            if (!range[1].equalsIgnoreCase("0")) {
                queryBuff.append(" and stock.stock_date <='" + range[1] + "'");
            }
        }
        if (sqlParameters.get("accession_name") != null) {
        	String accessionInput = sqlParameters.get("accession_name").replace("*", "%");
            queryBuff.append(" and accession_name like '" + accessionInput + "'");
        }
        if (sqlParameters.get("pedigree") != null) {
        	String pedigreeInput = sqlParameters.get("pedigree").replace("*", "%");
            queryBuff.append(" and pedigree like '" + pedigreeInput + "'");
        }
        if (sqlParameters.get("person_name") != null) {
        	String personInput = sqlParameters.get("person_name").replace("*", "%");
            queryBuff.append(" and person_name like '" + personInput + "'");
        }
        if (sqlParameters.get("generation") != null) {
        	String generarionInput = sqlParameters.get("generation").replace("*", "%");
            queryBuff.append(" and generation like '" + generarionInput + "'");
        }
        if (sqlParameters.get("cycle") != null) {
        	String cycleInput = sqlParameters.get("cycle").replace("*", "%");
            queryBuff.append(" and cycle like '" + cycleInput + "'");
        }
	    if(sqlParameters.get("population") !=null) {
	    	String populationInput = sqlParameters.get("population").replace("*", "%");
	    	queryBuff.append(" and population like '"+ populationInput +"'");
	    }
	    if(sqlParameters.get("classification_code") != null) {
	    	queryBuff.append(" and classification.classification_code = '"+sqlParameters.get("classification_code")+"'");
        }
	    if (LoggerUtils.isLogEnabled()) {
            LoggerUtils.log(Level.INFO, "Query Prepared for database => " + queryBuff.toString());
        }
	    if(!this.stocksInfoPanel.getShowAllPackets().isSelected())
	    {
	    	queryBuff.append(" group by stock.stock_id");
	    }
	    System.out.println(queryBuff.toString());
        return queryBuff.toString();
    }


    /**
     * runs the SQL query, gets redults and populate the stock info table
     * @return model of stock info table
     * @throws Exception
     */
    @Override
    protected DefaultTableModel doInBackground() throws Exception {
        try {
            List<Object[]> results = createQueryResults(getSqlParameters());
            removeAllRowsFromTable(getModel());
            stocksInfoPanel.getSearchResultsPanel().getTable().revalidate();
            if (LoggerUtils.isLogEnabled()) {
                LoggerUtils.log(Level.INFO, "Query returned " + results.size() + " " + "Results.");
            }
            for (int rowCounter = 0; rowCounter < results.size(); rowCounter++) {
                results.get(rowCounter)[0] = String.valueOf(rowCounter);
                if(!isCancelled()){
                	  addRowToTable(results.get(rowCounter), getModel(), rowCounter, true);
                      stocksInfoPanel.getSearchResultsPanel().getTable().revalidate();
                      stocksInfoPanel.getSearchResultsPanel().repaint();
                }              
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
            System.out.println(ex.toString());
        }
        
        return getModel();
    }
    
    @Override
    protected void done() {
      try {
        super.get();        
        if(stocksInfoPanel.getSearchResultsPanel().getTable().getRowCount() == 0){
        	stocksInfoPanel.getMatchNotFound().setVisible(true);
        	stocksInfoPanel.getSearchResultsPanel().getNumberOfRows().setText(String.valueOf(stocksInfoPanel.getSearchResultsPanel().getTable().getRowCount()));	
            
        }else{
        	stocksInfoPanel.getSearchResultsPanel().getExpandButton().setEnabled(true);
        	stocksInfoPanel.getSearchResultsPanel().getAll().setEnabled(true);
        	stocksInfoPanel.getSearchResultsPanel().getNone().setEnabled(true);
        	stocksInfoPanel.getSearchResultsPanel().getNumberOfRows().setText(String.valueOf(stocksInfoPanel.getSearchResultsPanel().getTable().getRowCount()));	
        }
      } catch (Throwable t) {
        //do something with the exception
      }
    }

}
