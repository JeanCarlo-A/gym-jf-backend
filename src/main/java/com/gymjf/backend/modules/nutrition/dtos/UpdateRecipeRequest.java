package com.gymjf.backend.modules.nutrition.dtos;

import com.gymjf.backend.modules.nutrition.domain.Goal;
import com.gymjf.backend.modules.nutrition.domain.MealTime;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateRecipeRequest {

    @Size(min = 1, max = 255, message = "El título no puede estar vacío ni superar los 255 caracteres")
    private String title;

    @Size(max = 65535, message = "Las instrucciones no puede superar los 65535 caracteres")
    private String instructions;

    private Goal suggestedGoal;

    private MealTime mealTime;

    @Size(max = 255, message = "La URL de la imagen no puede superar los 255 caracteres")
    private String imageUrl;

    @Size(max = 255, message = "La URL del video no puede superar los 255 caracteres")
    private String videoUrl;

    private Integer userId;

}
