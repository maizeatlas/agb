package org.accretegb.modules.germplasm.preplantinginfo;

import org.accretegb.modules.util.PopulateTableWorker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;
import static org.accretegb.modules.customswingcomponent.Utils.addRowToTable;
import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;

public class PrePlantingTableWorker extends PopulateTableWorker {

    public PrePlantingTableWorker(DefaultTableModel model, Map<String, String> sqlParameters, JProgressBar progressBar) {
        super(model, sqlParameters, progressBar);
    }

    @Override
    public String createSQLQuery(Map<String, String> sqlParameters) {
        StringBuffer queryBuff = new StringBuffer();
        queryBuff.append("select observation_unit.plot, stock.stock_name, passport.accession_name, "
                + "passport.pedigree, observation_unit.tagname, observation_unit.plant, observation_unit.planting_date, "
                + "observation_unit.delay, observation_unit.harvest_date, observation_unit.purpose from "
                + "field, observation_unit, stock, passport where " + "observation_unit.field_id "
                + "= field.field_id and observation_unit.stock_id = stock.stock_id and "
                + "stock.passport_id=passport.passport_id");

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
	                  	
	        if (sqlParameters.get("planting_date") != null) {
	            String range[] = sqlParameters.get("planting_date").split(":");
	            if (!range[0].equalsIgnoreCase("0")) {
	                queryBuff.append(" and observation_unit.planting_date >='" + range[0] + "'");
	            }
	            if (!range[1].equalsIgnoreCase("0")) {
	                queryBuff.append(" and observation_unit.planting_date <='" + range[1] + "'");
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
	
	        if (sqlParameters.get("plant") == null) {
	            queryBuff.append(" and observation_unit.plant = 0000000");
	        }
	        queryBuff.append(" LIMIT 5000");
	        return queryBuff.toString();
        }
        return null;
    }

    @Override
    protected DefaultTableModel doInBackground() throws Exception {
        try {
        	  PrePlantingInfoPanel prePlantingInfoPanel = (PrePlantingInfoPanel) getContext().getBean(
                      "preplantingInfoChildPanel0");  
        	List<Object[]> results = createQueryResults(getSqlParameters()); 
        	if(results != null)
        	{
        		removeAllRowsFromTable(getModel());      		                          
                prePlantingInfoPanel.getTableOutput().revalidate();
                if ( results.size() == 0) {
                    getProgressBar().setValue(100);
                    prePlantingInfoPanel.getProgressBarValue().setText("Progress : " + 100 + " %");
                }
                for (int rowCounter = 0; rowCounter < results.size(); rowCounter++) {
                    addRowToTable(results.get(rowCounter), getModel(), rowCounter, false);
                    int currentProgress = (rowCounter + 1) * 100 / results.size();
                    getProgressBar().setValue(currentProgress);
                    prePlantingInfoPanel.getProgressBarValue().setText("Progress : " + currentProgress + " %");
                    prePlantingInfoPanel.getTableOutput().revalidate();
                    prePlantingInfoPanel.repaint();
                }
        	}else{
        		getProgressBar().setValue(0);
                prePlantingInfoPanel.getProgressBarValue().setText("Progress : " + 0 + " %");
        	}
            
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return getModel();
    }

}
