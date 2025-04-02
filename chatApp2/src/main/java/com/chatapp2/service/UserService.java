package com.chatapp2.service;

import java.util.Base64;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatapp2.dto.UpdateUserRequest;
import com.chatapp2.exception.InvalidCredentialsException;
import com.chatapp2.model.User;
import com.chatapp2.repository.UserRepository;
import com.chatapp2.security.JwtTokenProvider;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final BCryptPasswordEncoder passwordEncoder;
	private final RefreshTokenService refreshTokenService;

	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("User with ID " + email + " not found"));
	}

	public User getUserById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));
	}

	public User getUserByUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException("User with Username " + username + " not found"));
	}

	public String getUsernameByEmail(String email) {
		return getUserByEmail(email).getUsername();
	}

	@Transactional
	public User registerUser(String username, String displayName, String email, String rawPassword) {
		if (userRepository.existsByEmail(email)) {
			throw new DuplicateKeyException("Email: " + email + " is already in use");
		}
		return userRepository.save(new User(username, displayName, email, passwordEncoder.encode(rawPassword)));
	}

	public void authenticateAndSetTokens(String email, String rawPassword, boolean rememberMe,
			HttpServletResponse response) {
		User user = getUserByEmail(email);
		authenticateUser(email, rawPassword);
		String accessToken = jwtTokenProvider.createAccessToken(user.getId(), email, 30 * 60 * 1000);
		ResponseCookie accessCookie = createResponseCookie("jwt", accessToken, 1 * 60); // TODO test 30 * 60
		response.addHeader("Set-Cookie", accessCookie.toString());
		if (rememberMe) {
			String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), 7L * 24 * 60 * 60 * 1000);
			ResponseCookie refreshCookie = createResponseCookie("refresh_jwt_long", refreshToken, 7 * 24 * 60 * 60);
			response.addHeader("Set-Cookie", refreshCookie.toString());
			refreshTokenService.saveRefreshToken(user.getId(), refreshToken, 7 * 24 * 60 * 60);
		} else {
			String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), 6 * 60 * 60 * 1000);
			ResponseCookie refreshCookie = createResponseCookie("refresh_jwt_short", refreshToken, 6 * 60 * 60);
			response.addHeader("Set-Cookie", refreshCookie.toString());
			refreshTokenService.saveRefreshToken(user.getId(), refreshToken, 6 * 60 * 60);
		}
	}

	public void setAccessToken(String refreshToken, HttpServletResponse response) {
		if (!jwtTokenProvider.validateToken(refreshToken)
				|| !refreshTokenService.validateRefreshTokenExists(refreshToken)) {
			throw new InvalidCredentialsException();
		}
		Long userId = refreshTokenService.validateAndExtractUserId(refreshToken);
		User user = getUserById(userId);
		String newAccessToken = createAccessToken(user);
		String newRefreshToken = createRefreshToken(user);
		refreshTokenService.invalidateRefreshToken(refreshToken);
		refreshTokenService.saveRefreshToken(user.getId(), newRefreshToken, 7 * 24 * 60 * 60);
		ResponseCookie accessCookie = createResponseCookie("jwt", newAccessToken, 30 * 60);
		ResponseCookie refreshCookie = createResponseCookie("refresh_jwt", newRefreshToken, 7 * 24 * 60 * 60);
		response.addHeader("Set-Cookie", accessCookie.toString());
		response.addHeader("Set-Cookie", refreshCookie.toString());
	}

	public void invalidateToken(HttpServletResponse response) {
		ResponseCookie deleteAccessToken = ResponseCookie.from("jwt", "").httpOnly(true).secure(true).sameSite("Strict")
				.path("/").maxAge(0).build();

		ResponseCookie deleteLongRefreshToken = ResponseCookie.from("refresh_jwt_long", "").httpOnly(true).secure(true)
				.sameSite("Strict").path("/").maxAge(0).build();

		ResponseCookie deleteShortRefreshToken = ResponseCookie.from("refresh_jwt_short", "").httpOnly(true)
				.secure(true).sameSite("Strict").path("/").maxAge(0).build();

		response.addHeader("Set-Cookie", deleteAccessToken.toString());
		response.addHeader("Set-Cookie", deleteLongRefreshToken.toString());
		response.addHeader("Set-Cookie", deleteShortRefreshToken.toString());
	}

	@Transactional
	public void updateUser(UserDetails userDetails, UpdateUserRequest request) {
		if (request.getUsername().length() < 3 || request.getDisplayName() == null || request.getDisplayName().isEmpty()
				|| request.getEmail() == null || request.getEmail().isEmpty()) {
			throw new IllegalArgumentException("Username or email is not provided.");
		}
		User user = getUserByEmail(userDetails.getUsername());
		if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
			throw new IllegalArgumentException("Current password is incorrect");
		}
		user.setUsername(request.getUsername());
		user.setDisplayName(request.getDisplayName());
		user.setEmail(request.getEmail());
		if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
			user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		}
		if (request.getProfilePicture() != null && !request.getProfilePicture().isEmpty()) {
			try {
				String base64Image = request.getProfilePicture();

				if (base64Image.startsWith("data:image/")) {
					int commaIndex = base64Image.indexOf(",");
					base64Image = base64Image.substring(commaIndex + 1);
				}
				byte[] imageBytes = Base64.getDecoder().decode(base64Image);
				user.setProfilePicture(imageBytes);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Invalid profile picture format.");
			}
		}
		userRepository.save(user);
	}

	private void authenticateUser(String email, String rawPassword) {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, rawPassword));
		} catch (AuthenticationException e) {
			throw new InvalidCredentialsException();
		}
	}

	private String createAccessToken(User user) {
		return jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), 30 * 60 * 1000);
	}

	private String createRefreshToken(User user) {
		return jwtTokenProvider.createRefreshToken(user.getId(), 7 * 24 * 60 * 60);
	}

	private ResponseCookie createResponseCookie(String name, String value, long maxAge) {
		return ResponseCookie.from(name, value).httpOnly(true).secure(false) // TODO: 本番環境では true にする
				.sameSite("Strict").path("/").maxAge(maxAge).build();
	}
}
