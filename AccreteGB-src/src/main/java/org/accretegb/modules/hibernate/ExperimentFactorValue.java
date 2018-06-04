package org.accretegb.modules.hibernate;
// Generated Jan 7, 2016 12:35:46 PM by Hibernate Tools 4.3.1.Final

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * ExperimentFactorValue generated by hbm2java
 */
@Entity
@Table(name = "experiment_factor_value", catalog = "agbv2" )
public class ExperimentFactorValue implements java.io.Serializable {

	private Integer experimentFactorValueId;
	private Experiment experiment;
	private ExperimentFactor experimentFactor;
	private Stock stock;
	private MeasurementUnit measurementUnit;
	private Integer observationUnitId;
	private String expFactorValueLevel;
	private String expFactorValueComments;

	public ExperimentFactorValue() {
	}

	public ExperimentFactorValue(Experiment experiment, ExperimentFactor experimentFactor,
			Stock stock, MeasurementUnit measurementUnit,
			Integer observationUnitId, String expFactorValueLevel, String expFactorValueComments) {
		this.experiment = experiment;
		this.experimentFactor = experimentFactor;
		this.stock = stock;
		this.measurementUnit = measurementUnit;
		this.observationUnitId = observationUnitId;
		this.expFactorValueLevel = expFactorValueLevel;
		this.expFactorValueComments = expFactorValueComments;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "experiment_factor_value_id", unique = true, nullable = false)
	public Integer getExperimentFactorValueId() {
		return this.experimentFactorValueId;
	}

	public void setExperimentFactorValueId(Integer experimentFactorValueId) {
		this.experimentFactorValueId = experimentFactorValueId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "experiment_id",nullable = false)
	public Experiment getExperiment() {
		return this.experiment;
	}

	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "experiment_factor_id",nullable = false)
	public ExperimentFactor getExperimentFactor() {
		return this.experimentFactor;
	}

	public void setExperimentFactor(ExperimentFactor experimentFactor) {
		this.experimentFactor = experimentFactor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_id")
	public Stock getStock() {
		return this.stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "measurement_unit_id")
	public MeasurementUnit getMeasurementUnit() {
		return this.measurementUnit;
	}

	public void setMeasurementUnit(MeasurementUnit measurementUnit) {
		this.measurementUnit = measurementUnit;
	}

	@Column(name = "observation_unit_id")
	public Integer getObservationUnitId() {
		return this.observationUnitId;
	}

	public void setObservationUnitId(Integer observationUnitId) {
		this.observationUnitId = observationUnitId;
	}

	@Column(name = "exp_factor_value_level",nullable = false)
	public String getExpFactorValueLevel() {
		return this.expFactorValueLevel;
	}

	public void setExpFactorValueLevel(String expFactorValueLevel) {
		this.expFactorValueLevel = expFactorValueLevel;
	}

	@Column(name = "exp_factor_value_comments", length = 65535)
	public String getExpFactorValueComments() {
		return this.expFactorValueComments;
	}

	public void setExpFactorValueComments(String expFactorValueComments) {
		this.expFactorValueComments = expFactorValueComments;
	}

}