package com.gymjf.backend.modules.inventory.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.gymjf.backend.modules.inventory.domain.OrderStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {

    private Integer id;
    private String code;
    private BigDecimal total;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private Integer userId;
}
