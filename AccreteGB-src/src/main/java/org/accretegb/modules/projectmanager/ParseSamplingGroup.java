package org.accretegb.modules.projectmanager;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.constants.JsonConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.hibernate.dao.SamplingGroupDAO;
import org.accretegb.modules.sampling.Sampling;
import org.apache.avro.data.Json;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * save information of Sampling module to Json
 * parse Json for Sampling module retrieved from database
 * @author Ningjing
 *
 */
public class ParseSamplingGroup {
	private static final long serialVersionUID = 2L;
	public int projectId;
	
	public ParseSamplingGroup(int projectId){
		this.projectId = projectId;
	}
	
	
	public void getSamplingSelectionTable(String selectionTableJson, String groupName) {
		Sampling SamplingPanel = (Sampling) getContext().getBean("Sampling - " + projectId + groupName);
		CheckBoxIndexColumnTable sampleSelectionTable = SamplingPanel.getSampleSelectionPanel().getSampleSelectionTablePanel().getTable();
		LinkedHashMap<String, String> subsetCommentMap = new LinkedHashMap<String, String>();
		List<Object[]> selectionTableRows  = new ArrayList<Object[]>();
		
		try {
			
			//get all table
			JSONObject json = new JSONObject(selectionTableJson);
			String allcomment = (String) json.get("allcomment");
			
			//get comment for all
			if(!allcomment.equals(JsonConstants.SPACE)){
				SamplingPanel.getSampleSelectionPanel().getCommentField().setText(allcomment);
				subsetCommentMap.put("All", allcomment);
			}
			
			//get subset tables and comment for subset tables
		    JSONObject selectionTableSubsetObject = json.getJSONObject("subset");
		    Iterator iter = selectionTableSubsetObject.keys(); 
		    while(iter.hasNext()){
		    	String subsetName = iter.next().toString();
		    	if(subsetName.contains(ColumnConstants.COMMENT)){
		    		String comment = selectionTableSubsetObject.getString(subsetName).equals(JsonConstants.SPACE) ? null : selectionTableSubsetObject.getString(subsetName);
		    		subsetCommentMap.put(subsetName.replace(ColumnConstants.COMMENT, ""),comment);
		    	}else{
			    	JSONArray subsetJsonArray= selectionTableSubsetObject.getJSONArray(subsetName);
			    	int rowCount = subsetJsonArray.length();
			    	int colCount = sampleSelectionTable.getColumnCount();
			    	Object[][] subsetTable = new Object[rowCount][colCount];
			    	for (int row = 0; row < rowCount; row++) {
			    		JSONObject rowJson = subsetJsonArray.getJSONObject(row);
						for(int col = 0; col < sampleSelectionTable.getColumnCount(); ++col){
							Object object = rowJson.get(sampleSelectionTable.getColumnName(col));
							if(String.valueOf(object).equalsIgnoreCase(JsonConstants.SPACE)){
								subsetTable[row][col] = null;
							}else{
								subsetTable[row][col] = object;							
							}										
						}
					}
			    	if(!subsetName.equals("All"))
			    	{
			    		SamplingPanel.getSampleSelectionPanel().getSampleSelectionTablePanel().getTableSubset().addItem(subsetName);	
			    	}
			    	SamplingPanel.getSampleSelectionPanel().getSubsetTableMap().put(subsetName, subsetTable);
			    	SamplingPanel.getSampleSelectionPanel().setSubsetCommentMap(subsetCommentMap);
			    	
		    	}		    	
		    }
		    SamplingPanel.getSampleSelectionPanel().populateSubset("All");
		  } catch (Exception e) {
			  System.out.println(e.toString());
		}
	}
	
