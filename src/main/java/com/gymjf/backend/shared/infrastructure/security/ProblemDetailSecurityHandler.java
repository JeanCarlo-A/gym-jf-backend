package com.gymjf.backend.shared.infrastructure.security;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

/**
 * Responde los errores de Spring Security (que ocurren antes de llegar a los
 * controladores, fuera del alcance de GlobalExceptionHandler) con el mismo
 * formato ProblemDetail en español:
 * 401 cuando falta el token o es inválido, 403 cuando no hay permisos.
 */
@Component
@RequiredArgsConstructor
public class ProblemDetailSecurityHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException ex) throws IOException {
        String detail = ex instanceof InsufficientAuthenticationException
                ? "Debes iniciar sesión para acceder a este recurso"
                : ex.getMessage();
        write(request, response, HttpStatus.UNAUTHORIZED, "No autorizado", detail);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException ex) throws IOException {
        write(request, response, HttpStatus.FORBIDDEN, "Acceso denegado",
                "No tienes permisos para realizar esta acción");
    }

    private void write(HttpServletRequest request, HttpServletResponse response,
            HttpStatus status, String title, String detail) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", title);
        body.put("status", status.value());
        body.put("detail", detail);
        body.put("instance", request.getRequestURI());
        body.put("timestamp", Instant.now().toString());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        jsonMapper.writeValue(response.getWriter(), body);
    }
}
