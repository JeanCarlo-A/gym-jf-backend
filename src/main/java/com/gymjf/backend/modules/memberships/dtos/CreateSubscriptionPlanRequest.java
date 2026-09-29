package com.gymjf.backend.modules.memberships.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateSubscriptionPlanRequest {
    @NotBlank(message = "El nombre del plan es obligatorio")
    private String planName;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    private BigDecimal price;

    @NotNull(message = "La duración en días es obligatoria")
    @Positive(message = "La duración en días debe ser positiva")
    private Integer durationDays;

}
