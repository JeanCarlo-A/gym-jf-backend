package com.gymjf.backend.modules.nutrition.dtos;

import com.gymjf.backend.modules.nutrition.domain.Goal;
import com.gymjf.backend.modules.nutrition.domain.MealTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeResponse {

    private Integer id;
    private String title;
    private String instructions;
    private Goal suggestedGoal;
    private MealTime mealTime;
    private String imageUrl;
    private String videoUrl;
    private Integer userId;

}
