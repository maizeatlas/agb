package org.accretegb.modules.hibernate.dao;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.MeasurementParameter;
import org.accretegb.modules.hibernate.MeasurementUnit;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("measurementParameterDAO")
public class MeasurementParameterDAO {

	private static MeasurementParameterDAO instance = null;	
	public static MeasurementParameterDAO getInstance() {
	      if(instance == null) {
	         instance = new MeasurementParameterDAO();
	      }
	      return instance;
	}
	private  final String SELECTALL= "SELECT * FROM measurement_parameter ";

	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;

	public  List<MeasurementParameter> findAll() {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Query query = session.createSQLQuery(SELECTALL)
				.addEntity(MeasurementParameter.class);
		List<MeasurementParameter> measurementParameters = (List<MeasurementParameter>)query.list();
		session.close();
		return measurementParameters;
	}
	
	public  int insert(List<String> info){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			int unitsId = MeasurementUnitDAO.getInstance().insertOrUpdate(info.get(11));
			MeasurementUnit munit = new MeasurementUnit();
			munit.setMeasurementUnitId(unitsId);
					
			MeasurementParameter dmp = new MeasurementParameter();
			dmp.setMeasurementUnit(unitsId==0?null:munit);
			dmp.setOntologyAccession(info.get(3));
			dmp.setMeasurementClassification(info.get(2));
			dmp.setParameterName(info.get(0));
			dmp.setParameterCode(info.get(1));
			dmp.setFormat(info.get(5));
			dmp.setDefaultValue(info.get(6));
			dmp.setMinValue(info.get(7));
			dmp.setMaxValue(info.get(8));
			dmp.setCategories(info.get(9));
			dmp.setIsVisible(info.get(10));
			dmp.setProtocol(info.get(4));				
			session.save(dmp);	
			transaction.commit();	
			return dmp.getMeasurementParameterId();
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in insert" + ex);
			}
			ex.printStackTrace();
			return 0;
		} finally {
			session.close();
		}	
	
		
	}
	
	public void update(List<String> info){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		Transaction transaction = session.beginTransaction();
		try{
			int unitsId = MeasurementUnitDAO.getInstance().insertOrUpdate(info.get(11));
			MeasurementUnit munit = new MeasurementUnit();
			munit.setMeasurementUnitId(unitsId);					
			MeasurementParameter dmp = new MeasurementParameter();
			dmp.setMeasurementUnit(unitsId==0?null:munit);
			dmp.setOntologyAccession(info.get(3));
			dmp.setMeasurementClassification(info.get(2));
			dmp.setParameterName(info.get(0));
			dmp.setParameterCode(info.get(1));
			dmp.setFormat(info.get(5));
			dmp.setDefaultValue(info.get(6));
			dmp.setMinValue(info.get(7));
			dmp.setMaxValue(info.get(8));
			dmp.setCategories(info.get(9));
			dmp.setIsVisible(info.get(10));
			dmp.setProtocol(info.get(4));		
			dmp.setMeasurementParameterId(Integer.parseInt(info.get(12)));
			session.update(dmp);			
			transaction.commit();				
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in update" + ex);
			}
			ex.printStackTrace();
		} finally {
			session.close();
		}	
		
		
	}
	
	
	public  void delete(int measurementParameterid){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			MeasurementParameter measurementParameter= (MeasurementParameter) session.get(MeasurementParameter.class, measurementParameterid);
			session.delete(measurementParameter);
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

}