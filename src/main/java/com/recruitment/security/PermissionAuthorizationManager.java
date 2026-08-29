package com.recruitment.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import com.recruitment.entity.AdminPermission;
import com.recruitment.entity.AdminRole;
import com.recruitment.repository.AdminRoleRepository;

@Component
public class PermissionAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

	private final AdminRoleRepository adminRoleRepository;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	private volatile Map<String, List<AdminPermission>> rolePermissionsCache = Map.of();

	public PermissionAuthorizationManager(AdminRoleRepository adminRoleRepository) {
		this.adminRoleRepository = adminRoleRepository;
		loadCache();
	}

	public synchronized void loadCache() {
		List<AdminRole> roles = adminRoleRepository.findAllWithPermissions();
		Map<String, List<AdminPermission>> cache = new HashMap<>();
		for (AdminRole role : roles) {
			cache.computeIfAbsent("ROLE_" + role.getName(), key -> new ArrayList<>())
					.addAll(role.getPermissions());
		}
		this.rolePermissionsCache = cache;
	}

	@Override
	public AuthorizationDecision authorize(
			Supplier<? extends Authentication> authenticationSupplier,
			RequestAuthorizationContext context) {

		Authentication authentication = authenticationSupplier.get();
		if (authentication == null || !authentication.isAuthenticated()) {
			return new AuthorizationDecision(false);
		}

		if (hasFullAccess(authentication)) {
			return new AuthorizationDecision(true);
		}

		String requestPath = context.getRequest().getRequestURI();
		String httpMethod = context.getRequest().getMethod();

		for (String role : getUserRolesFromJwt(authentication)) {
			List<AdminPermission> permissions = rolePermissionsCache.getOrDefault(role, Collections.emptyList());
			for (AdminPermission permission : permissions) {
				if (permission.getMethod().equalsIgnoreCase(httpMethod)
						&& pathMatcher.match(permission.getPath(), requestPath)) {
					return new AuthorizationDecision(true);
				}
			}
		}

		return new AuthorizationDecision(false);
	}

	private boolean hasFullAccess(Authentication authentication) {
		if (authentication instanceof JwtAuthenticationToken jwtToken) {
			Boolean fullAccess = jwtToken.getToken().getClaim("fullAccess");
			return Boolean.TRUE.equals(fullAccess);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private List<String> getUserRolesFromJwt(Authentication authentication) {
		if (authentication instanceof JwtAuthenticationToken jwtToken) {
			Object rolesClaim = jwtToken.getToken().getClaim("roles");
			if (rolesClaim instanceof List<?> roles) {
				return (List<String>) roles;
			}
		}
		return Collections.emptyList();
	}
}
