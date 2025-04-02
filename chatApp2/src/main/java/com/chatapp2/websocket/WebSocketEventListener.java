package com.chatapp2.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.chatapp2.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WebSocketEventListener {
	private final SimpMessagingTemplate messagingTemplate;

	public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	private final ObjectMapper objectMapper = new ObjectMapper();

	private static final ConcurrentHashMap<Long, String> userStatusMap = new ConcurrentHashMap<>(); // メモリ上でユーザーステータスを管理(DBの更新を避けるため)
	private static final ConcurrentHashMap<Long, Long> lastActiveTime = new ConcurrentHashMap<>();
	private static final long AWAY_THRESHOLD = 5 * 60 * 100;

	@EventListener
	public void handleWebSocketConnectListener(SessionConnectEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		Authentication auth = (Authentication) headerAccessor.getUser();
		CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
		Long userId = userDetails.getUserId();
		userStatusMap.put(userId, "ONLINE");
		lastActiveTime.put(userId, System.currentTimeMillis());
		sendUserStatusUpdate(userId, "ONLINE");
	}

	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		Authentication auth = (Authentication) headerAccessor.getUser();
		CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
		Long userId = userDetails.getUserId();
		userStatusMap.remove(userId);
		lastActiveTime.remove(userId);
		sendUserStatusUpdate(userId, "OFFLINE");
	}

	@Scheduled(fixedRate = 30 * 100)
	public void checkInactiveUsers() {
		long currentTime = System.currentTimeMillis();
		for (Map.Entry<Long, Long> entry : lastActiveTime.entrySet()) {
			Long userId = entry.getKey();
			long lastActive = entry.getValue();
			if (currentTime - lastActive > AWAY_THRESHOLD && "ONLINE".equals(userStatusMap.get(userId))) {
				userStatusMap.put(userId, "AWAY");
				sendUserStatusUpdate(userId, "AWAY");
			}
		}
	}

	public void updateUserActivity(Long userId) {
		if (userId == null) {
			return;
		}
		lastActiveTime.put(userId, System.currentTimeMillis());
		if (!"online".equals(userStatusMap.get(userId))) {
			userStatusMap.put(userId, "ONLINE");
			sendUserStatusUpdate(userId, "ONLINE");
		}
	}

	public String getUserStatus(Long userId) {
		return userStatusMap.get(userId);
	}

	private void sendUserStatusUpdate(Long userId, String status) {
		try {
			String message = objectMapper.writeValueAsString(Map.of("userId", userId, "status", status));
			messagingTemplate.convertAndSend("/topic/status", message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
