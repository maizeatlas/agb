package org.accretegb.modules.hibernate.dao;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.StockSelectionGroup;
import org.accretegb.modules.util.LoggerUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

public class StockSelectionGroupDAO {
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;
	
	private static StockSelectionGroupDAO instance = null;	
	public static StockSelectionGroupDAO getInstance() {
	      if(instance == null) {
	         instance = new StockSelectionGroupDAO();
	      }
	      return instance;
	}
	
	public   List<StockSelectionGroup>  findByProjectid(Integer projectId)
	{
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession(); 
		Query query = session.createSQLQuery("select * from stock_selection_group t where t.project_id = :project_id")
        		.addEntity(StockSelectionGroup.class)
        		.setParameter("project_id", projectId);
              
        List<StockSelectionGroup> stockSelectionGroups = (List<StockSelectionGroup>)query.list();
        session.close();

		return stockSelectionGroups;
	}

	public  void save(int projectId, String groupName, String cartJson){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			Query query = session.createSQLQuery("select * from stock_selection_group t where t.project_id = :project_id"
					+ " and t.stock_selection_group_name = :stock_selection_group_name")
	        		.addEntity(StockSelectionGroup.class)
	        		.setParameter("project_id", projectId)
	        		.setParameter("stock_selection_group_name", groupName);
	              
	        List<StockSelectionGroup> stockSelectionGroups = (List<StockSelectionGroup>)query.list();    
	        if(stockSelectionGroups.size()>0){
	        	stockSelectionGroups.get(0).setCartJson(cartJson);
	        	session.update(stockSelectionGroups.get(0));
	        }else{
	        	StockSelectionGroup stockSelectionGroup = new StockSelectionGroup(projectId, groupName, cartJson);
	        	session.save(stockSelectionGroup);	
	        }
		
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
	
	public  void delete(int projectId, String groupName){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			Query query = session.createSQLQuery("select * from stock_selection_group t where t.project_id = :project_id"
					+ " and t.stock_selection_group_name = :stock_selection_group_name")
	        		.addEntity(StockSelectionGroup.class)
	        		.setParameter("project_id", projectId)
	        		.setParameter("stock_selection_group_name", groupName);
	              
	        List<StockSelectionGroup> stockSelectionGroups = (List<StockSelectionGroup>)query.list(); 
	        if(stockSelectionGroups.size()>0)
	        {
	        	session.delete(stockSelectionGroups.get(0));
	        }
			transaction.commit();
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in delete" + ex);
			}
			ex.printStackTrace();
		} finally {
			session.close();
		}
		
	}
	
	public  void updateGroupName(int projectId, String oldGroupName, String newGroupName){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			Query query = session.createSQLQuery("select * from stock_selection_group t where t.project_id = :project_id"
					+ " and t.stock_selection_group_name = :stock_selection_group_name")
	        		.addEntity(StockSelectionGroup.class)
	        		.setParameter("project_id", projectId)
	        		.setParameter("stock_selection_group_name", oldGroupName);
	              
	        List<StockSelectionGroup> stockSelectionGroups = (List<StockSelectionGroup>)query.list(); 
	        if(stockSelectionGroups.size()>0)
	        {
	        	StockSelectionGroup stockSelectionGroup = stockSelectionGroups.get(0);
	        	stockSelectionGroup.setStockSelectionGroupName(newGroupName);
	        	session.update(stockSelectionGroup);
	        }
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
