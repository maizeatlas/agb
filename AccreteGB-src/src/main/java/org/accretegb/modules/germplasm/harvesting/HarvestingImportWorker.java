package org.accretegb.modules.germplasm.harvesting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.hibernate.Stock;
import org.accretegb.modules.hibernate.dao.ObservationUnitDAO;
import org.accretegb.modules.hibernate.dao.StockDAO;
import org.accretegb.modules.constants.ColumnConstants;

public class HarvestingImportWorker extends SwingWorker<Void, Void> {

	private FieldGenerated fieldGenerated;
	private JFileChooser fc;
	private ArrayList<String> duplicate = new ArrayList<String>();
	private ArrayList<String> nonExist = new ArrayList<String>();
	private ArrayList<String> invalidFormat = new ArrayList<String>();
	HashSet<String> existingStockList = new HashSet<String>();
	
	public HarvestingImportWorker(FieldGenerated fieldGenerated, JFileChooser fc) {
		this.fieldGenerated = fieldGenerated;
		this.fc = fc;
	}
	@Override
	protected Void doInBackground() throws Exception {
		TableToolBoxPanel crossingTablePanel = fieldGenerated.getCrossingTablePanel();
		DefaultTableModel model = (DefaultTableModel) crossingTablePanel.getTable().getModel();
		// get all existing ones
		
		for(int row=0; row < crossingTablePanel.getTable().getRowCount(); ++row){
			existingStockList.add(String.valueOf((crossingTablePanel.getTable().getValueAt(row, 
					crossingTablePanel.getTable().getIndexOf(ColumnConstants.STOCK_NAME)))));
		}
		
		//Utils.removeAllRowsFromTable(model);
		int mateLink = fieldGenerated.nextLink;
		int selectIndex = crossingTablePanel.getTable().getIndexOf(ColumnConstants.SELECT);
		int roleIndex = crossingTablePanel.getTable().getIndexOf(ColumnConstants.ROLE);
		int mateMethodIndex = crossingTablePanel.getTable().getIndexOf(ColumnConstants.MATE_METHOD);
		int matingTypeIndex = crossingTablePanel.getTable().getIndexOf(ColumnConstants.MATING_TYPE);
		int tagIndex = crossingTablePanel.getTable().getIndexOf(ColumnConstants.TAG_NAME);
		int linkIndex = crossingTablePanel.getTable().getIndexOf(ColumnConstants.MATE_LINK);
		int stockNameIndex = crossingTablePanel.getTable().getIndexOf(ColumnConstants.STOCK_NAME);
		int accessionIndex = crossingTablePanel.getTable().getIndexOf(ColumnConstants.ACCESSION);
		int pedigreeIndex = crossingTablePanel.getTable().getIndexOf(ColumnConstants.PEDIGREE);
		int generationIndex = crossingTablePanel.getTable().getIndexOf(ColumnConstants.GENERATION);
		//System.out.println(accessionIndex + ", " + pedigreeIndex + ", " + generationIndex);
		int obsUnitIndex = crossingTablePanel.getTable().getIndexOf(ColumnConstants.TAG_ID);
		int selectionIndex = crossingTablePanel.getTable().getIndexOf(ColumnConstants.SELECTION);
		Map<String, Integer> hashMap = new HashMap<String, Integer>();
		String filename = fc.getSelectedFile().toString();
        //ArrayList<Object[]> rows_to_add = new ArrayList<Object[]>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
			DecimalFormat df = new DecimalFormat("00000");
			String line, tagPrefix = null;
			DecimalFormat df2 = new DecimalFormat("0000000");
			String[] values = {"CR","BC","PP", "SF","SB","1MNF_BC","1MNF_CR"};
			ArrayList<String> matingTypes = new ArrayList<String>();
			for(String v : values){
				matingTypes.add(v);
			}
			
			while ((line = br.readLine()) != null) {
				line = line.trim();
				boolean selectionApplied = false;
				//start
				if(line.length() >=2 ){
					String selection = line.substring(line.length()-1, line.length());			  
					if(Utils.isInteger(selection)){
						if(Integer.parseInt(selection) == 1){
							selectionApplied = true;	
						}
						line = line.substring(0, line.length()-1);
					}
				}
			    if(matingTypes.contains(line.toUpperCase())){
			    	//set mating type
			    	String matingType = line.toUpperCase();
			    	
			    	//Next line should be female
			    	line = br.readLine();
			    	if (line != null) {
			    		line = line.trim();
			    	}
			    	if(line.split("\\.").length != 5){
			    		invalidFormat.add(line);
			    	}else{
			    		String stockName = line;
						boolean boolvalue = !existingStockList.contains(stockName);
						if(!boolvalue) {
							duplicate.add("group with female tag " + stockName + " has duplicates in the table.");
						}else{
							mateLink++;
							fieldGenerated.nextLink = mateLink;
						}
			    		String[] array;
			    		array = line.split(("\\."));
						tagPrefix = array[0] + "." + array[1] + "." + array[2];
						
			    		// read lines based on mating type
			    		if(matingType.equals("CR") || matingType.equals("BC")){
			    			//Next line should be male
			    			String maleTag = "";
			    			line = br.readLine();
			    			if (line != null) {
					    		line = line.trim();
					    	}
			    			if (line.split("\\.").length == 5){
			    				maleTag = line;
			    			}else{
			    				if(line.split("\\.").length != 2){
			    					invalidFormat.add(line);
			    				}else{
						    		array = line.split(("\\."));
									String rowNumber = array[0];
									String plantNumber = array[1];
									maleTag = tagPrefix+"."+df.format(Integer.parseInt(rowNumber))
													 +"."+df2.format(Integer.parseInt(plantNumber));
			    				}
			    			}
							
							//Next line should be method or end
							line = br.readLine();
							if (line != null) {
					    		line = line.trim();
					    	}
							String method = null;
							if(!line.equals("end")){
								method = line;
							}
							//create rows
							
							Object[] result = ObservationUnitDAO.getInstance().searchByTagname(stockName);
							Object[] result1 = ObservationUnitDAO.getInstance().searchByTagname(maleTag);
							if(result == null || result1 == null){
								nonExist.add("group with female tag " + stockName + ", mating type "+matingType+".");		
							}
							if(boolvalue && result != null && result1 != null)
							{
								Object[] rowDataFemale = new Object[crossingTablePanel.getTable().getColumnCount()];
								rowDataFemale[selectIndex] = new Boolean(false);
								rowDataFemale[roleIndex] = "F";
								rowDataFemale[mateMethodIndex] = method;
								rowDataFemale[matingTypeIndex] = matingType;
								rowDataFemale[tagIndex] = stockName;
								rowDataFemale[linkIndex] = mateLink;
								rowDataFemale[stockNameIndex] = stockName;
								rowDataFemale[obsUnitIndex] = result[0];
								rowDataFemale[accessionIndex] = result[1];
								rowDataFemale[pedigreeIndex] = result[2];
								rowDataFemale[generationIndex] = String.valueOf(result[3]).toUpperCase();
								rowDataFemale[selectionIndex] = new Boolean(selectionApplied);
								existingStockList.add(stockName);
								model.addRow(rowDataFemale);
								
								Object[] rowDataMale = new Object[crossingTablePanel.getTable().getColumnCount()];
								rowDataMale = new Object[crossingTablePanel.getTable().getColumnCount()];
								rowDataMale[selectIndex] = new Boolean(false);
								rowDataMale[roleIndex] = "M";
								rowDataMale[mateMethodIndex] = method;
								rowDataMale[matingTypeIndex] = matingType;
								rowDataMale[tagIndex] = maleTag;
								rowDataMale[linkIndex] = mateLink;
								rowDataMale[obsUnitIndex] = result1[0];
								rowDataMale[accessionIndex] = result1[1];
								rowDataMale[pedigreeIndex] = result1[2];
								rowDataMale[generationIndex] = String.valueOf(result1[3]).toUpperCase();
								rowDataMale[selectionIndex] = new Boolean(selectionApplied);
								model.addRow(rowDataMale);
							}						
			    		}
			    		else if (matingType.equals("SF") || matingType.equals("SB")){
			    		
			    			//Next line can be stockname, method or end
			    			ArrayList<String> sfStockNames = new ArrayList<String>();
			    			sfStockNames.add(stockName);
							line = br.readLine();
							if (line != null) {
					    		line = line.trim();
					    	}
							while (line.split("\\.").length == 5){
								sfStockNames.add(line);
								line = br.readLine();
								if (line != null) {
						    		line = line.trim();
						    	}
							}
							String method = null;
							if(!line.equals("end")){
								method = line;
							}
							for (String name : sfStockNames){
								boolvalue = !existingStockList.contains(name);
								if(!boolvalue) {
									duplicate.add("group with female tag " + stockName + " has duplicates in the table.");
								}else{
									mateLink++;
									fieldGenerated.nextLink = mateLink;
								}
								//create row
								Object[] rowDataFemale = new Object[crossingTablePanel.getTable().getColumnCount()];
								rowDataFemale[selectIndex] = new Boolean(false);
								rowDataFemale[roleIndex] = "F";
								rowDataFemale[mateMethodIndex] = method;
								rowDataFemale[matingTypeIndex] = matingType;
								rowDataFemale[tagIndex] = name;
								rowDataFemale[linkIndex] = mateLink;
								rowDataFemale[stockNameIndex] = name;
								Object[] result = ObservationUnitDAO.getInstance().searchByTagname(name);
								if(result == null){
									nonExist.add("group with female tag " + name + ", mating type "+matingType+".");	
								}
								if(boolvalue && result != null)
								{
									rowDataFemale[obsUnitIndex] = result[0];
									rowDataFemale[accessionIndex] = result[1];
									rowDataFemale[pedigreeIndex] = result[2];
									rowDataFemale[generationIndex] = String.valueOf(result[3]).toUpperCase();
									rowDataFemale[selectionIndex] = new Boolean(selectionApplied);
									model.addRow(rowDataFemale);
									existingStockList.add(name);
								}
							}
							
			    		}
			    		else if (matingType.equals("PP")){
			    			ArrayList<String> males = new ArrayList<String>();
			    			String method = null;
			    			while(!(line = String.valueOf(br.readLine()).trim()).equals("end")){
			 
			    				if(line.contains("."))
			    				{
			    					String maleTag = "";
					    			if (line.split("\\.").length == 5){
					    				maleTag = line;
					    			}else{
					    				if(line.split("\\.").length != 2){
					    					invalidFormat.add(line);
					    				}
							    		array = line.split(("\\."));
										String rowNumber = array[0];
										String plantNumber = array[1];
										maleTag = tagPrefix+"."+df.format(Integer.parseInt(rowNumber))
														 +"."+df2.format(Integer.parseInt(plantNumber));
					    			}
									males.add(maleTag);
			    				}else{
			    					method = line;
			    				}	
			    			}
			    			ArrayList<Object[]> rows_to_add = new ArrayList<Object[]>();
							for(String maleTag: males){
								Object[] rowDataMale = new Object[crossingTablePanel.getTable().getColumnCount()];
								rowDataMale = new Object[crossingTablePanel.getTable().getColumnCount()];
								rowDataMale[selectIndex] = new Boolean(false);
								rowDataMale[roleIndex] = "M";
								rowDataMale[mateMethodIndex] = method;
								rowDataMale[matingTypeIndex] = matingType;
								rowDataMale[tagIndex] = maleTag;
								rowDataMale[linkIndex] = mateLink;
								Object[] result = ObservationUnitDAO.getInstance().searchByTagname(maleTag);
								
								if (result == null) 
									System.out.print("male <"+maleTag+">");
									
								
								
								if(boolvalue && result != null)
								{
									rowDataMale[obsUnitIndex] = result[0];
									rowDataMale[accessionIndex] = result[1];
									rowDataMale[pedigreeIndex] = result[2];
									rowDataMale[generationIndex] = String.valueOf(result[3]).toUpperCase();
									rowDataMale[selectionIndex] = new Boolean(selectionApplied);
									rows_to_add.add(rowDataMale);
								}else{
									boolvalue = false;
									rows_to_add.clear();
								}
							}
							Object[] result = ObservationUnitDAO.getInstance().searchByTagname(stockName);
							if (result == null) 
							    System.out.print("female <"+stockName+">");
							if(rows_to_add.size() == 0 || result == null){
								nonExist.add("group with female tag " + stockName + ", mating type "+matingType+".");		
							}
							if(boolvalue && rows_to_add.size()!=0 && result != null){
								Object[] rowDataFemale = new Object[crossingTablePanel.getTable().getColumnCount()];
								rowDataFemale[selectIndex] = new Boolean(false);
								rowDataFemale[roleIndex] = "F";
								rowDataFemale[mateMethodIndex] = method;
								rowDataFemale[matingTypeIndex] = matingType;
								rowDataFemale[tagIndex] = stockName;
								rowDataFemale[linkIndex] = mateLink;
								rowDataFemale[stockNameIndex] = stockName;
								rowDataFemale[obsUnitIndex] = result[0];
								rowDataFemale[accessionIndex] = result[1];
								rowDataFemale[pedigreeIndex] = result[2];
								rowDataFemale[generationIndex] = String.valueOf(result[3]).toUpperCase();
								rowDataFemale[selectionIndex] = new Boolean(selectionApplied);
								model.addRow(rowDataFemale);
								existingStockList.add(stockName);
								
								for(Object[] maleRow: rows_to_add){
									model.addRow(maleRow);
								}
							}
			    		}
			    		else if (matingType.equals("1MNF_CR") || matingType.equals("1MNF_BC") ){
			    			String revisedMatingType = matingType;
			    			if(matingType.contains("_"))
			    			{
			    				revisedMatingType = matingType.split("_")[1];
			    			}
			    			ArrayList<String> females = new ArrayList<String>();
			    			String method = null;
			    			while(!(line = String.valueOf(br.readLine()).trim()).equals("end")){
			    				if(line.contains("."))
			    				{
			    					String femaleTag = "";
					    			if (line.split("\\.").length == 5){
					    				femaleTag = line;
					    			}else{
					    				if(line.split("\\.").length != 2){
					    					invalidFormat.add(line);
					    				}
							    		array = line.split(("\\."));
										String rowNumber = array[0];
										String plantNumber = array[1];
										femaleTag = tagPrefix+"."+df.format(Integer.parseInt(rowNumber))
														 +"."+df2.format(Integer.parseInt(plantNumber));
					    			}
					    			
					    			if(this.existingStockList.contains(femaleTag)){
					    				boolvalue = false;
					    			}
					    			
									females.add(femaleTag);
									
			    				}else{
			    					method = line;
			    					break;
			    				}
			    			}	
			    			// Each time creates two rows - one for F or for M
			    			ArrayList<Object[]> rows_to_add = new ArrayList<Object[]>();
							for(String femaleTag: females){
								Object[] rowDataFemale = new Object[crossingTablePanel.getTable().getColumnCount()];
								rowDataFemale = new Object[crossingTablePanel.getTable().getColumnCount()];
								rowDataFemale[selectIndex] = new Boolean(false);
								rowDataFemale[roleIndex] = "F";
								rowDataFemale[mateMethodIndex] = method;
								rowDataFemale[matingTypeIndex] = revisedMatingType;
								rowDataFemale[tagIndex] = femaleTag;
								rowDataFemale[linkIndex] = mateLink;
								rowDataFemale[stockNameIndex] = femaleTag;
								Object[] result = ObservationUnitDAO.getInstance().searchByTagname(femaleTag);
								if(boolvalue && result != null)
								{
									rowDataFemale[obsUnitIndex] = result[0];
									rowDataFemale[accessionIndex] = result[1];
									rowDataFemale[pedigreeIndex] = result[2];
									rowDataFemale[generationIndex] = String.valueOf(result[3]).toUpperCase();
									rowDataFemale[selectionIndex] = new Boolean(selectionApplied);
									rows_to_add.add(rowDataFemale);
								}else{
									boolvalue = false;
									rows_to_add.clear();
								}
							}
							Object[] result = ObservationUnitDAO.getInstance().searchByTagname(stockName);
							if(rows_to_add.size() == 0 || result == null){
								nonExist.add("group with male tag " + stockName + ", mating type "+matingType+".");		
							}
							if(boolvalue && rows_to_add.size()!=0 && result != null){
								for(Object[] femaleRow: rows_to_add){
									Object[] rowDataMale = new Object[crossingTablePanel.getTable().getColumnCount()];
									rowDataMale[selectIndex] = new Boolean(false);
									rowDataMale[roleIndex] = "M";
									rowDataMale[mateMethodIndex] = method;
									rowDataMale[matingTypeIndex] = revisedMatingType;
									rowDataMale[tagIndex] = stockName;
									rowDataMale[linkIndex] = mateLink;
									rowDataMale[obsUnitIndex] = result[0];
									rowDataMale[accessionIndex] = result[1];
									rowDataMale[pedigreeIndex] = result[2];
									rowDataMale[generationIndex] = String.valueOf(result[3]).toUpperCase();
									rowDataMale[selectionIndex] = new Boolean(selectionApplied);
									model.addRow(rowDataMale);
									
									femaleRow[linkIndex] = mateLink;
									model.addRow(femaleRow);
									existingStockList.add(String.valueOf(femaleRow[stockNameIndex]));
									mateLink++;
									this.fieldGenerated.nextLink = mateLink;
								}
							}
			    		}
			    		/*else if (matingType.equals("1FNM")){
			    			ArrayList<String> females = new ArrayList<String>();
			    			String method = null;
			    			while(!(line = br.readLine().trim()).equals("end")){
			    				if(line.contains("."))
			    				{
			    					array = line.split(("\\."));
			    					String rowNumber = array[0];
									String plantNumber = array[1];
									String maleTag = tagPrefix+"."+df.format(Integer.parseInt(rowNumber))
													 +"."+df2.format(Integer.parseInt(plantNumber));
									females.add(maleTag);
									// Each time creates two rows - one for F or for M
			    				}else{
			    					method = line;
			    					break;
			    				}		
			    			}		
			    		}*/
			    		else{
			    			invalidFormat.add(line);
			    		}
			    	}  	
			    }
			 }
			
			crossingTablePanel.getTable().setModel(model);
			br.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e1){
			e1.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void done() {
		ArrayList list = new ArrayList(existingStockList);
		List<Stock> stocks = StockDAO.getInstance().getStocksByNames(list);
		if (stocks.size() != 0){
			for(Stock s : stocks){
				duplicate.add("group with female tag " + s.getStockName() + " found in database.");
			}
		}
		fieldGenerated.modified = true;
		fieldGenerated.finishedImport(duplicate, nonExist, invalidFormat);
	}

}
