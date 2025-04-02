package com.chatapp2.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp2.dto.GroupMembershipDTO;
import com.chatapp2.model.User;
import com.chatapp2.service.GroupMembershipService;

@RestController
@RequestMapping("/api/member")
public class GroupMembershipController {
	private final GroupMembershipService groupMembershipService;

	public GroupMembershipController(GroupMembershipService groupMembershipService) {
		this.groupMembershipService = groupMembershipService;
	}

	@PostMapping("/{groupId}/request-join")
	public ResponseEntity<String> joinGroup(@PathVariable Long groupId,
			@AuthenticationPrincipal UserDetails userDetails) {
		groupMembershipService.joinRequestGroup(userDetails, groupId);
		return ResponseEntity.ok("Joined.");
	}

	@GetMapping("/{groupId}/status")
	public ResponseEntity<String> getGroupStatus(@PathVariable Long groupId,
			@AuthenticationPrincipal UserDetails userDetails) {
		String status = groupMembershipService.getGroupStatus(groupId, userDetails);
		return ResponseEntity.ok(status);
	}

	@GetMapping("/{groupId}/requests")
	public ResponseEntity<List<GroupMembershipDTO>> getRequests(@PathVariable Long groupId) {
		return ResponseEntity.ok(groupMembershipService.getMemberships(groupId));
	}

	@PostMapping("/{groupId}/leave")
	public ResponseEntity<String> leaveGroup(@PathVariable Long groupId, @AuthenticationPrincipal User user) {
		groupMembershipService.leaveGroup(user, groupId);
		return ResponseEntity.ok("Leave");
	}

	@GetMapping("/{groupId}/role")
	public ResponseEntity<String> getGroupRole(@PathVariable Long groupId,
			@AuthenticationPrincipal UserDetails userDetails) {
		return ResponseEntity.ok(groupMembershipService.getGroupRole(groupId, userDetails));
	}

	@PostMapping("/{requestId}/approve")
	public ResponseEntity<String> approveRequest(@PathVariable Long requestId,
			@AuthenticationPrincipal UserDetails userdeiDetails) {
		groupMembershipService.approveRequest(requestId, userdeiDetails);
		return ResponseEntity.ok("Approved");
	}

	@PostMapping("/{requestId}/reject")
	public ResponseEntity<String> rejectRequest(@PathVariable Long requestId,
			@AuthenticationPrincipal UserDetails userdeiDetails) {
		groupMembershipService.rejectRequest(requestId, userdeiDetails);
		return ResponseEntity.ok("Rejected");
	}

}
