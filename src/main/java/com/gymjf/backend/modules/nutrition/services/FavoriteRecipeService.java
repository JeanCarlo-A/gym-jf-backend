package com.gymjf.backend.modules.nutrition.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.nutrition.domain.FavoriteRecipe;
import com.gymjf.backend.modules.nutrition.domain.Recipe;
import com.gymjf.backend.modules.nutrition.dtos.CreateFavoriteRecipeRequest;
import com.gymjf.backend.modules.nutrition.dtos.FavoriteRecipeResponse;
import com.gymjf.backend.modules.nutrition.repositories.FavoriteRecipeRepository;
import com.gymjf.backend.modules.nutrition.repositories.RecipeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteRecipeService {

    private final FavoriteRecipeRepository favoriteRecipeRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    public List<FavoriteRecipeResponse> getByUserId(Integer userId) {
        return favoriteRecipeRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public FavoriteRecipeResponse create(CreateFavoriteRecipeRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        if (favoriteRecipeRepository.existsByUserIdAndRecipeId(request.getUserId(), request.getRecipeId())) {
            throw new RuntimeException("La receta ya está en favoritos");
        }

        FavoriteRecipe favoriteRecipe = FavoriteRecipe.builder()
                .user(user)
                .recipe(recipe)
                .build();

        favoriteRecipeRepository.save(favoriteRecipe);

        return mapToResponse(favoriteRecipe);
    }

    public void delete(Integer id) {
        FavoriteRecipe favoriteRecipe = favoriteRecipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Favorito no encontrado"));
        favoriteRecipeRepository.delete(favoriteRecipe);
    }

    private FavoriteRecipeResponse mapToResponse(FavoriteRecipe favoriteRecipe) {
        return FavoriteRecipeResponse.builder()
                .id(favoriteRecipe.getId())
                .userId(favoriteRecipe.getUser().getId())
                .recipeId(favoriteRecipe.getRecipe().getId())
                .savedAt(favoriteRecipe.getSavedAt())
                .build();
    }
}
