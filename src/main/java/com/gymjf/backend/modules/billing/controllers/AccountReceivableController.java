package com.gymjf.backend.modules.billing.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.billing.dtos.AccountReceivableResponse;
import com.gymjf.backend.modules.billing.dtos.CreateAccountReceivableRequest;
import com.gymjf.backend.modules.billing.dtos.UpdateAccountReceivableRequest;
import com.gymjf.backend.modules.billing.services.AccountReceivableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/accounts-receivable")
@RequiredArgsConstructor
public class AccountReceivableController {

    private final AccountReceivableService accountReceivableService;

    @GetMapping("")
    public ResponseEntity<List<AccountReceivableResponse>> getAll(
            @RequestParam(required = false) Integer userId) {
        if (userId != null) {
            return ResponseEntity.ok(accountReceivableService.getByUserId(userId));
        }
        return ResponseEntity.ok(accountReceivableService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountReceivableResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(accountReceivableService.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<AccountReceivableResponse> create(
            @Valid @RequestBody CreateAccountReceivableRequest request) {
        return ResponseEntity.ok(accountReceivableService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountReceivableResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateAccountReceivableRequest request) {
        return ResponseEntity.ok(accountReceivableService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        accountReceivableService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
