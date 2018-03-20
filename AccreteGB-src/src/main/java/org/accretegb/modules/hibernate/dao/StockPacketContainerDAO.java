package org.accretegb.modules.hibernate.dao;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.accretegb.modules.hibernate.StockPacketContainer;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.hibernate.StockPacket;
import org.accretegb.modules.hibernate.ContainerLocation;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("stockPacketContainerDao")
public class StockPacketContainerDAO {
	
	private static StockPacketContainerDAO instance = null;	
	public static StockPacketContainerDAO getInstance() {
	      if(instance == null) {
	         instance = new StockPacketContainerDAO();
	      }
	      return instance;
	}
	
	private  final String SELECTBYSTORAGEUNITID = "SELECT * from stock_packet_container Where LOWER(unit) = :unit ";
	private  final String SELECTBYSTOCKPACKETID = "SELECT * FROM stock_packet_container  "
			+ "INNER JOIN stock_packet ON stock_packet.stock_packet_container_id = stock_packet_container.stock_packet_container_id "
			+ "INNER JOIN container_location ON  container_location.container_location_id = stock_packet_container.container_location_id "
			+ "WHERE LOWER(stock_packet_id) = :stock_packet_id";
	
	
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;

	@SuppressWarnings({ "-access", "unchecked" })
	public  List<StockPacketContainer> findByContainerLocationId(int containerLocationId) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		
		Query query = session.createSQLQuery(SELECTBYSTORAGEUNITID)
				.addEntity(StockPacketContainer.class)
				.setParameter("container_location_id", containerLocationId);
		
		List<StockPacketContainer> stockPacketContainerList = (List<StockPacketContainer>)query.list();
		session.close();
		return stockPacketContainerList;

	}
	
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  List<StockPacketContainer> findByBoxStorageUit(String unit, ContainerLocation containerLocation) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		String unitTerm = unit.equalsIgnoreCase("NULL") ? "is null" : "= :unit";
		String idTerm = containerLocation ==null ? "is null" : "= :container_location_id";
		Query query = session.createSQLQuery("SELECT * FROM stock_packet_container WHERE unit " + unitTerm + "  AND container_location_id " + idTerm)
				.addEntity(StockPacketContainer.class);
		if(! unit.equalsIgnoreCase("NULL")){
		    query.setParameter("unit", unit);
		}
		if(containerLocation!=null){
		    query.setParameter("container_location_id", containerLocation.getContainerLocationId());
		}
		List<StockPacketContainer> stockPacketContainerList = (List<StockPacketContainer>)query.list();
		session.close();
		return stockPacketContainerList;
	}
	
	public  List<StockPacketContainer> findByBox(String unit) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		String unitTerm = unit.equalsIgnoreCase("NULL") ? "is null" : "= :unit";
		Query query = session.createSQLQuery("SELECT * FROM stock_packet_container WHERE unit " + unitTerm)
				.addEntity(StockPacketContainer.class);
		if(! unit.equalsIgnoreCase("NULL")){
		    query.setParameter("unit", unit);
		}
		List<StockPacketContainer> stockPacketContainerList = (List<StockPacketContainer>)query.list();
		session.close();
		return stockPacketContainerList;
	}
	
	
	
	
	public  void update(StockPacketContainer divPacketloc, ContainerLocation containerLocation) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			divPacketloc.setContainerLocation(containerLocation);
			session.update(divPacketloc);
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
	
	public  StockPacketContainer insert(ContainerLocation containerLocation, String stockPacketContainerAcc,String unit, String stockPacketContainerComments, Set<StockPacket> stockPackets) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			StockPacketContainer stockPacketContainer = new StockPacketContainer();
			stockPacketContainer.setContainerLocation(containerLocation);
			stockPacketContainer.setUnit(unit);
			stockPacketContainer.setStockPacketContainerComments(stockPacketContainerComments);
			stockPacketContainer.setStockPackets(stockPackets);
			session.save(stockPacketContainer);
			transaction.commit();
			return stockPacketContainer;
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
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  List findByPacketID(int packetid) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		Query query = session.createSQLQuery(SELECTBYSTOCKPACKETID)
				.addEntity(StockPacket.class)
				.addEntity(ContainerLocation.class)
				.addEntity(StockPacketContainer.class)
				.setParameter("stock_packet_id", packetid);	
		List resultList = query.list();
		session.close();
		return resultList;
	}
	
	public  void delete(StockPacketContainer stockPacketContainer){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			session.delete(stockPacketContainer);
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
	
	public  void deleteByUnit(String stockPacketContainerUnit){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		try{
			Query query = session.createSQLQuery(SELECTBYSTORAGEUNITID)
					.addEntity(StockPacketContainer.class)
					.setParameter("unit", stockPacketContainerUnit);			
			List<StockPacketContainer> stockPacketContainerList = (List<StockPacketContainer>)query.list();
			Transaction transaction = session.beginTransaction();			
			session.delete(stockPacketContainerList.get(0));
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