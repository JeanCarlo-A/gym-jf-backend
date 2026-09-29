package com.gymjf.backend.modules.biometrics.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnthropometricRecordResponse {
    private Integer id;

    private Integer userId;

    private BigDecimal weight;

    private BigDecimal height;

    private BigDecimal bodyFatPercentage;

    private BigDecimal shoulders;

    private BigDecimal chest;

    private BigDecimal waist;

    private BigDecimal hip;

    private BigDecimal arm;

    private BigDecimal thigh;

    private BigDecimal calf;

    private LocalDate recordDate;

}
