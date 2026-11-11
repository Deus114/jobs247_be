package com.recruitment.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.PublicJobResponse;
import com.recruitment.entity.Industry;
import com.recruitment.entity.Job;
import com.recruitment.entity.JobViewHistory;
import com.recruitment.entity.Province;
import com.recruitment.entity.User;
import com.recruitment.enums.UserTypeEnum;
import com.recruitment.exception.AccountInactiveException;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.InvalidTokenException;
import com.recruitment.mapper.JobMapper;
import com.recruitment.repository.JobApplicationRepository;
import com.recruitment.repository.JobRepository;
import com.recruitment.repository.JobViewHistoryRepository;
import com.recruitment.repository.SavedJobRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.security.JwtTokenProvider;
import com.recruitment.specification.PublicCatalogSpecification;

@Service
public class JobViewHistoryService {

	private static final Logger log = LoggerFactory.getLogger(JobViewHistoryService.class);
	private static final int RECENT_VIEW_LIMIT = 15;

	private final JobViewHistoryRepository jobViewHistoryRepository;
	private final UserRepository userRepository;
	private final JobRepository jobRepository;
	private final JobApplicationRepository jobApplicationRepository;
	private final SavedJobRepository savedJobRepository;
	private final JobMapper jobMapper;
	private final MessageService messageService;

	public JobViewHistoryService(
			JobViewHistoryRepository jobViewHistoryRepository,
			UserRepository userRepository,
			JobRepository jobRepository,
			JobApplicationRepository jobApplicationRepository,
			SavedJobRepository savedJobRepository,
			JobMapper jobMapper,
			MessageService messageService) {
		this.jobViewHistoryRepository = jobViewHistoryRepository;
		this.userRepository = userRepository;
		this.jobRepository = jobRepository;
		this.jobApplicationRepository = jobApplicationRepository;
		this.savedJobRepository = savedJobRepository;
		this.jobMapper = jobMapper;
		this.messageService = messageService;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void recordView(Long userId, Job job) {
		if (userId == null || job == null || job.getId() == null) {
			return;
		}
		try {
			upsert(userId, job);
		} catch (DataIntegrityViolationException ex) {
			JobViewHistory existing = jobViewHistoryRepository.findByUserIdAndJobId(userId, job.getId())
					.orElse(null);
			if (existing != null) {
				existing.setLastViewedAt(Instant.now());
				jobViewHistoryRepository.save(existing);
			}
		} catch (Exception ex) {
			log.warn("Skip job view history for userId={} jobId={}: {}",
					userId, job.getId(), ex.getMessage());
		}
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<PublicJobResponse>>> getRecommended(
			Pageable pageable,
			Jwt jwt) {
		User seeker = requireJobSeeker(jwt);
		List<JobViewHistory> recentViews = jobViewHistoryRepository.findRecentByUserId(
				seeker.getId(),
				PageRequest.of(0, RECENT_VIEW_LIMIT));

		Set<Long> excludeJobIds = new HashSet<>();
		Set<Long> industryIds = new HashSet<>();
		Set<Long> provinceIds = new HashSet<>();
		for (JobViewHistory history : recentViews) {
			Job viewed = history.getJob();
			excludeJobIds.add(viewed.getId());
			for (Industry industry : viewed.getIndustries()) {
				industryIds.add(industry.getId());
			}
			for (Province province : viewed.getProvinces()) {
				provinceIds.add(province.getId());
			}
		}

		List<Long> industryIdList = new ArrayList<>(industryIds);
		List<Long> provinceIdList = new ArrayList<>(provinceIds);
		List<Long> excludeJobIdList = new ArrayList<>(excludeJobIds);

		Specification<Job> recommendedSpec = PublicCatalogSpecification.recommendedJobs(
				industryIdList,
				provinceIdList,
				excludeJobIdList);
		Specification<Job> latestSpec = PublicCatalogSpecification.latestPublicJobs(
				industryIdList,
				provinceIdList,
				excludeJobIdList);

		long recommendedTotal = industryIds.isEmpty() && provinceIds.isEmpty()
				? 0L
				: jobRepository.count(recommendedSpec);
		long latestTotal = jobRepository.count(latestSpec);
		long total = recommendedTotal + latestTotal;

		List<Job> content = new ArrayList<>();
		int pageSize = pageable.getPageSize();
		long offset = pageable.getOffset();
		Sort sort = pageable.getSort().isSorted()
				? pageable.getSort()
				: Sort.by(Sort.Direction.DESC, "createdAt");

		if (total > 0 && offset < total && pageSize > 0) {
			if (offset < recommendedTotal) {
				int fromRecommended = (int) Math.min(pageSize, recommendedTotal - offset);
				content.addAll(jobRepository.findAll(
						recommendedSpec,
						offsetPageable(offset, fromRecommended, sort)).getContent());
				int need = pageSize - content.size();
				if (need > 0 && latestTotal > 0) {
					content.addAll(jobRepository.findAll(
							latestSpec,
							offsetPageable(0, need, sort)).getContent());
				}
			} else {
				long latestOffset = offset - recommendedTotal;
				content.addAll(jobRepository.findAll(
						latestSpec,
						offsetPageable(latestOffset, pageSize, sort)).getContent());
			}
		}

		Set<Long> appliedJobIds = appliedJobIds(seeker.getId(), content);
		Set<Long> savedJobIds = savedJobIds(seeker.getId(), content);
		Map<Long, Long> applicationCounts = applicationCounts(content);
		List<PublicJobResponse> responses = new ArrayList<>(content.size());
		for (Job job : content) {
			responses.add(jobMapper.toPublicResponse(
					job,
					appliedJobIds.contains(job.getId()),
					savedJobIds.contains(job.getId()),
					applicationCounts.getOrDefault(job.getId(), 0L)));
		}
		Page<PublicJobResponse> result = new PageImpl<>(responses, pageable, total);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("job.recommended.success"),
				PaginatedResponse.fromPage(result)));
	}

