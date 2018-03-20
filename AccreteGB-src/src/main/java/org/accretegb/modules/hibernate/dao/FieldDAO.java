package org.accretegb.modules.hibernate.dao;

import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.Field;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.util.LoggerUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

@Component("fieldDAO")
public class FieldDAO {

	private static FieldDAO instance = null;	
	public static FieldDAO getInstance() {
	      if(instance == null) {
	         instance = new FieldDAO();
	      }
	      return instance;
	}
	
	public  List<Field> getFields() {
		List<Field> fields;
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		try {
			fields = session.createCriteria(Field.class).list();            
		} finally {
			session.close();
		}
		return fields;
	}

	public  boolean deleteField(int fieldId) {
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();        
		try {        	
			Transaction transaction = session.beginTransaction();
			Field field = (Field) session.get(Field.class, fieldId);
			session.delete(field);
			transaction.commit();
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
				LoggerUtils.log(Level.INFO, "error in delete" + ex);
			ex.printStackTrace();
			return false;
		} finally {
			session.close();
		}
		return true;
	}

	public  Field getField(int fieldId) {
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Field field;
		field = (Field) session.get(Field.class, fieldId);
		session.close();
		return field;
	}

	public  void saveField(Field field) {
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();

		try {
			Transaction transaction = session.beginTransaction();
			session.saveOrUpdate(field);
			transaction.commit();
		} catch (HibernateException ex) {           
			if(LoggerUtils.isLogEnabled())
				LoggerUtils.log(Level.INFO,  ex.toString());
		} finally {
			session.close();
		}   
	}
	
	public   List<Object[]> findByLocation(){
		  SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
	      Session session = sessionFactory.openSession();
	      String sqlStatement = "select location.location_id, field_id, field_name, field_number, altitude, city, "
                  + "state_province, country, zipcode  from field, location where "
                  + "field.location_id=location.location_id;";
          Query query = session.createSQLQuery(sqlStatement);
          @SuppressWarnings("unchecked")
          List<Object[]> results = query.list();
          session.close();
          return results;
	}
}