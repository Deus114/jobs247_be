package com.recruitment.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.CompanyJoinRequest;

@Repository
public interface CompanyJoinRequestRepository extends JpaRepository<CompanyJoinRequest, Long> {

	boolean existsByCompanyIdAndUserIdAndStatus(Long companyId, Long userId, String status);

	long countByUserIdAndStatus(Long userId, String status);

	@Query("""
			SELECT r.company.id FROM CompanyJoinRequest r
			WHERE r.user.id = :userId
				AND r.status = :status
				AND r.company.id IN :companyIds
			""")
	List<Long> findCompanyIdsByUserIdAndStatusAndCompanyIdIn(
			@Param("userId")
			Long userId,
			@Param("status")
			String status,
			@Param("companyIds")
			Collection<Long> companyIds);

	
	Optional<CompanyJoinRequest> findByIdAndCompanyId(Long id, Long companyId);

	Page<CompanyJoinRequest> findByCompanyId(Long companyId, Pageable pageable);

	Page<CompanyJoinRequest> findByCompanyIdAndStatus(Long companyId, String status, Pageable pageable);

	Page<CompanyJoinRequest> findByCompanyIdIn(Collection<Long> companyIds, Pageable pageable);

	Page<CompanyJoinRequest> findByCompanyIdInAndStatus(
			Collection<Long> companyIds,
			String status,
			
			Pageable pageabl
			);

			
	Page<CompanyJoinRequest> findByUserId(Long userId, Pageable pageable);

	Page<CompanyJoinRequest> findByUserIdAndStatus(Long userId, String status, Pageable pageable);
}
