package com.recruitment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.AdminPermission;

@Repository
public interface AdminPermissionRepository extends JpaRepository<AdminPermission, Long> {

	Optional<AdminPermission> findByPathAndMethod(String path, String method);

	boolean existsByPathAndMethod(String path, String method);

	boolean existsByPathAndMethodAndIdNot(String path, String method, Long id);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "DELETE FROM admin_role_permission WHERE permission_id IN (:ids)", nativeQuery = true)
	void deleteRoleLinksByPermissionIds(@Param("ids") List<Long> ids);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "DELETE FROM admin_permissions WHERE id IN (:ids)", nativeQuery = true)
	void hardDeleteByIds(@Param("ids") List<Long> ids);
}
