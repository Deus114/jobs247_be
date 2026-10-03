package com.recruitment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

	@Query(value = "SELECT COUNT(*) FROM users WHERE email = :email", nativeQuery = true)
	long countByEmailIncludingDeleted(@Param("email") String email);

	@Modifying
	@Query(value = "UPDATE users SET deleted_at = 0, deleted_by = NULL WHERE id = :id AND deleted_at = 1",
			nativeQuery = true)
	int restoreById(@Param("id") Long id);
}
