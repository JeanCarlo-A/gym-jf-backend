package com.gymjf.backend.modules.inventory.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateOrderDetailRequest {

    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer quantity;

    @Positive(message = "El precio unitario debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "Valor no válido para el precio unitario: máximo 8 dígito(s) entero(s) y 2 decimal(es)")
    private BigDecimal unitPrice;
}
