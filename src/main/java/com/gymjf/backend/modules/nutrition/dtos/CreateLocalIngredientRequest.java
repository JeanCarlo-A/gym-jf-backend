package com.gymjf.backend.modules.nutrition.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLocalIngredientRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String imageUrl;

}
