package org.accretegb.modules.germplasm.sampling;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.hibernate.*;
import org.accretegb.modules.hibernate.dao.ExperimentFactorValueDAO;
import org.accretegb.modules.hibernate.dao.MateMethodConnectDAO;
import org.accretegb.modules.hibernate.dao.ObservationUnitDAO;
import org.accretegb.modules.hibernate.dao.ObservationUnitSampleDAO;
import org.accretegb.modules.util.LoggerUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import antlr.collections.List;

public class syncSampling extends SwingWorker<Void, Void> {
	
	private CheckBoxIndexColumnTable table;
	private int fieldId;
	private JProgressBar progress;
	private SampleSettingPanel sampleSettingPanel;
	private boolean rollbacked;
	
	public syncSampling(JProgressBar progress, SampleSettingPanel sampleSettingPanel) {
		this.progress = progress;
		this.sampleSettingPanel = sampleSettingPanel;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		progress.setValue(0);
		progress.setString("Syncing..." + progress.getValue() + "%");
        try {
        	syncWithDatabase();
        } catch(Exception e) {
        	if(LoggerUtils.isLogEnabled())
				LoggerUtils.log(Level.INFO,  e.toString());
        	rollbacked = true;
        } finally {
        }
		return null;
	}

	private void syncWithDatabase() {
		CheckBoxIndexColumnTable table = sampleSettingPanel.getSampleSettingTablePanel().getTable();
		System.out.println("table  size: " +  table.getRowCount());
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		String subsetName = this.sampleSettingPanel.currentSubset;
    	Object[][] subsetData =	(Object[][]) sampleSettingPanel.getSubsetTableMap().get(subsetName);
    	System.out.println("sampling subset size: " +  subsetData.length);
    	try {
			if (subsetData != null){
				int rows = subsetData.length;
				ArrayList<String> sampleNames = new ArrayList<String>();
				for(int row = 0; row < rows; row ++){
					String sampleName = (String)table.getValueAt(row, table.getIndexOf(ColumnConstants.SAMPLENAME));
					sampleNames.add(sampleName);
				}
				HashMap<String, Integer> sampleNameIdMap = ObservationUnitSampleDAO.getInstance().getSampleNameIdMap(sampleNames);
				if (sampleNameIdMap.size() > 0) {	
					int option = JOptionPane.showOptionDialog(null, 
					        "Some sample names were found in the database.\n"
					        + "Choose 'Reset' to stop the sync and generate a new set of sample names by changing the prefix\n"
					        + "Choose 'Continue' to update exisitng sample data  ",
					        "", 
					        JOptionPane.YES_NO_CANCEL_OPTION, 
					        JOptionPane.INFORMATION_MESSAGE, 
					        null, 
					        new String[]{"Reset","Continue","Cancel"},
					        "default");
			        if(option ==JOptionPane.OK_OPTION )
			        {	
			        	// reset
			        	return;
			        }else if(option == JOptionPane.NO_OPTION){
			        	// override, continue	 
			        }else{
			        	progress.setVisible(false);
			        	return;
			        }
				}
				for(int row = 0; row < rows; row ++){
					String sampleName = (String)table.getValueAt(row, table.getIndexOf(ColumnConstants.SAMPLENAME));
					int tagId = (Integer) table.getValueAt(row, table.getIndexOf(ColumnConstants.TAG_ID));
					int sourceId = 0;
					if(table.getValueAt(row, table.getIndexOf(ColumnConstants.COLLECTOR))!=null)
					{
						sourceId = sampleSettingPanel.getNameSourceid().get(table.getValueAt(row, table.getIndexOf(ColumnConstants.COLLECTOR)));
					}
					Object d = table.getValueAt(row, table.getIndexOf(ColumnConstants.COLLECTION_DATE));
					Date sampleDate =  null;
					if ( d instanceof Date){
						sampleDate = (Date) d;
					}else{
						DateFormat formatter = new SimpleDateFormat("MM/dd/yy");
						try {
							sampleDate = (Date)formatter.parse(String.valueOf(d));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							JOptionPane.showMessageDialog(null, "Imported tom columns have invalid date format. Format has to be mm/dd/yy.");
							break;
						}
					}
					
					String comment = sampleSettingPanel.getSubsetCommentMap().get(subsetName);
					ObservationUnitSampleDAO.getInstance().insert(sampleNameIdMap.get(sampleName) == null ? 0 : sampleNameIdMap.get(sampleName), tagId, sourceId, sampleDate, sampleName, comment,session);
					if ( row % 1000 == 0 ) { 
						session.flush();
						session.clear();
					}
				}
				transaction.commit();
			}
		progress.setValue((int) ((1/subsetData.length)*100));
		progress.setString("Syncing..." + progress.getValue() + "%");
    	}catch(Exception e) {
			transaction.rollback(); //table info need to rollback too.
			System.out.println("sampling syncWithDatabase " + e.getMessage());
			if(LoggerUtils.isLogEnabled())
				LoggerUtils.log(Level.INFO,  e.toString());
			rollbacked = true;
		} finally {
			session.close();
		}
		
	}
	
	
	
	@Override
	public void done() {
		sampleSettingPanel.finishedSync(rollbacked);
	}

}
