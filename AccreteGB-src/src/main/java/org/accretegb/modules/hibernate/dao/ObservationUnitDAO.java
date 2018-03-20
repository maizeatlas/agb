package org.accretegb.modules.hibernate.dao;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.Classification;
import org.accretegb.modules.hibernate.Field;
import org.accretegb.modules.hibernate.MateMethodConnect;
import org.accretegb.modules.hibernate.MeasurementValue;
import org.accretegb.modules.hibernate.ObservationUnit;
import org.accretegb.modules.hibernate.ObservationUnitSample;
import org.accretegb.modules.hibernate.Stock;
import org.accretegb.modules.hibernate.StockComposition;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

@Component("observationUnitDAO")
public class ObservationUnitDAO {
	
	private static ObservationUnitDAO instance = null;	
	public static ObservationUnitDAO getInstance() {
	      if(instance == null) {
	         instance = new ObservationUnitDAO();
	      }
	      return instance;
	}
	private  final String countOnField = "SELECT COUNT(*) FROM ObservationUnit WHERE field_id = ";
	
	private  final String OBSRELATEDINFO = " FROM observation_unit "
			+ "left join  stock on stock.stock_id = observation_unit.stock_id "
			+ "left join  stock_generation on stock_generation.stock_generation_id = stock.stock_generation_id "
			+ "left join  passport on passport.passport_id = stock.passport_id "
			+ "left join  field on observation_unit.field_id = field.field_id "
			+ "left join  location on location.location_id = field.location_id "
			+ "left join  mate_method_connect on observation_unit.mate_method_connect_id = mate_method_connect.mate_method_connect_id "
			+ "left join  mate on mate.mate_id = mate_method_connect.mate_id "
			+ "left join  mate_method on mate_method.mate_method_id = mate_method.mate_method_id "
			+ "left join  experiment_factor_value on observation_unit.observation_unit_id = experiment_factor_value.observation_unit_id "
			+ "left join  experiment_factor on experiment_factor_value.experiment_factor_id = experiment_factor.experiment_factor_id "
			+ "left join  experiment on experiment_factor_value.experiment_id = experiment.experiment_id "
			+ "WHERE observation_unit.observation_unit_id = ";
	
	

	public  long getNumberOfObservationUnit(int fieldId) {
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Query query = session.createQuery(countOnField + fieldId);
		long count = ((Number)query.uniqueResult()).longValue();
		session.close();
		return count;
	}
	
	public  List getObsRelatedInfo(int obsUnitId, List<String> columnnames){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		for(String col : columnnames){
			sql.append(col + ",");
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(OBSRELATEDINFO);
		sql.append(String.valueOf(obsUnitId));
		Query query = session.createSQLQuery(sql.toString());		
		List<Object[]> results  = query.list();
		session.close();
		return results;
		
	}
	
	public  Object[] searchByTagname(Object tagname){
		try{
			SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
			Session session = sessionFactory.openSession();
			Query query = session.createSQLQuery("SELECT observation_unit_id, accession_name, pedigree, generation "
					+ "FROM observation_unit o "
					+ "LEFT JOIN stock s ON(o.stock_id = s.stock_id) "
					+ "LEFT JOIN passport p ON (s.passport_id = p.passport_id) "
					+ "LEFT JOIN stock_generation on s.stock_generation_id = stock_generation.stock_generation_id "
					+ "WHERE tagname = :tagname")
					.setParameter("tagname", String.valueOf(tagname));
			Object[] result = (Object[]) query.uniqueResult();
			session.close();
			return result;
		}
		catch (HibernateException ex) {
			System.out.println(ex.getMessage());
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in update" + ex);
			}
			ex.printStackTrace();
			return null;
		}		
		
		
	}
	
	public  boolean isObservationUnitIdExist(int obsUnitId){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		ObservationUnit ObservationUnit= (ObservationUnit) session.load(ObservationUnit.class, obsUnitId);
		if(ObservationUnit == null){
			session.close();
			return false;
		}else{
			session.close();
			return true;
		}
		
	}
	
