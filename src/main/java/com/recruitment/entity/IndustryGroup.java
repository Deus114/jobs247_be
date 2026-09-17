package com.recruitment.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "industry_groups")
public class IndustryGroup extends BaseEntity {

	@Column(name = "name_vi", nullable = false, length = 255)
	private String nameVi;

	@Column(name = "name_en", nullable = false, length = 255)
	private String nameEn;

	@Column(name = "description_vi", columnDefinition = "TEXT")
	private String descriptionVi;

	@Column(name = "description_en", columnDefinition = "TEXT")
	private String descriptionEn;

	@Column(nullable = false)
	private int sortOrder = 0;

	@Column(length = 1000)
	private String image;

	@OneToMany(mappedBy = "industryGroup")
	@OrderBy("sortOrder ASC, id ASC")
	@BatchSize(size = 50)
	private List<Industry> industries = new ArrayList<>();

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

	public String getDescriptionVi() {
		return descriptionVi;
	}

	public void setDescriptionVi(String descriptionVi) {
		this.descriptionVi = descriptionVi;
	}

	public String getDescriptionEn() {
		return descriptionEn;
	}

	public void setDescriptionEn(String descriptionEn) {
		this.descriptionEn = descriptionEn;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public List<Industry> getIndustries() {
		return industries;
	}

	public void setIndustries(List<Industry> industries) {
		this.industries = industries != null ? industries : new ArrayList<>();
	}
}
