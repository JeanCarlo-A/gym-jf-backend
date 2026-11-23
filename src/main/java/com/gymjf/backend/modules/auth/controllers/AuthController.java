package com.gymjf.backend.modules.auth.controllers;

import com.gymjf.backend.modules.auth.dtos.AuthResponse;
import com.gymjf.backend.modules.auth.dtos.ForgotPasswordRequest;
import com.gymjf.backend.modules.auth.dtos.LoginRequest;
import com.gymjf.backend.modules.auth.dtos.MessageResponse;
import com.gymjf.backend.modules.auth.dtos.RegisterRequest;
import com.gymjf.backend.modules.auth.dtos.ResetPasswordRequest;
import com.gymjf.backend.modules.auth.services.AuthService;
import com.gymjf.backend.modules.auth.services.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.ok(new MessageResponse(
                "Si el correo está registrado, te enviamos un código para restablecer tu contraseña"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Contraseña restablecida. Ya puedes iniciar sesión"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}