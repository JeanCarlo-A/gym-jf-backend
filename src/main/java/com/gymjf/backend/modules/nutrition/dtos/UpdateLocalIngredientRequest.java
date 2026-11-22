package com.gymjf.backend.modules.nutrition.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateLocalIngredientRequest {

    @Size(min = 1, max = 255, message = "El nombre no puede estar vacío ni superar los 255 caracteres")
    private String name;

    @Size(max = 255, message = "La URL de la imagen no puede superar los 255 caracteres")
    private String imageUrl;

}
