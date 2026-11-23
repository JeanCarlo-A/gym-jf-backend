package com.gymjf.backend.modules.billing.controllers;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.billing.domain.PaymentMethod;
import com.gymjf.backend.modules.billing.domain.TransactionValidationStatus;
import com.gymjf.backend.modules.billing.dtos.CreateTransactionRequest;
import com.gymjf.backend.modules.billing.dtos.RegisterSinpeRequest;
import com.gymjf.backend.modules.billing.dtos.SubmitPaymentRequest;
import com.gymjf.backend.modules.billing.dtos.TransactionResponse;
import com.gymjf.backend.modules.billing.dtos.UpdateTransactionRequest;
import com.gymjf.backend.modules.billing.services.TransactionService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import com.gymjf.backend.shared.domain.dtos.PageResponse;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /** Filtros opcionales: userId, status, method y rango de fechas (from/to, ambas inclusive). */
    @GetMapping("")
    public ResponseEntity<PageResponse<TransactionResponse>> getAll(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) TransactionValidationStatus status,
            @RequestParam(required = false) PaymentMethod method,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(transactionService.search(userId, status, method, from, to, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    /** El cliente envía su pago SINPE Móvil con el comprobante; queda pendiente de aprobación. */
    @PostMapping("/submit")
    public ResponseEntity<TransactionResponse> submit(@Valid @RequestBody SubmitPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.submit(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(request));
    }

    /** SINPE ya verificado por el administrador: se registra aprobado (usa sp_procesar_pago_sinpe). */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sinpe")
    public ResponseEntity<TransactionResponse> registerSinpe(@Valid @RequestBody RegisterSinpeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.registerSinpe(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<TransactionResponse> approve(@PathVariable Integer id) {
        return ResponseEntity.ok(transactionService.approve(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<TransactionResponse> reject(@PathVariable Integer id) {
        return ResponseEntity.ok(transactionService.reject(id));
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
