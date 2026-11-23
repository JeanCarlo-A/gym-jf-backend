package com.gymjf.backend.modules.billing.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

/** El estado del pago ya no se cambia aquí: se usa POST /transactions/{id}/approve o /reject. */
@Data
public class UpdateTransactionRequest {

    @Size(max = 255, message = "La URL del comprobante no puede superar los 255 caracteres")
    private String receiptImageUrl;

}
