package org.accretegb.modules.germplasm.planting;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.hibernate.*;
import org.accretegb.modules.hibernate.dao.ExperimentFactorValueDAO;
import org.accretegb.modules.hibernate.dao.MateMethodConnectDAO;
import org.accretegb.modules.hibernate.dao.ObservationUnitDAO;
import org.accretegb.modules.hibernate.dao.StockDAO;
import org.accretegb.modules.util.LoggerUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class SyncTags extends SwingWorker<Void, Void> {

	private CheckBoxIndexColumnTable table;
	private int fieldId;
	private JProgressBar progress;
	private TagGenerator tagGenerator;
	private boolean rollbacked;
	private HashMap<Integer, Stock> foundStock = new HashMap<Integer, Stock>();
	private HashMap<Integer, ExperimentFactorValue> FoundExpValue = new HashMap<Integer, ExperimentFactorValue>();
	private Field foundField = null;
	private HashMap<Integer, ObservationUnit> foundTag= new HashMap<Integer, ObservationUnit>();
	private HashMap<Integer, MateMethodConnect> foundMateMethodConnect= new HashMap<Integer, MateMethodConnect>();

	public SyncTags(CheckBoxIndexColumnTable table, int fieldId, JProgressBar progress, TagGenerator tagGenerator) {
		this.table = table;
		this.fieldId = fieldId;
		this.progress = progress;
		this.tagGenerator = tagGenerator;
	}

	@Override
	protected Void doInBackground() throws Exception {
		tagGenerator.setEnabled(false);
		progress.setValue(0);
		progress.setString("Syncing..." + progress.getValue() + "%");
		syncWithDatabase();
		return null;
	}

	private void syncWithDatabase() {
		fillExperimentFactorValueTableForPlants();
		if(rollbacked == true){

		}else{
			SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
			Session session = sessionFactory.openSession();
			Transaction transaction = session.beginTransaction();
			try {
				long startTime = System.currentTimeMillis();
				ArrayList<String> tagStrings = new ArrayList<String>();
				ArrayList<Integer> stockIds = new ArrayList<Integer>();
				for(int row=0; row<table.getRowCount(); row++) {
					if(((String)table.getValueAt(row, ColumnConstants.STOCK_NAME)).equals("Filler"))
					{
						table.setValueAt(false, row, table.getIndexOf(ColumnConstants.MODIFIED));
						continue;
					}
					if(!(Boolean)table.getValueAt(row, ColumnConstants.MODIFIED))
					{
						continue;
					}
					String tag = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.TAG)));
					tagStrings.add(tag);

					if (this.foundField == null){
						this.foundField = (Field) session.get(Field.class, this.fieldId);
					}
					int stockid = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.STOCK_ID)));
					stockIds.add(stockid);

					int mateingPlanId = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.MATING_PLANT_ID)));
					if( mateingPlanId!= 0 ){
						MateMethodConnect mateMethodConnect = MateMethodConnectDAO.getInstance().getMateMethodConnectReference(mateingPlanId,session);
						foundMateMethodConnect.put(row, mateMethodConnect);
					}
				}

				ArrayList<ObservationUnit> obs = ObservationUnitDAO.getInstance().getObservationUnits(tagStrings, session);
				for(ObservationUnit ob : obs) {
					for(int row=0; row<table.getRowCount(); row++) {				
						String tag = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.TAG)));
						if (tag.equals(ob.getTagname())){
							foundTag.put(row, ob);
						}
					}
				}
				foundStock = StockDAO.getInstance().getStocks(stockIds, session);
				for(int row=0; row<table.getRowCount(); row++) {
					if(((String)table.getValueAt(row, ColumnConstants.STOCK_NAME)).equals("Filler"))
					{
						table.setValueAt(false, row, table.getIndexOf(ColumnConstants.MODIFIED));
						continue;
					}
					if(!(Boolean)table.getValueAt(row, ColumnConstants.MODIFIED))
					{
						continue;
					}
					ObservationUnit tag  = null;
					if(foundTag.containsKey(row)){
						ObservationUnit tagObj = foundTag.get(row);
						tag = populateTag(tagObj, row);
						session.merge(tag);
					}else{
						tag = populateTag(null, row);
						session.save(tag);
					}
					table.setValueAt(tag.getObservationUnitId(), row, table.getIndexOf(ColumnConstants.TAG_ID));
					//fill Exp_factor_value_table with observation_unit_ids
					Object divExpFactValueId= table.getValueAt( row, table.getIndexOf(ColumnConstants.EXP_FACTOR_VALUE_IDS));
					//NEED TO TEST THE MAP
					if(divExpFactValueId!=null){
						String[] factorValueIds = ((String) divExpFactValueId).split("\\s+");
						for(String id :factorValueIds ){
							ExperimentFactorValueDAO.getInstance().updateWithObs(Integer.parseInt(id), tag.getObservationUnitId(), session);
						}
					}
					if ( row % 1000 == 0 ) { //100, same as the JDBC batch size
						//flush a batch of inserts and release memory:
						session.flush();
						session.clear();
					}
					table.setValueAt(false, row, table.getIndexOf(ColumnConstants.MODIFIED));
					progress.setValue((int) ((row*1.0/table.getRowCount())*100));
					progress.setString("Syncing..." + progress.getValue() + "%");
				}
				transaction.commit();

				long endTime = System.currentTimeMillis();
				System.out.println("That took " + (endTime - startTime) + " milliseconds");
			}catch(Exception e) {
				transaction.rollback(); //table info need to rollback too.
				for(int row=0; row<table.getRowCount(); row++) {
					table.setValueAt(true, row, table.getIndexOf(ColumnConstants.MODIFIED));
					table.setValueAt(null, row, table.getIndexOf(ColumnConstants.TAG));
				} 
				if(LoggerUtils.isLogEnabled())
					LoggerUtils.log(Level.INFO,  e.toString());
				rollbacked = true;
			} finally {
				session.close();
			}
		}

	}

	private void fillExperimentFactorValueTableForPlants(){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		try {
			int currentRowForPlant = 0;
			Transaction transaction = session.beginTransaction();
			for(int row=0; row<table.getRowCount(); row++) {
				if(!String.valueOf(table.getValueAt(row, ColumnConstants.REP)).equals("null")){
					if(((String)table.getValueAt(row, ColumnConstants.TYPES)).equals(ColumnConstants.ROW)){
						currentRowForPlant = row;
					}
					if(((String)table.getValueAt(row, ColumnConstants.TYPES)).equals("   Plant")){
						Object divExpFactValueId= table.getValueAt( currentRowForPlant, table.getIndexOf(ColumnConstants.EXP_FACTOR_VALUE_IDS));
						String[] factorValueIds = ((String) divExpFactValueId).split("\\s+");
						for(String id :factorValueIds ){
							ExperimentFactorValue original= (ExperimentFactorValue) session.load(ExperimentFactorValue.class, Integer.parseInt(id));
							this.FoundExpValue.put(Integer.valueOf(id), original);
						}
					}	
				}
			}
			for(int row=0; row<table.getRowCount(); row++) {
				if(!String.valueOf(table.getValueAt(row, ColumnConstants.REP)).equals("null")){
					if(((String)table.getValueAt(row, ColumnConstants.TYPES)).equals(ColumnConstants.ROW)){
						currentRowForPlant = row;
					}
					if(((String)table.getValueAt(row, ColumnConstants.TYPES)).equals("   Plant")){
						if(!String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.EXP_FACTOR_VALUE_IDS))).equalsIgnoreCase("null")){
							// experiment ids are already there
							continue;
						}
						Object divExpFactValueId= table.getValueAt(currentRowForPlant, table.getIndexOf(ColumnConstants.EXP_FACTOR_VALUE_IDS));
						String[] factorValueIds = ((String) divExpFactValueId).split("\\s+");
						String copiedFactorValueIds = ExperimentFactorValueDAO.getInstance().copy(factorValueIds, this.FoundExpValue, session);
						table.setValueAt(copiedFactorValueIds, row, table.getIndexOf(ColumnConstants.EXP_FACTOR_VALUE_IDS));
					}	
				}
				if ( row % 1000 == 0 ) { //100, same as the JDBC batch size
					//flush a batch of inserts and release memory:
					session.flush();
					session.clear();
				}
			}

			transaction.commit();
		}catch(Exception e) {
			if(LoggerUtils.isLogEnabled()) LoggerUtils.log(Level.INFO,  e.toString());
			rollbacked = true; 
			for(int row=0; row<table.getRowCount(); row++) {
				if(!String.valueOf(table.getValueAt(row, ColumnConstants.REP)).equals("null")){
					if(((String)table.getValueAt(row, ColumnConstants.TYPES)).equals("   Plant")){
						table.setValueAt("", row, table.getIndexOf(ColumnConstants.EXP_FACTOR_VALUE_IDS));
					}	
				}
			}
			
		}finally{
			session.close();
		}

	}

	private  ObservationUnit populateTag(ObservationUnit tagObj, int row) {
		MateMethodConnect mateMethodConnect =foundMateMethodConnect.get(row);
		int stockid = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.STOCK_ID)));
		Stock stock = this.foundStock.get(stockid);
		int coordX = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.X)));
		int coordY = Integer.parseInt(String.valueOf(table.getValueAt(row, ColumnConstants.Y)));
		int plot = Integer.parseInt( String.valueOf(table.getValueAt(row, ColumnConstants.ROW)));
		String plant = String.valueOf(table.getValueAt(row, ColumnConstants.PLANT));
		String tagName = (String)table.getValueAt(row, ColumnConstants.TAG);
		String purpose = (String)table.getValueAt(row, ColumnConstants.PURPOSE);
		String kernelsString = String.valueOf(table.getValueAt(row, ColumnConstants.KERNELS));
		if(kernelsString.equalsIgnoreCase("null")) {
			kernelsString = null;
		}
		int kernel = Integer.parseInt( StringUtils.defaultIfBlank(kernelsString, "-1"));		
		String delayString = String.valueOf(table.getValueAt(row, ColumnConstants.DELAY));
		if(delayString.equalsIgnoreCase("null")) {
			delayString = null;
		}
		int delay = Integer.parseInt( StringUtils.defaultIfBlank(delayString, "-1"));
		String comment = (String)table.getValueAt(row, ColumnConstants.COMMENT);
		DateFormat dateFormatRead = new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy",Locale.getDefault());		
		Date plantDate = null;
		try {
			plantDate = dateFormatRead.parse(String.valueOf(table.getValueAt(row, ColumnConstants.PLANTING_DATE)));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ObservationUnit tag = ObservationUnitDAO.getInstance().saveOrUpdateTag(tagObj, coordX, coordY, plot, plant, tagName, 
				purpose,kernel, delay, plantDate, comment, mateMethodConnect,stock, foundField);
		return tag;
	}

	@Override
	public void done() {
		tagGenerator.finishedSync(rollbacked);
	}

}
