package com.gymjf.backend.modules.reservations.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.reservations.dtos.CreateReservationRequest;
import com.gymjf.backend.modules.reservations.dtos.ReservationResponse;
import com.gymjf.backend.modules.reservations.dtos.UpdateReservationRequest;
import com.gymjf.backend.modules.reservations.services.ReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping("")
    public ResponseEntity<List<ReservationResponse>> getAll(@RequestParam(required = false) Integer userId) {
        if (userId != null) {
            return ResponseEntity.ok(reservationService.getByUserId(userId));
        }
        return ResponseEntity.ok(reservationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(reservationService.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationRequest request) {
        return ResponseEntity.ok(reservationService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> update(@PathVariable Integer id,
            @Valid @RequestBody UpdateReservationRequest request) {
        return ResponseEntity.ok(reservationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
