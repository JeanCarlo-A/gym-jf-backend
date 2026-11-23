package com.gymjf.backend.modules.memberships.dtos;

import com.gymjf.backend.modules.memberships.domain.AccessMode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymSettingsResponse {
    private AccessMode accessMode;
}
