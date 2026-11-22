package com.gymjf.backend.modules.memberships.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateSubscriptionPlanRequest {
    @NotBlank(message = "El nombre del plan es obligatorio")
    @Size(max = 45, message = "El nombre del plan no puede superar los 45 caracteres")
    private String planName;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "Valor no válido para el precio: máximo 8 dígito(s) entero(s) y 2 decimal(es)")
    private BigDecimal price;

    @NotNull(message = "La duración en días es obligatoria")
    @Positive(message = "La duración en días debe ser mayor que cero")
    private Integer durationDays;

}
