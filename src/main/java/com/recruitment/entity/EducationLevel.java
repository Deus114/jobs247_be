package com.recruitment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "education_levels")
public class EducationLevel extends BaseEntity {

	@Column(name = "name_vi", nullable = false, length = 255)
	private String nameVi;

	@Column(name = "name_en", nullable = false, length = 255)
	private String nameEn;

	@Column(nullable = false)
	private int sortOrder = 0;

	public String getNameVi() {
		return nameVi;
	}

	public void setNameVi(String nameVi) {
		this.nameVi = nameVi;
	}

	public String getNameEn() {
		return nameEn;
	}

	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
}
