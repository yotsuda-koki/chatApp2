package com.chatapp2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chatapp2.model.ChatGroup;

@Repository
public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
	List<ChatGroup> findByOwnerId(Long ownerId);
}
