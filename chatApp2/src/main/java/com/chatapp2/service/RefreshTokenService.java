package com.chatapp2.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.chatapp2.exception.InvalidCredentialsException;
import com.chatapp2.model.RefreshToken;
import com.chatapp2.repository.RefreshTokenRepository;
import com.chatapp2.security.JwtTokenProvider;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public void saveRefreshToken(Long userId, String token, long expirationTimeInSeconds) {
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUserId(userId);
		refreshToken.setToken(token);
		refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(expirationTimeInSeconds));
		refreshTokenRepository.save(refreshToken);
	}

	public Long validateAndExtractUserId(String refreshToken) {
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			throw new InvalidCredentialsException();
		}
		Long userId = Long.parseLong(jwtTokenProvider.getUsername(refreshToken));
		if (!refreshTokenRepository.findByTokenAndUserId(refreshToken, userId)) {
			throw new InvalidCredentialsException();
		}
		return userId;
	}

	public boolean validateRefreshTokenExists(String refreshToken) {
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			throw new InvalidCredentialsException();
		}
		return refreshTokenRepository.existsByToken(refreshToken);
	}

	@Transactional
	public void invalidateRefreshToken(String refreshToken) {
		refreshTokenRepository.deleteByToken(refreshToken);
	}

}
