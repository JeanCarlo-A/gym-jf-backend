package com.gymjf.backend.modules.memberships.dtos;

import java.time.LocalDate;

import com.gymjf.backend.modules.memberships.domain.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionResponse {
    private Integer id;
    private Integer userId;
    private Integer planId;
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionStatus status;
}
