package com.gymjf.backend.modules.biometrics.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAnthropometricRecordRequest {
    @NotNull(message = "El usuario es obligatorio")
    private Integer userId;

    @NotNull(message = "El peso es obligatorio")
    private BigDecimal weight;

    @NotNull(message = "La altura es obligatoria")
    private BigDecimal height;

    private BigDecimal bodyFatPercentage;

    private BigDecimal shoulders;

    private BigDecimal chest;

    private BigDecimal waist;

    private BigDecimal hip;

    private BigDecimal arm;

    private BigDecimal thigh;

    private BigDecimal calf;

    @NotNull(message = "La fecha de registro es obligatoria")
    private LocalDate recordDate;

}
