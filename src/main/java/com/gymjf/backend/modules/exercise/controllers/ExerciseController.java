package com.gymjf.backend.modules.exercise.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.exercise.dtos.CreateExerciseRequest;
import com.gymjf.backend.modules.exercise.dtos.ExerciseResponse;
import com.gymjf.backend.modules.exercise.dtos.UpdateExerciseRequest;
import com.gymjf.backend.modules.exercise.services.ExerciseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping("")
    public ResponseEntity<List<ExerciseResponse>> getAll() {
        return ResponseEntity.ok(exerciseService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(exerciseService.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<ExerciseResponse> create(@Valid @RequestBody CreateExerciseRequest request) {
        return ResponseEntity.ok(exerciseService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExerciseResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateExerciseRequest request) {
        return ResponseEntity.ok(exerciseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        exerciseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
