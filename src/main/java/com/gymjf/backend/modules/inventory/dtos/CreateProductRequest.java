package com.gymjf.backend.modules.inventory.dtos;

import java.math.BigDecimal;

import com.gymjf.backend.modules.inventory.domain.ProductCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateProductRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotNull(message = "La categoría es obligatoria")
    private ProductCategory category;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    private BigDecimal price;

    @NotNull(message = "El stock es obligatorio")
    private Integer stock;

    @NotNull(message = "El stock mínimo de alerta es obligatorio")
    private Integer minStockAlert;

    private String imageUrl;
}
