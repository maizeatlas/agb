package org.accretegb.modules.sampling;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
		int index = 0;
		for(String subsetName : sampleSettingPanel.getSubsetTableMap().keySet()){
			index++;
        	Object[][] subsetData =	(Object[][]) sampleSettingPanel.getSubsetTableMap().get(subsetName);
				if (subsetData != null){
					int rows = subsetData.length;
					SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
					Session session = sessionFactory.openSession();
					Transaction transaction = session.beginTransaction();
					for(int row = 0; row < rows; row ++){
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
								JOptionPane.showMessageDialog(null, "Imported tom columns have invalid date format. Format has to be MM/dd/yy.");
								break;
							}
						}
						
						String sampleName = (String)table.getValueAt(row, table.getIndexOf(ColumnConstants.SAMPLENAME));
						String comment = sampleSettingPanel.getSubsetCommentMap().get(subsetName);
						ObservationUnitSampleDAO.getInstance().insert(tagId, sourceId, sampleDate, sampleName, comment,session);
						if ( row % 1000 == 0 ) { 
							session.flush();
							session.clear();
						}
					}
					transaction.commit();
					session.close();
				}
			progress.setValue((int) ((index*1.0/sampleSettingPanel.getSubsetTableMap().size())*100));
			progress.setString("Syncing..." + progress.getValue() + "%");
		}
	}
	
	
	
	@Override
	public void done() {
		sampleSettingPanel.finishedSync(rollbacked);
	}

}
