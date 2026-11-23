package com.gymjf.backend.modules.auth.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gymjf.backend.modules.auth.domain.PasswordResetToken;
import com.gymjf.backend.modules.auth.repositories.PasswordResetTokenRepository;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.shared.domain.exceptions.BadRequestException;
import com.gymjf.backend.shared.infrastructure.mail.EmailSender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Recuperación de contraseña (RF-1.1.4). El usuario pide el código con su correo y lo canjea por
 * una contraseña nueva. El código es de un solo uso, vence a los 30 minutos y en la BD solo se
 * guarda su hash. La respuesta de "olvidé mi contraseña" es siempre la misma, exista o no el
 * correo, para no revelar quién tiene cuenta.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int TOKEN_MINUTES = 30;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;

    @Value("${application.frontend.reset-url:http://localhost:3000/restablecer-contrasena}")
    private String resetUrl;

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // Un solo código vigente a la vez: los anteriores se invalidan.
            tokenRepository.findByUserIdAndUsedFalse(user.getId()).forEach(old -> old.setUsed(true));

            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            tokenRepository.save(PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(hash(token))
                    .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_MINUTES))
                    .build());

            try {
                emailSender.send(user.getEmail(), "Recupera tu contraseña - Gym JF", """
                        Hola %s,

                        Recibimos una solicitud para restablecer tu contraseña de Gym JF.

                        Tu código de recuperación es:
                        %s

                        Si usas la app, abre este enlace: %s?token=%s

                        El código vence en %d minutos y solo se puede usar una vez.
                        Si no fuiste tú, ignora este mensaje: tu contraseña no cambiará.
                        """.formatted(user.getName(), token, resetUrl, token, TOKEN_MINUTES));
            } catch (RuntimeException e) {
                // No se informa al cliente para no revelar si el correo existe; queda en el log.
                log.error("No se pudo enviar el correo de recuperación a {}", user.getEmail(), e);
            }
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken saved = tokenRepository.findByTokenHash(hash(token))
                .filter(t -> !t.isUsed() && t.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new BadRequestException(
                        "El código de recuperación no es válido o ya venció. Solicita uno nuevo"));

        saved.getUser().setPassword(passwordEncoder.encode(newPassword));
        saved.setUsed(true);
        userRepository.save(saved.getUser());
        tokenRepository.save(saved);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
