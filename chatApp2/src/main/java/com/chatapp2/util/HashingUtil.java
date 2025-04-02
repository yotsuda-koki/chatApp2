package com.chatapp2.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.chatapp2.model.User;
import com.chatapp2.service.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HashingUtil {
	private final UserService userService;

	public String hashWithUserSaltByPrincipal(Principal principal) {
		User user = userService.getUserByEmail(principal.getName());
		if (user == null) {
			throw new RuntimeException("User not found");
		}
		return hashWithUserSalt(user.getEmail(), user.getSalt());
	}

	public String hashWithUserSalt(String email, String salt) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest((email + salt).getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().encodeToString(hashBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorithm not found", e);
		}
	}
}
