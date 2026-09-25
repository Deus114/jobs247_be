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
	private static final String MODULE_ADMIN_ROLE = "admin-role";
	private static final String MODULE_ADMIN_PERMISSION = "admin-permission";
	private static final String MODULE_BUSINESS_CONFIG = "business-config";
	private static final String MODULE_INDUSTRY_GROUP = "industry-group";
	private static final String MODULE_INDUSTRY = "industry";
	private static final String MODULE_PROVINCE = "province";
	private static final String MODULE_EDUCATION_LEVEL = "education-level";
	private static final PermissionTypeEnum SEEDED_TYPE = PermissionTypeEnum.ACTION;
	private static final Set<String> MANAGED_MODULES = Set.of(
			MODULE_ADMIN_USER,
			MODULE_ADMIN_ROLE,
			MODULE_ADMIN_PERMISSION,
			MODULE_BUSINESS_CONFIG,
			MODULE_INDUSTRY_GROUP,
			MODULE_INDUSTRY,
			MODULE_PROVINCE,
			MODULE_EDUCATION_LEVEL);

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
		syncManagedPermissions();

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

	private void syncManagedPermissions() {
		List<PermissionSeed> seeds = new ArrayList<>();
		seeds.addAll(adminUserPermissionSeeds());
		seeds.addAll(adminRolePermissionSeeds());
		seeds.addAll(adminPermissionPermissionSeeds());
		seeds.addAll(businessConfigPermissionSeeds());
		seeds.addAll(industryGroupPermissionSeeds());
		seeds.addAll(industryPermissionSeeds());
		seeds.addAll(provincePermissionSeeds());
		seeds.addAll(educationLevelPermissionSeeds());

		Set<String> seedKeys = new HashSet<>();
		int created = 0;
		int updated = 0;
		int skipped = 0;

		for (PermissionSeed seed : seeds) {
			seedKeys.add(permissionKey(seed.path(), seed.method(), SEEDED_TYPE));
			AdminPermission permission = adminPermissionRepository
					.findByPathAndMethodAndType(seed.path(), seed.method(), SEEDED_TYPE)
					.orElse(null);

			if (permission == null) {
				if (adminPermissionRepository.existsByPathAndMethod(seed.path(), seed.method())) {
					skipped++;
					continue;
				}
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
			if (existing.getType() == SEEDED_TYPE
					&& MANAGED_MODULES.contains(existing.getModule())
					&& !seedKeys.contains(permissionKey(existing.getPath(), existing.getMethod(), existing.getType()))) {
				removedIds.add(existing.getId());
			}
		}

		if (!removedIds.isEmpty()) {
			adminPermissionRepository.deleteRoleLinksByPermissionIds(removedIds);
			adminPermissionRepository.hardDeleteByIds(removedIds);
		}

		log.info("Permissions synced — created={}, updated={}, removed={}, skipped={}",
				created, updated, removedIds.size(), skipped);
	}

	private List<PermissionSeed> adminUserPermissionSeeds() {
		return List.of(
				new PermissionSeed("VIEW_ADMIN_USERS", "Xem danh sách admin", MODULE_ADMIN_USER, "GET",
						Constants.ADMIN_USERS_BASE_PATH),
				new PermissionSeed("VIEW_ADMIN_USER", "Xem chi tiết admin", MODULE_ADMIN_USER, "GET",
						Constants.ADMIN_USERS_BASE_PATH + "/{id}"),
				new PermissionSeed("CREATE_ADMIN_USER", "Tạo admin", MODULE_ADMIN_USER, "POST",
						Constants.ADMIN_USERS_BASE_PATH),
				new PermissionSeed("UPDATE_ADMIN_USER", "Cập nhật admin", MODULE_ADMIN_USER, "PUT",
						Constants.ADMIN_USERS_BASE_PATH + "/{id}"),
				new PermissionSeed("DELETE_ADMIN_USER", "Xóa mềm admin", MODULE_ADMIN_USER, "DELETE",
						Constants.ADMIN_USERS_BASE_PATH + "/{id}"),
				new PermissionSeed("RESTORE_ADMIN_USER", "Khôi phục admin", MODULE_ADMIN_USER, "PATCH",
						Constants.ADMIN_USERS_BASE_PATH + "/{id}/restore"),
				new PermissionSeed("PERMANENT_DELETE_ADMIN_USER", "Xóa vĩnh viễn admin", MODULE_ADMIN_USER, "DELETE",
						Constants.ADMIN_USERS_BASE_PATH + "/{id}/permanent"));
	}

	private List<PermissionSeed> adminRolePermissionSeeds() {
		return List.of(
				new PermissionSeed("VIEW_ADMIN_ROLES", "Xem danh sách vai trò", MODULE_ADMIN_ROLE, "GET",
						Constants.ADMIN_ROLES_BASE_PATH),
				new PermissionSeed("VIEW_ADMIN_ROLE", "Xem chi tiết vai trò", MODULE_ADMIN_ROLE, "GET",
						Constants.ADMIN_ROLES_BASE_PATH + "/{id}"),
				new PermissionSeed("CREATE_ADMIN_ROLE", "Tạo vai trò", MODULE_ADMIN_ROLE, "POST",
						Constants.ADMIN_ROLES_BASE_PATH),
				new PermissionSeed("UPDATE_ADMIN_ROLE", "Cập nhật vai trò", MODULE_ADMIN_ROLE, "PUT",
						Constants.ADMIN_ROLES_BASE_PATH + "/{id}"),
				new PermissionSeed("DELETE_ADMIN_ROLE", "Xóa mềm vai trò", MODULE_ADMIN_ROLE, "DELETE",
						Constants.ADMIN_ROLES_BASE_PATH + "/{id}"),
				new PermissionSeed("RESTORE_ADMIN_ROLE", "Khôi phục vai trò", MODULE_ADMIN_ROLE, "PATCH",
						Constants.ADMIN_ROLES_BASE_PATH + "/{id}/restore"),
				new PermissionSeed("PERMANENT_DELETE_ADMIN_ROLE", "Xóa vĩnh viễn vai trò", MODULE_ADMIN_ROLE, "DELETE",
						Constants.ADMIN_ROLES_BASE_PATH + "/{id}/permanent"));
	}

	private List<PermissionSeed> adminPermissionPermissionSeeds() {
		return List.of(
				new PermissionSeed("VIEW_ADMIN_PERMISSIONS", "Xem danh sách quyền", MODULE_ADMIN_PERMISSION, "GET",
						Constants.ADMIN_PERMISSIONS_BASE_PATH),
				new PermissionSeed("VIEW_ADMIN_PERMISSION", "Xem chi tiết quyền", MODULE_ADMIN_PERMISSION, "GET",
						Constants.ADMIN_PERMISSIONS_BASE_PATH + "/{id}"),
				new PermissionSeed("CREATE_ADMIN_PERMISSION", "Tạo quyền", MODULE_ADMIN_PERMISSION, "POST",
						Constants.ADMIN_PERMISSIONS_BASE_PATH),
				new PermissionSeed("UPDATE_ADMIN_PERMISSION", "Cập nhật quyền", MODULE_ADMIN_PERMISSION, "PUT",
						Constants.ADMIN_PERMISSIONS_BASE_PATH + "/{id}"),
				new PermissionSeed("DELETE_ADMIN_PERMISSION", "Xóa mềm quyền", MODULE_ADMIN_PERMISSION, "DELETE",
						Constants.ADMIN_PERMISSIONS_BASE_PATH + "/{id}"),
				new PermissionSeed("RESTORE_ADMIN_PERMISSION", "Khôi phục quyền", MODULE_ADMIN_PERMISSION, "PATCH",
						Constants.ADMIN_PERMISSIONS_BASE_PATH + "/{id}/restore"),
				new PermissionSeed("PERMANENT_DELETE_ADMIN_PERMISSION", "Xóa vĩnh viễn quyền", MODULE_ADMIN_PERMISSION,
						"DELETE",
						Constants.ADMIN_PERMISSIONS_BASE_PATH + "/{id}/permanent"));
	}

	private List<PermissionSeed> businessConfigPermissionSeeds() {
		return List.of(
				new PermissionSeed("VIEW_BUSINESS_CONFIG", "Xem cấu hình doanh nghiệp", MODULE_BUSINESS_CONFIG, "GET",
						Constants.ADMIN_BUSINESS_CONFIG_BASE_PATH),
				new PermissionSeed("UPDATE_BUSINESS_CONFIG", "Cập nhật cấu hình doanh nghiệp", MODULE_BUSINESS_CONFIG,
						"PUT",
						Constants.ADMIN_BUSINESS_CONFIG_BASE_PATH));
	}

	private List<PermissionSeed> industryGroupPermissionSeeds() {
		return List.of(
				new PermissionSeed("VIEW_INDUSTRY_GROUPS", "Xem danh sách nhóm ngành nghề", MODULE_INDUSTRY_GROUP,
						"GET",
						Constants.ADMIN_INDUSTRY_GROUPS_BASE_PATH),
				new PermissionSeed("VIEW_INDUSTRY_GROUP", "Xem chi tiết nhóm ngành nghề", MODULE_INDUSTRY_GROUP, "GET",
						Constants.ADMIN_INDUSTRY_GROUPS_BASE_PATH + "/{id}"),
				new PermissionSeed("CREATE_INDUSTRY_GROUP", "Tạo nhóm ngành nghề", MODULE_INDUSTRY_GROUP, "POST",
						Constants.ADMIN_INDUSTRY_GROUPS_BASE_PATH),
				new PermissionSeed("UPDATE_INDUSTRY_GROUP", "Cập nhật nhóm ngành nghề", MODULE_INDUSTRY_GROUP, "PUT",
						Constants.ADMIN_INDUSTRY_GROUPS_BASE_PATH + "/{id}"),
				new PermissionSeed("DELETE_INDUSTRY_GROUP", "Xóa mềm nhóm ngành nghề", MODULE_INDUSTRY_GROUP, "DELETE",
						Constants.ADMIN_INDUSTRY_GROUPS_BASE_PATH + "/{id}"),
				new PermissionSeed("RESTORE_INDUSTRY_GROUP", "Khôi phục nhóm ngành nghề", MODULE_INDUSTRY_GROUP,
						"PATCH",
						Constants.ADMIN_INDUSTRY_GROUPS_BASE_PATH + "/{id}/restore"),
				new PermissionSeed("PERMANENT_DELETE_INDUSTRY_GROUP", "Xóa vĩnh viễn nhóm ngành nghề",
						MODULE_INDUSTRY_GROUP, "DELETE",
						Constants.ADMIN_INDUSTRY_GROUPS_BASE_PATH + "/{id}/permanent"));
	}

	private List<PermissionSeed> industryPermissionSeeds() {
		return List.of(
				new PermissionSeed("VIEW_INDUSTRIES", "Xem danh sách ngành nghề", MODULE_INDUSTRY, "GET",
						Constants.ADMIN_INDUSTRIES_BASE_PATH),
				new PermissionSeed("VIEW_INDUSTRY", "Xem chi tiết ngành nghề", MODULE_INDUSTRY, "GET",
						Constants.ADMIN_INDUSTRIES_BASE_PATH + "/{id}"),
				new PermissionSeed("CREATE_INDUSTRY", "Tạo ngành nghề", MODULE_INDUSTRY, "POST",
						Constants.ADMIN_INDUSTRIES_BASE_PATH),
				new PermissionSeed("UPDATE_INDUSTRY", "Cập nhật ngành nghề", MODULE_INDUSTRY, "PUT",
						Constants.ADMIN_INDUSTRIES_BASE_PATH + "/{id}"),
				new PermissionSeed("DELETE_INDUSTRY", "Xóa mềm ngành nghề", MODULE_INDUSTRY, "DELETE",
						Constants.ADMIN_INDUSTRIES_BASE_PATH + "/{id}"),
				new PermissionSeed("RESTORE_INDUSTRY", "Khôi phục ngành nghề", MODULE_INDUSTRY, "PATCH",
						Constants.ADMIN_INDUSTRIES_BASE_PATH + "/{id}/restore"),
				new PermissionSeed("PERMANENT_DELETE_INDUSTRY", "Xóa vĩnh viễn ngành nghề", MODULE_INDUSTRY, "DELETE",
						Constants.ADMIN_INDUSTRIES_BASE_PATH + "/{id}/permanent"));
	}

	private List<PermissionSeed> provincePermissionSeeds() {
		return List.of(
				new PermissionSeed("VIEW_PROVINCES", "Xem danh sách tỉnh thành phố", MODULE_PROVINCE, "GET",
						Constants.ADMIN_PROVINCES_BASE_PATH),
				new PermissionSeed("VIEW_PROVINCE", "Xem chi tiết tỉnh thành phố", MODULE_PROVINCE, "GET",
						Constants.ADMIN_PROVINCES_BASE_PATH + "/{id}"),
				new PermissionSeed("CREATE_PROVINCE", "Tạo tỉnh thành phố", MODULE_PROVINCE, "POST",
						Constants.ADMIN_PROVINCES_BASE_PATH),
				new PermissionSeed("UPDATE_PROVINCE", "Cập nhật tỉnh thành phố", MODULE_PROVINCE, "PUT",
						Constants.ADMIN_PROVINCES_BASE_PATH + "/{id}"),
				new PermissionSeed("DELETE_PROVINCE", "Xóa mềm tỉnh thành phố", MODULE_PROVINCE, "DELETE",
						Constants.ADMIN_PROVINCES_BASE_PATH + "/{id}"),
				new PermissionSeed("RESTORE_PROVINCE", "Khôi phục tỉnh thành phố", MODULE_PROVINCE, "PATCH",
						Constants.ADMIN_PROVINCES_BASE_PATH + "/{id}/restore"),
				new PermissionSeed("PERMANENT_DELETE_PROVINCE", "Xóa vĩnh viễn tỉnh thành phố", MODULE_PROVINCE,
						"DELETE",
						Constants.ADMIN_PROVINCES_BASE_PATH + "/{id}/permanent"));
	}

	private List<PermissionSeed> educationLevelPermissionSeeds() {
		return List.of(
				new PermissionSeed("VIEW_EDUCATION_LEVELS", "Xem danh sách trình độ", MODULE_EDUCATION_LEVEL, "GET",
						Constants.ADMIN_EDUCATION_LEVELS_BASE_PATH),
				new PermissionSeed("VIEW_EDUCATION_LEVEL", "Xem chi tiết trình độ", MODULE_EDUCATION_LEVEL, "GET",
						Constants.ADMIN_EDUCATION_LEVELS_BASE_PATH + "/{id}"),
				new PermissionSeed("CREATE_EDUCATION_LEVEL", "Tạo trình độ", MODULE_EDUCATION_LEVEL, "POST",
						Constants.ADMIN_EDUCATION_LEVELS_BASE_PATH),
				new PermissionSeed("UPDATE_EDUCATION_LEVEL", "Cập nhật trình độ", MODULE_EDUCATION_LEVEL, "PUT",
						Constants.ADMIN_EDUCATION_LEVELS_BASE_PATH + "/{id}"),
				new PermissionSeed("DELETE_EDUCATION_LEVEL", "Xóa mềm trình độ", MODULE_EDUCATION_LEVEL, "DELETE",
						Constants.ADMIN_EDUCATION_LEVELS_BASE_PATH + "/{id}"),
				new PermissionSeed("RESTORE_EDUCATION_LEVEL", "Khôi phục trình độ", MODULE_EDUCATION_LEVEL, "PATCH",
						Constants.ADMIN_EDUCATION_LEVELS_BASE_PATH + "/{id}/restore"),
				new PermissionSeed("PERMANENT_DELETE_EDUCATION_LEVEL", "Xóa vĩnh viễn trình độ", MODULE_EDUCATION_LEVEL,
						"DELETE",
						Constants.ADMIN_EDUCATION_LEVELS_BASE_PATH + "/{id}/permanent"));
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
		if (!Objects.equals(permission.getModule(), seed.module())) {
			permission.setModule(seed.module());
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
		if (permission.getType() != SEEDED_TYPE) {
			permission.setType(SEEDED_TYPE);
			changed = true;
		}
		if (!permission.isActive()) {
			permission.setActive(true);
			changed = true;
		}
		return changed;
	}

	private String permissionKey(String path, String method, PermissionTypeEnum type) {
		return method + " " + path + " " + type.name();
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

	private record PermissionSeed(String name, String description, String module, String method, String path) {
	}
}
