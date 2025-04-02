package com.chatapp2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class LoginRequest {
	@Email(message = "有効なメールアドレスを入力してください")
	private String email;
	@NotBlank(message = "パスワードは必須です")
	private String password;
	@NotNull
	private boolean rememberMe;
}
