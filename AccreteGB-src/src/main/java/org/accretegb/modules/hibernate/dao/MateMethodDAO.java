package org.accretegb.modules.hibernate.dao;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.MateMethod;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.MateMethod;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("mateMethodDAO")
public class MateMethodDAO {
	
	private static MateMethodDAO instance = null;	
	public static MateMethodDAO getInstance() {
	      if(instance == null) {
	         instance = new MateMethodDAO();
	      }
	      return instance;
	}
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;

	public  List<MateMethod> getAllMateMethods() {
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		List<MateMethod> allMateMethods = session.createCriteria(MateMethod.class).list();
		session.close();
		return allMateMethods;
	}
	
	public  MateMethod findMateMethod(String methodName){
			Session session = hibernateSessionFactory.getSessionFactory().openSession();		
			Query query = session.createSQLQuery("select * from mate_method where LOWER(mate_method_name) = :mate_method_name")
					.addEntity(MateMethod.class)
					.setParameter("mate_method_name", methodName);
		
			List<MateMethod> mateMethods = (List<MateMethod>)query.list();			
			session.close();
			return mateMethods.size() > 0? mateMethods.get(0): null;
	}
	
	public  MateMethod insert(String method, String description,String userString, Date dateDefined){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		try{
			Transaction transaction = session.beginTransaction();
			MateMethod mateMethod = new MateMethod( method, description, userString, dateDefined, null, null);
			session.save(mateMethod);	
			transaction.commit();
			return mateMethod;
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in insert" + ex);
			}
			ex.printStackTrace();
			return null;
		} finally {
			session.close();
		}			
		
	}
}