package org.accretegb.modules.projectmanager;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.constants.JsonConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.PlotIndexColumnTable;
import org.accretegb.modules.germplasm.planting.MapView;
import org.accretegb.modules.germplasm.planting.Planting;
import org.accretegb.modules.germplasm.planting.PlantingRow;
import org.accretegb.modules.hibernate.dao.PlantingGroupDAO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * save information of planting module to Json
 * parse Json for planting module retrieved from database
 * @author Ningjing
 *
 */
public class ParsePlantingGroup implements Serializable {
	private static final long serialVersionUID = 2L;
	public int projectId;
	public Object fieldId;
	public boolean synced = false;
	public boolean prefixIsFixed = false;
	
	public ParsePlantingGroup(int projectId){
		this.projectId = projectId;
	}

	public List<Object[]> getTableViewTable(String tableViewJson, String groupName) {
		System.out.println( projectId + groupName);
		Planting plantingPanel = (Planting) getContext().getBean("Planting - " + projectId + groupName);
		PlotIndexColumnTable table = plantingPanel.getTableView().getStocksOrderPanel().getTable();	
		List<Object[]> tableView  = new ArrayList<Object[]>();
		try {
			
			JSONObject json = new JSONObject(tableViewJson);
			fieldId = json.get("fieldId");
		    if(json.has("synced")){
		    	synced = json.getBoolean("synced");
		    }
		    if(json.has("prefixIsFixed")){
		    	prefixIsFixed = json.getBoolean("prefixIsFixed");
		    }
		   
			if(json.has("mapView")){
				JSONObject mapView = json.getJSONObject("mapView");
				plantingPanel.getMapView().getNumRows().setText(mapView.getString("rows"));	
				plantingPanel.getMapView().getNumCols().setText(mapView.getString("cols"));	
			}
			
			JSONArray items = json.getJSONArray("tableView");
			for (int i = 0; i < items.length(); i++) {
				JSONObject tableViewJSON = items.getJSONObject(i);
				Object[] oneRow = new Object[table.getColumnCount()+1];
				for(int objectIndex = 0; objectIndex < table.getColumnCount(); ++objectIndex){
					Object object = tableViewJSON.get(table.getColumnName(objectIndex));
					if(String.valueOf(object).equalsIgnoreCase(JsonConstants.SPACE)){
						oneRow[objectIndex] = null;
					}else{
						oneRow[objectIndex] = object;						
					}										
				}
				
				oneRow[table.getColumnCount()] = tableViewJSON.get("color");
				tableView.add(oneRow);
				
			}
		  } catch (Exception e) {
			  System.out.println(e.getMessage());
		}
		return tableView;
	}
	
	
	public List<Object[]> getTagGeneratorTable(String tagGeneratorJson, String groupName) {
		Planting plantingPanel = (Planting) getContext().getBean("Planting - " + projectId + groupName);
		CheckBoxIndexColumnTable table = plantingPanel.getTagGenerator().getTagsTablePanel().getTable();
		List<Object[]> tagGenerator = new ArrayList<Object[]>();
		try {
			
			JSONObject json = new JSONObject(tagGeneratorJson);
			JSONArray items = json.getJSONArray("tagGenerator");
			for (int i = 0; i < items.length(); i++) {
				JSONObject tagGeneratorJSON = items.getJSONObject(i);
				Object[] oneRow = new Object[table.getColumnCount()];
				for(int objectIndex = 0; objectIndex < table.getColumnCount(); ++objectIndex){
					Object object = tagGeneratorJSON.get(table.getColumnName(objectIndex));
					if(String.valueOf(object).equalsIgnoreCase(JsonConstants.SPACE)){
						oneRow[objectIndex] = null;
					}else{
						oneRow[objectIndex] = object;	
					
					}										
				}
				
				tagGenerator.add(oneRow);
			}
		  } catch (Exception e) {
			  System.out.println(e.getMessage());
		}
		return tagGenerator;
	}
	
