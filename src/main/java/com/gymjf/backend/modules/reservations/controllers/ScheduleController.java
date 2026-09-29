package com.gymjf.backend.modules.reservations.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.reservations.dtos.CreateScheduleRequest;
import com.gymjf.backend.modules.reservations.dtos.ScheduleResponse;
import com.gymjf.backend.modules.reservations.dtos.UpdateScheduleRequest;
import com.gymjf.backend.modules.reservations.services.ScheduleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("")
    public ResponseEntity<List<ScheduleResponse>> getAll() {
        return ResponseEntity.ok(scheduleService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(scheduleService.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<ScheduleResponse> create(@Valid @RequestBody CreateScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponse> update(@PathVariable Integer id,
            @Valid @RequestBody UpdateScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        scheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
