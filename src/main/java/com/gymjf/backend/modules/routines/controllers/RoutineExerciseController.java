package com.gymjf.backend.modules.routines.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.routines.dtos.CreateRoutineExerciseRequest;
import com.gymjf.backend.modules.routines.dtos.RoutineExerciseResponse;
import com.gymjf.backend.modules.routines.dtos.UpdateRoutineExerciseRequest;
import com.gymjf.backend.modules.routines.services.RoutineExerciseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/routine-exercises")
@RequiredArgsConstructor
public class RoutineExerciseController {

    private final RoutineExerciseService routineExerciseService;

    @GetMapping("")
    public ResponseEntity<List<RoutineExerciseResponse>> getAll(
            @RequestParam(required = false) Integer routineId) {
        if (routineId != null) {
            return ResponseEntity.ok(routineExerciseService.getByRoutineId(routineId));
        }
        return ResponseEntity.ok(routineExerciseService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoutineExerciseResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(routineExerciseService.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<RoutineExerciseResponse> create(
            @Valid @RequestBody CreateRoutineExerciseRequest request) {
        return ResponseEntity.ok(routineExerciseService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoutineExerciseResponse> update(@PathVariable Integer id,
            @Valid @RequestBody UpdateRoutineExerciseRequest request) {
        return ResponseEntity.ok(routineExerciseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        routineExerciseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
