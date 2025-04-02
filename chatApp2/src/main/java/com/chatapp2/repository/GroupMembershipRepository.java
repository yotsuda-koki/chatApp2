package com.chatapp2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chatapp2.model.ChatGroup;
import com.chatapp2.model.GroupMembership;
import com.chatapp2.model.User;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {
	List<GroupMembership> findByUser(User user);

	List<GroupMembership> findByGroup(ChatGroup group);

	Optional<GroupMembership> findByUserAndGroup(User user, ChatGroup group);
}
