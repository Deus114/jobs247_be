package com.recruitment.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.SavedJob;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long>, JpaSpecificationExecutor<SavedJob> {

	boolean existsByUserIdAndJobId(Long userId, Long jobId);

	Optional<SavedJob> findByUserIdAndJobId(Long userId, Long jobId);

	@Query("""
			SELECT s.job.id FROM SavedJob s
			WHERE s.user.id = :userId AND s.job.id IN :jobIds
			""")
	List<Long> findSavedJobIds(
			@Param("userId") Long userId,
			@Param("jobIds") Collection<Long> jobIds);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "DELETE FROM saved_jobs WHERE user_id = :userId AND job_id = :jobId", nativeQuery = true)
	int hardDeleteByUserIdAndJobId(
			@Param("userId") Long userId,
			@Param("jobId") Long jobId);
}
