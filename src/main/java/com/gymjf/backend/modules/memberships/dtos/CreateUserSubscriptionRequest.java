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

    /** Opcional: por defecto hoy. La fecha de vencimiento no se envía: se calcula con la duración del plan. */
    private LocalDate startDate;

    /** Opcional: por defecto PENDING (pendiente de pago). El administrador puede registrarla ya ACTIVE. */
    private SubscriptionStatus status;

}
