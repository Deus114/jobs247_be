package com.recruitment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.AdminRole;

@Repository
public interface AdminRoleRepository extends JpaRepository<AdminRole, Long> {

	Optional<AdminRole> findByName(String name);

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Long id);

	@Query("SELECT r FROM AdminRole r JOIN FETCH r.permissions")
	List<AdminRole> findAllWithPermissions();
}
