package org.accretegb.modules.hibernate;
// Generated Jan 13, 2016 4:22:32 PM by Hibernate Tools 4.3.1.Final

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

/**
 * MeasurementParameter generated by hbm2java
 */
@Entity
@Table(name = "measurement_parameter", catalog = "agbv2")
public class MeasurementParameter implements java.io.Serializable {

	private Integer measurementParameterId;
	private MeasurementUnit measurementUnit;
	private String ontologyAccession;
	private String measurementClassification;
	private String parameterName;
	private String parameterCode;
	private String format;
	private String defaultValue;
	private String minValue;
	private String maxValue;
	private String categories;
	private String isVisible;
	private String protocol;
	private Set<MeasurementValue> measurementValues = new HashSet(0);

	public MeasurementParameter() {
	}

	public MeasurementParameter(MeasurementUnit measurementUnit, String ontologyAccession,
			String measurementClassification, String parameterName, String parameterCode, String format,
			String defaultValue, String minValue, String maxValue, String categories, String isVisible, String protocol,
			Set<MeasurementValue> measurementValues) {
		this.measurementUnit = measurementUnit;
		this.ontologyAccession = ontologyAccession;
		this.measurementClassification = measurementClassification;
		this.parameterName = parameterName;
		this.parameterCode = parameterCode;
		this.format = format;
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.categories = categories;
		this.isVisible = isVisible;
		this.protocol = protocol;
		this.measurementValues = measurementValues;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "measurement_parameter_id", unique = true, nullable = false)
	public Integer getMeasurementParameterId() {
		return this.measurementParameterId;
	}

	public void setMeasurementParameterId(Integer measurementParameterId) {
		this.measurementParameterId = measurementParameterId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "measurement_unit_id",nullable = false)
	public MeasurementUnit getMeasurementUnit() {
		return this.measurementUnit;
	}

	public void setMeasurementUnit(MeasurementUnit measurementUnit) {
		this.measurementUnit = measurementUnit;
	}

	@Column(name = "ontology_accession")
	public String getOntologyAccession() {
		return this.ontologyAccession;
	}

	public void setOntologyAccession(String ontologyAccession) {
		this.ontologyAccession = ontologyAccession;
	}

	@Column(name = "measurement_classification")
	public String getMeasurementClassification() {
		return this.measurementClassification;
	}

	public void setMeasurementClassification(String measurementClassification) {
		this.measurementClassification = measurementClassification;
	}

	@Column(name = "parameter_name",nullable = false)
	public String getParameterName() {
		return this.parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	@Column(name = "parameter_code")
	public String getParameterCode() {
		return this.parameterCode;
	}

	public void setParameterCode(String parameterCode) {
		this.parameterCode = parameterCode;
	}

	@Column(name = "[format]")
	public String getFormat() {
		return this.format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Column(name = "defaultValue")
	public String getDefaultValue() {
		return this.defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Column(name = "minValue")
	public String getMinValue() {
		return this.minValue;
	}

	public void setMinValue(String minValue) {
		this.minValue = minValue;
	}

	@Column(name = "[maxValue]")
	public String getMaxValue() {
		return this.maxValue;
	}

	public void setMaxValue(String maxValue) {
		this.maxValue = maxValue;
	}

	@Column(name = "categories")
	public String getCategories() {
		return this.categories;
	}

	public void setCategories(String categories) {
		this.categories = categories;
	}

	@Column(name = "isVisible",nullable = false)
	public String getIsVisible() {
		return this.isVisible;
	}

	public void setIsVisible(String isVisible) {
		this.isVisible = isVisible;
	}

	@Column(name = "protocol", length = 65535)
	public String getProtocol() {
		return this.protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "measurementParameter")
	public Set<MeasurementValue> getMeasurementValues() {
		return this.measurementValues;
	}

	public void setMeasurementValues(Set<MeasurementValue> measurementValues) {
		this.measurementValues = measurementValues;
	}

}
