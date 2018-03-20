package org.accretegb.modules.hibernate.dao;

import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.Experiment;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.Taxonomy;
import org.accretegb.modules.util.LoggerUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

@Component("taxonomyDAO")
public class TaxonomyDAO {
	
	private static TaxonomyDAO instance = null;	
	public static TaxonomyDAO getInstance() {
	      if(instance == null) {
	         instance = new TaxonomyDAO();
	      }
	      return instance;
	}
	
	public  List<Taxonomy> findAll(){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
        Session session = sessionFactory.openSession();
  
        String sqlStatement = "SELECT * FROM taxonomy";
        Query query = session.createSQLQuery(sqlStatement).addEntity(Taxonomy.class);
        List<Taxonomy> results = (List<Taxonomy>) query.list();
        
        session.close();
        return results;
	}
	
	public  void delete(int taxnomyid){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
        Session session = sessionFactory.openSession();
        try{
        	Transaction transaction = session.beginTransaction();
	        Taxonomy taxonomy =  (Taxonomy)session.get(Taxonomy.class, taxnomyid);
	        if(taxonomy!=null)
	        {
	        	session.delete(taxonomy);
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
	
	public Taxonomy findById(int taxId){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
        Session session = sessionFactory.openSession();
		 Query query = session
                 .createSQLQuery("SELECT * FROM taxonomy WHERE taxonomy_id = :taxID")
                 .addEntity(Taxonomy.class).setParameter("taxID", taxId);
		 
		 Taxonomy t = null;
		 List<Taxonomy>	list = (List<Taxonomy>) query.list();
		 if(list.size() > 0){
			 t = list.get(0);
		 }
		 session.close();
		 return t;
		 
	}
	public  Taxonomy insert(String genus, String species, String subspecies, String subtaxa, String race, String population,
			String commonName, String gto, int taxonomyId){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
        Session session = sessionFactory.openSession();
        try{
        	Transaction transaction = session.beginTransaction();
			Taxonomy taxonomy = new Taxonomy(genus, species, subspecies, subtaxa, race, population,
					commonName, gto, null);
			if(taxonomyId != 0){
				taxonomy.setTaxonomyId(taxonomyId);
				session.update(taxonomy);
			}else{
				session.save(taxonomy);
			}		
			transaction.commit();
			return taxonomy;
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
}