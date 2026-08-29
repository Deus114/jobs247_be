package com.recruitment.seeder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.entity.AdminPermission;
import com.recruitment.entity.AdminRole;
import com.recruitment.entity.AdminUser;
import com.recruitment.enums.PermissionTypeEnum;
import com.recruitment.repository.AdminPermissionRepository;
import com.recruitment.repository.AdminRoleRepository;
import com.recruitment.repository.AdminUserRepository;
import com.recruitment.security.PermissionAuthorizationManager;
import com.recruitment.util.constant.Constants;

@Component
@Order(1)
@ConditionalOnProperty(name = "app.seed-admin", havingValue = "true")
public class AdminRbacSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminRbacSeeder.class);
	private static final String SUPER_ADMIN_ROLE = "Super Admin";
	private static final String DEFAULT_USERNAME = "admin";
	private static final String DEFAULT_PASSWORD = "admin";
	private static final String MODULE_ADMIN_USER = "admin-user";

	private final AdminRoleRepository adminRoleRepository;
	private final AdminUserRepository adminUserRepository;
	private final AdminPermissionRepository adminPermissionRepository;
	private final PasswordEncoder passwordEncoder;
	private final PermissionAuthorizationManager permissionAuthorizationManager;

	public AdminRbacSeeder(
			AdminRoleRepository adminRoleRepository,
			AdminUserRepository adminUserRepository,
			AdminPermissionRepository adminPermissionRepository,
			PasswordEncoder passwordEncoder,
			PermissionAuthorizationManager permissionAuthorizationManager) {
		this.adminRoleRepository = adminRoleRepository;
		this.adminUserRepository = adminUserRepository;
		this.adminPermissionRepository = adminPermissionRepository;
		this.passwordEncoder = passwordEncoder;
		this.permissionAuthorizationManager = permissionAuthorizationManager;
	}

	@Override
	@Transactional
	public void run(String... args) {
		syncAdminUserPermissions();

		if (adminUserRepository.count() == 0) {
			AdminRole superAdminRole = seedSuperAdminRole();
			seedSuperAdminUser(superAdminRole);
			log.info("Seeded Super Admin role and account ({}/{})", DEFAULT_USERNAME, DEFAULT_PASSWORD);
		} else {
			log.info("Admin users already exist — skip default account seed");
		}
		permissionAuthorizationManager.loadCache();
		log.info("Admin permission cache reloaded");
	}

	private void syncAdminUserPermissions() {
		List<PermissionSeed> seeds = List.of(
				new PermissionSeed("VIEW_ADMIN_USERS", "Xem danh sách admin", "GET",
						Constants.ADMIN_USERS_BASE_PATH),
				new PermissionSeed("VIEW_ADMIN_USER", "Xem chi tiết admin", "GET",
						Constants.ADMIN_USERS_BASE_PATH + "/{id}"),
				new PermissionSeed("CREATE_ADMIN_USER", "Tạo admin", "POST",
						Constants.ADMIN_USERS_BASE_PATH),
				new PermissionSeed("UPDATE_ADMIN_USER", "Cập nhật admin", "PUT",
						Constants.ADMIN_USERS_BASE_PATH + "/{id}"),
				new PermissionSeed("DELETE_ADMIN_USER", "Xóa mềm admin", "DELETE",
						Constants.ADMIN_USERS_BASE_PATH + "/{id}"),
				new PermissionSeed("RESTORE_ADMIN_USER", "Khôi phục admin", "PATCH",
						Constants.ADMIN_USERS_BASE_PATH + "/{id}/restore"),
				new PermissionSeed("PERMANENT_DELETE_ADMIN_USER", "Xóa vĩnh viễn admin", "DELETE",
						Constants.ADMIN_USERS_BASE_PATH + "/{id}/permanent"));

		Set<String> seedKeys = new HashSet<>();
		int created = 0;
		int updated = 0;

		for (PermissionSeed seed : seeds) {
			seedKeys.add(permissionKey(seed.path(), seed.method()));
			AdminPermission permission = adminPermissionRepository
					.findByPathAndMethod(seed.path(), seed.method())
					.orElse(null);

			if (permission == null) {
				permission = new AdminPermission();
				applySeed(permission, seed);
				adminPermissionRepository.save(permission);
				created++;
				continue;
			}

			if (applySeed(permission, seed)) {
				adminPermissionRepository.save(permission);
				updated++;
			}
		}

		List<Long> removedIds = new ArrayList<>();
		for (AdminPermission existing : adminPermissionRepository.findAll()) {
			if (!seedKeys.contains(permissionKey(existing.getPath(), existing.getMethod()))) {
				removedIds.add(existing.getId());
			}
		}

		if (!removedIds.isEmpty()) {
			adminPermissionRepository.deleteRoleLinksByPermissionIds(removedIds);
			adminPermissionRepository.hardDeleteByIds(removedIds);
		}

		log.info("Permissions synced — created={}, updated={}, removed={}", created, updated, removedIds.size());
	}

	private boolean applySeed(AdminPermission permission, PermissionSeed seed) {
		boolean changed = permission.getId() == null;
		if (!Objects.equals(permission.getName(), seed.name())) {
			permission.setName(seed.name());
			changed = true;
		}
		if (!Objects.equals(permission.getDescription(), seed.description())) {
			permission.setDescription(seed.description());
			changed = true;
		}
		if (!Objects.equals(permission.getModule(), MODULE_ADMIN_USER)) {
			permission.setModule(MODULE_ADMIN_USER);
			changed = true;
		}
		if (!Objects.equals(permission.getPath(), seed.path())) {
			permission.setPath(seed.path());
			changed = true;
		}
		if (!Objects.equals(permission.getMethod(), seed.method())) {
			permission.setMethod(seed.method());
			changed = true;
		}
		if (permission.getType() != PermissionTypeEnum.ACTION) {
			permission.setType(PermissionTypeEnum.ACTION);
			changed = true;
		}
		if (!permission.isActive()) {
			permission.setActive(true);
			changed = true;
		}
		return changed;
	}

	private String permissionKey(String path, String method) {
		return method + " " + path;
	}

	private AdminRole seedSuperAdminRole() {
		return adminRoleRepository.findByName(SUPER_ADMIN_ROLE).orElseGet(() -> {
			AdminRole role = new AdminRole();
			role.setName(SUPER_ADMIN_ROLE);
			role.setDescription("Toàn quyền quản trị hệ thống");
			role.setFullAccess(true);
			role.setActive(true);
			return adminRoleRepository.save(role);
		});
	}

	private void seedSuperAdminUser(AdminRole superAdminRole) {
		AdminUser admin = new AdminUser();
		admin.setUsername(DEFAULT_USERNAME);
		admin.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
		admin.setName("Super Admin");
		admin.setRole(superAdminRole);
		admin.setActive(true);
		adminUserRepository.save(admin);
	}

	private record PermissionSeed(String name, String description, String method, String path) {
	}
}
