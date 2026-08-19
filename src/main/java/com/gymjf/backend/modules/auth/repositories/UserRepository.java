package com.gymjf.backend.modules.auth.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.auth.domain.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findById(Integer id);

    Optional<User> findByEmail(String email);

    boolean existsById(Integer id);

    boolean existsByEmail(String email);
}
