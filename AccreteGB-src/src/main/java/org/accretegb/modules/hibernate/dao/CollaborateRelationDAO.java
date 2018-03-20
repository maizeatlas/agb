package org.accretegb.modules.hibernate.dao;

// default package
// Generated May 31, 2015 9:04:43 PM by Hibernate Tools 3.4.0.CR1

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.CollaborateRelation;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.Project;
import org.accretegb.modules.hibernate.TokenRelation;
import org.accretegb.modules.util.LoggerUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

public class CollaborateRelationDAO{
	
	private static CollaborateRelationDAO instance = null;	
	public static CollaborateRelationDAO getInstance() {
	      if(instance == null) {
	         instance = new CollaborateRelationDAO();
	      }
	      return instance;
	}
		
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  ArrayList<Integer> findByUserId(int userId) {
		ArrayList<Integer> projectIds = new ArrayList<Integer>();
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();
        Query query =session.createSQLQuery("select * from collaborate_relation where user_id = :user_id")
        		.addEntity(CollaborateRelation.class)
        		.setParameter("user_id", userId);
       
        List<CollaborateRelation> collaborateRelations = (List<CollaborateRelation>)query.list();
        for(CollaborateRelation c : collaborateRelations)
        {
        	projectIds.add(c.getProjectId());
        }
        session.close();
		return projectIds;
             
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  ArrayList<Integer> findByProjectId(int projectId) {
		ArrayList<Integer> userIds = new ArrayList<Integer>();
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();
        Query query = session.createSQLQuery("select * from collaborate_relation where project_id = :project_id")
        		.addEntity(CollaborateRelation.class)
        		.setParameter("project_id", projectId);
       
        List<CollaborateRelation> collaborateRelations = (List<CollaborateRelation>)query.list();
        for(CollaborateRelation c : collaborateRelations)
        {
        	userIds.add(c.getUserId());
        }
        session.close();
		return userIds;
             
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  boolean isCollaborator(int projectId, int userId) {
		ArrayList<Integer> userIds = new ArrayList<Integer>();
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();
        Query query = session.createSQLQuery("select * from collaborate_relation where project_id = :project_id "
        				+ " and user_id = :user_id")
        		.addEntity(CollaborateRelation.class)
        		.setParameter("project_id", projectId)
        		.setParameter("user_id", userId);
       
        List<CollaborateRelation> collaborateRelations = (List<CollaborateRelation>)query.list();
        boolean isCollaborator = collaborateRelations.size()>0;
        session.close();
		return isCollaborator;
             
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  void insert(int projectId, int userId){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			CollaborateRelation collaborateRelation = new CollaborateRelation(projectId,userId);
			session.save(collaborateRelation);
			transaction.commit();
		}catch (HibernateException ex) {
  			if(LoggerUtils.isLogEnabled())
  				LoggerUtils.log(Level.INFO, "error in insert" + ex);
  			ex.printStackTrace();
  		} finally {
  			session.close();
  		}
	
	}
	
	public  void delete(int projectId, int userId){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			Query query = session.createSQLQuery("select * from collaborate_relation where project_id = :project_id and user_id = :user_id")
		        		.addEntity(CollaborateRelation.class)
		        		.setParameter("project_id", projectId)
		        		.setParameter("user_id", userId);
			CollaborateRelation collaborateRelation =( (List<CollaborateRelation>)query.list()).get(0);
			session.delete(collaborateRelation);
			transaction.commit();
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
				LoggerUtils.log(Level.INFO, "error in delete" + ex);
			ex.printStackTrace();
		} finally {
			session.close();
		}
	}
	
	public  void delete(int projectId){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		
		try{		
			Transaction transaction = session.beginTransaction();
			Query query = session.createSQLQuery("select * from collaborate_relation where project_id = :project_id")
		        		.addEntity(CollaborateRelation.class)
		        		.setParameter("project_id", projectId);
			
		    List<CollaborateRelation> collaborateRelations = query.list();
		    for(CollaborateRelation c : collaborateRelations)
			{
		    	session.delete(c);
			}
			transaction.commit();
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
				LoggerUtils.log(Level.INFO, "error in delete" + ex);
			ex.printStackTrace();
		} finally {
			session.close();
		}
	}
	
	
}
