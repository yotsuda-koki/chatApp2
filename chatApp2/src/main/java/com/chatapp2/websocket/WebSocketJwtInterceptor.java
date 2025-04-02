package com.chatapp2.websocket;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.chatapp2.security.JwtTokenProvider;
import com.chatapp2.service.UserDetailsServiceImpl;

@Component
public class WebSocketJwtInterceptor implements ChannelInterceptor {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserDetailsServiceImpl userDetailsService;

	public WebSocketJwtInterceptor(JwtTokenProvider jwtTokenProvider, UserDetailsServiceImpl userDetailsService) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.userDetailsService = userDetailsService;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (accessor.getCommand() == StompCommand.CONNECT || accessor.getCommand() == StompCommand.SEND) {
			List<String> authorizationHeaders = accessor.getNativeHeader("Authorization");

			if (authorizationHeaders != null && !authorizationHeaders.isEmpty()) {
				String token = authorizationHeaders.get(0).replace("Bearer ", "");

				if (jwtTokenProvider.validateToken(token)) {
					String username = jwtTokenProvider.getUsername(token);
					UserDetails userDetails = userDetailsService.loadUserByUsername(username);

					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(authentication);

					accessor.setUser(authentication);
				}
			}
		}
		return message;
	}
}
