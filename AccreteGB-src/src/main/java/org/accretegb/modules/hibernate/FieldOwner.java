package org.accretegb.modules.hibernate;
// Generated Jan 11, 2016 10:35:13 PM by Hibernate Tools 4.3.1.Final

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * FieldOwner generated by hbm2java
 */
@Entity
@Table(name = "field_owner", catalog = "agbv2")
public class FieldOwner implements java.io.Serializable {

	private int fieldOwnerId;
	private Field field;
	private Users users;

	public FieldOwner() {
	}

	public FieldOwner(int fieldOwnerId) {
		this.fieldOwnerId = fieldOwnerId;
	}

	public FieldOwner(int fieldOwnerId, Field field, Users users) {
		this.fieldOwnerId = fieldOwnerId;
		this.field = field;
		this.users = users;
	}

	@Id

	@Column(name = "field_owner_id", unique = true, nullable = false)
	public int getFieldOwnerId() {
		return this.fieldOwnerId;
	}

	public void setFieldOwnerId(int fieldOwnerId) {
		this.fieldOwnerId = fieldOwnerId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "field_id",nullable = false)
	public Field getField() {
		return this.field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "users_id",nullable = false)
	public Users getUsers() {
		return this.users;
	}

	public void setUsers(Users users) {
		this.users = users;
	}

}