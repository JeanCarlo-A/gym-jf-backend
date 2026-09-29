package com.gymjf.backend.modules.inventory.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.inventory.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
