package com.gymjf.backend.modules.nutrition.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.nutrition.domain.FavoriteRecipe;

public interface FavoriteRecipeRepository extends JpaRepository<FavoriteRecipe, Integer> {
    List<FavoriteRecipe> findByUserId(Integer userId);

    boolean existsByUserIdAndRecipeId(Integer userId, Integer recipeId);
}
