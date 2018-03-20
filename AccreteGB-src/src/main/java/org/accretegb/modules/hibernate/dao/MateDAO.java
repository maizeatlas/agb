package org.accretegb.modules.hibernate.dao;

import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.Mate;
import org.accretegb.modules.util.LoggerUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("mateDAO")
public class MateDAO {
	
	private static MateDAO instance = null;	
	public static MateDAO getInstance() {
	      if(instance == null) {
	         instance = new MateDAO();
	      }
	      return instance;
	}
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;

	@SuppressWarnings({ "-access", "unchecked" })
	public  Mate insertIfNoExist(String type, String role) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Query query = session.createSQLQuery("select * from mate where mating_type = :mating_type and mate_role = :mate_role")
				.addEntity(Mate.class)
				.setParameter("mating_type", type)
				.setParameter("mate_role", role);
	
		List<Mate> mates = (List<Mate>)query.list();
		if(mates.size() <= 0){
			Transaction transaction = session.beginTransaction();
			Mate mate = new Mate();
			mate.setMateRole(role);
			mate.setMatingType(type);
			session.save(mate);
			transaction.commit();
			session.close();
			return mate;
		}
		session.close();
		return mates.get(0);

	}
	
	public  List findMate(String type, String role){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Criteria c = session.createCriteria(Mate.class)
				.add(StringUtils.isBlank(type)?Restrictions.isNull("matingType"):Restrictions.eq("matingType", type))
				.add(StringUtils.isBlank(role)?Restrictions.isNull("mateRole"):Restrictions.eq("mateRole", role));
		List resultSet = c.list(); 
		session.close();
		return resultSet;
	}
	
	public List<Mate> getMates(){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Query query = session.createSQLQuery("select * from mate").addEntity(Mate.class);
		List<Mate> mates = (List<Mate>)query.list();
		session.close();
		return mates;

	}
	
	public  Mate insert(String type, String role){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		try{
			Transaction transaction = session.beginTransaction();
			Mate mate = new Mate(type, role, null);
			session.save(mate);		
			transaction.commit();
			return mate;
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