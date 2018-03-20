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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 * MateMethod generated by hbm2java
 */
@Entity
@Table(name = "mate_method", catalog = "agbv2" )
public class MateMethod implements java.io.Serializable {

	private Integer mateMethodId;
	private String mateMethodName;
	private String mateMethodDesc;
	private String mateMethodUser;
	private Date dateDefined;
	private String mateMethodComments;
	private Set<MateMethodConnect> mateMethodConnects = new HashSet(0);

	public MateMethod() {
	}

	public MateMethod(String mateMethodName, String mateMethodDesc, String mateMethodUser,
			Date dateDefined, String mateMethodComments, Set<MateMethodConnect> mateMethodConnects) {
		this.mateMethodName = mateMethodName;
		this.mateMethodDesc = mateMethodDesc;
		this.mateMethodUser = mateMethodUser;
		this.dateDefined = dateDefined;
		this.mateMethodComments = mateMethodComments;
		this.mateMethodConnects = mateMethodConnects;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "mate_method_id", unique = true, nullable = false)
	public Integer getMateMethodId() {
		return this.mateMethodId;
	}

	public void setMateMethodId(Integer mateMethodId) {
		this.mateMethodId = mateMethodId;
	}


	@Column(name = "mate_method_name",nullable = false)
	public String getMateMethodName() {
		return this.mateMethodName;
	}

	public void setMateMethodName(String mateMethodName) {
		this.mateMethodName = mateMethodName;
	}

	@Column(name = "mate_method_desc", length = 65535)
	public String getMateMethodDesc() {
		return this.mateMethodDesc;
	}

	public void setMateMethodDesc(String mateMethodDesc) {
		this.mateMethodDesc = mateMethodDesc;
	}

	@Column(name = "mate_method_user")
	public String getMateMethodUser() {
		return this.mateMethodUser;
	}

	public void setMateMethodUser(String mateMethodUser) {
		this.mateMethodUser = mateMethodUser;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_defined", length = 0)
	public Date getDateDefined() {
		return this.dateDefined;
	}

	public void setDateDefined(Date dateDefined) {
		this.dateDefined = dateDefined;
	}

	@Column(name = "mate_method_comments", length = 65535)
	public String getMateMethodComments() {
		return this.mateMethodComments;
	}

	public void setMateMethodComments(String mateMethodComments) {
		this.mateMethodComments = mateMethodComments;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "mateMethod")
	public Set<MateMethodConnect> getMateMethodConnects() {
		return this.mateMethodConnects;
	}

	public void setMateMethodConnects(Set<MateMethodConnect> mateMethodConnects) {
		this.mateMethodConnects = mateMethodConnects;
	}

}
