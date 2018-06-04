package org.accretegb.modules.hibernate;
// Generated Jan 11, 2016 10:50:33 PM by Hibernate Tools 4.3.1.Final

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

/**
 * Taxonomy generated by hbm2java
 */
@Entity
@Table(name = "taxonomy", catalog = "agbv2")
public class Taxonomy implements java.io.Serializable {

	private Integer taxonomyId;
	private String genus;
	private String species;
	private String subspecies;
	private String subtaxa;
	private String race;
	private String population;
	private String commonName;
	private String gto;
	private Set<Passport> passports = new HashSet(0);

	public Taxonomy() {
	}

	public Taxonomy(String genus, String species, String subspecies, String subtaxa, String race, String population,
			String commonName, String gto, Set<Passport> passports) {
		this.genus = genus;
		this.species = species;
		this.subspecies = subspecies;
		this.subtaxa = subtaxa;
		this.race = race;
		this.population = population;
		this.commonName = commonName;
		this.gto = gto;
		this.passports = passports;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "taxonomy_id", unique = true, nullable = false)
	public Integer getTaxonomyId() {
		return this.taxonomyId;
	}

	public void setTaxonomyId(Integer taxonomyId) {
		this.taxonomyId = taxonomyId;
	}

	@Column(name = "genus")
	public String getGenus() {
		return this.genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	@Column(name = "species",nullable = false)
	public String getSpecies() {
		return this.species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	@Column(name = "subspecies")
	public String getSubspecies() {
		return this.subspecies;
	}

	public void setSubspecies(String subspecies) {
		this.subspecies = subspecies;
	}

	@Column(name = "subtaxa")
	public String getSubtaxa() {
		return this.subtaxa;
	}

	public void setSubtaxa(String subtaxa) {
		this.subtaxa = subtaxa;
	}

	@Column(name = "race")
	public String getRace() {
		return this.race;
	}

	public void setRace(String race) {
		this.race = race;
	}

	@Column(name = "population")
	public String getPopulation() {
		return this.population;
	}

	public void setPopulation(String population) {
		this.population = population;
	}

	@Column(name = "common_name")
	public String getCommonName() {
		return this.commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	@Column(name = "gto")
	public String getGto() {
		return this.gto;
	}

	public void setGto(String gto) {
		this.gto = gto;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "taxonomy")
	public Set<Passport> getPassports() {
		return this.passports;
	}

	public void setPassports(Set<Passport> passports) {
		this.passports = passports;
	}

}