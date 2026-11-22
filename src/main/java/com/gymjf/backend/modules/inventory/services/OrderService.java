package com.gymjf.backend.modules.inventory.services;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.inventory.domain.Order;
import com.gymjf.backend.modules.inventory.domain.OrderStatus;
import com.gymjf.backend.modules.inventory.dtos.CreateOrderRequest;
import com.gymjf.backend.modules.inventory.dtos.OrderResponse;
import com.gymjf.backend.modules.inventory.dtos.UpdateOrderRequest;
import com.gymjf.backend.modules.inventory.repositories.OrderRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.gymjf.backend.shared.domain.dtos.PageResponse;
import lombok.RequiredArgsConstructor;
import com.gymjf.backend.shared.infrastructure.security.CurrentUser;
import com.gymjf.backend.shared.domain.exceptions.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;

    public PageResponse<OrderResponse> getAll(Pageable pageable) {
        Page<Order> page = currentUser.isAdmin()
                ? orderRepository.findAll(pageable)
                : orderRepository.findByUserId(currentUser.id(), pageable);
        return PageResponse.from(page, this::mapToResponse);
    }

    public PageResponse<OrderResponse> getByUserId(Integer userId, Pageable pageable) {
        currentUser.requireOwnerOrAdmin(userId);
        return PageResponse.from(orderRepository.findByUserId(userId, pageable), this::mapToResponse);
    }

    public OrderResponse getById(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        currentUser.requireOwnerOrAdmin(order.getUser().getId());
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        currentUser.requireOwnerOrAdmin(request.getUserId());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Order order = Order.builder()
                .code(request.getCode())
                .total(request.getTotal())
                .status(currentUser.isAdmin() && request.getStatus() != null ? request.getStatus() : OrderStatus.PENDING)
                .user(user)
                .build();

        orderRepository.save(order);

        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse update(Integer id, UpdateOrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }
        if (request.getTotal() != null) {
            order.setTotal(request.getTotal());
        }

        orderRepository.save(order);

        return mapToResponse(order);
    }

    @Transactional
    public void delete(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        orderRepository.delete(order);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .code(order.getCode())
                .total(order.getTotal())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .userId(order.getUser().getId())
                .build();
    }
}
