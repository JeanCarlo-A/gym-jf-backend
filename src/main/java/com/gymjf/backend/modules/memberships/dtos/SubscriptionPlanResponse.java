package com.gymjf.backend.modules.memberships.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanResponse {
    private Integer id;
    private String planName;
    private BigDecimal price;
    private Integer durationDays;
}
