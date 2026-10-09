package com.recruitment.entity;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifications", indexes = {
		@Index(name = "idx_notifications_recipient_created", columnList = "recipient_type, recipient_id, created_at"),
		@Index(name = "idx_notifications_recipient_read", columnList = "recipient_type, recipient_id, read_at")
})
public class Notification extends BaseEntity {

	@Column(name = "recipient_type", nullable = false, length = 20)
	private String recipientType;

	@Column(name = "recipient_id", nullable = false)
	private Long recipientId;

	@Column(nullable = false, length = 50)
	private String type;

	@Column(name = "title", nullable = false, length = 255)
	private String titleKey;

	@Column(name = "body", nullable = false, length = 1000)
	private String bodyKey;

	@Column(name = "body_args", length = 4000)
	private String bodyArgs;

	@Column(name = "related_id")
	private Long relatedId;

	@Column(name = "read_at")
	private Instant readAt;

	public String getRecipientType() {
		return recipientType;
	}

	public void setRecipientType(String recipientType) {
		this.recipientType = recipientType;
	}

	public Long getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(Long recipientId) {
		this.recipientId = recipientId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitleKey() {
		return titleKey;
	}

	public void setTitleKey(String titleKey) {
		this.titleKey = titleKey;
	}

	public String getBodyKey() {
		return bodyKey;
	}

	public void setBodyKey(String bodyKey) {
		this.bodyKey = bodyKey;
	}

	public String getBodyArgs() {
		return bodyArgs;
	}

	public void setBodyArgs(String bodyArgs) {
		this.bodyArgs = bodyArgs;
	}

	public Long getRelatedId() {
		return relatedId;
	}

	public void setRelatedId(Long relatedId) {
		this.relatedId = relatedId;
	}

	public Instant getReadAt() {
		return readAt;
	}

	public void setReadAt(Instant readAt) {
		this.readAt = readAt;
	}
}
