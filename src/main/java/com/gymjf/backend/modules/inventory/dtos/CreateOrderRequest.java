package com.gymjf.backend.modules.inventory.dtos;

import java.math.BigDecimal;

import com.gymjf.backend.modules.inventory.domain.OrderStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Integer userId;

    @NotBlank(message = "El código es obligatorio")
    private String code;

    @NotNull(message = "El total es obligatorio")
    @Positive(message = "El total debe ser positivo")
    private BigDecimal total;

    private OrderStatus status;
}
