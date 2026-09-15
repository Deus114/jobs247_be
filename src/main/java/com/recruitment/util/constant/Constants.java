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

	public static final String ADMIN_API_PATTERN = "/api/v1/admin/**";

	// --- Admin Auth Cookie ---

	public static final String ADMIN_REFRESH_TOKEN_COOKIE_NAME = "admin_refresh_token";
	public static final String ADMIN_REFRESH_TOKEN_COOKIE_PATH = ADMIN_AUTH_BASE_PATH;
	public static final int ADMIN_REFRESH_TOKEN_COOKIE_MAX_AGE = 259200;

	// --- Swagger ---

	public static final String SWAGGER_API_DOCS = "/v3/api-docs/**";
	public static final String SWAGGER_UI = "/swagger-ui/**";
	public static final String SWAGGER_UI_HTML = "/swagger-ui.html";
}
