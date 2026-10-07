package com.recruitment.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.dto.request.common.PublicJobFilterRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.PublicJobResponse;
import com.recruitment.entity.Company;
import com.recruitment.entity.Job;
import com.recruitment.enums.CompanyStatusEnum;
import com.recruitment.enums.JobStatusEnum;
import com.recruitment.exception.ResourceNotFoundException;
import com.recruitment.mapper.JobMapper;
import com.recruitment.repository.JobApplicationRepository;
import com.recruitment.repository.JobRepository;
import com.recruitment.repository.SavedJobRepository;
import com.recruitment.specification.PublicCatalogSpecification;

@Service
public class PublicJobService {

	private static final int RELATED_LIMIT = 8;

	private final JobRepository jobRepository;
	private final JobApplicationRepository jobApplicationRepository;
	private final SavedJobRepository savedJobRepository;
	private final JobMapper jobMapper;
	private final HotJobQueryService hotJobQueryService;
	private final MessageService messageService;

	public PublicJobService(
			JobRepository jobRepository,
			JobApplicationRepository jobApplicationRepository,
			SavedJobRepository savedJobRepository,
			JobMapper jobMapper,
			HotJobQueryService hotJobQueryService,
			MessageService messageService) {
		this.jobRepository = jobRepository;
		this.jobApplicationRepository = jobApplicationRepository;
		this.savedJobRepository = savedJobRepository;
		this.jobMapper = jobMapper;
		this.hotJobQueryService = hotJobQueryService;
		this.messageService = messageService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PaginatedResponse<PublicJobResponse>>> getJobs(
			PublicJobFilterRequest filter,
			Pageable pageable,
			Long viewerUserId) {
		if (filter.isHotOnlyListing()) {
			return ResponseEntity.ok(ApiResponse.success(
					messageService.get("job.list.success"),
					withViewerFlags(hotJobQueryService.list(pageable), viewerUserId)));
		}

		Page<Job> page = jobRepository.findAll(PublicCatalogSpecification.buildJobs(filter), pageable);
		Set<Long> appliedJobIds = appliedJobIds(viewerUserId, page.getContent());
		Set<Long> savedJobIds = savedJobIds(viewerUserId, page.getContent());
		Map<Long, Long> applicationCounts = applicationCounts(page.getContent());
		Page<PublicJobResponse> result = page.map(job -> jobMapper.toPublicResponse(
				job,
				appliedJobIds.contains(job.getId()),
				savedJobIds.contains(job.getId()),
				applicationCounts.getOrDefault(job.getId(), 0L)));
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("job.list.success"),
				PaginatedResponse.fromPage(result)));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<PublicJobResponse>> getJob(Long id, Long viewerUserId) {
		Job job = requirePublicJob(id);
		boolean applied = viewerUserId != null
				&& jobApplicationRepository.countByJobIdAndUserIdIncludingDeleted(job.getId(), viewerUserId) > 0;
		boolean saved = viewerUserId != null
				&& savedJobRepository.existsByUserIdAndJobId(viewerUserId, job.getId());
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				jobMapper.toPublicResponse(
						job,
						applied,
						saved,
						jobApplicationRepository.countByJobId(job.getId()))));
	}

	@Transactional(readOnly = true)
	public ResponseEntity<ApiResponse<List<PublicJobResponse>>> getRelatedJobs(Long id, Long viewerUserId) {
		Job source = requirePublicJob(id);
		List<Job> jobs = jobRepository
				.findAll(PublicCatalogSpecification.relatedJobs(source), PageRequest.of(0, RELATED_LIMIT))
				.getContent();
		Set<Long> appliedJobIds = appliedJobIds(viewerUserId, jobs);
		Set<Long> savedJobIds = savedJobIds(viewerUserId, jobs);
		Map<Long, Long> applicationCounts = applicationCounts(jobs);
		List<PublicJobResponse> result = jobs.stream()
				.map(job -> jobMapper.toPublicResponse(
						job,
						appliedJobIds.contains(job.getId()),
						savedJobIds.contains(job.getId()),
						applicationCounts.getOrDefault(job.getId(), 0L)))
				.toList();
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("job.related.success"),
				result));
	}

	private PaginatedResponse<PublicJobResponse> withViewerFlags(
			PaginatedResponse<PublicJobResponse> page,
			Long viewerUserId) {
		if (viewerUserId == null || page.data().isEmpty()) {
			return page;
		}
		ArrayList<Long> jobIds = new ArrayList<>(page.data().size());
		for (PublicJobResponse job : page.data()) {
			jobIds.add(job.id());
		}
		Set<Long> appliedJobIds = new HashSet<>(
				jobApplicationRepository.findAppliedJobIdsIncludingDeleted(viewerUserId, jobIds));
		Set<Long> savedJobIds = new HashSet<>(savedJobRepository.findSavedJobIds(viewerUserId, jobIds));
		List<PublicJobResponse> data = page.data().stream()
				.map(job -> jobMapper.withViewerFlags(
						job,
						appliedJobIds.contains(job.id()),
						savedJobIds.contains(job.id())))
				.toList();
		return new PaginatedResponse<>(data, page.pagination());
	}

	private Job requirePublicJob(Long id) {
		Job job = jobRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("error.resource.job", "id", id));
		Company company = job.getCompany();
		if (!JobStatusEnum.APPROVED.name().equals(job.getStatus())
				|| !job.isActive()
				|| job.getDeadline() == null
				|| job.getDeadline().isBefore(LocalDate.now())
				|| !CompanyStatusEnum.APPROVED.name().equals(company.getStatus())
				|| !company.isActive()) {
			throw new ResourceNotFoundException("error.resource.job", "id", id);
		}
		return job;
	}

	private Set<Long> appliedJobIds(Long viewerUserId, List<Job> jobs) {
		if (viewerUserId == null || jobs.isEmpty()) {
			return Collections.emptySet();
		}
		ArrayList<Long> jobIds = new ArrayList<>(jobs.size());
		for (Job job : jobs) {
			jobIds.add(job.getId());
		}
		return new HashSet<>(jobApplicationRepository.findAppliedJobIdsIncludingDeleted(viewerUserId, jobIds));
	}

	private Set<Long> savedJobIds(Long viewerUserId, List<Job> jobs) {
		if (viewerUserId == null || jobs.isEmpty()) {
			return Collections.emptySet();
		}
		ArrayList<Long> jobIds = new ArrayList<>(jobs.size());
		for (Job job : jobs) {
			jobIds.add(job.getId());
		}
		return new HashSet<>(savedJobRepository.findSavedJobIds(viewerUserId, jobIds));
	}

	private Map<Long, Long> applicationCounts(List<Job> jobs) {
		ArrayList<Long> jobIds = new ArrayList<>(jobs.size());
		for (Job job : jobs) {
			jobIds.add(job.getId());
		}
		return jobApplicationRepository.countMapByJobIds(jobIds);
	}
}
