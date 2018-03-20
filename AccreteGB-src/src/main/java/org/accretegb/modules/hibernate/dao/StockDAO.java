package org.accretegb.modules.hibernate.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.accretegb.modules.hibernate.Stock;
import org.accretegb.modules.hibernate.Field;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.ObservationUnit;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("StockDAO")
public class StockDAO {
	private  final String SelectStockByStockName = "SELECT * FROM stock WHERE LOWER(stock_name) = :stock_name";

	private static StockDAO instance = null;	
	public static StockDAO getInstance() {
	      if(instance == null) {
	         instance = new StockDAO();
	      }
	      return instance;
	}
	
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;

	@SuppressWarnings({ "-access", "unchecked" })
	public List<Stock> findStockByName(String stockName) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Query query = session.createSQLQuery(SelectStockByStockName)
				.addEntity(Stock.class)
				.setParameter("stock_name", stockName);		
		List<Stock> stockList = (List<Stock>)query.list();
		session.close();
		
		return stockList;

	}
	
	public List<BigInteger> getSourceFromStock(int donorID){
		 SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
         Session session = sessionFactory.openSession();    
         Query checkQuery = session.createSQLQuery("SELECT COUNT(stock_id) FROM stock "
                 + "WHERE passport_id IN (SELECT passport_id FROM passport "
                 + "WHERE source_id = " + donorID + " )");

         List<BigInteger> stocksUnderSource = (List<BigInteger>) checkQuery.list();
     	session.close();
         return stocksUnderSource;
	}
	
	public void insert(Stock stock){
		 SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
         Session session = sessionFactory.openSession();   
         Transaction transaction = (Transaction) session.beginTransaction();        
         try {
        	session.saveOrUpdate(stock);
        	//System.out.println(stock.getPassport().getPedigree());
			transaction.commit();
		} catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			session.close();
		}
	}
	
	public List<Stock> getStocksByIds(ArrayList<Integer> ids){
		ArrayList<Stock> stocks = new ArrayList<Stock>();
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
        Session session = sessionFactory.openSession();     
		for(int id : ids){
			stocks.add((Stock) session.get(Stock.class, id));
		}
		session.close();
		return stocks;
	}
	
	public HashMap<Integer, Stock> getStocks(ArrayList<Integer> ids, Session session){
		HashMap<Integer, Stock> foundStock = new HashMap<Integer, Stock>();
		ArrayList<Stock> stocks = new ArrayList<Stock>();
        for (int id: ids) {
        	Stock stock = (Stock) session.load(Stock.class, id);
        	foundStock.put(id, stock);
        }
		return foundStock;
	}
	
	public List<Stock> getStocksByNames(ArrayList<String> names){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
        Session session = sessionFactory.openSession(); 
        Criteria criteria = session.createCriteria(Stock.class)
                .add(Restrictions.in("stockName", names));

        List<Stock> stocks = criteria.list();
        //System.out.println(stocks);
		session.close();
		return stocks;
	}
	
	public List<Object []> searchByStocks(ArrayList<String> stocknames){
		StringBuilder stocknamesSql = new StringBuilder();
		stocknamesSql.append("( ");
		for(String stockname: stocknames){
			stocknamesSql.append("'");
			stocknamesSql.append(stockname);
			stocknamesSql.append("',");			
		}
		stocknamesSql.deleteCharAt(stocknamesSql.length()-1);
		stocknamesSql.append(" )");
		
		 StringBuffer queryBuff = new StringBuffer();
	     queryBuff.append("select distinct 1, stock.stock_id, stock_name, accession_name, pedigree, generation, cycle, classification.classification_code, population,stock_date,count(packet_no) as total_pkts, packet_no, weight, no_seed, tier1_position, tier2_position, tier3_position, shelf, unit, room, building, location_name, city, state_province, country "
	        		+ "from stock "
	        		+ "left join passport on stock.passport_id = passport.passport_id "
	        		+ "left join classification on passport.classification_id = classification.classification_id "
	        		+ "left join stock_generation on stock.stock_generation_id = stock_generation.stock_generation_id "
	        		+ "left join source on passport.source_id = source.source_id "
	        		+ "left join taxonomy on passport.taxonomy_id = taxonomy.taxonomy_id  "
	        		+ "left join stock_packet on stock.stock_id = stock_packet.stock_id  "
	        		+ "left join stock_packet_container on stock_packet_container.stock_packet_container_id = stock_packet.stock_packet_container_id  "
	        		+ "left join container_location on container_location.container_location_id = stock_packet_container.container_location_id  "
	        		+ "left join location on location.location_id = container_location.location_id "
	        		+ "where stock_name in ");
	       
	        
	        
		String sql = queryBuff.toString() + stocknamesSql.toString() + "group by stock_id";
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
        Session session = sessionFactory.openSession();    
        Query query = session.createSQLQuery(sql);
        List<Object []> results = query.list();
        session.close();
        return results;	
	}
	
	public List<Object []> searchInventory(Set<String> stocknames){
		StringBuilder stocknamesSql = new StringBuilder();
		stocknamesSql.append("( ");
		for(String stockname: stocknames){
			stocknamesSql.append("'");
			stocknamesSql.append(stockname);
			stocknamesSql.append("',");			
		}
		stocknamesSql.deleteCharAt(stocknamesSql.length()-1);
		stocknamesSql.append(" )");
		
		 StringBuffer queryBuff = new StringBuffer();
	     queryBuff.append("select distinct 1, stock_name, packet_no, weight, no_seed, tier1_position, tier2_position, tier3_position, shelf, unit, room, building, location_name, city, state_province, country "
	        		+ "from stock "
	        		+ "left join stock_packet on stock.stock_id = stock_packet.stock_id  "
	        		+ "left join stock_packet_container on stock_packet_container.stock_packet_container_id = stock_packet.stock_packet_container_id  "
	        		+ "left join container_location on container_location.container_location_id = stock_packet_container.container_location_id  "
	        		+ "left join location on location.location_id = container_location.location_id "
	        		+ "where stock_name in ");
	               
		String sql = queryBuff.toString() + stocknamesSql.toString() + "group by stock_name";
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
        Session session = sessionFactory.openSession();    
        Query query = session.createSQLQuery(sql);
        List<Object []> results = query.list();
        session.close();
        return results;	
	}

}