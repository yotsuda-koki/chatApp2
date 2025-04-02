package com.chatapp2.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DirectMessageRequest {
	private Long receiverId;
	private String content;
	private LocalDateTime timestamp;
}
