package com.chatapp2.service;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.chatapp2.dto.ChatGroupDTO;
import com.chatapp2.dto.ChatGroupRequest;
import com.chatapp2.model.ChatGroup;
import com.chatapp2.model.GroupMembership;
import com.chatapp2.model.GroupMembership.GroupRole;
import com.chatapp2.model.GroupMembership.JoinRequestStatus;
import com.chatapp2.model.User;
import com.chatapp2.repository.ChatGroupRepository;
import com.chatapp2.repository.GroupMembershipRepository;
import com.chatapp2.security.CustomUserDetails;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ChatGroupService {
	private final ChatGroupRepository chatGroupRepository;
	private final GroupMembershipRepository groupMembershipRepository;
	private final UserService userService;

	public ChatGroupService(ChatGroupRepository chatGroupRepository,
			GroupMembershipRepository groupMembershipRepository, UserService userService) {
		this.chatGroupRepository = chatGroupRepository;
		this.groupMembershipRepository = groupMembershipRepository;
		this.userService = userService;
	}

	public ChatGroup getGroupById(Long groupId) {
		return chatGroupRepository.findById(groupId)
				.orElseThrow(() -> new EntityNotFoundException("ChatGroup with ID" + groupId + " not found"));
	}

	public List<ChatGroupDTO> getChatGroups() {
		List<ChatGroup> groups = chatGroupRepository.findAll();
		return groups.stream().map(ChatGroupDTO::new).collect(Collectors.toList());
	}

	public ChatGroupDTO getChatGroup(Long groupId) {
		ChatGroup group = chatGroupRepository.findById(groupId)
				.orElseThrow(() -> new EntityNotFoundException("Not found."));
		return new ChatGroupDTO(group);
	}

	public List<ChatGroupDTO> getGroupsForUser(Long userId) {
		List<ChatGroup> groups = chatGroupRepository.findAll();
		User user = userService.getUserById(userId);
		return groups.stream().filter(group -> groupMembershipRepository.findByUserAndGroup(user, group).isPresent())
				.map(ChatGroupDTO::new).collect(Collectors.toList());
	}

	@Transactional
	public ChatGroupDTO createGroup(ChatGroupRequest request, CustomUserDetails userDetails) {
		if (request.getName() == null || request.getName().isBlank()) {
			throw new IllegalArgumentException("Group name cannot be empty");
		}
		ChatGroup group = new ChatGroup();
		group.setName(request.getName());
		group.setOwner(userDetails.getUser());
		group.setPublic(request.isPublic());
		try {
			chatGroupRepository.save(group);
		} catch (DataIntegrityViolationException e) {
			throw new RuntimeException("Database constraint violation: " + e.getMessage());
		}
		if (!request.isPublic()) {
			GroupMembership membership = new GroupMembership();
			membership.setUser(userDetails.getUser());
			membership.setGroup(group);
			membership.setRole(GroupRole.ADMIN);
			membership.setRequestStatus(JoinRequestStatus.APPROVED);
			groupMembershipRepository.save(membership);
		}
		ChatGroupDTO groupDTO = new ChatGroupDTO(group);
		return groupDTO;
	}

	@Transactional
	public void deleteGroup(Long groupId, Principal principal) {
		if (!chatGroupRepository.existsById(groupId)) {
			throw new EntityNotFoundException("ChatGroup with ID " + groupId + " does not exist");
		}
		if (!principal.getName().equals(getGroupById(groupId).getOwner().getEmail())) {
			throw new AccessDeniedException("");
		}
		chatGroupRepository.deleteById(groupId);
	}
}