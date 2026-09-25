package com.recruitment.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.recruitment.dto.response.common.ApiResponse;
import com.recruitment.security.PermissionAuthorizationManager;
import com.recruitment.service.MessageService;
import com.recruitment.util.constant.Constants;

import jakarta.servlet.http.HttpServletRequest;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationConverter jwtAuthenticationConverter;
	private final PermissionAuthorizationManager permissionAuthorizationManager;
	private final MessageService messageService;
	private final JsonMapper jsonMapper;

	@Value("${cors.allowed-origins}")
	private String corsAllowedOrigins;

	public SecurityConfig(
			JwtAuthenticationConverter jwtAuthenticationConverter,
			PermissionAuthorizationManager permissionAuthorizationManager,
			MessageService messageService,
			JsonMapper jsonMapper) {
		this.jwtAuthenticationConverter = jwtAuthenticationConverter;
		this.permissionAuthorizationManager = permissionAuthorizationManager;
		this.messageService = messageService;
		this.jsonMapper = jsonMapper;
	}

	private static final String[] WHITELIST = {
			Constants.SWAGGER_API_DOCS,
			Constants.SWAGGER_UI,
			Constants.SWAGGER_UI_HTML,
			Constants.ADMIN_AUTH_LOGIN_PATH,
			Constants.ADMIN_AUTH_REFRESH_PATH,
			Constants.AUTH_OTP_SEND_PATH,
			Constants.AUTH_OTP_VERIFY_PATH,
			Constants.AUTH_REGISTER_PATH,
			Constants.AUTH_LOGIN_PATH,
			Constants.AUTH_REFRESH_PATH,
			Constants.BUSINESS_CONFIG_PUBLIC_PATH,
			Constants.INDUSTRY_GROUPS_PUBLIC_PATH,
			Constants.PROVINCES_PUBLIC_PATH,
			Constants.EDUCATION_LEVELS_PUBLIC_PATH
	};

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		List<String> origins = Arrays.stream(corsAllowedOrigins.split(","))
				.map(origin -> origin == null ? "" : origin.trim())
				.filter(origin -> !origin.isEmpty())
				.toList();

		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(origins);
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList(
				"Authorization", "Content-Type", "Cache-Control", "Accept-Language"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(WHITELIST).permitAll()
						.requestMatchers(
								Constants.ADMIN_USERS_ME_PATH,
								Constants.ADMIN_AUTH_LOGOUT_PATH,
								Constants.AUTH_LOGOUT_PATH)
						.authenticated()
						.requestMatchers(Constants.ADMIN_API_PATTERN).access(permissionAuthorizationManager)
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2
						.bearerTokenResolver(bearerTokenResolver())
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(authenticationEntryPoint())
						.accessDeniedHandler(accessDeniedHandler()));

		return http.build();
	}

	@Bean
	BearerTokenResolver bearerTokenResolver() {
		DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();
		return request -> {
			if (isPublicAuthPath(request)) {
				return null;
			}
			return delegate.resolve(request);
		};
	}

	private boolean isPublicAuthPath(HttpServletRequest request) {
		String path = request.getRequestURI();
		return Constants.ADMIN_AUTH_LOGIN_PATH.equals(path)
				|| Constants.ADMIN_AUTH_REFRESH_PATH.equals(path)
				|| Constants.AUTH_OTP_SEND_PATH.equals(path)
				|| Constants.AUTH_OTP_VERIFY_PATH.equals(path)
				|| Constants.AUTH_REGISTER_PATH.equals(path)
				|| Constants.AUTH_LOGIN_PATH.equals(path)
				|| Constants.AUTH_REFRESH_PATH.equals(path)
				|| Constants.BUSINESS_CONFIG_PUBLIC_PATH.equals(path)
				|| Constants.INDUSTRY_GROUPS_PUBLIC_PATH.equals(path)
				|| Constants.PROVINCES_PUBLIC_PATH.equals(path)
				|| Constants.EDUCATION_LEVELS_PUBLIC_PATH.equals(path);
	}

	@Bean
	AuthenticationEntryPoint authenticationEntryPoint() {
		return (request, response, authException) -> {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding("UTF-8");

			ApiResponse<Void> body = ApiResponse.unauthorized(
					messageService.get("error.unauthorized.invalid-token"));
			response.getWriter().write(jsonMapper.writeValueAsString(body));
		};
	}

	@Bean
	AccessDeniedHandler accessDeniedHandler() {
		return (request, response, accessDeniedException) -> {
			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding("UTF-8");

			ApiResponse<Void> body = ApiResponse.forbidden(
					messageService.get("error.forbidden.access-denied"));
			response.getWriter().write(jsonMapper.writeValueAsString(body));
		};
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
