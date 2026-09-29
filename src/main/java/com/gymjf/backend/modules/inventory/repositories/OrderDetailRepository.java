package com.gymjf.backend.modules.inventory.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.inventory.domain.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    List<OrderDetail> findByOrderId(Integer orderId);
}
