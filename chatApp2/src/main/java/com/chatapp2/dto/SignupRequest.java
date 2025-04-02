package com.chatapp2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class SignupRequest {
	@NotBlank(message = "IDは必須です")
	private String username;
	@NotBlank(message = "ユーザーネームは必須です")
	private String displayName;
	@Email(message = "有効なメールアドレスを入力してください")
	private String email;
	@NotBlank(message = "パスワードは必須です")
	private String password;
}
