package org.accretegb.modules.hibernate.dao;

import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.Experiment;
import org.accretegb.modules.hibernate.ExperimentFactorValue;
import org.accretegb.modules.hibernate.ObservationUnit;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("experimentDAO")
public class ExperimentDAO {
	
	
	private static ExperimentDAO instance = null;	
	public static ExperimentDAO getInstance() {
	      if(instance == null) {
	         instance = new ExperimentDAO();
	      }
	      return instance;
	}
	private  final String SELECTBYNAMEORIGINATOR = "SELECT * FROM experiment WHERE LOWER(exp_name) = :exp_name "
			+ "AND LOWER(exp_originator) = :exp_originator";
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  List<Experiment> findByNameOriginator(String exp_name, String exp_originator) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Query query = session.createSQLQuery(SELECTBYNAMEORIGINATOR)
				.addEntity(Experiment.class)
				.setParameter("exp_name", exp_name)
				.setParameter("exp_originator", exp_originator);	
		List<Experiment> ExperimentList = (List<Experiment>)query.list();
		session.close();
		return ExperimentList;
	}
	
	public  Experiment insert(String exp_name, String originator) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		try{
			
			Transaction transaction = session.beginTransaction();
			Experiment experiment = new Experiment( exp_name, null, originator,null, null);
			session.save(experiment);
			transaction.commit();
			return experiment;
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

	public void update(String function, String comment, int expId){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		try{
			Transaction transaction = session.beginTransaction();
			Experiment experiment= (Experiment) session.get(Experiment.class, expId);	
			experiment.setExpDesign(function);
			experiment.setExpComments(comment);
			session.update(experiment);
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

}