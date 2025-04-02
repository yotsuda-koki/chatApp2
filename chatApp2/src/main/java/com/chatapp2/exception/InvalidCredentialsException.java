package com.chatapp2.exception;

public class InvalidCredentialsException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidCredentialsException() {
		super("Invalid EMAIL or PASSWORD");
	}
}
