package org.accretegb.modules.projectmanager;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.germplasm.stocksinfo.StocksInfoPanel;
import org.accretegb.modules.hibernate.dao.StockSelectionGroupDAO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * decode Json retrieved from database for stock selection group
 * endode stock selection group to Json
 * @author Ningjing & Matthew
 *
 */
public class ParseStockSelectionGroup implements Serializable {
	private static final long serialVersionUID = 2L;
	public List<Object[]> cart;
	public int projectId;
	public ParseStockSelectionGroup(int projectId){
		this.projectId = projectId;
	}
	
	public List<Object[]> getCartTable(String cartJson,String groupName) {
		StocksInfoPanel stockInfoPanel = (StocksInfoPanel) getContext().getBean( "Stock Selection - " + projectId+ groupName);
		CheckBoxIndexColumnTable table = stockInfoPanel.getSaveTablePanel().getTable();
		cart = new ArrayList<Object[]>();
		try {
			JSONObject cartjson = new JSONObject(cartJson);
			JSONArray cartitems = cartjson.getJSONArray("cart");
			for (int i = 0; i < cartitems.length(); i++) {
				JSONObject stockSelectionCartRowJSON = cartitems.getJSONObject(i);
				Object[] oneRow = new Object[table.getColumnCount()];
				for(int objectIndex = 0; objectIndex < table.getColumnCount(); ++objectIndex){
					oneRow[objectIndex] = stockSelectionCartRowJSON.get(table.getColumnName(objectIndex));
				}
				cart.add(oneRow);
			}
		  } catch (Exception e) {
			  System.out.println(e.toString());
		}
		return cart;

	}

	public void saveCartTable(String groupName){
		  StocksInfoPanel stockInfoPanel = (StocksInfoPanel) getContext().getBean( "Stock Selection - " + projectId + groupName);
		  CheckBoxIndexColumnTable table = stockInfoPanel.getSaveTablePanel().getTable();
		  JSONArray array = new JSONArray();
		  
		  for(int row = 0; row < table.getRowCount();++row){
			  JSONObject jsonRow = new JSONObject();
			  for(int col = 0; col < table.getColumnCount(); ++col)
			  {
				  try {
					jsonRow.put(table.getColumnName(col), table.getValueAt(row, col));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			  }
			  array.put(jsonRow);
		  }
		  
		  JSONObject mainObj = new JSONObject();
		  mainObj.put("cart", array);
		  StockSelectionGroupDAO.getInstance().save(projectId, groupName, mainObj.toString());
		  
	}
	
	public void addCartRow(Object[] row){
		this.cart.add(row);
	}
}
