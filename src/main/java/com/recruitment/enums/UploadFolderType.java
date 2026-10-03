package com.recruitment.enums;

public enum UploadFolderType {

	ADMIN_AVATAR("admin-avatars"),
	USER_AVATAR("user-avatars"),
	LOGO("logos"),
	HOME_BANNER("home-banners"),
	USER_LOGIN_BACKGROUND("user-login-backgrounds"),
	ADMIN_LOGIN_BACKGROUND("admin-login-backgrounds"),
	USER_REGISTER_BANNER("user-register-banners"),
	INDUSTRY_GROUP("industry-groups"),
	INDUSTRY("industries"),
	COMPANY_LOGO("company-logos"),
	COMPANY_BACKGROUND("company-backgrounds");

	private final String folder;

	UploadFolderType(String folder) {
		this.folder = folder;
	}

	public String getFolder() {
		return folder;
	}
}
