package com.gymjf.backend.modules.auth.services;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.dtos.ChangePasswordRequest;
import com.gymjf.backend.modules.auth.dtos.CreateUserRequest;
import com.gymjf.backend.modules.auth.dtos.UpdateProfileRequest;
import com.gymjf.backend.modules.auth.dtos.UserResponse;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.shared.domain.dtos.PageResponse;
import com.gymjf.backend.shared.domain.exceptions.BadRequestException;
import com.gymjf.backend.shared.domain.exceptions.ConflictException;
import com.gymjf.backend.shared.domain.exceptions.ResourceNotFoundException;
import com.gymjf.backend.shared.infrastructure.security.CurrentUser;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;

    // --- Mi perfil (cualquier usuario autenticado) ---

    public UserResponse getMe() {
        return toResponse(loadCurrent());
    }

    @Transactional
    public UserResponse updateMe(UpdateProfileRequest request) {
        User user = loadCurrent();
        if (request.getName() != null) {
            if (request.getName().isBlank()) {
                throw new BadRequestException("El nombre no puede estar vacío");
            }
            user.setName(request.getName().trim());
        }
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        if (request.getPhysicalGoal() != null) {
            user.setPhysicalGoal(request.getPhysicalGoal());
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = loadCurrent();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("La contraseña actual no es correcta");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /** Registra la aceptación del consentimiento informado (R-1.2). Si ya estaba aceptado, conserva la fecha original. */
    @Transactional
    public UserResponse acceptHealthConsent() {
        User user = loadCurrent();
        if (user.getHealthConsentAt() == null) {
            user.setHealthConsentAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            userRepository.save(user);
        }
        return toResponse(user);
    }

    // --- Gestión de usuarios (solo ADMIN, ver @PreAuthorize en el controlador) ---

    public PageResponse<UserResponse> getAll(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable), this::toResponse);
    }

    public UserResponse getById(Integer id) {
        return toResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado")));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email ya está registrado");
        }
        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .build();
        return toResponse(userRepository.save(user));
    }

    // --- Utilidades ---

    private User loadCurrent() {
        return userRepository.findById(currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .birthDate(user.getBirthDate())
                .profilePictureUrl(user.getProfilePictureUrl())
                .physicalGoal(user.getPhysicalGoal())
                .rol(user.getRol())
                .healthConsentAccepted(user.getHealthConsentAt() != null)
                .healthConsentAt(user.getHealthConsentAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
