package org.accretegb.modules.hibernate;

// default package
// Generated May 27, 2015 9:54:40 PM by Hibernate Tools 3.4.0.CR1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * StockSelectionGroup generated by hbm2java
 */
@Entity
@Table(name = "stock_selection_group")
public class StockSelectionGroup implements java.io.Serializable {

	private Integer stockSelectionGroupId;
	private int projectId;
	private String stockSelectionGroupName;
	private String cartJson;

	public StockSelectionGroup() {
	}

	public StockSelectionGroup(int projectId, String stockSelectionGroupName) {
		this.projectId = projectId;
		this.stockSelectionGroupName = stockSelectionGroupName;
	}

	public StockSelectionGroup(int projectId, String stockSelectionGroupName,
			String cartJson) {
		this.projectId = projectId;
		this.stockSelectionGroupName = stockSelectionGroupName;
		this.cartJson = cartJson;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "stock_selection_group_id", unique = true, nullable = false)
	public Integer getStockSelectionGroupId() {
		return this.stockSelectionGroupId;
	}

	public void setStockSelectionGroupId(Integer stockSelectionGroupId) {
		this.stockSelectionGroupId = stockSelectionGroupId;
	}

	@Column(name = "project_id", nullable = false)
	public int getProjectId() {
		return this.projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	@Column(name = "stock_selection_group_name", nullable = false, length = 45)
	public String getStockSelectionGroupName() {
		return this.stockSelectionGroupName;
	}

	public void setStockSelectionGroupName(String stockSelectionGroupName) {
		this.stockSelectionGroupName = stockSelectionGroupName;
	}

	@Column(name = "cart_json")
	public String getCartJson() {
		return this.cartJson;
	}

	public void setCartJson(String cartJson) {
		this.cartJson = cartJson;
	}

}
