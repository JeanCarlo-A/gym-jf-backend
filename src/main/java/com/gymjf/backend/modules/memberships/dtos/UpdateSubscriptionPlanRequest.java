package com.gymjf.backend.modules.memberships.dtos;

import java.math.BigDecimal;

import com.gymjf.backend.modules.memberships.domain.PlanType;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateSubscriptionPlanRequest {
    @Size(min = 1, max = 45, message = "El nombre del plan no puede estar vacío ni superar los 45 caracteres")
    private String planName;

    @Positive(message = "El precio debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "Valor no válido para el precio: máximo 8 dígito(s) entero(s) y 2 decimal(es)")
    private BigDecimal price;

    @Positive(message = "La duración en días debe ser mayor que cero")
    private Integer durationDays;

    private PlanType type;

}
