package com.gymjf.backend.modules.billing.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountReceivableRepository accountReceivableRepository;

    public List<TransactionResponse> getAll() {
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getByUserId(Integer userId) {
        return transactionRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse getById(Integer id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
        return mapToResponse(transaction);
    }

    public TransactionResponse create(CreateTransactionRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        AccountReceivable accountReceivable = null;
        if (request.getAccountReceivableId() != null) {
            accountReceivable = accountReceivableRepository.findById(request.getAccountReceivableId())
                    .orElseThrow(() -> new RuntimeException("Cuenta por cobrar no encontrada"));
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

    public TransactionResponse update(Integer id, UpdateTransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (request.getValidationStatus() != null) {
            transaction.setValidationStatus(request.getValidationStatus());
        }

        if (request.getReceiptImageUrl() != null) {
            transaction.setReceiptImageUrl(request.getReceiptImageUrl());
        }

        transactionRepository.save(transaction);

        return mapToResponse(transaction);
    }

    public void delete(Integer id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
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
