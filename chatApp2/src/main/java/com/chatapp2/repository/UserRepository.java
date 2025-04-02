package com.chatapp2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chatapp2.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);

	@EntityGraph(attributePaths = "roles")
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);
}
