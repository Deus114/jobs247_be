package com.recruitment.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.CompanyMember;

@Repository
public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Long> {

	Optional<CompanyMember> findByCompanyIdAndUserId(Long companyId, Long userId);

	List<CompanyMember> findByCompanyIdOrderByCreatedAtAsc(Long companyId);

	List<CompanyMember> findByUserIdOrderByCreatedAtAsc(Long userId);

	List<CompanyMember> findByUserIdInOrderByCreatedAtAsc(Collection<Long> userIds);

	long countByUserId(Long userId);

	boolean existsByCompanyIdAndUserId(Long companyId, Long userId);
}
