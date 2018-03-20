package org.accretegb.modules.hibernate.dao;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.hibernate.ExperimentGroup;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

public class ExperimentGroupDAO {
	
	private static ExperimentGroupDAO instance = null;	
	public static ExperimentGroupDAO getInstance() {
	      if(instance == null) {
	         instance = new ExperimentGroupDAO();
	      }
	      return instance;
	}
	
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;
	
	public   List<ExperimentGroup>  findByProjectid(Integer projectId)
	{
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession(); 
		Query query = session.createSQLQuery("select * from experiment_group t where t.project_id = :project_id")
        		.addEntity(ExperimentGroup.class)
        		.setParameter("project_id", projectId);
              
        List<ExperimentGroup> ExperimentGroups = (List<ExperimentGroup>)query.list();
        session.close();

		return ExperimentGroups;
	}

	public  void save(int projectId, String groupName, String stockListJson, String ExpResultJson){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		Transaction transaction = session.beginTransaction();
		Query query = session.createSQLQuery("select * from experiment_group t where t.project_id = :project_id"
				+ " and t.experiment_group_name = :experiment_group_name")
        		.addEntity(ExperimentGroup.class)
        		.setParameter("project_id", projectId)
        		.setParameter("experiment_group_name", groupName);
              
        List<ExperimentGroup> ExperimentGroups = (List<ExperimentGroup>)query.list();   
        try{
	        if(ExperimentGroups.size()>0){
	        	ExperimentGroups.get(0).setStockListJson(stockListJson);
	        	ExperimentGroups.get(0).setExpResultJson(ExpResultJson);
	        	session.update(ExperimentGroups.get(0));
	        }else{
	        	ExperimentGroup ExperimentGroup = new ExperimentGroup(projectId, groupName, stockListJson,ExpResultJson);
	        	session.save(ExperimentGroup);	
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

	public  void delete(int projectId, String groupName) {
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			Query query = session.createSQLQuery("select * from experiment_group t where t.project_id = :project_id"
					+ " and t.experiment_group_name = :experiment_group_name")
	        		.addEntity(ExperimentGroup.class)
	        		.setParameter("project_id", projectId)
	        		.setParameter("experiment_group_name", groupName);
	              
	        List<ExperimentGroup> ExperimentGroups = (List<ExperimentGroup>)query.list();       
	        if(ExperimentGroups.size()>0){
	        	session.delete(ExperimentGroups.get(0));
	    		transaction.commit();
	        }
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
			Query query = session.createSQLQuery("select * from experiment_group t where t.project_id = :project_id"
					+ " and t.experiment_group_name = :experiment_group_name")
	        		.addEntity(ExperimentGroup.class)
	        		.setParameter("project_id", projectId)
	        		.setParameter("experiment_group_name", oldGroupName);
	              
	        List<ExperimentGroup> experimentGroups = (List<ExperimentGroup>)query.list(); 
	        if(experimentGroups.size()>0)
	        {
	        	ExperimentGroup experimentGroup = experimentGroups.get(0);
	        	experimentGroup.setExperimentGroupName(newGroupName);
	        	session.update(experimentGroup);
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
