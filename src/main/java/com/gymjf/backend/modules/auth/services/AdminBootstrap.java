package com.gymjf.backend.modules.auth.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gymjf.backend.modules.auth.domain.Rol;
import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Crea el primer administrador al arrancar, solo si todavía no existe ninguno y se definieron
 * BOOTSTRAP_ADMIN_EMAIL y BOOTSTRAP_ADMIN_PASSWORD (ver .env.example). Después de crearlo se pueden
 * quitar esas variables; los demás administradores los crea un administrador desde POST /api/v1/users.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.bootstrap.admin-email:}")
    private String email;

    @Value("${application.bootstrap.admin-password:}")
    private String password;

    @Value("${application.bootstrap.admin-name:Administrador Gym JF}")
    private String name;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }
        if (userRepository.existsByRol(Rol.ADMIN)) {
            return;
        }
        if (userRepository.existsByEmail(email)) {
            log.warn("No se creó el administrador inicial: el correo {} ya pertenece a un usuario que no es ADMIN", email);
            return;
        }
        userRepository.save(User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .rol(Rol.ADMIN)
                .build());
        log.info("Administrador inicial creado: {}", email);
    }
}
