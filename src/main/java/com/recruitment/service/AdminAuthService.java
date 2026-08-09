package com.recruitment.service;

import com.recruitment.dto.request.LoginRequest;
import com.recruitment.dto.response.AdminUserResponse;
import com.recruitment.dto.response.LoginResponse;

public interface AdminAuthService {

	LoginResponse login(LoginRequest request, String deviceInfo, String ipAddress);

	LoginResponse refresh(String rawRefreshToken);

	void logout(String rawRefreshToken);

	AdminUserResponse getMe(String username);
}
