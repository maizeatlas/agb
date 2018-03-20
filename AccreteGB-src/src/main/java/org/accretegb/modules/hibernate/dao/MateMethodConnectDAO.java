package org.accretegb.modules.hibernate.dao;

import java.util.List;

import org.accretegb.modules.hibernate.MateMethodConnect;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.Mate;
import org.accretegb.modules.hibernate.MateMethod;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

@Component("mateMethodConnectDAO")
public class MateMethodConnectDAO {
	
	private static MateMethodConnectDAO instance = null;	
	public static MateMethodConnectDAO getInstance() {
	      if(instance == null) {
	         instance = new MateMethodConnectDAO();
	      }
	      return instance;
	}
	
	public  List<MateMethodConnect> getAllMateConnects() {
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		List<MateMethodConnect> allMateConnects = session.createCriteria(MateMethodConnect.class).list();
		session.close();
		return allMateConnects;
	}
	
	public  MateMethodConnect insertIfNoExist(Mate mate, MateMethod method) {
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Criteria c = session.createCriteria(MateMethodConnect.class)
		.add((method == null)?Restrictions.isNull("mateMethod"):Restrictions.eq("mateMethod", method))
		.add((mate == null)?Restrictions.isNull("mate"):Restrictions.eq("mate", mate));
	
		if(c.list().size()>0){
			List resultSet = c.list(); 
			session.close();
			return (MateMethodConnect) resultSet.get(0);
		}else{
			Transaction transaction = session.beginTransaction();
			MateMethodConnect mateMethodConnect = new MateMethodConnect();
			mateMethodConnect.setMate(mate);
			mateMethodConnect.setMateMethod(method);
			session.save(mateMethodConnect);
			transaction.commit();
			session.close();			
			return mateMethodConnect;
		}		
	}
	
	public  MateMethodConnect getMateMethodConnect(int mateMethodConnectId){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();        
     	MateMethodConnect mateMethodConnect = (MateMethodConnect) session.get(MateMethodConnect.class, mateMethodConnectId);
		session.close();
     	return mateMethodConnect;
	}
	
	public MateMethodConnect getMateMethodConnectReference(int mateMethodConnectId, Session session){     
     	MateMethodConnect mateMethodConnect = (MateMethodConnect) session.load(MateMethodConnect.class, mateMethodConnectId);
     	return mateMethodConnect;
	}
	
	
	
}