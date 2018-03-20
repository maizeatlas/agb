package org.accretegb.modules.projectmanager;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.accretegb.modules.constants.JsonConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.PlotIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TextField;
import org.accretegb.modules.germplasm.experimentaldesign.ExperimentSelectionPanel;
import org.accretegb.modules.hibernate.dao.ExperimentGroupDAO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * save information of experiment module to Json
 * parse Json for experiment module retrieved from database
 * @author Ningjing
 *
 */
public class ParseExperimentGroup implements Serializable {
	private static final long serialVersionUID = 2L;
	public int projectId;
	public String experimentId;
	public String experimentComment;	
	public boolean randomizeButtonStatus;
	public ArrayList<String> expDesignSettings;
	public ParseExperimentGroup(int projectId){
		this.projectId = projectId;
	}

	/**
	 * get the experiment group panel based on the projectid and group name
	 * populate stocklist table of the panel by parsing stockListJson
	 * @param stockListJson
	 * @param groupName
	 * @return
	 */
	public List<Object[]> getStockListTable(String stockListJson, String groupName) {
		ExperimentSelectionPanel experimentSelectionPanel = (ExperimentSelectionPanel) getContext().getBean(
	                "Experiment Design - " + projectId + groupName);
		CheckBoxIndexColumnTable table = experimentSelectionPanel.getExperimentalSelectedStocksPanel().getTable();	
		List<Object[]> stockList  = new ArrayList<Object[]>();
		
		try {			
			JSONObject json = new JSONObject(stockListJson);
			JSONArray items = json.getJSONArray(JsonConstants.STOCKLIST);
			experimentId = (String) json.get(JsonConstants.EXPERIMENT_ID);
			experimentComment = (String) json.get(JsonConstants.EXPERIMENT_COMMENT);
			randomizeButtonStatus = (Boolean) json.get(JsonConstants.RANDOMIZEBUTTONSTATUS);
			for (int i = 0; i < items.length(); i++) {
				JSONObject stockListJSON = items.getJSONObject(i);
				Object[] oneRow = new Object[table.getColumnCount()];
				for(int objectIndex = 0; objectIndex < table.getColumnCount(); ++objectIndex){
					Object object = stockListJSON.get(table.getColumnName(objectIndex));
					if(String.valueOf(object).equalsIgnoreCase(JsonConstants.SPACE)){
						oneRow[objectIndex] = null;
					}else{
						oneRow[objectIndex] = object;	
					
					}										
				}
				stockList.add(oneRow);
			}
		  } catch (Exception e) {
			  System.out.println(e.toString());
		}
		return stockList;
	}
	
