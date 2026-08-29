package com.recruitment.enums;

public enum UploadFolderType {

	ADMIN_AVATAR("admin-avatars");

	private final String folder;

	UploadFolderType(String folder) {
		this.folder = folder;
	}

	public String getFolder() {
		return folder;
	}
}
