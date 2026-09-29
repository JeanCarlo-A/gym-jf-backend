package com.gymjf.backend.modules.memberships.dtos;

import java.time.LocalDate;

import com.gymjf.backend.modules.memberships.domain.SubscriptionStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserSubscriptionRequest {
    @NotNull(message = "El usuario es obligatorio")
    private Integer userId;

    @NotNull(message = "El plan es obligatorio")
    private Integer planId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate endDate;

    private SubscriptionStatus status;

}
