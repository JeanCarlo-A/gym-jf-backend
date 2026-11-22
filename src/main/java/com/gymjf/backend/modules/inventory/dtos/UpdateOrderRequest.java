package com.gymjf.backend.modules.inventory.dtos;

import java.math.BigDecimal;

import com.gymjf.backend.modules.inventory.domain.OrderStatus;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateOrderRequest {

    private OrderStatus status;

    @Positive(message = "El total debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "Valor no válido para el total: máximo 8 dígito(s) entero(s) y 2 decimal(es)")
    private BigDecimal total;
}
