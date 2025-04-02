package com.chatapp2.dto;

import java.time.LocalDateTime;

import com.chatapp2.model.GroupMembership;
import com.chatapp2.model.GroupMembership.GroupRole;
import com.chatapp2.model.GroupMembership.JoinRequestStatus;

import lombok.Getter;

@Getter
public class GroupMembershipDTO {
	private Long id;
	private String displayName;
	private String userName;
	private Long groupId;
	private GroupRole role;
	private JoinRequestStatus requestStatus;
	private LocalDateTime joinedAt;

	public GroupMembershipDTO(GroupMembership groupMembership) {
		this.id = groupMembership.getId();
		this.displayName = groupMembership.getUser().getDisplayName();
		this.userName = groupMembership.getUser().getUsername();
		this.groupId = groupMembership.getGroup().getId();
		this.role = groupMembership.getRole();
		this.requestStatus = groupMembership.getRequestStatus();
		this.joinedAt = groupMembership.getJoinedAt();
	}
}
