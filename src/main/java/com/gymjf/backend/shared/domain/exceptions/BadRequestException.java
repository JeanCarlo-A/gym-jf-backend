package com.gymjf.backend.shared.domain.exceptions;

/** Se lanza cuando la solicitud es válida en formato pero no se puede aceptar (HTTP 400). */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
