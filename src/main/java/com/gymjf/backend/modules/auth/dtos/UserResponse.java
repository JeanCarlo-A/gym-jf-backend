package com.gymjf.backend.modules.auth.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gymjf.backend.modules.auth.domain.PhysicalGoal;
import com.gymjf.backend.modules.auth.domain.Rol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Integer id;
    private String name;
    private String email;
    private LocalDate birthDate;
    private String profilePictureUrl;
    private PhysicalGoal physicalGoal;
    private Rol rol;
    private boolean healthConsentAccepted;
    private LocalDateTime healthConsentAt;
    private LocalDateTime createdAt;
}
