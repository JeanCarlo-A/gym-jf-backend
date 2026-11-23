package com.gymjf.backend.modules.billing.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Cierre de caja de un día: lo cobrado (pagos aprobados) por método y los pagos que quedaron sin resolver. */
public record CashClosingResponse(
        LocalDate date,
        List<MethodTotal> byMethod,
        BigDecimal totalCollected,
        long approvedCount,
        long pendingCount,
        long rejectedCount) {
}
