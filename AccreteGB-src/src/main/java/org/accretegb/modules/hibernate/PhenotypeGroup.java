package org.accretegb.modules.hibernate;

// default package
// Generated Jun 15, 2015 2:18:48 PM by Hibernate Tools 3.4.0.CR1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * PhenotypeGroup generated by hbm2java
 */
@Entity
@Table(name = "phenotype_group")
public class PhenotypeGroup implements java.io.Serializable {

	private Integer phenotypeGroupId;
	private int projectId;
	private String phenotypeGroupName;
	private String exportTableJson;
	private String importTableJson;

	public PhenotypeGroup() {
	}

	public PhenotypeGroup(int projectId, String phenotypeGroupName,
			String exportTableJson, String importTableJson) {
		this.projectId = projectId;
		this.phenotypeGroupName = phenotypeGroupName;
		this.exportTableJson = exportTableJson;
		this.importTableJson = importTableJson;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "phenotype_group_id", unique = true, nullable = false)
	public Integer getPhenotypeGroupId() {
		return this.phenotypeGroupId;
	}

	public void setPhenotypeGroupId(Integer phenotypeGroupId) {
		this.phenotypeGroupId = phenotypeGroupId;
	}

	@Column(name = "project_id", nullable = false)
	public int getProjectId() {
		return this.projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	@Column(name = "phenotype_group_name", nullable = false, length = 45)
	public String getPhenotypeGroupName() {
		return this.phenotypeGroupName;
	}

	public void setPhenotypeGroupName(String phenotypeGroupName) {
		this.phenotypeGroupName = phenotypeGroupName;
	}

	@Column(name = "export_table_json", nullable = true)
	public String getExportTableJson() {
		return this.exportTableJson;
	}

	public void setExportTableJson(String exportTableJson) {
		this.exportTableJson = exportTableJson;
	}

	@Column(name = "import_table_json", nullable = true)
	public String getImportTableJson() {
		return this.importTableJson;
	}

	public void setImportTableJson(String importTableJson) {
		this.importTableJson = importTableJson;
	}

}