	/**
	 * get the experiment group panel based on the projectid and group name
	 * populate experiment result table and experiment design settings data by parsing expResultJson
	 * @param expResultJson
	 * @param groupName
	 * @return
	 */
	public List<Object[]> getExpResultTable(String expResultJson, String groupName) {
		ExperimentSelectionPanel experimentSelectionPanel = (ExperimentSelectionPanel) getContext().getBean(
                "Experiment Design - " + projectId + groupName);		
		List<Object[]> expResultRows = new ArrayList<Object[]>();
		try {
			
			JSONObject json = new JSONObject(expResultJson);
			JSONArray tableItems = json.getJSONArray(JsonConstants.EXP_RESULT);
			Object expDesignTypeIndex = json.get(JsonConstants.DESIGN_INDEX);
			experimentSelectionPanel.getExperimentSelectionBox().setSelectedIndex((Integer)expDesignTypeIndex);			
			JSONArray trtItems = json.getJSONArray(JsonConstants.TREATMENT);
			JScrollPane trtsPane = experimentSelectionPanel.getTreatmentPane();	
			JPanel view = ((JPanel)trtsPane.getViewport().getView());
			int index = -1;
		   	for(Component comp : view.getComponents()){
		   		index ++;
		   		JSONArray trtArray = trtItems.getJSONArray(index);	   		 
		   		if(comp instanceof javax.swing.JPanel){
		   		     int infoIndex = 0;
		   			 for(int i = 0; i < ((Container) comp).getComponents().length; ++i){		   				
		   				 Component subcomp = ((Container) comp).getComponent(i);
		   				 if((i == 0 || i == 1) && subcomp instanceof javax.swing.JComboBox<?>)
		   				 {
		   					((javax.swing.JComboBox<?>) subcomp).getEditor().setItem(trtArray.get(infoIndex));
		   					infoIndex++;		   					 
		   				 }
		   				 else{
		   					 if(subcomp instanceof javax.swing.JComboBox<?>)
		   					 {
		   						 ((javax.swing.JComboBox<?>) subcomp).getEditor().setItem(trtArray.get(trtArray.length()-1));   						 
		   					 }else if(subcomp instanceof JTextField){	   						 
		   						 if(subcomp instanceof TextField && trtArray.get(infoIndex).equals(""))
		   						 { 		   		
		   						 }else{
		   							((JTextField) subcomp).setText((String) trtArray.get(infoIndex));
		   						 }
		   						  infoIndex++;		    				    						 
		   					 }  					 
		   				 }   				 
		   			 }
		   		 }    		 
		   	 }
		   	
			CheckBoxIndexColumnTable table = experimentSelectionPanel.getExperimentalOutputPanel().getTable();
			String designSetting = (String) json.get(JsonConstants.DESIGN_SETTING);
			expDesignSettings = new ArrayList<String>(Arrays.asList(designSetting.split(",")));
			
			for (int i = 0; i < tableItems.length(); i++) {
				JSONObject tagGeneratorJSON = tableItems.getJSONObject(i);
				Object[] oneRow = new Object[table.getModel().getColumnCount()];
				for(int objectIndex = 0; objectIndex < table.getModel().getColumnCount(); ++objectIndex){
					Object object = tagGeneratorJSON.get(table.getColumnName(objectIndex));
					if(String.valueOf(object).equalsIgnoreCase(JsonConstants.SPACE)){
						oneRow[objectIndex] = null;
					}else{
						oneRow[objectIndex] = object;						
					}										
				}				
				expResultRows.add(oneRow);
			}
		  } catch (Exception e) {
			  System.out.println(e.toString());
		}
		return expResultRows;
	}
	
