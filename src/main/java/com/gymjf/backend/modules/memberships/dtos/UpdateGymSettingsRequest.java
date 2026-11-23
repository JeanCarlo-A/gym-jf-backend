package com.gymjf.backend.modules.memberships.dtos;

import com.gymjf.backend.modules.memberships.domain.AccessMode;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateGymSettingsRequest {
    @NotNull(message = "El modo de acceso es obligatorio (FREE o SELECTIVE)")
    private AccessMode accessMode;
}
