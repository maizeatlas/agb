package org.accretegb.modules.hibernate.dao;

import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.MateMethod;
import org.accretegb.modules.hibernate.MeasurementUnit;
import org.accretegb.modules.hibernate.Stock;
import org.accretegb.modules.hibernate.StockComposition;
import org.accretegb.modules.util.LoggerUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("stockCompositionDAO")
public class StockCompositionDAO {

	private static StockCompositionDAO instance = null;	
	public static StockCompositionDAO getInstance() {
	      if(instance == null) {
	         instance = new StockCompositionDAO();
	      }
	      return instance;
	}
	
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;

	@SuppressWarnings({ "-access", "unchecked" })
	public  int findMaxMateLink() {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Criteria criteria = session
			    .createCriteria(StockComposition.class)
			    .setProjection(Projections.max("mateLink"));
		Integer maxMateLink = (Integer)criteria.uniqueResult();
		session.close();
		return maxMateLink == null ? 0 : maxMateLink;

	}
	
	public  StockComposition getStockCompositionByBulk(Stock stock, Stock mix_from_stock){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		Criteria c = session.createCriteria(StockComposition.class)
				.add(Restrictions.eq("stockByStockId", stock))
				.add(Restrictions.eq("stockByMixFromStockId", mix_from_stock));
		
		 List<StockComposition> results = (List<StockComposition>)c.list();
		 session.close();
		 return results.size()>0? results.get(0) : null;
	}
	
	public  StockComposition getStockCompositionByMate(int stock_id, int observation_unit_id){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		 Query query = session.createSQLQuery("SELECT * FROM stock_composition WHERE stock_id = :stock_id"
		 		+ " AND observation_unit_id = :observation_unit_id")
                 .addEntity(StockComposition.class)
                 .setParameter("stock_id", stock_id)
                 .setParameter("observation_unit_id", observation_unit_id);
		
		 List<StockComposition> results = (List<StockComposition>)query.list();
		 session.close();
		 return results.size() > 0? results.get(0) : null;
	}
	
	public  void updateOrSave(StockComposition stockComposition){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();			
		try{
			Transaction transaction = session.beginTransaction();
			session.saveOrUpdate(stockComposition);
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