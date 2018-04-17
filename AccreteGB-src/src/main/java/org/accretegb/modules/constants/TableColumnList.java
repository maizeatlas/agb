package org.accretegb.modules.constants;

import java.util.ArrayList;
public class TableColumnList {
	
	public static ArrayList<String> STOCK_SELECTION_TABLE_COLUMN_LIST;
	public static ArrayList<String> STOCK_SELECTION_SHOW_COLUMN_LIST;
	
	static {		
		setStockSelectionColumnList();
	}
	
	private static void setStockSelectionColumnList(){
		ArrayList<String> columnNames = new ArrayList<String>();
		columnNames.add(ColumnConstants.SELECT);
		columnNames.add(ColumnConstants.ROW);
		columnNames.add(ColumnConstants.STOCK_ID);
		columnNames.add(ColumnConstants.STOCK_NAME);
		columnNames.add(ColumnConstants.ACCESSION);
		columnNames.add(ColumnConstants.PEDIGREE);
		columnNames.add(ColumnConstants.GENERATION);
		columnNames.add(ColumnConstants.CYCLE);
		columnNames.add(ColumnConstants.CLASSIFICATION_CODE);
		columnNames.add(ColumnConstants.POPULATION);
		columnNames.add(ColumnConstants.STOCK_DATE);
		columnNames.add(ColumnConstants.TOTAL_PACKETS);
		columnNames.add(ColumnConstants.PACKET_NO);
		columnNames.add(ColumnConstants.WEIGHT);
		columnNames.add(ColumnConstants.NUMBER_OF_SEEDS);
		columnNames.add(ColumnConstants.TIER1_POSITION);
		columnNames.add(ColumnConstants.TIER2_POSITION);
		columnNames.add(ColumnConstants.TIER3_POSITION);
		columnNames.add(ColumnConstants.SHELF);
		columnNames.add(ColumnConstants.UNIT);	
		columnNames.add(ColumnConstants.ROOM);
		columnNames.add(ColumnConstants.BUILDING);
		columnNames.add(ColumnConstants.LOCATION_NAME);		
		columnNames.add(ColumnConstants.CITY);
		columnNames.add(ColumnConstants.STATE);
		columnNames.add(ColumnConstants.COUNTRY);
		columnNames.add(ColumnConstants.FEMALE_PARENT_STOCK);
		TableColumnList.STOCK_SELECTION_TABLE_COLUMN_LIST = columnNames;
		
		ArrayList<String> showColumns = new ArrayList<String>();
		showColumns.add(ColumnConstants.SELECT);
		showColumns.add(ColumnConstants.STOCK_NAME);	
		showColumns.add(ColumnConstants.ACCESSION);
		showColumns.add(ColumnConstants.PEDIGREE);
		showColumns.add(ColumnConstants.GENERATION);
		showColumns.add(ColumnConstants.CLASSIFICATION_CODE);
		showColumns.add(ColumnConstants.POPULATION);
		TableColumnList.STOCK_SELECTION_SHOW_COLUMN_LIST = showColumns;
	}

}
