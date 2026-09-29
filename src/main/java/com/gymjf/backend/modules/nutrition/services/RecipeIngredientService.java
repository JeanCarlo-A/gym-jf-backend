package com.gymjf.backend.modules.nutrition.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.nutrition.domain.LocalIngredient;
import com.gymjf.backend.modules.nutrition.domain.Recipe;
import com.gymjf.backend.modules.nutrition.domain.RecipeIngredient;
import com.gymjf.backend.modules.nutrition.dtos.CreateRecipeIngredientRequest;
import com.gymjf.backend.modules.nutrition.dtos.RecipeIngredientResponse;
import com.gymjf.backend.modules.nutrition.dtos.UpdateRecipeIngredientRequest;
import com.gymjf.backend.modules.nutrition.repositories.LocalIngredientRepository;
import com.gymjf.backend.modules.nutrition.repositories.RecipeIngredientRepository;
import com.gymjf.backend.modules.nutrition.repositories.RecipeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeIngredientService {

    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeRepository recipeRepository;
    private final LocalIngredientRepository localIngredientRepository;

    public List<RecipeIngredientResponse> getAll() {
        return recipeIngredientRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<RecipeIngredientResponse> getByRecipeId(Integer recipeId) {
        return recipeIngredientRepository.findByRecipeId(recipeId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RecipeIngredientResponse getById(Integer id) {
        RecipeIngredient recipeIngredient = recipeIngredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente de receta no encontrado"));
        return mapToResponse(recipeIngredient);
    }

    public RecipeIngredientResponse create(CreateRecipeIngredientRequest request) {
        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        LocalIngredient ingredient = localIngredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));

        RecipeIngredient recipeIngredient = RecipeIngredient.builder()
                .suggestedQuantity(request.getSuggestedQuantity())
                .localSubstitute(request.getLocalSubstitute())
                .recipe(recipe)
                .ingredient(ingredient)
                .build();

        recipeIngredientRepository.save(recipeIngredient);

        return mapToResponse(recipeIngredient);
    }

    public RecipeIngredientResponse update(Integer id, UpdateRecipeIngredientRequest request) {
        RecipeIngredient recipeIngredient = recipeIngredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente de receta no encontrado"));

        if (request.getSuggestedQuantity() != null) {
            recipeIngredient.setSuggestedQuantity(request.getSuggestedQuantity());
        }
        if (request.getLocalSubstitute() != null) {
            recipeIngredient.setLocalSubstitute(request.getLocalSubstitute());
        }

        recipeIngredientRepository.save(recipeIngredient);

        return mapToResponse(recipeIngredient);
    }

    public void delete(Integer id) {
        RecipeIngredient recipeIngredient = recipeIngredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente de receta no encontrado"));
        recipeIngredientRepository.delete(recipeIngredient);
    }

    private RecipeIngredientResponse mapToResponse(RecipeIngredient recipeIngredient) {
        return RecipeIngredientResponse.builder()
                .id(recipeIngredient.getId())
                .suggestedQuantity(recipeIngredient.getSuggestedQuantity())
                .localSubstitute(recipeIngredient.getLocalSubstitute())
                .recipeId(recipeIngredient.getRecipe().getId())
                .ingredientId(recipeIngredient.getIngredient().getId())
                .build();
    }
}
