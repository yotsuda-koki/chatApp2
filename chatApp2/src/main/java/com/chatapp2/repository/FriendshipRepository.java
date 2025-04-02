package com.chatapp2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chatapp2.model.Friendship;
import com.chatapp2.model.Friendship.FriendStatus;
import com.chatapp2.model.User;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
	List<Friendship> findBySenderOrReceiver(User sender, User receiver);

	Optional<Friendship> findBySenderAndReceiver(User sender, User receiver);

	List<Friendship> findBySenderAndStatusOrReceiverAndStatus(User sender, FriendStatus senderStatus, User receiver,
			FriendStatus receiverStatus);

	List<Friendship> findByReceiver(User receiver);
}
