package com.gymjf.backend.shared.domain.exceptions;

/**
 * Se lanza cuando el email o la contraseña no son correctos (HTTP 401).
 * El mensaje se muestra tal cual al usuario, por eso va en español.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
