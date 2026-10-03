package com.recruitment.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.CompanyJoinRequest;

@Repository
public interface CompanyJoinRequestRepository extends JpaRepository<CompanyJoinRequest, Long> {

	boolean existsByCompanyIdAndUserIdAndStatus(Long companyId, Long userId, String status);

	long countByUserIdAndStatus(Long userId, String status);

	Optional<CompanyJoinRequest> findByIdAndCompanyId(Long id, Long companyId);

	Page<CompanyJoinRequest> findByCompanyId(Long companyId, Pageable pageable);

	Page<CompanyJoinRequest> findByCompanyIdAndStatus(Long companyId, String status, Pageable pageable);

	Page<CompanyJoinRequest> findByUserId(Long userId, Pageable pageable);

	Page<CompanyJoinRequest> findByUserIdAndStatus(Long userId, String status, Pageable pageable);
}
