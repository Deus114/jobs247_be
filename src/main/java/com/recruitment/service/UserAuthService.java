package com.recruitment.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.recruitment.dto.request.user.auth.RegisterRequest;
import com.recruitment.dto.request.user.auth.SendOtpRequest;
import com.recruitment.dto.request.user.auth.UpdateUserMeRequest;
import com.recruitment.dto.request.user.auth.UserLoginRequest;
import com.recruitment.dto.request.user.auth.UserRefreshRequest;
import com.recruitment.dto.request.user.auth.VerifyOtpRequest;
import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.dto.response.common.UploadedFileResponse;
import com.recruitment.dto.response.user.auth.RegisterResponse;
import com.recruitment.dto.response.user.auth.UserLoginResponse;
import com.recruitment.dto.response.user.auth.UserSessionResponse;
import com.recruitment.dto.response.user.auth.VerifyOtpResponse;
import com.recruitment.dto.response.user.company.UserCompanyMembershipResponse;
import com.recruitment.entity.EmailOtp;
import com.recruitment.entity.User;
import com.recruitment.entity.UserRefreshToken;
import com.recruitment.enums.EmailOtpPurposeEnum;
import com.recruitment.enums.EmailType;
import com.recruitment.enums.UploadFolderType;
import com.recruitment.enums.UserTypeEnum;
import com.recruitment.exception.AccountInactiveException;
import com.recruitment.exception.BusinessRuleException;
import com.recruitment.exception.DuplicateResourceException;
import com.recruitment.exception.InvalidTokenException;
import com.recruitment.mail.EmailPublisher;
import com.recruitment.mapper.CompanyMapper;
import com.recruitment.repository.CompanyMemberRepository;
import com.recruitment.repository.EmailOtpRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.security.JwtTokenProvider;
import com.recruitment.util.constant.Constants;
import com.recruitment.util.helper.FileHelper;
import com.recruitment.util.helper.RefreshCookieHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class UserAuthService {

	private static final int OTP_MAX_ATTEMPTS = 5;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final UserRepository userRepository;
	private final CompanyMemberRepository companyMemberRepository;
	private final EmailOtpRepository emailOtpRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailPublisher emailPublisher;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserRefreshTokenService userRefreshTokenService;
	private final CompanyMapper companyMapper;
	private final MessageService messageService;
	private final FileStorageService fileStorageService;
	private final RefreshCookieHelper refreshCookieHelper;
	private final Duration otpTtl;
	private final Duration otpResendCooldown;

	public UserAuthService(
			UserRepository userRepository,
			CompanyMemberRepository companyMemberRepository,
			EmailOtpRepository emailOtpRepository,
			PasswordEncoder passwordEncoder,
			EmailPublisher emailPublisher,
			JwtTokenProvider jwtTokenProvider,
			UserRefreshTokenService userRefreshTokenService,
			CompanyMapper companyMapper,
			MessageService messageService,
			FileStorageService fileStorageService,
			RefreshCookieHelper refreshCookieHelper,
			@Value("${app.auth.otp-expiration}")
			long otpExpirationMillis,
			@Value("${app.auth.otp-resend-cooldown}")
			long otpResendCooldownMillis) {
		this.userRepository = userRepository;
		this.companyMemberRepository = companyMemberRepository;
		this.emailOtpRepository = emailOtpRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailPublisher = emailPublisher;
		this.jwtTokenProvider = jwtTokenProvider;
		this.userRefreshTokenService = userRefreshTokenService;
		this.companyMapper = companyMapper;
		this.messageService = messageService;
		this.fileStorageService = fileStorageService;
		this.refreshCookieHelper = refreshCookieHelper;
		this.otpTtl = Duration.ofMillis(otpExpirationMillis);
		this.otpResendCooldown = Duration.ofMillis(otpResendCooldownMillis);
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> sendOtp(SendOtpRequest request) {
		String email = normalizeEmail(request.email());
		rejectIfEmailTaken(email);

		emailOtpRepository
				.findFirstByEmailAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
						email, EmailOtpPurposeEnum.REGISTER.name())
				.ifPresent(existing -> {
					if (existing.getCreatedAt().isAfter(Instant.now().minus(otpResendCooldown))) {
						throw new BusinessRuleException("error.auth.otp.resend-too-soon");
					}
					existing.setConsumedAt(Instant.now());
					emailOtpRepository.save(existing);
				});

		String code = generateOtpCode();
		EmailOtp otp = new EmailOtp();
		otp.setEmail(email);
		otp.setCodeHash(passwordEncoder.encode(code));
		otp.setPurpose(EmailOtpPurposeEnum.REGISTER.name());
		otp.setExpiresAt(Instant.now().plus(otpTtl));
		emailOtpRepository.save(otp);

		long minutes = Math.max(1, (otpTtl.toSeconds() + 59) / 60);
		emailPublisher.publish(
				EmailType.OTP_REGISTER.name(),
				email,
				LocaleContextHolder.getLocale().toLanguageTag(),
				Map.of("code", code, "minutes", Long.toString(minutes)));

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("auth.otp.send.success"),
				null));
	}

	@Transactional
	public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(VerifyOtpRequest request) {
		String email = normalizeEmail(request.email());
		EmailOtp otp = emailOtpRepository
				.findFirstByEmailAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
						email, EmailOtpPurposeEnum.REGISTER.name())
				.orElseThrow(() -> new BusinessRuleException("error.auth.otp.invalid"));

		if (otp.getExpiresAt().isBefore(Instant.now())) {
			throw new BusinessRuleException("error.auth.otp.expired");
		}
		if (otp.getAttemptCount() >= OTP_MAX_ATTEMPTS) {
			throw new BusinessRuleException("error.auth.otp.max-attempts");
		}

		if (!passwordEncoder.matches(request.code().trim(), otp.getCodeHash())) {
			otp.setAttemptCount(otp.getAttemptCount() + 1);
			emailOtpRepository.save(otp);
			throw new BusinessRuleException("error.auth.otp.invalid");
		}

		otp.setConsumedAt(Instant.now());
		emailOtpRepository.save(otp);

		String verificationToken = jwtTokenProvider.generateEmailVerificationToken(email);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("auth.otp.verify.success"),
				new VerifyOtpResponse(verificationToken)));
	}

	@Transactional
	public ResponseEntity<ApiResponse<RegisterResponse>> register(RegisterRequest request) {
		if (!request.password().equals(request.confirmPassword())) {
			throw new BusinessRuleException("error.auth.password.mismatch");
		}

		String email = jwtTokenProvider.requireRegisterVerifiedEmail(request.verificationToken());
		rejectIfEmailTaken(email);

		String type = UserTypeEnum.fromValue(request.type())
				.orElseThrow(() -> new BusinessRuleException("error.auth.user-type.invalid", request.type()))
				.name();

		User user = new User();
		user.setName(request.name().trim());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.password()));
		user.setType(type);
		user.setEmailVerified(true);
		user.setPhone(trimToNull(request.phone()));
		user.setActive(true);
		user.setCreatedBy(email);
		user.setUpdatedBy(email);

		User saved = userRepository.save(user);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
				HttpStatus.CREATED.value(),
				messageService.get("auth.register.success"),
				new RegisterResponse(
						saved.getId(),
						saved.getName(),
						saved.getEmail(),
						saved.getType(),
						saved.isEmailVerified(),
						saved.getAvatar(),
						saved.getPhone(),
						List.of(),
						saved.getCreatedAt(),
						saved.getUpdatedAt())));
	}

	@Transactional
	public ResponseEntity<ApiResponse<UserLoginResponse>> login(
			UserLoginRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		String email = normalizeEmail(request.email());
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new BadCredentialsException("error.unauthorized.invalid-credentials"));
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new BadCredentialsException("error.unauthorized.invalid-credentials");
		}
		requireActive(user);

		UserLoginResponse loginResponse = issueTokens(
				user,
				httpRequest.getHeader("User-Agent"),
				extractClientIp(httpRequest),
				httpResponse);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("auth.user.login.success"),
				loginResponse));
	}

	@Transactional
	public ResponseEntity<ApiResponse<UserLoginResponse>> refresh(
			UserRefreshRequest body,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		String rawRefreshToken = extractRefreshToken(body, httpRequest);
		if (rawRefreshToken == null) {
			throw new InvalidTokenException("error.refresh-token.missing");
		}

		String email = jwtTokenProvider.requireUserRefreshEmail(rawRefreshToken);
		UserRefreshToken storedToken = userRefreshTokenService.validateForRefresh(rawRefreshToken);
		User user = storedToken.getUser();
		if (user == null || !email.equals(user.getEmail())) {
			throw new InvalidTokenException("error.refresh-token.invalid");
		}
		requireActive(user);

		userRefreshTokenService.revoke(storedToken);
		UserLoginResponse loginResponse = issueTokens(
				user,
				storedToken.getDeviceInfo(),
				storedToken.getIpAddress(),
				httpResponse);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("auth.user.refresh.success"),
				loginResponse));
	}

	@Transactional
	public ResponseEntity<ApiResponse<Void>> logout(
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		String rawRefreshToken = extractRefreshTokenFromCookie(httpRequest);
		if (rawRefreshToken != null) {
			userRefreshTokenService.revokeSessionByRawToken(rawRefreshToken);
		}
		refreshCookieHelper.clear(
				httpResponse,
				Constants.USER_REFRESH_TOKEN_COOKIE_NAME,
				Constants.AUTH_BASE_PATH);
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("auth.user.logout.success"),
				null));
	}

	private void rejectIfEmailTaken(String email) {
		if (userRepository.countByEmailIncludingDeleted(email) > 0) {
			throw new DuplicateResourceException("error.auth.email.duplicate", email);
		}
	}

	private void requireActive(User user) {
		if (!user.isActive()) {
			throw new AccountInactiveException("error.auth.account.inactive");
		}
	}

	private UserLoginResponse issueTokens(
			User user,
			String deviceInfo,
			String ipAddress,
			HttpServletResponse httpResponse) {
		String accessToken = jwtTokenProvider.generateUserAccessToken(user);
		String refreshToken = jwtTokenProvider.generateUserRefreshToken(user);
		userRefreshTokenService.save(refreshToken, user, deviceInfo, ipAddress);
		refreshCookieHelper.set(
				httpResponse,
				Constants.USER_REFRESH_TOKEN_COOKIE_NAME,
				Constants.AUTH_BASE_PATH,
				refreshToken);
		return new UserLoginResponse(accessToken, toSession(user));
	}

	public ResponseEntity<ApiResponse<UserSessionResponse>> getMe(Jwt jwt) {
		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("common.success"),
				toSession(requireUser(jwt))));
	}

	@Transactional
	public ResponseEntity<ApiResponse<UserSessionResponse>> updateMe(Jwt jwt, UpdateUserMeRequest request) {
		User user = requireUser(jwt);

		if (StringUtils.hasText(request.name())) {
			user.setName(request.name().trim());
		}
		if (StringUtils.hasText(request.phone())) {
			user.setPhone(request.phone().trim());
		}
		applyPasswordChange(user, request.currentPassword(), request.newPassword());
		user.setUpdatedBy(user.getEmail());

		String oldAvatarUrl = user.getAvatar();
		MultipartFile avatar = request.avatar();
		if (FileHelper.hasFile(avatar)) {
			UploadedFileResponse uploaded = fileStorageService.uploadImage(avatar, UploadFolderType.USER_AVATAR);
			user.setAvatar(uploaded.url());
		}

		User saved = userRepository.save(user);
		if (FileHelper.hasFile(avatar) && oldAvatarUrl != null && !oldAvatarUrl.equals(saved.getAvatar())) {
			fileStorageService.deleteByPublicUrl(oldAvatarUrl);
		}

		return ResponseEntity.ok(ApiResponse.success(
				messageService.get("auth.me.update.success"),
				toSession(saved)));
	}

	private User requireUser(Jwt jwt) {
		if (jwt == null
				|| !JwtTokenProvider.USER_ACCOUNT.equals(jwt.getClaimAsString("account"))
				|| !JwtTokenProvider.USER_ACCESS_TYPE.equals(jwt.getClaimAsString("type"))) {
			throw new InvalidTokenException("error.unauthorized.invalid-token");
		}
		User user = userRepository.findByEmail(jwt.getSubject())
				.orElseThrow(() -> new InvalidTokenException("error.unauthorized.invalid-token"));
		requireActive(user);
		return user;
	}

	private void applyPasswordChange(User user, String currentPassword, String newPassword) {
		boolean changingPassword = StringUtils.hasText(newPassword);
		if (!changingPassword) {
			if (StringUtils.hasText(currentPassword)) {
				throw new BusinessRuleException("error.user.password.new-required");
			}
			return;
		}
		if (!StringUtils.hasText(currentPassword)) {
			throw new BusinessRuleException("error.user.password.current-required");
		}
		if (newPassword.trim().length() < 6 || newPassword.trim().length() > 100) {
			throw new BusinessRuleException("error.user.password.length");
		}
		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
			throw new BusinessRuleException("error.user.password.current-invalid");
		}
		user.setPassword(passwordEncoder.encode(newPassword.trim()));
	}

	private UserSessionResponse toSession(User user) {
		List<UserCompanyMembershipResponse> companies = UserTypeEnum.EMPLOYER.name().equals(user.getType())
				? companyMemberRepository.findByUserIdOrderByCreatedAtAsc(user.getId()).stream()
						.map(companyMapper::toUserMembershipResponse)
						.toList()
				: List.of();
		return new UserSessionResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getType(),
				user.isEmailVerified(),
				user.getAvatar(),
				user.getPhone(),
				user.isActive(),
				companies,
				user.getCreatedAt(),
				user.getUpdatedAt());
	}

	private String extractRefreshToken(UserRefreshRequest body, HttpServletRequest request) {
		String fromCookie = extractRefreshTokenFromCookie(request);
		if (fromCookie != null) {
			return fromCookie;
		}
		if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
			return body.refreshToken();
		}
		return null;
	}

	private String extractRefreshTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() == null) {
			return null;
		}
		return Arrays.stream(request.getCookies())
				.filter(cookie -> cookie != null
						&& Constants.USER_REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
				.map(cookie -> cookie.getValue())
				.findFirst()
				.orElse(null);
	}

	private String extractClientIp(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isBlank()) {
			return xForwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private static String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}

	private static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private static String generateOtpCode() {
		int value = SECURE_RANDOM.nextInt(1_000_000);
		return String.format("%06d", value);
	}
}