	private static Pageable offsetPageable(long offset, int size, Sort sort) {
		return new Pageable() {
			@Override
			public int getPageNumber() {
				return size == 0 ? 0 : (int) (offset / size);
			}

			@Override
			public int getPageSize() {
				return size;
			}

			@Override
			public long getOffset() {
				return offset;
			}

			@Override
			public Sort getSort() {
				return sort;
			}

			@Override
			public Pageable next() {
				return offsetPageable(offset + size, size, sort);
			}

			@Override
			public Pageable previousOrFirst() {
				long previous = Math.max(0, offset - size);
				return offsetPageable(previous, size, sort);
			}

			@Override
			public Pageable first() {
				return offsetPageable(0, size, sort);
			}

			@Override
			public Pageable withPage(int pageNumber) {
				return offsetPageable((long) pageNumber * size, size, sort);
			}

			@Override
			public boolean hasPrevious() {
				return offset > 0;
			}
		};
	}

	private void upsert(Long userId, Job job) {
		JobViewHistory history = jobViewHistoryRepository.findByUserIdAndJobId(userId, job.getId())
				.orElseGet(JobViewHistory::new);
		if (history.getId() == null) {
			User user = userRepository.getReferenceById(userId);
			history.setUser(user);
			history.setJob(job);
		}
		history.setLastViewedAt(Instant.now());
		jobViewHistoryRepository.saveAndFlush(history);
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

	private Set<Long> appliedJobIds(Long userId, List<Job> jobs) {
		if (jobs.isEmpty()) {
			return Collections.emptySet();
		}
		ArrayList<Long> jobIds = new ArrayList<>(jobs.size());
		for (Job job : jobs) {
			jobIds.add(job.getId());
		}
		return new HashSet<>(jobApplicationRepository.findAppliedJobIdsIncludingDeleted(userId, jobIds));
	}

	private Set<Long> savedJobIds(Long userId, List<Job> jobs) {
		if (jobs.isEmpty()) {
			return Collections.emptySet();
		}
		ArrayList<Long> jobIds = new ArrayList<>(jobs.size());
		for (Job job : jobs) {
			jobIds.add(job.getId());
		}
		return new HashSet<>(savedJobRepository.findSavedJobIds(userId, jobIds));
	}

	private Map<Long, Long> applicationCounts(List<Job> jobs) {
		ArrayList<Long> jobIds = new ArrayList<>(jobs.size());
		for (Job job : jobs) {
			jobIds.add(job.getId());
		}
		return jobApplicationRepository.countMapByJobIds(jobIds);
	}
}
