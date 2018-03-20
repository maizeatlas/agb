package org.accretegb.modules.germplasm.stockannotation;


import java.awt.Component;
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
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.accretegb.modules.config.AccreteGBContext;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.hibernate.Mate;
import org.accretegb.modules.hibernate.MateMethodConnect;
import org.accretegb.modules.hibernate.MeasurementUnit;
import org.accretegb.modules.hibernate.MateMethod;
import org.accretegb.modules.hibernate.ObservationUnit;
import org.accretegb.modules.hibernate.StockComposition;
import org.accretegb.modules.hibernate.Passport;
import org.accretegb.modules.hibernate.Stock;
import org.accretegb.modules.hibernate.StockPacket;
import org.accretegb.modules.hibernate.dao.MateMethodConnectDAO;
import org.accretegb.modules.hibernate.dao.MeasurementUnitDAO;
import org.accretegb.modules.hibernate.dao.ObservationUnitDAO;
import org.accretegb.modules.hibernate.dao.PassportDAO;
import org.accretegb.modules.hibernate.dao.StockCompositionDAO;
import org.accretegb.modules.hibernate.dao.StockDAO;
import org.accretegb.modules.hibernate.dao.StockPacketDAO;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.accretegb.modules.constants.ColumnConstants;

public class SyncPassport extends SwingWorker<Void, Void> {

	private StockAnnotationPanel stockAnnotation;
	private boolean rollBacked;
	
	public SyncPassport(StockAnnotationPanel stockAnnotation) {
		this.stockAnnotation = stockAnnotation;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		
		JProgressBar progress = stockAnnotation.getProgressBar();
		progress.setVisible(true);
		try {
			CheckBoxIndexColumnTable table = stockAnnotation.getStockTablePanel().getTable();
			for(int row=0; row<table.getRowCount(); row++) {
				String passport_id = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.PASSPORT_ID)));
				String classification_id = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.CLASSIFICATION_ID)));
				String taxonomy_id = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.TAXONOMY_ID)));
				String accession = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.ACCESSION)));
				String pedigree = String.valueOf(table.getValueAt(row, table.getIndexOf(ColumnConstants.PEDIGREE)));
				if(!Utils.isInteger(passport_id)){
					passport_id = "0";
				}
				if(!Utils.isInteger(classification_id)){
					classification_id = "0";
				}
				if(!Utils.isInteger(taxonomy_id)){
					taxonomy_id = "0";
				}
				
				PassportDAO.getInstance().insert(Integer.parseInt(classification_id), 
						Integer.parseInt(taxonomy_id), Integer.parseInt(passport_id),
						accession, pedigree);
				progress.setValue((int) ((row*1.0/table.getRowCount())*100));
			    rollBacked = false;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			rollBacked = true;
			
		} 
		return null;
	}

	
	@Override
	protected void done() {
		stockAnnotation.getStockTablePanel().getTable().setHasSynced(!rollBacked);
		stockAnnotation.getStockTablePanel().getTable().clearSelection();
		stockAnnotation.getProgressBar().setVisible(false);
	}

}
