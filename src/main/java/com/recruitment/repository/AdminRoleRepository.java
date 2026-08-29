package com.recruitment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.AdminRole;

@Repository
public interface AdminRoleRepository extends JpaRepository<AdminRole, Long> {

	Optional<AdminRole> findByName(String name);

	@Query("SELECT r FROM AdminRole r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
	Optional<AdminRole> findByIdWithPermissions(@Param("id") Long id);

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Long id);

	@Query("SELECT DISTINCT r FROM AdminRole r LEFT JOIN FETCH r.permissions")
	List<AdminRole> findAllWithPermissions();
}
