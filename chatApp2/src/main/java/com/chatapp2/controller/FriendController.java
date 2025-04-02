package com.chatapp2.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp2.dto.FriendDTO;
import com.chatapp2.dto.UsernameRequest;
import com.chatapp2.security.CustomUserDetails;
import com.chatapp2.service.FriendshipService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/friends")
public class FriendController {
	private final FriendshipService friendshipService;

	@GetMapping
	public ResponseEntity<List<FriendDTO>> getFriends(@AuthenticationPrincipal CustomUserDetails userDetails) {
		List<FriendDTO> friends = friendshipService.findFriends(userDetails);
		return ResponseEntity.ok(friends);
	}

	@PostMapping("/add")
	public ResponseEntity<String> addFriend(@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestBody UsernameRequest request) {
		return ResponseEntity.ok(friendshipService.addFriendship(userDetails, request));
	}

	@PostMapping("/accept/{friendId}")
	public ResponseEntity<String> acceptFriendRequest(@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long friendId) {
		return ResponseEntity.ok(friendshipService.acceptFriendship(userDetails, friendId));
	}

	@PostMapping("/reject/{friendId}")
	public ResponseEntity<String> rejectFriendRequest(@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long friendId) {
		return ResponseEntity.ok(friendshipService.rejectFrindship(userDetails, friendId));
	}

	@DeleteMapping("/remove/{friendId}")
	public ResponseEntity<String> removeFriend(@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long friendId) {
		friendshipService.removeFriendship(userDetails, friendId);
		return ResponseEntity.ok("Friend removed");
	}

	@PostMapping("/block/{friendId}")
	public ResponseEntity<String> blockFriend(@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long friendId) {
		friendshipService.blockFriendship(userDetails, friendId);
		return ResponseEntity.ok("Blocked");
	}

	@GetMapping("/requests")
	public ResponseEntity<List<FriendDTO>> getRequests(@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ResponseEntity.ok(friendshipService.findReceivedRequest(userDetails));
	}

}
