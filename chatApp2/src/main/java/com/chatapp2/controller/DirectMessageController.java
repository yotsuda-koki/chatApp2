package com.chatapp2.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp2.dto.DirectMessageDTO;
import com.chatapp2.model.DirectMessage;
import com.chatapp2.model.User;
import com.chatapp2.service.DirectMessageService;
import com.chatapp2.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/direct")
public class DirectMessageController {
	private final DirectMessageService directMessageService;
	private final UserService userService;

	@GetMapping("/{receiverId}")
	public ResponseEntity<List<DirectMessageDTO>> getDMs(@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable Long receiverId) {
		User sender = userService.getUserByEmail(userDetails.getUsername());
		User receiver = userService.getUserById(receiverId);
		List<DirectMessage> messages = directMessageService.getDirectMessages(sender, receiver);
		List<DirectMessageDTO> dtos = messages.stream().map(DirectMessageDTO::new).collect(Collectors.toList());
		return ResponseEntity.ok(dtos);
	}

	@PostMapping("/{messageId}/read")
	public ResponseEntity<String> markMessageAsRead(@PathVariable Long messageId) {
		directMessageService.makeAsRead(messageId);
		return ResponseEntity.ok("メッセージを既読しました。");
	}
}
