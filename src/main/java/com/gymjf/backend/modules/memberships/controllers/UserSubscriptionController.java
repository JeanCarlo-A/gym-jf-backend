package com.gymjf.backend.modules.memberships.controllers;

import java.util.List;
import java.util.Map;

import com.gymjf.backend.modules.memberships.dtos.RenewSubscriptionRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.memberships.dtos.CreateUserSubscriptionRequest;
import com.gymjf.backend.modules.memberships.dtos.UpdateUserSubscriptionRequest;
import com.gymjf.backend.modules.memberships.dtos.UserSubscriptionResponse;
import com.gymjf.backend.modules.memberships.services.UserSubscriptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/user-subscriptions")
@RequiredArgsConstructor
public class UserSubscriptionController {

    private final UserSubscriptionService userSubscriptionService;

    @GetMapping("")
    public ResponseEntity<List<UserSubscriptionResponse>> getAll(
            @RequestParam(required = false) Integer userId) {
        if (userId != null) {
            return ResponseEntity.ok(userSubscriptionService.getByUserId(userId));
        }
        return ResponseEntity.ok(userSubscriptionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserSubscriptionResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(userSubscriptionService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<UserSubscriptionResponse> create(@Valid @RequestBody CreateUserSubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userSubscriptionService.create(request));
    }

    /** Compra o renueva un plan (queda PENDING hasta que se pague). El cliente solo para sí mismo. */
    @PostMapping("/renew")
    public ResponseEntity<UserSubscriptionResponse> renew(@Valid @RequestBody RenewSubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userSubscriptionService.renew(request));
    }

    /** Ejecuta ya la revisión de vencimientos que corre sola todos los días a las 00:00. */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/expire-due")
    public ResponseEntity<Map<String, Integer>> expireDue() {
        return ResponseEntity.ok(Map.of("expired", userSubscriptionService.expireDue()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserSubscriptionResponse> update(@PathVariable Integer id,
            @Valid @RequestBody UpdateUserSubscriptionRequest request) {
        return ResponseEntity.ok(userSubscriptionService.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        userSubscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
