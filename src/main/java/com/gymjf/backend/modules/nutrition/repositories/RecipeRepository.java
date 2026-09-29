package com.gymjf.backend.modules.nutrition.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.nutrition.domain.Recipe;

public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
}
