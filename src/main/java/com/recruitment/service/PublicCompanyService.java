package com.recruitment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.dto.request.common.PublicCompanyFilterRequest;
import com.recruitment.dto.request.common.PublicJobFilterRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.PublicCompanyResponse;
import com.recruitment.dto.response.common.PublicJobResponse;
import com.recruitment.entity.Company;
import com.recruitment.enums.CompanyStatusEnum;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.PublicCompanyMapper;
import com.recruitment.repository.CompanyRepository;
import com.recruitment.specification.PublicCatalogSpecification;

@Service
public class PublicCompanyService {

	private final CompanyRepository companyRepository;
	private final PublicCompanyMapper publicCompanyMapper;
	private final PublicJobService publicJobService;
	private final MessageService messageService;

	public PublicCompanyService(
			CompanyRepository companyRepository,
			PublicCompanyMapper publicCompanyMapper,
			PublicJobService publicJobService,
			MessageService messageService) {
		this.companyRepository = companyRepository;
		this.publicCompanyMapper = publicCompanyMapper;
		this.publicJobService = publicJobService;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<PublicCompanyResponse>>> getCompanies(
			PublicCompanyFilterRequest filter,
			Pageable pageable) {
		Page<PublicCompanyResponse> page = companyRepository
				.findAll(PublicCatalogSpecification.buildCompanies(filter), pageable)
				.map(publicCompanyMapper::toResponse);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("company.list.success"),
				PaginatedResponse.fromPage(page)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PublicCompanyResponse>> getCompany(Long id) {
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				publicCompanyMapper.toResponse(requirePublicCompany(id))));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<PublicJobResponse>>> getCompanyJobs(
			Long companyId,
			Pageable pageable,
			Long viewerUserId) {
		requirePublicCompany(companyId);
		PublicJobFilterRequest filter = new PublicJobFilterRequest(
				null, companyId, null, null, null, null, null, null, null, null, null);
		return publicJobService.getJobs(filter, pageable, viewerUserId);
	}

	private Company requirePublicCompany(Long id) {
		Company company = companyRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.company", "id", id));
		if (!CompanyStatusEnum.APPROVED.name().equals(company.getStatus()) || !company.isActive()) {
			throw new ResourceNotFoundException("error.resource.company", "id", id);
		}
		return company;
	}
}
