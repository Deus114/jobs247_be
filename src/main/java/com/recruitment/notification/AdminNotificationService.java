package com.recruitment.notification;

import java.util.List;

import org.springframework.stereotype.Service;

import com.recruitment.entity.Company;
import com.recruitment.entity.Job;
import com.recruitment.enums.NotificationTypeEnum;
import com.recruitment.repository.AdminUserRepository;

@Service
public class AdminNotificationService {

	private final NotificationDispatchService notificationDispatchService;
	private final AdminUserRepository adminUserRepository;

	public AdminNotificationService(
			NotificationDispatchService notificationDispatchService,
			AdminUserRepository adminUserRepository) {
		this.notificationDispatchService = notificationDispatchService;
		this.adminUserRepository = adminUserRepository;
	}

	public void notifyCompanyPending(Company company, String actor) {
		notificationDispatchService.notifyAdmins(
				adminUserRepository.findByActiveTrue(),
				NotificationTypeEnum.ADMIN_COMPANY_PENDING,
				"notification.admin.company.pending.title",
				"notification.admin.company.pending.body",
				List.of(company.getName()),
				company.getId(),
				actor);
	}

	public void notifyJobPending(Job job, String actor) {
		notificationDispatchService.notifyAdmins(
				adminUserRepository.findByActiveTrue(),
				NotificationTypeEnum.ADMIN_JOB_PENDING,
				"notification.admin.job.pending.title",
				"notification.admin.job.pending.body",
				List.of(job.getTitle(), job.getCompany().getName()),
				job.getId(),
				actor);
	}
}
