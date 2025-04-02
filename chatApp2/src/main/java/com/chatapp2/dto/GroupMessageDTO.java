package com.chatapp2.dto;

import java.time.LocalDateTime;
import java.util.Base64;

import com.chatapp2.model.GroupMessage;

import lombok.Getter;

@Getter
public class GroupMessageDTO {
	private Long id;
	private Long senderId;
	private String senderDisplayName;
	private String profilePicture;
	private Long groupId;
	private String content;
	private LocalDateTime timestamp;

	public GroupMessageDTO(GroupMessage message) {
		this.id = message.getId();
		this.senderId = message.getSender().getId();
		this.senderDisplayName = message.getSender().getDisplayName();
		if (message.getSender().getProfilePicture() != null) {
			this.profilePicture = "data:image/png;base64,"
					+ Base64.getEncoder().encodeToString(message.getSender().getProfilePicture());
		} else {
			this.profilePicture = "/images/no-image.png";
		}
		this.groupId = message.getGroup().getId();
		this.content = message.getContent();
		this.timestamp = message.getTimestamp();
	}
}
