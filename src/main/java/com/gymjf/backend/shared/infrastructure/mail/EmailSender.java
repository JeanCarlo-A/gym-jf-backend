package com.gymjf.backend.shared.infrastructure.mail;

/** Envío de correos de la aplicación. En desarrollo se simula; en producción usa SMTP (Brevo). */
public interface EmailSender {

    void send(String to, String subject, String body);
}
