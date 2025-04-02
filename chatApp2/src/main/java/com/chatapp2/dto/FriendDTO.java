package com.chatapp2.dto;

import java.util.Base64;

import lombok.Getter;

@Getter
public class FriendDTO {
	private Long id;
	private String username;
	private String displayName;
	private String email;
	private String status;
	private String profilePicture;

	public FriendDTO(Long id, String username, String displayName, String email) {
		this.id = id;
		this.username = username;
		this.displayName = displayName;
		this.email = email;
	}

	public FriendDTO(Long id, String username, String displayName, String email, String status, String profilePicture) {
		this.id = id;
		this.username = username;
		this.displayName = displayName;
		this.email = email;
		this.status = status;
		this.profilePicture = profilePicture;
	}

	public static FriendDTO from(Long id, String username, String displayName, String email, String status,
			byte[] profilePicture) {
		String base64Image = null;
		if (profilePicture != null) {
			base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(profilePicture);
		} else {
			base64Image = "/images/no-image.png";
		}
		return new FriendDTO(id, username, displayName, email, status, base64Image);
	}

	public static FriendDTO from(Long id, String username, String displayName, String email) {
		return new FriendDTO(id, username, displayName, email);
	}

}
