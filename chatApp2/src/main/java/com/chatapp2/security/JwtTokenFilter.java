package com.chatapp2.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.chatapp2.service.RefreshTokenService;
import com.chatapp2.service.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
	private final JwtTokenProvider jwtTokenProvider;
	private final UserDetailsServiceImpl userDetailsServiceImpl;
	private final RefreshTokenService refreshTokenService;

	private static final long ACCESS_TOKEN_EXPIRATION = 30 * 60 * 1000;
	private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();
		return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")
				|| path.startsWith("/favicon.ico") || path.startsWith("/login") || path.startsWith("/signup")
				|| path.startsWith("/api/auth/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			if (isAuthenticationRequired()) {
				Optional<String> accessTokenOpt = getTokenFromCookies(request, "jwt");
				Optional<String> refreshTokenOptLong = getTokenFromCookies(request, "refresh_jwt_long");
				Optional<String> refreshTokenOptShort = getTokenFromCookies(request, "refresh_jwt_short");

				if (accessTokenOpt.isPresent()) {
					String accessToken = accessTokenOpt.get();

					if (jwtTokenProvider.validateToken(accessToken)) {
						authenticateUser(accessToken, request);
					} else {
						handleRefreshToken(refreshTokenOptLong, refreshTokenOptShort, response, request);
					}
				} else {
					handleRefreshToken(refreshTokenOptLong, refreshTokenOptShort, response, request);
				}
			}
			filterChain.doFilter(request, response);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	private void handleRefreshToken(Optional<String> refreshTokenOptLong, Optional<String> refreshTokenOptShort,
			HttpServletResponse response, HttpServletRequest request) {
		try {
			if (refreshTokenOptLong.isPresent() && jwtTokenProvider.validateToken(refreshTokenOptLong.get())) {
				processLongRefreshToken(refreshTokenOptLong.get(), response, request);
			} else if (refreshTokenOptShort.isPresent() && jwtTokenProvider.validateToken(refreshTokenOptShort.get())) {
				processShortRefreshToken(refreshTokenOptShort.get(), response, request);
			} else {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}
		} catch (Exception e) {
			System.err.println("リフレッシュトークン処理中にエラーが発生しました: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	private Optional<String> getTokenFromCookies(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return Optional.empty();
		return Arrays.stream(cookies).filter(cookie -> name.equals(cookie.getName())).map(Cookie::getValue).findFirst();
	}

	private boolean isAuthenticationRequired() {
		return SecurityContextHolder.getContext().getAuthentication() == null;
	}

	private void authenticateUser(String token, HttpServletRequest request) {
		Optional.ofNullable(jwtTokenProvider.getUsername(token))
				.flatMap(email -> Optional.ofNullable(userDetailsServiceImpl.loadUserByUsername(email)))
				.ifPresent(userDetails -> {
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				});
	}

	private void processLongRefreshToken(String refreshToken, HttpServletResponse response,
			HttpServletRequest request) {
		if (jwtTokenProvider.validateToken(refreshToken)) {
			Long userId = Long.valueOf(jwtTokenProvider.getUsername(refreshToken));
			UserDetails userDetails = userDetailsServiceImpl.loadUserByUserId(userId);
			String email = userDetails.getUsername();
			String newAccessToken = jwtTokenProvider.createAccessToken(userId, email, ACCESS_TOKEN_EXPIRATION);
			String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, REFRESH_TOKEN_EXPIRATION);
			ResponseCookie accessCookie = createResponseCookie("jwt", newAccessToken, 30 * 60);
			ResponseCookie refreshCookie = createResponseCookie("refresh_jwt_long", newRefreshToken, 7 * 24 * 60 * 60);
			response.addHeader("Set-Cookie", accessCookie.toString());
			response.addHeader("Set-Cookie", refreshCookie.toString());
			refreshTokenService.saveRefreshToken(userId, newRefreshToken, 7 * 24 * 60 * 60);
			authenticateUser(newAccessToken, request);
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	private void processShortRefreshToken(String refreshToken, HttpServletResponse response,
			HttpServletRequest request) {
		if (jwtTokenProvider.validateToken(refreshToken)) {
			Long userId = Long.valueOf(jwtTokenProvider.getUsername(refreshToken));
			UserDetails userDetails = userDetailsServiceImpl.loadUserByUserId(userId);
			String email = userDetails.getUsername();
			String newAccessToken = jwtTokenProvider.createAccessToken(userId, email, ACCESS_TOKEN_EXPIRATION);
			String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, REFRESH_TOKEN_EXPIRATION);
			ResponseCookie accessCookie = createResponseCookie("jwt", newAccessToken, 30 * 60);
			ResponseCookie refreshCookie = createResponseCookie("refresh_jwt_short", newRefreshToken, 6 * 60 * 60);
			response.addHeader("Set-Cookie", accessCookie.toString());
			response.addHeader("Set-Cookie", refreshCookie.toString());
			refreshTokenService.saveRefreshToken(userId, newRefreshToken, 6 * 60 * 60);
			authenticateUser(newAccessToken, request);
			System.out.println("New access token: " + newAccessToken);
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	private ResponseCookie createResponseCookie(String name, String value, long maxAge) {
		return ResponseCookie.from(name, value).httpOnly(true).secure(false) // TODO: 本番環境ではHTTPSでtrueにする
				.sameSite("Strict").path("/").maxAge(maxAge).build();
	}
}
