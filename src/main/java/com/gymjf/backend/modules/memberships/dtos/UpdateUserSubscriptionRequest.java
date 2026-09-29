package com.gymjf.backend.modules.memberships.dtos;

import java.time.LocalDate;

import com.gymjf.backend.modules.memberships.domain.SubscriptionStatus;

import lombok.Data;

@Data
public class UpdateUserSubscriptionRequest {
    private LocalDate startDate;

    private LocalDate endDate;

    private SubscriptionStatus status;

}
