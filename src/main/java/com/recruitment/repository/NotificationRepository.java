package com.recruitment.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.Notification;

@Repository
public interface NotificationRepository
		extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

	Optional<Notification> findByIdAndRecipientTypeAndRecipientId(
			Long id,
			String recipientType,
			Long recipientId);

	long countByRecipientTypeAndRecipientIdAndReadAtIsNull(String recipientType, Long recipientId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			UPDATE Notification n
			SET n.readAt = :readAt, n.updatedAt = :readAt, n.updatedBy = :updatedBy
			WHERE n.recipientType = :recipientType
				AND n.recipientId = :recipientId
				AND n.readAt IS NULL
			""")
	int markAllRead(
			@Param("recipientType")
			String recipientType,
			@Param("recipientId")
			Long recipientId,
			@Param("readAt")
			Instant readAt,
			@Param("updatedBy")
			String updatedBy);
}
