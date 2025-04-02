package com.chatapp2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class ChatGroupRequest {
	@NotBlank
	@Size(min = 3, max = 25)
	private String name;
	private String iconUrl;
	private boolean isPublic;
}
