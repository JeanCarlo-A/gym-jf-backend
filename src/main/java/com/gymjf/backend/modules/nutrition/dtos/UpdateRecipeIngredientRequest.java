package com.gymjf.backend.modules.nutrition.dtos;

import lombok.Data;

@Data
public class UpdateRecipeIngredientRequest {

    private String suggestedQuantity;

    private String localSubstitute;

}
