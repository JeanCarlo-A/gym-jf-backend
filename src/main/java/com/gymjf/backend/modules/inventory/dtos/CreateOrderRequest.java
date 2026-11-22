package com.gymjf.backend.modules.inventory.dtos;

import java.math.BigDecimal;

import com.gymjf.backend.modules.inventory.domain.OrderStatus;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Integer userId;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 45, message = "El código no puede superar los 45 caracteres")
    private String code;

    @NotNull(message = "El total es obligatorio")
    @Positive(message = "El total debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "Valor no válido para el total: máximo 8 dígito(s) entero(s) y 2 decimal(es)")
    private BigDecimal total;

    private OrderStatus status;
}
