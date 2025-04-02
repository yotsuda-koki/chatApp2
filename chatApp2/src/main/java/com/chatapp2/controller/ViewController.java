package com.chatapp2.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.chatapp2.service.ChatGroupService;
import com.chatapp2.service.GroupMembershipService;

@Controller
public class ViewController {
	private final ChatGroupService chatGroupService;
	private final GroupMembershipService groupMembershipService;

	public ViewController(ChatGroupService chatGroupService, GroupMembershipService groupMembershipService) {
		this.chatGroupService = chatGroupService;
		this.groupMembershipService = groupMembershipService;
	}

	@GetMapping("/login")
	public String showLoginPage() {
		return "login";
	}

	@GetMapping("/signup")
	public String showSignupPage() {
		return "signup";
	}

	@GetMapping("/home")
	public String showHomePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		if (!isAuthenticated(userDetails)) {
			return "redirect:/login";
		}
		return "home";
	}

	@GetMapping("/friends")
	public String showFriendsPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		if (!isAuthenticated(userDetails)) {
			return "redirect:/login";
		}
		return "friends";
	}

	@GetMapping("/direct-message")
	public String showDirectMessagePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		if (!isAuthenticated(userDetails)) {
			return "redirect:/login";
		}
		return "directMessage";
	}

	@GetMapping("/settings")
	public String showSettingsPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		if (!isAuthenticated(userDetails)) {
			return "redirect:/login";
		}
		return "settings";
	}

	@GetMapping("/groups")
	public String showGroupList(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		if (!isAuthenticated(userDetails)) {
			return "redirect:/login";
		}
		return "groups";
	}

	@GetMapping("/groups/{groupId}")
	public String getMethodName(@PathVariable Long groupId, @AuthenticationPrincipal UserDetails userDetails) {
		if (!isAuthenticated(userDetails)) {
			return "redirect:/login";
		}
		boolean isPublic = chatGroupService.getChatGroup(groupId).isPublic();
		if (isPublic) {
			return "chat";
		}
		String status = groupMembershipService.getGroupStatus(groupId, userDetails);
		if ("APPROVED".equals(status)) {
			return "chat";
		}
		if ("PENDING".equals(status)) {
			return "redirect:/groups?error=pending";
		} else if ("REJECTED".equals(status)) {
			return "redirect:/groups?error=rejected";
		} else {
			return "redirect:/groups?error=not_allowed";
		}
	}

	private boolean isAuthenticated(UserDetails userDetails) {
		if (userDetails == null) {
			return false;
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
	}

}