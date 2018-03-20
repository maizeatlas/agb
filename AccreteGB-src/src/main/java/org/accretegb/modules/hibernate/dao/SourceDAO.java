package org.accretegb.modules.hibernate.dao;

import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.Location;
import org.accretegb.modules.hibernate.Source;
import org.accretegb.modules.util.LoggerUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

@Component("sourceDAO")
public class SourceDAO {
	
	private static SourceDAO instance = null;	
	public static SourceDAO getInstance() {
	      if(instance == null) {
	         instance = new SourceDAO();
	      }
	      return instance;
	}
	
	public  List<Object[]> getSourceNames(){
		 SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
	     Session session = sessionFactory.openSession();
	     String sqlStatement = "select person_name, source_id from source";
	     Query query = session.createSQLQuery(sqlStatement);
	     List<Object[]> results = query.list();
	     session.close();
	     return results;
	} 
	
	public  Source findSource(String sourceID) {
	        SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
	        Session session = sessionFactory.openSession();
	        try {
	           
	            Query query = session.createSQLQuery("SELECT * FROM source WHERE source_id = :sourceID")
	                    .addEntity(Source.class).setParameter("sourceID", String.valueOf(sourceID));
	            return (Source) query.list().get(0);
	        } catch (HibernateException ex) {
	            if (LoggerUtils.isLogEnabled()) {
	                LoggerUtils.log(Level.INFO, ex.toString());
	            }
	        } finally {
	            session.close();
	        }
	        return null;
	    }
	
	public  int insert(String[] values, Location location) {
        SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
        Session session = sessionFactory.openSession();
        try {

            Transaction transaction = session.beginTransaction();
            Source source = new Source(location, String.valueOf(values[0] + " " + values[1]).equalsIgnoreCase("null")? null : values[0] + " " + values[1],
            		String.valueOf(values[2]).equalsIgnoreCase("null")? null : values[2], 
            		String.valueOf(values[3]).equalsIgnoreCase("null")? null : values[3],
      				String.valueOf(values[4]).equalsIgnoreCase("null")? null : values[4],
   					String.valueOf(values[11]).equalsIgnoreCase("null")? null : values[11],
            		String.valueOf(values[12]).equalsIgnoreCase("null")? null : values[12],
            		String.valueOf(values[13]).equalsIgnoreCase("null")? null : values[13],
            		String.valueOf(values[14]).equalsIgnoreCase("null")? null : values[14],
                    String.valueOf(values[15]).equalsIgnoreCase("null")? null : values[15], null, null, null, null);
            session.save(source);
            transaction.commit();
            return source.getSourceId();
        } catch (HibernateException ex) {
            if (LoggerUtils.isLogEnabled()) {
                LoggerUtils.log(Level.INFO, ex.toString());
            }
        } finally {
            session.close();
        }
        return -1;
    }
	public  void update(String[] values, Integer donorID, Location existingLocation) {

	        SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
	        Session session = sessionFactory.openSession();

	        try {
	            Transaction transaction = session.beginTransaction();
	            Source source = new Source(existingLocation, String.valueOf(values[0] + " " + values[1]).equalsIgnoreCase("null")? null : values[0] + " " + values[1],
	            		String.valueOf(values[2]).equalsIgnoreCase("null")? null : values[2], 
	            		String.valueOf(values[3]).equalsIgnoreCase("null")? null : values[3],
	      				String.valueOf(values[4]).equalsIgnoreCase("null")? null : values[4],
	   					String.valueOf(values[11]).equalsIgnoreCase("null")? null : values[11],
	            		String.valueOf(values[12]).equalsIgnoreCase("null")? null : values[12],
	            		String.valueOf(values[13]).equalsIgnoreCase("null")? null : values[13],
	            		String.valueOf(values[14]).equalsIgnoreCase("null")? null : values[14],
	                    String.valueOf(values[15]).equalsIgnoreCase("null")? null : values[15], null, null, null, null);
	            source.setSourceId(donorID);
	            session.update(source);
	            transaction.commit();

	        } catch (HibernateException ex) {
	            if (LoggerUtils.isLogEnabled()) {
	                LoggerUtils.log(Level.INFO, ex.toString());
	            }
	        } finally {
	            session.close();
	        }

	    }
	
	public  void delete(int donorID){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
	    Session session = sessionFactory.openSession();	
	    try{
	    	Transaction transaction = session.beginTransaction();
	        Source source = (Source)session.get(Source.class,donorID);
	        source.setSourceId(donorID);
	        session.delete(source);
	        transaction.commit();
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in insert" + ex);
			}
			ex.printStackTrace();
		} finally {
			session.close();
		}
	}
	
	public  List<Object[]> findByLocation(){
		 SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
	     Session session = sessionFactory.openSession();
	     String sqlStatement = "select 1, source_id, person_name,'last', institute,department, street_address, city, state_province, country, "
                 + "zipcode,location_name, location_comments, phone, fax, email, url, source_comments from source, location where "
                 + "source.location_id=location.location_id;";
         Query query = session.createSQLQuery(sqlStatement);
         List<Object[]> results = query.list();
         session.close();
         return results;
	}

	    
}