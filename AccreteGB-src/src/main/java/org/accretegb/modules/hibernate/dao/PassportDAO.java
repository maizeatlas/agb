package org.accretegb.modules.hibernate.dao;

import java.util.ArrayList;
import java.util.List;

import org.accretegb.modules.hibernate.Classification;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.Passport;
import org.accretegb.modules.hibernate.Source;
import org.accretegb.modules.hibernate.Stock;
import org.accretegb.modules.hibernate.StockGeneration;
import org.accretegb.modules.hibernate.Taxonomy;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

@Component("passportDAO")
public class PassportDAO {
	
	private  HibernateSessionFactory hibernateSessionFactory;
	private  final String findByInfo = "SELECT * FROM passport WHERE LOWER(accession_name) = :accession_name"
			+ " AND LOWER(pedigree) = :pedigree";
	
	
	
	private static PassportDAO instance = null;	
	public static PassportDAO getInstance() {
	      if(instance == null) {
	         instance = new PassportDAO();
	      }
	      return instance;
	}
	
	
	public List<String> findAccessions(List<String> accessionNames){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		Query query = session.createSQLQuery("SELECT accession_name FROM passport WHERE accession_name IN :list")
							 .setParameterList("list", accessionNames);
		
		List<String> existAcces = query.list();
		if(query.list().size() > 0){
			session.close();
			return existAcces;
		}else{
			session.close();
			return new ArrayList<String>();
		}
		
	}
	
	
	public Boolean findAccession(String accessionName){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		Query query = session.createSQLQuery("SELECT count(accession_name) FROM passport WHERE accession_name = '"+accessionName
				+ "' AND accession_name like '"+accessionName.substring(0, 4)+"%' having count(accession_name) > 1");
		
		if(query.list().size() > 0){
			session.close();
			return true;
		}else{
			session.close();
			return false;
		}
		
	}
	
	
	public Passport insert(Source source, Classification classification, Taxonomy taxonomy, String accession_name, 
			String accession_identifier, String pedigree, String passportComments){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		 Criteria query = session
                 .createCriteria(Passport.class)
                 .add(taxonomy == null ? Restrictions.isNull("taxonomy")
                         : Restrictions.eq("taxonomy", taxonomy))
                 .add( classification == null ? Restrictions.isNull("classification") : Restrictions.eq(
                         "classification", classification))
                 .add(StringUtils.isBlank(accession_name) ? Restrictions.isNull("accession_name") : Restrictions.eq(
                         "accession_name", accession_name))
                 .add(StringUtils.isBlank(pedigree) ? Restrictions.isNull("pedigree") : Restrictions.eq(
                         "pedigree", pedigree))
                 .add(Restrictions.eq("source", source))
                 .add(StringUtils.isBlank(accession_identifier) ? Restrictions.isNull("accession_identifier") : Restrictions.eq(
                         "accession_identifier", accession_identifier));
		 		
		 Passport passport = null;
		if(query.list().size() > 0 ){
			passport = (Passport) query.list().get(0);
			session.close();
		
		}else{
			passport = new Passport( source, null, classification, taxonomy, accession_name, accession_identifier, pedigree,
	         		passportComments, null, null, null);
			Transaction transaction = session.beginTransaction();
			session.save(passport);
			transaction.commit();
			session.close();
		}	
		return passport;
	}
	
	public Passport insert(Integer classification_id, Integer taxonomy_id, Integer passport_id,
			String accession, String pedigree){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		Passport passport = (Passport)session.get(Passport.class, passport_id);
		if(passport != null){
			if(classification_id != 0){
				passport.setClassification((Classification)session.get(Classification.class, classification_id));
			}
			if(classification_id != 0){
				passport.setTaxonomy((Taxonomy)session.get(Taxonomy.class, taxonomy_id));
			}
			if(accession != ""){
				passport.setAccession_name(accession);
			}
			if(pedigree != ""){
				passport.setPedigree(pedigree);
			}
			session.saveOrUpdate(passport);
		}
		Transaction transaction = session.beginTransaction();
		transaction.commit();
		session.close();	
		return null;
	}
	
	public  List<Passport> findByTaxonomy(int taxonomyId){
		 SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
         Session session = sessionFactory.openSession();
         Query selectQuery = session.createSQLQuery("SELECT passport_id " + "FROM passport "
                 + "WHERE taxonomy_id = " + taxonomyId);
         List<Passport> parentPassports = selectQuery.list();
         session.close();
         return parentPassports;
	}
	
	public  List<Passport> findByClassificationCode(String ClassficationCode){
		 SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
        Session session = sessionFactory.openSession();
        Query selectQuery = session.createSQLQuery("SELECT passport_id " + "FROM classification S "
                + "JOIN passport P " + "ON (S.classification_id = P.classification_id) "
                + "WHERE S.classification_code = " + ClassficationCode);
        List<Passport> parentPassports = selectQuery.list();
        session.close();
        return parentPassports;
	}
	

}