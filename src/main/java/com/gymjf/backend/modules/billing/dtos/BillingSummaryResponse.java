package com.gymjf.backend.modules.billing.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Resumen para el panel del administrador: ingresos del período, deuda abierta y morosos. */
public record BillingSummaryResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal totalCollected,
        List<MethodTotal> byMethod,
        /** Pagos esperando aprobación (de cualquier fecha). */
        long pendingApprovalCount,
        BigDecimal openDebtTotal,
        long openDebtAccounts,
        long delinquentUsers) {
}
