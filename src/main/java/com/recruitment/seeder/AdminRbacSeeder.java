package com.recruitment.seeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.recruitment.entity.AdminRole;
import com.recruitment.entity.AdminUser;
import com.recruitment.repository.AdminRoleRepository;
import com.recruitment.repository.AdminUserRepository;

@Component
@ConditionalOnProperty(name = "app.seed-admin", havingValue = "true")
public class AdminRbacSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminRbacSeeder.class);
	private static final String SUPER_ADMIN_ROLE = "Super Admin";
	private static final String DEFAULT_USERNAME = "admin";
	private static final String DEFAULT_PASSWORD = "admin";

	private final AdminRoleRepository adminRoleRepository;
	private final AdminUserRepository adminUserRepository;
	private final PasswordEncoder passwordEncoder;

	public AdminRbacSeeder(
			AdminRoleRepository adminRoleRepository,
			AdminUserRepository adminUserRepository,
			PasswordEncoder passwordEncoder) {
		this.adminRoleRepository = adminRoleRepository;
		this.adminUserRepository = adminUserRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		if (adminUserRepository.count() > 0) {
			log.info("Admin data already seeded — skipping");
			return;
		}

		AdminRole superAdminRole = seedSuperAdminRole();
		seedSuperAdminUser(superAdminRole);

		log.info("Seeded Super Admin role and account ({}/{})", DEFAULT_USERNAME, DEFAULT_PASSWORD);
	}

	private AdminRole seedSuperAdminRole() {
		AdminRole role = new AdminRole();
		role.setName(SUPER_ADMIN_ROLE);
		role.setDescription("Toàn quyền quản trị hệ thống");
		role.setFullAccess(true);
		role.setActive(true);
		return adminRoleRepository.save(role);
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
}
