package com.gymjf.backend.modules.memberships.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.memberships.dtos.CreateUserSubscriptionRequest;
import com.gymjf.backend.modules.memberships.dtos.UpdateUserSubscriptionRequest;
import com.gymjf.backend.modules.memberships.dtos.UserSubscriptionResponse;
import com.gymjf.backend.modules.memberships.services.UserSubscriptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

    @PostMapping("")
    public ResponseEntity<UserSubscriptionResponse> create(@Valid @RequestBody CreateUserSubscriptionRequest request) {
        return ResponseEntity.ok(userSubscriptionService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserSubscriptionResponse> update(@PathVariable Integer id,
            @Valid @RequestBody UpdateUserSubscriptionRequest request) {
        return ResponseEntity.ok(userSubscriptionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        userSubscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
