package com.recruitment.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {

	long countByStatus(String status);

	long countByStatusAndActiveTrue(String status);

	long countByCreatedAtLessThan(Instant end);

	long countByStatusAndCreatedAtLessThan(String status, Instant end);

	@Query(value = "SELECT COUNT(*) FROM companies WHERE tax_code = :taxCode", nativeQuery = true)
	long countByTaxCodeIncludingDeleted(
			@Param("taxCode")
			String taxCode);

	@Query(value = "SELECT COUNT(*) FROM companies WHERE tax_code = :taxCode AND id <> :id", nativeQuery = true)
	long countByTaxCodeIncludingDeletedAndIdNot(
			@Param("taxCode")
			String taxCode,
			@Param("id")
			Long id);

	@Modifying
	@Query(value = "UPDATE companies SET deleted_at = 0, deleted_by = NULL WHERE id = :id AND deleted_at = 1", nativeQuery = true)
	int restoreById(
			@Param("id")
			Long id);
}
