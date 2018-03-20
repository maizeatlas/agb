package org.accretegb.modules.hibernate.dao;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.Experiment;
import org.accretegb.modules.hibernate.ExperimentFactor;
import org.accretegb.modules.hibernate.ExperimentFactorValue;
import org.accretegb.modules.hibernate.ObservationUnit;
import org.accretegb.modules.hibernate.Stock;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.hibernate.MeasurementUnit;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("experimentFactorValueDAO")
public class ExperimentFactorValueDAO {
	
	private static ExperimentFactorValueDAO instance = null;	
	public static ExperimentFactorValueDAO getInstance() {
	      if(instance == null) {
	         instance = new ExperimentFactorValueDAO();
	      }
	      return instance;
	}
	private  final String SELECTBYIdS = "SELECT * FROM experiment_factor_value WHERE LOWER(experiment_factor_id) = :experiment_factor_id "
			+ "AND LOWER(experiment_id) = :experiment_id";
	private  final String SELECTBYFACTORId = "SELECT distinct exp_factor_value_level FROM experiment_factor_value WHERE LOWER(experiment_factor_id) =";

	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;

	@SuppressWarnings({ "-access", "unchecked" })
	public  List<ExperimentFactorValue> findByIds(int experiment_factor_id, int experiment_id) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Query query = session.createSQLQuery(SELECTBYIdS)
				.addEntity(ExperimentFactorValue.class)
				.setParameter("experiment_factor_id", experiment_factor_id)
				.setParameter("experiment_id", experiment_id);	
		List<ExperimentFactorValue> experimentFactorList = (List<ExperimentFactorValue>)query.list();
		session.close();
		return experimentFactorList;
	}
	
	public  ExperimentFactorValue insert(Session session, ExperimentFactor experimentFactor, int experimentId,String expFactorValueLevel) {
		//Session session = hibernateSessionFactory.getSessionFactory().openSession();
		try{
			Transaction transaction = session.beginTransaction();
			Experiment experiment = new Experiment();
			experiment.setExperimentId(experimentId);		
			ExperimentFactorValue ExperimentFactorValue = new ExperimentFactorValue();
			ExperimentFactorValue.setExperiment(experiment);
			ExperimentFactorValue.setExperimentFactor(experimentFactor);
			ExperimentFactorValue.setExpFactorValueLevel(expFactorValueLevel);
			session.save(ExperimentFactorValue);
			transaction.commit();
			return ExperimentFactorValue;
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in insert" + ex);
			}
			ex.printStackTrace();
			return null;	
		}
		
	}
	
	public  void deleteAList(List<Integer> experimentFactorValueIds){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			for(Integer id : experimentFactorValueIds)
			{
				ExperimentFactorValue experimentFactorValue= (ExperimentFactorValue) session.get(ExperimentFactorValue.class, id);
				experimentFactorValue.setExperimentFactorValueId(id);
				session.delete(experimentFactorValue);
			}
			transaction.commit();
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in delete" + ex);
			}
			ex.printStackTrace();
		} finally {
			session.close();
		}	
	}
	
	public  void updateWithObs(int experimentFactorValueId, int obsUnitid, Session session){

		try{
			ExperimentFactorValue experimentFactorValue= (ExperimentFactorValue) session.get(ExperimentFactorValue.class, experimentFactorValueId);
			ObservationUnit observationUnit = new ObservationUnit();
			observationUnit.setObservationUnitId(obsUnitid);
			experimentFactorValue.setObservationUnitId(observationUnit.getObservationUnitId());
			session.update(experimentFactorValue);	
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in update" + ex);
			}
			ex.printStackTrace();
		} 	
	}
	
	public  void updateWithUnit(int experimentFactorValueId, int uniOfMeasureId){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		try{
			Transaction transaction = session.beginTransaction();
			ExperimentFactorValue experimentFactorValue= (ExperimentFactorValue) session.get(ExperimentFactorValue.class, experimentFactorValueId);
			MeasurementUnit measurementUnit = new MeasurementUnit();
			measurementUnit.setMeasurementUnitId((uniOfMeasureId));
			experimentFactorValue.setMeasurementUnit(uniOfMeasureId==0 ? null :measurementUnit);
			session.update(experimentFactorValue);
			transaction.commit();
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in delete" + ex);
			}
			ex.printStackTrace();
		} finally {
			session.close();
		}	
	}
	
	public  ExperimentFactorValue getExperimentFactorValue(int experimentFactorValueId){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		ExperimentFactorValue experimentFactorValue= (ExperimentFactorValue) session.get(ExperimentFactorValue.class, experimentFactorValueId);
		session.close();
		return experimentFactorValue;
	}
	
	public  String copy(String[] factorValueIds, HashMap<Integer, ExperimentFactorValue> FoundExpValue, Session session){		
		StringBuilder copiedfactorValueIds = new StringBuilder();
		for(String id :factorValueIds ){
			ExperimentFactorValue copy = new ExperimentFactorValue();
			try{
				ExperimentFactorValue original = FoundExpValue.get(Integer.parseInt(id));		
				copy.setExperiment(original.getExperiment());
				copy.setExperimentFactor(original.getExperimentFactor());
				copy.setExpFactorValueLevel(original.getExpFactorValueLevel());
				copy.setStock(original.getStock());
				copy.setMeasurementUnit(original.getMeasurementUnit());
				copy.setExpFactorValueComments(original.getExpFactorValueComments());
				session.save(copy);
			}catch (HibernateException ex) {
				if(LoggerUtils.isLogEnabled())
				{
					LoggerUtils.log(Level.INFO, "error in insert" + ex);
				}
				ex.printStackTrace();
			} 
			copiedfactorValueIds.append(String.valueOf(copy.getExperimentFactorValueId()) + " ");			
		}
		
		return copiedfactorValueIds.toString().trim();
	}
	
	public  List<String> findByFactorId(int experiment_factor_id) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		Query query = session.createSQLQuery(SELECTBYFACTORId + experiment_factor_id);
		List<String> uniqueValues = query.list();
		session.close();
		return uniqueValues;
	}

}