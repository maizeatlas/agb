package org.accretegb.modules.germplasm.planting;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.PlotIndexColumnTable;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.hibernate.MateMethodConnect;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import javax.swing.table.TableColumnModel;

import java.awt.Color;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class PlantingRow {
	
	private static long counter;
	private long uniqueId;
	private int stockId;
	private String type;
	private String row;
	private String plant;
	private String stockName;
	private String accession;
	private String pedigree;
	private String generation;
	private String cycle;
	private String classification_code;
	private String population;
	private Date plantingDate;
	private String kernels;
	private String purpose;
	private String comment;
	private String delay;
	private String rep;
	private int startTag =-1;
	private int endTag = -1;
	private int count = -1;
	private int x;
	private int y;
	private int mateMethodConnectid;
	private String matingPlan;
	private int tagId = -1;
	private String tag;
	private boolean modified;
	private int expId = -1;	
	private String expFactorValueids;
	private static int fillerId = -1;
	private boolean selectedInMapView = false;
	private Color plotColor = Color.white;
	

	

	public PlantingRow getNewInstance() {
        return new PlantingRow(stockId, stockName, accession, pedigree, generation, cycle, classification_code, population);
    }

    public PlantingRow(int stockId, String stockName, String accession, String pedigree, String generation, String cycle,
            String classification_code, String population) {
    	plotColor = Color.white;
    	this.uniqueId = counter++;
        this.stockId = stockId;
        this.stockName = stockName;
        this.accession = accession;
        this.pedigree = pedigree;
        this.generation = generation;
        this.cycle = cycle;
        this.classification_code = classification_code;
        this.population = population;
        this.startTag = 1;
        this.endTag = this.count = 0;
        this.mateMethodConnectid = 0;
        this.x = this.y = -1;
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        DateUtils.truncate(now, Calendar.DATE);
        this.plantingDate = now.getTime();
        this.modified = true;
    }

    public PlantingRow(Object[] rowData, PlotIndexColumnTable table) {
    	plotColor = Color.white;
    	this.uniqueId = counter++;
    	if(rowData[table.getIndexOf(ColumnConstants.STOCK_ID)] != null) {
    		stockId = Integer.parseInt(String.valueOf(rowData[table.getIndexOf(ColumnConstants.STOCK_ID)]));
    	} else {
    		stockId = fillerId --;
    	}
    	type = (String)rowData[table.getIndexOf(ColumnConstants.TYPES)];
    	row = (String)rowData[table.getIndexOf(ColumnConstants.ROW)];
    	plant = (String)rowData[table.getIndexOf(ColumnConstants.PLANT)];
        stockName = (String)rowData[table.getIndexOf(ColumnConstants.STOCK_NAME)];
        accession = (String)rowData[table.getIndexOf(ColumnConstants.ACCESSION)];
        pedigree = (String)rowData[table.getIndexOf(ColumnConstants.PEDIGREE)];
        generation = (String)rowData[table.getIndexOf(ColumnConstants.GENERATION)];
        cycle = (String)rowData[table.getIndexOf(ColumnConstants.CYCLE)];
        classification_code = (String)rowData[table.getIndexOf(ColumnConstants.CLASSIFICATION_CODE)];
        population = (String)rowData[table.getIndexOf(ColumnConstants.POPULATION)];        
        String start = String.valueOf(rowData[table.getIndexOf(ColumnConstants.START_TAG)]);
        startTag = isBlank(start)?-1:Integer.parseInt(start);
        String end = String.valueOf(rowData[table.getIndexOf(ColumnConstants.END_TAG)]);
		endTag = isBlank(end)?-1:Integer.parseInt(String.valueOf(end));
		String count = String.valueOf(rowData[table.getIndexOf(ColumnConstants.COUNT)]);
		this.count = isBlank(count)?-1:Integer.parseInt(count);
		DateFormat dateFormatRead = new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy",Locale.getDefault());		
		Date date = null;
		try {
			if(rowData[table.getIndexOf(ColumnConstants.PLANTING_DATE)] == null){
                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            	date = calendar.getTime();
			}else{
				date = dateFormatRead.parse(String.valueOf(rowData[table.getIndexOf(ColumnConstants.PLANTING_DATE)]));
			}
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		plantingDate = (date instanceof Date)?date:calendar.getTime();
		kernels = (String)rowData[table.getIndexOf(ColumnConstants.KERNELS)];
		delay = (String)rowData[table.getIndexOf(ColumnConstants.DELAY)];
		purpose = (String)rowData[table.getIndexOf(ColumnConstants.PURPOSE)];
		comment = (String)rowData[table.getIndexOf(ColumnConstants.COMMENT)];
		if(!String.valueOf(rowData[table.getIndexOf(ColumnConstants.MATING_PLANT_ID)]).equals("null"))
		{
			mateMethodConnectid = Integer.parseInt(String.valueOf(rowData[table.getIndexOf(ColumnConstants.MATING_PLANT_ID)]));
		}
		matingPlan = (String)rowData[table.getIndexOf(ColumnConstants.MATING_PLAN)];
		
		String stringX = String.valueOf(rowData[table.getIndexOf(ColumnConstants.X)]);
		x = isBlank(stringX)?-1:Integer.parseInt(stringX);
		String stringY = String.valueOf(rowData[table.getIndexOf(ColumnConstants.Y)]);
		y = isBlank(stringY)?-1:Integer.parseInt(stringY);
		
		rep = (String)rowData[table.getIndexOf(ColumnConstants.REP)];
		
		Object tagId = rowData[table.getIndexOf(ColumnConstants.TAG_ID)];
		if(tagId == null)
			tagId = -1;
		else
			if(Utils.isInteger(String.valueOf(tagId)))
			{
				tagId = Integer.parseInt(String.valueOf(tagId));
				this.tagId = (Integer) tagId;
			}else{
				tagId = -1;
			}
		modified = (Boolean)rowData[table.getIndexOf(ColumnConstants.MODIFIED)];
		tag = (String) rowData[table.getIndexOf(ColumnConstants.TAG)];
		
		Object expId = rowData[table.getIndexOf(ColumnConstants.EXP_ID)];
		if(expId == null)
			this.expId = -1;
		else
			this.expId = Integer.parseInt(String.valueOf(expId));
		
		expFactorValueids = (String)rowData[table.getIndexOf(ColumnConstants.EXP_FACTOR_VALUE_IDS)];
    }

    public Object[] getRowData(PlotIndexColumnTable table) {
        Object[] rowData = new Object[table.getColumnCount()];
        rowData[table.getIndexOf(ColumnConstants.TYPES)] = type;
        rowData[table.getIndexOf(ColumnConstants.ROW)] = row;
        rowData[table.getIndexOf(ColumnConstants.PLANT)] = plant;
        rowData[table.getIndexOf(ColumnConstants.STOCK_ID)] = stockId;
        rowData[table.getIndexOf(ColumnConstants.STOCK_NAME)] = (stockId > 0 || stockName != null) ? stockName : null;
        rowData[table.getIndexOf(ColumnConstants.ACCESSION)] = (stockId > 0 || accession != null) ? accession : null;
        rowData[table.getIndexOf(ColumnConstants.PEDIGREE)] = (stockId > 0 || pedigree != null) ? pedigree : null;
        rowData[table.getIndexOf(ColumnConstants.GENERATION)] = (stockId > 0 || generation != null) ? generation : null;
        rowData[table.getIndexOf(ColumnConstants.CYCLE)] = (stockId > 0 || cycle != null) ? cycle : null;
        rowData[table.getIndexOf(ColumnConstants.CLASSIFICATION_CODE)] = (stockId > 0 || classification_code != null) ? classification_code : null;
        rowData[table.getIndexOf(ColumnConstants.POPULATION)] = (stockId > 0 || population != null) ? population : null;
        rowData[table.getIndexOf(ColumnConstants.START_TAG)] = startTag==-1?1:startTag;
		rowData[table.getIndexOf(ColumnConstants.END_TAG)] = endTag==-1?0:endTag;
		rowData[table.getIndexOf(ColumnConstants.COUNT)] = count==-1?0:count;
		rowData[table.getIndexOf(ColumnConstants.PLANTING_DATE)] =  (stockId>0)?plantingDate:null;
		rowData[table.getIndexOf(ColumnConstants.KERNELS)] =  (stockId>0)?StringUtils.defaultIfEmpty(kernels, null):null;
		rowData[table.getIndexOf(ColumnConstants.DELAY)] = (stockId>0)?StringUtils.defaultIfEmpty(delay, null):null;
		rowData[table.getIndexOf(ColumnConstants.PURPOSE)] =  (stockId>0)?StringUtils.defaultIfEmpty(purpose, null):null;
		rowData[table.getIndexOf(ColumnConstants.COMMENT)] =  (stockId>0)?StringUtils.defaultIfEmpty(comment, null):null;
		rowData[table.getIndexOf(ColumnConstants.MATING_PLANT_ID)] = mateMethodConnectid;
		rowData[table.getIndexOf(ColumnConstants.MATING_PLAN)] = (stockId>0)?StringUtils.defaultIfEmpty(matingPlan, null):null;
		rowData[table.getIndexOf(ColumnConstants.X)] = x;
		rowData[table.getIndexOf(ColumnConstants.Y)] = y;
        rowData[table.getIndexOf(ColumnConstants.REP)] = (stockId > 0 || rep != null) ? rep : null;
        rowData[table.getIndexOf(ColumnConstants.TAG_ID)] = (tagId > 0) ? tagId : null;
        rowData[table.getIndexOf(ColumnConstants.TAG)] = tag;
        rowData[table.getIndexOf(ColumnConstants.MODIFIED)] = modified;
        rowData[table.getIndexOf(ColumnConstants.EXP_ID)] = (expId > 0) ? expId : null;
        rowData[table.getIndexOf(ColumnConstants.EXP_FACTOR_VALUE_IDS)] = (expFactorValueids != null) ? expFactorValueids : null;
        return rowData;
    }


	public String toString() {
		return "<HTML>" + stockName + " (" + x + ", " + y + ")<br>Acession: "
				+ this.accession + "<br>Pedigree: " + this.pedigree + "<br>ClassificationCode: " 
				+ this.classification_code + "<br>Purpose: " + this.purpose + "<br>Replication: " + this.rep 
				+ "<br>Comment: " + this.comment + "</HTML>";
	}
    
	public void setCoordinates(int x, int y) {
		if(this.x != x || this.y != y)
			setModified(true);
		this.x = x;
		this.y = y;
	}
	
	
	public boolean isBlank(String string) {
		return StringUtils.isBlank(string) || string.equalsIgnoreCase("null");
	}	

	public long getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(long uniqueId) {
		this.uniqueId = uniqueId;
	}
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
	public String getClassificationCode() {
		return classification_code;
	}
	public void setClassificationCode(String classification_code) {
		this.classification_code = classification_code;
	}
	public String getPopulation() {
		return population;
	}
	public void setPopulation(String population) {
		this.population = population;
	}
	public Date getPlantingDate() {
		return plantingDate;
	}
	public void setPlantingDate(Date plantingDate) {
		this.plantingDate = plantingDate;
	}
	public String getKernels() {
		return kernels;
	}
	public void setKernels(String kernels) {
		this.kernels = kernels;
	}
	public String getPurpose() {
		return purpose;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getDelay() {
		return delay;
	}
	public void setDelay(String delay) {
		this.delay = delay;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getStartTag() {
		return startTag;
	}
	public void setStartTag(int startTag) {
		this.startTag = startTag;
	}
	public int getEndTag() {
		return endTag;
	}
	public void setEndTag(int endTag) {
		this.endTag = endTag;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getMateMethodConnectid() {
		return mateMethodConnectid;
	}
	public void setMatingId(int mateConnect) {
		this.mateMethodConnectid = mateConnect;
	}
	public String getMatingPlan() {
		return matingPlan;
	}
	public void setMatingPlan(String matingPlan) {
		this.matingPlan = matingPlan;
	}
	public int getTagId() {
		return tagId;
	}
	public void setTagId(int tagId) {
		this.tagId = tagId;
	}
	public boolean isModified() {
		return modified;
	}
	public void setModified(boolean modified) {
		this.modified = modified;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}

    public String getExpFactorValueids() {
		return expFactorValueids;
	}

	public void setExpFactorValueids(String expFactorValueids) {
		this.expFactorValueids = expFactorValueids;
	}
	public int getExpId() {
		return expId;
	}

	public void setExpId(int expId) {
		this.expId = expId;
		
	}
	public String getRep() {
		return rep;
	}
	public void setRep(String rep) {
		this.rep = rep;
	}
	
	public boolean isSelectedInMapView() {
		return selectedInMapView;
	}

	public void setSelectedInMapView(boolean selectedInMapView) {
		this.selectedInMapView = selectedInMapView;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getRow() {
		return row;
	}

	public void setRow(String row) {
		this.row = row;
	}

	public String getPlant() {
		return plant;
	}

	public void setPlant(String plant) {
		this.plant = plant;
	}
	public Color getPlotColor() {
		return plotColor;
	}

	public void setPlotColor(Color plotColor) {
		this.plotColor = plotColor;
	}
	public String getGeneration() {
		return generation;
	}

	public void setGeneration(String generation) {
		this.generation = generation;
	}


	
}
