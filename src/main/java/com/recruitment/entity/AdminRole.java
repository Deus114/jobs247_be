package com.recruitment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admin_roles")
public class AdminRole {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(length = 255)
	private String description;

	@Column(nullable = false)
	private boolean fullAccess = false;

	@Column(nullable = false)
	private boolean active = true;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "admin_role_permission",
			joinColumns = @JoinColumn(name = "role_id"),
			inverseJoinColumns = @JoinColumn(name = "permission_id"))
	private List<AdminPermission> permissions = new ArrayList<>();

	private Instant createdAt;

	private Instant updatedAt;

	@PrePersist
	protected void onCreate() {
		createdAt = Instant.now();
		updatedAt = Instant.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public List<AdminPermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<AdminPermission> permissions) {
		this.permissions = permissions;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
