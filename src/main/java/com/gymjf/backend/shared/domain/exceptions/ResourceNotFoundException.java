package com.gymjf.backend.shared.domain.exceptions;

/**
 * Se lanza cuando un recurso solicitado no existe (HTTP 404).
 * El mensaje se muestra tal cual al usuario, por eso va en español.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
