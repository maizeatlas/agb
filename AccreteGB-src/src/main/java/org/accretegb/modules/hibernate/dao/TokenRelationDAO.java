package org.accretegb.modules.hibernate.dao;

// default package
// Generated May 31, 2015 9:04:43 PM by Hibernate Tools 3.4.0.CR1

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.PMProject;
import org.accretegb.modules.hibernate.TokenRelation;
import org.accretegb.modules.util.LoggerUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

public class TokenRelationDAO{
		
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;
	
	private static TokenRelationDAO instance = null;	
	public static TokenRelationDAO getInstance() {
	      if(instance == null) {
	         instance = new TokenRelationDAO();
	      }
	      return instance;
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  void deleteOutdatedTokens(List<Integer> projectIds){
		     
		for(Integer projectId : projectIds){
			Session session = hibernateSessionFactory.getPmSessionFactory().openSession();  
			Query query = session.createSQLQuery("select * from token_relation t where t.project_id = :project_id")
	        		.addEntity(TokenRelation.class)
	        		.setParameter("project_id", projectId);
	              
	        List<TokenRelation> tokenRelations = (List<TokenRelation>)query.list();
	    	Date today = new Date();
	    	if(tokenRelations.size() > 0)
	    	{
	    		if(today.compareTo(tokenRelations.get(0).getExpirationTime()) > 0){
	    	       delete(tokenRelations.get(0));
	    	    }
	    	}
	    	session.close();
    	} 
		
	}
	
	public  TokenRelation findTokenHolder(Integer projectId)
	{
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession(); 
		Query query = session.createSQLQuery("select * from token_relation where project_id = '"+projectId+"'")
        		.addEntity(TokenRelation.class);
              
        List<TokenRelation> tokenRelations = (List<TokenRelation>)query.list();
        session.close();
    	if(tokenRelations.size() > 0)
    	{
    		return tokenRelations.get(0);
    	}
		return null;
	}
	
	public  ArrayList<Integer> findProjects(Integer userId)
	{
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession(); 
		Query query = session.createSQLQuery("select * from token_relation t where t.user_id = :user_id")
        		.addEntity(TokenRelation.class)
        		.setParameter("user_id", userId);
              
        List<TokenRelation> tokenRelations = (List<TokenRelation>)query.list();
      
        ArrayList<Integer> projectIds = new ArrayList<Integer>();
        for(TokenRelation t : tokenRelations){
        	projectIds.add(t.getProjectId());
        }
        session.close();
		return projectIds;
	}
	
	public  ArrayList<PMProject> findProjectObjects(Integer userId)
	{
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession(); 
		Query query = session.createSQLQuery("select * from token_relation t where t.user_id = :user_id")
        		.addEntity(TokenRelation.class)
        		.setParameter("user_id", userId);
              
        List<TokenRelation> tokenRelations = (List<TokenRelation>)query.list();
      
        ArrayList<PMProject> projects = new ArrayList<PMProject>();
        for(TokenRelation t : tokenRelations){
        	projects.add((PMProject) session.get(PMProject.class, t.getProjectId()));
        }
        session.close();    
		return projects;
	}
	
	
	public  TokenRelation insert(int projectId, int userId){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			Date today = Calendar.getInstance(TimeZone.getDefault()).getTime();
			int day = today.getDate() + 10;
			today.setDate(day);
			TokenRelation tokenRelation = new TokenRelation(userId,projectId, today);
			session.save(tokenRelation);	
			transaction.commit();
			return tokenRelation;
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in insert" + ex);
			}
			ex.printStackTrace();
			return null;
		} finally {
			session.close();
		}
		
	}
	
	public  void delete(TokenRelation tokenRelation){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		Transaction transaction = session.beginTransaction();
		session.delete(tokenRelation);	
		transaction.commit();
		session.close();
	}
	
	public  void delete(int projectId, int userId){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			Query query = session.createSQLQuery("select * from token_relation t "
					+ "where t.project_id = :project_id "
					+ "and t.user_id = :user_id")
	        		.addEntity(TokenRelation.class)
	        		.setParameter("project_id", projectId)
	        		.setParameter("user_id", userId);
	              
	        List<TokenRelation> tokenRelations = (List<TokenRelation>)query.list();
			session.delete(tokenRelations.get(0));	
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
	
	public void delete(int projectId){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			Query query = session.createSQLQuery("select * from token_relation t "
					+ "where t.project_id = :project_id ")
	        		.addEntity(TokenRelation.class)
	        		.setParameter("project_id", projectId);
	              
	        List<TokenRelation> tokenRelations = (List<TokenRelation>)query.list();
	        for(TokenRelation t : tokenRelations)
			{
	        	session.delete(t);	
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
	
}
