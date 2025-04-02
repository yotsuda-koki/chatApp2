package com.chatapp2.dto;

import lombok.Getter;

@Getter
public class UpdateUserRequest {
	private String username;
	private String displayName;
	private String email;
	private String currentPassword;
	private String newPassword;
	private String profilePicture;
}
