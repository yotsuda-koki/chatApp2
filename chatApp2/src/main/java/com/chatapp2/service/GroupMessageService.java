package com.chatapp2.service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.chatapp2.dto.GroupMessageDTO;
import com.chatapp2.dto.GroupMessageRequest;
import com.chatapp2.model.ChatGroup;
import com.chatapp2.model.GroupMessage;
import com.chatapp2.model.User;
import com.chatapp2.repository.GroupMessageRepository;

import jakarta.transaction.Transactional;

@Service
public class GroupMessageService {
	private final GroupMessageRepository groupMessageRepository;
	private final ChatGroupService chatGroupService;
	private final UserService userService;
	private final SimpMessagingTemplate messagingTemplate;

	public GroupMessageService(GroupMessageRepository groupMessageRepository, ChatGroupService chatGroupService,
			UserService userService, SimpMessagingTemplate messagingTemplate) {
		this.groupMessageRepository = groupMessageRepository;
		this.chatGroupService = chatGroupService;
		this.userService = userService;
		this.messagingTemplate = messagingTemplate;
	}

	@Transactional
	public void sendMessage(GroupMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
		Long groupId = Optional.ofNullable(request.getGroupId())
				.orElseThrow(() -> new IllegalArgumentException("Group ID is required."));
		String content = Optional.ofNullable(request.getContent())
				.orElseThrow(() -> new IllegalArgumentException("Message content is required."));
		Principal senderId = Optional.ofNullable(headerAccessor.getUser())
				.orElseThrow(() -> new AccessDeniedException("User not authenticated."));
		User sender = userService.getUserByEmail(senderId.getName());
		GroupMessage message = new GroupMessage();
		message.setSender(sender);
		message.setGroup(chatGroupService.getGroupById(groupId));
		message.setContent(content);
		message.setTimestamp(LocalDateTime.now());
		GroupMessageDTO dto = new GroupMessageDTO(groupMessageRepository.save(message));
		messagingTemplate.convertAndSend("/topic/group/" + groupId, dto);
	}

	public List<GroupMessageDTO> getMessages(Long groupId) {
		ChatGroup group = chatGroupService.getGroupById(groupId);
		List<GroupMessage> messages = groupMessageRepository.findByGroup(group);
		List<GroupMessageDTO> dtos = messages.stream().map(GroupMessageDTO::new).toList();
		return dtos;
	}
}
