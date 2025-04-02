package com.chatapp2.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatapp2.dto.FriendDTO;
import com.chatapp2.dto.UsernameRequest;
import com.chatapp2.model.Friendship;
import com.chatapp2.model.Friendship.FriendStatus;
import com.chatapp2.model.User;
import com.chatapp2.repository.FriendshipRepository;
import com.chatapp2.security.CustomUserDetails;
import com.chatapp2.websocket.WebSocketEventListener;

@Service
public class FriendshipService {
	private final FriendshipRepository friendshipRepository;
	private final UserService userService;
	private final WebSocketEventListener eventListener;

	public FriendshipService(FriendshipRepository friendshipRepository, UserService userService,
			WebSocketEventListener eventListener) {
		this.friendshipRepository = friendshipRepository;
		this.userService = userService;
		this.eventListener = eventListener;
	}

	@Transactional
	public List<FriendDTO> findFriends(CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		return friendshipRepository
				.findBySenderAndStatusOrReceiverAndStatus(user, FriendStatus.ACCEPTED, user, FriendStatus.ACCEPTED)
				.stream().map(friendship -> convertToFriendDTO(friendship, user)).toList();
	}

	@Transactional
	public String addFriendship(CustomUserDetails userDetails, UsernameRequest request) {
		User user = userDetails.getUser();
		User friend = userService.getUserByUsername(request.getUsername());

		if (user.equals(friend)) {
			return "Cannot add yourself as a friend";
		}

		Optional<Friendship> existingFriendship = friendshipRepository.findBySenderAndReceiver(user, friend)
				.or(() -> friendshipRepository.findBySenderAndReceiver(friend, user));

		if (existingFriendship.isPresent()) {
			Friendship friendship = existingFriendship.get();
			switch (friendship.getStatus()) {
			case ACCEPTED:
				return "Already";
			case PENDING:
				return "Pending";
			case BLOCKED:
				return "Blocked";
			case REJECTED:
				friendship.setStatus(FriendStatus.PENDING);
				friendshipRepository.save(friendship);
				return "Sent";
			default:
				return "Unknown";
			}
		} else {
			Friendship newFriendship = new Friendship(user, friend);
			newFriendship.setStatus(FriendStatus.PENDING);
			friendshipRepository.save(newFriendship);
			return "Sent";
		}
	}

	@Transactional
	public List<FriendDTO> findPendingFriendRequests(CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		return friendshipRepository.findBySenderOrReceiver(user, user).stream()
				.filter(friendship -> friendship.getStatus() == FriendStatus.PENDING)
				.map(friendship -> convertToFriendDTO(friendship, user)).toList();
	}

	@Transactional
	public List<FriendDTO> findReceivedRequest(CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		List<Friendship> friendships = friendshipRepository.findByReceiver(user);
		List<FriendDTO> dtos = friendships.stream().filter(friendship -> friendship.getStatus() == FriendStatus.PENDING)
				.map(this::convertSenderToFriendDTO).toList();
		return dtos;
	}

	@Transactional
	public String acceptFriendship(CustomUserDetails userDetails, Long friendId) {
		User user = userDetails.getUser();
		User friend = userService.getUserById(friendId);
		return friendshipRepository.findBySenderAndReceiver(friend, user)
				.filter(friendship -> friendship.getStatus() == FriendStatus.PENDING).map(friendship -> {
					friendship.setStatus(FriendStatus.ACCEPTED);
					friendshipRepository.save(friendship);
					return "Accepted";
				}).orElse("Already friends or no request found");
	}

	@Transactional
	public String rejectFrindship(CustomUserDetails userDetails, Long friendId) {
		User user = userDetails.getUser();
		User friend = userService.getUserById(friendId);
		return friendshipRepository.findBySenderAndReceiver(friend, user)
				.filter(friendship -> friendship.getStatus() == FriendStatus.PENDING).map(friendship -> {
					friendship.setStatus(FriendStatus.REJECTED);
					friendshipRepository.save(friendship);
					return "Rejected";
				}).orElse("No pending request found");
	}

	@Transactional
	public void removeFriendship(CustomUserDetails userDetails, Long friendId) {
		User user = userDetails.getUser();
		User friend = userService.getUserById(friendId);
		Friendship friendship = friendshipRepository.findBySenderAndReceiver(user, friend)
				.or(() -> friendshipRepository.findBySenderAndReceiver(friend, user))
				.orElseThrow(() -> new IllegalArgumentException("User is not your friend"));
		friendshipRepository.delete(friendship);
	}

	@Transactional
	public void blockFriendship(CustomUserDetails userDetails, Long friendId) {
		User user = userDetails.getUser();
		User friend = userService.getUserById(friendId);
		Friendship friendship = friendshipRepository.findBySenderAndReceiver(user, friend)
				.or(() -> friendshipRepository.findBySenderAndReceiver(friend, user))
				.orElseThrow(() -> new IllegalArgumentException("User is not your friend"));
		friendship.setStatus(FriendStatus.BLOCKED);
		friendshipRepository.save(friendship);
	}

	private FriendDTO convertSenderToFriendDTO(Friendship friendship) {
		User sender = friendship.getSender();
		String status = Optional.ofNullable(eventListener.getUserStatus(sender.getId())).orElse("OFFLINE");
		return FriendDTO.from(sender.getId(), sender.getUsername(), sender.getDisplayName(), sender.getEmail(), status,
				sender.getProfilePicture());
	}

	private FriendDTO convertToFriendDTO(Friendship friendship, User user) {
		User friend = getFriendFromFriendship(friendship, user);
		String status = Optional.ofNullable(eventListener.getUserStatus(friend.getId())).orElse("OFFLINE");
		return FriendDTO.from(friend.getId(), friend.getUsername(), friend.getDisplayName(), friend.getEmail(), status,
				friend.getProfilePicture());
	}

	private User getFriendFromFriendship(Friendship friendship, User currentUser) {
		return friendship.getSender().getId().equals(currentUser.getId()) ? friendship.getReceiver()
				: friendship.getSender();
	}

}
