package com.recruitment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "industries")
public class Industry extends BaseEntity {

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

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "industry_group_id", nullable = false)
	private IndustryGroup industryGroup;

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

	public IndustryGroup getIndustryGroup() {
		return industryGroup;
	}

	public void setIndustryGroup(IndustryGroup industryGroup) {
		this.industryGroup = industryGroup;
	}
}
