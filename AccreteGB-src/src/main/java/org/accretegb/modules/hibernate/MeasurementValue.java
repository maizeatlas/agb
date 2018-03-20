package org.accretegb.modules.hibernate;
// Generated Jan 7, 2016 12:35:46 PM by Hibernate Tools 4.3.1.Final

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
import javax.persistence.UniqueConstraint;

/**
 * MeasurementValue generated by hbm2java
 */
@Entity
@Table(name = "measurement_value", catalog = "agbv2")
public class MeasurementValue implements java.io.Serializable {

	private Integer measurementValueId;
	private Source source;
	private Field field;
	private MeasurementParameter measurementParameter;
	private ObservationUnit observationUnit;
	private MeasurementType measurementType;
	private Date tom;
	private String value;
	private String measurementComments;

	public MeasurementValue() {
	}

	public MeasurementValue(Source source, Field field, MeasurementParameter measurementParameter,
			ObservationUnit observationUnit, MeasurementType measurementType, Date tom, String value,
			String measurementComments) {
		this.source = source;
		this.field = field;
		this.measurementParameter = measurementParameter;
		this.observationUnit = observationUnit;
		this.measurementType = measurementType;
		this.tom = tom;
		this.value = value;
		this.measurementComments = measurementComments;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "measurement_value_id", unique = true, nullable = false)
	public Integer getMeasurementValueId() {
		return this.measurementValueId;
	}

	public void setMeasurementValueId(Integer measurementValueId) {
		this.measurementValueId = measurementValueId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "source_id")
	public Source getSource() {
		return this.source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "field_id")
	public Field getField() {
		return this.field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "measurement_parameter_id")
	public MeasurementParameter getMeasurementParameter() {
		return this.measurementParameter;
	}

	public void setMeasurementParameter(MeasurementParameter measurementParameter) {
		this.measurementParameter = measurementParameter;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "observation_unit_id")
	public ObservationUnit getObservationUnit() {
		return this.observationUnit;
	}

	public void setObservationUnit(ObservationUnit observationUnit) {
		this.observationUnit = observationUnit;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "measurement_type_id")
	public MeasurementType getMeasurementType() {
		return this.measurementType;
	}

	public void setMeasurementType(MeasurementType measurementType) {
		this.measurementType = measurementType;
	}


	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "tom", length = 0)
	public Date getTom() {
		return this.tom;
	}

	public void setTom(Date tom) {
		this.tom = tom;
	}

	@Column(name = "value")
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Column(name = "measurement_value_comments", length = 65535)
	public String getMeasurementComments() {
		return this.measurementComments;
	}

	public void setMeasurementComments(String measurementComments) {
		this.measurementComments = measurementComments;
	}

}
