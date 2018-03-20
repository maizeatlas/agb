package org.accretegb.modules.germplasm.experimentaldesign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.accretegb.modules.constants.ExperimentConstants;
import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.hibernate.ExperimentFactor;
import org.accretegb.modules.hibernate.ExperimentFactorValue;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.dao.ExperimentDAO;
import org.accretegb.modules.hibernate.dao.ExperimentFactorValueDAO;
import org.accretegb.modules.hibernate.dao.MeasurementUnitDAO;
import org.hibernate.Session;

public class SyncExperimentalDesign extends SwingWorker<Void, Void> {

    private CheckBoxIndexColumnTable table;
    private JProgressBar progress;
    private long initialTime;
    private List<Integer> expFactValIds;
    private String units = "";
    private ExperimentSelectionPanel experimentSelectionPanel;

    public SyncExperimentalDesign(CheckBoxIndexColumnTable table,ExperimentSelectionPanel experimentSelectionPanel) {
        this.table = table;
        this.progress = experimentSelectionPanel.progress;
        this.experimentSelectionPanel = experimentSelectionPanel;
    }

    @Override
    protected Void doInBackground() throws Exception {
        initialTime = System.currentTimeMillis();
        progress.setVisible(true);
        progress.setValue(0);
        progress.setIndeterminate(false);
        progress.setString(progress.getValue() + "%");
        long duration = System.currentTimeMillis() - initialTime;
        System.out.println("Opening connection: " + duration / 1000);
        List<String> factorNames = new ArrayList<String>();
		HashMap<String,ExperimentFactor> divExpFactors= new HashMap<String,ExperimentFactor>();
		for(String name : experimentSelectionPanel.getDesignFactorNames())
		{
			ExperimentFactor divExpFact = experimentSelectionPanel.manageExperimentFactor(name,"design",null,null);
			divExpFactors.put(name, divExpFact);
			factorNames.add(name);
		}
		
		String trt1 = "";
		String trt2 = "";
		String stock = "";
		if(experimentSelectionPanel.getChosenTrtsInfo() == null)
	    {
			experimentSelectionPanel.submitDesignSelectionButton();
	    }
	    if(experimentSelectionPanel.getChosenTrtsInfo().size() == 1)
		{
			String name = (String) experimentSelectionPanel.getChosenTrtsInfo().keySet().toArray()[0];
			List<String> values = experimentSelectionPanel.getChosenTrtsInfo().get(name);
			ExperimentFactor divExpFact = experimentSelectionPanel.manageExperimentFactor(name,ExperimentConstants.TREATMENT,
					(values.get(0).trim().equals(ExperimentConstants.DESCRIPTION)|| values.get(0).trim().equals(""))? null : values.get(0),
			    	(values.get(1).trim().equals(ExperimentConstants.COMMENT)|| values.get(1).trim().equals(""))? null : values.get(1));				
			divExpFactors.put(name, divExpFact);
			factorNames.add(name);	
			stock = name;
		}else{
			trt1 = (String) experimentSelectionPanel.getChosenTrtsInfo().keySet().toArray()[0];
			trt2 = (String) experimentSelectionPanel.getChosenTrtsInfo().keySet().toArray()[1];
			List<String> values1 = experimentSelectionPanel.getChosenTrtsInfo().get(trt1);
			List<String> values2 = experimentSelectionPanel.getChosenTrtsInfo().get(trt2);
			ExperimentFactor divExpFact1;
			ExperimentFactor divExpFact2;
			if(values1.size() == 2)
		    {
		    	divExpFact1 = experimentSelectionPanel.manageExperimentFactor(trt1,ExperimentConstants.TREATMENT,
		    			(values1.get(0).trim().equals(ExperimentConstants.DESCRIPTION) || values1.get(0).trim().equals(""))? null : values1.get(0),
		    		    (values1.get(1).trim().equals(ExperimentConstants.COMMENT)|| values1.get(1).trim().equals(""))? null : values1.get(1));
		    	divExpFact2 = experimentSelectionPanel.manageExperimentFactor(trt2,ExperimentConstants.TREATMENT,
		    			(values2.get(1).trim().equals(ExperimentConstants.DESCRIPTION)|| values2.get(1).trim().equals(""))? null : values2.get(1),
		    			(values2.get(2).trim().equals(ExperimentConstants.COMMENT)|| values2.get(2).trim().equals(""))? null : values2.get(2));
		    	units = values2.get(3).equals(ExperimentConstants.UNIT_TYPE)  ? "" : values2.get(3);
		    	
		    }else{
		    	divExpFact1 = experimentSelectionPanel.manageExperimentFactor(trt1,ExperimentConstants.TREATMENT,
		    			(values1.get(1).trim().equals(ExperimentConstants.DESCRIPTION)|| values1.get(1).trim().equals(""))? null : values1.get(1),
		    			(values1.get(2).trim().equals(ExperimentConstants.COMMENT)|| values1.get(2).trim().equals(""))? null : values1.get(2));
		    	divExpFact2 = experimentSelectionPanel.manageExperimentFactor(trt2,ExperimentConstants.TREATMENT,
		    			(values2.get(0).trim().equals(ExperimentConstants.DESCRIPTION)|| values2.get(0).trim().equals(""))? null : values2.get(0),
				    	(values2.get(1).trim().equals(ExperimentConstants.COMMENT)|| values2.get(1).trim().equals(""))? null : values2.get(1));
		    	units = values1.get(3).equals(ExperimentConstants.UNIT_TYPE) ? "" : values1.get(3);
		    }
			divExpFactors.put(trt1, divExpFact1);
			divExpFactors.put(trt2, divExpFact2);
			factorNames.add(trt1);	
			factorNames.add(trt2);	
		}
		expFactValIds = new ArrayList<Integer>();
		int expId = Integer.parseInt(experimentSelectionPanel.getExperimentId());	
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		for (int rowCounter = 0; rowCounter < table.getRowCount(); rowCounter++) {
			StringBuilder expFactorValueIds = new StringBuilder();	
			for(String name: factorNames){
			    ExperimentFactor divExpFactor = divExpFactors.get(name);	
			    String valueLevel = "";
			    if(name.equals(trt1)) {
			    	valueLevel = (String)table.getValueAt(rowCounter, table.getColumnModel().getColumnIndex("trt1"));
			    }else if(name.equals(trt2)){
			    	valueLevel = (String)table.getValueAt(rowCounter, table.getColumnModel().getColumnIndex("trt2"));
			    }else if(name.equals(stock)){
			    	valueLevel = (String)table.getValueAt(rowCounter, table.getColumnModel().getColumnIndex(ColumnConstants.STOCK_NAME));
			    } else{
			    	valueLevel = (String)table.getValueAt(rowCounter, table.getColumnModel().getColumnIndex(name));
			    }
			    ExperimentFactorValue divExpFacVal = ExperimentFactorValueDAO.getInstance().insert(session,divExpFactor, expId, valueLevel);
			    table.setValueAt(experimentSelectionPanel.getExperimentId(), rowCounter,table.getColumnModel().getColumnIndex(ColumnConstants.EXP_ID));
			    expFactorValueIds.append(Integer.toString(divExpFacVal.getExperimentFactorValueId())+" ");
			    expFactValIds.add(divExpFacVal.getExperimentFactorValueId());
			}
			String valueIds = expFactorValueIds.toString().trim();
			table.setValueAt(valueIds, rowCounter,table.getColumnModel().getColumnIndex(ColumnConstants.EXP_FACTOR_VALUE_IDS));	
			table.setHasSynced(true);
			progress.setValue((int) ((rowCounter * 1.0 / table.getRowCount()) * 100));
			progress.setString(progress.getValue() + "%");
			
		}
		session.close();
        return null;
    }

    @Override
    public void done() {
        long duration = System.currentTimeMillis() - initialTime;        
	        System.out.println("Time: " + duration / 1000);
	        experimentSelectionPanel.setIsSyncedFactorValueIds(expFactValIds);	        
        if(!units.isEmpty())
		{
			int unitsId = MeasurementUnitDAO.getInstance().insertOrUpdate(units);
			for(int id : expFactValIds){
				ExperimentFactorValueDAO.getInstance().updateWithUnit(id, unitsId);
			}
		}		
		table.repaint();
        progress.setVisible(false);
        
    }
}
