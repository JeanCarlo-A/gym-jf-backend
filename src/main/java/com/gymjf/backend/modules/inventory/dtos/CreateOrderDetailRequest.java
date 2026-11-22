package com.gymjf.backend.modules.inventory.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateOrderDetailRequest {

    @NotNull(message = "El pedido es obligatorio")
    private Integer orderId;

    @NotNull(message = "El producto es obligatorio")
    private Integer productId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer quantity;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "Valor no válido para el precio unitario: máximo 8 dígito(s) entero(s) y 2 decimal(es)")
    private BigDecimal unitPrice;
}
