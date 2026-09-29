package com.gymjf.backend.modules.billing.dtos;

import java.math.BigDecimal;

import com.gymjf.backend.modules.billing.domain.PaymentMethod;
import com.gymjf.backend.modules.billing.domain.TransactionValidationStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateTransactionRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Integer userId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal amount;

    @NotNull(message = "El método de pago es obligatorio")
    private PaymentMethod paymentMethod;

    private String referenceNumber;

    private String receiptImageUrl;

    private TransactionValidationStatus validationStatus;

    private Integer accountReceivableId;

}
