package com.gymjf.backend.modules.billing.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

/** Estado de morosidad de un usuario: debe más del monto límite o tiene una deuda abierta de más de N días. */
@Data
@Builder
public class DelinquencyResponse {
    private Integer userId;
    private boolean delinquent;
    /** Suma de los saldos pendientes de todas sus deudas abiertas. */
    private BigDecimal totalPending;
    /** Parte de lo pendiente que ya pasó los días de gracia: es la que se compara con el monto límite. */
    private BigDecimal totalOverdue;
    private int graceDays;
    private int openAccounts;
    private LocalDate oldestIssueDate;
    /** Días de la deuda abierta más antigua. */
    private long oldestDebtDays;
    private BigDecimal maxAmount;
    private int maxDays;
    /** Explicación en español, lista para mostrar. */
    private String reason;
}
