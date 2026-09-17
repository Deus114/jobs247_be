package com.recruitment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.AdminUser;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long>, JpaSpecificationExecutor<AdminUser> {

	Optional<AdminUser> findByUsername(String username);

	@Query("""
			SELECT u FROM AdminUser u
			JOIN FETCH u.role r
			LEFT JOIN FETCH r.permissions
			WHERE u.username = :username
			""")
	Optional<AdminUser> findByUsernameWithRolePermissions(@Param("username") String username);

	@Query("""
			SELECT u FROM AdminUser u
			JOIN FETCH u.role r
			LEFT JOIN FETCH r.permissions
			WHERE u.id = :id
			""")
	Optional<AdminUser> findByIdWithRolePermissions(@Param("id") Long id);

	boolean existsByUsername(String username);

	boolean existsByUsernameAndIdNot(String username, Long id);

	boolean existsByRoleId(Long roleId);

	@Query(value = "SELECT * FROM admin_users WHERE id = :id AND deleted_at = 1", nativeQuery = true)
	Optional<AdminUser> findSoftDeletedById(@Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE admin_users SET deleted_at = 0, deleted_by = NULL WHERE id = :id AND deleted_at = 1",
			nativeQuery = true)
	int restoreById(@Param("id") Long id);

	@Modifying
	@Query(value = "DELETE FROM admin_refresh_tokens WHERE admin_user_id = :adminUserId", nativeQuery = true)
	void deleteRefreshTokensByAdminUserId(@Param("adminUserId") Long adminUserId);

	@Modifying
	@Query(value = "DELETE FROM admin_users WHERE id = :id", nativeQuery = true)
	void hardDeleteById(@Param("id") Long id);
}
