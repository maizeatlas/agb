package org.accretegb.modules.hibernate.dao;

import java.util.List;

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.SamplingGroup;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

public class SamplingGroupDAO {
	
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;
	
	private static SamplingGroupDAO instance = null;	
	public static SamplingGroupDAO getInstance() {
	      if(instance == null) {
	         instance = new SamplingGroupDAO();
	      }
	      return instance;
	}
	
	public   List<SamplingGroup>  findByProjectid(Integer projectId)
	{
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession(); 
		Query query = session.createSQLQuery("select * from sampling_group t where t.project_id = :project_id")
        		.addEntity(SamplingGroup.class)
        		.setParameter("project_id", projectId);
              
        List<SamplingGroup> SamplingGroups = (List<SamplingGroup>)query.list();
        session.close();

		return SamplingGroups;
	}

	public  void save(int projectId, String groupName, String sampleSelectionJson, String sampleSettingJson){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		Transaction transaction = session.beginTransaction();
		Query query = session.createSQLQuery("select * from sampling_group t where t.project_id = :project_id"
				+ " and t.sampling_group_name = :sampling_group_name")
        		.addEntity(SamplingGroup.class)
        		.setParameter("project_id", projectId)
        		.setParameter("sampling_group_name", groupName);
              
        List<SamplingGroup> SamplingGroups = (List<SamplingGroup>)query.list();       
        if(SamplingGroups.size()>0){
        	if(sampleSelectionJson != null)
        	{
        		SamplingGroups.get(0).setSampleSelectionJson(sampleSelectionJson);
        	}
        	if(sampleSettingJson != null){
        		SamplingGroups.get(0).setSampleSettingJson(sampleSettingJson);
        	}
        	
        	session.update(SamplingGroups.get(0));
        }else{
        	SamplingGroup SamplingGroup = new SamplingGroup(projectId, groupName, sampleSelectionJson, sampleSettingJson);
        	session.save(SamplingGroup);	
        }
	
		transaction.commit();
		session.close();
	}

	public  void delete(int projectId, String groupName) {
			Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
			Transaction transaction = session.beginTransaction();
			Query query = session.createSQLQuery("select * from sampling_group t where t.project_id = :project_id"
					+ " and t.sampling_group_name = :sampling_group_name")
	        		.addEntity(SamplingGroup.class)
	        		.setParameter("project_id", projectId)
	        		.setParameter("sampling_group_name", groupName);
	              
	        List<SamplingGroup> SamplingGroups = (List<SamplingGroup>)query.list();       
	        if(SamplingGroups.size()>0){
	        	session.delete(SamplingGroups.get(0));
	        }
			transaction.commit();
			session.close();
		
	}

	public  void updateGroupName(int projectId, String oldGroupName,String newGroupName) {
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		Transaction transaction = session.beginTransaction();
		Query query = session.createSQLQuery("select * from sampling_group t where t.project_id = :project_id"
				+ " and t.sampling_group_name = :sampling_group_name")
        		.addEntity(SamplingGroup.class)
        		.setParameter("project_id", projectId)
        		.setParameter("sampling_group_name", oldGroupName);
              
        List<SamplingGroup> SamplingGroups = (List<SamplingGroup>)query.list();       
        if(SamplingGroups.size()>0){
        	SamplingGroup  samplingGroup = SamplingGroups.get(0);
        	samplingGroup.setSamplingGroupName(newGroupName);
        	session.update(samplingGroup);
        }
		transaction.commit();
		session.close();
	}
}
