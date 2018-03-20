package org.accretegb.modules.hibernate.dao;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.hibernate.StockPacket;
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

@Component("stockPacketDAO")
public class StockPacketDAO {
	
	private  final String SELECTSTOCKPACKETBYSTOCKIDPACKETNO = "SELECT * FROM stock_packet WHERE LOWER(packet_no) = :packet_no "
			+ "AND LOWER(stock_id) = :stock_id";
	
	private  final String SELECTSTOCKPACKETBYSTOCKID = "SELECT * FROM stock_packet WHERE LOWER(stock_id) = :stock_id";

	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;

	private static StockPacketDAO instance = null;	
	public static StockPacketDAO getInstance() {
	      if(instance == null) {
	         instance = new StockPacketDAO();
	      }
	      return instance;
	}
	@SuppressWarnings({ "-access", "unchecked" })
	public  List<StockPacket> findByStockandPacket(int newStockid, int packetNo) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Query query = session.createSQLQuery(SELECTSTOCKPACKETBYSTOCKIDPACKETNO)
				.addEntity(StockPacket.class)
				.setParameter("packet_no", packetNo)
				.setParameter("stock_id", newStockid);	
		List<StockPacket> stockPacketsList = (List<StockPacket>)query.list();
		session.close();
		return stockPacketsList;

	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  List<StockPacket> findByStock(int newStockid) {
		Session session = hibernateSessionFactory.getSessionFactory().openSession();		
		Query query = session.createSQLQuery(SELECTSTOCKPACKETBYSTOCKID)
				.addEntity(StockPacket.class)
				.setParameter("stock_id", newStockid);	
		List<StockPacket> stockPacketsList = (List<StockPacket>)query.list();
		session.close();
		return stockPacketsList;

	}
	
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  StockPacket insert(StockPacket stockPacket){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();
		try{
			Transaction transaction = session.beginTransaction();
			session.save(stockPacket);
			transaction.commit();
			return stockPacket;
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in insert" + ex);
			}
			ex.printStackTrace();
			return null;
		}finally {
			session.close();
		}
		
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  void updateStockPacket(StockPacket stockPacket, int stockPacketId){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			stockPacket.setStockPacketId(stockPacketId);
			session.update(stockPacket);
			transaction.commit();
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in insert" + ex);
			}
			ex.printStackTrace();
		} finally {
			session.close();
		}
	}
	
	public  StockPacket updateStockPacket(int stockPacketId, int packetNo, int noSeed, Date date, String comment,Session session){
		StockPacket packet = (StockPacket) session.load(StockPacket.class, stockPacketId);
		try{
			packet.setPacketNo(packetNo);
			packet.setNoSeed(noSeed);
			packet.setStockPacketDate(date);
			packet.setStockPacketComments(comment);
		}catch (HibernateException ex) {
			if(LoggerUtils.isLogEnabled())
			{
				LoggerUtils.log(Level.INFO, "error in insert" + ex);
			}
			ex.printStackTrace();
		}
		return packet;
	}
	
	public  ArrayList<StockPacket> getStockPackets(ArrayList packetIds)
	{
		SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
		Session session = sessionFactory.openSession();
		Criteria criteria = session.createCriteria(StockPacket.class)
                .add(Restrictions.in("stock_packet_id", packetIds));
		ArrayList<StockPacket> stockPackets  =  (ArrayList<StockPacket>) criteria.list();
		return stockPackets;		
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  void delete(int stockPacketId){
		Session session = hibernateSessionFactory.getSessionFactory().openSession();	
		try{
			Transaction transaction = session.beginTransaction();
			StockPacket stockPacket  = (StockPacket) session.load(StockPacket.class, stockPacketId);
			session.delete(stockPacket);
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