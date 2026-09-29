package com.gymjf.backend.modules.biometrics.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.biometrics.dtos.AnthropometricRecordResponse;
import com.gymjf.backend.modules.biometrics.dtos.CreateAnthropometricRecordRequest;
import com.gymjf.backend.modules.biometrics.dtos.UpdateAnthropometricRecordRequest;
import com.gymjf.backend.modules.biometrics.services.AnthropometricRecordService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/anthropometric-records")
@RequiredArgsConstructor
public class AnthropometricRecordController {

    private final AnthropometricRecordService anthropometricRecordService;

    @GetMapping("")
    public ResponseEntity<List<AnthropometricRecordResponse>> getAll(
            @RequestParam(required = false) Integer userId) {
        if (userId != null) {
            return ResponseEntity.ok(anthropometricRecordService.getByUserId(userId));
        }

        return ResponseEntity.ok(anthropometricRecordService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnthropometricRecordResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(anthropometricRecordService.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<AnthropometricRecordResponse> create(
            @Valid @RequestBody CreateAnthropometricRecordRequest request) {
        return ResponseEntity.ok(anthropometricRecordService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnthropometricRecordResponse> update(@PathVariable Integer id,
            @Valid @RequestBody UpdateAnthropometricRecordRequest request) {
        return ResponseEntity.ok(anthropometricRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        anthropometricRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
