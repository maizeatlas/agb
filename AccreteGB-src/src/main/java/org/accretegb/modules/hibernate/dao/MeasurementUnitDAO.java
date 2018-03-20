package org.accretegb.modules.hibernate.dao;

import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.MeasurementUnit;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

@Component("measurementUnitDAO")
public class MeasurementUnitDAO {
	
	private static MeasurementUnitDAO instance = null;	
	public static MeasurementUnitDAO getInstance() {
	      if(instance == null) {
	         instance = new MeasurementUnitDAO();
	      }
	      return instance;
	}
	private  HibernateSessionFactory hibernateSessionFactory;

	@SuppressWarnings({ "-access", "unchecked" })
	public  List<MeasurementUnit> findAll() {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Query query = session.createSQLQuery("select * from measurement_unit").addEntity(MeasurementUnit.class);
		List<MeasurementUnit> unitOfMeasureList = (List<MeasurementUnit>)query.list();
		session.close();
		return unitOfMeasureList;
	}
	
	public  List<MeasurementUnit> findByID(int id) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Query query = session.createSQLQuery("select * from measurement_unit where measurement_unit_id = "+id)
				.addEntity(MeasurementUnit.class);
		List<MeasurementUnit> unitOfMeasureList = (List<MeasurementUnit>)query.list();
		session.close();
		return unitOfMeasureList;
	}
	
	
	public  int insertOrUpdate(String units){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		Query query = session.createSQLQuery("select * from measurement_unit where units = +'"+units+"'")
				.addEntity(MeasurementUnit.class);
		if(query.list().size()>0)
		{
			List<MeasurementUnit> list =  query.list();
			session.close();
			return (int)(list.get(0)).getMeasurementUnitId();
			
		}else{
			if(units!=null && units!=""){
				MeasurementUnit measurementUnit = new MeasurementUnit();
				measurementUnit.setUnitType(units);
				session.saveOrUpdate(measurementUnit);
				transaction.commit();
				session.close();
				return measurementUnit.getMeasurementUnitId();
			}
			
		}
		return 0;
			
	}

}