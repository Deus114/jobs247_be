package com.recruitment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admin_roles")
public class AdminRole extends BaseEntity {

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(length = 255)
	private String description;

	@Column(nullable = false)
	private boolean fullAccess = false;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "admin_role_permission",
			joinColumns = @JoinColumn(name = "role_id"),
			inverseJoinColumns = @JoinColumn(name = "permission_id"))
	private List<AdminPermission> permissions = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isFullAccess() {
		return fullAccess;
	}

	public void setFullAccess(boolean fullAccess) {
		this.fullAccess = fullAccess;
	}

	public List<AdminPermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<AdminPermission> permissions) {
		this.permissions = permissions;
	}
}
