package org.accretegb.modules.germplasm.harvesting;

import net.miginfocom.swing.MigLayout;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.hibernate.dao.StockCompositionDAO;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.util.ThreadPool;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Future;

import org.accretegb.modules.constants.ColumnConstants;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

public class Harvesting extends TabComponentPanel {

	private FieldGenerated fieldGenerated;
	private Bulk bulk;
	private StickerGenerator stickerGenerator;
	private TreeMap<String, List<ArrayList<String>>> stockCompositionByMate = new TreeMap<String, List<ArrayList<String>>>();
	private TreeMap<String, List<ArrayList<String>>> stockCompositionByBulk = new TreeMap<String, List<ArrayList<String>>>();
	private Future currentThread;
	public boolean fromDB = false;
	public FieldGenerated getFieldGenerated() {
		return fieldGenerated;
	}

	public void setFieldGenerated(FieldGenerated fieldGenerated) {
		this.fieldGenerated = fieldGenerated;
	}

	public Bulk getBulk() {
		return bulk;
	}

	public void setBulk(Bulk bulk) {
		this.bulk = bulk;
	}

	public StickerGenerator getStickerGenerator() {
		return stickerGenerator;
	}

	public void setStickerGenerator(StickerGenerator stickerGenerator) {
		this.stickerGenerator = stickerGenerator;
	}
	

