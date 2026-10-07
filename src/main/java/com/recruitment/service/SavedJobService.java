package com.recruitment.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.user.application.SavedJobResponse;
import com.recruitment.entity.Job;
import com.recruitment.entity.SavedJob;
import com.recruitment.entity.User;
import com.recruitment.enums.JobStatusEnum;
import com.recruitment.enums.UserTypeEnum;
import com.recruitment.exception.AccountInactiveException;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.InvalidTokenException;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.JobApplicationMapper;
import com.recruitment.repository.JobApplicationRepository;
import com.recruitment.repository.JobRepository;
import com.recruitment.repository.SavedJobRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.security.JwtTokenProvider;
import com.recruitment.specification.SavedJobSpecification;

@Service
public class SavedJobService {

	private final SavedJobRepository savedJobRepository;
	private final JobRepository jobRepository;
	private final JobApplicationRepository jobApplicationRepository;
	private final UserRepository userRepository;
	private final JobApplicationMapper jobApplicationMapper;
	private final MessageService messageService;

	public SavedJobService(
			SavedJobRepository savedJobRepository,
			JobRepository jobRepository,
			JobApplicationRepository jobApplicationRepository,
			UserRepository userRepository,
			JobApplicationMapper jobApplicationMapper,
			MessageService messageService) {
		this.savedJobRepository = savedJobRepository;
		this.jobRepository = jobRepository;
		this.jobApplicationRepository = jobApplicationRepository;
		this.userRepository = userRepository;
		this.jobApplicationMapper = jobApplicationMapper;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<SavedJobResponse>>> getList(
			Pageable pageable,
			Jwt jwt) {
		User seeker = requireJobSeeker(jwt);
		Page<SavedJob> page = savedJobRepository.findAll(
				SavedJobSpecification.byUser(seeker.getId()),
				pageable);
		Set<Long> appliedJobIds = appliedJobIds(seeker.getId(), page.getContent());
		Map<Long, Long> applicationCounts = applicationCounts(page.getContent());
		Page<SavedJobResponse> result = page.map(saved -> jobApplicationMapper.toSavedJobResponse(
				saved,
				appliedJobIds.contains(saved.getJob().getId()),
				applicationCounts.getOrDefault(saved.getJob().getId(), 0L)));
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("saved-job.list.success"),
				PaginatedResponse.fromPage(result)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<SavedJobResponse>> save(Long jobId, Jwt jwt) {
		User seeker = requireJobSeeker(jwt);
		Job job = requireSavableJob(jobId);
		SavedJob saved = savedJobRepository.findByUserIdAndJobId(seeker.getId(), job.getId())
				.orElseGet(() -> {
					SavedJob entity = new SavedJob();
					entity.setUser(seeker);
					entity.setJob(job);
					entity.setCreatedBy(seeker.getEmail());
					entity.setUpdatedBy(seeker.getEmail());
					return savedJobRepository.save(entity);
				});
		boolean applied = jobApplicationRepository
				.countByJobIdAndUserIdIncludingDeleted(job.getId(), seeker.getId()) > 0;
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
				HttpStatus.CREATED.value(),
				messageService.get("saved-job.create.success"),
				jobApplicationMapper.toSavedJobResponse(
						saved,
						applied,
						jobApplicationRepository.countByJobId(job.getId()))));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> unsave(Long jobId, Jwt jwt) {
		User seeker = requireJobSeeker(jwt);
		int deleted = savedJobRepository.hardDeleteByUserIdAndJobId(seeker.getId(), jobId);
		if (deleted == 0) {
			throw new ResourceNotFoundException("error.resource.saved-job", "jobId", jobId);
		}
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("saved-job.delete.success"),
				null));
	}

	private Job requireSavableJob(Long jobId) {
		Job job = jobRepository.findById(jobId)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.job", "id", jobId));
		if (!JobStatusEnum.APPROVED.name().equals(job.getStatus()) || !job.isActive()) {
			throw new BusinessRuleException("error.saved-job.job-unavailable");
		}
		return job;
	}

	private User requireJobSeeker(Jwt jwt) {
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
		if (!UserTypeEnum.JOB_SEEKER.name().equals(user.getType())) {
			throw new BusinessRuleException("error.application.job-seeker-only");
		}
		return user;
	}

	private Set<Long> appliedJobIds(Long userId, List<SavedJob> savedJobs) {
		if (savedJobs.isEmpty()) {
			return Set.of();
		}
		ArrayList<Long> jobIds = new ArrayList<>(savedJobs.size());
		for (SavedJob saved : savedJobs) {
			jobIds.add(saved.getJob().getId());
		}
		return new HashSet<>(jobApplicationRepository.findAppliedJobIdsIncludingDeleted(userId, jobIds));
	}

	private Map<Long, Long> applicationCounts(List<SavedJob> savedJobs) {
		ArrayList<Long> jobIds = new ArrayList<>(savedJobs.size());
		for (SavedJob saved : savedJobs) {
			jobIds.add(saved.getJob().getId());
		}
		return jobApplicationRepository.countMapByJobIds(jobIds);
	}
}
