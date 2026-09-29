package com.gymjf.backend.modules.nutrition.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteRecipeResponse {

    private Integer id;
    private Integer userId;
    private Integer recipeId;
    private LocalDateTime savedAt;

}
