package com.gymjf.backend.modules.routines.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.routines.dtos.CreateRoutineRequest;
import com.gymjf.backend.modules.routines.dtos.RoutineResponse;
import com.gymjf.backend.modules.routines.dtos.UpdateRoutineRequest;
import com.gymjf.backend.modules.routines.services.RoutineService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/routines")
@RequiredArgsConstructor
public class RoutineController {

    private final RoutineService routineService;

    @GetMapping("")
    public ResponseEntity<List<RoutineResponse>> getAll(
            @RequestParam(required = false) Integer userId) {
        if (userId != null) {
            return ResponseEntity.ok(routineService.getByUserId(userId));
        }
        return ResponseEntity.ok(routineService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoutineResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(routineService.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<RoutineResponse> create(@Valid @RequestBody CreateRoutineRequest request) {
        return ResponseEntity.ok(routineService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoutineResponse> update(@PathVariable Integer id,
            @Valid @RequestBody UpdateRoutineRequest request) {
        return ResponseEntity.ok(routineService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        routineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
