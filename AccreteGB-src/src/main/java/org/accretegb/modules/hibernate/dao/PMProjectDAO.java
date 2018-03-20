package org.accretegb.modules.hibernate.dao;

// default package
// Generated May 31, 2015 9:04:43 PM by Hibernate Tools 3.4.0.CR1

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.PMProject;
import org.accretegb.modules.util.LoggerUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

public class PMProjectDAO{
		
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;
	
	private static PMProjectDAO instance = null;	
	public static PMProjectDAO getInstance() {
	      if(instance == null) {
	         instance = new PMProjectDAO();
	      }
	      return instance;
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  boolean isProjectNameExist(String projectName) {
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();
        Query query = session.createSQLQuery("Select * from project where project_name = '"+projectName+"'");
        
        Boolean isProjectNameExist = ((List<PMProject>) query.list()).size()>0;
        session.close();
        
        return isProjectNameExist;
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  Integer findProjectId(String projectName) {
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();
        Query query = session.createSQLQuery("Select * from project where project_name = '"+projectName+"'")
        		.addEntity(PMProject.class);
        int projectId = ((List<PMProject>) query.list()).get(0).getProjectId();
        
        session.close();
        return projectId;
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  PMProject findProjectName(Integer projectId) {
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
        Query query = session.createSQLQuery("Select * from project where project_id = '"+projectId+"'")
        		.addEntity(PMProject.class);
        
        PMProject project = ((List<PMProject>) query.list()).get(0);
        session.close();
        return project;
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public int insert(int userId, String projectName, Date createdDate, Date lastModified){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		Transaction transaction = session.beginTransaction();
		try{			
			PMProject newProject = new PMProject( userId, projectName, createdDate, lastModified);
			session.save(newProject);
			transaction.commit();
			return newProject.getProjectId();
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in insert" + ex);
			}
			ex.printStackTrace();
			return 0;
		} finally {
			session.close();
		}
		
		
	}
	
	public Date updateLastModifiedDate(int projectId){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		Transaction transaction = session.beginTransaction();
		Date lastModified = new Date();
		try{
			
			Query query = session.createSQLQuery("Select * from project where project_id = '"+projectId+"'")
	        		.addEntity(PMProject.class);
			PMProject project = ((List<PMProject>) query.list()).get(0);
			project.setLastModified(lastModified);
			session.update(project);
			transaction.commit();
			return lastModified;
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in update" + ex);
			}
			ex.printStackTrace();
			return lastModified;
		} finally {
			session.close();
		}
	}

	public  void delete(int projectId) {
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		try{			
			Transaction transaction = session.beginTransaction();
			Query query = session.createSQLQuery("Select * from project where project_id = '"+projectId+"'")
	        		.addEntity(PMProject.class);
			
			List<PMProject> list = ((List<PMProject>) query.list());
			if (list.size() > 0){
				PMProject project = ((List<PMProject>) query.list()).get(0);
				session.delete(project);
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
	
	
	
}
