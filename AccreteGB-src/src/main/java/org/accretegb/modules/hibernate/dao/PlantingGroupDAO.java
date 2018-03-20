package org.accretegb.modules.hibernate.dao;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.PlantingGroup;
import org.accretegb.modules.util.LoggerUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

public class PlantingGroupDAO {
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;
	
	private static PlantingGroupDAO instance = null;	
	public static PlantingGroupDAO getInstance() {
	      if(instance == null) {
	         instance = new PlantingGroupDAO();
	      }
	      return instance;
	}
	
	public   List<PlantingGroup>  findByProjectid(Integer projectId)
	{
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession(); 
		Query query = session.createSQLQuery("select * from planting_group t where t.project_id = :project_id")
        		.addEntity(PlantingGroup.class)
        		.setParameter("project_id", projectId);
              
        List<PlantingGroup> PlantingGroups = (List<PlantingGroup>)query.list();
        session.close();

		return PlantingGroups;
	}

	public  void save(int projectId, String groupName, String tableViewJson, String tagGeneratorJson){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			Query query = session.createSQLQuery("select * from planting_group t where t.project_id = :project_id"
					+ " and t.planting_group_name = :planting_group_name")
	        		.addEntity(PlantingGroup.class)
	        		.setParameter("project_id", projectId)
	        		.setParameter("planting_group_name", groupName);
	              
	        List<PlantingGroup> PlantingGroups = (List<PlantingGroup>)query.list();       
	        if(PlantingGroups.size()>0){
	        	PlantingGroups.get(0).setTableViewJson(tableViewJson);
	        	PlantingGroups.get(0).setTagGeneratorJson(tagGeneratorJson);
	        	session.update(PlantingGroups.get(0));
	        }else{
	        	PlantingGroup PlantingGroup = new PlantingGroup(projectId, groupName, tableViewJson,tagGeneratorJson);
	        	session.save(PlantingGroup);	
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
			Query query = session.createSQLQuery("select * from planting_group t where t.project_id = :project_id"
					+ " and t.planting_group_name = :planting_group_name")
	        		.addEntity(PlantingGroup.class)
	        		.setParameter("project_id", projectId)
	        		.setParameter("planting_group_name", groupName);
			 List<PlantingGroup> PlantingGroups = (List<PlantingGroup>)query.list();       
	        if(PlantingGroups.size()>0){
	        	session.delete(PlantingGroups.get(0));  
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
		try{
				
			Transaction transaction = session.beginTransaction();
			Query query = session.createSQLQuery("select * from planting_group t where t.project_id = :project_id"
					+ " and t.planting_group_name = :planting_group_name")
	        		.addEntity(PlantingGroup.class)
	        		.setParameter("project_id", projectId)
	        		.setParameter("planting_group_name", oldGroupName);
			 List<PlantingGroup> PlantingGroups = (List<PlantingGroup>)query.list();       
	        if(PlantingGroups.size()>0){
	        	PlantingGroup plantingGroup = PlantingGroups.get(0);
	        	plantingGroup.setPlantingGroupName(newGroupName);
	        	session.update(plantingGroup);
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
