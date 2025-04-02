package com.chatapp2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp2.dto.LoginRequest;
import com.chatapp2.dto.SignupRequest;
import com.chatapp2.security.CustomUserDetails;
import com.chatapp2.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
	private final UserService userService;

	@PostMapping("/signup")
	public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
		userService.registerUser(request.getUsername(), request.getDisplayName(), request.getEmail(),
				request.getPassword());
		return ResponseEntity.ok("User registered successfully");
	}

	@PostMapping("/login")
	public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
		userService.authenticateAndSetTokens(request.getEmail(), request.getPassword(), request.isRememberMe(),
				response);
		return ResponseEntity.ok("Login successful");
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletResponse response) {
		userService.invalidateToken(response);
		return ResponseEntity.ok("Logout successful");
	}

	@GetMapping("/check")
	public ResponseEntity<String> checkAuth(@AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.ok("Unauthenticated");
		}
		return ResponseEntity.ok("Authenticated");
	}

}
