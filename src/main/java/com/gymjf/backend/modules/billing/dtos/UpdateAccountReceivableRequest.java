package com.gymjf.backend.modules.billing.dtos;

import java.math.BigDecimal;

import com.gymjf.backend.modules.billing.domain.DelinquencyStatus;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateAccountReceivableRequest {

    @Positive(message = "La deuda total debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "Valor no válido para la deuda total: máximo 8 dígito(s) entero(s) y 2 decimal(es)")
    private BigDecimal totalDebt;

    @PositiveOrZero(message = "El saldo pendiente no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "Valor no válido para el saldo pendiente: máximo 8 dígito(s) entero(s) y 2 decimal(es)")
    private BigDecimal pendingBalance;

    private DelinquencyStatus delinquencyStatus;

}
