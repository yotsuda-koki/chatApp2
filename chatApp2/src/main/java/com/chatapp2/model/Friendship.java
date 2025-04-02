package com.chatapp2.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "friendships")
@Check(constraints = "user1_id <> user2_id")
public class Friendship {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver_id", nullable = false)
	private User receiver;

	@Enumerated(EnumType.STRING)
	private FriendStatus status;

	@CreationTimestamp
	private LocalDateTime createdAt;

	public enum FriendStatus {
		PENDING, ACCEPTED, REJECTED, BLOCKED
	}

	public Friendship() {
	}

	public Friendship(User sender, User receiver) {
		this.sender = sender;
		this.receiver = receiver;
		this.status = FriendStatus.PENDING;
	}

	public Friendship(User sender, User receiver, FriendStatus status) {
		this.sender = sender;
		this.receiver = receiver;
		this.status = status;
	}
}
