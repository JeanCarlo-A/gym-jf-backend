package com.gymjf.backend.modules.billing.dtos;

import com.gymjf.backend.modules.billing.domain.TransactionValidationStatus;

import lombok.Data;

@Data
public class UpdateTransactionRequest {

    private TransactionValidationStatus validationStatus;

    private String receiptImageUrl;

}
