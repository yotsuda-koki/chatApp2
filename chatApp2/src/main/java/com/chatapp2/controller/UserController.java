package com.chatapp2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp2.dto.UpdateUserRequest;
import com.chatapp2.dto.UserDTO;
import com.chatapp2.model.User;
import com.chatapp2.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/user")
public class UserController {
	private final UserService userService;

	@GetMapping("/me")
	public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
		}
		User user = userService.getUserByEmail(userDetails.getUsername());
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
		UserDTO userDTO = new UserDTO(user);
		return ResponseEntity.ok(userDTO);
	}

	@PostMapping("/update")
	public ResponseEntity<String> editUser(@AuthenticationPrincipal UserDetails userDetails,
			@RequestBody UpdateUserRequest updateUserRequest, HttpServletResponse response) {
		userService.updateUser(userDetails, updateUserRequest);
		userService.invalidateToken(response);
		return ResponseEntity.ok("Edited");
	}

}
