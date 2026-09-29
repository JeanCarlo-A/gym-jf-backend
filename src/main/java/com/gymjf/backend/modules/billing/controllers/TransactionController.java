package com.gymjf.backend.modules.billing.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.billing.dtos.CreateTransactionRequest;
import com.gymjf.backend.modules.billing.dtos.TransactionResponse;
import com.gymjf.backend.modules.billing.dtos.UpdateTransactionRequest;
import com.gymjf.backend.modules.billing.services.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("")
    public ResponseEntity<List<TransactionResponse>> getAll(
            @RequestParam(required = false) Integer userId) {
        if (userId != null) {
            return ResponseEntity.ok(transactionService.getByUserId(userId));
        }
        return ResponseEntity.ok(transactionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.ok(transactionService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        return ResponseEntity.ok(transactionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
