package org.accretegb.modules.hibernate;
// Generated Jan 7, 2016 12:35:46 PM by Hibernate Tools 4.3.1.Final

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;

/**
 * ObservationUnit generated by hbm2java
 */
@Entity
@Table(name = "observation_unit", catalog = "agbv2")
public class ObservationUnit implements java.io.Serializable {

	private Integer observationUnitId;
	private Field field;
	private MateMethodConnect mateMethodConnect;
	private Stock stock;
	private Integer coordX;
	private Integer coordY;
	private Integer coordZ;
	private Integer plot;
	private Integer row;
	private String plant;
	private String tagname;
	private String purpose;
	private Date plantingDate;
	private Date harvestDate;
	private Integer kernels;
	private Integer delay;
	private String obsUnitComments;
	private Set<MeasurementValue> measurementValues = new HashSet(0);
	private Set<ObservationUnitSample> observationUnitSamples = new HashSet(0);
	private Set<StockComposition> mixFromStocks = new HashSet(0);

	public ObservationUnit() {
	}

	public ObservationUnit(Field field, MateMethodConnect mateMethodConnect, Stock stock,
			Integer coordX, Integer coordY, Integer coordZ, Integer plot, Integer row, String plant, String tagname,
			String purpose, Date plantingDate, Date harvestDate, Integer kernels,Integer delay, String obsUnitComments,
			Set<MeasurementValue> measurementValues, Set<ObservationUnitSample> observationUnitSamples, Set<StockComposition> mixFromStocks) {
		this.field = field;
		this.mateMethodConnect = mateMethodConnect;
		this.stock = stock;
		this.coordX = coordX;
		this.coordY = coordY;
		this.coordZ = coordZ;
		this.plot = plot;
		this.row = row;
		this.plant = plant;
		this.tagname = tagname;
		this.purpose = purpose;
		this.plantingDate = plantingDate;
		this.harvestDate = harvestDate;
		this.kernels = kernels;
		this.delay = delay;
		this.obsUnitComments = obsUnitComments;
		this.measurementValues = measurementValues;
		this.observationUnitSamples = observationUnitSamples;
		this.mixFromStocks = mixFromStocks;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)

	@Column(name = "observation_unit_id", unique = true, nullable = false)
	public Integer getObservationUnitId() {
		return this.observationUnitId;
	}

	public void setObservationUnitId(Integer observationUnitId) {
		this.observationUnitId = observationUnitId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "field_id", nullable = false)
	public Field getField() {
		return this.field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mate_method_connect_id")
	public MateMethodConnect getMateMethodConnect() {
		return this.mateMethodConnect;
	}

	public void setMateMethodConnect(MateMethodConnect mateMethodConnect) {
		this.mateMethodConnect = mateMethodConnect;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_id")
	public Stock getStock() {
		return this.stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}


	@Column(name = "coordinate_x")
	public Integer getCoordX() {
		return this.coordX;
	}

	public void setCoordX(Integer coordX) {
		this.coordX = coordX;
	}

	@Column(name = "coordinate_y")
	public Integer getCoordY() {
		return this.coordY;
	}

	public void setCoordY(Integer coordY) {
		this.coordY = coordY;
	}

	@Column(name = "coordinate_z")
	public Integer getCoordZ() {
		return this.coordZ;
	}

	public void setCoordZ(Integer coordZ) {
		this.coordZ = coordZ;
	}

	@Column(name = "plot")
	public Integer getPlot() {
		return this.plot;
	}

	public void setPlot(Integer plot) {
		this.plot = plot;
	}

	@Column(name = "row")
	public Integer getRow() {
		return this.row;
	}

	public void setRow(Integer row) {
		this.row = row;
	}

	@Column(name = "plant")
	public String getPlant() {
		return this.plant;
	}

	public void setPlant(String plant) {
		this.plant = plant;
	}

	@Column(name = "tagname", nullable = false)
	public String getTagname() {
		return this.tagname;
	}

	public void setTagname(String tagname) {
		this.tagname = tagname;
	}

	@Column(name = "purpose")
	public String getPurpose() {
		return this.purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "planting_date", length = 0)
	public Date getPlantingDate() {
		return this.plantingDate;
	}

	public void setPlantingDate(Date plantingDate) {
		this.plantingDate = plantingDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "harvest_date", length = 0)
	public Date getHarvestDate() {
		return this.harvestDate;
	}

	public void setHarvestDate(Date harvestDate) {
		this.harvestDate = harvestDate;
	}
	
	@Column(name = "kernels")
	public Integer getKernels() {
		return this.kernels;
	}

	public void setKernels(Integer kernels) {
		this.kernels = kernels;
	}

	@Column(name = "delay")
	public Integer getDelay() {
		return this.delay;
	}

	public void setDelay(Integer delay) {
		this.delay = delay;
	}

	@Column(name = "observation_unit_comments", length = 65535)
	public String getObservationUnitComments() {
		return this.obsUnitComments;
	}

	public void setObservationUnitComments(String obsUnitComments) {
		this.obsUnitComments = obsUnitComments;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "observationUnit")
	public Set<MeasurementValue> getMeasurementValues() {
		return this.measurementValues;
	}

	public void setMeasurementValues(Set<MeasurementValue> measurementValues) {
		this.measurementValues = measurementValues;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "observationUnit")
	public Set<ObservationUnitSample> getObservationUnitSamples() {
		return this.observationUnitSamples;
	}

	public void setObservationUnitSamples(Set<ObservationUnitSample> observationUnitSamples) {
		this.observationUnitSamples = observationUnitSamples;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "observationUnit")
	public Set<StockComposition> getStockCompositions() {
		return this.mixFromStocks;
	}

	public void setStockCompositions(Set<StockComposition> mixFromStocks) {
		this.mixFromStocks = mixFromStocks;
	}

}
