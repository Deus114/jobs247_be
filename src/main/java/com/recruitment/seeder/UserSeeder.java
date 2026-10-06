package com.recruitment.seeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.entity.User;
import com.recruitment.enums.UserTypeEnum;
import com.recruitment.repository.UserRepository;

@Component
@Order(3)
public class UserSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(UserSeeder.class);

	private static final String DEFAULT_PASSWORD = "123456";
	private static final String SEED_BY = "system";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(String... args) {
		seedUser(
				"employer@gmail.com",
				"Nhà tuyển dụng",
				UserTypeEnum.EMPLOYER);
		seedUser(
				"user@gmail.com",
				"Người dùng",
				UserTypeEnum.JOB_SEEKER);
	}

	private void seedUser(String email, String name, UserTypeEnum type) {
		if (userRepository.existsByEmail(email)) {
			log.info("User {} already exists — skip seed", email);
			return;
		}

		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
		user.setType(type.name());
		user.setEmailVerified(true);
		user.setActive(true);
		user.setCreatedBy(SEED_BY);
		user.setUpdatedBy(SEED_BY);
		userRepository.save(user);

		log.info("Seeded default {} account: {}", type.name(), email);
	}
}