	public void initialize() {
		setLayout(new MigLayout("insets 10, gap 10"));

		final JTabbedPane tabbedPane = new JTabbedPane();

		tabbedPane.add(fieldGenerated);
		tabbedPane.setTitleAt(0, "Crossing Record");

		tabbedPane.add(bulk);
		tabbedPane.setTitleAt(1, "Bulks");

		tabbedPane.add(stickerGenerator);
		tabbedPane.setTitleAt(2, "Stock Generator");
		
		bulk.setStickerGenerator(stickerGenerator);

		final Runnable updateMaps = new Runnable(){
			public void run() {															
				fieldGenerated.modified = false;
				bulk.modified = false;
				bulk.getImportStocks().setText("Loading...");
				stickerGenerator.getImportButton().setText("Loading...");
				bulk.getImportStocks().setEnabled(false);
				stickerGenerator.getImportButton().setEnabled(false);
				updateStockCompositionFromMate();
				updateStockCompositionFromBulk();	
				updateStockGeneratorTable();
				//((DefaultTableModel)stickerGenerator.getStickerTablePanel().getTable().getModel()).setRowCount(0);				
				bulk.getImportStocks().setEnabled(true);
				bulk.getImportStocks().setText("Import");
				stickerGenerator.getImportButton().setEnabled(true);
				stickerGenerator.getImportButton().setText("Import");
			}																		
		};

		tabbedPane.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if (fromDB){
					fromDB = false;
					updateStockCompositionFromMate();
					updateStockCompositionFromBulk();	
					updateStockGeneratorTable();
				}
				if((fieldGenerated.modified == true || bulk.modified == true) && !fromDB){
					// start updating maps					
					// if previous updating is not done, stop
					if(currentThread != null && !currentThread.isDone()){
						currentThread.cancel(true);
					}
					currentThread = ThreadPool.getAGBThreadPool().submitTask(updateMaps);
				}
				
			}					
		});
		
		
		stickerGenerator.setHarvesting(this);
		add(tabbedPane, "w 100%, h 100%");
	}
	
	

	public void updateStockCompositionFromMate(){
		stockCompositionByMate = new TreeMap<String, List<ArrayList<String>>>();
		CheckBoxIndexColumnTable mateTable = fieldGenerated.getCrossingTablePanel().getTable();
		int maxMatelink = StockCompositionDAO.getInstance().findMaxMateLink();
		Set<String> links = new HashSet<String>();
		for(int row = 0; row < mateTable.getRowCount(); row++){
			if(!String.valueOf(mateTable.getValueAt(row,mateTable.getIndexOf(ColumnConstants.MATE_LINK))).equals("null"))
			{
				links.add(String.valueOf(mateTable.getValueAt(row,mateTable.getIndexOf(ColumnConstants.MATE_LINK))));
			}
		}
		for(String currentLink : links){
			String femailStock = "";
			List<ArrayList<String>> compositions = new ArrayList<ArrayList<String>>();
			maxMatelink++;
			for(int row = 0; row < mateTable.getRowCount(); row++){
				if(String.valueOf(mateTable.getValueAt(row,mateTable.getIndexOf(ColumnConstants.MATE_LINK))).equals(currentLink)){
					ArrayList<String> compostion = new ArrayList<String>();
					//obs_unit, tag name, mate_link, methodName, mate type, mate role, accession, pedigree, generation
					compostion.add(String.valueOf(mateTable.getValueAt(row,mateTable.getIndexOf(ColumnConstants.TAG_ID))));
					compostion.add(String.valueOf(mateTable.getValueAt(row,mateTable.getIndexOf(ColumnConstants.TAG_NAME))));
					compostion.add(String.valueOf(maxMatelink));
					String type = String.valueOf(mateTable.getValueAt(row,mateTable.getIndexOf(ColumnConstants.MATING_TYPE)));
					String role = String.valueOf(mateTable.getValueAt(row,mateTable.getIndexOf(ColumnConstants.ROLE)));
					String methodName = String.valueOf(mateTable.getValueAt(row,mateTable.getIndexOf(ColumnConstants.MATE_METHOD)));
					compostion.add(methodName);
					compostion.add(type);
					compostion.add(role);
					compostion.add(String.valueOf(mateTable.getValueAt(row,mateTable.getIndexOf(ColumnConstants.ACCESSION))));
					compostion.add(String.valueOf(mateTable.getValueAt(row,mateTable.getIndexOf(ColumnConstants.PEDIGREE))));
					compostion.add(String.valueOf(mateTable.getValueAt(row,mateTable.getIndexOf(ColumnConstants.GENERATION))));
					compositions.add(compostion);
					if(String.valueOf(mateTable.getValueAt(row,mateTable.getIndexOf(ColumnConstants.ROLE))).equals("F")){
						femailStock = String.valueOf(mateTable.getValueAt(row,mateTable.getIndexOf(ColumnConstants.STOCK_NAME)));
					}
				}
			}
			
			stockCompositionByMate.put(femailStock, compositions);
			//System.out.println(femailStock);
			stickerGenerator.setStockCompositionByMate(stockCompositionByMate);
		}
	}
	
	
	public void updateStockCompositionFromBulk(){
		stockCompositionByBulk = new TreeMap<String, List<ArrayList<String>>>();
		CheckBoxIndexColumnTable bulkTable = bulk.getBulkTablePanel().getTable();
		Set<String> mixIds = new HashSet<String>();
		for(int row = 0; row < bulkTable.getRowCount(); row++){
			if(!String.valueOf(bulkTable.getValueAt(row,bulkTable.getIndexOf(ColumnConstants.MIX_ID))).equals("null"))
			{
				mixIds.add(String.valueOf(bulkTable.getValueAt(row,bulkTable.getIndexOf(ColumnConstants.MIX_ID))));
			}
		}
		for(String mixId : mixIds){
			String finalStock = "";
			List<ArrayList<String>> compositions = new ArrayList<ArrayList<String>>();
			for(int row = 0; row < bulkTable.getRowCount(); row++){
				if(String.valueOf(bulkTable.getValueAt(row,bulkTable.getIndexOf(ColumnConstants.MIX_ID))).equals(mixId)){
					ArrayList<String> compostion = new ArrayList<String>();
					//mix_from_stock_id, stock name,  mix_quantity, measurement_unit_id,unit, accession, pedigree
					String stockName = String.valueOf(bulkTable.getValueAt(row,bulkTable.getIndexOf(ColumnConstants.STOCK_NAME)));
					String stockId = String.valueOf(bulkTable.getValueAt(row,bulkTable.getIndexOf(ColumnConstants.STOCK_ID)));
					compostion.add(stockId);
					compostion.add(stockName);
					compostion.add(String.valueOf(bulkTable.getValueAt(row,bulkTable.getIndexOf(ColumnConstants.QUANTITY))));
					compostion.add(String.valueOf(bulkTable.getValueAt(row,bulkTable.getIndexOf(ColumnConstants.UNIT_ID))));	
					compostion.add(String.valueOf(bulkTable.getValueAt(row,bulkTable.getIndexOf(ColumnConstants.UNIT))));
					compostion.add(String.valueOf(bulkTable.getValueAt(row,bulkTable.getIndexOf(ColumnConstants.ACCESSION))));
					compostion.add(String.valueOf(bulkTable.getValueAt(row,bulkTable.getIndexOf(ColumnConstants.PEDIGREE))));
					compositions.add(compostion);
					finalStock = String.valueOf(bulkTable.getValueAt(row,bulkTable.getIndexOf(ColumnConstants.FINAL_STOCK_NAME)));						
			   }
			}
			stockCompositionByBulk.put(finalStock, compositions);
			stickerGenerator.setStockCompositionByBulk(stockCompositionByBulk);
		}
	}
	
	public void updateStockGeneratorTable(){
		CheckBoxIndexColumnTable stockGeneratorTable = stickerGenerator.getStickerTablePanel().getTable();
		//System.out.println(stockCompositionByMate.keySet());
		//System.out.println(stockCompositionByBulk.keySet());
		for(int row = 0; row < stockGeneratorTable.getRowCount(); ++row ){
			
			String stockName = String.valueOf(stockGeneratorTable.getValueAt(row,stockGeneratorTable.getIndexOf(ColumnConstants.STOCK_NAME)));
			//System.out.println(stockName);
			//System.out.println(stockCompositionByMate.containsKey(stockName));		
			//System.out.println(stockCompositionByBulk.containsKey(stockName));
			if(!stockCompositionByMate.containsKey(stockName) && !stockCompositionByBulk.containsKey(stockName)){
				DefaultTableModel model = (DefaultTableModel)stockGeneratorTable.getModel();
				model.removeRow(stockGeneratorTable.convertRowIndexToModel(row));
				row--;
				//System.out.println(row);
			}
		}
	}

}