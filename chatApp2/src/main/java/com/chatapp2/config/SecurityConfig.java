package com.chatapp2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.chatapp2.security.JwtTokenFilter;
import com.chatapp2.security.JwtTokenProvider;
import com.chatapp2.service.RefreshTokenService;
import com.chatapp2.service.UserDetailsServiceImpl;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtTokenProvider jwtTokenProvider;
	private final UserDetailsServiceImpl userDetailsServiceImpl;
	private final RefreshTokenService refreshTokenService;

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.requestMatchers("/login", "/signup", "/api/auth/**").permitAll()
						.requestMatchers("/js/**", "/css/**", "/images/**", "/favicon.ico").permitAll()
						.requestMatchers("/ws/**").permitAll().anyRequest().authenticated())
				.addFilterBefore(new JwtTokenFilter(jwtTokenProvider, userDetailsServiceImpl, refreshTokenService),
						UsernamePasswordAuthenticationFilter.class)
				.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
					if (request.getHeader("Accept").contains("text/html")) {
						response.sendRedirect("/login");
					} else {
						response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
						response.setContentType("application/json;charset=UTF-8");
						response.getWriter().write("{\"message\": \"Unauthorized\"}");
					}
				}));
		return http.build();
	}
}
