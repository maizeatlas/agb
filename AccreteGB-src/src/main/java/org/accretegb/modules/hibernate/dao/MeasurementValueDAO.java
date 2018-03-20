package org.accretegb.modules.hibernate.dao;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.MeasurementValue;
import org.accretegb.modules.hibernate.MeasurementParameter;
import org.accretegb.modules.hibernate.ObservationUnit;
import org.accretegb.modules.hibernate.Stock;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("measurementValueDAO")
public class MeasurementValueDAO {
	
	
	private static MeasurementValueDAO instance = null;	
	public static MeasurementValueDAO getInstance() {
	      if(instance == null) {
	         instance = new MeasurementValueDAO();
	      }
	      return instance;
	}
	private  final String countOnField = "SELECT COUNT(*) FROM MeasurementValue WHERE field_id = ";
	private  final String findByParameterId = "SELECT * FROM measurement_value WHERE measurement_parameter_id = ";
	private  final String findByInfo = "SELECT * FROM measurement_value WHERE LOWER(observation_unit_id) = :observation_unit_id"
			+ " AND LOWER(measurement_parameter_id) = :measurement_parameter_id"
			+ " AND LOWER(value) = :value"
			+ " AND LOWER(tom) = :tom";
	
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;
	public  long getNumberOfObservationUnit(int fieldId) {
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Query query = session.createQuery(countOnField + fieldId);
		long count = ((Number)query.uniqueResult()).longValue();
		session.close();
		return count;
	}
	
	public  boolean findByParameterId(int parameterId){
		
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Query query = session.createSQLQuery(findByParameterId + parameterId);
		List result = query.list();
		session.close();
		if(result.size()>0){
			return true;
		}else{
			return false;
		}
			
	}
	
	public int insert(int observationUnitId, int parameterId, String value, Date time, Session session){
		try{
			MeasurementValue measurement = new MeasurementValue();
			MeasurementParameter measurementParameter= (MeasurementParameter) session.load(MeasurementParameter.class, parameterId);
			ObservationUnit ObservationUnit= (ObservationUnit) session.load(ObservationUnit.class, observationUnitId);		
			
			measurement.setMeasurementParameter(measurementParameter);
			measurement.setObservationUnit(ObservationUnit);
			measurement.setValue(value);
			measurement.setTom(time);				
			session.save(measurement);
			return measurement.getMeasurementValueId();
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in insert " + ex);
			}
			ex.printStackTrace();
			return 0;
		} 
	
	}
	
	public void update(int observationUnitId, int parameterId, String value, Date time, int measurementId,Session session){
		try{
			MeasurementValue measurement = new MeasurementValue();
			MeasurementParameter measurementParameter= (MeasurementParameter) session.load(MeasurementParameter.class, parameterId);
			ObservationUnit ObservationUnit= (ObservationUnit) session.load(ObservationUnit.class, observationUnitId);		
			measurement.setMeasurementParameter(measurementParameter);
			measurement.setObservationUnit(ObservationUnit);
			measurement.setValue(value);
			measurement.setTom(time);
			measurement.setMeasurementValueId(measurementId);
			session.merge(measurement);
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in update " + ex);
			}
			ex.printStackTrace();

		} 
		
		
	}

}