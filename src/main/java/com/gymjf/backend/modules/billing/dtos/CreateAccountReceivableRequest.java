package com.gymjf.backend.modules.billing.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gymjf.backend.modules.billing.domain.DelinquencyStatus;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateAccountReceivableRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Integer userId;

    @NotNull(message = "La deuda total es obligatoria")
    @Positive(message = "La deuda total debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "Valor no válido para la deuda total: máximo 8 dígito(s) entero(s) y 2 decimal(es)")
    private BigDecimal totalDebt;

    @NotNull(message = "El saldo pendiente es obligatorio")
    @PositiveOrZero(message = "El saldo pendiente no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "Valor no válido para el saldo pendiente: máximo 8 dígito(s) entero(s) y 2 decimal(es)")
    private BigDecimal pendingBalance;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate issueDate;

    private DelinquencyStatus delinquencyStatus;

}
