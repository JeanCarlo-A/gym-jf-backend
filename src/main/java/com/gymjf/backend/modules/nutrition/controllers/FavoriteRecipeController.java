package com.gymjf.backend.modules.nutrition.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.nutrition.dtos.CreateFavoriteRecipeRequest;
import com.gymjf.backend.modules.nutrition.dtos.FavoriteRecipeResponse;
import com.gymjf.backend.modules.nutrition.services.FavoriteRecipeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/favorite-recipes")
@RequiredArgsConstructor
public class FavoriteRecipeController {

    private final FavoriteRecipeService favoriteRecipeService;

    @GetMapping("")
    public ResponseEntity<List<FavoriteRecipeResponse>> getByUserId(@RequestParam Integer userId) {
        return ResponseEntity.ok(favoriteRecipeService.getByUserId(userId));
    }

    @PostMapping("")
    public ResponseEntity<FavoriteRecipeResponse> create(@Valid @RequestBody CreateFavoriteRecipeRequest request) {
        return ResponseEntity.ok(favoriteRecipeService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        favoriteRecipeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
