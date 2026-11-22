package com.gymjf.backend.modules.billing.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.billing.dtos.CreateTransactionRequest;
import com.gymjf.backend.modules.billing.dtos.TransactionResponse;
import com.gymjf.backend.modules.billing.dtos.UpdateTransactionRequest;
import com.gymjf.backend.modules.billing.services.TransactionService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import com.gymjf.backend.shared.domain.dtos.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("")
    public ResponseEntity<PageResponse<TransactionResponse>> getAll(
            @RequestParam(required = false) Integer userId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        if (userId != null) {
            return ResponseEntity.ok(transactionService.getByUserId(userId, pageable));
        }
        return ResponseEntity.ok(transactionService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        return ResponseEntity.ok(transactionService.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
