package com.gymjf.backend.modules.inventory.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateOrderDetailRequest {

    @NotNull(message = "El pedido es obligatorio")
    private Integer orderId;

    @NotNull(message = "El producto es obligatorio")
    private Integer productId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser positiva")
    private Integer quantity;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser positivo")
    private BigDecimal unitPrice;
}
