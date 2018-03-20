package org.accretegb.modules.hibernate.dao;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.hibernate.HarvestingGroup;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

public class HarvestingGroupDAO {
	
	private static HarvestingGroupDAO instance = null;	
	public static HarvestingGroupDAO getInstance() {
	      if(instance == null) {
	         instance = new HarvestingGroupDAO();
	      }
	      return instance;
	}
	
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;
	
	public   List<HarvestingGroup>  findByProjectid(Integer projectId)
	{
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession(); 
		Query query = session.createSQLQuery("select * from harvesting_group t where t.project_id = :project_id")
        		.addEntity(HarvestingGroup.class)
        		.setParameter("project_id", projectId);
              
        List<HarvestingGroup> HarvestingGroups = (List<HarvestingGroup>)query.list();
        session.close();

		return HarvestingGroups;
	}

	public  void save(int projectId, String groupName, String crossRecordJson, String bulkJson, String stickerGeneratorJson){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		Transaction transaction = session.beginTransaction();
		Query query = session.createSQLQuery("select * from harvesting_group t where t.project_id = :project_id"
				+ " and t.Harvesting_group_name = :Harvesting_group_name")
        		.addEntity(HarvestingGroup.class)
        		.setParameter("project_id", projectId)
        		.setParameter("Harvesting_group_name", groupName);
              
        List<HarvestingGroup> HarvestingGroups = (List<HarvestingGroup>)query.list();
        try{
	        if(HarvestingGroups.size()>0){
	        	HarvestingGroups.get(0).setCrossRecordJson(crossRecordJson);
	        	HarvestingGroups.get(0).setBulkJson(bulkJson);
	        	HarvestingGroups.get(0).setStickerGeneratorJson(stickerGeneratorJson);
	        	session.update(HarvestingGroups.get(0));
	        }else{
	        	HarvestingGroup HarvestingGroup = new HarvestingGroup(projectId, groupName, crossRecordJson,bulkJson,stickerGeneratorJson);
	        	session.save(HarvestingGroup);	
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
		Transaction transaction = session.beginTransaction();
		try{
			Query query = session.createSQLQuery("select * from harvesting_group t where t.project_id = :project_id"
					+ " and t.Harvesting_group_name = :Harvesting_group_name")
	        		.addEntity(HarvestingGroup.class)
	        		.setParameter("project_id", projectId)
	        		.setParameter("Harvesting_group_name", groupName);
	              
	        List<HarvestingGroup> HarvestingGroups = (List<HarvestingGroup>)query.list();       
	        if(HarvestingGroups.size()>0){
	        	session.delete(HarvestingGroups.get(0));
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

	public  void updateGroupName(int projectId, String oldGroupName,String newGroupName) {
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		Transaction transaction = session.beginTransaction();
		try{
			Query query = session.createSQLQuery("select * from harvesting_group t where t.project_id = :project_id"
					+ " and t.Harvesting_group_name = :Harvesting_group_name")
	        		.addEntity(HarvestingGroup.class)
	        		.setParameter("project_id", projectId)
	        		.setParameter("Harvesting_group_name", oldGroupName);
	              
	        List<HarvestingGroup> HarvestingGroups = (List<HarvestingGroup>)query.list();       
	        if(HarvestingGroups.size()>0){
	        	HarvestingGroup harvestingGroup =  HarvestingGroups.get(0);
	        	harvestingGroup.setHarvestingGroupName(newGroupName);
	        	session.update(harvestingGroup);
	    		transaction.commit();
	        }
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
