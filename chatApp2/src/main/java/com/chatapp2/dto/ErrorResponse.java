package com.chatapp2.dto;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class ErrorResponse {
	private LocalDateTime timestamp;
	private int status;
	private String error;
	private String code;
	private String message;
	private String path;

	public ErrorResponse(int status, String error, String code, String message, String path) {
		this.timestamp = LocalDateTime.now();
		this.status = status;
		this.error = error;
		this.code = code;
		this.message = message;
		this.path = path;
	}

}
