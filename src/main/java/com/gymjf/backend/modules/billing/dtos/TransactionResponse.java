package com.gymjf.backend.modules.billing.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.gymjf.backend.modules.billing.domain.PaymentMethod;
import com.gymjf.backend.modules.billing.domain.TransactionValidationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Integer id;

    private Integer userId;

    private BigDecimal amount;

    private PaymentMethod paymentMethod;

    private String referenceNumber;

    private String receiptImageUrl;

    private TransactionValidationStatus validationStatus;

    private LocalDateTime transactionDate;

    private Integer accountReceivableId;

}
