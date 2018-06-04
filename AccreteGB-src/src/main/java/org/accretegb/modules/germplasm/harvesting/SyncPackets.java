package org.accretegb.modules.germplasm.harvesting;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.hibernate.Mate;
import org.accretegb.modules.hibernate.MateMethodConnect;
import org.accretegb.modules.hibernate.MeasurementUnit;
import org.accretegb.modules.hibernate.MateMethod;
import org.accretegb.modules.hibernate.ObservationUnit;
import org.accretegb.modules.hibernate.StockComposition;
import org.accretegb.modules.hibernate.StockGeneration;
import org.accretegb.modules.hibernate.Passport;
import org.accretegb.modules.hibernate.Stock;
import org.accretegb.modules.hibernate.StockPacket;
import org.accretegb.modules.hibernate.dao.MateDAO;
import org.accretegb.modules.hibernate.dao.MateMethodConnectDAO;
import org.accretegb.modules.hibernate.dao.PassportDAO;
import org.accretegb.modules.hibernate.dao.StockDAO;
import org.accretegb.modules.hibernate.dao.StockGenerationDAO;
import org.accretegb.modules.hibernate.dao.StockPacketDAO;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.accretegb.modules.constants.ColumnConstants;

public class SyncPackets extends SwingWorker<Void, Void> {

	private Harvesting harvesting;
	private boolean rollBacked;
	private Map<String, Stock> stockMap;
	private Map<String, Stock> newStockMap;
	private Map<String, StockGeneration> newStockGenerationMap;
	private Map<String, Integer> mateTypeRoleToID;
	private HashMap<String, Integer> mateMethodtoID;
	private List<MateMethodConnect> mateMethodConnects;
	private JProgressBar progress;
	public SyncPackets(Harvesting harvesting) {
		this.harvesting = harvesting;
		stockMap = new HashMap<String, Stock>();
		newStockMap = new HashMap<String, Stock>();
		mateMethodtoID = this.harvesting.getFieldGenerated().mateMethodtoID;
		getMatesInfo();
		getGenerarionInfo();
		progress = harvesting.getStickerGenerator().getProgress();
	}
	
	private class objectToSync {
		  public final Stock stock;
		  public final StockPacket packet;

		  private objectToSync(Stock stock, StockPacket packet) {
		    this.stock = stock;
		    this.packet = packet;
		  }
	}
	
	public void getMatesInfo(){
		mateTypeRoleToID = new  HashMap<String, Integer> ();
		List<Mate> mates = MateDAO.getInstance().getMates();
		for(Mate mate : mates){
			mateTypeRoleToID.put(mate.getMatingType() + "-" + mate.getMateRole(), mate.getMateId());
		}
		mateMethodConnects = MateMethodConnectDAO.getInstance().getAllMateConnects();
	}
	
