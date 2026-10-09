package com.recruitment.service;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.dto.request.common.PublicJobFilterRequest;
import com.recruitment.dto.response.common.PaginatedResponse;
import com.recruitment.dto.response.common.PublicJobResponse;
import com.recruitment.entity.Job;
import com.recruitment.mapper.JobMapper;
import com.recruitment.repository.JobApplicationRepository;
import com.recruitment.repository.JobRepository;
import com.recruitment.specification.PublicCatalogSpecification;
import com.recruitment.util.constant.CacheNames;

@Service
public class HotJobQueryService {

	private static final PublicJobFilterRequest HOT_ONLY = new PublicJobFilterRequest(
			null, null, null, null, null, null, null, null, Boolean.TRUE, null, null);

	private final JobRepository jobRepository;
	private final JobApplicationRepository jobApplicationRepository;
	private final JobMapper jobMapper;

	public HotJobQueryService(
			JobRepository jobRepository,
			JobApplicationRepository jobApplicationRepository,
			JobMapper jobMapper) {
		this.jobRepository = jobRepository;
		this.jobApplicationRepository = jobApplicationRepository;
		this.jobMapper = jobMapper;
	}

	@Cacheable(
			cacheNames = CacheNames.HOT_JOBS,
			key = "T(com.recruitment.util.helper.CacheKeyHelper).pageable(#pageable)")
	@Transactional(readOnly = true)
	public PaginatedResponse<PublicJobResponse> list(Pageable pageable) {
		Page<Job> page = jobRepository.findAll(PublicCatalogSpecification.buildJobs(HOT_ONLY), pageable);
		Map<Long, Long> applicationCounts = applicationCounts(page.getContent());
		Page<PublicJobResponse> result = page.map(job -> jobMapper.toPublicResponse(
				job,
				false,
				false,
				applicationCounts.getOrDefault(job.getId(), 0L)));
		return PaginatedResponse.fromPage(result);
	}

	private Map<Long, Long> applicationCounts(java.util.List<Job> jobs) {
		ArrayList<Long> jobIds = new ArrayList<>(jobs.size());
		for (Job job : jobs) {
			jobIds.add(job.getId());
		}
		return jobApplicationRepository.countMapByJobIds(jobIds);
	}
}
