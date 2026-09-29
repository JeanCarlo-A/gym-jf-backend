package com.gymjf.backend.modules.billing.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gymjf.backend.modules.billing.domain.DelinquencyStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateAccountReceivableRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Integer userId;

    @NotNull(message = "La deuda total es obligatoria")
    @Positive(message = "La deuda total debe ser positiva")
    private BigDecimal totalDebt;

    @NotNull(message = "El saldo pendiente es obligatorio")
    private BigDecimal pendingBalance;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate issueDate;

    private DelinquencyStatus delinquencyStatus;

}
