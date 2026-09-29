package com.gymjf.backend.modules.inventory.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.inventory.domain.Order;
import com.gymjf.backend.modules.inventory.domain.OrderDetail;
import com.gymjf.backend.modules.inventory.domain.Product;
import com.gymjf.backend.modules.inventory.dtos.CreateOrderDetailRequest;
import com.gymjf.backend.modules.inventory.dtos.OrderDetailResponse;
import com.gymjf.backend.modules.inventory.dtos.UpdateOrderDetailRequest;
import com.gymjf.backend.modules.inventory.repositories.OrderDetailRepository;
import com.gymjf.backend.modules.inventory.repositories.OrderRepository;
import com.gymjf.backend.modules.inventory.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public List<OrderDetailResponse> getAll() {
        return orderDetailRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderDetailResponse> getByOrderId(Integer orderId) {
        return orderDetailRepository.findByOrderId(orderId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderDetailResponse getById(Integer id) {
        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle de pedido no encontrado"));
        return mapToResponse(orderDetail);
    }

    public OrderDetailResponse create(CreateOrderDetailRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .product(product)
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .build();

        orderDetailRepository.save(orderDetail);

        return mapToResponse(orderDetail);
    }

    public OrderDetailResponse update(Integer id, UpdateOrderDetailRequest request) {
        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle de pedido no encontrado"));

        if (request.getQuantity() != null) {
            orderDetail.setQuantity(request.getQuantity());
        }
        if (request.getUnitPrice() != null) {
            orderDetail.setUnitPrice(request.getUnitPrice());
        }

        orderDetailRepository.save(orderDetail);

        return mapToResponse(orderDetail);
    }

    public void delete(Integer id) {
        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle de pedido no encontrado"));
        orderDetailRepository.delete(orderDetail);
    }

    private OrderDetailResponse mapToResponse(OrderDetail orderDetail) {
        return OrderDetailResponse.builder()
                .id(orderDetail.getId())
                .orderId(orderDetail.getOrder().getId())
                .productId(orderDetail.getProduct().getId())
                .quantity(orderDetail.getQuantity())
                .unitPrice(orderDetail.getUnitPrice())
                .build();
    }
}
