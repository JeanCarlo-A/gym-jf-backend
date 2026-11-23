package com.gymjf.backend.modules.memberships.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RenewSubscriptionRequest {
    @NotNull(message = "El usuario es obligatorio")
    private Integer userId;

    @NotNull(message = "El plan es obligatorio")
    private Integer planId;
}
