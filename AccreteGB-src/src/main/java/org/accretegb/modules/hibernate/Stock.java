package org.accretegb.modules.hibernate;
// Generated Jan 7, 2016 12:35:46 PM by Hibernate Tools 4.3.1.Final

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 * Stock generated by hbm2java
 */
@Entity
@Table(name = "stock", catalog = "agbv2" )
public class Stock implements java.io.Serializable {

	private Integer stockId;
	private StockGeneration stockGeneration;
	private Passport passport;
	private Project project;
	private String stockName;
	private Date stockDate;
	private String stockComments;
	private Set<ObservationUnit> observationUnits = new HashSet(0);
	private Set<StockPacket> stockPackets = new HashSet(0);
	private Set<StockComposition> mixFromStocksForStockId = new HashSet(0);
	private Set<ExperimentFactorValue> experimentFactorValues = new HashSet(0);
	private Set<StockComposition> mixFromStocksForDivParentId = new HashSet(0);

	public Stock() {
	}

	public Stock(StockGeneration stockGeneration, Passport passport, Project project,
			String stockName, Date stockDate, String stockComments, Set<ObservationUnit> observationUnits,
			Set<StockPacket> stockPackets, Set<StockComposition> mixFromStocksForStockId, Set<ExperimentFactorValue> experimentFactorValues,
			Set<StockComposition> mixFromStocksForDivParentId) {
		this.stockGeneration = stockGeneration;
		this.passport = passport;
		this.project = project;
		this.stockName = stockName;
		this.stockDate = stockDate;
		this.stockComments = stockComments;
		this.observationUnits = observationUnits;
		this.stockPackets = stockPackets;
		this.mixFromStocksForStockId = mixFromStocksForStockId;
		this.experimentFactorValues = experimentFactorValues;
		this.mixFromStocksForDivParentId = mixFromStocksForDivParentId;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "stock_id", unique = true, nullable = false)
	public Integer getStockId() {
		return this.stockId;
	}

	public void setStockId(Integer stockId) {
		this.stockId = stockId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_generation_id")
	public StockGeneration getStockGeneration() {
		return this.stockGeneration;
	}

	public void setStockGeneration(StockGeneration stockGeneration) {
		this.stockGeneration = stockGeneration;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "passport_id",nullable = false)
	public Passport getPassport() {
		return this.passport;
	}

	public void setPassport(Passport passport) {
		this.passport = passport;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	public Project getProject() {
		return this.project;
	}

	public void setProject(Project project) {
		this.project = project;
	}


	@Column(name = "stock_name",nullable = false)
	public String getStockName() {
		return this.stockName;
	}

	public void setStockName(String stockName) {
		this.stockName = stockName;
	}


	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "stock_date", length = 0)
	public Date getStockDate() {
		return this.stockDate;
	}

	public void setStockDate(Date stockDate) {
		this.stockDate = stockDate;
	}

	@Column(name = "stock_comments", length = 65535)
	public String getStockComments() {
		return this.stockComments;
	}

	public void setStockComments(String stockComments) {
		this.stockComments = stockComments;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "stock")
	public Set<ObservationUnit> getObservationUnits() {
		return this.observationUnits;
	}

	public void setObservationUnits(Set<ObservationUnit> observationUnits) {
		this.observationUnits = observationUnits;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "stock")
	public Set<StockPacket> getStockPackets() {
		return this.stockPackets;
	}

	public void setStockPackets(Set<StockPacket> stockPackets) {
		this.stockPackets = stockPackets;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "stockByStockId")
	public Set<StockComposition> getStockCompositionsForStockId() {
		return this.mixFromStocksForStockId;
	}

	public void setStockCompositionsForStockId(Set<StockComposition> mixFromStocksForStockId) {
		this.mixFromStocksForStockId = mixFromStocksForStockId;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "stock")
	public Set<ExperimentFactorValue> getExperimentFactorValues() {
		return this.experimentFactorValues;
	}

	public void setExperimentFactorValues(Set<ExperimentFactorValue> experimentFactorValues) {
		this.experimentFactorValues = experimentFactorValues;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "stockByMixFromStockId")
	public Set<StockComposition> getStockCompositionsForDivParentId() {
		return this.mixFromStocksForDivParentId;
	}

	public void setStockCompositionsForDivParentId(Set<StockComposition> mixFromStocksForDivParentId) {
		this.mixFromStocksForDivParentId = mixFromStocksForDivParentId;
	}

}
