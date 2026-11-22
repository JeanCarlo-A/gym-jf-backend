package com.gymjf.backend.modules.billing.services;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.billing.domain.AccountReceivable;
import com.gymjf.backend.modules.billing.domain.Transaction;
import com.gymjf.backend.modules.billing.domain.TransactionValidationStatus;
import com.gymjf.backend.modules.billing.dtos.CreateTransactionRequest;
import com.gymjf.backend.modules.billing.dtos.TransactionResponse;
import com.gymjf.backend.modules.billing.dtos.UpdateTransactionRequest;
import com.gymjf.backend.modules.billing.repositories.AccountReceivableRepository;
import com.gymjf.backend.modules.billing.repositories.TransactionRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.gymjf.backend.shared.domain.dtos.PageResponse;
import lombok.RequiredArgsConstructor;
import com.gymjf.backend.shared.infrastructure.security.CurrentUser;
import com.gymjf.backend.shared.domain.exceptions.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;
    private final AccountReceivableRepository accountReceivableRepository;

    public PageResponse<TransactionResponse> getAll(Pageable pageable) {
        Page<Transaction> page = currentUser.isAdmin()
                ? transactionRepository.findAll(pageable)
                : transactionRepository.findByUserId(currentUser.id(), pageable);
        return PageResponse.from(page, this::mapToResponse);
    }

    public PageResponse<TransactionResponse> getByUserId(Integer userId, Pageable pageable) {
        currentUser.requireOwnerOrAdmin(userId);
        return PageResponse.from(transactionRepository.findByUserId(userId, pageable), this::mapToResponse);
    }

    public TransactionResponse getById(Integer id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada"));
        currentUser.requireOwnerOrAdmin(transaction.getUser().getId());
        return mapToResponse(transaction);
    }

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        AccountReceivable accountReceivable = null;
        if (request.getAccountReceivableId() != null) {
            accountReceivable = accountReceivableRepository.findById(request.getAccountReceivableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cuenta por cobrar no encontrada"));
        }

        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(request.getReferenceNumber())
                .receiptImageUrl(request.getReceiptImageUrl())
                .validationStatus(request.getValidationStatus() != null
                        ? request.getValidationStatus()
                        : TransactionValidationStatus.PENDING)
                .accountReceivable(accountReceivable)
                .build();

        transactionRepository.save(transaction);

        return mapToResponse(transaction);
    }

    @Transactional
    public TransactionResponse update(Integer id, UpdateTransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada"));

        if (request.getValidationStatus() != null) {
            transaction.setValidationStatus(request.getValidationStatus());
        }

        if (request.getReceiptImageUrl() != null) {
            transaction.setReceiptImageUrl(request.getReceiptImageUrl());
        }

        transactionRepository.save(transaction);

        return mapToResponse(transaction);
    }

    @Transactional
    public void delete(Integer id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada"));
        transactionRepository.delete(transaction);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUser().getId())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod())
                .referenceNumber(transaction.getReferenceNumber())
                .receiptImageUrl(transaction.getReceiptImageUrl())
                .validationStatus(transaction.getValidationStatus())
                .transactionDate(transaction.getTransactionDate())
                .accountReceivableId(transaction.getAccountReceivable() != null
                        ? transaction.getAccountReceivable().getId()
                        : null)
                .build();
    }
}
