package com.gymjf.backend.shared.infrastructure.web;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.gymjf.backend.shared.domain.exceptions.ConflictException;
import com.gymjf.backend.shared.domain.exceptions.InvalidCredentialsException;
import com.gymjf.backend.shared.domain.exceptions.ResourceNotFoundException;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * Convierte cualquier error de la API en una respuesta ProblemDetail (RFC 9457)
 * con mensajes en español, listos para mostrarse en la app:
 *
 * <pre>
 * { "title": "Recurso no encontrado", "status": 404,
 *   "detail": "Horario no encontrado", "instance": "/api/v1/schedules/99" }
 * </pre>
 *
 * Nunca expone stack traces; los errores inesperados se registran en el log.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- Errores de negocio (lanzados por los servicios) ---

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "No autorizado", ex.getMessage());
    }

    // --- Errores de la solicitud ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "Uno o más campos no son válidos");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Solicitud inválida", describeUnreadableBody(ex));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Solicitud inválida",
                "Valor inválido '%s' para el parámetro '%s'%s"
                        .formatted(ex.getValue(), ex.getName(), expectedFormat(ex.getRequiredType())));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Solicitud inválida",
                "Falta el parámetro obligatorio '%s'".formatted(ex.getParameterName()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResource(NoResourceFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Ruta no encontrada",
                "La ruta '/%s' no existe".formatted(ex.getResourcePath()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return problem(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido",
                "El método %s no está permitido en esta ruta".formatted(ex.getMethod()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato no soportado",
                "El cuerpo de la solicitud debe enviarse como application/json");
    }

    // --- Errores de datos y seguridad ---

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return problem(HttpStatus.CONFLICT, "Conflicto",
                "La operación no se puede completar: el registro está duplicado o está siendo usado por otros datos");
    }

    /**
     * Los triggers y procedimientos de MySQL rechazan operaciones con SIGNAL SQLSTATE '45000'
     * (aforo máximo, stock insuficiente...). Spring los envuelve como DataAccessException genérica;
     * aquí se recupera el mensaje en español de la BD y se responde como conflicto.
     */
    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDataAccess(DataAccessException ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && "45000".equals(sql.getSQLState())) {
                String message = sql.getMessage() == null ? "" : sql.getMessage().replaceFirst("^Error:\\s*", "");
                return problem(HttpStatus.CONFLICT, "Conflicto", message);
            }
        }
        return handleUnexpected(ex);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado",
                "No tienes permisos para realizar esta acción");
    }

    // --- Cualquier otro error ---

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Error inesperado", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado. Intenta de nuevo más tarde.");
    }

    // --- Utilidades ---

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /** Traduce los errores de Jackson (JSON mal formado, enum o fecha inválidos) a un mensaje claro. */
    private String describeUnreadableBody(HttpMessageNotReadableException ex) {
        for (Throwable cause = ex.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof InvalidFormatException invalid) {
                String field = fieldPath(invalid);
                Class<?> target = invalid.getTargetType();
                if (target != null && target.isEnum()) {
                    String allowed = Arrays.stream(target.getEnumConstants())
                            .map(Object::toString)
                            .collect(Collectors.joining(", "));
                    return "Valor inválido '%s' para '%s'. Valores permitidos: %s"
                            .formatted(invalid.getValue(), field, allowed);
                }
                return "Valor inválido '%s' para '%s'%s"
                        .formatted(invalid.getValue(), field, expectedFormat(target));
            }
            if (cause instanceof MismatchedInputException mismatched) {
                return "Tipo de dato incorrecto para '%s'%s"
                        .formatted(fieldPath(mismatched), expectedFormat(mismatched.getTargetType()));
            }
            if (cause instanceof JacksonException) {
                return "El cuerpo de la solicitud no es un JSON válido";
            }
        }
        return "El cuerpo de la solicitud es obligatorio o está vacío";
    }

    private String fieldPath(JacksonException ex) {
        return ex.getPath().stream()
                .map(ref -> ref.getPropertyName() != null ? ref.getPropertyName() : "[" + ref.getIndex() + "]")
                .collect(Collectors.joining("."));
    }

    private String expectedFormat(Class<?> type) {
        if (type == null) {
            return "";
        }
        if (type == LocalTime.class) {
            return ". Formato esperado: HH:mm:ss (ej. 07:30:00)";
        }
        if (type == LocalDate.class) {
            return ". Formato esperado: yyyy-MM-dd (ej. 2026-10-05)";
        }
        if (type == LocalDateTime.class) {
            return ". Formato esperado: yyyy-MM-ddTHH:mm:ss (ej. 2026-10-05T07:30:00)";
        }
        if (Number.class.isAssignableFrom(type) || type == BigDecimal.class
                || type == int.class || type == long.class || type == double.class) {
            return ". Se esperaba un número";
        }
        if (type == Boolean.class || type == boolean.class) {
            return ". Se esperaba true o false";
        }
        return "";
    }
}
