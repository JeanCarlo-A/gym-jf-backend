package com.gymjf.backend.modules.nutrition.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.nutrition.dtos.CreateLocalIngredientRequest;
import com.gymjf.backend.modules.nutrition.dtos.LocalIngredientResponse;
import com.gymjf.backend.modules.nutrition.dtos.UpdateLocalIngredientRequest;
import com.gymjf.backend.modules.nutrition.services.LocalIngredientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/local-ingredients")
@RequiredArgsConstructor
public class LocalIngredientController {

    private final LocalIngredientService localIngredientService;

    @GetMapping("")
    public ResponseEntity<List<LocalIngredientResponse>> getAll() {
        return ResponseEntity.ok(localIngredientService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocalIngredientResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(localIngredientService.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<LocalIngredientResponse> create(@Valid @RequestBody CreateLocalIngredientRequest request) {
        return ResponseEntity.ok(localIngredientService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocalIngredientResponse> update(@PathVariable Integer id,
            @Valid @RequestBody UpdateLocalIngredientRequest request) {
        return ResponseEntity.ok(localIngredientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        localIngredientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
