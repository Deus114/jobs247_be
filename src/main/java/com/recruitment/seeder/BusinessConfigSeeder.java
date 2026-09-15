package com.recruitment.seeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.entity.BusinessConfig;
import com.recruitment.repository.BusinessConfigRepository;
import com.recruitment.util.constant.Constants;

@Component
@Order(2)
public class BusinessConfigSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(BusinessConfigSeeder.class);

	private final BusinessConfigRepository businessConfigRepository;

	public BusinessConfigSeeder(BusinessConfigRepository businessConfigRepository) {
		this.businessConfigRepository = businessConfigRepository;
	}

	@Override
	@Transactional
	public void run(String... args) {
		if (businessConfigRepository.existsById(Constants.BUSINESS_CONFIG_ID)) {
			log.info("Business config id={} already exists — skip seed", Constants.BUSINESS_CONFIG_ID);
			return;
		}

		BusinessConfig config = new BusinessConfig();
		config.setId(Constants.BUSINESS_CONFIG_ID);
		config.setSiteName("Jobs247");
		config.setSmtpAuth(true);
		config.setSmtpStartTls(true);
		businessConfigRepository.save(config);

		log.info("Seeded business config id={} with siteName=Jobs247", Constants.BUSINESS_CONFIG_ID);
	}
}
