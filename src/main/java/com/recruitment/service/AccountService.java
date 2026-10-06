package com.recruitment.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.dto.request.admin.account.AccountFilterRequest;
import com.recruitment.dto.request.admin.account.UpdateAccountStatusRequest;
import com.recruitment.dto.response.admin.account.AccountResponse;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.entity.CompanyMember;
import com.recruitment.entity.User;
import com.recruitment.enums.UserTypeEnum;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.AccountMapper;
import com.recruitment.repository.CompanyMemberRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.specification.AccountSpecification;
import com.recruitment.util.helper.AccentHelper;
import com.recruitment.util.helper.SoftDeletedPageQuery;

@Service
public class AccountService {

	private final UserRepository userRepository;
	private final CompanyMemberRepository companyMemberRepository;
	private final AccountMapper accountMapper;
	private final SoftDeletedPageQuery softDeletedPageQuery;
	private final MessageService messageService;

	public AccountService(
			UserRepository userRepository,
			CompanyMemberRepository companyMemberRepository,
			AccountMapper accountMapper,
			SoftDeletedPageQuery softDeletedPageQuery,
			MessageService messageService) {
		this.userRepository = userRepository;
		this.companyMemberRepository = companyMemberRepository;
		this.accountMapper = accountMapper;
		this.softDeletedPageQuery = softDeletedPageQuery;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<AccountResponse>>> getList(
			AccountFilterRequest filter,
			Pageable pageable) {

		Page<User> usersPage = filter.isDeleted()
				? softDeletedPageQuery.find(
						User.class,
						"users u",
						"u",
						List.of("u.name", "u.email"),
						AccentHelper.normalizeForSearch(filter.keyword()),
						"AND (:type IS NULL OR u.type = :type) AND (:active IS NULL OR u.active = :active)",
						SoftDeletedPageQuery.params(
								"type", filter.type() == null ? null : filter.type().name(),
								"active", filter.active()),
						"u.created_at DESC, u.id DESC",
						pageable)
				: userRepository.findAll(AccountSpecification.build(filter), pageable);

		Map<Long, List<CompanyMember>> membershipsByUserId = loadMembershipsByUserId(usersPage.getContent());
		Page<AccountResponse> resultPage = usersPage.map(user -> accountMapper.toResponse(
				user,
				membershipsByUserId.getOrDefault(user.getId(), List.of())));

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("account.list.success"),
				PaginatedResponse.fromPage(resultPage)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<AccountResponse>> getById(Long id) {
		User user = findActiveById(id);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				toAccountResponse(user)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<AccountResponse>> updateStatus(
			Long id,
			UpdateAccountStatusRequest request,
			String currentUsername) {

		User user = findActiveById(id);
		user.setActive(request.active());
		user.setUpdatedBy(currentUsername);

		User saved = userRepository.save(user);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("account.update.success"),
				toAccountResponse(saved)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> softDelete(Long id, String currentUsername) {
		User user = findActiveById(id);
		user.setDeletedBy(currentUsername);
		userRepository.save(user);
		userRepository.delete(user);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("account.soft-delete.success"),
				null));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> restore(Long id, String currentUsername) {
		int restored = userRepository.restoreById(id);
		if (restored == 0) {
			throw new ResourceNotFoundException("error.resource.account", "id", id);
		}

		User user = findActiveById(id);
		user.setUpdatedBy(currentUsername);
		userRepository.save(user);

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("account.restore.success"),
				null));
	}

	private AccountResponse toAccountResponse(User user) {
		List<CompanyMember> memberships = UserTypeEnum.EMPLOYER.name().equals(user.getType())
				? companyMemberRepository.findByUserIdOrderByCreatedAtAsc(user.getId())
				: List.of();
		return accountMapper.toResponse(user, memberships);
	}

	private Map<Long, List<CompanyMember>> loadMembershipsByUserId(List<User> users) {
		List<Long> employerIds = users.stream()
				.filter(user -> UserTypeEnum.EMPLOYER.name().equals(user.getType()))
				.map(user -> user.getId())
				.toList();
		if (employerIds.isEmpty()) {
			return Collections.emptyMap();
		}
		return companyMemberRepository.findByUserIdInOrderByCreatedAtAsc(employerIds).stream()
				.collect(Collectors.groupingBy(member -> member.getUser().getId()));
	}

	private User findActiveById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.account", "id", id));
	}
}
