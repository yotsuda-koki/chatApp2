package com.chatapp2.dto;

import java.time.LocalDateTime;

import com.chatapp2.model.ChatGroup;

import lombok.Getter;

@Getter
public class ChatGroupDTO {
	private Long id;
	private String name;
	private Long ownerId;
	private boolean isPublic;
	private LocalDateTime createdAt;

	public ChatGroupDTO(ChatGroup chatGroup) {
		this.id = chatGroup.getId();
		this.name = chatGroup.getName();
		this.ownerId = chatGroup.getOwner().getId();
		this.isPublic = chatGroup.isPublic();
		this.createdAt = chatGroup.getCreatedAt();
	}
}
