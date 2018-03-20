package org.accretegb.modules.hibernate.dao;

import java.util.List;

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.Stock;
import org.accretegb.modules.hibernate.StockGeneration;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("generationDAO")
public class StockGenerationDAO {
	
	private static StockGenerationDAO instance = null;	
	public static StockGenerationDAO getInstance() {
	      if(instance == null) {
	         instance = new StockGenerationDAO();
	      }
	      return instance;
	}
	
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;
	
	public StockGeneration findStockGeneration(String generation, String cycle) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		 Criteria c = session
                 .createCriteria(StockGeneration.class)
                 .add(StringUtils.isBlank(generation) ? Restrictions.isNull("generation")
                         : Restrictions.eq("generation", generation))
                 .add(StringUtils.isBlank(cycle) ? Restrictions.isNull("cycle") : Restrictions.eq(
                         "cycle", cycle));

		StockGeneration sg = null;
		if(c.list().size() > 0)
		{
			sg = (StockGeneration) c.list().get(0);
		}
		session.close();
		return sg;

	}
	
	
	public StockGeneration insert(String generationValue, String cycleValue, String comment){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		Transaction transaction = session.beginTransaction();
		StockGeneration generation = new StockGeneration();
        generation.setGeneration(StringUtils.defaultIfBlank(generationValue, null));
        generation.setCycle(StringUtils.defaultIfBlank(cycleValue, null));
        generation.setGenerationComments(comment);
        session.save(generation);
        transaction.commit();
        session.close();
		return generation;      
	}

}