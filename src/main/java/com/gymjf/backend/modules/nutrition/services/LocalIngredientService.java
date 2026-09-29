package com.gymjf.backend.modules.nutrition.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.nutrition.domain.LocalIngredient;
import com.gymjf.backend.modules.nutrition.dtos.CreateLocalIngredientRequest;
import com.gymjf.backend.modules.nutrition.dtos.LocalIngredientResponse;
import com.gymjf.backend.modules.nutrition.dtos.UpdateLocalIngredientRequest;
import com.gymjf.backend.modules.nutrition.repositories.LocalIngredientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalIngredientService {

    private final LocalIngredientRepository localIngredientRepository;

    public List<LocalIngredientResponse> getAll() {
        return localIngredientRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public LocalIngredientResponse getById(Integer id) {
        LocalIngredient ingredient = localIngredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));
        return mapToResponse(ingredient);
    }

    public LocalIngredientResponse create(CreateLocalIngredientRequest request) {
        LocalIngredient ingredient = LocalIngredient.builder()
                .name(request.getName())
                .imageUrl(request.getImageUrl())
                .build();

        localIngredientRepository.save(ingredient);

        return mapToResponse(ingredient);
    }

    public LocalIngredientResponse update(Integer id, UpdateLocalIngredientRequest request) {
        LocalIngredient ingredient = localIngredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));

        if (request.getName() != null) {
            ingredient.setName(request.getName());
        }
        if (request.getImageUrl() != null) {
            ingredient.setImageUrl(request.getImageUrl());
        }

        localIngredientRepository.save(ingredient);

        return mapToResponse(ingredient);
    }

    public void delete(Integer id) {
        LocalIngredient ingredient = localIngredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));
        localIngredientRepository.delete(ingredient);
    }

    private LocalIngredientResponse mapToResponse(LocalIngredient ingredient) {
        return LocalIngredientResponse.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .imageUrl(ingredient.getImageUrl())
                .build();
    }
}
