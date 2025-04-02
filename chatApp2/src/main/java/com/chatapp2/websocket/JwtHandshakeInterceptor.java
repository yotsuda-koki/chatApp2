package com.chatapp2.websocket;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.chatapp2.model.User;
import com.chatapp2.security.JwtTokenProvider;
import com.chatapp2.service.UserService;
import com.chatapp2.util.HashingUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;
	private final HashingUtil hashingUtil;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {
		if (!(request instanceof ServletServerHttpRequest)) {
			return false;
		}
		HttpHeaders headers = ((ServletServerHttpRequest) request).getHeaders();
		List<String> cookies = headers.get(HttpHeaders.COOKIE);

		if (cookies == null || cookies.isEmpty()) {
			return false;
		}
		String token = null;
		for (String cookie : cookies) {
			for (String part : cookie.split(";")) {
				String[] kv = part.trim().split("=");
				if (kv.length == 2 && kv[0].equals("jwt")) {
					token = kv[1];
				}
			}
		}
		if (token == null || token.isEmpty()) {
			return false;
		}
		if (!jwtTokenProvider.validateToken(token)) {
			return false;
		}
		String username = jwtTokenProvider.getUsername(token);
		User user = userService.getUserByEmail(username);
		attributes.put("jwtToken", token);
		String hashedEmail = hashingUtil.hashWithUserSalt(user.getEmail(), user.getSalt());
		StompPrincipal principal = new StompPrincipal(hashedEmail);
		attributes.put("principal", principal);
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
	}
}
