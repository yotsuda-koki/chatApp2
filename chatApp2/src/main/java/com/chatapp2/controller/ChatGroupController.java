package com.chatapp2.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp2.dto.ChatGroupDTO;
import com.chatapp2.dto.ChatGroupRequest;
import com.chatapp2.security.CustomUserDetails;
import com.chatapp2.service.ChatGroupService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/groups")
public class ChatGroupController {
	private final ChatGroupService chatGroupService;

	public ChatGroupController(ChatGroupService chatGroupService) {
		this.chatGroupService = chatGroupService;
	}

	@GetMapping("/")
	public ResponseEntity<List<ChatGroupDTO>> getGroups() {
		return ResponseEntity.ok(chatGroupService.getChatGroups());
	}

	@GetMapping("/{groupId}")
	public ResponseEntity<ChatGroupDTO> getGroup(@PathVariable Long groupId) {
		return ResponseEntity.ok(chatGroupService.getChatGroup(groupId));
	}

	@PostMapping("/create")
	public ResponseEntity<ChatGroupDTO> createGroup(@Valid @RequestBody ChatGroupRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails, BindingResult result) {
		ChatGroupDTO groupDTO = chatGroupService.createGroup(request, userDetails);
		return ResponseEntity.ok(groupDTO);
	}

	@DeleteMapping("/delete/{groupId}")
	public ResponseEntity<String> deleteGroup(@RequestBody Long groupId, Principal principal) {
		chatGroupService.deleteGroup(groupId, principal);
		return ResponseEntity.ok("Delete Group");
	}

}
