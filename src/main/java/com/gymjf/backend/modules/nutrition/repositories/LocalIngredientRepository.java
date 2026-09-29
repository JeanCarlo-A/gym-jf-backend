package com.gymjf.backend.modules.nutrition.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.nutrition.domain.LocalIngredient;

public interface LocalIngredientRepository extends JpaRepository<LocalIngredient, Integer> {
}
