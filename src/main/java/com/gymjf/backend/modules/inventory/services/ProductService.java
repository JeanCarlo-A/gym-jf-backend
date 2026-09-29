package com.gymjf.backend.modules.inventory.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gymjf.backend.modules.inventory.domain.Product;
import com.gymjf.backend.modules.inventory.dtos.CreateProductRequest;
import com.gymjf.backend.modules.inventory.dtos.ProductResponse;
import com.gymjf.backend.modules.inventory.dtos.UpdateProductRequest;
import com.gymjf.backend.modules.inventory.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return mapToResponse(product);
    }

    public ProductResponse create(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .category(request.getCategory())
                .price(request.getPrice())
                .stock(request.getStock())
                .minStockAlert(request.getMinStockAlert())
                .imageUrl(request.getImageUrl())
                .build();

        productRepository.save(product);

        return mapToResponse(product);
    }

    public ProductResponse update(Integer id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getMinStockAlert() != null) {
            product.setMinStockAlert(request.getMinStockAlert());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        productRepository.save(product);

        return mapToResponse(product);
    }

    public void delete(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        productRepository.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stock(product.getStock())
                .minStockAlert(product.getMinStockAlert())
                .imageUrl(product.getImageUrl())
                .build();
    }
}
