package com.recruitment.repository;

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

	@Modifying
	@Query(value = "DELETE FROM saved_jobs WHERE user_id = :userId AND job_id = :jobId", nativeQuery = true)
	int hardDeleteByUserIdAndJobId(
			@Param("userId")
			Long userId,
			@Param("jobId")
			Long jobId);

}
