package com.recruitment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.JobViewHistory;

@Repository
public interface JobViewHistoryRepository extends JpaRepository<JobViewHistory, Long> {

	Optional<JobViewHistory> findByUserIdAndJobId(Long userId, Long jobId);

	@Query("""
			SELECT h FROM JobViewHistory h
			JOIN FETCH h.job j
			WHERE h.user.id = :userId
			ORDER BY h.lastViewedAt DESC
			""")
	List<JobViewHistory> findRecentByUserId(
			@Param("userId")
			Long userId,
			Pageable pageable);
}
