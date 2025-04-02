package com.chatapp2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chatapp2.model.DirectMessage;
import com.chatapp2.model.User;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {
	List<DirectMessage> findBySenderAndReceiverOrSenderAndReceiverOrderByTimestampAsc(User sender, User receiver,
			User receiver2, User sender2);
}
