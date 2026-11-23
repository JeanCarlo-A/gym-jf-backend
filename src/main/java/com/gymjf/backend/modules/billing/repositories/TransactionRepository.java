package com.gymjf.backend.modules.billing.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gymjf.backend.modules.billing.domain.PaymentMethod;
import com.gymjf.backend.modules.billing.domain.Transaction;
import com.gymjf.backend.modules.billing.domain.TransactionValidationStatus;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Page<Transaction> findByUserId(Integer userId, Pageable pageable);

    List<Transaction> findByUserId(Integer userId);

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    boolean existsByReferenceNumber(String referenceNumber);

    /** Listado con filtros opcionales (los que vienen null no filtran). {@code to} es exclusivo. */
    @Query("select t from Transaction t where (:userId is null or t.user.id = :userId) "
            + "and (:status is null or t.validationStatus = :status) "
            + "and (:method is null or t.paymentMethod = :method) "
            + "and (:from is null or t.transactionDate >= :from) "
            + "and (:to is null or t.transactionDate < :to)")
    Page<Transaction> search(@Param("userId") Integer userId, @Param("status") TransactionValidationStatus status,
            @Param("method") PaymentMethod method, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
            Pageable pageable);

    /** Por método de pago: [método, cantidad de pagos, monto total] de los pagos con ese estado en el rango. */
    @Query("select t.paymentMethod, count(t), coalesce(sum(t.amount), 0) from Transaction t "
            + "where t.validationStatus = :status and t.transactionDate >= :from and t.transactionDate < :to "
            + "group by t.paymentMethod")
    List<Object[]> totalsByMethod(@Param("status") TransactionValidationStatus status,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countByValidationStatusAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
            TransactionValidationStatus status, LocalDateTime from, LocalDateTime to);

    long countByValidationStatus(TransactionValidationStatus status);
}
