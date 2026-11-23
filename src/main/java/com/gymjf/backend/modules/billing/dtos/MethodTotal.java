package com.gymjf.backend.modules.billing.dtos;

import java.math.BigDecimal;

import com.gymjf.backend.modules.billing.domain.PaymentMethod;

/** Cantidad y monto de pagos de un método (efectivo, SINPE...). */
public record MethodTotal(PaymentMethod method, long count, BigDecimal total) {
}
