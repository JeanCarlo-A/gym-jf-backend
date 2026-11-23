package com.gymjf.backend.modules.billing.services;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;
import com.gymjf.backend.modules.billing.domain.AccountReceivable;
import com.gymjf.backend.modules.billing.domain.PaymentMethod;
import com.gymjf.backend.modules.billing.domain.Transaction;
import com.gymjf.backend.modules.billing.domain.TransactionValidationStatus;
import com.gymjf.backend.modules.billing.dtos.CreateTransactionRequest;
import com.gymjf.backend.modules.billing.dtos.RegisterSinpeRequest;
import com.gymjf.backend.modules.billing.dtos.SubmitPaymentRequest;
import com.gymjf.backend.modules.billing.dtos.TransactionResponse;
import com.gymjf.backend.modules.billing.dtos.UpdateTransactionRequest;
import com.gymjf.backend.modules.billing.repositories.AccountReceivableRepository;
import com.gymjf.backend.modules.billing.repositories.TransactionRepository;
import com.gymjf.backend.modules.memberships.domain.SubscriptionStatus;
import com.gymjf.backend.modules.memberships.repositories.UserSubscriptionRepository;
import com.gymjf.backend.shared.domain.dtos.PageResponse;
import com.gymjf.backend.shared.domain.exceptions.BadRequestException;
import com.gymjf.backend.shared.domain.exceptions.ConflictException;
import com.gymjf.backend.shared.domain.exceptions.ResourceNotFoundException;
import com.gymjf.backend.shared.infrastructure.security.CurrentUser;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;
    private final AccountReceivableRepository accountReceivableRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final Clock clock;
    @PersistenceContext
    private EntityManager entityManager;

    /** Listado con filtros opcionales. Un cliente solo ve sus pagos; el administrador ve todos. */
    public PageResponse<TransactionResponse> search(Integer userId, TransactionValidationStatus status,
            PaymentMethod method, LocalDate from, LocalDate to, Pageable pageable) {
        Integer owner = userId;
        if (!currentUser.isAdmin()) {
            if (userId != null) {
                currentUser.requireOwnerOrAdmin(userId);
            }
            owner = currentUser.id();
        }
        LocalDateTime start = from != null ? from.atStartOfDay() : null;
        LocalDateTime end = to != null ? to.plusDays(1).atStartOfDay() : null;
        Page<Transaction> page = transactionRepository.search(owner, status, method, start, end, pageable);
        return PageResponse.from(page, this::mapToResponse);
    }

    public TransactionResponse getById(Integer id) {
        Transaction transaction = findOrThrow(id);
        currentUser.requireOwnerOrAdmin(transaction.getUser().getId());
        return mapToResponse(transaction);
    }

    /** Registro del administrador (efectivo, tarjeta, transferencia...). El efectivo pide doble confirmación. */
    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        User user = findUser(request.getUserId());
        AccountReceivable account = request.getAccountReceivableId() != null
                ? findAccount(request.getAccountReceivableId()) : null;

        if (request.getPaymentMethod() == PaymentMethod.CASH && (request.getConfirmationAmount() == null
                || request.getConfirmationAmount().compareTo(request.getAmount()) != 0)) {
            throw new BadRequestException(
                    "Pago en efectivo: debe confirmar el monto volviéndolo a escribir (confirmationAmount) y debe coincidir");
        }
        TransactionValidationStatus status = request.getValidationStatus() != null
                ? request.getValidationStatus() : TransactionValidationStatus.PENDING;
        return mapToResponse(register(user, request.getAmount(), request.getPaymentMethod(),
                request.getReferenceNumber(), request.getReceiptImageUrl(), status, account));
    }

    /** Pago SINPE del cliente con su comprobante: queda PENDING hasta que el administrador lo apruebe. */
    @Transactional
    public TransactionResponse submit(SubmitPaymentRequest request) {
        AccountReceivable account = findAccount(request.getAccountReceivableId());
        currentUser.requireOwnerOrAdmin(account.getUser().getId());
        User user = findUser(currentUser.id());
        if (!account.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Un administrador debe registrar el pago de otro usuario con POST /transactions/sinpe");
        }
        return mapToResponse(register(user, request.getAmount(), PaymentMethod.MOBILE_PAYMENT,
                request.getReferenceNumber(), request.getReceiptImageUrl(),
                TransactionValidationStatus.PENDING, account));
    }

    /**
     * SINPE que el administrador ya verificó: se registra aprobado de una vez con el procedimiento almacenado
     * sp_procesar_pago_sinpe (pago + saldo + activación de la suscripción en una sola transacción).
     */
    @Transactional
    public TransactionResponse registerSinpe(RegisterSinpeRequest request) {
        User user = findUser(request.getUserId());
        AccountReceivable account = findAccount(request.getAccountReceivableId());
        checkBelongsTo(account, user);
        String reference = normalizeReference(request.getReferenceNumber());
        checkReference(reference);
        checkAmount(account, request.getAmount());

        entityManager.createNativeQuery("CALL sp_procesar_pago_sinpe(:userId, :accountId, :amount, :reference, :receipt, :now)")
                .setParameter("userId", user.getId())
                .setParameter("accountId", account.getId())
                .setParameter("amount", request.getAmount())
                .setParameter("reference", reference)
                .setParameter("receipt", request.getReceiptImageUrl())
                .setParameter("now", LocalDateTime.now(clock))
                .executeUpdate();
        entityManager.refresh(account);
        return mapToResponse(transactionRepository.findByReferenceNumber(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada")));
    }

    @Transactional
    public TransactionResponse approve(Integer id) {
        Transaction transaction = findOrThrow(id);
        if (transaction.getValidationStatus() == TransactionValidationStatus.APPROVED) {
            throw new ConflictException("El pago ya está aprobado");
        }
        AccountReceivable account = transaction.getAccountReceivable();
        if (account != null) {
            entityManager.refresh(account);
            checkAmount(account, transaction.getAmount());
        }
        transaction.setValidationStatus(TransactionValidationStatus.APPROVED);
        transactionRepository.saveAndFlush(transaction);
        afterBalanceChange(transaction);
        return mapToResponse(transaction);
    }

    @Transactional
    public TransactionResponse reject(Integer id) {
        Transaction transaction = findOrThrow(id);
        if (transaction.getValidationStatus() == TransactionValidationStatus.REJECTED) {
            throw new ConflictException("El pago ya está rechazado");
        }
        transaction.setValidationStatus(TransactionValidationStatus.REJECTED);
        transactionRepository.saveAndFlush(transaction);
        afterBalanceChange(transaction); // si estaba aprobado, el trigger devolvió el saldo
        return mapToResponse(transaction);
    }

    @Transactional
    public TransactionResponse update(Integer id, UpdateTransactionRequest request) {
        Transaction transaction = findOrThrow(id);
        if (request.getReceiptImageUrl() != null) {
            transaction.setReceiptImageUrl(request.getReceiptImageUrl());
        }
        transactionRepository.save(transaction);
        return mapToResponse(transaction);
    }

    @Transactional
    public void delete(Integer id) {
        Transaction transaction = findOrThrow(id);
        transactionRepository.delete(transaction);
    }

    // --- Reglas compartidas ---

    private Transaction register(User user, BigDecimal amount, PaymentMethod method, String referenceNumber,
            String receiptUrl, TransactionValidationStatus status, AccountReceivable account) {
        String reference = normalizeReference(referenceNumber);
        checkReference(reference);
        if (account != null) {
            checkBelongsTo(account, user);
            if (status != TransactionValidationStatus.REJECTED) {
                checkAmount(account, amount);
            }
        }
        Transaction transaction = transactionRepository.saveAndFlush(Transaction.builder()
                .user(user)
                .amount(amount)
                .paymentMethod(method)
                .referenceNumber(reference)
                .receiptImageUrl(receiptUrl)
                .validationStatus(status)
                .transactionDate(LocalDateTime.now(clock))
                .accountReceivable(account)
                .build());
        if (status == TransactionValidationStatus.APPROVED) {
            afterBalanceChange(transaction);
        }
        return transaction;
    }

    /** Referencia duplicada (RF-5.1.5): el mismo comprobante no puede usarse dos veces. */
    private void checkReference(String reference) {
        if (reference != null && transactionRepository.existsByReferenceNumber(reference)) {
            throw new ConflictException("La referencia " + reference
                    + " ya fue registrada en otro pago. Es un posible comprobante duplicado: revise antes de continuar");
        }
    }

    /** El pago no puede ser mayor al saldo que queda por pagar. */
    private void checkAmount(AccountReceivable account, BigDecimal amount) {
        if (amount.compareTo(account.getPendingBalance()) > 0) {
            throw new BadRequestException("El monto (₡%s) supera el saldo pendiente de la deuda (₡%s)"
                    .formatted(amount.toPlainString(), account.getPendingBalance().toPlainString()));
        }
    }

    private void checkBelongsTo(AccountReceivable account, User user) {
        if (!account.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("La deuda indicada no pertenece a ese usuario");
        }
    }

    /**
     * Los triggers de MySQL ya actualizaron el saldo de la deuda; aquí se recarga y, si quedó en cero,
     * se activan las suscripciones pendientes ligadas a esa deuda.
     */
    private void afterBalanceChange(Transaction transaction) {
        AccountReceivable account = transaction.getAccountReceivable();
        if (account == null) {
            return;
        }
        entityManager.refresh(account);
        if (account.getPendingBalance().signum() <= 0) {
            userSubscriptionRepository.findByAccountReceivableId(account.getId()).stream()
                    .filter(s -> s.getStatus() == SubscriptionStatus.PENDING)
                    .forEach(s -> {
                        s.setStatus(SubscriptionStatus.ACTIVE);
                        userSubscriptionRepository.save(s);
                    });
        }
    }

    private String normalizeReference(String reference) {
        return reference == null || reference.isBlank() ? null : reference.trim();
    }

    private Transaction findOrThrow(Integer id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada"));
    }

    private User findUser(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private AccountReceivable findAccount(Integer id) {
        return accountReceivableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta por cobrar no encontrada"));
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