	/**
	 * get the experiment group panel based on the projectid and group name
	 * fetch data from the panel and save it into database
	 * @param groupName
	 */
	public void saveTables(String groupName){
		
		ExperimentSelectionPanel experimentSelectionPanel = (ExperimentSelectionPanel) getContext().getBean(
                "Experiment Design - " + projectId + groupName);
		//save stocklist table and experimentId
	    CheckBoxIndexColumnTable stockListTable = experimentSelectionPanel.getExperimentalSelectedStocksPanel().getTable();		
	    String experimentId = experimentSelectionPanel.getExperimentId();
	    String experimentComment = experimentSelectionPanel.getExpComm().getText() + "-" + String.valueOf(experimentSelectionPanel.getReminderMsg().isVisible());
	    Boolean randomizeButtonStatus = experimentSelectionPanel.getRandomizeButton().isEnabled();
	    JSONArray stockListArray = new JSONArray();		  
		for(int row = 0; row < stockListTable.getRowCount();++row){
			JSONObject jsonRow = new JSONObject();
		    for(int col = 0; col < stockListTable.getColumnCount(); ++col)
		    {
		    	try {
					jsonRow.put(stockListTable.getColumnName(col), stockListTable.getValueAt(row, col) == null ? JsonConstants.SPACE : stockListTable.getValueAt(row, col));
				} catch (JSONException e) {
					e.printStackTrace();
				} 
			  }
		    stockListArray.put(jsonRow);
		 }
		
		//save experiment design settings
		CheckBoxIndexColumnTable expResultTable = experimentSelectionPanel.getExperimentalOutputPanel().getTable();
		int designIndex = experimentSelectionPanel.getExperimentSelectionBox().getSelectedIndex();
		ArrayList<String> expDesignSettings = new ArrayList<String>();
		for(Component comp : experimentSelectionPanel.getDesignPanel().getComponents()){
			if(comp instanceof JTextField){
				expDesignSettings.add(((JTextField) comp).getText());
			}
			if(comp instanceof JComboBox){
				expDesignSettings.add(String.valueOf(((JComboBox) comp).getSelectedIndex()));
			}
		}
		StringBuilder settings = new StringBuilder();
		for(String setting: expDesignSettings){
			settings.append(setting);
			settings.append(",");
		}
		String expDesignSetting = settings.toString().substring(0, settings.length()-1);
		
		//save experiment design results
		JSONArray expResultArray = new JSONArray();       		  
		for(int row = 0; row < expResultTable.getRowCount();++row){
			JSONObject jsonRow = new JSONObject();
		    for(int col = 0; col < expResultTable.getModel().getColumnCount(); ++col)
		    {
		    	try {
					jsonRow.put(expResultTable.getColumnName(col), expResultTable.getValueAt(row, col) == null ? JsonConstants.SPACE : expResultTable.getValueAt(row, col));
				} catch (JSONException e) {
					e.printStackTrace();
				} 
			  }
		    expResultArray.put(jsonRow);
		 }
		
		//save treatment panel settings
		JSONArray treatmentArray = new JSONArray();   
		JScrollPane trtsPane = experimentSelectionPanel.getTreatmentPane();	
		JPanel view = ((JPanel)trtsPane.getViewport().getView());
	   	for(Component comp : view.getComponents()){
	   		 List<String> trtInfo = new ArrayList<String>();	   		 
	   		 if(comp instanceof javax.swing.JPanel){
	   			 for(int i = 0; i < ((Container) comp).getComponents().length; ++i){
	   				 Component subcomp = ((Container) comp).getComponent(i);
	   				 if((i == 0 || i == 1)&& subcomp instanceof javax.swing.JComboBox<?>)
	   				 {
	   					 String name = (String)((javax.swing.JComboBox<?>) subcomp).getEditor().getItem();
	   					 trtInfo.add(name);
	   				 }
	   				 else{
	   					 if(subcomp instanceof javax.swing.JComboBox<?>)
	   					 {
	   						 trtInfo.add((String)((javax.swing.JComboBox<?>) subcomp).getEditor().getItem());
	   					 }else if(subcomp instanceof TextField){
	   						 String input = (String)((TextField) subcomp).getText();
		    				 trtInfo.add(input);    						 
	   					 }   					 
	   				 }   				 
	   			 }
	   			treatmentArray.put(trtInfo);
	   		 }    		 
	   	 }
		
	   	//sync with database
		JSONObject stockListMainObject = new JSONObject();
		JSONObject expResultMainObject = new JSONObject();		 		
		if(stockListArray.length() > 0 && expResultArray.length() > 0){
			 try {
				stockListMainObject.put(JsonConstants.STOCKLIST, stockListArray);
				stockListMainObject.put(JsonConstants.EXPERIMENT_ID,experimentId);
				stockListMainObject.put(JsonConstants.EXPERIMENT_COMMENT,experimentComment);
				stockListMainObject.put(JsonConstants.RANDOMIZEBUTTONSTATUS, randomizeButtonStatus);
				expResultMainObject.put(JsonConstants.EXP_RESULT, expResultArray);
				expResultMainObject.put(JsonConstants.DESIGN_INDEX, designIndex);
				expResultMainObject.put(JsonConstants.DESIGN_SETTING, expDesignSetting);
				expResultMainObject.put(JsonConstants.TREATMENT, treatmentArray);
				ExperimentGroupDAO.getInstance().save(projectId, groupName, stockListMainObject.toString(),expResultMainObject.toString());
			 } catch (JSONException e) {
				e.printStackTrace();
			 }	 
		 }else if(stockListArray.length() > 0  && expResultArray.length() <= 0 ){
			 try {
					stockListMainObject.put(JsonConstants.STOCKLIST, stockListArray);
					stockListMainObject.put(JsonConstants.EXPERIMENT_ID, experimentId);
					stockListMainObject.put(JsonConstants.EXPERIMENT_COMMENT,experimentComment);
					stockListMainObject.put(JsonConstants.RANDOMIZEBUTTONSTATUS, randomizeButtonStatus);
					ExperimentGroupDAO.getInstance().save(projectId, groupName, stockListMainObject.toString(),null);
				 } catch (JSONException e) {
					e.printStackTrace();
				 }	 
			 
		 }else{
			 ExperimentGroupDAO.getInstance().save(projectId, groupName, null,null);
		 }
		  		
	}
}
