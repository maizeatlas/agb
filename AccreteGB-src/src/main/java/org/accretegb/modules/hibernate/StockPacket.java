package org.accretegb.modules.hibernate;
// Generated Jan 12, 2016 9:25:45 AM by Hibernate Tools 4.3.1.Final

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * StockPacket generated by hbm2java
 */
@Entity
@Table(name = "stock_packet", catalog = "agbv2")
public class StockPacket implements java.io.Serializable {

	private Integer stockPacketId;
	private Stock stock;
	private StockPacketContainer stockPacketContainer;
	private Integer packetNo;
	private Double weight;
	private Integer noSeed;
	private Date stockPacketDate;
	private String stockPacketComments;

	public StockPacket() {
	}

	public StockPacket(Stock stock, StockPacketContainer stockPacketContainer, Integer packetNo, Double weight,
			Integer noSeed, Date stockPacketDate, String stockPacketComments) {
		this.stock = stock;
		this.stockPacketContainer = stockPacketContainer;
		this.packetNo = packetNo;
		this.weight = weight;
		this.noSeed = noSeed;
		this.stockPacketDate = stockPacketDate;
		this.stockPacketComments = stockPacketComments;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "stock_packet_id", unique = true, nullable = false)
	public Integer getStockPacketId() {
		return this.stockPacketId;
	}

	public void setStockPacketId(Integer stockPacketId) {
		this.stockPacketId = stockPacketId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_id",nullable = false)
	public Stock getStock() {
		return this.stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_packet_container_id")
	public StockPacketContainer getStockPacketContainer() {
		return this.stockPacketContainer;
	}

	public void setStockPacketContainer(StockPacketContainer stockPacketContainer) {
		this.stockPacketContainer = stockPacketContainer;
	}

	@Column(name = "packet_no")
	public Integer getPacketNo() {
		return this.packetNo;
	}

	public void setPacketNo(Integer packetNo) {
		this.packetNo = packetNo;
	}

	@Column(name = "weight")
	public Double getWeight() {
		return this.weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	@Column(name = "no_seed")
	public Integer getNoSeed() {
		return this.noSeed;
	}

	public void setNoSeed(Integer noSeed) {
		this.noSeed = noSeed;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "stock_packet_date", length = 0)
	public Date getStockPacketDate() {
		return this.stockPacketDate;
	}

	public void setStockPacketDate(Date stockPacketDate) {
		this.stockPacketDate = stockPacketDate;
	}

	@Column(name = "stock_packet_comments", length = 65535)
	public String getStockPacketComments() {
		return this.stockPacketComments;
	}

	public void setStockPacketComments(String stockPacketComments) {
		this.stockPacketComments = stockPacketComments;
	}

}
