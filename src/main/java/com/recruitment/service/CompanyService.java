package com.recruitment.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.recruitment.dto.request.user.company.CompanyJoinRequestFilterRequest;
import com.recruitment.dto.request.user.company.CreateCompanyJoinRequest;
import com.recruitment.dto.request.user.company.CreateEmployerCompanyRequest;
import com.recruitment.dto.request.user.company.EmployerCompanyFilterRequest;
import com.recruitment.dto.request.user.company.ReviewCompanyJoinRequest;
import com.recruitment.dto.request.user.company.UpdateEmployerCompanyRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.UploadedFileResponse;
import com.recruitment.dto.response.user.company.EmployerCompanyJoinRequestResponse;
import com.recruitment.dto.response.user.company.EmployerCompanyResponse;
import com.recruitment.entity.Company;
import com.recruitment.entity.CompanyJoinRequest;
import com.recruitment.entity.CompanyMember;
import com.recruitment.entity.Industry;
import com.recruitment.entity.Province;
import com.recruitment.entity.User;
import com.recruitment.enums.CompanyJoinRequestStatusEnum;
import com.recruitment.enums.CompanyMemberRoleEnum;
import com.recruitment.enums.CompanySizeEnum;
import com.recruitment.enums.CompanyStatusEnum;
import com.recruitment.enums.UploadFolderType;
import com.recruitment.enums.UserTypeEnum;
import com.recruitment.exception.AccountInactiveException;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.DuplicateResourceException;
import com.recruitment.exception.InvalidTokenException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.CompanyMapper;
import com.recruitment.notification.AdminNotificationService;
import com.recruitment.notification.UserNotificationService;
import com.recruitment.repository.CompanyJoinRequestRepository;
import com.recruitment.repository.CompanyMemberRepository;
import com.recruitment.repository.CompanyRepository;
import com.recruitment.repository.IndustryRepository;
import com.recruitment.repository.ProvinceRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.security.JwtTokenProvider;
import com.recruitment.specification.CompanySpecification;
import com.recruitment.util.constant.Constants;
import com.recruitment.util.helper.FileHelper;
import com.recruitment.util.helper.SlugHelper;

@Service
public class CompanyService {

	private final CompanyRepository companyRepository;
	private final CompanyMemberRepository companyMemberRepository;
	private final CompanyJoinRequestRepository companyJoinRequestRepository;
	private final UserRepository userRepository;
	private final IndustryRepository industryRepository;
	private final ProvinceRepository provinceRepository;
	private final FileStorageService fileStorageService;
	private final CompanyMapper companyMapper;
	private final AdminNotificationService adminNotificationService;
	private final UserNotificationService userNotificationService;
	private final MessageService messageService;

