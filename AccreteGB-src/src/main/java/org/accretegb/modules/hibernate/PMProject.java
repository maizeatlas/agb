package org.accretegb.modules.hibernate;

// default package
// Generated Jun 6, 2015 8:48:55 AM by Hibernate Tools 3.4.0.CR1

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 * Project generated by hbm2java
 */
@Entity
@Table(name = "project", uniqueConstraints = @UniqueConstraint(columnNames = "project_name"))
public class PMProject implements java.io.Serializable {

	private Integer projectId;
	private int userId;
	private String projectName;
	private Date lastModified;
	private Date dateCreated;

	public PMProject() {
	}

	public PMProject(int userId, String projectName,Date dateCreated, Date lastModified) {
		this.userId = userId;
		this.projectName = projectName;
		this.lastModified = lastModified;
		this.dateCreated = dateCreated;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "project_id", unique = true, nullable = false)
	public Integer getProjectId() {
		return this.projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	@Column(name = "user_id", nullable = false)
	public int getUserId() {
		return this.userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	@Column(name = "project_name", unique = true, nullable = false, length = 45)
	public String getProjectName() {
		return this.projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_modified", nullable = false, length = 19)
	public Date getLastModified() {
		return this.lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_created", nullable = false, length = 19)
	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

}
