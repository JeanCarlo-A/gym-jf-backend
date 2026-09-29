package com.gymjf.backend.modules.inventory.dtos;

import java.math.BigDecimal;

import com.gymjf.backend.modules.inventory.domain.OrderStatus;

import lombok.Data;

@Data
public class UpdateOrderRequest {

    private OrderStatus status;

    private BigDecimal total;
}
