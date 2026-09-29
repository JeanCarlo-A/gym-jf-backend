package com.gymjf.backend.modules.inventory.dtos;

import java.math.BigDecimal;

import com.gymjf.backend.modules.inventory.domain.ProductCategory;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {

    private Integer id;
    private String name;
    private ProductCategory category;
    private BigDecimal price;
    private Integer stock;
    private Integer minStockAlert;
    private String imageUrl;
}
