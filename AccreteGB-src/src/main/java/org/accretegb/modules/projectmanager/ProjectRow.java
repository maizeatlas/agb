package org.accretegb.modules.projectmanager;

import java.util.Date;
public class ProjectRow {
	
	private String name;
	private String owner;
	private String token;
	private Date creationDate;
	private Date modificationDate;
	private Date tokenExpirationDate;
	
	public ProjectRow(String name, String owner, String token, Date tokenExpirationDate, Date creationDate,
			Date modificationDate) {
		
		this.name = name;
		this.owner = owner;
		this.token = token;
		this.tokenExpirationDate = tokenExpirationDate;
		this.creationDate = creationDate;
		this.modificationDate = modificationDate;
	}
	

	public Object[] toObjects(){
		Object[] row = new Object[6 + 1];
		row[0] = new Boolean(false);
		row[1] = getName();
		row[2] = getOwner();
		row[3] = getToken();
		row[4] = getTokenExpirationDate();	
		row[5] = getCreationDate();
		row[6] = getModificationDate();
		
		return row;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public Date getModificationDate() {
		return modificationDate;
	}
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}
	public Date getTokenExpirationDate() {
		return tokenExpirationDate;
	}

	public void setTokenExpirationDate(Date tokenExpirationDate) {
		this.tokenExpirationDate = tokenExpirationDate;
	}

	
}
