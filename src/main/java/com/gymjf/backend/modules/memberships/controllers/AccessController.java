package com.gymjf.backend.modules.memberships.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gymjf.backend.modules.memberships.dtos.AccessCheckResponse;
import com.gymjf.backend.modules.memberships.dtos.GymSettingsResponse;
import com.gymjf.backend.modules.memberships.dtos.UpdateGymSettingsRequest;
import com.gymjf.backend.modules.memberships.services.AccessService;
import com.gymjf.backend.modules.memberships.services.GymSettingsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccessController {

    private final AccessService accessService;
    private final GymSettingsService gymSettingsService;

    /** ¿Puede esta persona entrar ahora? Lo consulta el dueño de la cuenta o un administrador. */
    @GetMapping("/access/check")
    public ResponseEntity<AccessCheckResponse> check(@RequestParam Integer userId) {
        return ResponseEntity.ok(accessService.check(userId));
    }

    @GetMapping("/gym-settings")
    public ResponseEntity<GymSettingsResponse> getSettings() {
        return ResponseEntity.ok(gymSettingsService.get());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/gym-settings")
    public ResponseEntity<GymSettingsResponse> updateSettings(@Valid @RequestBody UpdateGymSettingsRequest request) {
        return ResponseEntity.ok(gymSettingsService.update(request));
    }
}
