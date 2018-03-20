package org.accretegb.modules.hibernate.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.Location;
import org.accretegb.modules.hibernate.MateMethodConnect;
import org.accretegb.modules.hibernate.StockPacketContainer;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.hibernate.ContainerLocation;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("containerLocationDAO")
public class ContainerLocationDAO {
	
	private static ContainerLocationDAO instance = null;	
	public static ContainerLocationDAO getInstance() {
	      if(instance == null) {
	         instance = new ContainerLocationDAO();
	      }
	      return instance;
	}
	
	private  final String SELECTUNIQUESTORAGEUNIT = "SELECT * FROM container_location WHERE "
			+ "LOWER(building) = :building AND LOWER(room) = :room "
			+ "AND LOWER(shelf) = :shelf";
	
	private  final String SELECTSTORAGEUNITBYBUILDINGROOM = "SELECT * FROM container_location WHERE LOWER(room) = :room"
			+ " AND LOWER(building) = :building";
	
	private  final String SELECTSTORAGEUNITSBYLocation = "SELECT * FROM container_location INNER JOIN location"
			+ " on container_location.location_id = location.location_id"
			+ " group by container_location.location_id, building, room";

	private  final String SELECTNUMBERSOFSHELVES = "SELECT count(*) FROM container_location "
			+ "group by container_location.location_id, building, room";
	
	private  final String SELECTALLSTORAGEUNITS = "SELECT  * FROM container_location  ORDER BY room";
	
	private  final String SELECTDIVSTORAGEUNITBYSHELF= "SELECT * FROM container_location WHERE LOWER(shelf) = :shelf";
	
	private  final String SELECTSHELFFROMBOX= "SELECT * FROM stock_packet_container, container_location WHERE LOWER(unit) = :unit"
			+ " AND stock_packet_container.container_location_id = container_location.container_location_id";

	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;

	@SuppressWarnings({ "-access", "unchecked" })
	public  List<ContainerLocation> findUniqueContainerLocation(String building, String room, String shelf) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Query query = session.createSQLQuery(SELECTUNIQUESTORAGEUNIT)
				.addEntity(ContainerLocation.class)
				.setParameter("building", building)
				.setParameter("room", room)
				.setParameter("shelf", shelf);
		List<ContainerLocation> storageunitList = (List<ContainerLocation>)query.list();
		session.close();
		return storageunitList;

	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  List<ContainerLocation> findByBuildingRoom( String building, String room) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		Query query = session.createSQLQuery(SELECTSTORAGEUNITBYBUILDINGROOM)
				.addEntity(ContainerLocation.class)
				.setParameter("building", building)
				.setParameter("room", room);
		List<ContainerLocation> storageunitList = (List<ContainerLocation>)query.list();
		session.close();
		return storageunitList;

	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  List findByShelf(String shelf) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		Criteria c = session.createCriteria(ContainerLocation.class)
				.add((shelf.toLowerCase().equals("select"))?Restrictions.isNull("shelf"):Restrictions.eq("shelf", shelf.toLowerCase()));			
		
		List resultSet = c.list();
		session.close();
		return resultSet;

	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  ContainerLocation insert(Location divLocation, String building, String room,
			String tier1_position, String tier2_position, String tier3_position, String shelf,
			String containerLocationComments, Set<StockPacketContainer> stockPacketContainers) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		try{
			Transaction transaction = session.beginTransaction();
			ContainerLocation storageunit = new ContainerLocation(divLocation, building,room, tier1_position, tier2_position, tier3_position, shelf,containerLocationComments, stockPacketContainers);
			session.save(storageunit);
			transaction.commit();
			return storageunit;
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
				LoggerUtils.log(Level.INFO, "error in insert" + ex);
			ex.printStackTrace();
			return null;
		} finally {
			session.close();
		}
	
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  ContainerLocation updateContainerLocation(int divLocationId,int containerLocationId, String building, String room,
			String tier1_position, String tier2_position, String tier3_position, String shelf,
			String containerLocationComments, Set<StockPacketContainer> stockPacketContainers) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		try{
			Location divLocation = new Location();
			divLocation.setLocationId(divLocationId);					
			Transaction transaction = session.beginTransaction();
			ContainerLocation storageunit = new ContainerLocation(divLocation, building,room, tier1_position, tier2_position, tier3_position, shelf,containerLocationComments, stockPacketContainers);
			storageunit.setContainerLocationId(containerLocationId);
			session.update(storageunit);
			transaction.commit();
			return storageunit;
		}catch (HibernateException ex) {
		if(LoggerUtils.isLogEnabled())
				LoggerUtils.log(Level.INFO, "error in update" + ex);
			ex.printStackTrace();
			return null;
		} finally {
			session.close();
		}
		
	}
	
	public  void updateListOfContainerLocation(List<String[]> updateContainerLocations) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		try{
			Transaction transaction = session.beginTransaction();
			for(String[] values : updateContainerLocations){
				Location divLocation = new Location();
				divLocation.setLocationId(Integer.parseInt(values[0]));
				ContainerLocation storageunit = new ContainerLocation(divLocation, values[2],values[3], values[4], values[5],values[6], values[7], null, null);
				storageunit.setContainerLocationId(Integer.parseInt(values[1]));
				session.update(storageunit);
			}
			transaction.commit();
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
				LoggerUtils.log(Level.INFO, "error in update" + ex);
			ex.printStackTrace();
		} finally {
			session.close();
		}
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  List findByLocation() {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Query query = session.createSQLQuery(SELECTSTORAGEUNITSBYLocation)
				.addEntity(Location.class)
				.addEntity(ContainerLocation.class);
		List resultList = query.list();
		session.close();
		return resultList;

	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  List<BigInteger> findNumberOfShelvesEachRoom() {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		Query query = session.createSQLQuery(SELECTNUMBERSOFSHELVES);
		List resultList = query.list();				
		session.close();
		return resultList;
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  List<ContainerLocation> findAllContainerLocations() {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Query query = session.createSQLQuery(SELECTALLSTORAGEUNITS).addEntity(ContainerLocation.class);
		List<ContainerLocation> storageunitList = (List<ContainerLocation>)query.list();
		session.close();
		return storageunitList;

	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  void deleteContainerLocation(ContainerLocation containerLocation) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		try{		
			Transaction transaction = session.beginTransaction();
			session.delete(containerLocation);
			transaction.commit();
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
					LoggerUtils.log(Level.INFO, "error in delete" + ex);
				ex.printStackTrace();
		} finally {
			session.close();
		}
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  void deleteListOfContainerLocation(List<ContainerLocation> containerLocations) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		try{
			Transaction transaction = session.beginTransaction();
			for(ContainerLocation containerLocation: containerLocations){
				session.delete(containerLocation);	
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
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  List findByBox(String unit) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		Query query = session.createSQLQuery(SELECTSHELFFROMBOX)
				.addEntity(StockPacketContainer.class)
				.addEntity(ContainerLocation.class)
				.setParameter("unit", unit.toLowerCase());
		
		List resultList = query.list();
		session.close();
		return resultList;

	}
	

}