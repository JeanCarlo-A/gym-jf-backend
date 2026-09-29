package com.gymjf.backend.modules.nutrition.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.nutrition.dtos.CreateRecipeIngredientRequest;
import com.gymjf.backend.modules.nutrition.dtos.RecipeIngredientResponse;
import com.gymjf.backend.modules.nutrition.dtos.UpdateRecipeIngredientRequest;
import com.gymjf.backend.modules.nutrition.services.RecipeIngredientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/recipe-ingredients")
@RequiredArgsConstructor
public class RecipeIngredientController {

    private final RecipeIngredientService recipeIngredientService;

    @GetMapping("")
    public ResponseEntity<List<RecipeIngredientResponse>> getAll(
            @RequestParam(required = false) Integer recipeId) {
        if (recipeId != null) {
            return ResponseEntity.ok(recipeIngredientService.getByRecipeId(recipeId));
        }
        return ResponseEntity.ok(recipeIngredientService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeIngredientResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(recipeIngredientService.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<RecipeIngredientResponse> create(
            @Valid @RequestBody CreateRecipeIngredientRequest request) {
        return ResponseEntity.ok(recipeIngredientService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeIngredientResponse> update(@PathVariable Integer id,
            @Valid @RequestBody UpdateRecipeIngredientRequest request) {
        return ResponseEntity.ok(recipeIngredientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        recipeIngredientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