	public void getSamplingSettingTable(String settingTableJson, String groupName) {
		Sampling SamplingPanel = (Sampling) getContext().getBean("Sampling - " + projectId + groupName);
		CheckBoxIndexColumnTable sampleSettingTable = SamplingPanel.getSampleSettingPanel().getSampleSettingTablePanel().getTable();
		LinkedHashMap<String, String> subsetCommentMap = new LinkedHashMap<String, String>();
		try {
			JSONObject json = new JSONObject(settingTableJson);
			//get subset info
		    JSONObject settingTableSubsetInfoObject = json.getJSONObject("subsetInfo");
		    LinkedHashMap<String, HashMap<String, Object>> subsetInfoMap = new LinkedHashMap<String, HashMap<String, Object>>();
		    Iterator subsetInfoIter = settingTableSubsetInfoObject.keys(); 
		    while(subsetInfoIter.hasNext()){
		    	String subsetName = subsetInfoIter.next().toString();
		    	JSONObject infoObject = (JSONObject) settingTableSubsetInfoObject.get(subsetName);
		    	HashMap<String, Object> map = new HashMap<String, Object>();
		    	Iterator infoObjectIter = infoObject.keys(); 
		    	while(infoObjectIter.hasNext()){
		    		String key = infoObjectIter.next().toString();
			    	if(key.equals("date"))
			    	{
			    		DateFormat dateFormatRead = new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy",Locale.getDefault());		
			    		Date date = null;
			    		try {
			    			date = dateFormatRead.parse(String.valueOf(infoObject.get(key)));
			    		} catch (ParseException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		}
			    		map.put(key, date);
			    	}else{
			    		map.put(key, infoObject.get(key));
			    	}
		    	}
		    	subsetInfoMap.put(subsetName, map);	
		    }
		    SamplingPanel.getSampleSettingPanel().setSubsetInfo(subsetInfoMap);
			
		    //get prefix info 
		    JSONObject prefixIndexObject = json.getJSONObject("prefixIndex");
		    LinkedHashMap<String, Integer> prefixIndex = new LinkedHashMap<String, Integer>();
		    Iterator prefixIndexIter = prefixIndexObject.keys(); 
		    while(prefixIndexIter.hasNext()){
		    	String prefix = prefixIndexIter.next().toString();
		    	prefixIndex.put(prefix, (Integer) prefixIndexObject.get(prefix));
		    }
		    SamplingPanel.getSampleSettingPanel().setPrefixIndex(prefixIndex);
		    
		    
		    //get subset data for subset tables
			JSONObject SettingTableSubsetObject = json.getJSONObject("subset");
		    Iterator iter = SettingTableSubsetObject.keys(); 
		    LinkedHashMap<String, Object[][]> subsetTableMap = new LinkedHashMap<String, Object[][]>();
		    JComboBox subset = SamplingPanel.getSampleSettingPanel().getSampleSettingTablePanel().getTableSubset();
		    while(iter.hasNext()){
		    	String subsetName = iter.next().toString();
		    	if(subsetName.contains(ColumnConstants.COMMENT)){
		    		String comment = SettingTableSubsetObject.getString(subsetName).equals(JsonConstants.SPACE) ? null : SettingTableSubsetObject.getString(subsetName);
		    		subsetCommentMap.put(subsetName.replace(ColumnConstants.COMMENT, ""),comment);
		    	}else{
		    		JSONArray subsetJsonArray= SettingTableSubsetObject.getJSONArray(subsetName);
			    	int rowCount = subsetJsonArray.length();
			    	int colCount = sampleSettingTable.getColumnCount();
			    	Object[][] subsetTable = new Object[rowCount][colCount];
			    	for (int row = 0; row < rowCount; row++) {
			    		JSONObject rowJson = subsetJsonArray.getJSONObject(row);
						for(int col = 0; col < sampleSettingTable.getColumnCount(); ++col){
							Object object = rowJson.get(sampleSettingTable.getColumnName(col));
							if( sampleSettingTable.getColumnName(col).equals("Collection Date") || 
									sampleSettingTable.getColumnName(col).equals("Planting Date")){
								DateFormat dateFormatRead = new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy",Locale.getDefault());		
					    		Date date = null;
					    		try {
					    			date = new Date(dateFormatRead.parse(String.valueOf(object)).getTime());
					    		} catch (ParseException e) {
					    			// TODO Auto-generated catch block
					    			e.printStackTrace();
					    		}
					    		subsetTable[row][col]= date;
							}else{
								
								if(String.valueOf(object).equalsIgnoreCase(JsonConstants.SPACE)){
									subsetTable[row][col] = null;
								}else{
									subsetTable[row][col] = object;							
								}	
							}
																	
						}
			    	}		    	
			    	subset.addItem(subsetName);			    	
			    	subsetTableMap.put(subsetName, subsetTable);			    	
		    	}
		    }
		    Boolean synced = false;
		    if(json.has("synced")){
		    	synced = json.getBoolean("synced");
		    }

		    SamplingPanel.getSampleSettingPanel().setSubsetTableMap(subsetTableMap);
		    SamplingPanel.getSampleSettingPanel().populateSettingSubset("All");
		   
		   		    
		    
		  } catch (Exception e) {
			  System.out.println(e.toString());
		}
	}
	
