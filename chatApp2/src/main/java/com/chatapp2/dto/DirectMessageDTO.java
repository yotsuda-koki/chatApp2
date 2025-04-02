package com.chatapp2.dto;

import java.time.LocalDateTime;

import com.chatapp2.model.DirectMessage;

import lombok.Getter;

@Getter
public class DirectMessageDTO {
	private Long id;
	private Long senderId;
	private Long receiverId;
	private String content;
	private boolean read;
	private LocalDateTime timestamp;

	public DirectMessageDTO(DirectMessage message) {
		this.id = message.getId();
		this.senderId = message.getSender().getId();
		this.receiverId = message.getReceiver().getId();
		this.content = message.getContent();
		this.read = message.isRead();
		this.timestamp = message.getTimestamp();
	}
}
