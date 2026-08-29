package com.recruitment.entity;

import com.recruitment.enums.PermissionTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "admin_permissions", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "path", "method" })
})
public class AdminPermission extends BaseEntity {

	@Column(nullable = false, length = 150)
	private String name;

	@Column(length = 255)
	private String description;

	@Column(nullable = false, length = 100)
	private String module;

	@Column(nullable = false, length = 255)
	private String path;

	@Column(nullable = false, length = 10)
	private String method;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PermissionTypeEnum type = PermissionTypeEnum.ACTION;

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

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public PermissionTypeEnum getType() {
		return type;
	}

	public void setType(PermissionTypeEnum type) {
		this.type = type;
	}
}
