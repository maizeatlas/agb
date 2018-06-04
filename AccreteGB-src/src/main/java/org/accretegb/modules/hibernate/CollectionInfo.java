package org.accretegb.modules.hibernate;
// Generated Jan 11, 2016 10:15:02 PM by Hibernate Tools 4.3.1.Final

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

/**
 * CollectionInfo generated by hbm2java
 */
@Entity
@Table(name = "collection_info", catalog = "agbv2")
public class CollectionInfo implements java.io.Serializable {

	private Integer collectionInfoId;
	private Field field;
	private Source source;
	private String collectionIdentifier;
	private Date colDate;
	private Set<Passport> passports = new HashSet(0);

	public CollectionInfo() {
	}

	public CollectionInfo(Field field, Source source, String collectionIdentifier, Date colDate, Set<Passport> passports) {
		this.field = field;
		this.source = source;
		this.collectionIdentifier = collectionIdentifier;
		this.colDate = colDate;
		this.passports = passports;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "collection_info_id", unique = true, nullable = false)
	public Integer getCollectionInfoId() {
		return this.collectionInfoId;
	}

	public void setCollectionInfoId(Integer collectionInfoId) {
		this.collectionInfoId = collectionInfoId;
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
	@JoinColumn(name = "source_id")
	public Source getSource() {
		return this.source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	@Column(name = "collection_identifier",nullable = false)
	public String getCollectionIdentifier() {
		return this.collectionIdentifier;
	}

	public void setCollectionIdentifier(String collectionIdentifier) {
		this.collectionIdentifier = collectionIdentifier;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "col_date", length = 0)
	public Date getColDate() {
		return this.colDate;
	}

	public void setColDate(Date colDate) {
		this.colDate = colDate;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "collectionInfo")
	public Set<Passport> getPassports() {
		return this.passports;
	}

	public void setPassports(Set<Passport> passports) {
		this.passports = passports;
	}

}