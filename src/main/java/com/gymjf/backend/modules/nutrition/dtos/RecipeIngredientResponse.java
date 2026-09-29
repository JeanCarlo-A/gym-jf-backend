package com.gymjf.backend.modules.nutrition.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeIngredientResponse {

    private Integer id;
    private String suggestedQuantity;
    private String localSubstitute;
    private Integer recipeId;
    private Integer ingredientId;

}
