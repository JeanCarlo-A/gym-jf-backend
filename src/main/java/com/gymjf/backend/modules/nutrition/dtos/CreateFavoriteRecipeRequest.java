package com.gymjf.backend.modules.nutrition.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateFavoriteRecipeRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Integer userId;

    @NotNull(message = "La receta es obligatoria")
    private Integer recipeId;

}