	public void saveTables(String groupName){
		Planting plantingPanel = (Planting) getContext().getBean("Planting - " + projectId + groupName);
		PlotIndexColumnTable tableviewTable = plantingPanel.getTableView().getStocksOrderPanel().getTable();		
		JSONArray tableViewArray = new JSONArray();		 
		for(int row = 0; row < tableviewTable.getRowCount();++row){
			JSONObject jsonRow = new JSONObject();
		    for(int col = 0; col < tableviewTable.getColumnCount(); ++col)
		    {
		    	try {
					jsonRow.put(tableviewTable.getColumnName(col), tableviewTable.getValueAt(row, col) == null ? JsonConstants.SPACE : tableviewTable.getValueAt(row, col));
				} catch (JSONException e) {
					e.printStackTrace();
				} 
			}
		    Color color = null;
		    for(PlantingRow plantRow : plantingPanel.getListOfStocks()){
		    	if(plantRow.getTag().equals(tableviewTable.getValueAt(row, ColumnConstants.TAG))){
		    		color = plantRow.getPlotColor();
		    		jsonRow.put("color", Integer.toString(color.getRGB()));
		    		break;
		    	}
		    }
		    if(color == null)
		    {
		    	jsonRow.put("color", "null");
		    }
		    tableViewArray.put(jsonRow);
		 }
		
		CheckBoxIndexColumnTable tagGeneratorTable = plantingPanel.getTagGenerator().getTagsTablePanel().getTable();
		JSONArray tagGenertorArray = new JSONArray();
		  
		for(int row = 0; row < tagGeneratorTable.getRowCount();++row){
			JSONObject jsonRow = new JSONObject();
		    for(int col = 0; col < tagGeneratorTable.getColumnCount(); ++col)
		    {
		    	try {
					jsonRow.put(tagGeneratorTable.getColumnName(col), tagGeneratorTable.getValueAt(row, col) == null ? JsonConstants.SPACE : tagGeneratorTable.getValueAt(row, col));
				} catch (JSONException e) {
					e.printStackTrace();
				} 
			  }
		    tagGenertorArray.put(jsonRow);
		 }		  
		
		 MapView mapview = plantingPanel.getMapView();
		 JSONObject mapViewObject = null;
		 if(!String.valueOf(mapview.getNumRows().getText()).equals("null")
				 && !String.valueOf(mapview.getNumCols().getText()).equals("null")){
			 mapViewObject = new JSONObject();
			 mapViewObject.put("rows", String.valueOf(mapview.getNumRows().getText()));
			 mapViewObject.put("cols", String.valueOf(mapview.getNumCols().getText()));
		 }
		
		 JSONObject tableViewMainObject = new JSONObject();
		 JSONObject tagGenertorMainObject = new JSONObject();
		 
		 String tableViewJson = null;
		 String tagGeneratorJson = null;
		 if(tableViewArray.length() > 0){
			 if(mapViewObject!=null){
				 tableViewMainObject.put("mapView", mapViewObject) ;
			 }
			 tableViewMainObject.put("tableView", tableViewArray);
			 tableViewMainObject.put("fieldId", String.valueOf(plantingPanel.getFieldSelection().getFieldId()));
			 tableViewMainObject.put("synced", plantingPanel.getTableView().synced);
			 tableViewMainObject.put("prefixIsFixed", plantingPanel.getTableView().prefixIsFixed);
			 
			 tableViewJson = tableViewMainObject.toString();
		 }
		 
		 if(tagGenertorArray.length() > 0){
			 tagGenertorMainObject.put("tagGenerator", tagGenertorArray);
			 tagGeneratorJson = tagGenertorMainObject.toString();
		 } 
         PlantingGroupDAO.getInstance().save(projectId, groupName, tableViewJson,tagGeneratorJson);
		 
	}

}
