package com.gymjf.backend.modules.biometrics.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.biometrics.dtos.AnthropometricRecordResponse;
import com.gymjf.backend.modules.biometrics.dtos.CreateAnthropometricRecordRequest;
import com.gymjf.backend.modules.biometrics.dtos.UpdateAnthropometricRecordRequest;
import com.gymjf.backend.modules.biometrics.services.AnthropometricRecordService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import com.gymjf.backend.shared.domain.dtos.PageResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/anthropometric-records")
@RequiredArgsConstructor
public class AnthropometricRecordController {

    private final AnthropometricRecordService anthropometricRecordService;

    @GetMapping("")
    public ResponseEntity<PageResponse<AnthropometricRecordResponse>> getAll(
            @RequestParam(required = false) Integer userId,
            @PageableDefault(size = 20, sort = "recordDate", direction = Sort.Direction.DESC) Pageable pageable) {
        if (userId != null) {
            return ResponseEntity.ok(anthropometricRecordService.getByUserId(userId, pageable));
        }
        return ResponseEntity.ok(anthropometricRecordService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnthropometricRecordResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(anthropometricRecordService.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<AnthropometricRecordResponse> create(
            @Valid @RequestBody CreateAnthropometricRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(anthropometricRecordService.create(request));
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
