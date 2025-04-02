package com.chatapp2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chatapp2.model.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	boolean findByTokenAndUserId(String token, Long userId);

	boolean existsByToken(String token);

	void deleteByToken(String token);
}
