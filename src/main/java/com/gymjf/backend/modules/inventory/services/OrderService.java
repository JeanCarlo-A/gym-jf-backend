package com.gymjf.backend.modules.inventory.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.inventory.domain.Order;
import com.gymjf.backend.modules.inventory.domain.OrderStatus;
import com.gymjf.backend.modules.inventory.dtos.CreateOrderRequest;
import com.gymjf.backend.modules.inventory.dtos.OrderResponse;
import com.gymjf.backend.modules.inventory.dtos.UpdateOrderRequest;
import com.gymjf.backend.modules.inventory.repositories.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getByUserId(Integer userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getById(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        return mapToResponse(order);
    }

    public OrderResponse create(CreateOrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Order order = Order.builder()
                .code(request.getCode())
                .total(request.getTotal())
                .status(request.getStatus() != null ? request.getStatus() : OrderStatus.PENDING)
                .user(user)
                .build();

        orderRepository.save(order);

        return mapToResponse(order);
    }

    public OrderResponse update(Integer id, UpdateOrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }
        if (request.getTotal() != null) {
            order.setTotal(request.getTotal());
        }

        orderRepository.save(order);

        return mapToResponse(order);
    }

    public void delete(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
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
