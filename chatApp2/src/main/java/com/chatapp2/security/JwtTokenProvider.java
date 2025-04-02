package com.chatapp2.security;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JwtTokenProvider {
	private static final String SECRET_KEY = "mysecretkey";

	public String createAccessToken(Long userId, String email, long expirationTime) { // 60*60*1000
		return JWT.create().withSubject(email).withClaim("userId", userId).withIssuedAt(new Date())
				.withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
				.sign(Algorithm.HMAC256(SECRET_KEY));
	}

	public String createRefreshToken(Long userId, long expirationTime) {
		return JWT.create().withSubject(userId.toString()).withIssuedAt(new Date())
				.withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
				.sign(Algorithm.HMAC256(SECRET_KEY));
	}

	public String getUsername(String token) {
		try {
			DecodedJWT decodedJWT = JWT.decode(token);
			String subject = decodedJWT.getSubject();
			return subject;
		} catch (Exception e) {
			return null;
		}
	}

	public boolean validateToken(String token) {
		try {
			JWT.require(Algorithm.HMAC256(SECRET_KEY)).build().verify(token);
			return true;
		} catch (JWTVerificationException e) {
			return false;
		}
	}
}
