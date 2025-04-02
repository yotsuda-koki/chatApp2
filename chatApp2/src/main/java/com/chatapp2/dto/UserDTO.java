package com.chatapp2.dto;

import java.util.Base64;

import com.chatapp2.model.User;

import lombok.Getter;

@Getter
public class UserDTO {
	private Long id;
	private String username;
	private String displayName;
	private String email;
	private String profilePicture;

	public UserDTO(User user) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.displayName = user.getDisplayName();
		this.email = user.getEmail();
		if (user.getProfilePicture() != null) {
			this.profilePicture = "data:image/png;base64,"
					+ Base64.getEncoder().encodeToString(user.getProfilePicture());
		} else {
			this.profilePicture = null;
		}
	}
}
