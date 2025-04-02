package com.chatapp2.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp2.dto.GroupMessageDTO;
import com.chatapp2.service.GroupMessageService;

@RestController
@RequestMapping("/api/chat")
public class GroupMessageController {
	private final GroupMessageService groupMessageService;

	public GroupMessageController(GroupMessageService groupMessageService) {
		this.groupMessageService = groupMessageService;
	}

	@GetMapping("/{groupId}")
	public ResponseEntity<List<GroupMessageDTO>> getMessages(@PathVariable Long groupId) {
		return ResponseEntity.ok(groupMessageService.getMessages(groupId));
	}
}