	public void saveTables(String groupName){
	    SamplingGroupDAO.getInstance().save(projectId, groupName, null,null);
		Sampling SamplingPanel = (Sampling) getContext().getBean("Sampling - " + projectId + groupName);
		CheckBoxIndexColumnTable selectionTable = SamplingPanel.getSampleSelectionPanel().getSampleSelectionTablePanel().getTable();			
		
		//save selection subset tables
		LinkedHashMap<String, Object[][]> selectionSubsetTableMap = SamplingPanel.getSampleSelectionPanel().getSubsetTableMap();
		LinkedHashMap<String, String> selectionSubsetCommentMap = SamplingPanel.getSampleSelectionPanel().getSubsetCommentMap();
	    Iterator iter = selectionSubsetTableMap.entrySet().iterator();
	    JSONObject selectionTableSubsetObject = new JSONObject();	    
		while (iter.hasNext()) {
			Map.Entry entry = (Entry) iter.next();
			String subsetName = (String) entry.getKey();
			Object[][] subset = (Object[][]) entry.getValue();
			JSONArray selectionTableSubsetArray = new JSONArray();
			int rowCount = subset.length;
			int colCount = subset[0].length;
			for(int row = 0; row < rowCount; row++){
				JSONObject jsonRow = new JSONObject();
				for(int col = 0 ; col < colCount && col < selectionTable.getColumnCount(); col++){
					jsonRow.put(selectionTable.getColumnName(col), subset[row][col] == null ? JsonConstants.SPACE : subset[row][col]);					
				}
				selectionTableSubsetArray.put(jsonRow);
			}
			
			selectionTableSubsetObject.put(subsetName, selectionTableSubsetArray);			
			selectionTableSubsetObject.put(subsetName+ColumnConstants.COMMENT, selectionSubsetCommentMap.get(subsetName) == null ? JsonConstants.SPACE :selectionSubsetCommentMap.get(subsetName));
		}
		
		//save setting subset tables
		CheckBoxIndexColumnTable settingTable = SamplingPanel.getSampleSettingPanel().getSampleSettingTablePanel().getTable();
		LinkedHashMap<String, Object[][]> settingSubsetTableMap = SamplingPanel.getSampleSettingPanel().getSubsetTableMap();
		Iterator iter1 = settingSubsetTableMap.entrySet().iterator();
	    JSONObject settingTableSubsetObject = new JSONObject();	    
		while (iter1.hasNext()) {
			Map.Entry entry = (Entry) iter1.next();
			String subsetName = (String) entry.getKey();
			Object[][] subset = (Object[][]) entry.getValue();
			JSONArray settingTableSubsetArray = new JSONArray();	
			int rowCount = subset.length;
			int colCount = subset[0].length;
			for(int row = 0; row < rowCount; row ++){
				JSONObject jsonRow = new JSONObject();
				for(int col = 0 ; col < colCount && col < settingTable.getColumnCount(); ++col){		
					//System.out.print(settingTable.getColumnName(col));
					jsonRow.put(settingTable.getColumnName(col), subset[row][col] == null ? JsonConstants.SPACE : subset[row][col]);					
				}
				settingTableSubsetArray.put(jsonRow);
			}
			settingTableSubsetObject.put(subsetName, settingTableSubsetArray);			
		}
		
		LinkedHashMap<String, HashMap<String, Object>> subsetInfo = SamplingPanel.getSampleSettingPanel().getSubsetInfo();
		Iterator iter2 = subsetInfo.entrySet().iterator();
	    JSONObject settingTableSubsetInfoObject = new JSONObject();	    
		while (iter2.hasNext()) {
			Map.Entry entry = (Entry) iter2.next();
			String subsetName = (String) entry.getKey();
			HashMap<String, Object> info = (HashMap<String, Object>) entry.getValue();
			JSONObject infoObject = new JSONObject();	
			infoObject.put(info.keySet().iterator().next(), info.values().iterator().next());
			settingTableSubsetInfoObject.put(subsetName,info);			
		}
		
		//prefixIndex
		JSONObject prefixIndexObject = new JSONObject();	
		LinkedHashMap<String, Integer> prefixIndex = SamplingPanel.getSampleSettingPanel().getPrefixIndex();
		Iterator iter3 = prefixIndex.entrySet().iterator();    
		while (iter3.hasNext()) {
			Map.Entry entry = (Entry) iter3.next();
			prefixIndexObject.put((String) entry.getKey(),entry.getValue());	
		}
		
		JSONObject selectionTableMainObject = null;		 
		JSONObject settingTableMainObject = null;
		if(selectionTableSubsetObject != null){
			 try {
				 	selectionTableMainObject = new JSONObject();
				 	selectionTableMainObject.put("allcomment",selectionSubsetCommentMap.get("All") ==null 
				 									|| selectionSubsetCommentMap.get("All").equals("")
				 									? JsonConstants.SPACE:selectionSubsetCommentMap.get("All"));
				 	selectionTableMainObject.put("subset", selectionTableSubsetObject);
				 } catch (JSONException e) {
					e.printStackTrace();
				 }	 
			 
		 }
		if(settingTableSubsetObject != null){
			 try {
				 settingTableMainObject = new JSONObject();
				 settingTableMainObject.put("subset", settingTableSubsetObject);
				 settingTableMainObject.put("subsetInfo", settingTableSubsetInfoObject);
				 settingTableMainObject.put("prefixIndex", prefixIndexObject);
				 } catch (JSONException e) {
					e.printStackTrace();
			}		
		}
		SamplingGroupDAO.getInstance().save(projectId, groupName, 
				selectionTableMainObject == null ? null : selectionTableMainObject.toString(), 
				settingTableMainObject == null ? null : settingTableMainObject.toString());
	
	}
	
}
