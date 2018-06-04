package org.accretegb.modules.hibernate;
// Generated Jan 7, 2016 12:35:46 PM by Hibernate Tools 4.3.1.Final

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * DivLocation generated by hbm2java
 */
@Entity
@Table(name = "location", catalog = "agbv2" )
public class Location implements java.io.Serializable {

	private Integer locationId;
	private String LocationName;
	private String city;
	private String stateProvince;
	private String country;
	private String zipcode;
	private String locationComments;
	private Set<Field> fields = new HashSet(0);
	private Set<Source> Sources = new HashSet(0);
	private Set<ContainerLocation> containerLocations = new HashSet(0);

	public Location() {
	}

	public Location(String LocationName,String city,
			String stateProvince, String country, String zipcode, String locationComments, Set<Field> fields,
			Set<Source> Sources, Set<ContainerLocation> containerLocations) {
		this.LocationName = LocationName;
		this.city = city;
		this.stateProvince = stateProvince;
		this.country = country;
		this.zipcode = zipcode;
		this.locationComments = locationComments;
		this.fields = fields;
		this.Sources = Sources;
		this.containerLocations = containerLocations;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "location_id", unique = true, nullable = false)
	public Integer getLocationId() {
		return this.locationId;
	}

	public void setLocationId(Integer locationId) {
		this.locationId = locationId;
	}


	@Column(name = "location_name")
	public String getLocationName() {
		return this.LocationName;
	}

	public void setLocationName(String locationName) {
		this.LocationName = locationName;
	}


	@Column(name = "city")
	public String getCity() {
		return this.city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Column(name = "state_province")
	public String getStateProvince() {
		return this.stateProvince;
	}

	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}

	@Column(name = "country")
	public String getCountry() {
		return this.country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@Column(name = "zipcode", nullable = false)
	public String getZipcode() {
		return this.zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	@Column(name = "location_comments", length = 65535)
	public String getLocationComments() {
		return this.locationComments;
	}

	public void setLocationComments(String LocationComments) {
		this.locationComments = LocationComments;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "location")
	public Set<Field> getFields() {
		return this.fields;
	}

	public void setFields(Set<Field> fields) {
		this.fields = fields;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "location")
	public Set<Source> getSources() {
		return this.Sources;
	}

	public void setSources(Set<Source> Sources) {
		this.Sources = Sources;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "location")
	public Set<ContainerLocation> getContainerLocations() {
		return this.containerLocations;
	}

	public void setContainerLocations(Set<ContainerLocation> containerLocations) {
		this.containerLocations = containerLocations;
	}

}