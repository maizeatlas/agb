package org.accretegb.modules.projectmanager;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

import java.util.ArrayList;
import java.util.List;

import org.accretegb.modules.constants.JsonConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.germplasm.harvesting.Harvesting;
import org.accretegb.modules.hibernate.dao.HarvestingGroupDAO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * save information of harvest module to Json
 * parse Json for harvest module retrieved from database
 * @author Ningjing
 *
 */
public class ParseHarvestGroup {
	
	private static final long serialVersionUID = 2L;
	public int projectId;
	
	public ParseHarvestGroup(int projectId){
		this.projectId = projectId;
	}
	
	public int mateLink;
	public int mixLink;
	
	public List<Object[]> getFieldGeneratedTable(String crossRecordJson,String groupName) {
		Harvesting harvestingPanel = (Harvesting) getContext().getBean("Harvesting - " + projectId + groupName);
        CheckBoxIndexColumnTable table = harvestingPanel.getFieldGenerated().getCrossingTablePanel().getTable();
        List<Object[]> crossRecordRows = new ArrayList<Object[]>();
		try {
			JSONObject crossRecordjson = new JSONObject(crossRecordJson);
			mateLink = crossRecordjson.getInt("mateLink");
			JSONArray crossRecorditems = crossRecordjson.getJSONArray(JsonConstants.CROSS_RECORD);
			for (int i = 0; i < crossRecorditems.length(); i++) {
				JSONObject rowJson = crossRecorditems.getJSONObject(i);
				Object[] oneRow = new Object[table.getColumnCount()];
				for(int objectIndex = 0; objectIndex < table.getColumnCount(); ++objectIndex){
					Object object = rowJson.get(table.getColumnName(objectIndex));
					if(String.valueOf(object).equalsIgnoreCase(JsonConstants.SPACE)){
						oneRow[objectIndex] = null;
					}else{
						oneRow[objectIndex] = object;						
					}
				}
				crossRecordRows.add(oneRow);
			}
		  } catch (Exception e) {
			  System.out.println(e.toString());
		}
		return crossRecordRows;
	}
	
	public List<Object[]> getBulkTable(String bulkJson,String groupName) {
		Harvesting harvestingPanel = (Harvesting) getContext().getBean("Harvesting - " + projectId + groupName);
        CheckBoxIndexColumnTable table = harvestingPanel.getBulk().getBulkTablePanel().getTable();
        List<Object[]> bulkRows = new ArrayList<Object[]>();
		try {
			JSONObject bulkjson = new JSONObject(bulkJson);
			mixLink = bulkjson.getInt("mixLink");
			JSONArray bulkjsonitems = bulkjson.getJSONArray(JsonConstants.BULK);
			for (int i = 0; i < bulkjsonitems.length(); i++) {
				JSONObject rowJson = bulkjsonitems.getJSONObject(i);
				Object[] oneRow = new Object[table.getColumnCount()];
				for(int objectIndex = 0; objectIndex < table.getColumnCount(); ++objectIndex){
					Object object = rowJson.get(table.getColumnName(objectIndex));
					if(String.valueOf(object).equalsIgnoreCase(JsonConstants.SPACE)){
						oneRow[objectIndex] = null;
					}else{
						oneRow[objectIndex] = object;						
					}
				}
				bulkRows.add(oneRow);
			}
		  } catch (Exception e) {
			  System.out.println(e.toString());
		}
		return bulkRows;
	}
	
	public List<Object[]> getStickerTable(String stickerGeneratorJson,String groupName) {
		Harvesting harvestingPanel = (Harvesting) getContext().getBean("Harvesting - " + projectId + groupName);
        CheckBoxIndexColumnTable table = harvestingPanel.getStickerGenerator().getStickerTablePanel().getTable();
        List<Object[]> stickerRows = new ArrayList<Object[]>();
		try {
			JSONObject stickerjson = new JSONObject(stickerGeneratorJson);
			JSONArray stickerjsonitems = stickerjson.getJSONArray("sticker");
			for (int i = 0; i < stickerjsonitems.length(); i++) {
				JSONObject rowJson = stickerjsonitems.getJSONObject(i);
				Object[] oneRow = new Object[table.getColumnCount()];
				for(int objectIndex = 0; objectIndex < table.getColumnCount(); ++objectIndex){
					Object object = rowJson.get(table.getColumnName(objectIndex));
					if(String.valueOf(object).equalsIgnoreCase(JsonConstants.SPACE)){
						oneRow[objectIndex] = null;
					}else{
						oneRow[objectIndex] = object;						
					}
				}
				stickerRows.add(oneRow);
			}
		  } catch (Exception e) {
			  System.out.println(e.toString());
		}
		return stickerRows;
	}
	
