package com.gymjf.backend.modules.memberships.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.memberships.dtos.CreateSubscriptionPlanRequest;
import com.gymjf.backend.modules.memberships.dtos.SubscriptionPlanResponse;
import com.gymjf.backend.modules.memberships.dtos.UpdateSubscriptionPlanRequest;
import com.gymjf.backend.modules.memberships.services.SubscriptionPlanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping("")
    public ResponseEntity<List<SubscriptionPlanResponse>> getAll() {
        return ResponseEntity.ok(subscriptionPlanService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPlanResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(subscriptionPlanService.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<SubscriptionPlanResponse> create(@Valid @RequestBody CreateSubscriptionPlanRequest request) {
        return ResponseEntity.ok(subscriptionPlanService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionPlanResponse> update(@PathVariable Integer id,
            @Valid @RequestBody UpdateSubscriptionPlanRequest request) {
        return ResponseEntity.ok(subscriptionPlanService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        subscriptionPlanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