	public int getSeasonIndex(String year, String zipcode){
		System.out.println("SELECT MAX(CAST(SUBSTRING(tagname, 4, length(tagname)-20) AS UNSIGNED)) FROM observation_unit WHERE (tagname REGEXP '^"+year+".[0-9]*."+zipcode+"')");
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Query query = session.createSQLQuery("SELECT MAX(CAST(SUBSTRING(tagname, 4, length(tagname)-20) AS UNSIGNED)) FROM observation_unit WHERE (tagname REGEXP '^"+year+".[0-9]*."+zipcode+"')");
		List<Object> results  = query.list();
		int max = 1;
		if(!String.valueOf(results.get(0)).equalsIgnoreCase("null")){
			max = Integer.parseInt(String.valueOf(results.get(0)))+1;
		}
		session.close();
		return max;
		
	}
	
	public int getMaxPlotIndex(String prefix)
	{
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Query query = session.createSQLQuery("SELECT tagname from observation_unit Where tagname LIKE '%"+prefix+"%'");
		List<Object> results  = query.list();
        int max = 0;
        for(Object tagname : results){
        	List<String> tagParts= Arrays.asList(((String) tagname).split("\\."));
    	    int plotIndex = Integer.parseInt(tagParts.get(3));
    	    if(plotIndex > max)
    	    {
    	    	max = plotIndex;
    	    }
        }
		session.close();		
		return max;
		
	}
	
	public  boolean isExist(String tagname)
	{
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Query query = session.createSQLQuery("SELECT tagname from observation_unit Where tagname = '"+ tagname+"'");
		List<Object> results  = query.list();
		session.close();		
		return results.size() > 0 ? true : false ;
		
	}
	
	public  boolean isExist(ArrayList tags)
	{
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Query query = session.createSQLQuery("SELECT tagname from observation_unit Where tagname in ( :ids )")
				.setParameterList("ids", tags);
		List<Object> results  = query.list();
		session.close();		
		return results.size() > 0 ? true : false ;
		
	}
	
	public  ArrayList<ObservationUnit> getObservationUnits(ArrayList tags, Session session)
	{
		Criteria criteria = session.createCriteria(ObservationUnit.class)
                .add(Restrictions.in("tagname", tags));
		ArrayList<ObservationUnit> obs  =  (ArrayList<ObservationUnit>) criteria.list();
		return obs;
		
	}
	
	
	
	public ObservationUnit createNewTagInHarvesting(String tagname){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Query query = session.createSQLQuery("SELECT * from observation_unit Where tagname like '%" + tagname.substring(0,17) + "%'" )
				  .addEntity(ObservationUnit.class);
		List<ObservationUnit> obsWithSamePrefis  = query.list();
		
		ObservationUnit lastPlantTag= obsWithSamePrefis.get(obsWithSamePrefis.size()-1);		
		ObservationUnit newObs = new  ObservationUnit(lastPlantTag.getField(),null,lastPlantTag.getStock(),lastPlantTag.getCoordX(),
				lastPlantTag.getCoordY(),lastPlantTag.getCoordZ(),lastPlantTag.getPlot(),lastPlantTag.getRow(),null,
				null,lastPlantTag.getPurpose(),lastPlantTag.getPlantingDate(),lastPlantTag.getHarvestDate(),lastPlantTag.getKernels(),
				lastPlantTag.getDelay(),lastPlantTag.getObservationUnitComments(),null,null,null);
		
		int plantid = Integer.parseInt(lastPlantTag.getPlant())+1;
		DecimalFormat tagFormat = new DecimalFormat("0000000");
		String plant =tagFormat.format(plantid);
		newObs.setPlant(plant);
		newObs.setTagname(tagname.substring(0,17)+plant);
		Transaction transaction = session.beginTransaction();
		session.saveOrUpdate(newObs);
		transaction.commit();
		session.close();
		return newObs;	
	}
	
	public  ObservationUnit getObservationUnit(String tagname)
	{
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Query query = session.createSQLQuery("SELECT * from observation_unit Where tagname = :tagname")
				  .addEntity(ObservationUnit.class).setParameter("tagname", tagname);;
		ObservationUnit obs  = (ObservationUnit)query.list().get(0);
		session.close();		
		return obs;
		
	}
	
	public  String findByTagid(int tagid)
	{
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Query query = session.createSQLQuery("SELECT tagname from observation_unit Where obseration_unit_id = "+ tagid+"");
		List<Object> results  = query.list();
		session.close();		
		return (String) results.get(0) ;
		
	}
	
