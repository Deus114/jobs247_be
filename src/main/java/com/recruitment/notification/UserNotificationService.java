package com.recruitment.notification;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.recruitment.entity.Company;
import com.recruitment.entity.CompanyJoinRequest;
import com.recruitment.entity.CompanyMember;
import com.recruitment.entity.Job;
import com.recruitment.entity.JobApplication;
import com.recruitment.entity.User;
import com.recruitment.enums.CompanyMemberRoleEnum;
import com.recruitment.enums.CompanyStatusEnum;
import com.recruitment.enums.JobStatusEnum;
import com.recruitment.enums.NotificationTypeEnum;
import com.recruitment.repository.CompanyMemberRepository;

@Service
public class UserNotificationService {

	private final NotificationDispatchService notificationDispatchService;
	private final CompanyMemberRepository companyMemberRepository;

	public UserNotificationService(
			NotificationDispatchService notificationDispatchService,
			CompanyMemberRepository companyMemberRepository) {
		this.notificationDispatchService = notificationDispatchService;
		this.companyMemberRepository = companyMemberRepository;
	}

	public void notifyCompanyReviewed(Company company, String newStatus, String actor) {
		String titleKey;
		String bodyKey;
		List<String> args;
		if (CompanyStatusEnum.APPROVED.name().equals(newStatus)) {
			titleKey = "notification.employer.company.approved.title";
			bodyKey = "notification.employer.company.approved.body";
			args = List.of(company.getName());
		} else if (CompanyStatusEnum.REJECTED.name().equals(newStatus)) {
			titleKey = "notification.employer.company.rejected.title";
			if (StringUtils.hasText(company.getRejectionReason())) {
				bodyKey = "notification.employer.company.rejected.body-with-reason";
				args = List.of(company.getName(), company.getRejectionReason().trim());
			} else {
				bodyKey = "notification.employer.company.rejected.body";
				args = List.of(company.getName());
			}
		} else {
			return;
		}
		notificationDispatchService.notifyUsers(
				activeMembers(company.getId(), false),
				NotificationTypeEnum.EMPLOYER_COMPANY_STATUS_CHANGED,
				titleKey,
				bodyKey,
				args,
				company.getId(),
				actor);
	}

	public void notifyJobReviewed(Job job, String newStatus, String actor) {
		String titleKey;
		String bodyKey;
		List<String> args;
		if (JobStatusEnum.APPROVED.name().equals(newStatus)) {
			titleKey = "notification.employer.job.approved.title";
			bodyKey = "notification.employer.job.approved.body";
			args = List.of(job.getTitle());
		} else if (JobStatusEnum.REJECTED.name().equals(newStatus)) {
			titleKey = "notification.employer.job.rejected.title";
			if (StringUtils.hasText(job.getRejectionReason())) {
				bodyKey = "notification.employer.job.rejected.body-with-reason";
				args = List.of(job.getTitle(), job.getRejectionReason().trim());
			} else {
				bodyKey = "notification.employer.job.rejected.body";
				args = List.of(job.getTitle());
			}
		} else {
			return;
		}
		notificationDispatchService.notifyUsers(
				activeMembers(job.getCompany().getId(), false),
				NotificationTypeEnum.EMPLOYER_JOB_STATUS_CHANGED,
				titleKey,
				bodyKey,
				args,
				job.getId(),
				actor);
	}

	public void notifyJoinRequestCreated(CompanyJoinRequest joinRequest, String actor) {
		User applicant = joinRequest.getUser();
		notificationDispatchService.notifyUsers(
				activeMembers(joinRequest.getCompany().getId(), true),
				NotificationTypeEnum.EMPLOYER_JOIN_REQUEST_CREATED,
				"notification.employer.join-request.created.title",
				"notification.employer.join-request.created.body",
				List.of(applicant.getName(), joinRequest.getCompany().getName()),
				joinRequest.getId(),
				actor);
	}

	public void notifyApplicationReceived(JobApplication application, String actor) {
		Job job = application.getJob();
		notificationDispatchService.notifyUsers(
				activeMembers(job.getCompany().getId(), false),
				NotificationTypeEnum.EMPLOYER_APPLICATION_RECEIVED,
				"notification.employer.application.received.title",
				"notification.employer.application.received.body",
				List.of(application.getFullName(), job.getTitle()),
				application.getId(),
				actor);
	}

	public void notifyApplicationSubmitted(JobApplication application, String actor) {
		Job job = application.getJob();
		notificationDispatchService.notifyUser(
				application.getUser(),
				NotificationTypeEnum.JOB_SEEKER_APPLICATION_SUBMITTED,
				"notification.job-seeker.application.submitted.title",
				"notification.job-seeker.application.submitted.body",
				List.of(job.getTitle(), job.getCompany().getName()),
				application.getId(),
				actor);
	}

	public void notifyApplicationViewed(JobApplication application, String actor) {
		Job job = application.getJob();
		notificationDispatchService.notifyUser(
				application.getUser(),
				NotificationTypeEnum.JOB_SEEKER_APPLICATION_VIEWED,
				"notification.job-seeker.application.viewed.title",
				"notification.job-seeker.application.viewed.body",
				List.of(job.getTitle(), job.getCompany().getName()),
				application.getId(),
				actor);
	}

	private List<User> activeMembers(Long companyId, boolean ownersOnly) {
		Map<Long, User> users = new LinkedHashMap<>();
		for (CompanyMember member : companyMemberRepository.findByCompanyIdOrderByCreatedAtAsc(companyId)) {
			if (ownersOnly && !CompanyMemberRoleEnum.OWNER.name().equals(member.getRole())) {
				continue;
			}
			User user = member.getUser();
			if (user != null && user.isActive()) {
				users.putIfAbsent(user.getId(), user);
			}
		}
		return new ArrayList<>(users.values());
	}
}
