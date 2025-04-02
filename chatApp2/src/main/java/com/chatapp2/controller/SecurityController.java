package com.chatapp2.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp2.util.HashingUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/security")
public class SecurityController {
	private final HashingUtil hashingUtil;

	@GetMapping("/hashed-identifier")
	public ResponseEntity<String> getHashedIdentifier(Principal principal) {
		String hashedEmail = hashingUtil.hashWithUserSaltByPrincipal(principal);
		return ResponseEntity.ok(hashedEmail);
	}
}
