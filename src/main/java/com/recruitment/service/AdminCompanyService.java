package com.recruitment.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.recruitment.dto.request.admin.company.AdminCompanyFilterRequest;
import com.recruitment.dto.request.admin.company.UpdateAdminCompanyRequest;
import com.recruitment.dto.response.admin.company.AdminCompanyResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.entity.Company;
import com.recruitment.entity.CompanyMember;
import com.recruitment.enums.CompanyStatusEnum;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.AdminCompanyMapper;
import com.recruitment.repository.CompanyMemberRepository;
import com.recruitment.repository.CompanyRepository;
import com.recruitment.specification.AdminCompanySpecification;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.ApprovalStatusHelper;
import com.recruitment.util.helper.SoftDeletedPageQuery;

@Service
public class AdminCompanyService {

	private final CompanyRepository companyRepository;
	private final CompanyMemberRepository companyMemberRepository;
	private final AdminCompanyMapper adminCompanyMapper;
	private final SoftDeletedPageQuery softDeletedPageQuery;
	private final MessageService messageService;

	public AdminCompanyService(
			CompanyRepository companyRepository,
			CompanyMemberRepository companyMemberRepository,
			AdminCompanyMapper adminCompanyMapper,
			SoftDeletedPageQuery softDeletedPageQuery,
			MessageService messageService) {
		this.companyRepository = companyRepository;
		this.companyMemberRepository = companyMemberRepository;
		this.adminCompanyMapper = adminCompanyMapper;
		this.softDeletedPageQuery = softDeletedPageQuery;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<AdminCompanyResponse>>> getList(
			AdminCompanyFilterRequest filter,
			Pageable pageable) {

		validateStatusFilter(filter.status());

		Page<AdminCompanyResponse> resultPage = filter.isDeleted()
				? softDeletedPageQuery.find(
						Company.class,
						"companies c",
						"c",
						List.of("c.name", "c.email", "c.tax_code"),
						AccentHelper.normalizeForSearch(filter.keyword()),
						"AND (:status IS NULL OR c.status = :status) AND (:active IS NULL OR c.active = :active)",
						SoftDeletedPageQuery.params(
								"status", StringUtils.hasText(filter.status())
										? filter.status().trim().toUpperCase()
										: null,
								"active", filter.active()),
						"c.created_at DESC, c.id DESC",
						pageable)
						.map(adminCompanyMapper::toListResponse)
				: companyRepository.findAll(AdminCompanySpecification.build(filter), pageable)
						.map(adminCompanyMapper::toListResponse);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-company.list.success"),
				PaginatedResponse.fromPage(resultPage)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<AdminCompanyResponse>> getById(Long id) {
		Company company = findActiveById(id);
		List<CompanyMember> members = companyMemberRepository.findByCompanyIdOrderByCreatedAtAsc(id);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				adminCompanyMapper.toDetailResponse(company, members)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<AdminCompanyResponse>> update(
			Long id,
			UpdateAdminCompanyRequest request,
			String currentUsername) {

		if (request.active() == null && !StringUtils.hasText(request.status())) {
			throw new BusinessRuleException("error.admin-company.update.empty");
		}

		Company company = findActiveById(id);
		if (request.active() != null) {
			company.setActive(request.active());
		}
		if (StringUtils.hasText(request.status())) {
			var fields = ApprovalStatusHelper.apply(
					request.status(),
					request.rejectionReason(),
					"error.company.status.invalid");
			company.setStatus(fields.status());
			company.setRejectionReason(fields.rejectionReason());
			company.setRejectedAt(fields.rejectedAt());
			company.setApprovedAt(fields.approvedAt());
		}
		company.setUpdatedBy(currentUsername);

		Company saved = companyRepository.save(company);
		List<CompanyMember> members = companyMemberRepository.findByCompanyIdOrderByCreatedAtAsc(id);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-company.update.success"),
				adminCompanyMapper.toDetailResponse(saved, members)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> softDelete(Long id, String currentUsername) {
		Company company = findActiveById(id);
		company.setDeletedBy(currentUsername);
		companyRepository.save(company);
		companyRepository.delete(company);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-company.soft-delete.success"),
				null));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> restore(Long id, String currentUsername) {
		int restored = companyRepository.restoreById(id);
		if (restored == 0) {
			throw new ResourceNotFoundException("error.resource.company", "id", id);
		}

		Company company = findActiveById(id);
		company.setUpdatedBy(currentUsername);
		companyRepository.save(company);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("admin-company.restore.success"),
				null));
	}

	private Company findActiveById(Long id) {
		return companyRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.company", "id", id));
	}

	private void validateStatusFilter(String status) {
		if (!StringUtils.hasText(status)) {
			return;
		}
		CompanyStatusEnum.fromValue(status)
				.orElseThrow(() -> new BusinessRuleException("error.company.status.invalid", status));
	}
}
