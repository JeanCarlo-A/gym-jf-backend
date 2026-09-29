package com.gymjf.backend.modules.nutrition.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.nutrition.dtos.CreateRecipeRequest;
import com.gymjf.backend.modules.nutrition.dtos.RecipeResponse;
import com.gymjf.backend.modules.nutrition.dtos.UpdateRecipeRequest;
import com.gymjf.backend.modules.nutrition.services.RecipeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping("")
    public ResponseEntity<List<RecipeResponse>> getAll() {
        return ResponseEntity.ok(recipeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(recipeService.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<RecipeResponse> create(@Valid @RequestBody CreateRecipeRequest request) {
        return ResponseEntity.ok(recipeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> update(@PathVariable Integer id,
            @Valid @RequestBody UpdateRecipeRequest request) {
        return ResponseEntity.ok(recipeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        recipeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
