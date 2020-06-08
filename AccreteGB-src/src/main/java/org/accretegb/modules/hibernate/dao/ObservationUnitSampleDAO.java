package org.accretegb.modules.hibernate.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.ObservationUnit;
import org.accretegb.modules.hibernate.ObservationUnitSample;
import org.accretegb.modules.hibernate.Source;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

@Component("ObservationUnitSampleDAO")
public class ObservationUnitSampleDAO {
	
	private static ObservationUnitSampleDAO instance = null;	
	public static ObservationUnitSampleDAO getInstance() {
	      if(instance == null) {
	         instance = new ObservationUnitSampleDAO();
	      }
	      return instance;
	}
	
	public int getSeasonIndex(String prefix){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		int start = prefix.length() + 3;
		String sql = "SELECT MAX(CAST(SUBSTRING(sample_name, "+String.valueOf(start)
				+", length(sample_name)-"+String.valueOf(start-1)+") AS UNSIGNED)) FROM obervation_unit_sample"
				+ " WHERE (sample_name REGEXP '^"+prefix+".[0-9]*')";
		Query query = session.createSQLQuery(sql);
		List<Object> results  = query.list();
		int max = 1;
		if(!String.valueOf(results.get(0)).equalsIgnoreCase("null")){
			max = Integer.parseInt(String.valueOf(results.get(0)))+1;
		}
		return max;
		
	}
	
	public HashMap<String, Integer> getSampleNameIdMap(List<String> sampleNames) {
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Query query = session.createSQLQuery("SELECT observation_unit_sample_id, sample_name from obervation_unit_sample Where sample_name in ( :sampleNames )")
				.setParameterList("sampleNames", sampleNames);
		List<Object[]> results  = query.list();
		HashMap<String, Integer> nameId = new HashMap<String, Integer>();
		for(Object[] objs : results) {
			nameId.put((String)objs[1], (Integer)objs[0]);
		}
		session.close();		
		return nameId;
	}
	
	public void insert(int sampleId, int tagId, int sourceId, Date sampleDate, String sampleName, String observationUnitSampleComments, Session session){
		ObservationUnit observationUnit = (ObservationUnit) session.load(ObservationUnit.class, tagId);
		Source source = (Source)session.load(Source.class, sourceId);
		if (sampleId == 0) {
			ObservationUnitSample observationUnitSample = new ObservationUnitSample(observationUnit,source,sampleDate,sampleName,observationUnitSampleComments);
			session.save(observationUnitSample);
		} else {
			
		}
		
	}

}