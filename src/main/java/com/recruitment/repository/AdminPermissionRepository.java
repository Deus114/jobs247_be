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

import com.recruitment.entity.AdminPermission;

@Repository
public interface AdminPermissionRepository
		extends JpaRepository<AdminPermission, Long>, JpaSpecificationExecutor<AdminPermission> {

	Optional<AdminPermission> findByPathAndMethod(String path, String method);

	boolean existsByPathAndMethod(String path, String method);

	boolean existsByPathAndMethodAndIdNot(String path, String method, Long id);

	@Query(value = """
			SELECT p.* FROM admin_permissions p
			WHERE p.deleted_at = 1
			AND (
				:keyword IS NULL
				OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(IFNULL(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(p.path) LIKE LOWER(CONCAT('%', :keyword, '%'))
			)
			AND (:module IS NULL OR p.module = :module)
			AND (:method IS NULL OR p.method = :method)
			AND (:type IS NULL OR p.type = :type)
			AND (:active IS NULL OR p.active = :active)
			ORDER BY p.created_at DESC
			""",
			countQuery = """
			SELECT COUNT(*) FROM admin_permissions p
			WHERE p.deleted_at = 1
			AND (
				:keyword IS NULL
				OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(IFNULL(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(p.path) LIKE LOWER(CONCAT('%', :keyword, '%'))
			)
			AND (:module IS NULL OR p.module = :module)
			AND (:method IS NULL OR p.method = :method)
			AND (:type IS NULL OR p.type = :type)
			AND (:active IS NULL OR p.active = :active)
			""",
			nativeQuery = true)
	Page<AdminPermission> findAllSoftDeleted(
			@Param("keyword") String keyword,
			@Param("module") String module,
			@Param("method") String method,
			@Param("type") String type,
			@Param("active") Boolean active,
			Pageable pageable);

	@Query(value = "SELECT * FROM admin_permissions WHERE id = :id AND deleted_at = 1", nativeQuery = true)
	Optional<AdminPermission> findSoftDeletedById(@Param("id") Long id);

	@Modifying
	@Query(value = """
			UPDATE admin_permissions
			SET deleted_at = 0, deleted_by = NULL
			WHERE id = :id AND deleted_at = 1
			""",
			nativeQuery = true)
	int restoreById(@Param("id") Long id);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "DELETE FROM admin_role_permission WHERE permission_id IN (:ids)", nativeQuery = true)
	void deleteRoleLinksByPermissionIds(@Param("ids") List<Long> ids);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "DELETE FROM admin_permissions WHERE id IN (:ids)", nativeQuery = true)
	void hardDeleteByIds(@Param("ids") List<Long> ids);

	@Modifying
	@Query(value = "DELETE FROM admin_permissions WHERE id = :id", nativeQuery = true)
	void hardDeleteById(@Param("id") Long id);
}
