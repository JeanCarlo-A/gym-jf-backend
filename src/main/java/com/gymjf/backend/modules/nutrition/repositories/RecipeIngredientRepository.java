package com.gymjf.backend.modules.nutrition.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.nutrition.domain.RecipeIngredient;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Integer> {
    List<RecipeIngredient> findByRecipeId(Integer recipeId);
}
