package org.accretegb.modules.hibernate.dao;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.Source;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.hibernate.Field;
import org.accretegb.modules.hibernate.Location;
import org.accretegb.modules.hibernate.ContainerLocation;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("locationDAO")
public class LocationDAO {
	
	private  final String SELECTALLLOCALITIES = "SELECT * FROM location";
	private static LocationDAO instance = null;	
	public static LocationDAO getInstance() {
	      if(instance == null) {
	         instance = new LocationDAO();
	      }
	      return instance;
	}
	
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  List<Location> findLocationByCityZipCodeName(String city, String zipcode, String LocationName) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
        String cityTerm = city== null ? "is null " : "= :city ";
		String zipcodeTerm = zipcode == null ? "is null " : "= :zipcode ";
		String LocationNameTerm = LocationName == null ? "is null " : "= :location_name ";
		Query query = session.createSQLQuery("SELECT * FROM location WHERE "
				+ "LOWER(city) " + cityTerm 
				+ "AND zipcode " + zipcodeTerm
				+ "AND LOWER(location_name) " + LocationNameTerm)
				.addEntity(Location.class);
		
		if(city != null){
		    query.setParameter("city", city);
		}
		if(zipcode != null){
		    query.setParameter("zipcode", zipcode);
		}
		if(LocationName != null){
		    query.setParameter("location_name", LocationName);
		}
        List<Location> LocationList = (List<Location>) query.list();
        session.close();
        return LocationList;
	}
	
	public  Location findLocation( String locationName,  String city, String state, String country, String zipcode, String locationComments) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
        String cityTerm = String.valueOf(city).equalsIgnoreCase("null")? "is null " : "= :city ";
        String stateTerm =  String.valueOf(state).equalsIgnoreCase("null")? "is null " : "= :state_province ";
        String countryTerm =  String.valueOf(country).equalsIgnoreCase("null")? "is null " : "= :country ";
		String zipcodeTerm =  String.valueOf(zipcode).equalsIgnoreCase("null")? "is null " : "= :zipcode ";
		String locationNameTerm =  String.valueOf(locationName).equalsIgnoreCase("null")? "is null " : "= :location_name ";
		String locationCommentTerm =  String.valueOf(locationComments).equalsIgnoreCase("null")? "is null " : "= :location_comments ";
		Query query = session.createSQLQuery("SELECT * FROM location WHERE "
				+ "LOWER(city) " + cityTerm 
				+ "AND zipcode " + zipcodeTerm
				+ "AND LOWER(location_name) " + locationNameTerm
				+ "AND LOWER(state_province) " + stateTerm
				+ "AND LOWER(country) " + countryTerm
				+ "AND LOWER(location_comments) " + locationCommentTerm)
				.addEntity(Location.class);
		
		if(!String.valueOf(locationName).equalsIgnoreCase("null")){
		    query.setParameter("location_name", locationName);
		}
		if(!String.valueOf(city).equalsIgnoreCase("null")){
		    query.setParameter("city", city);
		}
		
		if(!String.valueOf(state).equalsIgnoreCase("null")){
		    query.setParameter("state_province", state);
		}
		if(!String.valueOf(country).equalsIgnoreCase("null")){
		    query.setParameter("country", country);
		}
		if(!String.valueOf(zipcode).equalsIgnoreCase("null")){
		    query.setParameter("zipcode", zipcode);
		}
		if(!String.valueOf(locationComments).equalsIgnoreCase("null")){
		    query.setParameter("location_comments", locationComments);
		}
    	
		List<Location> LocationList = (List<Location>) query.list();
        session.close();
        return LocationList.size() > 0 ? LocationList.get(0) : null;
	}
	
	public  void updateLocation(String locationName,  String city, String state, String country, String zipcode, String comments, Integer existingLocationId){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		try{
			Transaction transaction = session.beginTransaction();
			Location updateLocation = new Location();
			updateLocation.setLocationId(existingLocationId);
			updateLocation.setLocationName(String.valueOf(locationName).equalsIgnoreCase("null") ? null : locationName);
			updateLocation.setCity(String.valueOf(city).equalsIgnoreCase("null") ? null : city);
			updateLocation.setStateProvince(String.valueOf(state).equalsIgnoreCase("null") ?null : state);
			updateLocation.setCountry(String.valueOf(country).equalsIgnoreCase("null") ?null : country);
			updateLocation.setZipcode(String.valueOf(zipcode).equalsIgnoreCase("null") ?null : zipcode);
			updateLocation.setLocationComments(String.valueOf(comments).equalsIgnoreCase("null") ?null : comments);
			session.update(updateLocation);
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
	
	
	public  List<Location> findLocationByCityZipcode(String city, String zipcode) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		if(session == null) {
			SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
			session = sessionFactory.openSession();
		}
		Criteria criteria = session.createCriteria(Location.class);
		criteria.add(Restrictions.ilike("city", city.toLowerCase()));
		criteria.add(Restrictions.ilike("zipcode", zipcode));
		List<Location> LocationList = criteria.list();
        session.close();
        return LocationList;
	}
	
	public  List<Location> findLocationByZipcode(String city, String zipcode) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		if(session == null) {
			SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
			session = sessionFactory.openSession();
		}
		Criteria criteria = session.createCriteria(Location.class);
		criteria.add(Restrictions.ilike("zipcode", zipcode));
		List<Location> LocationList = criteria.list();
        session.close();
        return LocationList;
	}
	
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  List<Location> findLocationByZipcode(String zipcode) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		if(session == null) {
			SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
			session = sessionFactory.openSession();
		}
		Criteria criteria = session.createCriteria(Location.class);
		criteria.add(Restrictions.ilike("zipcode", zipcode));
        
        List<Location> LocationList = criteria.list();
        session.close();
        return LocationList;
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  List<Location> findAllLocalities() {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
        Query query = session.createSQLQuery(SELECTALLLOCALITIES).addEntity(Location.class);
        
        List<Location> LocationList = (List<Location>) query.list();
        session.close();
        return LocationList;
	}
	

	@SuppressWarnings({ "-access", "unchecked" })
	public  Location insertNewLocation(
			String locationName, String city,
			String state, String country, String zipcode,
			String LocationComments, Set<Source> Sources,
			Set<Field> fields, Set<ContainerLocation> containerLocations) {

		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		if(session == null) {
			SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
			session = sessionFactory.openSession();
		}
		try{
			
			Transaction transaction = session.beginTransaction();
			Location location = new Location();
			location.setLocationName(String.valueOf(locationName).equalsIgnoreCase("null") ? null : locationName);
			location.setCity(String.valueOf(city).equalsIgnoreCase("null") ? null : city);
			location.setStateProvince(String.valueOf(state).equalsIgnoreCase("null") ? null : state);
			location.setCountry(String.valueOf(country).equalsIgnoreCase("null") ? null : country);
			location.setZipcode(String.valueOf(zipcode).equalsIgnoreCase("null") ? null : zipcode);
			location.setLocationComments(String.valueOf(LocationComments).equalsIgnoreCase("null") ? null : LocationComments);
			session.save(location);
			transaction.commit();
			return location;
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