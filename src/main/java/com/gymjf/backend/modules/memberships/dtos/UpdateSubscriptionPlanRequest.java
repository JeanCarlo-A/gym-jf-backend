package com.gymjf.backend.modules.memberships.dtos;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class UpdateSubscriptionPlanRequest {
    private String planName;

    private BigDecimal price;

    private Integer durationDays;

}
