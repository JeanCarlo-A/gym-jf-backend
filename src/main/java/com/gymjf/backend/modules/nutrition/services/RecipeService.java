package com.gymjf.backend.modules.nutrition.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.nutrition.domain.Recipe;
import com.gymjf.backend.modules.nutrition.dtos.CreateRecipeRequest;
import com.gymjf.backend.modules.nutrition.dtos.RecipeResponse;
import com.gymjf.backend.modules.nutrition.dtos.UpdateRecipeRequest;
import com.gymjf.backend.modules.nutrition.repositories.RecipeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    public List<RecipeResponse> getAll() {
        return recipeRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RecipeResponse getById(Integer id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));
        return mapToResponse(recipe);
    }

    public RecipeResponse create(CreateRecipeRequest request) {
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        }

        Recipe recipe = Recipe.builder()
                .title(request.getTitle())
                .instructions(request.getInstructions())
                .suggestedGoal(request.getSuggestedGoal())
                .mealTime(request.getMealTime())
                .imageUrl(request.getImageUrl())
                .videoUrl(request.getVideoUrl())
                .user(user)
                .build();

        recipeRepository.save(recipe);

        return mapToResponse(recipe);
    }

    public RecipeResponse update(Integer id, UpdateRecipeRequest request) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        if (request.getTitle() != null) {
            recipe.setTitle(request.getTitle());
        }
        if (request.getInstructions() != null) {
            recipe.setInstructions(request.getInstructions());
        }
        if (request.getSuggestedGoal() != null) {
            recipe.setSuggestedGoal(request.getSuggestedGoal());
        }
        if (request.getMealTime() != null) {
            recipe.setMealTime(request.getMealTime());
        }
        if (request.getImageUrl() != null) {
            recipe.setImageUrl(request.getImageUrl());
        }
        if (request.getVideoUrl() != null) {
            recipe.setVideoUrl(request.getVideoUrl());
        }
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            recipe.setUser(user);
        }

        recipeRepository.save(recipe);

        return mapToResponse(recipe);
    }

    public void delete(Integer id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));
        recipeRepository.delete(recipe);
    }

    private RecipeResponse mapToResponse(Recipe recipe) {
        return RecipeResponse.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .instructions(recipe.getInstructions())
                .suggestedGoal(recipe.getSuggestedGoal())
                .mealTime(recipe.getMealTime())
                .imageUrl(recipe.getImageUrl())
                .videoUrl(recipe.getVideoUrl())
                .userId(recipe.getUser() != null ? recipe.getUser().getId() : null)
                .build();
    }
}
