package com.gymjf.backend.modules.billing.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gymjf.backend.modules.billing.domain.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Page<Transaction> findByUserId(Integer userId, Pageable pageable);

    List<Transaction> findByUserId(Integer userId);

    Optional<Transaction> findByReferenceNumber(String referenceNumber);
}
