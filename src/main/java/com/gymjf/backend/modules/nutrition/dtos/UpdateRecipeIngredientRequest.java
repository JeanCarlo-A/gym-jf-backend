package com.gymjf.backend.modules.nutrition.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateRecipeIngredientRequest {

    @Size(max = 45, message = "La cantidad sugerida no puede superar los 45 caracteres")
    private String suggestedQuantity;

    @Size(max = 45, message = "El sustituto local no puede superar los 45 caracteres")
    private String localSubstitute;

}
