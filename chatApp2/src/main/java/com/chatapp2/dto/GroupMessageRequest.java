package com.chatapp2.dto;

import lombok.Getter;

@Getter
public class GroupMessageRequest {
	private Long groupId;
	private String content;
}
