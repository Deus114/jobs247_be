package com.recruitment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.UserRefreshToken;

@Repository
public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {

	Optional<UserRefreshToken> findByToken(String token);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			UPDATE UserRefreshToken t
			SET t.revoked = true
			WHERE t.user.email = :email
			AND t.revoked = false
			""")
	int revokeAllActiveByEmail(@Param("email") String email);
}
