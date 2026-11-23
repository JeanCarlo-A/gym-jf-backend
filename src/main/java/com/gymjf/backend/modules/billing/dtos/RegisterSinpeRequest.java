package com.gymjf.backend.modules.billing.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.Data;

/** Pago SINPE Móvil que el administrador ya verificó: se registra aprobado en un solo paso. */
@Data
public class RegisterSinpeRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Integer userId;

    @NotNull(message = "La deuda que se paga es obligatoria")
    private Integer accountReceivableId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "Valor no válido para el monto: máximo 8 dígito(s) entero(s) y 2 decimal(es)")
    private BigDecimal amount;

    @NotBlank(message = "El número de referencia del SINPE es obligatorio")
    @Size(max = 255, message = "El número de referencia no puede superar los 255 caracteres")
    private String referenceNumber;

    @Size(max = 255, message = "La URL del comprobante no puede superar los 255 caracteres")
    private String receiptImageUrl;
}
