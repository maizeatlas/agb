package org.accretegb.modules.hibernate.dao;

import java.util.List;

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.PhenotypeGroup;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

public class PhenotypeGroupDAO {
	
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;
	
	private static PhenotypeGroupDAO instance = null;	
	public static PhenotypeGroupDAO getInstance() {
	      if(instance == null) {
	         instance = new PhenotypeGroupDAO();
	      }
	      return instance;
	}
	
	public   List<PhenotypeGroup>  findByProjectid(Integer projectId)
	{
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession(); 
		Query query = session.createSQLQuery("select * from phenotype_group t where t.project_id = :project_id")
        		.addEntity(PhenotypeGroup.class)
        		.setParameter("project_id", projectId);
              
        List<PhenotypeGroup> PhenotypeGroups = (List<PhenotypeGroup>)query.list();
        session.close();

		return PhenotypeGroups;
	}

	public  void save(int projectId, String groupName, String exportTableJson, String importTableJson){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		Transaction transaction = session.beginTransaction();
		Query query = session.createSQLQuery("select * from phenotype_group t where t.project_id = :project_id"
				+ " and t.phenotype_group_name = :phenotype_group_name")
        		.addEntity(PhenotypeGroup.class)
        		.setParameter("project_id", projectId)
        		.setParameter("phenotype_group_name", groupName);
              
        List<PhenotypeGroup> PhenotypeGroups = (List<PhenotypeGroup>)query.list();       
        if(PhenotypeGroups.size()>0){
        	PhenotypeGroups.get(0).setExportTableJson(exportTableJson);
        	PhenotypeGroups.get(0).setImportTableJson(importTableJson);
        	session.update(PhenotypeGroups.get(0));
        }else{
        	PhenotypeGroup PhenotypeGroup = new PhenotypeGroup(projectId, groupName, exportTableJson,"");
        	session.save(PhenotypeGroup);	
        }
	
		transaction.commit();
		session.close();
	}

	public  void delete(int projectId, String groupName) {
			Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
			Transaction transaction = session.beginTransaction();
			Query query = session.createSQLQuery("select * from phenotype_group t where t.project_id = :project_id"
					+ " and t.phenotype_group_name = :phenotype_group_name")
	        		.addEntity(PhenotypeGroup.class)
	        		.setParameter("project_id", projectId)
	        		.setParameter("phenotype_group_name", groupName);
	              
	        List<PhenotypeGroup> PhenotypeGroups = (List<PhenotypeGroup>)query.list();       
	        if(PhenotypeGroups.size()>0){
	        	session.delete(PhenotypeGroups.get(0));
	        }
			transaction.commit();
			session.close();
		
	}

	public  void updateGroupName(int projectId, String oldGroupName,String newGroupName) {
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		Transaction transaction = session.beginTransaction();
		Query query = session.createSQLQuery("select * from phenotype_group t where t.project_id = :project_id"
				+ " and t.phenotype_group_name = :phenotype_group_name")
        		.addEntity(PhenotypeGroup.class)
        		.setParameter("project_id", projectId)
        		.setParameter("phenotype_group_name", oldGroupName);
              
        List<PhenotypeGroup> PhenotypeGroups = (List<PhenotypeGroup>)query.list();       
        if(PhenotypeGroups.size()>0){
        	PhenotypeGroup  phenotypeGroup = PhenotypeGroups.get(0);
        	phenotypeGroup.setPhenotypeGroupName(newGroupName);
        	session.update(phenotypeGroup);
        }
		transaction.commit();
		session.close();
	}
}
