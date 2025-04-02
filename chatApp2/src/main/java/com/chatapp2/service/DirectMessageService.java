package com.chatapp2.service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatapp2.dto.DirectMessageDTO;
import com.chatapp2.dto.DirectMessageRequest;
import com.chatapp2.model.DirectMessage;
import com.chatapp2.model.User;
import com.chatapp2.repository.DirectMessageRepository;
import com.chatapp2.util.HashingUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectMessageService {
	private final DirectMessageRepository directMessageRepository;
	private final UserService userService;
	private final SimpMessagingTemplate messagingTemplate;
	private final HashingUtil hashingUtil;

	@Transactional
	public void sendDirectMessage(DirectMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
		Principal user = headerAccessor.getUser();
		if (user == null) {
			throw new AccessDeniedException("User not authenticated");
		}
		User sender = userService.getUserByEmail(user.getName());
		User receiver = userService.getUserById(request.getReceiverId());
		DirectMessage message = new DirectMessage();
		message.setSender(sender);
		message.setReceiver(receiver);
		message.setContent(request.getContent());
		message.setRead(false);
		message.setTimestamp(LocalDateTime.now());
		directMessageRepository.save(message);
		String hashedEmail = hashingUtil.hashWithUserSalt(receiver.getEmail(), receiver.getSalt());
		DirectMessageDTO dto = new DirectMessageDTO(message);
		messagingTemplate.convertAndSendToUser(hashedEmail, "/topic/directMessages", dto);
	}

	public List<DirectMessage> getDirectMessages(User sender, User receiver) {
		return directMessageRepository.findBySenderAndReceiverOrSenderAndReceiverOrderByTimestampAsc(sender, receiver,
				receiver, sender);
	}

	@Transactional
	public void makeAsRead(Long messageId) {
		DirectMessage message = directMessageRepository.findById(messageId)
				.orElseThrow(() -> new EntityNotFoundException("Message not found"));
		if (!message.isRead()) {
			message.setRead(true);
			directMessageRepository.save(message);
			messagingTemplate.convertAndSendToUser(message.getReceiver().getId().toString(), "/queue/readReceipts",
					Map.of("type", "readReceipts", "messageId", messageId));
		}
	}

}