	public CompanyService(
			CompanyRepository companyRepository,
			CompanyMemberRepository companyMemberRepository,
			CompanyJoinRequestRepository companyJoinRequestRepository,
			UserRepository userRepository,
			IndustryRepository industryRepository,
			ProvinceRepository provinceRepository,
			FileStorageService fileStorageService,
			CompanyMapper companyMapper,
			AdminNotificationService adminNotificationService,
			UserNotificationService userNotificationService,
			MessageService messageService) {
		this.companyRepository = companyRepository;
		this.companyMemberRepository = companyMemberRepository;
		this.companyJoinRequestRepository = companyJoinRequestRepository;
		this.userRepository = userRepository;
		this.industryRepository = industryRepository;
		this.provinceRepository = provinceRepository;
		this.fileStorageService = fileStorageService;
		this.companyMapper = companyMapper;
		this.adminNotificationService = adminNotificationService;
		this.userNotificationService = userNotificationService;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<EmployerCompanyResponse>>> getList(
			EmployerCompanyFilterRequest filter,
			Pageable pageable,
			Jwt jwt) {

		User employer = requireEmployer(jwt);
		validateStatusFilter(filter.status());

		Page<Company> companies = companyRepository.findAll(
				CompanySpecification.build(filter, employer.getId()),
				pageable);
		Set<Long> appliedCompanyIds = appliedCompanyIds(employer.getId(), companies.getContent());
		Page<EmployerCompanyResponse> resultPage = companies.map(
				company -> companyMapper.toResponse(company, appliedCompanyIds.contains(company.getId())));

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("company.list.success"),
				PaginatedResponse.fromPage(resultPage)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<EmployerCompanyResponse>> getById(Long id, Jwt jwt) {
		User employer = requireEmployer(jwt);
		Company company = findActiveById(id);
		boolean applied = companyJoinRequestRepository.existsByCompanyIdAndUserIdAndStatus(
				company.getId(),
				employer.getId(),
				CompanyJoinRequestStatusEnum.PENDING.name());
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				companyMapper.toResponse(company, applied)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<EmployerCompanyResponse>> create(CreateEmployerCompanyRequest request, Jwt jwt) {
		User employer = requireEmployer(jwt);
		ensureMembershipSlotAvailable(employer.getId());
		String taxCode = request.taxCode().trim();
		if (companyRepository.countByTaxCodeIncludingDeleted(taxCode) > 0) {
			throw new DuplicateResourceException("error.company.tax-code.duplicate", taxCode);
		}

		CompanySizeEnum size = CompanySizeEnum.fromValue(request.size())
				.orElseThrow(() -> new BusinessRuleException("error.company.size.invalid", request.size()));
		Province province = requireActiveProvince(request.provinceId());
		List<Industry> industries = requireActiveIndustries(request.industryIds());

		Company company = new Company();
		company.setName(request.name().trim());
		company.setSlug(SlugHelper.from(request.name()));
		company.setSize(size.name());
		company.setProvince(province);
		company.setAddress(request.address().trim());
		company.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
		company.setPhone(request.phone().trim());
		company.setWebsite(trimToNull(request.website()));
		company.setTaxCode(taxCode);
		company.setDescription(request.description().trim());
		company.setStatus(CompanyStatusEnum.PENDING.name());
		company.setIndustries(industries);
		company.setCreatedBy(employer.getEmail());
		company.setUpdatedBy(employer.getEmail());
		applyImageField(
				request.logoFile(),
				request.logo(),
				company::getLogo,
				company::setLogo,
				UploadFolderType.COMPANY_LOGO);
		applyImageField(
				request.backgroundImageFile(),
				request.backgroundImage(),
				company::getBackgroundImage,
				company::setBackgroundImage,
				UploadFolderType.COMPANY_BACKGROUND);

		Company saved = companyRepository.save(company);
		saveOwner(saved, employer);
		adminNotificationService.notifyCompanyPending(saved, employer.getEmail());

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
				HttpStatus.CREATED.value(),
				messageService.get("company.create.success"),
				companyMapper.toResponse(saved, false)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<EmployerCompanyResponse>> update(
			Long id,
			UpdateEmployerCompanyRequest request,
			Jwt jwt) {

		User employer = requireEmployer(jwt);
		Company company = findActiveById(id);
		requireOwner(company.getId(), employer.getId());

		if (StringUtils.hasText(request.name())) {
			company.setName(request.name().trim());
			company.setSlug(SlugHelper.from(request.name()));
		}
		if (request.industryIds() != null) {
			if (request.industryIds().isEmpty()) {
				throw new BusinessRuleException("error.company.industries.required");
			}
			company.setIndustries(requireActiveIndustries(request.industryIds()));
		}
		if (StringUtils.hasText(request.size())) {
			CompanySizeEnum size = CompanySizeEnum.fromValue(request.size())
					.orElseThrow(() -> new BusinessRuleException("error.company.size.invalid", request.size()));
			company.setSize(size.name());
		}
		if (request.provinceId() != null) {
			company.setProvince(requireActiveProvince(request.provinceId()));
		}
		if (StringUtils.hasText(request.address())) {
			company.setAddress(request.address().trim());
		}
		if (StringUtils.hasText(request.email())) {
			company.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
		}
		if (StringUtils.hasText(request.phone())) {
			company.setPhone(request.phone().trim());
		}
		if (request.website() != null) {
			company.setWebsite(trimToNull(request.website()));
		}
		if (StringUtils.hasText(request.taxCode())) {
			String taxCode = request.taxCode().trim();
			if (companyRepository.countByTaxCodeIncludingDeletedAndIdNot(taxCode, id) > 0) {
				throw new DuplicateResourceException("error.company.tax-code.duplicate", taxCode);
			}
			company.setTaxCode(taxCode);
		}
		if (StringUtils.hasText(request.description())) {
			company.setDescription(request.description().trim());
		}

		company.setUpdatedBy(employer.getEmail());
		applyImageField(
				request.logoFile(),
				request.logo(),
				company::getLogo,
				company::setLogo,
				UploadFolderType.COMPANY_LOGO);
		applyImageField(
				request.backgroundImageFile(),
				request.backgroundImage(),
				company::getBackgroundImage,
				company::setBackgroundImage,
				UploadFolderType.COMPANY_BACKGROUND);

		Company saved = companyRepository.save(company);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("company.update.success"),
				companyMapper.toResponse(saved, false)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<EmployerCompanyJoinRequestResponse>> createJoinRequest(
			Long companyId,
			CreateCompanyJoinRequest request,
			Jwt jwt) {

		User employer = requireEmployer(jwt);
		Company company = findActiveById(companyId);
		if (!CompanyStatusEnum.APPROVED.name().equals(company.getStatus())) {
			throw new BusinessRuleException("error.company.join.not-approved");
		}
		if (companyMemberRepository.existsByCompanyIdAndUserId(companyId, employer.getId())) {
			throw new BusinessRuleException("error.company.join.already-member");
		}
		if (companyJoinRequestRepository.existsByCompanyIdAndUserIdAndStatus(
				companyId,
				employer.getId(),
				CompanyJoinRequestStatusEnum.PENDING.name())) {
			throw new BusinessRuleException("error.company.join.pending-exists");
		}
		ensureMembershipSlotAvailable(employer.getId());

		CompanyJoinRequest joinRequest = new CompanyJoinRequest();
		joinRequest.setCompany(company);
		joinRequest.setUser(employer);
		joinRequest.setStatus(CompanyJoinRequestStatusEnum.PENDING.name());
		joinRequest.setMessage(trimToNull(request.message()));
		joinRequest.setCreatedBy(employer.getEmail());
		joinRequest.setUpdatedBy(employer.getEmail());

		CompanyJoinRequest saved = companyJoinRequestRepository.save(joinRequest);
		userNotificationService.notifyJoinRequestCreated(saved, employer.getEmail());
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
				HttpStatus.CREATED.value(),
				messageService.get("company.join-request.create.success"),
				companyMapper.toJoinRequestResponse(saved)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<EmployerCompanyJoinRequestResponse>>> getJoinRequests(
			CompanyJoinRequestFilterRequest filter,
			Pageable pageable,
			Jwt jwt) {

		User employer = requireEmployer(jwt);
		List<Long> companyIds = resolveOwnedCompanyIds(employer.getId(), filter.companyId());
		if (companyIds.isEmpty()) {
			return ResponseEntity.ok(ApiResponse.success(
					messageService.get("company.join-request.list.success"),
					PaginatedResponse.fromPage(Page.empty(pageable))));
		}

		String status = normalizeJoinStatusFilter(filter.status());
		Page<EmployerCompanyJoinRequestResponse> resultPage = (status == null
				? companyJoinRequestRepository.findByCompanyIdIn(companyIds, pageable)
				: companyJoinRequestRepository.findByCompanyIdInAndStatus(companyIds, status, pageable))
				.map(companyMapper::toJoinRequestResponse);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("company.join-request.list.success"),
				PaginatedResponse.fromPage(resultPage)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<EmployerCompanyJoinRequestResponse>>> getMyJoinRequests(
			CompanyJoinRequestFilterRequest filter,
			Pageable pageable,
			Jwt jwt) {

		User employer = requireEmployer(jwt);
		String status = normalizeJoinStatusFilter(filter.status());
		Page<EmployerCompanyJoinRequestResponse> resultPage = (status == null
				? companyJoinRequestRepository.findByUserId(employer.getId(), pageable)
				: companyJoinRequestRepository.findByUserIdAndStatus(employer.getId(), status, pageable))
				.map(companyMapper::toJoinRequestResponse);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("company.join-request.my-list.success"),
				PaginatedResponse.fromPage(resultPage)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<EmployerCompanyJoinRequestResponse>> reviewJoinRequest(
			Long requestId,
			ReviewCompanyJoinRequest request,
			Jwt jwt) {

		User employer = requireEmployer(jwt);
		CompanyJoinRequest joinRequest = companyJoinRequestRepository.findById(requestId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"error.resource.company-join-request", "id", requestId));
		Long companyId = joinRequest.getCompany().getId();
		requireOwner(companyId, employer.getId());

		CompanyJoinRequestStatusEnum reviewStatus = CompanyJoinRequestStatusEnum.fromValue(request.status())
				.filter(status -> status == CompanyJoinRequestStatusEnum.APPROVED
						|| status == CompanyJoinRequestStatusEnum.REJECTED)
				.orElseThrow(() -> new BusinessRuleException("error.company.join.status.invalid", request.status()));

		if (!CompanyJoinRequestStatusEnum.PENDING.name().equals(joinRequest.getStatus())) {
			throw new BusinessRuleException("error.company.join.not-pending");
		}

		User applicant = joinRequest.getUser();
		if (reviewStatus == CompanyJoinRequestStatusEnum.APPROVED) {
			if (companyMemberRepository.existsByCompanyIdAndUserId(companyId, applicant.getId())) {
				throw new BusinessRuleException("error.company.join.already-member");
			}
			if (companyMemberRepository.countByUserId(applicant.getId()) >= Constants.EMPLOYER_MAX_COMPANIES) {
				throw new BusinessRuleException(
						"error.company.membership.limit",
						Constants.EMPLOYER_MAX_COMPANIES);
			}
			CompanyMember admin = new CompanyMember();
			admin.setCompany(joinRequest.getCompany());
			admin.setUser(applicant);
			admin.setRole(CompanyMemberRoleEnum.ADMIN.name());
			admin.setCreatedBy(employer.getEmail());
			admin.setUpdatedBy(employer.getEmail());
			companyMemberRepository.save(admin);
		}

		joinRequest.setStatus(reviewStatus.name());
		joinRequest.setUpdatedBy(employer.getEmail());
		CompanyJoinRequest saved = companyJoinRequestRepository.save(joinRequest);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("company.join-request.review.success"),
				companyMapper.toJoinRequestResponse(saved)));
	}

	private List<Long> ownedCompanyIds(Long userId) {
		return companyMemberRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
				.filter(member -> CompanyMemberRoleEnum.OWNER.name().equals(member.getRole()))
				.map(member -> member.getCompany().getId())
				.toList();
	}

	private List<Long> resolveOwnedCompanyIds(Long userId, Long companyIdFilter) {
		List<Long> owned = ownedCompanyIds(userId);
		if (companyIdFilter == null) {
			return owned;
		}
		requireOwner(companyIdFilter, userId);
		return List.of(companyIdFilter);
	}

	private Set<Long> appliedCompanyIds(Long userId, List<Company> companies) {
		if (companies.isEmpty()) {
			return Collections.emptySet();
		}
		ArrayList<Long> companyIds = new ArrayList<>(companies.size());
		for (Company company : companies) {
			companyIds.add(company.getId());
		}
		return new HashSet<>(companyJoinRequestRepository.findCompanyIdsByUserIdAndStatusAndCompanyIdIn(
				userId,
				CompanyJoinRequestStatusEnum.PENDING.name(),
				companyIds));
	}

	private User requireEmployer(Jwt jwt) {
		if (jwt == null
				|| !JwtTokenProvider.USER_ACCOUNT.equals(jwt.getClaimAsString("account"))
				|| !JwtTokenProvider.USER_ACCESS_TYPE.equals(jwt.getClaimAsString("type"))) {
			throw new InvalidTokenException("error.unauthorized.invalid-token");
		}
		User user = userRepository.findByEmail(jwt.getSubject())
				.orElseThrow(() -> new InvalidTokenException("error.unauthorized.invalid-token"));
		if (!user.isActive()) {
			throw new AccountInactiveException("error.auth.account.inactive");
		}
		if (!UserTypeEnum.EMPLOYER.name().equals(user.getType())) {
			throw new BusinessRuleException("error.company.employer-only");
		}
		return user;
	}

	private void requireOwner(Long companyId, Long userId) {
		CompanyMember member = companyMemberRepository.findByCompanyIdAndUserId(companyId, userId)
				.orElseThrow(() -> new BusinessRuleException("error.company.owner-only"));
		if (!CompanyMemberRoleEnum.OWNER.name().equals(member.getRole())) {
			throw new BusinessRuleException("error.company.owner-only");
		}
	}

	private void ensureMembershipSlotAvailable(Long userId) {
		long occupied = companyMemberRepository.countByUserId(userId)
				+ companyJoinRequestRepository.countByUserIdAndStatus(
						userId,
						CompanyJoinRequestStatusEnum.PENDING.name());
		if (occupied >= Constants.EMPLOYER_MAX_COMPANIES) {
			throw new BusinessRuleException(
					"error.company.membership.limit",
					Constants.EMPLOYER_MAX_COMPANIES);
		}
	}

	private String normalizeJoinStatusFilter(String status) {
		if (!StringUtils.hasText(status)) {
			return null;
		}
		return CompanyJoinRequestStatusEnum.fromValue(status)
				.map(value -> value.name())
				.orElseThrow(() -> new BusinessRuleException("error.company.join.filter-status.invalid", status));
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

	private Province requireActiveProvince(Long provinceId) {
		Province province = provinceRepository.findById(provinceId)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.province", "id", provinceId));
		if (!province.isActive()) {
			throw new BusinessRuleException("error.company.province.inactive");
		}
		return province;
	}

	private List<Industry> requireActiveIndustries(List<Long> industryIds) {
		List<Long> distinctIds = industryIds.stream().distinct().toList();
		Map<Long, Industry> byId = new LinkedHashMap<>();
		for (Industry industry : industryRepository.findAllById(distinctIds)) {
			byId.put(industry.getId(), industry);
		}
		List<Industry> ordered = new ArrayList<>();
		for (Long id : distinctIds) {
			Industry industry = byId.get(id);
			if (industry == null) {
				throw new ResourceNotFoundException("error.resource.industry", "id", id);
			}
			if (!industry.isActive()) {
				throw new BusinessRuleException("error.company.industry.inactive", id);
			}
			ordered.add(industry);
		}
		return ordered;
	}

	private void saveOwner(Company company, User employer) {
		CompanyMember owner = new CompanyMember();
		owner.setCompany(company);
		owner.setUser(employer);
		owner.setRole(CompanyMemberRoleEnum.OWNER.name());
		owner.setCreatedBy(employer.getEmail());
		owner.setUpdatedBy(employer.getEmail());
		companyMemberRepository.save(owner);
	}

	private void applyImageField(
			MultipartFile file,
			String link,
			Supplier<String> currentGetter,
			Consumer<String> setter,
			UploadFolderType folderType) {

		boolean hasFile = FileHelper.hasFile(file);
		boolean hasLink = StringUtils.hasText(link);

		if (hasFile && hasLink) {
			throw new BusinessRuleException("error.business-config.image.file-and-link");
		}

		if (hasFile) {
			String oldUrl = currentGetter.get();
			UploadedFileResponse uploaded = fileStorageService.uploadImage(file, folderType);
			setter.accept(uploaded.url());
			if (oldUrl != null && !oldUrl.equals(uploaded.url())) {
				fileStorageService.deleteByPublicUrl(oldUrl);
			}
			return;
		}

		if (link != null) {
			setter.accept(trimToNull(link));
		}
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
