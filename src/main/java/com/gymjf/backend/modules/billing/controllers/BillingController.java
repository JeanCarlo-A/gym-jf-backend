package com.gymjf.backend.modules.billing.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gymjf.backend.modules.billing.dtos.BillingSummaryResponse;
import com.gymjf.backend.modules.billing.dtos.CashClosingResponse;
import com.gymjf.backend.modules.billing.dtos.DelinquencyResponse;
import com.gymjf.backend.modules.billing.services.BillingReportService;
import com.gymjf.backend.modules.billing.services.DelinquencyService;

import lombok.RequiredArgsConstructor;

/** Morosidad y reportes de facturación. */
@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final DelinquencyService delinquencyService;
    private final BillingReportService billingReportService;

    /** Estado de morosidad de un usuario: el dueño o un administrador. */
    @GetMapping("/delinquency/{userId}")
    public ResponseEntity<DelinquencyResponse> delinquency(@PathVariable Integer userId) {
        return ResponseEntity.ok(delinquencyService.status(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delinquents")
    public ResponseEntity<List<DelinquencyResponse>> delinquents() {
        return ResponseEntity.ok(delinquencyService.listDelinquent());
    }

    /** Ejecuta ya la revisión de morosidad que corre sola cada noche. Devuelve cuántas deudas cambiaron de estado. */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delinquency/mark")
    public ResponseEntity<Map<String, Integer>> mark() {
        return ResponseEntity.ok(Map.of("updated", delinquencyService.markDelinquent()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/cash-closing")
    public ResponseEntity<CashClosingResponse> cashClosing(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(billingReportService.cashClosing(date));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/summary")
    public ResponseEntity<BillingSummaryResponse> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(billingReportService.summary(from, to));
    }
}
