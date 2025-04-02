package com.chatapp2.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.chatapp2.dto.GroupMessageRequest;
import com.chatapp2.service.GroupMessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class GroupChatWebSocketController {
	private final GroupMessageService groupMessageService;

	@MessageMapping("/group.sendMessage")
	public void sendDirectMessage(@Valid @Payload GroupMessageRequest request,
			SimpMessageHeaderAccessor headerAccessor) {
		groupMessageService.sendMessage(request, headerAccessor);
	}
}
