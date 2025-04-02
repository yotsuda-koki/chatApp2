package com.chatapp2.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.chatapp2.model.User;
import com.chatapp2.repository.UserRepository;
import com.chatapp2.security.CustomUserDetails;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("User with ID " + email + " not found"));
		return new CustomUserDetails(user);
	}

	public UserDetails loadUserByUserId(Long userId) throws UsernameNotFoundException {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));
		return new CustomUserDetails(user);
	}
}