	public  ObservationUnit saveOrUpdateTag(ObservationUnit tag, int coordX, int coordY, int plot,
			String plant, String tagName, String purpose, int kernels, int delay, Date plantDate, 
			String comment, MateMethodConnect mateMethodConnect, Stock stock, Field field){	
		ObservationUnit updatedTag = null;
		try{
			if(tag == null)
			{
				updatedTag = new ObservationUnit(field,mateMethodConnect, stock,coordX, coordY, null, plot, null, plant, tagName,
						purpose, plantDate, null,kernels==-1?null:kernels, delay==-1?null:delay, comment,
						null, null, null);
			}else{
				updatedTag = tag;
				updatedTag.setMateMethodConnect(mateMethodConnect);
				updatedTag.setStock(stock);
				updatedTag.setField(field);
				updatedTag.setCoordX(coordX);
				updatedTag.setCoordY(coordY);
				updatedTag.setPlot(plot);
				updatedTag.setPlant(plant);
				updatedTag.setTagname(tagName);
				updatedTag.setPurpose(purpose);
				updatedTag.setKernels(kernels==-1?null:kernels);
				updatedTag.setDelay(delay==-1?null:delay);
				updatedTag.setPlantingDate(plantDate);
				updatedTag.setObservationUnitComments(comment);
			}
		}catch (HibernateException ex) {
			System.out.println(ex.getMessage());
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in update" + ex);
			}
			ex.printStackTrace();
		}		
		return updatedTag;		
	}
	
	public  ObservationUnit findTag(int tagId){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		ObservationUnit tag = (ObservationUnit) session.get(ObservationUnit.class, tagId);
		session.close();
		return tag;		
	}
	
	public  void updateTag(ObservationUnit tag,int stockId, int fieldId){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		try{
			Transaction transaction = session.beginTransaction();
			tag.setStock((Stock) session.get(Stock.class, stockId));
			tag.setField((Field) session.get(Field.class, fieldId));
			session.update(tag);
			transaction.commit();
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
	
	public  void delete(int tagId){
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		try{
			Transaction transaction = session.beginTransaction();
			ObservationUnit tag = (ObservationUnit) session.get(ObservationUnit.class, tagId);
			if(tag != null)
			{
				session.delete(tag);
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
	
	public  List<Object[]> query(String sql){
	   SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
       Session session = sessionFactory.openSession();
       try {
            if(sql!=null)
            {
            	Query query = session.createSQLQuery(sql);
            	List<Object[]> results = query.list();
                return results;
            }
            return null;
         
        } finally {
            session.close();
        }
	}

	public List<Object[]> searchByTags(ArrayList<String> tagnames) {
		StringBuilder tagnamesSql = new StringBuilder();
		tagnamesSql.append("( ");
		for(String stockname: tagnames){
			tagnamesSql.append("'");
			tagnamesSql.append(stockname);
			tagnamesSql.append("',");			
		}
		tagnamesSql.deleteCharAt(tagnamesSql.length()-1);
		tagnamesSql.append(" )");
		
		 StringBuffer queryBuff = new StringBuffer();
	     queryBuff.append("select distinct 1 as a, 1 as b, observation_unit.observation_unit_id, 1 as c, plot, plant, "
	     			+ "coordinate_x, coordinate_y, tagname, stock.stock_id, stock.stock_name, "
	     			+ "accession_name,pedigree, generation, cycle, classification_code, population, "
	     			+ "1 as d , 1 as e ,1 as f, planting_date, kernels, delay, purpose "
	        		+ "from observation_unit "
	        		+ "left join stock on stock.stock_id = observation_unit.stock_id "
	        		+ "left join passport on stock.passport_id = passport.passport_id "
	        		+ "left join classification on passport.classification_id = classification.classification_id "
	        		+ "left join taxonomy on passport.taxonomy_id = taxonomy.taxonomy_id  "
	        		+ "left join stock_generation on stock.stock_generation_id = stock_generation.stock_generation_id "
	        		+ "where tagname in ");
	       
	        
	        
		String sql = queryBuff.toString() + tagnamesSql.toString() + "group by observation_unit.observation_unit_id";
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
        Session session = sessionFactory.openSession();    
        Query query = session.createSQLQuery(sql);
        List<Object []> results = query.list();
        session.close();
        return results;	
		
	}
	
}