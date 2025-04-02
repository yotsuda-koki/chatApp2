package com.chatapp2.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.chatapp2.dto.GroupMembershipDTO;
import com.chatapp2.model.ChatGroup;
import com.chatapp2.model.GroupMembership;
import com.chatapp2.model.GroupMembership.GroupRole;
import com.chatapp2.model.GroupMembership.JoinRequestStatus;
import com.chatapp2.model.User;
import com.chatapp2.repository.GroupMembershipRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class GroupMembershipService {
	public final GroupMembershipRepository groupMembershipRepository;
	public final ChatGroupService chatGroupService;
	public final UserService userService;

	public GroupMembershipService(GroupMembershipRepository groupMembershipRepository,
			ChatGroupService chatGroupService, UserService userService) {
		this.groupMembershipRepository = groupMembershipRepository;
		this.chatGroupService = chatGroupService;
		this.userService = userService;
	}

	public GroupMembership getMembership(User user, ChatGroup group) {
		return groupMembershipRepository.findByUserAndGroup(user, group)
				.orElseThrow(() -> new EntityNotFoundException("Membership not found"));
	}

	public boolean existsMembership(User user, ChatGroup group) {
		return groupMembershipRepository.findByUserAndGroup(user, group).isPresent();
	}

	@Transactional
	public void joinRequestGroup(UserDetails userDetails, Long groupId) {
		User user = userService.getUserByEmail(userDetails.getUsername());
		ChatGroup group = chatGroupService.getGroupById(groupId);
		if (groupMembershipRepository.findByUserAndGroup(user, group).isPresent()) {
			throw new IllegalStateException("User is already a member of this group");
		}
		GroupMembership membership = new GroupMembership();
		membership.setUser(user);
		membership.setGroup(group);
		membership.setRequestStatus(JoinRequestStatus.PENDING);
		groupMembershipRepository.save(membership);
	}

	public String getGroupStatus(Long groupId, UserDetails userDetails) {
		User user = userService.getUserByEmail(userDetails.getUsername());
		ChatGroup group = chatGroupService.getGroupById(groupId);
		Optional<GroupMembership> membership = groupMembershipRepository.findByUserAndGroup(user, group);
		if (membership.isPresent()) {
			return membership.get().getRequestStatus().name();
		}
		return "NONE";
	}

	public List<GroupMembershipDTO> getMemberships(Long groupId) {
		ChatGroup group = chatGroupService.getGroupById(groupId);
		List<GroupMembership> memberships = groupMembershipRepository.findByGroup(group);
		List<GroupMembershipDTO> dtos = memberships.stream().map(membership -> new GroupMembershipDTO(membership))
				.toList();
		return dtos;

	}

	public String getGroupRole(Long groupId, UserDetails userDetails) {
		User user = userService.getUserByEmail(userDetails.getUsername());
		ChatGroup group = chatGroupService.getGroupById(groupId);
		return groupMembershipRepository.findByUserAndGroup(user, group).map(membership -> membership.getRole().name())
				.orElse(null);
	}

	@Transactional
	public void leaveGroup(User user, Long groupId) {
		ChatGroup group = chatGroupService.getGroupById(groupId);
		GroupMembership membership = groupMembershipRepository.findByUserAndGroup(user, group)
				.orElseThrow(() -> new IllegalArgumentException("User is not a member of this group"));
		groupMembershipRepository.delete(membership);
	}

	@Transactional
	public void approveRequest(Long requestId, UserDetails userDetails) {
		GroupMembership membership = groupMembershipRepository.findById(requestId)
				.orElseThrow(() -> new EntityNotFoundException("Membership not found"));
		User user = userService.getUserByEmail(userDetails.getUsername());
		GroupMembership adminMembership = groupMembershipRepository.findByUserAndGroup(user, membership.getGroup())
				.orElseThrow(() -> new AccessDeniedException("You are not an admin"));
		if (!adminMembership.getRole().equals(GroupRole.ADMIN)) {
			throw new AccessDeniedException("You are not an admin");
		}
		membership.setRole(GroupRole.MEMBER);
		membership.setRequestStatus(JoinRequestStatus.APPROVED);
		groupMembershipRepository.save(membership);
	}

	@Transactional
	public void rejectRequest(Long requestId, UserDetails userDetails) {
		GroupMembership membership = groupMembershipRepository.findById(requestId)
				.orElseThrow(() -> new EntityNotFoundException("Membership not found"));
		User user = userService.getUserByEmail(userDetails.getUsername());
		GroupMembership adminMembership = groupMembershipRepository.findByUserAndGroup(user, membership.getGroup())
				.orElseThrow(() -> new AccessDeniedException("You are not an admin"));
		if (!adminMembership.getRole().equals(GroupRole.ADMIN)) {
			throw new AccessDeniedException("You are not an admin");
		}
		membership.setRequestStatus(JoinRequestStatus.REJECTED);
		groupMembershipRepository.save(membership);
	}
}