	public void saveTables(String groupName){
		Harvesting harvestingPanel = (Harvesting) getContext().getBean("Harvesting - " + projectId + groupName);
        CheckBoxIndexColumnTable crossRecordTable = harvestingPanel.getFieldGenerated().getCrossingTablePanel().getTable();
        CheckBoxIndexColumnTable bulkTable = harvestingPanel.getBulk().getBulkTablePanel().getTable();
        CheckBoxIndexColumnTable stickerTable = harvestingPanel.getStickerGenerator().getStickerTablePanel().getTable();
        //save crossRecord panel table
		JSONArray crossRecordArray = new JSONArray();		  
	    for(int row = 0; row < crossRecordTable.getRowCount();++row){
	    	JSONObject jsonRow = new JSONObject();
	    	for(int col = 0; col < crossRecordTable.getColumnCount(); ++col)
	    	{
	    		try {
	    				jsonRow.put(crossRecordTable.getColumnName(col), crossRecordTable.getValueAt(row, col) == null ? JsonConstants.SPACE : crossRecordTable.getValueAt(row, col));
	    			} catch (JSONException e) {
	    				e.printStackTrace();
	    			} 
	    		}
	    	crossRecordArray.put(jsonRow);
	    	}
	    
	    //save bulk panel table
	    JSONArray bulkArray = new JSONArray();		  
	    for(int row = 0; row < bulkTable.getRowCount();++row){
	    	JSONObject jsonRow = new JSONObject();
	    	for(int col = 0; col < bulkTable.getColumnCount(); ++col)
	    	{
	    		try {
	    				jsonRow.put(bulkTable.getColumnName(col), bulkTable.getValueAt(row, col) == null ? JsonConstants.SPACE : bulkTable.getValueAt(row, col));
	    			} catch (JSONException e) {
	    				e.printStackTrace();
	    			} 
	    		}
	    	bulkArray.put(jsonRow);
	    }
	    
	    //save sticker panel table
	    JSONArray stickerArray = new JSONArray();		  
	    for(int row = 0; row < stickerTable.getRowCount();++row){
	    	JSONObject jsonRow = new JSONObject();
	    	for(int col = 0; col < stickerTable.getColumnCount(); ++col)
	    	{
	    		try {
	    				jsonRow.put(stickerTable.getColumnName(col), stickerTable.getValueAt(row, col) == null ? JsonConstants.SPACE : stickerTable.getValueAt(row, col));
	    			} catch (JSONException e) {
	    				e.printStackTrace();
	    			} 
	    		}
	    	stickerArray.put(jsonRow);
	    }
	    
	    // save mate link id
	    int matelink = harvestingPanel.getFieldGenerated().nextLink;
	    int mixlink = harvestingPanel.getBulk().nextLink;
	    
	    // save mix id
	    JSONObject crossRecordObj = new JSONObject();
	    JSONObject bulkObj = new JSONObject();
	    JSONObject stickerObj = new JSONObject();
	    
	    String crossRecordJson = null;
	    String bulkJson = null;
	    String stickerJson = null;
	    
	    if(crossRecordArray.length() > 0){
	    	crossRecordObj.put(JsonConstants.CROSS_RECORD, crossRecordArray);
	    	crossRecordObj.put("mateLink", matelink);
	    	crossRecordJson = crossRecordObj.toString();
	    }
	    
	    if(bulkArray.length() > 0){
	    	bulkObj.put(JsonConstants.BULK, bulkArray);
	    	bulkObj.put("mixLink", mixlink);
	    	bulkJson = bulkObj.toString();
	    }
	    
	    if(stickerArray.length() > 0){
	    	stickerObj.put("sticker", stickerArray);
	    	stickerJson = stickerObj.toString();
	    }
	    
	    
	    	    
	    HarvestingGroupDAO.getInstance().save(projectId, groupName, crossRecordJson, bulkJson, stickerJson);
	  }
	

}
