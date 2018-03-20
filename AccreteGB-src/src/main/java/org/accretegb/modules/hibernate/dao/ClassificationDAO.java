package org.accretegb.modules.hibernate.dao;

import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.Classification;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.util.LoggerUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

@Component("classificationDAO")
public class ClassificationDAO {
	 
	private static ClassificationDAO instance = null;	
	public static ClassificationDAO getInstance() {
	      if(instance == null) {
	         instance = new ClassificationDAO();
	      }
	      return instance;
	}
	
	public  void update(String values[], String oldClassificationCode){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
	    Session session = sessionFactory.openSession();   
	    try {  
	        Transaction transaction = session.beginTransaction();
	        Query query = session.createSQLQuery("SELECT * FROM classification WHERE classification_code = :classification_code")
	                .addEntity(Classification.class).setParameter("classification_code", oldClassificationCode);
	        Classification classification_code = ((Classification) query.list().get(0));
	        classification_code.setClassificationCode(values[0]);
	        classification_code.setClassificationType(values[1]);
	        session.update(classification_code);
	        transaction.commit();
	    }catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
				LoggerUtils.log(Level.INFO, "error in update" + ex);
			ex.printStackTrace();
		} finally {
			session.close();
		}
	       
	}
	
	public   List<Classification> findAll(){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
        Session session = sessionFactory.openSession();
       
        String sqlStatement = "SELECT * FROM classification";
        Query query = session.createSQLQuery(sqlStatement).addEntity(Classification.class);
        List<Classification> results = (List<Classification>) query.list();
        session.close();
        return results;
	}
	
	public  Classification findById(int classificationId){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
        Session session = sessionFactory.openSession();
        Query query = session
                .createSQLQuery("SELECT * FROM classification WHERE classification_id = :classification_id")
                .addEntity(Classification.class).setParameter("classification_id", classificationId);
        
        Classification classification = null;
        if(query.list().size() > 0){
        	classification = (Classification) query.list().get(0);
        }      
        session.close();
        return classification;
	}
	
	public  void delete(String classficationCode){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
	    Session session = sessionFactory.openSession();    
	    try{
		    Transaction transaction = session.beginTransaction();
		    Query query = session.createSQLQuery("SELECT * FROM classification WHERE classification_code = :classification_code")
		            .addEntity(Classification.class).setParameter("classification_code", classficationCode);
		    session.delete(((Classification) query.list().get(0)));
		    transaction.commit();
	    }catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
				LoggerUtils.log(Level.INFO, "error in delete" + ex);
			ex.printStackTrace();
		} finally {
			session.close();
		}
		
	}
	
	public  Classification insert(String[] values){
		  SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
          Session session = sessionFactory.openSession();
          try{
	          Transaction transaction = session.beginTransaction();
	          Classification classification = new Classification(values[0], values[1], null);
	          session.save(classification);
	          transaction.commit();
	          return classification;
          }catch (HibernateException ex) {
  			if(LoggerUtils.isLogEnabled())
  				LoggerUtils.log(Level.INFO, "error in insert" + ex);
  			ex.printStackTrace();
  			return null;
  		 } finally {
  			session.close();
  		 }
         
	}
	
	public  List<String> findClassification(){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
        Session session = sessionFactory.openSession();
		String sqlStatement = "SELECT CONCAT(classification_code, ' - ', classification_type) FROM classification ORDER BY classification_code";
        Query query = session.createSQLQuery(sqlStatement);
        List<String> classificationCodeValues = (List<String>) query.list();
        session.close();
        return classificationCodeValues;
	}

}