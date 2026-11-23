package com.gymjf.backend.modules.memberships.dtos;

import java.time.LocalDate;

import com.gymjf.backend.modules.memberships.domain.AccessMode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessCheckResponse {
    private Integer userId;
    private boolean allowed;
    private AccessMode accessMode;
    /** Mensaje en español que explica la decisión. */
    private String reason;
    private Integer subscriptionId;
    /** Último día en que la suscripción da acceso (vence a las 00:00 del día siguiente). */
    private LocalDate validUntil;
}
