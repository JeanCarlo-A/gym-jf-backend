package com.gymjf.backend.modules.billing.dtos;

import java.math.BigDecimal;

import com.gymjf.backend.modules.billing.domain.PaymentMethod;
import com.gymjf.backend.modules.billing.domain.TransactionValidationStatus;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateTransactionRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Integer userId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "Valor no válido para el monto: máximo 8 dígito(s) entero(s) y 2 decimal(es)")
    private BigDecimal amount;

    @NotNull(message = "El método de pago es obligatorio")
    private PaymentMethod paymentMethod;

    /** Doble confirmación del efectivo (R-1.3): el administrador vuelve a escribir el monto y debe coincidir. */
    private BigDecimal confirmationAmount;

    @Size(max = 255, message = "El número de referencia no puede superar los 255 caracteres")
    private String referenceNumber;

    @Size(max = 255, message = "La URL del comprobante no puede superar los 255 caracteres")
    private String receiptImageUrl;

    private TransactionValidationStatus validationStatus;

    private Integer accountReceivableId;

}
