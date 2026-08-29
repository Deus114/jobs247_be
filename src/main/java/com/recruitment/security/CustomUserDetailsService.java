package com.recruitment.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.recruitment.entity.AdminUser;
import com.recruitment.repository.AdminUserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final AdminUserRepository adminUserRepository;

	public CustomUserDetailsService(AdminUserRepository adminUserRepository) {
		this.adminUserRepository = adminUserRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AdminUser adminUser = adminUserRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy admin với username: " + username));

		String roleAuthority = "ROLE_" + adminUser.getRole().getName();

		return User.builder()
				.username(adminUser.getUsername())
				.password(adminUser.getPassword())
				.authorities(new SimpleGrantedAuthority(roleAuthority))
				.disabled(!adminUser.isActive())
				.build();
	}
}
