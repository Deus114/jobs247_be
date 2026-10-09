package com.recruitment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.DeviceToken;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

	Optional<DeviceToken> findByToken(String token);

	List<DeviceToken> findByOwnerTypeAndOwnerId(String ownerType, Long ownerId);

	long deleteByToken(String token);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			DELETE FROM DeviceToken d
			WHERE d.token = :token AND d.ownerType = :ownerType AND d.ownerId = :ownerId
			""")
	int deleteByTokenAndOwner(
			@Param("token")
			String token,
			@Param("ownerType")
			String ownerType,
			@Param("ownerId")
			Long ownerId);
}
