package org.accretegb.modules.hibernate.dao;

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

@Component("collectionInfoDAO")
public class CollectionInfoDAO {
	private  final String countOnField = "SELECT COUNT(*) FROM CollectionInfo WHERE field_id = ";

	
	private static CollectionInfoDAO instance = null;	
	public static CollectionInfoDAO getInstance() {
	      if(instance == null) {
	         instance = new CollectionInfoDAO();
	      }
	      return instance;
	}
	
	public  long getNumberOfObservationUnit(int fieldId) {
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Query query = session.createQuery(countOnField + fieldId);
		long count = ((Number)query.uniqueResult()).longValue();
		session.close();
		return count;
	}
}