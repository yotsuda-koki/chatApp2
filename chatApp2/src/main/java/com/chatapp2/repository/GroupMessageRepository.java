package com.chatapp2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chatapp2.model.ChatGroup;
import com.chatapp2.model.GroupMessage;

@Repository
public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {
	List<GroupMessage> findByGroupOrderByTimestampAsc(ChatGroup group);

	List<GroupMessage> findByGroup(ChatGroup group);
}
