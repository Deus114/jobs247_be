package com.recruitment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.AdminPermission;

@Repository
public interface AdminPermissionRepository extends JpaRepository<AdminPermission, Long> {

	boolean existsByApiPathAndMethod(String apiPath, String method);

	boolean existsByApiPathAndMethodAndIdNot(String apiPath, String method, Long id);
}
