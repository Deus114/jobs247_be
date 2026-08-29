package com.recruitment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByToken(String token);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			UPDATE RefreshToken r
			SET r.revoked = true
			WHERE r.adminUser.username = :username
			AND r.revoked = false
			""")
	int revokeAllActiveByUsername(@Param("username") String username);
}
