package com.gymjf.backend.shared.infrastructure.mail;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Correo simulado (application.mail.enabled=false, el valor por defecto): no sale nada a internet.
 * En el perfil dev escribe el correo completo en el log para poder probar el flujo; en cualquier
 * otro perfil solo avisa, para no dejar enlaces de recuperación en los logs de producción.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.mail.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    private final Environment environment;

    @Override
    public void send(String to, String subject, String body) {
        if (environment.acceptsProfiles(Profiles.of("dev"))) {
            log.info("CORREO SIMULADO\n  Para: {}\n  Asunto: {}\n  Mensaje:\n{}", to, subject, body);
        } else {
            log.warn("Correo a {} no enviado: el servicio de correo no está configurado (application.mail.enabled=false)", to);
        }
    }
}
