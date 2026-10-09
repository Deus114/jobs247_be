package com.recruitment.util.constant;

public final class Constants {

	// --- Admin Auth API ---

	public static final String ADMIN_AUTH_BASE_PATH = "/api/v1/admin/auth";
	public static final String ADMIN_AUTH_LOGIN_PATH = ADMIN_AUTH_BASE_PATH + "/login";
	public static final String ADMIN_AUTH_REFRESH_PATH = ADMIN_AUTH_BASE_PATH + "/refresh";
	public static final String ADMIN_AUTH_LOGOUT_PATH = ADMIN_AUTH_BASE_PATH + "/logout";

	// --- Admin Users API ---

	public static final String ADMIN_USERS_BASE_PATH = "/api/v1/admin/users";
	public static final String ADMIN_USERS_ME_PATH = ADMIN_USERS_BASE_PATH + "/me";

	// --- Admin Roles API ---

	public static final String ADMIN_ROLES_BASE_PATH = "/api/v1/admin/roles";

	// --- Admin Permissions API ---

	public static final String ADMIN_PERMISSIONS_BASE_PATH = "/api/v1/admin/permissions";

	// --- Business Config API ---

	public static final String BUSINESS_CONFIG_PUBLIC_PATH = "/api/v1/business-config";
	public static final String ADMIN_BUSINESS_CONFIG_BASE_PATH = "/api/v1/admin/business-config";
	public static final long BUSINESS_CONFIG_ID = 1L;

	// --- Industry Group / Industry API ---

	public static final String INDUSTRY_GROUPS_PUBLIC_PATH = "/api/v1/industry-groups";
	public static final String ADMIN_INDUSTRY_GROUPS_BASE_PATH = "/api/v1/admin/industry-groups";
	public static final String ADMIN_INDUSTRIES_BASE_PATH = "/api/v1/admin/industries";

	// --- Province API ---

	public static final String PROVINCES_PUBLIC_PATH = "/api/v1/provinces";
	public static final String ADMIN_PROVINCES_BASE_PATH = "/api/v1/admin/provinces";

	// --- Education Level API ---

	public static final String EDUCATION_LEVELS_PUBLIC_PATH = "/api/v1/education-levels";
	public static final String ADMIN_EDUCATION_LEVELS_BASE_PATH = "/api/v1/admin/education-levels";

	// --- Account (end user) API ---

	public static final String ADMIN_ACCOUNTS_BASE_PATH = "/api/v1/admin/accounts";

	// --- Admin Company API ---

	public static final String ADMIN_COMPANIES_BASE_PATH = "/api/v1/admin/companies";

	// --- Admin Job API ---

	public static final String ADMIN_JOBS_BASE_PATH = "/api/v1/admin/jobs";
	public static final String ADMIN_DASHBOARD_PATH = "/api/v1/admin/dashboard";

	// --- User Auth API ---

	public static final String AUTH_BASE_PATH = "/api/v1/auth";
	public static final String AUTH_OTP_SEND_PATH = AUTH_BASE_PATH + "/otp/send";
	public static final String AUTH_OTP_VERIFY_PATH = AUTH_BASE_PATH + "/otp/verify";
	public static final String AUTH_REGISTER_PATH = AUTH_BASE_PATH + "/register";
	public static final String AUTH_LOGIN_PATH = AUTH_BASE_PATH + "/login";
	public static final String AUTH_REFRESH_PATH = AUTH_BASE_PATH + "/refresh";
	public static final String AUTH_LOGOUT_PATH = AUTH_BASE_PATH + "/logout";
	public static final String AUTH_ME_PATH = AUTH_BASE_PATH + "/me";

	// --- Public catalog ---

	public static final String PUBLIC_JOBS_BASE_PATH = "/api/v1/jobs";
	public static final String PUBLIC_COMPANIES_BASE_PATH = "/api/v1/companies";

	// --- Employer / job seeker API ---

	public static final String EMPLOYER_API_BASE_PATH = "/api/v1/employer";
	public static final String JOB_SEEKER_API_BASE_PATH = "/api/v1/job-seeker";
	public static final String EMPLOYER_COMPANIES_BASE_PATH = EMPLOYER_API_BASE_PATH + "/companies";
	public static final String EMPLOYER_JOBS_BASE_PATH = EMPLOYER_API_BASE_PATH + "/jobs";
	public static final String EMPLOYER_DASHBOARD_PATH = EMPLOYER_API_BASE_PATH + "/dashboard";
	public static final String EMPLOYER_APPLICATIONS_BASE_PATH = EMPLOYER_API_BASE_PATH + "/applications";
	public static final String EMPLOYER_JOIN_REQUESTS_BASE_PATH = EMPLOYER_API_BASE_PATH + "/join-requests";
	public static final String JOB_SEEKER_JOB_APPLICATIONS_BASE_PATH = JOB_SEEKER_API_BASE_PATH
			+ "/jobs/{jobId}/applications";
	public static final String JOB_SEEKER_APPLICATIONS_BASE_PATH = JOB_SEEKER_API_BASE_PATH + "/applications";
	public static final String JOB_SEEKER_SAVED_JOBS_BASE_PATH = JOB_SEEKER_API_BASE_PATH + "/saved-jobs";
	public static final String ADMIN_APPLICATIONS_BASE_PATH = "/api/v1/admin/applications";

	public static final String USER_NOTIFICATIONS_BASE_PATH = "/api/v1/notifications";
	public static final String USER_DEVICE_TOKENS_BASE_PATH = "/api/v1/device-tokens";
	public static final String ADMIN_NOTIFICATIONS_BASE_PATH = "/api/v1/admin/notifications";
	public static final String ADMIN_DEVICE_TOKENS_BASE_PATH = "/api/v1/admin/device-tokens";

	public static final int EMPLOYER_MAX_COMPANIES = 3;

	public static final String ADMIN_API_PATTERN = "/api/v1/admin/**";

	// --- Auth Cookie ---

	public static final String ADMIN_REFRESH_TOKEN_COOKIE_NAME = "admin_refresh_token";
	public static final String USER_REFRESH_TOKEN_COOKIE_NAME = "user_refresh_token";

	// --- Swagger ---

	public static final String SWAGGER_API_DOCS = "/v3/api-docs/**";
	public static final String SWAGGER_UI = "/swagger-ui/**";
	public static final String SWAGGER_UI_HTML = "/swagger-ui.html";
}
