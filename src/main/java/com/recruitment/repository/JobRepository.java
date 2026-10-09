package com.recruitment.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

	long countByStatus(String status);

	long countByCreatedAtLessThan(Instant end);

	long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(Instant start, Instant end);

	long countByStatusAndCreatedAtLessThan(String status, Instant end);

	List<Job> findTop8ByOrderByCreatedAtDesc();

	long countByCompanyIdIn(Collection<Long> companyIds);

	List<Job> findTop8ByCompanyIdInOrderByCreatedAtDesc(Collection<Long> companyIds);

	@Query("""
			SELECT COUNT(job.id) FROM Job job
			WHERE job.company.id IN :companyIds
			  AND job.status = :status
			  AND job.active = true
			  AND job.deadline >= :today
			  AND job.company.status = :companyStatus
			  AND job.company.active = true
			""")
	long countOpenByCompanyIds(
			@Param("companyIds")
			Collection<Long> companyIds,
			@Param("status")
			String status,
			@Param("companyStatus")
			String companyStatus,
			@Param("today")
			LocalDate today);

	@Query("""
			SELECT industry.id, industry.nameVi, industry.nameEn, COUNT(job.id)
			FROM Job job
			JOIN job.industries industry
			GROUP BY industry.id, industry.nameVi, industry.nameEn
			ORDER BY COUNT(job.id) DESC
			""")

	List<Object[]> countByIndustry();

	@Query("""
			SELECT COUNT(job.id) FROM Job job
			WHERE job.status = :status
			  AND job.active = true
			  AND job.deadline >= :today
			  AND job.company.status = :companyStatus
			  AND job.company.active = true
			""")
	long countApprovedOpen(
			@Param("status")
			String status,
			@Param("companyStatus")
			String companyStatus,
			@Param("today")
			LocalDate today);

	@Query(value = "SELECT * FROM jobs WHERE id = :id AND deleted_at = 1", nativeQuery = true)
	Optional<Job> findSoftDeletedById(
			@Param("id")
			Long id);

	@Modifying
	@Query(value = "UPDATE jobs SET deleted_at = 0, deleted_by = NULL WHERE id = :id AND deleted_at = 1", nativeQuery = true)
	int restoreById(
			@Param("id")
			Long id);
}
