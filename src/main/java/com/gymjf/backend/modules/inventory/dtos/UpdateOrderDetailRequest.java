package com.gymjf.backend.modules.inventory.dtos;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class UpdateOrderDetailRequest {

    private Integer quantity;

    private BigDecimal unitPrice;
}
