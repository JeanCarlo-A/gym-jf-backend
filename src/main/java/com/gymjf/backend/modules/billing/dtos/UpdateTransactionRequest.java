package com.gymjf.backend.modules.billing.dtos;

import com.gymjf.backend.modules.billing.domain.TransactionValidationStatus;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateTransactionRequest {

    private TransactionValidationStatus validationStatus;

    @Size(max = 255, message = "La URL del comprobante no puede superar los 255 caracteres")
    private String receiptImageUrl;

}