	private void getGenerarionInfo(){
		newStockGenerationMap = new HashMap<String, StockGeneration>();
		List<StockGeneration> generations = StockGenerationDAO.getInstance().getGenerations();
		for(StockGeneration sg : generations){
			newStockGenerationMap.put(sg.getGeneration()+"-"+sg.getCycle(), sg);
		}
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		CheckBoxIndexColumnTable table = harvesting.getStickerGenerator().getStickerTablePanel().getTable();
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession(); 
		progress.setVisible(true);
		progress.setValue(0);
	    //progress.setIndeterminate(false);
	    progress.setString("Syncing..." + progress.getValue() + "%");
	    ArrayList<Integer> packetIds = new ArrayList<Integer>();
	    Transaction transaction = session.beginTransaction();
	    long tStart = System.currentTimeMillis();
		try {
			getExistingStocks();
			int size = table.getRowCount();
			for(int row = 0; row < size; ++row)
			{
				progress.setValue((int) ((row*1.0/(size*2))*100));
				if(!(Boolean)table.getValueAt(row, ColumnConstants.MODIFIED))
					continue;
				Integer packetId = (Integer) table.getValueAt(row, ColumnConstants.PACKET_ID);
				//packet id exists, only update packets
				if(packetId != null && packetId > 0) {
					int packetNumber = String.valueOf(table.getValueAt(row, ColumnConstants.PACKET_NUMBER)).equalsIgnoreCase("null") ? 0 
							:Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.PACKET_NUMBER)));
					int quantity = String.valueOf(table.getValueAt(row, ColumnConstants.QUANTITY)).equalsIgnoreCase("null") ? 0 
							:Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.QUANTITY)));					
					String comment = (String)table.getValueAt(row, ColumnConstants.COMMENT);
					Date date = new Date() ;
					if(table.getValueAt(row, ColumnConstants.DATE) instanceof String){						
				        SimpleDateFormat strToDate = new SimpleDateFormat ("E MMM dd HH:mm:ss Z yyyy",Locale.getDefault());
				        // parse format String to date
				        try {
							date = (Date)strToDate.parse((String) table.getValueAt(row, ColumnConstants.DATE));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						date = (Date) table.getValueAt(row, ColumnConstants.DATE);
					}
					StockPacket sp = StockPacketDAO.getInstance().updateStockPacket(packetId, packetNumber, quantity, date, comment , session);
					table.setValueAt(false, row, table.getIndexOf(ColumnConstants.MODIFIED));
					if ( row % 500 == 0 ) { 
						session.merge(sp);
				        //flush a batch of inserts and release memory:
				        session.flush();
				        session.clear();
				    }
					packetIds.add(packetId);
					continue;
				}
				//packet id does not exist, create stocks, packets, passport, stock compositions
				String stockName = (String) table.getValueAt(row, ColumnConstants.STOCK_NAME);
				String accession = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.ACCESSION)));
				String pedigree = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.PEDIGREE)));
				String generation = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.GENERATION)));
				objectToSync o = null;
				if(stockName.contains("m")) {
					o = syncFromBulk(stockName, row, session, accession, pedigree, generation);
				} else {
					o = syncFromFieldGenerated(stockName, row, session, accession, pedigree, generation);
				}
				session.save(o.stock);
				session.save(o.packet);
				table.setValueAt(o.packet.getStockPacketId(), row, table.getIndexOf(ColumnConstants.PACKET_ID));
				table.setValueAt(false, row, table.getIndexOf(ColumnConstants.MODIFIED));
				Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
				table.setValueAt(calendar.getTime(), row, table.getIndexOf(ColumnConstants.DATE));
				if ( row % 500 == 0 ) { 
			        session.flush();
			        session.clear();
			    }
				
			}
			
			long tEnd = System.currentTimeMillis();
			long tDelta = tEnd - tStart;
			double elapsedSeconds = tDelta / 1000.0;
			System.out.println("SYNC(packets, stocks and passport) TIME USED " + elapsedSeconds);
			syncForStockComposition(session);
			rollBacked = false;
			table.clearSelection();
			transaction.commit();
			tDelta = System.currentTimeMillis() - tEnd;
			elapsedSeconds = tDelta / 1000.0;
			System.out.println("syncForStockComposition TIME USED " + elapsedSeconds);
			session.close();
			for(int packetId : harvesting.getStickerGenerator().getDeletedPakcets()) {
				StockPacketDAO.getInstance().delete(packetId);
			}
		harvesting.getStickerGenerator().getDeletedPakcets().clear();
		} catch (Exception e) {
			int size = table.getRowCount();
			for(int row = 0; row < size; ++row)
			{
				table.setValueAt(-1, row, table.getIndexOf(ColumnConstants.PACKET_ID));	
				table.setValueAt(true, row, table.getIndexOf(ColumnConstants.MODIFIED));
			}
			//e.printStackTrace();
			transaction.rollback();
			rollBacked = true;
			session.close();
		} 
		return null;
	}

	private objectToSync syncFromBulk(String stockName, int packetRow, Session session, String accession, 
			String pedigree, String generation) {
		CheckBoxIndexColumnTable table = harvesting.getStickerGenerator().getStickerTablePanel().getTable();
		Stock stock;
		if(!stockMap.containsKey(stockName)) {
			stock = createStock(stockName, packetRow, accession, pedigree, generation, session);
			stockMap.put(stockName, stock);
			newStockMap.put(stockName, stock);
		} 
		stock = stockMap.get(stockName);
		StockPacket packet = new StockPacket();
		packet.setStock(stock);
		if(table.getValueAt(packetRow, ColumnConstants.DATE) instanceof String){
			Date date = new Date() ;
	        SimpleDateFormat strToDate = new SimpleDateFormat ("E MMM dd HH:mm:ss Z yyyy",Locale.getDefault());
	        // parse format String to date
	        try {
				date = (Date)strToDate.parse((String) table.getValueAt(packetRow, ColumnConstants.DATE));
				packet.setStockPacketDate(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			packet.setStockPacketDate((Date) table.getValueAt(packetRow, ColumnConstants.DATE));
		}
		int packetNumber = String.valueOf(table.getValueAt(packetRow, ColumnConstants.PACKET_NUMBER)).equalsIgnoreCase("null") ? 0 
				:Integer.parseInt(String.valueOf(table.getValueAt(packetRow, ColumnConstants.PACKET_NUMBER)));
		int quantity = String.valueOf(table.getValueAt(packetRow, ColumnConstants.QUANTITY)).equalsIgnoreCase("null") ? 0 
				:Integer.parseInt(String.valueOf(table.getValueAt(packetRow, ColumnConstants.QUANTITY)));
		String comment = (String)table.getValueAt(packetRow, ColumnConstants.COMMENT);
		packet.setPacketNo(packetNumber);
		packet.setNoSeed(quantity);
		packet.setStockPacketComments(comment);
		return new objectToSync(stock,packet);
	}
	
	private objectToSync syncFromFieldGenerated(String stockName, int packetRow, Session session, String accession, 
			String pedigree,String generation) {
		
		CheckBoxIndexColumnTable table = harvesting.getStickerGenerator().getStickerTablePanel().getTable();
		Stock stock;
		if(!stockMap.containsKey(stockName)) {
			stock = createStock(stockName, packetRow,accession, pedigree, generation, session);
			stockMap.put(stockName, stock);
			newStockMap.put(stockName, stock);
		} 
		stock = stockMap.get(stockName);	
		StockPacket packet = new StockPacket();
		packet.setStock(stock);
		if(table.getValueAt(packetRow, ColumnConstants.DATE) instanceof String){
			Date date = new Date() ;
	        SimpleDateFormat strToDate = new SimpleDateFormat ("E MMM dd HH:mm:ss Z yyyy",Locale.getDefault());
	        // parse format String to date
	        try {
				date = (Date)strToDate.parse((String) table.getValueAt(packetRow, ColumnConstants.DATE));
				packet.setStockPacketDate(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			packet.setStockPacketDate((Date) table.getValueAt(packetRow, ColumnConstants.DATE));
		}
		int packetNumber = String.valueOf(table.getValueAt(packetRow, ColumnConstants.PACKET_NUMBER)).equalsIgnoreCase("null") ? 0 
				:Integer.parseInt(String.valueOf(table.getValueAt(packetRow, ColumnConstants.PACKET_NUMBER)));
		int quantity = String.valueOf(table.getValueAt(packetRow, ColumnConstants.QUANTITY)).equalsIgnoreCase("null") ? 0 
				:Integer.parseInt(String.valueOf(table.getValueAt(packetRow, ColumnConstants.QUANTITY)));
		String comment = (String)table.getValueAt(packetRow, ColumnConstants.COMMENT);
		packet.setPacketNo(packetNumber);
		packet.setNoSeed(quantity);
		packet.setStockPacketComments(comment);
		return new objectToSync(stock,packet);
	}

	private void syncForStockComposition(Session session){
		CheckBoxIndexColumnTable table = harvesting.getStickerGenerator().getStickerTablePanel().getTable();
		int size = table.getRowCount();
		for(int row = 0; row < size; ++row)
		{
			progress.setValue((int) (((row+size)*1.0/(size*2))*100));
			progress.setString("Syncing..." + progress.getValue() + "%");
			String stockName = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.STOCK_NAME)));		
			if(harvesting.getStickerGenerator().getStockCompositionByMate().containsKey(stockName)){
				 // only generate stock compositions for new stock
	            if(!this.newStockMap.containsKey(stockName)){
	            	continue;
	            }
	            Stock stock = newStockMap.get(stockName);	
				List<ArrayList<String>>  compositions = harvesting.getStickerGenerator().getStockCompositionByMate().get(stockName);
				for(ArrayList<String> composition : compositions){
					StockComposition stockComposition =  new StockComposition();;
					ObservationUnit obsunit = (ObservationUnit)session.load(ObservationUnit.class, Integer.parseInt(composition.get(0)));
					int mateLink = Integer.parseInt(composition.get(2));
					String methodName = composition.get(3);
					String type = composition.get(4);
					String role = composition.get(5);
					Mate mate = null;
					boolean createNewConnect = false;
					if(this.mateTypeRoleToID.containsKey(type+"-"+role)){
						mate = (Mate)session.load(Mate.class, this.mateTypeRoleToID.get(type+"-"+role));
					}else{
						//create new one
						mate = new Mate(type, role, null);
						session.save(mate);
						this.mateTypeRoleToID.put(type+"-"+role,mate.getMateId());
						createNewConnect = true;
					}
					MateMethod mateMethod = null;
					if (!String.valueOf(methodName).equalsIgnoreCase("null")){
						if ( this.mateMethodtoID.containsKey(methodName)){
							mateMethod = (MateMethod) session.load(MateMethod.class, this.mateMethodtoID.get(methodName));
						}
					}
					MateMethodConnect  mateMethodConnect = null;
					for(MateMethodConnect m : this.mateMethodConnects){
						int foundCount = 0;
						if (mate != null ){
							if (m.getMate() != null && m.getMate().getMateId() == mate.getMateId() ){
								foundCount ++;
							}
						}else{
							if ( m.getMate() == mate) {
								foundCount ++;
							}
						}
						
						if (mateMethod != null ){
							if (m.getMateMethod() != null && m.getMateMethod().getMateMethodId() == mateMethod.getMateMethodId() ){
								foundCount ++;
							}
						}else{
							if ( m.getMateMethod() == mateMethod) {
								foundCount ++;
							}
						}
						if (foundCount == 2) {
							mateMethodConnect = m;
						}
						
					}
					if(createNewConnect){
						mateMethodConnect = new MateMethodConnect();
						mateMethodConnect.setMate(mate);
						mateMethodConnect.setMateMethod(mateMethod);
						session.save(mateMethodConnect);
					}
					stockComposition.setStockByStockId(stock);
					stockComposition.setObservationUnit(obsunit);
					stockComposition.setMateLink(mateLink);
					stockComposition.setMateMethodConnect(mateMethodConnect);
					session.save(stockComposition);
					
				}				
			}		
			if ( row % 500 == 0 ) { 
		        session.flush();
		        session.clear();
		    }
		}
		
		// sync the bulks last
		for(int row = 0; row < size; ++row)
		{
			String stockName = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.STOCK_NAME)));
			if(harvesting.getStickerGenerator().getStockCompositionByBulk().containsKey(stockName)){
				 // only generate stock compositions for new stock
	            if(!this.newStockMap.containsKey(stockName)){
	            	continue;
	            }
	            Stock stock = newStockMap.get(stockName);
				List<ArrayList<String>>  compositions = harvesting.getStickerGenerator().getStockCompositionByBulk().get(stockName);
				for(ArrayList<String> composition : compositions){					
					StockComposition stockComposition =  new StockComposition();
					//stock can come from old new harvest or old years
					String stockstring = composition.get(1);
					Stock mixFrom = null;
					if (this.stockMap.containsKey(stockstring))
					{
						mixFrom = stockMap.get(stockstring);
					}else{
						String stockid = composition.get(0);
						if (String.valueOf(stockid).equalsIgnoreCase("null")){
							JOptionPane.showMessageDialog(null, "Please make sure the stocks in the bulks exist(sync the stocks first). ", "Error", JOptionPane.ERROR_MESSAGE);
						}
						mixFrom = (Stock) session.load(Stock.class, Integer.parseInt(String.valueOf(stockid)));
					}
					
					int quantity = composition.get(2).equals("null")? 0 : Integer.parseInt(composition.get(2));
					MeasurementUnit unit = composition.get(3).equals("null")? null : (MeasurementUnit)session.load(MeasurementUnit.class, Integer.parseInt(composition.get(3)));
					stockComposition.setStockByStockId(stock);
					stockComposition.setStockByMixFromStockId(mixFrom);
					stockComposition.setMixQuantity(quantity == 0 ? null : quantity);
					stockComposition.setMeasurementUnit(unit);	
					session.save(stockComposition);
				}				
			}
			if ( row % 100 == 0 ) { 
		        session.flush();
		        session.clear();
		    }
		}
	}
	
	private void getExistingStocks(){
		CheckBoxIndexColumnTable table = harvesting.getStickerGenerator().getStickerTablePanel().getTable();
		ArrayList<String> stockNames = new ArrayList<String>();
		for(int row = 0; row < table.getRowCount(); ++row)
		{
			String stockName = (String) table.getValueAt(row, ColumnConstants.STOCK_NAME);
			stockNames.add(stockName);
		}
		List<Stock> stocks = StockDAO.getInstance().getStocksByNames(stockNames);
		for(Stock s : stocks) {
			this.stockMap.put(s.getStockName(), s);
		}
	}

	

	private Stock createStock(String stockName, int packetRow, String accession, String pedigree, String generation,Session session) {
		CheckBoxIndexColumnTable table = harvesting.getStickerGenerator().getStickerTablePanel().getTable();
		Stock stock = new Stock();
		stock.setStockName(stockName);
		
		if(table.getValueAt(packetRow, ColumnConstants.DATE) instanceof String){
			Date date = new Date() ;
	        SimpleDateFormat strToDate = new SimpleDateFormat ("E MMM dd HH:mm:ss Z yyyy",Locale.getDefault());
	        // parse format String to date
	        try {
				date = (Date)strToDate.parse((String) table.getValueAt(packetRow, ColumnConstants.DATE));
				stock.setStockDate(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			stock.setStockDate((Date) table.getValueAt(packetRow, ColumnConstants.DATE));
		}	
		
		Passport passport = new Passport();
		passport.setAccession_name(accession);
		passport.setAccession_identifier("NA");
		passport.setPedigree(pedigree);
		session.save(passport);
		stock.setPassport(passport);
		
		StockGeneration sg = findOrInsertStockGeneration(generation,null, session);
		stock.setStockGeneration(sg);

		return stock;
	}
	
	private StockGeneration findOrInsertStockGeneration(String generation, String cycle, Session session){
		String key = generation+"-"+String.valueOf(cycle);
		if(newStockGenerationMap.containsKey(key)){
			return newStockGenerationMap.get(key);
		}
		StockGeneration sg = new StockGeneration();
		sg.setGeneration(generation);
		sg.setCycle(cycle);
		session.save(sg);
		newStockGenerationMap.put(key, sg);
		return sg;
	}
	
	private String validate(String value) {
		if(StringUtils.isBlank(value)) {
			return null;
		}
		return value.toUpperCase();
	}

	@Override
	protected void done() {
		harvesting.getStickerGenerator().finishedSync(rollBacked);
	}

}
