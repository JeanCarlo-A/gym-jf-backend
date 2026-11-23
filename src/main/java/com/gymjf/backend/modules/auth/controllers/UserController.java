package com.gymjf.backend.modules.auth.controllers;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.auth.dtos.ChangePasswordRequest;
import com.gymjf.backend.modules.auth.dtos.CreateUserRequest;
import com.gymjf.backend.modules.auth.dtos.MessageResponse;
import com.gymjf.backend.modules.auth.dtos.UpdateProfileRequest;
import com.gymjf.backend.modules.auth.dtos.UserResponse;
import com.gymjf.backend.modules.auth.services.UserService;
import com.gymjf.backend.shared.domain.dtos.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // --- Mi perfil: cualquier usuario autenticado ---

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(userService.getMe());
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateMe(request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(new MessageResponse("Contraseña actualizada correctamente"));
    }

    @PostMapping("/me/health-consent")
    public ResponseEntity<UserResponse> acceptHealthConsent() {
        return ResponseEntity.ok(userService.acceptHealthConsent());
    }

    // --- Gestión de usuarios: solo ADMIN ---

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> getAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }
}
