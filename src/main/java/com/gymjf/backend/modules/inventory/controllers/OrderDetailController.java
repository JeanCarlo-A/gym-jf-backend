package com.gymjf.backend.modules.inventory.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gymjf.backend.modules.inventory.dtos.CreateOrderDetailRequest;
import com.gymjf.backend.modules.inventory.dtos.OrderDetailResponse;
import com.gymjf.backend.modules.inventory.dtos.UpdateOrderDetailRequest;
import com.gymjf.backend.modules.inventory.services.OrderDetailService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/order-details")
@RequiredArgsConstructor
public class OrderDetailController {

    private final OrderDetailService orderDetailService;

    @GetMapping("")
    public ResponseEntity<List<OrderDetailResponse>> getAll(@RequestParam(required = false) Integer orderId) {
        if (orderId != null) {
            return ResponseEntity.ok(orderDetailService.getByOrderId(orderId));
        }
        return ResponseEntity.ok(orderDetailService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderDetailService.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<OrderDetailResponse> create(@Valid @RequestBody CreateOrderDetailRequest request) {
        return ResponseEntity.ok(orderDetailService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> update(@PathVariable Integer id,
            @Valid @RequestBody UpdateOrderDetailRequest request) {
        return ResponseEntity.ok(orderDetailService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        orderDetailService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
