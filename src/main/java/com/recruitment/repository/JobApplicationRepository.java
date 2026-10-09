package com.recruitment.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.JobApplication;

@Repository
public interface JobApplicationRepository
		extends JpaRepository<JobApplication, Long>, JpaSpecificationExecutor<JobApplication> {

	long countByJobId(Long jobId);

	@Query("""
			SELECT COUNT(application.id) FROM JobApplication application
			WHERE application.job.company.id IN :companyIds
			""")
	long countByCompanyIds(@Param("companyIds") Collection<Long> companyIds);

	@Query("""
			SELECT COUNT(application.id) FROM JobApplication application
			WHERE application.job.company.id IN :companyIds
			  AND application.createdAt >= :start
			""")
	long countByCompanyIdsSince(
			@Param("companyIds") Collection<Long> companyIds,
			@Param("start") Instant start);

	@Query("""
			SELECT COUNT(application.id) FROM JobApplication application
			WHERE application.job.company.id IN :companyIds
			  AND application.status = :status
			""")
	long countByCompanyIdsAndStatus(
			@Param("companyIds") Collection<Long> companyIds,
			@Param("status") String status);

	@Query("""
			SELECT job.id, job.title, COUNT(application.id)
			FROM JobApplication application
			JOIN application.job job
			WHERE job.company.id IN :companyIds
			GROUP BY job.id, job.title
			ORDER BY COUNT(application.id) DESC
			""")
	List<Object[]> countGroupedByCompanyIds(@Param("companyIds") Collection<Long> companyIds);

	@Query("""
			SELECT application.createdAt FROM JobApplication application
			WHERE application.job.company.id IN :companyIds
			  AND application.createdAt >= :start
			""")
	List<Instant> findCreatedAtByCompanyIdsSince(
			@Param("companyIds") Collection<Long> companyIds,
			@Param("start") Instant start);

	List<JobApplication> findTop8ByJob_Company_IdInOrderByCreatedAtDesc(Collection<Long> companyIds);

	@Query("""
			SELECT ja.job.id, COUNT(ja)
			FROM JobApplication ja
			WHERE ja.job.id IN :jobIds
			GROUP BY ja.job.id
			""")
	List<Object[]> countGroupedByJobIds(@Param("jobIds") Collection<Long> jobIds);

	default Map<Long, Long> countMapByJobIds(Collection<Long> jobIds) {
		if (jobIds == null || jobIds.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<Long, Long> counts = new HashMap<>();
		for (Object[] row : countGroupedByJobIds(jobIds)) {
			counts.put((Long) row[0], (Long) row[1]);
		}
		return counts;
	}

	@Query(value = """
			SELECT COUNT(*) FROM job_applications
			WHERE job_id = :jobId AND user_id = :userId
			""", nativeQuery = true)
	long countByJobIdAndUserIdIncludingDeleted(
			@Param("jobId") Long jobId,
			@Param("userId") Long userId);

	@Query(value = """
			SELECT DISTINCT job_id FROM job_applications
			WHERE user_id = :userId AND job_id IN (:jobIds)
			""", nativeQuery = true)
	List<Long> findAppliedJobIdsIncludingDeleted(
			@Param("userId") Long userId,
			@Param("jobIds") Collection<Long> jobIds);

	Optional<JobApplication> findByIdAndJobCompanyId(Long id, Long companyId);

	@Query(value = "SELECT * FROM job_applications WHERE id = :id AND deleted_at = 1", nativeQuery = true)
	Optional<JobApplication> findSoftDeletedById(@Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE job_applications SET deleted_at = 0, deleted_by = NULL WHERE id = :id AND deleted_at = 1",
			nativeQuery = true)
	int restoreById(@Param("id") Long id);

	@Modifying
	@Query(value = "DELETE FROM job_applications WHERE id = :id", nativeQuery = true)
	void hardDeleteById(@Param("id") Long id);
}
