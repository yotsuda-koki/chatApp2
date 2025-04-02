package com.chatapp2.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.chatapp2.dto.DirectMessageRequest;
import com.chatapp2.service.DirectMessageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DirectMessageWebSocketController {
	private final DirectMessageService directMessageService;

	@MessageMapping("/direct.sendMessage")
	public void sendDirectMessage(@Payload DirectMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
		directMessageService.sendDirectMessage(request, headerAccessor);
	}
}
