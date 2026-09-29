package com.gymjf.backend.modules.inventory.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.inventory.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserId(Integer userId);

    Optional<Order> findByCode(String code);
}
