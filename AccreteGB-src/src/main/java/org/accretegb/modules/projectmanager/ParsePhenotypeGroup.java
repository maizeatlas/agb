package org.accretegb.modules.projectmanager;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.constants.JsonConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.hibernate.dao.PMProjectDAO;
import org.accretegb.modules.hibernate.dao.PhenotypeGroupDAO;
import org.accretegb.modules.phenotype.Phenotype;
import org.accretegb.modules.phenotype.PhenotypeInfoPanel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * save information of phenotype module to Json
 * parse Json for phenotype module retrieved from database
 * @author Ningjing
 *
 */
public class ParsePhenotypeGroup {
	private static final long serialVersionUID = 2L;
	public int projectId;
	
	public ParsePhenotypeGroup(int projectId){
		this.projectId = projectId;
	}
	
	
	public List<Object[]> getExportTagsTable(String exportTableJson, String groupName) {
		Phenotype phenotypePanel = (Phenotype) getContext().getBean("Phenotype - " + projectId + groupName);
		PhenotypeInfoPanel phenotyInfoPanel = (PhenotypeInfoPanel) getContext().getBean("phenotypeInfoChildPanel0");
		CheckBoxIndexColumnTable exportTable = phenotypePanel.getPhenotypeExportPanel().getPhenotypeTagsTablePanel().getTable();
		LinkedHashMap<String, String> subsetCommentMap = new LinkedHashMap<String, String>();
		List<Object[]> exportTagsRows  = new ArrayList<Object[]>();
		try {
			
			//get all table
			JSONObject json = new JSONObject(exportTableJson);
			JSONArray items = json.getJSONArray("all");
			String allcomment = (String) json.get("allcomment");
			ArrayList<String> tagnames = new ArrayList<String>();
			
			for (int i = 0; i < items.length(); i++) {
				JSONObject tableViewJSON = items.getJSONObject(i);
				Object[] oneRow = new Object[exportTable.getColumnCount()];
				for(int objectIndex = 0; objectIndex < exportTable.getColumnCount(); ++objectIndex){
					Object object = tableViewJSON.get(exportTable.getColumnName(objectIndex));
					if( exportTable.getColumnName(objectIndex).equals("Planting Date")){
						DateFormat dateFormatRead = new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy",Locale.getDefault());		
			    		Date date = null;
			    		try {
			    			date = new Date(dateFormatRead.parse(String.valueOf(object)).getTime());
			    		} catch (ParseException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		}
			    		oneRow[objectIndex]= date;
					}else{
						if(String.valueOf(object).equalsIgnoreCase(JsonConstants.SPACE)){
							oneRow[objectIndex] = null;
						}else{
							oneRow[objectIndex] = object;	
						
						}	
					}
														
				}
				exportTagsRows.add(oneRow);
				tagnames.add(String.valueOf(oneRow[8]));
					
			}
			String boxItem = PMProjectDAO.getInstance().findProjectName(projectId).getProjectName()+"-"+groupName+"-"+"all";
			phenotyInfoPanel.subsets.put(boxItem, tagnames);	
			phenotyInfoPanel.subsetListComboBox.addItem(boxItem);
			
			//get comment for all
			if(!allcomment.equals(JsonConstants.SPACE)){
				phenotypePanel.getPhenotypeExportPanel().getCommentField().setText(allcomment);
				subsetCommentMap.put("All", allcomment);
			}
			
			//get subset tables and comment for subset tables
		    JSONObject exportTableSubsetObject = json.getJSONObject("subset");
		    Iterator iter = exportTableSubsetObject.keys(); 
		    while(iter.hasNext()){
		    	String subsetName = iter.next().toString();
		    	if(subsetName.contains(ColumnConstants.COMMENT)){
		    		String comment = exportTableSubsetObject.getString(subsetName).equals(JsonConstants.SPACE) ? null : exportTableSubsetObject.getString(subsetName);
		    		subsetCommentMap.put(subsetName.replace(ColumnConstants.COMMENT, ""),comment);
		    	}else{
			    	JSONArray subsetJsonArray= exportTableSubsetObject.getJSONArray(subsetName);
			    	int rowCount = subsetJsonArray.length();
			    	int colCount = exportTable.getColumnCount();
			    	Object[][] subsetTable = new Object[rowCount][colCount];
			    	ArrayList<String> subsetTagnames = new ArrayList<String>();
			    	for (int row = 0; row < rowCount; row++) {
			    		JSONObject rowJson = subsetJsonArray.getJSONObject(row);
						for(int col = 0; col < exportTable.getColumnCount(); ++col){
							
							Object object = rowJson.get(exportTable.getColumnName(col));
							if( exportTable.getColumnName(col).equals("Planting Date")){
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
						subsetTagnames.add(String.valueOf(subsetTable[row][8]));
					}
			    	phenotypePanel.getPhenotypeExportPanel().getPhenotypeTagsTablePanel().getTableSubset().addItem(subsetName);
			    	phenotypePanel.getPhenotypeExportPanel().getSubsetTableMap().put(subsetName, subsetTable);
			    	boxItem = PMProjectDAO.getInstance().findProjectName(projectId).getProjectName()+"-"+groupName+"-"+subsetName;
			    	phenotyInfoPanel.subsets.put(boxItem, subsetTagnames);
			    	phenotyInfoPanel.subsetListComboBox.addItem(boxItem);
			    	phenotypePanel.getPhenotypeExportPanel().setSubsetCommentMap(subsetCommentMap);
			    	
			    	
		    	}		    	
		    }
		    
		    
		    //get Jlist content		    
		    JSONObject jlistSubsetObject = json.getJSONObject("jlist");
		    Iterator iterJlist = jlistSubsetObject.keys(); 
		    while(iterJlist.hasNext()){
		    	String subsetName = iterJlist.next().toString();
		    	JSONArray jlistArrays =  (JSONArray) jlistSubsetObject.get(subsetName);
		    	JSONArray des =  (JSONArray)jlistArrays.get(0);
				JSONArray para =  (JSONArray)jlistArrays.get(1);
				List<List<String>> JlistSelection = new ArrayList<List<String>>();
				List<String> desList = new ArrayList<String> ();
				for (int i = 0; i < des.length(); i++) {
					desList.add(des.get(i).toString());
				}
				
				List<String> paraList = new ArrayList<String> ();
				for (int i = 0; i < para.length(); i++) {
					paraList.add(para.get(i).toString());
				}
				JlistSelection.add(desList);
				JlistSelection.add(paraList);
				phenotypePanel.getPhenotypeExportPanel().getSubsetJlistMap().put(subsetName.replace("jlist", ""), JlistSelection);
			
		    }
		    
		    if(!jlistSubsetObject.isNull("Alljlist"))
		    {
		    	// populate Jlist for all
		    	JSONArray all =  (JSONArray) jlistSubsetObject.get("Alljlist");
				JSONArray des =  (JSONArray)all.get(0);
				JSONArray para =  (JSONArray)all.get(1);
				DefaultListModel deslistModel = (DefaultListModel)phenotypePanel.getPhenotypeExportPanel().getDescriptorsSelected().getModel();
			    for (int i = 0; i < des.length(); i++) {
			    	deslistModel.addElement(des.get(i));
				}
			   
			    DefaultListModel paralistModel = (DefaultListModel)phenotypePanel.getPhenotypeExportPanel().getParameterSelected().getModel();
				for (int i = 0; i < para.length(); i++) {
					paralistModel.addElement(para.get(i));
				}
		    }
		   
			
			
		   
		   
		    
		  } catch (Exception e) {
			  System.out.println(e.toString());
		}
		return exportTagsRows;
	}
	
	public  List<Object[]>  getImportTable(String importTableJson, String groupName){
		Phenotype phenotypePanel = (Phenotype) getContext().getBean("Phenotype - " + projectId + groupName);
		JTable importTable = phenotypePanel.getPhenotypeImportPanel().getCurrentTable();
		List<Object[]> importTableRows = new ArrayList<Object[]>();
		try {
			
			JSONObject json = new JSONObject(importTableJson);
			JSONArray items = json.getJSONArray("import");
			JSONArray cols = json.getJSONArray("importcols");
			
			ArrayList<String> colnames = new ArrayList<String>();
			for (int i = 0; i < cols.length(); i++) {
		    	String colName = cols.getString(i);
		    	colnames.add(colName);
			}
			for (int i = 0; i < items.length(); i++) {
				JSONObject importTableJSONObject = items.getJSONObject(i); 
				Object[] oneRow = new Object[colnames.size()];
				int colIndex = -1;
			    for(String colname : colnames){
			    	colIndex++;
			    	oneRow[colIndex] = importTableJSONObject.get(colname).equals(JsonConstants.SPACE) ? null :importTableJSONObject.get(colname) ;
			    }
			    importTableRows.add(oneRow);
			}
			
			String[] colIndentifiers = new String[colnames.size()];
			colIndentifiers = colnames.toArray(colIndentifiers);
			((DefaultTableModel) importTable.getModel()).setColumnIdentifiers(colIndentifiers);
			
		  } catch (Exception e) {
			  System.out.println(e.toString());
		}
		return importTableRows;
	}
	public void saveTables(String groupName){
		Phenotype phenotypePanel = (Phenotype) getContext().getBean("Phenotype - " + projectId + groupName);
		CheckBoxIndexColumnTable exportTable = phenotypePanel.getPhenotypeExportPanel().getPhenotypeTagsTablePanel().getTable();			
		
		//save export panel table
		JSONArray exportTableAllArray = new JSONArray();		  		
		if(phenotypePanel.getPhenotypeExportPanel().getTableData() != null){
			int rows = phenotypePanel.getPhenotypeExportPanel().getTableData().size();
			int cols = ((Vector)phenotypePanel.getPhenotypeExportPanel().getTableData().elementAt(0)).size();
			for(int row = 0; row < rows; row ++){
				Object[] newRow = new Object[cols -1];
				JSONObject jsonRow = new JSONObject();
				for(int col = 0 ; col < (cols -1 ); ++col){
					if(col < 29){//remove "modified"
						newRow[col] = ((Vector)phenotypePanel.getPhenotypeExportPanel().getTableData().elementAt(row)).elementAt(col);						
					}else{
						newRow[col] = ((Vector)phenotypePanel.getPhenotypeExportPanel().getTableData().elementAt(row)).elementAt(col+1);	
					}
					jsonRow.put(exportTable.getColumnName(col), newRow[col] == null ? JsonConstants.SPACE : newRow[col]);					
				}
				 exportTableAllArray.put(jsonRow);
			}
		}
		
		//save subset tables
		LinkedHashMap<String, Object[][]> subsetTableMap = phenotypePanel.getPhenotypeExportPanel().getSubsetTableMap();
		LinkedHashMap<String, String> subsetCommentMap = phenotypePanel.getPhenotypeExportPanel().getSubsetCommentMap();
	    Iterator iter = subsetTableMap.entrySet().iterator();
	    JSONObject exportTableSubsetObject = new JSONObject();	    
		while (iter.hasNext()) {
			Map.Entry entry = (Entry) iter.next();
			String subsetName = (String) entry.getKey();
			Object[][] subset = (Object[][]) entry.getValue();
			JSONArray exportTableSubsetArray = new JSONArray();	
			int rowCount = subset.length;
			int colCount = subset[0].length;
			for(int row = 0; row < rowCount; row ++){
				JSONObject jsonRow = new JSONObject();
				for(int col = 0 ; col < colCount; ++col){					
					jsonRow.put(exportTable.getColumnName(col), subset[row][col] == null ? JsonConstants.SPACE : subset[row][col]);					
				}
				exportTableSubsetArray.put(jsonRow);
			}
			
			exportTableSubsetObject.put(subsetName, exportTableSubsetArray);			
			exportTableSubsetObject.put(subsetName+ColumnConstants.COMMENT, subsetCommentMap.get(subsetName) == null ? JsonConstants.SPACE :subsetCommentMap.get(subsetName));
		}
		
		//save Jlist selection
		LinkedHashMap<String, List<List<String>>> subsetJlistMap = phenotypePanel.getPhenotypeExportPanel().getSubsetJlistMap();
		Iterator iterJlist = subsetJlistMap.entrySet().iterator();
	    JSONObject jlistSubsetObject = new JSONObject();	    
		while (iterJlist.hasNext()) {
			Map.Entry entry = (Entry) iterJlist.next();
			String subsetName = (String) entry.getKey();
			List<List<String>> subsetJlist = (List<List<String>>) entry.getValue();
			jlistSubsetObject.put(subsetName+"jlist",subsetJlist);
		}	    
	    		
		//import table
		JTable importTable = phenotypePanel.getPhenotypeImportPanel().getCurrentTable();
		JSONArray importTableArray = new JSONArray();	
		JSONArray colNamesArray = new JSONArray();
		for(int row = 0; row < importTable.getRowCount();++row){
			JSONObject jsonRow = new JSONObject();
		    for(int col = 0; col < importTable.getColumnCount(); ++col)
		    {
		    	try {
		    		if(row == 0 ){
		    			colNamesArray.put(importTable.getColumnName(col));
		    		}		    		
		    		jsonRow.put(importTable.getColumnName(col), importTable.getValueAt(row, col) == null ? JsonConstants.SPACE : importTable.getValueAt(row, col));
				} catch (JSONException e) {
					e.printStackTrace();
				} 
			  }
		    importTableArray.put(jsonRow);
		 }		  
		
		JSONObject importTableMainObject = new JSONObject();		 
		JSONObject exportTableMainObject = new JSONObject();
		if(exportTableAllArray.length() > 0 && importTableArray.length() <=0){
			 try {
				 	exportTableMainObject.put("all", exportTableAllArray);
				 	exportTableMainObject.put("allcomment",subsetCommentMap.get("All") ==null || subsetCommentMap.get("All").equals("")? JsonConstants.SPACE:subsetCommentMap.get("All"));
				 	exportTableMainObject.put("subset", exportTableSubsetObject);
					exportTableMainObject.put("jlist", jlistSubsetObject);
				 	PhenotypeGroupDAO.getInstance().save(projectId, groupName, exportTableMainObject.toString(),null);
				 } catch (JSONException e) {
					e.printStackTrace();
				 }	 
			 
		 }
		if(exportTableAllArray.length() > 0 && importTableArray.length() > 0){
			 try {
				 	exportTableMainObject.put("all", exportTableAllArray);
				 	exportTableMainObject.put("allcomment",subsetCommentMap.get("All") ==null || subsetCommentMap.get("All").equals("")? JsonConstants.SPACE:subsetCommentMap.get("All"));
				 	exportTableMainObject.put("subset", exportTableSubsetObject);
				 	exportTableMainObject.put("jlist", jlistSubsetObject);
				 	importTableMainObject.put("import", importTableArray);
				 	importTableMainObject.put("importcols", colNamesArray);
				 	PhenotypeGroupDAO.getInstance().save(projectId, groupName, exportTableMainObject.toString(),importTableMainObject.toString());
				 } catch (JSONException e) {
					e.printStackTrace();
				 }	
		}
		if(exportTableAllArray.length() <= 0){
			PhenotypeGroupDAO.getInstance().save(projectId, groupName, null,null);
		}
		  		
	}
}
