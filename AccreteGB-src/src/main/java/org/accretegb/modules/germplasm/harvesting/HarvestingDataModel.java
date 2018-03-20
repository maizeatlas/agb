package org.accretegb.modules.germplasm.harvesting;

import java.util.Date;

import javax.swing.table.TableColumnModel;
import org.accretegb.modules.constants.ColumnConstants;

public class HarvestingDataModel {
	
	private int stockId;
	private String stockName;
	private String packetName;
	private Date stockDate;
	private String comments;
	
	private String accession;
	private String pedigree;
	private String generation;
	private String matingType;
	
	private boolean modified;
	
	private long uniqueId;
	private int packetNumber;
	private Integer quantity;
	
	private static long uniqueIdCounter;

	public int getStockId() {
		return stockId;
	}

	public void setStockId(int stockId) {
		this.stockId = stockId;
	}

	public String getStockName() {
		return stockName;
	}

	public void setStockName(String stockName) {
		this.stockName = stockName;
	}

	public String getPacketName() {
		return packetName;
	}

	public void setPacketName(String packetName) {
		this.packetName = packetName;
	}

	public Date getStockDate() {
		return stockDate;
	}

	public void setStockDate(Date stockDate) {
		this.stockDate = stockDate;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public String getPedigree() {
		return pedigree;
	}

	public void setPedigree(String pedigree) {
		this.pedigree = pedigree;
	}

	public String getGeneration() {
		return generation;
	}

	public void setGeneration(String generation) {
		this.generation = generation;
	}

	public String getMatingType() {
		return matingType;
	}

	public void setMatingType(String matingType) {
		this.matingType = matingType;
	}

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}
	
	public long getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(long uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public HarvestingDataModel(Object[] rowData, TableColumnModel model) {
		uniqueId = uniqueIdCounter++;
		update(rowData, model);
	}
	
	public void update(Object[] rowData, TableColumnModel model) {
		modified = (Boolean) rowData[model.getColumnIndex(ColumnConstants.MODIFIED)];
		if(rowData[model.getColumnIndex(ColumnConstants.PACKET_ID)] != null) {
			stockId = (Integer) rowData[model.getColumnIndex(ColumnConstants.PACKET_ID)];
		} else {
			stockId = -1;
		}
		packetNumber = (Integer) rowData[model.getColumnIndex(ColumnConstants.PACKET_NUMBER)];
		stockName = (String) rowData[model.getColumnIndex(ColumnConstants.STOCK_NAME)];
		packetName = (String) rowData[model.getColumnIndex(ColumnConstants.PACKET_NAME)];
		stockDate = (Date) rowData[model.getColumnIndex(ColumnConstants.DATE)];
		comments = (String) rowData[model.getColumnIndex(ColumnConstants.COMMENT)];
		accession = (String) rowData[model.getColumnIndex(ColumnConstants.ACCESSION)];
		pedigree = (String) rowData[model.getColumnIndex(ColumnConstants.PEDIGREE)];
		quantity = (Integer) rowData[model.getColumnIndex(ColumnConstants.QUANTITY)];
		generation = (String) rowData[model.getColumnIndex(ColumnConstants.GENERATION)];
		matingType = (String) rowData[model.getColumnIndex(ColumnConstants.MATING_TYPE)];
	}
	
	public Object[] getRowData(TableColumnModel model) {
		Object[] rowData = new Object[model.getColumnCount()];
		rowData[model.getColumnIndex(ColumnConstants.MODIFIED)] = modified;
		rowData[model.getColumnIndex(ColumnConstants.PACKET_ID)] = stockId;
		rowData[model.getColumnIndex(ColumnConstants.STOCK_NAME)] = stockName;
		rowData[model.getColumnIndex(ColumnConstants.PACKET_NAME)] = packetName;
		rowData[model.getColumnIndex(ColumnConstants.DATE)] = stockDate;
		rowData[model.getColumnIndex(ColumnConstants.COMMENT)] = comments;
		rowData[model.getColumnIndex(ColumnConstants.ACCESSION)] = accession;
		rowData[model.getColumnIndex(ColumnConstants.PEDIGREE)] = pedigree;
		rowData[model.getColumnIndex(ColumnConstants.QUANTITY)] = quantity;
		rowData[model.getColumnIndex(ColumnConstants.GENERATION)] = generation;
		rowData[model.getColumnIndex(ColumnConstants.MATING_TYPE)] = matingType;		
		rowData[model.getColumnIndex(ColumnConstants.UNIQUE_ID)] = uniqueId;	
		rowData[model.getColumnIndex(ColumnConstants.PACKET_NUMBER)] = packetNumber;
		return rowData;
	}
}
