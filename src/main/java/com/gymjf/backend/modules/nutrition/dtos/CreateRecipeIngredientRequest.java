package com.gymjf.backend.modules.nutrition.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRecipeIngredientRequest {

    private String suggestedQuantity;

    private String localSubstitute;

    @NotNull(message = "La receta es obligatoria")
    private Integer recipeId;

    @NotNull(message = "El ingrediente es obligatorio")
    private Integer ingredientId;

}
