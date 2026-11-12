package com.gymjf.backend.shared.domain.exceptions;

/**
 * Se lanza cuando la operación choca con el estado actual de los datos, p. ej. un duplicado (HTTP 409).
 * El mensaje se muestra tal cual al usuario, por eso va en español.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
