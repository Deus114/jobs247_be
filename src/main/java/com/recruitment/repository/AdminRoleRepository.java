package com.recruitment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.AdminRole;

@Repository
public interface AdminRoleRepository extends JpaRepository<AdminRole, Long>, JpaSpecificationExecutor<AdminRole> {

	Optional<AdminRole> findByName(String name);

	@Query("SELECT r FROM AdminRole r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
	Optional<AdminRole> findByIdWithPermissions(@Param("id") Long id);

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Long id);

	@Query("SELECT DISTINCT r FROM AdminRole r LEFT JOIN FETCH r.permissions")
	List<AdminRole> findAllWithPermissions();

	@Query(value = """
			SELECT r.* FROM admin_roles r
			WHERE r.deleted_at = 1
			AND (
				:keyword IS NULL
				OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(IFNULL(r.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
			)
			AND (:fullAccess IS NULL OR r.full_access = :fullAccess)
			AND (:active IS NULL OR r.active = :active)
			ORDER BY r.created_at DESC
			""",
			countQuery = """
			SELECT COUNT(*) FROM admin_roles r
			WHERE r.deleted_at = 1
			AND (
				:keyword IS NULL
				OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(IFNULL(r.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
			)
			AND (:fullAccess IS NULL OR r.full_access = :fullAccess)
			AND (:active IS NULL OR r.active = :active)
			""",
			nativeQuery = true)
	Page<AdminRole> findAllSoftDeleted(
			@Param("keyword") String keyword,
			@Param("fullAccess") Boolean fullAccess,
			@Param("active") Boolean active,
			Pageable pageable);

	@Query(value = "SELECT * FROM admin_roles WHERE id = :id AND deleted_at = 1", nativeQuery = true)
	Optional<AdminRole> findSoftDeletedById(@Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE admin_roles SET deleted_at = 0, deleted_by = NULL WHERE id = :id AND deleted_at = 1",
			nativeQuery = true)
	int restoreById(@Param("id") Long id);

	@Modifying
	@Query(value = "DELETE FROM admin_role_permission WHERE role_id = :roleId", nativeQuery = true)
	void deletePermissionLinksByRoleId(@Param("roleId") Long roleId);

	@Modifying
	@Query(value = "DELETE FROM admin_roles WHERE id = :id", nativeQuery = true)
	void hardDeleteById(@Param("id") Long id);

	@Query(value = "SELECT COUNT(*) FROM admin_users WHERE role_id = :roleId", nativeQuery = true)
	long countUsersByRoleId(@Param("roleId") Long roleId);
}
