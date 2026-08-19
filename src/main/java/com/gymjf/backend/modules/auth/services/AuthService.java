package com.gymjf.backend.modules.auth.services;

import com.gymjf.backend.modules.auth.domain.Rol;
import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.dtos.AuthResponse;
import com.gymjf.backend.modules.auth.dtos.LoginRequest;
import com.gymjf.backend.modules.auth.dtos.RegisterRequest;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.shared.infrastructure.security.JwService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwService jwService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.CUSTOMER) // Rol por defecto para nuevos usuarios
                .build();

        userRepository.save(user);

        String token = jwService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .rol(user.getRol())
                .build();
    }

    // --- INICIO DE SESIÓN ---
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .rol(user.getRol())
                .build();
    }
}