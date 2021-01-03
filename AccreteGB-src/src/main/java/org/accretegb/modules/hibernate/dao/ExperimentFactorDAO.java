package org.accretegb.modules.hibernate.dao;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.Experiment;
import org.accretegb.modules.hibernate.ExperimentFactor;
import org.accretegb.modules.hibernate.ExperimentFactorValue;
import org.accretegb.modules.hibernate.Location;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("experimentFactorDAO")
public class ExperimentFactorDAO {
	
	private static ExperimentFactorDAO instance = null;	
	public static ExperimentFactorDAO getInstance() {
	      if(instance == null) {
	         instance = new ExperimentFactorDAO();
	      }
	      return instance;
	}
	private  final String SELECTBYTYPE= "SELECT * FROM experiment_factor WHERE LOWER(exp_factor_type) = :exp_factor_type";

	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;

	@SuppressWarnings({ "-access", "unchecked" })
	public  List<ExperimentFactor> findByNameType(String name, String type, String desc, String comment) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();				
		String descTerm = (String.valueOf(desc).equals("null"))? "is null " : "= :exp_factor_desc ";
		String commTerm = (String.valueOf(comment).equals("null"))? "is null" : "= :exp_factor_comments";
		Query query = session.createSQLQuery("SELECT * FROM experiment_factor WHERE "
				    + "LOWER(exp_factor_name) = :exp_factor_name "
			        + "AND LOWER(exp_factor_type) = :exp_factor_type "
					+ "AND LOWER(exp_factor_desc) " + descTerm 
					+ "AND LOWER(exp_factor_comments)" + commTerm)					
					.addEntity(ExperimentFactor.class)
					.setParameter("exp_factor_name", name)
					.setParameter("exp_factor_type", type);
			
		if(desc != null){
		    query.setParameter("exp_factor_desc", desc);
		}
		if(comment != null){
		    query.setParameter("exp_factor_comments", comment);
		}

		List<ExperimentFactor> experimentFactorList = (List<ExperimentFactor>)query.list();
		session.close();
		return experimentFactorList;
	}
	
	public  List<ExperimentFactor> findByType(String type) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Query query = session.createSQLQuery(SELECTBYTYPE)
				.addEntity(ExperimentFactor.class)
				.setParameter("exp_factor_type", type);	
		List<ExperimentFactor> experimentFactorList = (List<ExperimentFactor>)query.list();
		session.close();
		return experimentFactorList;
	}
	
	public  List<ExperimentFactor> findAll() {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Query query = session.createSQLQuery("select * from experiment_factor").addEntity(ExperimentFactor.class);
		List<ExperimentFactor> experimentFactorList = (List<ExperimentFactor>)query.list();
		session.close();
		return experimentFactorList;
	}
	
	
	
	public  ExperimentFactor insert(String exp_factor_name, String exp_factor_type, String exp_factor_desc, String exp_factor_comments) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		
		try{
			Transaction transaction = session.beginTransaction();
			ExperimentFactor experimentFactor = new ExperimentFactor( exp_factor_name, exp_factor_type,
					exp_factor_desc, exp_factor_comments,null);
			session.save(experimentFactor);
			transaction.commit();
			return experimentFactor;
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