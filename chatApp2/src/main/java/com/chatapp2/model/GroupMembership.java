package com.chatapp2.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "group_memberships")
public class GroupMembership {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "group_id", nullable = false)
	private ChatGroup group;

	@Enumerated(EnumType.STRING)
	private GroupRole role;

	@Enumerated(EnumType.STRING)
	private JoinRequestStatus requestStatus;

	@CreationTimestamp
	private LocalDateTime joinedAt;

	public enum GroupRole {
		ADMIN, MEMBER;
	}

	public enum JoinRequestStatus {
		PENDING, APPROVED, REJECTED
	}
}
